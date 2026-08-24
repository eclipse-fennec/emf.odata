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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.MediaService;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService.WriteResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The writable in-memory reference backend: mutations through {@code WriteService} are
 * immediately visible through the read pipeline ({@code EntityRepository} →
 * {@code InMemoryQueryService}) — the same store serves both sides.
 */
@DisplayName("MemoryWriteRepository: reference write backend")
class MemoryWriteRepositoryTest {

	private EcoreHelper ecoreHelper;
	private EClass productClass;
	private MemoryWriteRepository repository;
	private InMemoryQueryService queryService;
	private EPackage pkg;

	@BeforeEach
	void setUp() throws Exception {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(findResource("testdata/webshop.ecore",
				"org.eclipse.fennec.odata.persistence.inmemory/testdata/webshop.ecore"));
		productClass = EcoreHelper.getEClass(pkg, "Product");
		repository = new MemoryWriteRepository();
		repository.addEPackage(pkg);
		queryService = new InMemoryQueryService();
		queryService.addRepository(repository);
	}

	@AfterEach
	void tearDown() {
		ecoreHelper.releaseAll();
	}

	@Test
	@DisplayName("create/patch/put/upsert/delete round trip through the read pipeline")
	void writeRoundTrip() {
		assertTrue(repository.supports(productClass));

		repository.create(productClass, product("w1", "Water", "0.50"));
		assertEquals(1, read().size());
		assertThrows(WriteConflictException.class,
				() -> repository.create(productClass, product("w1", "Clone", "1.00")));
		assertThrows(IllegalArgumentException.class,
				() -> repository.create(productClass, product(null, "Keyless", "1.00")));

		// PATCH: only transmitted features
		EObject patch = pkg.getEFactoryInstance().create(productClass);
		patch.eSet(productClass.getEStructuralFeature("price"), new BigDecimal("0.60"));
		WriteResult patched = repository.update(productClass, "'w1'", patch, false);
		assertFalse(patched.created());
		assertEquals("Water", patched.entity().eGet(productClass.getEStructuralFeature("name")));
		assertEquals(0, new BigDecimal("0.60").compareTo(
				(BigDecimal) patched.entity().eGet(productClass.getEStructuralFeature("price"))));

		// PUT: replace — missing features reset
		EObject replace = pkg.getEFactoryInstance().create(productClass);
		replace.eSet(productClass.getEStructuralFeature("name"), "Sparkling");
		WriteResult replaced = repository.update(productClass, "'w1'", replace, true);
		assertEquals("Sparkling", replaced.entity().eGet(productClass.getEStructuralFeature("name")));
		assertNull(replaced.entity().eGet(productClass.getEStructuralFeature("price")));

		// upsert: unknown key creates, the URL key wins over the payload key
		WriteResult upserted = repository.update(productClass, "'w2'",
				product("IGNORED", "Juice", "1.80"), false);
		assertTrue(upserted.created());
		assertEquals("w2", upserted.entity().eGet(productClass.getEStructuralFeature("id")));
		assertEquals(2, read().size());

		assertTrue(repository.delete(productClass, "'w1'"));
		assertFalse(repository.delete(productClass, "'w1'"), "already gone");
		assertEquals(1, read().size());
	}

