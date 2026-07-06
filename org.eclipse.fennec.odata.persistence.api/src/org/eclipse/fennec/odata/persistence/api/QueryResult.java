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

import org.eclipse.emf.ecore.EObject;

/**
 * Result of an {@link EntityQuery}.
 *
 * @param entities   the matching page (after filter/order/skip/top), never null, read-only
 * @param totalCount total match count before paging, or -1 when the query did not request it
 */
public record QueryResult(List<EObject> entities, long totalCount) {

	public QueryResult {
		entities = entities == null ? List.of() : List.copyOf(entities);
	}
}
