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
package org.eclipse.fennec.odata.runtime;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.odata.csdl.ODataAnnotationConstants;

/**
 * A model shaped like the case behind emf.odata#91: an open-data root publishes one set of a
 * package that also holds the internal model of a ticketing system. Published through the set
 * {@code Auslastung} are its base type, a derived type, an enum, a complex type, a navigation
 * target, and the result type of its bound function. Internal and unreachable: the visit record
 * with personal data, the feedback with its own enum and unbound function, and a second package
 * of internal types only.
 */
final class NarrowedModelFixture {

	static final String NS_URI = "http://example.org/baeder";
	static final String INTERN_NS_URI = "http://example.org/intern";

	final EPackage baeder;
	final EPackage intern;

	NarrowedModelFixture() {
		baeder = pkg("baeder", NS_URI);
		EClass messung = eClass(baeder, "Messung", true);
		EClass auslastung = eClass(baeder, "Auslastung", false);
		auslastung.getESuperTypes().add(messung);
		key(messung, "id");
		eClass(baeder, "AuslastungSchaetzung", false).getESuperTypes().add(auslastung);
		EEnum status = eEnum(baeder, "Status", "OFFEN", "GESCHLOSSEN");
		attribute(auslastung, "status", status);
		EClass zeitraum = eClass(baeder, "Zeitraum", false);
		attribute(zeitraum, "von", EcorePackage.Literals.EDATE);
		reference(auslastung, "zeit", zeitraum, true);
		EClass bad = eClass(baeder, "Bad", false);
		key(bad, "id");
		reference(auslastung, "bad", bad, false);
		EClass prognose = eClass(baeder, "Prognose", false);
		attribute(prognose, "personen", EcorePackage.Literals.EINT);
		operation(auslastung, "prognose", prognose, true);

		EClass besuch = eClass(baeder, "KarteninhaberBesuch", false);
		key(besuch, "id");
		attribute(besuch, "email", EcorePackage.Literals.ESTRING);
		reference(besuch, "bad", bad, false); // points AT a published type: does not pull it in
		EClass feedback = eClass(baeder, "Feedback", false);
		key(feedback, "id");
		attribute(feedback, "art", eEnum(baeder, "FeedbackArt", "LOB", "BESCHWERDE"));
		operation(feedback, "exportFeedback", EcorePackage.Literals.ESTRING, false);

		intern = pkg("intern", INTERN_NS_URI);
		EClass checkin = eClass(intern, "Checkin", false);
		key(checkin, "id");
	}

	EClassifier type(String name) {
		EClassifier classifier = baeder.getEClassifier(name);
		return classifier != null ? classifier : intern.getEClassifier(name);
	}

	private static EPackage pkg(String name, String nsUri) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName(name);
		pkg.setNsPrefix(name);
		pkg.setNsURI(nsUri);
		return pkg;
	}

	private static EClass eClass(EPackage pkg, String name, boolean isAbstract) {
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(name);
		eClass.setAbstract(isAbstract);
		pkg.getEClassifiers().add(eClass);
		return eClass;
	}

	private static EEnum eEnum(EPackage pkg, String name, String... literals) {
		EEnum eEnum = EcoreFactory.eINSTANCE.createEEnum();
		eEnum.setName(name);
		for (int i = 0; i < literals.length; i++) {
			EEnumLiteral literal = EcoreFactory.eINSTANCE.createEEnumLiteral();
			literal.setName(literals[i]);
			literal.setValue(i);
			eEnum.getELiterals().add(literal);
		}
		pkg.getEClassifiers().add(eEnum);
		return eEnum;
	}

	private static void key(EClass eClass, String name) {
		attribute(eClass, name, EcorePackage.Literals.ESTRING).setID(true);
	}

	private static EAttribute attribute(EClass eClass, String name, EClassifier type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		eClass.getEStructuralFeatures().add(attribute);
		return attribute;
	}

	private static void reference(EClass eClass, String name, EClass target, boolean containment) {
		EReference reference = EcoreFactory.eINSTANCE.createEReference();
		reference.setName(name);
		reference.setEType(target);
		reference.setContainment(containment);
		eClass.getEStructuralFeatures().add(reference);
	}

	private static void operation(EClass eClass, String name, EClassifier returnType, boolean bound) {
		EOperation operation = EcoreFactory.eINSTANCE.createEOperation();
		operation.setName(name);
		operation.setEType(returnType);
		if (!bound) {
			operation.getEAnnotations().add(annotation(ODataAnnotationConstants.BOUND, "false"));
		}
		eClass.getEOperations().add(operation);
	}

	private static EAnnotation annotation(String key, String value) {
		EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
		annotation.setSource(ODataAnnotationConstants.SOURCE);
		annotation.getDetails().put(key, value);
		return annotation;
	}
}
