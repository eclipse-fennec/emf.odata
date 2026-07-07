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
package org.eclipse.fennec.odata.persistence.jpa;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.eclipse.fennec.persistence.api.ConverterService;
import org.eclipse.fennec.persistence.converter.DefaultConverterService;
import org.eclipse.fennec.persistence.eclipselink.dynamic.EDynamicHelper;
import org.eclipse.fennec.persistence.eclipselink.dynamic.EDynamicPersistenceUnitInfo;
import org.eclipse.fennec.persistence.eclipselink.dynamic.EDynamicType;
import org.eclipse.fennec.persistence.eclipselink.dynamic.EDynamicTypeGenerator;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.eclipse.fennec.persistence.epersistence.EPersistenceFactory;
import org.eclipse.fennec.persistence.epersistence.PersistenceUnit;
import org.eclipse.fennec.persistence.orm.EntityMapper;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.dynamic.DynamicClassLoader;
import org.eclipse.persistence.jpa.JpaHelper;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.eclipse.persistence.logging.AbstractSessionLog;
import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.eclipse.persistence.sessions.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * Shared H2 harness for the JPA backend tests: bootstraps a real EclipseLink persistence
 * unit with dynamic EMF entities from this bundle's {@code webshop.ecore} (Fennec Persistence
 * JPA non-OSGi pattern), persists the reference data set and installs a SELECT-counting
 * session log so tests can assert HOW MANY statements a read actually costs.
 *
 * <p>Fixture: Milk 1.20, Cheese 4.50 (rating 5, Green, reviews 5/4), Bread 2.80, Salt bare,
 * SaleMilk 0.90 (derived DiscountedProduct) — entity queries are polymorphic.
 */
abstract class JpaWebshopTestBase {

	protected ResourceSet resourceSet;
	protected EPackage pkg;
	protected EClass productClass;
	protected EClass discountedClass;
	protected EntityManagerFactory emf;
	protected Server serverSession;

	protected final ODataQueryParser parser = new ODataQueryParser();
	protected final JpaQueryService service = new JpaQueryService();

	private final SqlCountingLog sqlLog = new SqlCountingLog();

	@BeforeEach
	void setUpWebshop() throws Exception {
		resourceSet = new ResourceSetImpl();
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMIResourceFactoryImpl());
		pkg = loadEcore(findResource("testdata/webshop.ecore",
				"org.eclipse.fennec.odata.persistence.jpa/testdata/webshop.ecore"));
		productClass = (EClass) pkg.getEClassifier("Product");
		discountedClass = (EClass) pkg.getEClassifier("DiscountedProduct");

		bootstrapPersistence();
		persistWebshopData();
		service.addEntityManagerFactory(emf);

