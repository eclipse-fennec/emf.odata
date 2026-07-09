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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.fennec.odata.query.ResourcePath.CountSegment;
import org.eclipse.fennec.odata.query.ResourcePath.PropertySegment;
import org.eclipse.fennec.odata.query.ResourcePath.RefSegment;
import org.eclipse.fennec.odata.query.ResourcePath.Segment;
import org.eclipse.fennec.odata.query.ResourcePath.TypeCastSegment;
import org.eclipse.fennec.odata.query.ResourcePath.ValueSegment;
import org.eclipse.fennec.odata.query.antlr.ODataFilterLexer;
import org.eclipse.fennec.odata.query.antlr.ODataFilterParser;

/**
 * The own OData resource-path parser (ADR-0005 — Olingo is archived, so URI parsing is first
 * party): {@code Set}, {@code Set(key)}, navigation/property segments with optional key
 * predicates, derived-type casts ({@code /Ns.Type}, optionally keyed), terminal
 * {@code $count}/{@code $value}/{@code $ref}. Grammar rules live in {@code ODataFilter.g4}
 * (rule {@code resource}); the OASIS ABNF {@code resourcePath} test cases run against it as
 * acceptance suite.
 *
 * <p>Purely syntactic and stateless; malformed paths and paths exceeding
 * {@value #MAX_SEGMENTS} segments raise {@link ODataQueryParseException} (→ 400/404 at the
 * protocol layer). v1 gaps (documented, syntax rejects them): function-call segments, {@code $all}/{@code $crossjoin}/{@code $entity} forms.
 */
public class ODataResourceParser {

	/** Hard segment cap — resource paths are shallow by nature; guards against URI bombs. */
	public static final int MAX_SEGMENTS = 16;

	/**
	 * Hard length cap enforced BEFORE parsing. The {@link #MAX_SEGMENTS} check only fires after the
	 * whole tree is built, so it does not bound parse-time work on a path with tens of thousands of
	 * tiny segments; this rejects such input at O(n) string-length cost first. Generous enough for
	 * any legitimate 16-segment path (mirrors the default expression-length limit).
	 */
	public static final int MAX_PATH_LENGTH = 4096;

	public ResourcePath parse(String path) {
		if (path == null || path.isBlank()) {
			throw new ODataQueryParseException("empty resource path");
		}
		if (path.length() > MAX_PATH_LENGTH) {
			throw new ODataQueryParseException(
					"resource path exceeds the maximum length of " + MAX_PATH_LENGTH);
		}
		try {
			return parseChecked(path);
		} catch (StackOverflowError e) {
			throw new ODataQueryParseException("the resource path is nested too deeply to parse");
		}
	}

	private ResourcePath parseChecked(String path) {
		ODataFilterLexer lexer = new ODataFilterLexer(CharStreams.fromString(path));
		lexer.removeErrorListeners();
		lexer.addErrorListener(THROWING);
		ODataFilterParser parser = new ODataFilterParser(new CommonTokenStream(lexer));
		parser.removeErrorListeners();
		parser.addErrorListener(THROWING);

		ODataFilterParser.ResourceContext resource = parser.resource();
		if (resource.resourceSegment().size() > MAX_SEGMENTS) {
			throw new ODataQueryParseException(
					"resource path exceeds the maximum of " + MAX_SEGMENTS + " segments");
		}
		List<Segment> segments = new ArrayList<>();
		for (ODataFilterParser.ResourceSegmentContext segment : resource.resourceSegment()) {
			// $count/$value/$ref close the path (ABNF: they are terminal alternatives)
			if (!segments.isEmpty() && !(segments.get(segments.size() - 1) instanceof PropertySegment
					|| segments.get(segments.size() - 1) instanceof TypeCastSegment)) {
				throw new ODataQueryParseException(
						"no segment allowed after $count/$value/$ref: " + path);
			}
			// the ABNF grants at most ONE cast per navigation step — no /Ns.T1/Ns.T2 chains
			if (segment instanceof ODataFilterParser.CastSegmentContext && !segments.isEmpty()
					&& segments.get(segments.size() - 1) instanceof TypeCastSegment) {
				throw new ODataQueryParseException("consecutive type-cast segments: " + path);
			}
			segments.add(switch (segment) {
				case ODataFilterParser.CastSegmentContext cast ->
					new TypeCastSegment(cast.castName().getText(), keyText(cast.keyPredicate()));
				case ODataFilterParser.PropertySegmentContext property ->
					new PropertySegment(property.IDENT().getText(), keyText(property.keyPredicate()));
				case ODataFilterParser.CountSegmentContext c -> new CountSegment();
				case ODataFilterParser.ValueSegmentContext v -> new ValueSegment();
				case ODataFilterParser.RefSegmentContext r -> new RefSegment();
				default -> throw new ODataQueryParseException("unsupported path segment");
			});
		}
		ODataFilterParser.KeyPredicateContext key = resource.keyPredicate();
		return new ResourcePath(resource.IDENT().getText(), keyText(key), namedKeys(key), segments);
	}

	/**
	 * The raw text of a key predicate: the positional literal, or for a compound predicate the
	 * text between the parentheses ({@code OrderID=1,ProductID=2}) — non-null either way, so
	 * "has a key" routing checks stay uniform. Segment predicates only accept the positional
	 * form (compound keys address the entity set).
	 */
	private static String keyText(ODataFilterParser.KeyPredicateContext key) {
		if (key == null) {
			return null;
		}
		if (key.keyLiteral() != null) {
			return key.keyLiteral().getText();
		}
		if (!(key.getParent() instanceof ODataFilterParser.ResourceContext)) {
			throw new ODataQueryParseException(
					"compound key predicates are only supported on the entity set");
		}
		return key.namedKeyValue().stream()
				.map(pair -> pair.IDENT().getText() + "=" + pair.keyLiteral().getText())
				.collect(Collectors.joining(","));
	}

	/** The named components of a compound key predicate, in declaration order; empty otherwise. */
	private static Map<String, String> namedKeys(
			ODataFilterParser.KeyPredicateContext key) {
		if (key == null || key.keyLiteral() != null) {
			return Map.of();
		}
		Map<String, String> named = new LinkedHashMap<>();
		for (ODataFilterParser.NamedKeyValueContext pair : key.namedKeyValue()) {
			if (named.put(pair.IDENT().getText(), pair.keyLiteral().getText()) != null) {
				throw new ODataQueryParseException(
						"duplicate key component '" + pair.IDENT().getText() + "'");
			}
		}
		return named;
	}

	private static final BaseErrorListener THROWING = new BaseErrorListener() {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
				int charPositionInLine, String msg, RecognitionException e) {
			throw new ODataQueryParseException(
					"invalid resource path at " + charPositionInLine + " - " + msg, e);
		}
	};
}
