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
package org.eclipse.fennec.odata.persistence.read;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.query.Expand;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.QueryFactory;
import org.eclipse.fennec.model.query.builder.Expressions;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.ExpandPushdown;
import org.eclipse.fennec.odata.persistence.api.ExpandSpec;
import org.eclipse.fennec.persistence.capabilities.QueryFeature;
import org.eclipse.fennec.persistence.query.support.QueryValidator;

/**
 * Plans an OData entity read as ONE Fennec {@link Query} — the part of the read path that
 * does not care where the query is executed. Both backends of this bundle share it: the
 * command backend runs the plan against a {@code QueryableResource} of a persistence unit,
 * the repository backend hands it to a {@code ReadRepository} facade.
 *
 * <p>A plan is the IR envelope (predicate, ordering, paging, expands) plus what the
 * {@code $expand} options became: pushed down where the backend declares the capability
 * (ADR-0008), left to an in-memory walk over the resolved proxies otherwise. Capability
 * violations reported by a validator turn into the SPI's error classes — unsupported
 * features → {@link UnsupportedOperationException} (501), structural violations →
 * {@link IllegalArgumentException} (400).
 */
public final class ReadPlans {

	private ReadPlans() {
	}

	/**
	 * One planned page read.
	 *
	 * @param query the IR query to execute
	 * @param pushedExpands per {@code $expand} path, what of its options the query pushes down
	 * @param chains the navigation chains whose targets must be resolved in memory afterwards
	 *        (plain expands, and expands whose options were not pushed)
	 * @param narrowedChains the chains the store resolves itself with the pushed options, by
	 *        {@code $expand} path — {@link #withoutExpandOptions()} moves them into {@code chains}
	 */
	public record Page(Query query, Map<String, ExpandPushdown> pushedExpands,
			List<List<EReference>> chains, Map<String, List<EReference>> narrowedChains) {

		public Page {
			pushedExpands = Map.copyOf(pushedExpands);
			chains = List.copyOf(chains);
			narrowedChains = Map.copyOf(narrowedChains);
		}

		/**
		 * The same read with every pushed {@code $expand} option withdrawn: the expands
		 * become plain, their chains are resolved in memory, and the report says so.
		 */
		public Page withoutExpandOptions() {
			query.getExpand().forEach(expand -> {
				expand.setFilter(null);
				expand.getOrderBy().clear();
				expand.setTop(0);
				expand.setSkip(0);
			});
			List<List<EReference>> all = new ArrayList<>(chains);
			all.addAll(narrowedChains.values());
			Map<String, ExpandPushdown> pushed = new LinkedHashMap<>(pushedExpands);
			narrowedChains.keySet().forEach(path -> pushed.put(path, ExpandPushdown.NONE));
			return new Page(query, pushed, all, Map.of());
		}
	}

	/** {@code $count} is total-before-paging: a countOnly query with the predicate, no order, no page. */
	public static Query count(EntityQuery query) {
		EClass entityType = query.entityType();
		QueryBuilder builder = QueryBuilder.from(entityType).countOnly();
		Expression predicate = ReadQueries.predicate(query.filter(), entityType, query.castType());
		if (predicate != null) {
			builder.where(predicate);
		}
		return builder.build();
	}

