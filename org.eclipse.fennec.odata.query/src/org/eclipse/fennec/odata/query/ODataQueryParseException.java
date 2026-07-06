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
package org.eclipse.fennec.odata.query;

/**
 * Raised when an OData {@code $filter}/{@code $orderby} expression is syntactically invalid,
 * uses an unknown function, or references a property that does not exist on the context type.
 * Maps to a {@code 400 Bad Request} at the protocol layer.
 */
public class ODataQueryParseException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ODataQueryParseException(String message) {
		super(message);
	}

	public ODataQueryParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
