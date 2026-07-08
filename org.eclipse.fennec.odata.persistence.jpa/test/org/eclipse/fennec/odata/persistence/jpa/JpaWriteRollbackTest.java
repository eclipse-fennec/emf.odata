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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Write atomicity: a failure BETWEEN {@code begin()} and {@code commit()} must roll the whole unit
 * back, leaving no partial state — the guarantee the {@code inTransaction} safety net provides.
 * Exercised by a deep insert whose two containment children collide on their primary key, so the
 * flush fails after the parent row has already been staged.
 */
@DisplayName("JPA write path: a mid-transaction failure rolls back atomically")
class JpaWriteRollbackTest extends JpaWebshopTestBase {

	@Test
	@DisplayName("a failed deep insert persists neither the parent nor any child")
	void deepInsertFailureLeavesNothing() {
		EObject product = instance("Product", "id", "pRollback", "name", "Doomed");
		EObject r1 = instance("Review", "id", "dupKey", "stars", 5, "comment", "a");
		EObject r2 = instance("Review", "id", "dupKey", "stars", 4, "comment", "b"); // same PK → flush fails
		product.eSet(productClass.getEStructuralFeature("reviews"), List.of(r1, r2));

		assertThrows(RuntimeException.class, () -> service.create(productClass, product),
				"the duplicate child key must fail the write");

		QueryResult after = query("id eq 'pRollback'", null, 0, -1, false);
		assertTrue(after.entities().isEmpty(),
				"the transaction rolled back: the parent was not left half-committed");
	}
}
