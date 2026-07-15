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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.EntityRepository;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.inmemory.InMemoryQueryService;
import org.eclipse.fennec.odata.persistence.inmemory.MemoryWriteRepository;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Differential parity: the SAME parsed OData query is answered by the JPA pushdown backend
 * (inherited H2/EclipseLink harness) AND the in-memory reference backend, over an IDENTICAL
 * dataset, and the two must return the same entities / aggregates. This is the guard the two
 * hand-authored, separately-fixtured suites could never give — it catches a translation that
 * silently returns the WRONG rows (or diverges between backends) with an HTTP 200.
 *
 * <p>The dataset is deliberately edge-heavy: a null price/category ({@code Salt}), a name with
 * SQL-LIKE metacharacters ({@code 50%_off}), a non-ASCII name ({@code Süßware}), and two rows
 * sharing a price (tie-break for ordering). No derived type is seeded here — polymorphic reads
 * and derived-type cast filters are covered by their own focused tests ({@code JpaDerivedCastTest},
 * {@code MemoryWriteRepositoryTest.polymorphicRead}); this suite keeps the dataset focused on the
 * comparison/function/aggregate corpus.
 */
@DisplayName("Differential parity: JPA pushdown vs in-memory over one dataset")
class DifferentialBackendTest extends JpaWebshopTestBase {

	private QueryService inMemory;
	private EAttribute id;

	/** The differential dataset — replaces the base webshop seed (no derived type). */
	@Override
	protected List<EObject> buildSeedData() {
		EEnum color = (EEnum) pkg.getEClassifier("Color");
		EObject dairy = instance("Category", "id", "c1", "name", "Dairy");
		EObject bakery = instance("Category", "id", "c2", "name", "Bakery");

		EObject milk = instance("Product", "id", "p1", "name", "Milk",
				"price", new BigDecimal("1.20"), "rating", 3, "active", true, "category", dairy,
				"color", color.getEEnumLiteral("Green"),
				"released", Date.from(Instant.parse("2024-05-03T10:15:30Z")));
		EObject cheese = instance("Product", "id", "p2", "name", "Cheese",
				"price", new BigDecimal("4.50"), "rating", 5, "active", true, "category", dairy);
		EObject r1 = instance("Review", "id", "r1", "stars", 5, "comment", "great");
		EObject r2 = instance("Review", "id", "r2", "stars", 4, "comment", "good");
		cheese.eSet(productClass.getEStructuralFeature("reviews"), List.of(r1, r2));
		EObject bread = instance("Product", "id", "p3", "name", "Bread",
				"price", new BigDecimal("2.80"), "rating", 4, "active", false, "category", bakery);
		EObject salt = instance("Product", "id", "p4", "name", "Salt"); // null price AND null category
		EObject off = instance("Product", "id", "p5", "name", "50%_off",
				"price", new BigDecimal("2.00"), "rating", 4, "active", true, "category", bakery);
		EObject suess = instance("Product", "id", "p6", "name", "Süßware",
				"price", new BigDecimal("2.00"), "rating", 2, "active", true, "category", dairy);

		return new ArrayList<>(List.of(dairy, bakery, milk, cheese, bread, salt, off, suess));
	}

	@BeforeEach
	void seedInMemoryBackend() {
		id = (EAttribute) productClass.getEStructuralFeature("id");
		MemoryWriteRepository repository = new MemoryWriteRepository();
		// addEPackage / addRepository are package-private DS bind methods — wire them reflectively
		// from this (foreign-package) test rather than widening production visibility
		bind(repository, "addEPackage", EPackage.class, pkg);
		for (EObject entity : buildSeedData()) { // a FRESH graph, independent of the JPA-persisted one
			if (productClass.isSuperTypeOf(entity.eClass())) {
				repository.create(entity.eClass(), entity); // referenced categories ride along as live refs
			}
		}
		InMemoryQueryService service = new InMemoryQueryService();
		bind(service, "addRepository", EntityRepository.class, repository);
		this.inMemory = service;
	}

