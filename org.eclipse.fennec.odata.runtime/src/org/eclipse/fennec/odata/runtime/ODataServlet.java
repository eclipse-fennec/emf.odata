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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
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
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.operation.api.ODataOperationHandler;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.DeltaGoneException;
import org.eclipse.fennec.odata.persistence.api.DeltaService;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.MediaService;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.odata.query.CachingODataQueryParser;
import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.ODataResourceParser;
import org.eclipse.fennec.odata.query.OclEvaluator;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.odata.query.ResourcePath;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TPropertyValue;
import org.open.oasis.docs.odata.ns.edm.TRecordExpression;
import org.open.oasis.docs.odata.ns.edmx.EdmxFactory;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;
import org.open.oasis.docs.odata.ns.edmx.TInclude;
import org.open.oasis.docs.odata.ns.edmx.TReference;
import org.open.oasis.docs.odata.ns.edmx.TVersion;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletPattern;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

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
 * expression length, parenthesis nesting depth (parser-bomb guard), an enforced {@code $top}
 * ceiling (applied even when the client sends none) and a {@code $batch} operation cap; the
 * defaults and configuration keys live in {@link RequestLimits} and the servlet PID. There is no
 * string concatenation into any backend — the ONLY query path is the typed OCL IR, unknown
 * properties/functions fail the parse (400). Error responses carry sanitized messages, never stack
 * traces or exception class names; unexpected failures answer with a generic 500.
 */
@RequireHttpWhiteboard
@HttpWhiteboardServletPattern("/odata/*")
@HttpWhiteboardServletName("Fennec OData")
@Component(service = Servlet.class, configurationPid = ODataServlet.PID)
public class ODataServlet extends HttpServlet {

	public static final String PID = "org.eclipse.fennec.odata.servlet";
	private static final long serialVersionUID = 1L;

	private static final System.Logger LOGGER = System.getLogger(ODataServlet.class.getName());

	/**
	 * Jackson mapper: immutable-config and thread-safe, so it is created ONCE and shared rather
	 * than per request (Jackson's documented reuse contract).
	 */
	static final ObjectMapper JSON = new ObjectMapper(); // shared with the extracted dispatchers

	final ODataResourceParser resourceParser = new ODataResourceParser();

	final List<EPackage> packages = new CopyOnWriteArrayList<>();
	final List<QueryService> queryServices = new CopyOnWriteArrayList<>();
	final List<WriteService> writeServices = new CopyOnWriteArrayList<>();
	private final List<MediaService> mediaServices = new CopyOnWriteArrayList<>();
	final List<DeltaService> deltaServices = new CopyOnWriteArrayList<>();
	final List<ODataOperationHandler> operationHandlers = new CopyOnWriteArrayList<>();
	final CachingODataQueryParser parser = new CachingODataQueryParser();
	private final OclEvaluator expandFilterEvaluator = new OclEvaluator();
	/** Schema namespace/alias per package for cast resolution — same derivation as $metadata. */
	final Map<EPackage, ODataPackageProfile> profiles =
			Collections.synchronizedMap(new java.util.WeakHashMap<>());
	final EntityShaper shaper = new EntityShaper();

	volatile MetadataService metadataService;
	volatile RequestLimits limits = RequestLimits.DEFAULTS;
	private final BatchDispatcher batchDispatcher = new BatchDispatcher(this);
	final AsyncDispatcher asyncDispatcher = new AsyncDispatcher(this);
	private final WriteDispatcher writeDispatcher = new WriteDispatcher(this);
	final OperationDispatcher operations = new OperationDispatcher(this);
	private final DeltaDispatcher deltas = new DeltaDispatcher(this);
	final ResponseFormatter formats = new ResponseFormatter(this);

	/**
	 * CORS origin(s) served to browser clients (e.g. the XOData explorer): {@code "*"} or a
	 * space-separated allowlist; EMPTY (the default) disables CORS entirely.
	 */
	private volatile String corsOrigin = "";

