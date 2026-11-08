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

import java.util.List;

/**
 * What a profile needs to write the root document of a service: the absolute base URL and the
 * published entity sets and singletons with their absolute URLs.
 *
 * @param baseUrl     the absolute URL of the service root, without a trailing slash
 * @param entitySets  the published entity sets, in the order of the OData service document
 * @param singletons  the published singletons
 */
public record ServiceRoot(String baseUrl, List<Entry> entitySets, List<Entry> singletons) {

	public ServiceRoot {
		entitySets = List.copyOf(entitySets);
		singletons = List.copyOf(singletons);
	}

	/**
	 * One addressable resource of the root.
	 *
	 * @param name the published name
	 * @param url  its absolute URL
	 */
	public record Entry(String name, String url) {
	}
}
