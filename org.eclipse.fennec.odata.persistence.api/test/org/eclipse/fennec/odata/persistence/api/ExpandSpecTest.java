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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The expand ask and the backend's answer (ADR-0008)")
class ExpandSpecTest {

	@Test
	@DisplayName("a plain spec asks for nothing beyond resolving the path")
	void plain() {
		ExpandSpec spec = ExpandSpec.of("orders");

		assertTrue(spec.isPlain());
		assertFalse(spec.pages());
		assertEquals(List.of(), spec.orderBy());
		assertEquals(-1, spec.top());
	}

	@Test
	@DisplayName("skip alone is paging too — a window with an open upper end is still a window")
	void skipAlonePages() {
		assertTrue(new ExpandSpec("orders", null, List.of(), 2, -1).pages());
		assertFalse(new ExpandSpec("orders", null, List.of(), 0, -1).pages());
		assertTrue(new ExpandSpec("orders", null, List.of(), 0, 0).pages());
	}

	@Test
	@DisplayName("the invariants are checked where they are cheap to check")
	void invariants() {
		assertThrows(NullPointerException.class, () -> ExpandSpec.of(null));
		assertThrows(IllegalArgumentException.class, () -> ExpandSpec.of("  "));
		assertThrows(IllegalArgumentException.class,
				() -> new ExpandSpec("orders", null, List.of(), -1, -1));
		assertThrows(IllegalArgumentException.class,
				() -> new ExpandSpec("orders", null, List.of(), 0, -2));
	}

	@Test
	@DisplayName("an unanswered path reads as 'nothing pushed', never as null")
	void pushdownDefaults() {
		QueryResult result = new QueryResult(List.of(), -1,
				Map.of("orders", new ExpandPushdown(true, false)));

		assertTrue(result.pushedFor("orders").filter());
		assertFalse(result.pushedFor("orders").paging());
		assertSame(ExpandPushdown.NONE, result.pushedFor("items"));
		assertTrue(ExpandPushdown.NONE.isNone());
		assertFalse(new ExpandPushdown(false, true).isNone());
	}

	@Test
	@DisplayName("the path-only constructor still works and yields plain specs")
	void pathsStayUsable() {
		EClass type = EcoreFactory.eINSTANCE.createEClass();
		type.setName("Customer");

		EntityQuery query = new EntityQuery(type, null, null, List.of(), 0, -1, false,
				Set.of("orders"));

		assertEquals(List.of("orders"), query.expandPaths());
		assertTrue(query.expand().get(0).isPlain());
		assertEquals(List.of(), new EntityQuery(type, null, null, List.of(), 0, -1, false)
				.expandPaths());
	}
}
