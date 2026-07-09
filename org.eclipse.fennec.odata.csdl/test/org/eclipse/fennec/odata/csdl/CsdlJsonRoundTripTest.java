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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;

import tools.jackson.databind.ObjectMapper;

/**
 * CSDL <b>JSON</b> round-trip regression: (1) the JSON emitted for the reference models is a
 * FIXED POINT of writer∘reader — re-serializing the parsed document reproduces it exactly, so
 * the reader provably covers everything the writer emits; (2) the Ecore→EDM→JSON→EDM→Ecore
 * chain preserves the structural facts (mirroring the XSD/XML round-trip test); (3) the official
 * OASIS example document parses into a usable model.
 */
class CsdlJsonRoundTripTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private EcoreHelper ecoreHelper;

	@AfterEach
	void cleanup() {
		if (ecoreHelper != null) {
			ecoreHelper.releaseAll();
		}
	}

	private EPackage load(String name) throws Exception {
		ecoreHelper = new EcoreHelper();
		return ecoreHelper.loadEcore(findResource("testdata/" + name,
				"org.eclipse.fennec.odata.csdl/testdata/" + name));
	}

	@Test
	@DisplayName("company + catalog: writer∘reader is a fixed point on the emitted JSON")
	void jsonFixedPoint() throws Exception {
		for (String model : new String[] { "company.ecore", "catalog.ecore" }) {
			EPackage pkg = load(model);
			String json = CsdlJsonWriter.write(new EcoreToEdmConverter().toEdmx(pkg));
			String reserialized = CsdlJsonWriter.write(new CsdlJsonReader().read(json));
			assertEquals(MAPPER.readTree(json), MAPPER.readTree(reserialized),
					model + ": the reader must capture everything the writer emits");
			ecoreHelper.releaseAll();
		}
	}

	@Test
	@DisplayName("Ecore → EDM → JSON → EDM → Ecore preserves the structural facts")
	void ecoreRoundTripThroughJson() throws Exception {
		EPackage model = load("company.ecore");
		EAnnotation singletons = EcoreFactory.eINSTANCE.createEAnnotation();
		singletons.setSource(ODataAnnotationConstants.SINGLETONS_SOURCE);
		singletons.getDetails().put("Me", "Person");
		model.getEAnnotations().add(singletons);

		String json = CsdlJsonWriter.write(new EcoreToEdmConverter().toEdmx(model));
		EdmxRoot parsed = new CsdlJsonReader().read(json);
		EPackage rt = new EdmToEcoreConverter().toEPackage(
				parsed.getEdmx().getDataServices().getSchema().get(0));

		assertEquals("company", rt.getName());
		EClass abstractEntity = (EClass) rt.getEClassifier("AbstractEntity");
		assertTrue(abstractEntity.isAbstract(), "abstract survives JSON");
		EClass person = (EClass) rt.getEClassifier("Person");
		assertTrue(person.getESuperTypes().contains(abstractEntity), "inheritance survives");
		assertTrue(((org.eclipse.emf.ecore.EAttribute) abstractEntity
				.getEStructuralFeature("id")).isID(), "$Key survives");
		EEnum priority = (EEnum) rt.getEClassifier("Priority");
		assertEquals(4, priority.getELiterals().size(), "enum members survive");
		EReference contact = (EReference) person.getEStructuralFeature("contact");
		assertNotNull(contact, "complex-typed property survives");
		assertTrue(contact.isContainment(), "complex property stays containment");
		EReference previous = (EReference) person.getEStructuralFeature("previousAddresses");
		assertNotNull(previous, "collection-valued complex property survives");
		assertEquals(-1, previous.getUpperBound(), "$Collection survives");
		EAnnotation rtSingletons = rt.getEAnnotation(ODataAnnotationConstants.SINGLETONS_SOURCE);
		assertNotNull(rtSingletons, "singleton declaration survives JSON");
		assertEquals("Person", rtSingletons.getDetails().get("Me"));

		SchemaType schema = parsed.getEdmx().getDataServices().getSchema().get(0);
		assertTrue(schema.getAction().stream().anyMatch(a -> "raiseSalary".equals(a.getName())),
				"bound action survives JSON");
		assertEquals(1, schema.getEntityContainer().get(0).getSingleton().size(),
				"<Singleton> is in the JSON container");
	}

	@Test
	@DisplayName("the official OASIS CSDL JSON example parses into a usable model")
	void oasisExampleParses() throws Exception {
		String json = Files.readString(findResource("testdata/csdl-example-16.1.json",
				"org.eclipse.fennec.odata.csdl/testdata/csdl-example-16.1.json"));
		EdmxRoot parsed = new CsdlJsonReader().read(json);
		SchemaType schema = parsed.getEdmx().getDataServices().getSchema().get(0);
		assertEquals("ODataDemo", schema.getNamespace());
		assertEquals(4, schema.getEntityType().size(), "Product/Category/Supplier/Country");
		assertEquals(1, schema.getComplexType().size(), "Address");
		assertEquals(1, schema.getFunction().size(), "ProductsByRating");
		assertEquals(1, schema.getEntityContainer().get(0).getSingleton().size(), "MainSupplier");
		assertEquals(2, parsed.getEdmx().getReference().size(), "Core + Measures references");

		EPackage pkg = new EdmToEcoreConverter().toEPackage(schema);
		EClass product = (EClass) pkg.getEClassifier("Product");
		assertNotNull(product.getEStructuralFeature("Price"), "properties survive");
		EReference category = (EReference) product.getEStructuralFeature("Category");
		assertNotNull(category, "navigation survives");
		assertEquals("Category", category.getEReferenceType().getName());
	}

	private static Path findResource(String... candidatesRelative) {
		Path start = Paths.get("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String candidate : candidatesRelative) {
				Path resolved = dir.resolve(candidate);
				if (Files.exists(resolved)) {
					return resolved;
				}
			}
		}
		throw new IllegalStateException("test resource not found: " + String.join(", ", candidatesRelative));
	}
}
