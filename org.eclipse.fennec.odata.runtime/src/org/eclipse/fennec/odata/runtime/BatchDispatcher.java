/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package org.eclipse.fennec.odata.runtime;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.fennec.odata.persistence.api.WriteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The {@code $batch} arm of {@link ODataServlet} (OData v4.01 JSON batch format, OASIS Part 1
 * §11.7 + JSON batch spec; the multipart/mixed 4.0 wire form is translated into the same
 * request shape). Extracted dispatcher — sub-requests run back through the servlet’s
 * {@code service} dispatch, so every code path behaves exactly as it would top-level.
 */
final class BatchDispatcher {

	private static final System.Logger LOGGER = System.getLogger(BatchDispatcher.class.getName());
	/** Header prefix of a multipart batch part’s correlation id ([OData-Protocol] 11.7.4). */
	private static final String CONTENT_ID_HEADER = "Content-ID:";

	private final ODataServlet servlet;

	BatchDispatcher(ODataServlet servlet) {
		this.servlet = servlet;
	}


/**
 * Executes a JSON {@code $batch} request. Sub-requests are dispatched sequentially back through
 * {@link #service}, each against a synthetic request/response pair, so every code path (query
 * options, writes, functions) behaves exactly as it would for a top-level call.
 *
 * <p>Ordering follows the {@code requests} array; {@code dependsOn} is honored by short-circuiting
 * a request to {@code 424 Failed Dependency} when any predecessor it names failed (status ≥ 400)
 * or was itself short-circuited.
 *
 * <p>{@code atomicityGroup} runs a CONTIGUOUS run of same-group requests inside a transaction on
 * every {@linkplain WriteService#transactional() transactional} write backend: if all members
 * succeed the group commits, otherwise it rolls back and every non-failing member is reported as
 * {@code 424} (all-or-nothing change set). Backends that are not transactional execute the group
 * best-effort (no rollback). Non-contiguous re-use of a group id starts a fresh transaction.
 */
void execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
	response.setHeader("OData-Version", ODataServlet.negotiateVersion(request));
	if (!"POST".equals(request.getMethod())) {
		servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "$batch requires POST");
		return;
	}
	String contentType = request.getContentType();
	boolean multipart = contentType != null
			&& contentType.toLowerCase(Locale.ROOT).contains("multipart/mixed");
	if (contentType == null || (!multipart
			&& !contentType.toLowerCase(Locale.ROOT).contains("application/json"))) {
		servlet.error(response, 415, "only the OData JSON and multipart/mixed batch formats are supported");
		return;
	}

	byte[] body = request.getInputStream().readNBytes(servlet.limits.maxBodyBytes() + 1);
	if (body.length > servlet.limits.maxBodyBytes()) {
		servlet.error(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "batch body too large");
		return;
	}
	JsonNode requests;
	if (multipart) {
		// the 4.0 wire form: translate parts/change sets into the SAME request shape the JSON
		// loop processes (change set N -> atomicityGroup "csN", Content-ID -> id)
		String boundary = multipartBoundary(contentType);
		if (boundary == null) {
			servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "multipart batch without boundary");
			return;
		}
		try {
			requests = parseMultipartBatch(new String(body, StandardCharsets.UTF_8), boundary);
		} catch (IllegalArgumentException e) {
			servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed multipart batch");
			return;
		}
	} else {
		JsonNode root;
		try {
			root = ODataServlet.JSON.readTree(new String(body, StandardCharsets.UTF_8));
		} catch (Exception e) {
			servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed batch body");
			return;
		}
		requests = root.get("requests");
		if (requests == null || !requests.isArray()) {
			servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
					"batch body must carry a \"requests\" array");
			return;
		}
	}

	// DoS guard: bound the number of sub-requests BEFORE executing any of them — a 1 MiB body
	// can still carry thousands of tiny operations, each a full query/write on this one thread
	// ([OData-Protocol] 11.7). Configurable (odata.max.batch.operations); <= 0 disables the cap.
	if (servlet.limits.maxBatchOperations() > 0 && requests.size() > servlet.limits.maxBatchOperations()) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "the batch carries more than the "
				+ "maximum of " + servlet.limits.maxBatchOperations() + " operations");
		return;
	}

	ArrayNode responses = ODataServlet.JSON.createArrayNode();
	Map<String, Integer> statusById = new HashMap<>();
	Set<String> failedIds = new HashSet<>();
	String currentGroup = null;
	List<ObjectNode> groupBuffer = new ArrayList<>();
	boolean groupFailed = false;
	boolean groupOpen = false; // a transactional atomicity group is begun but not yet finalized
	try {
		for (JsonNode sub : requests) {
			String group = sub.path("atomicityGroup").asString(null);
			if (!Objects.equals(group, currentGroup)) {
				finalizeGroup(currentGroup, groupBuffer, groupFailed, responses, statusById, failedIds);
				groupOpen = false;
				groupBuffer = new ArrayList<>();
				groupFailed = false;
				currentGroup = group;
				if (group != null) {
					transactionalWriteServices().forEach(WriteService::begin);
					groupOpen = true;
				}
			}
			ObjectNode result = executeBatchRequest(request, response, sub, statusById, failedIds);
			if (group == null) {
				responses.add(result);
			} else {
				groupBuffer.add(result);
				groupFailed |= result.path("status").asInt(200) >= 400;
			}
		}
		finalizeGroup(currentGroup, groupBuffer, groupFailed, responses, statusById, failedIds);
		groupOpen = false;
	} catch (Exception e) {
		// batch orchestration (commit/rollback, JSON handling) must honour the same sanitized-500
		// contract as every other path — a raw stack trace must never reach the container error
		// page. Any half-open atomicity-group transaction is rolled back so nothing leaks/commits.
		if (groupOpen) {
			rollbackQuietly();
		}
		LOGGER.log(System.Logger.Level.ERROR,
				() -> "unhandled failure serving $batch " + request.getRequestURI(), e);
		servlet.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error");
		return;
	}

	if (multipart) {
		writeMultipartBatchResponse(requests, responses, response);
		return;
	}
	response.setStatus(HttpServletResponse.SC_OK);
	response.setContentType("application/json;charset=UTF-8");
	ObjectNode envelope = ODataServlet.JSON.createObjectNode();
	envelope.set("responses", responses);
	response.getWriter().write(ODataServlet.JSON.writeValueAsString(envelope));
}

