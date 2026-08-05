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
package org.eclipse.fennec.odata.example.jpa;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.eclipse.fennec.persistence.orm.EntityMapper;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Turns the in-memory example's shop model into a REAL JPA-backed OData service, entirely by
 * declarative wiring (mirrors {@code JpaWiringIntegrationTest}). On activation it:
 * <ol>
 *   <li>derives the entity mapping from the (already properly registered) {@code webshop}
 *       {@link EPackage} via {@link EntityMapper} and writes it as an {@code .eorm} file into
 *       this bundle's data area,</li>
 *   <li>creates two ConfigurationAdmin factory configurations — an H2 {@code DataSource}
 *       ({@value #DATASOURCE_PID}) and a {@code PersistenceUnit} ({@value #PERSISTENCE_UNIT_PID})
 *       selecting the model by {@code (emf.name=webshop)} — which together materialize an
 *       {@code EntityManagerFactory} the command backend executes against, and</li>
 *   <li>seeds a little demo data once the JPA backend is up.</li>
 * </ol>
 *
 * <p>The mapping file is a RUNTIME artifact (its URI is a concrete path under the bundle data
 * area), which is why the PersistenceUnit cannot be a static Configurator resource the way the
 * servlet limits are — the model must be turned into a mapping first, here, in code.
 *
 * <p>The seed runs entirely through the OData {@code WriteService}: categories first, then
 * products whose {@code category} member is a key-only stub — the write path binds each
 * non-containment reference to the EXISTING category row by its key. Reviews carry their own
 * {@code id} (the write path requires a key per created entity) and ride along as containment
 * children of Cheese.
 */
@Component(immediate = true)
public class ShopJpaBackendComponent {

	private static final System.Logger LOGGER = System.getLogger(ShopJpaBackendComponent.class.getName());

	/** Fennec Persistence JPA H2 DataSource factory PID (property {@code identifier} = db path). */
	static final String DATASOURCE_PID = "daanse.jdbc.datasource.h2.DataSource";
	/** Fennec Persistence JPA PersistenceUnit factory PID. */
	static final String PERSISTENCE_UNIT_PID = "fennec.jpa.PersistenceUnit";
	static final String UNIT_NAME = "webshop";

	/** The properly-registered example model (see {@code ShopExampleComponent}). */
	@Reference(target = "(emf.name=webshop)")
	private EPackage shop;

	@Reference
	private ConfigurationAdmin configurationAdmin;

	/**
	 * The JPA backend services appear only AFTER our configs materialize the
	 * {@code EntityManagerFactory}, so they are dynamic and optional; the seed thread waits for
	 * them.
	 */
	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL,
			target = "(fennec.odata.backend=command)")
	private volatile QueryService jpaQuery;
	@Reference(policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL,
			target = "(fennec.odata.backend=command)")
	private volatile WriteService jpaWrite;

	private Configuration dataSourceConfiguration;
	private Configuration unitConfiguration;
	private Configuration commandConfiguration;
	private volatile boolean active;
	private volatile Thread seedThread;

	@Activate
	void activate(BundleContext context) throws Exception {
		active = true;
		Path workDirectory = context.getDataFile("jpa").toPath();
		Files.createDirectories(workDirectory);
		Path mappingFile = workDirectory.resolve("webshop.eorm");
		writeMappingFile(mappingFile);

		dataSourceConfiguration = configurationAdmin.createFactoryConfiguration(DATASOURCE_PID, "?");
		Dictionary<String, Object> dataSourceProperties = new Hashtable<>();
		dataSourceProperties.put("identifier", workDirectory.resolve("h2/webshop").toString());
		dataSourceConfiguration.update(dataSourceProperties);

		unitConfiguration = configurationAdmin.createFactoryConfiguration(PERSISTENCE_UNIT_PID, "?");
		Dictionary<String, Object> unitProperties = new Hashtable<>();
		unitProperties.put("fennec.jpa.model", "(emf.name=" + shop.getName() + ")");
		unitProperties.put("fennec.jpa.model.target", "(emf.name=" + shop.getName() + ")");
		unitProperties.put("fennec.jpa.mappingFile", mappingFile.toUri().toString());
		unitProperties.put("fennec.jpa.persistenceUnitName", UNIT_NAME);
		unitProperties.put("fennec.jpa.ext.eclipselink.ddl-generation", "create-or-extend-tables");
		unitConfiguration.update(unitProperties);

		commandConfiguration = configurationAdmin
				.createFactoryConfiguration("org.eclipse.fennec.odata.persistence.command", "?");
		Dictionary<String, Object> commandProperties = new Hashtable<>();
		commandProperties.put("backend.uri", "jpa://" + UNIT_NAME);
		commandProperties.put("emf.nsURIs", shop.getNsURI());
		commandConfiguration.update(commandProperties);

		seedThread = Thread.ofVirtual().name("webshop-jpa-seed").start(this::seedWhenReady);
		LOGGER.log(INFO, "webshop JPA backend configured (unit={0}); seeding asynchronously", UNIT_NAME);
	}

	@Deactivate
	void deactivate() throws Exception {
		active = false;
		Thread thread = seedThread;
		if (thread != null) {
			thread.interrupt();
		}
		if (commandConfiguration != null) {
			commandConfiguration.delete();
		}
		if (unitConfiguration != null) {
			unitConfiguration.delete();
		}
		if (dataSourceConfiguration != null) {
			dataSourceConfiguration.delete();
		}
	}

	/** Derives the ORM mapping from the model and serializes it as the {@code .eorm} file. */
	private void writeMappingFile(Path mappingFile) throws Exception {
		EntityMapper mapper = new EntityMapper();
		EntityMappings mappings = mapper.createMappings(new ArrayList<EClassifier>(
				shop.getEClassifiers().stream().filter(EClass.class::isInstance).toList()));

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(shop.getNsURI(), shop);
		Resource resource = resourceSet.createResource(URI.createURI(mappingFile.toUri().toString()));
		resource.getContents().add(mappings);
		resource.save(null);
	}

	private void seedWhenReady() {
		try {
			EClass categoryClass = (EClass) shop.getEClassifier("Category");
			EClass productClass = (EClass) shop.getEClassifier("Product");
			EClass reviewClass = (EClass) shop.getEClassifier("Review");
			while (active && !Thread.currentThread().isInterrupted()) {
				QueryService query = jpaQuery;
				WriteService write = jpaWrite;
				if (query != null && write != null && query.supports(productClass)) {
					if (!query.execute(EntityQuery.all(productClass)).entities().isEmpty()) {
						LOGGER.log(INFO, "webshop already holds data — skipping demo seed");
						return;
					}
					seed(write, categoryClass, productClass, reviewClass);
					LOGGER.log(INFO, "seeded webshop demo data into the JPA backend");
					return;
				}
				Thread.sleep(200);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (RuntimeException e) {
			LOGGER.log(ERROR, "failed to seed webshop demo data", e);
		}
	}

	private void seed(WriteService write, EClass categoryClass, EClass productClass, EClass reviewClass) {
		// Categories first: Product.category is a NON-containment reference — the write path
		// binds each payload member to the EXISTING category row by its key.
		write.create(categoryClass, category(categoryClass, "c1", "Dairy"));
		write.create(categoryClass, category(categoryClass, "c2", "Bakery"));
		write.create(productClass, categorized(
				product(productClass, "p1", "Milk", "1.20", 3, true), productClass, categoryClass, "c1"));
		EObject cheese = categorized(
				product(productClass, "p2", "Cheese", "4.50", 5, true), productClass, categoryClass, "c1");
		// reviews is a CONTAINMENT reference — it rides along and cascades to the REVIEW table
		cheese.eSet(productClass.getEStructuralFeature("reviews"), List.of(
				review(reviewClass, "r1", 5, "great with wine"),
				review(reviewClass, "r2", 4, "a bit pricey")));
		write.create(productClass, cheese);
		write.create(productClass, categorized(
				product(productClass, "p3", "Bread", "2.80", 4, false), productClass, categoryClass, "c2"));
	}

	/** Sets a key-only category stub — the write path resolves it to the persisted row. */
	private EObject categorized(EObject product, EClass productClass, EClass categoryClass,
			String categoryId) {
		product.eSet(productClass.getEStructuralFeature("category"),
				create(categoryClass, Map.of("id", categoryId)));
		return product;
	}

	private EObject review(EClass reviewClass, String id, int stars, String comment) {
		return create(reviewClass, Map.of("id", id, "stars", stars, "comment", comment));
	}

	private EObject category(EClass categoryClass, String id, String name) {
		return create(categoryClass, Map.of("id", id, "name", name));
	}

	private EObject product(EClass productClass, String id, String name, String price, int rating,
			boolean active) {
		return create(productClass, Map.of("id", id, "name", name, "price", new BigDecimal(price),
				"rating", rating, "active", active));
	}

	private EObject create(EClass type, Map<String, Object> values) {
		EObject object = shop.getEFactoryInstance().create(type);
		values.forEach((feature, value) -> object.eSet(type.getEStructuralFeature(feature), value));
		return object;
	}
}
