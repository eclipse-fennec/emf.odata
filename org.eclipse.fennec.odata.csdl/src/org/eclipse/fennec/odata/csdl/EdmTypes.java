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
package org.eclipse.fennec.odata.csdl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EcorePackage;

/**
 * Bidirectional mapping between EMF primitive {@link EDataType}s and OData {@code Edm.*}
 * primitive type names. Best-effort for v1; types without a 1:1 counterpart are widened
 * (e.g. EBigInteger → Edm.Decimal) and unknown names fall back to Edm.String / EString.
 */
final class EdmTypes {

	static final String COLLECTION_OPEN = "Collection(";

	private static final Map<EDataType, String> EMF_TO_EDM = new HashMap<>();
	private static final Map<String, EClassifier> EDM_TO_EMF = new HashMap<>();

	static {
		EcorePackage e = EcorePackage.eINSTANCE;
		put(e.getEString(), "Edm.String", true);
		put(e.getEBoolean(), "Edm.Boolean", true);
		put(e.getEBooleanObject(), "Edm.Boolean", false);
		put(e.getEInt(), "Edm.Int32", true);
		put(e.getEIntegerObject(), "Edm.Int32", false);
		put(e.getELong(), "Edm.Int64", true);
		put(e.getELongObject(), "Edm.Int64", false);
		put(e.getEShort(), "Edm.Int16", true);
		put(e.getEShortObject(), "Edm.Int16", false);
		put(e.getEByte(), "Edm.SByte", true);
		put(e.getEByteObject(), "Edm.SByte", false);
		put(e.getEDouble(), "Edm.Double", true);
		put(e.getEDoubleObject(), "Edm.Double", false);
		put(e.getEFloat(), "Edm.Single", true);
		put(e.getEFloatObject(), "Edm.Single", false);
		put(e.getEBigDecimal(), "Edm.Decimal", true);
		put(e.getEBigInteger(), "Edm.Decimal", false); // no Edm big integer → widen
		put(e.getEDate(), "Edm.DateTimeOffset", true);
		put(e.getEByteArray(), "Edm.Binary", true);
		put(e.getEChar(), "Edm.String", false);
		put(e.getECharacterObject(), "Edm.String", false);
		// reverse-only canonical mappings for Edm types without a distinct EMF primitive
		EDM_TO_EMF.putIfAbsent("Edm.Guid", e.getEString());
		EDM_TO_EMF.putIfAbsent("Edm.Date", e.getEDate());
		EDM_TO_EMF.putIfAbsent("Edm.Duration", e.getEString());
		EDM_TO_EMF.putIfAbsent("Edm.TimeOfDay", e.getEString());
	}

	private static void put(EDataType dt, String edm, boolean canonicalReverse) {
		EMF_TO_EDM.put(dt, edm);
		if (canonicalReverse) {
			EDM_TO_EMF.put(edm, dt);
		}
	}

	/** EMF datatype → Edm primitive type name (defaults to Edm.String). */
	static String edm(EDataType dt) {
		return EMF_TO_EDM.getOrDefault(dt, "Edm.String");
	}

	static boolean isEdmPrimitive(String typeName) {
		return typeName.startsWith("Edm.");
	}

	/** Edm primitive type name → EMF datatype (defaults to EString). */
	static EDataType emf(String edmType) {
		EClassifier c = EDM_TO_EMF.get(edmType);
		return (c instanceof EDataType dt) ? dt : EcorePackage.eINSTANCE.getEString();
	}

	private EdmTypes() {
	}
}
