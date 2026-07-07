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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Fluent read request against one entity set: system query options collect as the request is
 * built, {@link #list()}/{@link #get(String)}/{@link #totalCount()} execute it. Filter and
 * order expressions are passed in OData syntax — the server parses them into its typed IR;
 * the client's job is exact URL assembly and response decoding.
 */
public final class EntitySetRequest {

	private final ODataClient client;
	private final String setName;
	private final EClass entityType;
	private final Map<String, String> options = new LinkedHashMap<>();

	EntitySetRequest(ODataClient client, String setName, EClass entityType) {
		this.client = client;
		this.setName = setName;
		this.entityType = entityType;
	}

	/** {@code $filter} in OData expression syntax, e.g. {@code price lt 3.00}. */
	public EntitySetRequest filter(String expression) {
		options.put("$filter", expression);
		return this;
	}

	/** {@code $orderby}, e.g. {@code name asc} or {@code price desc,name}. */
	public EntitySetRequest orderBy(String expression) {
		options.put("$orderby", expression);
		return this;
	}

	public EntitySetRequest top(int top) {
		options.put("$top", Integer.toString(top));
		return this;
	}

	public EntitySetRequest skip(int skip) {
		options.put("$skip", Integer.toString(skip));
		return this;
	}

	/** Requests {@code $count=true} — the total lands in {@link ODataPage#totalCount()}. */
	public EntitySetRequest count() {
		options.put("$count", "true");
		return this;
	}

	public EntitySetRequest select(String... properties) {
		options.put("$select", String.join(",", properties));
		return this;
	}

	public EntitySetRequest expand(String... navigations) {
		options.put("$expand", String.join(",", navigations));
		return this;
	}

	/** Executes the request and decodes one page. */
	public ODataPage list() {
		return ODataJsonDecoder.page(client.fetch(setName + queryString(), "application/json"),
				entityType, client.metadataService());
	}

	/**
	 * Reads one entity by its raw key literal — string keys quoted OData-style
	 * ({@code get("'p1'")}), numeric keys plain ({@code get("42")}).
	 */
	public EObject get(String keyLiteral) {
		return ODataJsonDecoder.entity(client.fetch(
				setName + "(" + encodeKey(keyLiteral) + ")" + queryString(), "application/json"),
				entityType, client.metadataService());
	}

	/** {@code GET Set/$count} with the collected {@code $filter}: the total as a number. */
	public long totalCount() {
		Map<String, String> filterOnly = new LinkedHashMap<>();
		if (options.containsKey("$filter")) {
			filterOnly.put("$filter", options.get("$filter"));
		}
		String answer = client.fetch(setName + "/$count" + queryString(filterOnly), "text/plain");
		try {
			return Long.parseLong(answer.trim());
		} catch (NumberFormatException e) {
			throw new ODataClientException("the $count answer is not a number: " + answer.trim());
		}
	}

	/** Follows an {@code @odata.nextLink} of a previous page (server-relative or absolute). */
	public ODataPage nextPage(ODataPage page) {
		if (!page.hasMore()) {
			throw new ODataClientException("the page has no @odata.nextLink");
		}
		String link = page.nextLink();
		// the servlet emits request-URI-based links — reduce to a service-root-relative form
		int setStart = link.lastIndexOf('/' + setName + '?');
		String relative = setStart < 0 ? link : link.substring(setStart + 1);
		return ODataJsonDecoder.page(client.fetch(relative, "application/json"), entityType,
				client.metadataService());
	}

	private String queryString() {
		return queryString(options);
	}

	private static String queryString(Map<String, String> options) {
		if (options.isEmpty()) {
			return "";
		}
		List<String> parts = new ArrayList<>();
		options.forEach((name, value) -> parts.add(name + "=" + encode(value)));
		return "?" + String.join("&", parts);
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
	}

	/** Key literals keep their OData quotes — {@code '} is a legal raw path character. */
	private static String encodeKey(String keyLiteral) {
		return encode(keyLiteral).replace("%27", "'");
	}
}
