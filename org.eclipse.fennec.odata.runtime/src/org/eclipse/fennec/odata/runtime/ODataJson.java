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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON writing for protocol-level payloads ($apply rows, error documents) plus the
 * output sanitizer: everything written into hand-assembled JSON goes through {@link #sanitize},
 * which JSON-escapes, strips control characters and truncates — no stack traces, exception
 * class names or unprintable bytes ever leave the server. Entity payloads use the E3 codec,
 * not this class.
 */
public final class ODataJson {

	private static final int MAX_MESSAGE_LENGTH = 500;

	/** Appends any $apply row value (maps/lists/numbers/booleans/text) as JSON. */
	public static void value(StringBuilder out, Object value) {
		value(out, value, false);
	}

	/**
	 * {@link #value(StringBuilder, Object)} with {@code IEEE754Compatible=true} support
	 * ([OData-JSON] 8.1): 64-bit integers and decimals travel as strings so their exact value
	 * survives IEEE 754 clients.
	 */
	public static void value(StringBuilder out, Object value, boolean ieee754Compatible) {
		switch (value) {
			case null -> out.append("null");
			case Map<?, ?> map -> {
				out.append('{');
				boolean first = true;
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					if (!first) {
						out.append(',');
					}
					first = false;
					out.append('"').append(sanitize(String.valueOf(entry.getKey()))).append("\":");
					value(out, entry.getValue(), ieee754Compatible);
				}
				out.append('}');
			}
			case List<?> list -> {
				out.append('[');
				for (int i = 0; i < list.size(); i++) {
					if (i > 0) {
						out.append(',');
					}
					value(out, list.get(i), ieee754Compatible);
				}
				out.append(']');
			}
			case Number number -> {
				String lexical = number instanceof BigDecimal decimal
						? decimal.toPlainString() : number.toString();
				if (ieee754Compatible && (number instanceof Long || number instanceof BigDecimal
						|| number instanceof BigInteger)) {
					out.append('"').append(lexical).append('"');
				} else {
					out.append(lexical);
				}
			}
			case Boolean bool -> out.append(bool);
			default -> out.append('"').append(sanitize(String.valueOf(value))).append('"');
		}
	}

	/** The OData error document for hand-written error responses. */
	public static String error(int status, String message) {
		return "{\"error\":{\"code\":\"" + status + "\",\"message\":\"" + sanitize(message) + "\"}}";
	}

	/** JSON-escapes, strips control characters and truncates arbitrary text for output. */
	public static String sanitize(String message) {
		if (message == null) {
			return "";
		}
		String text = message.length() > MAX_MESSAGE_LENGTH
				? message.substring(0, MAX_MESSAGE_LENGTH) : message;
		StringBuilder safe = new StringBuilder(text.length());
		for (char c : text.toCharArray()) {
			if (c == '"' || c == '\\') {
				safe.append('\\').append(c);
			} else if (c >= 0x20 && c != 0x7F) {
				safe.append(c);
			} else {
				safe.append(' ');
			}
		}
		return safe.toString();
	}

	private ODataJson() {
	}
}
