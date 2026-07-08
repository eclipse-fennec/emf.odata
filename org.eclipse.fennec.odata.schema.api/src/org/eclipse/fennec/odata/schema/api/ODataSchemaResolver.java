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

import java.util.Collection;
import java.util.Optional;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The read side of the schema registry — the ONLY thing the client data path depends on: given an
 * endpoint scope, hand back its registered schema (ADR-0007). The default implementation reads the
 * client's EPackage registry; an alternative reads the Model Atlas. The data path performs NO
 * {@code $metadata} fetch: a missing scope means the endpoint was never registered.
 */
@ProviderType
public interface ODataSchemaResolver {

	/** The registered schema for the scope, or empty if this endpoint was never registered. */
	Optional<ODataSchema> lookup(SchemaScope scope);

	/** The stored version/validators, without loading the packages — for change detection. */
	Optional<SchemaVersion> version(SchemaScope scope);

	/** Every currently-registered endpoint scope — for bulk re-check (see the refresher). */
	Collection<SchemaScope> scopes();

	/** The change-detection fingerprint of a registered schema. */
	record SchemaVersion(String contentHash, Optional<String> etag, Optional<String> lastModified) {
	}
}
