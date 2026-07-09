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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.odata.schema.api.ODataSchema;
import org.eclipse.fennec.odata.schema.api.ODataSchemaReader;
import org.eclipse.fennec.odata.schema.api.SchemaScope;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Default {@link ODataSchemaReader}: fetches an endpoint's {@code $metadata} over HTTP and converts
 * the CSDL to Ecore ({@link CsdlMetadataReader} + the E2 converter), side-effect-free (ADR-0007).
 * The content hash of the raw CSDL is the authoritative version; ETag/Last-Modified are captured
 * for a later conditional {@code GET} so an unchanged document is not re-downloaded.
 *
 * <p>Usable programmatically with an injected {@link HttpClient}, or as a DS service (the no-arg
 * constructor creates — and {@link #deactivate() closes} — its own connect-timed client).
 */
@Component(service = ODataSchemaReader.class)
public final class HttpODataSchemaReader implements ODataSchemaReader {

	private final HttpClient http;
	private final boolean ownsHttp;
	private final ODataClientConfig config;

	public HttpODataSchemaReader(HttpClient http) {
		this(http, ODataClientConfig.DEFAULTS);
	}

	/** Reader with transport config — sends the same auth headers/version/timeout as the client. */
	public HttpODataSchemaReader(HttpClient http, ODataClientConfig config) {
		this.http = http;
		this.ownsHttp = false;
		this.config = config;
	}

	/** DS constructor: owns a connect-timed {@link HttpClient} released on deactivation. */
	@Activate
	public HttpODataSchemaReader() {
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
		this.ownsHttp = true;
		this.config = ODataClientConfig.DEFAULTS;
	}

	@Deactivate
	void deactivate() {
		if (ownsHttp) {
			http.close();
		}
	}

	@Override
	public Optional<ODataSchema> read(SchemaScope scope, Conditional conditional) {
		URI target = URI.create(scope.serviceRoot() + "/$metadata");
		HttpRequest.Builder builder = HttpRequest.newBuilder(target)
				.timeout(config.requestTimeout())
				// XML preferred; JSON accepted so 4.01 JSON-only services can answer (CSDL JSON)
				.header("Accept", "application/xml, application/json;q=0.9")
				.header("OData-MaxVersion", config.odataMaxVersion())
				.GET();
		config.headers().forEach(builder::header); // auth / custom headers for a protected endpoint
		conditional.etag().ifPresent(tag -> builder.header("If-None-Match", tag));
		conditional.lastModified().ifPresent(since -> builder.header("If-Modified-Since", since));

		HttpResponse<InputStream> response;
		try {
			response = http.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());
		} catch (IOException e) {
			throw new ODataClientException("GET " + target + " failed", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new ODataClientException("GET " + target + " interrupted", e);
		}
		if (response.statusCode() == 304) {
			return Optional.empty(); // unchanged — the caller keeps the registered schema
		}
		String csdl = readBounded(response.body(), target);
		if (response.statusCode() / 100 != 2) {
			throw new ODataClientException("GET " + target + " answered "
					+ response.statusCode(), response.statusCode(), csdl);
		}
		Optional<String> etag = response.headers().firstValue("ETag");
		Optional<String> lastModified = response.headers().firstValue("Last-Modified");
		return Optional.of(build(scope, csdl, etag, lastModified));
	}

	@Override
	public ODataSchema read(SchemaScope scope, String csdl) {
		return build(scope, csdl, Optional.empty(), Optional.empty());
	}

	private static ODataSchema build(SchemaScope scope, String csdl,
			Optional<String> etag, Optional<String> lastModified) {
		List<EPackage> packages = CsdlMetadataReader.read(csdl);
		return new ODataSchema(scope, packages, sha256(csdl), etag, lastModified);
	}

	private static String sha256(String csdl) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
					.digest(csdl.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}

	private String readBounded(InputStream stream, URI target) {
		int cap = (int) Math.min(config.maxResponseBytes(), Integer.MAX_VALUE - 1L);
		try (InputStream in = stream) {
			byte[] bytes = in.readNBytes(cap + 1);
			if (bytes.length > cap) {
				throw new ODataClientException("the $metadata from " + target
						+ " exceeds the client limit of " + config.maxResponseBytes() + " bytes");
			}
			return new String(bytes, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new ODataClientException("reading $metadata from " + target + " failed", e);
		}
	}
}
