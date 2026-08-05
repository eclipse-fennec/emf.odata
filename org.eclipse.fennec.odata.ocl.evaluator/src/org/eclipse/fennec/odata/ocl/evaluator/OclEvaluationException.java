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
package org.eclipse.fennec.odata.ocl.evaluator;

/**
 * A query that is invalid for the actual data, detected at evaluation time (e.g. a string
 * function applied to a numeric property, an out-of-range literal). The message is curated
 * for clients — protocol layers map this to a client error (OData: {@code 400 Bad Request}),
 * never to an internal fault.
 */
public class OclEvaluationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OclEvaluationException(String message) {
		super(message);
	}

	public OclEvaluationException(String message, Throwable cause) {
		super(message, cause);
	}
}
