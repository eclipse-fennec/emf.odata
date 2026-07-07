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
package org.eclipse.fennec.odata.persistence.api;

/**
 * A write collided with existing data (key already present, constraint violated) — the
 * protocol layer answers 409 Conflict. The message must be safe to show a client.
 */
public class WriteConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WriteConflictException(String message) {
		super(message);
	}

	public WriteConflictException(String message, Throwable cause) {
		super(message, cause);
	}
}
