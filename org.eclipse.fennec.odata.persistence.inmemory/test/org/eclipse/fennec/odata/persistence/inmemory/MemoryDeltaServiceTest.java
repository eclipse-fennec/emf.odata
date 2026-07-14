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
package org.eclipse.fennec.odata.persistence.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.odata.persistence.api.DeltaGoneException;
import org.eclipse.fennec.odata.persistence.api.DeltaService;
import org.eclipse.fennec.odata.persistence.api.DeltaService.DeltaResult;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Change tracking through the in-memory reference backend ([OData-Protocol] 11.3): the token
 * marks "now", {@code changesSince} reports the latest outcome per touched entity, membership
 * follows the defining query's filter and cast.
 */
@DisplayName("MemoryWriteRepository: DeltaService change journal")
class MemoryDeltaServiceTest {

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private EClass productClass;
	private MemoryWriteRepository repository;
	private final ODataQueryParser parser = new ODataQueryParser();

	@BeforeEach
	void setUp() throws Exception {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(findResource("testdata/webshop.ecore",
				"org.eclipse.fennec.odata.persistence.inmemory/testdata/webshop.ecore"));
		productClass = EcoreHelper.getEClass(pkg, "Product");
		repository = new MemoryWriteRepository();
		repository.addEPackage(pkg);
	}

	@AfterEach
	void tearDown() {
		ecoreHelper.releaseAll();
	}

	@Test
	@DisplayName("create/update/delete since the token → upsert + removal, follow-up token is quiet")
	void changesRoundTrip() {
		repository.create(productClass, product("p1", "Water", "0.50"));
		repository.create(productClass, product("p2", "Juice", "1.80"));

		String token = repository.trackingToken(productClass);
		DeltaResult quiet = repository.changesSince(EntityQuery.all(productClass), token);
		assertEquals(0, quiet.changed().size(), "nothing changed yet");
		assertEquals(0, quiet.removals().size());

		repository.update(productClass, "'p1'", product("p1", "Sparkling Water", "0.60"), false);
		repository.delete(productClass, "'p2'");
		repository.create(productClass, product("p3", "Tea", "2.10"));

		DeltaResult delta = repository.changesSince(EntityQuery.all(productClass), token);
		assertEquals(List.of("Sparkling Water", "Tea"),
				delta.changed().stream().map(e -> e.eGet(productClass.getEStructuralFeature("name")))
						.toList(),
				"updated and created entities appear with their current state");
		assertEquals(1, delta.removals().size());
		assertEquals(DeltaService.REASON_DELETED, delta.removals().get(0).reason());
		assertEquals("p2", delta.removals().get(0).keyValues().get("id"));

		// applying the follow-up token: nothing further changed
		DeltaResult next = repository.changesSince(EntityQuery.all(productClass), delta.nextToken());
		assertEquals(0, next.changed().size());
		assertEquals(0, next.removals().size());
	}

	@Test
	@DisplayName("membership follows the defining filter: leaving → removal(changed), entering → upsert")
	void filterMembership() {
		repository.create(productClass, product("cheap", "Gum", "0.20"));
		repository.create(productClass, product("dear", "Caviar", "99.00"));
		String token = repository.trackingToken(productClass);

		// 'dear' drops below the threshold — leaves the tracked result
		repository.update(productClass, "'dear'", product("dear", "Caviar", "0.90"), false);
		// 'cheap' rises above it — enters the tracked result
		repository.update(productClass, "'cheap'", product("cheap", "Gum", "3.30"), false);

		EntityQuery tracked = new EntityQuery(productClass,
				parser.parseFilter("price gt 1.00", productClass), List.of(), 0, -1, false);
		DeltaResult delta = repository.changesSince(tracked, token);
		assertEquals(1, delta.changed().size());
		assertEquals("cheap", delta.changed().get(0).eGet(productClass.getEStructuralFeature("id")));
		assertEquals(1, delta.removals().size());
		assertEquals(DeltaService.REASON_CHANGED, delta.removals().get(0).reason());
		assertEquals("dear", delta.removals().get(0).keyValues().get("id"));
	}

	@Test
	@DisplayName("multiple changes to one entity collapse into the latest outcome")
	void changesCollapse() {
		String token = repository.trackingToken(productClass);
		repository.create(productClass, product("p1", "V1", "1.00"));
		repository.update(productClass, "'p1'", product("p1", "V2", "1.00"), false);
		repository.update(productClass, "'p1'", product("p1", "V3", "1.00"), false);
		repository.create(productClass, product("gone", "Ghost", "1.00"));
		repository.delete(productClass, "'gone'");

		DeltaResult delta = repository.changesSince(EntityQuery.all(productClass), token);
		assertEquals(1, delta.changed().size(), "three writes, one upsert");
		assertEquals("V3", delta.changed().get(0).eGet(productClass.getEStructuralFeature("name")));
		// created-and-deleted inside the window: a removal the client can apply as a no-op
		assertEquals(1, delta.removals().size());
		assertEquals(DeltaService.REASON_DELETED, delta.removals().get(0).reason());
	}

