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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Kleene three-valued logic in {@link OclEvaluator} (OData/SQL semantics): a relational comparison
 * against a NULL value is UNKNOWN, not false, and {@code and}/{@code or}/{@code not} must combine
 * UNKNOWN correctly. The regression this guards: {@code (nullable gt 5) or (name eq 'x')} used to
 * exclude a matching row because the UNKNOWN left operand collapsed the whole predicate to false —
 * a SILENT WRONG RESULT. The full truth table is asserted, including nesting and the null-literal
 * test that stays defined.
 */
@DisplayName("OclEvaluator: Kleene three-valued logic for and/or/not")
class OclEvaluatorTest {

	private EcoreHelper ecoreHelper;
	private EClass productClass;
	private EAttribute price;
	private EAttribute name;
	private final ODataQueryParser parser = new ODataQueryParser();
	private final OclEvaluator evaluator = new OclEvaluator();

	@BeforeEach
	void setUp() throws Exception {
		Path ecore = findResource("testdata/webshop.ecore",
				"org.eclipse.fennec.odata.query/testdata/webshop.ecore");
		ecoreHelper = new EcoreHelper();
		EPackage pkg = ecoreHelper.loadEcore(ecore);
		productClass = EcoreHelper.getEClass(pkg, "Product");
		price = (EAttribute) productClass.getEStructuralFeature("price");
		name = (EAttribute) productClass.getEStructuralFeature("name");
	}

	@AfterEach
	void tearDown() {
		ecoreHelper.releaseAll();
	}

	/** A Product with a NULL price (the UNKNOWN operand) and name "x". */
	private EObject nullPrice() {
		EObject p = productClass.getEPackage().getEFactoryInstance().create(productClass);
		p.eSet(name, "x"); // price left unset → eGet returns null
		return p;
	}

	/** A Product with price=10 and name "x". */
	private EObject setPrice() {
		EObject p = productClass.getEPackage().getEFactoryInstance().create(productClass);
		p.eSet(name, "x");
		p.eSet(price, new BigDecimal("10"));
		return p;
	}

	private boolean matches(String filter, EObject entity) {
		OclExpression predicate = parser.parseFilter(filter, productClass);
		return evaluator.matches(predicate, entity);
	}

	// --- OR: TRUE dominates; unknown OR true = true (the regression) ---

	@Test
	@DisplayName("unknown OR true = true (the silent-wrong-result regression)")
	void unknownOrTrue() {
		assertTrue(matches("price gt 5 or name eq 'x'", nullPrice()),
				"a NULL-price row must still match because the right disjunct is true");
	}

	@Test
	@DisplayName("unknown OR false = unknown → excluded")
	void unknownOrFalse() {
		assertFalse(matches("price gt 5 or name eq 'z'", nullPrice()));
	}

	@Test
	@DisplayName("true OR anything = true (short-circuit, non-null)")
	void trueOr() {
		assertTrue(matches("price gt 5 or name eq 'z'", setPrice()));
	}

	// --- AND: FALSE dominates ---

	@Test
	@DisplayName("unknown AND true = unknown → excluded")
	void unknownAndTrue() {
		assertFalse(matches("price gt 5 and name eq 'x'", nullPrice()));
	}

	@Test
	@DisplayName("unknown AND false = false → excluded")
	void unknownAndFalse() {
		assertFalse(matches("price gt 5 and name eq 'z'", nullPrice()));
	}

	@Test
	@DisplayName("true AND true = true (non-null)")
	void trueAndTrue() {
		assertTrue(matches("price gt 5 and name eq 'x'", setPrice()));
	}

	// --- NOT: NOT unknown = unknown ---

	@Test
	@DisplayName("NOT unknown = unknown → excluded")
	void notUnknown() {
		assertFalse(matches("not (price gt 5)", nullPrice()));
	}

	@Test
	@DisplayName("NOT false = true, NOT true = false (non-null)")
	void notDefinite() {
		assertTrue(matches("not (price gt 5)", withPrice("2")), "NOT (2 gt 5) = NOT false = true");
		assertFalse(matches("not (price gt 5)", withPrice("20")), "NOT (20 gt 5) = NOT true = false");
	}

	// --- nesting: the case a flat OR fix alone would miss ---

	@Test
	@DisplayName("(unknown AND false) OR true = true — nested connectives combine correctly")
	void nestedUnknownAndFalseOrTrue() {
		assertTrue(matches("(price gt 5 and name eq 'z') or name eq 'x'", nullPrice()),
				"the false AND short-circuits to false, so the true disjunct must carry the OR");
	}

	@Test
	@DisplayName("(unknown OR false) AND true = unknown → excluded")
	void nestedUnknownOrFalseAndTrue() {
		assertFalse(matches("(price gt 5 or name eq 'z') and name eq 'x'", nullPrice()));
	}

	// --- the null LITERAL test stays defined (x eq/ne null) ---

	@Test
	@DisplayName("eq/ne null is a defined test, not UNKNOWN")
	void nullLiteralComparisonDefined() {
		assertTrue(matches("price eq null", nullPrice()));
		assertFalse(matches("price eq null", setPrice()));
		assertTrue(matches("price ne null", setPrice()));
		assertFalse(matches("price ne null", nullPrice()));
	}

	@Test
	@DisplayName("substring with an out-of-int-range start saturates (no overflow wrap) → empty")
	void substringHugeOffsetDoesNotOverflow() {
		// 2e12 > Integer.MAX_VALUE: it must saturate to the end (empty result), not wrap via
		// intValue() into a small in-range offset that returns a bogus non-empty substring
		assertTrue(matches("substring(name, 2000000000000) eq ''", nullPrice()));
	}

	private EObject withPrice(String value) {
		EObject p = productClass.getEPackage().getEFactoryInstance().create(productClass);
		p.eSet(name, "x");
		p.eSet(price, new BigDecimal(value));
		return p;
	}

	private static Path findResource(String... candidates) {
		Path start = Paths.get("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidates) {
				Path p = dir.resolve(rel);
				if (Files.exists(p)) {
					return p;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
