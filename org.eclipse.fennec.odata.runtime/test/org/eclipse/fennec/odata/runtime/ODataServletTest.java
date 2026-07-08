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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.codec.util.MetadataServiceFactory;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.model.metadata.api.MetadataWhiteboard;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.odata.operation.api.ODataOperationHandler;
import org.eclipse.fennec.odata.query.OclEvaluator;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet-level tests with a strong destructive focus (mocked HTTP plumbing): the error
 * contract (400/404/405/500 as OData error JSON, sanitized — no stack traces, no exception
 * class names) and the resource-exhaustion limits (expression length, parenthesis nesting,
 * enforced $top ceiling) — injection-style input must die at the parser, never reach a backend.
 */
@DisplayName("ODataServlet error contract + limits")
class ODataServletTest {

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private EClass productClass;

	private ODataServlet servlet;
	private final AtomicReference<EntityQuery> lastQuery = new AtomicReference<>();
	private final AtomicReference<RuntimeException> backendFailure = new AtomicReference<>();
	private List<EObject> backendResult = List.of();
	private List<Map<String, Object>> applyResult = List.of();
	private boolean applySupported = true;
	private final AtomicReference<org.eclipse.fennec.odata.persistence.api.ApplyQuery> lastApplyQuery =
			new AtomicReference<>();
	private final AtomicReference<EObject> lastWritePayload = new AtomicReference<>();
	private final AtomicReference<String> lastWriteKey = new AtomicReference<>();
	private final AtomicReference<Boolean> lastReplace = new AtomicReference<>();
	private boolean writeSupported = true;
	private boolean writeConflict = false;
	private boolean upsertCreates = false;
	private boolean deleteFound = true;
	private boolean unlinkFound = true;
	private final AtomicReference<String> lastLink = new AtomicReference<>();
	private final List<String> linkCalls = new java.util.concurrent.CopyOnWriteArrayList<>();
	private final AtomicReference<String> lastUnlink = new AtomicReference<>();
	private final AtomicReference<EObject> lastRelated = new AtomicReference<>();

	@BeforeEach
	void setUp() throws Exception {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(findResource("testdata/webshop.ecore",
				"org.eclipse.fennec.odata.runtime/testdata/webshop.ecore"));
		productClass = EcoreHelper.getEClass(pkg, "Product");

		servlet = new ODataServlet();
		servlet.activate(Map.of("odata.max.top", "50",
				"odata.max.expression.length", "128", "odata.max.nesting.depth", "8",
				"odata.max.body.size", "256"));
		servlet.addEPackage(pkg);
		MetadataWhiteboard metadataService = MetadataServiceFactory.create();
		metadataService.registerPackage(pkg);
		servlet.setMetadataService(metadataService);
		servlet.addQueryService(new QueryService() {
			@Override
			public boolean supports(EClass entityType) {
				return entityType == productClass;
			}

			@Override
			public QueryResult execute(EntityQuery query) {
				lastQuery.set(query);
				if (backendFailure.get() != null) {
					throw backendFailure.get();
				}
				List<EObject> entities = query.castType() == null ? backendResult
						: backendResult.stream().filter(query.castType()::isInstance).toList();
				return new QueryResult(entities, query.count() ? entities.size() : -1);
			}

			@Override
			public org.eclipse.fennec.odata.persistence.api.ApplyResult executeApply(
					org.eclipse.fennec.odata.persistence.api.ApplyQuery query) {
				lastApplyQuery.set(query);
				if (!applySupported) {
					throw new UnsupportedOperationException();
				}
				return new org.eclipse.fennec.odata.persistence.api.ApplyResult(applyResult,
						query.count() ? applyResult.size() : -1);
			}
		});
		servlet.addWriteService(new WriteService() {
			@Override
			public boolean supports(EClass entityType) {
				return writeSupported && entityType == productClass;
			}

			@Override
			public EObject create(EClass entityType, EObject entity) {
				if (writeConflict) {
					throw new WriteConflictException("an entity with this key already exists");
				}
				lastWritePayload.set(entity);
				return entity;
			}

			@Override
			public WriteResult update(EClass entityType, String rawKey, EObject payload,
					boolean replace) {
				lastWritePayload.set(payload);
				lastWriteKey.set(rawKey);
				lastReplace.set(replace);
				return new WriteResult(payload, upsertCreates);
			}

			@Override
			public boolean delete(EClass entityType, String rawKey) {
				lastWriteKey.set(rawKey);
				return deleteFound;
			}

			@Override
			public EObject createRelated(EClass entityType, String rawKey, String navigation,
					EObject child) {
				lastRelated.set(child);
				lastWriteKey.set(rawKey);
				return child;
			}

			@Override
			public void link(EClass entityType, String rawKey, String navigation,
					String targetRawKey) {
				lastLink.set(navigation + ":" + targetRawKey);
				linkCalls.add(rawKey + "→" + navigation + ":" + targetRawKey);
			}

			@Override
			public boolean unlink(EClass entityType, String rawKey, String navigation,
					String targetRawKey) {
				lastUnlink.set(navigation + ":" + targetRawKey);
				return unlinkFound;
			}
		});
	}

	@AfterEach
	void tearDown() {
		ecoreHelper.releaseAll();
	}

	private record Response(int status, String body, Map<String, String> headers) {
	}

	private Response get(String path, Map<String, String> parameters) throws Exception {
		return call("GET", path, parameters, null);
	}

	private Response call(String method, String path, Map<String, String> parameters) throws Exception {
		return call(method, path, parameters, null);
	}

	private Response call(String method, String path, Map<String, String> parameters, String accept)
			throws Exception {
		return call(method, path, parameters, accept, null);
	}

	private Response call(String method, String path, Map<String, String> parameters, String accept,
			String maxVersion) throws Exception {
		return call(method, path, parameters, accept, maxVersion, Map.of());
	}

	/** A write call carrying a JSON body (or another content type for the 415 case). */
	private Response callWrite(String method, String path, String body, String contentType)
			throws Exception {
		return call(method, path, Map.of(), null, null, Map.of(), body, contentType);
	}

	/** {@link #callWrite} with extra request headers (If-Match) and query parameters ($id). */
	private Response callWrite(String method, String path, Map<String, String> parameters,
			String body, String contentType, Map<String, String> headers) throws Exception {
		return call(method, path, parameters, null, null, headers, body, contentType);
	}

	private Response call(String method, String path, Map<String, String> parameters, String accept,
			String maxVersion, Map<String, String> requestHeaders) throws Exception {
		return call(method, path, parameters, accept, maxVersion, requestHeaders, null, null);
	}

