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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * A CONFIGURED service root (#77/#78): a factory configuration of the servlet mounts one OData
 * root at its own whiteboard pattern, bound to one named HTTP runtime via
 * {@code osgi.http.whiteboard.target}, and publishes exactly the model its allowlist names —
 * although a second, unrelated EPackage is registered in the same framework. A matching filter
 * configuration guards that root with its own limits. While the factory configuration exists the
 * unconfigured default root at {@code /odata} is gone; deleting it brings the default back (the
 * other integration tests rely on that).
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("configured service roots: pattern, whiteboard target, published model")
public class ODataServiceRootIntegrationTest {

	private static final String HOST = "http://127.0.0.1:18893";
	private static final String ROOT = HOST + "/atlas/shop";
	private static final String SHOP_ECORE = "/org/eclipse/fennec/odata/itests/webshop.ecore";
	private static final String FOREIGN_ECORE = "/org/eclipse/fennec/odata/itests/clientshop.ecore";

	private final HttpClient client = HttpClient.newHttpClient();

	private EcoreHelper ecoreHelper;
	private EPackage shop;
	private EPackage foreign;
	private final List<ServiceRegistration<EPackage>> packageRegistrations = new ArrayList<>();
	private final List<Configuration> configurations = new ArrayList<>();
	private Path dataDirectory;

	@BeforeAll
	void setUpStack(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin configurationAdmin) throws Exception {
		ecoreHelper = new EcoreHelper();
		shop = ecoreHelper.loadEcore(SHOP_ECORE, ODataServiceRootIntegrationTest.class);
		foreign = ecoreHelper.loadEcore(FOREIGN_ECORE, ODataServiceRootIntegrationTest.class);
		EPackage.Registry.INSTANCE.put(shop.getNsURI(), shop);
		EPackage.Registry.INSTANCE.put(foreign.getNsURI(), foreign);
		packageRegistrations.add(context.registerService(EPackage.class, shop, null));
		packageRegistrations.add(context.registerService(EPackage.class, foreign, null));

		dataDirectory = Files.createTempDirectory("odata-root-itest");
		writeDataFile();
		Configuration repository = configurationAdmin
				.createFactoryConfiguration("org.eclipse.fennec.odata.repository.file", "?");
		Hashtable<String, Object> repositoryProperties = new Hashtable<>();
		repositoryProperties.put("directory", dataDirectory.toString());
		repository.update(repositoryProperties);
		configurations.add(repository);

		// the ONE runtime this root binds to, selected the whiteboard way (140.3: target filter)
		String target = "(service.id=" + httpServiceRuntimeId(context) + ")";

		Configuration servlet = configurationAdmin
				.createFactoryConfiguration("org.eclipse.fennec.odata.servlet", "?");
		Hashtable<String, Object> servletProperties = new Hashtable<>();
		servletProperties.put("osgi.http.whiteboard.servlet.pattern", "/atlas/shop/*");
		servletProperties.put("osgi.http.whiteboard.target", target);
		servletProperties.put("odata.model.packages", new String[] { shop.getNsURI() });
		servletProperties.put("odata.model.entitysets",
				new String[] { "Products=" + shop.getNsURI() + "#Product" });
		servlet.update(servletProperties);
		configurations.add(servlet);

		Configuration filter = configurationAdmin
				.createFactoryConfiguration("org.eclipse.fennec.odata.request.filter", "?");
		Hashtable<String, Object> filterProperties = new Hashtable<>();
		filterProperties.put("osgi.http.whiteboard.filter.pattern", "/atlas/shop/*");
		filterProperties.put("osgi.http.whiteboard.target", target);
		filterProperties.put("odata.max.expression.length", 16);
		filter.update(filterProperties);
		configurations.add(filter);

		awaitStatus(ROOT + "/Products", 200);
	}

	@AfterAll
	void tearDownStack() throws Exception {
		for (Configuration configuration : configurations) {
			configuration.delete();
		}
		packageRegistrations.forEach(ServiceRegistration::unregister);
		EPackage.Registry.INSTANCE.remove(shop.getNsURI());
		EPackage.Registry.INSTANCE.remove(foreign.getNsURI());
		ecoreHelper.releaseAll();
		// the unconfigured default root returns once no factory configuration is left
		awaitStatus(HOST + "/odata/", 200);
	}

	private static long httpServiceRuntimeId(BundleContext context) throws Exception {
		ServiceReference<?>[] runtimes = context
				.getServiceReferences("org.osgi.service.servlet.runtime.HttpServiceRuntime", null);
		assertTrue(runtimes != null && runtimes.length == 1, "exactly one HTTP runtime in this framework");
		return (Long) runtimes[0].getProperty("service.id");
	}

	private void writeDataFile() throws Exception {
		EClass productClass = EcoreHelper.getEClass(shop, "Product");
		EClass categoryClass = EcoreHelper.getEClass(shop, "Category");
		EObject dairy = shop.getEFactoryInstance().create(categoryClass);
		dairy.eSet(categoryClass.getEStructuralFeature("id"), "c1");
		dairy.eSet(categoryClass.getEStructuralFeature("name"), "Dairy");
		EObject milk = shop.getEFactoryInstance().create(productClass);
		milk.eSet(productClass.getEStructuralFeature("id"), "p1");
		milk.eSet(productClass.getEStructuralFeature("name"), "Milk");
		milk.eSet(productClass.getEStructuralFeature("price"), new BigDecimal("1.20"));
		milk.eSet(productClass.getEStructuralFeature("category"), dairy);

		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		rs.getPackageRegistry().put(shop.getNsURI(), shop);
		Resource resource = rs.createResource(org.eclipse.emf.common.util.URI
				.createFileURI(dataDirectory.resolve("shop.xmi").toString()));
		resource.getContents().addAll(List.of(dairy, milk));
		resource.save(null);
	}

	private void awaitStatus(String url, int expected) throws Exception {
		long deadline = System.currentTimeMillis() + 15_000;
		int last = -1;
		Exception failure = null;
		while (System.currentTimeMillis() < deadline) {
			try {
				last = get(url).statusCode();
				if (last == expected) {
					return;
				}
			} catch (Exception e) {
				failure = e;
			}
			Thread.sleep(200);
		}
		throw new IllegalStateException(url + " did not answer " + expected + " within 15s (last: "
				+ last + ")", failure);
	}

	private HttpResponse<String> get(String url) throws Exception {
		return client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
				HttpResponse.BodyHandlers.ofString());
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	@Test
	@Order(1)
	@DisplayName("the configured root serves at its pattern, on the targeted runtime")
	void servesAtConfiguredPattern() throws Exception {
		HttpResponse<String> products = get(ROOT + "/Products");
		assertEquals(200, products.statusCode(), products.body());
		assertTrue(products.body().contains("\"name\":\"Milk\""), products.body());
		assertTrue(products.body().contains("/atlas/shop/$metadata#Products\""),
				"links resolve against the configured service root: " + products.body());

		HttpResponse<String> one = get(ROOT + "/Products('p1')");
		assertEquals(200, one.statusCode(), one.body());
	}

	@Test
	@Order(2)
	@DisplayName("$metadata and the service document describe the allowlisted model only")
	void publishesOnlyTheAllowlistedModel() throws Exception {
		HttpResponse<String> serviceDoc = get(ROOT + "/");
		assertEquals(200, serviceDoc.statusCode(), serviceDoc.body());
		assertTrue(serviceDoc.body().contains("\"name\":\"Products\""), serviceDoc.body());
		assertFalse(serviceDoc.body().contains("Category") || serviceDoc.body().contains("Gadget"),
				"unlisted sets and foreign packages are absent: " + serviceDoc.body());

		HttpResponse<String> metadata = get(ROOT + "/$metadata");
		assertEquals(200, metadata.statusCode(), metadata.body());
		assertTrue(metadata.body().contains("Namespace=\"webshop\""), metadata.body());
		assertFalse(metadata.body().contains("clientshop") || metadata.body().contains("Gadget"),
				"the foreign package registered in the same framework is no schema here: " + metadata.body());
		assertTrue(metadata.body().contains("EntitySet EntityType=\"webshop.Product\" Name=\"Products\""), metadata.body());
		assertFalse(metadata.body().contains("EntityType=\"webshop.Category\" Name=\"Category\""),
				"the container holds only the published sets: " + metadata.body());
	}

	@Test
	@Order(3)
	@DisplayName("an entity set outside the allowlist is a 404, not a leak")
	void unpublishedSetsAreUnknown() throws Exception {
		assertEquals(404, get(ROOT + "/Product").statusCode(), "published under its configured name only");
		assertEquals(404, get(ROOT + "/Category").statusCode(), "not in the allowlist");
		assertEquals(404, get(ROOT + "/Gadget").statusCode(), "foreign package");
	}

	@Test
	@Order(4)
	@DisplayName("the filter configuration guards the configured root with its own limits")
	void filterInstanceGuardsTheRoot() throws Exception {
		HttpResponse<String> within = get(ROOT + "/Products?$filter=" + encode("price lt 3"));
		assertEquals(200, within.statusCode(), within.body());

		HttpResponse<String> beyond = get(ROOT + "/Products?$filter="
				+ encode("price lt 3 and name eq 'Milk'")); // > 16 chars: the filter's cap, not the servlet's 4096
		assertEquals(400, beyond.statusCode(), beyond.body());
		assertTrue(beyond.body().contains("\"error\""), beyond.body());
	}

	@Test
	@Order(5)
	@DisplayName("a configured root replaces the unconfigured default root")
	void defaultRootIsReplaced() throws Exception {
		assertEquals(404, get(HOST + "/odata/").statusCode(),
				"with factory configurations present there is no default /odata root");
	}
}