	/**
	 * Plans one page for the given predicate. {@code paged} applies the query's ordering and
	 * paging (a delta re-query stays complete — its bound is the journal window, not a page
	 * cap); {@code maxPageSize} caps an unbounded page ({@code <= 0}: no cap).
	 *
	 * @param supports the backend's declared query capabilities — decides which
	 *        {@code $expand} options are pushed down
	 */
	public static Page page(EntityQuery query, Expression predicate, boolean paged, int maxPageSize,
			Predicate<QueryFeature> supports) {
		EClass entityType = query.entityType();
		QueryBuilder builder = QueryBuilder.from(entityType);
		if (predicate != null) {
			builder.where(predicate);
		}
		if (paged) {
			ReadQueries.applyOrderBy(builder, query.orderBy(), entityType, query.castType());
			if (query.skip() > 0) {
				builder.skip(query.skip());
			}
			if (query.top() > 0) {
				builder.top(query.top());
			} else if (maxPageSize > 0) {
				builder.top(maxPageSize);
			}
		}
		EClass context = query.castType() != null ? query.castType() : entityType;
		List<List<EReference>> chains = new ArrayList<>();
		Map<String, List<EReference>> narrowedChains = new LinkedHashMap<>();
		Map<String, ExpandPushdown> pushedExpands = new LinkedHashMap<>();
		boolean pushExpand = supports.test(QueryFeature.EXPAND);
		boolean pushFilters = pushExpand && supports.test(QueryFeature.EXPAND_FILTER);
		boolean pushPaging = pushExpand && supports.test(QueryFeature.EXPAND_PAGE);
		for (ExpandSpec spec : query.expand()) {
			List<EReference> chain = ReadQueries.referenceChain(context, spec.path());
			if (chain.isEmpty()) {
				continue;
			}
			ExpandPushdown pushed = ExpandPushdown.NONE;
			if (pushExpand) {
				pushed = expand(builder, spec, chain, pushFilters, pushPaging);
			}
			pushedExpands.put(spec.path(), pushed);
			if (pushed.isNone()) {
				chains.add(chain);
			} else {
				narrowedChains.put(spec.path(), chain);
			}
		}
		return new Page(builder.build(), pushedExpands, chains, narrowedChains);
	}

	/**
	 * Validates the plan against the backend and returns the plan to execute: the original
	 * when it passes; the plan {@linkplain Page#withoutExpandOptions() without expand options}
	 * when only those were refused (a declared expand capability is not a promise per query,
	 * #64); otherwise the refusal propagates.
	 *
	 * @param validator answers the backend's diagnostic for a query, or null when the backend
	 *        does not validate up front
	 */
	public static Page validated(Page page, Function<Query, Diagnostic> validator) {
		try {
			raise(validator.apply(page.query()));
			return page;
		} catch (UnsupportedOperationException refused) {
			if (page.narrowedChains().isEmpty()) {
				throw refused;
			}
			Page fallback = page.withoutExpandOptions();
			raise(validator.apply(fallback.query()));
			return fallback;
		}
	}

	/**
	 * Turns a validation diagnostic into the SPI's error classes: unsupported features →
	 * {@link UnsupportedOperationException} (501), structural violations →
	 * {@link IllegalArgumentException} (400). Nothing happens below {@link Diagnostic#ERROR}
	 * or for a null diagnostic.
	 */
	public static void raise(Diagnostic diagnostic) {
		if (diagnostic == null || diagnostic.getSeverity() < Diagnostic.ERROR) {
			return;
		}
		List<String> unsupported = new ArrayList<>();
		List<String> invalid = new ArrayList<>();
		for (Diagnostic child : diagnostic.getChildren()) {
			if (child.getSeverity() < Diagnostic.ERROR) {
				continue;
			}
			if (child.getCode() == QueryValidator.CODE_UNSUPPORTED_FEATURE) {
				unsupported.add(child.getMessage());
			} else {
				invalid.add(child.getMessage());
			}
		}
		if (!unsupported.isEmpty()) {
			throw new UnsupportedOperationException(String.join("; ", unsupported));
		}
		if (invalid.isEmpty()) { // an ERROR without children: the root message is all there is
			invalid.add(String.valueOf(diagnostic.getMessage()));
		}
		throw new IllegalArgumentException(String.join("; ", invalid));
	}

