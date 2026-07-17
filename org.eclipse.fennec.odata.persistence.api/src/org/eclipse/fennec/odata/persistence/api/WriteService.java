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

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Write side of the persistence SPI (OASIS "Updatable OData Service", req §9 E5/E6): one
 * service per backend, mirroring {@link QueryService}. Payloads arrive as EObjects of the
 * addressed entity type with EXACTLY the transmitted features set ({@code eIsSet} = "was in
 * the payload") — the difference between PATCH (merge) and PUT (replace) is the caller's
 * {@code replace} flag, the key always comes from the URL.
 *
 * <p>Scope: attributes, containment children (deep insert) and non-containment bindings — a
 * non-containment payload member references an EXISTING entity by its key and is bound to the
 * store instance; an unknown target refuses the write (→ 400), it is never a silent deep
 * insert. An omitted navigation keeps its binding under PATCH and PUT alike — PUT replace
 * semantics cover structural properties only ([OData-Protocol] 11.4.3). Conflicts (existing
 * key on create) raise {@link WriteConflictException} → 409 at the protocol layer; unknown
 * keys on delete return {@code false} → 404.
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

	/**
	 * {@link #update(EClass, String, EObject, boolean)} addressed by a COMPOUND key predicate
	 * ([OData-URL] compoundKey — composite keys). Optional capability: backends without named-key
	 * support keep the default, which the protocol layer maps to {@code 501}.
	 */
	default WriteResult update(EClass entityType, Map<String, String> namedKeys,
			EObject payload, boolean replace) {
		throw new UnsupportedOperationException("composite-key updates are not supported");
	}

	/** {@link #delete(EClass, String)} addressed by a compound key predicate; optional (→ 501). */
	default boolean delete(EClass entityType, Map<String, String> namedKeys) {
		throw new UnsupportedOperationException("composite-key deletes are not supported");
	}

	/**
	 * Creates a new entity INSIDE a navigation ({@code POST Set(key)/nav}, 13.1.1/20):
	 * containments attach the child to the owner, non-containments persist the child in its
	 * own set AND link it. Backends without support keep the default → 501.
	 *
	 * @return the created child entity
	 */
	default EObject createRelated(EClass entityType, String rawKey, String navigation,
			EObject child) {
		throw new UnsupportedOperationException("related creation is not supported");
	}

	/**
	 * Links an EXISTING entity into a navigation ({@code PUT/POST …/nav/$ref}, 13.1.1/21+22):
	 * single-valued navigations are set, collection-valued ones gain a member. Backends
	 * without support keep the default → 501.
	 */
	default void link(EClass entityType, String rawKey, String navigation, String targetRawKey) {
		throw new UnsupportedOperationException("reference updates are not supported");
	}

	/**
	 * Removes a reference ({@code DELETE …/nav/$ref}, 13.1.1/25): single-valued navigations
	 * clear ({@code targetRawKey} null), collection-valued ones drop the member with the
	 * given key. Backends without support keep the default → 501.
	 *
	 * @return true when a reference was removed, false when there was none
	 */
	default boolean unlink(EClass entityType, String rawKey, String navigation,
			String targetRawKey) {
		throw new UnsupportedOperationException("reference updates are not supported");
	}

	/**
	 * Whether this backend supports the thread-bound transaction hooks below. When {@code false}
	 * (the default), {@code $batch} atomicity groups are executed non-atomically (best-effort).
	 */
	default boolean transactional() {
		return false;
	}

	/**
	 * Begins a thread-bound transaction: every {@code create}/{@code update}/{@code delete}/link
	 * call on the SAME thread joins it until {@link #commit()} or {@link #rollback()}. Used by the
	 * protocol layer for {@code $batch} atomicity groups (all-or-nothing change sets). No-op unless
	 * {@link #transactional()} is {@code true}.
	 */
	default void begin() {
	}

	/** Commits the thread-bound transaction opened by {@link #begin()}. */
	default void commit() {
	}

	/** Rolls back the thread-bound transaction, discarding every write since {@link #begin()}. */
	default void rollback() {
	}

	/** Outcome of an {@link #update}: the persisted entity, created vs. updated. */
	record WriteResult(EObject entity, boolean created) {
	}
}