	@Test
	@DisplayName("derived instances are visible when tracking the base set; casts restrict membership")
	void polymorphicTracking() {
		EClass discounted = EcoreHelper.getEClass(pkg, "DiscountedProduct");
		String token = repository.trackingToken(productClass);

		EObject sale = pkg.getEFactoryInstance().create(discounted);
		sale.eSet(discounted.getEStructuralFeature("id"), "d1");
		sale.eSet(discounted.getEStructuralFeature("name"), "Sale");
		sale.eSet(discounted.getEStructuralFeature("price"), new BigDecimal("5.00"));
		repository.create(discounted, sale);
		repository.create(productClass, product("p1", "Plain", "1.00"));

		DeltaResult base = repository.changesSince(EntityQuery.all(productClass), token);
		assertEquals(2, base.changed().size(), "base set sees derived and plain instances");

		EntityQuery castOnly = new EntityQuery(productClass, discounted, null, List.of(), 0, -1, false);
		DeltaResult cast = repository.changesSince(castOnly, token);
		assertEquals(1, cast.changed().size(), "cast tracks only derived instances");
		assertEquals("d1", cast.changed().get(0).eGet(discounted.getEStructuralFeature("id")));
		assertEquals(DeltaService.REASON_CHANGED, cast.removals().get(0).reason(),
				"the plain instance never enters the cast membership");
	}

	@Test
	@DisplayName("transactions: rollback drops the journal entries, commit publishes them")
	void transactionalJournal() {
		String token = repository.trackingToken(productClass);

		repository.begin();
		repository.create(productClass, product("tx1", "Rolled back", "1.00"));
		repository.rollback();
		DeltaResult afterRollback = repository.changesSince(EntityQuery.all(productClass), token);
		assertEquals(0, afterRollback.changed().size(), "a rolled-back write never happened");
		assertEquals(0, afterRollback.removals().size());

		repository.begin();
		repository.create(productClass, product("tx2", "Committed", "1.00"));
		repository.commit();
		DeltaResult afterCommit = repository.changesSince(EntityQuery.all(productClass), token);
		assertEquals(1, afterCommit.changed().size());
		assertEquals("Committed",
				afterCommit.changed().get(0).eGet(productClass.getEStructuralFeature("name")));
	}

	@Test
	@DisplayName("expanded tracking: member and membership changes report the OWNER")
	void expandTracking() {
		EObject milk = product("p1", "Milk", "1.20");
		EObject cable = product("p2", "Cable", "1.50");
		repository.create(productClass, milk);
		repository.create(productClass, cable);
		String token = repository.trackingToken(productClass);

		EntityQuery expanded = new EntityQuery(productClass, null,
				parser.parseFilter("id eq 'p1'", productClass), List.of(), 0, -1, false,
				Set.of("accessories"));

		// membership change: linking reports the owner
		repository.link(productClass, "'p1'", "accessories", "'p2'");
		DeltaService.DeltaResult linked = repository.changesSince(expanded, token);
		assertEquals(List.of("p1"), linked.changed().stream()
				.map(e -> e.eGet(productClass.getEStructuralFeature("id"))).toList(),
				"the owner reports — the payload carries the full expanded membership");

		// member content change: renaming the accessory reports the owner too
		String token2 = linked.nextToken();
		repository.update(productClass, "'p2'", product("p2", "Golden Cable", "9.99"), false);
		DeltaService.DeltaResult renamed = repository.changesSince(expanded, token2);
		assertTrue(renamed.changed().stream()
				.anyMatch(e -> "p1".equals(e.eGet(productClass.getEStructuralFeature("id")))),
				"a change INSIDE the expanded navigation reports the owner: " + renamed.changed());

		// without $expand the same window reports only the accessory itself
		DeltaService.DeltaResult plain = repository.changesSince(
				new EntityQuery(productClass, parser.parseFilter("id eq 'p1'", productClass),
						List.of(), 0, -1, false), token2);
		assertTrue(plain.changed().isEmpty(), "p2 fails the filter; p1 did not change structurally");

		// unlink reports the owner again
		String token3 = renamed.nextToken();
		repository.unlink(productClass, "'p1'", "accessories", "'p2'");
		DeltaService.DeltaResult unlinked = repository.changesSince(expanded, token3);
		assertEquals(List.of("p1"), unlinked.changed().stream()
				.map(e -> e.eGet(productClass.getEStructuralFeature("id"))).toList());

		assertTrue(repository.supportsExpandTracking());
	}

	@Test
	@DisplayName("invalid or aged-out tokens raise DeltaGoneException (→ 410)")
	void goneTokens() {
		EntityQuery all = EntityQuery.all(productClass);
		assertThrows(DeltaGoneException.class, () -> repository.changesSince(all, "not-a-token"));
		assertThrows(DeltaGoneException.class, () -> repository.changesSince(all, "-1"));
		assertThrows(DeltaGoneException.class, () -> repository.changesSince(all, "999999"),
				"a token from the future is not ours");

		// age a token out of the retention window: > MAX_JOURNAL_ENTRIES subsequent changes
		String token = repository.trackingToken(productClass);
		repository.create(productClass, product("w", "Churn", "1.00"));
		for (int i = 0; i < 10_001; i++) {
			repository.update(productClass, "'w'", product("w", "Churn " + i, "1.00"), false);
		}
		assertThrows(DeltaGoneException.class, () -> repository.changesSince(all, token),
				"changes were evicted — the client must refetch");
		assertTrue(repository.changesSince(all, repository.trackingToken(productClass))
				.changed().isEmpty(), "a fresh token still works");
	}

	private EObject product(String id, String name, String price) {
		EObject object = pkg.getEFactoryInstance().create(productClass);
		object.eSet(productClass.getEStructuralFeature("id"), id);
		object.eSet(productClass.getEStructuralFeature("name"), name);
		object.eSet(productClass.getEStructuralFeature("price"), new BigDecimal(price));
		return object;
	}

	private static Path findResource(String... candidatesRelative) {
		for (String candidate : candidatesRelative) {
			Path path = Paths.get(candidate);
			if (Files.exists(path)) {
				return path;
			}
		}
		throw new IllegalStateException("test resource not found: " + String.join(", ", candidatesRelative));
	}
}
