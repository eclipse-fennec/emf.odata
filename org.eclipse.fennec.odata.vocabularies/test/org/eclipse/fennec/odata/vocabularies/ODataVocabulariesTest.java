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
package org.eclipse.fennec.odata.vocabularies;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.odata.csdl.ODataAnnotationConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E1 vocabulary bootstrapping (req §3.4/Q5): the vendored OASIS CSDL definitions must come out
 * of the CSDL read path as usable EPackages — structured types as EClasses/EEnums/EDataTypes,
 * {@code <Term>} declarations as per-term EAnnotations on the package.
 */
@DisplayName("OASIS vocabularies as EPackages")
class ODataVocabulariesTest {

	@Test
	@DisplayName("Core: alias-named package with enum, type definition and term annotations")
	void coreVocabulary() {
		EPackage core = ODataVocabularies.getEPackage(ODataVocabularies.CORE);

		assertEquals("Core", core.getName(), "Schema alias becomes the package name");
		assertEquals("http://eclipse.org/fennec/odata/Org.OData.Core.V1", core.getNsURI());

		EEnum permission = assertInstanceOf(EEnum.class, core.getEClassifier("Permission"));
		assertNotNull(permission.getEEnumLiteral("Read"));
		assertNotNull(permission.getEEnumLiteral("ReadWrite"));

		EDataType tag = assertInstanceOf(EDataType.class, core.getEClassifier("Tag"));
		assertEquals("boolean", tag.getInstanceClassName(), "TypeDefinition Tag underlies Edm.Boolean");

		EAnnotation computed = core.getEAnnotation(ODataAnnotationConstants.TERM_SOURCE_PREFIX + "Computed");
		assertNotNull(computed, "term Computed as EAnnotation");
		assertEquals("Core.Tag", computed.getDetails().get("type"));
		assertEquals("Property", computed.getDetails().get("appliesTo"));
		assertEquals("true", computed.getDetails().get("defaultValue"));
	}

	@Test
	@DisplayName("Capabilities: complex vocabulary types become EClasses")
	void capabilitiesVocabulary() {
		EPackage capabilities = ODataVocabularies.getEPackage(ODataVocabularies.CAPABILITIES);

		assertEquals("Capabilities", capabilities.getName());
		EClass filterRestrictions = assertInstanceOf(EClass.class,
				capabilities.getEClassifier("FilterRestrictionsType"));
		assertNotNull(filterRestrictions.getEStructuralFeature("Filterable"));
		assertNotNull(capabilities.getEAnnotation(
				ODataAnnotationConstants.TERM_SOURCE_PREFIX + "FilterRestrictions"));
	}

	@Test
	@DisplayName("Measures + Validation: terms with multi-target appliesTo")
	void measuresAndValidationVocabularies() {
		EPackage measures = ODataVocabularies.getEPackage(ODataVocabularies.MEASURES);
		EAnnotation isoCurrency = measures.getEAnnotation(
				ODataAnnotationConstants.TERM_SOURCE_PREFIX + "ISOCurrency");
		assertNotNull(isoCurrency);
		assertEquals("Edm.String", isoCurrency.getDetails().get("type"));

		EPackage validation = ODataVocabularies.getEPackage(ODataVocabularies.VALIDATION);
		EAnnotation minimum = validation.getEAnnotation(
				ODataAnnotationConstants.TERM_SOURCE_PREFIX + "Minimum");
		assertNotNull(minimum);
		assertEquals("Property Parameter Term", minimum.getDetails().get("appliesTo"),
				"appliesTo list joined with spaces");
		assertInstanceOf(EDataType.class, validation.getEClassifier("SingleOrCollectionType"));
	}

	@Test
	@DisplayName("packages are cached — repeated access returns the same instance")
	void cachesPackages() {
		assertSame(ODataVocabularies.getEPackage(ODataVocabularies.CORE),
				ODataVocabularies.getEPackage(ODataVocabularies.CORE));
		assertTrue(ODataVocabularies.all().size() == 4);
	}
}
