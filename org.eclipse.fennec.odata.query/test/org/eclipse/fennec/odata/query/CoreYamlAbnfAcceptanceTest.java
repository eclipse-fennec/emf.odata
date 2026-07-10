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
 * values through the grammar).
 *
 * <p>Two kinds of omission, deliberately distinct: cases whose subject belongs to ANOTHER
 * layer (percent-encoding = URL decoding, non-expression options and function-call routing =
 * servlet, model-category negatives = not judgeable syntax-only) are NOT GENERATED at all —
 * they can never become green here and would only blur the numbers. Cases for PLANNED parser
 * features ({@code OUT_OF_SCOPE} vs {@code BACKLOG} lists below) are skipped via assumption —
 * that shrinking skip count IS the E4/ADR-0005 backlog radar. Rules without a v1 entry point
 * (literals, headers, preferences, context fragments, select/expand trees, $search, geo) are
 * not generated either; they join once the owning layer exists.
 */
class CoreYamlAbnfAcceptanceTest {

	/** Rules that map onto our expression grammar entry points (filter/orderby). */
	private static final Set<String> EXPRESSION_RULES = Set.of(
			"filter", "boolCommonExpr", "boolcommonExpr", "commonExpr",
			"firstMemberExpr", "propertyPathExpr", "isofExpr", "notExpr", "orderby");

	/** Expression constructs the grammar deliberately does not cover YET (E4 backlog). */
	private static final List<Pattern> BACKLOG = List.of(
			Pattern.compile("(?i)\\bgeo(graphy|metry)?\\s*'"),       // spatial literals
			Pattern.compile("\\bdiv\\s+by\\b"),                      // spaced "div by" (only divby is one keyword)
			Pattern.compile("\\bgeo\\.\\w"));                          // geo.* built-ins on literals we lack

	/** Expression cases owned by ANOTHER layer — never judgeable here, not generated. */
	private static final List<Pattern> OUT_OF_SCOPE = List.of(
			Pattern.compile("%[0-9A-Fa-f]{2}"));                     // percent-encoding = URL decoding

	/**
	 * A NEGATIVE whose input is one bare qualified name: whether that is an (invalid)
	 * parenless function or a (valid) terminal type cast is a MODEL question — not
	 * judgeable syntax-only, so the case is not generated.
	 */
	private static final Pattern BARE_QUALIFIED_NAME =
			Pattern.compile("^[A-Za-z_]\\w*(\\.[A-Za-z_]\\w*)+$");

	/** Resource-path gaps of the parser itself (ADR-0005 backlog). */
	private static final List<Pattern> BACKLOG_PATHS = List.of(
			Pattern.compile("/[A-Za-z_]\\w*'"),        // key-as-segment with a RAW apostrophe (O'Neil)
			Pattern.compile("\\{"));                   // JSON values in key predicates

