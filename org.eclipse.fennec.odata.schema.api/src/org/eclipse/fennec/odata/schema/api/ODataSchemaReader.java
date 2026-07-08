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
package org.eclipse.fennec.odata.schema.api;

import java.util.Optional;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Obtains the Ecore schema of an OData endpoint from its {@code $metadata} — side-effect-free:
 * it neither registers nor caches anything (ADR-0007). This is the "give me only the Ecores"
 * operation, deliberately separate from {@link ODataSchemaRegistrar}.
 */
@ProviderType
public interface ODataSchemaReader {

	/**
	 * Fetches {@code $metadata} for the scope and converts it to an {@link ODataSchema}. When
	 * {@code conditional} carries validators and the server answers {@code 304 Not Modified}, this
	 * returns {@link Optional#empty()} (the caller keeps the already-registered schema).
	 *
	 * @param scope       the endpoint to read
	 * @param conditional prior ETag/Last-Modified to send as a conditional GET, or
	 *                    {@link Conditional#NONE} for an unconditional read
	 * @return the freshly read schema, or empty if the server reported it unchanged
	 */
	Optional<ODataSchema> read(SchemaScope scope, Conditional conditional);

	/** Converts an already-fetched CSDL document, without any I/O (offline/testing). */
	ODataSchema read(SchemaScope scope, String csdl);

	/** Conditional-GET validators carried into {@link #read(SchemaScope, Conditional)}. */
	record Conditional(Optional<String> etag, Optional<String> lastModified) {

		/** No validators — an unconditional read. */
		public static final Conditional NONE = new Conditional(Optional.empty(), Optional.empty());
	}
}
