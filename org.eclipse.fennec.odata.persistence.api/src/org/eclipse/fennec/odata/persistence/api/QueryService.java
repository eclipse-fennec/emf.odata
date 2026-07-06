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

/**
 * Backend query SPI (req §3.5): each backend (in-memory over {@link EntityRepository}s, JPA,
 * MongoDB, ...) registers one QueryService that translates/evaluates the OCL predicate IR in
 * its native query language. Backend pushdown is mandatory — no partial in-memory re-filtering
 * on top of a pushdown backend.
 */
public interface QueryService {

	/** Whether this backend can answer queries for the given entity type. */
	boolean supports(EClass entityType);

	/**
	 * Executes the query. Implementations throw {@link IllegalArgumentException} for predicates
	 * they cannot translate (unknown custom operation, unresolvable alias) — the protocol layer
	 * maps that to a client error, never to silent partial results.
	 */
	QueryResult execute(EntityQuery query);

	/**
	 * Executes a {@code $apply} aggregation pipeline (plus the post-pipeline query options,
	 * see {@link ApplyQuery}) over all instances of the entity type. Optional capability —
	 * backends without aggregation support keep the default, which the protocol layer maps
	 * to {@code 501 Not Implemented}.
	 */
	default ApplyResult executeApply(ApplyQuery query) {
		throw new UnsupportedOperationException("$apply is not supported by this backend");
	}
}
