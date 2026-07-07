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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.eclipse.fennec.odata.query.apply.ApplyFactory;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
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
import org.eclipse.persistence.sessions.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * E5 acceptance: the SAME queries the in-memory reference backend answers
 * ({@code InMemoryQueryServiceTest}) run against a REAL EclipseLink + H2 persistence unit
 * with dynamic EMF entities — differential testing of the Criteria pushdown. Bootstrap
 * follows the Fennec Persistence JPA non-OSGi pattern ({@code NonOsgiPersistenceTestBase}).
 *
 * <p>Fixture note: this bundle's {@code webshop.ecore} gives {@code Review} an own id
 * attribute (JPA entities need a primary key) — semantically neutral for the queries.
 */
@DisplayName("JPA QueryService: OCL IR → Criteria pushdown against H2")
class JpaQueryServiceTest {

	private ResourceSet resourceSet;
	private EPackage pkg;
	private EClass productClass;
	private EClass discountedClass;
	private EntityManagerFactory emf;
	private Server serverSession;

	private final ODataQueryParser parser = new ODataQueryParser();
	private final JpaQueryService service = new JpaQueryService();

	@BeforeEach
	void setUp() throws Exception {
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
	}

	@AfterEach
	void tearDown() {
		if (emf != null && emf.isOpen()) {
			emf.close();
		}
	}

	// --- differential cases (mirroring InMemoryQueryServiceTest) ---

	// NOTE on the fixture: entity queries are polymorphic — the derived SaleMilk
	// (DiscountedProduct, 0.90, Dairy, no released date) is part of the Product set.

	@Test
	@DisplayName("filter + orderby + paging + count push down")
	void filterOrderPage() {
		assertEquals(List.of("SaleMilk", "Milk", "Bread"),
				names(query("price lt 3.00", "price asc", 0, -1, false)));
		assertEquals(List.of("Cheese", "Bread"), names(query("price gt 1.50", "price desc", 0, -1, false)));

		QueryResult page = query(null, "name asc", 1, 2, true);
		assertEquals(List.of("Cheese", "Milk"), names(page),
				"Bread,Cheese,Milk,SaleMilk,Salt → skip 1 top 2");
		assertEquals(5, page.totalCount(), "count is the total before paging");
	}

	@Test
	@DisplayName("navigation paths become joins; eq/ne null on navigations → IS NULL")
	void navigationPaths() {
		assertEquals(List.of("Cheese", "Milk", "SaleMilk"),
				names(query("category/name eq 'Dairy'", "name asc", 0, -1, false)));
		assertEquals(List.of("Salt"), names(query("category eq null", null, 0, -1, false)));
		assertEquals(4, names(query("category ne null", null, 0, -1, false)).size());
	}

	@Test
	@DisplayName("lambdas: any/all over containment collections → correlated EXISTS")
	void lambdas() {
		assertEquals(List.of("Cheese"), names(query("reviews/any(r: r/stars ge 5)", null, 0, -1, false)));
		List<String> allGood = names(query("reviews/all(r: r/stars ge 4)", null, 0, -1, false));
		assertTrue(allGood.contains("Cheese"), "both reviews are >= 4: " + allGood);
		assertTrue(allGood.containsAll(List.of("Milk", "Bread", "Salt")),
				"forAll over empty collections is true (like the evaluator): " + allGood);
	}

	@Test
	@DisplayName("functions, in-list, enum literals, divby, typed date literals")
	void richPredicates() {
		assertEquals(List.of("Cheese"), names(query("contains(name,'hees')", null, 0, -1, false)));
		assertEquals(List.of("SaleMilk", "Salt"),
				names(query("startswith(tolower(name),'sa')", "name asc", 0, -1, false)));
		assertEquals(List.of("Milk", "Salt"), names(query("name in ('Milk','Salt')", "name asc", 0, -1, false)));
		assertEquals(List.of("Cheese"), names(query("color eq 'Green'", null, 0, -1, false)),
				"enum literals compare by name");
		assertEquals(List.of("Milk", "SaleMilk"),
				names(query("price divby 2 lt 1.0", "name asc", 0, -1, false)),
				"0.60 and 0.45 are the halved prices below 1.00");
		assertEquals(List.of("Milk"), names(query("released ge 2024-01-01", null, 0, -1, false)),
				"typed date literal coerces to the column type");
		assertEquals(List.of("Bread", "Cheese", "SaleMilk"),
				names(query("length(name) gt 4", "name asc", 0, -1, false)));
	}

	@Test
	@DisplayName("derived-type cast pushes down as TYPE() restriction")
	void derivedTypeCast() {
		QueryResult cast = service.execute(new EntityQuery(productClass, discountedClass,
				null, List.of(), 0, -1, true));
		assertEquals(List.of("SaleMilk"), names(cast));
		assertEquals(1, cast.totalCount());

		QueryResult all = service.execute(EntityQuery.all(productClass));
		assertEquals(5, all.entities().size(), "polymorphic: the derived instance is in the base set");

		// filter on a derived property with the derived context
		QueryResult filtered = service.execute(new EntityQuery(productClass, discountedClass,
				parser.parseFilter("discount gt 10", discountedClass), List.of(), 0, -1, false));
		assertEquals(List.of("SaleMilk"), names(filtered));
	}

