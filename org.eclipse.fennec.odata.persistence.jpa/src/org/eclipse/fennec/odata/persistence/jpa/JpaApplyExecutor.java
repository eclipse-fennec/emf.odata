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
package org.eclipse.fennec.odata.persistence.jpa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.odata.query.apply.AggregateExpression;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyTransformation;
import org.eclipse.fennec.odata.query.apply.FilterTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.EntityType;

/**
 * {@code $apply} pushdown (E5): translates the aggregation pipeline into ONE grouped
 * criteria query — {@code filter} stages before the grouping become WHERE, the grouping
 * stage becomes GROUP BY with aggregate selections, {@code filter} stages after the
 * grouping and the post-pipeline {@code $filter} become HAVING (aliases and grouped paths
 * resolve to their criteria expressions), {@code $orderby}/paging run in the database.
 *
 * <p>Grouping paths use LEFT joins so entities with null navigations form their own group
 * (OASIS aggregation semantics, matching the in-memory reference); SQL aggregates ignore
 * nulls natively, exactly like the reference's null-ignoring arithmetic. Result rows mirror
 * the reference shape: grouping paths as nested maps, aggregate aliases top-level.
 *
 * <p>v1 pipeline subset (everything else raises {@link UnsupportedOperationException} → 501,
 * never a silently wrong aggregate): optional leading {@code filter} stages, at most ONE
 * {@code groupby} (with nested {@code aggregate}) or standalone {@code aggregate} stage,
 * optional trailing {@code filter} stages; numeric {@code min}/{@code max}/{@code sum}/
 * {@code average}, {@code countdistinct} and {@code $count}.
 */
class JpaApplyExecutor {

	private final OclToCriteriaTranslator translator = new OclToCriteriaTranslator();

	/** The pipeline split into WHERE stages, ONE grouping/aggregation stage, HAVING stages. */
	private record Stages(List<OclExpression> where, GroupByTransformation groupBy,
			AggregateTransformation aggregate, List<OclExpression> having) {
	}

	/** One grouping path: its OCL expression and the slash-joined segment names. */
	private record GroupingPath(OclExpression expression, List<String> segments) {

		String name() {
			return String.join("/", segments);
		}
	}

	ApplyResult execute(ApplyQuery query, EntityManager em, EntityType<?> entity) {
		Stages stages = split(query);
		CriteriaBuilder cb = em.getCriteriaBuilder();

		List<GroupingPath> groupings = groupingPaths(stages.groupBy());
		List<AggregateExpression> aggregations = stages.aggregate() == null ? List.of()
				: stages.aggregate().getAggregations();

		// --- the grouped tuple query ---
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<?> root = cq.from(entity);
		Composition composition = compose(query, stages, groupings, aggregations, cb, cq, root);
		cq.multiselect(composition.selections());
		if (!query.orderBy().isEmpty()) {
			cq.orderBy(orders(query.orderBy(), cb, cq, root, composition.named()));
		}

		TypedQuery<Tuple> typedQuery = em.createQuery(cq);
		if (query.skip() > 0) {
			typedQuery.setFirstResult(query.skip());
		}
		if (query.top() >= 0) {
			typedQuery.setMaxResults(query.top());
		}
		List<Map<String, Object>> rows = new ArrayList<>();
		for (Tuple tuple : typedQuery.getResultList()) {
			rows.add(row(tuple, groupings, aggregations));
		}

		long total = query.count() ? countGroups(query, stages, groupings, aggregations, em, entity) : -1;
		return new ApplyResult(rows, total);
	}

	/** Everything both queries share: selections in order plus the named-expression scope. */
	private record Composition(List<Selection<?>> selections, Map<String, Expression<?>> named) {
	}

	/** Builds WHERE/GROUP BY/HAVING and the selections against the given query's root. */
	private Composition compose(ApplyQuery query, Stages stages, List<GroupingPath> groupings,
			List<AggregateExpression> aggregations, CriteriaBuilder cb, CriteriaQuery<?> cq,
			Root<?> root) {
		if (!stages.where().isEmpty()) {
			cq.where(stages.where().stream()
					.map(predicate -> translator.predicate(predicate, cb, cq, root))
					.toArray(Predicate[]::new));
		}

		Map<String, Expression<?>> named = new LinkedHashMap<>();
		List<Selection<?>> selections = new ArrayList<>();
		List<Expression<?>> groupBy = new ArrayList<>();
		for (GroupingPath grouping : groupings) {
			Expression<?> path = leftJoinedPath(root, grouping.segments());
			named.put(grouping.name(), path);
			selections.add(path);
			groupBy.add(path);
		}
		for (AggregateExpression aggregation : aggregations) {
			Expression<?> aggregate = aggregate(aggregation, cb, cq, root);
			named.put(aggregation.getAlias(), aggregate);
			selections.add(aggregate);
		}
		if (!groupBy.isEmpty()) {
			cq.groupBy(groupBy.toArray(Expression[]::new));
		}

		List<Predicate> having = new ArrayList<>();
		for (OclExpression predicate : stages.having()) {
			having.add(translator.predicate(predicate, cb, cq, root, named));
		}
		if (query.rowFilter() != null) {
			having.add(translator.predicate(query.rowFilter(), cb, cq, root, named));
		}
		if (!having.isEmpty()) {
			cq.having(having.toArray(Predicate[]::new));
		}
		return new Composition(selections, named);
	}

