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
package org.eclipse.fennec.odata.persistence.inmemory;

import java.util.List;

import org.eclipse.emf.ecore.EPackage;

/**
 * Test-harness access to the in-memory backend's package-private DS wiring (the OSGi runtime
 * binds these via Declarative Services; the plain-Java mirror harness wires them by hand).
 */
public final class InMemoryBackends {

	/** A writable repository plus a query service over it, wired for the given packages. */
	public record Wiring(MemoryWriteRepository repository, InMemoryQueryService queryService) {
	}

	private InMemoryBackends() {
	}

	public static Wiring wire(List<EPackage> packages) {
		MemoryWriteRepository repository = new MemoryWriteRepository();
		packages.forEach(repository::addEPackage);
		InMemoryQueryService queryService = new InMemoryQueryService();
		queryService.addRepository(repository);
		return new Wiring(repository, queryService);
	}
}
