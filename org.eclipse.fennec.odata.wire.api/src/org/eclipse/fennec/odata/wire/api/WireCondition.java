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

/**
 * A condition over the parts of a wire property (#119): what a {@link WirePropertyHandler}
 * makes of a comparison. It can only name the parts and the other operand of the comparison,
 * so the runtime turns every condition into a query over model attributes — pushed down to the
 * backend like any filter. Nulls compare as in OData: {@code null eq null} is true, any other
 * comparison with {@code null} false.
 */
public sealed interface WireCondition {

	/** The part compared with the other operand: {@code part op operand}. */
	record Compare(EAttribute part, WireOperator operator) implements WireCondition {
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
