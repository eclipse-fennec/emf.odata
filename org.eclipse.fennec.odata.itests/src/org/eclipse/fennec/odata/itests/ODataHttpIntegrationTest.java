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
package org.eclipse.fennec.odata.itests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The full E1–E7 slice over REAL HTTP: webshop EPackage → metadata whiteboard, XMI data files →
 * {@code FileEntityRepository} (via ConfigurationAdmin), in-memory QueryService, ODataServlet on
 * the Jetty HTTP whiteboard. Happy paths plus the destructive contract from the outside:
 * injection-style filters, unknown resources, parser bombs, method restrictions — all must be
 * clean OData errors without internals.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("OData end-to-end over HTTP")
public class ODataHttpIntegrationTest {

	private static final String BASE = "http://127.0.0.1:18893/odata";
	private static final String ECORE = "/org/eclipse/fennec/odata/itests/webshop.ecore";

	private final HttpClient client = HttpClient.newHttpClient();

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private ServiceRegistration<EPackage> packageRegistration;
	private Configuration repositoryConfiguration;
	private Path dataDirectory;

	@BeforeAll
	void setUpStack(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin configurationAdmin) throws Exception {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(ECORE, ODataHttpIntegrationTest.class);
		EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);

		dataDirectory = Files.createTempDirectory("odata-itest-data");
		writeDataFile();

		packageRegistration = context.registerService(EPackage.class, pkg, null);

		repositoryConfiguration = configurationAdmin
				.getConfiguration("org.eclipse.fennec.odata.repository.file", "?");
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put("directory", dataDirectory.toString());
		repositoryConfiguration.update(properties);

