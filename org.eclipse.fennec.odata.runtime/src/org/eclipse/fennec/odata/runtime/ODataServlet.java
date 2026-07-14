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
import java.util.LinkedHashSet;
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
import org.eclipse.fennec.m2x.model.ocl.Variable;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;
import org.eclipse.fennec.model.metadata.api.MetadataService;
import org.eclipse.fennec.odata.codec.json.ODataJsonResourceImpl;
import org.eclipse.fennec.odata.csdl.CsdlJsonWriter;
import org.eclipse.fennec.odata.csdl.EcoreToEdmConverter;
import org.eclipse.fennec.odata.csdl.ODataAnnotationConstants;
import org.eclipse.fennec.odata.csdl.OdataResolver;
import org.eclipse.fennec.odata.operation.api.ODataOperationHandler;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.DeltaGoneException;
import org.eclipse.fennec.odata.persistence.api.DeltaService;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.MediaService;
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
import org.open.oasis.docs.odata.ns.edm.TPropertyValue;
import org.open.oasis.docs.odata.ns.edm.TRecordExpression;
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
	private final List<MediaService> mediaServices = new CopyOnWriteArrayList<>();
	private final List<DeltaService> deltaServices = new CopyOnWriteArrayList<>();
	private final List<ODataOperationHandler> operationHandlers = new CopyOnWriteArrayList<>();
	private final CachingODataQueryParser parser = new CachingODataQueryParser();
	private final OclEvaluator expandFilterEvaluator = new OclEvaluator();
	/** Schema namespace/alias per package for cast resolution — same derivation as $metadata. */
	private final Map<EPackage, ODataPackageProfile> profiles =
			java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
	private final EntityShaper shaper = new EntityShaper();

	private volatile MetadataService metadataService;
	private volatile RequestLimits limits = RequestLimits.DEFAULTS;

	/**
	 * CORS origin(s) served to browser clients (e.g. the XOData explorer): {@code "*"} or a
	 * space-separated allowlist; EMPTY (the default) disables CORS entirely.
	 */
	private volatile String corsOrigin = "";

	@Activate
	void activate(Map<String, Object> configuration) {
		limits = RequestLimits.fromConfiguration(configuration);
		Object origin = configuration.get("odata.cors.origin");
		corsOrigin = origin == null ? "" : String.valueOf(origin).trim();
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
	void addMediaService(MediaService mediaService) {
		mediaServices.add(mediaService);
	}

	void removeMediaService(MediaService mediaService) {
		mediaServices.remove(mediaService);
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addDeltaService(DeltaService deltaService) {
		deltaServices.add(deltaService);
	}

	void removeDeltaService(DeltaService deltaService) {
		deltaServices.remove(deltaService);
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
			"$search", "$compute", "$deltatoken");
	/** Spec-defined options we know but do not implement yet → 501 (conformance 13.1.1/7). */
	private static final Set<String> KNOWN_UNSUPPORTED_OPTIONS = Set.of(
			"$skiptoken", "$id", "$index", "$schemaversion", "$levels");
	/**
	 * The parts of a defining query a delta link re-encodes ([OData-Protocol] 11.3.1: the link
	 * MUST NOT carry top/skip and SHOULD NOT carry count; ordering does not apply to deltas).
	 * Everything else present on a delta request → 400, the client MUST NOT append options.
	 */
	private static final Set<String> DELTA_LINK_OPTIONS = Set.of(
			"$filter", "$search", "$select", "$compute", "$format");

	/** The {@code odata.metadata=} format parameter of an Accept header or {@code $format}. */
	private static final java.util.regex.Pattern METADATA_PARAM =
			java.util.regex.Pattern.compile("odata\\.metadata\\s*=\\s*(full|minimal|none)",
					java.util.regex.Pattern.CASE_INSENSITIVE);

	/**
	 * The response metadata level for the current request, carried request-scoped so the many
	 * response writers do not each need the request threaded through. Set in {@link #service} and
	 * cleared afterwards; {@code null} outside a request means {@code minimal}.
	 */
	private static final ThreadLocal<String> METADATA_LEVEL = new ThreadLocal<>();

	/**
	 * The requested response metadata level ([OData-JSON] 3.1): {@code full} or {@code none} when
	 * the client asked for it via {@code Accept: …;odata.metadata=…} or {@code $format}, else
	 * {@code minimal} (the default).
	 */
	private static String metadataLevel(HttpServletRequest request) {
		String source = request.getParameter("$format");
		if (source == null || source.isBlank()) {
			source = request.getHeader("Accept");
		}
		if (source != null) {
			java.util.regex.Matcher matcher = METADATA_PARAM.matcher(source);
			if (matcher.find() && !matcher.group(1).equalsIgnoreCase("minimal")) {
				return matcher.group(1).toLowerCase(java.util.Locale.ROOT);
			}
		}
		return "minimal";
	}

	private static String responseMetadataLevel() {
		String level = METADATA_LEVEL.get();
		return level == null ? "minimal" : level;
	}

	/** The JSON content type carrying the current request's metadata level. */
	private static String contentTypeJson() {
		return "application/json;odata.metadata=" + responseMetadataLevel()
				+ (ieee754() ? ";IEEE754Compatible=true" : "") + ";charset=UTF-8";
	}

	/**
	 * {@code odata.metadata=none} ([OData-JSON] 3.1.1): the payload MUST omit all control
	 * information other than {@code @odata.nextLink} and {@code @odata.count} — no context URL,
	 * no type discriminators.
	 */
	private static boolean omitContext() {
		return "none".equals(responseMetadataLevel());
	}

	private static final java.util.regex.Pattern IEEE754_PARAM =
			java.util.regex.Pattern.compile("IEEE754Compatible\\s*=\\s*(true|false)",
					java.util.regex.Pattern.CASE_INSENSITIVE);

	/** Whether the current response runs {@code IEEE754Compatible=true}; request-scoped like the metadata level. */
	private static final ThreadLocal<Boolean> IEEE754 = new ThreadLocal<>();

	/** {@code IEEE754Compatible=true} requested via {@code $format} or {@code Accept} ([OData-JSON] 8.1). */
	private static boolean ieee754Requested(HttpServletRequest request) {
		String source = request.getParameter("$format");
		if (source == null || source.isBlank()) {
			source = request.getHeader("Accept");
		}
		if (source == null) {
			return false;
		}
		java.util.regex.Matcher matcher = IEEE754_PARAM.matcher(source);
		return matcher.find() && "true".equalsIgnoreCase(matcher.group(1));
	}

	private static boolean ieee754() {
		return Boolean.TRUE.equals(IEEE754.get());
	}

	/** {@code @odata.count} is Edm.Int64 — a string under {@code IEEE754Compatible=true}. */
	private static String countValue(long count) {
		return ieee754() ? "\"" + count + "\"" : Long.toString(count);
	}

	/** Weaves the context annotation into an entity object (single entities have no envelope). */
	private static String withContext(String contextUrl, String entityJson) {
		if (omitContext()) {
			return entityJson;
		}
		return "{\"@odata.context\":\"" + contextUrl + "\"," + entityJson.substring(1);
	}

	/** Collection-envelope head: an opening brace plus the context property — context-free under metadata=none. */
	private static StringBuilder envelopeHead(String contextUrl) {
		StringBuilder head = new StringBuilder("{");
		if (!omitContext()) {
			head.append("\"@odata.context\":\"").append(contextUrl).append('"');
		}
		return head;
	}

	/** Separates the next top-level envelope property unless it is the first one. */
	private static StringBuilder envelopeProperty(StringBuilder envelope) {
		if (envelope.length() > 1) {
			envelope.append(',');
		}
		return envelope;
	}

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
				metadataDocument(request, response);
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
		if (applyCors(request, response)) {
			return; // a CORS preflight was answered
		}
		// carry the requested metadata level request-scoped (save/restore keeps $batch sub-requests,
		// which re-enter this method, from clobbering the outer request's level)
		String previousLevel = METADATA_LEVEL.get();
		Boolean previousIeee754 = IEEE754.get();
		METADATA_LEVEL.set(metadataLevel(request));
		IEEE754.set(ieee754Requested(request));
		try {
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
		} finally {
			if (previousLevel == null) {
				METADATA_LEVEL.remove();
			} else {
				METADATA_LEVEL.set(previousLevel);
			}
			if (previousIeee754 == null) {
				IEEE754.remove();
			} else {
				IEEE754.set(previousIeee754);
			}
		}
	}

	/**
	 * Applies CORS headers when configured ({@code odata.cors.origin}) so browser-based clients
	 * (XOData & co.) can call the service; answers OPTIONS preflights. Returns {@code true} when
	 * the request WAS a handled preflight. Disabled (no headers at all) by default.
	 */
	private boolean applyCors(HttpServletRequest request, HttpServletResponse response) {
		if (corsOrigin.isEmpty()) {
			return false;
		}
		String origin = request.getHeader("Origin");
		String allowed = "*".equals(corsOrigin) ? "*"
				: origin != null && List.of(corsOrigin.split("\\s+")).contains(origin) ? origin : null;
		if (allowed == null) {
			return false; // no CORS headers for a non-allowlisted origin — the browser blocks it
		}
		response.setHeader("Access-Control-Allow-Origin", allowed);
		response.setHeader("Access-Control-Expose-Headers",
				"OData-Version, OData-EntityId, ETag, Location, Preference-Applied");
		if ("OPTIONS".equals(request.getMethod())) {
			response.setHeader("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS");
			response.setHeader("Access-Control-Allow-Headers",
					"Content-Type, Accept, If-Match, OData-Version, OData-MaxVersion, Prefer,"
							+ " Authorization, X-CSRF-Token");
			response.setHeader("Access-Control-Max-Age", "3600");
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return true;
		}
		return false;
	}

	// --- $batch (OData v4.01 JSON batch format, OASIS Part 1 §11.7 + JSON batch spec) ---

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
	private void batch(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("OData-Version", negotiateVersion(request));
		if (!"POST".equals(request.getMethod())) {
			error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "$batch requires POST");
			return;
		}
		String contentType = request.getContentType();
		boolean multipart = contentType != null
				&& contentType.toLowerCase(Locale.ROOT).contains("multipart/mixed");
		if (contentType == null || (!multipart
				&& !contentType.toLowerCase(Locale.ROOT).contains("application/json"))) {
			error(response, 415, "only the OData JSON and multipart/mixed batch formats are supported");
			return;
		}

		byte[] body = request.getInputStream().readNBytes(limits.maxBodyBytes() + 1);
		if (body.length > limits.maxBodyBytes()) {
			error(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "batch body too large");
			return;
		}
		JsonNode requests;
		if (multipart) {
			// the 4.0 wire form: translate parts/change sets into the SAME request shape the JSON
			// loop processes (change set N -> atomicityGroup "csN", Content-ID -> id)
			String boundary = multipartBoundary(contentType);
			if (boundary == null) {
				error(response, HttpServletResponse.SC_BAD_REQUEST, "multipart batch without boundary");
				return;
			}
			try {
				requests = parseMultipartBatch(new String(body, StandardCharsets.UTF_8), boundary);
			} catch (IllegalArgumentException e) {
				error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed multipart batch");
				return;
			}
		} else {
			JsonNode root;
			try {
				root = JSON.readTree(new String(body, StandardCharsets.UTF_8));
			} catch (Exception e) {
				error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed batch body");
				return;
			}
			requests = root.get("requests");
			if (requests == null || !requests.isArray()) {
				error(response, HttpServletResponse.SC_BAD_REQUEST,
						"batch body must carry a \"requests\" array");
				return;
			}
		}

		ArrayNode responses = JSON.createArrayNode();
		Map<String, Integer> statusById = new HashMap<>();
		Set<String> failedIds = new HashSet<>();
		String currentGroup = null;
		List<ObjectNode> groupBuffer = new ArrayList<>();
		boolean groupFailed = false;
		for (JsonNode sub : requests) {
			String group = sub.path("atomicityGroup").asString(null);
			if (!java.util.Objects.equals(group, currentGroup)) {
				finalizeGroup(currentGroup, groupBuffer, groupFailed, responses, statusById, failedIds);
				groupBuffer = new ArrayList<>();
				groupFailed = false;
				currentGroup = group;
				if (group != null) {
					transactionalWriteServices().forEach(WriteService::begin);
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

		if (multipart) {
			writeMultipartBatchResponse(requests, responses, response);
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json;charset=UTF-8");
		ObjectNode envelope = JSON.createObjectNode();
		envelope.set("responses", responses);
		response.getWriter().write(JSON.writeValueAsString(envelope));
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
		ArrayNode requests = JSON.createArrayNode();
		int changeset = 0;
		int generated = 0;
		for (String part : body.split("\\r?\\n?--" + java.util.regex.Pattern.quote(boundary))) {
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
				for (String member : partBody.split("\\r?\\n?--" + java.util.regex.Pattern.quote(nested))) {
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
		ObjectNode node = JSON.createObjectNode();
		String id = null;
		for (String line : partHeaders.split("\\r?\\n")) {
			if (line.regionMatches(true, 0, "Content-ID:", 0, 11)) {
				id = line.substring(11).trim();
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
			if (lines[index].regionMatches(true, 0, "Content-ID:", 0, 11)) {
				id = lines[index].substring(11).trim();
			}
			index++; // inner request headers (Accept, Content-Type, …)
		}
		String requestBody = index >= lines.length ? ""
				: String.join("\n", java.util.Arrays.asList(lines)
						.subList(Math.min(index + 1, lines.length), lines.length)).trim();
		if (url.startsWith("http://") || url.startsWith("https://")) {
			// absolute-form request lines: reduce to service-root-relative (keep the query!)
			java.net.URI absolute = java.net.URI.create(url);
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
				node.set("body", JSON.readTree(requestBody));
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
					result.set("body", JSON.readTree(ODataJson.error(424,
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
		return writeServices.stream().filter(WriteService::transactional).toList();
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
		path = resolveKeyAliases(path, request, response);
		if (path == null) {
			return; // 400 already written
		}
		path = keyAsSegment(path);
		EClass entityType = resolveEntityType(path.entitySet());
		if (entityType == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND,
					"unknown entity set '" + ODataJson.sanitize(path.entitySet()) + "'");
			return;
		}
		// POST Set(key)/Ns.Action — a bound action (parameters in the body). The qualified action
		// name parses as a single cast-shaped segment; it is dispatched through the operation SPI,
		// not the write backend, so it is intercepted before the WriteService is resolved.
		if ("POST".equals(request.getMethod()) && path.key() != null && path.segments().size() == 1
				&& path.segments().get(0) instanceof ResourcePath.TypeCastSegment action) {
			boundAction(path, action.qualifiedName(), request, response);
			return;
		}
		// 4.01 13.2.1/9.5: the same action invoked UNQUALIFIED (default namespace) parses as a
		// property segment — dispatched as an action when the name is an operation, not a feature
		if ("POST".equals(request.getMethod()) && path.key() != null && path.segments().size() == 1
				&& path.segments().get(0) instanceof ResourcePath.PropertySegment property
				&& property.key() == null
				&& entityType.getEStructuralFeature(property.name()) == null
				&& hasBoundOperation(entityType, property.name())) {
			boundAction(path, property.name(), request, response);
			return;
		}
		// entity-level compound-key writes go through the named-key SPI overloads; below the
		// entity ($ref/nav/media) the SPI is single-raw-key — refused honestly
		if (!path.namedKeys().isEmpty() && !path.segments().isEmpty()) {
			error(response, 501, "writes below a composite-key entity are not supported");
			return;
		}
		// PUT Set(key)/$value on a media entity replaces the binary stream — routed to the
		// MediaService SPI before the WriteService (and its JSON-only content-type guard).
		if ("PUT".equals(request.getMethod()) && path.key() != null && path.segments().size() == 1
				&& path.segments().get(0) instanceof ResourcePath.ValueSegment
				&& hasStream(entityType)) {
			mediaWrite(entityType, path, request, response);
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
				if (!preconditionHolds(entityType, path, request, response)) {
					return; // 428/412 already written
				}
				WritePayload payload = readPayload(request, response, entityType);
				if (payload == null) {
					return; // error already written
				}
				WriteService.WriteResult result = path.namedKeys().isEmpty()
						? writeService.update(entityType, path.key(),
								payload.entity(), "PUT".equals(request.getMethod()))
						: writeService.update(entityType, path.namedKeys(),
								payload.entity(), "PUT".equals(request.getMethod()));
				if (!payload.bindings().isEmpty()) {
					applyBindings(writeService, entityType, path.key(), payload.bindings());
				}
				if (result.created()) { // OData upsert (13.1.1/29)
					respondCreated(path.entitySet(), result.entity(), entityType, request, response);
				} else {
					respondUpdated(path.entitySet(), result.entity(), entityType, request, response);
				}
			}
			case "DELETE" -> {
				if (path.key() == null) {
					error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
							"DELETE addresses one entity by key");
					return;
				}
				if (!preconditionHolds(entityType, path, request, response)) {
					return; // 428/412 already written
				}
				if (path.namedKeys().isEmpty() ? writeService.delete(entityType, path.key())
						: writeService.delete(entityType, path.namedKeys())) {
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
			response.setContentType(contentTypeJson());
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
	/** {@link #preconditionHolds(EClass, String, HttpServletRequest, HttpServletResponse)} for a parsed path (compound-key aware). */
	private boolean preconditionHolds(EClass entityType, ResourcePath path,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (path.namedKeys().isEmpty()) {
			return preconditionHolds(entityType, path.key(), request, response);
		}
		return preconditionHolds(entityType,
				currentEntity(entityType, compositeKeyEquals(entityType, path.namedKeys())),
				request, response);
	}

	private boolean preconditionHolds(EClass entityType, String rawKey,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		return preconditionHolds(entityType, currentEntity(entityType, rawKey), request, response);
	}

	/** The If-Match core over an already-loaded current state (null = upsert, no precondition). */
	private boolean preconditionHolds(EClass entityType, EObject current,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
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

	/**
	 * Weak ETag over the full serialized entity — stable per state, cheap to recompute. Pinned
	 * to the canonical (minimal-metadata) serialization so the tag does not vary with the
	 * REQUESTED metadata level (a GET under {@code none}/{@code full} must yield an ETag a
	 * later write with the default level can match).
	 */
	private String etagOf(EObject entity, EClass entityType) throws IOException {
		String requestedLevel = METADATA_LEVEL.get();
		Boolean requestedIeee754 = IEEE754.get();
		METADATA_LEVEL.set("minimal");
		IEEE754.set(Boolean.FALSE);
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(
					entityJson(entity, entityType, null, Set.of())
							.getBytes(StandardCharsets.UTF_8));
			return "W/\"" + HexFormat.of().formatHex(digest, 0, 8) + "\"";
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		} finally {
			if (requestedLevel == null) {
				METADATA_LEVEL.remove();
			} else {
				METADATA_LEVEL.set(requestedLevel);
			}
			if (requestedIeee754 == null) {
				IEEE754.remove();
			} else {
				IEEE754.set(requestedIeee754);
			}
		}
	}

	/** The current entity by key, or null when absent or no read backend serves the type. */
	private EObject currentEntity(EClass entityType, String rawKey) {
		EAttribute keyAttribute = entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
		return keyAttribute == null ? null
				: currentEntity(entityType, keyEquals(keyAttribute, rawKey));
	}

	/** {@link #currentEntity(EClass, String)} over an already-built key predicate AST. */
	private EObject currentEntity(EClass entityType, OclExpression keyPredicate) {
		QueryService queryService = queryServices.stream()
				.filter(s -> s.supports(entityType)).findFirst().orElse(null);
		if (queryService == null) {
			return null; // no read backend serves this type — no ETag was ever issued, skip the check
		}
		// A backend FAILURE must NOT be silently degraded to "entity absent": that would skip the
		// If-Match precondition and let a PATCH/PUT/DELETE overwrite a concurrently-changed entity
		// exactly when the backend is flaky. Let it propagate (→ logged 500); only a genuinely
		// empty result means "not found".
		QueryResult result = queryService.execute(new EntityQuery(entityType, null,
				keyPredicate, List.of(), 0, 1, false));
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
		String payloadContentType = request.getContentType();
		if (payloadContentType != null) { // IEEE754Compatible=true payloads carry Int64/Decimal as strings
			java.util.regex.Matcher matcher = IEEE754_PARAM.matcher(payloadContentType);
			resource.ieee754Compatible(matcher.find() && "true".equalsIgnoreCase(matcher.group(1)));
		}
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

	/**
	 * 201 with Location/OData-EntityId and the created entity body, unless the client asked for
	 * {@code Prefer: return=minimal} — then 204 with just the headers ([OData-Protocol] 8.2.8.7).
	 * A honoured preference is echoed via {@code Preference-Applied}.
	 */
	private void respondCreated(String setName, EObject entity, EClass entityType,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		EAttribute id = entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
		String editUrl = contextRoot(request) + "/" + setName
				+ (id == null ? "" : "(" + urlKeyLiteral(id, entity.eGet(id)) + ")");
		response.setHeader("Location", editUrl);
		response.setHeader("OData-EntityId", editUrl);
		if ("minimal".equals(returnPreference(request))) {
			response.setHeader("Preference-Applied", "return=minimal");
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return;
		}
		if ("representation".equals(returnPreference(request))) {
			response.setHeader("Preference-Applied", "return=representation");
		}
		response.setStatus(HttpServletResponse.SC_CREATED);
		String json = entityJson(entity, entityType, null, Set.of());
		response.setContentType(contentTypeJson());
		response.getWriter().write(withContext(
				contextRoot(request) + "/$metadata#" + setName + "/$entity", json));
	}

	/**
	 * 204 for a successful update, unless the client asked for {@code Prefer: return=representation}
	 * — then 200 with the updated entity ([OData-Protocol] 8.2.8.7). A honoured preference is echoed
	 * via {@code Preference-Applied}.
	 */
	private void respondUpdated(String setName, EObject entity, EClass entityType,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if ("representation".equals(returnPreference(request)) && entity != null) {
			response.setHeader("Preference-Applied", "return=representation");
			response.setStatus(HttpServletResponse.SC_OK);
			String json = entityJson(entity, entityType, null, Set.of());
			response.setContentType(contentTypeJson());
			response.getWriter().write(withContext(
					contextRoot(request) + "/$metadata#" + setName + "/$entity", json));
			return;
		}
		if ("minimal".equals(returnPreference(request))) {
			response.setHeader("Preference-Applied", "return=minimal");
		}
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	/** The {@code return=} value of the {@code Prefer} header ("minimal"/"representation"), or null. */
	private static String returnPreference(HttpServletRequest request) {
		String prefer = request.getHeader("Prefer");
		if (prefer == null) {
			return null;
		}
		for (String token : prefer.split(",")) {
			String t = token.trim();
			if (t.regionMatches(true, 0, "return=", 0, 7)) {
				String value = t.substring(7).trim();
				if (value.equalsIgnoreCase("minimal")) {
					return "minimal";
				}
				if (value.equalsIgnoreCase("representation")) {
					return "representation";
				}
			}
		}
		return null;
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
		String singletons = singletonNames().stream()
				.map(name -> "{\"name\":\"" + name + "\",\"kind\":\"Singleton\",\"url\":\"" + name + "\"}")
				.collect(Collectors.joining(","));
		String value = singletons.isEmpty() ? sets : sets.isEmpty() ? singletons : sets + "," + singletons;
		response.setContentType("application/json;charset=UTF-8");
		StringBuilder json = envelopeHead(request.getRequestURI() + "/$metadata");
		envelopeProperty(json).append("\"value\":[").append(value).append("]}");
		response.getWriter().write(json.toString());
	}

	/** Names of the container singletons declared across the registered packages ([OData-CSDL] 13.5). */
	private List<String> singletonNames() {
		List<String> names = new ArrayList<>();
		for (EPackage pkg : packages) {
			EAnnotation annotation = pkg.getEAnnotation(ODataAnnotationConstants.SINGLETONS_SOURCE);
			if (annotation != null) {
				names.addAll(annotation.getDetails().keySet());
			}
		}
		return names;
	}

	/** The entity type of a declared container singleton by name, or null. */
	private EClass resolveSingleton(String name) {
		for (EPackage pkg : packages) {
			EAnnotation annotation = pkg.getEAnnotation(ODataAnnotationConstants.SINGLETONS_SOURCE);
			if (annotation != null && annotation.getDetails().containsKey(name)
					&& pkg.getEClassifier(annotation.getDetails().get(name)) instanceof EClass type) {
				return type;
			}
		}
		return null;
	}

	/**
	 * Serves a container singleton {@code GET /Me[/…]}: the backend supplies the single instance
	 * (via {@link QueryService#singleton}); a bare singleton serialises like a single entity (with a
	 * singleton context URL {@code #Me}), a path below it walks from that instance.
	 */
	private void singletonResource(ResourcePath path, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		EClass type = resolveSingleton(path.entitySet());
		QueryService queryService = queryServices.stream().filter(s -> s.supports(type))
				.findFirst().orElse(null);
		if (queryService == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND,
					"no backend for singleton '" + ODataJson.sanitize(path.entitySet()) + "'");
			return;
		}
		EObject entity = queryService.singleton(type, path.entitySet()).orElse(null);
		if (entity == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "the singleton has no instance");
			return;
		}
		if (path.segments().isEmpty()) {
			singletonEntity(path.entitySet(), entity, type, request, response);
		} else {
			walk(path, entity, request, response);
		}
	}

	/** {@link #singleEntity} for a container singleton — same shaping, but the context URL is {@code #Name}. */
	private void singletonEntity(String name, EObject entity, EClass type,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("ETag", etagOf(entity, type));
		SelectTree select = selectOption(request, type);
		Map<String, ExpandItem> expand = expandOption(request, type);
		if (wantsXml(request)) {
			List<EObject> copies = shaper.shapeAll(List.of(entity), type, select, inlineNavs(expand));
			copies.forEach(copy -> applyNestedFilters(copy, expand));
			writeXmi(response, copies);
			return;
		}
		String json = entityJson(entity, type, select, expand);
		response.setContentType(contentTypeJson());
		response.getWriter().write(withContext(
				contextRoot(request) + "/$metadata#" + name, json));
	}

	/** Whether the client asked for the CSDL <b>JSON</b> representation of {@code $metadata} (4.01). */
	private static boolean wantsJsonMetadata(HttpServletRequest request) {
		String format = request.getParameter("$format");
		if (format != null && !format.isBlank()) {
			String normalized = format.trim().toLowerCase(java.util.Locale.ROOT);
			return normalized.equals("json") || normalized.startsWith("application/json");
		}
		String accept = request.getHeader("Accept");
		return accept != null && accept.toLowerCase(java.util.Locale.ROOT).contains("application/json");
	}

	private void metadataDocument(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
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
		// set renames are declared per package but the container may live in ANOTHER schema than
		// its types (Northwind) — collect the renames of all packages and apply them everywhere
		Map<String, String> setNames = new HashMap<>();
		packages.forEach(pkg -> setNames.putAll(EcoreToEdmConverter.entitySetNames(pkg)));
		for (EPackage pkg : packages) { // one Schema per registered package (req §3.3 composition)
			SchemaType schema = converter.toSchema(pkg);
			converter.applyEntitySetNames(setNames, schema);
			for (TEntityContainer container : schema.getEntityContainer()) {
				AnnotationType versions = EdmFactory.eINSTANCE.createAnnotationType();
				versions.setTerm("Org.OData.Core.V1.ODataVersions");
				versions.setString1("4.0 4.01");
				container.getAnnotation().add(versions);
				// what this v1 read-only service can and cannot do (12/13.2.1 advertisement)
				AnnotationType conformance = EdmFactory.eINSTANCE.createAnnotationType();
				conformance.setTerm("Org.OData.Capabilities.V1.ConformanceLevel");
				// every 4.0 AND 4.01 Advanced MUST is implemented and clause-audited
				// (docs/odata-conformance-status.md, re-audit 2026-07-14)
				conformance.setEnumMember1(
						List.of("Org.OData.Capabilities.V1.ConformanceLevelType/Advanced"));
				container.getAnnotation().add(conformance);
				container.getAnnotation().add(
						boolCapability("Org.OData.Capabilities.V1.BatchSupported", true));
				container.getAnnotation().add(changeTrackingCapability(!deltaServices.isEmpty()));
				container.getAnnotation().add(
						boolCapability("Org.OData.Capabilities.V1.AsynchronousRequestsSupported", false));
				container.getAnnotation().add(
						boolCapability("Org.OData.Capabilities.V1.KeyAsSegmentSupported", false));
			}
			dataServices.getSchema().add(schema);
		}
		edmx.setDataServices(dataServices);
		root.setEdmx(edmx);

		if (wantsJsonMetadata(request)) { // CSDL JSON ([OData-CSDL-JSON]) — same tree, second wire form
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(CsdlJsonWriter.write(root));
			return;
		}

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

	/** {@code Capabilities.ChangeTracking} is a record-typed term ([OData-Protocol] 11.3). */
	private static AnnotationType changeTrackingCapability(boolean supported) {
		AnnotationType annotation = EdmFactory.eINSTANCE.createAnnotationType();
		annotation.setTerm("Org.OData.Capabilities.V1.ChangeTracking");
		TRecordExpression record = EdmFactory.eINSTANCE.createTRecordExpression();
		TPropertyValue member = EdmFactory.eINSTANCE.createTPropertyValue();
		member.setProperty("Supported");
		member.setBool1(supported);
		record.getPropertyValue().add(member);
		annotation.getRecord().add(record);
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
		if (option(request, "$deltatoken") != null) { // following a delta link ([OData-Protocol] 11.3.2)
			deltaResponse(setName, castName, castType, target, request, response);
			return;
		}
		// a cast makes the DERIVED type the context: its properties are addressable in options
		EClass context = castType != null ? castType : target.entityType();
		boolean xml = wantsXml(request);
		// $compute defines dynamic aliases that may be referenced from $filter/$orderby/$select
		ApplyPipeline computePipeline = computePipeline(request, context);
		Map<String, OclExpression> computeAliases = computeAliasMap(computePipeline);
		SelectTree select = selectOption(request, context, computeAliases.keySet());
		Map<String, ExpandItem> expand = expandOption(request, context);
		Map<String, String> aliases = parameterAliases(request);

		List<OrderBySegment> orderBy = parseChecked(option(request, "$orderby"),
				value -> inlineOrderBy(computePipeline != null
						? parser.parseOrderByAfterApply(value, context, computePipeline, aliases)
						: aliases.isEmpty() ? parser.parseOrderBy(value, context)
								: parser.parseOrderBy(value, context, aliases),
						computeAliases));
		int skip = limits.effectiveSkip(option(request, "$skip"));
		int top = pageSize(request, response, limits.effectiveTop(option(request, "$top")));
		// peek one row beyond the page: partial results MUST carry @odata.nextLink (13.1.1/3)
		EntityQuery query = new EntityQuery(target.entityType(), castType,
				parseChecked(filterWithSearch(request, context),
						filter -> inlineComputeAliases(computePipeline != null
								? parser.parseFilterAfterApply(filter, context, computePipeline, aliases)
								: aliases.isEmpty() ? parser.parseFilter(filter, context)
										: parser.parseFilter(filter, context, aliases),
								computeAliases)),
				orderBy == null ? List.of() : orderBy,
				skip, top + 1,
				"true".equals(option(request, "$count")),
				expand.keySet()); // backends prefetch expanded navigations (no N+1, no lazy proxies)

		// change tracking ([OData-Protocol] 11.3): a preference, applied only when the backend
		// can track this type and the defining query stays inside the supported shape (no $expand
		// v1). The token is taken BEFORE the query runs — a write racing the read is re-reported
		// in the first delta rather than lost.
		DeltaService deltaService = trackChangesRequested(request) && expand.isEmpty()
				? deltaService(target.entityType()) : null;
		String deltaToken = deltaService == null ? null
				: deltaService.trackingToken(target.entityType());

		QueryResult result = target.queryService().execute(query);
		boolean hasMore = result.entities().size() > top;
		List<EObject> page = hasMore ? result.entities().subList(0, top) : result.entities();

		if (xml) { // XMI is a non-OData projection — trimmed, but without an embedded link
			List<EObject> copies = shaper.shapeAll(page, context, select, inlineNavs(expand));
			copies.forEach(copy -> applyNestedFilters(copy, expand));
			writeXmi(response, copies);
			return;
		}
		StringBuilder json = envelopeHead(contextRoot(request) + "/$metadata#" + setName
				+ (castName != null ? "/" + castName : ""));
		if (result.totalCount() >= 0) {
			envelopeProperty(json).append("\"@odata.count\":").append(countValue(result.totalCount()));
		}
		List<ComputeExpression> computes = selectedComputes(computePipeline, request, computeAliases);
		envelopeProperty(json).append("\"value\":[");
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
		} else if (deltaToken != null) { // the delta link replaces the next link on the LAST page
			response.setHeader("Preference-Applied", "odata.track-changes");
			json.append(",\"@odata.deltaLink\":\"")
					.append(ODataJson.sanitize(deltaLink(request, deltaToken))).append('"');
		}
		json.append('}');
		response.setContentType(contentTypeJson());
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

	// --- change tracking ([OData-Protocol] 11.3, [OData-JSON] delta payloads) ---

	/** Whether the client sent {@code Prefer: odata.track-changes} (prefix optional, 4.01). */
	private static boolean trackChangesRequested(HttpServletRequest request) {
		String prefer = request.getHeader("Prefer");
		if (prefer == null) {
			return false;
		}
		for (String preference : prefer.split(",")) {
			String name = preference.trim().split("=", 2)[0].trim().toLowerCase(Locale.ROOT);
			if ("odata.track-changes".equals(name) || "track-changes".equals(name)) {
				return true;
			}
		}
		return false;
	}

	private DeltaService deltaService(EClass entityType) {
		return deltaServices.stream().filter(s -> s.supports(entityType)).findFirst().orElse(null);
	}

	/**
	 * The delta link: the defining query's {@linkplain #DELTA_LINK_OPTIONS carry-over options}
	 * (plus its {@code @}-parameter aliases) re-encoded around the fresh {@code $deltatoken} —
	 * self-describing, so the server stays stateless per client.
	 */
	private String deltaLink(HttpServletRequest request, String token) {
		return definingUrl(request) + (definingUrl(request).indexOf('?') < 0 ? '?' : '&')
				+ "$deltatoken=" + java.net.URLEncoder.encode(token, StandardCharsets.UTF_8);
	}

	/** The defining query's URL without the token — the refetch target for {@code 410 Gone}. */
	private String definingUrl(HttpServletRequest request) {
		StringBuilder link = new StringBuilder(request.getRequestURI());
		char separator = '?';
		for (String option : DELTA_LINK_OPTIONS) {
			String value = option(request, option);
			if (value != null) {
				link.append(separator).append(option).append('=')
						.append(java.net.URLEncoder.encode(value, StandardCharsets.UTF_8));
				separator = '&';
			}
		}
		for (Map.Entry<String, String> alias : parameterAliases(request).entrySet()) {
			link.append(separator)
					.append(java.net.URLEncoder.encode(alias.getKey(), StandardCharsets.UTF_8))
					.append('=').append(java.net.URLEncoder.encode(alias.getValue(), StandardCharsets.UTF_8));
			separator = '&';
		}
		return link.toString();
	}

	/**
	 * Answers a delta link ({@code GET Set?$deltatoken=…}): everything that changed since the
	 * token, as a delta payload — upserts with their current state, removals as deleted-entity
	 * objects in the negotiated version's form, and a fresh delta link for the next round.
	 * An aged-out token answers {@code 410 Gone} with the refetch URL in {@code Location}.
	 */
	private void deltaResponse(String setName, String castName, EClass castType, Target target,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		for (String name : request.getParameterMap().keySet()) {
			String normalized = normalizeOption(name);
			if (normalized.startsWith("$") && !"$deltatoken".equals(normalized)
					&& !DELTA_LINK_OPTIONS.contains(normalized)) {
				error(response, HttpServletResponse.SC_BAD_REQUEST,
						"query options must not be appended to a delta link");
				return;
			}
		}
		DeltaService deltaService = deltaService(target.entityType());
		if (deltaService == null) {
			error(response, 501, "change tracking is not supported for this entity set");
			return;
		}
		EClass context = castType != null ? castType : target.entityType();
		Map<String, String> aliases = parameterAliases(request);
		SelectTree select = selectOption(request, context);
		EntityQuery definingQuery = new EntityQuery(target.entityType(), castType,
				parseChecked(filterWithSearch(request, context),
						filter -> aliases.isEmpty() ? parser.parseFilter(filter, context)
								: parser.parseFilter(filter, context, aliases)),
				List.of(), 0, -1, false);

		DeltaService.DeltaResult delta;
		try {
			delta = deltaService.changesSince(definingQuery, option(request, "$deltatoken"));
		} catch (DeltaGoneException e) {
			// the client refetches the full set: the defining query without the token (11.3.2)
			response.setHeader("Location", definingUrl(request));
			error(response, 410, "the delta token is no longer valid");
			return;
		}

		StringBuilder json = envelopeHead(contextRoot(request) + "/$metadata#" + setName
				+ (castName != null ? "/" + castName : "") + "/$delta");
		envelopeProperty(json).append("\"value\":[");
		boolean first = true;
		for (EObject entity : delta.changed()) {
			if (!first) {
				json.append(',');
			}
			first = false;
			json.append(entityJson(entity, context, select, Map.of()));
		}
		boolean v40 = "4.0".equals(negotiateVersion(request));
		for (DeltaService.Removal removal : delta.removals()) {
			if (!first) {
				json.append(',');
			}
			first = false;
			String id = setName + "(" + keyLiteral(removal.keyValues()) + ")";
			if (v40) { // 4.0 deleted-entity object: context fragment + plain id property
				json.append("{\"@odata.context\":\"#").append(setName).append("/$deletedEntity\",")
						.append("\"reason\":\"").append(removal.reason()).append("\",")
						.append("\"id\":\"").append(ODataJson.sanitize(id)).append("\"}");
			} else { // 4.01 form: @removed control information
				json.append("{\"@removed\":{\"reason\":\"").append(removal.reason()).append("\"},")
						.append("\"@id\":\"").append(ODataJson.sanitize(id)).append("\"}");
			}
		}
		json.append(']');
		json.append(",\"@odata.deltaLink\":\"")
				.append(ODataJson.sanitize(deltaLink(request, delta.nextToken()))).append('"');
		json.append('}');
		response.setContentType(contentTypeJson());
		response.getWriter().write(json.toString());
	}

	/**
	 * The key predicate for an entity id from the removal's key values: string values quoted
	 * ({@code ''}-escaped), composite keys as named pairs — the same forms {@code keyEquals}
	 * accepts back.
	 */
	/** The canonical entity id ({@code Set(key)}) — used by `$ref` reads and reference payloads. */
	private String entityIdOf(EObject entity) {
		Map<String, Object> keyValues = new LinkedHashMap<>();
		for (EAttribute id : entity.eClass().getEAllAttributes()) {
			if (id.isID()) {
				keyValues.put(id.getName(), entity.eGet(id));
			}
		}
		if (keyValues.isEmpty()) { // keyless (containment-only) types have no canonical URL
			throw new UnsupportedOperationException(
					"the type '" + entity.eClass().getName() + "' has no key — no entity reference");
		}
		return setNameOf(entity.eClass()) + "(" + keyLiteral(keyValues) + ")";
	}

	/** The container set name serving the given type — honours per-package set renames. */
	private String setNameOf(EClass entityType) {
		for (EPackage pkg : packages) {
			EAnnotation sets = pkg.getEAnnotation(ODataAnnotationConstants.ENTITY_SETS_SOURCE);
			if (sets == null) {
				continue;
			}
			for (Map.Entry<String, String> entry : sets.getDetails()) {
				if (entry.getValue().equals(entityType.getName())) {
					return entry.getKey(); // set name -> type name, inverted
				}
			}
		}
		return entityType.getName();
	}

	private static String keyLiteral(Map<String, Object> keyValues) {
		StringBuilder literal = new StringBuilder();
		boolean named = keyValues.size() > 1;
		for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
			if (literal.length() > 0) {
				literal.append(',');
			}
			if (named) {
				literal.append(entry.getKey()).append('=');
			}
			Object value = entry.getValue();
			if (value instanceof String string) {
				literal.append('\'').append(string.replace("'", "''")).append('\'');
			} else {
				literal.append(value);
			}
		}
		return literal.toString();
	}

	/** Dispatches a parsed resource path: set, set/$count, keyed entity, navigation walk. */
	private void resource(String rawPath, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		if (rawPath.startsWith("$crossjoin") || rawPath.startsWith("$all")
				|| rawPath.startsWith("$entity")) {
			// $crossjoin/$all/$entity ([OData-URL] 4.10/4.14/4.15) parse but have no engine yet
			error(response, 501, "this URL form is not implemented");
			return;
		}
		// a set name with named args is a COMPOUND KEY predicate (Set(id='x'), [OData-URL]),
		// not a function call — only a non-set name routes to the function imports
		if (isFunctionCall(rawPath)
				&& resolveEntityType(rawPath.substring(0, rawPath.indexOf('('))) == null) {
			functionImport(rawPath, request, response); // GET FuncName(p=…) — the resource parser
			return;                                      // deliberately does not model function segments
		}
		// 4.01 13.2.1/9.3: a parameterless function import invoked WITHOUT parentheses — a bare
		// name that is neither a set nor a singleton but an unbound operation
		if (rawPath.indexOf('/') < 0 && rawPath.indexOf('(') < 0
				&& resolveEntityType(rawPath) == null && resolveSingleton(rawPath) == null
				&& resolveUnboundFunction(rawPath) != null) {
			functionImport(rawPath + "()", request, response);
			return;
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
		if (path.entitySet().startsWith("$")) {
			// $crossjoin/$all/$entity parse ([OData-URL] 4.10/4.14/4.15) but have no engine yet
			error(response, 501, "'" + ODataJson.sanitize(path.entitySet()) + "' is not implemented");
			return;
		}
		path = resolveKeyAliases(path, request, response);
		if (path == null) {
			return; // 400 already written
		}
		path = keyAsSegment(path);
		if (path.key() == null && resolveSingleton(path.entitySet()) != null) {
			singletonResource(path, request, response); // container singleton (GET /Me[/…])
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
		if (option(request, "$deltatoken") != null) {
			// a delta link addresses an entity SET — a keyed resource cannot carry a token
			error(response, HttpServletResponse.SC_BAD_REQUEST,
					"a delta token applies to an entity set");
			return;
		}
		Target target = resolveTarget(path.entitySet(), response);
		if (target == null) {
			return;
		}
		// media entity ([OData-Protocol] 11.2.4): GET Set(key)/$value on a HasStream type is the
		// binary media stream, not a property value — routed to the MediaService SPI
		if (path.segments().size() == 1
				&& path.segments().get(0) instanceof ResourcePath.ValueSegment
				&& hasStream(target.entityType())) {
			mediaRead(target.entityType(), path.key(), response);
			return;
		}
		EObject entity = fetchByKey(target, path.key(), path.namedKeys(),
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

	/** Whether the entity type is a media entity ({@code @OData.HasStream}, [OData-CSDL] 8.1.2). */
	private static boolean hasStream(EClass entityType) {
		EAnnotation annotation = entityType.getEAnnotation(ODataAnnotationConstants.SOURCE);
		return annotation != null
				&& "true".equals(annotation.getDetails().get(ODataAnnotationConstants.HAS_STREAM));
	}

	/** {@code GET Set(key)/$value} on a media entity: the raw stream with its media type. */
	private void mediaRead(EClass entityType, String rawKey, HttpServletResponse response)
			throws IOException {
		MediaService mediaService = mediaServices.stream()
				.filter(s -> s.supports(entityType)).findFirst().orElse(null);
		if (mediaService == null) {
			error(response, 501, "no media backend for this entity type");
			return;
		}
		MediaService.MediaStream stream = mediaService.readMedia(entityType, rawKey).orElse(null);
		if (stream == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "the media entity has no stream");
			return;
		}
		response.setContentType(stream.contentType());
		response.setContentLength(stream.content().length);
		response.getOutputStream().write(stream.content());
	}

	/** {@code PUT Set(key)/$value} on a media entity: replaces the stream ([OData-Protocol] 11.4.7.1). */
	private void mediaWrite(EClass entityType, ResourcePath path, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (!preconditionHolds(entityType, path.key(), request, response)) {
			return; // 428/412 already written
		}
		MediaService mediaService = mediaServices.stream()
				.filter(s -> s.supports(entityType)).findFirst().orElse(null);
		if (mediaService == null) {
			error(response, 501, "no media backend for this entity type");
			return;
		}
		byte[] body = request.getInputStream().readNBytes(limits.maxBodyBytes() + 1);
		if (body.length > limits.maxBodyBytes()) {
			error(response, 413, "payload exceeds the maximum size of " + limits.maxBodyBytes() + " bytes");
			return;
		}
		if (!mediaService.writeMedia(entityType, path.key(),
				new MediaService.MediaStream(body, request.getContentType()))) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "no entity with this key");
			return;
		}
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

	/** {@code GET Set/$count}: the (optionally filtered, optionally cast) total as text/plain. */
	private void setCount(String setName, EClass castType, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (option(request, "$deltatoken") != null) { // 11.3.2 allows /$count on a delta link (MAY)
			error(response, 501, "the count of changes on a delta link is not implemented");
			return;
		}
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
		path = keyAsSegment(path);
		if (path.key() == null || !path.segments().isEmpty()) {
			error(response, HttpServletResponse.SC_NOT_FOUND,
					"a bound function is invoked on a keyed entity");
			return;
		}
		Target target = resolveTarget(path.entitySet(), response);
		if (target == null) {
			return;
		}
		EObject entity = fetchByKey(target, path.key(), path.namedKeys(), Set.of(), response);
		if (entity == null) {
			return; // error already written
		}
		invokeBoundFunction(entity, target.entityType(), localName, parameterList, request, response);
	}

	/** Whether the type carries a BOUND operation with the given (local) name. */
	private static boolean hasBoundOperation(EClass entityType, String localName) {
		return entityType.getEAllOperations().stream()
				.anyMatch(op -> op.getName().equals(localName) && !isUnbound(op));
	}

	/** Resolves and dispatches a bound function on an already-loaded entity (shared tail). */
	private void invokeBoundFunction(EObject entity, EClass declaredType, String localName,
			String parameterList, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		EOperation operation = entity.eClass().getEAllOperations().stream()
				.filter(op -> op.getName().equals(localName) && !isUnbound(op)).findFirst().orElse(null);
		if (operation == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "no bound function '" + localName + "'");
			return;
		}
		String qualifiedName = operationNamespace(declaredType) + "." + localName;
		ODataOperationHandler handler = operationHandlers.stream()
				.filter(h -> h.handles(qualifiedName)).findFirst().orElse(null);
		if (handler == null) {
			error(response, 501, "no handler for the operation");
			return;
		}
		Object result = handler.invoke(operation, entity, functionParameters(parameterList, operation));
		writeFunctionResult(result, request, response);
	}

	/**
	 * Invokes a bound action {@code POST Set(key)/Ns.Action} on the addressed entity, with the
	 * parameters in the JSON body ([OData-Protocol] 11.5.4.2). Mirrors {@link #boundFunction} but for
	 * the POST/body shape; the result is serialised like any operation result (void → 204).
	 */
	private void boundAction(ResourcePath path, String qualified, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String localName = qualified.contains(".")
				? qualified.substring(qualified.lastIndexOf('.') + 1) : qualified;
		Target target = resolveTarget(path.entitySet(), response);
		if (target == null) {
			return;
		}
		EObject entity = fetchByKey(target, path.key(), path.namedKeys(), Set.of(), response);
		if (entity == null) {
			return; // error already written
		}
		EOperation operation = entity.eClass().getEAllOperations().stream()
				.filter(op -> op.getName().equals(localName) && !isUnbound(op)).findFirst().orElse(null);
		if (operation == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "no bound action '" + localName + "'");
			return;
		}
		Map<String, Object> parameters = readActionParameters(request, operation, response);
		if (parameters == null) {
			return; // error already written
		}
		String qualifiedName = operationNamespace(target.entityType()) + "." + localName;
		ODataOperationHandler handler = operationHandlers.stream()
				.filter(h -> h.handles(qualifiedName)).findFirst().orElse(null);
		if (handler == null) {
			error(response, 501, "no handler for the operation");
			return;
		}
		writeFunctionResult(handler.invoke(operation, entity, parameters), request, response);
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
		response.setContentType(contentTypeJson());
		if (result instanceof EObject entity) {
			String json = entityJson(entity, entity.eClass(), null, Set.of());
			response.getWriter().write(withContext(contextRoot(request) + "/$metadata#"
					+ entity.eClass().getName() + "/$entity", json));
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
	 * Key-as-segment normalization ([OData-URL] 4.3.3, 4.01 MAY — the Microsoft Graph style):
	 * folds bare key segments into their collection's key predicate, so {@code Products/5} and
	 * {@code Products(5)} route identically. Two shapes arrive from the parser: explicit
	 * {@link ResourcePath.KeySegment}s (non-identifier literals) fold structurally; a
	 * {@link ResourcePath.PropertySegment} that does NOT match a declared feature of the
	 * current collection's type folds as a (quoted) string key — declared properties always
	 * win the ambiguity. Anything that does not fold cleanly leaves the path unchanged, so
	 * the classic routing (and its 404s) stays authoritative.
	 */
	private ResourcePath keyAsSegment(ResourcePath path) {
		EClass current = resolveEntityType(path.entitySet());
		if (current == null || path.segments().isEmpty()) {
			return path;
		}
		String key = path.key();
		// a keyless set (no positional AND no compound predicate) is a collection context
		boolean collection = key == null && path.namedKeys().isEmpty();
		boolean changed = false;
		List<ResourcePath.Segment> out = new ArrayList<>();
		for (ResourcePath.Segment segment : path.segments()) {
			switch (segment) {
				case ResourcePath.KeySegment(String value) -> {
					if (!collection) {
						return path; // a bare key needs a keyless collection before it
					}
					if (out.isEmpty()) {
						key = value;
					} else if (!foldKey(out, value)) {
						return path;
					}
					collection = false;
					changed = true;
				}
				case ResourcePath.PropertySegment property -> {
					EStructuralFeature feature = current == null ? null
							: current.getEStructuralFeature(property.name());
					if (feature == null && collection && property.key() == null) {
						// unknown name on a collection = an unquoted STRING key segment
						String quoted = "'" + property.name().replace("'", "''") + "'";
						if (out.isEmpty()) {
							key = quoted;
						} else if (!foldKey(out, quoted)) {
							return path;
						}
						collection = false;
						changed = true;
					} else {
						out.add(property);
						collection = feature != null && feature.isMany() && property.key() == null;
						current = feature != null && feature.getEType() instanceof EClass structured
								? structured : null;
					}
				}
				case ResourcePath.TypeCastSegment cast -> {
					out.add(cast);
					EClass castType = resolveCastType(cast.qualifiedName(), null);
					if (castType != null) {
						current = castType;
					}
					if (cast.key() != null) {
						collection = false;
					}
				}
				default -> {
					out.add(segment); // $count/$value/$ref — terminal, nothing folds after them
					collection = false;
				}
			}
		}
		return changed ? new ResourcePath(path.entitySet(), key, path.namedKeys(), out) : path;
	}

	/**
	 * Key aliases ([OData-URL] 4.3.2): {@code Products(@key)} takes the key literal from the
	 * query parameter {@code @key}. Substituted for the set key, named keys and segment keys;
	 * a referenced alias without a value is a client error (null return, 400 written).
	 */
	private ResourcePath resolveKeyAliases(ResourcePath path, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String key = aliasValue(path.key(), request, response);
		if (key == null && path.key() != null) {
			return null;
		}
		Map<String, String> namedKeys = new LinkedHashMap<>();
		for (Map.Entry<String, String> named : path.namedKeys().entrySet()) {
			String value = aliasValue(named.getValue(), request, response);
			if (value == null && named.getValue() != null) {
				return null;
			}
			namedKeys.put(named.getKey(), value);
		}
		List<ResourcePath.Segment> segments = new ArrayList<>(path.segments());
		for (int i = 0; i < segments.size(); i++) {
			if (segments.get(i) instanceof ResourcePath.PropertySegment(String name, String raw)
					&& raw != null && raw.startsWith("@")) {
				String value = aliasValue(raw, request, response);
				if (value == null) {
					return null;
				}
				segments.set(i, new ResourcePath.PropertySegment(name, value));
			}
		}
		return new ResourcePath(path.entitySet(), key, namedKeys, segments);
	}

	private String aliasValue(String rawKey, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (rawKey == null || !rawKey.startsWith("@")) {
			return rawKey;
		}
		String value = request.getParameter(rawKey);
		if (value == null || value.isBlank()) {
			error(response, HttpServletResponse.SC_BAD_REQUEST,
					"unresolved key alias '" + ODataJson.sanitize(rawKey) + "'");
			return null;
		}
		limits.checkExpression(value);
		return value.trim();
	}

	/** Folds a key value into the trailing keyless property/cast segment; false when keyed. */
	private static boolean foldKey(List<ResourcePath.Segment> segments, String value) {
		int last = segments.size() - 1;
		if (segments.get(last) instanceof ResourcePath.PropertySegment(String name, String key)
				&& key == null) {
			segments.set(last, new ResourcePath.PropertySegment(name, value));
			return true;
		}
		if (segments.get(last) instanceof ResourcePath.TypeCastSegment(String qualified, String key)
				&& key == null) {
			segments.set(last, new ResourcePath.TypeCastSegment(qualified, value));
			return true;
		}
		return false;
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
		return fetchByKey(target, rawKey, Map.of(), expand, response);
	}

	/**
	 * Fetches one entity by its key predicate: positional ({@code rawKey} against the single ID
	 * attribute) or compound ({@code namedKeys}, [OData-URL] compoundKey — composite keys and the
	 * named single-key form). The predicate is BUILT as a typed AST, never expression-parsed.
	 */
	private EObject fetchByKey(Target target, String rawKey, Map<String, String> namedKeys,
			Set<String> expand, HttpServletResponse response) throws IOException {
		OclExpression predicate;
		if (namedKeys.isEmpty()) {
			EAttribute keyAttribute = target.entityType().getEAllAttributes().stream()
					.filter(EAttribute::isID).findFirst().orElse(null);
			if (keyAttribute == null) {
				error(response, HttpServletResponse.SC_BAD_REQUEST, "entity set has no key");
				return null;
			}
			predicate = keyEquals(keyAttribute, rawKey);
		} else {
			predicate = compositeKeyEquals(target.entityType(), namedKeys);
		}
		QueryResult result = target.queryService().execute(new EntityQuery(target.entityType(),
				null, predicate, List.of(), 0, 1, false, expand));
		if (result.entities().isEmpty()) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
			return null;
		}
		return result.entities().get(0);
	}

	/**
	 * A compound key predicate as a typed AND-of-equalities AST. Every named component must be a
	 * key property and ALL key properties must be named ([OData-URL] compoundKey) — violations
	 * raise {@link IllegalArgumentException} (→ 400).
	 */
	private static OclExpression compositeKeyEquals(EClass entityType, Map<String, String> namedKeys) {
		List<EAttribute> keyAttributes = entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).toList();
		if (namedKeys.size() != keyAttributes.size()) {
			throw new IllegalArgumentException("the key predicate must name all "
					+ keyAttributes.size() + " key properties of " + entityType.getName());
		}
		OclExpression combined = null;
		for (Map.Entry<String, String> component : namedKeys.entrySet()) {
			EAttribute attribute = keyAttributes.stream()
					.filter(a -> a.getName().equals(component.getKey())).findFirst()
					.orElseThrow(() -> new IllegalArgumentException("'"
							+ ODataJson.sanitize(component.getKey()) + "' is not a key property"));
			OclExpression term = keyEquals(attribute, component.getValue());
			if (combined == null) {
				combined = term;
			} else {
				OperationCallExp and = OclFactory.eINSTANCE.createOperationCallExp();
				and.setName("and");
				and.setOwnedSource(combined);
				and.getOwnedArguments().add(term);
				combined = and;
			}
		}
		return combined;
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
						// 4.01 13.2.1/9.3+9.5: a parameterless bound function invoked WITHOUT
						// parentheses, unqualified (default namespace)
						if (last && property.key() == null
								&& hasBoundOperation(object.eClass(), property.name())) {
							invokeBoundFunction(object, object.eClass(), property.name(), "",
									request, response);
							return;
						}
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
				case ResourcePath.KeySegment(String value) -> {
					// only reaches the walk when key-as-segment could not fold it earlier;
					// on a collection it still selects, anywhere else it is not addressable
					current = current instanceof List<?> ? selectByKey(current, value) : null;
					if (current == null) {
						error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
						return;
					}
				}
				case ResourcePath.TypeCastSegment cast -> {
					EClass castClass = resolveCastType(cast.qualifiedName(), null);
					if (castClass == null) {
						// 4.01 13.2.1/9.3: a QUALIFIED parameterless bound function without parens
						String local = cast.qualifiedName()
								.substring(cast.qualifiedName().lastIndexOf('.') + 1);
						if (last && cast.key() == null && current instanceof EObject object
								&& hasBoundOperation(object.eClass(), local)) {
							invokeBoundFunction(object, object.eClass(), local, "", request, response);
							return;
						}
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
					// entity reference(s) of the addressed resource ([OData-Protocol] 11.2.8):
					// ids only, no entity content — the read counterpart of the $ref writes
					if (current == null) {
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						return;
					}
					if (current instanceof List<?> collection) {
						List<?> shaped = pageCollection(
								orderCollection(filterCollection(collection, request), request), request);
						StringBuilder json = envelopeHead(
								contextRoot(request) + "/$metadata#Collection($ref)");
						envelopeProperty(json).append("\"value\":[");
						for (int r = 0; r < shaped.size(); r++) {
							if (r > 0) {
								json.append(',');
							}
							json.append("{\"@odata.id\":\"")
									.append(ODataJson.sanitize(entityIdOf((EObject) shaped.get(r))))
									.append("\"}");
						}
						json.append("]}");
						response.setContentType(contentTypeJson());
						response.getWriter().write(json.toString());
						return;
					}
					if (current instanceof EObject entity && !(entity instanceof Enumerator)) {
						response.setContentType(contentTypeJson());
						response.getWriter().write(withContext(contextRoot(request) + "/$metadata#$ref",
								"{\"@odata.id\":\"" + ODataJson.sanitize(entityIdOf(entity)) + "\"}"));
						return;
					}
					error(response, HttpServletResponse.SC_BAD_REQUEST,
							"$ref requires an entity or an entity collection");
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
			response.setContentType(contentTypeJson());
			StringBuilder json = envelopeHead(ODataJson.sanitize(context));
			envelopeProperty(json).append("\"value\":\"")
					.append(ODataJson.sanitize(literal.getLiteral())).append("\"}");
			response.getWriter().write(json.toString());
			return;
		}
		if (value instanceof EObject object) {
			if (wantsXml(request)) {
				writeXmi(response, shaper.shapeAll(List.of(object), object.eClass(), null, Set.of()));
				return;
			}
			String json = entityJson(object, object.eClass(), null, Set.of());
			response.setContentType(contentTypeJson());
			response.getWriter().write(withContext(ODataJson.sanitize(context), json));
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
			StringBuilder json = envelopeHead(ODataJson.sanitize(context));
			envelopeProperty(json).append("\"value\":[");
			boolean first = true;
			for (Object member : collection) {
				if (!first) {
					json.append(',');
				}
				first = false;
				if (member instanceof EObject object) {
					json.append(entityJson(object, object.eClass(), null, Set.of()));
				} else {
					ODataJson.value(json, member, ieee754());
				}
			}
			json.append("]}");
			response.setContentType(contentTypeJson());
			response.getWriter().write(json.toString());
			return;
		}
		StringBuilder json = envelopeHead(ODataJson.sanitize(context));
		envelopeProperty(json).append("\"value\":");
		ODataJson.value(json, value instanceof java.util.Date date
				? java.time.format.DateTimeFormatter.ISO_INSTANT.format(date.toInstant()) : value,
				ieee754());
		json.append('}');
		response.setContentType(contentTypeJson());
		response.getWriter().write(json.toString());
	}

	/** {@code /{Set}({key})}: the key becomes a typed equality AST — never parsed as expression. */
	private void singleEntity(String setName, EObject entity, EClass entityType,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		// optimistic concurrency (13.1.1/26): the served ETag is the write preconditions' ETag
		response.setHeader("ETag", etagOf(entity, entityType));
		SelectTree select = selectOption(request, entityType);
		Map<String, ExpandItem> expand = expandOption(request, entityType);
		if (wantsXml(request)) {
			List<EObject> copies = shaper.shapeAll(List.of(entity), entityType, select,
					inlineNavs(expand));
			copies.forEach(copy -> applyNestedFilters(copy, expand));
			writeXmi(response, copies);
			return;
		}
		String json = entityJson(entity, entityType, select, expand);
		response.setContentType(contentTypeJson());
		response.getWriter().write(withContext(
				contextRoot(request) + "/$metadata#" + setName + "/$entity", json));
	}


	/** Wrapper/primitive number types a plain-number key literal may address. */
	private static final Set<Class<?>> NUMERIC_KEY_TYPES = Set.of(
			Integer.class, int.class, Long.class, long.class, Short.class, short.class,
			Byte.class, byte.class, java.math.BigInteger.class, java.math.BigDecimal.class);

	/**
	 * The key as a typed equality AST ({@code id = <literal>}) — never expression-parsed. A key
	 * literal whose FORM contradicts the key property's type (quoted string against a numeric key,
	 * plain number against a string key) is a malformed request → {@link IllegalArgumentException}
	 * (400), not a silent empty match (404) — the WCF stacks answer 400 here too.
	 */
	private static OperationCallExp keyEquals(EAttribute keyAttribute, String rawKey) {
		Class<?> instanceClass = keyAttribute.getEAttributeType() == null ? null
				: keyAttribute.getEAttributeType().getInstanceClass();
		boolean quoted = rawKey.length() >= 2 && rawKey.startsWith("'") && rawKey.endsWith("'");
		if (instanceClass != null) {
			if (quoted && NUMERIC_KEY_TYPES.contains(instanceClass)) {
				throw new IllegalArgumentException("the key literal must be numeric for '"
						+ keyAttribute.getName() + "'");
			}
			if (!quoted && rawKey.matches("-?\\d+") && instanceClass == String.class) {
				throw new IllegalArgumentException("the key literal must be a quoted string for '"
						+ keyAttribute.getName() + "'");
			}
		}
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
		StringBuilder json = envelopeHead(contextRoot(request) + "/$metadata#" + setName);
		if (result.totalCount() >= 0) {
			envelopeProperty(json).append("\"@odata.count\":").append(countValue(result.totalCount()));
		}
		envelopeProperty(json).append("\"value\":[");
		for (int i = 0; i < rows.size(); i++) {
			if (i > 0) {
				json.append(',');
			}
			ODataJson.value(json, rows.get(i), ieee754());
		}
		json.append(']');
		if (hasMore) {
			json.append(",\"@odata.nextLink\":\"")
					.append(ODataJson.sanitize(nextLink(request, skip + top))).append('"');
		}
		json.append('}');
		response.setContentType(contentTypeJson());
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
		String searchExpr = searchExpression(search, context);
		return filter == null || filter.isBlank() ? searchExpr
				: "(" + filter + ") and (" + searchExpr + ")";
	}

	/** The free-text term as a contains-OR over the type's string properties (OData syntax). */
	private static String searchExpression(String search, EClass context) {
		String literal = "'" + search.trim().replace("'", "''") + "'";
		return context.getEAllAttributes().stream()
				.filter(a -> !a.isMany() && a.getEAttributeType() != null
						&& String.class.equals(a.getEAttributeType().getInstanceClass()))
				.map(a -> "contains(" + a.getName() + "," + literal + ")")
				.reduce((l, r) -> l + " or " + r)
				.orElse("false"); // no string properties → matches nothing
	}

	/**
	 * {@code $compute} (13.1.2 SHOULD) parsed by reusing the {@code $apply} {@code compute(…)}
	 * grammar into a one-stage pipeline. The pipeline is reused both to make the aliases referable
	 * from {@code $filter}/{@code $orderby}/{@code $select} and to splice the computed members into
	 * the response (evaluated per entity), so it stays backend-agnostic. Null when no {@code $compute}.
	 */
	private ApplyPipeline computePipeline(HttpServletRequest request, EClass context) {
		String compute = option(request, "$compute");
		if (compute == null || compute.isBlank()) {
			return null;
		}
		limits.checkExpression(compute);
		return parser.parseApply("compute(" + compute + ")", context);
	}

	private List<ComputeExpression> computeExpressions(ApplyPipeline computePipeline) {
		if (computePipeline == null) {
			return List.of();
		}
		return ((ComputeTransformation) computePipeline.getTransformations().get(0))
				.getComputeExpressions();
	}

	/** Alias → defining expression for every {@code $compute} item (insertion order). */
	private Map<String, OclExpression> computeAliasMap(ApplyPipeline computePipeline) {
		Map<String, OclExpression> map = new LinkedHashMap<>();
		for (ComputeExpression compute : computeExpressions(computePipeline)) {
			map.put(compute.getAlias(), compute.getExpression());
		}
		return map;
	}

	/**
	 * The computes to splice into the response: all of them when there is no {@code $select},
	 * otherwise only those whose alias appears as a top-level {@code $select} token (projection).
	 */
	private List<ComputeExpression> selectedComputes(ApplyPipeline computePipeline,
			HttpServletRequest request, Map<String, OclExpression> computeAliases) {
		List<ComputeExpression> all = computeExpressions(computePipeline);
		String select = option(request, "$select");
		if (select == null || select.isBlank() || all.isEmpty()) {
			return all;
		}
		Set<String> selected = new HashSet<>();
		for (String token : SelectTree.splitTopLevel(select, ',')) {
			selected.add(token.trim());
		}
		return all.stream().filter(c -> selected.contains(c.getAlias())).toList();
	}

	/**
	 * Rewrites a parsed {@code $filter}/{@code $orderby} tree so every reference to a {@code $compute}
	 * alias (a {@link VariableExp}) is replaced by a copy of the alias' defining expression. The
	 * result references only real properties, so it pushes down to the backend like any other filter.
	 */
	private OclExpression inlineComputeAliases(OclExpression expression,
			Map<String, OclExpression> computeAliases) {
		if (expression == null || computeAliases.isEmpty()) {
			return expression;
		}
		if (aliasOf(expression, computeAliases) != null) { // the whole expression IS an alias reference
			return EcoreUtil.copy(computeAliases.get(aliasOf(expression, computeAliases)));
		}
		List<VariableExp> references = new ArrayList<>();
		expression.eAllContents().forEachRemaining(node -> {
			if (node instanceof VariableExp variable && aliasOf(variable, computeAliases) != null) {
				references.add(variable);
			}
		});
		for (VariableExp reference : references) {
			EcoreUtil.replace(reference, EcoreUtil.copy(computeAliases.get(aliasOf(reference, computeAliases))));
		}
		return expression;
	}

	private List<OrderBySegment> inlineOrderBy(List<OrderBySegment> segments,
			Map<String, OclExpression> computeAliases) {
		if (segments == null || computeAliases.isEmpty()) {
			return segments;
		}
		List<OrderBySegment> inlined = new ArrayList<>();
		for (OrderBySegment segment : segments) {
			inlined.add(new OrderBySegment(
					inlineComputeAliases(segment.expression(), computeAliases), segment.ascending()));
		}
		return inlined;
	}

	/** The compute-alias name a node refers to (when it is a {@link VariableExp} for one), else null. */
	private static String aliasOf(Object node, Map<String, OclExpression> computeAliases) {
		if (node instanceof VariableExp variable) {
			Variable referred = variable.getReferredVariable();
			if (referred != null && computeAliases.containsKey(referred.getName())) {
				return referred.getName();
			}
		}
		return null;
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
		// container set names may differ from their types (TripPin People -> Person): the read
		// path captures them as an EPackage annotation the runtime honours. The container can
		// live in a DIFFERENT schema than the types (Northwind), so mapping and type resolve
		// across all packages independently.
		String typeName = setName;
		for (EPackage pkg : packages) {
			EAnnotation sets = pkg.getEAnnotation(ODataAnnotationConstants.ENTITY_SETS_SOURCE);
			if (sets != null && sets.getDetails().containsKey(setName)) {
				typeName = sets.getDetails().get(setName);
				break;
			}
		}
		for (EPackage pkg : packages) {
			if (pkg.getEClassifier(typeName) instanceof EClass eClass && !eClass.isAbstract()) {
				return eClass;
			}
		}
		return null;
	}

	private List<String> entitySetNames() {
		List<String> names = new ArrayList<>();
		for (EPackage pkg : packages) {
			EAnnotation sets = pkg.getEAnnotation(ODataAnnotationConstants.ENTITY_SETS_SOURCE);
			Map<String, String> renamed = new HashMap<>(); // type name -> set name
			if (sets != null) {
				sets.getDetails().forEach(entry -> renamed.put(entry.getValue(), entry.getKey()));
			}
			pkg.getEClassifiers().stream()
					.filter(EClass.class::isInstance).map(EClass.class::cast)
					.filter(c -> !c.isAbstract())
					.map(c -> renamed.getOrDefault(c.getName(), c.getName()))
					.forEach(names::add);
		}
		return names.stream().sorted().toList();
	}

	// --- $select / $expand / formats ---

	private SelectTree selectOption(HttpServletRequest request, EClass entityType) {
		return selectOption(request, entityType, Set.of());
	}

	/**
	 * Validated {@code $select} tree (nested selects incl., 4.01), or null when absent. Any top-level
	 * token that names a {@code $compute} alias is stripped before model validation (the computed
	 * member is projected separately); a {@code $select} of only aliases leaves no real-property
	 * projection constraint (null).
	 */
	private SelectTree selectOption(HttpServletRequest request, EClass entityType,
			Set<String> computeAliases) {
		String select = option(request, "$select");
		if (select == null || select.isBlank()) {
			return null;
		}
		limits.checkExpression(select); // nested trees are parsed — same hostile-input guard
		if (!computeAliases.isEmpty()) {
			List<String> realProperties = new ArrayList<>();
			for (String token : SelectTree.splitTopLevel(select, ',')) {
				if (!computeAliases.contains(token.trim())) {
					realProperties.add(token);
				}
			}
			if (realProperties.isEmpty()) {
				return null;
			}
			select = String.join(",", realProperties);
		}
		// nested collection options over selected collections ([OData-URL] 5.1.3, 4.01 Advanced
		// §13.2.3/5.1–5.4) parse through the same guarded parser as every other expression
		return SelectTree.parse(select, entityType, nestedOptionParser);
	}

	/**
	 * One validated {@code $expand} item ([OData-URL] 5.1.3):
	 *
	 * @param options nested collection options ({@code $filter}/{@code $search}/{@code $orderby}/
	 *                {@code $top}/{@code $skip}/{@code $count} — Advanced 9.2/9.4–9.7)
	 * @param refOnly {@code nav/$ref} ([OData-URL] 5.1.3.1): only entity REFERENCES are
	 *                expanded — the response carries {@code {"@odata.id": …}} objects
	 * @param cast    {@code nav/Ns.Type} (5.1.3.2): only related instances of the derived type
	 *                are expanded; null without a cast
	 */
	private record ExpandItem(CollectionOptions options, boolean refOnly, EClass cast) {
	}

	/** The guarded parser behind every expression-valued nested option. */
	private final NestedOptionParser nestedOptionParser = new NestedOptionParser() {
		@Override
		public OclExpression filter(String expression, EClass context) {
			limits.checkExpression(expression);
			return parser.parseFilter(expression, context);
		}

		@Override
		public List<OrderBySegment> orderBy(String expression, EClass context) {
			limits.checkExpression(expression);
			return parser.parseOrderBy(expression, context);
		}

		@Override
		public OclExpression search(String term, EClass context) {
			return filter(searchExpression(term, context), context);
		}
	};

	/**
	 * Validated {@code $expand} items: navigation name → {@link ExpandItem}. Supported item
	 * shapes: {@code nav}, {@code nav($filter=…)}, {@code nav/$ref} and {@code nav/Ns.Type}
	 * (optionally with a nested {@code $filter} against the derived type); other nested
	 * options answer 501.
	 */
	private Map<String, ExpandItem> expandOption(HttpServletRequest request, EClass entityType) {
		String expand = option(request, "$expand");
		Map<String, ExpandItem> items = new LinkedHashMap<>();
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
			boolean refOnly = false;
			String castName = null;
			int slash = name.indexOf('/');
			if (slash >= 0) { // nav/$ref or nav/Ns.Type
				String suffix = name.substring(slash + 1).trim();
				name = name.substring(0, slash).trim();
				if ("$ref".equals(suffix)) {
					refOnly = true;
				} else {
					castName = suffix;
				}
			}
			if (!(entityType.getEStructuralFeature(name) instanceof EReference reference)) {
				throw new ODataQueryParseException("unknown $expand navigation '" + name + "'");
			}
			if (refOnly && nested != null) {
				throw new UnsupportedOperationException(
						"options on $expand=nav/$ref are not implemented");
			}
			EClass cast = null;
			if (castName != null) {
				cast = resolveCastType(castName, null);
				if (cast == null || !reference.getEReferenceType().isSuperTypeOf(cast)) {
					throw new ODataQueryParseException("'" + castName
							+ "' is not a derived type of the '" + name + "' navigation");
				}
			}
			items.put(name, new ExpandItem(
					nested == null ? CollectionOptions.NONE
							: expandItemOptions(nested, reference,
									cast != null ? cast : reference.getEReferenceType()),
					refOnly, cast));
		}
		return items;
	}

	/**
	 * The {@code ;}-separated option list of one {@code $expand} item — the collection options
	 * (Advanced 9.2/9.4–9.7); {@code $select}/{@code $expand}/{@code $levels}/{@code $compute}
	 * inside {@code $expand} answer 501.
	 */
	private CollectionOptions expandItemOptions(String optionList, EReference reference,
			EClass context) {
		CollectionOptions.Accumulator options = new CollectionOptions.Accumulator();
		for (String option : SelectTree.splitTopLevel(optionList, ';')) {
			String trimmed = option.trim();
			if (!options.accept(trimmed, reference, context, nestedOptionParser)) {
				throw new UnsupportedOperationException(
						"this nested $expand option is not implemented");
			}
		}
		return options.build();
	}

	/** The navigations rendered INLINE — everything except the {@code /$ref} items. */
	private static Set<String> inlineNavs(Map<String, ExpandItem> expand) {
		return expand.entrySet().stream()
				.filter(item -> !item.getValue().refOnly())
				.map(Map.Entry::getKey)
				.collect(Collectors.toCollection(LinkedHashSet::new));
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

	/**
	 * Nested {@code $expand} casts and collection options run on the SHAPED copy — never on
	 * backend objects. Returns the requested inline counts (navigation → filtered, pre-paging
	 * count) for the {@code name@odata.count} response members.
	 */
	private Map<String, Long> applyNestedFilters(EObject copy, Map<String, ExpandItem> expand) {
		Map<String, Long> counts = new LinkedHashMap<>();
		for (Map.Entry<String, ExpandItem> entry : expand.entrySet()) {
			ExpandItem item = entry.getValue();
			if (item.refOnly() || (item.cast() == null && item.options().isNone())) {
				continue;
			}
			EStructuralFeature feature = copy.eClass().getEStructuralFeature(entry.getKey());
			if (feature == null) {
				continue;
			}
			if (copy.eGet(feature) instanceof List<?> children) {
				if (item.cast() != null) { // cast-in-expand: only derived instances stay (5.1.3.2)
					children.removeIf(child -> !item.cast().isInstance(child));
				}
				long total = shaper.applyOptions(children, item.options());
				if (item.options().count()) {
					counts.put(entry.getKey(), total);
				}
			} else if (item.cast() != null && copy.eGet(feature) instanceof EObject child
					&& !item.cast().isInstance(child)) {
				copy.eUnset(feature); // single-valued: a non-matching instance is not expanded
			}
		}
		return counts;
	}

	private String entityJson(EObject entity, EClass entityType, SelectTree select, Set<String> expand)
			throws IOException {
		return serializeEntity(entity, shaper.shape(entity, entityType, select, expand, null),
				entityType, expand);
	}

	/** {@link #entityJson} for parsed expand specs: applies nested casts/options after shaping. */
	private String entityJson(EObject entity, EClass entityType, SelectTree select,
			Map<String, ExpandItem> expand) throws IOException {
		Set<String> inline = inlineNavs(expand);
		Map<String, Long> counts = new LinkedHashMap<>();
		EObject copy = shaper.shape(entity, entityType, select, inline, null, counts);
		counts.putAll(applyNestedFilters(copy, expand));
		return withExpandedRefs(withNestedCounts(
				serializeEntity(entity, copy, entityType, inline), counts), entity, expand);
	}

	/**
	 * Splices the requested inline counts of nested collections ({@code $expand}/{@code $select}
	 * {@code $count=true}) as {@code name@odata.count} members ([OData-JSON] 4.5.5; strings
	 * under IEEE754Compatible like every Edm.Int64 count).
	 */
	private String withNestedCounts(String entityJson, Map<String, Long> counts) {
		if (counts.isEmpty()) {
			return entityJson;
		}
		StringBuilder members = new StringBuilder();
		counts.forEach((name, total) -> members.append(",\"").append(name)
				.append("@odata.count\":").append(countValue(total)));
		String inner = entityJson.substring(1, entityJson.length() - 1);
		return "{" + (inner.isEmpty() ? members.substring(1) : inner + members) + "}";
	}

	/**
	 * Splices {@code $expand=nav/$ref} members into the entity JSON ([OData-URL] 5.1.3.1):
	 * entity-reference objects built from the ORIGINAL entity's navigation values — full
	 * entities are neither shaped nor serialized for these navigations.
	 */
	private String withExpandedRefs(String entityJson, EObject entity,
			Map<String, ExpandItem> expand) {
		StringBuilder members = new StringBuilder();
		for (Map.Entry<String, ExpandItem> entry : expand.entrySet()) {
			if (!entry.getValue().refOnly()) {
				continue;
			}
			EStructuralFeature feature = entity.eClass().getEStructuralFeature(entry.getKey());
			if (feature == null) {
				continue;
			}
			members.append(",\"").append(entry.getKey()).append("\":");
			if (entity.eGet(feature) instanceof List<?> children) {
				members.append('[');
				for (int i = 0; i < children.size(); i++) {
					if (i > 0) {
						members.append(',');
					}
					members.append("{\"@odata.id\":\"")
							.append(ODataJson.sanitize(entityIdOf((EObject) children.get(i))))
							.append("\"}");
				}
				members.append(']');
			} else if (entity.eGet(feature) instanceof EObject child) {
				members.append("{\"@odata.id\":\"")
						.append(ODataJson.sanitize(entityIdOf(child))).append("\"}");
			} else {
				members.append("null"); // no related entity ([OData-JSON] expanded references)
			}
		}
		if (members.isEmpty()) {
			return entityJson;
		}
		String inner = entityJson.substring(1, entityJson.length() - 1);
		return "{" + (inner.isEmpty() ? members.substring(1) : inner + members) + "}";
	}

	private String serializeEntity(EObject entity, EObject copy, EClass entityType,
			Set<String> expand) throws IOException {
		boolean full = "full".equals(responseMetadataLevel());
		// full metadata: the default codec profile emits @odata.type/@odata.id per entity;
		// minimal: control info that is computable from the context URL is left out ([OData-JSON] 3.1)
		ODataJsonResourceImpl resource = full
				? new ODataJsonResourceImpl(URI.createURI("response.odatajson"), metadataService, expand)
				: ODataJsonResourceImpl.minimalMetadata(
						URI.createURI("response.odatajson"), metadataService, expand);
		resource.ieee754Compatible(ieee754());
		resource.getContents().add(copy);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, null);
		String json = out.toString(StandardCharsets.UTF_8);
		if (!full && !omitContext() && entity.eClass() != entityType) {
			// derived instance under minimal metadata: the type is NOT computable from the context
			// URL, so transport the single-field discriminator ([OData-JSON] 4.5.8). Full metadata
			// already carries @odata.type; metadata=none MUST omit it like all control information.
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

	/** The service root: the request URI without the resource path (not just its last segment). */
	private static String contextRoot(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String pathInfo = request.getPathInfo();
		if (pathInfo != null && !pathInfo.isEmpty() && uri.endsWith(pathInfo)) {
			return uri.substring(0, uri.length() - pathInfo.length());
		}
		return uri.replaceFirst("/[^/]*$", "");
	}



	private void error(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(ODataJson.error(status, message));
	}
}
