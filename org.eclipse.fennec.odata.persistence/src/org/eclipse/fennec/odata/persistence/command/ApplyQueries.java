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
 * row-flattened by the caller. Transformations the stage model cannot express
 * ({@code concat}, {@code bottom*}/{@code top*}, {@code rollup}, custom aggregates,
 * {@code from}) are refused with {@link UnsupportedOperationException} — the servlet
 * maps that to an honest 501.
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
		if (query.skip() > 0) {
			irQuery.setSkip(query.skip());
		}
		if (query.top() > 0) {
			irQuery.setTop(query.top());
		} else if (maxPageSize > 0) {
			irQuery.setTop(maxPageSize);
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
