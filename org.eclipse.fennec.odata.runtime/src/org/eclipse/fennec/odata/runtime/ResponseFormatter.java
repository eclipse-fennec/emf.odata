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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.fennec.odata.persistence.api.ExpandPushdown;
import org.eclipse.fennec.odata.persistence.api.ExpandSpec;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.codec.json.ODataJsonResourceImpl;
import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.odata.wire.api.ControlNames;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Response shaping and formats for {@link ODataServlet}: {@code $select}/{@code $expand}
 * option parsing (incl. nested options and {@code $levels}), the entity-JSON serialization
 * glue (codec invocation, nested counts, expanded {@code $ref} members) and the XML/XMI
 * content negotiation. One instance per servlet; stateless beyond the servlet reference.
 */
final class ResponseFormatter {

	private final ODataServlet servlet;

	ResponseFormatter(ODataServlet servlet) {
		this.servlet = servlet;
	}


SelectTree selectOption(HttpServletRequest request, EClass entityType) {
	return selectOption(request, entityType, Set.of());
}

/**
 * Validated {@code $select} tree (nested selects incl., 4.01), or null when absent. Any top-level
 * token that names a {@code $compute} alias is stripped before model validation (the computed
 * member is projected separately); a {@code $select} of only aliases leaves no real-property
 * projection constraint (null).
 */
SelectTree selectOption(HttpServletRequest request, EClass entityType,
		Set<String> computeAliases) {
	String select = ODataServlet.option(request, "$select");
	if (select == null || select.isBlank()) {
		return null;
	}
	servlet.limits.checkExpression(select); // nested trees are parsed — same hostile-input guard
	if (!computeAliases.isEmpty()) {
		List<String> realProperties = new ArrayList<>();
		for (String token : SelectTree.splitTopLevel(select, ',')) {
			if (!computeAliases.contains(token.trim())) {
				realProperties.add(token);
			}
		}
		if (realProperties.isEmpty()) {
			return null;
		}
		select = String.join(",", realProperties);
	}
	// nested collection options over selected collections ([OData-URL] 5.1.3, 4.01 Advanced
	// §13.2.3/5.1–5.4) parse through the same guarded parser as every other expression
	return SelectTree.parse(select, entityType, nestedOptions(request), selectableControls());
}

/**
 * The control information a {@code $select} may name (#121 D4): the self link of a wire
 * profile, e.g. {@code $select=@iot.selfLink}. None for plain OData.
 */
static Set<String> selectableControls() {
	String selfLink = Wire.current().names().selfLink();
	return selfLink == null ? Set.of() : Set.of(selfLink);
}

/**
 * One validated {@code $expand} item ([OData-URL] 5.1.3):
 *
 * @param options nested collection options ({@code $filter}/{@code $search}/{@code $orderby}/
 *                {@code $top}/{@code $skip}/{@code $count} — Advanced 9.2/9.4–9.7)
 * @param refOnly {@code nav/$ref} ([OData-URL] 5.1.3.1): only entity REFERENCES are
 *                expanded — the response carries {@code {"@odata.id": …}} objects
 * @param cast    {@code nav/Ns.Type} (5.1.3.2): only related instances of the derived type
 *                are expanded; null without a cast
 * @param levels  {@code $levels} of a self-recursive navigation (9.8), 1 without
 * @param select  nested {@code $select} of the expanded entities, null without one
 * @param expand  nested {@code $expand} of the expanded entities, never null
 */
record ExpandItem(CollectionOptions options, boolean refOnly, EClass cast, int levels,
		SelectTree select, Map<String, ExpandItem> expand) {

	ExpandItem {
		// request order is response order (and prefetch order)
		expand = expand == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(expand));
	}

	ExpandItem(CollectionOptions options, boolean refOnly, EClass cast, int levels) {
		this(options, refOnly, cast, levels, null, Map.of());
	}
}

/**
 * The guarded parser behind every expression-valued nested option — request-scoped so the
 * request's {@code @}-parameter aliases resolve inside nested {@code $filter}/{@code $orderby}
 * too ([OData-Protocol] 13.2.3/9).
 */