		awaitReady();
	}

	@AfterAll
	void tearDownStack() throws Exception {
		if (repositoryConfiguration != null) {
			repositoryConfiguration.delete();
		}
		if (packageRegistration != null) {
			packageRegistration.unregister();
		}
		EPackage.Registry.INSTANCE.remove(pkg.getNsURI());
		ecoreHelper.releaseAll();
	}

	private void writeDataFile() throws Exception {
		EClass productClass = EcoreHelper.getEClass(pkg, "Product");
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");

		EObject dairy = create(categoryClass, "id", "c1", "name", "Dairy");
		EObject milk = create(productClass, "id", "p1", "name", "Milk",
				"price", new BigDecimal("1.20"), "rating", 3, "active", true, "category", dairy);
		EObject cheese = create(productClass, "id", "p2", "name", "Cheese",
				"price", new BigDecimal("4.50"), "rating", 5, "active", true, "category", dairy);

		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		rs.getPackageRegistry().put(pkg.getNsURI(), pkg);
		Resource resource = rs.createResource(
				org.eclipse.emf.common.util.URI.createFileURI(dataDirectory.resolve("data.xmi").toString()));
		resource.getContents().addAll(List.of(dairy, milk, cheese));
		resource.save(null);
	}

	private EObject create(EClass type, Object... featureValuePairs) {
		EObject object = pkg.getEFactoryInstance().create(type);
		for (int i = 0; i < featureValuePairs.length; i += 2) {
			object.eSet(type.getEStructuralFeature((String) featureValuePairs[i]), featureValuePairs[i + 1]);
		}
		return object;
	}

	private void awaitReady() throws Exception {
		long deadline = System.currentTimeMillis() + 15_000;
		Exception last = null;
		while (System.currentTimeMillis() < deadline) {
			try {
				HttpResponse<String> response = get("/Product");
				if (response.statusCode() == 200) {
					return;
				}
			} catch (Exception e) {
				last = e;
			}
			Thread.sleep(200);
		}
		throw new IllegalStateException("OData endpoint did not come up within 15s", last);
	}

	private HttpResponse<String> get(String pathAndQuery) throws Exception {
		return client.send(HttpRequest.newBuilder(URI.create(BASE + pathAndQuery)).GET().build(),
				HttpResponse.BodyHandlers.ofString());
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	@Test
	@Order(1)
	@DisplayName("service document and $metadata are served")
	void documents() throws Exception {
		HttpResponse<String> serviceDoc = get("/");
		assertEquals(200, serviceDoc.statusCode());
		assertTrue(serviceDoc.body().contains("\"Product\""), serviceDoc.body());
		assertEquals("4.01", serviceDoc.headers().firstValue("OData-Version").orElse(null));

		HttpResponse<String> metadata = get("/$metadata");
		assertEquals(200, metadata.statusCode());
		assertTrue(metadata.body().contains("edmx:Edmx") && metadata.body().contains("Product"),
				metadata.body());
	}

	@Test
	@Order(2)
	@DisplayName("filtered, ordered, counted query over data loaded from files")
	void query() throws Exception {
		HttpResponse<String> response = get("/Product?$filter=" + encode("price lt 3.00")
				+ "&$orderby=" + encode("price desc") + "&$count=true");
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"Milk\""), response.body());
		assertFalse(response.body().contains("\"Cheese\""), "Cheese costs 4.50: " + response.body());
		assertTrue(response.body().contains("\"@odata.count\":1"), response.body());

		HttpResponse<String> navigation = get("/Product?$filter="
				+ encode("category/name eq 'Dairy'") + "&$count=true");
		assertTrue(navigation.body().contains("\"@odata.count\":2"), navigation.body());
	}

	@Test
	@Order(2)
	@DisplayName("$compute alias referable in $filter/$orderby end-to-end (inlined → pushed down)")
	void computeAliasQuery() throws Exception {
		HttpResponse<String> response = get("/Product?$compute=" + encode("price mul 2 as doubled")
				+ "&$filter=" + encode("doubled ge 8") + "&$orderby=" + encode("doubled desc"));
		assertEquals(200, response.statusCode(), response.body());
		assertTrue(response.body().contains("\"Cheese\""),
				"Cheese (2×4.50 = 9.00) passes the computed filter: " + response.body());
		assertFalse(response.body().contains("\"Milk\""),
				"Milk (2×1.20 = 2.40) is filtered out by the computed alias: " + response.body());
		assertTrue(response.body().contains("\"doubled\":9.00"),
				"the computed member is present: " + response.body());
	}

	@Test
	@Order(3)
	@DisplayName("destructive: injection-style filters die with 400, nothing leaks")
	void injection() throws Exception {
		for (String attack : List.of(
				"name eq 'a' or 1=1 --",
				"name eq 'a'; DROP TABLE users",
				"nosuchproperty eq 1",
				"frobnicate(name) eq 1")) {
			HttpResponse<String> response = get("/Product?$filter=" + encode(attack));
			assertEquals(400, response.statusCode(), attack);
			assertTrue(response.body().startsWith("{\"error\""), attack);
			assertFalse(response.body().contains("Exception"), "no internals: " + response.body());
		}
	}

	@Test
	@Order(4)
	@DisplayName("destructive: resource exhaustion is rejected at the door")
	void exhaustion() throws Exception {
		String parenBomb = "(".repeat(200) + "name eq 'x'" + ")".repeat(200);
		assertEquals(400, get("/Product?$filter=" + encode(parenBomb)).statusCode(), "paren bomb");

		String longFilter = "name eq '" + "x".repeat(8000) + "'";
		assertEquals(400, get("/Product?$filter=" + encode(longFilter)).statusCode(), "length limit");

		assertEquals(400, get("/Product?$top=-1").statusCode(), "negative paging");
		assertEquals(200, get("/Product?$top=999999").statusCode(), "huge $top is capped, not an error");
	}

	@Test
	@Order(5)
	@DisplayName("Prefer: respond-async over real HTTP: background execution, monitor polling, "
			+ "one-shot delivery (11.6)")
	void respondAsync() throws Exception {
		// the query options exercise the request snapshot: the worker runs AFTER Jetty
		// recycled the original request, so every value must come from the captured copy
		HttpResponse<String> accepted = client.send(HttpRequest
				.newBuilder(URI.create(BASE + "/Product?$filter=" + encode("price lt 3.00")
						+ "&$count=true"))
				.header("Prefer", "respond-async").GET().build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(202, accepted.statusCode(), accepted.body());
		assertEquals("respond-async",
				accepted.headers().firstValue("Preference-Applied").orElse(null));
		String location = accepted.headers().firstValue("Location").orElseThrow();
		assertTrue(location.contains("/$async/"), location);
		// Location is a relative reference (RFC 7231 §7.1.2) — resolve against the service host
		URI monitor = URI.create(BASE).resolve(location);

		HttpResponse<String> result = null;
		for (int attempt = 0; attempt < 500; attempt++) {
			result = client.send(HttpRequest.newBuilder(monitor).GET().build(),
					HttpResponse.BodyHandlers.ofString());
			if (result.statusCode() != 202) {
				break;
			}
			Thread.sleep(10);
		}
		assertEquals(200, result.statusCode(), result.body());
		assertTrue(result.headers().firstValue("Content-Type").orElse("")
				.startsWith("application/http"), "delivery travels as application/http");
		assertTrue(result.body().startsWith("HTTP/1.1 200 OK"), result.body());
		assertTrue(result.body().contains("\"Milk\""), result.body());
		assertTrue(result.body().contains("\"@odata.count\":1"),
				"query options survived the snapshot: " + result.body());

		assertEquals(404, client.send(HttpRequest.newBuilder(monitor).GET().build(),
				HttpResponse.BodyHandlers.ofString()).statusCode(),
				"the monitor is one-shot — gone once retrieved");
	}

	@Test
	@Order(6)
	@DisplayName("single entity, $select/$expand, $apply and $format=xml end-to-end")
	void richQuerySurface() throws Exception {
		HttpResponse<String> single = get("/Product('p1')");
		assertEquals(200, single.statusCode());
		assertTrue(single.body().contains("\"Milk\"") && single.body().contains("$entity"), single.body());
		assertEquals(404, get("/Product('nope')").statusCode());

		HttpResponse<String> selected = get("/Product('p1')?$select=name");
		assertFalse(selected.body().contains("1.2"), "price deselected: " + selected.body());

		HttpResponse<String> expanded = get("/Product?$expand=category&$filter="
				+ encode("name eq 'Milk'"));
		assertTrue(expanded.body().contains("\"Dairy\""), "category inline: " + expanded.body());

		HttpResponse<String> apply = get("/Product?$apply=" + encode(
				"groupby((category/name),aggregate(price with sum as Total,$count as Cnt))"));
		assertEquals(200, apply.statusCode());
		assertTrue(apply.body().contains("\"Total\":5.70") || apply.body().contains("\"Total\":5.7"),
				apply.body());
		assertTrue(apply.body().contains("\"Cnt\":2"), apply.body());

		// post-pipeline options: alias filter + count on the transformed set
		HttpResponse<String> applyFiltered = get("/Product?$apply=" + encode(
				"groupby((category/name),aggregate(price with sum as Total))")
				+ "&$filter=" + encode("Total gt 3.00") + "&$count=true");
		assertEquals(200, applyFiltered.statusCode());
		assertTrue(applyFiltered.body().contains("\"@odata.count\":1"), applyFiltered.body());

		HttpResponse<String> xml = get("/Product?$format=xml");
		assertEquals(200, xml.statusCode());
		assertTrue(xml.body().startsWith("<?xml"), xml.body());
		assertTrue(xml.body().contains("Milk"), xml.body());
		assertTrue(xml.headers().firstValue("Content-Type").orElse("").contains("application/xml"));
	}

	@Test
	@Order(7)
	@DisplayName("conformance: nextLink paging round-trip and 501 for unsupported options")
	void conformance() throws Exception {
		HttpResponse<String> firstPage = get("/Product?$top=1&$orderby=name");
		assertEquals(200, firstPage.statusCode());
		assertTrue(firstPage.body().contains("\"@odata.nextLink\""), firstPage.body());
		assertTrue(firstPage.body().contains("\"Cheese\""), "first by name: " + firstPage.body());

		String link = firstPage.body().replaceAll(".*\"@odata.nextLink\":\"([^\"]+)\".*", "$1");
		HttpResponse<String> secondPage = client.send(HttpRequest.newBuilder(
				URI.create("http://127.0.0.1:18893" + link)).GET().build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(200, secondPage.statusCode());
		assertTrue(secondPage.body().contains("\"Milk\""), "next page: " + secondPage.body());
		assertFalse(secondPage.body().contains("@odata.nextLink"),
				"last page has no link: " + secondPage.body());

		assertEquals(501, get("/Product?$schemaversion=*").statusCode(), "unsupported option");
		assertEquals(400, get("/Product?$frobnicate=1").statusCode(), "unknown $-option");

		// own resource-path parser (ADR-0005): navigation, property addressing, $value, $count
		HttpResponse<String> nav = get("/Product('p1')/category");
		assertEquals(200, nav.statusCode());
		assertTrue(nav.body().contains("\"Dairy\""), nav.body());
		assertEquals("Milk", get("/Product('p1')/name/$value").body());
		assertEquals("2", get("/Product/$count").body());
		assertEquals("1", get("/Product/$count?$filter=" + encode("price lt 3.00")).body(),
				"filtered count");
		assertEquals(404, get("/Product('p1')/nosuch").statusCode());
	}

	@Test
	@Order(5)
	@DisplayName("destructive: unknown resources and write methods")
	void resourceContract() throws Exception {
		assertEquals(404, get("/NoSuchSet").statusCode());
		assertEquals(404, get("/Product/sub/path").statusCode());

		HttpResponse<String> post = client.send(HttpRequest.newBuilder(URI.create(BASE + "/Product"))
				.POST(HttpRequest.BodyPublishers.ofString("{}")).build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(415, post.statusCode(),
				"the write path guards the media type first (no JSON content type sent)");
	}

	@Test
	@Order(6)
	@DisplayName("write round trip over real HTTP: POST 201 → GET → DELETE 204 → 404")
	void writeRoundTrip() throws Exception {
		HttpResponse<String> created = client.send(
				HttpRequest.newBuilder(URI.create(BASE + "/Product"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(
								"{\"id\":\"w-e2e\",\"name\":\"Created over HTTP\"}"))
						.build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(201, created.statusCode(), created.body());
		assertTrue(created.headers().firstValue("Location").orElse("")
				.endsWith("/Product('w-e2e')"), "Location is the edit URL");

		HttpResponse<String> fetched = get("/Product('w-e2e')");
		assertEquals(200, fetched.statusCode());
		assertTrue(fetched.body().contains("Created over HTTP"), fetched.body());
		String etag = fetched.headers().firstValue("ETag").orElse(null);
		assertTrue(etag != null && etag.startsWith("W/\""), "single GET serves an ETag: " + etag);

		HttpResponse<String> unconditional = client.send(
				HttpRequest.newBuilder(URI.create(BASE + "/Product('w-e2e')")).DELETE().build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(428, unconditional.statusCode(),
				"existing entities carry an ETag — writes need If-Match (11.4.1.1)");

		HttpResponse<String> deleted = client.send(
				HttpRequest.newBuilder(URI.create(BASE + "/Product('w-e2e')"))
						.header("If-Match", etag).DELETE().build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(204, deleted.statusCode());
		assertEquals(404, get("/Product('w-e2e')").statusCode(), "really gone");
	}

	@Test
	@Order(8)
	@DisplayName("$batch round trip over real HTTP: a GET and a create dispatch and answer in order")
	void batchRoundTrip() throws Exception {
		String body = """
				{"requests":[
				  {"id":"1","method":"GET","url":"Product"},
				  {"id":"2","method":"POST","url":"Product","headers":{"content-type":"application/json"},\
				"body":{"id":"batch-e2e","name":"Batched"}},
				  {"id":"3","method":"GET","url":"Product('batch-e2e')","dependsOn":["2"]}
				]}""";
		HttpResponse<String> batch = client.send(
				HttpRequest.newBuilder(URI.create(BASE + "/$batch"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(body)).build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(200, batch.statusCode(), batch.body());
		assertTrue(batch.body().contains("\"responses\""), batch.body());
		assertTrue(batch.body().contains("\"status\":200"), batch.body());
		assertTrue(batch.body().contains("\"status\":201"), "the create answers 201: " + batch.body());
		assertTrue(batch.body().contains("Batched"),
				"the dependent GET sees the created entity: " + batch.body());

		// the classic multipart form (4.0) is accepted since the multipart batch landed —
		// an empty multipart answers an empty multipart; unknown formats stay 415
		HttpResponse<String> multipart = client.send(
				HttpRequest.newBuilder(URI.create(BASE + "/$batch"))
						.header("Content-Type", "multipart/mixed;boundary=b")
						.POST(HttpRequest.BodyPublishers.ofString("--b--")).build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(200, multipart.statusCode(), multipart.body());
		HttpResponse<String> unknown = client.send(
				HttpRequest.newBuilder(URI.create(BASE + "/$batch"))
						.header("Content-Type", "text/plain")
						.POST(HttpRequest.BodyPublishers.ofString("nope")).build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(415, unknown.statusCode(), unknown.body());
	}

	@Test
	@Order(9)
	@DisplayName("$batch atomicity group rolls back all-or-nothing when a member fails")
	void batchAtomicityGroupRollback() throws Exception {
		// two creates in one group; the second reuses the first's key → 409 → the whole group rolls back
		String body = """
				{"requests":[
				  {"id":"1","atomicityGroup":"g1","method":"POST","url":"Product",\
				"headers":{"content-type":"application/json"},"body":{"id":"atomic-e2e","name":"First"}},
				  {"id":"2","atomicityGroup":"g1","method":"POST","url":"Product",\
				"headers":{"content-type":"application/json"},"body":{"id":"atomic-e2e","name":"Dup"}}
				]}""";
		HttpResponse<String> batch = client.send(
				HttpRequest.newBuilder(URI.create(BASE + "/$batch"))
						.header("Content-Type", "application/json")
						.POST(HttpRequest.BodyPublishers.ofString(body)).build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(200, batch.statusCode(), batch.body());
		assertTrue(batch.body().contains("\"status\":409"), "the duplicate create fails: " + batch.body());
		assertTrue(batch.body().contains("\"status\":424"),
				"the first member is rolled back to 424: " + batch.body());

		assertEquals(404, get("/Product('atomic-e2e')").statusCode(),
				"the first create was rolled back — nothing persisted");
	}

	@Test
	@Order(20)
	@DisplayName("concurrency: parallel reads and writes over HTTP all succeed and stay consistent")
	void concurrentRequests() throws Exception {
		int threads = 12;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			List<Future<Integer>> reads = new ArrayList<>();
			List<Future<Integer>> writes = new ArrayList<>();
			for (int i = 0; i < threads; i++) {
				reads.add(pool.submit(() -> get("/Product?$count=true").statusCode()));
				String id = "cc-" + i;
				writes.add(pool.submit(() -> post("/Product",
						"{\"id\":\"" + id + "\",\"name\":\"Concurrent " + id + "\"}").statusCode()));
			}
			for (Future<Integer> r : reads) {
				assertEquals(200, r.get(20, TimeUnit.SECONDS), "every concurrent read succeeds");
			}
			for (Future<Integer> w : writes) {
				assertEquals(201, w.get(20, TimeUnit.SECONDS), "every concurrent create succeeds");
			}
		} finally {
			pool.shutdownNow();
		}
		// all concurrently-created rows are readable and none was lost
		String all = get("/Product?$top=1000").body();
		for (int i = 0; i < threads; i++) {
			assertTrue(all.contains("Concurrent cc-" + i), "cc-" + i + " missing: race/lost write");
		}
	}

	@Test
	@Order(21)
	@DisplayName("deep server-driven paging over HTTP: follow @odata.nextLink to the last page")
	void deepPagingOverHttp() throws Exception {
		int rows = 45;
		for (int i = 0; i < rows; i++) {
			String id = String.format("pg-%02d", i);
			assertEquals(201, post("/Product",
					"{\"id\":\"" + id + "\",\"name\":\"" + id + "\"}").statusCode());
		}
		// page with a small $top and follow nextLink to the end, collecting the paged ids
		Set<String> seen = new java.util.HashSet<>();
		String pathAndQuery = "/Product?$filter=" + encode("startswith(name,'pg-')") + "&$top=10";
		int pages = 0;
		while (pathAndQuery != null && pages++ < 100) {
			HttpResponse<String> page = get(pathAndQuery);
			assertEquals(200, page.statusCode(), page.body());
			Matcher ids = Pattern.compile("\"id\":\"(pg-\\d\\d)\"").matcher(page.body());
			while (ids.find()) {
				assertTrue(seen.add(ids.group(1)), "a row appeared on two pages: " + ids.group(1));
			}
			Matcher next = Pattern.compile("\"@odata.nextLink\":\"([^\"]+)\"").matcher(page.body());
			pathAndQuery = next.find() ? relativeNextLink(next.group(1)) : null;
		}
		assertEquals(rows, seen.size(), "every paged row is visited exactly once across the pages");
	}

	/** The nextLink relative to BASE (it may be absolute or already relative). */
	private static String relativeNextLink(String nextLink) {
		int odata = nextLink.indexOf("/odata");
		return odata >= 0 ? nextLink.substring(odata + "/odata".length()) : nextLink;
	}

	private HttpResponse<String> post(String path, String json) throws Exception {
		return client.send(HttpRequest.newBuilder(URI.create(BASE + path))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build(),
				HttpResponse.BodyHandlers.ofString());
	}
}
