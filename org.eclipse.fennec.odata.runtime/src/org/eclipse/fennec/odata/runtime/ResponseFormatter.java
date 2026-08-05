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
import java.util.HashMap;
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
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.fennec.m2x.model.ocl.IntegerLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OclFactory;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.RealLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.Variable;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.odata.codec.json.ODataJsonResourceImpl;
import org.eclipse.fennec.odata.csdl.CsdlJsonWriter;
import org.eclipse.fennec.odata.csdl.EcoreToEdmConverter;
import org.eclipse.fennec.odata.csdl.ODataAnnotationConstants;
import org.eclipse.fennec.odata.csdl.OdataResolver;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.operation.api.ODataOperationHandler;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.DeltaGoneException;
import org.eclipse.fennec.odata.persistence.api.DeltaService;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.MediaService;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.odata.query.CachingODataQueryParser;
import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.ODataResourceParser;
import org.eclipse.fennec.odata.ocl.evaluator.OclEvaluator;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.odata.query.ResourcePath;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TPropertyValue;
import org.open.oasis.docs.odata.ns.edm.TRecordExpression;
import org.open.oasis.docs.odata.ns.edmx.EdmxFactory;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;
import org.open.oasis.docs.odata.ns.edmx.TInclude;
import org.open.oasis.docs.odata.ns.edmx.TReference;
import org.open.oasis.docs.odata.ns.edmx.TVersion;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletPattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

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
	return SelectTree.parse(select, entityType, nestedOptions(request));
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
 */
record ExpandItem(CollectionOptions options, boolean refOnly, EClass cast, int levels) {
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
 * shapes: {@code nav}, {@code nav($filter=…)}, {@code nav/$ref} and {@code nav/Ns.Type}
 * (optionally with a nested {@code $filter} against the derived type); other nested
 * options answer 501.
 */
Map<String, ExpandItem> expandOption(HttpServletRequest request, EClass entityType) {
	String expand = ODataServlet.option(request, "$expand");
	Map<String, ExpandItem> items = new LinkedHashMap<>();
	if (expand == null || expand.isBlank()) {
		return items;
	}
	servlet.limits.checkExpression(expand); // nested $filter trees are parsed — same hostile-input guard
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
						nestedOptions(request), refOnly, cast));
	}
	return items;
}

/** Recursion cap for {@code $levels=max} ([OData-URL] 5.1.2 expandOption). */
private static final int MAX_EXPAND_LEVELS = 8;

/**
 * The {@code ;}-separated option list of one {@code $expand} item — the collection options
 * (Advanced 9.2/9.4–9.7) plus {@code $levels} for SELF-RECURSIVE navigations (9.8);
 * {@code $select}/{@code $expand}/{@code $compute} inside {@code $expand} answer 501.
 */