private NestedOptionParser nestedOptions(HttpServletRequest request) {
	Map<String, String> aliases = servlet.parameterAliases(request);
	return new NestedOptionParser() {
		@Override
		public OclExpression filter(String expression, EClass context) {
			servlet.limits.checkExpression(expression);
			return aliases.isEmpty() ? servlet.parser.parseFilter(expression, context)
					: servlet.parser.parseFilter(expression, context, aliases);
		}

		@Override
		public List<OrderBySegment> orderBy(String expression, EClass context) {
			servlet.limits.checkExpression(expression);
			return aliases.isEmpty() ? servlet.parser.parseOrderBy(expression, context)
					: servlet.parser.parseOrderBy(expression, context, aliases);
		}

		@Override
		public OclExpression search(String term, EClass context) {
			return filter(ODataServlet.searchExpression(term, context), context);
		}
	};
}

/**
 * Validated {@code $expand} items: navigation name → {@link ExpandItem}. Supported item
 * shapes: {@code nav}, {@code nav(…options…)}, {@code nav/$ref} and {@code nav/Ns.Type}
 * (optionally with nested options against the derived type). The options are the collection
 * options, {@code $levels}, and a nested {@code $select} and {@code $expand}; {@code $compute}
 * answers 501.
 */
Map<String, ExpandItem> expandOption(HttpServletRequest request, EClass entityType) {
	String expand = ODataServlet.option(request, "$expand");
	if (expand == null || expand.isBlank()) {
		return new LinkedHashMap<>();
	}
	servlet.limits.checkExpression(expand); // nested $filter trees are parsed — same hostile-input guard
	return parseExpand(expand, entityType, nestedOptions(request), 0);
}

/** One {@code $expand} value against a context type; {@code depth} counts the enclosing items. */
private Map<String, ExpandItem> parseExpand(String expand, EClass entityType,
		NestedOptionParser optionParser, int depth) {
	Map<String, ExpandItem> items = new LinkedHashMap<>();
	if (expand.isBlank()) {
		throw new ODataQueryParseException("empty $expand");
	}
	for (String item : splitExpandItems(expand)) {
		String trimmed = item.trim();
		String name = trimmed;
		String nested = null;
		int paren = trimmed.indexOf('(');
		if (paren >= 0 && trimmed.endsWith(")")) {
			name = trimmed.substring(0, paren).trim();
			nested = trimmed.substring(paren + 1, trimmed.length() - 1).trim();
		}
		boolean refOnly = false;
		String castName = null;
		int slash = name.indexOf('/');
		if (slash >= 0) { // nav/$ref or nav/Ns.Type
			String suffix = name.substring(slash + 1).trim();
			name = name.substring(0, slash).trim();
			if ("$ref".equals(suffix)) {
				refOnly = true;
			} else {
				castName = suffix;
			}
		}
		if (!(entityType.getEStructuralFeature(name) instanceof EReference reference)) {
			throw new ODataQueryParseException("unknown $expand navigation '" + name + "'");
		}
		if (refOnly && nested != null) {
			throw new UnsupportedOperationException(
					"options on $expand=nav/$ref are not implemented");
		}
		if (refOnly && depth > 0) {
			throw new UnsupportedOperationException(
					"$expand=nav/$ref inside $expand is not implemented");
		}
		EClass cast = null;
		if (castName != null) {
			cast = servlet.resolveCastType(castName, null);
			if (cast == null || !reference.getEReferenceType().isSuperTypeOf(cast)) {
				throw new ODataQueryParseException("'" + castName
						+ "' is not a derived type of the '" + name + "' navigation");
			}
		}
		items.put(name, nested == null
				? new ExpandItem(CollectionOptions.NONE, refOnly, cast, 1)
				: expandItemOptions(nested, reference,
						cast != null ? cast : reference.getEReferenceType(),
						optionParser, refOnly, cast, depth));
	}
	return items;
}

/** Recursion cap for {@code $levels=max} ([OData-URL] 5.1.2 expandOption). */
private static final int MAX_EXPAND_LEVELS = 8;

