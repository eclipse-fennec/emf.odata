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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.fennec.odata.schema.api.ODataSchema;
import org.eclipse.fennec.odata.schema.api.ODataSchemaRegistrar;
import org.eclipse.fennec.odata.schema.api.ODataSchemaResolver;
import org.eclipse.fennec.odata.schema.api.SchemaScope;

/**
 * Default schema registry (ADR-0007): an in-process, per-instance store keyed by endpoint scope.
 * Because each instance holds one endpoint's schema in isolation, the semantic nsURIs (derived from
 * the OData namespace) never collide across endpoints — the instance IS the scope boundary. An
 * alternative implementation (e.g. the Model Atlas) persists across endpoints using its own scopes.
 */
public final class EPackageSchemaRegistry implements ODataSchemaRegistrar, ODataSchemaResolver {

	private final ConcurrentMap<SchemaScope, ODataSchema> byScope = new ConcurrentHashMap<>();

	@Override
	public void register(ODataSchema schema) {
		byScope.put(schema.scope(), schema);
	}

	@Override
	public void remove(SchemaScope scope) {
		byScope.remove(scope);
	}

	@Override
	public Optional<ODataSchema> lookup(SchemaScope scope) {
		return Optional.ofNullable(byScope.get(scope));
	}

	@Override
	public Optional<SchemaVersion> version(SchemaScope scope) {
		return lookup(scope).map(ODataSchema::toVersion);
	}
}
