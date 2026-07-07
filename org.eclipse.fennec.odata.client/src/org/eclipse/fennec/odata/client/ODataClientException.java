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
package org.eclipse.fennec.odata.client;

/**
 * Client-side failure: transport errors, non-2xx service answers (with the HTTP status) and
 * undecodable payloads. The service's error body is carried verbatim in the message — it is
 * OData JSON produced by the server, already sanitized there.
 */
public class ODataClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int status;

	public ODataClientException(String message) {
		this(message, 0, null);
	}

	public ODataClientException(String message, Throwable cause) {
		super(message, cause);
		this.status = 0;
	}

	public ODataClientException(String message, int status, String body) {
		super(body == null || body.isBlank() ? message : message + ": " + body);
		this.status = status;
	}

	/** The HTTP status of the failed exchange, 0 when the failure happened before/after HTTP. */
	public int status() {
		return status;
	}
}