	private Response call(String method, String path, Map<String, String> parameters, String accept,
			String maxVersion, Map<String, String> requestHeaders, String payload, String contentType)
			throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn(method);
		when(request.getPathInfo()).thenReturn(path);
		when(request.getContentType()).thenReturn(contentType);
		if (payload != null) {
			when(request.getInputStream()).thenReturn(
					servletInputStream(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
		}
		when(request.getHeader("Accept")).thenReturn(accept);
		when(request.getHeader("OData-MaxVersion")).thenReturn(maxVersion);
		requestHeaders.forEach((name, value) -> when(request.getHeader(name)).thenReturn(value));
		when(request.getRequestURI()).thenReturn("/odata" + path);
		when(request.getParameterNames())
				.thenAnswer(i -> java.util.Collections.enumeration(parameters.keySet()));
		java.util.Map<String, String[]> parameterMap = new HashMap<>();
		parameters.forEach((key, value) -> parameterMap.put(key, new String[] { value }));
		when(request.getParameterMap()).thenReturn(parameterMap);
		parameters.forEach((key, value) -> when(request.getParameter(key)).thenReturn(value));

		HttpServletResponse response = mock(HttpServletResponse.class);
		StringWriter body = new StringWriter();
		when(response.getWriter()).thenReturn(new PrintWriter(body, true));
		AtomicInteger status = new AtomicInteger(200);
		doAnswer(invocation -> {
			status.set(invocation.getArgument(0));
			return null;
		}).when(response).setStatus(anyInt());
		Map<String, String> headers = new HashMap<>();
		doAnswer(invocation -> {
			headers.put(invocation.getArgument(0), invocation.getArgument(1));
			return null;
		}).when(response).setHeader(org.mockito.ArgumentMatchers.anyString(),
				org.mockito.ArgumentMatchers.anyString());

		servlet.service(request, response);
		return new Response(status.get(), body.toString(), headers);
	}

	private static jakarta.servlet.ServletInputStream servletInputStream(byte[] data) {
		java.io.ByteArrayInputStream bytes = new java.io.ByteArrayInputStream(data);
		return new jakarta.servlet.ServletInputStream() {
			@Override
			public int read() {
				return bytes.read();
			}

			@Override
			public boolean isFinished() {
				return bytes.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(jakarta.servlet.ReadListener listener) {
			}
		};
	}

	@Test
	@DisplayName("happy path: filter reaches the backend as typed IR, response is OData JSON")
	void happyPath() throws Exception {
		EObject milk = pkg.getEFactoryInstance().create(productClass);
		milk.eSet(productClass.getEStructuralFeature("id"), "p1");
		milk.eSet(productClass.getEStructuralFeature("name"), "Milk");
		milk.eSet(productClass.getEStructuralFeature("price"), new BigDecimal("1.20"));
		backendResult = List.of(milk);

		Response response = get("/Product", Map.of("$filter", "price lt 3.00", "$count", "true"));

		assertEquals(200, response.status());
		assertEquals("4.01", response.headers().get("OData-Version"),
				"4.01 without a client OData-MaxVersion pin (8.1.5)");
		assertTrue(response.body().contains("\"@odata.context\""), response.body());
		assertTrue(response.body().contains("\"@odata.count\":1"), response.body());
		assertTrue(response.body().contains("\"Milk\""), response.body());
		assertFalse(response.body().contains("@odata.type"),
				"odata.metadata=minimal omits computable control information ([OData-JSON] 4.5.8)");
		assertFalse(response.body().contains("@odata.id"),
				"the id is computable from context URL + key property");
		assertTrue(response.body().contains("\"id\":\"p1\""), "the key PROPERTY itself stays");
		assertEquals("<", ((OperationCallExp) lastQuery.get().filter()).getName(),
				"typed OCL IR reached the backend");
	}

	@Test
	@DisplayName("service document and $metadata answer")
	void documents() throws Exception {
		Response serviceDoc = get("/", Map.of());
		assertEquals(200, serviceDoc.status());
		assertTrue(serviceDoc.body().contains("\"Product\""));

		Response metadata = get("/$metadata", Map.of());
		assertEquals(200, metadata.status());
		assertTrue(metadata.body().contains("edmx:Edmx"), "CSDL document");
		assertTrue(metadata.body().contains("EntityType") && metadata.body().contains("Product"));
		assertTrue(metadata.body().contains("Org.OData.Core.V1.ODataVersions")
				&& metadata.body().contains("4.0 4.01"),
				"the served protocol versions are announced on the container (13.1.2)");
		assertTrue(metadata.body().indexOf("edmx:Reference") < metadata.body().indexOf("DataServices"),
				"the Core vocabulary reference precedes DataServices");
		assertTrue(metadata.body().contains("Org.OData.Capabilities.V1.ConformanceLevel")
				&& metadata.body().contains("ConformanceLevelType/Minimal"),
				"the conformance level is advertised (12 / 13.2.1 SHOULD)");
		assertTrue(metadata.body().contains("Org.OData.Capabilities.V1.BatchSupported")
				&& metadata.body().contains("Org.OData.Capabilities.V1.AsynchronousRequestsSupported"),
				"unsupported capabilities are announced as false");
		assertTrue(metadata.body().contains("Org.OData.Capabilities.V1.xml"),
				"the Capabilities vocabulary reference resolves the terms");
	}

	@Test
	@DisplayName("destructive: injection-style and malformed $filter → 400, backend untouched")
	void badFilters() throws Exception {
		lastQuery.set(null);
		for (String attack : List.of(
				"name eq 'a' or 1=1 --",           // SQL-injection style
				"name eq 'a'; DROP TABLE users",   // statement smuggling
				"nosuchproperty eq 1",             // unknown property probing
				"frobnicate(name) eq 1",           // unknown function
				"name eq 'unterminated")) {        // broken literal
			Response response = get("/Product", Map.of("$filter", attack));
			assertEquals(400, response.status(), attack);
			assertTrue(response.body().startsWith("{\"error\""), attack);
		}
		assertEquals(null, lastQuery.get(), "no attack input ever reached the backend");
	}

	@Test
	@DisplayName("destructive: resource exhaustion — length, nesting, $top ceiling")
	void exhaustionLimits() throws Exception {
		assertEquals(400, get("/Product",
				Map.of("$filter", "name eq '" + "x".repeat(500) + "'")).status(), "length limit (128)");

		String parenBomb = "(".repeat(50) + "name eq 'x'" + ")".repeat(50);
		assertEquals(400, get("/Product", Map.of("$filter", parenBomb)).status(), "nesting limit (8)");

		get("/Product", Map.of("$top", "999999"));
		assertEquals(51, lastQuery.get().top(), "$top capped at ceiling (+1 = nextLink peek)");
		get("/Product", Map.of());
		assertEquals(51, lastQuery.get().top(), "ceiling applies even without $top (+1 peek)");

		assertEquals(400, get("/Product", Map.of("$top", "-5")).status(), "negative paging");
		assertEquals(400, get("/Product", Map.of("$skip", "abc")).status(), "non-numeric paging");
	}

	@Test
	@DisplayName("destructive: 404/405 and sanitized 500 without internals")
	void errorContract() throws Exception {
		Response unknown = get("/NoSuchSet", Map.of());
		assertEquals(404, unknown.status());
		assertEquals("4.01", unknown.headers().get("OData-Version"), "headers also on errors");

		assertEquals(405, call("TRACE", "/Product", Map.of()).status(),
				"non-OData methods stay rejected (writes are routed since the write path)");

		backendFailure.set(new IllegalStateException("secret connection string: jdbc://internal"));
		Response failure = get("/Product", Map.of());
		assertEquals(500, failure.status());
		assertFalse(failure.body().contains("secret"), "no internals leak: " + failure.body());
		assertFalse(failure.body().contains("IllegalStateException"), "no exception class names");
		assertFalse(failure.body().contains("at org.eclipse"), "no stack traces");
		backendFailure.set(null);
	}

	private EObject product(String id, String name, String price, EObject category) {
		EObject product = pkg.getEFactoryInstance().create(productClass);
		product.eSet(productClass.getEStructuralFeature("id"), id);
		product.eSet(productClass.getEStructuralFeature("name"), name);
		if (price != null) {
			product.eSet(productClass.getEStructuralFeature("price"), new BigDecimal(price));
		}
		if (category != null) {
			product.eSet(productClass.getEStructuralFeature("category"), category);
		}
		return product;
	}

	@Test
	@DisplayName("single entity: key becomes a typed AST — quotes cannot smuggle expressions")
	void singleEntity() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));
		Response found = get("/Product('p1')", Map.of());
		assertEquals(200, found.status());
		assertTrue(found.body().contains("$entity"), found.body());
		assertTrue(found.body().contains("\"Milk\""), found.body());
		assertEquals("=", ((OperationCallExp) lastQuery.get().filter()).getName());