	@Test
	@SuppressWarnings("unchecked")
	@DisplayName("$apply pushdown: filter → WHERE, groupby(aggregate) → GROUP BY, alias filter → HAVING")
	void applyPushdown() {
		var pipeline = parser.parseApply(
				"filter(price ne null)/groupby((category/name),aggregate(price with sum as Total,$count as Cnt))",
				productClass);
		List<Map<String, Object>> rows = service.executeApply(
				new ApplyQuery(productClass, pipeline, null, List.of(), 0, -1, false)).rows();

		assertEquals(2, rows.size(), "Salt (null price) is filtered before grouping: " + rows);
		Map<String, Object> dairy = rows.stream()
				.filter(r -> "Dairy".equals(((Map<String, Object>) r.get("category")).get("name")))
				.findFirst().orElseThrow();
		assertEquals(0, new BigDecimal("6.60").compareTo(new BigDecimal(dairy.get("Total").toString())),
				"Milk 1.20 + Cheese 4.50 + SaleMilk 0.90: " + dairy);
		assertEquals(3L, ((Number) dairy.get("Cnt")).longValue());

		// alias usable in a subsequent filter stage → HAVING
		var filtered = parser.parseApply(
				"groupby((category/name),aggregate(price with sum as Total))/filter(Total gt 3.00)",
				productClass);
		assertEquals(1, service.executeApply(
				new ApplyQuery(productClass, filtered, null, List.of(), 0, -1, false)).rows().size(),
				"only Dairy exceeds 3.00");

		// post-pipeline options: row filter with alias + orderby + paging + count
		var grouped = parser.parseApply(
				"groupby((category/name),aggregate(price with sum as Total))", productClass);
		var post = service.executeApply(new ApplyQuery(productClass, grouped,
				parser.parseFilterAfterApply("Total gt 1.00", productClass, grouped),
				parser.parseOrderByAfterApply("Total desc", productClass, grouped), 0, 1, true));
		assertEquals(1, post.rows().size(), "top 1 after ordering");
		assertEquals("Dairy",
				((Map<String, Object>) post.rows().get(0).get("category")).get("name"));
		assertEquals(2, post.totalCount(), "Dairy + Bakery match the row filter (before paging)");
	}

	@Test
	@DisplayName("$apply pushdown: null navigations form their own group; ungrouped aggregate")
	void applyNullGroupAndUngrouped() {
		var groups = parser.parseApply("groupby((category/name),aggregate($count as Cnt))",
				productClass);
		var rows = service.executeApply(
				new ApplyQuery(productClass, groups, null, List.of(), 0, -1, false)).rows();
		assertEquals(3, rows.size(), "Dairy, Bakery and the null-category group (Salt): " + rows);

		var average = parser.parseApply("aggregate(price with average as AvgPrice)", productClass);
		var row = service.executeApply(
				new ApplyQuery(productClass, average, null, List.of(), 0, -1, true));
		assertEquals(1, row.rows().size());
		assertEquals(1, row.totalCount(), "an ungrouped aggregate is always one row");
		assertEquals(2.35, ((Number) row.rows().get(0).get("AvgPrice")).doubleValue(), 0.0001,
				"AVG ignores Salt's null price: 9.40 / 4");
	}

	@Test
	@DisplayName("no silent fallback: pipelines without a pushdown raise (→ 501)")
	void unsupportedConstructs() {
		ApplyPipeline empty = ApplyFactory.eINSTANCE.createApplyPipeline();
		assertThrows(UnsupportedOperationException.class,
				() -> service.executeApply(new ApplyQuery(
						productClass, empty, null, List.of(), 0, -1, false)),
				"no grouping/aggregation stage");

		var compute = parser.parseApply("compute(rating mul 2 as Doubled)", productClass);
		assertThrows(UnsupportedOperationException.class,
				() -> service.executeApply(new ApplyQuery(
						productClass, compute, null, List.of(), 0, -1, false)),
				"compute stages have no pushdown yet");
	}

	// --- harness (NonOsgiPersistenceTestBase pattern, trimmed to what this test needs) ---

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

	/** Milk 1.20, Cheese 4.50 (rating 5, Green, reviews 5/4), Bread 2.80, Salt bare, SaleMilk derived. */
	private void persistWebshopData() {
		EEnum color = (EEnum) pkg.getEClassifier("Color");
		EObject dairy = instance("Category", "id", "c1", "name", "Dairy");
		EObject bakery = instance("Category", "id", "c2", "name", "Bakery");

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

		try (EntityManager em = emf.createEntityManager()) {
			em.getTransaction().begin();
			for (EObject object : List.of(dairy, bakery, milk, cheese, bread, salt, sale)) {
				em.persist(object);
			}
			em.getTransaction().commit();
		}
	}

	private EObject instance(String className, Object... featureValuePairs) {
		ClassDescriptor descriptor = serverSession.getDescriptorForAlias(className);
		EObject object = (EObject) descriptor.getInstantiationPolicy().buildNewInstance();
		EClass eClass = (EClass) pkg.getEClassifier(className);
		for (int i = 0; i < featureValuePairs.length; i += 2) {
			object.eSet(eClass.getEStructuralFeature((String) featureValuePairs[i]),
					featureValuePairs[i + 1]);
		}
		return object;
	}

	private QueryResult query(String filter, String orderBy, int skip, int top, boolean count) {
		return service.execute(new EntityQuery(productClass,
				filter == null ? null : parser.parseFilter(filter, productClass),
				orderBy == null ? List.of() : parser.parseOrderBy(orderBy, productClass),
				skip, top, count));
	}

	private static List<String> names(QueryResult result) {
		return result.entities().stream()
				.map(e -> String.valueOf(e.eGet(e.eClass().getEStructuralFeature("name")))).toList();
	}

	private static Path findResource(String... candidatesRelative) {
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
