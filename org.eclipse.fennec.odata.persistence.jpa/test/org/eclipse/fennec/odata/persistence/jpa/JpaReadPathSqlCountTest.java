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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Read-path efficiency contract (req §5: full pushdown, "no N+1 on $expand"): these tests
 * assert HOW MANY SELECT statements a read actually issues — deterministic instead of
 * wall-clock, so they hold in CI. The counting log lives in {@link JpaWebshopTestBase}.
 */
@DisplayName("JPA read path: SQL statement counts (no N+1)")
class JpaReadPathSqlCountTest extends JpaWebshopTestBase {

	@Test
	@DisplayName("read = 1 main SELECT + 1 batched SELECT per to-many feature; $count adds one")
	void constantStatementCountPerRead() {
		// evict so entity building really loads — the shared cache must not mask costs
		emf.getCache().evictAll();
		resetSqlCount();
		query("price lt 3.00", "price asc", 1, 2, false);
		assertEquals(3, selectCount(),
				"1 entity page + 1 batch each for tags and reviews (NOT per row):\n"
						+ String.join("\n", selectStatements()));

		emf.getCache().evictAll();
		resetSqlCount();
		query("price lt 3.00", null, 0, 2, true);
		assertEquals(4, selectCount(), "the total count is exactly one extra SELECT");
	}

	@Test
	@DisplayName("$expand over N entities stays O(1) queries and materializes the targets")
	void expandWithoutNPlusOne() {
		// 20 products in 20 DISTINCT categories — the shared session cache cannot mask N+1
		List<EObject> extra = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			EObject category = instance("Category", "id", "xc" + i, "name", "XCat" + i);
			extra.add(category);
			extra.add(instance("Product", "id", "xp" + i, "name", "XProd" + i,
					"price", new BigDecimal(i + ".10"), "category", category));
		}
		persist(extra.toArray(EObject[]::new));
		emf.getCache().evictAll(); // nothing preloaded — loading must happen in the read

		resetSqlCount();
		QueryResult result = service.execute(new EntityQuery(productClass, null,
				parser.parseFilter("startswith(name,'XProd')", productClass),
				parser.parseOrderBy("name asc", productClass),
				0, -1, false, Set.of("category")));

		assertEquals(20, result.entities().size());
		for (EObject product : result.entities()) {
			String productName = String.valueOf(
					product.eGet(productClass.getEStructuralFeature("name")));
			EObject category = (EObject) product.eGet(
					productClass.getEStructuralFeature("category"));
			assertNotNull(category, "expanded navigation must be present for " + productName);
			String categoryName = String.valueOf(
					category.eGet(category.eClass().getEStructuralFeature("name")));
			assertEquals("XCat" + productName.substring("XProd".length()), categoryName,
					"expanded target must be materialized AND correctly associated");
		}
		assertTrue(selectCount() <= 3,
				"expected O(1) statements for entities + expanded targets, got " + selectCount()
						+ ":\n" + String.join("\n", selectStatements().stream().distinct().limit(6).toList()));
	}
}
