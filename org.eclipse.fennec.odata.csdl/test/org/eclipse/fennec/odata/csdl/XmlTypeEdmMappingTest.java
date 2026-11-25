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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.fennec.odata.csdl.profile.ODataClassProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TProperty;

/**
 * The XSD datatypes of {@link XMLTypePackage} map to the {@code Edm.*} primitive they are
 * modelled on (#144). An Ecore generated from an XSD types its features with these, so the
 * {@code Edm.String} default published a string contract for numeric, temporal and binary
 * data — a consumer comparing or parsing by the declared type then gets it wrong.
 *
 * <p>The first test is deliberately EXHAUSTIVE: a datatype EMF adds (or one dropped from the
 * table) fails here instead of silently widening to {@code Edm.String} again.
 */
@DisplayName("XSD datatypes (XMLTypePackage) → Edm primitive types")
class XmlTypeEdmMappingTest {

	/**
	 * Every XSD datatype by the Edm type it must produce. Rationale per group:
	 * the string family shares the lexical form; the exact-width integers map to the Edm type
	 * of the same width (Edm.Byte is UNSIGNED 8-bit, which is what xs:unsignedByte is); the
	 * BigInteger-backed families and xs:unsignedLong exceed Edm.Int64 and stay Edm.Decimal,
	 * like {@code EBigInteger}; both XSD binary encodings travel base64 on the wire; and the
	 * gX types have no Edm counterpart, so they must NOT be declared as a date type.
	 */
	private static final Map<String, List<String>> EXPECTED_BY_EDM = Map.ofEntries(
			Map.entry("Edm.String", List.of("String", "NormalizedString", "Token", "Language",
					"Name", "NCName", "NMTOKEN", "ID", "IDREF", "ENTITY", "AnyURI", "QName",
					"NOTATION", "AnySimpleType", "IDREFS", "IDREFSBase", "ENTITIES",
					"ENTITIESBase", "NMTOKENS", "NMTOKENSBase", "GDay", "GMonth", "GMonthDay",
					"GYear", "GYearMonth")),
			Map.entry("Edm.Boolean", List.of("Boolean", "BooleanObject")),
			Map.entry("Edm.SByte", List.of("Byte", "ByteObject")),
			Map.entry("Edm.Byte", List.of("UnsignedByte", "UnsignedByteObject")),
			Map.entry("Edm.Int16", List.of("Short", "ShortObject")),
			Map.entry("Edm.Int32", List.of("Int", "IntObject", "UnsignedShort", "UnsignedShortObject")),
			Map.entry("Edm.Int64", List.of("Long", "LongObject", "UnsignedInt", "UnsignedIntObject")),
			Map.entry("Edm.Decimal", List.of("Decimal", "Integer", "NonNegativeInteger",
					"PositiveInteger", "NonPositiveInteger", "NegativeInteger", "UnsignedLong")),
			Map.entry("Edm.Double", List.of("Double", "DoubleObject")),
			Map.entry("Edm.Single", List.of("Float", "FloatObject")),
			Map.entry("Edm.Binary", List.of("Base64Binary", "HexBinary")),
			Map.entry("Edm.Date", List.of("Date")),
			Map.entry("Edm.DateTimeOffset", List.of("DateTime")),
			Map.entry("Edm.TimeOfDay", List.of("Time")),
			Map.entry("Edm.Duration", List.of("Duration")));

	@Test
	@DisplayName("every XSD datatype maps to its Edm primitive, none left at the default")
	void everyXsdDatatypeMapsToItsEdmType() {
		Map<String, String> expected = expectedByDatatype();

		Map<String, String> actual = new LinkedHashMap<>();
		for (EClassifier classifier : XMLTypePackage.eINSTANCE.getEClassifiers()) {
			if (classifier instanceof EDataType dataType && !(classifier instanceof EEnum)) {
				actual.put(dataType.getName(), EdmTypes.edm(dataType));
			}
		}

		for (Map.Entry<String, String> entry : actual.entrySet()) {
			String want = expected.get(entry.getKey());
			assertNotNull(want, "XSD datatype not in the mapping table — map it in EdmTypes "
					+ "(and here), do not let it fall back to Edm.String: " + entry.getKey());
			assertEquals(want, entry.getValue(), "xs:" + entry.getKey());
		}
		assertEquals(expected.keySet(), actual.keySet(),
				"the table must describe exactly the datatypes XMLTypePackage declares");
	}

