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

import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.odata.csdl.ODataAnnotationConstants;
import org.eclipse.fennec.codec.util.MetadataServiceFactory;
import org.eclipse.fennec.model.metadata.api.MetadataService;
import org.eclipse.fennec.model.metadata.api.MetadataWhiteboard;
import org.eclipse.fennec.odata.schema.api.ODataSchema;
import org.eclipse.fennec.odata.schema.api.ODataSchemaReader;
import org.eclipse.fennec.odata.schema.api.ODataSchemaResolver;
import org.eclipse.fennec.odata.schema.api.SchemaScope;

/**
 * OData v4 client foundation (E8): connects to a service root, reads {@code $metadata} into
 * Ecore packages (one per CSDL schema, E2 read path) and hands out fluent entity-set requests
 * whose responses decode into {@link org.eclipse.emf.ecore.EObject}s through the E3 codec —
 * the client works against ANY spec-conformant OData v4 service, not just this project's server.
 *
 * <pre>
 * ODataClient client = ODataClient.connect("http://localhost:8080/odata/");
 * ODataPage page = client.entitySet("Product")
 *     .filter("price lt 3.00").orderBy("name asc").top(10).count()
 *     .list();
 * </pre>
 */
public final class ODataClient implements AutoCloseable {

	private final HttpClient http;
	private final boolean ownsHttp;
	private final URI serviceRoot;
	private final List<EPackage> packages;
	private final MetadataService metadataService;
	private final ODataClientConfig config;

	/**
	 * Inbound-size cap: a foreign service without server-driven paging (or a hostile one) must not
	 * OOM the client by streaming an unbounded body. Seeded from {@link ODataClientConfig}; kept
	 * mutable (package-private) so tests can lower it before a specific request.
	 */
	long maxResponseBytes;

	/** Cached CSRF token (SAP handshake); {@code null} until fetched, cleared on a 403 Required. */
	private final java.util.concurrent.atomic.AtomicReference<String> csrfToken =
			new java.util.concurrent.atomic.AtomicReference<>();

	private ODataClient(HttpClient http, boolean ownsHttp, URI serviceRoot, List<EPackage> packages,
			MetadataWhiteboard whiteboard, ODataClientConfig config) {
		this.http = http;
		this.ownsHttp = ownsHttp;
		this.serviceRoot = serviceRoot;
		this.packages = List.copyOf(packages);
		this.config = config;
		this.maxResponseBytes = config.maxResponseBytes();
		// the codec profile lookup runs against a MetadataService — decoupled like the
		// server side: callers (OSGi wiring, tests) may inject their own whiteboard; the
		// default is an ISOLATED plain-Java instance so the remote service's packages never
		// leak into a shared/server whiteboard
		MetadataWhiteboard effective = whiteboard != null ? whiteboard
				: MetadataServiceFactory.create();
		packages.forEach(effective::registerPackage);
		this.metadataService = effective;
	}

	public static ODataClient connect(String serviceRoot) {
		// the HttpClient is created here, so this client OWNS it and closes it in close();
		// a connect timeout bounds establishing the connection (the request timeout bounds the rest)
		// cookies enabled so a CSRF session cookie survives between the token fetch and the write
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
				.cookieHandler(new CookieManager()).build();
		return connect(serviceRoot, http, null, true, ODataClientConfig.DEFAULTS);
	}

