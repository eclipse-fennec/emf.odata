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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
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

		repository.link(productClass, "'m1'", "category", "'c1'");
		EObject milk = read().get(0);
		assertEquals(dairy, milk.eGet(productClass.getEStructuralFeature("category")));

		assertTrue(repository.unlink(productClass, "'m1'", "category", null));
		assertNull(milk.eGet(productClass.getEStructuralFeature("category")));
		assertFalse(repository.unlink(productClass, "'m1'", "category", null));

		EClass reviewClass = EcoreHelper.getEClass(pkg, "Review");
		EObject review = pkg.getEFactoryInstance().create(reviewClass);
		review.eSet(reviewClass.getEStructuralFeature("stars"), 5);
		repository.createRelated(productClass, "'m1'", "reviews", review);
		assertEquals(1, ((List<?>) milk.eGet(productClass.getEStructuralFeature("reviews"))).size());

		assertThrows(IllegalArgumentException.class,
				() -> repository.link(productClass, "'m1'", "category", "'nosuch'"));
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