	/** Default maximum of concurrently EXECUTING respond-async requests (secure default). */
	static final int DEFAULT_MAX_ASYNC_INFLIGHT = 16;
	/** Default maximum of PARKED async status monitors (LRU; unretrieved results age out). */
	static final int DEFAULT_MAX_ASYNC_MONITORS = 100;

	/**
	 * Bounds concurrently executing respond-async requests. {@code null} = unbounded (the
	 * {@code odata.max.async.inflight <= 0} foot-gun). Rebuilt on {@link #activate} before serving.
	 */
	volatile Semaphore asyncInflight =
			new Semaphore(DEFAULT_MAX_ASYNC_INFLIGHT);
	volatile int maxAsyncMonitors = DEFAULT_MAX_ASYNC_MONITORS;

	@Activate
	void activate(Map<String, Object> configuration) {
		limits = RequestLimits.fromConfiguration(configuration);
		Object origin = configuration.get("odata.cors.origin");
		corsOrigin = origin == null ? "" : String.valueOf(origin).trim();
		int inflight = intConfig(configuration, "odata.max.async.inflight", DEFAULT_MAX_ASYNC_INFLIGHT);
		asyncInflight = inflight > 0 ? new Semaphore(inflight) : null;
		maxAsyncMonitors = intConfig(configuration, "odata.max.async.monitors", DEFAULT_MAX_ASYNC_MONITORS);
	}

	private static int intConfig(Map<String, Object> configuration, String key, int fallback) {
		Object value = configuration == null ? null : configuration.get(key);
		return value == null ? fallback : Integer.parseInt(String.valueOf(value));
	}

	/**
	 * Plain {@link HttpServlet} dispatch (doGet/doPost/…), BYPASSING this servlet's own
	 * {@code service} override — the async worker re-enters here so respond-async cannot
	 * recurse into itself.
	 */
	void dispatchDirectly(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		super.service(request, response);
	}

