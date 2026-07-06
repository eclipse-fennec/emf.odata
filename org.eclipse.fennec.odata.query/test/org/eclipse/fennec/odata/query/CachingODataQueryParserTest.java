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
package org.eclipse.fennec.odata.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E4-AP-7 (req §3.6.1): the ad-hoc query LRU — cache hits return the same AST, misses parse,
 * eviction respects the per-EClass capacity, statistics count, failures are not cached.
 */
@DisplayName("Ad-hoc query LRU cache (§3.6.1)")
class CachingODataQueryParserTest {

	private EcoreHelper ecoreHelper;
	private EClass productClass;
	private EClass categoryClass;

	@BeforeEach
	void setUp() throws Exception {
		Path ecore = findResource("testdata/webshop.ecore",
				"org.eclipse.fennec.odata.query/testdata/webshop.ecore");
		ecoreHelper = new EcoreHelper();
		EPackage pkg = ecoreHelper.loadEcore(ecore);
		productClass = EcoreHelper.getEClass(pkg, "Product");
		categoryClass = EcoreHelper.getEClass(pkg, "Category");
	}

	@AfterEach
	void tearDown() {
		ecoreHelper.releaseAll();
	}

	@Test
	@DisplayName("repeat parse is a hit and returns the same AST instance")
	void cachesFilters() {
		CachingODataQueryParser parser = new CachingODataQueryParser();
		OclExpression first = parser.parseFilter("price lt 10", productClass);
		OclExpression second = parser.parseFilter("price lt 10", productClass);

		assertSame(first, second, "hit returns the cached AST (read-only sharing contract)");
		assertEquals(1, parser.cache(productClass).hits());
		assertEquals(1, parser.cache(productClass).misses());

		assertSame(parser.parseOrderBy("name desc", productClass),
				parser.parseOrderBy("name desc", productClass), "orderby is cached too");
	}

	@Test
	@DisplayName("caches are per context EClass")
	void cachesPerClass() {
		CachingODataQueryParser parser = new CachingODataQueryParser();
		OclExpression forProduct = parser.parseFilter("name eq 'x'", productClass);
		OclExpression forCategory = parser.parseFilter("name eq 'x'", categoryClass);

		assertNotSame(forProduct, forCategory, "same string, different context → different entry");
		assertEquals(1, parser.cache(productClass).size());
		assertEquals(1, parser.cache(categoryClass).size());
	}

	@Test
	@DisplayName("LRU evicts beyond capacity; invalidate drops a class's cache")
	void evictsAndInvalidates() {
		CachingODataQueryParser parser = new CachingODataQueryParser(2);
		parser.parseFilter("rating eq 1", productClass);
		parser.parseFilter("rating eq 2", productClass);
		parser.parseFilter("rating eq 3", productClass); // evicts "rating eq 1"
		assertEquals(2, parser.cache(productClass).size());

		OclExpression third = parser.parseFilter("rating eq 3", productClass);
		assertSame(third, parser.parseFilter("rating eq 3", productClass), "still cached");

		parser.invalidate(productClass);
		assertEquals(0, parser.cache(productClass).size());
	}

	@Test
	@DisplayName("parse failures are not cached")
	void failuresAreNotCached() {
		CachingODataQueryParser parser = new CachingODataQueryParser();
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("nosuch eq 1", productClass));
		assertEquals(0, parser.cache(productClass).size());
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