/**
 * The {@code ;}-separated option list of one {@code $expand} item — the collection options
 * (Advanced 9.2/9.4–9.7), {@code $levels} for SELF-RECURSIVE navigations (9.8), and a nested
 * {@code $select}/{@code $expand} against the target (or cast) type ([OData-URL] 5.1.3);
 * {@code $compute} inside {@code $expand} answers 501.
 */
private ExpandItem expandItemOptions(String optionList, EReference reference,
		EClass context, NestedOptionParser optionParser, boolean refOnly, EClass cast, int depth) {
	CollectionOptions.Accumulator options = new CollectionOptions.Accumulator();
	int levels = 1;
	SelectTree select = null;
	Map<String, ExpandItem> expand = Map.of();
	for (String option : SelectTree.splitTopLevel(optionList, ';')) {
		String trimmed = option.trim();
		Matcher selectMatcher = NESTED_SELECT.matcher(trimmed);
		if (selectMatcher.find()) {
			if (select != null) {
				throw new ODataQueryParseException("duplicate $select inside $expand");
			}
			select = SelectTree.parse(trimmed.substring(selectMatcher.end()), context, optionParser,
					selectableControls());
			continue;
		}
		Matcher expandMatcher = NESTED_EXPAND.matcher(trimmed);
		if (expandMatcher.find()) {
			if (!expand.isEmpty()) {
				throw new ODataQueryParseException("duplicate $expand inside $expand");
			}
			expand = parseExpand(trimmed.substring(expandMatcher.end()), context, optionParser,
					depth + 1);
			continue;
		}
		Matcher matcher = NESTED_LEVELS.matcher(trimmed);
		if (matcher.find()) {
			String value = trimmed.substring(matcher.end()).trim();
			levels = "max".equalsIgnoreCase(value) ? MAX_EXPAND_LEVELS
					: parsedLevels(value);
			if (!(reference.getEReferenceType()
					.getEStructuralFeature(reference.getName()) instanceof EReference)) {
				throw new ODataQueryParseException(
						"$levels requires a self-recursive navigation");
			}
			continue;
		}
		if (!options.accept(trimmed, reference, context, optionParser)) {
			throw new UnsupportedOperationException(
					"this nested $expand option is not implemented");
		}
	}
	return new ExpandItem(options.build(), refOnly, cast, levels, select, expand);
}

private static final Pattern NESTED_LEVELS =
		Pattern.compile("(?i)^\\$?levels=");
private static final Pattern NESTED_SELECT =
		Pattern.compile("(?i)^\\$?select=");
private static final Pattern NESTED_EXPAND =
		Pattern.compile("(?i)^\\$?expand=");

private static int parsedLevels(String value) {
	try {
		int levels = Integer.parseInt(value);
		if (levels < 1 || levels > MAX_EXPAND_LEVELS) {
			throw new NumberFormatException();
		}
		return levels;
	} catch (NumberFormatException e) {
		throw new ODataQueryParseException(
				"$levels takes 1.." + MAX_EXPAND_LEVELS + " or 'max'");
	}
}

/**
 * The request tree for the shaper: the {@code $select} and every inline expansion with its
 * nested select/expand. {@code $levels} unrolls into nested plain expansions of the same
 * navigation — only the first level carries the options.
 */
static EntityShaper.Shape shape(SelectTree select, Map<String, ExpandItem> expand) {
	return new EntityShaper.Shape(select, expansions(expand));
}

private static Map<String, EntityShaper.Expansion> expansions(Map<String, ExpandItem> expand) {
	Map<String, EntityShaper.Expansion> expansions = new LinkedHashMap<>();
	expand.forEach((name, item) -> {
		if (item.refOnly()) {
			return;
		}
		Map<String, EntityShaper.Expansion> nested = expansions(item.expand());
		if (item.levels() > 1 && !nested.containsKey(name)) {
			nested = new LinkedHashMap<>(nested);
			nested.putAll(levelChain(name, item.levels() - 1));
		}
		expansions.put(name, new EntityShaper.Expansion(
				new EntityShaper.Shape(item.select(), nested), item.options(), item.cast()));
	});
	return expansions;
}

