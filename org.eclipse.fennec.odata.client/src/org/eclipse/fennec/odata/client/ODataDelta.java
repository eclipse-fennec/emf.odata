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

import java.util.List;

import org.eclipse.emf.ecore.EObject;

/**
 * One batch of changes from a delta link ([OData-Protocol] 11.3, [OData-JSON] delta payloads):
 * added or changed entities with their current state, removals, and the follow-up
 * {@code @odata.deltaLink} for the next round ({@code @odata.nextLink} when the changes span
 * pages). Both the 4.01 ({@code @removed}) and the 4.0 ({@code #Set/$deletedEntity} context)
 * deleted-entity forms decode into {@link Removal}s.
 */
public record ODataDelta(List<EObject> changed, List<Removal> removals, String deltaLink,
		String nextLink) {

	/**
	 * An entity that left the tracked result.
	 *
	 * @param id     the entity id (e.g. {@code Products('p9')}); {@code null} when the service
	 *               identified the entity by key fields only
	 * @param reason {@code "deleted"} (destroyed) or {@code "changed"} (left the membership);
	 *               {@code null} when the service sent none
	 */
	public record Removal(String id, String reason) {
	}

	public boolean hasMore() {
		return nextLink != null;
	}
}
