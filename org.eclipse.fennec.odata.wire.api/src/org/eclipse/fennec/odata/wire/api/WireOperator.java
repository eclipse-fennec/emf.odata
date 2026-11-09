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

/** A comparison of {@code $filter} as a {@link WirePropertyHandler} sees it (#119). */
public enum WireOperator {
	EQ, NE, GT, GE, LT, LE;

	/**
	 * The operator with its operands swapped: {@code X lt p} is {@code p gt X}. The runtime puts
	 * the wire property on the left before it asks the handler.
	 */
	public WireOperator converse() {
		return switch (this) {
		case GT -> LT;
		case GE -> LE;
		case LT -> GT;
		case LE -> GE;
		default -> this;
		};
	}
}
