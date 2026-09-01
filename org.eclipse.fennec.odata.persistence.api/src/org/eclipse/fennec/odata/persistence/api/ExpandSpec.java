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
package org.eclipse.fennec.odata.persistence.api;

import java.util.List;
import java.util.Objects;

import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.query.OrderBySegment;

/**
 * One expanded navigation and the options the backend is ASKED to apply to it (ADR-0008).
 *
 * <p>Asked, not told: whether a backend applies them depends on what it declares
 * ({@code EXPAND_FILTER} / {@code EXPAND_PAGE}), and the answer comes back in
 * {@link QueryResult#pushedExpands()}. Whatever is not applied there stays with the caller's
 * in-memory pass — per navigation, and per option within it. {@code $count} is deliberately
 * absent: it is never pushed (upstream D2), it is DERIVED from a pushed filter when nothing
 * was paged, and computed in memory otherwise.
 *
 * @param path    the expand path, root feature first, slash-separated (never null or blank)
 * @param filter  item predicate, or null
 * @param orderBy sort segments, never null (may be empty) — selector input for {@code top}/
 *                {@code skip} only, never pushed on its own (upstream D3)
 * @param skip    items to skip per parent (0 = none)
 * @param top     maximum items per parent, or -1 for unlimited
 */
public record ExpandSpec(String path, OclExpression filter, List<OrderBySegment> orderBy,
		int skip, int top) {

	public ExpandSpec {
		Objects.requireNonNull(path, "path must not be null");
		if (path.isBlank()) {
			throw new IllegalArgumentException("path must not be blank");
		}
		orderBy = orderBy == null ? List.of() : List.copyOf(orderBy);
		if (skip < 0) {
			throw new IllegalArgumentException("skip must be >= 0, was: " + skip);
		}
		if (top < -1) {
			throw new IllegalArgumentException("top must be >= 0 or -1 (unlimited), was: " + top);
		}
	}

	/** A plain expansion: resolve the path, apply nothing. */
	public static ExpandSpec of(String path) {
		return new ExpandSpec(path, null, List.of(), 0, -1);
	}

	/** Whether this asks for per-parent paging at all. */
	public boolean pages() {
		return skip > 0 || top >= 0;
	}

	/** Whether the backend is asked for anything beyond resolving the path. */
	public boolean isPlain() {
		return filter == null && !pages();
	}
}
