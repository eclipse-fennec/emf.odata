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
package org.eclipse.fennec.odata.persistence.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.expression.ExpressionFactory;
import org.eclipse.fennec.model.expression.IntegerLiteral;
import org.eclipse.fennec.model.expression.IsNull;
import org.eclipse.fennec.model.expression.PropertyPath;
import org.eclipse.fennec.model.query.Aggregate;
import org.eclipse.fennec.model.query.ComputeStage;
import org.eclipse.fennec.model.query.Computation;
import org.eclipse.fennec.model.query.FilterStage;
import org.eclipse.fennec.model.query.GroupByStage;
import org.eclipse.fennec.model.query.OrderBy;
import org.eclipse.fennec.model.query.Pipeline;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.QueryFactory;
import org.eclipse.fennec.model.query.Selection;
import org.eclipse.fennec.model.query.SortDirection;
import org.eclipse.fennec.model.query.builder.Expressions;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.odata.query.apply.AggregateExpression;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.BottomTopMethod;
import org.eclipse.fennec.odata.query.apply.BottomTopTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyTransformation;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.eclipse.fennec.odata.query.apply.FilterTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;
import org.eclipse.fennec.odata.query.apply.IdentityTransformation;
import org.eclipse.fennec.persistence.query.api.QueryResultRow;

/**
 * Translates the parsed {@code $apply} pipeline (apply.ecore) onto the Fennec query
 * envelope's {@link Pipeline} stages, plus the post-pipeline query options ([OASIS
 * aggregation]: {@code $filter}/{@code $orderby}/{@code $skip}/{@code $top} address the
 * TRANSFORMED set — alias references become {@code AliasRef}s via
 * {@link ReadQueries#rowExpression}).
 *
 * <p>Mapping choices: leading {@code filter(...)} transformations fold into the plain
 * {@code WHERE} predicate (no PIPELINE capability needed for them), a terminal
 * {@code groupby} without aggregates becomes a DISTINCT projection, and a pipeline
 * without any shape-changing transformation stays an OBJECTS query whose entities are
 * row-flattened by the caller. {@code topcount}/{@code bottomcount} fold into
 * the query's sort window (see {@link #bottomTop}). Transformations the stage model cannot
 * express ({@code concat}, {@code rollup}, custom aggregates, {@code from}) — and the
 * running-total members of the {@code bottom*}/{@code top*} family — are refused with
 * {@link UnsupportedOperationException}, which the servlet maps to an honest 501.
 */
final class ApplyQueries {

	private ApplyQueries() {
	}

	/** One output column of the transformed set. */
	record Column(String rowKey, List<String> nestedPath) {

		/** Grouped property path: derived row key (underscore-joined), nested in the result row. */
		static Column groupedPath(List<String> segments) {
			return new Column(String.join("_", segments), List.copyOf(segments));
		}

		/** Aggregate/compute alias: flat key. */
		static Column alias(String alias) {
			return new Column(alias, null);
		}
	}

	/**
	 * The translated query plus the output column layout; {@code columns} is empty when
	 * the pipeline never left the entity shape (OBJECTS result, caller flattens).
	 */
	record Plan(Query query, List<Column> columns) {
	}