/** Best-effort rollback of every transactional write service (guards a half-open batch group). */
private void rollbackQuietly() {
	for (WriteService writeService : transactionalWriteServices()) {
		try {
			writeService.rollback();
		} catch (RuntimeException secondary) {
			LOGGER.log(System.Logger.Level.WARNING,
					() -> "rollback of a failed $batch group also failed", secondary);
		}
	}
}

/** The boundary parameter of a multipart content type, unquoted; null when absent. */
private static String multipartBoundary(String contentType) {
	for (String parameter : contentType.split(";")) {
		String trimmed = parameter.trim();
		if (trimmed.regionMatches(true, 0, "boundary=", 0, 9)) {
			String value = trimmed.substring(9).trim();
			return value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2
					? value.substring(1, value.length() - 1) : value;
		}
	}
	return null;
}

/**
 * Parses a multipart/mixed {@code $batch} body ([OData-Protocol] 11.7, the 4.0 wire form) into
 * the request shape of the JSON loop: every {@code application/http} part becomes one request
 * node (change-set members share an {@code atomicityGroup}, {@code Content-ID} → id); relative
 * and absolute request URLs both reduce to service-root-relative form.
 */
private ArrayNode parseMultipartBatch(String body, String boundary) {
	ArrayNode requests = ODataServlet.JSON.createArrayNode();
	int changeset = 0;
	int generated = 0;
	for (String part : body.split("\\r?\\n?--" + Pattern.quote(boundary))) {
		if (part.isBlank() || part.startsWith("--")) {
			continue;
		}
		int headerEnd = headerEnd(part);
		if (headerEnd < 0) {
			continue;
		}
		String partHeaders = part.substring(0, headerEnd);
		String partBody = part.substring(headerEnd).stripLeading();
		String nested = multipartBoundary(partHeaders.replace("\r\n", ";").replace("\n", ";"));
		if (partHeaders.toLowerCase(Locale.ROOT).contains("multipart/mixed") && nested != null) {
			changeset++;
			String group = "cs" + changeset;
			for (String member : partBody.split("\\r?\\n?--" + Pattern.quote(nested))) {
				if (member.isBlank() || member.startsWith("--")) {
					continue;
				}
				int memberHeaderEnd = headerEnd(member);
				if (memberHeaderEnd < 0) {
					continue;
				}
				requests.add(httpPartRequest(member.substring(0, memberHeaderEnd),
						member.substring(memberHeaderEnd).stripLeading(), group, "g" + generated++));
			}
			continue;
		}
		requests.add(httpPartRequest(partHeaders, partBody, null, "g" + generated++));
	}
	return requests;
}

