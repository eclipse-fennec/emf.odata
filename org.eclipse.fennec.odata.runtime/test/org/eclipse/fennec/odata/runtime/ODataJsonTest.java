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
package org.eclipse.fennec.odata.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Direct tests for the protocol-level JSON writer and the output sanitizer. */
@DisplayName("ODataJson — row writer + sanitizer")
class ODataJsonTest {

	private static String write(Object value) {
		StringBuilder out = new StringBuilder();
		ODataJson.value(out, value);
		return out.toString();
	}

	@Test
	@DisplayName("rows: nested maps, plain decimals, lists, null")
	void rows() {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put("category", Map.of("name", "Dairy"));
		row.put("Total", new BigDecimal("5.70"));
		row.put("Cnt", 2L);
		row.put("empty", null);
		assertEquals("{\"category\":{\"name\":\"Dairy\"},\"Total\":5.70,\"Cnt\":2,\"empty\":null}",
				write(row));
		assertEquals("[1,true,\"x\"]", write(List.of(1, true, "x")));
	}

	@Test
	@DisplayName("sanitizer: escapes, control characters, truncation")
	void sanitizer() {
		assertEquals("a\\\"b\\\\c", ODataJson.sanitize("a\"b\\c"));
		assertEquals("a b", ODataJson.sanitize("a\nb"), "control chars become spaces");
		assertEquals(500, ODataJson.sanitize("x".repeat(9000)).length(), "truncated");
		assertEquals("", ODataJson.sanitize(null));

		String error = ODataJson.error(500, "boom\nat org.eclipse.Secret.method()");
		assertTrue(error.startsWith("{\"error\""), error);
		assertFalse(error.contains("\n"), "no raw control characters in the document");
	}
}
