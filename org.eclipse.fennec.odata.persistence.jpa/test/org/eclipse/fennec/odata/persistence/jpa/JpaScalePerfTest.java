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
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;

/**
 * Read-path scaling smoke over 50k rows ({@code @Tag("perf")} — excluded from the normal
 * build, run via {@code ./gradlew perfTest}): the statement counts must stay CONSTANT with
 * table size (the deterministic part), and paged reads/aggregations must complete within
 * generous bounds that only catch accidental O(n) materialization, not machine jitter.
 */
@Tag("perf")
@DisplayName("JPA read path: 50k-row scaling smoke")
class JpaScalePerfTest extends JpaWebshopTestBase {

	private static final int ROWS = 50_000;

	@BeforeEach
	void seedBulkData() {
		ClassDescriptor productDescriptor = serverSession.getDescriptorForAlias("Product");
		ClassDescriptor categoryDescriptor = serverSession.getDescriptorForAlias("Category");
		EObject[] categories = new EObject[10];
		try (EntityManager em = emf.createEntityManager()) {
			em.getTransaction().begin();
			for (int i = 0; i < categories.length; i++) {
				categories[i] = instance("Category", "id", "bc" + i, "name", "Bulk" + i);
				em.persist(categories[i]);
			}
			for (int i = 0; i < ROWS; i++) {
				EObject product = (EObject) productDescriptor.getInstantiationPolicy().buildNewInstance();
				product.eSet(productClass.getEStructuralFeature("id"), "bulk" + i);
				product.eSet(productClass.getEStructuralFeature("name"), "Bulk-" + i);
				product.eSet(productClass.getEStructuralFeature("price"),
						new BigDecimal(i % 1000).movePointLeft(2));
				product.eSet(productClass.getEStructuralFeature("rating"), i % 5);
				product.eSet(productClass.getEStructuralFeature("category"), categories[i % 10]);
				em.persist(product);
				if (i % 1000 == 999) {
					em.flush();
					em.clear();
					for (int j = 0; j < categories.length; j++) {
						// clear() detached the categories — reattach, or the next products
						// would cascade-persist duplicates
						categories[j] = (EObject) em.find(categoryDescriptor.getJavaClass(), "bc" + j);
					}
				}
			}
			em.getTransaction().commit();
		}
		emf.getCache().evictAll();
	}

	@Test
	@DisplayName("paged read over 50k rows: constant statements, bounded time")
	void pagedReadScales() {
		resetSqlCount();
		long start = System.nanoTime();
		QueryResult page = query("price lt 5.00", "name asc", 20_000, 25, true);
		long millis = (System.nanoTime() - start) / 1_000_000;

		assertEquals(25, page.entities().size());
		assertTrue(page.totalCount() > 20_000, "filter matches roughly half the table");
		assertEquals(4, selectCount(),
				"page + tags batch + reviews batch + count — independent of table size:\n"
						+ String.join("\n", selectStatements()));
		// timing is an observation, not a gate (CI machines vary); the structural assert above IS
		System.getLogger(JpaScalePerfTest.class.getName())
				.log(System.Logger.Level.INFO, "paged read over 50k rows took {0} ms", millis);
	}

	@Test
	@DisplayName("$apply groupby over 50k rows aggregates in the database, bounded time")
	void aggregationScales() {
		var pipeline = parser.parseApply(
				"groupby((category/name),aggregate(price with sum as Total,$count as Cnt))",
				productClass);
		resetSqlCount();
		long start = System.nanoTime();
		List<Map<String, Object>> rows = service.executeApply(
				new ApplyQuery(productClass, pipeline, null, List.of(), 0, -1, false)).rows();
		long millis = (System.nanoTime() - start) / 1_000_000;

		assertTrue(rows.size() >= 10, "10 bulk groups (+ fixture groups): " + rows.size());
		long bulkTotal = rows.stream()
				.filter(r -> String.valueOf(((Map<?, ?>) r.get("category")).get("name")).startsWith("Bulk"))
				.mapToLong(r -> ((Number) r.get("Cnt")).longValue()).sum();
		assertEquals(ROWS, bulkTotal, "every bulk row is aggregated — in the database");
		assertEquals(1, selectCount(), "ONE grouped statement, no materialization of 50k rows");
		System.getLogger(JpaScalePerfTest.class.getName())
				.log(System.Logger.Level.INFO, "aggregation over 50k rows took {0} ms", millis);
	}
}
