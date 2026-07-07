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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
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

	@BeforeAll
	void setUpStub() throws Exception {
		String csdl = csdlFor(loadWebshop());
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/odata/", exchange -> {
			lastRequest.set(exchange.getRequestURI());
			String path = exchange.getRequestURI().getPath();
			String answer;
			String contentType = "application/json;odata.metadata=minimal;charset=UTF-8";
			int status = 200;
			if (path.endsWith("/$metadata")) {
				answer = csdl;
				contentType = "application/xml;charset=UTF-8";
			} else if (path.endsWith("/Product/$count")) {
				answer = "5";
				contentType = "text/plain;charset=UTF-8";
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
