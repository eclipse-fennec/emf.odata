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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.odata.persistence.api.WriteService.WriteResult;
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
 * The backend-neutral command write path, end to end INSIDE the OSGi framework: an
 * {@code EPackage} service, the H2 DataSource + {@code fennec.jpa.PersistenceUnit}
 * configurations AND a {@code org.eclipse.fennec.odata.persistence.command} factory
 * configuration must materialize a {@link WriteService} with
 * {@code fennec.odata.backend=command} that writes through
 * {@code CommandResource.execute(...)} on {@code jpa://commandshop/Item} resources —
 * no test touches an EntityManager, everything flows through the persistence SPI.
 *
 * <p>Uses its own model ({@code commandshop.ecore}) and its own persistence unit so it
 * never competes with {@link JpaWiringIntegrationTest} or the in-memory backend for
 * entity types.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Command backend wiring: config → JPAUnit → CommandResource → WriteService")
public class CommandBackendIntegrationTest {

	private static final String ECORE = "/org/eclipse/fennec/odata/itests/commandshop.ecore";
	private static final String DATASOURCE_PID = "daanse.jdbc.datasource.h2.DataSource";
	private static final String PERSISTENCE_UNIT_PID = "fennec.jpa.PersistenceUnit";
	private static final String COMMAND_PID = "org.eclipse.fennec.odata.persistence.command";
	private static final String UNIT_NAME = "commandshop";

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private EClass itemClass;
	private ServiceRegistration<EPackageConfigurator> configuratorRegistration;
	private ServiceRegistration<EPackage> packageRegistration;
	private Configuration dataSourceConfiguration;
	private Configuration unitConfiguration;
	private Configuration commandConfiguration;
	private Path workDirectory;

	@BeforeAll
	void setUpWiring(@InjectBundleContext BundleContext context,
			@InjectService ConfigurationAdmin configurationAdmin) throws Exception {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(ECORE, CommandBackendIntegrationTest.class);
		pkg.eResource().setURI(URI.createURI(pkg.getNsURI()));
		itemClass = EcoreHelper.getEClass(pkg, "Item");
		EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);

		workDirectory = Files.createTempDirectory("odata-command-backend");
		Path mappingFile = writeMappingFile();

		registerModel(context);

		dataSourceConfiguration = configurationAdmin
				.createFactoryConfiguration(DATASOURCE_PID, "?");
		Hashtable<String, Object> dataSourceProperties = new Hashtable<>();
		dataSourceProperties.put("identifier", workDirectory.resolve("h2/command").toString());
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

		commandConfiguration = configurationAdmin
				.createFactoryConfiguration(COMMAND_PID, "?");
		Hashtable<String, Object> commandProperties = new Hashtable<>();
		commandProperties.put("backend.uri", "jpa://" + UNIT_NAME);
		commandProperties.put("emf.nsURIs", pkg.getNsURI());
		commandConfiguration.update(commandProperties);
	}

	@AfterAll
	void tearDownWiring() throws Exception {
		if (commandConfiguration != null) {
			commandConfiguration.delete();
		}
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
	@DisplayName("the factory configuration materializes the command WriteService")
	void commandWriteServiceAppears(
			@InjectService(cardinality = 0, filter = "(fennec.odata.backend=command)")
			ServiceAware<WriteService> writeAware) throws Exception {
		WriteService writeService = writeAware.waitForService(20_000);
		assertNotNull(writeService,
				"the command factory configuration should yield the WriteService");
		assertTrue(writeService.supports(itemClass));
		assertFalse(writeService.transactional(),
				"per-command transactions cannot join a $batch group");
	}

	@Test
	@DisplayName("CRUD round trip through Insert/Update/DeleteCommand on the JPA unit")
	void crudRoundTripOverTheCommandSpi(
			@InjectService(cardinality = 0, filter = "(osgi.unit.name=" + UNIT_NAME + ")")
			ServiceAware<EntityManagerFactory> factoryAware,
			@InjectService(cardinality = 0, filter = "(fennec.odata.backend=command)")
			ServiceAware<WriteService> writeAware) throws Exception {
		assertNotNull(factoryAware.waitForService(20_000),
				"the persistence unit must be up before commands can execute");
		WriteService writeService = writeAware.waitForService(5_000);
		assertNotNull(writeService);

		// POST → InsertCommand (existence pre-check → 409 on the second attempt)
		EObject item = pkg.getEFactoryInstance().create(itemClass);
		item.eSet(itemClass.getEStructuralFeature("id"), "c1");
		item.eSet(itemClass.getEStructuralFeature("name"), "Command");
		item.eSet(itemClass.getEStructuralFeature("price"), new BigDecimal("9.99"));
		EObject created = writeService.create(itemClass, item);
		assertEquals("Command", created.eGet(itemClass.getEStructuralFeature("name")));
		assertThrows(WriteConflictException.class, () -> writeService.create(itemClass, item));

		// PATCH → UpdateCommand with a SET template; untouched attributes survive
		EObject patch = pkg.getEFactoryInstance().create(itemClass);
		patch.eSet(itemClass.getEStructuralFeature("name"), "Command v2");
		WriteResult patched = writeService.update(itemClass, "'c1'", patch, false);
		assertFalse(patched.created());
		assertEquals("Command v2", patched.entity().eGet(itemClass.getEStructuralFeature("name")));
		assertEquals(0, new BigDecimal("9.99").compareTo(
				(BigDecimal) patched.entity().eGet(itemClass.getEStructuralFeature("price"))));

		// PUT → UNSET for omitted attributes
		EObject replacement = pkg.getEFactoryInstance().create(itemClass);
		replacement.eSet(itemClass.getEStructuralFeature("name"), "Command v3");
		WriteResult replaced = writeService.update(itemClass, "'c1'", replacement, true);
		assertFalse(replaced.created());
		assertEquals("Command v3", replaced.entity().eGet(itemClass.getEStructuralFeature("name")));
		assertNull(replaced.entity().eGet(itemClass.getEStructuralFeature("price")),
				"PUT must clear the omitted price");

		// upsert: unknown key inserts with the URL key
		EObject upsert = pkg.getEFactoryInstance().create(itemClass);
		upsert.eSet(itemClass.getEStructuralFeature("name"), "Upserted");
		WriteResult upserted = writeService.update(itemClass, "'c2'", upsert, false);
		assertTrue(upserted.created());
		assertEquals("c2", upserted.entity().eGet(itemClass.getEStructuralFeature("id")));

		// reference operations are honest 501s
		assertThrows(UnsupportedOperationException.class,
				() -> writeService.link(itemClass, "'c1'", "whatever", "'c2'"));

		// DELETE → DeleteCommand; the second attempt reports the miss
		assertTrue(writeService.delete(itemClass, "'c1'"));
		assertTrue(writeService.delete(itemClass, "'c2'"));
		assertFalse(writeService.delete(itemClass, "'c1'"));
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
