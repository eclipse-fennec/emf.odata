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
 * outside the pipeline submodel are skipped via assumption — the shrinking skip count tracks
 * the aggregation backlog (search, expand/nest/addnested, join/outerjoin, ancestors/
 * descendants/traverse, rolluprecursive, {@code $these}/{@code $root}, type casts in
 * aggregation paths, custom aggregation FUNCTIONS).
 */
class AggregationAbnfAcceptanceTest {

	/** Negatives that hinge on model categories (collection/primitive classes), not syntax. */
	private static final Pattern SEMANTIC_NEGATIVE = Pattern.compile(
			"forbidden|requires method|requires with|no two consecutive|collection-valued");

	/** Aggregation constructs the $apply submodel deliberately does not cover YET (backlog). */
	private static final List<Pattern> BACKLOG = List.of(
			Pattern.compile("\\b(expand|search|nest|addnested|join|outerjoin|traverse"
					+ "|ancestors|descendants|rolluprecursive)\\s*\\("),
			Pattern.compile("\\$all"),                            // $all grouping
			Pattern.compile("\\$it|\\$root|\\$these"),            // instance refs
			Pattern.compile("[A-Za-z_]\\w*\\.[A-Za-z_]\\w*\\s*\\("), // qualified custom FUNCTIONS
			// qualified names OUTSIDE 'with' clauses = type casts in paths (aggrCastPath)
			Pattern.compile("(?<!with )\\b[A-Za-z_]\\w*\\.[A-Za-z_]"),
			Pattern.compile("@"),                                 // annotations / parameter aliases
			Pattern.compile("\\w\\('"));                          // keyed path segments in expressions

	/** Cases owned by ANOTHER layer — never judgeable here, not generated. */
	private static final List<Pattern> OUT_OF_SCOPE = List.of(
			Pattern.compile("&"),                                 // combined query options (URL layer)
			Pattern.compile("%[0-9A-Fa-f]{2}"));                  // percent-encoding (URL layer)

	@TestFactory
	List<DynamicTest> oasisAggregationCases() throws Exception {
		List<DynamicTest> tests = new ArrayList<>();
		int omitted = 0;
		for (Case testCase : OasisAbnfYaml.load(OasisAbnfYaml.findResource(
				"testdata/odata-aggregation-testcases.yaml",
				"org.eclipse.fennec.odata.query/testdata/odata-aggregation-testcases.yaml"))) {
			if (!"queryOptions".equals(testCase.rule()) || !testCase.input().startsWith("$apply=")) {
				continue;
			}
			String expression = testCase.input().substring("$apply=".length());
			// another layer's subject (combined options, percent-encoding) or a MODEL-category
			// negative (the aggregation ABNF encodes collection/primitive property classes in
			// rule names) — never judgeable in this syntax-only harness, so not generated
			if (OUT_OF_SCOPE.stream().anyMatch(p -> p.matcher(expression).find())
					|| (testCase.negative() && SEMANTIC_NEGATIVE.matcher(testCase.name()).find())) {
				omitted++;
				continue;
			}
			String label = "$apply " + (testCase.negative() ? "x " : "ok ") + expression
					+ " [" + testCase.name() + "]";
			tests.add(DynamicTest.dynamicTest(label, () -> {
				Assumptions.assumeTrue(
						BACKLOG.stream().noneMatch(p -> p.matcher(expression).find()),
						"$apply submodel backlog (E4 aggregation)");
				if (testCase.negative()) {
					assertThrows(ODataQueryParseException.class, () -> parse(expression),
							"grammar must reject: " + expression);
				} else {
					parse(expression);
				}
			}));
		}
		if (omitted > 0) {
			System.out.println("[AggregationAbnf] " + omitted
					+ " cases not generated — URL layer or model-category negatives");
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
