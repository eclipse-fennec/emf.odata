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
import java.util.Map;
import java.util.function.Supplier;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.query.antlr.ODataFilterLexer;
import org.eclipse.fennec.odata.query.antlr.ODataFilterParser;
import org.eclipse.fennec.odata.query.apply.AggregateExpression;
import org.eclipse.fennec.odata.query.apply.AggregateMethod;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyFactory;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ApplyTransformation;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.eclipse.fennec.odata.query.apply.FilterTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;

/**
 * E4 entry point: parses OData {@code $filter} and {@code $orderby} expressions with the own
 * ANTLR4 grammar ({@code grammar/ODataFilter.g4}, req §3.6) and returns the m2x OCL AST — the
 * internal predicate IR every backend translator consumes (req §3.5).
 *
 * <p>Parsing needs the request's context {@link EClass} (the entity type addressed by the URI)
 * so property paths resolve to {@code referredProperty} eagerly. Syntax errors, unknown
 * functions and unknown properties raise {@link ODataQueryParseException}.
 *
 * <p>Instances are stateless and thread-safe. Per-EClass LRU caching of parsed expressions is
 * deliberately NOT here — it hooks into the {@code ODataAspectProvider} profile per req §3.6.1.
 */
public class ODataQueryParser {

	private final OclTypeResolver typeResolver = new OclTypeResolver();

	/** Max nesting when parameter alias values reference other aliases ({@code @a=@b add 1}). */
	private static final int MAX_ALIAS_DEPTH = 8;

	/** Parse a {@code $filter} value into a boolean-typed OCL expression. */
	public OclExpression parseFilter(String filter, EClass context) {
		return parseFilter(filter, context, Map.of());
	}

	/**
	 * {@link #parseFilter(String, EClass)} with 4.01 parameter aliases (11.2.5.1.3): keys are
	 * the alias names AS SENT incl. {@code @}, values are their (unparsed) expression texts —
	 * resolved lazily and recursively, unreferenced aliases are never parsed.
	 */
	public OclExpression parseFilter(String filter, EClass context,
			Map<String, String> parameterAliases) {
		ODataFilterParser parser = newParser(filter);
		Supplier<ODataToOclBuilder> factory = () -> new ODataToOclBuilder(context);
		return typeResolver.resolve(
				aliasWired(factory, parameterAliases, 0).visit(parser.filter()));
	}

	/**
	 * Parse a {@code $filter} that runs AFTER a {@code $apply} pipeline (OASIS: system query
	 * options apply to the transformed set): the pipeline's aggregate/compute aliases are in
	 * scope and resolve as {@code VariableExp}s against the result rows.
	 */
	public OclExpression parseFilterAfterApply(String filter, EClass context, ApplyPipeline pipeline) {
		return parseFilterAfterApply(filter, context, pipeline, Map.of());
	}

	/** {@link #parseFilterAfterApply} with 4.01 parameter aliases. */
	public OclExpression parseFilterAfterApply(String filter, EClass context, ApplyPipeline pipeline,
			Map<String, String> parameterAliases) {
		ODataFilterParser parser = newParser(filter);
		Supplier<ODataToOclBuilder> factory = () -> builderWithAliases(context, pipeline);
		return typeResolver.resolve(
				aliasWired(factory, parameterAliases, 0).visit(parser.filter()));
	}

	/** {@code $orderby} counterpart of {@link #parseFilterAfterApply}. */
	public List<OrderBySegment> parseOrderByAfterApply(String orderBy, EClass context,
			ApplyPipeline pipeline) {
		return parseOrderByAfterApply(orderBy, context, pipeline, Map.of());
	}

	/** {@link #parseOrderByAfterApply} with 4.01 parameter aliases. */
	public List<OrderBySegment> parseOrderByAfterApply(String orderBy, EClass context,
			ApplyPipeline pipeline, Map<String, String> parameterAliases) {
		ODataFilterParser parser = newParser(orderBy);
		return orderBySegments(parser,
				aliasWired(() -> builderWithAliases(context, pipeline), parameterAliases, 0));
	}

