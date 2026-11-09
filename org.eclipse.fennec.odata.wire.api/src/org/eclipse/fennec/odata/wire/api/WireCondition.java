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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.fennec.odata.wire.mapping.WireProperty;

/**
 * A condition over the parts of a wire property (#119): what a {@link WirePropertyHandler}
 * makes of a comparison. It can only name the parts, the other operand of the comparison, the
 * bounds of an interval literal operand and, between two wire properties, the parts of the other
 * one ({@link WireProperty}), so the runtime turns every condition into a query over model attributes — pushed down to the
 * backend like any filter. Nulls compare as in OData: {@code null eq null} is true, any other
 * comparison with {@code null} false.
 */
public sealed interface WireCondition {

	/** The part compared with the other operand: {@code part op operand}. */
	record Compare(EAttribute part, WireOperator operator) implements WireCondition {
	}

	/** A bound of an interval literal operand ({@link OperandKind#INTERVAL}, #129). */
	enum Bound {
		START, END
	}

	/** The part compared with a bound of the interval literal operand: {@code part op start}. */
	record CompareBound(EAttribute part, WireOperator operator, Bound bound) implements WireCondition {
	}

	/**
	 * The part compared with a part of the other wire property, when two wire properties are
	 * compared ({@link WirePropertyHandler#compare(WireOperator, WireProperty)}, #129).
	 */
	record CompareParts(EAttribute part, WireOperator operator, EAttribute otherPart) implements WireCondition {
	}

	/** The part is null. */
	record IsNull(EAttribute part) implements WireCondition {
	}

	record Not(WireCondition condition) implements WireCondition {
	}

	record And(List<WireCondition> conditions) implements WireCondition {
		public And {
			conditions = List.copyOf(conditions);
		}
	}

	record Or(List<WireCondition> conditions) implements WireCondition {
		public Or {
			conditions = List.copyOf(conditions);
		}
	}

	/** Always true or always false, e.g. a number compared with a property without a number part. */
	record Constant(boolean value) implements WireCondition {
	}

	static WireCondition compare(EAttribute part, WireOperator operator) {
		return new Compare(part, operator);
	}

	static WireCondition compare(EAttribute part, WireOperator operator, Bound bound) {
		return new CompareBound(part, operator, bound);
	}

	static WireCondition compare(EAttribute part, WireOperator operator, EAttribute otherPart) {
		return new CompareParts(part, operator, otherPart);
	}

	static WireCondition isNull(EAttribute part) {
		return new IsNull(part);
	}

	static WireCondition not(WireCondition condition) {
		return new Not(condition);
	}

	static WireCondition and(WireCondition... conditions) {
		return new And(List.of(conditions));
	}

	static WireCondition or(WireCondition... conditions) {
		return new Or(List.of(conditions));
	}

	static WireCondition constant(boolean value) {
		return new Constant(value);
	}
}
