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
 * A delta token is malformed or has aged out of the backend's retention window — the protocol
 * layer answers 410 Gone and the client refetches the full set ([OData-Protocol] 11.3.2). The
 * message must be safe to show a client.
 */
public class DeltaGoneException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DeltaGoneException(String message) {
		super(message);
	}
}
