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
 * The type of the other side of a comparison with a wire property (#119), so a handler can
 * pick the part that compares: a number literal, arithmetic or a math function is a
 * {@link #NUMBER}, a string literal or a string function a {@link #STRING}, a property by its
 * type.
 */
public enum OperandKind {
	NUMBER, STRING, BOOLEAN,
	/** {@code Edm.DateTimeOffset}. */
	DATE_TIME,
	/** {@code Edm.Date}. */
	DATE,
	/** {@code Edm.TimeOfDay}. */
	TIME_OF_DAY,
	/** The {@code null} literal. */
	NULL,
	/** A typed value of any other kind: GUID, duration, enumeration, binary. */
	OTHER,
	/** The type is not known, e.g. another wire property or an untyped path. */
	UNKNOWN
}