	static Plan plan(ApplyQuery query, int maxPageSize) {
		EClass entityType = query.entityType();
		Query irQuery = QueryFactory.eINSTANCE.createQuery();
		irQuery.setFrom(entityType);

		List<Column> columns = new ArrayList<>();
		Set<String> aliases = new LinkedHashSet<>();
		Pipeline pipeline = QueryFactory.eINSTANCE.createPipeline();
		/** Set by a terminal topcount/bottomcount: the sort window that selects its rows. */
		SortWindow window = null;

		List<ApplyTransformation> transformations = query.pipeline().getTransformations();
		for (int i = 0; i < transformations.size(); i++) {
			ApplyTransformation transformation = transformations.get(i);
			boolean last = i == transformations.size() - 1;
			switch (transformation) {
				case IdentityTransformation identity -> { /* no-op */ }
				case FilterTransformation filter -> {
					if (pipeline.getStages().isEmpty() && columns.isEmpty()) {
						// leading filters are plain WHERE restrictions, no pipeline needed
						Expression predicate = ReadQueries.predicate(filter.getPredicate(),
								entityType, null);
						irQuery.setPredicate(irQuery.getPredicate() == null ? predicate
								: Expressions.and(irQuery.getPredicate(), predicate));
					} else {
						FilterStage stage = QueryFactory.eINSTANCE.createFilterStage();
						stage.setPredicate(ReadQueries.rowExpression(filter.getPredicate(), aliases));
						pipeline.getStages().add(stage);
					}
				}
				case AggregateTransformation aggregate -> {
					GroupByStage stage = QueryFactory.eINSTANCE.createGroupByStage();
					columns.clear();
					aggregates(aggregate, stage, columns, aliases, entityType);
					pipeline.getStages().add(stage);
				}
				case GroupByTransformation groupBy -> {
					if (!groupBy.getRollups().isEmpty()) {
						throw new UnsupportedOperationException(
								"the command backend cannot aggregate 'rollup' hierarchies");
					}
					if (groupBy.getThen() == null) {
						// distinct groupby: only expressible as a DISTINCT projection,
						// which owns the whole query — refusable anywhere but terminal
						if (!last || !pipeline.getStages().isEmpty() || !columns.isEmpty()) {
							throw new UnsupportedOperationException(
									"the command backend supports 'groupby' without aggregates only "
											+ "as the final transformation");
						}
						columns.clear();
						for (OclExpression property : groupBy.getGroupingProperties()) {
							List<String> segments = segments(property);
							Selection selection = QueryFactory.eINSTANCE.createSelection();
							selection.setPath(propertyPath(property));
							// explicit alias = the derived key — alias-less cells are
							// ordinal-only in parts of the engine row API
							selection.setAlias(String.join("_", segments));
							irQuery.getSelect().add(selection);
							columns.add(Column.groupedPath(segments));
						}
						irQuery.setDistinct(true);
						continue;
					}
					if (!(groupBy.getThen() instanceof AggregateTransformation aggregate)) {
						throw new UnsupportedOperationException(
								"the command backend supports only 'aggregate' as the nested "
										+ "groupby transformation");
					}
					GroupByStage stage = QueryFactory.eINSTANCE.createGroupByStage();
					columns.clear();
					for (OclExpression property : groupBy.getGroupingProperties()) {
						stage.getPaths().add(propertyPath(property));
						columns.add(Column.groupedPath(segments(property)));
					}
					aggregates(aggregate, stage, columns, aliases, entityType);
					pipeline.getStages().add(stage);
				}
				case ComputeTransformation compute -> {
					if (columns.isEmpty()) {
						// entity-space compute would need "all attributes + alias" result rows,
						// which the stage model's terminal compute does not produce — refuse
						throw new UnsupportedOperationException("the command backend supports "
								+ "'compute' only after a grouping transformation");
					}
					ComputeStage stage = QueryFactory.eINSTANCE.createComputeStage();
					for (var expression : compute.getComputeExpressions()) {
						Computation computation = QueryFactory.eINSTANCE.createComputation();
						computation.setExpression(
								ReadQueries.rowExpression(expression.getExpression(), aliases));
						computation.setAlias(expression.getAlias());
						stage.getComputations().add(computation);
						columns.add(Column.alias(expression.getAlias()));
						aliases.add(expression.getAlias());
					}
					pipeline.getStages().add(stage);
				}
				case BottomTopTransformation bottomTop -> {
					window = bottomTop(bottomTop, last, query, entityType, aliases, columns,
							irQuery, pipeline);
				}
				default -> throw new UnsupportedOperationException("the command backend cannot apply '"
						+ transformation.eClass().getName() + "'");
			}
		}
		if (!pipeline.getStages().isEmpty()) {
			irQuery.setApply(pipeline);
		}

		// post-pipeline query options address the transformed set
		if (query.rowFilter() != null) {
			Expression predicate = rowOrEntityExpression(query.rowFilter(), entityType, aliases,
					columns);
			if (irQuery.getApply() != null) {
				FilterStage stage = QueryFactory.eINSTANCE.createFilterStage();
				stage.setPredicate(predicate);
				irQuery.getApply().getStages().add(stage);
			} else {
				irQuery.setPredicate(irQuery.getPredicate() == null ? predicate
						: Expressions.and(irQuery.getPredicate(), predicate));
			}
		}
		for (OrderBySegment segment : query.orderBy()) {
			irQuery.getOrderBy().add(orderBy(segment, entityType, aliases, columns));
		}
		if (window != null) {
			irQuery.getOrderBy().add(window.orderBy());
		}
		if (query.skip() > 0) {
			irQuery.setSkip(query.skip());
		}
		int cap = query.top() > 0 ? query.top() : maxPageSize;
		if (window != null) {
			// both windows read the SAME order, so the narrower one wins — $top after a
			// topcount is the first m of the n, never a different n rows
			irQuery.setTop(cap > 0 ? Math.min(window.count(), cap) : window.count());
		} else if (cap > 0) {
			irQuery.setTop(cap);
		}
		return new Plan(irQuery, List.copyOf(columns));
	}

