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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.odata.csdl.profile.ODataClassProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TProperty;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * A key property is never published as nullable, whatever its Ecore {@code lowerBound} says
 * ([OData-CSDL] Key: "Key properties MUST NOT be nullable") — #143.
 *
 * <p>The bound is not a modelling mistake: a type whose key is MINTED at the write boundary has
 * to keep {@code lowerBound="0"}, or EMF validation would reject exactly the POST bodies the
 * service accepts. Only the published contract has to state that the key is always present.
 * The nullability flag drives the two {@code $metadata} writers and nothing else, so forcing it
 * cannot change which requests the service accepts.
 */
@DisplayName("key properties are never Nullable (#143)")
class KeyNullabilityTest {

	/** The three ways a key is declared — all of them resolve through the same profile. */
	private static final String IS_ID = "isID";
	private static final String ODATA_KEY = "@OData.Key";
	private static final String ID_FEATURES = "idFeatures";

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { IS_ID, ODATA_KEY, ID_FEATURES })
	@DisplayName("an optional ID attribute still resolves to a non-nullable key property")
	void optionalIdAttributeResolvesNonNullable(String keyForm) {
		EPackage model = identifiedResource(keyForm);

		ODataClassProfile resource = resolve(model, "IdentifiedResource");
		assertEquals(List.of("about"), resource.getKeyPropertyNames(), "precondition: it IS the key");
		assertFalse(property(resource, "about").isNullable(),
				"the key declaration wins over lowerBound == 0");

		// the Ecore is untouched — the model keeps accepting a POST without the key
		assertEquals(0, ((EClass) model.getEClassifier("IdentifiedResource"))
				.getEStructuralFeature("about").getLowerBound(),
				"the fix must not push the bound back onto the model");
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { IS_ID, ODATA_KEY, ID_FEATURES })
	@DisplayName("CSDL XML declares the key property Nullable=\"false\"")
	void csdlXmlDeclaresTheKeyNonNullable(String keyForm) {
		SchemaType schema = new EcoreToEdmConverter().toSchema(identifiedResource(keyForm));
		TEntityType published = byName(schema.getEntityType(), TEntityType::getName, "IdentifiedResource");

		assertEquals(List.of("about"), published.getKey().stream()
				.flatMap(k -> k.getPropertyRef().stream()).map(r -> r.getName()).toList());
		assertFalse(byName(published.getProperty(), TProperty::getName, "about").isNullable(),
				"the document a strict client validates against");
		// the non-key property is untouched: the rule is about keys, not about optionality
		assertTrue(byName(published.getProperty(), TProperty::getName, "title").isNullable());
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = { IS_ID, ODATA_KEY, ID_FEATURES })
	@DisplayName("CSDL JSON omits $Nullable on the key, and keeps it on the optional property")
	void csdlJsonOmitsNullableOnTheKey(String keyForm) {
		String json = CsdlJsonWriter.write(new EcoreToEdmConverter().toEdmx(identifiedResource(keyForm)));
		JsonNode type = new ObjectMapper().readTree(json).path("dcat").path("IdentifiedResource");

		// [OData-CSDL-JSON] $Nullable defaults to FALSE, so a conformant key simply has no flag
		assertTrue(type.path("about").path("$Nullable").isMissingNode(),
				"the key must not carry $Nullable: " + json);
		assertTrue(type.path("title").path("$Nullable").asBoolean(false),
				"the optional non-key property still does: " + json);
	}

	@Test
	@DisplayName("a composite key forces every one of its properties")
	void compositeKeyForcesAllOfItsProperties() {
		EPackage model = compositeModel();

		ODataClassProfile reading = resolve(model, "Reading");
		assertEquals(List.of("sensorId", "observedAt"), reading.getKeyPropertyNames());
		assertFalse(property(reading, "sensorId").isNullable());
		assertFalse(property(reading, "observedAt").isNullable());
		assertTrue(property(reading, "value").isNullable(), "a non-key property is left alone");
	}

	@Test
	@DisplayName("a complex type (no key) is not touched")
	void complexTypeIsUntouched() {
		EPackage model = identifiedResource(IS_ID);
		EClass address = EcoreFactory.eINSTANCE.createEClass();
		address.setName("Address");
		address.getEStructuralFeatures().add(attribute("street", 0));
		model.getEClassifiers().add(address);

		ODataClassProfile resolved = resolve(model, "Address");
		assertTrue(resolved.getKeyPropertyNames().isEmpty(), "precondition: a complex type");
		assertTrue(property(resolved, "street").isNullable());
	}

	// === helpers =============================================================================

	/** The shape of the DCAT-AP type that raised this: the key is optional in Ecore. */
	private static EPackage identifiedResource(String keyForm) {
		EPackage pkg = pkg("dcat", "http://example.org/dcat-ap-" + keyForm);

		EClass cl = EcoreFactory.eINSTANCE.createEClass();
		cl.setName("IdentifiedResource");
		pkg.getEClassifiers().add(cl);

		EAttribute about = attribute("about", 0); // minted at the write boundary, hence optional
		cl.getEStructuralFeatures().add(about);
		cl.getEStructuralFeatures().add(attribute("title", 0));

		switch (keyForm) {
		case IS_ID -> about.setID(true);
		case ODATA_KEY -> annotate(about, ODataAnnotationConstants.SOURCE,
				ODataAnnotationConstants.KEY, "true");
		case ID_FEATURES -> annotate(cl, ODataAnnotationConstants.IDENTITY_SOURCE,
				ODataAnnotationConstants.ID_FEATURES, "about");
		default -> throw new IllegalArgumentException(keyForm);
		}
		return pkg;
	}

	/** Two optional attributes declared as one composite key. */
	private static EPackage compositeModel() {
		EPackage pkg = pkg("sta", "http://example.org/sta-composite");

		EClass reading = EcoreFactory.eINSTANCE.createEClass();
		reading.setName("Reading");
		pkg.getEClassifiers().add(reading);
		reading.getEStructuralFeatures().add(attribute("sensorId", 0));
		reading.getEStructuralFeatures().add(attribute("observedAt", 0));
		reading.getEStructuralFeatures().add(attribute("value", 0));

		annotate(reading, ODataAnnotationConstants.IDENTITY_SOURCE,
				ODataAnnotationConstants.ID_FEATURES, "sensorId,observedAt");
		return pkg;
	}

	private static EPackage pkg(String name, String nsUri) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName(name);
		pkg.setNsPrefix(name);
		pkg.setNsURI(nsUri);
		return pkg;
	}

	private static EAttribute attribute(String name, int lowerBound) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(EcorePackage.eINSTANCE.getEString());
		attribute.setLowerBound(lowerBound);
		return attribute;
	}

	private static void annotate(org.eclipse.emf.ecore.EModelElement element, String source,
			String detail, String value) {
		EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
		annotation.setSource(source);
		annotation.getDetails().put(detail, value);
		element.getEAnnotations().add(annotation);
	}

	private static ODataClassProfile resolve(EPackage model, String className) {
		ODataPackageProfile profile = new OdataResolver().resolve(model);
		return byName(profile.getClasses(), ODataClassProfile::getName, className);
	}

	private static ODataPropertyProfile property(ODataClassProfile c, String name) {
		return byName(c.getProperties(), ODataPropertyProfile::getName, name);
	}

	private static <T> T byName(List<T> candidates, Function<T, String> name, String wanted) {
		return candidates.stream().filter(c -> wanted.equals(name.apply(c))).findFirst()
				.orElseThrow(() -> new AssertionError("no element named " + wanted));
	}
}
