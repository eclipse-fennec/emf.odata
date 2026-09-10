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
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.eclipse.fennec.persistence.orm.EntityMapper;
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
 * The repository backend (#79) end to end: a JPA persistence unit on H2, the emf.persistence-jpa
 * repository facade over it ({@code fennec.repository.jpa}), and the OData
 * {@code RepositoryQueryService} bound to that repository by {@code persistence.repository.id}.
 * Data is written through a plain {@code jpa://} resource, read back through the facade — as a
 * {@code QueryService} and over real HTTP with filter, ordering, paging, {@code $count} and
 * {@code $apply} pushed down.
 *
 * <p>Uses its own model ({@code reposhop.ecore}), unit and repository so it never interferes with
 * the command-backend test that shares the framework.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Repository backend: QueryService over the emf.persistence-jpa ReadRepository facade")
public class RepositoryBackendIntegrationTest {

	private static final String BASE = "http://127.0.0.1:18893/odata";
	private static final String ECORE = "/org/eclipse/fennec/odata/itests/reposhop.ecore";
	private static final String UNIT_NAME = "reposhop";
	private static final String REPOSITORY_ID = "reposhop";
	private static final String BACKEND_FILTER = "(fennec.odata.backend=repository)";

	private final HttpClient client = HttpClient.newHttpClient();
	private final ODataQueryParser parser = new ODataQueryParser();

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private EClass articleClass;
	private ServiceRegistration<EPackageConfigurator> configuratorRegistration;
	private ServiceRegistration<EPackage> packageRegistration;
	private final List<Configuration> configurations = new ArrayList<>();
	private Path workDirectory;

	@BeforeAll
	void setUpWiring(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin configurationAdmin,
			@InjectService(cardinality = 0) ServiceAware<ResourceSetFactory> resourceSets,
			@InjectService(cardinality = 0, filter = "(osgi.unit.name=" + UNIT_NAME + ")")
			ServiceAware<EntityManagerFactory> factoryAware) throws Exception {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(ECORE, RepositoryBackendIntegrationTest.class);
		pkg.eResource().setURI(URI.createURI(pkg.getNsURI()));
		articleClass = EcoreHelper.getEClass(pkg, "Article");
		EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
		workDirectory = Files.createTempDirectory("odata-repository-backend");
		Path mappingFile = writeMappingFile();
		registerModel(context);

		// the JPA unit on H2 (the same declarative wiring as the command backend test)
		Hashtable<String, Object> dataSourceProperties = new Hashtable<>();
		dataSourceProperties.put("identifier", workDirectory.resolve("h2/repository").toString());
		configure(configurationAdmin, "daanse.jdbc.datasource.h2.DataSource", dataSourceProperties);
		Hashtable<String, Object> unitProperties = new Hashtable<>();
		unitProperties.put("fennec.jpa.model", "(emf.name=" + pkg.getName() + ")");
		unitProperties.put("fennec.jpa.model.target", "(emf.name=" + pkg.getName() + ")");
		unitProperties.put("fennec.jpa.mappingFile", mappingFile.toUri().toString());
		unitProperties.put("fennec.jpa.persistenceUnitName", UNIT_NAME);
		unitProperties.put("fennec.jpa.ext.eclipselink.ddl-generation", "create-or-extend-tables");
		configure(configurationAdmin, "fennec.jpa.PersistenceUnit", unitProperties);

		// the repository facade over that unit ...
		Hashtable<String, Object> repositoryProperties = new Hashtable<>();
		repositoryProperties.put("repositoryId", REPOSITORY_ID);
		repositoryProperties.put("unit.target", "(osgi.unit.name=" + UNIT_NAME + ")");
		repositoryProperties.put("readOnly", Boolean.TRUE);
		configure(configurationAdmin, "fennec.repository.jpa", repositoryProperties);

		// ... and the OData backend bound to the facade by its id (#79)
		Hashtable<String, Object> backendProperties = new Hashtable<>();
		backendProperties.put("repository.target", "(persistence.repository.id=" + REPOSITORY_ID + ")");
		backendProperties.put("emf.nsURIs", pkg.getNsURI());
		// the in-memory reference backend (also deployed here) claims every keyed class of every
		// registered package — the servlet consults backends by service.ranking, so rank this one above it
		backendProperties.put("service.ranking", 10);
		configure(configurationAdmin, "org.eclipse.fennec.odata.persistence.repository", backendProperties);

		assertNotNull(factoryAware.waitForService(20_000), "the persistence unit must be up");
		seed(resourceSets.waitForService(5_000));
	}

	@AfterAll
	void tearDownWiring() throws Exception {
		for (Configuration configuration : configurations) {
			configuration.delete();
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

	private void configure(ConfigurationAdmin configurationAdmin, String factoryPid,
			Hashtable<String, Object> properties) throws Exception {
		Configuration configuration = configurationAdmin.createFactoryConfiguration(factoryPid, "?");
		configuration.update(properties);
		configurations.add(configuration);
	}

	/** Three articles, written through a plain jpa:// resource of the unit — not through OData. */
	private void seed(ResourceSetFactory resourceSets) throws Exception {
		assertNotNull(resourceSets, "a ResourceSetFactory must be available");
		ResourceSet resourceSet = resourceSets.createResourceSet();
		Resource resource = resourceSet.createResource(URI.createURI("jpa://" + UNIT_NAME + "/Article"));
		resource.getContents().add(article("r1", "Rope", "4.50", 12));
		resource.getContents().add(article("r2", "Lamp", "19.90", 3));
		resource.getContents().add(article("r3", "Nail", "0.10", 500));
		resource.save(null);
	}

	private EObject article(String id, String name, String price, int stock) {
		EObject article = pkg.getEFactoryInstance().create(articleClass);
		article.eSet(articleClass.getEStructuralFeature("id"), id);
		article.eSet(articleClass.getEStructuralFeature("name"), name);
		article.eSet(articleClass.getEStructuralFeature("price"), new BigDecimal(price));
		article.eSet(articleClass.getEStructuralFeature("stock"), stock);
		return article;
	}

	private HttpResponse<String> get(String pathAndQuery) throws Exception {
		return client.send(HttpRequest.newBuilder(java.net.URI.create(BASE + pathAndQuery)).GET().build(),
				HttpResponse.BodyHandlers.ofString());
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}

	@Test
	@Order(1)
	@DisplayName("the factory configuration yields a QueryService bound to the repository")
	void repositoryQueryServiceAppears(
			@InjectService(cardinality = 0, filter = BACKEND_FILTER) ServiceAware<QueryService> queryAware)
			throws Exception {
		QueryService queryService = queryAware.waitForService(20_000);
		assertNotNull(queryService, "the repository factory configuration should yield the QueryService");
		assertTrue(queryService.supports(articleClass));

		QueryResult cheap = queryService.execute(new EntityQuery(articleClass, null,
				parser.parseFilter("price lt 10", articleClass), parser.parseOrderBy("name asc", articleClass),
				0, 1, true));
		assertEquals(1, cheap.entities().size(), "top 1 of the two cheap articles");
		assertEquals("Nail", cheap.entities().get(0).eGet(articleClass.getEStructuralFeature("name")));
		assertEquals(2, cheap.totalCount(), "$count is total-before-paging, counted by the facade");
	}

	@Test
	@Order(2)
	@DisplayName("filter, ordering, paging and $count over HTTP are served through the facade")
	void readsOverHttp(
			@InjectService(cardinality = 0, filter = BACKEND_FILTER) ServiceAware<QueryService> queryAware)
			throws Exception {
		assertNotNull(queryAware.waitForService(20_000));

		HttpResponse<String> cheap = get("/Article?$filter=" + encode("price lt 10")
				+ "&$orderby=" + encode("price desc") + "&$count=true");
		assertEquals(200, cheap.statusCode(), cheap.body());
		assertTrue(cheap.body().indexOf("\"Rope\"") < cheap.body().indexOf("\"Nail\""),
				"ordered by price desc: " + cheap.body());
		assertFalse(cheap.body().contains("\"Lamp\""), cheap.body());
		assertTrue(cheap.body().contains("\"@odata.count\":2"), cheap.body());

		HttpResponse<String> one = get("/Article('r2')");
		assertEquals(200, one.statusCode(), one.body());
		assertTrue(one.body().contains("\"name\":\"Lamp\""), one.body());

		HttpResponse<String> missing = get("/Article('nope')");
		assertEquals(404, missing.statusCode(), missing.body());

		HttpResponse<String> page = get("/Article?$orderby=name&$top=2&$skip=1");
		assertEquals(200, page.statusCode(), page.body());
		assertTrue(page.body().contains("\"Nail\"") && page.body().contains("\"Rope\"")
				&& !page.body().contains("\"Lamp\""), "page 2 of 2 by name: " + page.body());
	}

	@Test
	@Order(3)
	@DisplayName("$apply runs as a pipeline on the facade")
	void applyOverHttp(
			@InjectService(cardinality = 0, filter = BACKEND_FILTER) ServiceAware<QueryService> queryAware)
			throws Exception {
		assertNotNull(queryAware.waitForService(20_000));

		HttpResponse<String> total = get("/Article?$apply=" + encode("aggregate(stock with sum as total)"));
		assertEquals(200, total.statusCode(), total.body());
		assertTrue(total.body().contains("\"total\":515"), "12 + 3 + 500: " + total.body());
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
