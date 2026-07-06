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
import java.util.Set;
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
 * Acceptance against the CURRENT TC-maintained <b>OData ABNF Test Cases</b> YAML (vendored
 * {@code testdata/odata-abnf-testcases.yaml}, source: oasis-tcs/odata-abnf — 840 cases, a
 * superset of the 4.01 OS XML set that {@link AbnfAcceptanceTest} runs, incl. 4.02
 * preparations). Everything runs SYNTAX-ONLY: positives must parse, negatives ({@code FailAt})
 * must be rejected.
 *
 * <p>Beyond the XML harness this also exercises the URL layer the ADR-0005 URI parser owns:
 * {@code odataRelativeUri} (path?query splitting) and {@code queryOptions} ($filter/$orderby
 * values through the grammar; other options are outside the query bundle and skipped).
 * Constructs outside the v1 subset are SKIPPED via assumption — the shrinking skip count is
 * the E4/ADR-0005 backlog radar. Rules without a v1 entry point (literals, headers,
 * preferences, context fragments, select/expand trees, $search, geo) are not generated at all;
 * they join once the owning layer exists.
 */
class CoreYamlAbnfAcceptanceTest {

	/** Rules that map onto our expression grammar entry points (filter/orderby). */
	private static final Set<String> EXPRESSION_RULES = Set.of(
			"filter", "boolCommonExpr", "boolcommonExpr", "commonExpr",
			"firstMemberExpr", "propertyPathExpr", "isofExpr", "notExpr", "orderby");

	/** Constructs the v1 grammar subset deliberately does not cover yet (E4 backlog). */
	private static final List<Pattern> UNSUPPORTED = List.of(
			Pattern.compile("\\$(it|this|root|these)"),              // instance refs
			Pattern.compile("\\$count\\(|\\$filter\\("),             // filtered $count / inline $filter segments
			Pattern.compile("@\\w+\\."),                             // @Ns.Term annotation refs (plain @alias parses)
			Pattern.compile("/@|@\\w+/"),                            // instance-annotation values in paths
			Pattern.compile("-\\s*[A-Za-z_(]"),                      // unary minus on expressions
			Pattern.compile("(?i)\\bgeo(graphy|metry)?\\s*'"),       // spatial literals
			Pattern.compile("\\bbinary'"),                           // binary literals
			Pattern.compile("[A-Za-z_][\\w.]*'[^']*,"),              // enum FLAG combinations (comma list)
			Pattern.compile("[Nn][Aa][Nn]|INF"),                     // nanInfinity literals
			Pattern.compile("\\bdiv\\s+by\\b"),                      // spaced "div by" (only divby is one keyword)
			Pattern.compile("\\.[\\w.]*\\("),                        // namespace-qualified function calls
			Pattern.compile("(^|/)[A-Za-z_]\\w*\\.[A-Za-z_][\\w.]*(/|$| )"), // type-cast path segments
			Pattern.compile("\"|\\{|\\["),                           // JSON-ish / string-with-quote forms
			Pattern.compile("%[0-9A-Fa-f]{2}"),                      // percent-encoding = URL layer (URI parser)
			Pattern.compile("/(?!any\\(|all\\()[A-Za-z_]\\w*\\("));  // bound/composed functions in paths (not lambdas)

	/** v1 resource-path subset gaps (ADR-0005 backlog): functions, multi-part keys, ... */
	private static final List<Pattern> UNSUPPORTED_PATHS = List.of(
			Pattern.compile("\\.[\\w.]*\\("),          // qualified function/action calls (casts parse)
			Pattern.compile("\\(\\s*\\w+\\s*="),       // named/multi-part key predicates (ID=1)
			Pattern.compile("\\([^)]*,"),              // multi-part keys / function parameters
			Pattern.compile("\\(\\s*\\)"),             // parameterless function/action call segments
			Pattern.compile("@"),                      // key aliases
			Pattern.compile("\\$(all|crossjoin|entity|metadata|batch|root|filter|each|query)"),
			Pattern.compile("'[^']*/"),                // slash inside string key (URL-decoding layer)
			Pattern.compile("%[0-9A-Fa-f]{2}"),        // percent-encoding = URL-decoding layer
			Pattern.compile("/-?\\d"),                 // Key-as-Segment / ordered-collection index
			Pattern.compile("/[^/()]*'"),              // Key-as-Segment with string key
			Pattern.compile("=|\\{"));                 // parameter assignments / JSON in path

