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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.eclipse.fennec.odata.client.ODataClient;
import org.eclipse.fennec.odata.client.ODataClientException;
import org.eclipse.fennec.odata.client.ODataPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * E8 end to end: OUR client consumes OUR server over real HTTP inside the framework —
 * {@code $metadata} discovery → Ecore, fluent queries with server-side {@code $filter}/
 * {@code $orderby} pushdown, single-entity reads and error mapping. Uses its own model
 * ({@code clientshop.ecore}) and data directory, so it never interferes with the other
 * integration tests.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("OData client against the own server over HTTP")
public class ODataClientIntegrationTest {

	private static final String BASE = "http://127.0.0.1:18893/odata/";
	private static final String ECORE = "/org/eclipse/fennec/odata/itests/clientshop.ecore";

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private ServiceRegistration<EPackage> packageRegistration;
	private Configuration repositoryConfiguration;
	private Path dataDirectory;

	@BeforeAll
	void setUpStack(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin configurationAdmin) throws Exception {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(ECORE, ODataClientIntegrationTest.class);
		EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);

		dataDirectory = Files.createTempDirectory("odata-client-itest");
		writeDataFile();

		packageRegistration = context.registerService(EPackage.class, pkg, null);
		repositoryConfiguration = configurationAdmin
				.createFactoryConfiguration("org.eclipse.fennec.odata.repository.file", "?");
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

	@Test
	@DisplayName("full read round trip: discovery, filtered query, single entity, errors")
	void clientRoundTrip() {
		ODataClient client = ODataClient.connect(BASE);

		// discovery: the remote $metadata (ALL schemas of this server) parses into Ecore
		EClass gadget = client.entityType("Gadget");
		assertNotNull(gadget.getEStructuralFeature("price"));

		// server-side $filter/$orderby/$count through the client's fluent surface
		ODataPage cheap = client.entitySet("Gadget")
				.filter("price lt 5.00").orderBy("name asc").count().list();
		assertEquals(List.of("Cable", "Fuse"), cheap.entities().stream()
				.map(e -> e.eGet(gadget.getEStructuralFeature("name"))).toList());
		assertEquals(2, cheap.totalCount(), "@odata.count reaches the client");
		assertEquals(0, new BigDecimal("1.50").compareTo(
				(BigDecimal) cheap.entities().get(0).eGet(gadget.getEStructuralFeature("price"))),
				"Edm.Decimal survives the full wire round trip");

		// single entity by key
		EObject fuse = client.entitySet("Gadget").get("'g2'");
		assertEquals("Fuse", fuse.eGet(gadget.getEStructuralFeature("name")));
		assertEquals(7, fuse.eGet(gadget.getEStructuralFeature("stock")));

		// server-driven paging through the client
		ODataPage first = client.entitySet("Gadget").orderBy("name asc").top(2).list();
		assertEquals(2, first.entities().size());

		// /$count with filter
		assertEquals(3, client.entitySet("Gadget").totalCount());

		// error mapping: the server's OData error document surfaces with its status
		ODataClientException notFound = assertThrows(ODataClientException.class,
				() -> client.entitySet("Gadget").get("'nope'"));
		assertEquals(404, notFound.status());
		ODataClientException badFilter = assertThrows(ODataClientException.class,
				() -> client.entitySet("Gadget").filter("nosuch eq 1").list());
		assertEquals(400, badFilter.status(), "server-side parse errors surface as 400");
	}

	private void writeDataFile() throws Exception {
		EClass gadgetClass = EcoreHelper.getEClass(pkg, "Gadget");
		EObject cable = create(gadgetClass, "g1", "Cable", "1.50", 12);
		EObject fuse = create(gadgetClass, "g2", "Fuse", "0.80", 7);
		EObject drill = create(gadgetClass, "g3", "Drill", "89.00", 2);

		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		rs.getPackageRegistry().put(pkg.getNsURI(), pkg);
		Resource resource = rs.createResource(org.eclipse.emf.common.util.URI
				.createFileURI(dataDirectory.resolve("gadgets.xmi").toString()));
		resource.getContents().addAll(List.of(cable, fuse, drill));
		resource.save(null);
	}

	private EObject create(EClass type, String id, String name, String price, int stock) {
		EObject object = pkg.getEFactoryInstance().create(type);
		object.eSet(type.getEStructuralFeature("id"), id);
		object.eSet(type.getEStructuralFeature("name"), name);
		object.eSet(type.getEStructuralFeature("price"), new BigDecimal(price));
		object.eSet(type.getEStructuralFeature("stock"), stock);
		return object;
	}

	private void awaitReady() throws Exception {
		HttpClient http = HttpClient.newHttpClient();
		long deadline = System.currentTimeMillis() + 15_000;
		Exception last = null;
		while (System.currentTimeMillis() < deadline) {
			try {
				HttpResponse<String> response = http.send(
						HttpRequest.newBuilder(URI.create(BASE + "Gadget")).GET().build(),
						HttpResponse.BodyHandlers.ofString());
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
}
