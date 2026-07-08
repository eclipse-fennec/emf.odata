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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.fennec.m2x.model.ocl.IntegerLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OclFactory;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.RealLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.model.metadata.api.MetadataService;
import org.eclipse.fennec.odata.codec.json.ODataJsonResourceImpl;
import org.eclipse.fennec.odata.csdl.EcoreToEdmConverter;
import org.eclipse.fennec.odata.csdl.ODataAnnotationConstants;
import org.eclipse.fennec.odata.csdl.OdataResolver;
import org.eclipse.fennec.odata.operation.api.ODataOperationHandler;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.odata.query.CachingODataQueryParser;
import org.eclipse.fennec.odata.query.OclEvaluator;
import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.ODataResourceParser;
import org.eclipse.fennec.odata.query.ResourcePath;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edmx.EdmxFactory;
import org.open.oasis.docs.odata.ns.edmx.TInclude;
import org.open.oasis.docs.odata.ns.edmx.TReference;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;
import org.open.oasis.docs.odata.ns.edmx.TVersion;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import jakarta.servlet.ReadListener;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServlet;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/**
 * Catch-all OData servlet (E6/E7 skeleton, ADR-0001: plain Jakarta Servlet on the OSGi HTTP
 * Whiteboard, no Jakarta REST). v1 surface, read-only:
 * <ul>
 *   <li>{@code GET <root>/} — service document (entity sets as JSON)</li>
 *   <li>{@code GET <root>/$metadata} — CSDL document via the E2 converter, one Schema per
 *       registered EPackage</li>
 *   <li>{@code GET <root>/<EntitySet>?$filter=&$orderby=&$top=&$skip=&$count=} — query via the
 *       E5 {@link QueryService} SPI, OData-JSON response via the E3 codec conventions</li>
 * </ul>
 *
 * <p><b>Security posture:</b> every request runs against hard limits BEFORE parsing —
 * expression length ({@value #DEFAULT_MAX_EXPRESSION_LENGTH}), parenthesis nesting depth
 * ({@value #DEFAULT_MAX_NESTING_DEPTH}, parser-bomb guard) and an enforced {@code $top} ceiling
 * ({@value #DEFAULT_MAX_TOP}, applied even when the client sends none). There is no string
 * concatenation into any backend — the ONLY query path is the typed OCL IR, unknown properties/
 * functions fail the parse (400). Error responses carry sanitized messages, never stack traces
 * or exception class names; unexpected failures answer with a generic 500.
 */
@Component(service = Servlet.class, configurationPid = ODataServlet.PID, property = {
		"osgi.http.whiteboard.servlet.pattern=/odata/*",
		"osgi.http.whiteboard.servlet.name=Fennec OData"
})
public class ODataServlet extends HttpServlet {

	public static final String PID = "org.eclipse.fennec.odata.servlet";
	private static final long serialVersionUID = 1L;

	private static final System.Logger LOGGER = System.getLogger(ODataServlet.class.getName());

	/**
	 * Jackson mapper: immutable-config and thread-safe, so it is created ONCE and shared rather
	 * than per request (Jackson's documented reuse contract).
	 */
	private static final ObjectMapper JSON = new ObjectMapper();

	private final ODataResourceParser resourceParser = new ODataResourceParser();

	private final List<EPackage> packages = new CopyOnWriteArrayList<>();
	private final List<QueryService> queryServices = new CopyOnWriteArrayList<>();
	private final List<WriteService> writeServices = new CopyOnWriteArrayList<>();
	private final List<ODataOperationHandler> operationHandlers = new CopyOnWriteArrayList<>();
	private final CachingODataQueryParser parser = new CachingODataQueryParser();
	private final OclEvaluator expandFilterEvaluator = new OclEvaluator();
	/** Schema namespace/alias per package for cast resolution — same derivation as $metadata. */
	private final Map<EPackage, ODataPackageProfile> profiles =
			java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
	private final EntityShaper shaper = new EntityShaper();

	private volatile MetadataService metadataService;
	private volatile RequestLimits limits = RequestLimits.DEFAULTS;

	@Activate
	void activate(Map<String, Object> configuration) {
		limits = RequestLimits.fromConfiguration(configuration);
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addEPackage(EPackage ePackage) {
		packages.add(ePackage);
	}

	void removeEPackage(EPackage ePackage) {
		packages.remove(ePackage);
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addQueryService(QueryService queryService) {
		queryServices.add(queryService);
	}

	void removeQueryService(QueryService queryService) {
		queryServices.remove(queryService);
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addWriteService(WriteService writeService) {
		writeServices.add(writeService);
	}

	void removeWriteService(WriteService writeService) {
		writeServices.remove(writeService);
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addOperationHandler(ODataOperationHandler handler) {
		operationHandlers.add(handler);
	}

	void removeOperationHandler(ODataOperationHandler handler) {
		operationHandlers.remove(handler);
	}

	@Reference
	void setMetadataService(MetadataService metadataService) {
		this.metadataService = metadataService;
	}

	/** System query options this service implements. */
	private static final Set<String> SUPPORTED_OPTIONS = Set.of(
			"$filter", "$orderby", "$top", "$skip", "$count", "$select", "$expand", "$apply", "$format",
			"$search", "$compute");
	/** Spec-defined options we know but do not implement yet → 501 (conformance 13.1.1/7). */
	private static final Set<String> KNOWN_UNSUPPORTED_OPTIONS = Set.of(
			"$skiptoken", "$deltatoken", "$id", "$index", "$schemaversion", "$levels");

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("OData-Version", negotiateVersion(request));

		String maxVersion = request.getHeader("OData-MaxVersion");
		if (maxVersion != null && !maxVersion.isBlank()
				&& !"4.0".equals(maxVersion.trim()) && !"4.01".equals(maxVersion.trim())) {
			error(response, HttpServletResponse.SC_BAD_REQUEST,
					"unsupported OData-MaxVersion (supported: 4.0, 4.01)");
			return;
		}
		for (String name : request.getParameterMap().keySet()) {
			if (name.startsWith("@")) { // parameter alias value (11.2.5.1.3), consumed at parse time
				continue;
			}
			String normalized = normalizeOption(name);
			if (SUPPORTED_OPTIONS.contains(normalized)) {
				continue;
			}
			if (KNOWN_UNSUPPORTED_OPTIONS.contains(normalized)) { // known but unsupported → 501
				error(response, 501, "system query option '" + ODataJson.sanitize(name)
						+ "' is not implemented");
				return;
			}
			if (name.startsWith("$")) { // not a valid system query option → 400
				error(response, HttpServletResponse.SC_BAD_REQUEST,
						"unknown system query option '" + ODataJson.sanitize(name) + "'");
				return;
			}
			// no $ prefix and no system-option name → custom query option, ignored (11.2.12)
		}

		String path = request.getPathInfo() == null ? "/" : request.getPathInfo();
		try {
			if ("/".equals(path) || path.isEmpty()) {
				serviceDocument(request, response);
			} else if ("/$metadata".equals(path)) {
				metadataDocument(response);
			} else {
				resource(path.substring(1), request, response);
			}
		} catch (ODataQueryParseException | IllegalArgumentException e) {
			// client errors carry the (parser-)message — it never contains internals
			error(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} catch (UnsupportedOperationException e) {
			// pushdown backends refuse loudly instead of answering wrongly (e.g. a JPA
			// backend without a translation for a construct) — an honest 501
			error(response, 501, "the backend does not support this request");
		} catch (Exception e) {
			// no exception details leave the server (no class names, no stack traces) — but the
			// server MUST record what it hid, so an operator can tell a bug from an attack
			LOGGER.log(System.Logger.Level.ERROR,
					() -> "unhandled failure serving GET " + request.getRequestURI(), e);
			error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error");
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws jakarta.servlet.ServletException, IOException {
		String pathInfo = request.getPathInfo() == null ? "/" : request.getPathInfo();
		if ("/$batch".equals(pathInfo)) {
			batch(request, response);
			return;
		}
		switch (request.getMethod()) {
			case "GET" -> super.service(request, response);
			case "POST", "PATCH", "PUT", "DELETE" -> write(request, response);
			default -> {
				response.setHeader("OData-Version", negotiateVersion(request));
				error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
						"method not supported");
			}
		}
	}

	// --- $batch (OData v4.01 JSON batch format, OASIS Part 1 §11.7 + JSON batch spec) ---

	/**
	 * Executes a JSON {@code $batch} request. Sub-requests are dispatched sequentially back through
	 * {@link #service}, each against a synthetic request/response pair, so every code path (query
	 * options, writes, functions) behaves exactly as it would for a top-level call.
	 *
	 * <p>Ordering follows the {@code requests} array; {@code dependsOn} is honored by short-circuiting
	 * a request to {@code 424 Failed Dependency} when any predecessor it names failed (status ≥ 400)
	 * or was itself short-circuited. {@code atomicityGroup} is accepted and its members are treated as
	 * mutually dependent, but there is no cross-request rollback — the {@link WriteService} SPI commits
	 * per call, so a change set is best-effort, not transactional (documented conformance gap).
	 */
	private void batch(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("OData-Version", negotiateVersion(request));
		if (!"POST".equals(request.getMethod())) {
			error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "$batch requires POST");
			return;
		}
		String contentType = request.getContentType();
		if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("application/json")) {
			error(response, 415, "only the OData JSON batch format is supported");
			return;
		}

		byte[] body = request.getInputStream().readNBytes(limits.maxBodyBytes() + 1);
		if (body.length > limits.maxBodyBytes()) {
			error(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "batch body too large");
			return;
		}
		JsonNode root;
		try {
			root = JSON.readTree(new String(body, StandardCharsets.UTF_8));
		} catch (Exception e) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed batch body");
			return;
		}
		JsonNode requests = root.get("requests");
		if (requests == null || !requests.isArray()) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "batch body must carry a \"requests\" array");
			return;
		}

		ArrayNode responses = JSON.createArrayNode();
		Map<String, Integer> statusById = new HashMap<>();
		Set<String> failedIds = new HashSet<>();
		for (JsonNode sub : requests) {
			ObjectNode result = executeBatchRequest(request, response, sub, statusById, failedIds);
			responses.add(result);
		}

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json;charset=UTF-8");
		ObjectNode envelope = JSON.createObjectNode();
		envelope.set("responses", responses);
		response.getWriter().write(JSON.writeValueAsString(envelope));
	}

