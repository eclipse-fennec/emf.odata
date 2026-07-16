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
package org.eclipse.fennec.odata.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Builder for an OData v4.01 JSON {@code $batch} request. Sub-requests are accumulated and sent as a
 * single {@code POST <root>/$batch} with a {@code {"requests":[…]}} body; {@link #execute()} returns
 * one {@link Result} per sub-request, in the order the server answered them.
 *
 * <p>Convenience methods ({@link #read}, {@link #create}, {@link #update}, {@link #delete}) auto-assign
 * sequential ids; use {@link #add} to supply an explicit id and {@code dependsOn} references (the id of
 * a request that must succeed first — the server short-circuits dependents of a failed request to 424).
 */
public final class ODataBatch {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final ODataClient client;
	private final ArrayNode requests = MAPPER.createArrayNode();
	private int sequence = 0;
	private boolean multipart;

	ODataBatch(ODataClient client) {
		this.client = client;
	}

	/**
	 * Switches this batch to the {@code multipart/mixed} representation — the only batch format
	 * 4.0 services (the SAP world, TripPin) accept; the JSON format is 4.01. Reads become
	 * individual parts; each write (or contiguous same-{@code atomicityGroup} run) becomes a
	 * change set with {@code Content-ID} correlation. {@code dependsOn} has no multipart
	 * equivalent and is ignored.
	 */
	public ODataBatch multipart() {
		this.multipart = true;
		return this;
	}

	/** GET a resource relative to the service root (e.g. {@code "Product"}, {@code "Product('p1')"}). */
	public ODataBatch read(String relativeUrl) {
		return add(nextId(), "GET", relativeUrl, null, List.of());
	}

	/** POST a new entity to an entity set; the entity is encoded with the same codec as a direct create. */
	public ODataBatch create(String setName, EObject entity) {
		return add(nextId(), "POST", setName, encode(entity), List.of());
	}

	/** PATCH an existing entity addressed by its relative URL (e.g. {@code "Product('p1')"}). */
	public ODataBatch update(String entityUrl, EObject entity) {
		return add(nextId(), "PATCH", entityUrl, encode(entity), List.of());
	}

	/** DELETE an entity addressed by its relative URL. */
	public ODataBatch delete(String entityUrl) {
		return add(nextId(), "DELETE", entityUrl, null, List.of());
	}

	/**
	 * Adds a sub-request with an explicit id, verb, relative URL, optional JSON body and
	 * {@code dependsOn} references. Returns {@code this} for chaining.
	 */
	public ODataBatch add(String id, String method, String url, JsonNode body, List<String> dependsOn) {
		return add(id, method, url, body, dependsOn, null);
	}

	/**
	 * As {@link #add(String, String, String, JsonNode, List)} but places the request in an
	 * {@code atomicityGroup}: a contiguous run of same-group requests commits or rolls back as one
	 * change set on transactional backends.
	 */
	public ODataBatch add(String id, String method, String url, JsonNode body, List<String> dependsOn,
			String atomicityGroup) {
		ObjectNode request = MAPPER.createObjectNode();
		request.put("id", id);
		request.put("method", method);
		request.put("url", url);
		if (body != null && !body.isNull()) {
			request.set("body", body);
			ObjectNode headers = MAPPER.createObjectNode();
			headers.put("content-type", "application/json");
			request.set("headers", headers);
		}
		if (dependsOn != null && !dependsOn.isEmpty()) {
			ArrayNode deps = MAPPER.createArrayNode();
			dependsOn.forEach(deps::add);
			request.set("dependsOn", deps);
		}
		if (atomicityGroup != null && !atomicityGroup.isBlank()) {
			request.put("atomicityGroup", atomicityGroup);
		}
		requests.add(request);
		return this;
	}

	/** The id the NEXT convenience call ({@link #read}/{@link #create}/…) will use — handy for {@code dependsOn}. */
	public String nextId() {
		return String.valueOf(sequence++);
	}

	/** Sends the accumulated sub-requests and decodes the {@code responses} envelope. */
	public List<Result> execute() {
		if (multipart) {
			return executeMultipart();
		}
		ObjectNode envelope = MAPPER.createObjectNode();
		envelope.set("requests", requests);
		String body = MAPPER.writeValueAsString(envelope);

		ODataClient.Response response =
				client.exchange("POST", "$batch", "application/json", body, "application/json", Map.of());
		if (response.status() / 100 != 2) {
			throw new ODataClientException("$batch answered " + response.status(),
					response.status(), response.body());
		}

		JsonNode root;
		try {
			root = MAPPER.readTree(response.body());
		} catch (RuntimeException e) {
			// a malformed $batch response must surface as the client's own exception type, not a
			// raw Jackson exception (Jackson 3's JacksonException is unchecked)
			throw new ODataClientException("$batch response is not valid JSON", e);
		}
		JsonNode responses = root.get("responses");
		if (responses == null || !responses.isArray()) {
			throw new ODataClientException("$batch response carries no 'responses' array");
		}
		List<Result> results = new ArrayList<>();
		for (JsonNode node : responses) {
			String id = node.path("id").asString(null);
			int status = node.path("status").asInt(0);
			JsonNode bodyNode = node.get("body");
			String text = bodyNode == null || bodyNode.isNull() ? null
					: bodyNode.isObject() || bodyNode.isArray() ? bodyNode.toString() : bodyNode.asString("");
			results.add(new Result(id, status, text, client));
		}
		return results;
	}

	private JsonNode encode(EObject entity) {
		String json = ODataJsonEncoder.encode(entity, entity.eClass(), client.metadataService());
		return MAPPER.readTree(json);
	}

	// --- multipart/mixed representation ([OData-Protocol] 11.7, the 4.0 batch format) ---

	private List<Result> executeMultipart() {
		String boundary = "batch_fennec_" + Integer.toHexString(System.identityHashCode(this));
		StringBuilder body = new StringBuilder();
		String openGroup = null;
		int changeset = 0;
		for (JsonNode request : requests) {
			String method = request.path("method").asString("GET");
			String group = request.path("atomicityGroup").asString("");
			if ("GET".equals(method)) {
				closeChangeset(body, openGroup, boundary, changeset);
				openGroup = null;
				body.append("--").append(boundary).append("\r\n")
						.append("Content-Type: application/http\r\n")
						.append("Content-Transfer-Encoding: binary\r\n\r\n")
						.append("GET ").append(absolute(request)).append(" HTTP/1.1\r\n")
						.append("Accept: application/json\r\n\r\n\r\n");
				continue;
			}
			// a write: contiguous same-group requests share one change set, otherwise one each
			String effectiveGroup = group.isEmpty() ? "cs-" + request.path("id").asString("") : group;
			if (!effectiveGroup.equals(openGroup)) {
				closeChangeset(body, openGroup, boundary, changeset);
				changeset++;
				body.append("--").append(boundary).append("\r\n")
						.append("Content-Type: multipart/mixed; boundary=")
						.append(changesetBoundary(boundary, changeset)).append("\r\n\r\n");
				openGroup = effectiveGroup;
			}
			body.append("--").append(changesetBoundary(boundary, changeset)).append("\r\n")
					.append("Content-Type: application/http\r\n")
					.append("Content-Transfer-Encoding: binary\r\n")
					.append("Content-ID: ").append(request.path("id").asString("")).append("\r\n\r\n")
					.append(method).append(' ').append(absolute(request)).append(" HTTP/1.1\r\n")
					.append("Accept: application/json\r\n");
			JsonNode payload = request.get("body");
			if (payload != null && !payload.isNull()) {
				body.append("Content-Type: application/json\r\n\r\n")
						.append(payload.toString()).append("\r\n");
			} else {
				body.append("\r\n");
			}
		}
		closeChangeset(body, openGroup, boundary, changeset);
		body.append("--").append(boundary).append("--\r\n");

		ODataClient.Response response = client.exchange("POST", "$batch", "multipart/mixed",
				body.toString(), "multipart/mixed; boundary=" + boundary, Map.of());
		if (response.status() / 100 != 2) {
			throw new ODataClientException("$batch answered " + response.status(),
					response.status(), response.body());
		}
		String responseBoundary = boundaryOf(response.header("Content-Type"));
		if (responseBoundary == null) {
			throw new ODataClientException("the $batch response is not multipart/mixed");
		}
		List<Result> results = new ArrayList<>();
		parseParts(response.body(), responseBoundary, results);
		return results;
	}

	private static void closeChangeset(StringBuilder body, String openGroup, String boundary,
			int changeset) {
		if (openGroup != null) {
			body.append("--").append(changesetBoundary(boundary, changeset)).append("--\r\n");
		}
	}

	private static String changesetBoundary(String batchBoundary, int index) {
		return batchBoundary.replace("batch_", "changeset_") + "_" + index;
	}

	/** Batch part request lines carry absolute URLs — the safest form across 4.0 servers. */
	private String absolute(JsonNode request) {
		return client.rootUri().resolve(request.path("url").asString("")).toString();
	}

	private static String boundaryOf(String contentType) {
		if (contentType == null) {
			return null;
		}
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

	/** Splits a multipart body and collects the application/http parts (recursing into change sets). */
	private void parseParts(String body, String boundary, List<Result> results) {
		String[] parts = body.split("\\r?\\n?--" + Pattern.quote(boundary));
		for (String part : parts) {
			if (part.isBlank() || part.startsWith("--")) {
				continue; // preamble or the closing marker
			}
			int headerEnd = part.indexOf("\r\n\r\n");
			if (headerEnd < 0) {
				headerEnd = part.indexOf("\n\n");
			}
			if (headerEnd < 0) {
				continue;
			}
			String partHeaders = part.substring(0, headerEnd);
			String partBody = part.substring(headerEnd).stripLeading();
			String nested = boundaryOf(partHeaders.replace("\r\n", ";").replace("\n", ";"));
			if (partHeaders.toLowerCase(Locale.ROOT).contains("multipart/mixed")
					&& nested != null) {
				parseParts(partBody, nested, results); // a change set: recurse into its parts
				continue;
			}
			results.add(httpPart(partHeaders, partBody));
		}
	}

	/** Parses one {@code application/http} part: status line, headers (Content-ID), then the body. */
	private Result httpPart(String partHeaders, String content) {
		String id = null;
		for (String line : partHeaders.split("\\r?\\n")) {
			if (line.regionMatches(true, 0, "Content-ID:", 0, 11)) {
				id = line.substring(11).trim();
			}
		}
		String[] lines = content.split("\\r?\\n", -1);
		int status = 0;
		int index = 0;
		for (; index < lines.length; index++) {
			String line = lines[index].trim();
			if (line.startsWith("HTTP/")) {
				String[] words = line.split("\\s+");
				try {
					status = words.length > 1 ? Integer.parseInt(words[1]) : 0;
				} catch (NumberFormatException e) {
					throw new ODataClientException("malformed status line in a multipart batch part", e);
				}
				index++;
				break;
			}
		}
		while (index < lines.length && !lines[index].isBlank()) {
			if (lines[index].regionMatches(true, 0, "Content-ID:", 0, 11)) {
				id = lines[index].substring(11).trim();
			}
			index++; // response headers of the inner HTTP message
		}
		String responseBody = index >= lines.length ? ""
				: String.join("\n", Arrays.asList(lines).subList(index + 1, lines.length)).trim();
		return new Result(id, status, responseBody.isEmpty() ? null : responseBody, client);
	}

	/** One sub-response of a {@code $batch}: its correlation id, HTTP status and raw JSON body. */
	public record Result(String id, int status, String body, ODataClient client) {

		/** {@code true} for a 2xx status. */
		public boolean isSuccess() {
			return status / 100 == 2;
		}

		/** Decodes the body as a single entity of the given type (for GET-one / create responses). */
		public EObject asEntity(EClass entityType) {
			return ODataJsonDecoder.entity(body, entityType, client.metadataService());
		}

		/** Decodes the body as a collection page (for GET-set responses). */
		public ODataPage asPage(EClass entityType) {
			return ODataJsonDecoder.page(body, entityType, client.metadataService());
		}
	}
}