	private static void aggregates(AggregateTransformation transformation, GroupByStage stage,
			List<Column> columns, Set<String> aliases, EClass entityType) {
		for (AggregateExpression aggregation : transformation.getAggregations()) {
			if (!aggregation.getFrom().isEmpty()) {
				throw new UnsupportedOperationException(
						"the command backend cannot aggregate 'from' specifications");
			}
			Aggregate aggregate = QueryFactory.eINSTANCE.createAggregate();
			aggregate.setMethod(method(aggregation));
			aggregate.setAlias(aggregation.getAlias());
			if (aggregation.getExpression() != null) {
				Expression bridged = ReadQueries.predicate(aggregation.getExpression(), entityType,
						null);
				if (bridged instanceof PropertyPath path && path.getBase() == null) {
					aggregate.setPath(path);
				} else {
					aggregate.setSource(bridged);
				}
			}
			stage.getAggregates().add(aggregate);
			columns.add(Column.alias(aggregation.getAlias()));
			aliases.add(aggregation.getAlias());
		}
	}

	private static org.eclipse.fennec.model.query.AggregateMethod method(
			AggregateExpression aggregation) {
		return switch (aggregation.getMethod()) {
			case SUM -> org.eclipse.fennec.model.query.AggregateMethod.SUM;
			case MIN -> org.eclipse.fennec.model.query.AggregateMethod.MIN;
			case MAX -> org.eclipse.fennec.model.query.AggregateMethod.MAX;
			case AVERAGE -> org.eclipse.fennec.model.query.AggregateMethod.AVG;
			case COUNT -> org.eclipse.fennec.model.query.AggregateMethod.COUNT;
			case COUNT_DISTINCT -> org.eclipse.fennec.model.query.AggregateMethod.COUNT_DISTINCT;
			default -> throw new UnsupportedOperationException(
					"the command backend cannot aggregate with the custom method '"
							+ aggregation.getCustomMethod() + "'");
		};
	}

	/** Entity-shaped context bridges like a $filter; row-shaped context binds aliases. */
	/** A terminal {@code topcount}/{@code bottomcount} expressed as an ordered window. */
	private record SortWindow(OrderBy orderBy, int count) {
	}

