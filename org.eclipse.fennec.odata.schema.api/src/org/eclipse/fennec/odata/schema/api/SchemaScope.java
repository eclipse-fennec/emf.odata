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

import java.net.URI;
import java.util.Objects;

/**
 * The identity of a registered OData endpoint's schema — the key under which its Ecore packages
 * are registered and looked up (ADR-0007). Backed by the service-root URI, canonicalised so that
 * trivially different spellings of the same endpoint map to one scope.
 *
 * <p>The scope is the tenant/isolation boundary that keeps two endpoints exposing the SAME OData
 * namespace apart: the model identity ({@code EPackage} nsURI) stays semantic (derived from the
 * OData namespace), while the scope separates endpoints.
 *
 * @param serviceRoot the canonicalised service-root URI (never {@code null})
 */
public record SchemaScope(URI serviceRoot) {

	public SchemaScope {
		serviceRoot = canonical(Objects.requireNonNull(serviceRoot, "serviceRoot"));
	}

	/** {@link #SchemaScope(URI)} from a string service root. */
	public static SchemaScope of(String serviceRoot) {
		return new SchemaScope(URI.create(serviceRoot));
	}

	/** Canonical form: a trailing {@code /} is dropped so {@code .../odata} == {@code .../odata/}. */
	private static URI canonical(URI uri) {
		String text = uri.toString();
		return text.endsWith("/") ? URI.create(text.substring(0, text.length() - 1)) : uri;
	}
}
