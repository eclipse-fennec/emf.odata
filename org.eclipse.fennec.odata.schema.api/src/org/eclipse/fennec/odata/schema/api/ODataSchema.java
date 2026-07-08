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

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;

/**
 * The converted schema of ONE OData endpoint: the Ecore packages (one per OData namespace in the
 * {@code $metadata} document) together with the metadata used to detect later changes (ADR-0007).
 *
 * @param scope        the endpoint this schema belongs to
 * @param packages     the converted {@link EPackage}s (nsURI derived from the OData namespace)
 * @param contentHash  authoritative version — a hash of the raw CSDL document
 * @param etag         the {@code $metadata} ETag, if the server sent one (conditional-GET hint)
 * @param lastModified the {@code $metadata} Last-Modified value, if any (conditional-GET hint)
 */
public record ODataSchema(
		SchemaScope scope,
		List<EPackage> packages,
		String contentHash,
		Optional<String> etag,
		Optional<String> lastModified) {

	public ODataSchema {
		packages = List.copyOf(packages);
	}

	/** The stored version/validators, without the packages — what change-detection compares. */
	public ODataSchemaResolver.SchemaVersion toVersion() {
		return new ODataSchemaResolver.SchemaVersion(contentHash, etag, lastModified);
	}
}
