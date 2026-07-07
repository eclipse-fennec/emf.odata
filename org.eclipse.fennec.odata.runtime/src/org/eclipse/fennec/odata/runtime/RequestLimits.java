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

import java.util.Map;

import org.eclipse.fennec.odata.query.ODataQueryParseException;

/**
 * The hard per-request limits (req §4.5, Q6 defaults) — enforced BEFORE any parsing so that
 * hostile input is rejected at O(n) string-scan cost. Extracted from the servlet so the checks
 * are unit-testable in isolation and can move into the planned {@code ODataRequestFilter}
 * (req §5.1.1) without touching dispatch logic.
 *
 * @param maxTop              enforced {@code $top} ceiling (applies even without a client value)
 * @param maxExpressionLength maximum length of any expression query option
 * @param maxNestingDepth     maximum parenthesis nesting (parser-bomb guard)
 * @param maxBodyBytes        maximum accepted request-body size for writes
 */
public record RequestLimits(int maxTop, int maxExpressionLength, int maxNestingDepth,
		int maxBodyBytes) {

	public static final RequestLimits DEFAULTS = new RequestLimits(1000, 4096, 64, 1_048_576);

	/** Limits without a body cap change (compatibility for read-only setups). */
	public RequestLimits(int maxTop, int maxExpressionLength, int maxNestingDepth) {
		this(maxTop, maxExpressionLength, maxNestingDepth, DEFAULTS_BODY_BYTES);
	}

	private static final int DEFAULTS_BODY_BYTES = 1_048_576;

	/** Reads limits from component configuration, falling back to {@link #DEFAULTS}. */
	public static RequestLimits fromConfiguration(Map<String, Object> configuration) {
		return new RequestLimits(
				intValue(configuration, "odata.max.top", DEFAULTS.maxTop()),
				intValue(configuration, "odata.max.expression.length", DEFAULTS.maxExpressionLength()),
				intValue(configuration, "odata.max.nesting.depth", DEFAULTS.maxNestingDepth()),
				intValue(configuration, "odata.max.body.size", DEFAULTS.maxBodyBytes()));
	}

	private static int intValue(Map<String, Object> configuration, String key, int fallback) {
		Object value = configuration == null ? null : configuration.get(key);
		return value == null ? fallback : Integer.parseInt(String.valueOf(value));
	}

	/** Validates an expression BEFORE parsing; throws the 400-mapped parse exception. */
	public void checkExpression(String expression) {
		if (expression.length() > maxExpressionLength) {
			throw new ODataQueryParseException(
					"expression exceeds the maximum length of " + maxExpressionLength);
		}
		int depth = 0;
		int deepest = 0;
		for (int i = 0; i < expression.length(); i++) {
			char c = expression.charAt(i);
			if (c == '(') {
				deepest = Math.max(deepest, ++depth);
			} else if (c == ')' && depth > 0) {
				depth--; // never below 0: unbalanced closers must not discount later opens
			}
		}
		if (deepest > maxNestingDepth) {
			throw new ODataQueryParseException(
					"expression exceeds the maximum nesting depth of " + maxNestingDepth);
		}
	}

	/** The effective {@code $top}: the client value capped at the ceiling; ceiling when absent. */
	public int effectiveTop(String requestedTop) {
		return Math.min(nonNegativeInt(requestedTop, maxTop), maxTop);
	}

	/** The effective {@code $skip} (0 when absent); negative or non-numeric values → 400. */
	public int effectiveSkip(String requestedSkip) {
		return nonNegativeInt(requestedSkip, 0);
	}

	private static int nonNegativeInt(String value, int fallback) {
		if (value == null) {
			return fallback;
		}
		try {
			int parsed = Integer.parseInt(value);
			if (parsed < 0) {
				throw new ODataQueryParseException("paging options must be non-negative");
			}
			return parsed;
		} catch (NumberFormatException e) {
			throw new ODataQueryParseException("invalid paging option value");
		}
	}
}
