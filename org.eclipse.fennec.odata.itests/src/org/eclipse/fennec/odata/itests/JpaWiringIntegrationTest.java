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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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
