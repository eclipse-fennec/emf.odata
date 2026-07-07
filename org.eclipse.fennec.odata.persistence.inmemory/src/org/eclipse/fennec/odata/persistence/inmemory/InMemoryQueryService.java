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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.EntityRepository;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.query.OclEvaluator;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * In-memory {@link QueryService} (req §3.5, the mandatory reference backend): aggregates all
 * registered {@link EntityRepository} services and evaluates the OCL predicate IR with the
 * {@link OclEvaluator}. Filter → order → count → skip/top, in that order — semantics every
 * pushdown backend (JPA, Mongo) must reproduce.
 */
@Component(service = QueryService.class)
public class InMemoryQueryService implements QueryService {

	private final List<EntityRepository> repositories = new CopyOnWriteArrayList<>();
	private final OclEvaluator evaluator = new OclEvaluator();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addRepository(EntityRepository repository) {
		repositories.add(repository);
	}

	void removeRepository(EntityRepository repository) {
		repositories.remove(repository);
	}

	@Override
	public boolean supports(EClass entityType) {
		return repositories.stream().anyMatch(r -> r.supplies(entityType));
	}

	@Override
	public ApplyResult executeApply(ApplyQuery query) {
		List<EObject> input = new ArrayList<>();
		for (EntityRepository repository : repositories) {
			if (repository.supplies(query.entityType())) {
				input.addAll(repository.entities(query.entityType()));
			}
		}
		List<Map<String, Object>> rows = new ArrayList<>(
				new ApplyExecutor().execute(query.pipeline(), input));

		// post-pipeline query options (OASIS: they operate on the transformed set)
		if (query.rowFilter() != null) {
			rows.removeIf(row -> !evaluator.matchesNullSafe(query.rowFilter(), row));
		}
		if (!query.orderBy().isEmpty()) {
			rows.sort(rowComparator(query.orderBy()));
		}
		long total = query.count() ? rows.size() : -1;
		int from = Math.min(query.skip(), rows.size());
		int to = query.top() < 0 ? rows.size() : Math.min(rows.size(), from + query.top());
		return new ApplyResult(rows.subList(from, to), total);
	}

	private Comparator<Map<String, Object>> rowComparator(List<OrderBySegment> orderBy) {
		Comparator<Map<String, Object>> comparator = null;
		for (OrderBySegment segment : orderBy) {
			Comparator<Map<String, Object>> keyOrder = Comparator.comparing(
					row -> asComparable(evaluator.evaluate(segment.expression(), row)),
					Comparator.nullsFirst(Comparator.naturalOrder()));
			if (!segment.ascending()) {
				keyOrder = keyOrder.reversed();
			}
			comparator = comparator == null ? keyOrder : comparator.thenComparing(keyOrder);
		}
		return comparator;
	}

	@Override
	public QueryResult execute(EntityQuery query) {
		List<EObject> matches = new ArrayList<>();
		for (EntityRepository repository : repositories) {
			if (!repository.supplies(query.entityType())) {
				continue;
			}
			for (EObject entity : repository.entities(query.entityType())) {
				if (query.castType() != null && !query.castType().isInstance(entity)) {
					continue; // URL type cast: only instances of the derived type ([OData-URL] 4.11)
				}
				if (query.filter() == null || evaluator.matchesNullSafe(query.filter(), entity)) {
					matches.add(entity);
				}
			}
		}

		if (!query.orderBy().isEmpty()) {
			matches.sort(comparator(query.orderBy()));
		}

		long total = query.count() ? matches.size() : -1;
		int from = Math.min(query.skip(), matches.size());
		int to = query.top() < 0 ? matches.size() : Math.min(matches.size(), from + query.top());
		return new QueryResult(matches.subList(from, to), total);
	}

	private Comparator<EObject> comparator(List<OrderBySegment> orderBy) {
		Comparator<EObject> comparator = null;
		for (OrderBySegment segment : orderBy) {
			Comparator<EObject> keyOrder = Comparator.comparing(
					entity -> asComparable(evaluator.evaluate(segment.expression(), entity)),
					Comparator.nullsFirst(Comparator.naturalOrder()));
			if (!segment.ascending()) {
				keyOrder = keyOrder.reversed();
			}
			comparator = comparator == null ? keyOrder : comparator.thenComparing(keyOrder);
		}
		return comparator;
	}

	@SuppressWarnings("unchecked")
	private static Comparable<Object> asComparable(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number number) { // uniform numeric ordering across Integer/BigDecimal/...
			return (Comparable<Object>) (Comparable<?>) new java.math.BigDecimal(number.toString());
		}
		if (value instanceof Comparable<?> comparable) {
			return (Comparable<Object>) comparable;
		}
		throw new IllegalArgumentException(
				"$orderby key is not comparable: " + value.getClass().getSimpleName());
	}
}