	/**
	 * Path negatives that hinge on model categories from the TC {@code Constraints} block
	 * ($value on a complex/stream property) — not judgeable syntax-only.
	 */
	private static final Pattern SEMANTIC_NEGATIVE_PATH =
			Pattern.compile("- complex$|does not make sense");

	/** Query options whose values the query bundle parses; $apply has its own harness. */
	private static final Pattern SUPPORTED_OPTION = Pattern.compile("(?i)^\\$?(filter|orderby)$");

	private final ODataResourceParser resourceParser = new ODataResourceParser();

	@TestFactory
	List<DynamicTest> expressionCases() throws Exception {
		List<DynamicTest> tests = new ArrayList<>();
		for (Case c : cases()) {
			if (!EXPRESSION_RULES.contains(c.rule())) {
				continue;
			}
			tests.add(DynamicTest.dynamicTest(label(c), () -> runExpressionCase(c)));
		}
		return tests;
	}

	@TestFactory
	List<DynamicTest> resourcePathCases() throws Exception {
		List<DynamicTest> tests = new ArrayList<>();
		for (Case c : cases()) {
			if (!"resourcePath".equals(c.rule())) {
				continue;
			}
			tests.add(DynamicTest.dynamicTest(label(c), () -> {
				assumePathJudgeable(c.input());
				if (c.negative()) {
					assumePathNegativeJudgeable(c);
					assertThrows(ODataQueryParseException.class, () -> resourceParser.parse(c.input()),
							"parser must reject: " + c.input());
				} else {
					resourceParser.parse(c.input());
				}
			}));
		}
		return tests;
	}

	@TestFactory
	List<DynamicTest> queryOptionsCases() throws Exception {
		List<DynamicTest> tests = new ArrayList<>();
		for (Case c : cases()) {
			if (!"queryOptions".equals(c.rule())) {
				continue;
			}
			tests.add(DynamicTest.dynamicTest(label(c), () -> {
				if (c.negative()) {
					runOptionNegative(optionAt(c.input(), c.failAt()));
				} else {
					runOptionsPositive(c.input());
				}
			}));
		}
		return tests;
	}

	@TestFactory
	List<DynamicTest> relativeUriCases() throws Exception {
		List<DynamicTest> tests = new ArrayList<>();
		for (Case c : cases()) {
			if (!"odataRelativeUri".equals(c.rule())) {
				continue;
			}
			tests.add(DynamicTest.dynamicTest(label(c), () -> runRelativeUri(c)));
		}
		return tests;
	}

	// --- odataRelativeUri: the '?' split is the URI parser's URL-layer contract ---

	private void runRelativeUri(Case c) {
		String uri = c.input();
		int q = uri.indexOf('?');
		String path = q < 0 ? uri : uri.substring(0, q);
		if (!c.negative()) {
			assumePathJudgeable(path);
			resourceParser.parse(path);
			if (q >= 0) {
				runOptionsPositive(uri.substring(q + 1));
			}
		} else if (q < 0 || c.failAt() <= q) {
			// invalid portion starts in the resource path
			assumePathJudgeable(path);
			assumePathNegativeJudgeable(c);
			assertThrows(ODataQueryParseException.class, () -> resourceParser.parse(path),
					"parser must reject: " + path);
		} else {
			// invalid portion starts inside one query option — judge exactly that option
			runOptionNegative(optionAt(uri.substring(q + 1), c.failAt() - q - 1));
		}
	}

	private void assumePathJudgeable(String path) {
		Assumptions.assumeTrue(!path.isBlank()
				&& UNSUPPORTED_PATHS.stream().noneMatch(p -> p.matcher(path).find()),
				"outside the v1 resource-path subset (ADR-0005 backlog)");
	}

	private void assumePathNegativeJudgeable(Case c) {
		Assumptions.assumeFalse(SEMANTIC_NEGATIVE_PATH.matcher(c.name()).find(),
				"model-category negative — not judgeable syntax-only");
	}

	// --- queryOptions: name=value pairs, expression-bearing values through the grammar ---