	@Test
	@DisplayName("$ref operations: link/unlink and related create through the store")
	void referenceOperations() {
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");
		EObject dairy = pkg.getEFactoryInstance().create(categoryClass);
		dairy.eSet(categoryClass.getEStructuralFeature("id"), "c1");
		dairy.eSet(categoryClass.getEStructuralFeature("name"), "Dairy");
		repository.create(categoryClass, dairy);
		repository.create(productClass, product("m1", "Milk", "1.20"));

		// reads return defensive COPIES (no live-object leak), so re-read after each mutation to
		// observe it — a held read reflects the store state AT THE TIME OF THE READ, by design
		repository.link(productClass, "'m1'", "category", "'c1'");
		EObject linkedCategory = (EObject) read().get(0)
				.eGet(productClass.getEStructuralFeature("category"));
		assertEquals("c1", linkedCategory == null ? null
				: linkedCategory.eGet(categoryClass.getEStructuralFeature("id")),
				"the linked category is visible on a fresh read");

		assertTrue(repository.unlink(productClass, "'m1'", "category", null));
		assertNull(read().get(0).eGet(productClass.getEStructuralFeature("category")),
				"the unlink is visible on a fresh read");
		assertFalse(repository.unlink(productClass, "'m1'", "category", null));

		EClass reviewClass = EcoreHelper.getEClass(pkg, "Review");
		EObject review = pkg.getEFactoryInstance().create(reviewClass);
		review.eSet(reviewClass.getEStructuralFeature("stars"), 5);
		repository.createRelated(productClass, "'m1'", "reviews", review);
		assertEquals(1, ((List<?>) read().get(0)
				.eGet(productClass.getEStructuralFeature("reviews"))).size());

		assertThrows(IllegalArgumentException.class,
				() -> repository.link(productClass, "'m1'", "category", "'nosuch'"));
	}

	@Test
	@DisplayName("deleting an entity a reference still points at is a conflict, not a removal")
	void deletingAStillReferencedEntityIsRefused() {
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");
		EObject dairy = pkg.getEFactoryInstance().create(categoryClass);
		dairy.eSet(categoryClass.getEStructuralFeature("id"), "c1");
		repository.create(categoryClass, dairy);
		repository.create(productClass, product("m1", "Milk", "1.20"));
		repository.link(productClass, "'m1'", "category", "'c1'");

		// the same contract both database backends hold (persistence-jpa#195/#219, §4c), so
		// the reference backend answers the conflict too instead of leaving a dangling link
		WriteConflictException refused = assertThrows(WriteConflictException.class,
				() -> repository.delete(categoryClass, "'c1'"));
		assertTrue(refused.getMessage().contains("Product.category"),
				"the refusal names the referring reference: " + refused.getMessage());

		// the referrer goes first — then the target deletes normally
		assertTrue(repository.unlink(productClass, "'m1'", "category", null));
		assertTrue(repository.delete(categoryClass, "'c1'"));

		// containment is ownership, not this question: a review lives inside its product
		assertTrue(repository.delete(productClass, "'m1'"),
				"a product owning contained reviews still deletes");
	}

	@Test
	@DisplayName("non-containment payload members resolve to STORE entities by key")
	void nonContainmentBindings() {
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");
		EObject dairy = pkg.getEFactoryInstance().create(categoryClass);
		dairy.eSet(categoryClass.getEStructuralFeature("id"), "c1");
		dairy.eSet(categoryClass.getEStructuralFeature("name"), "Dairy");
		repository.create(categoryClass, dairy);

		// create with a key-only stub: the store instance is bound, not the stub
		EObject payload = product("m1", "Milk", "1.20");
		EObject stub = pkg.getEFactoryInstance().create(categoryClass);
		stub.eSet(categoryClass.getEStructuralFeature("id"), "c1");
		payload.eSet(productClass.getEStructuralFeature("category"), stub);
		repository.create(productClass, payload);
		EObject bound = (EObject) read().get(0).eGet(productClass.getEStructuralFeature("category"));
		assertEquals("Dairy", bound.eGet(categoryClass.getEStructuralFeature("name")),
				"the resolved STORE category carries its full state, not just the stub key");

		// PUT without the navigation keeps the binding (11.4.3: replace is structural-only)
		EObject replace = pkg.getEFactoryInstance().create(productClass);
		replace.eSet(productClass.getEStructuralFeature("name"), "Whole Milk");
		repository.update(productClass, "'m1'", replace, true);
		EObject kept = (EObject) read().get(0).eGet(productClass.getEStructuralFeature("category"));
		assertEquals("c1", kept.eGet(categoryClass.getEStructuralFeature("id")),
				"PUT must NOT clear an omitted navigation");

		// unknown target refuses the write, nothing is stored
		EObject ghost = product("m2", "Ghost", "1.00");
		EObject nosuch = pkg.getEFactoryInstance().create(categoryClass);
		nosuch.eSet(categoryClass.getEStructuralFeature("id"), "nosuch");
		ghost.eSet(productClass.getEStructuralFeature("category"), nosuch);
		assertThrows(IllegalArgumentException.class,
				() -> repository.create(productClass, ghost),
				"an unknown reference target is refused, never a silent deep insert");
		assertEquals(1, read().size(), "the failed create left nothing behind");
	}