	private static void bind(Object target, String method, Class<?> parameterType, Object argument) {
		try {
			var bind = target.getClass().getDeclaredMethod(method, parameterType);
			bind.setAccessible(true);
			bind.invoke(target, argument);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("cannot wire the in-memory backend for the test", e);
		}
	}

	@TestFactory
	@DisplayName("$filter / $orderby results are identical across both backends")
	Stream<DynamicTest> filterAndOrderParity() {
		record Case(String label, String filter, String orderBy, boolean ordered) {}
		List<Case> corpus = List.of(
				new Case("lt decimal", "price lt 3.00", null, false),
				new Case("ge decimal", "price ge 2.00", null, false),
				new Case("eq null", "price eq null", null, false),
				new Case("ne null", "price ne null", null, false),
				new Case("and", "rating gt 3 and price lt 3.00", null, false),
				new Case("or", "rating eq 5 or active eq false", null, false),
				new Case("in list", "rating in (4,5)", null, false),
				new Case("contains literal", "contains(name,'Milk')", null, false),
				new Case("contains LIKE-% literal", "contains(name,'%')", null, false),
				new Case("contains LIKE-_ literal", "contains(name,'_')", null, false),
				new Case("startswith %", "startswith(name,'50%')", null, false),
				new Case("endswith", "endswith(name,'ff')", null, false),
				new Case("contains unicode", "contains(name,'ß')", null, false),
				new Case("tolower", "tolower(name) eq 'milk'", null, false),
				new Case("toupper", "toupper(name) eq 'MILK'", null, false),
				new Case("length", "length(name) eq 4", null, false),
				new Case("substring", "substring(name,0,2) eq 'Ch'", null, false),
				new Case("indexof", "indexof(name,'ee') eq 2", null, false),
				new Case("concat", "concat(name,'!') eq 'Milk!'", null, false),
				new Case("year", "year(released) eq 2024", null, false),
				new Case("navigation eq", "category/name eq 'Dairy'", null, false),
				new Case("filtered count ge", "reviews/$count($filter=stars ge 4) ge 2", null, false),
				new Case("filtered count some", "reviews/$count($filter=stars ge 5) ge 1", null, false),
				new Case("filtered count none", "reviews/$count($filter=stars ge 4) lt 1", null, false),
				new Case("searched count", "reviews/$count($search=great) ge 1", null, false),
				new Case("enum by name", "color eq 'Green'", null, false),
				new Case("bool", "active eq false", null, false),
				new Case("3VL not over null", "not (price eq 5)", null, false),
				new Case("order price asc", null, "price asc,id asc", true),
				new Case("order price desc", null, "price desc,id asc", true),
				new Case("order name asc", null, "name asc", true),
				new Case("order rating desc + tie", null, "rating desc,price asc,id asc", true));
		return corpus.stream().map(c -> DynamicTest.dynamicTest(c.label(),
				() -> assertParity(c.filter(), c.orderBy(), c.ordered())));
	}

	@Test
	@DisplayName("$apply groupby with all aggregate methods is identical across both backends")
	void applyAggregateParity() {
		String apply = "groupby((category/name),aggregate("
				+ "price with sum as Total,price with min as Lo,price with max as Hi,"
				+ "price with average as Avg,rating with countdistinct as Ratings,$count as Cnt))";
		Map<String, Map<String, Object>> jpa = groups(runApply(service, apply));
		Map<String, Map<String, Object>> mem = groups(runApply(inMemory, apply));

		assertEquals(jpa.keySet(), mem.keySet(), "the grouping keys diverge between backends");
		for (String group : jpa.keySet()) {
			Map<String, Object> j = jpa.get(group);
			Map<String, Object> m = mem.get(group);
			assertEquals(num(j.get("Cnt")), num(m.get("Cnt")), group + " $count");
			assertEquals(num(j.get("Ratings")), num(m.get("Ratings")), group + " countdistinct");
			assertDecimalEquals(j.get("Total"), m.get("Total"), 0, group + " sum");
			assertDecimalEquals(j.get("Lo"), m.get("Lo"), 0, group + " min");
			assertDecimalEquals(j.get("Hi"), m.get("Hi"), 0, group + " max");
			// SQL AVG is double, in-memory is DECIMAL64 — equal in value within FP tolerance
			assertDecimalEquals(j.get("Avg"), m.get("Avg"), 1e-6, group + " average");
		}
	}

