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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transport configuration applied to every request a client (and its schema reader) issues:
 * default headers (e.g. {@code Authorization}), the {@code OData-MaxVersion}, the per-request
 * timeout and the inbound response-size cap. Immutable — derive variants with the {@code with*}
 * helpers. Default headers are sent on every request but a per-call header (e.g. {@code If-Match})
 * takes precedence.
 *
 * @param headers          default request headers (sent on every request)
 * @param odataMaxVersion  the {@code OData-MaxVersion} header value
 * @param requestTimeout   per-request timeout
 * @param maxResponseBytes inbound body cap (bytes) — a larger response is rejected
 * @param csrf             when {@code true}, fetch and replay an {@code X-CSRF-Token} on writes
 *                         (the SAP/OData pattern); requires a cookie-capable client
 */
public record ODataClientConfig(Map<String, String> headers, String odataMaxVersion,
		Duration requestTimeout, long maxResponseBytes, boolean csrf) {

	public static final ODataClientConfig DEFAULTS = new ODataClientConfig(
			Map.of(), "4.01", Duration.ofSeconds(30), 16L * 1024 * 1024, false);

	public ODataClientConfig {
		headers = Map.copyOf(headers);
	}

	/** Adds/overrides a default header. */
	public ODataClientConfig withHeader(String name, String value) {
		Map<String, String> merged = new LinkedHashMap<>(headers);
		merged.put(name, value);
		return new ODataClientConfig(merged, odataMaxVersion, requestTimeout, maxResponseBytes, csrf);
	}

	/** {@code Authorization: Bearer <token>}. */
	public ODataClientConfig withBearerToken(String token) {
		return withHeader("Authorization", "Bearer " + token);
	}

	/** {@code Authorization: Basic <base64(user:password)>}. */
	public ODataClientConfig withBasicAuth(String user, String password) {
		String credentials = Base64.getEncoder()
				.encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
		return withHeader("Authorization", "Basic " + credentials);
	}

	public ODataClientConfig withMaxVersion(String odataMaxVersion) {
		return new ODataClientConfig(headers, odataMaxVersion, requestTimeout, maxResponseBytes, csrf);
	}

	public ODataClientConfig withRequestTimeout(Duration requestTimeout) {
		return new ODataClientConfig(headers, odataMaxVersion, requestTimeout, maxResponseBytes, csrf);
	}

	public ODataClientConfig withMaxResponseBytes(long maxResponseBytes) {
		return new ODataClientConfig(headers, odataMaxVersion, requestTimeout, maxResponseBytes, csrf);
	}

	/** Enables the {@code X-CSRF-Token} fetch/replay handshake on write requests. */
	public ODataClientConfig withCsrf() {
		return new ODataClientConfig(headers, odataMaxVersion, requestTimeout, maxResponseBytes, true);
	}
}
