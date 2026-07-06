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
package org.eclipse.fennec.odata.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Direct tests for the request limits (the security guard that runs before any parsing) —
 * extracted from the servlet exactly so these boundaries are testable in isolation.
 */
@DisplayName("RequestLimits — pre-parse security boundaries")
class RequestLimitsTest {

	private final RequestLimits limits = new RequestLimits(50, 32, 4);

	@Test
	@DisplayName("expression length and nesting boundaries are exact")
	void expressionBoundaries() {
		assertDoesNotThrow(() -> limits.checkExpression("x".repeat(32)));
		assertThrows(ODataQueryParseException.class, () -> limits.checkExpression("x".repeat(33)));

		assertDoesNotThrow(() -> limits.checkExpression("((((x))))")); // depth 4
		assertThrows(ODataQueryParseException.class, () -> limits.checkExpression("(((((x)))))"));
		// unbalanced closers must not underflow the counter into discounting a later bomb
		assertThrows(ODataQueryParseException.class, () -> limits.checkExpression(")))))((((("));
	}

	@Test
	@DisplayName("$top ceiling and paging validation")
	void paging() {
		assertEquals(50, limits.effectiveTop(null), "ceiling applies without a client value");
		assertEquals(50, limits.effectiveTop("999999"), "huge $top is capped");
		assertEquals(7, limits.effectiveTop("7"));
		assertEquals(0, limits.effectiveSkip(null));
		assertEquals(3, limits.effectiveSkip("3"));
		assertThrows(ODataQueryParseException.class, () -> limits.effectiveTop("-1"));
		assertThrows(ODataQueryParseException.class, () -> limits.effectiveSkip("abc"));
	}

	@Test
	@DisplayName("configuration parsing with defaults")
	void configuration() {
		assertEquals(RequestLimits.DEFAULTS, RequestLimits.fromConfiguration(null));
		assertEquals(new RequestLimits(9, 4096, 64),
				RequestLimits.fromConfiguration(Map.of("odata.max.top", "9")));
	}
}