	@Test
	@DisplayName("concurrent createRelated on one owner keeps every add (no lost updates / CME)")
	void concurrentReferenceMutations() throws Exception {
		repository.create(productClass, product("m1", "Milk", "1.20"));
		EClass reviewClass = EcoreHelper.getEClass(pkg, "Review");
		int threads = 8;
		int perThread = 40;
		java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
		List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>();
		for (int t = 0; t < threads; t++) {
			futures.add(pool.submit(() -> {
				for (int i = 0; i < perThread; i++) {
					EObject review = pkg.getEFactoryInstance().create(reviewClass);
					review.eSet(reviewClass.getEStructuralFeature("stars"), 5);
					repository.createRelated(productClass, "'m1'", "reviews", review);
				}
			}));
		}
		for (java.util.concurrent.Future<?> f : futures) {
			f.get(); // propagates any ConcurrentModificationException / lost-update failure
		}
		pool.shutdown();
		EObject milk = read().get(0);
		assertEquals(threads * perThread,
				((List<?>) milk.eGet(productClass.getEStructuralFeature("reviews"))).size(),
				"every concurrent add is retained under the per-owner lock");
	}

	@Test
	@DisplayName("a concurrent commit survives another thread's rollback (no foreign-data loss)")
	void concurrentCommitSurvivesForeignRollback() throws Exception {
		CountDownLatch ghostCreated = new CountDownLatch(1);
		CountDownLatch foreignCommitted = new CountDownLatch(1);
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			// Thread A: open a batch, create 'Ghost', wait for the foreign write, then roll back
			Future<?> a = pool.submit(() -> {
				repository.begin();
				repository.create(productClass, product("ghost", "Ghost", "1.00"));
				ghostCreated.countDown();
				await(foreignCommitted);
				repository.rollback();
			});
			// Thread B: after A's in-batch create, write 'Survivor' WITHOUT a transaction (its own
			// thread has no ambient tx → the write commits immediately and A never touched its key)
			Future<?> b = pool.submit(() -> {
				await(ghostCreated);
				repository.create(productClass, product("survivor", "Survivor", "2.00"));
				foreignCommitted.countDown();
			});
			a.get(10, TimeUnit.SECONDS);
			b.get(10, TimeUnit.SECONDS);
		} finally {
			pool.shutdownNow();
		}
		List<String> names = allNames();
		assertFalse(names.contains("Ghost"), "the rolled-back batch's write must be gone");
		assertTrue(names.contains("Survivor"),
				"a concurrent commit on an untouched key MUST survive the foreign rollback");
	}

	@Test
	@DisplayName("concurrent read during structural mutation does not throw (defensive copy)")
	void concurrentReadDuringMutationHasNoCme() throws Exception {
		repository.create(productClass, product("m1", "Milk", "1.20"));
		EClass reviewClass = EcoreHelper.getEClass(pkg, "Review");
		AtomicBoolean writerDone = new AtomicBoolean(false);
		int writes = 400; // bounded: keeps the multi-valued feature from growing without limit
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try {
			Future<?> writer = pool.submit(() -> {
				for (int i = 0; i < writes; i++) {
					EObject r = pkg.getEFactoryInstance().create(reviewClass);
					r.eSet(reviewClass.getEStructuralFeature("stars"), 5);
					repository.createRelated(productClass, "'m1'", "reviews", r);
				}
				writerDone.set(true);
			});
			// the reader iterates a multi-valued feature of the returned entities WHILE the writer
			// structurally mutates it; on LIVE objects this would throw ConcurrentModificationException
			// (at the reader or inside the copy) — the class-store-locked defensive copy must prevent it
			Future<?> reader = pool.submit(() -> {
				do {
					for (EObject product : repository.entities(productClass)) {
						Object reviews = product.eGet(productClass.getEStructuralFeature("reviews"));
						if (reviews instanceof List<?> list) {
							for (Object ignored : list) {
								// merely iterating a live shared EList would CME
							}
						}
					}
				} while (!writerDone.get());
			});
			writer.get(20, TimeUnit.SECONDS);
			reader.get(20, TimeUnit.SECONDS); // completes only if no CME/exception escaped
		} finally {
			pool.shutdownNow();
		}
		assertEquals(writes, ((List<?>) read().get(0)
				.eGet(productClass.getEStructuralFeature("reviews"))).size(),
				"every concurrent add is retained");
	}

	@Test
	@DisplayName("media writes participate in the transaction (rolled back / committed)")
	void mediaRolledBackWithTransaction() {
		repository.create(productClass, product("m1", "Milk", "1.20"));
		repository.writeMedia(productClass, "'m1'",
				new MediaService.MediaStream(new byte[] { 1 }, "image/png"));

		repository.begin();
		repository.writeMedia(productClass, "'m1'",
				new MediaService.MediaStream(new byte[] { 2, 2 }, "image/png"));
		repository.rollback();
		assertEquals(1, repository.readMedia(productClass, "'m1'").orElseThrow().content().length,
				"the media write is rolled back to the prior stream");

		repository.begin();
		repository.writeMedia(productClass, "'m1'",
				new MediaService.MediaStream(new byte[] { 3, 3, 3 }, "image/png"));
		repository.commit();
		assertEquals(3, repository.readMedia(productClass, "'m1'").orElseThrow().content().length,
				"a committed media write persists");
	}

	private List<String> allNames() {
		return read().stream()
				.map(e -> String.valueOf(e.eGet(productClass.getEStructuralFeature("name")))).toList();
	}

	private static void await(CountDownLatch latch) {
		try {
			if (!latch.await(10, TimeUnit.SECONDS)) {
				throw new IllegalStateException("timed out waiting for the other thread");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	@Test
	@DisplayName("transaction: rollback discards writes since begin(), commit keeps them")
	void transactionalChangeSet() {
		assertTrue(repository.transactional());
		repository.create(productClass, product("keep", "Water", "0.50"));

		// rollback: a create and a mutation done after begin() are both undone
		repository.begin();
		repository.create(productClass, product("temp", "Soda", "2.00"));
		repository.update(productClass, "'keep'", patch("keep", "Sparkling Water"), false);
		assertEquals(2, read().size(), "writes are visible while the transaction is open");
		repository.rollback();

		List<EObject> afterRollback = read();
		assertEquals(1, afterRollback.size(), "the temp create is gone");
		assertEquals("Water", afterRollback.get(0).eGet(productClass.getEStructuralFeature("name")),
				"the mutation to 'keep' is reverted");

		// commit: writes persist
		repository.begin();
		repository.create(productClass, product("added", "Juice", "1.80"));
		repository.commit();
		assertEquals(2, read().size(), "committed writes remain");
	}

	private EObject patch(String id, String newName) {
		EObject payload = pkg.getEFactoryInstance().create(productClass);
		payload.eSet(productClass.getEStructuralFeature("id"), id);
		payload.eSet(productClass.getEStructuralFeature("name"), newName);
		return payload;
	}

	@Test
	@DisplayName("media stream: write/read round-trip; no entity → empty read, refused write")
	void mediaStreamRoundTrip() {
		repository.create(productClass, product("m1", "Photo", "1.00"));

		assertTrue(repository.readMedia(productClass, "'m1'").isEmpty(),
				"an entity without content has no stream yet");
		assertTrue(repository.readMedia(productClass, "'nope'").isEmpty(), "no entity → no stream");
		assertFalse(repository.writeMedia(productClass, "'nope'",
				new MediaService.MediaStream(new byte[] { 1 }, "image/png")),
				"media belongs to an existing entity");

		assertTrue(repository.writeMedia(productClass, "'m1'",
				new MediaService.MediaStream(new byte[] { 1, 2, 3 }, "image/png")));
		MediaService.MediaStream stream = repository.readMedia(productClass, "'m1'").orElseThrow();
		assertEquals("image/png", stream.contentType());
		assertEquals(3, stream.content().length);
	}

	@Test
	@DisplayName("polymorphic read: a base-set query sees derived instances ([OData-URL] 4.11)")
	void polymorphicRead() {
		EClass discountedClass = EcoreHelper.getEClass(pkg, "DiscountedProduct");
		repository.create(productClass, product("p1", "Milk", "1.20"));
		EObject sale = pkg.getEFactoryInstance().create(discountedClass);
		sale.eSet(discountedClass.getEStructuralFeature("id"), "d1");
		sale.eSet(discountedClass.getEStructuralFeature("name"), "SaleMilk");
		sale.eSet(discountedClass.getEStructuralFeature("price"), new BigDecimal("0.90"));
		sale.eSet(discountedClass.getEStructuralFeature("discount"), 20);
		repository.create(discountedClass, sale);

		// the base Product set MUST include the DiscountedProduct (stored under its exact eClass)
		assertEquals(2, read().size(), "base-set query omits the derived instance");
		// the derived set sees only the derived instance
		assertEquals(1, queryService.execute(EntityQuery.all(discountedClass)).entities().size());
	}

	@Test
	@DisplayName("a composite identity declared on the type addresses the store by named key")
	void compositeIdentity() {
		// Ecore allows at most one isID attribute, so the identity is declared once on the type
		// (persistence-jpa#115) — the backend resolves it through EntityKeys, in key order
		EAnnotation identity = EcoreFactory.eINSTANCE.createEAnnotation();
		identity.setSource("http://eclipse.org/fennec/persistence/1.0");
		identity.getDetails().put("idFeatures", "id,name");
		productClass.getEAnnotations().add(identity);

		repository.create(productClass, product("w1", "Water", "0.50"));
		repository.create(productClass, product("w1", "Sparkling", "0.80"));
		assertEquals(2, read().size(), "the components together identify the entity, not id alone");

		Map<String, String> key = Map.of("id", "'w1'", "name", "'Water'");
		EObject patch = pkg.getEFactoryInstance().create(productClass);
		patch.eSet(productClass.getEStructuralFeature("price"), new BigDecimal("0.55"));
		WriteResult patched = repository.update(productClass, key, patch, false);
		assertFalse(patched.created());
		assertEquals("Water", patched.entity().eGet(productClass.getEStructuralFeature("name")));

		assertThrows(IllegalArgumentException.class,
				() -> repository.update(productClass, Map.of("id", "'w1'"), patch, false),
				"a predicate that names only one component addresses nothing");

		assertTrue(repository.delete(productClass, key));
		assertEquals(1, read().size());
		assertEquals("Sparkling", read().get(0).eGet(productClass.getEStructuralFeature("name")),
				"the sibling sharing the id component survives");
	}

	private List<EObject> read() {
		return queryService.execute(EntityQuery.all(productClass)).entities();
	}

	private EObject product(String id, String name, String price) {
		EObject object = pkg.getEFactoryInstance().create(productClass);
		if (id != null) {
			object.eSet(productClass.getEStructuralFeature("id"), id);
		}
		object.eSet(productClass.getEStructuralFeature("name"), name);
		object.eSet(productClass.getEStructuralFeature("price"), new BigDecimal(price));
		return object;
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