private static Map<String, EntityShaper.Expansion> levelChain(String name, int levels) {
	if (levels <= 0) {
		return Map.of();
	}
	return Map.of(name, new EntityShaper.Expansion(
			new EntityShaper.Shape(null, levelChain(name, levels - 1)), CollectionOptions.NONE, null));
}

/**
 * The same tree, carrying each item's options as the ASK to the backend (ADR-0008). Only a
 * first-level navigation WITHOUT deeper paths carries them: a deeper path ({@code nav/next},
 * nested {@code $expand}, {@code $levels}) has the backend resolve {@code nav} in full on the
 * way, and the resolved state is the very thing a narrowed expansion is read from — that
 * navigation keeps its options in memory instead.
 *
 * <p>An item that wants {@code $count} deliberately asks for no paging: the count of matching
 * children is derivable from a pushed filter only as long as nothing truncated the result, and
 * a filtered-but-unpaged expansion is exactly that state.
 */
static List<ExpandSpec> expandSpecs(Map<String, ExpandItem> expand) {
	List<ExpandSpec> specs = new ArrayList<>();
	expand.forEach((name, item) -> {
		if (item.refOnly()) {
			return;
		}
		CollectionOptions options = item.options();
		boolean countable = options.count();
		boolean leaf = item.expand().isEmpty() && item.levels() <= 1;
		specs.add(leaf
				? new ExpandSpec(name, options.filter(), options.orderBy(),
						countable ? 0 : options.skip(), countable ? -1 : options.top())
				: ExpandSpec.of(name));
		for (String path : shapePaths(Map.of(name, item))) {
			if (!path.equals(name)) {
				specs.add(ExpandSpec.of(path));
			}
		}
	});
	return specs;
}

/**
 * The paths the SHAPER co-copies (and backends prefetch): the inline navigation names, their
 * nested expansions and the self-recursive {@code $levels} chains ({@code nav}, {@code nav/nav},
 * …).
 */
static Set<String> shapePaths(Map<String, ExpandItem> expand) {
	return shape(null, expand).paths();
}

/** Top-level comma split of {@code $expand} — parens and string literals stay intact. */
private static List<String> splitExpandItems(String expand) {
	List<String> items = new ArrayList<>();
	int depth = 0;
	boolean quoted = false;
	int start = 0;
	for (int i = 0; i < expand.length(); i++) {
		char c = expand.charAt(i);
		if (c == '\'') {
			quoted = !quoted;
		} else if (!quoted && c == '(') {
			depth++;
		} else if (!quoted && c == ')') {
			depth--;
		} else if (!quoted && depth == 0 && c == ',') {
			items.add(expand.substring(start, i));
			start = i + 1;
		}
	}
	items.add(expand.substring(start));
	return items;
}

/**
 * Shaped copies for a self-contained document (XMI): the entities plus their expanded targets
 * as extra roots, nested casts and collection options applied ({@link EntityShaper}).
 */
List<EObject> shapeAll(List<EObject> entities, SelectTree select, Map<String, ExpandItem> expand,
		Map<String, ExpandPushdown> narrowed) {
	return servlet.shaper.shapeAll(entities, shape(select, expand), narrowed);
}

String entityJson(EObject entity, EClass entityType, SelectTree select, Set<String> expand)
		throws IOException {
	return entityJson(entity, entityType, EntityShaper.Shape.ofPaths(select, expand), Map.of(), Map.of());
}

/** {@link #entityJson} for parsed expand specs: applies nested casts/options after shaping. */
String entityJson(EObject entity, EClass entityType, SelectTree select,
		Map<String, ExpandItem> expand) throws IOException {
	return entityJson(entity, entityType, select, expand, Map.of());
}

/** {@link #entityJson} knowing which expansions the backend already narrowed (ADR-0008). */
String entityJson(EObject entity, EClass entityType, SelectTree select,
		Map<String, ExpandItem> expand, Map<String, ExpandPushdown> narrowed) throws IOException {
	return entityJson(entity, entityType, shape(select, expand), expand, narrowed);
}

