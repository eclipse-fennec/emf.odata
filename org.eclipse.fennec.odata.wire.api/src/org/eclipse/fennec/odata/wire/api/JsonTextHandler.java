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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

/**
 * The handler of a property whose single string attribute holds JSON text (#118), e.g. the
 * SensorThings {@code properties}, {@code parameters}, {@code location} and {@code feature}: on
 * the wire it is the JSON object or array itself, not a string. It is neither filterable nor
 * sortable (501 / 400); paths into the JSON are not addressable.
 */
public final class JsonTextHandler implements WirePropertyHandler {

	private final EAttribute text;

	/** @param text the string attribute holding the JSON text, the only part of the property */
	public JsonTextHandler(EAttribute text) {
		this.text = text;
	}

	@Override
	public Object write(EObject entity) {
		Object value = entity.eGet(text);
		return value == null ? null : new WireJson(value.toString());
	}

	@Override
	public Map<EAttribute, Object> read(Object value) {
		if (value != null && !(value instanceof WireJson)) {
			throw new IllegalArgumentException("'" + text.getName() + "' takes a JSON object or array");
		}
		Map<EAttribute, Object> parts = new HashMap<>();
		parts.put(text, value == null ? null : ((WireJson) value).json());
		return parts;
	}

	@Override
	public WireCondition compare(WireOperator operator, OperandKind operand) {
		throw new UnsupportedOperationException("'" + text.getName() + "' is a JSON value and cannot be filtered");
	}

	@Override
	public EAttribute operandOf(String function) {
		return null;
	}

	@Override
	public List<EAttribute> orderBy() {
		return List.of();
	}
}
