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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;

/** ADR-0010: the base URL of profile links, and who may choose it. */
@DisplayName("Wire: base URL of a root")
class WireTest {

	private static HttpServletRequest request(String scheme, String host, int port,
			Map<String, String> headers) {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getScheme()).thenReturn(scheme);
		when(request.getServerName()).thenReturn(host);
		when(request.getServerPort()).thenReturn(port);
		when(request.getRequestURI()).thenReturn("/sta/v1.1/Things");
		when(request.getPathInfo()).thenReturn("/Things");
		headers.forEach((name, value) -> when(request.getHeader(name)).thenReturn(value));
		return request;
	}

	private static final Map<String, String> FORWARDED = Map.of("X-Forwarded-Host", "public.example",
			"X-Forwarded-Proto", "https", "X-Forwarded-Prefix", "/gateway");

	@Test
	@DisplayName("the configured base URL wins")
	void configured() {
		assertEquals("https://cfg.example/sta/v1.1", Wire.baseUrl(
				request("http", "internal", 8080, FORWARDED), "https://cfg.example/sta/v1.1/", true));
	}

	@Test
	@DisplayName("forwarding headers count only when the root trusts them")
	void forwarded() {
		assertEquals("https://public.example/gateway/sta/v1.1",
				Wire.baseUrl(request("http", "internal", 8080, FORWARDED), null, true));
		assertEquals("http://internal:8080/sta/v1.1",
				Wire.baseUrl(request("http", "internal", 8080, FORWARDED), null, false),
				"untrusted headers would let any client choose the host of every link");
	}

	@Test
	@DisplayName("a malformed forwarded host is ignored")
	void hostileForwardedHost() {
		assertEquals("http://internal:8080/sta/v1.1", Wire.baseUrl(request("http", "internal", 8080,
				Map.of("X-Forwarded-Host", "evil.example/\"><script>")), null, true));
		assertEquals("https://public.example/sta/v1.1", Wire.baseUrl(request("http", "internal", 8080,
				Map.of("X-Forwarded-Host", "public.example, other.example", "X-Forwarded-Proto", "https",
						"X-Forwarded-Prefix", "/../x\"")), null, true),
				"first host of a list; an unsafe prefix is dropped");
	}

	@Test
	@DisplayName("from the request: default ports are left out, IPv6 hosts bracketed")
	void fromRequest() {
		assertEquals("https://h.example/sta/v1.1",
				Wire.baseUrl(request("https", "h.example", 443, Map.of()), null, false));
		assertEquals("http://[::1]:8181/sta/v1.1",
				Wire.baseUrl(request("http", "::1", 8181, Map.of()), null, false));
	}
}
