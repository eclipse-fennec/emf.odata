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

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.wire.mapping.WireProperty;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * What a wire property means (#119): its JSON form and how it filters and sorts. The profile
 * gives one per {@link WireProperty} of its
 * mapping ({@link WireProfile#handler}); the runtime does the rest — the wire name replaces the
 * parts everywhere, the parts are not addressable, every query stays on the parts and is pushed
 * down. The semantics of a dialect (SensorThings intervals, typed results) live in the
 * handler, not in the OData runtime.
 *
 * <p>A handler signals a request it cannot answer with {@link IllegalArgumentException} (400)
 * or {@link UnsupportedOperationException} (501); the message goes to the client.
 */
@ConsumerType
public interface WirePropertyHandler {

	/**
	 * The value on the wire: a {@link String}, {@link Number}, {@link Boolean} or {@code null}.
	 *
	 * @param entity the entity whose parts to read
	 */
	Object write(EObject entity);

	/**
	 * The parts for a value from the wire (POST, PUT, PATCH, a property write). Every part the
	 * map leaves out keeps its value; only parts of the property may appear.
	 *
	 * @param value a {@link String}, {@link Number}, {@link Boolean} or {@code null}; a JSON
	 *              object or array arrives as its JSON text
	 * @throws IllegalArgumentException when the value is not a valid form (400)
	 */
	Map<EAttribute, Object> read(Object value);

	/**
	 * The condition for {@code property operator operand}. The runtime puts the property on the
	 * left ({@link WireOperator#converse()}) and expands {@code in} to {@code eq} comparisons.
	 *
	 * @throws IllegalArgumentException      when the operand cannot compare (400)
	 * @throws UnsupportedOperationException when the comparison is not supported (501)
	 */
	WireCondition compare(WireOperator operator, OperandKind operand);

	/**
	 * The part that stands for the property as the operand of a function or an arithmetic
	 * operator: the OData name such as {@code year}, {@code length}, {@code contains},
	 * {@code round}, {@code add}; {@code null} when the property is no valid operand (400).
	 */
	EAttribute operandOf(String function);

	/**
	 * The sort keys of {@code $orderby}, in order; the requested direction applies to each,
	 * nulls first ascending and last descending as in OData.
	 */
	List<EAttribute> orderBy();
}
