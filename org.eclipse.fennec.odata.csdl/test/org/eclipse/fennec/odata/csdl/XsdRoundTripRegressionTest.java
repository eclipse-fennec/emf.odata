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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;

/**
 * E2/AP-11 regression tests (req Q15): the XSD contract of the CSDL bridge.
 * <ul>
 *   <li>The vendored real-world {@code $metadata} (TripPin) must validate against the vendored
 *       OASIS XSDs — guards the schemas themselves.</li>
 *   <li>The FULL round trip Ecore → EDM → CSDL-XML → EDM → Ecore must preserve the structural
 *       core; the in-memory round trip in {@link EcoreEdmRoundTripTest} deliberately skips the
 *       serialization layer, this test closes that gap.</li>
 * </ul>
 */
@DisplayName("E2 XSD + XML round-trip regression")
class XsdRoundTripRegressionTest {

	private EcoreHelper ecoreHelper;

	@AfterEach
	void cleanup() {
		if (ecoreHelper != null) {
			ecoreHelper.releaseAll();
		}
	}

	@Test
	@DisplayName("vendored TripPin $metadata validates against the OASIS XSDs")
	void trippinMetadataIsXsdValid() throws Exception {
		Path metadata = findResource("testdata/trippin-v4-metadata.xml",
				"org.eclipse.fennec.odata.csdl/testdata/trippin-v4-metadata.xml");
		assertValidCsdl(Files.readString(metadata, StandardCharsets.UTF_8));
	}

	@Test
	@DisplayName("TripPin constant annotations surface on the Ecore read (AP-5)")
	void trippinAnnotationsSurfaceOnEcoreRead() throws Exception {
		Path metadata = findResource("testdata/trippin-v4-metadata.xml",
				"org.eclipse.fennec.odata.csdl/testdata/trippin-v4-metadata.xml");
		EPackage pkg = new EdmToEcoreConverter()
				.toEPackage(parse(Files.readString(metadata, StandardCharsets.UTF_8)));

		// the Trip.Concurrency property carries <Annotation Term="Org.OData.Core.V1.Computed" Bool="true"/>
		EAttribute concurrency = pkg.getEClassifiers().stream()
				.filter(EClass.class::isInstance).map(EClass.class::cast)
				.map(c -> c.getEStructuralFeature("Concurrency"))
				.filter(EAttribute.class::isInstance).map(EAttribute.class::cast)
				.findFirst().orElseThrow(() -> new AssertionError("no Concurrency attribute found"));
		EAnnotation annotations = concurrency.getEAnnotation(ODataAnnotationConstants.ANNOTATIONS_SOURCE);
		assertNotNull(annotations, "constant annotation mapped onto the attribute");
		assertEquals("true", annotations.getDetails().get("Org.OData.Core.V1.Computed"));
	}

	@Test
	@DisplayName("company.ecore survives Ecore → EDM → XML → EDM → Ecore")
	void companyRoundTripsThroughXml() throws Exception {
		EPackage model = loadEcore("testdata/company.ecore",
				"org.eclipse.fennec.odata.csdl/testdata/company.ecore");

		String xml = serialize(new EcoreToEdmConverter().toEdmx(model));
		assertValidCsdl(xml);
		EPackage rt = new EdmToEcoreConverter().toEPackage(parse(xml));

		// structural spot checks across the categories the in-memory test covers in depth
		EClass abstractEntity = (EClass) rt.getEClassifier("AbstractEntity");
		assertTrue(abstractEntity.isAbstract(), "abstract flag survives the XML layer");
		assertTrue(((EAttribute) abstractEntity.getEStructuralFeature("id")).isID(), "key survives");

		EClass region = (EClass) rt.getEClassifier("RegionCode");
		assertTrue(((EAttribute) region.getEStructuralFeature("countryCode")).isID()
				&& ((EAttribute) region.getEStructuralFeature("regionCode")).isID(), "composite key survives");

		EClass employee = (EClass) rt.getEClassifier("Employee");
		EClass person = (EClass) rt.getEClassifier("Person");
		assertTrue(employee.getESuperTypes().contains(person), "inheritance chain survives");

		EReference manager = (EReference) employee.getEStructuralFeature("manager");
		EReference reports = (EReference) employee.getEStructuralFeature("reports");
		assertSame(reports, manager.getEOpposite(), "Partner wiring survives the XML layer");

		EClass project = (EClass) rt.getEClassifier("Project");
		assertTrue(((EReference) project.getEStructuralFeature("milestones")).isContainment(),
				"ContainsTarget survives");
		assertSame(rt.getEClassifier("Priority"),
				((EAttribute) project.getEStructuralFeature("priority")).getEType(), "enum typing survives");
	}

