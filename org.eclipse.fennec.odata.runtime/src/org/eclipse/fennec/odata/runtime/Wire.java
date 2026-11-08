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

import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.fennec.odata.wire.api.ControlNames;
import org.eclipse.fennec.odata.wire.api.WireProfile;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The wire format of the current request (ADR-0010): the one place that knows the names of the
 * control information. Request-scoped like the metadata level; {@code null} profile = plain
 * OData. With a profile the links are absolute, built from the base URL of the root.
 *
 * @param profile the root's profile, or null for plain OData
 * @param names   the control names in effect
 * @param baseUrl the absolute URL of the service root (no trailing slash); null for plain OData
 */
record Wire(WireProfile profile, ControlNames names, String baseUrl) {

	static final Wire ODATA = new Wire(null, ControlNames.ODATA, null);

	/** The wire of the request being served; plain OData outside a request. */
	static final ThreadLocal<Wire> CURRENT = new ThreadLocal<>();

	static Wire current() {
		Wire wire = CURRENT.get();
		return wire == null ? ODATA : wire;
	}

	boolean profiled() {
		return profile != null;
	}

	/**
	 * The absolute service root URL of a request: the configured {@code odata.base.url}, else
	 * the {@code X-Forwarded-*} headers when the root trusts them, else the request itself.
	 * Untrusted forwarding headers are ignored — they would let any client choose the host of
	 * every link the server writes.
	 */
	static String baseUrl(HttpServletRequest request, String configured, boolean trustForwarded) {
		if (configured != null && !configured.isBlank()) {
			return stripSlash(configured.trim());
		}
		String root = ODataServlet.contextRoot(request);
		if (trustForwarded) {
			String host = first(request.getHeader("X-Forwarded-Host"));
			if (host != null && HOST.matcher(host).matches()) {
				String proto = first(request.getHeader("X-Forwarded-Proto"));
				proto = proto != null && PROTO.matcher(proto).matches()
						? proto.toLowerCase(Locale.ROOT) : request.getScheme();
				String prefix = first(request.getHeader("X-Forwarded-Prefix"));
				prefix = prefix != null && PREFIX.matcher(prefix).matches() ? stripSlash(prefix) : "";
				return proto + "://" + host + prefix + root;
			}
		}
		int port = request.getServerPort();
		boolean defaultPort = port <= 0 || ("http".equals(request.getScheme()) && port == 80)
				|| ("https".equals(request.getScheme()) && port == 443);
		String host = request.getServerName();
		if (host != null && host.indexOf(':') >= 0 && !host.startsWith("[")) {
			host = "[" + host + "]"; // IPv6 literal
		}
		return request.getScheme() + "://" + host + (defaultPort ? "" : ":" + port) + root;
	}

	private static final Pattern HOST = Pattern.compile("[A-Za-z0-9.\\-]+(:\\d{1,5})?|\\[[0-9A-Fa-f:.]+\\](:\\d{1,5})?");
	private static final Pattern PROTO = Pattern.compile("(?i)https?");
	private static final Pattern PREFIX = Pattern.compile("(/[A-Za-z0-9._~\\-]+)+/?");

	private static String first(String header) {
		if (header == null) {
			return null;
		}
		String value = header.split(",")[0].trim();
		return value.isEmpty() ? null : value;
	}

	private static String stripSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}
}
