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
 * {@value #PROFILE_PROPERTY}. A root binds exactly one profile through its static reference
 * {@value #REFERENCE}; its configuration selects it with the target filter
 * {@code wireProfile.target}, e.g. {@code (odata.wire.profile=sta)}. Without a target a root
 * binds the plain OData profile ({@value #ODATA}), which the runtime registers itself. A root
 * whose target matches no profile does not start — it never falls back to plain OData.
 *
 * <p>The defaults of this interface are the OData behaviour. Everything not covered here — the
 * query language, {@code $metadata}, writes — stays OData.
 */
@ConsumerType
public interface WireProfile {

	/** Service property naming the profile, e.g. {@code sta}. */
	String PROFILE_PROPERTY = "odata.wire.profile";

	/** The {@value #PROFILE_PROPERTY} of the plain OData profile, the default of every root. */
	String ODATA = "odata";

	/**
	 * Name of the reference of a service root (and its request filter) to its profile; the
	 * configuration key {@code wireProfile.target} selects the profile.
	 */
	String REFERENCE = "wireProfile";

	/** The target filter of {@link #REFERENCE} without a configured one: plain OData. */
	String DEFAULT_TARGET = "(" + PROFILE_PROPERTY + "=" + ODATA + ")";

	/** The names of the control information; OData's by default. */
	default ControlNames controls() {
		return ControlNames.ODATA;
	}

	/**
	 * Whether the key attributes appear as properties of the entity body. SensorThings writes the
	 * key only as {@link ControlNames#keyValue()}.
	 */
	default boolean keyInBody(EClass type) {
		return true;
	}

	/**
	 * Whether links are absolute, built from the base URL of the root ({@code odata.base.url},
	 * else the request). SensorThings writes absolute self, navigation and next links; OData keeps
	 * its context URLs and next links relative.
	 */
	default boolean absoluteLinks() {
		return false;
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

	/**
	 * The OData literal of one key value: strings quoted with single quotes doubled, every other
	 * value (numbers, booleans, GUIDs, dates) as it prints ([OData-ABNF] {@code keyPredicate}).
	 */
	static String literal(Object value) {
		if (value instanceof CharSequence text) {
			return "'" + text.toString().replace("'", "''") + "'";
		}
		return String.valueOf(value);
	}
}