	/**
	 * Builds one {@code Expand} and reports what of the ask survived (ADR-0008).
	 *
	 * <p>The one composition rule that is not symmetric: paging is pushed only when the
	 * filter is pushed too (or there is none). Filter down there and page up here is sound —
	 * the resolved entries already ARE the match set and the store order is untouched. The
	 * other way round would page first and filter an already truncated set.
	 *
	 * <p>{@code $top=0} is never pushed: {@code Expand.top} spells "unlimited" as 0, so the
	 * empty page has no representation down there. The in-memory pass serves it exactly.
	 */
	private static ExpandPushdown expand(QueryBuilder builder, ExpandSpec spec, List<EReference> chain,
			boolean pushFilters, boolean pushPaging) {
		if (spec.isPlain()) {
			builder.expand(chain.toArray(EReference[]::new));
			return ExpandPushdown.NONE;
		}
		boolean filter = spec.filter() != null && pushFilters;
		boolean paging = spec.pages() && spec.top() != 0 && pushPaging
				&& (spec.filter() == null || filter);
		if (!filter && !paging) {
			builder.expand(chain.toArray(EReference[]::new));
			return ExpandPushdown.NONE;
		}
		EClass target = chain.get(chain.size() - 1).getEReferenceType();
		Expand expand = QueryFactory.eINSTANCE.createExpand();
		expand.setPath(Expressions.propertyPath(chain.toArray(EStructuralFeature[]::new)));
		if (filter) {
			expand.setFilter(ReadQueries.predicate(spec.filter(), target, null));
		}
		if (paging) {
			expand.getOrderBy().addAll(ReadQueries.orderByList(spec.orderBy(), target, null));
			expand.setSkip(spec.skip());
			expand.setTop(spec.top() < 0 ? 0 : spec.top());
		}
		builder.expand(expand);
		return new ExpandPushdown(filter, paging);
	}

	/**
	 * The SPI promises plain readable results — walks every chain and swaps proxies for
	 * their resolved targets (keyed find through the resource set, deduplicated per proxy
	 * URI).
	 */
	public static void materialize(List<EObject> entities, List<List<EReference>> chains,
			ResourceSet resourceSet) {
		if (chains.isEmpty() || entities.isEmpty()) {
			return;
		}
		Map<String, EObject> resolved = new HashMap<>();
		for (List<EReference> chain : chains) {
			for (EObject entity : entities) {
				descend(entity, chain, 0, resourceSet, resolved);
			}
		}
	}

	private static void descend(EObject object, List<EReference> chain, int index,
			ResourceSet resourceSet, Map<String, EObject> resolved) {
		if (object == null || index >= chain.size()) {
			return;
		}
		EReference reference = chain.get(index);
		if (!reference.getEContainingClass().isInstance(object)) {
			return; // polymorphic page: this row does not carry the navigation
		}
		if (reference.isMany()) {
			@SuppressWarnings("unchecked")
			List<EObject> members = (List<EObject>) object.eGet(reference);
			for (ListIterator<EObject> iterator = members.listIterator(); iterator.hasNext();) {
				EObject member = iterator.next();
				EObject target = resolve(member, resourceSet, resolved);
				if (target != member) {
					iterator.set(target);
				}
				descend(target, chain, index + 1, resourceSet, resolved);
			}
		} else if (object.eGet(reference, false) instanceof EObject member) {
			EObject target = resolve(member, resourceSet, resolved);
			if (target != member) {
				object.eSet(reference, target);
			}
			descend(target, chain, index + 1, resourceSet, resolved);
		}
	}

	private static EObject resolve(EObject candidate, ResourceSet resourceSet,
			Map<String, EObject> resolved) {
		if (!candidate.eIsProxy()) {
			return candidate;
		}
		String key = ((InternalEObject) candidate).eProxyURI().toString();
		EObject target = resolved.computeIfAbsent(key,
				proxyUri -> EcoreUtil.resolve(candidate, resourceSet));
		if (target.eIsProxy()) {
			throw new IllegalStateException("the backend returned an unresolvable reference");
		}
		return target;
	}
}
