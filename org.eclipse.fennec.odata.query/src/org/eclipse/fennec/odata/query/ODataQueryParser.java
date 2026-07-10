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
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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
import org.eclipse.fennec.odata.query.apply.AggregateFrom;
import org.eclipse.fennec.odata.query.apply.AggregateMethod;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyFactory;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ApplyTransformation;
import org.eclipse.fennec.odata.query.apply.BottomTopMethod;
import org.eclipse.fennec.odata.query.apply.BottomTopTransformation;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.eclipse.fennec.odata.query.apply.ConcatTransformation;
import org.eclipse.fennec.odata.query.apply.FilterTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;
import org.eclipse.fennec.odata.query.apply.OrderByExpression;
import org.eclipse.fennec.odata.query.apply.OrderByTransformation;
import org.eclipse.fennec.odata.query.apply.RollupHierarchy;
import org.eclipse.fennec.odata.query.apply.SkipTransformation;
import org.eclipse.fennec.odata.query.apply.TopTransformation;

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

	/**
	 * Length cap for an individual parameter-alias value. Alias values are a SEPARATE query
	 * parameter re-parsed during resolution, so the front-door {@code RequestLimits} length/depth
	 * check on the top-level {@code $filter} does not cover them — this bounds each one here so a
	 * small {@code $filter=@a} plus a giant {@code @a=…} cannot smuggle an unbounded expression
	 * past the guard. Mirrors the default {@code odata.max.expression.length} (4096).
	 */
	private static final int MAX_ALIAS_VALUE_LENGTH = 4096;

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
		return parsing(() -> {
			ODataFilterParser parser = newParser(filter);
			Supplier<ODataToOclBuilder> factory = () -> new ODataToOclBuilder(context);
			return typeResolver.resolve(
					aliasWired(factory, parameterAliases, 0).visit(parser.filter()));
		});
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
		return parsing(() -> {
			ODataFilterParser parser = newParser(filter);
			Supplier<ODataToOclBuilder> factory = () -> builderWithAliases(context, pipeline);
			return typeResolver.resolve(
					aliasWired(factory, parameterAliases, 0).visit(parser.filter()));
		});
	}

	/** {@code $orderby} counterpart of {@link #parseFilterAfterApply}. */
	public List<OrderBySegment> parseOrderByAfterApply(String orderBy, EClass context,
			ApplyPipeline pipeline) {
		return parseOrderByAfterApply(orderBy, context, pipeline, Map.of());
	}

	/** {@link #parseOrderByAfterApply} with 4.01 parameter aliases. */
	public List<OrderBySegment> parseOrderByAfterApply(String orderBy, EClass context,
			ApplyPipeline pipeline, Map<String, String> parameterAliases) {
		return parsing(() -> {
			ODataFilterParser parser = newParser(orderBy);
			return orderBySegments(parser,
					aliasWired(() -> builderWithAliases(context, pipeline), parameterAliases, 0));
		});
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
		return parsing(() -> {
			ODataFilterParser parser = newParser(orderBy);
			return orderBySegments(parser,
					aliasWired(() -> new ODataToOclBuilder(context), parameterAliases, 0));
		});
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
			if (value.length() > MAX_ALIAS_VALUE_LENGTH) {
				throw new ODataQueryParseException("parameter alias '" + name
						+ "' value exceeds the maximum length of " + MAX_ALIAS_VALUE_LENGTH);
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
		return parsing(() -> {
			ODataFilterParser parser = newParser(apply);
			ODataToOclBuilder builder = new ODataToOclBuilder(context);
			return pipeline(parser.apply().applySeq(), builder);
		});
	}

	private ApplyPipeline pipeline(ODataFilterParser.ApplySeqContext seq, ODataToOclBuilder builder) {
		ApplyPipeline pipeline = ApplyFactory.eINSTANCE.createApplyPipeline();
		for (ODataFilterParser.ApplyTrafoContext trafo : seq.applyTrafo()) {
			pipeline.getTransformations().add(transformation(trafo, builder));
		}
		return pipeline;
	}

	// The grammar gates each transformation shape by its (case-sensitive, ABNF %s) soft
	// keyword, so the contexts arrive pre-dispatched; only value-level rules remain here.
	private ApplyTransformation transformation(ODataFilterParser.ApplyTrafoContext ctx,
			ODataToOclBuilder builder) {
		return switch (ctx) {
			case ODataFilterParser.GroupByTrafoContext groupBy -> groupBy(groupBy, builder);
			case ODataFilterParser.AggregateTrafoContext aggregate -> {
				AggregateTransformation t = ApplyFactory.eINSTANCE.createAggregateTransformation();
				for (ODataFilterParser.AggregateItemContext item : aggregate.aggregateItem()) {
					t.getAggregations().add(aggregation(item, builder));
				}
				yield t;
			}
			case ODataFilterParser.ComputeTrafoContext compute -> {
				ComputeTransformation t = ApplyFactory.eINSTANCE.createComputeTransformation();
				for (ODataFilterParser.ComputeItemContext item : compute.computeItem()) {
					ComputeExpression ce = ApplyFactory.eINSTANCE.createComputeExpression();
					ce.setExpression(typeResolver.resolve(builder.visit(item.expr())));
					ce.setAlias(item.alias.getText());
					t.getComputeExpressions().add(ce);
					builder.registerAlias(ce.getAlias());
				}
				yield t;
			}
			case ODataFilterParser.ConcatTrafoContext concat -> {
				ConcatTransformation t = ApplyFactory.eINSTANCE.createConcatTransformation();
				for (ODataFilterParser.ApplySeqContext branch : concat.applySeq()) {
					t.getPipelines().add(pipeline(branch, builder));
				}
				yield t;
			}
			case ODataFilterParser.FilterTrafoContext filter -> {
				FilterTransformation t = ApplyFactory.eINSTANCE.createFilterTransformation();
				t.setPredicate(typeResolver.resolve(builder.visit(filter.expr())));
				yield t;
			}
			case ODataFilterParser.BottomTopTrafoContext bottomTop -> {
				BottomTopTransformation t = ApplyFactory.eINSTANCE.createBottomTopTransformation();
				t.setMethod(switch (bottomTop.name.getText()) {
					case "topcount" -> BottomTopMethod.TOP_COUNT;
					case "topsum" -> BottomTopMethod.TOP_SUM;
					case "toppercent" -> BottomTopMethod.TOP_PERCENT;
					case "bottomcount" -> BottomTopMethod.BOTTOM_COUNT;
					case "bottomsum" -> BottomTopMethod.BOTTOM_SUM;
					default -> BottomTopMethod.BOTTOM_PERCENT;
				});
				t.setThreshold(typeResolver.resolve(builder.visit(bottomTop.expr(0))));
				t.setValue(typeResolver.resolve(builder.visit(bottomTop.expr(1))));
				yield t;
			}
			case ODataFilterParser.OrderByTrafoContext orderBy -> {
				OrderByTransformation t = ApplyFactory.eINSTANCE.createOrderByTransformation();
				for (ODataFilterParser.OrderbyItemContext item : orderBy.orderbyItem()) {
					OrderByExpression e = ApplyFactory.eINSTANCE.createOrderByExpression();
					e.setExpression(typeResolver.resolve(builder.visit(item.expr())));
					e.setAscending(item.direction == null
							|| item.direction.getType() != ODataFilterLexer.DESC);
					t.getItems().add(e);
				}
				yield t;
			}
			case ODataFilterParser.RowLimitTrafoContext rowLimit -> {
				long count = Long.parseLong(rowLimit.INT().getText());
				if (count < 0) {
					throw new ODataQueryParseException(
							"'" + rowLimit.name.getText() + "' requires a non-negative integer");
				}
				if ("top".equals(rowLimit.name.getText())) {
					TopTransformation t = ApplyFactory.eINSTANCE.createTopTransformation();
					t.setCount(count);
					yield t;
				}
				SkipTransformation t = ApplyFactory.eINSTANCE.createSkipTransformation();
				t.setCount(count);
				yield t;
			}
			default -> ApplyFactory.eINSTANCE.createIdentityTransformation();
		};
	}

	private GroupByTransformation groupBy(ODataFilterParser.GroupByTrafoContext ctx,
			ODataToOclBuilder builder) {
		GroupByTransformation t = ApplyFactory.eINSTANCE.createGroupByTransformation();
		for (ODataFilterParser.GroupbyElementContext element : ctx.groupbyElement()) {
			if (element instanceof ODataFilterParser.PathElementContext path) {
				t.getGroupingProperties().add(typeResolver.resolve(builder.visit(path.memberPath())));
			} else {
				ODataFilterParser.RollupElementContext rollup =
						(ODataFilterParser.RollupElementContext) element;
				RollupHierarchy hierarchy = ApplyFactory.eINSTANCE.createRollupHierarchy();
				List<ODataFilterParser.MemberPathContext> levels = rollup.memberPath();
				if (levels.size() == 1) {
					// ONE simple identifier = named leveled hierarchy (its qualifier, not a
					// property — deliberately unresolved); a single PATH is neither form
					String name = levels.get(0).getText();
					if (name.indexOf('/') >= 0) {
						throw new ODataQueryParseException(
								"rollup needs two or more levels or a hierarchy name: " + name);
					}
					hierarchy.setHierarchy(name);
				} else {
					for (ODataFilterParser.MemberPathContext level : levels) {
						hierarchy.getLevels().add(typeResolver.resolve(builder.visit(level)));
					}
				}
				t.getRollups().add(hierarchy);
			}
		}
		if (ctx.applySeq() != null) {
			List<ODataFilterParser.ApplyTrafoContext> nested = ctx.applySeq().applyTrafo();
			if (nested.size() > 1) {
				// syntactically valid ([OData-Aggregation] groupbyTrafo takes an applyExpr),
				// but the per-group pipeline execution only covers a single stage → 501
				throw new UnsupportedOperationException(
						"nested groupby pipelines with more than one transformation are not supported");
			}
			t.setThen(transformation(nested.get(0), builder));
		}
		return t;
	}

	/** Custom aggregates are bare identifier paths ([OData-Aggregation] aggregateCustom). */
	private static final Pattern CUSTOM_AGGREGATE_PATH =
			Pattern.compile("[A-Za-z_]\\w*(/[A-Za-z_]\\w*)*");

	private AggregateExpression aggregation(ODataFilterParser.AggregateItemContext ctx,
			ODataToOclBuilder builder) {
		AggregateExpression aggregate = ApplyFactory.eINSTANCE.createAggregateExpression();
		switch (ctx) {
			case ODataFilterParser.AggregateWithItemContext with -> {
				aggregate.setExpression(typeResolver.resolve(builder.visit(with.expr())));
				method(with.method.getText(), aggregate::setMethod, aggregate::setCustomMethod);
				aggregate.setAlias(with.alias.getText());
				for (ODataFilterParser.AggrFromContext from : with.aggrFrom()) {
					aggregate.getFrom().add(fromClause(from.memberPath(), from.method.getText(), builder));
				}
			}
			case ODataFilterParser.AggregateCountItemContext count -> {
				aggregate.setMethod(AggregateMethod.COUNT); // $count virtual aggregate, no operand
				aggregate.setAlias(count.alias.getText());
				for (ODataFilterParser.AggrFromContext from : count.aggrFrom()) {
					aggregate.getFrom().add(fromClause(from.memberPath(), from.method.getText(), builder));
				}
			}
			case ODataFilterParser.AggregateCustomAliasedContext custom -> {
				customOrCountPath(aggregate, custom.expr(), builder);
				aggregate.setAlias(custom.alias.getText());
				for (ODataFilterParser.CustomFromContext from : custom.customFrom()) {
					aggregate.getFrom().add(fromClause(from.memberPath(),
							from.method == null ? null : from.method.getText(), builder));
				}
			}
			default -> {
				ODataFilterParser.AggregateCustomBareContext bare =
						(ODataFilterParser.AggregateCustomBareContext) ctx;
				customOrCountPath(aggregate, bare.expr(), builder);
				if (aggregate.getMethod() == AggregateMethod.COUNT) {
					throw new ODataQueryParseException("'path/$count' aggregates require an alias");
				}
			}
		}
		if (aggregate.getAlias() != null) {
			builder.registerAlias(aggregate.getAlias());
		}
		return aggregate;
	}

	/** No 'with': either the {@code path/$count} form or a model-declared custom aggregate. */
	private void customOrCountPath(AggregateExpression aggregate,
			ODataFilterParser.ExprContext expr, ODataToOclBuilder builder) {
		String text = expr.getText();
		if (text.endsWith("/$count")) {
			aggregate.setExpression(typeResolver.resolve(builder.visit(expr)));
			aggregate.setMethod(AggregateMethod.COUNT);
			return;
		}
		if (!CUSTOM_AGGREGATE_PATH.matcher(text).matches()) {
			throw new ODataQueryParseException(
					"aggregate expression without 'with' must be a custom aggregate path: " + text);
		}
		aggregate.setMethod(AggregateMethod.CUSTOM_AGGREGATE);
		aggregate.setCustomMethod(text); // deliberately unresolved — not a model property
	}

	private AggregateFrom fromClause(List<ODataFilterParser.MemberPathContext> paths,
			String methodText, ODataToOclBuilder builder) {
		AggregateFrom from = ApplyFactory.eINSTANCE.createAggregateFrom();
		for (ODataFilterParser.MemberPathContext path : paths) {
			from.getGroupingProperties().add(typeResolver.resolve(builder.visit(path)));
		}
		if (methodText != null) {
			method(methodText, from::setMethod, from::setCustomMethod);
		}
		return from;
	}

	/** Standard method name → enum; namespace-qualified → CUSTOM; anything else → 400. */
	private static void method(String text, Consumer<AggregateMethod> setMethod,
			Consumer<String> setCustomMethod) {
		if (text.indexOf('.') >= 0) {
			setMethod.accept(AggregateMethod.CUSTOM);
			setCustomMethod.accept(text);
			return;
		}
		setMethod.accept(switch (text.toLowerCase()) {
			case "sum" -> AggregateMethod.SUM;
			case "min" -> AggregateMethod.MIN;
			case "max" -> AggregateMethod.MAX;
			case "average" -> AggregateMethod.AVERAGE;
			case "countdistinct" -> AggregateMethod.COUNT_DISTINCT;
			default -> throw new ODataQueryParseException("unknown aggregate method '" + text + "'");
		});
	}

	/**
	 * Runs a parse action under a stack-overflow guard: paren-free deep recursion (a long
	 * {@code not not …} chain, deep member paths, {@code f(f(f(…)))}) is NOT caught by the
	 * paren-counting front-door depth check, and the resulting {@link StackOverflowError} is an
	 * {@link Error} the ANTLR error listener never sees. Catching it here — the parser is stateless
	 * and thrown away — turns a would-be 500/DoS into a clean 400. All recursion (ANTLR parse, the
	 * visitor tree walk, and type resolution) runs inside the action, so one guard covers them all.
	 */
	private static <T> T parsing(Supplier<T> action) {
		try {
			return action.get();
		} catch (StackOverflowError e) {
			throw new ODataQueryParseException("the expression is nested too deeply to parse");
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
