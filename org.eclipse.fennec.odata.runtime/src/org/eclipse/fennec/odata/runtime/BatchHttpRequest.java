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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/** Synthetic request wrapping one JSON-batch sub-request; delegates everything else to {@code outer}. */
final class BatchHttpRequest extends HttpServletRequestWrapper {
	private final String method;
	private final String pathInfo;
	private final String queryString;
	private final Map<String, String[]> parameters;
	private final Map<String, String> headers; // keys lower-cased
	private final String requestURI; // captured for detached use; null → derive from wrapper
	private final byte[] body;

	BatchHttpRequest(HttpServletRequest outer, String method, String url,
			Map<String, String> headers, byte[] body) {
		this(outer, method, pathOf(url), queryOf(url), parseQuery(queryOf(url)), headers,
				null, body);
	}

	private BatchHttpRequest(HttpServletRequest outer, String method, String pathInfo,
			String queryString, Map<String, String[]> parameters, Map<String, String> headers,
			String requestURI, byte[] body) {
		super(outer);
		this.method = method;
		this.pathInfo = pathInfo;
		this.queryString = queryString;
		this.parameters = parameters;
		this.headers = new LinkedHashMap<>();
		headers.forEach((k, v) -> this.headers.put(k.toLowerCase(Locale.ROOT), v));
		this.requestURI = requestURI;
		this.body = body;
	}

	/**
	 * Detached copy of a live container request for background execution (respond-async):
	 * everything the read pipeline touches is captured NOW, on the container thread — the
	 * container recycles the original object once the accepting call returns.
	 */
	static BatchHttpRequest asyncSnapshot(HttpServletRequest outer) {
		Map<String, String[]> parameters = new LinkedHashMap<>();
		Map<String, String[]> outerParameters = outer.getParameterMap();
		if (outerParameters != null) {
			outerParameters.forEach((name, values) -> parameters.put(name, values.clone()));
		}
		Map<String, String> headers = new LinkedHashMap<>();
		Enumeration<String> names = outer.getHeaderNames();
		if (names != null) {
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				String value = outer.getHeader(name);
				if (value != null) {
					headers.put(name, value);
				}
			}
		}
		// getHeaderNames() may legally be null (a container may deny header enumeration) —
		// the negotiation headers the read pipeline consumes are captured explicitly
		for (String name : List.of("Accept", "OData-MaxVersion", "Prefer")) {
			String value = outer.getHeader(name);
			if (value != null) {
				headers.putIfAbsent(name, value);
			}
		}
		return new BatchHttpRequest(outer, outer.getMethod(), outer.getPathInfo(),
				outer.getQueryString(), parameters, headers, outer.getRequestURI(),
				new byte[0]);
	}

	private static String pathOf(String url) {
		String relative = url.startsWith("/") ? url : "/" + url;
		int q = relative.indexOf('?');
		return q < 0 ? relative : relative.substring(0, q);
	}

	private static String queryOf(String url) {
		String relative = url.startsWith("/") ? url : "/" + url;
		int q = relative.indexOf('?');
		return q < 0 ? null : relative.substring(q + 1);
	}

	private static Map<String, String[]> parseQuery(String query) {
		Map<String, String[]> map = new LinkedHashMap<>();
		if (query == null || query.isBlank()) {
			return map;
		}
		for (String pair : query.split("&")) {
			if (pair.isEmpty()) {
				continue;
			}
			int eq = pair.indexOf('=');
			String name = eq < 0 ? pair : pair.substring(0, eq);
			String value = eq < 0 ? "" : pair.substring(eq + 1);
			name = URLDecoder.decode(name, StandardCharsets.UTF_8);
			value = URLDecoder.decode(value, StandardCharsets.UTF_8);
			String[] existing = map.get(name);
			if (existing == null) {
				map.put(name, new String[] { value });
			} else {
				String[] grown = Arrays.copyOf(existing, existing.length + 1);
				grown[existing.length] = value;
				map.put(name, grown);
			}
		}
		return map;
	}

	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		return pathInfo;
	}

	@Override
	public String getRequestURI() {
		if (requestURI != null) {
			return requestURI; // detached snapshot — never touch the recycled delegate
		}
		String context = getContextPath() == null ? "" : getContextPath();
		String servlet = getServletPath() == null ? "" : getServletPath();
		return context + servlet + pathInfo;
	}

	@Override
	public String getQueryString() {
		return queryString;
	}

	@Override
	public String getParameter(String name) {
		String[] values = parameters.get(name);
		return values == null ? null : values[0];
	}

	@Override
	public Map<String, String[]> getParameterMap() {
		return Collections.unmodifiableMap(parameters);
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return Collections.enumeration(parameters.keySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		String[] values = parameters.get(name);
		return values == null ? null : values.clone();
	}

	@Override
	public String getHeader(String name) {
		return headers.get(name.toLowerCase(Locale.ROOT));
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		String value = headers.get(name.toLowerCase(Locale.ROOT));
		return value == null ? Collections.emptyEnumeration()
				: Collections.enumeration(List.of(value));
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return Collections.enumeration(headers.keySet());
	}

	@Override
	public String getContentType() {
		return headers.get("content-type");
	}

	@Override
	public int getContentLength() {
		return body.length;
	}

	@Override
	public long getContentLengthLong() {
		return body.length;
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream source = new ByteArrayInputStream(body);
		return new ServletInputStream() {
			@Override
			public int read() {
				return source.read();
			}

			@Override
			public boolean isFinished() {
				return source.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(body),
				StandardCharsets.UTF_8));
	}
}
