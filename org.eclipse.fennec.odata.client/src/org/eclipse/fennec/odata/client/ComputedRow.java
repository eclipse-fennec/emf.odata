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
package org.eclipse.fennec.odata.client;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;

import tools.jackson.databind.ObjectMapper;

/**
 * One row of a {@code $compute} read: the entity decoded into a typed {@link EObject} (its own
 * model properties) paired with the dynamic computed members. Computed values are not model
 * features, so they cannot live on the {@code EObject}; {@link #value(String, Class)} coerces them
 * to a requested Java type on demand (e.g. {@code java.math.BigDecimal}, {@code Long}, {@code String}).
 */
public record ComputedRow(EObject entity, Map<String, Object> computed) {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	/** Whether a computed member with this alias is present. */
	public boolean has(String alias) {
		return computed.containsKey(alias);
	}

	/** The raw computed value (Jackson's natural mapping), or {@code null} when absent. */
	public Object value(String alias) {
		return computed.get(alias);
	}

	/**
	 * The computed value coerced to {@code type} (via the same JSON mapper the client decodes with),
	 * or {@code null} when the alias is absent or its value is JSON null.
	 */
	public <T> T value(String alias, Class<T> type) {
		Object raw = computed.get(alias);
		return raw == null ? null : MAPPER.convertValue(raw, type);
	}
}
