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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * Backend SPI for <b>change tracking</b> ([OData-Protocol] 11.3 "Requesting Changes"): a client
 * that sends {@code Prefer: odata.track-changes} receives a delta link whose token marks "now";
 * following the link yields everything that changed since. The protocol layer keeps the delta
 * link self-describing (it re-encodes the defining query's options), so a backend only has to
 * answer "what changed between this token and now" — it never stores per-client state.
 *
 * <p>Membership semantics follow the defining query: an entity that changed and now matches the
 * query's filter/cast is reported as an upsert (with its full current state), one that changed
 * and no longer matches — or was deleted — is reported as a {@link Removal}. Relationship-only
 * changes are NOT tracked ([OData-Protocol] 11.3: "Entities are considered changed if any of the
 * structural properties have changed").
 *
 * <p>Tokens are opaque to clients. A token older than the backend's retention window raises
 * {@link DeltaGoneException}, which the protocol layer maps to {@code 410 Gone} — the client
 * refetches the full set. A type without a registered DeltaService simply never gets the
 * {@code track-changes} preference applied.
 */
public interface DeltaService {

	/** Whether this backend tracks changes for the given entity type. */
	boolean supports(EClass entityType);

	/**
	 * The token capturing the CURRENT change state for the entity type — the starting point a
	 * delta link issued right now would track from.
	 */
	String trackingToken(EClass entityType);

	/**
	 * Everything that changed since {@code token}, evaluated against the defining query: upserts
	 * for entities that now match, removals for entities that were deleted or no longer match.
	 * Only the query's {@code entityType}, {@code castType} and {@code filter} define membership —
	 * paging and ordering do not apply to deltas ([OData-Protocol] 11.3.1).
	 *
	 * @throws DeltaGoneException when the token is malformed or has aged out of the backend's
	 *                            retention window ({@code 410 Gone})
	 */
	DeltaResult changesSince(EntityQuery definingQuery, String token);

	/**
	 * One batch of changes: the current state of every added or changed entity that matches the
	 * defining query, the removals, and the follow-up token for the next delta link. Multiple
	 * changes to the same entity collapse into its latest outcome.
	 */
	record DeltaResult(List<EObject> changed, List<Removal> removals, String nextToken) {

		public DeltaResult {
			changed = changed == null ? List.of() : List.copyOf(changed);
			removals = removals == null ? List.of() : List.copyOf(removals);
			Objects.requireNonNull(nextToken, "nextToken must not be null");
		}
	}

	/**
	 * An entity that left the tracked result ([OData-JSON] deleted entity).
	 *
	 * @param keyValues key-property name → value, in the entity type's id-attribute order —
	 *                  enough for the protocol layer to render the entity id
	 * @param reason    {@link #REASON_DELETED} when the entity was destroyed,
	 *                  {@link #REASON_CHANGED} when it merely left the tracked membership
	 */
	record Removal(Map<String, Object> keyValues, String reason) {

		public Removal {
			keyValues = keyValues == null ? Map.of()
					: java.util.Collections.unmodifiableMap(new LinkedHashMap<>(keyValues));
			reason = REASON_DELETED.equals(reason) ? REASON_DELETED : REASON_CHANGED;
		}
	}

	/** The entity was deleted (destroyed). */
	String REASON_DELETED = "deleted";
	/** The entity no longer matches the defining query (or left the collection). */
	String REASON_CHANGED = "changed";
}
