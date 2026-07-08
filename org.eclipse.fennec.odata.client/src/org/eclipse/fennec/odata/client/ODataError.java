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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * A structured OData error ([OData-JSON] 5.1 / [OData-Protocol] 9.4): {@code code}, {@code message}
 * and optional {@code target}, with nested {@code details}. Parsed leniently from a service's error
 * body — a non-conforming body yields {@link Optional#empty()} (the raw body stays on the
 * {@link ODataClientException} message).
 *
 * @param code    a service-defined error code (may be empty)
 * @param message a human-readable message
 * @param target  the target of the error (e.g. the offending property), or {@code null}
 * @param details nested sub-errors (never {@code null})
 */
public record ODataError(String code, String message, String target, List<ODataError> details) {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	public ODataError {
		details = List.copyOf(details);
	}

	/** Parses the OData {@code {"error":{...}}} envelope, or empty if the body is not one. */
	static Optional<ODataError> parse(String body) {
		if (body == null || body.isBlank()) {
			return Optional.empty();
		}
		try {
			JsonNode error = MAPPER.readTree(body).get("error");
			return error != null && error.isObject() ? Optional.of(from(error)) : Optional.empty();
		} catch (RuntimeException notAnErrorDocument) {
			return Optional.empty();
		}
	}

	private static ODataError from(JsonNode node) {
		List<ODataError> details = new ArrayList<>();
		JsonNode detailNodes = node.get("details");
		if (detailNodes != null && detailNodes.isArray()) {
			detailNodes.forEach(detail -> details.add(from(detail)));
		}
		return new ODataError(text(node, "code"), text(node, "message"), text(node, "target"), details);
	}

	private static String text(JsonNode node, String field) {
		JsonNode value = node.get(field);
		return value == null || value.isNull() ? null : value.asString();
	}
}
