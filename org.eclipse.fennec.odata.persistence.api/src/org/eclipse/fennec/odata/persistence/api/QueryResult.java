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
import java.util.Map;

import org.eclipse.emf.ecore.EObject;

/**
 * Result of an {@link EntityQuery}.
 *
 * @param entities      the matching page (after filter/order/skip/top), never null, read-only
 * @param totalCount    total match count before paging, or -1 when the query did not request it
 * @param pushedExpands what the backend applied per expanded path (ADR-0008), never null;
 *                      a path that is absent was expanded plainly, so its options — if any —
 *                      are still the caller's to apply
 */
public record QueryResult(List<EObject> entities, long totalCount,
		Map<String, ExpandPushdown> pushedExpands) {

	public QueryResult {
		entities = entities == null ? List.of() : List.copyOf(entities);
		pushedExpands = pushedExpands == null ? Map.of() : Map.copyOf(pushedExpands);
	}

	/** Result of a query that pushed no expand options. */
	public QueryResult(List<EObject> entities, long totalCount) {
		this(entities, totalCount, Map.of());
	}

	/** What the backend applied to {@code path} — never null. */
	public ExpandPushdown pushedFor(String path) {
		return pushedExpands.getOrDefault(path, ExpandPushdown.NONE);
	}
}
