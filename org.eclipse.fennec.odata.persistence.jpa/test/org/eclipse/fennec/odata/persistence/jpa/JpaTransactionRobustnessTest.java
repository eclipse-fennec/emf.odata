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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The ambient {@code $batch} transaction is thread-bound and caller-managed (the servlet calls
 * begin/commit/rollback). On a POOLED servlet thread an unfinished batch would otherwise leave an
 * open EM/transaction in the ThreadLocal that the NEXT request joins — cross-request contamination
 * or a leaked connection. These tests assert the defence: a leaked ambient transaction is safely
 * ROLLED BACK (never committed or joined) when the next {@code begin()} runs, and out-of-order
 * commit/rollback calls are no-ops rather than faults.
 */
@DisplayName("JpaQueryService: ambient transaction robustness")
class JpaTransactionRobustnessTest extends JpaWebshopTestBase {

	@Test
	@DisplayName("a leaked (unfinished) ambient transaction is rolled back on the next begin()")
	void leakedAmbientRolledBackOnNextBegin() {
		service.begin();
		service.create(productClass, product("leak1", "LeakedGhost", "9.99"));
		// simulate the servlet throwing mid-batch: NO commit()/rollback() — the ambient tx leaks
		service.begin(); // must detect the leaked ambient, roll it back, and open a fresh one
		service.commit(); // commit the fresh (empty) transaction

		assertFalse(allNames().contains("LeakedGhost"),
				"the uncommitted write of the leaked batch must have been rolled back, not committed");
	}

	@Test
	@DisplayName("after discarding a leaked ambient, a fresh batch commits normally (no contamination)")
	void freshBatchWorksAfterLeak() {
		service.begin();
		service.create(productClass, product("leak2", "AnotherGhost", "1.00"));
		// leak again, then a clean batch on the same 'thread'
		service.begin();
		service.create(productClass, product("ok1", "Persisted", "2.00"));
		service.commit();

		List<String> names = allNames();
		assertTrue(names.contains("Persisted"), "the clean batch's write must be committed");
		assertFalse(names.contains("AnotherGhost"), "the leaked write must not survive");
	}

	@Test
	@DisplayName("commit()/rollback() without a begin() are no-ops (wrong-order calls do not fault)")
	void outOfOrderTransactionCallsAreNoops() {
		assertDoesNotThrow(service::commit, "commit without begin must be a no-op");
		assertDoesNotThrow(service::rollback, "rollback without begin must be a no-op");
		// and a normal batch still works right after the stray calls
		service.begin();
		service.create(productClass, product("ok2", "StillWorks", "3.00"));
		service.commit();
		assertTrue(allNames().contains("StillWorks"));
	}

	private List<String> allNames() {
		return names(service.execute(new EntityQuery(productClass, null, List.of(), 0, -1, false)));
	}

	private EObject product(String id, String name, String price) {
		return instance("Product", "id", id, "name", name, "price", new BigDecimal(price));
	}
}
