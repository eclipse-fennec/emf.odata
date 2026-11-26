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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.fennec.odata.query.WireView;
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

	@Test
	@DisplayName("#152: a reference target is cut at the SERVICE ROOT, never at its last '/'")
	void serviceRelative() {
		HttpServletRequest request = request("http", "internal", 8080, Map.of());
		// the root is published behind a gateway prefix, so the configured base URL and the
		// request's own context root differ — a target may be spelled either way
		Wire.CURRENT.set(Wire.of(ODataWireProfile.INSTANCE, "https://cfg.example/gateway/sta/v1.1",
				WireView.NONE));
		try {
			assertEquals("Things('t1')", ODataServlet.serviceRelative(
					"https://cfg.example/gateway/sta/v1.1/Things('t1')", request));
			assertEquals("Things('t1')", ODataServlet.serviceRelative(
					"http://internal:8080/sta/v1.1/Things('t1')", request), "the request's own root");
			assertEquals("Things('t1')", ODataServlet.serviceRelative("/sta/v1.1/Things('t1')", request),
					"a root-relative target");
			assertEquals("Things('t1')", ODataServlet.serviceRelative("Things('t1')", request),
					"a target already relative to the service root is untouched");
			assertEquals("Things('http://ex.org/a/b')", ODataServlet.serviceRelative(
					"https://cfg.example/gateway/sta/v1.1/Things('http://ex.org/a/b')", request),
					"a key may contain slashes — only the ROOT is stripped");
			assertEquals("Things/t1", ODataServlet.serviceRelative(
					"http://internal:8080/sta/v1.1/Things/t1", request),
					"key-as-segment survives to the parser, which folds it");
			assertNull(ODataServlet.serviceRelative("https://other.example/odata/Things('t1')", request),
					"a target below some OTHER root is not a resource of this service");
			assertNull(ODataServlet.serviceRelative("https://cfg.example/gateway/sta/v1.1", request),
					"the service root itself addresses no entity");
		} finally {
			Wire.CURRENT.remove();
		}
	}

	@Test
	@DisplayName("#152: percent-decoding a payload target is lenient (it is a path, not a form)")
	void percentDecode() {
		assertEquals("http://ex.org/a/b", ODataServlet.percentDecode("http%3A%2F%2Fex.org%2Fa%2Fb"));
		assertEquals("ä", ODataServlet.percentDecode("%C3%A4"));
		assertEquals("50%off", ODataServlet.percentDecode("50%off"),
				"a '%' that is not an escape stays — the service's own @odata.id is unencoded");
		assertEquals("a+b", ODataServlet.percentDecode("a+b"), "'+' is a plus, not a space");
		assertEquals("done%", ODataServlet.percentDecode("done%"));
		assertEquals("'c1'", ODataServlet.percentDecode("'c1'"));
	}
}