	private ODataToOclBuilder builderWithAliases(EClass context, ApplyPipeline pipeline) {
		ODataToOclBuilder builder = new ODataToOclBuilder(context);
		pipeline.eAllContents().forEachRemaining(element -> {
			if (element instanceof AggregateExpression aggregate) {
				builder.registerAlias(aggregate.getAlias());
			} else if (element instanceof ComputeExpression compute) {
				builder.registerAlias(compute.getAlias());
			}
		});
		return builder;
	}

	/** Parse a {@code $orderby} value into its sort segments (OData default: ascending). */
	public List<OrderBySegment> parseOrderBy(String orderBy, EClass context) {
		return parseOrderBy(orderBy, context, Map.of());
	}

	/** {@link #parseOrderBy(String, EClass)} with 4.01 parameter aliases. */
	public List<OrderBySegment> parseOrderBy(String orderBy, EClass context,
			Map<String, String> parameterAliases) {
		ODataFilterParser parser = newParser(orderBy);
		return orderBySegments(parser,
				aliasWired(() -> new ODataToOclBuilder(context), parameterAliases, 0));
	}

	private List<OrderBySegment> orderBySegments(ODataFilterParser parser, ODataToOclBuilder builder) {
		List<OrderBySegment> segments = new ArrayList<>();
		for (ODataFilterParser.OrderbyItemContext item : parser.orderby().orderbyItem()) {
			boolean ascending = item.direction == null || item.direction.getType() != ODataFilterLexer.DESC;
			segments.add(new OrderBySegment(typeResolver.resolve(builder.visit(item.expr())), ascending));
		}
		return segments;
	}

	/**
	 * Builds via {@code factory} and installs the {@code @name} resolver: value looked up in
	 * {@code values}, parsed as a complete expression with the SAME builder setup (so lambda/
	 * apply alias scoping rules apply), depth-capped against alias cycles ({@code @a=@a}).
	 */
	private ODataToOclBuilder aliasWired(Supplier<ODataToOclBuilder> factory,
			Map<String, String> values, int depth) {
		ODataToOclBuilder builder = factory.get();
		builder.parameterAliasResolver(name -> {
			String value = values.get(name);
			if (value == null || value.isBlank()) {
				throw new ODataQueryParseException("unresolved parameter alias '" + name + "'");
			}
			if (depth >= MAX_ALIAS_DEPTH) {
				throw new ODataQueryParseException(
						"parameter alias nesting exceeds " + MAX_ALIAS_DEPTH + " levels");
			}
			return aliasWired(factory, values, depth + 1).visit(newParser(value).filter());
		});
		return builder;
	}

	/**
	 * Parse a {@code $apply} value into the aggregation pipeline (E4-AP-4, req §3.5): pipeline
	 * stages are first-class {@link ApplyTransformation}s, only their embedded expressions are
	 * OCL. Aliases introduced by {@code aggregate}/{@code compute} are referable in later stages
	 * (they surface as {@code VariableExp}s — the backend resolves them against its stage output).
	 */
	public ApplyPipeline parseApply(String apply, EClass context) {
		ODataFilterParser parser = newParser(apply);
		ODataToOclBuilder builder = new ODataToOclBuilder(context);
		ApplyPipeline pipeline = ApplyFactory.eINSTANCE.createApplyPipeline();
		for (ODataFilterParser.ApplyTrafoContext trafo : parser.apply().applyTrafo()) {
			pipeline.getTransformations().add(transformation(trafo, builder));
		}
		return pipeline;
	}