private String entityJson(EObject entity, EClass entityType, EntityShaper.Shape shape,
		Map<String, ExpandItem> expand, Map<String, ExpandPushdown> narrowed) throws IOException {
	Map<EObject, Map<String, Long>> counts = new IdentityHashMap<>();
	EObject copy = servlet.shaper.shape(entity, shape, narrowed, counts, null);
	return withExpandedRefs(serializeEntity(entity, copy, entityType, shape, counts), entity, expand);
}

/**
 * Splices {@code $expand=nav/$ref} members into the entity JSON ([OData-URL] 5.1.3.1):
 * entity-reference objects built from the ORIGINAL entity's navigation values — full
 * entities are neither shaped nor serialized for these navigations.
 */
private String withExpandedRefs(String entityJson, EObject entity,
		Map<String, ExpandItem> expand) {
	StringBuilder members = new StringBuilder();
	for (Map.Entry<String, ExpandItem> entry : expand.entrySet()) {
		if (!entry.getValue().refOnly()) {
			continue;
		}
		EStructuralFeature feature = entity.eClass().getEStructuralFeature(entry.getKey());
		if (feature == null) {
			continue;
		}
		members.append(",\"").append(entry.getKey()).append("\":");
		if (entity.eGet(feature) instanceof List<?> children) {
			members.append('[');
			for (int i = 0; i < children.size(); i++) {
				if (i > 0) {
					members.append(',');
				}
				members.append(servlet.referenceJson((EObject) children.get(i)));
			}
			members.append(']');
		} else if (entity.eGet(feature) instanceof EObject child) {
			members.append(servlet.referenceJson(child));
		} else {
			members.append("null"); // no related entity ([OData-JSON] expanded references)
		}
	}
	if (members.isEmpty()) {
		return entityJson;
	}
	String inner = entityJson.substring(1, entityJson.length() - 1);
	return "{" + (inner.isEmpty() ? members.substring(1) : inner + members) + "}";
}

/**
 * @param shape  the request tree the copy was shaped with
 * @param counts the inline counts per shaped copy ({@code name@odata.count} members)
 */
private String serializeEntity(EObject entity, EObject copy, EClass entityType,
		EntityShaper.Shape shape, Map<EObject, Map<String, Long>> counts) throws IOException {
	// the copy carries exactly the requested navigations, linked one way: everything it holds
	// is to be written inline, bidirectional navigations included
	Set<String> expand = shape.navigationNames();
	Wire wire = Wire.current();
	// a wire profile writes its own control information — never the full OData metadata
	boolean full = wire.odataControls() && "full".equals(ODataServlet.responseMetadataLevel());
	// full metadata: the default codec profile emits @odata.type/@odata.id per entity;
	// minimal: control info that is computable from the context URL is left out ([OData-JSON] 3.1)
	ODataJsonResourceImpl resource = full
			? new ODataJsonResourceImpl(URI.createURI("response.odatajson"), servlet.metadataService, expand)
			: ODataJsonResourceImpl.minimalMetadata(
					URI.createURI("response.odatajson"), servlet.metadataService, expand);
	resource.ieee754Compatible(ODataServlet.ieee754()).allValues(true);
	resource.getContents().add(copy);
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	resource.save(out, null);
	String json = retainShaped(out.toString(StandardCharsets.UTF_8), copy, shape, counts);
	String typeName = wire.names().type();
	if (!full && !ODataServlet.omitContext() && entity.eClass() != entityType && typeName != null) {
		// derived instance under minimal metadata: the type is NOT computable from the context
		// URL, so transport the single-field discriminator ([OData-JSON] 4.5.8). Full metadata
		// already carries @odata.type; metadata=none MUST omit it like all control information.
		json = "{\"" + typeName + "\":\"" + resource.typeDiscriminator(entity) + "\""
				+ (json.length() > 2 ? "," : "") + json.substring(1);
	}
	return json;
}

/**
 * Exact numbers on the round trip: a re-serialized {@code 1.20} stays {@code 1.20}, an Int64
 * beyond the double range stays exact.
 */
private static final ObjectMapper EXACT_JSON = JsonMapper.builder()
		.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
		.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
		.enable(JsonNodeFeature.USE_BIG_DECIMAL_FOR_FLOATS)
		.disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES)
		.enable(JsonNodeFeature.READ_NULL_PROPERTIES)
		.enable(JsonNodeFeature.WRITE_NULL_PROPERTIES)
		.build();

