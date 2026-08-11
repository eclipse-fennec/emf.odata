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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.fennec.persistence.helper.CompositeIds;

/**
 * The identity of an entity type as a BACKEND sees it (emf.odata#35): the key attributes in
 * canonical order, exactly as the persistence stack resolves them. Backends must agree with the
 * storage layer here — a store key or delta id derived differently from the {@code CompositeIds}
 * fragment would address a different entity.
 * <p>
 * The OData-facing layers take their key order from the resolved profile
 * ({@code ODataClassProfile.getKeyPropertyNames}) instead. Both read the same declaration — the
 * {@code idFeatures} detail of the {@code http://eclipse.org/fennec/persistence/1.0} annotation
 * (persistence-jpa#115), or a single {@code isID} attribute — so they agree; only a model keyed
 * purely by {@code @OData.Key}, which no backend ever supported, is OData-only.
 * <p>
 * This is the single place in the OData bundles that reads the persistence identity vocabulary
 * directly: backends go through it instead of importing the helper themselves.
 *
 * @since 1.0
 */
public final class EntityKeys {

	private EntityKeys() {
	}

	/**
	 * The key attributes of the type in canonical (key) order.
	 *
	 * @param entityType the type, must not be {@code null}
	 * @return the key attributes; empty for a keyless type
	 * @throws IllegalStateException if the type declares several {@code isID} attributes without the
	 *         identity annotation — invalid Ecore ({@code validateEClass_AtMostOneID}), not a
	 *         composite declaration
	 */
	public static List<EAttribute> of(EClass entityType) {
		return CompositeIds.idAttributes(entityType);
	}

	/**
	 * @param entityType the type, must not be {@code null}
	 * @return the first key attribute, or {@code null} for a keyless type
	 */
	public static EAttribute first(EClass entityType) {
		return of(entityType).stream().findFirst().orElse(null);
	}
}
