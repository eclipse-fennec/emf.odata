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

import java.util.Optional;

/**
 * Client-side failure: transport errors, non-2xx service answers (with the HTTP status) and
 * undecodable payloads. The service's error body is carried verbatim in the message — it is
 * OData JSON produced by the server, already sanitized there — and, when it is a conforming OData
 * error document, also parsed into a structured {@link ODataError} available via {@link #error()}.
 */
public class ODataClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int status;
	private final transient ODataError error;

	public ODataClientException(String message) {
		this(message, 0, null);
	}

	public ODataClientException(String message, Throwable cause) {
		super(message, cause);
		this.status = 0;
		this.error = null;
	}

	/** Cap on how much of a raw error body is inlined into the exception message (log-bloat guard). */
	private static final int MAX_BODY_EXCERPT = 512;

	public ODataClientException(String message, int status, String body) {
		super(body == null || body.isBlank() ? message : message + ": " + excerpt(body));
		this.status = status;
		this.error = ODataError.parse(body).orElse(null);
	}

	private static String excerpt(String body) {
		String trimmed = body.strip();
		return trimmed.length() <= MAX_BODY_EXCERPT ? trimmed
				: trimmed.substring(0, MAX_BODY_EXCERPT) + "… (" + trimmed.length() + " bytes)";
	}

	/** The HTTP status of the failed exchange, 0 when the failure happened before/after HTTP. */
	public int status() {
		return status;
	}

	/** The parsed OData error document, if the service returned a conforming one. */
	public Optional<ODataError> error() {
		return Optional.ofNullable(error);
	}
}