	/** The option containing 0-based position {@code at} of the raw query string. */
	private static String optionAt(String query, int at) {
		int begin = query.lastIndexOf('&', Math.min(Math.max(at, 0), query.length() - 1)) + 1;
		int end = query.indexOf('&', begin);
		return end < 0 ? query.substring(begin) : query.substring(begin, end);
	}

	/** Every v1-parsable option value must parse; foreign options pass through untouched. */
	private void runOptionsPositive(String query) {
		boolean judged = false;
		for (String option : query.split("&", -1)) {
			int eq = option.indexOf('=');
			if (eq <= 0 || !SUPPORTED_OPTION.matcher(option.substring(0, eq)).matches()) {
				continue;
			}
			String name = option.substring(0, eq);
			String value = option.substring(eq + 1);
			assumeExpressionJudgeable(value);
			parse(name.toLowerCase().endsWith("orderby") ? "orderby" : "filter", value);
			judged = true;
		}
		Assumptions.assumeTrue(judged, "no v1-parsable query option (URL/servlet layer)");
	}

	/** Negative located in {@code option}: judge only if it is ours, then it must be rejected. */
	private void runOptionNegative(String option) {
		int eq = option.indexOf('=');
		Assumptions.assumeTrue(eq > 0 && SUPPORTED_OPTION.matcher(option.substring(0, eq)).matches(),
				"failing option is outside the query bundle (URL/servlet layer)");
		String rule = option.substring(0, eq).toLowerCase().endsWith("orderby") ? "orderby" : "filter";
		String value = option.substring(eq + 1);
		assumeExpressionJudgeable(value);
		assertThrows(ODataQueryParseException.class, () -> parse(rule, value),
				"grammar must reject: " + value);
	}

	// --- expression rules ---

	private void runExpressionCase(Case c) {
		// the query-option name itself ($ optional, case-insensitive, no space before the value)
		// is URL-layer syntax — the URI parser's job, not the expression grammar's
		Assumptions.assumeFalse(c.negative() && QUERY_OPTION_PREFIX.matcher(c.input()).lookingAt()
				&& Pattern.compile("=\\s").matcher(c.input()).find(),
				"query-option-level case (URI parser layer)");
		String input = QUERY_OPTION_PREFIX.matcher(c.input()).replaceFirst("");
		assumeExpressionJudgeable(input);
		if (c.negative()) {
			assertThrows(ODataQueryParseException.class, () -> parse(c.rule(), input),
					"grammar must reject: " + input);
		} else {
			parse(c.rule(), input);
		}
	}

	private void assumeExpressionJudgeable(String input) {
		Assumptions.assumeTrue(UNSUPPORTED.stream().noneMatch(p -> p.matcher(input).find()),
				"outside the v1 grammar subset (E4 backlog)");
	}

	private static final Pattern QUERY_OPTION_PREFIX =
			Pattern.compile("(?i)^\\$?(filter|orderby)=");

	private static String label(Case c) {
		return c.rule() + (c.negative() ? " x " : " ok ") + c.input() + " [" + c.name() + "]";
	}

	private static List<Case> cases() throws Exception {
		return OasisAbnfYaml.load(OasisAbnfYaml.findResource("testdata/odata-abnf-testcases.yaml",
				"org.eclipse.fennec.odata.query/testdata/odata-abnf-testcases.yaml"));
	}

	/** Syntax-only: run the ANTLR grammar without OCL building / property resolution. */
	private static void parse(String rule, String input) {
		if (input.isBlank()) {
			throw new ODataQueryParseException("empty expression");
		}
		ODataFilterLexer lexer = new ODataFilterLexer(CharStreams.fromString(input));
		lexer.removeErrorListeners();
		lexer.addErrorListener(THROWING);
		ODataFilterParser parser = new ODataFilterParser(new CommonTokenStream(lexer));
		parser.removeErrorListeners();
		parser.addErrorListener(THROWING);
		if ("orderby".equals(rule)) {
			parser.orderby();
		} else {
			parser.filter();
		}
	}

	private static final BaseErrorListener THROWING = new BaseErrorListener() {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
				int charPositionInLine, String msg, RecognitionException e) {
			throw new ODataQueryParseException("syntax error at " + line + ":" + charPositionInLine + " - " + msg, e);
		}
	};
}