	@Deactivate
	void deactivate() {
		asyncDispatcher.shutdown();
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
	static final Set<String> DELTA_LINK_OPTIONS = Set.of(
			"$filter", "$search", "$select", "$compute", "$format", "$expand");

	/** The {@code odata.metadata=} format parameter of an Accept header or {@code $format}. */
	private static final Pattern METADATA_PARAM =
			Pattern.compile("odata\\.metadata\\s*=\\s*(full|minimal|none)",
					Pattern.CASE_INSENSITIVE);

	/**
	 * The response metadata level for the current request, carried request-scoped so the many
	 * response writers do not each need the request threaded through. Set in {@link #service} and
	 * cleared afterwards; {@code null} outside a request means {@code minimal}.
	 */
	static final ThreadLocal<String> METADATA_LEVEL = new ThreadLocal<>();

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
			Matcher matcher = METADATA_PARAM.matcher(source);
			if (matcher.find() && !matcher.group(1).equalsIgnoreCase("minimal")) {
				return matcher.group(1).toLowerCase(Locale.ROOT);
			}
		}
		return "minimal";
	}

	static String responseMetadataLevel() {
		String level = METADATA_LEVEL.get();
		return level == null ? "minimal" : level;
	}

	/** The JSON content type carrying the current request's metadata level. */
	static String contentTypeJson() {
		return "application/json;odata.metadata=" + responseMetadataLevel()
				+ (ieee754() ? ";IEEE754Compatible=true" : "") + ";charset=UTF-8";
	}

	/**
	 * {@code odata.metadata=none} ([OData-JSON] 3.1.1): the payload MUST omit all control
	 * information other than {@code @odata.nextLink} and {@code @odata.count} — no context URL,
	 * no type discriminators.
	 */
	static boolean omitContext() {
		return "none".equals(responseMetadataLevel());
	}

	static final Pattern IEEE754_PARAM =
			Pattern.compile("IEEE754Compatible\\s*=\\s*(true|false)",
					Pattern.CASE_INSENSITIVE);

	/** Whether the current response runs {@code IEEE754Compatible=true}; request-scoped like the metadata level. */
	static final ThreadLocal<Boolean> IEEE754 = new ThreadLocal<>();

	/** {@code IEEE754Compatible=true} requested via {@code $format} or {@code Accept} ([OData-JSON] 8.1). */
	private static boolean ieee754Requested(HttpServletRequest request) {
		String source = request.getParameter("$format");
		if (source == null || source.isBlank()) {
			source = request.getHeader("Accept");
		}
		if (source == null) {
			return false;
		}
		Matcher matcher = IEEE754_PARAM.matcher(source);
		return matcher.find() && "true".equalsIgnoreCase(matcher.group(1));
	}

	static boolean ieee754() {
		return Boolean.TRUE.equals(IEEE754.get());
	}

	/** {@code @odata.count} is Edm.Int64 — a string under {@code IEEE754Compatible=true}. */
	static String countValue(long count) {
		return ieee754() ? "\"" + count + "\"" : Long.toString(count);
	}

	/** Weaves the context annotation into an entity object (single entities have no envelope). */
	static String withContext(String contextUrl, String entityJson) {
		if (omitContext()) {
			return entityJson;
		}
		return "{\"@odata.context\":\"" + contextUrl + "\"," + entityJson.substring(1);
	}

	/** Collection-envelope head: an opening brace plus the context property — context-free under metadata=none. */
	static StringBuilder envelopeHead(String contextUrl) {
		StringBuilder head = new StringBuilder("{");
		if (!omitContext()) {
			head.append("\"@odata.context\":\"").append(contextUrl).append('"');
		}
		return head;
	}

	/** Separates the next top-level envelope property unless it is the first one. */
	static StringBuilder envelopeProperty(StringBuilder envelope) {
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
		// content negotiation: an Accept header that lists ONLY media types we never produce is
		// unsatisfiable → 406. Only the JSON/XML-producing paths are checked; the paths whose
		// content type is path-dependent are exempt — /$count (text/plain), /$value (a media
		// entity's own content type, arbitrary) and the /$async/ monitor (application/http).
		if (!path.startsWith("/$async/") && !path.endsWith("/$count") && !path.endsWith("/$value")
				&& ResponseFormatter.notAcceptable(request)) {
			error(response, 406, "no acceptable representation for the requested media type");
			return;
		}
		try {
			if ("/".equals(path) || path.isEmpty()) {
				serviceDocument(request, response);
			} else if ("/$metadata".equals(path)) {
				metadataDocument(request, response);
			} else if (path.startsWith("/$async/")) {
				asyncDispatcher.monitor(path.substring("/$async/".length()), request, response); // 11.6
			} else {
				resource(path.substring(1), request, response);
			}
		} catch (ODataQueryParseException e) {
			// the query/parse layer curates its client-facing messages (never internals)
			error(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} catch (IllegalArgumentException e) {
			// a raw IAE reaching here (e.g. an EMF value-conversion fault) is NOT a curated message
			// and may carry internal detail — log it, answer a fixed generic 400
			LOGGER.log(System.Logger.Level.WARNING,
					() -> "bad request serving GET " + request.getRequestURI(), e);
			error(response, HttpServletResponse.SC_BAD_REQUEST, "the request could not be processed");
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
			throws ServletException, IOException {
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
				batchDispatcher.execute(request, response);
				return;
			}
			switch (request.getMethod()) {
				case "GET" -> {
					// Prefer: respond-async ([OData-Protocol] 11.6, Advanced SHOULD 13.1.3/13):
					// the request runs to completion, only DELIVERY moves to a status monitor
					if (AsyncDispatcher.requested(request) && !pathInfo.startsWith("/$async")) {
						asyncDispatcher.accept(request, response);
					} else {
						super.service(request, response);
					}
				}
				case "POST", "PATCH", "PUT", "DELETE" -> writeDispatcher.execute(request, response);
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
		if (!"*".equals(allowed)) {
			// the response varies by Origin (allowlist echo) — a shared cache must not hand one
			// origin's Access-Control-Allow-Origin to another
			response.setHeader("Vary", "Origin");
		}
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

	// --- optimistic concurrency (13.1.1/26): weak ETags from the serialized state ---

	/**
	 * Enforces {@code If-Match} on updates/deletes of EXISTING entities: our single-entity
	 * GETs return an ETag, so clients must send the precondition (11.4.1.1) — absent → 428,
	 * mismatch → 412. Upserts of absent entities pass. Without a read backend the check is
	 * skipped (no ETag was ever served).
	 */
	/** {@link #preconditionHolds(EClass, String, HttpServletRequest, HttpServletResponse)} for a parsed path (compound-key aware). */
	boolean preconditionHolds(EClass entityType, ResourcePath path,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (path.namedKeys().isEmpty()) {
			return preconditionHolds(entityType, path.key(), request, response);
		}
		return preconditionHolds(entityType,
				currentEntity(entityType, compositeKeyEquals(entityType, path.namedKeys())),
				request, response);
	}

	boolean preconditionHolds(EClass entityType, String rawKey,
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
					formats.entityJson(entity, entityType, null, Set.of())
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
	EObject currentEntity(EClass entityType, String rawKey) {
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


	/** The response is 4.01 unless the client pins {@code OData-MaxVersion: 4.0} (8.1.5). */
	static String negotiateVersion(HttpServletRequest request) {
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
		SelectTree select = formats.selectOption(request, type);
		Map<String, ResponseFormatter.ExpandItem> expand = formats.expandOption(request, type);
		if (formats.wantsXml(request)) {
			List<EObject> copies = shaper.shapeAll(List.of(entity), type, select, ResponseFormatter.shapePaths(expand));
			copies.forEach(copy -> formats.applyNestedFilters(copy, expand));
			formats.writeXmi(response, copies);
			return;
		}
		String json = formats.entityJson(entity, type, select, expand);
		response.setContentType(contentTypeJson());
		response.getWriter().write(withContext(
				contextRoot(request) + "/$metadata#" + name, json));
	}

	/** Whether the client asked for the CSDL <b>JSON</b> representation of {@code $metadata} (4.01). */
	private static boolean wantsJsonMetadata(HttpServletRequest request) {
		String format = request.getParameter("$format");
		if (format != null && !format.isBlank()) {
			String normalized = format.trim().toLowerCase(Locale.ROOT);
			return normalized.equals("json") || normalized.startsWith("application/json");
		}
		String accept = request.getHeader("Accept");
		return accept != null && accept.toLowerCase(Locale.ROOT).contains("application/json");
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
						boolCapability("Org.OData.Capabilities.V1.AsynchronousRequestsSupported", true));
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

	record Target(EClass entityType, QueryService queryService) {
	}

	Target resolveTarget(String setName, HttpServletResponse response) throws IOException {
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
			deltas.deltaResponse(setName, castName, castType, target, request, response);
			return;
		}
		// a cast makes the DERIVED type the context: its properties are addressable in options
		EClass context = castType != null ? castType : target.entityType();
		boolean xml = formats.wantsXml(request);
		// $compute defines dynamic aliases that may be referenced from $filter/$orderby/$select
		ApplyPipeline computePipeline = computePipeline(request, context);
		Map<String, OclExpression> computeAliases = computeAliasMap(computePipeline);
		SelectTree select = formats.selectOption(request, context, computeAliases.keySet());
		Map<String, ResponseFormatter.ExpandItem> expand = formats.expandOption(request, context);
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
				ResponseFormatter.shapePaths(expand)); // backends prefetch expanded navigations (no N+1, no lazy proxies)

		// change tracking ([OData-Protocol] 11.3): a preference, applied only when the backend
		// can track this type. Expanding defining queries additionally need an expand-capable
		// backend AND a 4.01 client — 4.0 REQUIRES the flattened delta payload we do not emit.
		// The token is taken BEFORE the query runs — a write racing the read is re-reported in
		// the first delta rather than lost.
		DeltaService candidate = DeltaDispatcher.trackChangesRequested(request)
				? deltas.deltaService(target.entityType(), target.queryService()) : null;
		DeltaService deltaService = candidate != null && (expand.isEmpty()
				|| (candidate.supportsExpandTracking() && !"4.0".equals(negotiateVersion(request))))
				? candidate : null;
		String deltaToken = deltaService == null ? null
				: deltaService.trackingToken(target.entityType());

		QueryResult result = target.queryService().execute(query);
		boolean hasMore = result.entities().size() > top;
		List<EObject> page = hasMore ? result.entities().subList(0, top) : result.entities();

		if (xml) { // XMI is a non-OData projection — trimmed, but without an embedded link
			List<EObject> copies = shaper.shapeAll(page, context, select, ResponseFormatter.shapePaths(expand));
			copies.forEach(copy -> formats.applyNestedFilters(copy, expand));
			formats.writeXmi(response, copies);
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
			json.append(withComputed(formats.entityJson(page.get(i), context, select, expand),
					page.get(i), computes));
		}
		json.append(']');
		if (hasMore) {
			json.append(",\"@odata.nextLink\":\"")
					.append(ODataJson.sanitize(nextLink(request, skip + top))).append('"');
		} else if (deltaToken != null) { // the delta link replaces the next link on the LAST page
			response.setHeader("Preference-Applied", "odata.track-changes");
			json.append(",\"@odata.deltaLink\":\"")
					.append(ODataJson.sanitize(deltas.deltaLink(request, deltaToken))).append('"');
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

	/**
	 * The key predicate for an entity id from the removal's key values: string values quoted
	 * ({@code ''}-escaped), composite keys as named pairs — the same forms {@code keyEquals}
	 * accepts back.
	 */
	/** The canonical entity id ({@code Set(key)}) — used by `$ref` reads and reference payloads. */
	String entityIdOf(EObject entity) {
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

	static String keyLiteral(Map<String, Object> keyValues) {
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
		if (OperationDispatcher.isFunctionCall(rawPath)
				&& resolveEntityType(rawPath.substring(0, rawPath.indexOf('('))) == null) {
			operations.functionImport(rawPath, request, response); // GET FuncName(p=…) — the resource parser
			return;                                      // deliberately does not model function segments
		}
		// 4.01 13.2.1/9.3: a parameterless function import invoked WITHOUT parentheses — a bare
		// name that is neither a set nor a singleton but an unbound operation
		if (rawPath.indexOf('/') < 0 && rawPath.indexOf('(') < 0
				&& resolveEntityType(rawPath) == null && resolveSingleton(rawPath) == null
				&& operations.resolveUnboundFunction(rawPath) != null) {
			operations.functionImport(rawPath + "()", request, response);
			return;
		}
		if (OperationDispatcher.isBoundFunctionCall(rawPath)) {
			operations.boundFunction(rawPath, request, response); // GET Set(key)/Ns.Func(p=…)
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
				path.segments().isEmpty() ? formats.expandOption(request, target.entityType()).keySet()
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
	static boolean hasStream(EClass entityType) {
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
	void mediaWrite(EClass entityType, ResourcePath path, HttpServletRequest request,
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
		Target target = resolveTarget(setName, response);
		if (target == null) {
			return;
		}
		if (option(request, "$deltatoken") != null) {
			deltas.deltaCount(target, castType, request, response); // /$count on a delta link (11.3.2 MAY)
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
					rest.isEmpty() ? formats.expandOption(request, castType).keySet()
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
	ResourcePath keyAsSegment(ResourcePath path) {
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
	ResourcePath resolveKeyAliases(ResourcePath path, HttpServletRequest request,
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
	EClass resolveCastType(String qualifiedName, EClass baseType) {
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
	EObject fetchByKey(Target target, String rawKey, Map<String, String> namedKeys,
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
								&& OperationDispatcher.hasBoundOperation(object.eClass(), property.name())) {
							operations.invokeBoundFunction(object, object.eClass(), property.name(), "",
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
								&& OperationDispatcher.hasBoundOperation(object.eClass(), local)) {
							operations.invokeBoundFunction(object, object.eClass(), local, "", request, response);
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
			if (formats.wantsXml(request)) {
				formats.writeXmi(response, shaper.shapeAll(List.of(object), object.eClass(), null, Set.of()));
				return;
			}
			String json = formats.entityJson(object, object.eClass(), null, Set.of());
			response.setContentType(contentTypeJson());
			response.getWriter().write(withContext(ODataJson.sanitize(context), json));
			return;
		}
		if (value instanceof List<?> collection) {
			if (formats.wantsXml(request)) {
				List<EObject> objects = collection.stream()
						.filter(EObject.class::isInstance).map(EObject.class::cast).toList();
				formats.writeXmi(response, objects.isEmpty() ? List.of()
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
					json.append(formats.entityJson(object, object.eClass(), null, Set.of()));
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
		ODataJson.value(json, value instanceof Date date
				? DateTimeFormatter.ISO_INSTANT.format(date.toInstant()) : value,
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
		SelectTree select = formats.selectOption(request, entityType);
		Map<String, ResponseFormatter.ExpandItem> expand = formats.expandOption(request, entityType);
		if (formats.wantsXml(request)) {
			List<EObject> copies = shaper.shapeAll(List.of(entity), entityType, select,
					ResponseFormatter.shapePaths(expand));
			copies.forEach(copy -> formats.applyNestedFilters(copy, expand));
			formats.writeXmi(response, copies);
			return;
		}
		String json = formats.entityJson(entity, entityType, select, expand);
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
		if (formats.wantsXml(request)) {
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
	static String option(HttpServletRequest request, String canonical) {
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
	static String normalizeOption(String name) {
		String normalized = name.toLowerCase(Locale.ROOT);
		return normalized.startsWith("$") ? normalized : "$" + normalized;
	}

	/**
	 * 4.01 parameter aliases (11.2.5.1.3): every {@code @name} query parameter, keyed as sent.
	 * Values are expression texts and get parsed on use — so they pass the SAME pre-parse
	 * limits as {@code $filter} itself (hostile-input guard).
	 */
	Map<String, String> parameterAliases(HttpServletRequest request) {
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
		int maxPageSize = maxPageSizePreference(request);
		if (maxPageSize > 0 && maxPageSize < top) {
			response.setHeader("Preference-Applied", "odata.maxpagesize=" + maxPageSize);
			return maxPageSize;
		}
		return top;
	}

	/** The {@code Prefer: (odata.)maxpagesize} value, or {@code -1} when absent/malformed. */
	static int maxPageSizePreference(HttpServletRequest request) {
		String prefer = request.getHeader("Prefer");
		if (prefer == null) {
			return -1;
		}
		for (String preference : prefer.split(",")) {
			String[] nameValue = preference.trim().split("=", 2);
			String name = nameValue[0].trim().toLowerCase(Locale.ROOT);
			if (!"odata.maxpagesize".equals(name) && !"maxpagesize".equals(name)) {
				continue;
			}
			try {
				int maxPageSize = Integer.parseInt(nameValue.length > 1 ? nameValue[1].trim() : "");
				if (maxPageSize > 0) {
					return maxPageSize;
				}
			} catch (NumberFormatException e) {
				// preferences are hints — a malformed value is ignored, not an error
			}
		}
		return -1;
	}

	/**
	 * The effective {@code $filter}, folding in {@code $search} (13.1.2 SHOULD): a free-text search
	 * becomes {@code contains(prop,'term')} OR-ed over the type's string properties, AND-ed with any
	 * {@code $filter}. It thus rides the existing typed-IR pushdown — no backend change, both backends.
	 */
	String filterWithSearch(HttpServletRequest request, EClass context) {
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
	static String searchExpression(String search, EClass context) {
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

	<T> T parseChecked(String expression, Function<String, T> parse) {
		if (expression == null || expression.isBlank()) {
			return null;
		}
		limits.checkExpression(expression);
		return parse.apply(expression);
	}

	EClass resolveEntityType(String setName) {
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

	/** The service root: the request URI without the resource path (not just its last segment). */
	static String contextRoot(HttpServletRequest request) {
		String uri = request.getRequestURI();
		String pathInfo = request.getPathInfo();
		if (pathInfo != null && !pathInfo.isEmpty() && uri.endsWith(pathInfo)) {
			return uri.substring(0, uri.length() - pathInfo.length());
		}
		return uri.replaceFirst("/[^/]*$", "");
	}



	void error(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(ODataJson.error(status, message));
	}
}
