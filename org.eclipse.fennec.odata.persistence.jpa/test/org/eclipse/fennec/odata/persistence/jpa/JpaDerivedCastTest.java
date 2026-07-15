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

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Derived-type cast segments in a {@code $filter} path ({@code Ns.SubType/prop}) push down to
 * {@code treat()}. Asserted directly against the polymorphic JPA backend over the base fixture
 * (SaleMilk {@code d1} is a {@code DiscountedProduct} with discount 20; the other products are
 * plain {@code Product}s) — the in-memory {@code MemoryWriteRepository} is not subtype-aware for
 * base queries, so it cannot serve as the differential reference here (the cast semantics of the
 * IR itself are covered by the in-memory {@code OclEvaluator} unit tests).
 *
 * <p>The OR probe is the load-bearing correctness check: {@code treat()} must yield null for a
 * non-derived row (3VL exclusion of that disjunct), NOT restrict the whole query to the subtype —
 * otherwise a plain {@code Product} satisfying the other disjunct would be wrongly dropped.
 */
@DisplayName("Derived-type cast in $filter → treat() pushdown")
class JpaDerivedCastTest extends JpaWebshopTestBase {

	@TestFactory
	@DisplayName("cast-filter results match OData derived-type semantics")
	Stream<DynamicTest> castFilter() {
		record Case(String label, String filter, List<String> expected) {}
		List<Case> corpus = List.of(
				new Case("subtype attribute ge", "webshop.DiscountedProduct/discount ge 10",
						List.of("d1")),
				new Case("subtype attribute eq", "webshop.DiscountedProduct/discount eq 20",
						List.of("d1")),
				new Case("subtype attribute no match", "webshop.DiscountedProduct/discount ge 100",
						List.of()),
				new Case("inherited attribute via cast", "webshop.DiscountedProduct/name eq 'SaleMilk'",
						List.of("d1")),
				// OR: SaleMilk via the cast, Cheese (4.50, a plain Product) via price — the
				// plain Product MUST survive despite the treat() on the other disjunct
				new Case("cast OR plain-product predicate",
						"webshop.DiscountedProduct/discount ge 10 or price gt 4.00",
						List.of("d1", "p2")),
				// OR where only the non-cast disjunct matches — the plain Product must not be
				// dropped by an over-eager type restriction
				new Case("cast no-match OR keeps plain match",
						"webshop.DiscountedProduct/discount ge 100 or name eq 'Milk'",
						List.of("p1")));
		return corpus.stream().map(c -> DynamicTest.dynamicTest(c.label(),
				() -> assertEquals(c.expected().stream().sorted().toList(),
						keys(c.filter()), "filter=" + c.filter())));
	}

	private List<String> keys(String filter) {
		EAttribute id = (EAttribute) productClass.getEStructuralFeature("id");
		OclExpression predicate = parser.parseFilter(filter, productClass);
		QueryResult result = service.execute(
				new EntityQuery(productClass, predicate, List.of(), 0, -1, false));
		return result.entities().stream().map(e -> String.valueOf(e.eGet(id))).sorted().toList();
	}
}
