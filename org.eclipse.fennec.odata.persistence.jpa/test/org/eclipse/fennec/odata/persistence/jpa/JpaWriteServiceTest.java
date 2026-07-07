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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService.WriteResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E5 write side against H2: payloads are PLAIN dynamic EMF objects (what the OData-JSON
 * codec produces) — the backend rebuilds them as store instances, so the whole chain
 * "JSON → EObject → JPA entity" is exercised. Fixture and harness: {@link JpaWebshopTestBase}.
 */
@DisplayName("JPA WriteService: create/update/delete against H2")
class JpaWriteServiceTest extends JpaWebshopTestBase {

	@Test
	@DisplayName("create persists attributes and containment children; duplicates → conflict")
	void createAndConflict() {
		EObject payload = plain("Product", "id", "w1", "name", "Water",
				"price", new BigDecimal("0.50"));
		EObject review = plain("Review", "id", "wr1", "stars", 4, "comment", "refreshing");
		payload.eSet(productClass.getEStructuralFeature("reviews"), List.of(review));

		service.create(productClass, payload);

		assertEquals(List.of("Water"), names(query("id eq 'w1'", null, 0, -1, false)));
		assertEquals(List.of("Water"), names(query("reviews/any(r: r/stars eq 4 and r/comment eq 'refreshing')",
				null, 0, -1, false)), "the containment child was rebuilt and persisted");

		assertThrows(WriteConflictException.class,
				() -> service.create(productClass, plain("Product", "id", "w1", "name", "Clone")),
				"existing key → conflict (409 at the protocol layer)");
		assertThrows(IllegalArgumentException.class,
				() -> service.create(productClass, plain("Product", "name", "Keyless")),
				"the key property is required");
	}

	@Test
	@DisplayName("PATCH merges only transmitted features, PUT replaces the entity")
	void patchAndPut() {
		WriteResult patched = service.update(productClass, "'p1'",
				plain("Product", "price", new BigDecimal("1.50")), false);
		assertFalse(patched.created());
		EObject milk = single("id eq 'p1'");
		assertEquals(0, new BigDecimal("1.50").compareTo(
				(BigDecimal) milk.eGet(productClass.getEStructuralFeature("price"))));
		assertEquals("Milk", milk.eGet(productClass.getEStructuralFeature("name")),
				"PATCH leaves untransmitted features untouched");

		WriteResult replaced = service.update(productClass, "'p1'",
				plain("Product", "name", "Whole Milk"), true);
		assertFalse(replaced.created());
		EObject wholeMilk = single("id eq 'p1'");
		assertEquals("Whole Milk", wholeMilk.eGet(productClass.getEStructuralFeature("name")));
		assertNull(wholeMilk.eGet(productClass.getEStructuralFeature("price")),
				"PUT resets features missing from the payload");
	}

	@Test
	@DisplayName("update with an unknown key creates (OData upsert), the URL key wins")
	void upsert() {
		WriteResult result = service.update(productClass, "'u1'",
				plain("Product", "id", "IGNORED", "name", "Upserted"), false);
		assertTrue(result.created());
		EObject upserted = single("id eq 'u1'");
		assertEquals("Upserted", upserted.eGet(productClass.getEStructuralFeature("name")));
	}

	@Test
	@DisplayName("delete removes the entity; deleting the absent → false (404)")
	void deleteEntity() {
		assertTrue(service.delete(productClass, "'p4'"));
		assertEquals(List.of(), names(query("id eq 'p4'", null, 0, -1, false)));
		assertFalse(service.delete(productClass, "'p4'"), "already gone");
	}

	@Test
	@DisplayName("writes work on derived types (discriminator column filled)")
	void createDerived() {
		EClass discounted = discountedClass;
		EObject payload = plain("DiscountedProduct", "id", "wd1", "name", "Bargain", "discount", 30);
		service.create(discounted, payload);

		assertEquals(List.of("Bargain"), names(service.execute(
				new org.eclipse.fennec.odata.persistence.api.EntityQuery(productClass, discounted,
						parser.parseFilter("discount eq 30", discounted),
						List.of(), 0, -1, false))));
	}

	// --- helpers ---

	/** A PLAIN dynamic EMF instance (what the JSON codec produces) — not a store entity. */
	private EObject plain(String className, Object... featureValuePairs) {
		EClass eClass = (EClass) pkg.getEClassifier(className);
		EObject object = pkg.getEFactoryInstance().create(eClass);
		for (int i = 0; i < featureValuePairs.length; i += 2) {
			object.eSet(eClass.getEStructuralFeature((String) featureValuePairs[i]),
					featureValuePairs[i + 1]);
		}
		return object;
	}

	private EObject single(String filter) {
		List<EObject> entities = query(filter, null, 0, -1, false).entities();
		assertEquals(1, entities.size(), "exactly one match for: " + filter);
		return entities.get(0);
	}
}
