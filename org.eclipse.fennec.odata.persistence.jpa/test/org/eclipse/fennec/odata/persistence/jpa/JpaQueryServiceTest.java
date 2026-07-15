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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.query.apply.ApplyFactory;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E5 acceptance: the SAME queries the in-memory reference backend answers
 * ({@code InMemoryQueryServiceTest}) run against a REAL EclipseLink + H2 persistence unit
 * with dynamic EMF entities — differential testing of the Criteria pushdown. Harness and
 * fixture live in {@link JpaWebshopTestBase}.
 */
@DisplayName("JPA QueryService: OCL IR → Criteria pushdown against H2")
class JpaQueryServiceTest extends JpaWebshopTestBase {

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
		// negative substring start counts from the end ([OData-URL] 5.1.1.7 SHOULD)
		assertEquals(List.of("Milk", "SaleMilk"),
				names(query("substring(name,-4) eq 'Milk'", "name asc", 0, -1, false)));
		assertEquals(List.of("Milk", "SaleMilk"),
				names(query("substring(name,-3,2) eq 'il'", "name asc", 0, -1, false)));
		assertEquals(5, names(query("substring(name,-99) eq name", null, 0, -1, false)).size(),
				"negative start beyond the length clamps to the whole string");
	}

	@Test
	@DisplayName("date-part functions push down as EXTRACT (year/month/day/hour)")
	void datePartFunctions() {
		assertEquals(List.of("Milk"), names(query("year(released) eq 2024", null, 0, -1, false)));
		assertEquals(List.of("Cheese"), names(query("month(released) eq 11", null, 0, -1, false)));
		assertEquals(List.of("Cheese", "Milk"),
				names(query("year(released) ge 2023 and day(released) gt 1", "name asc", 0, -1, false)));
		assertEquals(List.of("Cheese", "Milk"),
				names(query("hour(released) ge 0 and hour(released) le 23", "name asc", 0, -1, false)),
				"time parts extract from the timestamp column (absolute values are timezone-"
						+ "dependent, so only sanity-check the range)");
	}

	@Test
	@DisplayName("rounding functions push down (round = half away from zero)")
	void roundingFunctions() {
		assertEquals(List.of("Milk"), names(query("floor(price) eq 1", null, 0, -1, false)),
				"1.20 floors to 1; 0.90 floors to 0");
		assertEquals(List.of("Bread"), names(query("ceiling(price) eq 3", null, 0, -1, false)),
				"2.80 ceils to 3");
		assertEquals(List.of("Milk", "SaleMilk"),
				names(query("round(price) eq 1", "name asc", 0, -1, false)),
				"1.20 and 0.90 both round to 1");
		assertEquals(List.of("Cheese"), names(query("round(price) gt 2 and rating eq 5", null, 0, -1, false)));
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
	@DisplayName("$apply compute: terminal rows with aliases, aliases usable in aggregates")
	void computePushdown() {
		// terminal compute: one row per entity, attributes + alias
		var doubled = parser.parseApply("compute(rating mul 2 as Doubled)", productClass);
		var rows = service.executeApply(new ApplyQuery(productClass, doubled,
				parser.parseFilterAfterApply("Doubled ge 8", productClass, doubled),
				parser.parseOrderByAfterApply("Doubled desc", productClass, doubled),
				0, -1, true));
		assertEquals(2, rows.rows().size(), "Cheese (10) and Bread (8): " + rows.rows());
		assertEquals("Cheese", rows.rows().get(0).get("name"), "attributes ride along");
		assertEquals(10, ((Number) rows.rows().get(0).get("Doubled")).intValue());
		assertEquals(2, rows.totalCount());

		// compute feeding a groupby aggregate
		var grouped = parser.parseApply(
				"compute(price mul 2 as DoublePrice)/groupby((category/name),aggregate(DoublePrice with sum as Total))",
				productClass);
		var groups = service.executeApply(
				new ApplyQuery(productClass, grouped, null, List.of(), 0, -1, false)).rows();
		var dairy = groups.stream()
				.filter(r -> "Dairy".equals(((Map<?, ?>) r.get("category")).get("name")))
				.findFirst().orElseThrow();
		assertEquals(0, new BigDecimal("13.20").compareTo(new BigDecimal(dairy.get("Total").toString())),
				"2 * (1.20 + 4.50 + 0.90): " + dairy);
	}

	@Test
	@DisplayName("no silent fallback: pipelines without a pushdown raise (→ 501)")
	void unsupportedConstructs() {
		ApplyPipeline empty = ApplyFactory.eINSTANCE.createApplyPipeline();
		assertThrows(UnsupportedOperationException.class,
				() -> service.executeApply(new ApplyQuery(
						productClass, empty, null, List.of(), 0, -1, false)),
				"no grouping/aggregation/compute stage");

		// rollup grouping sets have no portable Criteria pushdown (GROUPING SETS is not in the API)
		var rollup = parser.parseApply(
				"groupby((rollup(category/name,rating)),aggregate(price with sum as Total))",
				productClass);
		assertThrows(UnsupportedOperationException.class,
				() -> service.executeApply(new ApplyQuery(
						productClass, rollup, null, List.of(), 0, -1, false)),
				"rollup grouping sets have no JPA pushdown");
	}
}
