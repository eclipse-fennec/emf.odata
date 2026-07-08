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

import org.eclipse.fennec.odata.schema.api.ODataSchema;
import org.eclipse.fennec.odata.schema.api.ODataSchemaReader;
import org.eclipse.fennec.odata.schema.api.ODataSchemaReader.Conditional;
import org.eclipse.fennec.odata.schema.api.ODataSchemaRegistrar;
import org.eclipse.fennec.odata.schema.api.ODataSchemaResolver;
import org.eclipse.fennec.odata.schema.api.ODataSchemaResolver.SchemaVersion;
import org.eclipse.fennec.odata.schema.api.SchemaScope;

/**
 * Ties the reader, registrar and resolver into the endpoint lifecycle (ADR-0007): register a schema
 * when an endpoint is added to the system, re-check it on demand (downloading only when the server
 * reports a change), and drop it on removal. This is the ONLY place that couples "fetch" to
 * "persist"; the client data path never touches it.
 */
public final class ODataSchemaManager {

	/** Outcome of {@link #refresh(SchemaScope)}. */
	public enum RefreshResult {
		/** The endpoint's schema is unchanged (server said {@code 304} or the hash matched). */
		UNCHANGED,
		/** The schema changed and was re-registered. */
		UPDATED,
		/** The scope was not registered, so there was nothing to refresh. */
		NOT_FOUND
	}

	private final ODataSchemaReader reader;
	private final ODataSchemaRegistrar registrar;
	private final ODataSchemaResolver resolver;

	public ODataSchemaManager(ODataSchemaReader reader, ODataSchemaRegistrar registrar,
			ODataSchemaResolver resolver) {
		this.reader = reader;
		this.registrar = registrar;
		this.resolver = resolver;
	}

	/** Reads and registers the endpoint's schema. Called once when the endpoint is registered. */
	public ODataSchema onRegister(SchemaScope scope) {
		ODataSchema schema = reader.read(scope, Conditional.NONE).orElseThrow(
				() -> new ODataClientException("no $metadata available for " + scope.serviceRoot()));
		registrar.register(schema);
		return schema;
	}

	/**
	 * Re-checks a registered endpoint: a conditional {@code GET} avoids the transfer when unchanged,
	 * and even a fetched document is only re-registered when its content hash actually differs.
	 */
	public RefreshResult refresh(SchemaScope scope) {
		Optional<SchemaVersion> known = resolver.version(scope);
		if (known.isEmpty()) {
			return RefreshResult.NOT_FOUND;
		}
		SchemaVersion prior = known.get();
		Optional<ODataSchema> fresh = reader.read(scope,
				new Conditional(prior.etag(), prior.lastModified()));
		if (fresh.isEmpty() || fresh.get().contentHash().equals(prior.contentHash())) {
			return RefreshResult.UNCHANGED;
		}
		registrar.register(fresh.get());
		return RefreshResult.UPDATED;
	}

	/** The registered schema, registering it on first use — the opt-in lazy policy. */
	public ODataSchema ensureRegistered(SchemaScope scope) {
		return resolver.lookup(scope).orElseGet(() -> onRegister(scope));
	}

	/** Removes the endpoint's schema — deregistration. */
	public void onDeregister(SchemaScope scope) {
		registrar.remove(scope);
	}

	/** The read side, for wiring a data client via {@link ODataClient#forEndpoint}. */
	public ODataSchemaResolver resolver() {
		return resolver;
	}
}
