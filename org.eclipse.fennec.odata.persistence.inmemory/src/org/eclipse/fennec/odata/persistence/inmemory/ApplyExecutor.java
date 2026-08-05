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
package org.eclipse.fennec.odata.persistence.inmemory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.odata.ocl.evaluator.OclEvaluator;
import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.apply.AggregateExpression;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ApplyTransformation;
import org.eclipse.fennec.odata.query.apply.BottomTopTransformation;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.eclipse.fennec.odata.query.apply.ConcatTransformation;
import org.eclipse.fennec.odata.query.apply.FilterTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;
import org.eclipse.fennec.odata.query.apply.IdentityTransformation;
import org.eclipse.fennec.odata.query.apply.OrderByExpression;
import org.eclipse.fennec.odata.query.apply.OrderByTransformation;
import org.eclipse.fennec.odata.query.apply.RollupHierarchy;
import org.eclipse.fennec.odata.query.apply.SkipTransformation;
import org.eclipse.fennec.odata.query.apply.TopTransformation;

/**
 * In-memory execution of the {@code $apply} pipeline submodel (E4-AP-4/req §3.5): stages run
 * left to right over rows, where a row is an {@link EObject} (before the first shape-changing
 * stage) or a {@link Map} (after {@code groupby}/{@code aggregate}/{@code compute} — grouping
 * paths become nested maps, aliases become top-level keys). Aggregation arithmetic uses
 * {@link BigDecimal}; aggregating a non-numeric or null operand is an error, never a silent 0.
 *
 * <p>Preserving transformations keep the row shape: {@code topcount}/{@code topsum}/
 * {@code toppercent} (and their {@code bottom*} mirrors), {@code top}/{@code skip},
 * {@code orderby}, {@code identity} and {@code concat} (each branch pipeline runs over the
 * stage input, results concatenated). {@code groupby} supports {@code rollup(...)} elements
 * as additional coarser grouping sets. NOT executable (→ {@link UnsupportedOperationException}
 * → 501, never a silently wrong result): {@code from} clauses, custom aggregation methods/
 * aggregates and every transformation outside the submodel.
 */
public class ApplyExecutor {

	private final OclEvaluator evaluator = new OclEvaluator();

	public List<Map<String, Object>> execute(ApplyPipeline pipeline, List<EObject> input) {
		List<Object> rows = new ArrayList<>(input);
		for (ApplyTransformation transformation : pipeline.getTransformations()) {
			rows = stage(transformation, rows);
		}
		return rows.stream().map(this::toRow).toList();
	}

	private List<Object> stage(ApplyTransformation transformation, List<Object> rows) {
		return switch (transformation) {
			case FilterTransformation filter -> rows.stream()
					.filter(row -> evaluator.matchesNullSafe(filter.getPredicate(), row)).toList();
			case ComputeTransformation compute -> rows.stream()
					.<Object>map(row -> computeRow(compute, row)).toList();
			case GroupByTransformation groupBy -> groupBy(groupBy, rows);
			case AggregateTransformation aggregate -> List.of(aggregateRows(aggregate, rows));
			case BottomTopTransformation bottomTop -> bottomTop(bottomTop, rows);
			case ConcatTransformation concat -> concat(concat, rows);
			case OrderByTransformation orderBy -> orderBy(orderBy, rows);
			case TopTransformation top -> rows.subList(0,
					(int) Math.min(top.getCount(), rows.size()));
			case SkipTransformation skip -> rows.subList(
					(int) Math.min(skip.getCount(), rows.size()), rows.size());
			case IdentityTransformation identity -> rows;
			default -> throw new UnsupportedOperationException("$apply transformation "
					+ transformation.eClass().getName() + " has no in-memory execution");
		};
	}

	private Map<String, Object> computeRow(ComputeTransformation compute, Object row) {
		Map<String, Object> result = toRow(row);
		for (ComputeExpression expression : compute.getComputeExpressions()) {
			result.put(expression.getAlias(), evaluator.evaluate(expression.getExpression(), row));
		}
		return result;
	}

	// --- groupby (incl. rollup grouping sets) ---

	private List<Object> groupBy(GroupByTransformation groupBy, List<Object> rows) {
		if (groupBy.getRollups().isEmpty()) {
			return groupBySet(groupBy.getGroupingProperties(), groupBy.getThen(), rows);
		}
		// rollup(l1,...,ln): the hierarchy contributes its level prefixes (n down to 1) as
		// grouping sets; multiple rollups combine as a cartesian product, plain grouping
		// properties are part of every set ([OData-Aggregation] leveled hierarchies)
		List<Object> result = new ArrayList<>();
		for (List<OclExpression> set : rollupSets(groupBy)) {
			result.addAll(groupBySet(set, groupBy.getThen(), rows));
		}
		return result;
	}

