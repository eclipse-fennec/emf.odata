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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.odata.query.apply.AggregateExpression;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ApplyTransformation;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.eclipse.fennec.odata.query.apply.FilterTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;

/**
 * In-memory execution of the {@code $apply} pipeline submodel (E4-AP-4/req §3.5): stages run
 * left to right over rows, where a row is an {@link EObject} (before the first shape-changing
 * stage) or a {@link Map} (after {@code groupby}/{@code aggregate}/{@code compute} — grouping
 * paths become nested maps, aliases become top-level keys). Aggregation arithmetic uses
 * {@link BigDecimal}; aggregating a non-numeric or null operand is an error, never a silent 0.
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
			default -> throw new IllegalArgumentException(
					"unsupported $apply transformation " + transformation.eClass().getName());
		};
	}

	private Map<String, Object> computeRow(ComputeTransformation compute, Object row) {
		Map<String, Object> result = toRow(row);
		for (ComputeExpression expression : compute.getComputeExpressions()) {
			result.put(expression.getAlias(), evaluator.evaluate(expression.getExpression(), row));
		}
		return result;
	}

	private List<Object> groupBy(GroupByTransformation groupBy, List<Object> rows) {
		Map<List<Object>, List<Object>> groups = new LinkedHashMap<>();
		for (Object row : rows) {
			List<Object> key = groupBy.getGroupingProperties().stream()
					.map(property -> evaluator.evaluate(property, row)).toList();
			groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
		}

		List<Object> result = new ArrayList<>();
		for (Map.Entry<List<Object>, List<Object>> group : groups.entrySet()) {
			Map<String, Object> row = new LinkedHashMap<>();
			for (int i = 0; i < groupBy.getGroupingProperties().size(); i++) {
				putPath(row, path(groupBy.getGroupingProperties().get(i)), group.getKey().get(i));
			}
			if (groupBy.getThen() instanceof AggregateTransformation aggregate) {
				row.putAll(aggregateRows(aggregate, group.getValue()));
			} else if (groupBy.getThen() != null) {
				throw new IllegalArgumentException(
						"only aggregate is supported as nested groupby transformation");
			}
			result.add(row);
		}
		return result;
	}

	private Map<String, Object> aggregateRows(AggregateTransformation aggregate, List<Object> rows) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (AggregateExpression aggregation : aggregate.getAggregations()) {
			result.put(aggregation.getAlias(), aggregateValue(aggregation, rows));
		}
		return result;
	}

	private Object aggregateValue(AggregateExpression aggregation, List<Object> rows) {
		switch (aggregation.getMethod()) {
			case COUNT:
				return (long) rows.size();
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
				"aggregate '" + alias + "' over a non-numeric value: " + value.getClass().getSimpleName());
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
