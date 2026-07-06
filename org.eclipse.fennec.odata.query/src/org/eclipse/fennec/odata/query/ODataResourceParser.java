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
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.fennec.odata.query.ResourcePath.CountSegment;
import org.eclipse.fennec.odata.query.ResourcePath.PropertySegment;
import org.eclipse.fennec.odata.query.ResourcePath.RefSegment;
import org.eclipse.fennec.odata.query.ResourcePath.Segment;
import org.eclipse.fennec.odata.query.ResourcePath.ValueSegment;
import org.eclipse.fennec.odata.query.antlr.ODataFilterLexer;
import org.eclipse.fennec.odata.query.antlr.ODataFilterParser;

/**
 * The own OData resource-path parser (ADR-0005 — Olingo is archived, so URI parsing is first
 * party): {@code Set}, {@code Set(key)}, navigation/property segments with optional key
 * predicates, terminal {@code $count}/{@code $value}/{@code $ref}. Grammar rules live in
 * {@code ODataFilter.g4} (rule {@code resource}); the OASIS ABNF {@code resourcePath} test
 * cases run against it as acceptance suite.
 *
 * <p>Purely syntactic and stateless; malformed paths and paths exceeding
 * {@value #MAX_SEGMENTS} segments raise {@link ODataQueryParseException} (→ 400/404 at the
 * protocol layer). v1 gaps (documented, syntax rejects them): type-cast segments
 * ({@code Ns.Type}), multi-part/named key predicates, function-call segments, {@code $all}/
 * {@code $crossjoin}/{@code $entity} forms.
 */
public class ODataResourceParser {

	/** Hard segment cap — resource paths are shallow by nature; guards against URI bombs. */
	public static final int MAX_SEGMENTS = 16;

	public ResourcePath parse(String path) {
		if (path == null || path.isBlank()) {
			throw new ODataQueryParseException("empty resource path");
		}
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
			if (!segments.isEmpty() && !(segments.get(segments.size() - 1) instanceof PropertySegment)) {
				throw new ODataQueryParseException(
						"no segment allowed after $count/$value/$ref: " + path);
			}
			segments.add(switch (segment) {
				case ODataFilterParser.PropertySegmentContext property ->
					new PropertySegment(property.IDENT().getText(), keyText(property.keyPredicate()));
				case ODataFilterParser.CountSegmentContext c -> new CountSegment();
				case ODataFilterParser.ValueSegmentContext v -> new ValueSegment();
				case ODataFilterParser.RefSegmentContext r -> new RefSegment();
				default -> throw new ODataQueryParseException("unsupported path segment");
			});
		}
		return new ResourcePath(resource.IDENT().getText(), keyText(resource.keyPredicate()), segments);
	}

	private static String keyText(ODataFilterParser.KeyPredicateContext key) {
		return key == null ? null : key.keyLiteral().getText();
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