	/**
	 * Group count BEFORE paging: the same grouped query trimmed to the group keys — grouping
	 * and filtering stay in the database, only one slim tuple per group travels. (A portable
	 * single-value COUNT over multi-column groups does not exist in the Criteria API.)
	 */
	private long countGroups(ApplyQuery query, Stages stages, List<GroupingPath> groupings,
			List<AggregateExpression> aggregations, EntityManager em, EntityType<?> entity) {
		if (groupings.isEmpty()) {
			return 1; // ungrouped aggregate: always exactly one row
		}
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<?> root = cq.from(entity);
		Composition composition = compose(query, stages, groupings, aggregations, cb, cq, root);
		cq.multiselect(composition.selections().subList(0, groupings.size()));
		return em.createQuery(cq).getResultList().size();
	}

	// --- pipeline analysis ---

	private Stages split(ApplyQuery query) {
		List<OclExpression> where = new ArrayList<>();
		List<OclExpression> having = new ArrayList<>();
		GroupByTransformation groupBy = null;
		AggregateTransformation aggregate = null;
		for (ApplyTransformation stage : query.pipeline().getTransformations()) {
			switch (stage) {
				case FilterTransformation filter -> {
					(groupBy == null && aggregate == null ? where : having).add(filter.getPredicate());
				}
				case GroupByTransformation grouping -> {
					if (groupBy != null || aggregate != null) {
						throw new UnsupportedOperationException(
								"only one grouping stage has a JPA pushdown");
					}
					groupBy = grouping;
					if (grouping.getThen() != null) {
						if (!(grouping.getThen() instanceof AggregateTransformation nested)) {
							throw new UnsupportedOperationException(
									"only aggregate is supported as nested groupby transformation");
						}
						aggregate = nested;
					}
				}
				case AggregateTransformation aggregation -> {
					if (groupBy != null || aggregate != null) {
						throw new UnsupportedOperationException(
								"only one aggregation stage has a JPA pushdown");
					}
					aggregate = aggregation;
				}
				default -> throw new UnsupportedOperationException("transformation '"
						+ stage.eClass().getName() + "' has no JPA pushdown");
			}
		}
		if (groupBy == null && aggregate == null) {
			throw new UnsupportedOperationException(
					"$apply without a grouping/aggregation stage has no JPA pushdown");
		}
		return new Stages(where, groupBy, aggregate, having);
	}

	private List<GroupingPath> groupingPaths(GroupByTransformation groupBy) {
		if (groupBy == null) {
			return List.of();
		}
		List<GroupingPath> paths = new ArrayList<>();
		for (OclExpression property : groupBy.getGroupingProperties()) {
			if (!(property instanceof PropertyCallExp call)) {
				throw new UnsupportedOperationException("grouping requires plain property paths");
			}
			String name = OclToCriteriaTranslator.pathName(call);
			if (name == null) {
				throw new UnsupportedOperationException("grouping requires plain property paths");
			}
			paths.add(new GroupingPath(property, List.of(name.split("/"))));
		}
		return paths;
	}

	/**
	 * Grouping path via LEFT joins: entities with null navigations must form their own group
	 * (the in-memory reference propagates null) — an implicit inner join would drop them.
	 */
	private static Expression<?> leftJoinedPath(Root<?> root, List<String> segments) {
		From<?, ?> from = root;
		for (int i = 0; i < segments.size() - 1; i++) {
			from = from.join(segments.get(i), JoinType.LEFT);
		}
		return from.get(segments.get(segments.size() - 1));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Expression<?> aggregate(AggregateExpression aggregation, CriteriaBuilder cb,
			CriteriaQuery<?> cq, Root<?> root) {
		if (aggregation.getMethod() == null) {
			throw new UnsupportedOperationException("aggregate without a method");
		}
		return switch (aggregation.getMethod()) {
			case COUNT -> cb.count(root); // $count virtual aggregate, no operand
			case COUNT_DISTINCT -> cb.countDistinct(
					translator.expression(aggregation.getExpression(), cb, cq, root));
			case SUM -> cb.sum((Expression) translator
					.expression(aggregation.getExpression(), cb, cq, root));
			case MIN -> cb.min((Expression) translator
					.expression(aggregation.getExpression(), cb, cq, root));
			case MAX -> cb.max((Expression) translator
					.expression(aggregation.getExpression(), cb, cq, root));
			case AVERAGE -> cb.avg((Expression) translator
					.expression(aggregation.getExpression(), cb, cq, root));
		};
	}

	private List<Order> orders(List<OrderBySegment> orderBy, CriteriaBuilder cb,
			CriteriaQuery<?> cq, Root<?> root, Map<String, Expression<?>> named) {
		List<Order> orders = new ArrayList<>();
		for (OrderBySegment segment : orderBy) {
			Expression<?> key = translator.expression(segment.expression(), cb, cq, root, named);
			orders.add(segment.ascending() ? cb.asc(key) : cb.desc(key));
		}
		return orders;
	}

	/** Reference row shape: grouping paths as nested maps, aggregate aliases top-level. */
	private static Map<String, Object> row(Tuple tuple, List<GroupingPath> groupings,
			List<AggregateExpression> aggregations) {
		Map<String, Object> row = new LinkedHashMap<>();
		int index = 0;
		for (GroupingPath grouping : groupings) {
			nest(row, grouping.segments(), tuple.get(index++));
		}
		for (AggregateExpression aggregation : aggregations) {
			row.put(aggregation.getAlias(), tuple.get(index++));
		}
		return row;
	}

	@SuppressWarnings("unchecked")
	private static void nest(Map<String, Object> row, List<String> segments, Object value) {
		Map<String, Object> target = row;
		for (int i = 0; i < segments.size() - 1; i++) {
			target = (Map<String, Object>) target
					.computeIfAbsent(segments.get(i), key -> new LinkedHashMap<String, Object>());
		}
		target.put(segments.get(segments.size() - 1), value);
	}
}
