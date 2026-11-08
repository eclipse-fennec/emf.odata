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

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * The wire profile of a service root (ADR-0010). Registered as a service with the property
 * {@value #PROFILE_PROPERTY}; a root selects it with its configuration key
 * {@value #ROOT_PROPERTY}. A root without that key speaks plain OData; a root whose profile is
 * not (yet) available answers 503 rather than plain OData.
 *
 * <p>Everything not covered here — the query language, {@code }, writes — stays OData.
 */
@ConsumerType
public interface WireProfile {

	/** Service property naming the profile, e.g. {@code sta}. */
	String PROFILE_PROPERTY = "odata.wire.profile";

	/** Configuration key of a service root selecting a profile by its {@value #PROFILE_PROPERTY}. */
	String ROOT_PROPERTY = "odata.wire.profile";

	/** The names of the control information. */
	ControlNames controls();

	/**
	 * Whether the key attributes appear as properties of the entity body. SensorThings writes the
	 * key only as {@link ControlNames#keyValue()}.
	 */
	default boolean keyInBody(EClass type) {
		return true;
	}

	/**
	 * The name of the key in {@code $filter}/{@code $select}/{@code $orderby}, or {@code null}
	 * when it is addressed by its modelled name (SensorThings {@code id}).
	 */
	default String keyQueryName(EClass type) {
		return null;
	}

	/**
	 * The key predicate in a link, without parentheses: {@code 42}, {@code 'abc'} (single quotes
	 * doubled), {@code a=1,b='x'} for a composite key. The default is the OData literal.
	 *
	 * @param key the key values by attribute name, in key order
	 */
	default String keyLiteral(Map<String, Object> key) {
		if (key.size() == 1) {
			return literal(key.values().iterator().next());
		}
		return key.entrySet().stream().map(entry -> entry.getKey() + "=" + literal(entry.getValue()))
				.collect(Collectors.joining(","));
	}

	/** The root document, or empty for the OData service document. */
	default Optional<String> serviceDocument(ServiceRoot root) {
		return Optional.empty();
	}

	/**
	 * The error body for a status and a message, or empty for the OData error ([OData-JSON] 21).
	 * The message is already JSON-escaped, stripped of control characters and truncated: it goes
	 * into a JSON string as it is, never escaped a second time.
	 */
	default Optional<String> errorBody(int status, String message) {
		return Optional.empty();
	}

	/** The OData literal of one key value: strings quoted with single quotes doubled. */
	static String literal(Object value) {
		if (value instanceof Number || value instanceof Boolean) {
			return String.valueOf(value);
		}
		return "'" + String.valueOf(value).replace("'", "''") + "'";
	}
}
