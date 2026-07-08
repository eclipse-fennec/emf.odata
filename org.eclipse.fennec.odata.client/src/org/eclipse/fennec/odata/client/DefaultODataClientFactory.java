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

import java.net.http.HttpClient;
import java.time.Duration;

import org.eclipse.fennec.odata.schema.api.ODataSchemaResolver;
import org.eclipse.fennec.odata.schema.api.SchemaScope;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

/**
 * Default {@link ODataClientFactory}: resolves the schema through the bound
 * {@link ODataSchemaResolver} and builds data clients over ONE shared, connect-timed
 * {@link HttpClient} that it owns (closed on deactivation). The produced {@link ODataClient}s do
 * not own that client, so closing an individual one leaves the shared pool intact.
 */
@Component(service = ODataClientFactory.class)
public final class DefaultODataClientFactory implements ODataClientFactory, AutoCloseable {

	private final ODataSchemaResolver resolver;
	private final HttpClient http;

	@Activate
	public DefaultODataClientFactory(@Reference ODataSchemaResolver resolver) {
		this.resolver = resolver;
		this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
	}

	@Override
	public ODataClient forEndpoint(SchemaScope scope) {
		return ODataClient.forEndpoint(scope, resolver, http);
	}

	@Deactivate
	@Override
	public void close() {
		http.close();
	}
}
