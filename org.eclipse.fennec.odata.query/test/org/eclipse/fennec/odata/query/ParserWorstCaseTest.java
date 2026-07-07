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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Read-path robustness: pathological-but-legal expressions JUST UNDER the request limits
 * (req §5.1.1: length 4096, nesting 64) must parse in bounded time — this guards against
 * catastrophic ANTLR prediction blowups that a functional test with small inputs would
 * never notice. The generous timeouts only catch exponential behavior, so they hold in CI.
 */
@DisplayName("parser worst cases stay within bounded time at the request limits")
class ParserWorstCaseTest {

	private static final Duration BOUND = Duration.ofSeconds(10);

	private EcoreHelper ecoreHelper;
	private EClass productClass;
	private final ODataQueryParser parser = new ODataQueryParser();

	@BeforeEach
	void setUp() throws Exception {
		Path ecore = findResource("testdata/webshop.ecore",
				"org.eclipse.fennec.odata.query/testdata/webshop.ecore");
		ecoreHelper = new EcoreHelper();
		EPackage pkg = ecoreHelper.loadEcore(ecore);
		productClass = EcoreHelper.getEClass(pkg, "Product");
	}

	@AfterEach
	void tearDown() {
		ecoreHelper.releaseAll();
	}

	@Test
	@DisplayName("nesting just under the depth limit (60 paren levels)")
	void deepNesting() {
		String expression = "(".repeat(60) + "name eq 'x'" + ")".repeat(60);
		assertTimeoutPreemptively(BOUND,
				() -> assertNotNull(parser.parseFilter(expression, productClass)));
	}

	@Test
	@DisplayName("or-chain filling the length limit (~4000 chars)")
	void longOrChain() {
		StringBuilder expression = new StringBuilder("name eq 'x0'");
		while (expression.length() < 3980) {
			expression.append(" or name eq 'x").append(expression.length()).append('\'');
		}
		assertTimeoutPreemptively(BOUND,
				() -> assertNotNull(parser.parseFilter(expression.toString(), productClass)));
	}

	@Test
	@DisplayName("in-list with hundreds of members")
	void wideInList() {
		StringBuilder expression = new StringBuilder("name in ('m0'");
		for (int i = 1; i < 400; i++) {
			expression.append(",'m").append(i).append('\'');
		}
		expression.append(')');
		assertTimeoutPreemptively(BOUND,
				() -> assertNotNull(parser.parseFilter(expression.toString(), productClass)));
	}

	@Test
	@DisplayName("deep arithmetic/comparison mix (alternating precedence levels)")
	void alternatingPrecedence() {
		StringBuilder expression = new StringBuilder("rating add 1");
		for (int i = 0; i < 200; i++) {
			expression.append(" add rating mul 2");
		}
		expression.append(" gt 0");
		assertTimeoutPreemptively(BOUND,
				() -> assertNotNull(parser.parseFilter(expression.toString(), productClass)));
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
