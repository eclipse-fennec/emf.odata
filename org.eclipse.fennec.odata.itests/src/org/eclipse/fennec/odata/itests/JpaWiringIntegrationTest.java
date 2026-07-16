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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.eclipse.fennec.persistence.orm.EntityMapper;
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
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.persistence.EntityManagerFactory;

/**
 * The declarative wiring of the JPA backend, end to end INSIDE the OSGi framework: an
 * {@code EPackage} service plus two ConfigurationAdmin configurations (H2 DataSource,
 * {@code fennec.jpa.PersistenceUnit}) must materialize an {@link EntityManagerFactory}
 * service, which the {@code JpaQueryService} component picks up dynamically — no test
 * bootstraps the persistence unit by hand (that path is covered by the bundle tests).
 *
 * <p>Uses its own model ({@code wiringshop.ecore}) so it never competes with the in-memory
 * backend of {@link ODataHttpIntegrationTest} for the same entity types.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("JPA backend wiring: config → EntityManagerFactory → QueryService")
public class JpaWiringIntegrationTest {

	private static final String ECORE = "/org/eclipse/fennec/odata/itests/wiringshop.ecore";
	private static final String BASE = "http://127.0.0.1:18893/odata";
	private static final String DATASOURCE_PID = "daanse.jdbc.datasource.h2.DataSource";
	private static final String PERSISTENCE_UNIT_PID = "fennec.jpa.PersistenceUnit";
	private static final String UNIT_NAME = "wiringshop";

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private EClass itemClass;
	private ServiceRegistration<EPackageConfigurator> configuratorRegistration;
	private ServiceRegistration<EPackage> packageRegistration;
	private Configuration dataSourceConfiguration;
	private Configuration unitConfiguration;
	private Path workDirectory;
	private final HttpClient http = HttpClient.newHttpClient();

	@BeforeAll
	void setUpWiring(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin configurationAdmin) throws Exception {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(ECORE, JpaWiringIntegrationTest.class);
		// hrefs from the mapping file must serialize against the nsURI so the configurator's
		// ResourceSet resolves them through its package registry
		pkg.eResource().setURI(URI.createURI(pkg.getNsURI()));
		itemClass = EcoreHelper.getEClass(pkg, "Item");
		EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);

		workDirectory = Files.createTempDirectory("odata-jpa-wiring");
		Path mappingFile = writeMappingFile();

		registerModel(context);

		dataSourceConfiguration = configurationAdmin
				.createFactoryConfiguration(DATASOURCE_PID, "?");
		Hashtable<String, Object> dataSourceProperties = new Hashtable<>();
		dataSourceProperties.put("identifier", workDirectory.resolve("h2/wiring").toString());
		dataSourceConfiguration.update(dataSourceProperties);

		unitConfiguration = configurationAdmin
				.createFactoryConfiguration(PERSISTENCE_UNIT_PID, "?");
		Hashtable<String, Object> unitProperties = new Hashtable<>();
		unitProperties.put("fennec.jpa.model", "(emf.name=" + pkg.getName() + ")");
		unitProperties.put("fennec.jpa.model.target", "(emf.name=" + pkg.getName() + ")");
		unitProperties.put("fennec.jpa.mappingFile", mappingFile.toUri().toString());
		unitProperties.put("fennec.jpa.persistenceUnitName", UNIT_NAME);
		unitProperties.put("fennec.jpa.ext.eclipselink.ddl-generation", "create-or-extend-tables");
		unitConfiguration.update(unitProperties);
	}

	@AfterAll
	void tearDownWiring() throws Exception {
		if (unitConfiguration != null) {
			unitConfiguration.delete();
		}
		if (dataSourceConfiguration != null) {
			dataSourceConfiguration.delete();
		}
		if (packageRegistration != null) {
			packageRegistration.unregister();
		}
		if (configuratorRegistration != null) {
			configuratorRegistration.unregister();
		}
		EPackage.Registry.INSTANCE.remove(pkg.getNsURI());
		ecoreHelper.releaseAll();
	}

	@Test
	@DisplayName("the configurations materialize an EntityManagerFactory service")
	void entityManagerFactoryAppears(
			@InjectService(cardinality = 0, filter = "(osgi.unit.name=" + UNIT_NAME + ")")
			ServiceAware<EntityManagerFactory> factoryAware) throws Exception {
		assertNotNull(factoryAware.waitForService(20_000),
				"PersistenceUnit config + DataSource + EPackage should yield the factory service");
	}

	@Test
	@DisplayName("JpaQueryService binds the factory: write and read through the services")
	void queryServiceServesTheModel(
			@InjectService(cardinality = 0, filter = "(osgi.unit.name=" + UNIT_NAME + ")")
			ServiceAware<EntityManagerFactory> factoryAware,
			@InjectService(cardinality = 0, filter = "(fennec.odata.backend=jpa)")
			ServiceAware<QueryService> queryAware,
			@InjectService(cardinality = 0, filter = "(fennec.odata.backend=jpa)")
			ServiceAware<WriteService> writeAware) throws Exception {
		assertNotNull(factoryAware.waitForService(20_000));
		QueryService queryService = queryAware.waitForService(5_000);
		WriteService writeService = writeAware.waitForService(5_000);
		assertNotNull(queryService);
		assertNotNull(writeService);

		// the DYNAMIC factory reference binds after service activation — poll briefly
		long deadline = System.currentTimeMillis() + 10_000;
		while (!queryService.supports(itemClass) && System.currentTimeMillis() < deadline) {
			Thread.sleep(100);
		}
		assertTrue(queryService.supports(itemClass),
				"the JPA QueryService should pick up the new EntityManagerFactory");

		EObject item = pkg.getEFactoryInstance().create(itemClass);
		item.eSet(itemClass.getEStructuralFeature("id"), "i1");
		item.eSet(itemClass.getEStructuralFeature("name"), "Wire");
		item.eSet(itemClass.getEStructuralFeature("price"), new BigDecimal("9.99"));
		writeService.create(itemClass, item);

		var result = queryService.execute(EntityQuery.all(itemClass));
		assertEquals(1, result.entities().size());
		assertEquals("Wire", result.entities().get(0)
				.eGet(itemClass.getEStructuralFeature("name")));
	}

	@Test
	@DisplayName("end to end over HTTP: the ODataServlet serves the JPA backend (write + SQL query)")
	void httpEndToEndOverJpaBackend(
			@InjectService(cardinality = 0, filter = "(fennec.odata.backend=jpa)")
			ServiceAware<QueryService> queryAware) throws Exception {
		QueryService queryService = queryAware.waitForService(20_000);
		assertNotNull(queryService);
		long deadline = System.currentTimeMillis() + 15_000;
		while (!queryService.supports(itemClass) && System.currentTimeMillis() < deadline) {
			Thread.sleep(100);
		}
		assertTrue(queryService.supports(itemClass), "the JPA backend must be bound");
		awaitItemEndpoint();

		// WRITE over HTTP → JPA WriteService → H2 (distinct names so the assertions are independent
		// of any rows other tests created)
		assertEquals(201, post("/Item",
				"{\"id\":\"h1\",\"name\":\"HttpAlpha\",\"price\":5.00}").statusCode());
		assertEquals(201, post("/Item",
				"{\"id\":\"h2\",\"name\":\"HttpBeta\",\"price\":12.50}").statusCode());
		assertEquals(201, post("/Item",
				"{\"id\":\"h3\",\"name\":\"HttpGamma\",\"price\":1.00}").statusCode());

		// READ with $filter + $orderby + $count — all pushed down to SQL by the JPA backend
		HttpResponse<String> filtered = get("/Item?$filter="
				+ encode("startswith(name,'Http') and price gt 3.00")
				+ "&$orderby=" + encode("price desc") + "&$count=true");
		assertEquals(200, filtered.statusCode(), filtered.body());
		String body = filtered.body();
		assertTrue(body.contains("\"@odata.count\":2"), body);
		assertTrue(body.contains("HttpBeta") && body.contains("HttpAlpha"), body);
		assertFalse(body.contains("HttpGamma"), "price 1.00 is filtered out: " + body);
		assertTrue(body.indexOf("HttpBeta") < body.indexOf("HttpAlpha"),
				"12.50 must sort before 5.00 under price desc: " + body);

		// single entity by key
		HttpResponse<String> single = get("/Item('h1')");
		assertEquals(200, single.statusCode(), single.body());
		assertTrue(single.body().contains("HttpAlpha"), single.body());

		// $apply aggregate pushed down to SQL SUM over the filtered rows
		HttpResponse<String> aggregate = get("/Item?$apply="
				+ encode("filter(startswith(name,'Http'))/aggregate(price with sum as Total)"));
		assertEquals(200, aggregate.statusCode(), aggregate.body());
		assertTrue(aggregate.body().contains("18.5"),
				"SUM(5.00 + 12.50 + 1.00) = 18.50: " + aggregate.body());
	}

	private void awaitItemEndpoint() throws Exception {
		IllegalStateException last = null;
		for (int attempt = 0; attempt < 75; attempt++) {
			try {
				HttpResponse<String> metadata = get("/$metadata");
				if (metadata.statusCode() == 200 && metadata.body().contains("Item")) {
					return;
				}
				last = new IllegalStateException("$metadata status " + metadata.statusCode());
			} catch (Exception e) {
				last = new IllegalStateException(e);
			}
			Thread.sleep(200);
		}
		throw new IllegalStateException("the Item endpoint did not come up within 15s", last);
	}

	private HttpResponse<String> get(String pathAndQuery) throws Exception {
		// java.net.URI is fully qualified here: org.eclipse.emf.common.util.URI is imported for the
		// EMF wiring above, so the two cannot both be imported
		return http.send(HttpRequest.newBuilder(java.net.URI.create(BASE + pathAndQuery)).GET().build(),
				HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> post(String path, String jsonBody) throws Exception {
		return http.send(HttpRequest.newBuilder(java.net.URI.create(BASE + path))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8)).build(),
				HttpResponse.BodyHandlers.ofString());
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	private Path writeMappingFile() throws Exception {
		EntityMapper mapper = new EntityMapper();
		EntityMappings mappings = mapper.createMappings(new ArrayList<EClassifier>(
				pkg.getEClassifiers().stream().filter(EClass.class::isInstance).toList()));

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(pkg.getNsURI(), pkg);
		Path mappingFile = workDirectory.resolve("model.eorm");
		Resource resource = resourceSet.createResource(URI.createURI(mappingFile.toUri().toString()));
		resource.getContents().add(mappings);
		resource.save(null);
		return mappingFile;
	}

	private void registerModel(BundleContext context) {
		EPackageConfigurator configurator = new EPackageConfigurator() {

			@Override
			public void configureEPackage(EPackage.Registry registry) {
				registry.put(pkg.getNsURI(), pkg);
			}

			@Override
			public void unconfigureEPackage(EPackage.Registry registry) {
				registry.remove(pkg.getNsURI());
			}
		};
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put(EMFNamespaces.EMF_NAME, pkg.getName());
		properties.put(EMFNamespaces.EMF_MODEL_NSURI, pkg.getNsURI());
		properties.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_PROVIDED);
		properties.put(EMFNamespaces.EMF_MODEL_SCOPE, EMFNamespaces.EMF_MODEL_SCOPE_RESOURCE_SET);
		configuratorRegistration = context.registerService(EPackageConfigurator.class,
				configurator, properties);
		packageRegistration = context.registerService(EPackage.class, pkg, properties);
	}
}
