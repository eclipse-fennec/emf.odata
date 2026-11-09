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
 * A query construct the server knows but does not implement, with a message for the client:
 * {@code 501 Not Implemented} at the protocol layer.
 */
public class ODataQueryNotImplementedException extends UnsupportedOperationException {

	private static final long serialVersionUID = 1L;

	public ODataQueryNotImplementedException(String message) {
		super(message);
	}
}