		// count every SELECT from here on — data setup must not pollute the numbers
		sqlLog.setLevel(SessionLog.FINE, SessionLog.SQL);
		serverSession.setSessionLog(sqlLog);
		resetSqlCount();
	}

	@AfterEach
	void tearDownWebshop() {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
	}

	// --- SQL statement counting ---

	/** Counting {@link SessionLog}: every logged SQL SELECT increments the counter. */
	private static final class SqlCountingLog extends AbstractSessionLog {

		private final AtomicInteger selects = new AtomicInteger();
		private final List<String> statements = new java.util.concurrent.CopyOnWriteArrayList<>();

		@Override
		public void log(SessionLogEntry entry) {
			if (SessionLog.SQL.equals(entry.getNameSpace()) && entry.getMessage() != null
					&& entry.getMessage().trim().regionMatches(true, 0, "SELECT", 0, 6)) {
				selects.incrementAndGet();
				statements.add(entry.getMessage().trim());
			}
		}
	}

	protected void resetSqlCount() {
		sqlLog.selects.set(0);
		sqlLog.statements.clear();
	}

	/** SELECT statements issued since the last {@link #resetSqlCount()}. */
	protected int selectCount() {
		return sqlLog.selects.get();
	}

	/** The recorded SELECT statements (diagnosis for count-assertion failures). */
	protected List<String> selectStatements() {
		return List.copyOf(sqlLog.statements);
	}

	// --- bootstrap (NonOsgiPersistenceTestBase pattern, trimmed) ---

	private EPackage loadEcore(Path ecorePath) throws Exception {
		File file = ecorePath.toFile();
		Resource resource = resourceSet.createResource(URI.createFileURI(file.getAbsolutePath()));
		resource.load(null);
		EPackage loaded = (EPackage) resource.getContents().get(0);
		resourceSet.getPackageRegistry().put(loaded.getNsURI(), loaded);
		return loaded;
	}

	private void bootstrapPersistence() throws Exception {
		EntityMapper mapper = new EntityMapper();
		List<EClassifier> classes = pkg.getEClassifiers().stream()
				.filter(EClass.class::isInstance).map(EClassifier.class::cast).toList();
		EntityMappings mappings = mapper.createMappings(new ArrayList<>(classes));

		DynamicClassLoader loader = new DynamicClassLoader(getClass().getClassLoader());
		Map<String, Object> props = new HashMap<>();
		props.put(PersistenceUnitProperties.DDL_GENERATION, "create-or-extend-tables");
		props.put(PersistenceUnitProperties.DDL_GENERATION_MODE, "database");
		props.put(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
		props.put(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:mem:odata_" + UUID.randomUUID());
		props.put(PersistenceUnitProperties.JDBC_USER, "sa");
		props.put(PersistenceUnitProperties.JDBC_PASSWORD, "");
		props.put(PersistenceUnitProperties.LOGGING_LEVEL, "WARNING");
		props.put(PersistenceUnitProperties.WEAVING, "false");
		props.put(PersistenceUnitProperties.TARGET_DATABASE, "Auto");
		props.put(PersistenceUnitProperties.TRANSACTION_TYPE, "RESOURCE_LOCAL");
		props.put(PersistenceUnitProperties.CLASSLOADER, loader);

		PersistenceUnit unit = EPersistenceFactory.eINSTANCE.createPersistenceUnit();
		unit.setName("webshop");
		unit.setProperties(EPersistenceFactory.eINSTANCE.createProperties());
		EDynamicPersistenceUnitInfo unitInfo = new EDynamicPersistenceUnitInfo(unit,
				getClass().getProtectionDomain().getCodeSource().getLocation(), props);

		emf = new PersistenceProvider().createContainerEntityManagerFactory(unitInfo, props);
		serverSession = JpaHelper.getServerSession(emf);

		ConverterService converter = new DefaultConverterService() {
		};
		EDynamicTypeGenerator generator = new EDynamicTypeGenerator(loader, serverSession,
				"webshop", converter);
		List<EDynamicType> types = generator.createFromMappings(List.of(mappings));
		new EDynamicHelper(emf, loader).addETypes(true, true, types);
	}

	private void persistWebshopData() {
		EEnum color = (EEnum) pkg.getEClassifier("Color");
		EObject food = instance("Category", "id", "c0", "name", "Food");
		EObject dairy = instance("Category", "id", "c1", "name", "Dairy", "parent", food);
		EObject bakery = instance("Category", "id", "c2", "name", "Bakery", "parent", food);

		EObject milk = instance("Product", "id", "p1", "name", "Milk",
				"price", new BigDecimal("1.20"), "rating", 3, "active", true, "category", dairy,
				"released", Date.from(Instant.parse("2024-05-03T10:15:30Z")));
		EObject cheese = instance("Product", "id", "p2", "name", "Cheese",
				"price", new BigDecimal("4.50"), "rating", 5, "active", true, "category", dairy,
				"color", color.getEEnumLiteral("Green"),
				"released", Date.from(Instant.parse("2023-11-20T08:00:00Z")));
		EObject reviewGreat = instance("Review", "id", "r1", "stars", 5, "comment", "great");
		EObject reviewGood = instance("Review", "id", "r2", "stars", 4, "comment", "good");
		cheese.eSet(productClass.getEStructuralFeature("reviews"), List.of(reviewGreat, reviewGood));
		EObject bread = instance("Product", "id", "p3", "name", "Bread",
				"price", new BigDecimal("2.80"), "rating", 4, "active", false, "category", bakery);
		EObject salt = instance("Product", "id", "p4", "name", "Salt");
		EObject sale = instance("DiscountedProduct", "id", "d1", "name", "SaleMilk",
				"price", new BigDecimal("0.90"), "discount", 20, "category", dairy);

		persist(food, dairy, bakery, milk, cheese, bread, salt, sale);
	}

	protected void persist(EObject... objects) {
		try (EntityManager em = emf.createEntityManager()) {
			em.getTransaction().begin();
			for (EObject object : objects) {
				em.persist(object);
			}
			em.getTransaction().commit();
		}
	}

	protected EObject instance(String className, Object... featureValuePairs) {
		ClassDescriptor descriptor = serverSession.getDescriptorForAlias(className);
		EObject object = (EObject) descriptor.getInstantiationPolicy().buildNewInstance();
		EClass eClass = (EClass) pkg.getEClassifier(className);
		for (int i = 0; i < featureValuePairs.length; i += 2) {
			object.eSet(eClass.getEStructuralFeature((String) featureValuePairs[i]),
					featureValuePairs[i + 1]);
		}
		return object;
	}

	protected QueryResult query(String filter, String orderBy, int skip, int top, boolean count) {
		return service.execute(new EntityQuery(productClass,
				filter == null ? null : parser.parseFilter(filter, productClass),
				orderBy == null ? List.of() : parser.parseOrderBy(orderBy, productClass),
				skip, top, count));
	}

	protected static List<String> names(QueryResult result) {
		return result.entities().stream()
				.map(e -> String.valueOf(e.eGet(e.eClass().getEStructuralFeature("name")))).toList();
	}

	protected static Path findResource(String... candidatesRelative) {
		Path start = Paths.get("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidatesRelative) {
				Path p = dir.resolve(rel);
				if (Files.exists(p)) {
					return p;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
