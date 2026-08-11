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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.odata.csdl.profile.ODataClassProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataTypeKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TPropertyRef;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;

/**
 * Composite identity across the CSDL bridge (emf.odata#35). A multi-part OData key maps onto the
 * one identity declaration the persistence stack reads — the {@code idFeatures} detail of the
 * {@link ODataAnnotationConstants#IDENTITY_SOURCE} annotation, in canonical key order — and never
 * onto several {@code isID} attributes, which Ecore does not allow
 * ({@code validateEClass_AtMostOneID}, persistence-jpa#115).
 * <p>
 * The XML layer is covered by {@code XsdRoundTripRegressionTest}; these cases stay on the model
 * level to pin the vocabulary and the key ORDER, which is what {@code CompositeIds.fragment} and
 * {@code CompositeIds.parse} depend on.
 */
class CompositeIdentityRoundTripTest {

	@Test
	@DisplayName("idFeatures alone makes an entity type with a multi-part key — no isID needed")
	void identityDeclarationAloneCarriesTheKey() {
		EPackage pkg = packageOf(composite("orderId,lineNo"));

		ODataClassProfile profile = new OdataResolver().resolve(pkg).getClasses().get(0);
		assertEquals(ODataTypeKind.ENTITY, profile.getKind(),
				"a type keyed only by the identity declaration is still an entity");
		assertEquals(List.of("orderId", "lineNo"), profile.getKeyPropertyNames());

		assertEquals(List.of("orderId", "lineNo"), keyRefs(entityType(pkg)),
				"both components reach the CSDL <Key>");
	}

	@Test
	@DisplayName("the key order follows the declaration, not the feature order")
	void keyOrderFollowsTheDeclaration() {
		// declared back to front: the fragment contract (k1=v1,k2=v2) is order-sensitive, so the
		// declaration — not the order the attributes happen to sit in — decides
		EPackage pkg = packageOf(composite("lineNo,orderId"));

		assertEquals(List.of("lineNo", "orderId"), keyRefs(entityType(pkg)));
	}

	@Test
	@DisplayName("a multi-part key comes back as the identity declaration, not as isID flags")
	void keyReturnsAsTheIdentityDeclaration() {
		EPackage pkg = packageOf(composite("orderId,lineNo"));

		EdmxRoot edmx = new EcoreToEdmConverter().toEdmx(pkg);
		EClass rt = orderLineOf(new EdmToEcoreConverter().toEPackage(edmx));

		EAnnotation identity = rt.getEAnnotation(ODataAnnotationConstants.IDENTITY_SOURCE);
		assertNotNull(identity, "the composite identity is declared on the type");
		assertEquals("orderId,lineNo", identity.getDetails().get(ODataAnnotationConstants.ID_FEATURES));
		assertFalse(attributeOf(rt, "orderId").isID(),
				"no isID flag — several of them would be an invalid model");
		assertFalse(attributeOf(rt, "lineNo").isID());
		assertEquals(1, attributeOf(rt, "orderId").getLowerBound(), "key components stay required");
	}

	@Test
	@DisplayName("a single-part key keeps the plain isID mapping")
	void singleKeyStaysOnIsId() {
		EPackage pkg = packageOf(singleKey());

		EClass rt = orderLineOf(new EdmToEcoreConverter()
				.toEPackage(new EcoreToEdmConverter().toEdmx(pkg)));

		assertTrue(attributeOf(rt, "orderId").isID(), "one key property is EMF's own eID attribute");
		assertNull(rt.getEAnnotation(ODataAnnotationConstants.IDENTITY_SOURCE),
				"no identity declaration needed for a single key");
	}

	@Test
	@DisplayName("the pre-#115 shape (several isID attributes) is still read as a composite key")
	void legacyIsIdShapeIsStillRead() {
		// models written before the identity declaration existed are still understood on the way
		// out — they are simply no longer produced on the way in
		EPackage pkg = packageOf(legacyBothFlagged());

		assertEquals(List.of("orderId", "lineNo"), keyRefs(entityType(pkg)));
	}

	// ============================================================ fixtures

	/** {@code OrderLine} keyed by the identity declaration alone — the canonical composite shape. */
	private static EClass composite(String idFeatures) {
		EClass orderLine = orderLine();
		EAnnotation identity = EcoreFactory.eINSTANCE.createEAnnotation();
		identity.setSource(ODataAnnotationConstants.IDENTITY_SOURCE);
		identity.getDetails().put(ODataAnnotationConstants.ID_FEATURES, idFeatures);
		orderLine.getEAnnotations().add(identity);
		return orderLine;
	}

	/** {@code OrderLine} keyed by {@code orderId} only — EMF's own single-eID shape. */
	private static EClass singleKey() {
		EClass orderLine = orderLine();
		attributeOf(orderLine, "orderId").setID(true);
		return orderLine;
	}

	/** {@code OrderLine} with both components flagged {@code isID} — the pre-#115 shape. */
	private static EClass legacyBothFlagged() {
		EClass orderLine = orderLine();
		attributeOf(orderLine, "orderId").setID(true);
		attributeOf(orderLine, "lineNo").setID(true);
		return orderLine;
	}

	private static EClass orderLine() {
		EClass orderLine = EcoreFactory.eINSTANCE.createEClass();
		orderLine.setName("OrderLine");
		orderLine.getEStructuralFeatures().addAll(List.of(
				attribute("orderId", EcorePackage.Literals.ESTRING, 1),
				attribute("lineNo", EcorePackage.Literals.EINT, 1),
				attribute("quantity", EcorePackage.Literals.EINT, 0)));
		return orderLine;
	}

	private static EAttribute attribute(String name, EClassifier type, int lowerBound) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		attribute.setLowerBound(lowerBound);
		return attribute;
	}

	private static EPackage packageOf(EClass... classes) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("identity");
		pkg.setNsURI("http://example.org/identity");
		pkg.setNsPrefix("id");
		pkg.getEClassifiers().addAll(List.of(classes));
		return pkg;
	}

	private static EClass orderLineOf(EPackage pkg) {
		return (EClass) pkg.getEClassifier("OrderLine");
	}

	private static EAttribute attributeOf(EClass cl, String name) {
		return (EAttribute) cl.getEStructuralFeature(name);
	}

	private static TEntityType entityType(EPackage pkg) {
		SchemaType schema = new EcoreToEdmConverter().toSchema(pkg);
		assertEquals(1, schema.getEntityType().size(), "the type is an entity type");
		return schema.getEntityType().get(0);
	}

	private static List<String> keyRefs(TEntityType type) {
		assertEquals(1, type.getKey().size(), "exactly one <Key> element");
		return type.getKey().get(0).getPropertyRef().stream().map(TPropertyRef::getName).toList();
	}
}
