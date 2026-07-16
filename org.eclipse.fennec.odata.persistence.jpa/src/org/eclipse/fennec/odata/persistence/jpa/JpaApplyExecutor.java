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
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.fennec.odata.query.apply.AggregateExpression;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyTransformation;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
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

	/**
	 * The pipeline split into WHERE stages, pre-grouping computes, ONE grouping/aggregation,
	 * post-grouping computes (columns over the aggregates/grouping keys) and HAVING.
	 */
	private record Stages(List<OclExpression> where, List<ComputeTransformation> computes,
			GroupByTransformation groupBy, AggregateTransformation aggregate,
			List<ComputeTransformation> postComputes, List<OclExpression> having) {
	}

	/** One result column: nested map segments for grouping paths, one segment otherwise. */
	private record Column(List<String> segments) {
	}

	/** One grouping path: its OCL expression and the slash-joined segment names. */
	private record GroupingPath(OclExpression expression, List<String> segments) {

		String name() {
			return String.join("/", segments);
		}
	}

	ApplyResult execute(ApplyQuery query, EntityManager em, EntityType<?> entity, int maxPageSize) {
		Stages stages = split(query);
		CriteriaBuilder cb = em.getCriteriaBuilder();

		List<GroupingPath> groupings = groupingPaths(stages.groupBy());
		List<AggregateExpression> aggregations = stages.aggregate() == null ? List.of()
				: stages.aggregate().getAggregations();

		// --- the grouped tuple query ---
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<?> root = cq.from(entity);
		Composition composition = compose(query, stages, groupings, aggregations, cb, cq, root);
		// cb.tuple(...) (not the deprecated multiselect); the varargs overload is the pre-3.2 form
		// the JPA provider actually implements — tuple(List) is a 3.2 addition it does not yet have
		cq.select(cb.tuple(composition.selections().toArray(Selection[]::new)));
		if (!query.orderBy().isEmpty()) {
			cq.orderBy(orders(query.orderBy(), cb, cq, root, composition.named()));
		}

		TypedQuery<Tuple> typedQuery = em.createQuery(cq);
		if (query.skip() > 0) {
			typedQuery.setFirstResult(query.skip());
		}
		if (query.top() >= 0) {
			typedQuery.setMaxResults(query.top());
		} else if (maxPageSize > 0) {
			// server-driven safety net (same as the read path): a groupby over a high-cardinality
			// property with no $top would otherwise materialize one row per group into the heap
			typedQuery.setMaxResults(maxPageSize);
		}
		List<Map<String, Object>> rows = new ArrayList<>();
		for (Tuple tuple : typedQuery.getResultList()) {
			rows.add(row(tuple, composition.columns()));
		}

		long total = query.count() ? countGroups(query, stages, groupings, aggregations, em, entity) : -1;
		return new ApplyResult(rows, total);
	}

	/** Everything both queries share: selections, their column shapes, the named scope. */
	private record Composition(List<Selection<?>> selections, List<Column> columns,
			Map<String, Expression<?>> named) {
	}

	/** Builds WHERE/GROUP BY/HAVING and the selections against the given query's root. */
	private Composition compose(ApplyQuery query, Stages stages, List<GroupingPath> groupings,
			List<AggregateExpression> aggregations, CriteriaBuilder cb, CriteriaQuery<?> cq,
			Root<?> root) {
		List<Predicate> where = new ArrayList<>();
		for (OclExpression predicate : stages.where()) {
			where.add(translator.predicate(predicate, cb, cq, root));
		}

		Map<String, Expression<?>> named = new LinkedHashMap<>();
		List<Selection<?>> selections = new ArrayList<>();
		List<Column> columns = new ArrayList<>();
		// computed aliases first — visible to grouping, aggregates, HAVING and $orderby
		List<String> computedAliases = new ArrayList<>();
		for (ComputeTransformation stage : stages.computes()) {
			for (ComputeExpression compute : stage.getComputeExpressions()) {
				named.put(compute.getAlias(), translator.expression(
						compute.getExpression(), cb, cq, root, named));
				computedAliases.add(compute.getAlias());
			}
		}
		List<Expression<?>> groupBy = new ArrayList<>();
		for (GroupingPath grouping : groupings) {
			Expression<?> path = leftJoinedPath(root, grouping.segments());
			named.put(grouping.name(), path);
			selections.add(path);
			columns.add(new Column(grouping.segments()));
			groupBy.add(path);
		}
		for (AggregateExpression aggregation : aggregations) {
			Expression<?> aggregate = aggregate(aggregation, cb, cq, root, named);
			named.put(aggregation.getAlias(), aggregate);
			selections.add(aggregate);
			columns.add(new Column(List.of(aggregation.getAlias())));
		}
		// post-grouping computes: columns over the aggregate/grouping expressions (e.g.
		// Total div Cnt as Avg) — added AFTER the aggregates so they resolve via `named`, and
		// before HAVING so a trailing filter may reference a computed alias
		for (ComputeTransformation stage : stages.postComputes()) {
			for (ComputeExpression compute : stage.getComputeExpressions()) {
				Expression<?> value = translator.expression(compute.getExpression(), cb, cq, root, named);
				named.put(compute.getAlias(), value);
				selections.add(value);
				columns.add(new Column(List.of(compute.getAlias())));
			}
		}
		if (!groupBy.isEmpty()) {
			cq.groupBy(groupBy.toArray(Expression[]::new));
		}
		if (groupings.isEmpty() && aggregations.isEmpty()) {
			// terminal compute: one row per entity — its attributes plus the aliases
			EClass shape = query.entityType();
			for (EAttribute attribute : shape.getEAllAttributes()) {
				if (!attribute.isMany()) {
					selections.add(root.get(attribute.getName()));
					columns.add(new Column(List.of(attribute.getName())));
				}
			}
			for (String alias : computedAliases) {
				selections.add(named.get(alias));
				columns.add(new Column(List.of(alias)));
			}
		}

		// post-pipeline predicates: HAVING for grouped queries, plain WHERE otherwise
		// (SQL only allows HAVING with GROUP BY; terminal compute rows have no grouping)
		boolean grouped = !groupings.isEmpty() || !aggregations.isEmpty();
		List<Predicate> post = new ArrayList<>();
		for (OclExpression predicate : stages.having()) {
			post.add(translator.predicate(predicate, cb, cq, root, named));
		}
		if (query.rowFilter() != null) {
			post.add(translator.predicate(query.rowFilter(), cb, cq, root, named));
		}
		if (grouped && !post.isEmpty()) {
			cq.having(post.toArray(Predicate[]::new));
		} else {
			where.addAll(post);
		}
		if (!where.isEmpty()) {
			cq.where(where.toArray(Predicate[]::new));
		}
		return new Composition(selections, columns, named);
	}

	/**
	 * Group count BEFORE paging: the same grouped query trimmed to the group keys — grouping
	 * and filtering stay in the database, only one slim tuple per group travels. (A portable
	 * single-value COUNT over multi-column groups does not exist in the Criteria API.)
	 */
	private long countGroups(ApplyQuery query, Stages stages, List<GroupingPath> groupings,
			List<AggregateExpression> aggregations, EntityManager em, EntityType<?> entity) {
		if (groupings.isEmpty() && stages.aggregate() != null) {
			return 1; // ungrouped aggregate: always exactly one row
		}
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> cq = cb.createTupleQuery();
		Root<?> root = cq.from(entity);
		Composition composition = compose(query, stages, groupings, aggregations, cb, cq, root);
		int keyColumns = groupings.isEmpty() ? 1 : groupings.size();
		cq.select(cb.tuple(composition.selections().subList(0, keyColumns).toArray(Selection[]::new)));
		return em.createQuery(cq).getResultList().size();
	}

	// --- pipeline analysis ---

	private Stages split(ApplyQuery query) {
		List<OclExpression> where = new ArrayList<>();
		List<OclExpression> having = new ArrayList<>();
		List<ComputeTransformation> computes = new ArrayList<>();
		List<ComputeTransformation> postComputes = new ArrayList<>();
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
					if (!grouping.getRollups().isEmpty()) {
						throw new UnsupportedOperationException(
								"rollup grouping sets have no JPA pushdown");
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
				case ComputeTransformation compute -> {
					// a compute after the grouping adds columns over the aggregates/grouping keys
					(groupBy == null && aggregate == null ? computes : postComputes).add(compute);
				}
				default -> throw new UnsupportedOperationException("transformation '"
						+ stage.eClass().getName() + "' has no JPA pushdown");
			}
		}
		if (groupBy == null && aggregate == null && computes.isEmpty()) {
			throw new UnsupportedOperationException(
					"$apply without a grouping/aggregation/compute stage has no JPA pushdown");
		}
		return new Stages(where, computes, groupBy, aggregate, postComputes, having);
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
			CriteriaQuery<?> cq, Root<?> root, Map<String, Expression<?>> named) {
		if (aggregation.getMethod() == null) {
			throw new UnsupportedOperationException("aggregate without a method");
		}
		if (!aggregation.getFrom().isEmpty()) {
			throw new UnsupportedOperationException("aggregate 'from' clauses have no JPA pushdown");
		}
		return switch (aggregation.getMethod()) {
			case CUSTOM, CUSTOM_AGGREGATE -> throw new UnsupportedOperationException(
					"custom aggregation methods/aggregates have no JPA pushdown: "
							+ aggregation.getCustomMethod());
			case COUNT -> {
				if (aggregation.getExpression() != null) {
					throw new UnsupportedOperationException(
							"'path/$count' aggregates have no JPA pushdown");
				}
				yield cb.count(root); // $count virtual aggregate, no operand
			}
			case COUNT_DISTINCT -> cb.countDistinct(
					translator.expression(aggregation.getExpression(), cb, cq, root, named));
			case SUM -> cb.sum((Expression) translator
					.expression(aggregation.getExpression(), cb, cq, root, named));
			case MIN -> cb.min((Expression) translator
					.expression(aggregation.getExpression(), cb, cq, root, named));
			case MAX -> cb.max((Expression) translator
					.expression(aggregation.getExpression(), cb, cq, root, named));
			case AVERAGE -> cb.avg((Expression) translator
					.expression(aggregation.getExpression(), cb, cq, root, named));
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

	/** Reference row shape: grouping paths as nested maps, aliases/attributes top-level. */
	private static Map<String, Object> row(Tuple tuple, List<Column> columns) {
		Map<String, Object> row = new LinkedHashMap<>();
		int index = 0;
		for (Column column : columns) {
			nest(row, column.segments(), tuple.get(index++));
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