	/**
	 * {@code topcount(n, expr)} and {@code bottomcount(n, expr)} are an ordered window, not a
	 * new row shape: order the current set by {@code expr} and keep the first {@code n}. That
	 * is the query's own {@code ORDER BY} plus {@code top} — no window function, no new
	 * capability, and it works in entity space and after a grouping alike (persistence-jpa#259
	 * turned out not to be needed for this: its representatives are top-N PER GROUP, which is
	 * a different question and a different result shape).
	 *
	 * <p>Refused, each for its own reason:
	 * <ul>
	 * <li>{@code topsum}/{@code toppercent} and their {@code bottom*} mirrors — these select
	 * the smallest prefix whose RUNNING SUM reaches a threshold, and the IR has no windowed
	 * running aggregate to express it with;
	 * <li>a non-terminal position — the engines defer the sort window to the end of the
	 * pipeline, so a later stage would see the unlimited set;
	 * <li>a post-{@code $apply} {@code $filter} — same reason, it would run before the window
	 * rather than on the rows the window selected;
	 * <li>a post-{@code $apply} {@code $orderby} — one {@code ORDER BY} list cannot both pick
	 * the top rows and sort the answer;
	 * <li>a post-{@code $apply} {@code $skip} — it would page the underlying set, not the
	 * window ({@code $top} composes, because it reads the same order);
	 * <li>{@code $count} — the count is taken before the window, so it would report the
	 * untruncated set.
	 * </ul>
	 * All of them are honest 501s rather than a plausible wrong answer.
	 */
	private static SortWindow bottomTop(BottomTopTransformation transformation, boolean last,
			ApplyQuery query, EClass entityType, Set<String> aliases, List<Column> columns,
			Query irQuery, Pipeline pipeline) {
		BottomTopMethod method = transformation.getMethod();
		boolean descending = switch (method) {
			case TOP_COUNT -> true;
			case BOTTOM_COUNT -> false;
			default -> throw new UnsupportedOperationException("the command backend cannot apply '"
					+ label(method) + "': it selects the smallest prefix whose running sum reaches "
					+ "the threshold, and the query model has no windowed running aggregate");
		};
		if (!last) {
			throw new UnsupportedOperationException("the command backend supports '" + label(method)
					+ "' only as the final transformation — the sort window is applied at the end "
					+ "of the pipeline, so a later transformation would see the unlimited set");
		}
		if (query.rowFilter() != null || !query.orderBy().isEmpty() || query.skip() > 0
				|| query.count()) {
			throw new UnsupportedOperationException("the command backend cannot combine '"
					+ label(method) + "' with a post-$apply $filter, $orderby, $skip or $count — "
					+ "each of them would address the set the window was taken from, not the "
					+ "window ($top composes and is supported)");
		}
		int count = windowCount(transformation, entityType, aliases, columns, method);

		OrderBy orderBy = QueryFactory.eINSTANCE.createOrderBy();
		orderBy.setDirection(descending ? SortDirection.DESC : SortDirection.ASC);
		OclExpression value = transformation.getValue();
		if (value instanceof PropertyCallExp property && isPlainChain(property)) {
			orderBy.setPath(propertyPath(property));
		} else {
			orderBy.setKey(rowOrEntityExpression(value, entityType, aliases, columns));
		}

		// the reference executor drops rows whose value is null before ordering — they
		// contribute no rank, so they must not fill the window either
		IsNull notNull = ExpressionFactory.eINSTANCE.createIsNull();
		notNull.setSource(rowOrEntityExpression(value, entityType, aliases, columns));
		notNull.setNegated(true);
		if (pipeline.getStages().isEmpty()) {
			irQuery.setPredicate(irQuery.getPredicate() == null ? notNull
					: Expressions.and(irQuery.getPredicate(), notNull));
		} else {
			FilterStage stage = QueryFactory.eINSTANCE.createFilterStage();
			stage.setPredicate(notNull);
			pipeline.getStages().add(stage);
		}
		return new SortWindow(orderBy, count);
	}

	/** The threshold must be a positive integer constant — the window size is not a per-row value. */
	private static int windowCount(BottomTopTransformation transformation, EClass entityType,
			Set<String> aliases, List<Column> columns, BottomTopMethod method) {
		Expression threshold = rowOrEntityExpression(transformation.getThreshold(), entityType,
				aliases, columns);
		if (!(threshold instanceof IntegerLiteral literal) || literal.getValue() <= 0) {
			throw new UnsupportedOperationException("the command backend needs a positive integer "
					+ "constant as the '" + label(method) + "' count");
		}
		return (int) Math.min(literal.getValue(), Integer.MAX_VALUE);
	}

