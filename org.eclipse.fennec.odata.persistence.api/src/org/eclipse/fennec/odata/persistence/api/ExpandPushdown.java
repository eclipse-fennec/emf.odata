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

/**
 * What a backend actually applied to one expanded navigation (ADR-0008) — the answer to the
 * ask in {@link ExpandSpec}.
 *
 * <p>The caller cannot guess this and must not: with {@code filter} the resolved entries ARE
 * the match set, so the payload is chosen by {@code eIsProxy()} and an in-memory filter would
 * run twice; without it the entries are the whole collection and the filter still has to run.
 * Guessing either way is wrong.
 *
 * @param filter whether the item predicate was applied by the backend
 * @param paging whether per-parent {@code skip}/{@code top} was applied by the backend
 */
public record ExpandPushdown(boolean filter, boolean paging) {

	public static final ExpandPushdown NONE = new ExpandPushdown(false, false);

	/** Nothing was pushed: the caller owns the whole option set for this navigation. */
	public boolean isNone() {
		return !filter && !paging;
	}
}
