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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;

/**
 * {@link ODataQueryParser} with the §3.6.1 ad-hoc query cache: parsed {@code $filter}/
 * {@code $orderby} results are kept in one {@link ODataQueryLruCache} per context
 * {@link EClass} (weakly keyed, so unregistered models can be collected).
 *
 * <p><b>Sharing contract</b> (same as the m2x expression cache): cache hits return the SAME
 * AST instance — consumers must treat parsed expressions as read-only and must NOT re-parent
 * them into another containment tree; copy first ({@code EcoreUtil.copy}) when a mutable
 * instance is needed.
 *
 * <p>This is the standalone core (ADR-0004). The {@code ODataAspectProvider} adapter later
 * exposes these caches on the metadata profile (aspect slot {@code parsedQueryCache}) so the
 * whiteboard lifecycle invalidates them on package unregistration; until then
 * {@link #invalidate(EClass)} / {@link #invalidateAll()} are manual hooks.
 */
public class CachingODataQueryParser extends ODataQueryParser {

	private static final String FILTER_PREFIX = "$filter=";
	private static final String ORDERBY_PREFIX = "$orderby=";
	private static final String APPLY_PREFIX = "$apply=";

	private final int cacheSize;
	/**
	 * NOTE on lifecycle: the weak keying alone cannot free entries, because cached ASTs
	 * reference their EClass via {@code referredProperty} (value → key). Bounded growth comes
	 * from the per-class LRU capacity; FREEING a class's memory requires
	 * {@link #invalidate(EClass)} — which the ODataAspectProvider adapter calls on package
	 * unregistration (ADR-0004 phase 2).
	 */
	private final Map<EClass, ODataQueryLruCache> caches =
			Collections.synchronizedMap(new WeakHashMap<>());

	public CachingODataQueryParser() {
		this(ODataQueryLruCache.DEFAULT_MAX_SIZE);
	}

	public CachingODataQueryParser(int cacheSizePerClass) {
		this.cacheSize = cacheSizePerClass;
	}

	@Override
	public OclExpression parseFilter(String filter, EClass context) {
		ODataQueryLruCache cache = cache(context);
		String key = FILTER_PREFIX + filter;
		OclExpression cached = (OclExpression) cache.get(key);
		if (cached != null) {
			return cached;
		}
		OclExpression parsed = super.parseFilter(filter, context);
		cache.put(key, parsed);
		return parsed;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<OrderBySegment> parseOrderBy(String orderBy, EClass context) {
		ODataQueryLruCache cache = cache(context);
		String key = ORDERBY_PREFIX + orderBy;
		List<OrderBySegment> cached = (List<OrderBySegment>) cache.get(key);
		if (cached != null) {
			return cached;
		}
		List<OrderBySegment> parsed = List.copyOf(super.parseOrderBy(orderBy, context));
		cache.put(key, parsed);
		return parsed;
	}

	@Override
	public ApplyPipeline parseApply(String apply, EClass context) {
		ODataQueryLruCache cache = cache(context);
		String key = APPLY_PREFIX + apply;
		ApplyPipeline cached = (ApplyPipeline) cache.get(key);
		if (cached != null) {
			return cached;
		}
		ApplyPipeline parsed = super.parseApply(apply, context);
		cache.put(key, parsed);
		return parsed;
	}

	/** The cache of one context EClass (for statistics and targeted invalidation). */
	public ODataQueryLruCache cache(EClass context) {
		return caches.computeIfAbsent(context, c -> new ODataQueryLruCache(cacheSize));
	}

	/** Drops the cache of one context EClass (e.g. when its package is unregistered). */
	public void invalidate(EClass context) {
		caches.remove(context);
	}

	public void invalidateAll() {
		caches.clear();
	}
}
