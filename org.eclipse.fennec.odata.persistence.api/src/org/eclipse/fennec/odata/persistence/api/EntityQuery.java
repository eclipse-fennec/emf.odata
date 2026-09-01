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
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.query.OrderBySegment;

/**
 * One backend-agnostic entity query (req §3.5): the parsed OCL predicate IR plus paging.
 * Backends MUST push the whole query down (or evaluate it themselves) — partial evaluation
 * with client-side re-filtering is not allowed. All members are read-only for the backend.
 *
 * @param entityType the queried entity EClass — the SET's declared type, used for backend
 *                   routing ({@code supports}); never null
 * @param castType   derived-type restriction from a URL cast segment ([OData-URL] 4.11):
 *                   only instances of this EClass (or its subtypes) match; null = no cast.
 *                   Pushdown backends map this onto their discriminator mechanism
 *                   (JPA: {@code TYPE(e) IN ...}).
 * @param filter     boolean-typed OCL predicate, or null for "all"
 * @param orderBy    sort segments, never null (may be empty)
 * @param skip       number of entities to skip (>= 0)
 * @param top        maximum number of entities to return, or -1 for no limit
 * @param count      whether the total match count (before skip/top) must be computed
 * @param expand     navigation names or slash-separated navigation PATHS the caller will
 *                   read on the results ({@code $expand}, or the walked prefix of a resource
 *                   path like {@code category/parent}): backends MUST prefetch/materialize
 *                   them efficiently — accessing them afterwards must neither lazy-load per
 *                   entity (N+1) nor yield unresolved proxies; never null (may be empty)
 */
public record EntityQuery(EClass entityType, EClass castType, OclExpression filter,
		List<OrderBySegment> orderBy, int skip, int top, boolean count, List<ExpandSpec> expand) {

	public EntityQuery {
		Objects.requireNonNull(entityType, "entityType must not be null");
		orderBy = orderBy == null ? List.of() : List.copyOf(orderBy);
		expand = expand == null ? List.of() : List.copyOf(expand);
		if (skip < 0) {
			throw new IllegalArgumentException("skip must be >= 0, was: " + skip);
		}
		if (top < -1) {
			throw new IllegalArgumentException("top must be >= 0 or -1 (unlimited), was: " + top);
		}
	}

	/** Query without expanded navigations. */
	public EntityQuery(EClass entityType, EClass castType, OclExpression filter,
			List<OrderBySegment> orderBy, int skip, int top, boolean count) {
		this(entityType, castType, filter, orderBy, skip, top, count, List.of());
	}

	/** Query with plain expansions — paths only, no nested options (ADR-0008). */
	public EntityQuery(EClass entityType, EClass castType, OclExpression filter,
			List<OrderBySegment> orderBy, int skip, int top, boolean count, Set<String> expand) {
		this(entityType, castType, filter, orderBy, skip, top, count,
				expand == null ? List.<ExpandSpec>of()
						: expand.stream().map(ExpandSpec::of).toList());
	}

	/** The expanded paths, in request order — for callers that need the shape, not the options. */
	public List<String> expandPaths() {
		return expand.stream().map(ExpandSpec::path).toList();
	}

	/** Query without a derived-type cast (the common case). */
	public EntityQuery(EClass entityType, OclExpression filter, List<OrderBySegment> orderBy,
			int skip, int top, boolean count) {
		this(entityType, null, filter, orderBy, skip, top, count, Set.of());
	}

	/** Query for all instances of a type, no filter, no paging. */
	public static EntityQuery all(EClass entityType) {
		return new EntityQuery(entityType, null, List.of(), 0, -1, false);
	}
}
