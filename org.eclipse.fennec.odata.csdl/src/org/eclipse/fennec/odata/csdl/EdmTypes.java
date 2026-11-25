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
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;

/**
 * Bidirectional mapping between EMF primitive {@link EDataType}s and OData {@code Edm.*}
 * primitive type names. Best-effort for v1; types without a 1:1 counterpart are widened
 * (e.g. EBigInteger → Edm.Decimal) and unknown names fall back to Edm.String / EString.
 *
 * <p>Both the {@link EcorePackage} primitives and the XSD datatypes of
 * {@link XMLTypePackage} are mapped: an Ecore generated from an XSD types its features with
 * the latter, and leaving them at the Edm.String default published a string contract for
 * numeric, temporal and binary data (#144). The XSD entries are FORWARD-ONLY — the canonical
 * reverse (reading a foreign {@code $metadata}) stays on the Ecore primitives, which is what
 * {@code CsdlJsonReader}/{@code EdmToEcoreConverter} build with.
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
		// Edm.Byte is UNSIGNED 8-bit, so EByte (signed) cannot hold it — EShort can, and the
		// forward map below produces Edm.Byte for xs:unsignedByte, so our own $metadata must
		// read back without widening to EString (mirror round-trip)
		EDM_TO_EMF.putIfAbsent("Edm.Byte", e.getEShort());

		// --- XSD datatypes (XMLTypePackage), forward-only ---
		// Mapped along the XSD types the Edm.* primitives are modelled on
		// ([OData-CSDL-XML] 7.1), but never to an Edm type whose range the XSD value class
		// could exceed: the BigInteger-backed families stay Edm.Decimal, like EBigInteger.
		XMLTypePackage x = XMLTypePackage.eINSTANCE;

		// string family — same lexical form, Edm.String is exact
		forward(x.getString(), "Edm.String");
		forward(x.getNormalizedString(), "Edm.String");
		forward(x.getToken(), "Edm.String");
		forward(x.getLanguage(), "Edm.String");
		forward(x.getName_(), "Edm.String");
		forward(x.getNCName(), "Edm.String");
		forward(x.getNMTOKEN(), "Edm.String");
		forward(x.getID(), "Edm.String");
		forward(x.getIDREF(), "Edm.String");
		forward(x.getENTITY(), "Edm.String");
		forward(x.getAnyURI(), "Edm.String");
		forward(x.getQName(), "Edm.String");
		forward(x.getNOTATION(), "Edm.String");
		forward(x.getAnySimpleType(), "Edm.String");
		// the list types hold a java.util.List on a SINGLE-valued attribute; their whitespace-
		// separated lexical form is what the payload carries, so Edm.String stays truthful
		forward(x.getIDREFS(), "Edm.String");
		forward(x.getIDREFSBase(), "Edm.String");
		forward(x.getENTITIES(), "Edm.String");
		forward(x.getENTITIESBase(), "Edm.String");
		forward(x.getNMTOKENS(), "Edm.String");
		forward(x.getNMTOKENSBase(), "Edm.String");

		forward(x.getBoolean(), "Edm.Boolean");
		forward(x.getBooleanObject(), "Edm.Boolean");

		// exact-width integers → the Edm type of the same width
		forward(x.getByte(), "Edm.SByte");
		forward(x.getByteObject(), "Edm.SByte");
		forward(x.getUnsignedByte(), "Edm.Byte"); // 0..255, Edm.Byte is unsigned
		forward(x.getUnsignedByteObject(), "Edm.Byte");
		forward(x.getShort(), "Edm.Int16");
		forward(x.getShortObject(), "Edm.Int16");
		forward(x.getUnsignedShort(), "Edm.Int32"); // 0..65535 needs 32 bit signed
		forward(x.getUnsignedShortObject(), "Edm.Int32");
		forward(x.getInt(), "Edm.Int32");
		forward(x.getIntObject(), "Edm.Int32");
		forward(x.getUnsignedInt(), "Edm.Int64"); // 0..2^32-1 needs 64 bit signed
		forward(x.getUnsignedIntObject(), "Edm.Int64");
		forward(x.getLong(), "Edm.Int64");
		forward(x.getLongObject(), "Edm.Int64");

		// unbounded (BigInteger) and xs:unsignedLong (up to 2^64-1) exceed Edm.Int64 —
		// Edm.Decimal is the only Edm primitive that carries them exactly, and the JSON codec
		// writes it as a NUMBER, so $filter/$orderby compare numerically everywhere (#144)
		forward(x.getDecimal(), "Edm.Decimal");
		forward(x.getInteger(), "Edm.Decimal");
		forward(x.getNonNegativeInteger(), "Edm.Decimal");
		forward(x.getPositiveInteger(), "Edm.Decimal");
		forward(x.getNonPositiveInteger(), "Edm.Decimal");
		forward(x.getNegativeInteger(), "Edm.Decimal");
		forward(x.getUnsignedLong(), "Edm.Decimal");

		forward(x.getDouble(), "Edm.Double");
		forward(x.getDoubleObject(), "Edm.Double");
		forward(x.getFloat(), "Edm.Single");
		forward(x.getFloatObject(), "Edm.Single");

		// both XSD binary encodings travel base64 on the wire — Edm.Binary IS base64
		forward(x.getBase64Binary(), "Edm.Binary");
		forward(x.getHexBinary(), "Edm.Binary");

		// the calendar types travel as XMLGregorianCalendar — EdmJsonValues writes and reads the
		// Edm lexical form for exactly these three, xs:duration converts through the datatype
		// itself; the gX types have no Edm counterpart and must NOT reach a date writer, so
		// they stay strings
		forward(x.getDate(), "Edm.Date");
		forward(x.getDateTime(), "Edm.DateTimeOffset");
		forward(x.getTime(), "Edm.TimeOfDay");
		forward(x.getDuration(), "Edm.Duration");
		forward(x.getGDay(), "Edm.String");
		forward(x.getGMonth(), "Edm.String");
		forward(x.getGMonthDay(), "Edm.String");
		forward(x.getGYear(), "Edm.String");
		forward(x.getGYearMonth(), "Edm.String");
	}

	/** EMF → Edm only; the reverse stays with the {@link EcorePackage} primitive of that Edm type. */
	private static void forward(EDataType dt, String edm) {
		put(dt, edm, false);
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