/**
 * The codec writes EVERY value (null, default, empty — a selected property must be in the
 * payload, [OData-Protocol] 8.2.8.6), so the members the shaping deliberately excluded are
 * dropped here: unselected structural properties and non-expanded navigation properties. The
 * inline counts are spliced in on the way ({@code name@odata.count}, [OData-JSON] 4.5.5;
 * strings under IEEE754Compatible like every Edm.Int64 count).
 */
private String retainShaped(String json, EObject copy, EntityShaper.Shape shape,
		Map<EObject, Map<String, Long>> counts) {
	if (!(EXACT_JSON.readTree(json) instanceof ObjectNode node)) {
		return json;
	}
	retainShaped(node, copy, shape, counts);
	return EXACT_JSON.writeValueAsString(node);
}

private void retainShaped(ObjectNode node, EObject copy, EntityShaper.Shape shape,
		Map<EObject, Map<String, Long>> counts) {
	EClass type = copy.eClass();
	SelectTree select = shape.select();
	List<EAttribute> keys = servlet.keyAttributes(type);
	for (EStructuralFeature feature : type.getEAllStructuralFeatures()) {
		String name = feature.getName();
		JsonNode member = node.get(name);
		if (member == null) {
			continue;
		}
		EntityShaper.Expansion expansion = shape.expand().get(name);
		if (feature instanceof EReference reference && !reference.isContainment()) {
			if (expansion == null) {
				node.remove(name); // not expanded: navigation stays out of the payload
				continue;
			}
			retainNested(member, copy.eGet(reference), expansion.shape(), counts);
			continue;
		}
		SelectTree child = select == null ? null : select.child(name);
		if (select != null && child == null && expansion == null && !keys.contains(feature)) {
			node.remove(name);
			continue;
		}
		if (feature instanceof EReference) { // complex value: a nested $select prunes it too
			EntityShaper.Shape nested = expansion != null ? expansion.shape()
					: new EntityShaper.Shape(child == null || child.isLeaf() ? null : child, Map.of());
			retainNested(member, copy.eGet(feature), nested, counts);
		}
	}
	Map<String, Long> inline = counts.get(copy);
	String countName = Wire.current().names().count();
	if (inline != null && countName != null) {
		inline.forEach((name, total) -> {
			if (ODataServlet.ieee754()) {
				node.put(name + countName, String.valueOf(total));
			} else {
				node.put(name + countName, total.longValue());
			}
		});
	}
	decorate(node, copy, shape);
}

/**
 * The per-entity control information of a wire profile (ADR-0010): the key value and the self
 * link first, then the properties (the key left out when the profile keeps it out of the body),
 * then one navigation link per navigation property. Only entities served by an entity set get
 * them; complex values and contained keyless objects stay as they are. Under {@code $select}
 * the control information behaves like properties (#121 D4, as FROST does): the key value comes
 * with a selected key, the self link with its selected name ({@code $select=@iot.selfLink}), a
 * navigation link with a selected navigation — being expanded is not enough.
 */