	/**
	 * Path cases owned by ANOTHER layer, not generated: function-call segments are routed by
	 * the SERVLET before the resource parser runs (ADR-0005: the parser deliberately does not
	 * model them), {@code $metadata}/{@code $batch}/… are servlet routes, slashes inside
	 * string keys and percent-encoding are URL-decoding concerns.
	 */
	private static final List<Pattern> OUT_OF_SCOPE_PATHS = List.of(
			Pattern.compile("\\.[\\w.]*\\("),          // qualified function/action call segments
			Pattern.compile("\\(\\s*\\)"),             // parameterless function/action call segments
			Pattern.compile("\\)\\s*\\("),             // composed function calls Fn(...)(key)
			Pattern.compile("\\$(metadata|batch|root|filter|each|query)"),
			Pattern.compile("'[^']*/"),                // slash inside string key (URL-decoding layer)
			Pattern.compile("%[0-9A-Fa-f]{2}"));       // percent-encoding = URL-decoding layer

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
		int omitted = 0;
		for (Case c : cases()) {
			if (!EXPRESSION_RULES.contains(c.rule())) {
				continue;
			}
			if (outOfScope(OUT_OF_SCOPE, c.input()) || queryOptionLevelNegative(c)
					|| (c.negative() && BARE_QUALIFIED_NAME.matcher(c.input().trim()).matches())) {
				omitted++;
				continue;
			}
			tests.add(DynamicTest.dynamicTest(label(c), () -> runExpressionCase(c)));
		}
		omitted("expression", omitted);
		return tests;
	}

	@TestFactory
	List<DynamicTest> resourcePathCases() throws Exception {
		List<DynamicTest> tests = new ArrayList<>();
		int omitted = 0;
		for (Case c : cases()) {
			if (!"resourcePath".equals(c.rule())) {
				continue;
			}
			if (outOfScope(OUT_OF_SCOPE_PATHS, c.input())
					|| (c.negative() && SEMANTIC_NEGATIVE_PATH.matcher(c.name()).find())) {
				omitted++;
				continue;
			}
			tests.add(DynamicTest.dynamicTest(label(c), () -> {
				assumePathJudgeable(c.input());
				if (c.negative()) {
					assertThrows(ODataQueryParseException.class, () -> resourceParser.parse(c.input()),
							"parser must reject: " + c.input());
				} else {
					resourceParser.parse(c.input());
				}
			}));
		}
		omitted("resourcePath", omitted);
		return tests;
	}

	@TestFactory
	List<DynamicTest> queryOptionsCases() throws Exception {
		List<DynamicTest> tests = new ArrayList<>();
		int omitted = 0;
		for (Case c : cases()) {
			if (!"queryOptions".equals(c.rule())) {
				continue;
			}
			boolean judgeable = c.negative()
					? judgeableOption(optionAt(c.input(), c.failAt()))
					: hasJudgeableOption(c.input());
			if (!judgeable) {
				omitted++; // non-expression options ($top, $search, $expand trees, …) = servlet layer
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
		omitted("queryOptions", omitted);
		return tests;
	}

	@TestFactory
	List<DynamicTest> relativeUriCases() throws Exception {
		List<DynamicTest> tests = new ArrayList<>();
		int omitted = 0;
		for (Case c : cases()) {
			if (!"odataRelativeUri".equals(c.rule())) {
				continue;
			}
			String uri = c.input();
			int q = uri.indexOf('?');
			String path = q < 0 ? uri : uri.substring(0, q);
			boolean pathOut = outOfScope(OUT_OF_SCOPE_PATHS, path);
			boolean judgeable;
			if (!c.negative()) {
				judgeable = !pathOut;
			} else if (q < 0 || c.failAt() <= q) {
				judgeable = !pathOut && !SEMANTIC_NEGATIVE_PATH.matcher(c.name()).find();
			} else {
				judgeable = judgeableOption(optionAt(uri.substring(q + 1), c.failAt() - q - 1));
			}
			if (!judgeable) {
				omitted++;
				continue;
			}
			tests.add(DynamicTest.dynamicTest(label(c), () -> runRelativeUri(c)));
		}
		omitted("odataRelativeUri", omitted);
		return tests;
	}

	// --- generation-time scoping (documented omission, never a hidden gap) ---

	private static boolean outOfScope(List<Pattern> patterns, String input) {
		return patterns.stream().anyMatch(p -> p.matcher(input).find());
	}

	/**
	 * Negatives whose defect is the OPTION SYNTAX itself ({@code $filter =…} with a space):
	 * that is URL-layer syntax the URI parser owns, not the expression grammar's.
	 */
	private static boolean queryOptionLevelNegative(Case c) {
		return c.negative() && QUERY_OPTION_PREFIX.matcher(c.input()).lookingAt()
				&& Pattern.compile("=\\s").matcher(c.input()).find();
	}

	/** An option this bundle can judge: $filter/$orderby with a value we own (no URL escapes). */
	private static boolean judgeableOption(String option) {
		int eq = option.indexOf('=');
		return eq > 0 && SUPPORTED_OPTION.matcher(option.substring(0, eq)).matches()
				&& !outOfScope(OUT_OF_SCOPE, option.substring(eq + 1));
	}

	private static boolean hasJudgeableOption(String query) {
		for (String option : query.split("&", -1)) {
			if (judgeableOption(option)) {
				return true;
			}
		}
		return false;
	}

	/** Documents how many cases a factory left out because another layer owns them. */
	private static void omitted(String factory, int count) {
		if (count > 0) {
			System.out.println("[CoreYamlAbnf] " + factory + ": " + count
					+ " cases not generated — owned by the URL/servlet layer or not judgeable syntax-only");
		}
	}

	// --- odataRelativeUri: the '?' split is the URI parser's URL-layer contract ---

	private void runRelativeUri(Case c) {
		String uri = c.input();
		int q = uri.indexOf('?');
		String path = q < 0 ? uri : uri.substring(0, q);
		if (!c.negative()) {
			assumePathJudgeable(path);
			resourceParser.parse(path);
			// the query part only participates when it carries an option we own — a foreign
			// option ($top, $search, …) is the servlet's subject, the parsed PATH still counts
			if (q >= 0 && hasJudgeableOption(uri.substring(q + 1))) {
				runOptionsPositive(uri.substring(q + 1));
			}
		} else if (q < 0 || c.failAt() <= q) {
			// invalid portion starts in the resource path
			assumePathJudgeable(path);
			assertThrows(ODataQueryParseException.class, () -> resourceParser.parse(path),
					"parser must reject: " + path);
		} else {
			// invalid portion starts inside one query option — judge exactly that option
			runOptionNegative(optionAt(uri.substring(q + 1), c.failAt() - q - 1));
		}
	}

	private void assumePathJudgeable(String path) {
		Assumptions.assumeTrue(!path.isBlank()
				&& BACKLOG_PATHS.stream().noneMatch(p -> p.matcher(path).find()),
				"resource-path parser backlog (ADR-0005)");
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
		Assumptions.assumeTrue(BACKLOG.stream().noneMatch(p -> p.matcher(input).find()),
				"expression grammar backlog (E4)");
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
