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
package org.eclipse.fennec.odata.persistence.inmemory.write;

import java.util.List;

import org.eclipse.emf.ecore.EPackage;

/**
 * Test-side wiring for the write store. It lives in the component's own package for one reason:
 * {@code addEPackage} is a DS bind method and stays package-private, so a harness outside OSGi
 * has to be a neighbour to feed it — the same arrangement {@code InMemoryBackends} uses for the
 * query service's {@code addRepository}. Widening either to public would turn a DS detail into
 * API for the sake of tests.
 */
public final class WriteBackends {

	private WriteBackends() {
	}

	/** A write store that knows the given packages, as DS would have bound them. */
	public static MemoryWriteRepository repository(List<EPackage> packages) {
		MemoryWriteRepository repository = new MemoryWriteRepository();
		packages.forEach(repository::addEPackage);
		return repository;
	}
}
