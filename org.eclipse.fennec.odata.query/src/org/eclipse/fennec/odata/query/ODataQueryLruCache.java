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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * LRU cache for parsed ad-hoc query expressions (req §3.6.1), modelled after the m2x
 * {@code OclLruExpressionCache}: access-ordered {@link LinkedHashMap}, synchronized,
 * hit/miss statistics. One instance caches the queries of ONE context EClass — the
 * per-package lifecycle (invalidation on unregister) is the {@code ODataAspectProvider}
 * adapter's job once it attaches these caches to the metadata profile.
 *
 * <p>Keys are the raw query strings (Q20: canonicalization is a phase-2 option).
 */
public final class ODataQueryLruCache {

	/** Default per-EClass capacity (req §3.6.1). */
	public static final int DEFAULT_MAX_SIZE = 1024;

	private final int maxSize;
	private final Map<String, Object> cache;
	private final AtomicLong hits = new AtomicLong();
	private final AtomicLong misses = new AtomicLong();

	public ODataQueryLruCache() {
		this(DEFAULT_MAX_SIZE);
	}

	public ODataQueryLruCache(int maxSize) {
		if (maxSize < 1) {
			throw new IllegalArgumentException("maxSize must be >= 1, was: " + maxSize);
		}
		this.maxSize = maxSize;
		this.cache = new LinkedHashMap<>(16, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
				return size() > ODataQueryLruCache.this.maxSize;
			}
		};
	}

	Object get(String key) {
		Object result;
		synchronized (cache) {
			result = cache.get(key);
		}
		if (result != null) {
			hits.incrementAndGet();
		} else {
			misses.incrementAndGet();
		}
		return result;
	}

	void put(String key, Object value) {
		Objects.requireNonNull(value, "value must not be null");
		synchronized (cache) {
			cache.put(key, value);
		}
	}

	public void invalidateAll() {
		synchronized (cache) {
			cache.clear();
		}
	}

	public int size() {
		synchronized (cache) {
			return cache.size();
		}
	}

	public long hits() {
		return hits.get();
	}

	public long misses() {
		return misses.get();
	}
}
