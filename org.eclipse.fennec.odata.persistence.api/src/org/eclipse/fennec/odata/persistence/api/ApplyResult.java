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

/**
 * Result of an {@link ApplyQuery}.
 *
 * @param rows       the result page (grouping paths as nested maps, aliases as keys)
 * @param totalCount total row count before paging, or -1 when not requested
 */
public record ApplyResult(List<Map<String, Object>> rows, long totalCount) {

	public ApplyResult {
		rows = rows == null ? List.of() : List.copyOf(rows);
	}
}