	@Test
	@DisplayName("$apply compute AFTER groupby is identical across both backends")
	void computeAfterGroupbyParity() {
		String apply = "groupby((category/name),aggregate(price with sum as Total,$count as Cnt))"
				+ "/compute(Total div Cnt as PerItem)";
		Map<String, Map<String, Object>> jpa = groups(runApply(service, apply));
		Map<String, Map<String, Object>> mem = groups(runApply(inMemory, apply));

		assertEquals(mem.keySet(), jpa.keySet(), "the grouping keys diverge between backends");
		for (String group : mem.keySet()) {
			// SQL divides decimal by bigint, in-memory divides DECIMAL64 — equal within FP tolerance;
			// the all-null price group divides null → null on both sides
			assertDecimalEquals(jpa.get(group).get("PerItem"), mem.get(group).get("PerItem"), 1e-6,
					group + " PerItem (compute after groupby)");
		}
	}

	// --- helpers ---

	private void assertParity(String filter, String orderBy, boolean ordered) {
		List<String> jpa = keys(service, filter, orderBy);
		List<String> mem = keys(inMemory, filter, orderBy);
		if (ordered) {
			assertEquals(jpa, mem, "ordered result diverges (orderby=" + orderBy + ")");
		} else {
			assertEquals(jpa.stream().sorted().toList(), mem.stream().sorted().toList(),
					"result set diverges (filter=" + filter + ")");
		}
	}

	private List<String> keys(QueryService service, String filter, String orderBy) {
		OclExpression predicate = filter == null ? null : parser.parseFilter(filter, productClass);
		List<OrderBySegment> order = orderBy == null ? List.of()
				: parser.parseOrderBy(orderBy, productClass);
		QueryResult result = service.execute(
				new EntityQuery(productClass, predicate, order, 0, -1, false));
		return result.entities().stream().map(e -> String.valueOf(e.eGet(id))).toList();
	}

	private List<Map<String, Object>> runApply(QueryService service, String apply) {
		ApplyPipeline pipeline = parser.parseApply(apply, productClass); // fresh parse per backend
		return service.executeApply(
				new ApplyQuery(productClass, pipeline, null, List.of(), 0, -1, false)).rows();
	}

	/** Rows keyed by their grouping value (category/name, or "∅" for the null-navigation group). */
	private static Map<String, Map<String, Object>> groups(List<Map<String, Object>> rows) {
		Map<String, Map<String, Object>> byGroup = new TreeMap<>();
		for (Map<String, Object> row : rows) {
			Object category = row.get("category");
			Object name = category instanceof Map<?, ?> map ? map.get("name") : null;
			Map<String, Object> flat = new LinkedHashMap<>(row);
			byGroup.put(name == null ? "∅" : String.valueOf(name), flat);
		}
		return byGroup;
	}

	private static long num(Object value) {
		return ((Number) value).longValue();
	}

	private static void assertDecimalEquals(Object a, Object b, double tolerance, String message) {
		if (a == null || b == null) {
			assertEquals(a, b, message + " (null aggregate)"); // both null (empty/all-null group) = equal
			return;
		}
		BigDecimal x = new BigDecimal(String.valueOf(a));
		BigDecimal y = new BigDecimal(String.valueOf(b));
		if (tolerance == 0) {
			assertEquals(0, x.compareTo(y), message + " (" + a + " vs " + b + ")");
		} else {
			assertTrue(x.subtract(y).abs().doubleValue() <= tolerance,
					message + " (" + a + " vs " + b + ")");
		}
	}
}
