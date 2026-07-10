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
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.fennec.odata.query.antlr.ODataFilterLexer;
import org.eclipse.fennec.odata.query.antlr.ODataFilterParser;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * E4-AP-5: grammar acceptance against the official OASIS <b>OData ABNF Test Cases 4.01</b>
 * (vendored {@code testdata/odata-abnf-testcases.xml}, req §3.6/§10.5 — mandatory test input).
 * Runs every case of the expression rules ({@code filter}, {@code boolCommonExpr},
 * {@code commonExpr}, {@code orderby}) SYNTAX-ONLY against our ANTLR grammar: positive cases
 * must parse, negative cases ({@code FailAt}) must be rejected.
 *
 * <p>Cases using constructs outside the v1 grammar subset (lambdas, cast/isof, typed literals,
 * {@code $count}/{@code $it}/{@code $root}, aliases, unary minus, …) are SKIPPED via assumption
 * — visible in the report, and shrinking as the E4 backlog (odata-e4-query-open-points.md)
 * closes. Semantic validation (property/function resolution) is deliberately out of scope here;
 * that is {@link ODataQueryParserTest}'s job.
 */
class AbnfAcceptanceTest {

	private static final Set<String> EXPRESSION_RULES = Set.of(
			"filter", "boolCommonExpr", "commonExpr", "orderby");

	/** Constructs the expression grammar deliberately does not cover YET (E4 backlog). */
	private static final List<Pattern> BACKLOG = List.of(
			Pattern.compile("\\$(it|this|root)"),                      // $it/$this/$root instance refs
			Pattern.compile("\\$count\\(|\\$filter\\("),             // filtered $count / inline $filter segments
			Pattern.compile("@\\w+\\."),                             // @Ns.Term annotation refs (plain @alias parses)
			Pattern.compile("/@|@\\w+/"),                            // instance-annotation values in paths
			Pattern.compile("-\\s*[A-Za-z_(]"),                      // unary minus on expressions
			Pattern.compile("(?i)\\bgeo(graphy|metry)?\\s*'"),       // spatial literals
			Pattern.compile("\\bbinary'"),                           // binary literals
			Pattern.compile("[A-Za-z_][\\w.]*'[^']*,"),              // enum FLAG combinations (comma list)
			Pattern.compile("[Nn][Aa][Nn]|INF"),                     // nanInfinity literals
			Pattern.compile("\\bdiv\\s+by\\b"),                      // spaced "div by" (only divby is one keyword)
			Pattern.compile("(^|/)[A-Za-z_]\\w*\\.[A-Za-z_][\\w.]*(/|$| )"), // type-cast path segments
			Pattern.compile("\"|\\{|\\["));                          // JSON-ish / string-with-quote forms

	/** Expression cases owned by ANOTHER layer — never judgeable here, not generated. */
	private static final List<Pattern> OUT_OF_SCOPE = List.of(
			Pattern.compile("%[0-9A-Fa-f]{2}"));                     // percent-encoding = URL decoding

	/** Resource-path gaps of the parser itself (ADR-0005 backlog). */
	private static final List<Pattern> BACKLOG_PATHS = List.of(
			Pattern.compile("@"),                      // key aliases
			Pattern.compile("\\$(all|crossjoin|entity)"), // advanced URL forms (backlog package)
			Pattern.compile("/[A-Za-z_]\\w*'"),        // key-as-segment with a RAW apostrophe (O'Neil)
			Pattern.compile("\\{"));                   // JSON values in key predicates

	/**
	 * Path cases owned by ANOTHER layer, not generated: function-call segments are routed by
	 * the SERVLET (ADR-0005: the resource parser deliberately does not model them), the
	 * {@code $metadata}/{@code $batch}/… forms are servlet routes, slashes inside string keys
	 * and percent-encoding are URL-decoding concerns.
	 */
	private static final List<Pattern> OUT_OF_SCOPE_PATHS = List.of(
			Pattern.compile("\\.[\\w.]*\\("),          // qualified function/action call segments
			Pattern.compile("\\(\\s*\\)"),             // parameterless function/action call segments
			Pattern.compile("\\)\\s*\\("),             // composed function calls Fn(...)(key)
			Pattern.compile("\\$(metadata|batch|root|filter|each|query)"),
			Pattern.compile("'[^']*/"),                // slash inside string key (URL-decoding layer)
			Pattern.compile("%[0-9A-Fa-f]{2}"));       // percent-encoding = URL-decoding layer