private ExpandItem expandItemOptions(String optionList, EReference reference,
		EClass context, NestedOptionParser optionParser, boolean refOnly, EClass cast) {
	CollectionOptions.Accumulator options = new CollectionOptions.Accumulator();
	int levels = 1;
	for (String option : SelectTree.splitTopLevel(optionList, ';')) {
		String trimmed = option.trim();
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
	return new ExpandItem(options.build(), refOnly, cast, levels);
}

private static final Pattern NESTED_LEVELS =
		Pattern.compile("(?i)^\\$?levels=");

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

/** The navigations rendered INLINE — everything except the {@code /$ref} items. */
static Set<String> inlineNavs(Map<String, ExpandItem> expand) {
	return expand.entrySet().stream()
			.filter(item -> !item.getValue().refOnly())
			.map(Map.Entry::getKey)
			.collect(Collectors.toCollection(LinkedHashSet::new));
}

/**
 * The paths the SHAPER co-copies (and backends prefetch): the inline navigation names plus
 * the self-recursive {@code $levels} chains ({@code nav}, {@code nav/nav}, …).
 */
static Set<String> shapePaths(Map<String, ExpandItem> expand) {
	Set<String> paths = new LinkedHashSet<>();
	expand.forEach((name, item) -> {
		if (item.refOnly()) {
			return;
		}
		StringBuilder chain = new StringBuilder(name);
		paths.add(chain.toString());
		for (int level = 2; level <= item.levels(); level++) {
			chain.append('/').append(name);
			paths.add(chain.toString());
		}
	});
	return paths;
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
 * Nested {@code $expand} casts and collection options run on the SHAPED copy — never on
 * backend objects. Returns the requested inline counts (navigation → filtered, pre-paging
 * count) for the {@code name@odata.count} response members.
 */
Map<String, Long> applyNestedFilters(EObject copy, Map<String, ExpandItem> expand) {
	Map<String, Long> counts = new LinkedHashMap<>();
	for (Map.Entry<String, ExpandItem> entry : expand.entrySet()) {
		ExpandItem item = entry.getValue();
		if (item.refOnly() || (item.cast() == null && item.options().isNone())) {
			continue;
		}
		EStructuralFeature feature = copy.eClass().getEStructuralFeature(entry.getKey());
		if (feature == null) {
			continue;
		}
		if (copy.eGet(feature) instanceof List<?> children) {
			if (item.cast() != null) { // cast-in-expand: only derived instances stay (5.1.3.2)
				children.removeIf(child -> !item.cast().isInstance(child));
			}
			long total = servlet.shaper.applyOptions(children, item.options());
			if (item.options().count()) {
				counts.put(entry.getKey(), total);
			}
		} else if (item.cast() != null && copy.eGet(feature) instanceof EObject child
				&& !item.cast().isInstance(child)) {
			copy.eUnset(feature); // single-valued: a non-matching instance is not expanded
		}
	}
	return counts;
}

String entityJson(EObject entity, EClass entityType, SelectTree select, Set<String> expand)
		throws IOException {
	return serializeEntity(entity, servlet.shaper.shape(entity, entityType, select, expand, null),
			entityType, expand);
}

/** {@link #entityJson} for parsed expand specs: applies nested casts/options after shaping. */
String entityJson(EObject entity, EClass entityType, SelectTree select,
		Map<String, ExpandItem> expand) throws IOException {
	Set<String> inline = inlineNavs(expand);
	Map<String, Long> counts = new LinkedHashMap<>();
	EObject copy = servlet.shaper.shape(entity, entityType, select, shapePaths(expand), null, counts);
	counts.putAll(applyNestedFilters(copy, expand));
	return withExpandedRefs(withNestedCounts(
			serializeEntity(entity, copy, entityType, inline), counts), entity, expand);
}

/**
 * Splices the requested inline counts of nested collections ({@code $expand}/{@code $select}
 * {@code $count=true}) as {@code name@odata.count} members ([OData-JSON] 4.5.5; strings
 * under IEEE754Compatible like every Edm.Int64 count).
 */
private String withNestedCounts(String entityJson, Map<String, Long> counts) {
	if (counts.isEmpty()) {
		return entityJson;
	}
	StringBuilder members = new StringBuilder();
	counts.forEach((name, total) -> members.append(",\"").append(name)
			.append("@odata.count\":").append(ODataServlet.countValue(total)));
	String inner = entityJson.substring(1, entityJson.length() - 1);
	return "{" + (inner.isEmpty() ? members.substring(1) : inner + members) + "}";
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
				members.append("{\"@odata.id\":\"")
						.append(ODataJson.sanitize(servlet.entityIdOf((EObject) children.get(i))))
						.append("\"}");
			}
			members.append(']');
		} else if (entity.eGet(feature) instanceof EObject child) {
			members.append("{\"@odata.id\":\"")
					.append(ODataJson.sanitize(servlet.entityIdOf(child))).append("\"}");
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

private String serializeEntity(EObject entity, EObject copy, EClass entityType,
		Set<String> expand) throws IOException {
	boolean full = "full".equals(ODataServlet.responseMetadataLevel());
	// full metadata: the default codec profile emits @odata.type/@odata.id per entity;
	// minimal: control info that is computable from the context URL is left out ([OData-JSON] 3.1)
	ODataJsonResourceImpl resource = full
			? new ODataJsonResourceImpl(URI.createURI("response.odatajson"), servlet.metadataService, expand)
			: ODataJsonResourceImpl.minimalMetadata(
					URI.createURI("response.odatajson"), servlet.metadataService, expand);
	resource.ieee754Compatible(ODataServlet.ieee754());
	resource.getContents().add(copy);
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	resource.save(out, null);
	String json = out.toString(StandardCharsets.UTF_8);
	if (!full && !ODataServlet.omitContext() && entity.eClass() != entityType) {
		// derived instance under minimal metadata: the type is NOT computable from the context
		// URL, so transport the single-field discriminator ([OData-JSON] 4.5.8). Full metadata
		// already carries @odata.type; metadata=none MUST omit it like all control information.
		json = "{\"@odata.type\":\"" + resource.typeDiscriminator(entity) + "\""
				+ (json.length() > 2 ? "," : "") + json.substring(1);
	}
	return json;
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
