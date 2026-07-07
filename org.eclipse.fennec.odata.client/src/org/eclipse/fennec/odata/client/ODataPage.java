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
 * One page of an entity-set read: the decoded entities plus the envelope's control
 * information — {@code @odata.count} ({@code -1} when the server sent none) and
 * {@code @odata.nextLink} ({@code null} on the last page).
 */
public record ODataPage(List<EObject> entities, long totalCount, String nextLink) {

	public boolean hasMore() {
		return nextLink != null;
	}
}