	/** The OData spelling of a method, for refusal messages. */
	private static String label(BottomTopMethod method) {
		return method.getName().toLowerCase().replace("_", "");
	}

	private static Expression rowOrEntityExpression(OclExpression ocl, EClass entityType,
			Set<String> aliases, List<Column> columns) {
		return columns.isEmpty() && aliases.isEmpty()
				? ReadQueries.predicate(ocl, entityType, null)
				: ReadQueries.rowExpression(ocl, aliases);
	}

	private static OrderBy orderBy(OrderBySegment segment, EClass entityType, Set<String> aliases,
			List<Column> columns) {
		OrderBy orderBy = QueryFactory.eINSTANCE.createOrderBy();
		orderBy.setDirection(segment.ascending() ? SortDirection.ASC : SortDirection.DESC);
		OclExpression expression = segment.expression();
		if (expression instanceof PropertyCallExp property && isPlainChain(property)) {
			orderBy.setPath(propertyPath(property));
		} else {
			orderBy.setKey(rowOrEntityExpression(expression, entityType, aliases, columns));
		}
		return orderBy;
	}

	private static boolean isPlainChain(OclExpression expression) {
		if (!(expression instanceof PropertyCallExp)) {
			return false;
		}
		OclExpression current = expression;
		while (current instanceof PropertyCallExp property) {
			if (property.getReferredProperty() == null) {
				return false;
			}
			current = property.getOwnedSource();
		}
		return current == null || (current instanceof VariableExp variable
				&& (variable.getReferredVariable() == null
						|| "self".equals(variable.getReferredVariable().getName())));
	}

	/** Grouping/sort paths must be plain property chains ([OASIS aggregation] groupby). */
	private static PropertyPath propertyPath(OclExpression expression) {
		List<EStructuralFeature> features = new ArrayList<>();
		OclExpression current = expression;
		while (current instanceof PropertyCallExp property) {
			if (property.getReferredProperty() == null) {
				throw new UnsupportedOperationException(
						"grouping properties must be resolved property paths");
			}
			features.add(0, property.getReferredProperty());
			current = property.getOwnedSource();
		}
		if (features.isEmpty()) {
			throw new UnsupportedOperationException("grouping properties must be property paths");
		}
		return Expressions.propertyPath(features.toArray(EStructuralFeature[]::new));
	}

	/** Grouping path {@code category/name} → segment names, root first (row-shape contract). */
	private static List<String> segments(OclExpression expression) {
		List<String> segments = new ArrayList<>();
		OclExpression current = expression;
		while (current instanceof PropertyCallExp property && property.getReferredProperty() != null) {
			segments.add(0, property.getReferredProperty().getName());
			current = property.getOwnedSource();
		}
		if (segments.isEmpty()) {
			throw new UnsupportedOperationException("grouping properties must be property paths");
		}
		return segments;
	}

	/** {@code QueryResultRow} → the ApplyResult row shape: paths nested, aliases flat. */
	static Map<String, Object> row(QueryResultRow source, List<Column> columns) {
		Map<String, Object> row = new LinkedHashMap<>();
		for (Column column : columns) {
			Object value = source.get(column.rowKey());
			if (column.nestedPath() == null) {
				row.put(column.rowKey(), value);
			} else {
				Map<String, Object> target = row;
				List<String> path = column.nestedPath();
				for (int i = 0; i < path.size() - 1; i++) {
					@SuppressWarnings("unchecked")
					Map<String, Object> next = (Map<String, Object>) target
							.computeIfAbsent(path.get(i), key -> new LinkedHashMap<String, Object>());
					target = next;
				}
				target.put(path.get(path.size() - 1), value);
			}
		}
		return row;
	}
}
