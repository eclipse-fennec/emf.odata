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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
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

	/** Per-request timeout so a hung/slow service cannot block the calling thread indefinitely. */
	private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
	private static final long DEFAULT_MAX_RESPONSE_BYTES = 16L * 1024 * 1024;

	private final HttpClient http;
	private final boolean ownsHttp;
	private final URI serviceRoot;
	private final List<EPackage> packages;
	private final MetadataService metadataService;

	/**
	 * Inbound-size cap: a foreign service without server-driven paging (or a hostile one) must not
	 * OOM the client by streaming an unbounded body. Package-private and mutable so tests can lower
	 * it; adjust before issuing the request whose response you want bounded.
	 */
	long maxResponseBytes = DEFAULT_MAX_RESPONSE_BYTES;

	private ODataClient(HttpClient http, boolean ownsHttp, URI serviceRoot, List<EPackage> packages,
			MetadataWhiteboard whiteboard) {
		this.http = http;
		this.ownsHttp = ownsHttp;
		this.serviceRoot = serviceRoot;
		this.packages = List.copyOf(packages);
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
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
		return connect(serviceRoot, http, null, true);
	}

	public static ODataClient connect(String serviceRoot, HttpClient http) {
		return connect(serviceRoot, http, null);
	}

	/**
	 * Connect with an injected metadata whiteboard (e.g. the OSGi service) — the parsed
	 * schema packages are registered THERE instead of an internal instance. The caller-supplied
	 * {@link HttpClient} is NOT owned: {@link #close()} leaves it open.
	 */
	public static ODataClient connect(String serviceRoot, HttpClient http,
			MetadataWhiteboard whiteboard) {
		return connect(serviceRoot, http, whiteboard, false);
	}

	private static ODataClient connect(String serviceRoot, HttpClient http,
			MetadataWhiteboard whiteboard, boolean ownsHttp) {
		// convenience/standalone path == the opt-in "lazy" policy (ADR-0007): read the schema now
		// (via the default reader) and build the data client from it, no external registry needed
		SchemaScope scope = SchemaScope.of(serviceRoot);
		ODataSchema schema = new HttpODataSchemaReader(http).read(scope, ODataSchemaReader.Conditional.NONE)
				.orElseThrow(() -> new ODataClientException(
						"no $metadata available for " + scope.serviceRoot()));
		return new ODataClient(http, ownsHttp, dataRoot(scope), schema.packages(), whiteboard);
	}

	/**
	 * Builds a DATA client for an ALREADY-registered endpoint (ADR-0007): the schema is looked up in
	 * the registry — no {@code $metadata} fetch happens on this path. A missing scope is a hard error
	 * (the endpoint must be registered first); the caller owns the {@link HttpClient}.
	 */
	public static ODataClient forEndpoint(SchemaScope scope, ODataSchemaResolver resolver,
			HttpClient http) {
		ODataSchema schema = resolver.lookup(scope).orElseThrow(() -> new ODataClientException(
				"endpoint not registered: " + scope.serviceRoot() + " — register its $metadata first"));
		return new ODataClient(http, false, dataRoot(scope), schema.packages(), null);
	}

	/**
	 * As {@link #forEndpoint(SchemaScope, ODataSchemaResolver, HttpClient)} but registers the schema
	 * on first use — the opt-in lazy policy for standalone callers.
	 */
	public static ODataClient forEndpoint(SchemaScope scope, ODataSchemaManager manager,
			HttpClient http) {
		return new ODataClient(http, false, dataRoot(scope), manager.ensureRegistered(scope).packages(),
				null);
	}

	/** The data client resolves relative segments against the root, so it keeps the trailing slash. */
	private static URI dataRoot(SchemaScope scope) {
		return URI.create(scope.serviceRoot() + "/");
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

	/** The client's codec metadata wiring — every schema package is registered. */
	MetadataService metadataService() {
		return metadataService;
	}

	/** The service model as Ecore — one {@link EPackage} per CSDL schema. */
	public List<EPackage> metadata() {
		return packages;
	}

	/**
	 * The entity type behind a set name (this server family names sets after their EClass;
	 * schemas are searched in document order).
	 */
	public EClass entityType(String setName) {
		for (EPackage pkg : packages) {
			if (pkg.getEClassifier(setName) instanceof EClass entityType) {
				return entityType;
			}
		}
		throw new ODataClientException("the service metadata has no entity type '" + setName + "'");
	}

	public EntitySetRequest entitySet(String setName) {
		return new EntitySetRequest(this, setName, entityType(setName));
	}

	/** Status, body text and response headers of an HTTP exchange. */
	record Response(int status, String body, HttpHeaders headers) {

		/** The first value of a response header, or {@code null}. */
		String header(String name) {
			return headers.firstValue(name).orElse(null);
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
	 * A raw HTTP exchange with any method; enforces same-origin and the response size cap but does
	 * NOT throw on a non-2xx status — the caller interprets it (writes need 201/204/404/412). The
	 * body may be {@code null} (e.g. {@code DELETE}).
	 */
	Response exchange(String method, String relative, String accept, String body,
			String contentType, Map<String, String> extraHeaders) {
		URI target = serviceRoot.resolve(relative);
		// a server-supplied absolute link (e.g. @odata.nextLink) must not steer the client — with
		// its Accept/version headers and any ambient credentials — to a different host (SSRF)
		if (!sameOrigin(serviceRoot, target)) {
			throw new ODataClientException(
					"refusing to follow a link to a different origin than the service root: " + target);
		}
		HttpRequest.Builder builder = HttpRequest.newBuilder(target)
				.timeout(REQUEST_TIMEOUT)
				.header("Accept", accept)
				.header("OData-MaxVersion", "4.01");
		if (contentType != null) {
			builder.header("Content-Type", contentType);
		}
		extraHeaders.forEach(builder::header);
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
			return new String(bytes, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new ODataClientException("reading the response from " + target + " failed", e);
		}
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
