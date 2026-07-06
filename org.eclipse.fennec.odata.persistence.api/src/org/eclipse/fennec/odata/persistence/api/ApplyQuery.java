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
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;

/**
 * A {@code $apply} query: the aggregation pipeline plus the system query options that apply
 * AFTER it (OASIS aggregation spec: {@code $filter}/{@code $orderby}/{@code $skip}/{@code $top}/
 * {@code $count} operate on the transformed set). Row-level expressions may reference the
 * pipeline's aggregate/compute aliases (as {@code VariableExp}s). Backends execute the whole
 * thing — pushdown-friendly (HAVING/ORDER BY), no protocol-layer re-filtering.
 *
 * @param entityType the queried entity EClass (never null)
 * @param pipeline   the parsed pipeline (never null)
 * @param rowFilter  boolean predicate over result rows, or null
 * @param orderBy    row sort segments, never null (may be empty)
 * @param skip       rows to skip (>= 0)
 * @param top        maximum rows, or -1 for no limit
 * @param count      whether the total row count (before skip/top) must be computed
 */
public record ApplyQuery(EClass entityType, ApplyPipeline pipeline, OclExpression rowFilter,
		List<OrderBySegment> orderBy, int skip, int top, boolean count) {

	public ApplyQuery {
		Objects.requireNonNull(entityType, "entityType must not be null");
		Objects.requireNonNull(pipeline, "pipeline must not be null");
		orderBy = orderBy == null ? List.of() : List.copyOf(orderBy);
		if (skip < 0) {
			throw new IllegalArgumentException("skip must be >= 0, was: " + skip);
		}
		if (top < -1) {
			throw new IllegalArgumentException("top must be >= 0 or -1 (unlimited), was: " + top);
		}
	}
}
