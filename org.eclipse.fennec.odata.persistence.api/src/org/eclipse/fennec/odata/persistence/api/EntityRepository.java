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
package org.eclipse.fennec.odata.persistence.api;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Data-source abstraction beneath the {@link QueryService}: where entity instances COME FROM
 * is independent of how queries are EXECUTED. Implementations can serve instances from files
 * (XMI/JSON resources), memory, or any other store; pushdown-capable backends (JPA) implement
 * {@link QueryService} directly and skip this layer.
 *
 * <p>Registered as OSGi services; a query service may aggregate several repositories.
 */
public interface EntityRepository {

	/** Whether this repository holds instances of the given entity type. */
	boolean supplies(EClass entityType);

	/**
	 * All instances of the given entity type, in stable order. Callers must treat the list and
	 * the contained objects as read-only.
	 */
	List<EObject> entities(EClass entityType);
}
