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

import org.eclipse.fennec.odata.schema.api.SchemaScope;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Hands out data {@link ODataClient}s for already-registered endpoints without the caller wiring a
 * resolver and an {@link java.net.http.HttpClient} by hand (ADR-0007). In OSGi this is a service:
 * inject it, call {@link #forEndpoint(SchemaScope)}. It binds the highest-ranked
 * {@link org.eclipse.fennec.odata.schema.api.ODataSchemaResolver}, so a downstream registry (e.g.
 * the Atlas) is used transparently, and shares one pooled {@code HttpClient} across the clients it
 * produces.
 */
@ProviderType
public interface ODataClientFactory {

	/** A data client for a registered endpoint; fails fast if the scope was never registered. */
	ODataClient forEndpoint(SchemaScope scope);

	/** Convenience: {@link #forEndpoint(SchemaScope)} from a service-root URI string. */
	default ODataClient forEndpoint(String serviceRoot) {
		return forEndpoint(SchemaScope.of(serviceRoot));
	}
}
