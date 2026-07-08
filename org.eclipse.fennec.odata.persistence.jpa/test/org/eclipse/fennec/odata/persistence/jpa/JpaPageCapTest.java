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
package org.eclipse.fennec.odata.persistence.jpa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Server-driven paging safety net: a request without a client {@code $top} must not materialize an
 * entire (potentially huge) table into the heap — the configured {@code odata.jpa.max.page.size}
 * caps the fetch. Regression guard for the unbounded-{@code getResultList()} finding.
 */
@DisplayName("JpaQueryService: default page cap for unbounded reads")
class JpaPageCapTest extends JpaWebshopTestBase {

	@Test
	@DisplayName("unbounded $top (-1) is capped at the configured page size")
	void unboundedTopIsCapped() {
		// the fixture has 5 products; cap well below that so the effect is observable
		service.activate(Map.of("odata.jpa.max.page.size", "2"));

		QueryResult capped = query(null, "name asc", 0, -1, false);
		assertEquals(2, capped.entities().size(),
				"an unbounded $top must be limited to the server page size: " + names(capped));

		// an explicit client $top is still honored exactly (already bounded upstream)
		QueryResult explicit = query(null, "name asc", 0, 3, false);
		assertEquals(3, explicit.entities().size(), "an explicit $top wins over the cap");
	}

	@Test
	@DisplayName("cap disabled (<= 0) restores the unbounded behaviour")
	void capDisabled() {
		service.activate(Map.of("odata.jpa.max.page.size", "0"));
		QueryResult all = query(null, "name asc", 0, -1, false);
		assertTrue(all.entities().size() >= 5,
				"with the cap disabled every row is returned: " + names(all));
	}
}
