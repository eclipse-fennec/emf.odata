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
package org.eclipse.fennec.odata.wire.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WireProfile defaults")
class WireProfileTest {

	private final WireProfile profile = () -> ControlNames.ODATA;

	@Test
	@DisplayName("the default key literal is the OData one")
	void keyLiteral() {
		assertEquals("42", profile.keyLiteral(Map.of("id", 42)));
		assertEquals("'it''s'", profile.keyLiteral(Map.of("id", "it's")));
		Map<String, Object> composite = new LinkedHashMap<>();
		composite.put("a", 1);
		composite.put("b", "x");
		assertEquals("a=1,b='x'", profile.keyLiteral(composite));
	}

	@Test
	@DisplayName("without overrides a profile changes nothing but the names")
	void defaults() {
		assertTrue(profile.keyInBody(null));
		assertEquals(null, profile.keyQueryName(null));
		assertTrue(profile.serviceDocument(new ServiceRoot("http://h/r", List.of(), List.of())).isEmpty());
		assertTrue(profile.errorBody(400, "x").isEmpty());
	}
}