	@Test
	@DisplayName("catalog.ecore annotation layer survives the XML layer")
	void catalogAnnotationsRoundTripThroughXml() throws Exception {
		EPackage model = loadEcore("testdata/catalog.ecore",
				"org.eclipse.fennec.odata.csdl/testdata/catalog.ecore");

		String xml = serialize(new EcoreToEdmConverter().toEdmx(model));
		assertValidCsdl(xml);
		assertTrue(xml.contains("NavigationPropertyBinding"), "binding serialized: " + xml);
		assertTrue(xml.contains("Org.OData.Core.V1.Computed"), "Core annotation serialized");
		assertTrue(xml.contains("edmx:Reference"), "vocabulary reference serialized");

		EPackage rt = new EdmToEcoreConverter().toEPackage(parse(xml));

		EClass document = (EClass) rt.getEClassifier("Document");
		assertTrue(((EAttribute) document.getEStructuralFeature("docId")).isID(), "@OData.Key → key → iD survives");

		EReference owner = (EReference) document.getEStructuralFeature("owner");
		EAnnotation constraint = owner.getEAnnotation(ODataAnnotationConstants.SOURCE);
		assertNotNull(constraint, "referential constraint annotation restored from XML");
		assertEquals("ownerId=id", constraint.getDetails().get(ODataAnnotationConstants.REFERENTIAL_CONSTRAINT));
	}

	// === helpers ===

	private EPackage loadEcore(String... candidates) throws IOException {
		ecoreHelper = new EcoreHelper();
		EPackage model = ecoreHelper.loadEcore(findResource(candidates));
		assertTrue(model.eResource().getErrors().isEmpty(), "test model must load cleanly");
		return model;
	}

	private static String serialize(EdmxRoot root) throws IOException {
		ResourceSet rs = edmResourceSet();
		Resource res = rs.createResource(URI.createURI("metadata.xml"));
		res.getContents().add(root);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		res.save(out, xmlOptions());
		return out.toString(StandardCharsets.UTF_8);
	}

	private static org.open.oasis.docs.odata.ns.edm.SchemaType parse(String xml) throws IOException {
		ResourceSet rs = edmResourceSet();
		Resource res = rs.createResource(URI.createURI("metadata.xml"));
		res.load(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), xmlOptions());
		assertTrue(res.getErrors().isEmpty(), "re-parse must be clean");
		EObject root = res.getContents().get(0);
		TEdmx edmx = (root instanceof EdmxRoot er) ? er.getEdmx() : (TEdmx) root;
		return edmx.getDataServices().getSchema().get(0);
	}

	private static ResourceSet edmResourceSet() {
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
		rs.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
		rs.getPackageRegistry().put(EdmxPackage.eNS_URI, EdmxPackage.eINSTANCE);
		return rs;
	}

	private static Map<Object, Object> xmlOptions() {
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
		options.put(XMLResource.OPTION_ENCODING, "UTF-8");
		return options;
	}

	private static void assertValidCsdl(String xml) throws Exception {
		File edmxXsd = findResource("testdata/schemas/edmx.xsd",
				"org.eclipse.fennec.odata.csdl/testdata/schemas/edmx.xsd").toFile();
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Validator validator = sf.newSchema(edmxXsd).newValidator();
		validator.validate(new StreamSource(new StringReader(xml)));
	}

	private static Path findResource(String... candidatesRelative) {
		Path start = Paths.get("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidatesRelative) {
				Path p = dir.resolve(rel);
				if (Files.exists(p)) {
					return p;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