	@Test
	@DisplayName("the XSD entries are forward-only: the canonical reverse stays on Ecore primitives")
	void xsdMappingsAreForwardOnly() {
		EcorePackage e = EcorePackage.eINSTANCE;
		assertSame(e.getEString(), EdmTypes.emf("Edm.String"));
		assertSame(e.getEBigDecimal(), EdmTypes.emf("Edm.Decimal"));
		assertSame(e.getELong(), EdmTypes.emf("Edm.Int64"));
		assertSame(e.getEInt(), EdmTypes.emf("Edm.Int32"));
		assertSame(e.getEShort(), EdmTypes.emf("Edm.Int16"));
		assertSame(e.getEByte(), EdmTypes.emf("Edm.SByte"));
		assertSame(e.getEDouble(), EdmTypes.emf("Edm.Double"));
		assertSame(e.getEFloat(), EdmTypes.emf("Edm.Single"));
		assertSame(e.getEByteArray(), EdmTypes.emf("Edm.Binary"));
		assertSame(e.getEDate(), EdmTypes.emf("Edm.DateTimeOffset"));
		assertSame(e.getEDate(), EdmTypes.emf("Edm.Date"));
		// new on the reverse side: Edm.Byte is unsigned 0..255, which EByte (signed) cannot hold
		assertSame(e.getEShort(), EdmTypes.emf("Edm.Byte"));
	}

	@Test
	@DisplayName("a DCAT-AP-shaped XSD model: profile and $metadata carry the real types")
	void xsdModelPublishesItsRealTypes() {
		EPackage model = dcatLikeModel();

		ODataPackageProfile profile = new OdataResolver().resolve(model);
		ODataClassProfile dataset = byName(profile.getClasses(), ODataClassProfile::getName, "Dataset");
		assertEquals("Edm.Decimal", typeOfProperty(dataset, "byteSize"), "xs:nonNegativeInteger");
		assertEquals("Edm.Decimal", typeOfProperty(dataset, "spatialResolutionInMeters"), "xs:decimal");
		assertEquals("Edm.Duration", typeOfProperty(dataset, "temporalResolution"), "xs:duration");
		assertEquals("Edm.DateTimeOffset", typeOfProperty(dataset, "issued"), "xs:dateTime");
		assertEquals("Edm.String", typeOfProperty(dataset, "accessURL"), "xs:anyURI stays a string");

		SchemaType schema = new EcoreToEdmConverter().toSchema(model);
		TEntityType published = byName(schema.getEntityType(), TEntityType::getName, "Dataset");
		assertEquals("Edm.Decimal",
				String.valueOf(byName(published.getProperty(), TProperty::getName, "byteSize").getType()),
				"$metadata is the contract a client programs against");
	}

	// --- helpers ---

	private static Map<String, String> expectedByDatatype() {
		Map<String, String> byDatatype = new HashMap<>();
		EXPECTED_BY_EDM.forEach((edm, datatypes) -> datatypes.forEach(name -> byDatatype.put(name, edm)));
		return byDatatype;
	}

	/** A dynamic model shaped like the DCAT-AP Ecore the issue measured (XSD-generated types). */
	private static EPackage dcatLikeModel() {
		EcoreFactory factory = EcoreFactory.eINSTANCE;
		XMLTypePackage x = XMLTypePackage.eINSTANCE;

		EPackage pkg = factory.createEPackage();
		pkg.setName("dcat");
		pkg.setNsPrefix("dcat");
		pkg.setNsURI("http://example.org/dcat-ap");

		EClass dataset = factory.createEClass();
		dataset.setName("Dataset");
		pkg.getEClassifiers().add(dataset);

		dataset.getEStructuralFeatures().add(attribute("id", EcorePackage.eINSTANCE.getEString(), true));
		dataset.getEStructuralFeatures().add(attribute("byteSize", x.getNonNegativeInteger(), false));
		dataset.getEStructuralFeatures().add(attribute("spatialResolutionInMeters", x.getDecimal(), false));
		dataset.getEStructuralFeatures().add(attribute("temporalResolution", x.getDuration(), false));
		dataset.getEStructuralFeatures().add(attribute("issued", x.getDateTime(), false));
		dataset.getEStructuralFeatures().add(attribute("accessURL", x.getAnyURI(), false));
		return pkg;
	}

	private static EAttribute attribute(String name, EDataType type, boolean key) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		attribute.setID(key);
		return attribute;
	}

	private static String typeOfProperty(ODataClassProfile classProfile, String property) {
		return byName(classProfile.getProperties(), ODataPropertyProfile::getName, property).getTypeName();
	}

	private static <T> T byName(List<T> candidates, Function<T, String> name, String wanted) {
		return candidates.stream().filter(c -> wanted.equals(name.apply(c))).findFirst()
				.orElseThrow(() -> new AssertionError("no element named " + wanted));
	}
}
