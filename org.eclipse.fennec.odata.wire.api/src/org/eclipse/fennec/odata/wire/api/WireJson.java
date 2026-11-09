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

/**
 * A JSON object or array as a wire value (#118): what a {@link WirePropertyHandler} writes for a
 * JSON-valued property, and what it reads when the payload carries an object or an array. The
 * runtime embeds it as JSON, never as a string.
 *
 * @param json the JSON text of an object or an array
 */
public record WireJson(String json) {

	public WireJson {
		if (json == null) {
			throw new IllegalArgumentException("no JSON text");
		}
	}
}
