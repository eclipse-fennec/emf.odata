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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("WireProfile defaults")
class WireProfileTest {

	private final WireProfile profile = new WireProfile() {
	};

	@Test
	@DisplayName("the default key literal is the OData one")
	void keyLiteral() {
		assertEquals("42", profile.keyLiteral(Map.of("id", 42)));
		assertEquals("'it''s'", profile.keyLiteral(Map.of("id", "it's")));
		UUID guid = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef");
		assertEquals(guid.toString(), profile.keyLiteral(Map.of("id", guid)),
				"a GUID is an unquoted literal, like the servlet's own key predicates");
		Map<String, Object> composite = new LinkedHashMap<>();
		composite.put("a", 1);
		composite.put("b", "x");
		assertEquals("a=1,b='x'", profile.keyLiteral(composite));
	}

	@Test
	@DisplayName("without overrides a profile is plain OData")
	void defaults() {
		assertEquals(ControlNames.ODATA, profile.controls());
		assertTrue(profile.keyInBody(null));
		assertFalse(profile.absoluteLinks(), "OData keeps its links relative (#121 D8)");
		assertTrue(profile.serviceDocument(new ServiceRoot("http://h/r", List.of(), List.of())).isEmpty());
		assertTrue(profile.errorBody(400, "x").isEmpty());
		assertTrue(profile.mapping().isEmpty(), "no wire properties: the model as it is (#119)");
		assertTrue(profile.handler(null).isEmpty());
	}

	@Test
	@DisplayName("a root without a target binds the plain OData profile")
	void defaultTarget() {
		assertEquals("(odata.wire.profile=odata)", WireProfile.DEFAULT_TARGET);
	}
}