	private static List<List<OclExpression>> rollupSets(GroupByTransformation groupBy) {
		List<List<OclExpression>> sets = new ArrayList<>();
		sets.add(new ArrayList<>(groupBy.getGroupingProperties()));
		for (RollupHierarchy hierarchy : groupBy.getRollups()) {
			if (hierarchy.getHierarchy() != null) {
				throw new UnsupportedOperationException("named leveled hierarchies (rollup('"
						+ hierarchy.getHierarchy() + "')) have no in-memory execution");
			}
			List<List<OclExpression>> expanded = new ArrayList<>();
			for (int depth = hierarchy.getLevels().size(); depth >= 1; depth--) {
				for (List<OclExpression> base : sets) {
					List<OclExpression> set = new ArrayList<>(base);
					set.addAll(hierarchy.getLevels().subList(0, depth));
					expanded.add(set);
				}
			}
			sets = expanded;
		}
		return sets;
	}

	private List<Object> groupBySet(List<OclExpression> groupingProperties,
			ApplyTransformation then, List<Object> rows) {
		Map<List<Object>, List<Object>> groups = new LinkedHashMap<>();
		for (Object row : rows) {
			List<Object> key = groupingProperties.stream()
					.map(property -> evaluator.evaluate(property, row)).toList();
			groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
		}

		List<Object> result = new ArrayList<>();
		for (Map.Entry<List<Object>, List<Object>> group : groups.entrySet()) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 0; i < groupingProperties.size(); i++) {
				putPath(row, path(groupingProperties.get(i)), group.getKey().get(i));
			}
			if (then instanceof AggregateTransformation aggregate) {
				row.putAll(aggregateRows(aggregate, group.getValue()));
			} else if (then != null) {
				throw new UnsupportedOperationException(
						"only aggregate is supported as nested groupby transformation");
			}
			result.add(row);
		}
		return result;
	}

	// --- preserving transformations ---

	private record ValuedRow(Object row, BigDecimal value) {
	}

	private List<Object> bottomTop(BottomTopTransformation trafo, List<Object> rows) {
		BigDecimal threshold = toDecimal(evaluator.evaluate(trafo.getThreshold(), null),
				"bottom/top threshold");
		boolean top = switch (trafo.getMethod()) {
			case TOP_COUNT, TOP_SUM, TOP_PERCENT -> true;
			default -> false;
		};
		// null values never make it into the selected subset (they contribute no order/sum)
		List<ValuedRow> valued = new ArrayList<>();
		for (Object row : rows) {
			Object value = evaluator.evaluate(trafo.getValue(), row);
			if (value != null) {
				valued.add(new ValuedRow(row, toDecimal(value, "bottom/top value")));
			}
		}
		Comparator<ValuedRow> order = Comparator.comparing(ValuedRow::value);
		valued.sort(top ? order.reversed() : order);

		return switch (trafo.getMethod()) {
			case TOP_COUNT, BOTTOM_COUNT -> valued.stream()
					.limit(Math.max(0, threshold.longValue())).map(ValuedRow::row).toList();
			default -> {
				BigDecimal total = valued.stream().map(ValuedRow::value)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal target = switch (trafo.getMethod()) {
					case TOP_SUM, BOTTOM_SUM -> threshold;
					// toppercent/bottompercent: the smallest subset reaching p% of the total
					default -> total.multiply(threshold)
							.divide(BigDecimal.valueOf(100), MathContext.DECIMAL64);
				};
				List<Object> selected = new ArrayList<>();
				BigDecimal sum = BigDecimal.ZERO;
				for (ValuedRow row : valued) {
					if (sum.compareTo(target) >= 0) {
						break;
					}
					selected.add(row.row());
					sum = sum.add(row.value());
				}
				// the whole set stays when it cannot reach the requested sum
				yield sum.compareTo(target) < 0 ? rows : selected;
			}
		};
	}

	private List<Object> concat(ConcatTransformation concat, List<Object> rows) {
		List<Object> result = new ArrayList<>();
		for (ApplyPipeline branch : concat.getPipelines()) {
			List<Object> branchRows = new ArrayList<>(rows);
			for (ApplyTransformation transformation : branch.getTransformations()) {
				branchRows = stage(transformation, branchRows);
			}
			result.addAll(branchRows);
		}
		return result;
	}

	private List<Object> orderBy(OrderByTransformation orderBy, List<Object> rows) {
		Comparator<Object> comparator = null;
		for (OrderByExpression item : orderBy.getItems()) {
			Comparator<Object> keyOrder = Comparator.comparing(
					row -> asComparable(evaluator.evaluate(item.getExpression(), row)),
					Comparator.nullsFirst(Comparator.naturalOrder()));
			if (!item.isAscending()) {
				keyOrder = keyOrder.reversed();
			}
			comparator = comparator == null ? keyOrder : comparator.thenComparing(keyOrder);
		}
		List<Object> sorted = new ArrayList<>(rows);
		if (comparator != null) {
			try {
				sorted.sort(comparator);
			} catch (ClassCastException e) {
				throw new ODataQueryParseException("the orderby keys are not mutually comparable", e);
			}
		}
		return sorted;
	}

	@SuppressWarnings("unchecked")
	private static Comparable<Object> asComparable(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) { // uniform numeric ordering across Integer/BigDecimal/...
			return (Comparable<Object>) (Comparable<?>) new BigDecimal(number.toString());
		}
		if (value instanceof Comparable<?> comparable) {
			return (Comparable<Object>) comparable;
		}
		throw new IllegalArgumentException(
				"orderby key is not comparable: " + value.getClass().getSimpleName());
	}

	// --- aggregation ---

	private Map<String, Object> aggregateRows(AggregateTransformation aggregate, List<Object> rows) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (AggregateExpression aggregation : aggregate.getAggregations()) {
			result.put(aggregation.getAlias(), aggregateValue(aggregation, rows));
		}
		return result;
	}

	private Object aggregateValue(AggregateExpression aggregation, List<Object> rows) {
		if (!aggregation.getFrom().isEmpty()) {
			throw new UnsupportedOperationException(
					"aggregate 'from' clauses have no in-memory execution");
		}
		switch (aggregation.getMethod()) {
			case CUSTOM, CUSTOM_AGGREGATE:
				throw new UnsupportedOperationException("custom aggregation methods/aggregates"
						+ " have no in-memory execution: " + aggregation.getCustomMethod());
			case COUNT:
				if (aggregation.getExpression() == null) {
					return (long) rows.size(); // bare $count: the number of rows
				}
				// path/$count: the number of related instances = the sizes summed up
				return rows.stream().mapToLong(row -> ((Number) evaluator
						.evaluate(aggregation.getExpression(), row)).longValue()).sum();
			case COUNT_DISTINCT:
				return rows.stream().map(row -> evaluator.evaluate(aggregation.getExpression(), row))
						.distinct().count();
			default: {
				List<BigDecimal> values = rows.stream()
						.map(row -> evaluator.evaluate(aggregation.getExpression(), row))
						.filter(Objects::nonNull) // OASIS aggregation: nulls are ignored
						.map(value -> toDecimal(value, aggregation.getAlias())).toList();
				if (values.isEmpty()) {
					return null; // empty group aggregates to null, not 0
				}
				return switch (aggregation.getMethod()) {
					case SUM -> values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
					case MIN -> values.stream().min(BigDecimal::compareTo).orElseThrow();
					case MAX -> values.stream().max(BigDecimal::compareTo).orElseThrow();
					case AVERAGE -> values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
							.divide(BigDecimal.valueOf(values.size()), MathContext.DECIMAL64);
					default -> throw new IllegalArgumentException(
							"unsupported aggregate method " + aggregation.getMethod());
				};
			}
		}
	}

	private static BigDecimal toDecimal(Object value, String alias) {
		if (value instanceof BigDecimal decimal) {
			return decimal;
		}
		if (value instanceof Number number) {
			return new BigDecimal(number.toString());
		}
		throw new IllegalArgumentException(
				"aggregate '" + alias + "' over a non-numeric value: "
						+ (value == null ? "null" : value.getClass().getSimpleName()));
	}

	/** Row → map: EObject rows flatten to their attribute values (no references, v1). */
	private Map<String, Object> toRow(Object row) {
		if (row instanceof Map<?, ?> map) {
			Map<String, Object> copy = new LinkedHashMap<>();
			map.forEach((key, value) -> copy.put(String.valueOf(key), value));
			return copy;
		}
		EObject entity = (EObject) row;
		Map<String, Object> result = new LinkedHashMap<>();
		for (EAttribute attribute : entity.eClass().getEAllAttributes()) {
			result.put(attribute.getName(), entity.eGet(attribute));
		}
		return result;
	}

	/** Grouping path {@code category/name} → segment names, root first. */
	private static List<String> path(OclExpression groupingProperty) {
		List<String> segments = new ArrayList<>();
		OclExpression current = groupingProperty;
		while (current instanceof PropertyCallExp property) {
			segments.add(0, property.getReferredProperty().getName());
			current = property.getOwnedSource();
		}
		if (segments.isEmpty()) {
			throw new IllegalArgumentException("groupby expects property paths");
		}
		return segments;
	}

	@SuppressWarnings("unchecked")
	private static void putPath(Map<String, Object> row, List<String> path, Object value) {
		Map<String, Object> target = row;
		for (int i = 0; i < path.size() - 1; i++) {
			target = (Map<String, Object>) target.computeIfAbsent(path.get(i),
					key -> new LinkedHashMap<String, Object>());
		}
		target.put(path.get(path.size() - 1), value);
	}

}
