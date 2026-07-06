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
import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
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
import org.eclipse.fennec.model.metadata.api.MetadataService;
import org.eclipse.fennec.odata.codec.json.ODataJsonResourceImpl;
import org.eclipse.fennec.odata.csdl.EcoreToEdmConverter;
import org.eclipse.fennec.odata.csdl.OdataResolver;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.query.CachingODataQueryParser;
import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.ODataResourceParser;
import org.eclipse.fennec.odata.query.ResourcePath;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edmx.EdmxFactory;
import org.open.oasis.docs.odata.ns.edmx.TInclude;
import org.open.oasis.docs.odata.ns.edmx.TReference;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;
import org.open.oasis.docs.odata.ns.edmx.TVersion;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Catch-all OData servlet (E6/E7 skeleton, ADR-0001: plain Jakarta Servlet on the OSGi HTTP
 * Whiteboard, no Jakarta REST). v1 surface, read-only:
 * <ul>
 *   <li>{@code GET <root>/} — service document (entity sets as JSON)</li>
 *   <li>{@code GET <root>/$metadata} — CSDL document via the E2 converter, one Schema per
 *       registered EPackage</li>
 *   <li>{@code GET <root>/<EntitySet>?$filter=&$orderby=&$top=&$skip=&$count=} — query via the
 *       E5 {@link QueryService} SPI, OData-JSON response via the E3 codec conventions</li>
 * </ul>
 *
 * <p><b>Security posture:</b> every request runs against hard limits BEFORE parsing —
 * expression length ({@value #DEFAULT_MAX_EXPRESSION_LENGTH}), parenthesis nesting depth
 * ({@value #DEFAULT_MAX_NESTING_DEPTH}, parser-bomb guard) and an enforced {@code $top} ceiling
 * ({@value #DEFAULT_MAX_TOP}, applied even when the client sends none). There is no string
 * concatenation into any backend — the ONLY query path is the typed OCL IR, unknown properties/
 * functions fail the parse (400). Error responses carry sanitized messages, never stack traces
 * or exception class names; unexpected failures answer with a generic 500.
 */
@Component(service = Servlet.class, configurationPid = ODataServlet.PID, property = {
		"osgi.http.whiteboard.servlet.pattern=/odata/*",
		"osgi.http.whiteboard.servlet.name=Fennec OData"
})
public class ODataServlet extends HttpServlet {

	public static final String PID = "org.eclipse.fennec.odata.servlet";
	private static final long serialVersionUID = 1L;

	private final ODataResourceParser resourceParser = new ODataResourceParser();

	private final List<EPackage> packages = new CopyOnWriteArrayList<>();
	private final List<QueryService> queryServices = new CopyOnWriteArrayList<>();
	private final CachingODataQueryParser parser = new CachingODataQueryParser();
	/** Schema namespace/alias per package for cast resolution — same derivation as $metadata. */
	private final Map<EPackage, ODataPackageProfile> profiles =
			java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
	private final EntityShaper shaper = new EntityShaper();

	private volatile MetadataService metadataService;
	private volatile RequestLimits limits = RequestLimits.DEFAULTS;

	@Activate
	void activate(Map<String, Object> configuration) {
		limits = RequestLimits.fromConfiguration(configuration);
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addEPackage(EPackage ePackage) {
		packages.add(ePackage);
	}

	void removeEPackage(EPackage ePackage) {
		packages.remove(ePackage);
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addQueryService(QueryService queryService) {
		queryServices.add(queryService);
	}

	void removeQueryService(QueryService queryService) {
		queryServices.remove(queryService);
	}

	@Reference
	void setMetadataService(MetadataService metadataService) {
		this.metadataService = metadataService;
	}

	/** System query options this service implements. */
	private static final Set<String> SUPPORTED_OPTIONS = Set.of(
			"$filter", "$orderby", "$top", "$skip", "$count", "$select", "$expand", "$apply", "$format");
	/** Spec-defined options we know but do not implement yet → 501 (conformance 13.1.1/7). */
	private static final Set<String> KNOWN_UNSUPPORTED_OPTIONS = Set.of(
			"$search", "$compute", "$skiptoken", "$deltatoken", "$id", "$index", "$schemaversion", "$levels");

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setHeader("OData-Version", negotiateVersion(request));

		String maxVersion = request.getHeader("OData-MaxVersion");
		if (maxVersion != null && !maxVersion.isBlank()
				&& !"4.0".equals(maxVersion.trim()) && !"4.01".equals(maxVersion.trim())) {
			error(response, HttpServletResponse.SC_BAD_REQUEST,
					"unsupported OData-MaxVersion (supported: 4.0, 4.01)");
			return;
		}
		for (String name : request.getParameterMap().keySet()) {
			if (name.startsWith("@")) { // parameter alias value (11.2.5.1.3), consumed at parse time
				continue;
			}
			String normalized = normalizeOption(name);
			if (SUPPORTED_OPTIONS.contains(normalized)) {
				continue;
			}
			if (KNOWN_UNSUPPORTED_OPTIONS.contains(normalized)) { // known but unsupported → 501
				error(response, 501, "system query option '" + ODataJson.sanitize(name)
						+ "' is not implemented");
				return;
			}
			if (name.startsWith("$")) { // not a valid system query option → 400
				error(response, HttpServletResponse.SC_BAD_REQUEST,
						"unknown system query option '" + ODataJson.sanitize(name) + "'");
				return;
			}
			// no $ prefix and no system-option name → custom query option, ignored (11.2.12)
		}

		String path = request.getPathInfo() == null ? "/" : request.getPathInfo();
		try {
			if ("/".equals(path) || path.isEmpty()) {
				serviceDocument(request, response);
			} else if ("/$metadata".equals(path)) {
				metadataDocument(response);
			} else {
				resource(path.substring(1), request, response);
			}
		} catch (ODataQueryParseException | IllegalArgumentException e) {
			// client errors carry the (parser-)message — it never contains internals
			error(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		} catch (Exception e) {
			// no exception details leave the server (no class names, no stack traces)
			error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error");
		}
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws jakarta.servlet.ServletException, IOException {
		if ("GET".equals(request.getMethod())) {
			super.service(request, response);
		} else {
			response.setHeader("OData-Version", negotiateVersion(request));
			error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "read-only service (v1): only GET is supported");
		}
	}

	/** The response is 4.01 unless the client pins {@code OData-MaxVersion: 4.0} (8.1.5). */
	private static String negotiateVersion(HttpServletRequest request) {
		String maxVersion = request.getHeader("OData-MaxVersion");
		return maxVersion != null && "4.0".equals(maxVersion.trim()) ? "4.0" : "4.01";
	}

	// --- routes ---

	private void serviceDocument(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String sets = entitySetNames().stream()
				.map(name -> "{\"name\":\"" + name + "\",\"kind\":\"EntitySet\",\"url\":\"" + name + "\"}")
				.collect(Collectors.joining(","));
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write("{\"@odata.context\":\"" + request.getRequestURI()
				+ "/$metadata\",\"value\":[" + sets + "]}");
	}

	private void metadataDocument(HttpServletResponse response) throws Exception {
		EcoreToEdmConverter converter = new EcoreToEdmConverter();
		EdmxRoot root = EdmxFactory.eINSTANCE.createEdmxRoot();
		TEdmx edmx = EdmxFactory.eINSTANCE.createTEdmx();
		edmx.setVersion(TVersion._40);
		// the served protocol versions (Core.ODataVersions, 13.1.2 SHOULD) and the capability
		// self-description (Capabilities, 13.2.1/13 SHOULD) need their vocabulary references —
		// they must precede DataServices in the edmx element sequence
		edmx.getReference().add(vocabularyReference("Org.OData.Core.V1", "Core"));
		edmx.getReference().add(vocabularyReference("Org.OData.Capabilities.V1", "Capabilities"));
		TDataServices dataServices = EdmxFactory.eINSTANCE.createTDataServices();
		for (EPackage pkg : packages) { // one Schema per registered package (req §3.3 composition)
			SchemaType schema = converter.toSchema(pkg);
			for (TEntityContainer container : schema.getEntityContainer()) {
				AnnotationType versions = EdmFactory.eINSTANCE.createAnnotationType();
				versions.setTerm("Org.OData.Core.V1.ODataVersions");
				versions.setString1("4.0 4.01");
				container.getAnnotation().add(versions);
				// what this v1 read-only service can and cannot do (12/13.2.1 advertisement)
				AnnotationType conformance = EdmFactory.eINSTANCE.createAnnotationType();
				conformance.setTerm("Org.OData.Capabilities.V1.ConformanceLevel");
				conformance.setEnumMember1(List.of("Org.OData.Capabilities.V1.ConformanceLevelType/Minimal"));
				container.getAnnotation().add(conformance);
				container.getAnnotation().add(
						boolCapability("Org.OData.Capabilities.V1.BatchSupported", false));
				container.getAnnotation().add(
						boolCapability("Org.OData.Capabilities.V1.AsynchronousRequestsSupported", false));
				container.getAnnotation().add(
						boolCapability("Org.OData.Capabilities.V1.KeyAsSegmentSupported", false));
			}
			dataServices.getSchema().add(schema);
		}
		edmx.setDataServices(dataServices);
		root.setEdmx(edmx);

		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
		rs.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
		rs.getPackageRegistry().put(EdmxPackage.eNS_URI, EdmxPackage.eINSTANCE);
		Resource resource = rs.createResource(URI.createURI("metadata.xml"));
		resource.getContents().add(root);
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
		options.put(XMLResource.OPTION_ENCODING, "UTF-8");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, options);

		response.setContentType("application/xml;charset=UTF-8");
		response.getWriter().write(out.toString(StandardCharsets.UTF_8));
	}

	/** {@code edmx:Reference} to an OASIS vocabulary, so its terms are resolvable for clients. */
	private static TReference vocabularyReference(String namespace, String alias) {
		TInclude include = EdmxFactory.eINSTANCE.createTInclude();
		include.setNamespace(namespace);
		include.setAlias(alias);
		TReference reference = EdmxFactory.eINSTANCE.createTReference();
		reference.setUri("https://oasis-tcs.github.io/odata-vocabularies/vocabularies/" + namespace + ".xml");
		reference.getInclude().add(include);
		return reference;
	}

	private static AnnotationType boolCapability(String term, boolean value) {
		AnnotationType annotation = EdmFactory.eINSTANCE.createAnnotationType();
		annotation.setTerm(term);
		annotation.setBool1(value);
		return annotation;
	}

	private record Target(EClass entityType, QueryService queryService) {
	}

	private Target resolveTarget(String setName, HttpServletResponse response) throws IOException {
		EClass entityType = resolveEntityType(setName);
		if (entityType == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "unknown entity set '" + ODataJson.sanitize(setName) + "'");
			return null;
		}
		QueryService queryService = queryServices.stream()
				.filter(s -> s.supports(entityType)).findFirst().orElse(null);
		if (queryService == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "no backend for entity set '" + ODataJson.sanitize(setName) + "'");
			return null;
		}
		return new Target(entityType, queryService);
	}

	private void entitySet(String setName, String castName, EClass castType,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		Target target = resolveTarget(setName, response);
		if (target == null) {
			return;
		}
		if (option(request, "$apply") != null) {
			if (castType != null) { // aggregation over a cast collection: honest 501 over wrong rows
				error(response, 501, "$apply on type-cast collections is not implemented");
				return;
			}
			apply(setName, target, request, response);
			return;
		}
		// a cast makes the DERIVED type the context: its properties are addressable in options
		EClass context = castType != null ? castType : target.entityType();
		boolean xml = wantsXml(request);
		SelectTree select = selectOption(request, context);
		Set<String> expand = expandOption(request, context);
		Map<String, String> aliases = parameterAliases(request);

		List<OrderBySegment> orderBy = parseChecked(option(request, "$orderby"),
				value -> aliases.isEmpty() ? parser.parseOrderBy(value, context)
						: parser.parseOrderBy(value, context, aliases));
		int skip = limits.effectiveSkip(option(request, "$skip"));
		int top = pageSize(request, response, limits.effectiveTop(option(request, "$top")));
		// peek one row beyond the page: partial results MUST carry @odata.nextLink (13.1.1/3)
		EntityQuery query = new EntityQuery(target.entityType(), castType,
				parseChecked(option(request, "$filter"),
						filter -> aliases.isEmpty() ? parser.parseFilter(filter, context)
								: parser.parseFilter(filter, context, aliases)),
				orderBy == null ? List.of() : orderBy,
				skip, top + 1,
				"true".equals(option(request, "$count")));

		QueryResult result = target.queryService().execute(query);
		boolean hasMore = result.entities().size() > top;
		List<EObject> page = hasMore ? result.entities().subList(0, top) : result.entities();

		if (xml) { // XMI is a non-OData projection — trimmed, but without an embedded link
			writeXmi(response, shaper.shapeAll(page, context, select, expand));
			return;
		}
		StringBuilder json = new StringBuilder("{\"@odata.context\":\"")
				.append(contextRoot(request)).append("/$metadata#").append(setName)
				.append(castName != null ? "/" + castName : "").append('"');
		if (result.totalCount() >= 0) {
			json.append(",\"@odata.count\":").append(result.totalCount());
		}
		json.append(",\"value\":[");
		for (int i = 0; i < page.size(); i++) {
			if (i > 0) {
				json.append(',');
			}
			json.append(entityJson(page.get(i), context, select, expand));
		}
		json.append(']');
		if (hasMore) {
			json.append(",\"@odata.nextLink\":\"")
					.append(ODataJson.sanitize(nextLink(request, skip + top))).append('"');
		}
		json.append('}');
		response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
		response.getWriter().write(json.toString());
	}

	/** The follow-up page URL: same request with {@code $skip} advanced past this page. */
	private String nextLink(HttpServletRequest request, int nextSkip) {
		StringBuilder link = new StringBuilder(request.getRequestURI()).append("?$skip=").append(nextSkip);
		for (String option : SUPPORTED_OPTIONS) {
			String value = option(request, option);
			if (value != null && !"$skip".equals(option)) {
				link.append('&').append(option).append('=')
						.append(java.net.URLEncoder.encode(value, StandardCharsets.UTF_8));
			}
		}
		return link.toString();
	}

	/** Dispatches a parsed resource path: set, set/$count, keyed entity, navigation walk. */
	private void resource(String rawPath, HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		ResourcePath path;
		try {
			path = resourceParser.parse(rawPath);
		} catch (ODataQueryParseException e) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "resource not found");
			return;
		}
		if (path.key() == null && path.segments().isEmpty()) {
			entitySet(path.entitySet(), null, null, request, response);
			return;
		}
		if (path.key() == null) {
			if (path.segments().get(0) instanceof ResourcePath.TypeCastSegment cast) {
				castOnSet(path, cast, request, response); // Set/Ns.T… ([OData-URL] 4.11)
			} else if (path.segments().size() == 1
					&& path.segments().get(0) instanceof ResourcePath.CountSegment) {
				setCount(path.entitySet(), null, request, response); // Set/$count (with optional $filter)
			} else {
				error(response, HttpServletResponse.SC_NOT_FOUND,
						"navigation requires an entity key");
			}
			return;
		}
		Target target = resolveTarget(path.entitySet(), response);
		if (target == null) {
			return;
		}
		EObject entity = fetchByKey(target, path.key(), response);
		if (entity == null) {
			return; // error already written
		}
		if (path.segments().isEmpty()) {
			singleEntity(path.entitySet(), entity, target.entityType(), request, response);
		} else {
			walk(path, entity, request, response);
		}
	}

	/** {@code GET Set/$count}: the (optionally filtered, optionally cast) total as text/plain. */
	private void setCount(String setName, EClass castType, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		Target target = resolveTarget(setName, response);
		if (target == null) {
			return;
		}
		EClass context = castType != null ? castType : target.entityType();
		QueryResult result = target.queryService().execute(new EntityQuery(target.entityType(),
				castType,
				parseChecked(option(request, "$filter"),
						filter -> parser.parseFilter(filter, context)),
				List.of(), 0, 0, true));
		response.setContentType("text/plain;charset=UTF-8");
		response.getWriter().write(String.valueOf(result.totalCount()));
	}

	/**
	 * Set-level derived-type cast ([OData-URL] 4.11): {@code Set/Ns.T} restricts the collection
	 * to instances of the derived type (and makes it the context type for query options),
	 * {@code Set/Ns.T(key)} addresses one instance and 404s when the entity is not of that
	 * type, both continue into {@code /$count} or a navigation walk.
	 */
	private void castOnSet(ResourcePath path, ResourcePath.TypeCastSegment cast,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		Target target = resolveTarget(path.entitySet(), response);
		if (target == null) {
			return;
		}
		EClass castType = resolveCastType(cast.qualifiedName(), target.entityType());
		if (castType == null) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "no type '"
					+ ODataJson.sanitize(cast.qualifiedName()) + "' derived from the entity set's type");
			return;
		}
		List<ResourcePath.Segment> rest = path.segments().subList(1, path.segments().size());
		if (cast.key() != null) {
			EObject entity = fetchByKey(target, cast.key(), response);
			if (entity == null) {
				return; // error already written
			}
			if (!castType.isInstance(entity)) {
				error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
				return;
			}
			if (rest.isEmpty()) {
				singleEntity(path.entitySet() + "/" + cast.qualifiedName(), entity, castType,
						request, response);
			} else {
				walk(new ResourcePath(path.entitySet(), cast.key(), rest), entity, request, response);
			}
			return;
		}
		if (rest.isEmpty()) {
			entitySet(path.entitySet(), cast.qualifiedName(), castType, request, response);
			return;
		}
		if (rest.size() == 1 && rest.get(0) instanceof ResourcePath.CountSegment) {
			setCount(path.entitySet(), castType, request, response);
			return;
		}
		error(response, HttpServletResponse.SC_NOT_FOUND, "navigation requires an entity key");
	}

	/**
	 * Resolves a {@code Ns.Type} (or {@code Alias.Type}) cast segment against the registered
	 * models; when {@code baseType} is given, the resolved class must derive from it (or be
	 * it). Namespace derivation matches {@code $metadata} (profile-driven).
	 */
	private EClass resolveCastType(String qualifiedName, EClass baseType) {
		int dot = qualifiedName.lastIndexOf('.');
		String namespace = qualifiedName.substring(0, dot);
		String localName = qualifiedName.substring(dot + 1);
		for (EPackage pkg : packages) {
			ODataPackageProfile profile = profiles.computeIfAbsent(pkg,
					p -> new OdataResolver().resolve(p));
			if (!namespace.equals(profile.getNamespace()) && !namespace.equals(profile.getAlias())) {
				continue;
			}
			if (pkg.getEClassifier(localName) instanceof EClass cast
					&& (baseType == null || baseType.isSuperTypeOf(cast))) {
				return cast;
			}
		}
		return null;
	}

	/** Loads one entity by raw key literal; writes the error response when absent/keyless. */
	private EObject fetchByKey(Target target, String rawKey, HttpServletResponse response)
			throws IOException {
		EAttribute keyAttribute = target.entityType().getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
		if (keyAttribute == null) {
			error(response, HttpServletResponse.SC_BAD_REQUEST, "entity set has no key");
			return null;
		}
		OperationCallExp keyFilter = OclFactory.eINSTANCE.createOperationCallExp();
		keyFilter.setName("=");
		PropertyCallExp keyProperty = OclFactory.eINSTANCE.createPropertyCallExp();
		keyProperty.setReferredProperty(keyAttribute);
		keyProperty.setIsImplicit(true);
		keyFilter.setOwnedSource(keyProperty);
		keyFilter.getOwnedArguments().add(keyLiteral(rawKey));

		QueryResult result = target.queryService().execute(
				new EntityQuery(target.entityType(), keyFilter, List.of(), 0, 1, false));
		if (result.entities().isEmpty()) {
			error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
			return null;
		}
		return result.entities().get(0);
	}

	/**
	 * Walks navigation/property segments on a loaded entity ([OData-URL] resource paths,
	 * ADR-0005). Terminals: entity/complex → JSON object, collection → JSON array,
	 * primitive → value document, {@code /$value} → raw text, {@code /$count} → count,
	 * null → 204. Query options on navigation paths are not implemented yet (501, except
	 * {@code $format}).
	 */
	private void walk(ResourcePath path, EObject root, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		for (String option : SUPPORTED_OPTIONS) {
			if (!"$format".equals(option) && option(request, option) != null) {
				error(response, 501, "query options on navigation paths are not implemented");
				return;
			}
		}
		Object current = root;
		for (int i = 0; i < path.segments().size(); i++) {
			ResourcePath.Segment segment = path.segments().get(i);
			boolean last = i == path.segments().size() - 1;
			switch (segment) {
				case ResourcePath.PropertySegment property -> {
					if (!(current instanceof EObject object)) {
						error(response, HttpServletResponse.SC_NOT_FOUND, "resource not found");
						return;
					}
					EStructuralFeature feature = object.eClass().getEStructuralFeature(property.name());
					if (feature == null) {
						error(response, HttpServletResponse.SC_NOT_FOUND,
								"unknown segment '" + ODataJson.sanitize(property.name()) + "'");
						return;
					}
					current = object.eGet(feature);
					if (property.key() != null) {
						current = selectByKey(current, property.key());
						if (current == null) {
							error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
							return;
						}
					}
				}
				case ResourcePath.TypeCastSegment cast -> {
					EClass castClass = resolveCastType(cast.qualifiedName(), null);
					if (castClass == null) {
						error(response, HttpServletResponse.SC_NOT_FOUND, "unknown type '"
								+ ODataJson.sanitize(cast.qualifiedName()) + "'");
						return;
					}
					if (current instanceof List<?> collection) { // collection cast: type filter
						current = collection.stream().filter(castClass::isInstance).toList();
						if (cast.key() != null) {
							current = selectByKey(current, cast.key());
							if (current == null) {
								error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
								return;
							}
						}
					} else if (current instanceof EObject object && cast.key() == null) {
						if (!castClass.isInstance(object)) { // single-entity cast: instance check
							error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
							return;
						}
					} else {
						error(response, HttpServletResponse.SC_NOT_FOUND, "resource not found");
						return;
					}
				}
				case ResourcePath.CountSegment count -> {
					if (!last || !(current instanceof List<?> collection)) {
						error(response, HttpServletResponse.SC_BAD_REQUEST,
								"$count requires a terminal collection");
						return;
					}
					response.setContentType("text/plain;charset=UTF-8");
					response.getWriter().write(String.valueOf(collection.size()));
					return;
				}
				case ResourcePath.ValueSegment value -> {
					// enum values are EObjects in dynamic EMF but primitive for OData purposes
					boolean structured = (current instanceof EObject && !(current instanceof Enumerator))
							|| current instanceof List<?>;
					if (!last || structured) {
						error(response, HttpServletResponse.SC_BAD_REQUEST,
								"$value requires a terminal primitive property");
						return;
					}
					if (current == null) {
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);
						return;
					}
					response.setContentType("text/plain;charset=UTF-8");
					response.getWriter().write(current instanceof Enumerator literal
							? literal.getLiteral() : String.valueOf(current));
					return;
				}
				case ResourcePath.RefSegment ref -> {
					error(response, 501, "$ref is not implemented");
					return;
				}
			}
		}
		writeWalkedValue(path, current, request, response);
	}

	private Object selectByKey(Object collection, String rawKey) {
		if (!(collection instanceof List<?> members)) {
			return null;
		}
		Object key = literalValue(rawKey);
		for (Object member : members) {
			if (member instanceof EObject object) {
				EAttribute id = object.eClass().getEAllAttributes().stream()
						.filter(EAttribute::isID).findFirst().orElse(null);
				if (id != null && String.valueOf(object.eGet(id)).equals(String.valueOf(key))) {
					return object;
				}
			}
		}
		return null;
	}

	private void writeWalkedValue(ResourcePath path, Object value, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		if (value == null) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			return;
		}
		String context = contextRoot(request) + "/$metadata#" + path.entitySet();
		if (value instanceof Enumerator literal) { // enum property → value document with the literal
			response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
			response.getWriter().write("{\"@odata.context\":\"" + ODataJson.sanitize(context)
					+ "\",\"value\":\"" + ODataJson.sanitize(literal.getLiteral()) + "\"}");
			return;
		}
		if (value instanceof EObject object) {
			if (wantsXml(request)) {
				writeXmi(response, shaper.shapeAll(List.of(object), object.eClass(), null, Set.of()));
				return;
			}
			String json = entityJson(object, object.eClass(), null, Set.of());
			response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
			response.getWriter().write("{\"@odata.context\":\"" + ODataJson.sanitize(context)
					+ "\"," + json.substring(1));
			return;
		}
		if (value instanceof List<?> collection) {
			if (wantsXml(request)) {
				List<EObject> objects = collection.stream()
						.filter(EObject.class::isInstance).map(EObject.class::cast).toList();
				writeXmi(response, objects.isEmpty() ? List.of()
						: shaper.shapeAll(objects, objects.get(0).eClass(), null, Set.of()));
				return;
			}
			StringBuilder json = new StringBuilder("{\"@odata.context\":\"")
					.append(ODataJson.sanitize(context)).append("\",\"value\":[");
			boolean first = true;
			for (Object member : collection) {
				if (!first) {
					json.append(',');
				}
				first = false;
				if (member instanceof EObject object) {
					json.append(entityJson(object, object.eClass(), null, Set.of()));
				} else {
					ODataJson.value(json, member);
				}
			}
			json.append("]}");
			response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
			response.getWriter().write(json.toString());
			return;
		}
		StringBuilder json = new StringBuilder("{\"@odata.context\":\"")
				.append(ODataJson.sanitize(context)).append("\",\"value\":");
		ODataJson.value(json, value instanceof java.util.Date date
				? java.time.format.DateTimeFormatter.ISO_INSTANT.format(date.toInstant()) : value);
		json.append('}');
		response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
		response.getWriter().write(json.toString());
	}

	/** {@code /{Set}({key})}: the key becomes a typed equality AST — never parsed as expression. */
	private void singleEntity(String setName, EObject entity, EClass entityType,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		SelectTree select = selectOption(request, entityType);
		Set<String> expand = expandOption(request, entityType);
		if (wantsXml(request)) {
			writeXmi(response, shaper.shapeAll(List.of(entity), entityType, select, expand));
			return;
		}
		String json = entityJson(entity, entityType, select, expand);
		// weave the context annotation into the entity object (single entities have no envelope)
		String context = "\"@odata.context\":\"" + contextRoot(request) + "/$metadata#" + setName
				+ "/$entity\",";
		response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
		response.getWriter().write("{" + context + json.substring(1));
	}

	/** Raw key literal → plain Java value (for member selection in walked collections). */
	private static Object literalValue(String rawKey) {
		if (rawKey.length() >= 2 && rawKey.startsWith("'") && rawKey.endsWith("'")) {
			return rawKey.substring(1, rawKey.length() - 1).replace("''", "'");
		}
		return rawKey;
	}

	/** OData key literal: {@code 'text'} (with {@code ''} escape) or a plain number. */
	private static OclExpression keyLiteral(String rawKey) {
		if (rawKey.length() >= 2 && rawKey.startsWith("'") && rawKey.endsWith("'")) {
			StringLiteralExp literal = OclFactory.eINSTANCE.createStringLiteralExp();
			literal.setStringSymbol(rawKey.substring(1, rawKey.length() - 1).replace("''", "'"));
			return literal;
		}
		if (rawKey.matches("-?\\d+")) {
			IntegerLiteralExp literal = OclFactory.eINSTANCE.createIntegerLiteralExp();
			literal.setIntegerSymbol(Long.parseLong(rawKey));
			return literal;
		}
		if (rawKey.matches("-?\\d+\\.\\d+")) {
			RealLiteralExp literal = OclFactory.eINSTANCE.createRealLiteralExp();
			literal.setRealSymbol(Double.parseDouble(rawKey));
			return literal;
		}
		throw new ODataQueryParseException("invalid key literal");
	}

	private void apply(String setName, Target target, HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		for (String incompatible : List.of("$select", "$expand")) { // rows are not entities
			if (option(request, incompatible) != null) {
				throw new ODataQueryParseException(
						"combining $apply with " + incompatible + " is not supported");
			}
		}
		if (wantsXml(request)) {
			throw new ODataQueryParseException("$apply results are JSON-only");
		}
		ApplyPipeline pipeline = parseChecked(option(request, "$apply"),
				value -> parser.parseApply(value, target.entityType()));
		Map<String, String> aliases = parameterAliases(request);
		// $filter/$orderby run AFTER the pipeline (OASIS) — the pipeline aliases are in scope
		List<OrderBySegment> orderBy = parseChecked(option(request, "$orderby"),
				value -> parser.parseOrderByAfterApply(value, target.entityType(), pipeline, aliases));
		int skip = limits.effectiveSkip(option(request, "$skip"));
		int top = pageSize(request, response, limits.effectiveTop(option(request, "$top")));
		ApplyQuery query = new ApplyQuery(target.entityType(), pipeline,
				parseChecked(option(request, "$filter"),
						value -> parser.parseFilterAfterApply(value, target.entityType(), pipeline, aliases)),
				orderBy == null ? List.of() : orderBy,
				skip, top + 1, // peek: partial results MUST carry @odata.nextLink
				"true".equals(option(request, "$count")));

		ApplyResult result;
		try {
			result = target.queryService().executeApply(query);
		} catch (UnsupportedOperationException e) {
			error(response, 501, "the backend of '" + ODataJson.sanitize(setName) + "' does not support $apply");
			return;
		}
		boolean hasMore = result.rows().size() > top;
		List<Map<String, Object>> rows = hasMore ? result.rows().subList(0, top) : result.rows();
		StringBuilder json = new StringBuilder("{\"@odata.context\":\"")
				.append(contextRoot(request)).append("/$metadata#").append(setName).append('"');
		if (result.totalCount() >= 0) {
			json.append(",\"@odata.count\":").append(result.totalCount());
		}
		json.append(",\"value\":[");
		for (int i = 0; i < rows.size(); i++) {
			if (i > 0) {
				json.append(',');
			}
			ODataJson.value(json, rows.get(i));
		}
		json.append(']');
		if (hasMore) {
			json.append(",\"@odata.nextLink\":\"")
					.append(ODataJson.sanitize(nextLink(request, skip + top))).append('"');
		}
		json.append('}');
		response.setContentType("application/json;odata.metadata=minimal;charset=UTF-8");
		response.getWriter().write(json.toString());
	}

	// --- helpers ---

	/**
	 * Resolves a system query option: since OData 4.01 option names are case-INsensitive and
	 * the {@code $} prefix is optional ([OData-Protocol] 8.2.7 / conformance 13.1.2), so
	 * {@code ?FILTER=…} and {@code ?$filter=…} are the same option.
	 */
	private static String option(HttpServletRequest request, String canonical) {
		String direct = request.getParameter(canonical);
		if (direct != null) {
			return direct;
		}
		for (String name : request.getParameterMap().keySet()) {
			if (canonical.equals(normalizeOption(name))) {
				return request.getParameter(name);
			}
		}
		return null;
	}

	/** Canonical form of a query-option name: lower-case with a {@code $} prefix. */
	private static String normalizeOption(String name) {
		String normalized = name.toLowerCase(java.util.Locale.ROOT);
		return normalized.startsWith("$") ? normalized : "$" + normalized;
	}

	/**
	 * 4.01 parameter aliases (11.2.5.1.3): every {@code @name} query parameter, keyed as sent.
	 * Values are expression texts and get parsed on use — so they pass the SAME pre-parse
	 * limits as {@code $filter} itself (hostile-input guard).
	 */
	private Map<String, String> parameterAliases(HttpServletRequest request) {
		Map<String, String> aliases = new HashMap<>();
		for (Map.Entry<String, String[]> parameter : request.getParameterMap().entrySet()) {
			if (parameter.getKey().startsWith("@") && parameter.getValue().length > 0) {
				limits.checkExpression(parameter.getValue()[0]);
				aliases.put(parameter.getKey(), parameter.getValue()[0]);
			}
		}
		return aliases;
	}

	/**
	 * Applies {@code Prefer: odata.maxpagesize} (4.01 8.2.8.7, prefix optional per 13.2.1/4):
	 * caps the page below the {@code $top}/ceiling value and echoes {@code Preference-Applied}.
	 */
	private static int pageSize(HttpServletRequest request, HttpServletResponse response, int top) {
		String prefer = request.getHeader("Prefer");
		if (prefer == null) {
			return top;
		}
		for (String preference : prefer.split(",")) {
			String[] nameValue = preference.trim().split("=", 2);
			String name = nameValue[0].trim().toLowerCase(java.util.Locale.ROOT);
			if (!"odata.maxpagesize".equals(name) && !"maxpagesize".equals(name)) {
				continue;
			}
			try {
				int maxPageSize = Integer.parseInt(nameValue.length > 1 ? nameValue[1].trim() : "");
				if (maxPageSize > 0 && maxPageSize < top) {
					response.setHeader("Preference-Applied", "odata.maxpagesize=" + maxPageSize);
					return maxPageSize;
				}
			} catch (NumberFormatException e) {
				// preferences are hints — a malformed value is ignored, not an error
			}
		}
		return top;
	}

	private <T> T parseChecked(String expression, java.util.function.Function<String, T> parse) {
		if (expression == null || expression.isBlank()) {
			return null;
		}
		limits.checkExpression(expression);
		return parse.apply(expression);
	}

	private EClass resolveEntityType(String setName) {
		for (EPackage pkg : packages) {
			if (pkg.getEClassifier(setName) instanceof EClass eClass && !eClass.isAbstract()) {
				return eClass;
			}
		}
		return null;
	}

	private List<String> entitySetNames() {
		return packages.stream()
				.flatMap(pkg -> pkg.getEClassifiers().stream())
				.filter(EClass.class::isInstance).map(EClass.class::cast)
				.filter(c -> !c.isAbstract())
				.map(EClass::getName).sorted().toList();
	}

	// --- $select / $expand / formats ---

	/** Validated {@code $select} tree (nested selects incl., 4.01), or null when absent. */
	private SelectTree selectOption(HttpServletRequest request, EClass entityType) {
		String select = option(request, "$select");
		if (select == null || select.isBlank()) {
			return null;
		}
		limits.checkExpression(select); // nested trees are parsed — same hostile-input guard
		return SelectTree.parse(select, entityType);
	}

	/** Validated {@code $expand} navigation names (always a set, possibly empty). */
	private Set<String> expandOption(HttpServletRequest request, EClass entityType) {
		String expand = option(request, "$expand");
		if (expand == null || expand.isBlank()) {
			return Set.of();
		}
		Set<String> names = new LinkedHashSet<>();
		for (String name : expand.split(",")) {
			String trimmed = name.trim();
			if (!(entityType.getEStructuralFeature(trimmed) instanceof EReference)) {
				throw new ODataQueryParseException("unknown $expand navigation '" + trimmed + "'");
			}
			names.add(trimmed);
		}
		return names;
	}

	private String entityJson(EObject entity, EClass entityType, SelectTree select, Set<String> expand)
			throws IOException {
		EObject copy = shaper.shape(entity, entityType, select, expand, null);
		ODataJsonResourceImpl resource = ODataJsonResourceImpl.minimalMetadata(
				URI.createURI("response.odatajson"), metadataService, expand);
		resource.getContents().add(copy);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, null);
		String json = out.toString(StandardCharsets.UTF_8);
		if (entity.eClass() != entityType) {
			// derived instance: the type is NOT computable from the context URL, so minimal
			// metadata must transport the single-field discriminator ([OData-JSON] 4.5.8)
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
	private void writeXmi(HttpServletResponse response, List<EObject> roots) throws IOException {
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

	private boolean wantsXml(HttpServletRequest request) {
		String format = option(request, "$format");
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

	private static String contextRoot(HttpServletRequest request) {
		return request.getRequestURI().replaceFirst("/[^/]*$", "");
	}



	private void error(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter().write(ODataJson.error(status, message));
	}
}