	/** {@link #connect(String)} with transport config (auth headers, version, timeout, size cap). */
	public static ODataClient connect(String serviceRoot, ODataClientConfig config) {
		// cookies enabled so a CSRF session cookie survives between the token fetch and the write
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10))
				.cookieHandler(new CookieManager()).build();
		return connect(serviceRoot, http, null, true, config);
	}

	public static ODataClient connect(String serviceRoot, HttpClient http) {
		return connect(serviceRoot, http, null, false, ODataClientConfig.DEFAULTS);
	}

	/**
	 * Connect with an injected metadata whiteboard (e.g. the OSGi service) — the parsed
	 * schema packages are registered THERE instead of an internal instance. The caller-supplied
	 * {@link HttpClient} is NOT owned: {@link #close()} leaves it open.
	 */
	public static ODataClient connect(String serviceRoot, HttpClient http,
			MetadataWhiteboard whiteboard) {
		return connect(serviceRoot, http, whiteboard, false, ODataClientConfig.DEFAULTS);
	}

	private static ODataClient connect(String serviceRoot, HttpClient http,
			MetadataWhiteboard whiteboard, boolean ownsHttp, ODataClientConfig config) {
		// convenience/standalone path == the opt-in "lazy" policy (ADR-0007): read the schema now
		// (via the default reader, same transport config) and build the data client from it
		SchemaScope scope = SchemaScope.of(resolveServiceRoot(serviceRoot, http, config));
		ODataSchema schema = new HttpODataSchemaReader(http, config)
				.read(scope, ODataSchemaReader.Conditional.NONE)
				.orElseThrow(() -> new ODataClientException(
						"no $metadata available for " + scope.serviceRoot()));
		return new ODataClient(http, ownsHttp, dataRoot(scope), schema.packages(), whiteboard, config);
	}

	/**
	 * Builds a DATA client for an ALREADY-registered endpoint (ADR-0007): the schema is looked up in
	 * the registry — no {@code $metadata} fetch happens on this path. A missing scope is a hard error
	 * (the endpoint must be registered first); the caller owns the {@link HttpClient}.
	 */
	public static ODataClient forEndpoint(SchemaScope scope, ODataSchemaResolver resolver,
			HttpClient http) {
		return forEndpoint(scope, resolver, http, ODataClientConfig.DEFAULTS);
	}

	/** {@link #forEndpoint(SchemaScope, ODataSchemaResolver, HttpClient)} with transport config. */
	public static ODataClient forEndpoint(SchemaScope scope, ODataSchemaResolver resolver,
			HttpClient http, ODataClientConfig config) {
		ODataSchema schema = resolver.lookup(scope).orElseThrow(() -> new ODataClientException(
				"endpoint not registered: " + scope.serviceRoot() + " — register its $metadata first"));
		return new ODataClient(http, false, dataRoot(scope), schema.packages(), null, config);
	}

	/**
	 * As {@link #forEndpoint(SchemaScope, ODataSchemaResolver, HttpClient)} but registers the schema
	 * on first use — the opt-in lazy policy for standalone callers.
	 */
	public static ODataClient forEndpoint(SchemaScope scope, ODataSchemaManager manager,
			HttpClient http) {
		return new ODataClient(http, false, dataRoot(scope), manager.ensureRegistered(scope).packages(),
				null, ODataClientConfig.DEFAULTS);
	}

	/** The data client resolves relative segments against the root, so it keeps the trailing slash. */
	private static URI dataRoot(SchemaScope scope) {
		return URI.create(scope.serviceRoot() + "/");
	}

	/**
	 * Follows service-root redirects before connecting: public services (e.g. TripPin) mint a
	 * per-session root via {@code 302 Location: …/(S(key))/Service/} — the SESSION URL must become
	 * the data root, or every later request would mint a fresh session. Only same-host targets are
	 * followed and the scheme may never downgrade ({@code https → http}); at most 3 hops.
	 */
	private static String resolveServiceRoot(String serviceRoot, HttpClient http,
			ODataClientConfig config) {
		URI current = URI.create(serviceRoot);
		for (int hop = 0; hop < 3; hop++) {
			HttpRequest probe = HttpRequest.newBuilder(current)
					.timeout(config.requestTimeout()).GET().build();
			HttpResponse<Void> response;
			try {
				response = http.send(probe, HttpResponse.BodyHandlers.discarding());
			} catch (IOException e) {
				throw new ODataClientException("GET " + current + " failed", e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new ODataClientException("GET " + current + " interrupted", e);
			}
			if (response.statusCode() / 100 != 3) {
				return current.toString();
			}
			String location = response.headers().firstValue("Location").orElse(null);
			if (location == null) {
				return current.toString();
			}
			URI target = current.resolve(location);
			if (!equalsIgnoreCase(current.getHost(), target.getHost())
					|| ("https".equalsIgnoreCase(current.getScheme())
							&& !"https".equalsIgnoreCase(target.getScheme()))) {
				throw new ODataClientException("refusing the service-root redirect to " + target);
			}
			current = target;
		}
		return current.toString();
	}

	/**
	 * Releases the internally-created {@link HttpClient} (its connection pool and selector/worker
	 * threads). A caller-injected client is left untouched — the caller owns its lifecycle.
	 */
	@Override
	public void close() {
		if (ownsHttp) {
			http.close();
		}
	}

	/** The service root the data client resolves against (batch parts need absolute URLs). */
	URI rootUri() {
		return serviceRoot;
	}

	/** The client's codec metadata wiring — every schema package is registered. */
	MetadataService metadataService() {
		return metadataService;
	}

	/** The service model as Ecore — one {@link EPackage} per CSDL schema. */
	public List<EPackage> metadata() {
		return packages;
	}

	/**
	 * The entity type behind a set name. Real services name sets differently from their types
	 * (TripPin {@code People -> Person}) — the container's set names, captured by the metadata
	 * read path as an EPackage annotation, resolve first; the convention "set name = type name"
	 * is the fallback. The container often lives in a DIFFERENT schema than the types (Northwind),
	 * so the set mapping and the type are looked up across ALL packages independently.
	 */
	public EClass entityType(String setName) {
		String typeName = setName;
		for (EPackage pkg : packages) {
			EAnnotation sets = pkg.getEAnnotation(ODataAnnotationConstants.ENTITY_SETS_SOURCE);
			if (sets != null && sets.getDetails().containsKey(setName)) {
				typeName = sets.getDetails().get(setName);
				break;
			}
		}
		for (EPackage pkg : packages) {
			if (pkg.getEClassifier(typeName) instanceof EClass entityType) {
				return entityType;
			}
		}
		throw new ODataClientException("the service metadata has no entity type '" + setName + "'");
	}

	public EntitySetRequest entitySet(String setName) {
		return new EntitySetRequest(this, setName, entityType(setName));
	}

	/**
	 * Reads a container singleton (e.g. {@code Me}) by name: {@code GET /<name>}, decoded into the
	 * singleton's declared type. The type comes from the service metadata (the singleton declaration
	 * the E2 read path captured); an unknown singleton is a hard error.
	 */
	public EObject singleton(String name) {
		return ODataJsonDecoder.entity(fetch(name, "application/json"), singletonType(name),
				metadataService);
	}

	private EClass singletonType(String name) {
		for (EPackage pkg : packages) {
			EAnnotation annotation = pkg.getEAnnotation(ODataAnnotationConstants.SINGLETONS_SOURCE);
			if (annotation != null && annotation.getDetails().containsKey(name)
					&& pkg.getEClassifier(annotation.getDetails().get(name)) instanceof EClass type) {
				return type;
			}
		}
		throw new ODataClientException("the service metadata has no singleton '" + name + "'");
	}

	/**
	 * Starts an OData v4.01 JSON {@code $batch}: accumulate sub-requests on the returned builder and
	 * call {@link ODataBatch#execute()} to send them in one round-trip.
	 */
	public ODataBatch batch() {
		return new ODataBatch(this);
	}

	/**
	 * Invokes an unbound function import: {@code GET Name(p1=v1,…)}. Parameters are formatted as
	 * OData literals (strings quoted). Returns the primitive {@code value} of the response.
	 */
	public Object function(String name, Map<String, ?> parameters) {
		return ODataJsonDecoder.value(fetch(functionCall(name, parameters), "application/json"));
	}

	/** As {@link #function}, but decodes an entity-typed result into an {@link EObject}. */
	public EObject functionAsEntity(String name, Map<String, ?> parameters, EClass resultType) {
		return ODataJsonDecoder.entity(fetch(functionCall(name, parameters), "application/json"),
				resultType, metadataService);
	}

	/** As {@link #function}, but decodes an entity-collection result into an {@link ODataPage}. */
	public ODataPage functionAsCollection(String name, Map<String, ?> parameters, EClass resultType) {
		return ODataJsonDecoder.page(fetch(functionCall(name, parameters), "application/json"),
				resultType, metadataService);
	}

	private static String functionCall(String name, Map<String, ?> parameters) {
		StringBuilder call = new StringBuilder(name).append('(');
		boolean first = true;
		for (Map.Entry<String, ?> parameter : parameters.entrySet()) {
			if (!first) {
				call.append(',');
			}
			first = false;
			call.append(parameter.getKey()).append('=').append(functionLiteral(parameter.getValue()));
		}
		return call.append(')').toString();
	}

	private static String functionLiteral(Object value) {
		if (value instanceof CharSequence text) {
			return "'" + text.toString().replace("'", "''") + "'";
		}
		return String.valueOf(value);
	}

	/**
	 * Invokes an unbound action import: {@code POST Name} with the parameters as the JSON body.
	 * Returns the response {@code value} (or {@code null} for a 204 / bodiless action).
	 */
	public Object action(String name, Map<String, ?> parameters) {
		String body = actionResult(name, parameters);
		return body == null || body.isBlank() ? null : ODataJsonDecoder.value(body);
	}

	/** As {@link #action}, but decodes an entity-typed result into an {@link EObject}. */
	public EObject actionAsEntity(String name, Map<String, ?> parameters, EClass resultType) {
		String body = actionResult(name, parameters);
		return body == null || body.isBlank() ? null
				: ODataJsonDecoder.entity(body, resultType, metadataService);
	}

	/** As {@link #action}, but decodes an entity-collection result into an {@link ODataPage}. */
	public ODataPage actionAsCollection(String name, Map<String, ?> parameters, EClass resultType) {
		String body = actionResult(name, parameters);
		return body == null || body.isBlank() ? null
				: ODataJsonDecoder.page(body, resultType, metadataService);
	}

	/** POSTs an action import and returns its raw response body ({@code null} for a 204). */
	private String actionResult(String name, Map<String, ?> parameters) {
		String body = ODataJsonDecoder.toJson(parameters);
		Response response = exchange("POST", name, "application/json", body, "application/json", Map.of());
		if (response.status() == 204) {
			return null;
		}
		if (response.status() / 100 != 2) {
			throw new ODataClientException("POST " + serviceRoot.resolve(name) + " answered "
					+ response.status(), response.status(), response.body());
		}
		return response.body();
	}

	/** Status, body text and response headers of an HTTP exchange. */
	record Response(int status, String body, HttpHeaders headers) {

		/** The first value of a response header, or {@code null}. */
		String header(String name) {
			return headers.firstValue(name).orElse(null);
		}
	}

	/** Status, raw body bytes and headers of a binary exchange (media streams). */
	record BinaryResponse(int status, byte[] body, HttpHeaders headers) {

		String header(String name) {
			return headers.firstValue(name).orElse(null);
		}
	}

	/**
	 * Binary exchange for media streams ({@code Set(key)/$value} on HasStream entities): the same
	 * origin/header/size rules as {@link #exchange}, but raw bytes in and out — media content must
	 * not pass through a UTF-8 String. Writes run the CSRF handshake like their JSON siblings.
	 */
	BinaryResponse exchangeBinary(String method, String relative, byte[] body, String contentType,
			Map<String, String> extraHeaders) {
		Map<String, String> headers = new LinkedHashMap<>(extraHeaders);
		if (config.csrf() && !"GET".equals(method) && !"HEAD".equals(method)) {
			headers.put("X-CSRF-Token", ensureCsrfToken());
		}
		URI target = upgradeToRootScheme(serviceRoot, serviceRoot.resolve(relative));
		if (!sameOrigin(serviceRoot, target)) {
			throw new ODataClientException(
					"refusing to follow a link to a different origin than the service root: " + target);
		}
		HttpRequest.Builder builder = HttpRequest.newBuilder(target)
				.timeout(config.requestTimeout())
				.header("Accept", "*/*")
				.header("OData-MaxVersion", config.odataMaxVersion());
		if (contentType != null) {
			builder.header("Content-Type", contentType);
		}
		config.headers().forEach(builder::header);
		headers.forEach(builder::header);
		builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
				: HttpRequest.BodyPublishers.ofByteArray(body));
		HttpResponse<InputStream> response;
		try {
			response = http.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException e) {
			throw new ODataClientException(method + " " + target + " failed", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ODataClientException(method + " " + target + " interrupted", e);
		}
		int cap = (int) Math.min(maxResponseBytes, Integer.MAX_VALUE - 1L);
		try (InputStream in = response.body()) {
			byte[] bytes = in.readNBytes(cap + 1);
			if (bytes.length > cap) {
				throw new ODataClientException("the response from " + target
						+ " exceeds the client limit of " + maxResponseBytes + " bytes");
			}
			return new BinaryResponse(response.statusCode(), bytes, response.headers());
		} catch (IOException e) {
			throw new ODataClientException("reading the response from " + target + " failed", e);
		}
	}

	/** GET relative to the service root; non-2xx answers raise with status and error body. */
	String fetch(String relative, String accept) {
		Response response = exchange("GET", relative, accept, null, null, Map.of());
		if (response.status() / 100 != 2) {
			throw new ODataClientException("GET " + serviceRoot.resolve(relative) + " answered "
					+ response.status(), response.status(), response.body());
		}
		return response.body();
	}

	/**
	 * Package-private entry for reads and writes. Reads go straight through; writes additionally run
	 * the CSRF handshake when {@link ODataClientConfig#csrf()} is on.
	 */
	Response exchange(String method, String relative, String accept, String body,
			String contentType, Map<String, String> extraHeaders) {
		if (!config.csrf() || "GET".equals(method) || "HEAD".equals(method)) {
			return rawExchange(method, relative, accept, body, contentType, extraHeaders);
		}
		Map<String, String> headers = new LinkedHashMap<>(extraHeaders);
		headers.put("X-CSRF-Token", ensureCsrfToken());
		Response response = rawExchange(method, relative, accept, body, contentType, headers);
		// token expired/invalid — SAP answers 403 with X-CSRF-Token: Required; refetch and retry once
		if (response.status() == 403 && "required".equalsIgnoreCase(response.header("X-CSRF-Token"))) {
			csrfToken.set(null);
			headers.put("X-CSRF-Token", ensureCsrfToken());
			response = rawExchange(method, relative, accept, body, contentType, headers);
		}
		return response;
	}

	/** Fetches and caches the CSRF token: a GET to the service root with {@code X-CSRF-Token: Fetch}. */
	private String ensureCsrfToken() {
		String token = csrfToken.get();
		if (token != null) {
			return token;
		}
		Response response = rawExchange("GET", "", "application/json", null, null,
				Map.of("X-CSRF-Token", "Fetch"));
		token = response.header("X-CSRF-Token");
		if (token == null || token.isBlank()) {
			throw new ODataClientException("the service returned no X-CSRF-Token on fetch");
		}
		csrfToken.set(token);
		return token;
	}

	/**
	 * A raw HTTP exchange with any method; enforces same-origin and the response size cap but does
	 * NOT throw on a non-2xx status — the caller interprets it (writes need 201/204/404/412). The
	 * body may be {@code null} (e.g. {@code DELETE}).
	 */
	private Response rawExchange(String method, String relative, String accept, String body,
			String contentType, Map<String, String> extraHeaders) {
		URI target = upgradeToRootScheme(serviceRoot, serviceRoot.resolve(relative));
		// a server-supplied absolute link (e.g. @odata.nextLink) must not steer the client — with
		// its Accept/version headers and any ambient credentials — to a different host (SSRF)
		if (!sameOrigin(serviceRoot, target)) {
			throw new ODataClientException(
					"refusing to follow a link to a different origin than the service root: " + target);
		}
		HttpRequest.Builder builder = HttpRequest.newBuilder(target)
				.timeout(config.requestTimeout())
				.header("Accept", accept)
				.header("OData-MaxVersion", config.odataMaxVersion());
		if (contentType != null) {
			builder.header("Content-Type", contentType);
		}
		config.headers().forEach(builder::header); // default headers (auth, custom)
		extraHeaders.forEach(builder::header); // a per-call header (If-Match) wins
		HttpRequest.BodyPublisher publisher = body == null ? HttpRequest.BodyPublishers.noBody()
				: HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8);
		builder.method(method, publisher);
		HttpResponse<InputStream> response;
		try {
			response = http.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException e) {
			throw new ODataClientException(method + " " + target + " failed", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ODataClientException(method + " " + target + " interrupted", e);
		}
		return new Response(response.statusCode(), readBounded(response.body(), target),
				response.headers());
	}

	/** Reads the response body streaming, rejecting anything beyond {@link #maxResponseBytes}. */
	private String readBounded(InputStream stream, URI target) {
		int cap = (int) Math.min(maxResponseBytes, Integer.MAX_VALUE - 1L);
		try (InputStream in = stream) {
			byte[] bytes = in.readNBytes(cap + 1);
			if (bytes.length > cap) {
				throw new ODataClientException("the response from " + target
						+ " exceeds the client limit of " + maxResponseBytes + " bytes");
			}
			String text = new String(bytes, StandardCharsets.UTF_8);
			// some services (RESTier's /$count) prefix responses with a UTF-8 BOM — strip it, or
			// number parsing and strict JSON parsing choke on U+FEFF
			return !text.isEmpty() && text.charAt(0) == '\uFEFF' ? text.substring(1) : text;
		} catch (IOException e) {
			throw new ODataClientException("reading the response from " + target + " failed", e);
		}
	}

	/**
	 * Upgrades a same-host {@code http://} link to the root's {@code https://} scheme: public
	 * services (e.g. TripPin) emit plain-http {@code @odata.nextLink}/{@code @odata.id} URLs even
	 * when reached over https. Following them verbatim would leak headers over cleartext (and the
	 * SSRF guard rightly refuses); upgrading is safe — the opposite direction never happens here,
	 * a downgrade still fails the {@link #sameOrigin} check. Package-private for the unit test.
	 */
	static URI upgradeToRootScheme(URI root, URI target) {
		if (!"https".equalsIgnoreCase(root.getScheme()) || !"http".equalsIgnoreCase(target.getScheme())
				|| !equalsIgnoreCase(root.getHost(), target.getHost())
				|| (target.getPort() != -1 && target.getPort() != 80)) {
			return target; // only the plain default-port http form of the SAME host is upgraded
		}
		return URI.create("https" + target.toString().substring("http".length())
				.replaceFirst("^://" + java.util.regex.Pattern.quote(target.getHost()) + ":80",
						"://" + target.getHost()));
	}

	/** Same scheme, host and (default-aware) port — the origin the service root was reached at. */
	private static boolean sameOrigin(URI a, URI b) {
		return equalsIgnoreCase(a.getScheme(), b.getScheme())
				&& equalsIgnoreCase(a.getHost(), b.getHost())
				&& effectivePort(a) == effectivePort(b);
	}

	private static boolean equalsIgnoreCase(String a, String b) {
		return a == null ? b == null : a.equalsIgnoreCase(b);
	}

	private static int effectivePort(URI uri) {
		if (uri.getPort() != -1) {
			return uri.getPort();
		}
		return "https".equalsIgnoreCase(uri.getScheme()) ? 443
				: "http".equalsIgnoreCase(uri.getScheme()) ? 80 : -1;
	}
}