	@TestFactory
	List<DynamicTest> oasisAbnfResourcePathCases() throws Exception {
		Path xml = findResource("testdata/odata-abnf-testcases.xml",
				"org.eclipse.fennec.odata.query/testdata/odata-abnf-testcases.xml");
		NodeList nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(Files.newInputStream(xml)).getElementsByTagName("TestCase");

		ODataResourceParser resourceParser = new ODataResourceParser();
		List<DynamicTest> tests = new ArrayList<>();
		int omitted = 0;
		for (int i = 0; i < nodes.getLength(); i++) {
			Element testCase = (Element) nodes.item(i);
			if (!"resourcePath".equals(testCase.getAttribute("Rule"))) {
				continue;
			}
			NodeList inputs = testCase.getElementsByTagName("Input");
			if (inputs.getLength() == 0) {
				continue;
			}
			String input = inputs.item(0).getTextContent();
			boolean negative = testCase.hasAttribute("FailAt");
			if (OUT_OF_SCOPE_PATHS.stream().anyMatch(p -> p.matcher(input).find())) {
				omitted++; // servlet-routed forms / URL-decoding concerns — never judgeable here
				continue;
			}
			String label = "resourcePath " + (negative ? "x " : "ok ") + input
					+ " [" + testCase.getAttribute("Name") + "]";
			tests.add(DynamicTest.dynamicTest(label, () -> {
				Assumptions.assumeTrue(BACKLOG_PATHS.stream().noneMatch(p -> p.matcher(input).find()),
						"resource-path parser backlog (ADR-0005)");
				if (negative) {
					assertThrows(ODataQueryParseException.class, () -> resourceParser.parse(input),
							"parser must reject: " + input);
				} else {
					resourceParser.parse(input);
				}
			}));
		}
		omitted("resourcePath", omitted);
		return tests;
	}

	@TestFactory
	List<DynamicTest> oasisAbnfExpressionCases() throws Exception {
		Path xml = findResource("testdata/odata-abnf-testcases.xml",
				"org.eclipse.fennec.odata.query/testdata/odata-abnf-testcases.xml");
		NodeList nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.parse(Files.newInputStream(xml)).getElementsByTagName("TestCase");

		List<DynamicTest> tests = new ArrayList<>();
		int omitted = 0;
		for (int i = 0; i < nodes.getLength(); i++) {
			Element testCase = (Element) nodes.item(i);
			String rule = testCase.getAttribute("Rule");
			if (!EXPRESSION_RULES.contains(rule)) {
				continue;
			}
			NodeList inputs = testCase.getElementsByTagName("Input");
			if (inputs.getLength() == 0) {
				continue;
			}
			String input = inputs.item(0).getTextContent();
			boolean negative = testCase.hasAttribute("FailAt");
			if (OUT_OF_SCOPE.stream().anyMatch(p -> p.matcher(input).find())
					|| queryOptionLevelNegative(input, negative)) {
				omitted++;
				continue;
			}
			String label = rule + (negative ? " ✗ " : " ✓ ") + input
					+ " [" + testCase.getAttribute("Name") + "]";
			tests.add(DynamicTest.dynamicTest(label, () -> runCase(rule, input, negative)));
		}
		omitted("expressions", omitted);
		return tests;
	}

	/**
	 * Negatives whose defect is the OPTION SYNTAX itself ({@code $filter =…} with a space):
	 * URL-layer syntax the URI parser owns, not the expression grammar's.
	 */
	private static boolean queryOptionLevelNegative(String input, boolean negative) {
		return negative && QUERY_OPTION_PREFIX.matcher(input).lookingAt()
				&& Pattern.compile("=\\s").matcher(input).find();
	}

	/** Documents how many cases a factory left out because another layer owns them. */
	private static void omitted(String factory, int count) {
		if (count > 0) {
			System.out.println("[XmlAbnf] " + factory + ": " + count
					+ " cases not generated — owned by the URL/servlet layer");
		}
	}

	private void runCase(String rule, String rawInput, boolean negative) {
		String input = stripQueryOptionPrefix(rawInput);
		if (!negative) {
			Assumptions.assumeTrue(BACKLOG.stream().noneMatch(p -> p.matcher(input).find()),
					"expression grammar backlog (E4)");
			parse(rule, input);
		} else {
			Assumptions.assumeTrue(BACKLOG.stream().noneMatch(p -> p.matcher(input).find()),
					"negative case about a backlog construct — not judgeable yet");
			assertThrows(ODataQueryParseException.class, () -> parse(rule, input),
					"grammar must reject: " + input);
		}
	}

	private static final Pattern QUERY_OPTION_PREFIX =
			Pattern.compile("(?i)^\\$?(filter|orderby)=");

	private static String stripQueryOptionPrefix(String input) {
		return QUERY_OPTION_PREFIX.matcher(input).replaceFirst("");
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
		return fail("test resource not found from " + start);
	}
}