private static int headerEnd(String part) {
	int end = part.indexOf("\r\n\r\n");
	return end >= 0 ? end : part.indexOf("\n\n");
}

/** One {@code application/http} part → a request node (method, relative url, id, body). */
private ObjectNode httpPartRequest(String partHeaders, String content, String group,
		String fallbackId) {
	ObjectNode node = ODataServlet.JSON.createObjectNode();
	String id = null;
	for (String line : partHeaders.split("\\r?\\n")) {
		if (line.regionMatches(true, 0, CONTENT_ID_HEADER, 0, CONTENT_ID_HEADER.length())) {
			id = line.substring(CONTENT_ID_HEADER.length()).trim();
		}
	}
	String[] lines = content.split("\\r?\\n", -1);
	int index = 0;
	String method = "GET";
	String url = "";
	for (; index < lines.length; index++) {
		String line = lines[index].trim();
		int space = line.indexOf(' ');
		if (space > 0 && line.endsWith("HTTP/1.1")) {
			method = line.substring(0, space);
			url = line.substring(space + 1, line.length() - "HTTP/1.1".length()).trim();
			index++;
			break;
		}
	}
	while (index < lines.length && !lines[index].isBlank()) {
		if (lines[index].regionMatches(true, 0, CONTENT_ID_HEADER, 0, CONTENT_ID_HEADER.length())) {
			id = lines[index].substring(CONTENT_ID_HEADER.length()).trim();
		}
		index++; // inner request headers (Accept, Content-Type, …)
	}
	String requestBody = index >= lines.length ? ""
			: String.join("\n", Arrays.asList(lines)
					.subList(Math.min(index + 1, lines.length), lines.length)).trim();
	if (url.startsWith("http://") || url.startsWith("https://")) {
		// absolute-form request lines: reduce to service-root-relative (keep the query!)
		URI absolute = URI.create(url);
		String path = absolute.getRawPath() == null ? "" : absolute.getRawPath();
		int secondSlash = path.indexOf('/', 1); // "/odata/People" → "People"
		url = (secondSlash >= 0 ? path.substring(secondSlash + 1) : path)
				+ (absolute.getRawQuery() != null ? "?" + absolute.getRawQuery() : "");
	}
	node.put("id", id != null ? id : fallbackId);
	node.put("method", method);
	node.put("url", url);
	if (group != null) {
		node.put("atomicityGroup", group);
	}
	if (!requestBody.isEmpty()) {
		try {
			node.set("body", ODataServlet.JSON.readTree(requestBody));
		} catch (Exception e) {
			throw new IllegalArgumentException("unparseable part body", e);
		}
	}
	return node;
}

/** Serialises the batch results as multipart/mixed — flat parts with Content-ID correlation. */
private void writeMultipartBatchResponse(JsonNode requests, ArrayNode responses,
		HttpServletResponse response) throws IOException {
	String boundary = "batchresponse_" + Integer.toHexString(System.identityHashCode(responses));
	StringBuilder body = new StringBuilder();
	for (JsonNode result : responses) {
		body.append("--").append(boundary).append("\r\n")
				.append("Content-Type: application/http\r\n")
				.append("Content-Transfer-Encoding: binary\r\n");
		String id = result.path("id").asString(null);
		if (id != null && !id.startsWith("g")) { // generated ids are not echoed
			body.append("Content-ID: ").append(id).append("\r\n");
		}
		int status = result.path("status").asInt(200);
		body.append("\r\nHTTP/1.1 ").append(status).append(' ').append("Response").append("\r\n");
		JsonNode resultBody = result.get("body");
		if (resultBody != null && !resultBody.isNull()) {
			body.append("Content-Type: application/json\r\n\r\n")
					.append(resultBody.toString()).append("\r\n");
		} else {
			body.append("\r\n");
		}
	}
	body.append("--").append(boundary).append("--\r\n");
	response.setStatus(HttpServletResponse.SC_OK);
	response.setContentType("multipart/mixed; boundary=" + boundary);
	response.getWriter().write(body.toString());
}

/**
 * Commits or rolls back a finished atomicity group and appends its buffered results. On failure
 * the transaction is rolled back and every non-failing member is rewritten to {@code 424}, so the
 * whole change set is all-or-nothing.
 */
