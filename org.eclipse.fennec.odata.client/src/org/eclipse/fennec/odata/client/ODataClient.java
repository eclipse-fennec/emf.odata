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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.codec.util.MetadataServiceFactory;
import org.eclipse.fennec.model.metadata.api.MetadataService;
import org.eclipse.fennec.model.metadata.api.MetadataWhiteboard;

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
		// the HttpClient is created here, so this client OWNS it and closes it in close()
		return connect(serviceRoot, HttpClient.newHttpClient(), null, true);
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
		URI root = URI.create(serviceRoot.endsWith("/") ? serviceRoot : serviceRoot + "/");
		// the throwaway bootstrap client never owns the HttpClient — only the returned one does
		ODataClient boot = new ODataClient(http, false, root, List.of(), whiteboard);
		String csdl = boot.fetch("$metadata", "application/xml");
		return new ODataClient(http, ownsHttp, root, CsdlMetadataReader.read(csdl), whiteboard);
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

	/** GET relative to the service root; non-2xx answers raise with status and error body. */
	String fetch(String relative, String accept) {
		URI target = serviceRoot.resolve(relative);
		// a server-supplied absolute link (e.g. @odata.nextLink) must not steer the client — with
		// its Accept/version headers and any ambient credentials — to a different host (SSRF)
		if (!sameOrigin(serviceRoot, target)) {
			throw new ODataClientException(
					"refusing to follow a link to a different origin than the service root: " + target);
		}
		HttpRequest request = HttpRequest.newBuilder(target)
				.header("Accept", accept)
				.header("OData-MaxVersion", "4.01")
				.GET().build();
		HttpResponse<String> response;
		try {
			response = http.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException e) {
			throw new ODataClientException("GET " + target + " failed", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ODataClientException("GET " + target + " interrupted", e);
		}
		if (response.statusCode() / 100 != 2) {
			throw new ODataClientException("GET " + target + " answered "
					+ response.statusCode(), response.statusCode(), response.body());
		}
		return response.body();
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
