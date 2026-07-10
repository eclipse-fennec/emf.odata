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

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.ODataResourceParser;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The {@code ODataRequestFilter} (req §5.1.1): a HTTP-whiteboard servlet filter in front of
 * the {@code ODataServlet} that enforces the pre-parse {@link RequestLimits} — expression
 * length/nesting of every expression-shaped system query option (and every {@code @alias}
 * value) plus the resource-path length — and rejects violations as a 400 OData error document
 * BEFORE any dispatch or parsing work runs.
 *
 * <p>The servlet keeps its own front-door checks (defence in depth and coverage for setups
 * that deploy the servlet without the whiteboard filter); this filter moves the rejection to
 * the earliest possible point of the pipeline. Configured via the same PID properties as the
 * servlet limits ({@code odata.max.expression.length}, {@code odata.max.nesting.depth}).
 */
@Component(service = Filter.class, configurationPid = ODataRequestFilter.PID, property = {
		"osgi.http.whiteboard.filter.pattern=/odata/*",
		"osgi.http.whiteboard.filter.name=Fennec OData Request Limits"
})
public class ODataRequestFilter implements Filter {

	public static final String PID = "org.eclipse.fennec.odata.request.filter";

	/** Query options whose values are expressions the parser would otherwise chew on. */
	private static final Set<String> EXPRESSION_OPTIONS = Set.of(
			"$filter", "$orderby", "$apply", "$select", "$expand", "$compute", "$search");

	private volatile RequestLimits limits = RequestLimits.DEFAULTS;

	@Activate
	void activate(Map<String, Object> configuration) {
		limits = RequestLimits.fromConfiguration(configuration);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (request instanceof HttpServletRequest http
				&& response instanceof HttpServletResponse httpResponse
				&& !withinLimits(http, httpResponse)) {
			return; // 400 already written
		}
		chain.doFilter(request, response);
	}

	private boolean withinLimits(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		try {
			String path = request.getPathInfo();
			if (path != null && path.length() > ODataResourceParser.MAX_PATH_LENGTH) {
				throw new ODataQueryParseException("resource path exceeds the maximum length of "
						+ ODataResourceParser.MAX_PATH_LENGTH);
			}
			for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
				// 4.01 option names are case-insensitive and may omit the $ prefix
				String canonical = parameter.getKey().startsWith("@") ? "@alias"
						: parameter.getKey().startsWith("$")
								? parameter.getKey().toLowerCase(Locale.ROOT)
								: "$" + parameter.getKey().toLowerCase(Locale.ROOT);
				if (!"@alias".equals(canonical) && !EXPRESSION_OPTIONS.contains(canonical)) {
					continue;
				}
				for (String value : parameter.getValue()) {
					if (value != null) {
						limits.checkExpression(value);
					}
				}
			}
			return true;
		} catch (ODataQueryParseException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(ODataJson.error(HttpServletResponse.SC_BAD_REQUEST,
					e.getMessage()));
			return false;
		}
	}
}
