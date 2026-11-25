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
package org.eclipse.fennec.odata.codec.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.codec.util.MetadataServiceFactory;
import org.eclipse.fennec.emf.osgi.metadata.MetadataWhiteboard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Value formats of the XSD datatypes an Ecore generated from an XSD carries (#144): since
 * {@code EdmTypes} publishes them as the Edm primitive they are modelled on, the payload must
 * carry THAT type's form — a number for {@code Edm.Decimal}, the spec string form for the
 * calendar types — and must read back into the attribute's own instance class.
 */
@DisplayName("XSD datatype value formats (XMLTypePackage)")
class XsdValueFormatTest {

	private static final XMLTypePackage XSD = XMLTypePackage.eINSTANCE;

	private EPackage pkg;
	private EClass recordClass;
	private MetadataWhiteboard metadataService;

	@BeforeEach
	void setUp() {
		pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("catalog");
		pkg.setNsPrefix("catalog");
		pkg.setNsURI("http://example.org/xsd-catalog");

		recordClass = EcoreFactory.eINSTANCE.createEClass();
		recordClass.setName("Record");
		pkg.getEClassifiers().add(recordClass);

		add("id", EcorePackage.eINSTANCE.getEString(), true);
		add("byteSize", XSD.getNonNegativeInteger(), false);
		add("issued", XSD.getDate(), false);
		add("modified", XSD.getDateTime(), false);
		add("openingTime", XSD.getTime(), false);
		add("temporalResolution", XSD.getDuration(), false);
		add("checksum", XSD.getHexBinary(), false);

		EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
		metadataService = MetadataServiceFactory.create();
		metadataService.registerPackage(pkg);
	}

	@AfterEach
	void tearDown() {
		EPackage.Registry.INSTANCE.remove(pkg.getNsURI());
	}

	@Test
	@DisplayName("an XSD-typed record serializes in the Edm form of its declared types")
	void writesEdmFormsForXsdTypes() throws IOException {
		String json = serialize(sampleRecord());

		// the point of #144: a number, not a quoted string — a client comparing byteSize
		// compares numerically, and the value survives beyond 2^53 exactly
		assertTrue(json.contains("\"byteSize\":9007199254740993"),
				"xs:nonNegativeInteger as an Edm.Decimal NUMBER: " + json);
		assertTrue(json.contains("\"issued\":\"2024-05-03\""), "xs:date as Edm.Date: " + json);
		assertTrue(json.contains("\"modified\":\"2024-05-03T10:15:30Z\""),
				"xs:dateTime as Edm.DateTimeOffset: " + json);
		assertTrue(json.contains("\"openingTime\":\"08:30\""), "xs:time as Edm.TimeOfDay: " + json);
		assertTrue(json.contains("\"temporalResolution\":\"P1DT2H\""),
				"xs:duration as Edm.Duration: " + json);
		assertTrue(json.contains("\"checksum\":\"-_A\""),
				"xs:hexBinary travels base64url, like any Edm.Binary: " + json);
	}

	@Test
	@DisplayName("the Edm forms read back into the XSD attribute types, unchanged")
	void readsEdmFormsBackIntoXsdTypes() throws IOException {
		EObject loaded = load(serialize(sampleRecord()));

		assertEquals(new BigInteger("9007199254740993"), loaded.eGet(feature("byteSize")));
		assertEquals("2024-05-03", lexical(loaded, "issued"));
		assertEquals("2024-05-03T10:15:30Z", lexical(loaded, "modified"));
		// Edm.TimeOfDay omits zero seconds, xs:time requires the field — the reader normalises
		assertEquals("08:30:00", lexical(loaded, "openingTime"));
		assertEquals("P1DT2H", String.valueOf(loaded.eGet(feature("temporalResolution"))));
		assertEquals("FBF0", EcoreUtil.convertToString(XSD.getHexBinary(), loaded.eGet(feature("checksum"))));
	}

	@Test
	@DisplayName("a calendar value keeps its own XSD timezone on the wire")
	void keepsExplicitTimezone() throws IOException {
		EObject record = pkg.getEFactoryInstance().create(recordClass);
		record.eSet(feature("id"), "r2");
		record.eSet(feature("modified"), xsd(XSD.getDateTime(), "2024-05-03T10:15:30+02:00"));

		String json = serialize(record);
		assertTrue(json.contains("\"modified\":\"2024-05-03T10:15:30+02:00\""),
				"the offset of the stored value, not a normalised UTC instant: " + json);
		assertEquals("2024-05-03T10:15:30+02:00", lexical(load(json), "modified"));
	}

	// === helpers ===

	private EObject sampleRecord() {
		EObject record = pkg.getEFactoryInstance().create(recordClass);
		record.eSet(feature("id"), "r1");
		// 2^53 + 1 — not representable as an IEEE754 double, so a lossy detour would show
		record.eSet(feature("byteSize"), new BigInteger("9007199254740993"));
		record.eSet(feature("issued"), xsd(XSD.getDate(), "2024-05-03"));
		record.eSet(feature("modified"), xsd(XSD.getDateTime(), "2024-05-03T10:15:30Z"));
		record.eSet(feature("openingTime"), xsd(XSD.getTime(), "08:30:00"));
		record.eSet(feature("temporalResolution"), xsd(XSD.getDuration(), "P1DT2H"));
		record.eSet(feature("checksum"), xsd(XSD.getHexBinary(), "FBF0"));
		return record;
	}

	private void add(String name, EDataType type, boolean key) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		attribute.setID(key);
		recordClass.getEStructuralFeatures().add(attribute);
	}

	private EAttribute feature(String name) {
		return (EAttribute) recordClass.getEStructuralFeature(name);
	}

	/** A value of an XSD datatype, built by the datatype's own factory (as EMF itself does). */
	private static Object xsd(EDataType type, String literal) {
		return EcoreUtil.createFromString(type, literal);
	}

	/** The XSD lexical form of a decoded calendar value. */
	private String lexical(EObject object, String name) {
		Object value = object.eGet(feature(name));
		return assertInstanceOf(XMLGregorianCalendar.class, value,
				name + " must decode into the attribute's own instance class").toXMLFormat();
	}

	private String serialize(EObject object) throws IOException {
		ODataJsonResourceImpl resource = new ODataJsonResourceImpl(
				URI.createURI("test://xsd-record.odatajson"), metadataService);
		resource.getContents().add(object);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, null);
		return out.toString(StandardCharsets.UTF_8);
	}

	private EObject load(String json) throws IOException {
		ODataJsonResourceImpl resource = new ODataJsonResourceImpl(
				URI.createURI("test://xsd-record-load.odatajson"), metadataService);
		Map<Object, Object> options = new HashMap<>();
		options.put(CodecResource.CODEC_ROOT_TYPE, recordClass);
		resource.load(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), options);
		return resource.getContents().get(0);
	}
}
