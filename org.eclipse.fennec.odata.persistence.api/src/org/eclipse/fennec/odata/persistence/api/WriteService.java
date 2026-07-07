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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Write side of the persistence SPI (OASIS "Updatable OData Service", req §9 E5/E6): one
 * service per backend, mirroring {@link QueryService}. Payloads arrive as EObjects of the
 * addressed entity type with EXACTLY the transmitted features set ({@code eIsSet} = "was in
 * the payload") — the difference between PATCH (merge) and PUT (replace) is the caller's
 * {@code replace} flag, the key always comes from the URL.
 *
 * <p>v1 scope: attributes and containment children; non-containment bindings
 * ({@code @odata.bind}) are a follow-up. Conflicts (existing key on create) raise
 * {@link WriteConflictException} → 409 at the protocol layer; unknown keys on delete return
 * {@code false} → 404.
 */
public interface WriteService {

	/** Whether this backend persists the given entity type. */
	boolean supports(EClass entityType);

	/**
	 * Inserts a new entity.
	 *
	 * @return the persisted entity (including store-populated values)
	 * @throws WriteConflictException when the key already exists
	 */
	EObject create(EClass entityType, EObject entity);

	/**
	 * Updates the entity with the given key — or CREATES it when absent (OData upsert,
	 * 13.1.1/29).
	 *
	 * @param rawKey  the raw key literal from the URL (still quoted for strings)
	 * @param payload the transmitted features ({@code eIsSet} marks presence)
	 * @param replace PUT semantics: features missing from the payload reset to their
	 *                defaults; false = PATCH: missing features stay untouched
	 * @return the resulting entity and whether it was newly created
	 */
	WriteResult update(EClass entityType, String rawKey, EObject payload, boolean replace);

	/** @return true when the entity existed and is gone now, false when it was absent */
	boolean delete(EClass entityType, String rawKey);

	/** Outcome of an {@link #update}: the persisted entity, created vs. updated. */
	record WriteResult(EObject entity, boolean created) {
	}
}