	private ApplyTransformation transformation(ODataFilterParser.ApplyTrafoContext ctx,
			ODataToOclBuilder builder) {
		if (ctx instanceof ODataFilterParser.GroupByTrafoContext groupBy) {
			requireTransformation(groupBy.name.getText(), "groupby");
			GroupByTransformation t = ApplyFactory.eINSTANCE.createGroupByTransformation();
			for (ODataFilterParser.MemberPathContext property : groupBy.memberPath()) {
				t.getGroupingProperties().add(typeResolver.resolve(builder.visit(property)));
			}
			if (groupBy.applyTrafo() != null) {
				t.setThen(transformation(groupBy.applyTrafo(), builder));
			}
			return t;
		}
		if (ctx instanceof ODataFilterParser.AggregateTrafoContext aggregate) {
			requireTransformation(aggregate.name.getText(), "aggregate");
			AggregateTransformation t = ApplyFactory.eINSTANCE.createAggregateTransformation();
			for (ODataFilterParser.AggregateItemContext item : aggregate.aggregateItem()) {
				t.getAggregations().add(aggregation(item, builder));
			}
			return t;
		}
		if (ctx instanceof ODataFilterParser.ComputeTrafoContext compute) {
			requireTransformation(compute.name.getText(), "compute");
			ComputeTransformation t = ApplyFactory.eINSTANCE.createComputeTransformation();
			for (ODataFilterParser.ComputeItemContext item : compute.computeItem()) {
				ComputeExpression ce = ApplyFactory.eINSTANCE.createComputeExpression();
				ce.setExpression(typeResolver.resolve(builder.visit(item.expr())));
				ce.setAlias(item.alias.getText());
				t.getComputeExpressions().add(ce);
				builder.registerAlias(ce.getAlias());
			}
			return t;
		}
		ODataFilterParser.FilterTrafoContext filter = (ODataFilterParser.FilterTrafoContext) ctx;
		requireTransformation(filter.name.getText(), "filter");
		FilterTransformation t = ApplyFactory.eINSTANCE.createFilterTransformation();
		t.setPredicate(typeResolver.resolve(builder.visit(filter.expr())));
		return t;
	}

	private AggregateExpression aggregation(ODataFilterParser.AggregateItemContext ctx,
			ODataToOclBuilder builder) {
		AggregateExpression aggregate = ApplyFactory.eINSTANCE.createAggregateExpression();
		if (ctx instanceof ODataFilterParser.AggregateWithItemContext with) {
			aggregate.setExpression(typeResolver.resolve(builder.visit(with.expr())));
			aggregate.setMethod(switch (with.method.getText().toLowerCase()) {
				case "sum" -> AggregateMethod.SUM;
				case "min" -> AggregateMethod.MIN;
				case "max" -> AggregateMethod.MAX;
				case "average" -> AggregateMethod.AVERAGE;
				case "countdistinct" -> AggregateMethod.COUNT_DISTINCT;
				default -> throw new ODataQueryParseException(
						"unknown aggregate method '" + with.method.getText() + "'");
			});
			aggregate.setAlias(with.alias.getText());
		} else {
			ODataFilterParser.AggregateCountItemContext count =
					(ODataFilterParser.AggregateCountItemContext) ctx;
			aggregate.setMethod(AggregateMethod.COUNT); // $count virtual aggregate, no operand
			aggregate.setAlias(count.alias.getText());
		}
		builder.registerAlias(aggregate.getAlias());
		return aggregate;
	}

	private static void requireTransformation(String actual, String expected) {
		if (!expected.equals(actual)) {
			throw new ODataQueryParseException("unknown or malformed $apply transformation '" + actual + "'");
		}
	}

	private static ODataFilterParser newParser(String text) {
		if (text == null || text.isBlank()) {
			throw new ODataQueryParseException("empty expression");
		}
		ODataFilterLexer lexer = new ODataFilterLexer(CharStreams.fromString(text));
		lexer.removeErrorListeners();
		lexer.addErrorListener(THROWING);
		ODataFilterParser parser = new ODataFilterParser(new CommonTokenStream(lexer));
		parser.removeErrorListeners();
		parser.addErrorListener(THROWING);
		return parser;
	}

	private static final BaseErrorListener THROWING = new BaseErrorListener() {
		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
				int charPositionInLine, String msg, RecognitionException e) {
			throw new ODataQueryParseException(
					"syntax error at " + line + ":" + charPositionInLine + " - " + msg, e);
		}
	};
}
