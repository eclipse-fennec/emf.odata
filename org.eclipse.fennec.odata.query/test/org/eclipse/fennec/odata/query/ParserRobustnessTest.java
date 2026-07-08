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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

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
 * Hostile/edge input must fail as a client error ({@link ODataQueryParseException} → 400), never
 * as an uncaught {@link Error}/{@link RuntimeException} that the protocol layer turns into a 500
 * (or, worse, a {@code StackOverflowError}-driven crash). Complements {@link ParserWorstCaseTest}
 * (which asserts legal-but-pathological input stays FAST) by asserting illegal input is REJECTED
 * cleanly.
 */
@DisplayName("parser & evaluator reject hostile input as 400, not 500")
class ParserRobustnessTest {

	private static final Duration BOUND = Duration.ofSeconds(10);

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private EClass productClass;
	private final ODataQueryParser parser = new ODataQueryParser();

	@BeforeEach
	void setUp() throws Exception {
		Path ecore = findResource("testdata/webshop.ecore",
				"org.eclipse.fennec.odata.query/testdata/webshop.ecore");
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(ecore);
		productClass = EcoreHelper.getEClass(pkg, "Product");
	}

	@AfterEach
	void tearDown() {
		ecoreHelper.releaseAll();
	}

	@Test
	@DisplayName("paren-free deep recursion (a huge not-chain) is rejected, not a StackOverflowError")
	void deepNotChainRejected() {
		// zero parentheses, so the front-door paren-depth guard would not catch it: the intrinsic
		// StackOverflowError guard must convert the deep parser/visitor recursion into a 400.
		String expression = "not ".repeat(50_000) + "active";
		assertTimeoutPreemptively(BOUND, () -> assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter(expression, productClass)));
	}

	@Test
	@DisplayName("integer literal beyond the 64-bit range is a 400, not a NumberFormatException 500")
	void integerLiteralOverflow() {
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("rating eq 99999999999999999999", productClass));
	}

	@Test
	@DisplayName("an over-long parameter-alias value is rejected before it is parsed")
	void aliasValueLengthCapped() {
		String hugeValue = "'" + "x".repeat(5000) + "'";
		assertThrows(ODataQueryParseException.class, () -> parser.parseFilter(
				"name eq @a", productClass, Map.of("@a", hugeValue)));
	}

	@Test
	@DisplayName("a resource path beyond the length cap is rejected before parsing")
	void resourcePathLengthCapped() {
		String path = "Product" + "/x".repeat(3000);
		assertThrows(ODataQueryParseException.class, () -> new ODataResourceParser().parse(path));
	}

	@Test
	@DisplayName("a type mismatch at evaluation time is a 400, not an internal 500")
	void evaluationTypeMismatchIsClientError() {
		// contains() on a numeric property parses (best-effort typing) but cannot evaluate.
		OclExpression expression = parser.parseFilter("contains(rating,'x')", productClass);
		EObject product = pkg.getEFactoryInstance().create(productClass);
		product.eSet(productClass.getEStructuralFeature("rating"), 5);
		OclEvaluator evaluator = new OclEvaluator();
		assertThrows(ODataQueryParseException.class,
				() -> evaluator.matchesNullSafe(expression, product));
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
