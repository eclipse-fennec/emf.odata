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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.fennec.odata.query.OasisAbnfYaml.Case;
import org.eclipse.fennec.odata.query.antlr.ODataFilterLexer;
import org.eclipse.fennec.odata.query.antlr.ODataFilterParser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * E4-AP-4 acceptance against the official <b>OData Aggregation ABNF Test Cases</b> (vendored
 * {@code testdata/odata-aggregation-testcases.yaml}, source: oasis-tcs/odata-abnf — the
 * CURRENT TC-maintained set). All {@code $apply=} cases run SYNTAX-ONLY against our
 * {@code apply} grammar rule; positives must parse, negatives must be rejected. Constructs
 * outside the v1 pipeline subset (groupby/aggregate/filter/compute) are skipped via
 * assumption — the shrinking skip count tracks the aggregation backlog (BottomTop, Concat,
 * expand/nest, rollup, {@code from}, custom aggregation functions).
 */
class AggregationAbnfAcceptanceTest {

	/** Negatives that hinge on model categories (collection/primitive classes), not syntax. */
	private static final Pattern SEMANTIC_NEGATIVE = Pattern.compile(
			"forbidden|requires method|no two consecutive|collection-valued");

	/** Aggregation constructs the v1 $apply subset deliberately does not cover yet. */
	private static final List<Pattern> UNSUPPORTED = List.of(
			Pattern.compile("\\b(topcount|topsum|toppercent|bottomcount|bottomsum|bottompercent"
					+ "|concat|expand|search|nest|addnested|join|outerjoin|traverse"
					+ "|ancestors|descendants|skip|top|orderby|identity)\\s*\\("),
			Pattern.compile("\\brollup\\s*\\(|\\$all"),          // rollup/$all grouping
			Pattern.compile("\\$it|\\$root|\\$count\\s+"),        // instance refs / $count paths
			Pattern.compile("\\bfrom\\b"),                        // aggregate ... from ...
			Pattern.compile("\\."),                               // qualified/custom functions, casts
			Pattern.compile("@"),                                 // annotations/aliases
			Pattern.compile("&"),                                 // combined query options (URL layer)
			Pattern.compile("%[0-9A-Fa-f]{2}"),                   // percent-encoding (URL layer)
			Pattern.compile("\\baggregate\\s*\\(\\s*\\$count\\b"),// $count WITHOUT alias variants
			Pattern.compile("\\(\\s*\\)"),                        // empty argument lists
			Pattern.compile("aggregate\\((?![^()]*\\bwith\\b)"),    // custom aggregates (no method)
			Pattern.compile("as\\s+\\w+\\s*,\\s*\\w+\\s*\\)"),   // custom aggregate as extra item
			Pattern.compile("\\$these"),                            // 4.01 $these instance ref
			Pattern.compile("\\bidentity\\b"),                     // identity transformation
			Pattern.compile("\\w\\('"));                           // keyed path segments in expressions

	@TestFactory
	List<DynamicTest> oasisAggregationCases() throws Exception {
		List<DynamicTest> tests = new ArrayList<>();
		for (Case testCase : OasisAbnfYaml.load(OasisAbnfYaml.findResource(
				"testdata/odata-aggregation-testcases.yaml",
				"org.eclipse.fennec.odata.query/testdata/odata-aggregation-testcases.yaml"))) {
			if (!"queryOptions".equals(testCase.rule()) || !testCase.input().startsWith("$apply=")) {
				continue;
			}
			String expression = testCase.input().substring("$apply=".length());
			String label = "$apply " + (testCase.negative() ? "x " : "ok ") + expression
					+ " [" + testCase.name() + "]";
			tests.add(DynamicTest.dynamicTest(label, () -> {
				Assumptions.assumeTrue(
						UNSUPPORTED.stream().noneMatch(p -> p.matcher(expression).find()),
						"outside the v1 $apply subset (E4 aggregation backlog)");
				// the aggregation ABNF encodes MODEL categories (collection vs primitive
				// property classes) — negatives depending on them are semantic, not syntactic
				Assumptions.assumeFalse(testCase.negative()
						&& SEMANTIC_NEGATIVE.matcher(testCase.name()).find(),
						"model-category negative — not judgeable syntax-only");
				if (testCase.negative()) {
					assertThrows(ODataQueryParseException.class, () -> parse(expression),
							"grammar must reject: " + expression);
				} else {
					parse(expression);
				}
			}));
		}
		return tests;
	}

	/** Syntax-only: the ANTLR {@code apply} rule without builder/property resolution. */
	private static void parse(String expression) {
		if (expression.isBlank()) {
			throw new ODataQueryParseException("empty expression");
		}
		ODataFilterLexer lexer = new ODataFilterLexer(CharStreams.fromString(expression));
		lexer.removeErrorListeners();
		lexer.addErrorListener(THROWING);
		ODataFilterParser parser = new ODataFilterParser(new CommonTokenStream(lexer));
		parser.removeErrorListeners();
		parser.addErrorListener(THROWING);
		parser.apply();
	}

	private static final BaseErrorListener THROWING = new BaseErrorListener() {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
				int charPositionInLine, String msg, RecognitionException e) {
			throw new ODataQueryParseException("syntax error at " + charPositionInLine + " - " + msg, e);
		}
	};
}
