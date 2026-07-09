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
import org.eclipse.emf.ecore.EReference;

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
	/** Namespace-qualified derived type of a type-cast segment ({@code Set/Ns.Type}), or null. */
	private String castSegment;
	/** The derived {@link EClass} the cast restricts to — what {@link #list()}/{@link #get} decode into. */
	private EClass castedType;
	/** {@code Prefer: return=} value applied to writes ({@code "minimal"}/{@code "representation"}), or null. */
	private String returnPreference;

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

	/** {@code $search} — free-text search (server SHOULD support it). */
	public EntitySetRequest search(String expression) {
		options.put("$search", expression);
		return this;
	}

	/** {@code $compute} — server-computed properties, e.g. {@code price mul 1.19 as gross}. */
	public EntitySetRequest compute(String expression) {
		options.put("$compute", expression);
		return this;
	}

	/** {@code $format}, e.g. {@code json} or {@code xml}. */
	public EntitySetRequest format(String format) {
		options.put("$format", format);
		return this;
	}

	/** A 4.01 parameter alias {@code @name=value} referenced from {@code $filter}/{@code $orderby}. */
	public EntitySetRequest parameterAlias(String name, String value) {
		options.put(name.startsWith("@") ? name : "@" + name, value);
		return this;
	}

	/**
	 * Restricts the set to a derived type via a type-cast segment ([OData-URL] 4.11):
	 * {@link #list()} addresses {@code Set/Ns.Type}, {@link #get(String)} addresses
	 * {@code Set/Ns.Type(key)}, and both decode into the derived type. {@code qualifiedTypeName} is
	 * the service's namespace-qualified type name (e.g. {@code webshop.DiscountedProduct}); the
	 * derived {@link EClass} is resolved from the service metadata (unknown → {@link ODataClientException}).
	 */
	public EntitySetRequest cast(String qualifiedTypeName) {
		String local = qualifiedTypeName.contains(".")
				? qualifiedTypeName.substring(qualifiedTypeName.lastIndexOf('.') + 1) : qualifiedTypeName;
		this.castedType = client.entityType(local);
		this.castSegment = qualifiedTypeName;
		return this;
	}

	/** The set path, with the type-cast segment appended when {@link #cast} was applied. */
	private String collectionPath() {
		return castSegment == null ? setName : setName + "/" + castSegment;
	}

	/** The entity type responses decode into — the derived type under a cast, else the set's type. */
	private EClass decodeType() {
		return castedType != null ? castedType : entityType;
	}

	/**
	 * {@code $apply} aggregation, e.g.
	 * {@code groupby((category/name),aggregate(price with sum as Total))}. The result rows are
	 * grouping keys + aggregate aliases (not entities), returned as generic maps.
	 */
	public List<Map<String, Object>> apply(String applyExpression) {
		options.put("$apply", applyExpression);
		return ODataJsonDecoder.rows(client.fetch(setName + queryString(), "application/json"));
	}

	/** Executes the request and decodes one page. */
	public ODataPage list() {
		return ODataJsonDecoder.page(client.fetch(collectionPath() + queryString(), "application/json"),
				decodeType(), client.metadataService());
	}

	/**
	 * Executes the request and returns the raw entity rows as maps — the accessor for
	 * {@code $compute}: computed properties are not model features, so they cannot surface on the
	 * typed {@link EObject}s of {@link #list()}; here each map carries the entity's properties
	 * together with the computed aliases.
	 */
	public List<Map<String, Object>> listRaw() {
		return ODataJsonDecoder.rows(client.fetch(setName + queryString(), "application/json"));
	}

	/**
	 * Executes the request and returns typed {@code $compute} rows: each pairs the entity decoded
	 * into a typed {@link EObject} (its model properties) with the computed members, which
	 * {@link ComputedRow#value(String, Class)} coerces to a requested Java type. Preferred over
	 * {@link #listRaw()} when the entity itself is wanted typed alongside the computed values.
	 */
	public List<ComputedRow> listComputed() {
		return ODataJsonDecoder.computedRows(client.fetch(setName + queryString(), "application/json"),
				entityType, client.metadataService());
	}

	/**
	 * Reads one entity by its raw key literal — string keys quoted OData-style
	 * ({@code get("'p1'")}), numeric keys plain ({@code get("42")}).
	 */
	public EObject get(String keyLiteral) {
		return ODataJsonDecoder.entity(client.fetch(
				collectionPath() + "(" + encodeKey(keyLiteral) + ")" + queryString(), "application/json"),
				decodeType(), client.metadataService());
	}

	// --- navigation-path addressing ---

	/** {@code GET Set(key)/nav} for a single-valued navigation → the related entity. */
	public EObject navigateEntity(String keyLiteral, String navigation) {
		EClass target = referenceTarget(navigation);
		return ODataJsonDecoder.entity(client.fetch(
				entityPath(keyLiteral) + "/" + navigation + queryString(), "application/json"),
				target, client.metadataService());
	}

	/** {@code GET Set(key)/nav} for a collection-valued navigation → a page of related entities. */
	public ODataPage navigateCollection(String keyLiteral, String navigation) {
		EClass target = referenceTarget(navigation);
		return ODataJsonDecoder.page(client.fetch(
				entityPath(keyLiteral) + "/" + navigation + queryString(), "application/json"),
				target, client.metadataService());
	}

	/** {@code GET Set(key)/property/$value} → the raw property value as text. */
	public String propertyValue(String keyLiteral, String property) {
		return client.fetch(entityPath(keyLiteral) + "/" + property + "/$value", "text/plain");
	}

	/** {@code GET Set(key)/nav/$count} → the size of a collection-valued navigation. */
	public long navigationCount(String keyLiteral, String navigation) {
		String answer = client.fetch(
				entityPath(keyLiteral) + "/" + navigation + "/$count", "text/plain");
		try {
			return Long.parseLong(answer.trim());
		} catch (NumberFormatException e) {
			throw new ODataClientException("the $count answer is not a number: " + answer.trim());
		}
	}

	/**
	 * Invokes a bound function on an entity: {@code GET Set(key)/Ns.Func(p=…)}. Returns the
	 * response's primitive {@code value}. {@code qualifiedName} is the service's namespace-qualified
	 * function name (e.g. {@code My.Shop.label}).
	 */
	public Object boundFunction(String keyLiteral, String qualifiedName, Map<String, ?> parameters) {
		return ODataJsonDecoder.value(
				client.fetch(boundFunctionCall(keyLiteral, qualifiedName, parameters), "application/json"));
	}

	/** As {@link #boundFunction}, but decodes an entity-typed result into an {@link EObject}. */
	public EObject boundFunctionAsEntity(String keyLiteral, String qualifiedName,
			Map<String, ?> parameters, EClass resultType) {
		return ODataJsonDecoder.entity(
				client.fetch(boundFunctionCall(keyLiteral, qualifiedName, parameters), "application/json"),
				resultType, client.metadataService());
	}

	/** As {@link #boundFunction}, but decodes an entity-collection result into an {@link ODataPage}. */
	public ODataPage boundFunctionAsCollection(String keyLiteral, String qualifiedName,
			Map<String, ?> parameters, EClass resultType) {
		return ODataJsonDecoder.page(
				client.fetch(boundFunctionCall(keyLiteral, qualifiedName, parameters), "application/json"),
				resultType, client.metadataService());
	}

	/**
	 * Invokes a bound action on an entity: {@code POST Set(key)/Ns.Action} with the parameters as the
	 * JSON body ([OData-Protocol] 11.5.4.2). Returns the response's primitive {@code value}, or
	 * {@code null} for a 204 / void action. {@code qualifiedName} is the service's namespace-qualified
	 * action name (e.g. {@code My.Shop.recalculate}).
	 */
	public Object boundAction(String keyLiteral, String qualifiedName, Map<String, ?> parameters) {
		String body = boundActionCall(keyLiteral, qualifiedName, parameters);
		return body == null || body.isBlank() ? null : ODataJsonDecoder.value(body);
	}

	/** As {@link #boundAction}, but decodes an entity-typed result into an {@link EObject}. */
	public EObject boundActionAsEntity(String keyLiteral, String qualifiedName,
			Map<String, ?> parameters, EClass resultType) {
		String body = boundActionCall(keyLiteral, qualifiedName, parameters);
		return body == null || body.isBlank() ? null
				: ODataJsonDecoder.entity(body, resultType, client.metadataService());
	}

	/** As {@link #boundAction}, but decodes an entity-collection result into an {@link ODataPage}. */
	public ODataPage boundActionAsCollection(String keyLiteral, String qualifiedName,
			Map<String, ?> parameters, EClass resultType) {
		String body = boundActionCall(keyLiteral, qualifiedName, parameters);
		return body == null || body.isBlank() ? null
				: ODataJsonDecoder.page(body, resultType, client.metadataService());
	}

	/** POSTs the bound action and returns its raw response body ({@code null} for a 204). */
	private String boundActionCall(String keyLiteral, String qualifiedName, Map<String, ?> parameters) {
		String path = entityPath(keyLiteral) + "/" + qualifiedName;
		String body = ODataJsonDecoder.toJson(parameters);
		ODataClient.Response response = client.exchange("POST", path, JSON, body, JSON, Map.of());
		if (response.status() == 204) {
			return null;
		}
		if (response.status() / 100 != 2) {
			throw failure("POST " + path, response);
		}
		return response.body();
	}

	private String boundFunctionCall(String keyLiteral, String qualifiedName, Map<String, ?> parameters) {
		StringBuilder call = new StringBuilder(entityPath(keyLiteral)).append('/')
				.append(qualifiedName).append('(');
		boolean first = true;
		for (Map.Entry<String, ?> parameter : parameters.entrySet()) {
			if (!first) {
				call.append(',');
			}
			first = false;
			call.append(parameter.getKey()).append('=').append(literal(parameter.getValue()));
		}
		return call.append(')').toString();
	}

	private static String literal(Object value) {
		return value instanceof CharSequence text
				? "'" + text.toString().replace("'", "''") + "'"
				: String.valueOf(value);
	}

	private EClass referenceTarget(String navigation) {
		if (entityType.getEStructuralFeature(navigation) instanceof EReference reference) {
			return reference.getEReferenceType();
		}
		throw new ODataClientException(
				"'" + navigation + "' is not a navigation of " + entityType.getName());
	}

	/** {@code GET Set/$count} with the collected {@code $filter}: the total as a number. */
	public long totalCount() {
		Map<String, String> filterOnly = new LinkedHashMap<>();
		if (options.containsKey("$filter")) {
			filterOnly.put("$filter", options.get("$filter"));
		}
		String answer = client.fetch(collectionPath() + "/$count" + queryString(filterOnly), "text/plain");
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

	// --- write path (mirrors the server's Updatable Service contract) ---

	private static final String JSON = "application/json";

	/**
	 * Sets {@code Prefer: return=<preference>} on the following write ([OData-Protocol] 8.2.8.7):
	 * {@code "minimal"} asks the server to answer 204 with no body, {@code "representation"} asks it
	 * to return the written entity. {@link #create}/{@link #update}/{@link #replace} interpret the
	 * response accordingly.
	 */
	public EntitySetRequest preferReturn(String preference) {
		this.returnPreference = preference;
		return this;
	}

	/** Per-write request headers: an optional If-Match and the {@code Prefer: return=} header. */
	private Map<String, String> writeHeaders(String ifMatch) {
		Map<String, String> headers = new LinkedHashMap<>();
		if (ifMatch != null) {
			headers.put("If-Match", ifMatch);
		}
		if (returnPreference != null) {
			headers.put("Prefer", "return=" + returnPreference);
		}
		return headers;
	}

	/**
	 * POSTs a new entity to the set (deep insert: containment children in the graph ride along).
	 * Returns the created entity decoded from the 201 response body, or {@code null} when the server
	 * honoured {@code Prefer: return=minimal} (204).
	 */
	public EObject create(EObject entity) {
		return create(entity, Map.of());
	}

	/**
	 * As {@link #create(EObject)}, but links the new entity to ALREADY-existing related entities via
	 * {@code "nav@odata.bind"}: each binding maps a non-containment navigation to its target edit
	 * URL(s) — a single {@code String} for a single-valued navigation, an {@code Iterable<String>}
	 * or {@code String[]} for a collection-valued one (e.g. {@code Map.of("category", "Category(1)")}).
	 */
	public EObject create(EObject entity, Map<String, ?> bindings) {
		String body = ODataJsonEncoder.encode(entity, entityType, client.metadataService(), bindings);
		ODataClient.Response response = client.exchange("POST", setName, JSON, body, JSON, writeHeaders(null));
		if (response.status() == 204) {
			return null; // Prefer: return=minimal
		}
		if (response.status() != 201) {
			throw failure("POST " + setName, response);
		}
		return ODataJsonDecoder.entity(response.body(), entityType, client.metadataService());
	}

	/**
	 * PATCH (merge — only the set features are sent) an entity by key; If-Match optional. Returns the
	 * updated entity when the server honoured {@code Prefer: return=representation}, else {@code null}.
	 */
	public EObject update(String keyLiteral, EObject patch, String ifMatch) {
		return write("PATCH", keyLiteral, patch, ifMatch, Map.of());
	}

	/** As {@link #update(String, EObject, String)}, additionally re-binding navigations to existing entities. */
	public EObject update(String keyLiteral, EObject patch, String ifMatch, Map<String, ?> bindings) {
		return write("PATCH", keyLiteral, patch, ifMatch, bindings);
	}

	/** PUT (replace) an entity by key; If-Match optional. Returns the representation when requested. */
	public EObject replace(String keyLiteral, EObject entity, String ifMatch) {
		return write("PUT", keyLiteral, entity, ifMatch, Map.of());
	}

	/** As {@link #replace(String, EObject, String)}, additionally re-binding navigations to existing entities. */
	public EObject replace(String keyLiteral, EObject entity, String ifMatch, Map<String, ?> bindings) {
		return write("PUT", keyLiteral, entity, ifMatch, bindings);
	}

	private EObject write(String method, String keyLiteral, EObject entity, String ifMatch,
			Map<String, ?> bindings) {
		String path = entityPath(keyLiteral);
		String body = ODataJsonEncoder.encode(entity, entityType, client.metadataService(), bindings);
		ODataClient.Response response = client.exchange(method, path, JSON, body, JSON, writeHeaders(ifMatch));
		if (response.status() != 204 && response.status() / 100 != 2) {
			throw failure(method + " " + path, response);
		}
		// Prefer: return=representation → the server answered 200 with the updated entity
		return response.status() == 200 && response.body() != null && !response.body().isBlank()
				? ODataJsonDecoder.entity(response.body(), entityType, client.metadataService())
				: null;
	}

	/** DELETEs an entity by key; If-Match optional. Returns {@code false} when it did not exist. */
	public boolean delete(String keyLiteral, String ifMatch) {
		String path = entityPath(keyLiteral);
		ODataClient.Response response = client.exchange("DELETE", path, JSON, null, null, ifMatch(ifMatch));
		return switch (response.status()) {
			case 204, 200 -> true;
			case 404 -> false;
			default -> throw failure("DELETE " + path, response);
		};
	}

	/** Sets a single-valued navigation: {@code PUT Set(key)/nav/$ref} with the target's edit URL. */
	public void setReference(String keyLiteral, String navigation, String targetEditUrl) {
		reference("PUT", refPath(keyLiteral, navigation), targetEditUrl);
	}

	/** Adds to a collection-valued navigation: {@code POST Set(key)/nav/$ref}. */
	public void addReference(String keyLiteral, String navigation, String targetEditUrl) {
		reference("POST", refPath(keyLiteral, navigation), targetEditUrl);
	}

	/**
	 * Removes a reference: {@code DELETE Set(key)/nav/$ref} for a single-valued navigation, or with
	 * {@code $id=<targetEditUrl>} for a specific collection member (pass {@code null} to clear a
	 * single-valued one).
	 */
	public void removeReference(String keyLiteral, String navigation, String targetEditUrl) {
		String path = refPath(keyLiteral, navigation);
		if (targetEditUrl != null) {
			path = path + "?$id=" + encode(targetEditUrl);
		}
		ODataClient.Response response = client.exchange("DELETE", path, JSON, null, null, Map.of());
		if (response.status() != 204 && response.status() / 100 != 2) {
			throw failure("DELETE " + path, response);
		}
	}

	private void reference(String method, String path, String targetEditUrl) {
		String body = "{\"@odata.id\":\"" + targetEditUrl + "\"}";
		ODataClient.Response response = client.exchange(method, path, JSON, body, JSON, Map.of());
		if (response.status() != 204 && response.status() / 100 != 2) {
			throw failure(method + " " + path, response);
		}
	}

	private String entityPath(String keyLiteral) {
		return setName + "(" + encodeKey(keyLiteral) + ")";
	}

	private String refPath(String keyLiteral, String navigation) {
		return entityPath(keyLiteral) + "/" + navigation + "/$ref";
	}

	private static Map<String, String> ifMatch(String ifMatch) {
		return ifMatch == null ? Map.of() : Map.of("If-Match", ifMatch);
	}

	private static ODataClientException failure(String what, ODataClient.Response response) {
		return new ODataClientException(what + " answered " + response.status(),
				response.status(), response.body());
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
