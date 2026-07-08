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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.fennec.odata.csdl.EcoreToEdmConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * E8 foundation against an HTTP stub that answers exactly like the server family: the CSDL
 * document is produced by the REAL E2 write path ({@link EcoreToEdmConverter} + EMF XML
 * serialization), entity payloads are OData JSON as {@code ODataServlet} emits it. The stub
 * records request URIs, so URL assembly and option encoding are asserted exactly.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ODataClient: $metadata → Ecore, fluent requests, OData-JSON decode")
class ODataClientTest {

	private HttpServer server;
	private String serviceRoot;
	private final AtomicReference<java.net.URI> lastRequest = new AtomicReference<>();
	private final AtomicReference<String> lastWriteMethod = new AtomicReference<>();
	private final AtomicReference<String> lastWriteBody = new AtomicReference<>();
	private final AtomicReference<String> lastIfMatch = new AtomicReference<>();
	private final AtomicReference<String> lastAuth = new AtomicReference<>();
	private final AtomicReference<String> lastMaxVersion = new AtomicReference<>();
	private final AtomicReference<String> lastCsrf = new AtomicReference<>();
	private final AtomicReference<String> lastBatchBody = new AtomicReference<>();

	@BeforeAll
	void setUpStub() throws Exception {
		String csdl = csdlFor(loadWebshop());
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/odata/", exchange -> {
			lastRequest.set(exchange.getRequestURI());
			lastAuth.set(exchange.getRequestHeaders().getFirst("Authorization"));
			lastMaxVersion.set(exchange.getRequestHeaders().getFirst("OData-MaxVersion"));
			if ("Fetch".equalsIgnoreCase(exchange.getRequestHeaders().getFirst("X-CSRF-Token"))) {
				exchange.getResponseHeaders().set("X-CSRF-Token", "tok-xyz");
				try {
					exchange.sendResponseHeaders(200, -1);
				} catch (java.io.IOException ignored) {
					// handled by the client as a failed exchange
				}
				exchange.close();
				return;
			}
			String path = exchange.getRequestURI().getPath();
			String method = exchange.getRequestMethod();
			if (path.endsWith("/$batch")) {
				handleBatch(exchange);
				return;
			}
			if (!"GET".equals(method)) {
				handleWrite(exchange, method, path);
				return;
			}
			String answer;
			String contentType = "application/json;odata.metadata=minimal;charset=UTF-8";
			int status = 200;
			if (path.endsWith("/$metadata")) {
				answer = csdl;
				contentType = "application/xml;charset=UTF-8";
			} else if (path.endsWith("/Product/$count")) {
				answer = "5";
				contentType = "text/plain;charset=UTF-8";
			} else if (path.endsWith("/Product")
					&& exchange.getRequestURI().getRawQuery() != null
					&& exchange.getRequestURI().getRawQuery().contains("$apply")) {
				answer = "{\"value\":[{\"category\":{\"name\":\"Dairy\"},\"Total\":5.70}]}";
			} else if (path.endsWith("/Product")
					&& exchange.getRequestURI().getRawQuery() != null
					&& exchange.getRequestURI().getRawQuery().contains("$compute")) {
				answer = "{\"value\":[{\"id\":\"p1\",\"name\":\"Milk\",\"doublePrice\":2.40}]}";
			} else if (path.contains("/featured(") || path.contains(".twin(")) {
				answer = "{\"@odata.context\":\"/odata/$metadata#Product/$entity\",\"id\":\"p1\","
						+ "\"name\":\"Milk\",\"price\":\"1.20\",\"rating\":3,\"active\":true}"; // entity-typed result
			} else if (path.contains("/topProducts(")) {
				answer = "{\"@odata.context\":\"/odata/$metadata#Product\",\"value\":[" // collection-typed result
						+ "{\"id\":\"p1\",\"name\":\"Milk\",\"price\":\"1.20\",\"rating\":3,\"active\":true},"
						+ "{\"id\":\"p2\",\"name\":\"Cheese\",\"price\":\"4.50\",\"rating\":5,\"active\":true}]}";
			} else if (path.contains("/doubleOf(")) {
				answer = "{\"value\":42}"; // unbound function import result
			} else if (path.contains("/webshop.label(")) {
				answer = "{\"value\":\"X:Milk\"}"; // bound function result
			} else if (path.endsWith("/name/$value")) {
				answer = "Milk";
				contentType = "text/plain;charset=UTF-8";
			} else if (path.endsWith("/reviews/$count")) {
				answer = "2";
				contentType = "text/plain;charset=UTF-8";
			} else if (path.endsWith("/category")) {
				answer = "{\"id\":\"c1\",\"name\":\"Dairy\"}";
			} else if (path.endsWith("/reviews")) {
				answer = "{\"value\":[{\"stars\":5,\"comment\":\"great\"},{\"stars\":4,\"comment\":\"good\"}]}";
			} else if (path.endsWith("/Product('bad')")) {
				answer = "this is not json"; // a 200 with an undecodable body
			} else if (path.endsWith("/Product('p1')")) {
				answer = """
						{"@odata.context":"/odata/$metadata#Product/$entity","id":"p1",\
						"name":"Milk","price":"1.20","rating":3,"active":true,\
						"reviews":[{"stars":5,"comment":"great"}]}""";
			} else if (path.endsWith("/Product")) {
				answer = """
						{"@odata.context":"/odata/$metadata#Product","@odata.count":5,"value":[\
						{"id":"p1","name":"Milk","price":"1.20","rating":3,"active":true},\
						{"id":"p2","name":"Cheese","price":"4.50","rating":5,"active":true,\
						"category":{"id":"c1","name":"Dairy"}}],\
						"@odata.nextLink":"/odata/Product?$skip=2"}""";
			} else {
				status = 404;
				answer = "{\"error\":{\"code\":\"404\",\"message\":\"unknown resource\"}}";
			}
			byte[] body = answer.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().set("Content-Type", contentType);
			exchange.sendResponseHeaders(status, body.length);
			exchange.getResponseBody().write(body);
			exchange.close();
		});
		server.start();
		serviceRoot = "http://127.0.0.1:" + server.getAddress().getPort() + "/odata/";
	}

	@AfterAll
	void tearDownStub() {
		server.stop(0);
	}

	@Test
	@DisplayName("connect reads $metadata into Ecore packages (E2 read path)")
	void metadataToEcore() {
		ODataClient client = ODataClient.connect(serviceRoot);
		assertEquals(1, client.metadata().size(), "one schema → one EPackage");
		EClass product = client.entityType("Product");
		assertNotNull(product.getEStructuralFeature("price"), "features survive the round trip");
		assertNotNull(product.getEStructuralFeature("category"), "navigations survive");
		assertThrows(ODataClientException.class, () -> client.entityType("Nope"));
	}

	@Test
	@DisplayName("fluent options assemble an exactly encoded URL")
	void urlAssembly() {
		ODataClient client = ODataClient.connect(serviceRoot);
		client.entitySet("Product")
				.filter("price lt 3.00 and name eq 'Süß'")
				.orderBy("name asc").top(10).skip(2).count()
				.select("name", "price").expand("category")
				.list();
		String query = lastRequest.get().getRawQuery();
		assertTrue(query.contains("$filter=price%20lt%203.00%20and%20name%20eq%20%27S%C3%BC%C3%9F%27"),
				"spaces %20, quotes and UTF-8 percent-encoded: " + query);
		assertTrue(query.contains("$orderby=name%20asc"), query);
		assertTrue(query.contains("$top=10") && query.contains("$skip=2")
				&& query.contains("$count=true"), query);
		assertTrue(query.contains("$select=name%2Cprice") && query.contains("$expand=category"),
				query);
	}

	@Test
	@DisplayName("collection responses decode into EObjects with envelope control info")
	void collectionDecode() {
		ODataClient client = ODataClient.connect(serviceRoot);
		ODataPage page = client.entitySet("Product").list();

		assertEquals(2, page.entities().size());
		assertEquals(5, page.totalCount(), "@odata.count from the envelope");
		assertTrue(page.hasMore(), "@odata.nextLink from the envelope");

		EClass product = client.entityType("Product");
		EObject milk = page.entities().get(0);
		assertEquals("Milk", milk.eGet(product.getEStructuralFeature("name")));
		assertEquals(0, new BigDecimal("1.20").compareTo(
				(BigDecimal) milk.eGet(product.getEStructuralFeature("price"))),
				"Edm.Decimal decodes to BigDecimal");
		assertEquals(3, milk.eGet(product.getEStructuralFeature("rating")));

		EObject cheese = page.entities().get(1);
		EObject category = (EObject) cheese.eGet(product.getEStructuralFeature("category"));
		assertNotNull(category, "expanded navigation decodes inline");
		assertEquals("Dairy", category.eGet(category.eClass().getEStructuralFeature("name")));
	}

	@Test
	@DisplayName("single entity by key, nested containment and /$count")
	void singleEntityAndCount() {
		ODataClient client = ODataClient.connect(serviceRoot);
		EClass product = client.entityType("Product");

		EObject milk = client.entitySet("Product").get("'p1'");
		assertTrue(lastRequest.get().getRawPath().endsWith("/Product('p1')"),
				"raw key literal lands quoted in the path: " + lastRequest.get());
		assertEquals("Milk", milk.eGet(product.getEStructuralFeature("name")));
		List<?> reviews = (List<?>) milk.eGet(product.getEStructuralFeature("reviews"));
		assertEquals(1, reviews.size(), "containment children decode");

		assertEquals(5, client.entitySet("Product").totalCount());
		assertTrue(lastRequest.get().getPath().endsWith("/Product/$count"));
	}

	@Test
	@DisplayName("service errors surface with status and the server's error body")
	void errors() {
		ODataClient client = ODataClient.connect(serviceRoot);
		ODataClientException error = assertThrows(ODataClientException.class,
				() -> client.entitySet("Category").list());
		assertEquals(404, error.status());
		assertTrue(error.getMessage().contains("unknown resource"), error.getMessage());
	}

	@Test
	@DisplayName("nextPage follows the server's @odata.nextLink to the next page of entities")
	void followsNextLink() {
		ODataClient client = ODataClient.connect(serviceRoot);
		ODataPage first = client.entitySet("Product").list();
		assertTrue(first.hasMore(), "the stub's first page carries a nextLink");

		ODataPage second = client.entitySet("Product").nextPage(first);
		assertEquals(2, second.entities().size(), "the follow-up request decodes into entities");
		assertTrue(lastRequest.get().toString().contains("$skip=2"),
				"the nextLink's query is carried onto the follow-up request: " + lastRequest.get());
	}

	@Test
	@DisplayName("real-world foreign $metadata (OData.org TripPin) parses into Ecore packages")
	void parsesRealWorldMetadata() throws Exception {
		String trippin = Files.readString(findResource(
				"testdata/metadata-samples/trippin-v4-metadata.xml",
				"org.eclipse.fennec.odata.csdl/testdata/trippin-v4-metadata.xml"));
		List<EPackage> packages = CsdlMetadataReader.read(trippin);
		assertFalse(packages.isEmpty(), "a foreign service's metadata converts to at least one EPackage");
		assertTrue(packages.stream().anyMatch(p -> p.getEClassifier("Person") != null),
				"TripPin's Person entity type survives the client read path");
	}

	@Test
	@DisplayName("an oversized response is rejected, not buffered unbounded into the client heap")
	void oversizedResponseRejected() {
		ODataClient client = ODataClient.connect(serviceRoot); // $metadata fetched under the default cap
		client.maxResponseBytes = 10; // now clamp hard
		ODataClientException error = assertThrows(ODataClientException.class,
				() -> client.entitySet("Product").list());
		assertTrue(error.getMessage().contains("exceeds the client limit"), error.getMessage());
	}

	@Test
	@DisplayName("a $metadata with a DOCTYPE/XXE payload is rejected, never resolved (XXE hardening)")
	void metadataXxeRejected() {
		String malicious = """
				<?xml version="1.0"?>
				<!DOCTYPE edmx [ <!ENTITY xxe SYSTEM "file:///etc/passwd"> ]>
				<edmx:Edmx xmlns:edmx="http://docs.oasis-open.org/odata/ns/edmx" Version="4.0">
				  <edmx:DataServices>
				    <Schema xmlns="http://docs.oasis-open.org/odata/ns/edm" Namespace="X">&xxe;</Schema>
				  </edmx:DataServices>
				</edmx:Edmx>""";
		// the DOCTYPE is disallowed outright, so the external entity is never expanded
		assertThrows(ODataClientException.class, () -> CsdlMetadataReader.read(malicious));
	}

	@Test
	@DisplayName("a malformed $metadata (no DataServices) is a clear client error, not a crash")
	void metadataWithoutDataServices() {
		String noDataServices =
				"<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.0\"/>";
		assertThrows(ODataClientException.class, () -> CsdlMetadataReader.read(noDataServices));
	}

	@Test
	@DisplayName("a server @odata.nextLink to a foreign origin is refused (SSRF guard)")
	void nextLinkToForeignOriginRefused() {
		ODataClient client = ODataClient.connect(serviceRoot);
		// a link that does NOT reduce to a service-root-relative path would otherwise be followed verbatim
		ODataPage foreign = new ODataPage(List.of(), -1, "http://evil.example.com/harvest?$skip=2");
		ODataClientException error = assertThrows(ODataClientException.class,
				() -> client.entitySet("Product").nextPage(foreign));
		assertTrue(error.getMessage().contains("different origin"), error.getMessage());
	}

	@Test
	@DisplayName("close() releases an owned HttpClient but leaves an injected one open")
	void injectedHttpClientSurvivesClose() {
		HttpClient injected = HttpClient.newHttpClient();
		ODataClient first = ODataClient.connect(serviceRoot, injected);
		first.close(); // must NOT close the caller-owned client

		ODataClient second = ODataClient.connect(serviceRoot, injected);
		assertEquals(1, second.metadata().size(), "the injected HttpClient is still usable after close()");
		injected.close();
	}

	@Test
	@DisplayName("create POSTs the encoded entity and decodes the 201 body")
	void createPostsEntity() {
		ODataClient client = ODataClient.connect(serviceRoot);
		EClass product = client.entityType("Product");
		EObject p = product.getEPackage().getEFactoryInstance().create(product);
		p.eSet(product.getEStructuralFeature("id"), "n1");
		p.eSet(product.getEStructuralFeature("name"), "Milk");

		EObject created = client.entitySet("Product").create(p);
		assertEquals("POST", lastWriteMethod.get());
		assertTrue(lastWriteBody.get().contains("\"name\":\"Milk\""), lastWriteBody.get());
		assertEquals("Milk", created.eGet(product.getEStructuralFeature("name")),
				"the 201 body decodes back into an entity");
	}

	@Test
	@DisplayName("update sends PATCH with If-Match and only the changed property (merge)")
	void updatePatchesWithIfMatch() {
		ODataClient client = ODataClient.connect(serviceRoot);
		EClass product = client.entityType("Product");
		EObject patch = product.getEPackage().getEFactoryInstance().create(product);
		patch.eSet(product.getEStructuralFeature("name"), "Renamed");

		client.entitySet("Product").update("'n1'", patch, "W/\"e1\"");
		assertEquals("PATCH", lastWriteMethod.get());
		assertEquals("W/\"e1\"", lastIfMatch.get(), "the If-Match ETag is sent");
		assertTrue(lastWriteBody.get().contains("\"name\":\"Renamed\""), lastWriteBody.get());
		assertFalse(lastWriteBody.get().contains("\"id\""), "unset properties are not sent: "
				+ lastWriteBody.get());
	}

	@Test
	@DisplayName("delete reports found (204) vs not-found (404)")
	void deleteReportsExistence() {
		ODataClient client = ODataClient.connect(serviceRoot);
		assertTrue(client.entitySet("Product").delete("'n1'", null));
		assertFalse(client.entitySet("Product").delete("'missing'", null));
	}

	@Test
	@DisplayName("$ref set sends the @odata.id body to nav/$ref")
	void setReferenceSendsRef() {
		ODataClient client = ODataClient.connect(serviceRoot);
		client.entitySet("Product").setReference("'n1'", "category", serviceRoot + "Category('c1')");
		assertEquals("PUT", lastWriteMethod.get());
		assertTrue(lastWriteBody.get().contains("@odata.id"), lastWriteBody.get());
		assertTrue(lastRequest.get().getPath().endsWith("/category/$ref"),
				lastRequest.get().toString());
	}

	@Test
	@DisplayName("configured auth header and OData-MaxVersion are sent on every request (incl. $metadata)")
	void configuredHeadersAreSent() {
		ODataClientConfig config = ODataClientConfig.DEFAULTS.withBearerToken("tok123").withMaxVersion("4.0");
		ODataClient client = ODataClient.connect(serviceRoot, config);
		assertEquals("Bearer tok123", lastAuth.get(), "the $metadata fetch already carries the token");
		assertEquals("4.0", lastMaxVersion.get());

		client.entitySet("Product").list();
		assertEquals("Bearer tok123", lastAuth.get(), "and so does a data request");
	}

	@Test
	@DisplayName("a service error document is parsed into a structured ODataError")
	void structuredError() {
		ODataClient client = ODataClient.connect(serviceRoot);
		ODataClientException error = assertThrows(ODataClientException.class,
				() -> client.entitySet("Product").get("'nope'"));
		assertEquals(404, error.status());
		assertTrue(error.error().isPresent(), "the OData error envelope is parsed: " + error.getMessage());
		assertEquals("404", error.error().get().code());
		assertEquals("unknown resource", error.error().get().message());
	}

	@Test
	@DisplayName("an undecodable 200 body surfaces as a clear ODataClientException")
	void undecodableBody() {
		ODataClient client = ODataClient.connect(serviceRoot);
		ODataClientException error = assertThrows(ODataClientException.class,
				() -> client.entitySet("Product").get("'bad'"));
		assertTrue(error.getMessage().toLowerCase().contains("json")
				|| error.getMessage().toLowerCase().contains("undecodable"), error.getMessage());
	}

	@Test
	@DisplayName("unbound action import posts the parameters as a JSON body")
	void actionImportCall() {
		ODataClient client = ODataClient.connect(serviceRoot);
		Object result = client.action("touch", java.util.Map.of("id", "p1"));
		assertNull(result, "a void action returns null");
		assertEquals("POST", lastWriteMethod.get());
		assertTrue(lastRequest.get().getRawPath().endsWith("/touch"), lastRequest.get().toString());
		assertTrue(lastWriteBody.get().contains("\"id\":\"p1\""), lastWriteBody.get());
	}

	@Test
	@DisplayName("unbound function import call formats params and returns the value")
	void functionImportCall() {
		ODataClient client = ODataClient.connect(serviceRoot);
		Object result = client.function("doubleOf", java.util.Map.of("n", 21));
		assertEquals(42, ((Number) result).intValue());
		assertTrue(lastRequest.get().getRawPath().endsWith("/doubleOf(n=21)"),
				lastRequest.get().toString());
	}

	@Test
	@DisplayName("bound function call addresses the entity and returns the value")
	void boundFunctionCall() {
		ODataClient client = ODataClient.connect(serviceRoot);
		Object result = client.entitySet("Product")
				.boundFunction("'p1'", "webshop.label", java.util.Map.of("prefix", "X"));
		assertEquals("X:Milk", result);
		assertTrue(lastRequest.get().getRawPath().endsWith("/webshop.label(prefix='X')"),
				lastRequest.get().toString());
	}

	@Test
	@DisplayName("navigation-path addressing: nav entity/collection, /$value and nav/$count")
	void navigationPaths() {
		ODataClient client = ODataClient.connect(serviceRoot);
		EClass category = client.entityType("Category");

		EObject cat = client.entitySet("Product").navigateEntity("'p1'", "category");
		assertEquals("Dairy", cat.eGet(category.getEStructuralFeature("name")));

		ODataPage reviews = client.entitySet("Product").navigateCollection("'p1'", "reviews");
		assertEquals(2, reviews.entities().size());

		assertEquals("Milk", client.entitySet("Product").propertyValue("'p1'", "name"));
		assertEquals(2, client.entitySet("Product").navigationCount("'p1'", "reviews"));
	}

	@Test
	@DisplayName("functions/actions with entity or collection results decode typed (not just primitive value)")
	void typedOperationResults() {
		ODataClient client = ODataClient.connect(serviceRoot);
		EClass product = client.entityType("Product");
		EStructuralFeature name = product.getEStructuralFeature("name");

		EObject one = client.functionAsEntity("featured", java.util.Map.of(), product);
		assertEquals("Milk", one.eGet(name), "unbound function → single entity");

		ODataPage many = client.functionAsCollection("topProducts", java.util.Map.of(), product);
		assertEquals(2, many.entities().size(), "unbound function → entity collection");

		EObject bound = client.entitySet("Product")
				.boundFunctionAsEntity("'p1'", "webshop.twin", java.util.Map.of(), product);
		assertEquals("Milk", bound.eGet(name), "bound function → single entity");
		assertTrue(lastRequest.get().getRawPath().endsWith("/webshop.twin()"),
				lastRequest.get().toString());
	}

	@Test
	@DisplayName("$batch: builder assembles a JSON requests envelope and decodes the responses")
	void jsonBatch() {
		ODataClient client = ODataClient.connect(serviceRoot);
		EClass product = client.entityType("Product");
		EObject fresh = product.getEPackage().getEFactoryInstance().create(product);
		fresh.eSet(product.getEStructuralFeature("id"), "p9");
		fresh.eSet(product.getEStructuralFeature("name"), "Butter");

		ODataBatch batch = client.batch();
		batch.create("Product", fresh); // id "0"
		batch.read("Product");          // id "1"
		java.util.List<ODataBatch.Result> results = batch.execute();

		String sent = lastBatchBody.get();
		assertTrue(sent.contains("\"requests\""), sent);
		assertTrue(sent.contains("\"method\":\"POST\""), sent);
		assertTrue(sent.contains("\"Butter\""), "the created entity is encoded into the sub-request body: " + sent);

		assertEquals(2, results.size());
		assertTrue(results.get(0).isSuccess());
		assertEquals("Milk", results.get(0).asEntity(product).eGet(product.getEStructuralFeature("name")));
		assertEquals(1, results.get(1).asPage(product).entities().size());
	}

	@Test
	@DisplayName("$batch: dependsOn references thread into the request envelope")
	void jsonBatchDependsOn() {
		ODataClient client = ODataClient.connect(serviceRoot);
		ODataBatch batch = client.batch();
		batch.add("c1", "POST", "Product", null, java.util.List.of());
		batch.add("g1", "GET", "Product", null, java.util.List.of("c1"));
		batch.execute();
		assertTrue(lastBatchBody.get().contains("\"dependsOn\":[\"c1\"]"), lastBatchBody.get());
	}

	@Test
	@DisplayName("query options thread onto a navigation-collection request ($filter/$orderby/$top)")
	void navigationPathQueryOptions() {
		ODataClient client = ODataClient.connect(serviceRoot);
		client.entitySet("Product").filter("stars ge 4").orderBy("stars desc").top(1)
				.navigateCollection("'p1'", "reviews");
		String query = lastRequest.get().getRawQuery();
		assertTrue(query != null && query.contains("$filter=stars%20ge%204"), String.valueOf(query));
		assertTrue(query.contains("$orderby=stars%20desc"), String.valueOf(query));
		assertTrue(query.contains("$top=1"), String.valueOf(query));
		assertTrue(lastRequest.get().getRawPath().endsWith("/Product('p1')/reviews"),
				lastRequest.get().toString());
	}

	@Test
	@DisplayName("$compute values are read via listRaw (computed props are not model features)")
	void computeReadViaListRaw() {
		ODataClient client = ODataClient.connect(serviceRoot);
		java.util.List<java.util.Map<String, Object>> rows = client.entitySet("Product")
				.compute("price mul 2 as doublePrice").listRaw();
		assertEquals(1, rows.size());
		assertEquals("Milk", rows.get(0).get("name"), "the entity's own properties are present");
		assertEquals(2.40, ((Number) rows.get(0).get("doublePrice")).doubleValue(), 1e-9,
				"the computed property is present");
	}

	@Test
	@DisplayName("$apply aggregation decodes into generic grouped rows")
	void applyReturnsRows() {
		ODataClient client = ODataClient.connect(serviceRoot);
		java.util.List<java.util.Map<String, Object>> rows = client.entitySet("Product")
				.apply("groupby((category/name),aggregate(price with sum as Total))");
		assertEquals(1, rows.size());
		assertTrue(lastRequest.get().toString().contains("$apply"), lastRequest.get().toString());
		@SuppressWarnings("unchecked")
		java.util.Map<String, Object> category = (java.util.Map<String, Object>) rows.get(0).get("category");
		assertEquals("Dairy", category.get("name"));
	}

	@Test
	@DisplayName("$search / $compute / $format / parameter aliases assemble into the query")
	void extraQueryOptionsAssemble() {
		ODataClient client = ODataClient.connect(serviceRoot);
		client.entitySet("Product")
				.search("milk").compute("price mul 2 as dbl").format("json")
				.parameterAlias("p", "3.00").filter("price lt @p").list();
		String query = lastRequest.get().getRawQuery();
		assertTrue(query.contains("$search=milk"), query);
		assertTrue(query.contains("$compute=price%20mul%202%20as%20dbl"), query);
		assertTrue(query.contains("$format=json"), query);
		assertTrue(query.contains("@p=3.00"), query);
	}

	@Test
	@DisplayName("CSRF: the client fetches a token, then sends it on the write (SAP handshake)")
	void csrfHandshake() {
		ODataClient client = ODataClient.connect(serviceRoot, ODataClientConfig.DEFAULTS.withCsrf());
		EClass product = client.entityType("Product");
		EObject p = product.getEPackage().getEFactoryInstance().create(product);
		p.eSet(product.getEStructuralFeature("id"), "n1");
		p.eSet(product.getEStructuralFeature("name"), "Milk");

		client.entitySet("Product").create(p);
		assertEquals("tok-xyz", lastCsrf.get(), "the write carries the fetched X-CSRF-Token");
	}

	/** Stub JSON-batch endpoint: echoes each posted sub-request's id back with a canned 200 response. */
	private void handleBatch(HttpExchange exchange) throws IOException {
		String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
		lastBatchBody.set(body);
		String answer = """
				{"responses":[\
				{"id":"0","status":200,"body":{"id":"p1","name":"Milk","price":"1.20","rating":3,"active":true}},\
				{"id":"1","status":200,"body":{"@odata.context":"/odata/$metadata#Product","value":[\
				{"id":"p1","name":"Milk","price":"1.20","rating":3,"active":true}]}}]}""";
		byte[] bytes = answer.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, bytes.length);
		exchange.getResponseBody().write(bytes);
		exchange.close();
	}

	private void handleWrite(HttpExchange exchange, String method, String path) throws IOException {
		lastWriteMethod.set(method);
		lastIfMatch.set(exchange.getRequestHeaders().getFirst("If-Match"));
		lastCsrf.set(exchange.getRequestHeaders().getFirst("X-CSRF-Token"));
		lastWriteBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
		int status;
		String answer = "";
		if ("POST".equals(method) && path.endsWith("/Product")) {
			status = 201;
			exchange.getResponseHeaders().set("Location", serviceRoot + "Product('n1')");
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			answer = lastWriteBody.get(); // echo the created entity so create() can decode it
		} else if (path.endsWith("/$ref")) {
			status = 204;
		} else if ("DELETE".equals(method)) {
			status = path.contains("('missing')") ? 404 : 204;
			if (status == 404) {
				exchange.getResponseHeaders().set("Content-Type", "application/json");
				answer = "{\"error\":{\"code\":\"404\",\"message\":\"not found\"}}";
			}
		} else {
			status = 204; // PATCH / PUT
		}
		byte[] body = answer.getBytes(StandardCharsets.UTF_8);
		if (status == 204) {
			exchange.sendResponseHeaders(204, -1);
		} else {
			exchange.sendResponseHeaders(status, body.length);
			exchange.getResponseBody().write(body);
		}
		exchange.close();
	}

	// --- stub metadata through the REAL E2 write path ---

	private EPackage loadWebshop() throws IOException {
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		Resource resource = rs.createResource(URI.createFileURI(
				findResource("testdata/webshop.ecore",
						"org.eclipse.fennec.odata.client/testdata/webshop.ecore").toString()));
		resource.load(null);
		return (EPackage) resource.getContents().get(0);
	}

	private String csdlFor(EPackage pkg) throws IOException {
		EdmxRoot root = new EcoreToEdmConverter().toEdmx(pkg);
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMLResourceFactoryImpl());
		rs.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
		rs.getPackageRegistry().put(EdmxPackage.eNS_URI, EdmxPackage.eINSTANCE);
		Resource resource = rs.createResource(URI.createURI("metadata.xml"));
		resource.getContents().add(root);
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
		options.put(XMLResource.OPTION_ENCODING, "UTF-8");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, options);
		return out.toString(StandardCharsets.UTF_8);
	}

	private static Path findResource(String... candidatesRelative) {
		Path start = Paths.get("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidatesRelative) {
				Path candidate = dir.resolve(rel);
				if (Files.exists(candidate)) {
					return candidate;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
