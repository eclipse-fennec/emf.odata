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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.query.OrderBySegment;

/**
 * One backend-agnostic entity query (req §3.5): the parsed OCL predicate IR plus paging.
 * Backends MUST push the whole query down (or evaluate it themselves) — partial evaluation
 * with client-side re-filtering is not allowed. All members are read-only for the backend.
 *
 * @param entityType the queried entity EClass (never null)
 * @param filter     boolean-typed OCL predicate, or null for "all"
 * @param orderBy    sort segments, never null (may be empty)
 * @param skip       number of entities to skip (>= 0)
 * @param top        maximum number of entities to return, or -1 for no limit
 * @param count      whether the total match count (before skip/top) must be computed
 */
public record EntityQuery(EClass entityType, OclExpression filter, List<OrderBySegment> orderBy,
		int skip, int top, boolean count) {

	public EntityQuery {
		Objects.requireNonNull(entityType, "entityType must not be null");
		orderBy = orderBy == null ? List.of() : List.copyOf(orderBy);
		if (skip < 0) {
			throw new IllegalArgumentException("skip must be >= 0, was: " + skip);
		}
		if (top < -1) {
			throw new IllegalArgumentException("top must be >= 0 or -1 (unlimited), was: " + top);
		}
	}

	/** Query for all instances of a type, no filter, no paging. */
	public static EntityQuery all(EClass entityType) {
		return new EntityQuery(entityType, null, List.of(), 0, -1, false);
	}
}
