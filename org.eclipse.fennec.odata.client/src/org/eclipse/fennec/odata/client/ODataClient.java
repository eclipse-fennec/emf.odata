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
public final class ODataClient {

	private final HttpClient http;
	private final URI serviceRoot;
	private final List<EPackage> packages;
	private final MetadataService metadataService;

	private ODataClient(HttpClient http, URI serviceRoot, List<EPackage> packages,
			MetadataWhiteboard whiteboard) {
		this.http = http;
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
		return connect(serviceRoot, HttpClient.newHttpClient());
	}

	public static ODataClient connect(String serviceRoot, HttpClient http) {
		return connect(serviceRoot, http, null);
	}

	/**
	 * Connect with an injected metadata whiteboard (e.g. the OSGi service) — the parsed
	 * schema packages are registered THERE instead of an internal instance.
	 */
	public static ODataClient connect(String serviceRoot, HttpClient http,
			MetadataWhiteboard whiteboard) {
		URI root = URI.create(serviceRoot.endsWith("/") ? serviceRoot : serviceRoot + "/");
		ODataClient boot = new ODataClient(http, root, List.of(), whiteboard);
		String csdl = boot.fetch("$metadata", "application/xml");
		return new ODataClient(http, root, CsdlMetadataReader.read(csdl), whiteboard);
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
}