private void finalizeGroup(String group, List<ObjectNode> buffer, boolean failed, ArrayNode responses,
		Map<String, Integer> statusById, Set<String> failedIds) {
	if (group == null) {
		return; // singletons were appended as they ran
	}
	List<WriteService> transactional = transactionalWriteServices();
	if (failed) {
		transactional.forEach(WriteService::rollback);
		for (ObjectNode result : buffer) {
			if (result.path("status").asInt(200) < 400) {
				result.put("status", 424);
				result.set("body", ODataServlet.JSON.readTree(ODataJson.error(424,
						"atomicity group '" + group + "' was rolled back")));
				String id = result.path("id").asString(null);
				if (id != null) {
					statusById.put(id, 424);
					failedIds.add(id);
				}
			}
		}
	} else {
		transactional.forEach(WriteService::commit);
	}
	buffer.forEach(responses::add);
}

private List<WriteService> transactionalWriteServices() {
	return servlet.writeServices.stream().filter(WriteService::transactional).toList();
}

private ObjectNode executeBatchRequest(HttpServletRequest outer, HttpServletResponse outerResponse,
		JsonNode sub, Map<String, Integer> statusById, Set<String> failedIds) throws IOException {
	String id = sub.path("id").asString(null);
	ObjectNode result = ODataServlet.JSON.createObjectNode();
	if (id != null) {
		result.put("id", id);
	}

	JsonNode dependsOn = sub.get("dependsOn");
	if (dependsOn != null && dependsOn.isArray()) {
		for (JsonNode dep : dependsOn) {
			String depId = dep.asString(null);
			if (depId != null && (failedIds.contains(depId)
					|| statusById.getOrDefault(depId, 500) >= 400)) {
				result.put("status", 424);
				result.set("body", ODataServlet.JSON.readTree(ODataJson.error(424,
						"skipped: a request it depends on (" + depId + ") failed")));
				if (id != null) {
					failedIds.add(id);
				}
				return result;
			}
		}
	}

	String method = sub.path("method").asString("GET").toUpperCase(Locale.ROOT);
	String url = sub.path("url").asString("");
	if (url.startsWith("$batch") || url.startsWith("/$batch")) {
		result.put("status", HttpServletResponse.SC_BAD_REQUEST);
		result.set("body", ODataServlet.JSON.readTree(ODataJson.error(400, "nested $batch is not allowed")));
		if (id != null) {
			failedIds.add(id);
		}
		return result;
	}

	byte[] subBody = new byte[0];
	JsonNode bodyNode = sub.get("body");
	if (bodyNode != null && !bodyNode.isNull()) {
		subBody = ODataServlet.JSON.writeValueAsBytes(bodyNode);
	}
	Map<String, String> headers = new LinkedHashMap<>();
	JsonNode headerNode = sub.get("headers");
	if (headerNode != null && headerNode.isObject()) {
		headerNode.properties().forEach(e -> headers.put(e.getKey(), e.getValue().asString("")));
	}

	BatchHttpRequest subRequest = new BatchHttpRequest(outer, method, url, headers, subBody);
	BatchHttpResponse subResponse = new BatchHttpResponse(outerResponse);
	try {
		servlet.service(subRequest, subResponse);
	} catch (Exception e) {
		LOGGER.log(System.Logger.Level.ERROR, () -> "unhandled failure in batch sub-request", e);
		subResponse.reset();
		subResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		subResponse.setContentType("application/json;charset=UTF-8");
		subResponse.getWriter().write(ODataJson.error(500, "internal server error"));
	}
	subResponse.flushBufferQuietly();

	int status = subResponse.status();
	result.put("status", status);
	if (id != null) {
		statusById.put(id, status);
		if (status >= 400) {
			failedIds.add(id);
		}
	}
	if (!subResponse.headers().isEmpty()) {
		ObjectNode responseHeaders = ODataServlet.JSON.createObjectNode();
		subResponse.headers().forEach(responseHeaders::put);
		result.set("headers", responseHeaders);
	}
	byte[] payload = subResponse.body();
	if (payload.length > 0) {
		String responseType = subResponse.headers().getOrDefault("content-type", "");
		String text = new String(payload, StandardCharsets.UTF_8);
		if (responseType.toLowerCase(Locale.ROOT).contains("json")) {
			result.set("body", ODataServlet.JSON.readTree(text));
		} else {
			result.put("body", text);
		}
	}
	return result;
}
}