private void decorate(ObjectNode node, EObject copy, EntityShaper.Shape shape) {
	Wire wire = Wire.current();
	ControlNames names = wire.names();
	EClass type = copy.eClass();
	boolean keyInBody = wire.profile().keyInBody(type);
	if (names.keyValue() == null && names.selfLink() == null && names.navigationLink() == null
			&& keyInBody) {
		return; // nothing to add or take away (plain OData)
	}
	List<EAttribute> keys = servlet.keyAttributes(type);
	String path = keys.isEmpty() ? null : servlet.entityPathOf(copy);
	if (path == null) {
		return;
	}
	SelectTree select = shape.select();
	String self = (wire.absoluteLinks() ? wire.baseUrl() + "/" : "") + path;
	ObjectNode ordered = node.objectNode();
	boolean keySelected = select == null
			|| keys.stream().anyMatch(key -> select.child(key.getName()) != null);
	if (keySelected && names.keyValue() != null) {
		if (keys.size() == 1) {
			JsonNode value = node.get(keys.get(0).getName());
			ordered.set(names.keyValue(), value != null ? value
					: node.pojoNode(copy.eGet(keys.get(0))));
		} else {
			ObjectNode composite = ordered.putObject(names.keyValue());
			keys.forEach(key -> composite.set(key.getName(), node.get(key.getName())));
		}
	}
	if (names.selfLink() != null && (select == null || select.selectsControl(names.selfLink()))) {
		ordered.put(names.selfLink(), self);
	}
	Set<String> keyNames = keys.stream().map(EAttribute::getName).collect(Collectors.toSet());
	node.properties().forEach(member -> {
		if (keyInBody || !keyNames.contains(member.getKey())) {
			ordered.set(member.getKey(), member.getValue());
		}
	});
	if (names.navigationLink() != null) {
		for (EReference reference : type.getEAllReferences()) {
			String name = reference.getName();
			if (!reference.isContainment() && (select == null || select.child(name) != null)) {
				ordered.put(name + names.navigationLink(), self + "/" + name);
			}
		}
	}
	node.removeAll();
	node.setAll(ordered);
}

/** Pairs the JSON value of a structured member with its EObject value(s), in payload order. */
private void retainNested(JsonNode member, Object value, EntityShaper.Shape shape,
		Map<EObject, Map<String, Long>> counts) {
	if (member instanceof ObjectNode object && value instanceof EObject nested) {
		retainShaped(object, nested, shape, counts);
	} else if (member instanceof ArrayNode array && value instanceof List<?> items
			&& array.size() == items.size()) {
		for (int i = 0; i < items.size(); i++) {
			if (array.get(i) instanceof ObjectNode object && items.get(i) instanceof EObject nested) {
				retainShaped(object, nested, shape, counts);
			}
		}
	}
}

/**
 * XML representation = EMF XMI of the (shaped) entities. Deliberately NOT OData Atom —
 * that format is deprecated since OData 4.01; XMI is the natural XML form in the EMF
 * ecosystem. Selected via {@code $format=xml} or an XML-only Accept header.
 */
void writeXmi(HttpServletResponse response, List<EObject> roots) throws IOException {
	ResourceSet rs = new ResourceSetImpl();
	rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
	Resource resource = rs.createResource(URI.createURI("response.xmi"));
	resource.getContents().addAll(roots);
	Map<Object, Object> options = new HashMap<>();
	options.put(XMLResource.OPTION_ENCODING, "UTF-8");
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	resource.save(out, options);
	response.setContentType("application/xml;charset=UTF-8");
	response.getWriter().write(out.toString(StandardCharsets.UTF_8));
}

/**
 * Whether the {@code Accept} header lists ONLY media types this server never emits. An absent or
 * blank header, or any range we can satisfy (JSON, XML, or a wildcard), is acceptable; a header
 * naming only e.g. {@code text/csv} or {@code application/atom+xml} (Atom is not emitted, 4.01
 * deprecated) is not → the caller answers 406.
 */
static boolean notAcceptable(HttpServletRequest request) {
	String accept = request.getHeader("Accept");
	if (accept == null || accept.isBlank()) {
		return false;
	}
	for (String range : accept.split(",")) {
		String media = range.split(";")[0].trim().toLowerCase(Locale.ROOT);
		if (media.isEmpty() || media.equals("*/*") || media.equals("application/*")
				|| media.equals("text/*") || media.equals("application/json")
				|| media.equals("application/xml") || media.equals("text/xml")) {
			return false;
		}
	}
	return true;
}

boolean wantsXml(HttpServletRequest request) {
	String format = ODataServlet.option(request, "$format");
	if (format != null) {
		if ("xml".equalsIgnoreCase(format)) {
			return true;
		}
		if ("json".equalsIgnoreCase(format)) {
			return false;
		}
		throw new ODataQueryParseException("unsupported $format (json or xml)");
	}
	String accept = request.getHeader("Accept");
	return accept != null && !accept.contains("application/json")
			&& (accept.contains("application/xml") || accept.contains("text/xml"));
}
}
