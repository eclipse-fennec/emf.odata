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

import org.osgi.annotation.versioning.ProviderType;

/**
 * The write side of the schema registry — driven by the registration/refresh lifecycle, NOT by the
 * data path (ADR-0007). The default implementation stores into the client's EPackage registry; an
 * alternative pushes the packages into the Model Atlas (one scope per endpoint). Kept separate from
 * {@link ODataSchemaResolver} so consumers depend only on what they use (ISP).
 */
@ProviderType
public interface ODataSchemaRegistrar {

	/**
	 * Registers (or replaces) the schema for its scope. Idempotent for an unchanged
	 * {@link ODataSchema#contentHash()}; a changed hash replaces the prior registration.
	 */
	void register(ODataSchema schema);

	/** Removes a scope's schema — endpoint deregistration. No-op if the scope is unknown. */
	void remove(SchemaScope scope);
}