	private ObjectNode executeBatchRequest(HttpServletRequest outer, HttpServletResponse outerResponse,
			JsonNode sub, Map<String, Integer> statusById, Set<String> failedIds) throws IOException {
		String id = sub.path("id").asString(null);
		ObjectNode result = JSON.createObjectNode();
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
					result.set("body", JSON.readTree(ODataJson.error(424,
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
			result.set("body", JSON.readTree(ODataJson.error(400, "nested $batch is not allowed")));
			if (id != null) {
				failedIds.add(id);
			}
			return result;
		}

		byte[] subBody = new byte[0];
		JsonNode bodyNode = sub.get("body");
		if (bodyNode != null && !bodyNode.isNull()) {
			subBody = JSON.writeValueAsBytes(bodyNode);
		}
		Map<String, String> headers = new LinkedHashMap<>();
		JsonNode headerNode = sub.get("headers");
		if (headerNode != null && headerNode.isObject()) {
			headerNode.properties().forEach(e -> headers.put(e.getKey(), e.getValue().asString("")));
		}

		BatchHttpRequest subRequest = new BatchHttpRequest(outer, method, url, headers, subBody);
		BatchHttpResponse subResponse = new BatchHttpResponse(outerResponse);
		try {
			service(subRequest, subResponse);
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
			ObjectNode responseHeaders = JSON.createObjectNode();
			subResponse.headers().forEach(responseHeaders::put);
			result.set("headers", responseHeaders);
		}
		byte[] payload = subResponse.body();
		if (payload.length > 0) {
			String responseType = subResponse.headers().getOrDefault("content-type", "");
			String text = new String(payload, StandardCharsets.UTF_8);
			if (responseType.toLowerCase(Locale.ROOT).contains("json")) {
				result.set("body", JSON.readTree(text));
			} else {
				result.put("body", text);
			}
		}
		return result;
	}

	/** Synthetic request wrapping one JSON-batch sub-request; delegates everything else to {@code outer}. */
	private static final class BatchHttpRequest extends HttpServletRequestWrapper {
		private final String method;
		private final String pathInfo;
		private final String queryString;
		private final Map<String, String[]> parameters;
		private final Map<String, String> headers; // keys lower-cased
		private final byte[] body;

		BatchHttpRequest(HttpServletRequest outer, String method, String url,
				Map<String, String> headers, byte[] body) {
			super(outer);
			this.method = method;
			this.body = body;
			this.headers = new LinkedHashMap<>();
			headers.forEach((k, v) -> this.headers.put(k.toLowerCase(Locale.ROOT), v));
			String relative = url.startsWith("/") ? url : "/" + url;
			int q = relative.indexOf('?');
			this.pathInfo = q < 0 ? relative : relative.substring(0, q);
			this.queryString = q < 0 ? null : relative.substring(q + 1);
			this.parameters = parseQuery(this.queryString);
		}

		private static Map<String, String[]> parseQuery(String query) {
			Map<String, String[]> map = new LinkedHashMap<>();
			if (query == null || query.isBlank()) {
				return map;
			}
			for (String pair : query.split("&")) {
				if (pair.isEmpty()) {
					continue;
				}
				int eq = pair.indexOf('=');
				String name = eq < 0 ? pair : pair.substring(0, eq);
				String value = eq < 0 ? "" : pair.substring(eq + 1);
				name = java.net.URLDecoder.decode(name, StandardCharsets.UTF_8);
				value = java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
				String[] existing = map.get(name);
				if (existing == null) {
					map.put(name, new String[] { value });
				} else {
					String[] grown = java.util.Arrays.copyOf(existing, existing.length + 1);
					grown[existing.length] = value;
					map.put(name, grown);
				}
			}
			return map;
		}

		@Override
		public String getMethod() {
			return method;
		}

		@Override
		public String getPathInfo() {
			return pathInfo;
		}

		@Override
		public String getRequestURI() {
			String context = getContextPath() == null ? "" : getContextPath();
			String servlet = getServletPath() == null ? "" : getServletPath();
			return context + servlet + pathInfo;
		}

		@Override
		public String getQueryString() {
			return queryString;
		}

		@Override
		public String getParameter(String name) {
			String[] values = parameters.get(name);
			return values == null ? null : values[0];
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return Collections.unmodifiableMap(parameters);
		}

		@Override
		public Enumeration<String> getParameterNames() {
			return Collections.enumeration(parameters.keySet());
		}

		@Override
		public String[] getParameterValues(String name) {
			String[] values = parameters.get(name);
			return values == null ? null : values.clone();
		}

		@Override
		public String getHeader(String name) {
			return headers.get(name.toLowerCase(Locale.ROOT));
		}

		@Override
		public Enumeration<String> getHeaders(String name) {
			String value = headers.get(name.toLowerCase(Locale.ROOT));
			return value == null ? Collections.emptyEnumeration()
					: Collections.enumeration(List.of(value));
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			return Collections.enumeration(headers.keySet());
		}

		@Override
		public String getContentType() {
			return headers.get("content-type");
		}

		@Override
		public int getContentLength() {
			return body.length;
		}

		@Override
		public long getContentLengthLong() {
			return body.length;
		}

		@Override
		public ServletInputStream getInputStream() {
			ByteArrayInputStream source = new ByteArrayInputStream(body);
			return new ServletInputStream() {
				@Override
				public int read() {
					return source.read();
				}

				@Override
				public boolean isFinished() {
					return source.available() == 0;
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setReadListener(ReadListener readListener) {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public BufferedReader getReader() {
			return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body),
					StandardCharsets.UTF_8));
		}
	}

	/** Synthetic response that captures status, headers and body of one batch sub-request. */
	private static final class BatchHttpResponse extends HttpServletResponseWrapper {
		private int status = HttpServletResponse.SC_OK;
		private final Map<String, String> headers = new LinkedHashMap<>(); // keys lower-cased
		private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		private PrintWriter writer;

		BatchHttpResponse(HttpServletResponse outer) {
			super(outer);
		}

		int status() {
			return status;
		}

		Map<String, String> headers() {
			return headers;
		}

		byte[] body() {
			flushBufferQuietly();
			return buffer.toByteArray();
		}

		void flushBufferQuietly() {
			if (writer != null) {
				writer.flush();
			}
		}

		@Override
		public void setStatus(int sc) {
			this.status = sc;
		}

		@Override
		public int getStatus() {
			return status;
		}

		@Override
		public void setContentType(String type) {
			headers.put("content-type", type);
		}

		@Override
		public String getContentType() {
			return headers.get("content-type");
		}

		@Override
		public void setHeader(String name, String value) {
			headers.put(name.toLowerCase(Locale.ROOT), value);
		}

		@Override
		public void addHeader(String name, String value) {
			headers.put(name.toLowerCase(Locale.ROOT), value);
		}

		@Override
		public boolean containsHeader(String name) {
			return headers.containsKey(name.toLowerCase(Locale.ROOT));
		}

		@Override
		public String getHeader(String name) {
			return headers.get(name.toLowerCase(Locale.ROOT));
		}

		@Override
		public void setCharacterEncoding(String charset) {
			// captured buffer is always UTF-8
		}

		@Override
		public PrintWriter getWriter() {
			if (writer == null) {
				writer = new PrintWriter(new java.io.OutputStreamWriter(buffer, StandardCharsets.UTF_8));
			}
			return writer;
		}

		@Override
		public ServletOutputStream getOutputStream() {
			return new ServletOutputStream() {
				@Override
				public void write(int b) {
					buffer.write(b);
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setWriteListener(WriteListener writeListener) {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public void reset() {
			buffer.reset();
			headers.clear();
			status = HttpServletResponse.SC_OK;
			writer = null;
		}
	}

	// --- write path (OASIS "Updatable Service" v1: POST set, PATCH/PUT/DELETE entity) ---

	private void write(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("OData-Version", negotiateVersion(request));
		try {
			dispatchWrite(request, response);
		} catch (WriteConflictException e) {
			error(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
		} catch (ODataQueryParseException | IllegalArgumentException e) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} catch (UnsupportedOperationException e) {
			error(response, 501, "the backend does not support this request");
		} catch (Exception e) {
			// no exception details leave the server (no class names, no stack traces) — but the
			// server MUST record what it hid, so an operator can tell a bug from an attack
			LOGGER.log(System.Logger.Level.ERROR, () -> "unhandled failure serving "
					+ request.getMethod() + " " + request.getRequestURI(), e);
			error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error");
		}
	}

	private void dispatchWrite(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String rawPath = request.getPathInfo() == null ? "/" : request.getPathInfo();
		if ("/".equals(rawPath) || rawPath.startsWith("/$")) {
			error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"this resource is not writable");
			return;
		}
		if ("POST".equals(request.getMethod()) && isActionImport(rawPath.substring(1))) {
			actionImport(rawPath.substring(1), request, response); // POST ActionName, params in the body
			return;
		}
		ResourcePath path;
		try {
			path = resourceParser.parse(rawPath.substring(1));
		} catch (ODataQueryParseException e) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "resource not found");
			return;
		}
		EClass entityType = resolveEntityType(path.entitySet());
		if (entityType == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND,
					"unknown entity set '" + ODataJson.sanitize(path.entitySet()) + "'");
			return;
		}
		WriteService writeService = writeServices.stream()
				.filter(s -> s.supports(entityType)).findFirst().orElse(null);
		if (writeService == null) {
			error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"no writable backend for '" + ODataJson.sanitize(path.entitySet()) + "'");
			return;
		}
		if (!path.segments().isEmpty()) {
			writeBelowEntity(path, entityType, writeService, request, response);
			return;
		}

		switch (request.getMethod()) {
			case "POST" -> {
				if (path.key() != null) {
					error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
							"POST addresses the entity set, not an entity");
					return;
				}
				WritePayload payload = readPayload(request, response, entityType);
				if (payload == null) {
					return; // error already written
				}
				EObject created = writeService.create(entityType, payload.entity());
				if (!payload.bindings().isEmpty()) {
					applyBindings(writeService, entityType,
							rawKeyOf(created, entityType), payload.bindings());
				}
				respondCreated(path.entitySet(), created, entityType, request, response);
			}
			case "PATCH", "PUT" -> {
				if (path.key() == null) {
					error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
							request.getMethod() + " addresses one entity by key");
					return;
				}
				if (!preconditionHolds(entityType, path.key(), request, response)) {
					return; // 428/412 already written
				}
				WritePayload payload = readPayload(request, response, entityType);
				if (payload == null) {
					return; // error already written
				}
				WriteService.WriteResult result = writeService.update(entityType, path.key(),
						payload.entity(), "PUT".equals(request.getMethod()));
				if (!payload.bindings().isEmpty()) {
					applyBindings(writeService, entityType, path.key(), payload.bindings());
				}
				if (result.created()) { // OData upsert (13.1.1/29)
					respondCreated(path.entitySet(), result.entity(), entityType, request, response);
				} else {
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				}
			}
			case "DELETE" -> {
				if (path.key() == null) {
					error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
							"DELETE addresses one entity by key");
					return;
				}
				if (!preconditionHolds(entityType, path.key(), request, response)) {
					return; // 428/412 already written
				}
				if (writeService.delete(entityType, path.key())) {
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				} else {
					error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
				}
			}
			default -> error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"method not supported");
		}
	}

	/**
	 * Writes below the entity level ([OData-Protocol] 11.4): {@code POST Set(key)/nav}
	 * creates a related entity (20), {@code PUT/POST/DELETE …/nav/$ref} manage references
	 * (21/22/25), {@code PATCH/PUT/DELETE Set(key)/prop} write one primitive property (30/31).
	 */
	private void writeBelowEntity(ResourcePath path, EClass entityType, WriteService writeService,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (path.key() == null || path.segments().size() > 2
				|| !(path.segments().get(0) instanceof ResourcePath.PropertySegment property)) {
			error(response, 501, "this write target is not implemented");
			return;
		}
		EStructuralFeature feature = entityType.getEStructuralFeature(property.name());
		if (feature == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND,
					"unknown property '" + ODataJson.sanitize(property.name()) + "'");
			return;
		}
		QueryService reader = queryServices.stream()
				.filter(s -> s.supports(entityType)).findFirst().orElse(null);
		if (reader != null && currentEntity(entityType, path.key()) == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
			return;
		}
		if (path.segments().size() == 2) { // …/nav/$ref  or  …/nav(targetKey)/$ref
			if (!(path.segments().get(1) instanceof ResourcePath.RefSegment)
					|| !(feature instanceof EReference reference)) {
				error(response, 501, "this write target is not implemented");
				return;
			}
			if (property.key() != null) { // 4.01 (13.2.1/19): remove a collection member by key
				if (!"DELETE".equals(request.getMethod()) || !reference.isMany()) {
					error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
							"keyed $ref segments only support DELETE on collections");
					return;
				}
				if (writeService.unlink(entityType, path.key(), reference.getName(), property.key())) {
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				} else {
					error(response, HttpServletResponse.SC_NOT_FOUND, "reference not found");
				}
				return;
			}
			referenceWrite(path, entityType, reference, writeService, request, response);
			return;
		}
		if (property.key() != null) {
			error(response, 501, "this write target is not implemented");
			return;
		}
		if (feature instanceof EReference reference) { // POST Set(key)/nav → create related
			if (!"POST".equals(request.getMethod())) {
				error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
						"only POST creates related entities");
				return;
			}
			WritePayload child = readPayload(request, response, reference.getEReferenceType());
			if (child == null) {
				return; // error already written
			}
			if (!child.bindings().isEmpty()) {
				error(response, 501, "@odata.bind is not supported below the entity level");
				return;
			}
			EObject created = writeService.createRelated(entityType, path.key(),
					reference.getName(), child.entity());
			response.setStatus(HttpServletResponse.SC_CREATED);
			response.setHeader("Location", request.getRequestURI());
			String json = entityJson(created, created.eClass(), null, Set.of());
			response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
			response.getWriter().write(json);
			return;
		}
		propertyWrite(path, entityType, feature, writeService, request, response);
	}

	/** {@code PUT} sets a single-valued, {@code POST} adds to a collection-valued reference. */
	private void referenceWrite(ResourcePath path, EClass entityType, EReference reference,
			WriteService writeService, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		switch (request.getMethod()) {
			case "PUT", "POST" -> {
				if ("PUT".equals(request.getMethod()) == reference.isMany()) {
					error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
							"PUT sets single-valued, POST adds to collection-valued references");
					return;
				}
				String targetKey = refTargetKey(referenceUrlFromBody(request, response),
						reference.getEReferenceType(), response);
				if (targetKey == null) {
					return; // error already written
				}
				writeService.link(entityType, path.key(), reference.getName(), targetKey);
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			}
			case "DELETE" -> {
				String targetKey = null;
				if (reference.isMany()) { // 4.0: DELETE …/$ref?$id=<target url>
					String id = option(request, "$id");
					if (id == null) {
						error(response, HttpServletResponse.SC_BAD_REQUEST,
								"removing a collection reference requires $id");
						return;
					}
					targetKey = refTargetKey(id, reference.getEReferenceType(), response);
					if (targetKey == null) {
						return; // error already written
					}
				}
				if (writeService.unlink(entityType, path.key(), reference.getName(), targetKey)) {
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
				} else {
					error(response, HttpServletResponse.SC_NOT_FOUND, "reference not found");
				}
			}
			default -> error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"unsupported $ref method");
		}
	}

	/**
	 * Single primitive property: {@code PATCH/PUT} with a value document, {@code DELETE} →
	 * null (11.4.9.2). Expressed as a REPLACE of the current state with the one property
	 * changed — a merge payload cannot say "set to null/default" in EMF terms ({@code eIsSet}
	 * would read as absent and the backend would skip it).
	 */
	private void propertyWrite(ResourcePath path, EClass entityType, EStructuralFeature feature,
			WriteService writeService, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (feature.isMany() || !(feature instanceof EAttribute attribute) || attribute.isID()) {
			error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"only single-valued non-key properties are writable");
			return;
		}
		if (!preconditionHolds(entityType, path.key(), request, response)) {
			return; // 428/412 already written
		}
		EObject current = currentEntity(entityType, path.key());
		if (current == null) {
			error(response, 501, "property writes need a read backend for the current state");
			return;
		}
		EObject payload = EcoreUtil.copy(current);
		switch (request.getMethod()) {
			case "PATCH", "PUT" -> {
				JsonNode document = readValueDocument(request, response);
				if (document == null) {
					return; // error already written
				}
				JsonNode value = document.get("value");
				if (value == null) {
					error(response, HttpServletResponse.SC_BAD_REQUEST,
							"property updates carry a {\"value\": …} document");
					return;
				}
				if (value.isNull()) {
					payload.eUnset(attribute); // the replace resets it to the default (null)
				} else {
					payload.eSet(attribute, EcoreUtil.createFromString(
							attribute.getEAttributeType(), value.asString()));
				}
			}
			case "DELETE" -> payload.eUnset(attribute); // the replace resets it to the default
			default -> {
				error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
						"unsupported property method");
				return;
			}
		}
		writeService.update(entityType, path.key(), payload, true);
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	/** The {@code @odata.id} of a {@code $ref} body ({@code {"@odata.id": "…"}}). */
	private String referenceUrlFromBody(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		JsonNode document = readValueDocument(request, response);
		if (document == null) {
			return null;
		}
		JsonNode id = document.get("@odata.id");
		if (id == null || !id.isString()) {
			error(response, HttpServletResponse.SC_BAD_REQUEST,
					"$ref bodies carry {\"@odata.id\": \"…\"}");
			return null;
		}
		return id.asString();
	}

	/** Reads a small JSON document (value/$ref bodies) under the same guards as payloads. */
	private JsonNode readValueDocument(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String contentType = request.getContentType();
		if (contentType == null
				|| !contentType.toLowerCase(Locale.ROOT).startsWith("application/json")) {
			error(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
					"write payloads must be application/json");
			return null;
		}
		byte[] body = request.getInputStream().readNBytes(limits.maxBodyBytes() + 1);
		if (body.length > limits.maxBodyBytes()) {
			error(response, 413, "payload exceeds the maximum size of "
					+ limits.maxBodyBytes() + " bytes");
			return null;
		}
		try {
			return JSON.readTree(body);
		} catch (Exception e) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
			return null;
		}
	}

	/**
	 * Extracts the raw key of a reference-target URL ({@code @odata.id} / {@code $id}) and
	 * validates that the addressed set matches the navigation's target type. Writes the error
	 * response and returns null when the URL does not identify a matching entity.
	 */
	private String refTargetKey(String url, EClass targetType, HttpServletResponse response)
			throws IOException {
		if (url == null) {
			return null; // error already written
		}
		String tail = url.substring(url.lastIndexOf('/') + 1);
		ResourcePath ref;
		try {
			ref = resourceParser.parse(tail);
		} catch (ODataQueryParseException e) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "invalid reference target URL");
			return null;
		}
		EClass set = resolveEntityType(ref.entitySet());
		if (ref.key() == null || !ref.segments().isEmpty() || set == null
				|| !(targetType.isSuperTypeOf(set) || set.isSuperTypeOf(targetType))) {
			error(response, HttpServletResponse.SC_BAD_REQUEST,
					"the reference target does not address an entity of the navigation's type");
			return null;
		}
		return ref.key();
	}

	// --- optimistic concurrency (13.1.1/26): weak ETags from the serialized state ---

	/**
	 * Enforces {@code If-Match} on updates/deletes of EXISTING entities: our single-entity
	 * GETs return an ETag, so clients must send the precondition (11.4.1.1) — absent → 428,
	 * mismatch → 412. Upserts of absent entities pass. Without a read backend the check is
	 * skipped (no ETag was ever served).
	 */
	private boolean preconditionHolds(EClass entityType, String rawKey,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		EObject current = currentEntity(entityType, rawKey);
		if (current == null) {
			return true; // nothing exists — upsert path, no precondition to check
		}
		String ifMatch = request.getHeader("If-Match");
		if (ifMatch == null || ifMatch.isBlank()) {
			error(response, 428, "If-Match is required — this resource carries an ETag");
			return false;
		}
		if ("*".equals(ifMatch.trim())) {
			return true;
		}
		if (!ifMatch.trim().equals(etagOf(current, entityType))) {
			error(response, HttpServletResponse.SC_PRECONDITION_FAILED,
					"the entity changed since it was read");
			return false;
		}
		return true;
	}

	/** Weak ETag over the full serialized entity — stable per state, cheap to recompute. */
	private String etagOf(EObject entity, EClass entityType) throws IOException {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(
					entityJson(entity, entityType, null, Set.of())
							.getBytes(StandardCharsets.UTF_8));
			return "W/\"" + HexFormat.of().formatHex(digest, 0, 8) + "\"";
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}

	/** The current entity by key, or null when absent or no read backend serves the type. */
	private EObject currentEntity(EClass entityType, String rawKey) {
		QueryService queryService = queryServices.stream()
				.filter(s -> s.supports(entityType)).findFirst().orElse(null);
		EAttribute keyAttribute = entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
		if (queryService == null || keyAttribute == null) {
			return null; // no read backend serves this type — no ETag was ever issued, skip the check
		}
		// A backend FAILURE must NOT be silently degraded to "entity absent": that would skip the
		// If-Match precondition and let a PATCH/PUT/DELETE overwrite a concurrently-changed entity
		// exactly when the backend is flaky. Let it propagate (→ logged 500); only a genuinely
		// empty result means "not found".
		QueryResult result = queryService.execute(new EntityQuery(entityType, null,
				keyEquals(keyAttribute, rawKey), List.of(), 0, 1, false));
		return result.entities().isEmpty() ? null : result.entities().get(0);
	}

	/** A decoded write payload: the entity plus {@code @odata.bind} targets per navigation. */
	private record WritePayload(EObject entity, Map<EReference, List<String>> bindings) {}

	private static final byte[] ODATA_BIND_MARKER = "@odata.bind".getBytes(StandardCharsets.US_ASCII);

	/**
	 * Whether {@code haystack} contains the ASCII {@code needle} — a byte scan that avoids
	 * allocating a full {@code String} copy of the (up to {@code maxBodyBytes}) payload just to
	 * probe for the rare {@code @odata.bind} marker.
	 */
	private static boolean containsAscii(byte[] haystack, byte[] needle) {
		if (needle.length == 0 || haystack.length < needle.length) {
			return needle.length == 0;
		}
		int last = haystack.length - needle.length;
		outer:
		for (int i = 0; i <= last; i++) {
			for (int j = 0; j < needle.length; j++) {
				if (haystack[i + j] != needle[j]) {
					continue outer;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Reads and decodes the JSON payload into an EObject of the addressed type — the codec
	 * leaves exactly the transmitted features set ({@code eIsSet} = "was in the payload").
	 * {@code "nav@odata.bind"} members ([OData-JSON] 8.5 / [OData-Protocol] 11.4.2.1) are
	 * extracted BEFORE decoding and returned as raw target keys per navigation.
	 * Writes the error response and returns null for media-type, size and syntax violations.
	 */
	private WritePayload readPayload(HttpServletRequest request, HttpServletResponse response,
			EClass entityType) throws IOException {
		String contentType = request.getContentType();
		if (contentType == null
				|| !contentType.toLowerCase(Locale.ROOT).startsWith("application/json")) {
			error(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
					"write payloads must be application/json");
			return null;
		}
		byte[] body = request.getInputStream().readNBytes(limits.maxBodyBytes() + 1);
		if (body.length > limits.maxBodyBytes()) {
			error(response, 413, "payload exceeds the maximum size of "
					+ limits.maxBodyBytes() + " bytes");
			return null;
		}
		if (body.length == 0) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "empty payload");
			return null;
		}
		Map<EReference, List<String>> bindings = new LinkedHashMap<>();
		if (containsAscii(body, ODATA_BIND_MARKER)) {
			body = extractBindings(body, entityType, bindings, response);
			if (body == null) {
				return null; // error already written
			}
		}
		ODataJsonResourceImpl resource = new ODataJsonResourceImpl(
				URI.createURI("request.odatajson"), metadataService);
		Map<Object, Object> options = new HashMap<>();
		options.put(CodecResource.CODEC_ROOT_TYPE, entityType);
		try {
			resource.load(new java.io.ByteArrayInputStream(body), options);
		} catch (Exception e) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
			return null;
		}
		if (resource.getContents().isEmpty()
				|| !(resource.getContents().get(0) instanceof EObject entity)) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
			return null;
		}
		return new WritePayload(entity, bindings);
	}

	/**
	 * Pulls {@code "nav@odata.bind"} members out of the payload: validates the navigation and
	 * the target URLs, fills {@code bindings} and returns the body WITHOUT the bind members
	 * (the codec only sees plain features). Null after a written error response.
	 */
	private byte[] extractBindings(byte[] body, EClass entityType,
			Map<EReference, List<String>> bindings, HttpServletResponse response)
			throws IOException {
		JsonNode document;
		try {
			document = JSON.readTree(body);
		} catch (Exception e) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
			return null;
		}
		if (!(document instanceof ObjectNode object)) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
			return null;
		}
		List<String> bindMembers = new ArrayList<>();
		object.propertyStream().map(Map.Entry::getKey)
				.filter(name -> name.endsWith("@odata.bind")).forEach(bindMembers::add);
		for (String member : bindMembers) {
			String navigationName = member.substring(0, member.length() - "@odata.bind".length());
			if (!(entityType.getEStructuralFeature(navigationName) instanceof EReference reference)) {
				error(response, HttpServletResponse.SC_BAD_REQUEST, "'"
						+ ODataJson.sanitize(navigationName) + "' is not a navigation property");
				return null;
			}
			JsonNode value = object.get(member);
			List<String> targets = new ArrayList<>();
			if (value.isArray() && reference.isMany()) {
				for (JsonNode element : value) {
					if (!element.isString()) {
						error(response, HttpServletResponse.SC_BAD_REQUEST,
								"@odata.bind targets must be entity URLs");
						return null;
					}
					String key = refTargetKey(element.asString(),
							reference.getEReferenceType(), response);
					if (key == null) {
						return null; // error already written
					}
					targets.add(key);
				}
			} else if (value.isString() && !reference.isMany()) {
				String key = refTargetKey(value.asString(), reference.getEReferenceType(), response);
				if (key == null) {
					return null; // error already written
				}
				targets.add(key);
			} else {
				error(response, HttpServletResponse.SC_BAD_REQUEST,
						"@odata.bind takes a single entity URL for single-valued and"
								+ " an array of entity URLs for collection-valued navigations");
				return null;
			}
			bindings.put(reference, targets);
			object.remove(member);
		}
		return JSON.writeValueAsBytes(object);
	}

	/** Applies {@code @odata.bind} targets as reference operations after the entity write. */
	private void applyBindings(WriteService writeService, EClass entityType, String rawKey,
			Map<EReference, List<String>> bindings) {
		for (Map.Entry<EReference, List<String>> binding : bindings.entrySet()) {
			for (String targetKey : binding.getValue()) {
				writeService.link(entityType, rawKey, binding.getKey().getName(), targetKey);
			}
		}
	}

	/** The entity's raw key literal (as it would appear in its edit URL), or null. */
	private static String rawKeyOf(EObject entity, EClass entityType) {
		EAttribute id = entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
		return id == null ? null : urlKeyLiteral(id, entity.eGet(id));
	}

	/** 201 with Location/OData-EntityId (the entity's edit URL) and the created entity body. */
	private void respondCreated(String setName, EObject entity, EClass entityType,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		EAttribute id = entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
		String editUrl = contextRoot(request) + "/" + setName
				+ (id == null ? "" : "(" + urlKeyLiteral(id, entity.eGet(id)) + ")");
		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setHeader("Location", editUrl);
		response.setHeader("OData-EntityId", editUrl);
		String json = entityJson(entity, entityType, null, Set.of());
		String context = "\"@odata.context\":\"" + contextRoot(request) + "/$metadata#" + setName
				+ "/$entity\",";
		response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
		response.getWriter().write("{" + context + json.substring(1));
	}

	/** URL form of a key value: quoted (with {@code ''} escape) for strings, raw otherwise. */
	private static String urlKeyLiteral(EAttribute id, Object value) {
		String text = String.valueOf(value);
		if (id.getEAttributeType() != null
				&& String.class.equals(id.getEAttributeType().getInstanceClass())) {
			return "'" + encodeControlChars(text.replace("'", "''")) + "'";
		}
		return encodeControlChars(text);
	}

	/**
	 * Percent-encodes ISO control characters (incl. CR/LF) so a persisted key value cannot inject
	 * line breaks into the {@code Location}/{@code OData-EntityId} response headers (HTTP response
	 * splitting). Printable key values pass through unchanged.
	 */
	private static String encodeControlChars(String text) {
		StringBuilder encoded = null;
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c < 0x20 || c == 0x7F) {
				if (encoded == null) {
					encoded = new StringBuilder(text.length() + 8).append(text, 0, i);
				}
				encoded.append('%').append(HexFormat.of().withUpperCase().toHexDigits((byte) c));
			} else if (encoded != null) {
				encoded.append(c);
			}
		}
		return encoded == null ? text : encoded.toString();
	}

	/** The response is 4.01 unless the client pins {@code OData-MaxVersion: 4.0} (8.1.5). */
	private static String negotiateVersion(HttpServletRequest request) {
		String maxVersion = request.getHeader("OData-MaxVersion");
		return maxVersion != null && "4.0".equals(maxVersion.trim()) ? "4.0" : "4.01";
	}

	// --- routes ---

	private void serviceDocument(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String sets = entitySetNames().stream()
				.map(name -> "{\"name\":\"" + name + "\",\"kind\":\"EntitySet\",\"url\":\"" + name + "\"}")
				.collect(Collectors.joining(","));
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"@odata.context\":\"" + request.getRequestURI()
				+ "/$metadata\",\"value\":[" + sets + "]}");
	}

	private void metadataDocument(HttpServletResponse response) throws Exception {
		EcoreToEdmConverter converter = new EcoreToEdmConverter();
		EdmxRoot root = EdmxFactory.eINSTANCE.createEdmxRoot();
		TEdmx edmx = EdmxFactory.eINSTANCE.createTEdmx();
		edmx.setVersion(TVersion._40);
		// the served protocol versions (Core.ODataVersions, 13.1.2 SHOULD) and the capability
		// self-description (Capabilities, 13.2.1/13 SHOULD) need their vocabulary references —
		// they must precede DataServices in the edmx element sequence
		edmx.getReference().add(vocabularyReference("Org.OData.Core.V1", "Core"));
		edmx.getReference().add(vocabularyReference("Org.OData.Capabilities.V1", "Capabilities"));
		TDataServices dataServices = EdmxFactory.eINSTANCE.createTDataServices();
		for (EPackage pkg : packages) { // one Schema per registered package (req §3.3 composition)
			SchemaType schema = converter.toSchema(pkg);
			for (TEntityContainer container : schema.getEntityContainer()) {
				AnnotationType versions = EdmFactory.eINSTANCE.createAnnotationType();
				versions.setTerm("Org.OData.Core.V1.ODataVersions");
				versions.setString1("4.0 4.01");
				container.getAnnotation().add(versions);
				// what this v1 read-only service can and cannot do (12/13.2.1 advertisement)
				AnnotationType conformance = EdmFactory.eINSTANCE.createAnnotationType();
				conformance.setTerm("Org.OData.Capabilities.V1.ConformanceLevel");
				conformance.setEnumMember1(List.of("Org.OData.Capabilities.V1.ConformanceLevelType/Minimal"));
				container.getAnnotation().add(conformance);
				container.getAnnotation().add(
						boolCapability("Org.OData.Capabilities.V1.BatchSupported", true));
				container.getAnnotation().add(
						boolCapability("Org.OData.Capabilities.V1.AsynchronousRequestsSupported", false));
				container.getAnnotation().add(
						boolCapability("Org.OData.Capabilities.V1.KeyAsSegmentSupported", false));
			}
			dataServices.getSchema().add(schema);
		}
		edmx.setDataServices(dataServices);
		root.setEdmx(edmx);

		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
		rs.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
		rs.getPackageRegistry().put(EdmxPackage.eNS_URI, EdmxPackage.eINSTANCE);
		Resource resource = rs.createResource(URI.createURI("metadata.xml"));
		resource.getContents().add(root);
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
		options.put(XMLResource.OPTION_ENCODING, "UTF-8");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, options);

		response.setContentType("application/xml;charset=UTF-8");
		response.getWriter().write(out.toString(StandardCharsets.UTF_8));
	}

	/** {@code edmx:Reference} to an OASIS vocabulary, so its terms are resolvable for clients. */
	private static TReference vocabularyReference(String namespace, String alias) {
		TInclude include = EdmxFactory.eINSTANCE.createTInclude();
		include.setNamespace(namespace);
		include.setAlias(alias);
		TReference reference = EdmxFactory.eINSTANCE.createTReference();
		reference.setUri("https://oasis-tcs.github.io/odata-vocabularies/vocabularies/" + namespace + ".xml");
		reference.getInclude().add(include);
		return reference;
	}

	private static AnnotationType boolCapability(String term, boolean value) {
		AnnotationType annotation = EdmFactory.eINSTANCE.createAnnotationType();
		annotation.setTerm(term);
		annotation.setBool1(value);
		return annotation;
	}

	private record Target(EClass entityType, QueryService queryService) {
	}

	private Target resolveTarget(String setName, HttpServletResponse response) throws IOException {
		EClass entityType = resolveEntityType(setName);
		if (entityType == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "unknown entity set '" + ODataJson.sanitize(setName) + "'");
			return null;
		}
		QueryService queryService = queryServices.stream()
				.filter(s -> s.supports(entityType)).findFirst().orElse(null);
		if (queryService == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "no backend for entity set '" + ODataJson.sanitize(setName) + "'");
			return null;
		}
		return new Target(entityType, queryService);
	}

	private void entitySet(String setName, String castName, EClass castType,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		Target target = resolveTarget(setName, response);
		if (target == null) {
			return;
		}
		if (option(request, "$apply") != null) {
			if (castType != null) { // aggregation over a cast collection: honest 501 over wrong rows
				error(response, 501, "$apply on type-cast collections is not implemented");
				return;
			}
			apply(setName, target, request, response);
			return;
		}
		// a cast makes the DERIVED type the context: its properties are addressable in options
		EClass context = castType != null ? castType : target.entityType();
		boolean xml = wantsXml(request);
		SelectTree select = selectOption(request, context);
		Map<String, OclExpression> expand = expandOption(request, context);
		Map<String, String> aliases = parameterAliases(request);

		List<OrderBySegment> orderBy = parseChecked(option(request, "$orderby"),
				value -> aliases.isEmpty() ? parser.parseOrderBy(value, context)
						: parser.parseOrderBy(value, context, aliases));
		int skip = limits.effectiveSkip(option(request, "$skip"));
		int top = pageSize(request, response, limits.effectiveTop(option(request, "$top")));
		// peek one row beyond the page: partial results MUST carry @odata.nextLink (13.1.1/3)
		EntityQuery query = new EntityQuery(target.entityType(), castType,
				parseChecked(filterWithSearch(request, context),
						filter -> aliases.isEmpty() ? parser.parseFilter(filter, context)
								: parser.parseFilter(filter, context, aliases)),
				orderBy == null ? List.of() : orderBy,
				skip, top + 1,
				"true".equals(option(request, "$count")),
				expand.keySet()); // backends prefetch expanded navigations (no N+1, no lazy proxies)

		QueryResult result = target.queryService().execute(query);
		boolean hasMore = result.entities().size() > top;
		List<EObject> page = hasMore ? result.entities().subList(0, top) : result.entities();

		if (xml) { // XMI is a non-OData projection — trimmed, but without an embedded link
			List<EObject> copies = shaper.shapeAll(page, context, select, expand.keySet());
			copies.forEach(copy -> applyNestedFilters(copy, expand));
			writeXmi(response, copies);
			return;
		}
		StringBuilder json = new StringBuilder("{\"@odata.context\":\"")
				.append(contextRoot(request)).append("/$metadata#").append(setName)
				.append(castName != null ? "/" + castName : "").append('"');
		if (result.totalCount() >= 0) {
			json.append(",\"@odata.count\":").append(result.totalCount());
		}
		List<ComputeExpression> computes = computeExpressions(request, context);
		json.append(",\"value\":[");
		for (int i = 0; i < page.size(); i++) {
			if (i > 0) {
				json.append(',');
			}
			json.append(withComputed(entityJson(page.get(i), context, select, expand),
					page.get(i), computes));
		}
		json.append(']');
		if (hasMore) {
			json.append(",\"@odata.nextLink\":\"")
					.append(ODataJson.sanitize(nextLink(request, skip + top))).append('"');
		}
		json.append('}');
		response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
		response.getWriter().write(json.toString());
	}

	/** The follow-up page URL: same request with {@code $skip} advanced past this page. */
	private String nextLink(HttpServletRequest request, int nextSkip) {
		StringBuilder link = new StringBuilder(request.getRequestURI()).append("?$skip=").append(nextSkip);
		for (String option : SUPPORTED_OPTIONS) {
			String value = option(request, option);
			if (value != null && !"$skip".equals(option)) {
				link.append('&').append(option).append('=')
						.append(java.net.URLEncoder.encode(value, StandardCharsets.UTF_8));
			}
		}
		return link.toString();
	}

	/** Dispatches a parsed resource path: set, set/$count, keyed entity, navigation walk. */
	private void resource(String rawPath, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (isFunctionCall(rawPath)) {
			functionImport(rawPath, request, response); // GET FuncName(p=…) — the resource parser
			return;                                      // deliberately does not model function segments
		}
		if (isBoundFunctionCall(rawPath)) {
			boundFunction(rawPath, request, response); // GET Set(key)/Ns.Func(p=…)
			return;
		}
		ResourcePath path;
		try {
			path = resourceParser.parse(rawPath);
		} catch (ODataQueryParseException e) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "resource not found");
			return;
		}
		if (path.key() == null && path.segments().isEmpty()) {
			entitySet(path.entitySet(), null, null, request, response);
			return;
		}
		if (path.key() == null) {
			if (path.segments().get(0) instanceof ResourcePath.TypeCastSegment cast) {
				castOnSet(path, cast, request, response); // Set/Ns.T… ([OData-URL] 4.11)
			} else if (path.segments().size() == 1
					&& path.segments().get(0) instanceof ResourcePath.CountSegment) {
				setCount(path.entitySet(), null, request, response); // Set/$count (with optional $filter)
			} else {
				error(response, HttpServletResponse.SC_NOT_FOUND,
						"navigation requires an entity key");
			}
			return;
		}
		Target target = resolveTarget(path.entitySet(), response);
		if (target == null) {
			return;
		}
		EObject entity = fetchByKey(target, path.key(),
				path.segments().isEmpty() ? expandOption(request, target.entityType()).keySet()
						: walkPrefetch(target.entityType(), path.segments()),
				response);
		if (entity == null) {
			return; // error already written
		}
		if (path.segments().isEmpty()) {
			singleEntity(path.entitySet(), entity, target.entityType(), request, response);
		} else {
			walk(path, entity, request, response);
		}
	}

	/** {@code GET Set/$count}: the (optionally filtered, optionally cast) total as text/plain. */
	private void setCount(String setName, EClass castType, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Target target = resolveTarget(setName, response);
		if (target == null) {
			return;
		}
		EClass context = castType != null ? castType : target.entityType();
		QueryResult result = target.queryService().execute(new EntityQuery(target.entityType(),
				castType,
				parseChecked(option(request, "$filter"),
						filter -> parser.parseFilter(filter, context)),
				List.of(), 0, 0, true));
		response.setContentType("text/plain;charset=UTF-8");
		response.getWriter().write(String.valueOf(result.totalCount()));
	}

	// --- function/action invocation (unbound function imports, GET) ---

	/** A single-segment path {@code Name()} / {@code Name(p=…)} — a function/action import call. */
	private static boolean isFunctionCall(String rawPath) {
		if (rawPath.indexOf('/') >= 0 || !rawPath.endsWith(")")) {
			return false;
		}
		int paren = rawPath.indexOf('(');
		if (paren <= 0) {
			return false;
		}
		return isFunctionArgs(rawPath.substring(paren + 1, rawPath.length() - 1));
	}

	/** A multi-segment path whose LAST segment is a function call — a bound function invocation. */
	private static boolean isBoundFunctionCall(String rawPath) {
		int lastSlash = rawPath.lastIndexOf('/');
		if (lastSlash < 0 || !rawPath.endsWith(")")) {
			return false;
		}
		String segment = rawPath.substring(lastSlash + 1);
		int paren = segment.indexOf('(');
		return paren > 0 && isFunctionArgs(segment.substring(paren + 1, segment.length() - 1));
	}

	/** Function arguments distinguish a call from an entity key: named params (or none). */
	private static boolean isFunctionArgs(String inside) {
		return inside.isBlank() || inside.matches("\\s*[A-Za-z_]\\w*\\s*=.*");
	}

	/** Invokes a bound function {@code Set(key)/Ns.Func(p=…)} on the addressed entity. */
	private void boundFunction(String rawPath, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		int lastSlash = rawPath.lastIndexOf('/');
		String prefix = rawPath.substring(0, lastSlash);
		String segment = rawPath.substring(lastSlash + 1);
		int paren = segment.indexOf('(');
		String qualified = segment.substring(0, paren);
		String localName = qualified.contains(".")
				? qualified.substring(qualified.lastIndexOf('.') + 1) : qualified;
		String parameterList = segment.substring(paren + 1, segment.length() - 1);

		ResourcePath path;
		try {
			path = resourceParser.parse(prefix);
		} catch (ODataQueryParseException e) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "resource not found");
			return;
		}
		if (path.key() == null || !path.segments().isEmpty()) {
			error(response, HttpServletResponse.SC_NOT_FOUND,
					"a bound function is invoked on a keyed entity");
			return;
		}
		Target target = resolveTarget(path.entitySet(), response);
		if (target == null) {
			return;
		}
		EObject entity = fetchByKey(target, path.key(), Set.of(), response);
		if (entity == null) {
			return; // error already written
		}
		EOperation operation = entity.eClass().getEAllOperations().stream()
				.filter(op -> op.getName().equals(localName) && !isUnbound(op)).findFirst().orElse(null);
		if (operation == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "no bound function '" + localName + "'");
			return;
		}
		String qualifiedName = operationNamespace(target.entityType()) + "." + localName;
		ODataOperationHandler handler = operationHandlers.stream()
				.filter(h -> h.handles(qualifiedName)).findFirst().orElse(null);
		if (handler == null) {
			error(response, 501, "no handler for the operation");
			return;
		}
		Object result = handler.invoke(operation, entity, functionParameters(parameterList, operation));
		writeFunctionResult(result, request, response);
	}

	private String operationNamespace(EClass entityType) {
		return profiles.computeIfAbsent(entityType.getEPackage(), p -> new OdataResolver().resolve(p))
				.getNamespace();
	}

	/** A bare name (no key, no nav) that is an unbound operation rather than an entity set. */
	private boolean isActionImport(String segment) {
		return segment.indexOf('/') < 0 && segment.indexOf('(') < 0
				&& resolveEntityType(segment) == null && resolveUnboundFunction(segment) != null;
	}

	/** Invokes an unbound action import: {@code POST ActionName} with the parameters in the body. */
	private void actionImport(String name, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		UnboundOperation resolved = resolveUnboundFunction(name);
		Map<String, Object> parameters = readActionParameters(request, resolved.operation(), response);
		if (parameters == null) {
			return; // error already written
		}
		ODataOperationHandler handler = operationHandlers.stream()
				.filter(h -> h.handles(resolved.qualifiedName())).findFirst().orElse(null);
		if (handler == null) {
			error(response, 501, "no handler for the operation");
			return;
		}
		writeFunctionResult(handler.invoke(resolved.operation(), null, parameters), request, response);
	}

	/** Reads action parameters from the JSON request body, coerced to the operation's parameter types. */
	private Map<String, Object> readActionParameters(HttpServletRequest request, EOperation operation,
			HttpServletResponse response) throws IOException {
		byte[] body = request.getInputStream().readNBytes(limits.maxBodyBytes() + 1);
		if (body.length > limits.maxBodyBytes()) {
			error(response, 413, "payload exceeds the maximum size of " + limits.maxBodyBytes() + " bytes");
			return null;
		}
		Map<String, Object> parameters = new LinkedHashMap<>();
		if (body.length == 0) {
			return parameters; // a parameterless action
		}
		JsonNode node;
		try {
			node = JSON.readTree(body);
		} catch (Exception e) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
			return null;
		}
		if (!(node instanceof ObjectNode object)) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "action parameters must be a JSON object");
			return null;
		}
		for (EParameter parameter : operation.getEParameters()) {
			JsonNode value = object.get(parameter.getName());
			if (value != null && !value.isNull()) {
				parameters.put(parameter.getName(), parameter.getEType() instanceof EDataType dataType
						? EcoreUtil.createFromString(dataType, value.asString())
						: value.asString());
			}
		}
		return parameters;
	}

	/** Invokes an unbound function import: resolve the operation, coerce params, dispatch, serialize. */
	private void functionImport(String rawPath, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		int paren = rawPath.indexOf('(');
		String name = rawPath.substring(0, paren);
		String parameterList = rawPath.substring(paren + 1, rawPath.length() - 1);
		UnboundOperation resolved = resolveUnboundFunction(name);
		if (resolved == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "no function import '" + name + "'");
			return;
		}
		Map<String, Object> parameters = functionParameters(parameterList, resolved.operation());
		ODataOperationHandler handler = operationHandlers.stream()
				.filter(h -> h.handles(resolved.qualifiedName())).findFirst().orElse(null);
		if (handler == null) {
			error(response, 501, "no handler for the operation");
			return;
		}
		Object result = handler.invoke(resolved.operation(), null, parameters);
		writeFunctionResult(result, request, response);
	}

	/** An unbound operation plus its namespace-qualified name (the handler dispatch key). */
	private record UnboundOperation(EOperation operation, String qualifiedName) {
	}

	/** Finds an unbound ({@code @OData.Bound=false}) operation with the given name across the models. */
	private UnboundOperation resolveUnboundFunction(String name) {
		for (EPackage pkg : packages) {
			ODataPackageProfile profile = profiles.computeIfAbsent(pkg,
					p -> new OdataResolver().resolve(p));
			for (EClassifier classifier : pkg.getEClassifiers()) {
				if (classifier instanceof EClass eClass) {
					for (EOperation operation : eClass.getEAllOperations()) {
						if (operation.getName().equals(name) && isUnbound(operation)) {
							return new UnboundOperation(operation, profile.getNamespace() + "." + name);
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean isUnbound(EOperation operation) {
		EAnnotation annotation = operation.getEAnnotation(ODataAnnotationConstants.SOURCE);
		return annotation != null
				&& "false".equals(annotation.getDetails().get(ODataAnnotationConstants.BOUND));
	}

	private static Map<String, Object> functionParameters(String parameterList, EOperation operation) {
		Map<String, Object> parameters = new LinkedHashMap<>();
		if (parameterList.isBlank()) {
			return parameters;
		}
		for (String part : parameterList.split(",")) {
			int equals = part.indexOf('=');
			if (equals < 0) {
				throw new ODataQueryParseException("function parameter must be name=value: " + part);
			}
			String parameterName = part.substring(0, equals).trim();
			String raw = part.substring(equals + 1).trim();
			EParameter parameter = operation.getEParameters().stream()
					.filter(p -> p.getName().equals(parameterName)).findFirst()
					.orElseThrow(() -> new ODataQueryParseException(
							"unknown parameter '" + parameterName + "'"));
			parameters.put(parameterName, coerceParameter(raw, parameter));
		}
		return parameters;
	}

	private static Object coerceParameter(String raw, EParameter parameter) {
		String literal = raw.length() >= 2 && raw.startsWith("'") && raw.endsWith("'")
				? raw.substring(1, raw.length() - 1).replace("''", "'")
				: raw;
		if (parameter.getEType() instanceof EDataType dataType) {
			return EcoreUtil.createFromString(dataType, literal);
		}
		return literal;
	}

	/** Serializes a function/action result: void (204), a single entity, a collection, or a value. */
	private void writeFunctionResult(Object result, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (result == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return;
		}
		response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
		if (result instanceof EObject entity) {
			String json = entityJson(entity, entity.eClass(), null, Set.of());
			String context = "\"@odata.context\":\"" + contextRoot(request) + "/$metadata#"
					+ entity.eClass().getName() + "/$entity\",";
			response.getWriter().write("{" + context + json.substring(1));
			return;
		}
		if (result instanceof java.util.Collection<?> collection) {
			StringBuilder body = new StringBuilder("{\"value\":[");
			boolean first = true;
			for (Object element : collection) {
				if (!(element instanceof EObject entity)) {
					throw new ODataQueryParseException("a collection function result must hold entities");
				}
				body.append(first ? "" : ",").append(entityJson(entity, entity.eClass(), null, Set.of()));
				first = false;
			}
			response.getWriter().write(body.append("]}").toString());
			return;
		}
		response.getWriter().write("{\"value\":" + JSON.writeValueAsString(result) + "}");
	}

	/**
	 * Set-level derived-type cast ([OData-URL] 4.11): {@code Set/Ns.T} restricts the collection
	 * to instances of the derived type (and makes it the context type for query options),
	 * {@code Set/Ns.T(key)} addresses one instance and 404s when the entity is not of that
	 * type, both continue into {@code /$count} or a navigation walk.
	 */
	private void castOnSet(ResourcePath path, ResourcePath.TypeCastSegment cast,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		Target target = resolveTarget(path.entitySet(), response);
		if (target == null) {
			return;
		}
		EClass castType = resolveCastType(cast.qualifiedName(), target.entityType());
		if (castType == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "no type '"
					+ ODataJson.sanitize(cast.qualifiedName()) + "' derived from the entity set's type");
			return;
		}
		List<ResourcePath.Segment> rest = path.segments().subList(1, path.segments().size());
		if (cast.key() != null) {
			EObject entity = fetchByKey(target, cast.key(),
					rest.isEmpty() ? expandOption(request, castType).keySet()
							: walkPrefetch(castType, rest),
					response);
			if (entity == null) {
				return; // error already written
			}
			if (!castType.isInstance(entity)) {
				error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
				return;
			}
			if (rest.isEmpty()) {
				singleEntity(path.entitySet() + "/" + cast.qualifiedName(), entity, castType,
						request, response);
			} else {
				walk(new ResourcePath(path.entitySet(), cast.key(), rest), entity, request, response);
			}
			return;
		}
		if (rest.isEmpty()) {
			entitySet(path.entitySet(), cast.qualifiedName(), castType, request, response);
			return;
		}
		if (rest.size() == 1 && rest.get(0) instanceof ResourcePath.CountSegment) {
			setCount(path.entitySet(), castType, request, response);
			return;
		}
		error(response, HttpServletResponse.SC_NOT_FOUND, "navigation requires an entity key");
	}

	/**
	 * The navigation prefix of a walked resource path as one slash-separated expand path —
	 * the backend prefetches/materializes it, so the walk never touches lazy references
	 * after the backend's session closed. Cast segments switch the context type; the first
	 * non-navigation segment ends the prefix (nothing to prefetch beyond it).
	 */
	private Set<String> walkPrefetch(EClass type, List<ResourcePath.Segment> segments) {
		StringBuilder path = new StringBuilder();
		EClass current = type;
		for (ResourcePath.Segment segment : segments) {
			if (segment instanceof ResourcePath.TypeCastSegment cast) {
				EClass castType = resolveCastType(cast.qualifiedName(), null);
				if (castType == null) {
					break; // the walk itself will answer 404
				}
				current = castType;
				continue;
			}
			if (!(segment instanceof ResourcePath.PropertySegment property)
					|| !(current.getEStructuralFeature(property.name()) instanceof EReference reference)) {
				break;
			}
			path.append(path.isEmpty() ? "" : "/").append(property.name());
			current = reference.getEReferenceType();
		}
		return path.isEmpty() ? Set.of() : Set.of(path.toString());
	}

	/**
	 * Resolves a {@code Ns.Type} (or {@code Alias.Type}) cast segment against the registered
	 * models; when {@code baseType} is given, the resolved class must derive from it (or be
	 * it). Namespace derivation matches {@code $metadata} (profile-driven).
	 */
	private EClass resolveCastType(String qualifiedName, EClass baseType) {
		int dot = qualifiedName.lastIndexOf('.');
		if (dot < 0) {
			return null; // an unqualified cast name resolves to no type (→ 404), never crashes
		}
		String namespace = qualifiedName.substring(0, dot);
		String localName = qualifiedName.substring(dot + 1);
		for (EPackage pkg : packages) {
			ODataPackageProfile profile = profiles.computeIfAbsent(pkg,
					p -> new OdataResolver().resolve(p));
			if (!namespace.equals(profile.getNamespace()) && !namespace.equals(profile.getAlias())) {
				continue;
			}
			if (pkg.getEClassifier(localName) instanceof EClass cast
					&& (baseType == null || baseType.isSuperTypeOf(cast))) {
				return cast;
			}
		}
		return null;
	}

	/** Loads one entity by raw key literal; writes the error response when absent/keyless. */
	private EObject fetchByKey(Target target, String rawKey, Set<String> expand,
			HttpServletResponse response) throws IOException {
		EAttribute keyAttribute = target.entityType().getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
		if (keyAttribute == null) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "entity set has no key");
			return null;
		}
		QueryResult result = target.queryService().execute(new EntityQuery(target.entityType(),
				null, keyEquals(keyAttribute, rawKey), List.of(), 0, 1, false, expand));
		if (result.entities().isEmpty()) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
			return null;
		}
		return result.entities().get(0);
	}

	/**
	 * Walks navigation/property segments on a loaded entity ([OData-URL] resource paths,
	 * ADR-0005). Terminals: entity/complex → JSON object, collection → JSON array,
	 * primitive → value document, {@code /$value} → raw text, {@code /$count} → count,
	 * null → 204. Query options on navigation paths are not implemented yet (501, except
	 * {@code $format}).
	 */
	private void walk(ResourcePath path, EObject root, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		// on a navigation path we apply $filter/$orderby/$top/$skip/$count to a terminal collection
		// (13.1.2 SHOULD); other options ($select/$expand/$apply/$search/$compute) stay 501 here
		for (String option : SUPPORTED_OPTIONS) {
			boolean handledOnNav = Set.of("$format", "$filter", "$orderby", "$top", "$skip", "$count")
					.contains(option);
			if (!handledOnNav && option(request, option) != null) {
				error(response, 501, "this query option is not implemented on navigation paths");
				return;
			}
		}
		Object current = root;
		for (int i = 0; i < path.segments().size(); i++) {
			ResourcePath.Segment segment = path.segments().get(i);
			boolean last = i == path.segments().size() - 1;
			switch (segment) {
				case ResourcePath.PropertySegment property -> {
					if (!(current instanceof EObject object)) {
						error(response, HttpServletResponse.SC_NOT_FOUND, "resource not found");
						return;
					}
					EStructuralFeature feature = object.eClass().getEStructuralFeature(property.name());
					if (feature == null) {
						error(response, HttpServletResponse.SC_NOT_FOUND,
								"unknown segment '" + ODataJson.sanitize(property.name()) + "'");
						return;
					}
					current = object.eGet(feature);
					if (property.key() != null) {
						current = selectByKey(current, property.key());
						if (current == null) {
							error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
							return;
						}
					}
				}
				case ResourcePath.TypeCastSegment cast -> {
					EClass castClass = resolveCastType(cast.qualifiedName(), null);
					if (castClass == null) {
						error(response, HttpServletResponse.SC_NOT_FOUND, "unknown type '"
								+ ODataJson.sanitize(cast.qualifiedName()) + "'");
						return;
					}
					if (current instanceof List<?> collection) { // collection cast: type filter
						current = collection.stream().filter(castClass::isInstance).toList();
						if (cast.key() != null) {
							current = selectByKey(current, cast.key());
							if (current == null) {
								error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
								return;
							}
						}
					} else if (current instanceof EObject object && cast.key() == null) {
						if (!castClass.isInstance(object)) { // single-entity cast: instance check
							error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
							return;
						}
					} else {
						error(response, HttpServletResponse.SC_NOT_FOUND, "resource not found");
						return;
					}
				}
				case ResourcePath.CountSegment count -> {
					if (!last || !(current instanceof List<?> collection)) {
						error(response, HttpServletResponse.SC_BAD_REQUEST,
								"$count requires a terminal collection");
						return;
					}
					response.setContentType("text/plain;charset=UTF-8");
					response.getWriter().write(String.valueOf(filterCollection(collection, request).size()));
					return;
				}
				case ResourcePath.ValueSegment value -> {
					// enum values are EObjects in dynamic EMF but primitive for OData purposes
					boolean structured = (current instanceof EObject && !(current instanceof Enumerator))
							|| current instanceof List<?>;
					if (!last || structured) {
						error(response, HttpServletResponse.SC_BAD_REQUEST,
								"$value requires a terminal primitive property");
						return;
					}
					if (current == null) {
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						return;
					}
					response.setContentType("text/plain;charset=UTF-8");
					response.getWriter().write(current instanceof Enumerator literal
							? literal.getLiteral() : String.valueOf(current));
					return;
				}
				case ResourcePath.RefSegment ref -> {
					error(response, 501, "$ref is not implemented");
					return;
				}
			}
		}
		if (current instanceof List<?> collection) { // terminal nav collection: $filter/$orderby/$skip/$top
			current = pageCollection(orderCollection(filterCollection(collection, request), request), request);
		}
		writeWalkedValue(path, current, request, response);
	}

	/** Applies {@code $filter} to a walked entity collection (in-memory; already materialized). */
	private List<?> filterCollection(List<?> collection, HttpServletRequest request) {
		String filter = option(request, "$filter");
		if (filter == null || filter.isBlank() || collection.isEmpty()
				|| !(collection.get(0) instanceof EObject sample)) {
			return collection;
		}
		limits.checkExpression(filter);
		OclExpression predicate = parser.parseFilter(filter, sample.eClass());
		List<Object> filtered = new ArrayList<>();
		for (Object item : collection) {
			if (item instanceof EObject entity && expandFilterEvaluator.matchesNullSafe(predicate, entity)) {
				filtered.add(entity);
			}
		}
		return filtered;
	}

	/** Applies {@code $orderby} to a walked entity collection (in-memory; multi-key, null-safe). */
	private List<?> orderCollection(List<?> collection, HttpServletRequest request) {
		String orderby = option(request, "$orderby");
		if (orderby == null || orderby.isBlank() || collection.isEmpty()
				|| !(collection.get(0) instanceof EObject sample)) {
			return collection;
		}
		limits.checkExpression(orderby);
		List<OrderBySegment> segments = parser.parseOrderBy(orderby, sample.eClass());
		Comparator<Object> comparator = null;
		for (OrderBySegment segment : segments) {
			Comparator<Object> byKey = (a, b) -> compareValues(
					expandFilterEvaluator.evaluate(segment.expression(), a),
					expandFilterEvaluator.evaluate(segment.expression(), b));
			if (!segment.ascending()) {
				byKey = byKey.reversed();
			}
			comparator = comparator == null ? byKey : comparator.thenComparing(byKey);
		}
		List<Object> sorted = new ArrayList<>(collection);
		sorted.sort(comparator);
		return sorted;
	}

	/** OData ordering of two evaluated sort keys: null sorts before any value (ascending). */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int compareValues(Object x, Object y) {
		if (x == null && y == null) {
			return 0;
		}
		if (x == null) {
			return -1;
		}
		if (y == null) {
			return 1;
		}
		if (x instanceof Enumerator ex && y instanceof Enumerator ey) {
			return ex.getLiteral().compareTo(ey.getLiteral());
		}
		if (x instanceof Comparable && x.getClass().isInstance(y)) {
			return ((Comparable) x).compareTo(y);
		}
		return String.valueOf(x).compareTo(String.valueOf(y));
	}

	/** Applies {@code $skip}/{@code $top} to a walked collection. */
	private List<?> pageCollection(List<?> collection, HttpServletRequest request) {
		int from = Math.min(limits.effectiveSkip(option(request, "$skip")), collection.size());
		List<?> skipped = collection.subList(from, collection.size());
		String top = option(request, "$top");
		return top == null ? skipped
				: skipped.subList(0, Math.min(limits.effectiveTop(top), skipped.size()));
	}

	private Object selectByKey(Object collection, String rawKey) {
		if (!(collection instanceof List<?> members)) {
			return null;
		}
		Object key = literalValue(rawKey);
		for (Object member : members) {
			if (member instanceof EObject object) {
				EAttribute id = object.eClass().getEAllAttributes().stream()
						.filter(EAttribute::isID).findFirst().orElse(null);
				if (id != null && String.valueOf(object.eGet(id)).equals(String.valueOf(key))) {
					return object;
				}
			}
		}
		return null;
	}

	private void writeWalkedValue(ResourcePath path, Object value, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (value == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return;
		}
		String context = contextRoot(request) + "/$metadata#" + path.entitySet();
		if (value instanceof Enumerator literal) { // enum property → value document with the literal
			response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
			response.getWriter().write("{\"@odata.context\":\"" + ODataJson.sanitize(context)
					+ "\",\"value\":\"" + ODataJson.sanitize(literal.getLiteral()) + "\"}");
			return;
		}
		if (value instanceof EObject object) {
			if (wantsXml(request)) {
				writeXmi(response, shaper.shapeAll(List.of(object), object.eClass(), null, Set.of()));
				return;
			}
			String json = entityJson(object, object.eClass(), null, Set.of());
			response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
			response.getWriter().write("{\"@odata.context\":\"" + ODataJson.sanitize(context)
					+ "\"," + json.substring(1));
			return;
		}
		if (value instanceof List<?> collection) {
			if (wantsXml(request)) {
				List<EObject> objects = collection.stream()
						.filter(EObject.class::isInstance).map(EObject.class::cast).toList();
				writeXmi(response, objects.isEmpty() ? List.of()
						: shaper.shapeAll(objects, objects.get(0).eClass(), null, Set.of()));
				return;
			}
			StringBuilder json = new StringBuilder("{\"@odata.context\":\"")
					.append(ODataJson.sanitize(context)).append("\",\"value\":[");
			boolean first = true;
			for (Object member : collection) {
				if (!first) {
					json.append(',');
				}
				first = false;
				if (member instanceof EObject object) {
					json.append(entityJson(object, object.eClass(), null, Set.of()));
				} else {
					ODataJson.value(json, member);
				}
			}
			json.append("]}");
			response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
			response.getWriter().write(json.toString());
			return;
		}
		StringBuilder json = new StringBuilder("{\"@odata.context\":\"")
				.append(ODataJson.sanitize(context)).append("\",\"value\":");
		ODataJson.value(json, value instanceof java.util.Date date
				? java.time.format.DateTimeFormatter.ISO_INSTANT.format(date.toInstant()) : value);
		json.append('}');
		response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
		response.getWriter().write(json.toString());
	}

	/** {@code /{Set}({key})}: the key becomes a typed equality AST — never parsed as expression. */
	private void singleEntity(String setName, EObject entity, EClass entityType,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		// optimistic concurrency (13.1.1/26): the served ETag is the write preconditions' ETag
		response.setHeader("ETag", etagOf(entity, entityType));
		SelectTree select = selectOption(request, entityType);
		Map<String, OclExpression> expand = expandOption(request, entityType);
		if (wantsXml(request)) {
			List<EObject> copies = shaper.shapeAll(List.of(entity), entityType, select,
					expand.keySet());
			copies.forEach(copy -> applyNestedFilters(copy, expand));
			writeXmi(response, copies);
			return;
		}
		String json = entityJson(entity, entityType, select, expand);
		// weave the context annotation into the entity object (single entities have no envelope)
		String context = "\"@odata.context\":\"" + contextRoot(request) + "/$metadata#" + setName
				+ "/$entity\",";
		response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
		response.getWriter().write("{" + context + json.substring(1));
	}


	/** The key as a typed equality AST ({@code id = <literal>}) — never expression-parsed. */
	private static OperationCallExp keyEquals(EAttribute keyAttribute, String rawKey) {
		OperationCallExp keyFilter = OclFactory.eINSTANCE.createOperationCallExp();
		keyFilter.setName("=");
		PropertyCallExp keyProperty = OclFactory.eINSTANCE.createPropertyCallExp();
		keyProperty.setReferredProperty(keyAttribute);
		keyProperty.setIsImplicit(true);
		keyFilter.setOwnedSource(keyProperty);
		keyFilter.getOwnedArguments().add(keyLiteral(rawKey));
		return keyFilter;
	}

	/** Raw key literal → plain Java value (for member selection in walked collections). */
	private static Object literalValue(String rawKey) {
		if (rawKey.length() >= 2 && rawKey.startsWith("'") && rawKey.endsWith("'")) {
			return rawKey.substring(1, rawKey.length() - 1).replace("''", "'");
		}
		return rawKey;
	}

	/** OData key literal: {@code 'text'} (with {@code ''} escape) or a plain number. */
	private static OclExpression keyLiteral(String rawKey) {
		if (rawKey.length() >= 2 && rawKey.startsWith("'") && rawKey.endsWith("'")) {
			StringLiteralExp literal = OclFactory.eINSTANCE.createStringLiteralExp();
			literal.setStringSymbol(rawKey.substring(1, rawKey.length() - 1).replace("''", "'"));
			return literal;
		}
		if (rawKey.matches("-?\\d+")) {
			IntegerLiteralExp literal = OclFactory.eINSTANCE.createIntegerLiteralExp();
			literal.setIntegerSymbol(Long.parseLong(rawKey));
			return literal;
		}
		if (rawKey.matches("-?\\d+\\.\\d+")) {
			RealLiteralExp literal = OclFactory.eINSTANCE.createRealLiteralExp();
			literal.setRealSymbol(Double.parseDouble(rawKey));
			return literal;
		}
		throw new ODataQueryParseException("invalid key literal");
	}

	private void apply(String setName, Target target, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		for (String incompatible : List.of("$select", "$expand")) { // rows are not entities
			if (option(request, incompatible) != null) {
				throw new ODataQueryParseException(
						"combining $apply with " + incompatible + " is not supported");
			}
		}
		if (wantsXml(request)) {
			throw new ODataQueryParseException("$apply results are JSON-only");
		}
		ApplyPipeline pipeline = parseChecked(option(request, "$apply"),
				value -> parser.parseApply(value, target.entityType()));
		Map<String, String> aliases = parameterAliases(request);
		// $filter/$orderby run AFTER the pipeline (OASIS) — the pipeline aliases are in scope
		List<OrderBySegment> orderBy = parseChecked(option(request, "$orderby"),
				value -> parser.parseOrderByAfterApply(value, target.entityType(), pipeline, aliases));
		int skip = limits.effectiveSkip(option(request, "$skip"));
		int top = pageSize(request, response, limits.effectiveTop(option(request, "$top")));
		ApplyQuery query = new ApplyQuery(target.entityType(), pipeline,
				parseChecked(option(request, "$filter"),
						value -> parser.parseFilterAfterApply(value, target.entityType(), pipeline, aliases)),
				orderBy == null ? List.of() : orderBy,
				skip, top + 1, // peek: partial results MUST carry @odata.nextLink
				"true".equals(option(request, "$count")));

		ApplyResult result;
		try {
			result = target.queryService().executeApply(query);
		} catch (UnsupportedOperationException e) {
			error(response, 501, "the backend of '" + ODataJson.sanitize(setName) + "' does not support $apply");
			return;
		}
		boolean hasMore = result.rows().size() > top;
		List<Map<String, Object>> rows = hasMore ? result.rows().subList(0, top) : result.rows();
		StringBuilder json = new StringBuilder("{\"@odata.context\":\"")
				.append(contextRoot(request)).append("/$metadata#").append(setName).append('"');
		if (result.totalCount() >= 0) {
			json.append(",\"@odata.count\":").append(result.totalCount());
		}
		json.append(",\"value\":[");
		for (int i = 0; i < rows.size(); i++) {
			if (i > 0) {
				json.append(',');
			}
			ODataJson.value(json, rows.get(i));
		}
		json.append(']');
		if (hasMore) {
			json.append(",\"@odata.nextLink\":\"")
					.append(ODataJson.sanitize(nextLink(request, skip + top))).append('"');
		}
		json.append('}');
		response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
		response.getWriter().write(json.toString());
	}

	// --- helpers ---

	/**
	 * Resolves a system query option: since OData 4.01 option names are case-INsensitive and
	 * the {@code $} prefix is optional ([OData-Protocol] 8.2.7 / conformance 13.1.2), so
	 * {@code ?FILTER=…} and {@code ?$filter=…} are the same option.
	 */
	private static String option(HttpServletRequest request, String canonical) {
		String direct = request.getParameter(canonical);
		if (direct != null) {
			return direct;
		}
		for (String name : request.getParameterMap().keySet()) {
			if (canonical.equals(normalizeOption(name))) {
				return request.getParameter(name);
			}
		}
		return null;
	}

	/** Canonical form of a query-option name: lower-case with a {@code $} prefix. */
	private static String normalizeOption(String name) {
		String normalized = name.toLowerCase(Locale.ROOT);
		return normalized.startsWith("$") ? normalized : "$" + normalized;
	}

	/**
	 * 4.01 parameter aliases (11.2.5.1.3): every {@code @name} query parameter, keyed as sent.
	 * Values are expression texts and get parsed on use — so they pass the SAME pre-parse
	 * limits as {@code $filter} itself (hostile-input guard).
	 */
	private Map<String, String> parameterAliases(HttpServletRequest request) {
		Map<String, String> aliases = new HashMap<>();
		for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
			if (parameter.getKey().startsWith("@") && parameter.getValue().length > 0) {
				limits.checkExpression(parameter.getValue()[0]);
				aliases.put(parameter.getKey(), parameter.getValue()[0]);
			}
		}
		return aliases;
	}

	/**
	 * Applies {@code Prefer: odata.maxpagesize} (4.01 8.2.8.7, prefix optional per 13.2.1/4):
	 * caps the page below the {@code $top}/ceiling value and echoes {@code Preference-Applied}.
	 */
	private static int pageSize(HttpServletRequest request, HttpServletResponse response, int top) {
		String prefer = request.getHeader("Prefer");
		if (prefer == null) {
			return top;
		}
		for (String preference : prefer.split(",")) {
			String[] nameValue = preference.trim().split("=", 2);
			String name = nameValue[0].trim().toLowerCase(Locale.ROOT);
			if (!"odata.maxpagesize".equals(name) && !"maxpagesize".equals(name)) {
				continue;
			}
			try {
				int maxPageSize = Integer.parseInt(nameValue.length > 1 ? nameValue[1].trim() : "");
				if (maxPageSize > 0 && maxPageSize < top) {
					response.setHeader("Preference-Applied", "odata.maxpagesize=" + maxPageSize);
					return maxPageSize;
				}
			} catch (NumberFormatException e) {
				// preferences are hints — a malformed value is ignored, not an error
			}
		}
		return top;
	}

	/**
	 * The effective {@code $filter}, folding in {@code $search} (13.1.2 SHOULD): a free-text search
	 * becomes {@code contains(prop,'term')} OR-ed over the type's string properties, AND-ed with any
	 * {@code $filter}. It thus rides the existing typed-IR pushdown — no backend change, both backends.
	 */
	private String filterWithSearch(HttpServletRequest request, EClass context) {
		String search = option(request, "$search");
		String filter = option(request, "$filter");
		if (search == null || search.isBlank()) {
			return filter;
		}
		String literal = "'" + search.trim().replace("'", "''") + "'";
		String searchExpr = context.getEAllAttributes().stream()
				.filter(a -> !a.isMany() && a.getEAttributeType() != null
						&& String.class.equals(a.getEAttributeType().getInstanceClass()))
				.map(a -> "contains(" + a.getName() + "," + literal + ")")
				.reduce((l, r) -> l + " or " + r)
				.orElse("false"); // no string properties → matches nothing
		return filter == null || filter.isBlank() ? searchExpr
				: "(" + filter + ") and (" + searchExpr + ")";
	}

	/**
	 * {@code $compute} expressions (13.1.2 SHOULD): parsed by reusing the {@code $apply}
	 * {@code compute(…)} grammar. The computed members are added to the response by the servlet
	 * (evaluated per entity), so it is backend-agnostic.
	 */
	private List<ComputeExpression> computeExpressions(HttpServletRequest request, EClass context) {
		String compute = option(request, "$compute");
		if (compute == null || compute.isBlank()) {
			return List.of();
		}
		limits.checkExpression(compute);
		ApplyPipeline pipeline = parser.parseApply("compute(" + compute + ")", context);
		return ((ComputeTransformation) pipeline.getTransformations().get(0)).getComputeExpressions();
	}

	/** Splices the {@code $compute} members (evaluated against the entity) into its JSON object. */
	private String withComputed(String entityJson, EObject entity, List<ComputeExpression> computes) {
		if (computes.isEmpty()) {
			return entityJson;
		}
		StringBuilder members = new StringBuilder();
		for (ComputeExpression compute : computes) {
			Object value = expandFilterEvaluator.evaluate(compute.getExpression(), entity);
			try {
				members.append(",\"").append(compute.getAlias()).append("\":")
						.append(JSON.writeValueAsString(value));
			} catch (Exception e) {
				throw new ODataQueryParseException("could not serialize $compute '" + compute.getAlias() + "'");
			}
		}
		String inner = entityJson.substring(1, entityJson.length() - 1);
		return "{" + (inner.isEmpty() ? members.substring(1) : inner + members) + "}";
	}

	private <T> T parseChecked(String expression, java.util.function.Function<String, T> parse) {
		if (expression == null || expression.isBlank()) {
			return null;
		}
		limits.checkExpression(expression);
		return parse.apply(expression);
	}

	private EClass resolveEntityType(String setName) {
		for (EPackage pkg : packages) {
			if (pkg.getEClassifier(setName) instanceof EClass eClass && !eClass.isAbstract()) {
				return eClass;
			}
		}
		return null;
	}

	private List<String> entitySetNames() {
		return packages.stream()
				.flatMap(pkg -> pkg.getEClassifiers().stream())
				.filter(EClass.class::isInstance).map(EClass.class::cast)
				.filter(c -> !c.isAbstract())
				.map(EClass::getName).sorted().toList();
	}

	// --- $select / $expand / formats ---

	/** Validated {@code $select} tree (nested selects incl., 4.01), or null when absent. */
	private SelectTree selectOption(HttpServletRequest request, EClass entityType) {
		String select = option(request, "$select");
		if (select == null || select.isBlank()) {
			return null;
		}
		limits.checkExpression(select); // nested trees are parsed — same hostile-input guard
		return SelectTree.parse(select, entityType);
	}

	/**
	 * Validated {@code $expand} items ([OData-URL] 5.1.3): navigation name → nested
	 * {@code $filter} IR (null without one). Only {@code $filter} is supported as a nested
	 * option (on collection-valued navigations); other nested options answer 501.
	 */
	private Map<String, OclExpression> expandOption(HttpServletRequest request, EClass entityType) {
		String expand = option(request, "$expand");
		Map<String, OclExpression> items = new LinkedHashMap<>();
		if (expand == null || expand.isBlank()) {
			return items;
		}
		limits.checkExpression(expand); // nested $filter trees are parsed — same hostile-input guard
		for (String item : splitExpandItems(expand)) {
			String trimmed = item.trim();
			String name = trimmed;
			String nested = null;
			int paren = trimmed.indexOf('(');
			if (paren >= 0 && trimmed.endsWith(")")) {
				name = trimmed.substring(0, paren).trim();
				nested = trimmed.substring(paren + 1, trimmed.length() - 1).trim();
			}
			if (!(entityType.getEStructuralFeature(name) instanceof EReference reference)) {
				throw new ODataQueryParseException("unknown $expand navigation '" + name + "'");
			}
			items.put(name, nested == null ? null : nestedExpandFilter(nested, reference));
		}
		return items;
	}

	/** One parenthesized expand option block — {@code $filter=<expr>} is the supported shape. */
	private OclExpression nestedExpandFilter(String nested, EReference reference) {
		int equals = nested.indexOf('=');
		String optionName = equals < 0 ? nested : nested.substring(0, equals).trim();
		if (!"$filter".equalsIgnoreCase(optionName) && !"filter".equalsIgnoreCase(optionName)) {
			throw new UnsupportedOperationException(
					"only $filter is supported as a nested $expand option");
		}
		if (!reference.isMany()) {
			throw new ODataQueryParseException(
					"$filter inside $expand applies to collection-valued navigations");
		}
		String nestedFilter = nested.substring(equals + 1).trim();
		limits.checkExpression(nestedFilter); // defence in depth: guard the inner filter too
		return parser.parseFilter(nestedFilter, reference.getEReferenceType());
	}

	/** Top-level comma split of {@code $expand} — parens and string literals stay intact. */
	private static List<String> splitExpandItems(String expand) {
		List<String> items = new ArrayList<>();
		int depth = 0;
		boolean quoted = false;
		int start = 0;
		for (int i = 0; i < expand.length(); i++) {
			char c = expand.charAt(i);
			if (c == '\'') {
				quoted = !quoted;
			} else if (!quoted && c == '(') {
				depth++;
			} else if (!quoted && c == ')') {
				depth--;
			} else if (!quoted && depth == 0 && c == ',') {
				items.add(expand.substring(start, i));
				start = i + 1;
			}
		}
		items.add(expand.substring(start));
		return items;
	}

	/** Nested {@code $expand} filters run on the SHAPED copy — never on backend objects. */
	private void applyNestedFilters(EObject copy, Map<String, OclExpression> expand) {
		for (Map.Entry<String, OclExpression> item : expand.entrySet()) {
			if (item.getValue() == null) {
				continue;
			}
			EStructuralFeature feature = copy.eClass().getEStructuralFeature(item.getKey());
			if (feature != null && copy.eGet(feature) instanceof List<?> children) {
				children.removeIf(child -> !expandFilterEvaluator
						.matchesNullSafe(item.getValue(), child));
			}
		}
	}

	private String entityJson(EObject entity, EClass entityType, SelectTree select, Set<String> expand)
			throws IOException {
		return serializeEntity(entity, shaper.shape(entity, entityType, select, expand, null),
				entityType, expand);
	}

	/** {@link #entityJson} for parsed expand specs: applies nested filters after shaping. */
	private String entityJson(EObject entity, EClass entityType, SelectTree select,
			Map<String, OclExpression> expand) throws IOException {
		EObject copy = shaper.shape(entity, entityType, select, expand.keySet(), null);
		applyNestedFilters(copy, expand);
		return serializeEntity(entity, copy, entityType, expand.keySet());
	}

	private String serializeEntity(EObject entity, EObject copy, EClass entityType,
			Set<String> expand) throws IOException {
		ODataJsonResourceImpl resource = ODataJsonResourceImpl.minimalMetadata(
				URI.createURI("response.odatajson"), metadataService, expand);
		resource.getContents().add(copy);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, null);
		String json = out.toString(StandardCharsets.UTF_8);
		if (entity.eClass() != entityType) {
			// derived instance: the type is NOT computable from the context URL, so minimal
			// metadata must transport the single-field discriminator ([OData-JSON] 4.5.8)
			json = "{\"@odata.type\":\"" + resource.typeDiscriminator(entity) + "\""
					+ (json.length() > 2 ? "," : "") + json.substring(1);
		}
		return json;
	}

	/**
	 * XML representation = EMF XMI of the (shaped) entities. Deliberately NOT OData Atom —
	 * that format is deprecated since OData 4.01; XMI is the natural XML form in the EMF
	 * ecosystem. Selected via {@code $format=xml} or an XML-only Accept header.
	 */
	private void writeXmi(HttpServletResponse response, List<EObject> roots) throws IOException {
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource resource = rs.createResource(URI.createURI("response.xmi"));
		resource.getContents().addAll(roots);
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_ENCODING, "UTF-8");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, options);
		response.setContentType("application/xml;charset=UTF-8");
		response.getWriter().write(out.toString(StandardCharsets.UTF_8));
	}

	private boolean wantsXml(HttpServletRequest request) {
		String format = option(request, "$format");
		if (format != null) {
			if ("xml".equalsIgnoreCase(format)) {
				return true;
			}
			if ("json".equalsIgnoreCase(format)) {
				return false;
			}
			throw new ODataQueryParseException("unsupported $format (json or xml)");
		}
		String accept = request.getHeader("Accept");
		return accept != null && !accept.contains("application/json")
				&& (accept.contains("application/xml") || accept.contains("text/xml"));
	}

	private static String contextRoot(HttpServletRequest request) {
		return request.getRequestURI().replaceFirst("/[^/]*$", "");
	}



	private void error(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(ODataJson.error(status, message));
	}
}
