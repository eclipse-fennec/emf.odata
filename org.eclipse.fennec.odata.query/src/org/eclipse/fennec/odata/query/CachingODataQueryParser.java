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
import java.util.LinkedHashMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;

/**
 * {@link ODataQueryParser} with the §3.6.1 ad-hoc query cache: parsed {@code $filter}/
 * {@code $orderby} results are kept in one {@link ODataQueryLruCache} per context
 * {@link EClass}, and the number of per-class caches is itself LRU-bounded so a flood of
 * transient models cannot grow the map without limit.
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

	/** Backstop cap on the NUMBER of per-EClass caches (bounds total memory: classes × cacheSize). */
	private static final int MAX_CACHED_CLASSES = 256;

	private final int cacheSize;
	/**
	 * NOTE on lifecycle: a weak key alone cannot free entries, because a cached AST references its
	 * EClass via {@code referredProperty} (value → key). Growth is bounded two ways: the per-class
	 * LRU capacity ({@code cacheSize}) AND this access-ordered LRU over the CLASSES themselves
	 * (evicting the least-recently-used class's whole cache past {@link #MAX_CACHED_CLASSES}), so a
	 * flood of transient EClasses cannot grow the map without limit. {@link #invalidate(EClass)}
	 * still lets the ODataAspectProvider adapter free a class PROACTIVELY on package unregistration
	 * (ADR-0004 phase 2, the complementary proactive path).
	 */
	private final Map<EClass, ODataQueryLruCache> caches = Collections.synchronizedMap(
			new LinkedHashMap<>(16, 0.75f, true) {
				private static final long serialVersionUID = 1L;

				@Override
				protected boolean removeEldestEntry(Map.Entry<EClass, ODataQueryLruCache> eldest) {
					return size() > MAX_CACHED_CLASSES;
				}
			});

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