		backendResult = List.of();
		assertEquals(404, get("/Product('missing')", Map.of()).status());

		// key injection: the escaped quote stays literal data, never an expression
		get("/Product('a'' or 1 eq 1''')", Map.of());
		OperationCallExp injected = (OperationCallExp) lastQuery.get().filter();
		assertEquals("a' or 1 eq 1'",
				((StringLiteralExp) injected.getOwnedArguments().get(0)).getStringSymbol(),
				"the whole attack string is ONE literal value");

		assertEquals(404, get("/Product('a'' or 1 eq 1)", Map.of()).status(),
				"unterminated quote = unparseable path = no such resource (never repaired)");
		assertEquals(404, get("/Product(abc def)", Map.of()).status(), "malformed key = 404");
	}

	@Test
	@DisplayName("$select shapes the payload (key survives), $expand inlines navigations")
	void selectAndExpand() throws Exception {
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");
		EObject dairy = pkg.getEFactoryInstance().create(categoryClass);
		dairy.eSet(categoryClass.getEStructuralFeature("id"), "c1");
		dairy.eSet(categoryClass.getEStructuralFeature("name"), "Dairy");
		backendResult = List.of(product("p1", "Milk", "1.20", dairy));

		Response selected = get("/Product", Map.of("$select", "name"));
		assertTrue(selected.body().contains("\"Milk\""), selected.body());
		assertFalse(selected.body().contains("1.20"), "price not selected: " + selected.body());
		assertTrue(selected.body().contains("p1"), "key always survives $select");

		Response plain = get("/Product", Map.of());
		assertFalse(plain.body().contains("Dairy"), "navigation omitted by default: " + plain.body());
		Response expanded = get("/Product", Map.of("$expand", "category"));
		assertTrue(expanded.body().contains("\"Dairy\""), "expanded inline: " + expanded.body());

		assertEquals(400, get("/Product", Map.of("$select", "nosuch")).status());
		assertEquals(400, get("/Product", Map.of("$expand", "nosuch")).status());
		assertEquals(400, get("/Product", Map.of("$expand", "name")).status(), "attribute is not expandable");
	}

	@Test
	@DisplayName("$expand with nested $filter trims the expanded collection (copies only)")
	void filterInExpand() throws Exception {
		EClass reviewClass = EcoreHelper.getEClass(pkg, "Review");
		EObject great = pkg.getEFactoryInstance().create(reviewClass);
		great.eSet(reviewClass.getEStructuralFeature("stars"), 5);
		great.eSet(reviewClass.getEStructuralFeature("comment"), "great");
		EObject poor = pkg.getEFactoryInstance().create(reviewClass);
		poor.eSet(reviewClass.getEStructuralFeature("stars"), 2);
		poor.eSet(reviewClass.getEStructuralFeature("comment"), "poor");
		EObject milk = product("p1", "Milk", "1.20", null);
		EStructuralFeature reviewsFeature = productClass.getEStructuralFeature("reviews");
		// many-valued reference: eGet returns the live EList (empty here), never null
		@SuppressWarnings("unchecked")
		List<EObject> reviews = (List<EObject>) milk.eGet(reviewsFeature);
		reviews.addAll(List.of(great, poor));
		backendResult = List.of(milk);

		Response filtered = get("/Product", Map.of("$expand", "reviews($filter=stars ge 4)"));
		assertEquals(200, filtered.status(), filtered.body());
		assertTrue(filtered.body().contains("\"great\""), filtered.body());
		assertFalse(filtered.body().contains("\"poor\""),
				"filtered out of the expanded collection: " + filtered.body());
		assertEquals(Set.of("reviews"), lastQuery.get().expand(),
				"the backend still prefetches the plain navigation");
		assertEquals(2, ((List<?>) milk.eGet(reviewsFeature)).size(),
				"the nested filter never mutates backend objects");

		Response single = get("/Product('p1')", Map.of("$expand", "reviews($filter=stars ge 4)"));
		assertTrue(single.body().contains("\"great\"") && !single.body().contains("\"poor\""),
				"single-entity reads filter the same way: " + single.body());

		assertEquals(501, get("/Product", Map.of("$expand", "reviews($top=1)")).status(),
				"other nested options are honestly unimplemented");
		assertEquals(400, get("/Product", Map.of("$expand", "category($filter=name eq 'x')")).status(),
				"nested $filter needs a collection-valued navigation");
		assertEquals(400, get("/Product", Map.of("$expand", "reviews($filter=nosuch eq 1)")).status(),
				"the nested expression parses against the TARGET type");
	}

	@Test
	@DisplayName("$apply: rows as JSON; incompatible combinations and XML rejected; 501 without support")
	void applyEndpoint() throws Exception {
		applyResult = List.of(Map.of("Total", new BigDecimal("5.70")));
		Response rows = get("/Product", Map.of("$apply",
				"groupby((category/name),aggregate(price with sum as Total))"));
		assertEquals(200, rows.status());
		assertTrue(rows.body().contains("\"Total\":5.70"), rows.body());

		// $filter AFTER the pipeline: aggregate aliases are in scope, reaches the backend typed
		Response combined = get("/Product", Map.of(
				"$apply", "groupby((category/name),aggregate(price with sum as Total))",
				"$filter", "Total gt 3.00", "$top", "5", "$count", "true"));
		assertEquals(200, combined.status(), combined.body());
		org.eclipse.fennec.odata.persistence.api.ApplyQuery applied = lastApplyQuery.get();
		assertTrue(applied.rowFilter() != null, "row filter forwarded");
		assertEquals(6, applied.top(), "requested 5 + nextLink peek");
		assertTrue(applied.count());
		assertEquals(400, get("/Product", Map.of("$apply", "filter(price gt 1)", "$select", "name"))
				.status(), "$select on rows is rejected");
		assertEquals(400, get("/Product", Map.of("$apply", "filter(price gt 1)", "$format", "xml"))
				.status(), "$apply is JSON-only");
		assertEquals(400, get("/Product", Map.of("$apply", "frobnicate(price gt 1)")).status());

		applySupported = false;
		assertEquals(501, get("/Product", Map.of("$apply", "filter(price gt 1)")).status());
		applySupported = true;
	}

	@Test
	@DisplayName("content negotiation: $format=xml / Accept → XMI, default JSON")
	void formats() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));

		Response xml = get("/Product", Map.of("$format", "xml"));
		assertEquals(200, xml.status());
		assertTrue(xml.body().startsWith("<?xml"), xml.body());
		assertTrue(xml.body().contains("Milk"), xml.body());

		Response viaAccept = call("GET", "/Product", Map.of(), "application/xml");
		assertTrue(viaAccept.body().startsWith("<?xml"), "Accept: application/xml → XMI");

		// XMI is sparse: unset (= deselected) features are simply absent from the document
		Response selectedXml = get("/Product", Map.of("$format", "xml", "$select", "name"));
		assertTrue(selectedXml.body().contains("Milk"), selectedXml.body());
		assertFalse(selectedXml.body().contains("price"), "deselected attribute absent: " + selectedXml.body());
		assertTrue(selectedXml.body().contains("p1"), "key survives $select in XMI too");

		Response json = call("GET", "/Product", Map.of(), "application/json, application/xml");
		assertTrue(json.body().startsWith("{"), "JSON wins when acceptable");

		assertEquals(400, get("/Product", Map.of("$format", "yaml")).status(), "unsupported format");
	}

	@Test
	@DisplayName("conformance: partial results carry @odata.nextLink (server-driven paging)")
	void nextLink() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null),
				product("p2", "Cheese", "4.50", null), product("p3", "Bread", "2.80", null));

		Response partial = get("/Product", Map.of("$top", "2", "$filter", "price gt 0.5"));
		assertEquals(200, partial.status());
		assertTrue(partial.body().contains("\"@odata.nextLink\""), partial.body());
		assertTrue(partial.body().contains("$skip=2"), "skip advanced past the page: " + partial.body());
		assertTrue(partial.body().contains("%24filter=") || partial.body().contains("$filter="),
				"other options preserved: " + partial.body());
		assertFalse(partial.body().contains("\"Bread\""), "page trimmed to $top");

		Response complete = get("/Product", Map.of());
		assertFalse(complete.body().contains("@odata.nextLink"),
				"no link when everything fits: " + complete.body());
	}

	@Test
	@DisplayName("conformance: OData-MaxVersion semantics and 501 for unsupported options")
	void versionAndOptionContract() throws Exception {
		assertEquals(400, call("GET", "/Product", Map.of(), null, "3.0").status(),
				"MaxVersion below 4.0 fails the request");
		// 8.1.5 negotiation: the RESPONSE OData-Version follows the pinned MaxVersion, not just 200
		Response pinned40 = call("GET", "/Product", Map.of(), null, "4.0");
		assertEquals(200, pinned40.status());
		assertEquals("4.0", pinned40.headers().get("OData-Version"),
				"OData-MaxVersion: 4.0 pins the response to 4.0");
		Response v401 = call("GET", "/Product", Map.of(), null, "4.01");
		assertEquals(200, v401.status());
		assertEquals("4.01", v401.headers().get("OData-Version"), "otherwise the response is 4.01");

		assertEquals(200, get("/Product", Map.of("$search", "milk")).status(),
				"$search is implemented → 200 (13.1.2 SHOULD)");
		assertEquals(501, get("/Product", Map.of("$schemaversion", "1")).status(),
				"a still-unimplemented known option → 501");
		assertEquals(400, get("/Product", Map.of("$frobnicate", "x")).status(),
				"unknown $-option → 400");
	}

	@Test
	@DisplayName("unbound function import: resolved+dispatched to a handler; 501 without one; 404 unknown")
	void unboundFunctionImport() throws Exception {
		// resolved (the model declares it) but no handler yet → 501
		assertEquals(501, get("/doubleOf(n=21)", Map.of()).status());

		servlet.addOperationHandler(new ODataOperationHandler() {
			@Override
			public boolean handles(String qualifiedOperationName) {
				return qualifiedOperationName.endsWith(".doubleOf");
			}

			@Override
			public Object invoke(org.eclipse.emf.ecore.EOperation operation, EObject boundInstance,
					Map<String, Object> parameters) {
				return ((Number) parameters.get("n")).intValue() * 2;
			}
		});

		Response ok = get("/doubleOf(n=21)", Map.of());
		assertEquals(200, ok.status(), ok.body());
		assertTrue(ok.body().contains("\"value\":42"), ok.body());

		assertEquals(404, get("/nosuchfunc(n=1)", Map.of()).status(), "unknown function import → 404");
	}

	@Test
	@DisplayName("function returning an entity serializes as a single entity with $entity context")
	void functionReturningEntity() throws Exception {
		servlet.addOperationHandler(new ODataOperationHandler() {
			@Override
			public boolean handles(String qualifiedOperationName) {
				return qualifiedOperationName.endsWith(".featured");
			}

			@Override
			public Object invoke(org.eclipse.emf.ecore.EOperation operation, EObject boundInstance,
					Map<String, Object> parameters) {
				return product("p9", "Featured", "9.99", null);
			}
		});
		Response result = get("/featured()", Map.of());
		assertEquals(200, result.status(), result.body());
		assertTrue(result.body().contains("\"Featured\""), result.body());
		assertTrue(result.body().contains("$entity"), "an entity result carries the $entity context");
	}

	@Test
	@DisplayName("bound function Set(key)/Ns.Func(p=…) is invoked on the addressed entity")
	void boundFunctionOnEntity() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));
		servlet.addOperationHandler(new ODataOperationHandler() {
			@Override
			public boolean handles(String qualifiedOperationName) {
				return qualifiedOperationName.endsWith(".label");
			}

			@Override
			public Object invoke(org.eclipse.emf.ecore.EOperation operation, EObject boundInstance,
					Map<String, Object> parameters) {
				Object name = boundInstance.eGet(boundInstance.eClass().getEStructuralFeature("name"));
				return parameters.get("prefix") + ":" + name;
			}
		});
		Response result = get("/Product('p1')/webshop.label(prefix='X')", Map.of());
		assertEquals(200, result.status(), result.body());
		assertTrue(result.body().contains("\"value\":\"X:Milk\""), result.body());
	}

	@Test
	@DisplayName("query options on a navigation path: $filter/$top/$count on the collection; $orderby → 501")
	void navigationPathQueryOptions() throws Exception {
		EClass reviewClass = EcoreHelper.getEClass(pkg, "Review");
		EObject great = pkg.getEFactoryInstance().create(reviewClass);
		great.eSet(reviewClass.getEStructuralFeature("stars"), 5);
		great.eSet(reviewClass.getEStructuralFeature("comment"), "great");
		EObject poor = pkg.getEFactoryInstance().create(reviewClass);
		poor.eSet(reviewClass.getEStructuralFeature("stars"), 2);
		poor.eSet(reviewClass.getEStructuralFeature("comment"), "poor");
		EObject milk = product("p1", "Milk", "1.20", null);
		((List<EObject>) milk.eGet(productClass.getEStructuralFeature("reviews")))
				.addAll(List.of(great, poor));
		backendResult = List.of(milk);

		Response filtered = get("/Product('p1')/reviews", Map.of("$filter", "stars ge 4"));
		assertEquals(200, filtered.status(), filtered.body());
		assertTrue(filtered.body().contains("great"), filtered.body());
		assertFalse(filtered.body().contains("poor"), "$filter trims the navigation collection");

		assertEquals("1", get("/Product('p1')/reviews/$count", Map.of("$filter", "stars ge 4"))
				.body().trim(), "$count is of the FILTERED collection");

		assertEquals(501, get("/Product('p1')/reviews", Map.of("$orderby", "stars")).status(),
				"$orderby on a navigation path is not implemented yet");
	}

	@Test
	@DisplayName("$search synthesizes a contains-predicate over string properties (no longer 501)")
	void searchOption() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));
		Response found = get("/Product", Map.of("$search", "Milk"));
		assertEquals(200, found.status(), found.body());

		// the synthesized predicate actually implements search semantics
		OclEvaluator evaluator = new OclEvaluator();
		assertTrue(evaluator.matchesNullSafe(lastQuery.get().filter(),
				product("p1", "Milk", "1.20", null)), "$search matches by a string property");
		assertFalse(evaluator.matchesNullSafe(lastQuery.get().filter(),
				product("p2", "Cheese", "2.00", null)), "a non-matching entity is excluded");

		// $search AND $filter combine
		Response combined = get("/Product", Map.of("$search", "Milk", "$filter", "price lt 3.00"));
		assertEquals(200, combined.status(), combined.body());
		assertEquals("and", ((OperationCallExp) lastQuery.get().filter()).getName(),
				"$filter and $search combine with a conjunction");
	}

	@Test
	@DisplayName("$compute adds a computed property to each entity (no longer 501)")
	void computeOption() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));
		Response result = get("/Product", Map.of("$compute", "price mul 2 as doublePrice"));
		assertEquals(200, result.status(), result.body());
		assertTrue(result.body().contains("\"doublePrice\":2.40"),
				"the computed property is spliced into the entity: " + result.body());
		assertTrue(result.body().contains("\"Milk\""), "the original properties remain");
	}

	@Test
	@DisplayName("unbound action import: POST with a JSON parameter body, void → 204")
	void unboundActionImport() throws Exception {
		AtomicReference<String> touched = new AtomicReference<>();
		servlet.addOperationHandler(new ODataOperationHandler() {
			@Override
			public boolean handles(String qualifiedOperationName) {
				return qualifiedOperationName.endsWith(".touch");
			}

			@Override
			public Object invoke(org.eclipse.emf.ecore.EOperation operation, EObject boundInstance,
					Map<String, Object> parameters) {
				touched.set(String.valueOf(parameters.get("id")));
				return null; // void action
			}
		});
		Response result = callWrite("POST", "/touch", "{\"id\":\"p1\"}", "application/json");
		assertEquals(204, result.status(), result.body());
		assertEquals("p1", touched.get(), "the body parameter reached the handler");
	}

	@Test
	@DisplayName("$orderby parses to typed IR that reaches the backend; unknown property → 400")
	void orderByReachesBackendOrFails() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));
		Response ok = get("/Product", Map.of("$orderby", "name desc"));
		assertEquals(200, ok.status(), ok.body());
		List<OrderBySegment> orderBy = lastQuery.get().orderBy();
		assertEquals(1, orderBy.size(), "the $orderby segment reaches the backend as IR");
		assertFalse(orderBy.get(0).ascending(), "the desc direction is carried through");
		// ABNF parse-or-fail (13.1.2): an unknown property is a 400 at the parser, never ignored
		assertEquals(400, get("/Product", Map.of("$orderby", "nosuchprop")).status());
	}

	@Test
	@DisplayName("deep insert: nested containment children in one POST body reach the write backend")
	void deepInsertContainmentChildren() throws Exception {
		Response created = callWrite("POST", "/Product",
				"{\"id\":\"n1\",\"name\":\"Milk\","
						+ "\"reviews\":[{\"id\":\"r1\",\"stars\":5},{\"id\":\"r2\",\"stars\":4}]}",
				"application/json");
		assertEquals(201, created.status(), created.body());
		EObject payload = lastWritePayload.get();
		assertNotNull(payload, "the created entity reached the write backend");
		List<?> reviews = (List<?>) payload.eGet(productClass.getEStructuralFeature("reviews"));
		assertEquals(2, reviews.size(),
				"both nested containment children are decoded and handed to the backend (13.1.1/32)");
	}

	@Test
	@DisplayName("4.01: option names are case-insensitive and the $ prefix is optional")
	void optionNames401() throws Exception {
		EObject milk = product("p1", "Milk", "1.20", null);
		backendResult = List.of(milk);

		Response prefixless = get("/Product", Map.of("filter", "price lt 3.00"));
		assertEquals(200, prefixless.status(), prefixless.body());
		assertEquals("<", ((OperationCallExp) lastQuery.get().filter()).getName(),
				"prefix-less 'filter' resolves to $filter");

		lastQuery.set(null);
		Response upperCase = get("/Product", Map.of("$FILTER", "price lt 3.00", "TOP", "5"));
		assertEquals(200, upperCase.status(), upperCase.body());
		assertEquals("<", ((OperationCallExp) lastQuery.get().filter()).getName());
		assertEquals(6, lastQuery.get().top(), "TOP resolves to $top (page + peek row)");

		assertEquals(200, get("/Product", Map.of("x-trace-id", "abc123")).status(),
				"custom query options (no $, no option name) are ignored (11.2.12)");
		assertEquals(501, get("/Product", Map.of("SCHEMAVERSION", "1")).status(),
				"the whitelist normalizes too: SCHEMAVERSION = $schemaversion → 501");
	}

	@Test
	@DisplayName("4.01: @parameter aliases feed $filter; unresolved aliases are client errors")
	void parameterAliases() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));

		Response aliased = get("/Product", Map.of("$filter", "name eq @wanted", "@wanted", "'Milk'"));
		assertEquals(200, aliased.status(), aliased.body());
		assertEquals("=", ((OperationCallExp) lastQuery.get().filter()).getName(),
				"the alias value was expanded into the expression");

		Response unresolved = get("/Product", Map.of("$filter", "name eq @missing"));
		assertEquals(400, unresolved.status());
		assertTrue(unresolved.body().contains("unresolved parameter alias"), unresolved.body());

		Response hostileValue = get("/Product",
				Map.of("$filter", "name eq @a", "@a", "((((((((((((((((((((1"));
		assertEquals(400, hostileValue.status(), "alias values pass the pre-parse limits");
	}

	@Test
	@DisplayName("derived types ([OData-URL] 4.11): set cast filters + re-types the options, "
			+ "keyed cast checks the instance, minimal metadata carries #Ns.Type")
	void derivedTypeCasts() throws Exception {
		EClass discounted = EcoreHelper.getEClass(pkg, "DiscountedProduct");
		EObject sale = pkg.getEFactoryInstance().create(discounted);
		sale.eSet(discounted.getEStructuralFeature("id"), "d1");
		sale.eSet(discounted.getEStructuralFeature("name"), "SaleMilk");
		sale.eSet(discounted.getEStructuralFeature("discount"), 20);
		EObject milk = product("p1", "Milk", "1.20", null);
		backendResult = List.of(milk, sale);

		// set-level cast: only derived instances; the DERIVED type is the option context
		Response cast = get("/Product/webshop.DiscountedProduct", Map.of("$filter", "discount gt 10"));
		assertEquals(200, cast.status(), cast.body());
		assertTrue(cast.body().contains("SaleMilk"), cast.body());
		assertFalse(cast.body().contains("\"Milk\""), "base instances filtered out: " + cast.body());
		assertTrue(cast.body().contains("$metadata#Product/webshop.DiscountedProduct"),
				"context URL names the cast: " + cast.body());
		assertEquals(discounted, lastQuery.get().castType(), "cast reaches the backend as IR");
		assertEquals(400, get("/Product", Map.of("$filter", "discount gt 10")).status(),
				"derived property is NOT addressable without the cast");

		// un-cast payload: the derived instance must carry the #Ns.Type discriminator
		Response plain = get("/Product", Map.of());
		assertTrue(plain.body().contains("\"@odata.type\":\"#webshop.DiscountedProduct\""),
				plain.body());
		assertFalse(plain.body().contains("\"@odata.type\":\"#webshop.Product\""),
				"non-derived instances stay discriminator-free (minimal metadata)");

		// keyed cast: instance of the type → entity (no discriminator, type is in the context)
		backendResult = List.of(sale);
		Response keyed = get("/Product/webshop.DiscountedProduct('d1')", Map.of());
		assertEquals(200, keyed.status(), keyed.body());
		assertTrue(keyed.body().contains("#Product/webshop.DiscountedProduct/$entity"), keyed.body());
		assertFalse(keyed.body().contains("@odata.type"), keyed.body());

		// keyed cast on a base instance → 404; unknown/unrelated type → 404
		backendResult = List.of(milk);
		assertEquals(404, get("/Product/webshop.DiscountedProduct('p1')", Map.of()).status());
		assertEquals(404, get("/Product/webshop.NoSuchType", Map.of()).status());
		assertEquals(404, get("/Product/webshop.Category", Map.of()).status(),
				"cast to a non-derived type identifies nothing");
	}

	@Test
	@DisplayName("4.01: nested $select prunes structured values recursively")
	void nestedSelect() throws Exception {
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");
		EObject dairy = pkg.getEFactoryInstance().create(categoryClass);
		dairy.eSet(categoryClass.getEStructuralFeature("id"), "c1");
		dairy.eSet(categoryClass.getEStructuralFeature("name"), "Dairy");
		backendResult = List.of(product("p1", "Milk", "1.20", dairy));

		Response nested = get("/Product", Map.of(
				"$expand", "category", "$select", "name,category($select=name)"));
		assertEquals(200, nested.status(), nested.body());
		assertTrue(nested.body().contains("\"Milk\"") && nested.body().contains("\"Dairy\""),
				nested.body());
		assertFalse(nested.body().contains("\"price\""), "unselected root property pruned");
		assertTrue(nested.body().contains("\"id\":\"c1\""), "nested key survives the sub-select");

		// nested select on a containment (reviews) without $expand
		Response containment = get("/Product", Map.of("$select", "reviews($select=stars)"));
		assertEquals(200, containment.status(), containment.body());

		// recognized-but-unsupported and malformed nested options fail the request
		assertEquals(400, get("/Product",
				Map.of("$select", "category($filter=name eq 'x')")).status(),
				"nested non-$select options are rejected, not ignored");
		assertEquals(400, get("/Product", Map.of("$select", "name($select=x)")).status(),
				"nested select on a primitive property");
		assertEquals(400, get("/Product", Map.of("$select", "category($select=nosuch)")).status(),
				"unknown nested property");
		assertEquals(400, get("/Product", Map.of("$select", "category($select=name")).status(),
				"unbalanced parentheses");
	}

	@Test
	@DisplayName("navigation walks send their prefix as prefetch path to the backend")
	void walkPrefetchReachesBackend() throws Exception {
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");
		EObject dairy = pkg.getEFactoryInstance().create(categoryClass);
		dairy.eSet(categoryClass.getEStructuralFeature("id"), "c1");
		dairy.eSet(categoryClass.getEStructuralFeature("name"), "Dairy");
		backendResult = List.of(product("p1", "Milk", "1.20", dairy));

		assertEquals(200, get("/Product('p1')/category/name", Map.of()).status());
		assertEquals(Set.of("category"), lastQuery.get().expand(),
				"the walked navigation prefix is a prefetch hint for the backend");

		assertEquals(200, get("/Product('p1')/name", Map.of()).status());
		assertEquals(Set.of(), lastQuery.get().expand(),
				"attribute-only walks need no prefetch");

		assertEquals(200, get("/Product('p1')", Map.of("$expand", "category")).status());
		assertEquals(Set.of("category"), lastQuery.get().expand(),
				"$expand on a single entity is a prefetch hint too");
	}

	@Test
	@DisplayName("write path: POST 201+Location, PATCH/PUT 204 (201 on upsert), DELETE 204/404")
	void writePath() throws Exception {
		Response created = callWrite("POST", "/Product",
				"{\"id\":\"n1\",\"name\":\"New\"}", "application/json");
		assertEquals(201, created.status(), created.body());
		assertTrue(created.headers().get("Location").endsWith("/Product('n1')"),
				"Location is the edit URL: " + created.headers());
		assertEquals(created.headers().get("Location"), created.headers().get("OData-EntityId"));
		assertTrue(created.body().contains("\"@odata.context\"")
				&& created.body().contains("\"id\":\"n1\""), created.body());
		assertEquals("New", lastWritePayload.get()
				.eGet(productClass.getEStructuralFeature("name")),
				"the decoded payload reached the backend");

		assertEquals(204, callWrite("PATCH", "/Product('p1')",
				"{\"name\":\"Renamed\"}", "application/json").status());
		assertEquals("'p1'", lastWriteKey.get());
		assertEquals(Boolean.FALSE, lastReplace.get(), "PATCH merges");

		assertEquals(204, callWrite("PUT", "/Product('p1')",
				"{\"name\":\"Replaced\"}", "application/json").status());
		assertEquals(Boolean.TRUE, lastReplace.get(), "PUT replaces");

		upsertCreates = true;
		Response upserted = callWrite("PATCH", "/Product('u1')",
				"{\"id\":\"u1\",\"name\":\"Upserted\"}", "application/json");
		upsertCreates = false;
		assertEquals(201, upserted.status(), "upsert creating → 201 with Location");
		assertTrue(upserted.headers().get("Location").endsWith("/Product('u1')"));

		assertEquals(204, callWrite("DELETE", "/Product('p1')", null, null).status());
		deleteFound = false;
		assertEquals(404, callWrite("DELETE", "/Product('gone')", null, null).status());
		deleteFound = true;
	}

	@Test
	@DisplayName("@odata.bind: payload bindings become reference operations after the write")
	void odataBindOnWrite() throws Exception {
		linkCalls.clear();
		Response created = callWrite("POST", "/Product",
				"{\"id\":\"n1\",\"name\":\"New\",\"category@odata.bind\":\"Category('c1')\"}",
				"application/json");
		assertEquals(201, created.status(), created.body());
		assertEquals(List.of("'n1'→category:'c1'"), linkCalls,
				"the created entity is linked to the bound target");
		assertEquals("New", lastWritePayload.get()
				.eGet(productClass.getEStructuralFeature("name")),
				"the bind member is stripped before decoding");

		linkCalls.clear();
		assertEquals(204, callWrite("PATCH", "/Product('p1')",
				"{\"name\":\"x\",\"reviews@odata.bind\":[\"Review('r1')\",\"Review('r2')\"]}",
				"application/json").status());
		assertEquals("'p1'", lastWriteKey.get(), "the update itself still ran");
		assertEquals(List.of("'p1'→reviews:'r1'", "'p1'→reviews:'r2'"), linkCalls,
				"collection binds link every target");

		assertEquals(400, callWrite("POST", "/Product",
				"{\"id\":\"n2\",\"nosuch@odata.bind\":\"Category('c1')\"}",
				"application/json").status(), "unknown navigation");
		assertEquals(400, callWrite("POST", "/Product",
				"{\"id\":\"n2\",\"name@odata.bind\":\"Category('c1')\"}",
				"application/json").status(), "attributes cannot be bound");
		assertEquals(400, callWrite("POST", "/Product",
				"{\"id\":\"n2\",\"category@odata.bind\":[\"Category('c1')\"]}",
				"application/json").status(), "array bind on a single-valued navigation");
		assertEquals(400, callWrite("POST", "/Product",
				"{\"id\":\"n2\",\"reviews@odata.bind\":\"Review('r1')\"}",
				"application/json").status(), "single bind on a collection navigation");
		assertEquals(400, callWrite("POST", "/Product",
				"{\"id\":\"n2\",\"category@odata.bind\":\"Review('r1')\"}",
				"application/json").status(), "target set must match the navigation's type");
		backendResult = List.of(product("p1", "Milk", "1.20", null));
		assertEquals(400, callWrite("POST", "/Product('p1')/reviews", Map.of(),
				"{\"stars\":5,\"category@odata.bind\":\"Category('c1')\"}", "application/json",
				Map.of()).status(),
				"related-create payloads validate bindings against the CHILD type (Review"
						+ " has no such navigation; valid child bindings answer 501)");
		backendResult = List.of();
	}

	@Test
	@DisplayName("optimistic concurrency: ETag on GET, If-Match required/checked on writes")
	void etagPreconditions() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));

		Response fetched = get("/Product('p1')", Map.of());
		String etag = fetched.headers().get("ETag");
		assertTrue(etag != null && etag.startsWith("W/\""), "single GET serves a weak ETag: " + etag);

		assertEquals(428, callWrite("PATCH", "/Product('p1')",
				"{\"name\":\"x\"}", "application/json").status(),
				"existing entity carries an ETag → If-Match required");
		assertEquals(412, callWrite("PATCH", "/Product('p1')", Map.of(),
				"{\"name\":\"x\"}", "application/json", Map.of("If-Match", "W/\"outdated\"")).status(),
				"stale ETag → precondition failed");
		assertEquals(204, callWrite("PATCH", "/Product('p1')", Map.of(),
				"{\"name\":\"x\"}", "application/json", Map.of("If-Match", etag)).status(),
				"the served ETag matches");
		assertEquals(204, callWrite("DELETE", "/Product('p1')", Map.of(),
				null, null, Map.of("If-Match", "*")).status(), "star matches anything");

		backendResult = List.of(); // absent → upsert path, no precondition
		assertEquals(204, callWrite("PATCH", "/Product('new')",
				"{\"name\":\"x\"}", "application/json").status());
	}

	@Test
	@DisplayName("a backend failure during the If-Match read is a 500, never a silent precondition bypass")
	void preconditionNotBypassedOnBackendError() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));
		// the read backend that serves currentEntity() is momentarily broken
		backendFailure.set(new IllegalStateException("secret: jdbc://internal"));

		Response patched = callWrite("PATCH", "/Product('p1')", Map.of(),
				"{\"name\":\"x\"}", "application/json", Map.of("If-Match", "*"));
		assertEquals(500, patched.status(),
				"a broken read backend must fail the write, not skip optimistic concurrency");
		assertNull(lastWritePayload.get(), "the write must NOT have reached the backend on a bypass");
		assertFalse(patched.body().contains("secret"), "no internals leak: " + patched.body());
		backendFailure.set(null);
	}

	@Test
	@DisplayName("$filter nested in $expand is subject to the same pre-parse nesting guard")
	void expandNestedFilterHonoursLimits() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));
		// well under the length limit but far past the depth limit (8 in the test config)
		String bomb = "reviews($filter=" + "(".repeat(20) + "stars eq 5" + ")".repeat(20) + ")";
		assertEquals(400, get("/Product", Map.of("$expand", bomb)).status(),
				"the parser-bomb guard must cover $expand's nested $filter, not only top-level $filter");
	}

	@Test
	@DisplayName("control characters in a created entity key cannot split response headers")
	void createdKeyControlCharsEncoded() throws Exception {
		// JSON-escaped CR/LF: valid JSON whose decoded key value carries the control characters
		Response created = callWrite("POST", "/Product",
				"{\"id\":\"a\\r\\nb\",\"name\":\"x\"}", "application/json");
		assertEquals(201, created.status(), created.body());
		String location = created.headers().get("Location");
		assertNotNull(location, "created entity carries a Location");
		assertFalse(location.contains("\r") || location.contains("\n"),
				"no raw CR/LF may reach the Location header (response splitting): " + location);
		assertTrue(location.contains("%0D") && location.contains("%0A"),
				"control characters are percent-encoded: " + location);
		assertEquals(location, created.headers().get("OData-EntityId"));
	}

	@Test
	@DisplayName("$ref operations: PUT/POST link, DELETE unlink (with $id for collections)")
	void referenceOperations() throws Exception {
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");
		EObject dairy = pkg.getEFactoryInstance().create(categoryClass);
		dairy.eSet(categoryClass.getEStructuralFeature("id"), "c1");
		backendResult = List.of(product("p1", "Milk", "1.20", dairy));

		assertEquals(204, callWrite("PUT", "/Product('p1')/category/$ref", Map.of(),
				"{\"@odata.id\":\"http://host/odata/Category('c9')\"}", "application/json",
				Map.of()).status());
		assertEquals("category:'c9'", lastLink.get(), "target key extracted from @odata.id");

		assertEquals(405, callWrite("POST", "/Product('p1')/category/$ref", Map.of(),
				"{\"@odata.id\":\"http://host/odata/Category('c9')\"}", "application/json",
				Map.of()).status(), "POST adds to collections, category is single-valued");

		assertEquals(204, callWrite("POST", "/Product('p1')/reviews/$ref", Map.of(),
				"{\"@odata.id\":\"http://host/odata/Review('r9')\"}", "application/json",
				Map.of()).status());
		assertEquals("reviews:'r9'", lastLink.get());

		assertEquals(400, callWrite("PUT", "/Product('p1')/category/$ref", Map.of(),
				"{\"@odata.id\":\"http://host/odata/Review('r1')\"}", "application/json",
				Map.of()).status(), "target set must match the navigation's type");

		assertEquals(204, callWrite("DELETE", "/Product('p1')/category/$ref", null, null).status());
		assertEquals("category:null", lastUnlink.get(), "single-valued clear");
		assertEquals(400, callWrite("DELETE", "/Product('p1')/reviews/$ref", null, null).status(),
				"collection removal requires $id");
		assertEquals(204, callWrite("DELETE", "/Product('p1')/reviews/$ref",
				Map.of("$id", "http://host/odata/Review('r2')"), null, null, Map.of()).status());
		assertEquals("reviews:'r2'", lastUnlink.get());

		assertEquals(204, callWrite("DELETE", "/Product('p1')/reviews('r3')/$ref",
				null, null).status(), "4.01: collection member removal by key in the URL");
		assertEquals("reviews:'r3'", lastUnlink.get());
		assertEquals(405, callWrite("PUT", "/Product('p1')/reviews('r3')/$ref", Map.of(),
				"{\"@odata.id\":\"x\"}", "application/json", Map.of()).status(),
				"keyed $ref segments only support DELETE");

		unlinkFound = false;
		assertEquals(404, callWrite("DELETE", "/Product('p1')/category/$ref", null, null).status());
		unlinkFound = true;

		backendResult = List.of();
		assertEquals(404, callWrite("DELETE", "/Product('gone')/category/$ref", null, null).status(),
				"reference writes on absent owners are 404");
	}

	@Test
	@DisplayName("related create and property-level writes")
	void relatedCreateAndPropertyWrites() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null));

		Response related = callWrite("POST", "/Product('p1')/reviews",
				"{\"id\":\"r9\",\"stars\":5}", "application/json");
		assertEquals(201, related.status(), related.body());
		assertEquals("Review", lastRelated.get().eClass().getName(),
				"the payload decodes against the NAVIGATION TARGET type");

		assertEquals(204, callWrite("PATCH", "/Product('p1')/price", Map.of(),
				"{\"value\": 2.5}", "application/json", Map.of("If-Match", "*")).status());
		assertEquals(0, new BigDecimal("2.5").compareTo((BigDecimal) lastWritePayload.get()
				.eGet(productClass.getEStructuralFeature("price"))),
				"the value document became a typed payload");
		assertEquals("Milk", lastWritePayload.get().eGet(productClass.getEStructuralFeature("name")),
				"the current state travels along (replace-based property write)");
		assertEquals(Boolean.TRUE, lastReplace.get(),
				"property writes replace — a merge cannot express null/default in EMF terms");

		assertEquals(204, callWrite("DELETE", "/Product('p1')/name", Map.of(),
				null, null, Map.of("If-Match", "*")).status());
		assertFalse(lastWritePayload.get().eIsSet(productClass.getEStructuralFeature("name")),
				"DELETE property: unset in a REPLACE payload → resets to default/null (11.4.9.2)");

		assertEquals(405, callWrite("PATCH", "/Product('p1')/id", Map.of(),
				"{\"value\":\"other\"}", "application/json", Map.of("If-Match", "*")).status(),
				"the key property is immutable");
		assertEquals(400, callWrite("PATCH", "/Product('p1')/price", Map.of(),
				"{\"notvalue\": 1}", "application/json", Map.of("If-Match", "*")).status(),
				"property updates need a value document");
	}

	@Test
	@DisplayName("write path guards: media type, body size, malformed payloads, wrong targets")
	void writeGuards() throws Exception {
		assertEquals(415, callWrite("POST", "/Product", "{}", "text/plain").status(),
				"writes must be application/json");
		assertEquals(400, callWrite("POST", "/Product", "", "application/json").status(),
				"empty payload");
		assertEquals(400, callWrite("POST", "/Product", "{invalid json",
				"application/json").status(), "malformed payload");
		assertEquals(413, callWrite("POST", "/Product",
				"{\"name\":\"" + "x".repeat(300) + "\"}", "application/json").status(),
				"payload above the configured body limit");

		assertEquals(405, callWrite("POST", "/Product('p1')",
				"{}", "application/json").status(), "POST addresses the set");
		assertEquals(405, callWrite("PATCH", "/Product",
				"{}", "application/json").status(), "PATCH addresses one entity");
		assertEquals(405, callWrite("POST", "/$metadata", "{}", "application/json").status());
		assertEquals(404, callWrite("POST", "/NoSuchSet", "{}", "application/json").status());
		assertEquals(404, callWrite("POST", "/Product('absent')/reviews",
				"{}", "application/json").status(), "related create needs an existing owner");
		backendResult = List.of(product("p1", "Milk", "1.20", null));
		assertEquals(501, callWrite("PATCH", "/Product('p1')/category/name",
				"{\"value\":\"x\"}", "application/json").status(),
				"writes deeper than one segment are not implemented");
		backendResult = List.of();

		writeConflict = true;
		Response conflict = callWrite("POST", "/Product",
				"{\"id\":\"p1\",\"name\":\"Clone\"}", "application/json");
		writeConflict = false;
		assertEquals(409, conflict.status(), "key conflicts are 409, not 500");

		writeSupported = false;
		assertEquals(405, callWrite("POST", "/Product",
				"{\"id\":\"x\"}", "application/json").status(), "no writable backend → 405");
		writeSupported = true;
	}

	@Test
	@DisplayName("pushdown refusal: UnsupportedOperationException from the backend → 501")
	void backendRefusal() throws Exception {
		backendFailure.set(new UnsupportedOperationException("no pushdown for this construct"));
		Response response = get("/Product", Map.of("$filter", "price lt 3.00"));
		backendFailure.set(null);
		assertEquals(501, response.status(),
				"backends refuse loudly instead of answering wrongly");
		assertFalse(response.body().contains("no pushdown"), "internal message stays inside");
	}

	@Test
	@DisplayName("4.01: Prefer maxpagesize (with and without odata. prefix) caps the page")
	void preferMaxPageSize() throws Exception {
		backendResult = List.of(product("p1", "Milk", "1.20", null),
				product("p2", "Cheese", "4.50", null), product("p3", "Bread", "2.80", null));

		Response paged = call("GET", "/Product", Map.of(), null, null,
				Map.of("Prefer", "odata.maxpagesize=2"));
		assertEquals(200, paged.status(), paged.body());
		assertFalse(paged.body().contains("\"Bread\""), "page capped at 2: " + paged.body());
		assertTrue(paged.body().contains("@odata.nextLink"), paged.body());
		assertEquals("odata.maxpagesize=2", paged.headers().get("Preference-Applied"),
				"the applied preference is echoed (8.2.8.7)");

		Response prefixless = call("GET", "/Product", Map.of(), null, null,
				Map.of("Prefer", "respond-async, maxpagesize=2"));
		assertEquals(200, prefixless.status());
		assertFalse(prefixless.body().contains("\"Bread\""),
				"prefix-less preference name works too (13.2.1/4)");

		Response unaffected = call("GET", "/Product", Map.of(), null, null,
				Map.of("Prefer", "odata.maxpagesize=broken"));
		assertEquals(200, unaffected.status(), "malformed preference values are hints — ignored");
		assertTrue(unaffected.body().contains("\"Bread\""));
	}

	@Test
	@DisplayName("resource paths (ADR-0005): navigation, property, $value, $count segments")
	void resourcePaths() throws Exception {
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");
		EObject dairy = pkg.getEFactoryInstance().create(categoryClass);
		dairy.eSet(categoryClass.getEStructuralFeature("id"), "c1");
		dairy.eSet(categoryClass.getEStructuralFeature("name"), "Dairy");
		EObject milk = product("p1", "Milk", "1.20", dairy);
		backendResult = List.of(milk);

		Response nav = get("/Product('p1')/category", Map.of());
		assertEquals(200, nav.status());
		assertTrue(nav.body().contains("\"Dairy\""), nav.body());

		Response property = get("/Product('p1')/name", Map.of());
		assertEquals(200, property.status());
		assertTrue(property.body().contains("\"value\":\"Milk\""), property.body());

		Response raw = get("/Product('p1')/name/$value", Map.of());
		assertEquals(200, raw.status());
		assertEquals("Milk", raw.body(), "raw $value is text/plain");

		Response nullValue = get("/Product('p1')/released/$value", Map.of());
		assertEquals(204, nullValue.status(), "null property → no content");

		Response enumValue = get("/Product('p1')/color/$value", Map.of());
		assertEquals("Red", enumValue.body(), "unset enum yields the EMF default literal");

		Response setCount = get("/Product/$count", Map.of());
		assertEquals(200, setCount.status());
		assertEquals("1", setCount.body(), "set count as text/plain");

		Response navCount = get("/Product('p1')/reviews/$count", Map.of());
		assertEquals("0", navCount.body(), "empty collection counts 0");

		assertEquals(404, get("/Product('p1')/nosuch", Map.of()).status(), "unknown segment");
		assertEquals(501, get("/Product('p1')/category/$ref", Map.of()).status(), "$ref later");
		assertEquals(501, get("/Product('p1')/category", Map.of("$orderby", "name")).status(),
				"unimplemented query options on navigation paths → 501");
		assertEquals(404, get("/NoSet('x')/name", Map.of()).status());
	}

	@Test
	@DisplayName("destructive: path traversal shapes are plain 404s")
	void pathShapes() throws Exception {
		assertEquals(404, get("/Product/../secret", Map.of()).status());
		assertEquals(404, get("/a/b/c", Map.of()).status());
	}

	private static Path findResource(String... candidatesRelative) {
		Path start = Path.of("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidatesRelative) {
				Path p = dir.resolve(rel);
				if (Files.exists(p)) {
					return p;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
