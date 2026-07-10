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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The whiteboard {@link ODataRequestFilter} (req §5.1.1): pre-parse limits fire BEFORE the
 * chain — hostile query options never reach the servlet — while clean requests pass through
 * untouched.
 */
@DisplayName("ODataRequestFilter enforces pre-parse limits before the chain")
class ODataRequestFilterTest {

	private final ODataRequestFilter filter = new ODataRequestFilter();

	@Test
	@DisplayName("clean requests pass through to the chain")
	void passesCleanRequests() throws Exception {
		FilterChain chain = mock(FilterChain.class);
		HttpServletRequest request = request(Map.of("$filter", "price gt 1"));
		filter.doFilter(request, response(new StringWriter()), chain);
		verify(chain).doFilter(same(request), any());
	}

	@Test
	@DisplayName("over-long/over-nested options and paths are 400 without touching the chain")
	void rejectsHostileInput() throws Exception {
		filter.activate(Map.of("odata.max.expression.length", "64", "odata.max.nesting.depth", "4"));

		assertRejected(request(Map.of("$filter", "x".repeat(65))));
		assertRejected(request(Map.of("filter", "((((( price gt 1 )))))")), "prefix-less option name");
		assertRejected(request(Map.of("@p", "y".repeat(65))), "parameter alias values count too");

		HttpServletRequest longPath = request(Map.of());
		when(longPath.getPathInfo()).thenReturn("/" + "a".repeat(8192));
		assertRejected(longPath);

		// non-expression options are not length-limited here (e.g. big $batch bodies)
		FilterChain chain = mock(FilterChain.class);
		HttpServletRequest custom = request(Map.of("custom", "z".repeat(100)));
		filter.doFilter(custom, response(new StringWriter()), chain);
		verify(chain).doFilter(same(custom), any());
	}

	private void assertRejected(HttpServletRequest request) throws Exception {
		assertRejected(request, "");
	}

	private void assertRejected(HttpServletRequest request, String message) throws Exception {
		FilterChain chain = mock(FilterChain.class);
		StringWriter body = new StringWriter();
		HttpServletResponse response = response(body);
		ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);
		filter.doFilter(request, response, chain);
		verify(chain, never()).doFilter(any(), any());
		verify(response).setStatus(captor.capture());
		assertEquals(400, captor.getValue(), message);
		assertTrue(body.toString().contains("\"error\""), message + ": " + body);
	}

	private static HttpServletRequest request(Map<String, String> parameters) {
		HttpServletRequest request = mock(HttpServletRequest.class);
		Map<String, String[]> parameterMap = new HashMap<>();
		parameters.forEach((key, value) -> parameterMap.put(key, new String[] { value }));
		when(request.getParameterMap()).thenReturn(parameterMap);
		when(request.getPathInfo()).thenReturn("/Product");
		return request;
	}

	private static HttpServletResponse response(StringWriter body) throws Exception {
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(new PrintWriter(body, true));
		return response;
	}
}
