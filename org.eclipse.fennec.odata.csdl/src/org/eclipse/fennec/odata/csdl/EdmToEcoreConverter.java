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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TComplexType;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TEnumType;
import org.open.oasis.docs.odata.ns.edm.TEnumTypeMember;
import org.open.oasis.docs.odata.ns.edm.TNavigationProperty;
import org.open.oasis.docs.odata.ns.edm.TProperty;
import org.open.oasis.docs.odata.ns.edm.TPropertyRef;
import org.open.oasis.docs.odata.ns.edm.TTerm;
import org.open.oasis.docs.odata.ns.edm.TTypeDefinition;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;

/**
 * Maps an OASIS EDM/EDMX model instance back to an EMF {@link EPackage} (the read half of the
 * CSDL bridge — inverse of {@link EcoreToEdmConverter}). v1 covers the structural core:
 * EntityType/ComplexType → EClass, Property → EAttribute (or containment EReference when the
 * type is a structured type), NavigationProperty → EReference, EnumType → EEnum, BaseType →
 * super type, key → {@code ID} attributes, and {@code Partner} → {@code eOpposite}. Three
 * passes: classifier shells first, then features, then bidirectional reference wiring.
 *
 * <p>Open work packages tracked in {@code docs/odata-e2-converter-open-points.md}
 * (functions/actions, annotations, cross-schema references, facets, …).
 */
public class EdmToEcoreConverter {

	private final EcoreFactory ecore = EcoreFactory.eINSTANCE;

	/** Marker detail for a navigation whose qualified target awaits cross-schema resolution. */
	private static final String PENDING_TARGET = "pendingTargetType";

	public EPackage toEPackage(EdmxRoot root) {
		return toEPackage(root.getEdmx().getDataServices().getSchema().get(0));
	}

	/**
	 * Converts EVERY schema of the document — one {@link EPackage} each — and resolves
	 * cross-schema navigation targets in a final pass over the qualified type names
	 * (namespace or alias). Navigations whose target stays unresolved are REMOVED: an
	 * {@link EReference} without a type is invalid Ecore and worse than an absent feature.
	 */
	public List<EPackage> toEPackages(TEdmx edmx) {
		List<SchemaType> schemas = edmx.getDataServices().getSchema();
		List<EPackage> packages = new ArrayList<>();
		Map<String, EPackage> byNamespace = new HashMap<>();
		for (SchemaType schema : schemas) {
			EPackage pkg = toEPackage(schema);
			packages.add(pkg);
			byNamespace.put(schema.getNamespace(), pkg);
			if (schema.getAlias() != null && !schema.getAlias().isBlank()) {
				byNamespace.put(schema.getAlias(), pkg);
			}
		}
		for (EPackage pkg : packages) {
			for (EClassifier classifier : pkg.getEClassifiers()) {
				if (classifier instanceof EClass eClass) {
					resolvePendingTargets(eClass, byNamespace);
				}
			}
		}
		return packages;
	}

	private void resolvePendingTargets(EClass eClass, Map<String, EPackage> byNamespace) {
		List<EStructuralFeature> unresolved = new ArrayList<>();
		for (EStructuralFeature feature : eClass.getEStructuralFeatures()) {
			EAnnotation marker = feature.getEAnnotation(ODataAnnotationConstants.SOURCE);
			String qualified = marker == null ? null : marker.getDetails().get(PENDING_TARGET);
			if (qualified == null) {
				continue;
			}
			int dot = qualified.lastIndexOf('.');
			EPackage targetPackage = dot < 0 ? null : byNamespace.get(qualified.substring(0, dot));
			EClassifier target = targetPackage == null ? null
					: targetPackage.getEClassifier(qualified.substring(dot + 1));
			if (target instanceof EClass targetClass) {
				feature.setEType(targetClass);
				marker.getDetails().remove(PENDING_TARGET);
				if (marker.getDetails().isEmpty()) {
					feature.getEAnnotations().remove(marker);
				}
			} else {
				unresolved.add(feature);
			}
		}
		eClass.getEStructuralFeatures().removeAll(unresolved);
	}

	public EPackage toEPackage(SchemaType schema) {
		EPackage pkg = ecore.createEPackage();
		String ns = schema.getNamespace();
		// vocabulary-style schemas ("Org.OData.Core.V1") get a usable name from their Alias
		String alias = schema.getAlias();
		String simple = (alias != null && !alias.isBlank()) ? alias : simpleName(ns);
		pkg.setName(simple);
		pkg.setNsPrefix(simple);
		pkg.setNsURI("http://eclipse.org/fennec/odata/" + ns);

		Map<String, EClassifier> byName = new HashMap<>();

		// pass 1 — classifiers (empty shells), so feature types can resolve in pass 2
		for (TTypeDefinition td : schema.getTypeDefinition()) {
			EDataType dt = ecore.createEDataType();
			dt.setName(td.getName());
			dt.setInstanceClassName(
					EdmTypes.emf(String.valueOf(td.getUnderlyingType())).getInstanceClassName());
			register(pkg, byName, dt);
		}
		for (TEnumType en : schema.getEnumType()) {
			EEnum ee = ecore.createEEnum();
			ee.setName(en.getName());
			for (TEnumTypeMember m : en.getMember()) {
				EEnumLiteral lit = ecore.createEEnumLiteral();
				lit.setName(m.getName());
				lit.setLiteral(m.getName());
				lit.setValue((int) m.getValue());
				ee.getELiterals().add(lit);
			}
			register(pkg, byName, ee);
		}
		for (TEntityType t : schema.getEntityType()) {
			EClass c = named(ecore.createEClass(), t.getName());
			c.setAbstract(t.isAbstract());
			mapAnnotations(t.getAnnotation(), c);
			register(pkg, byName, c);
		}
		for (TComplexType t : schema.getComplexType()) {
			EClass c = named(ecore.createEClass(), t.getName());
			c.setAbstract(t.isAbstract());
			mapAnnotations(t.getAnnotation(), c);
			register(pkg, byName, c);
		}

		// pass 2 — wire features, supertypes and keys
		for (TEntityType t : schema.getEntityType()) {
			wire((EClass) byName.get(t.getName()), t.getBaseType(),
					t.getProperty(), t.getNavigationProperty(), keyNames(t), byName);
		}
		for (TComplexType t : schema.getComplexType()) {
			wire((EClass) byName.get(t.getName()), t.getBaseType(),
					t.getProperty(), t.getNavigationProperty(), Set.of(), byName);
		}

		// pass 3 — wire bidirectional references (eOpposite) from navigation Partner names,
		// once all references exist on both ends
		for (TEntityType t : schema.getEntityType()) {
			wireOpposites(t.getName(), t.getNavigationProperty(), byName);
		}
		for (TComplexType t : schema.getComplexType()) {
			wireOpposites(t.getName(), t.getNavigationProperty(), byName);
		}

		mapAnnotations(schema.getAnnotation(), pkg);

		// vocabulary terms have no structural Ecore counterpart → one EAnnotation per term on
		// the package (source = TERM_SOURCE_PREFIX + name), queryable by term registries (E1)
		for (TTerm term : schema.getTerm()) {
			EAnnotation a = ecore.createEAnnotation();
			a.setSource(ODataAnnotationConstants.TERM_SOURCE_PREFIX + term.getName());
			putTermDetail(a, "type", term.getType());
			putTermDetail(a, "appliesTo", term.getAppliesTo());
			putTermDetail(a, "defaultValue", term.getDefaultValue());
			putTermDetail(a, "baseTerm", term.getBaseTerm());
			pkg.getEAnnotations().add(a);
		}
		return pkg;
	}

	/**
	 * Generic {@code <Annotation>} elements (AP-5) → the {@code @…/annotations} EAnnotation on
	 * the corresponding Ecore element. Only the constant-expression subset is mapped; rich
	 * expressions (Record/Collection/Path) are skipped.
	 */
	private void mapAnnotations(List<AnnotationType> annotations, EModelElement target) {
		for (AnnotationType annotation : annotations) {
			String value = constantValue(annotation);
			if (annotation.getTerm() == null || value == null) {
				continue;
			}
			EAnnotation holder = target.getEAnnotation(ODataAnnotationConstants.ANNOTATIONS_SOURCE);
			if (holder == null) {
				holder = ecore.createEAnnotation();
				holder.setSource(ODataAnnotationConstants.ANNOTATIONS_SOURCE);
				target.getEAnnotations().add(holder);
			}
			holder.getDetails().put(annotation.getTerm(), value);
		}
	}

	/** The constant-expression value in attribute form, or {@code null} for rich expressions. */
	private static String constantValue(AnnotationType a) {
		if (a.getString1() != null) {
			return a.getString1();
		}
		if (a.isSetBool1()) {
			return String.valueOf(a.isBool1());
		}
		if (a.getInt1() != null) {
			return a.getInt1().toString();
		}
		if (a.getDecimal1() != null) {
			return a.getDecimal1();
		}
		if (a.isSetFloat1()) {
			return String.valueOf(a.getFloat1());
		}
		if (a.getGuid1() != null) {
			return a.getGuid1();
		}
		if (a.getDate1() != null) {
			return a.getDate1().toString();
		}
		if (a.getDateTimeOffset1() != null) {
			return a.getDateTimeOffset1().toString();
		}
		if (a.getDuration1() != null) {
			return a.getDuration1().toString();
		}
		if (a.getTimeOfDay1() != null) {
			return a.getTimeOfDay1();
		}
		if (a.getBinary1() != null) {
			return a.getBinary1();
		}
		return null;
	}

	private static void putTermDetail(EAnnotation a, String key, Object value) {
		if (value == null) {
			return;
		}
		String text = value instanceof List<?> list
				? list.stream().map(String::valueOf).collect(Collectors.joining(" "))
				: String.valueOf(value);
		if (!text.isBlank()) {
			a.getDetails().put(key, text);
		}
	}

	private void wireOpposites(String ownerName, List<TNavigationProperty> navigation,
			Map<String, EClassifier> byName) {
		if (!(byName.get(ownerName) instanceof EClass owner)) {
			return;
		}
		for (TNavigationProperty n : navigation) {
			String partner = n.getPartner();
			if (partner == null || partner.isBlank()) {
				continue;
			}
			if (owner.getEStructuralFeature(n.getName()) instanceof EReference ref
					&& ref.getEType() instanceof EClass target
					&& target.getEStructuralFeature(partner) instanceof EReference opposite) {
				ref.setEOpposite(opposite);
			}
		}
	}

	private void wire(EClass cl, String baseType, List<TProperty> properties,
			List<TNavigationProperty> navigation, Set<String> keyNames, Map<String, EClassifier> byName) {
		if (baseType != null && !baseType.isBlank()) {
			EClassifier sup = byName.get(simpleName(baseType));
			if (sup instanceof EClass superClass) {
				cl.getESuperTypes().add(superClass);
			}
		}
		for (TProperty p : properties) {
			String type = String.valueOf(p.getType());
			boolean many = isCollection(type);
			String inner = unwrap(type);
			EClassifier resolved = resolve(inner, byName);
			if (resolved instanceof EClass complex) {
				// structured property → containment reference in Ecore
				EReference r = ecore.createEReference();
				r.setName(p.getName());
				r.setEType(complex);
				r.setContainment(true);
				applyBounds(r, many, p.isNullable());
				mapAnnotations(p.getAnnotation(), r);
				cl.getEStructuralFeatures().add(r);
			} else {
				EAttribute a = ecore.createEAttribute();
				a.setName(p.getName());
				a.setEType(resolved);
				applyBounds(a, many, p.isNullable());
				if (keyNames.contains(p.getName())) {
					a.setID(true);
					a.setLowerBound(1);
				}
				mapAnnotations(p.getAnnotation(), a);
				cl.getEStructuralFeatures().add(a);
			}
		}
		for (TNavigationProperty n : navigation) {
			String type = String.valueOf(n.getType());
			boolean many = isCollection(type);
			EClassifier target = resolve(unwrap(type), byName);
			EReference r = ecore.createEReference();
			r.setName(n.getName());
			if (target instanceof EClass tc) {
				r.setEType(tc);
			} else {
				// target lives in another schema — remember the qualified name so a
				// multi-schema conversion (toEPackages) can resolve it in its final pass
				EAnnotation pending = ecore.createEAnnotation();
				pending.setSource(ODataAnnotationConstants.SOURCE);
				pending.getDetails().put(PENDING_TARGET, unwrap(type));
				r.getEAnnotations().add(pending);
			}
			r.setContainment(n.isContainsTarget());
			applyBounds(r, many, n.isNullable());
			if (!n.getReferentialConstraint().isEmpty()) {
				// no structural Ecore counterpart → preserve as @OData annotation for round-trips
				EAnnotation a = ecore.createEAnnotation();
				a.setSource(ODataAnnotationConstants.SOURCE);
				a.getDetails().put(ODataAnnotationConstants.REFERENTIAL_CONSTRAINT,
						n.getReferentialConstraint().stream()
								.map(rc -> rc.getProperty() + "=" + rc.getReferencedProperty())
								.collect(Collectors.joining(",")));
				r.getEAnnotations().add(a);
			}
			mapAnnotations(n.getAnnotation(), r);
			cl.getEStructuralFeatures().add(r);
		}
	}

	private void applyBounds(EStructuralFeature f, boolean many, boolean nullable) {
		if (many) {
			f.setUpperBound(-1);
			f.setLowerBound(0);
		} else {
			f.setLowerBound(nullable ? 0 : 1);
		}
	}

	private EClassifier resolve(String typeName, Map<String, EClassifier> byName) {
		if (EdmTypes.isEdmPrimitive(typeName)) {
			return EdmTypes.emf(typeName);
		}
		return byName.getOrDefault(simpleName(typeName), EcorePackage.eINSTANCE.getEString());
	}

	private static Set<String> keyNames(TEntityType t) {
		if (t.getKey().isEmpty()) {
			return Set.of();
		}
		return t.getKey().get(0).getPropertyRef().stream()
				.map(TPropertyRef::getName).collect(Collectors.toSet());
	}

	private static boolean isCollection(String type) {
		return type.startsWith(EdmTypes.COLLECTION_OPEN);
	}

	private static String unwrap(String type) {
		return isCollection(type) ? type.substring(EdmTypes.COLLECTION_OPEN.length(), type.length() - 1) : type;
	}

	/** Last dot-segment of a qualified name (e.g. {@code Demo.Person} → {@code Person}). */
	private static String simpleName(String qualified) {
		int dot = qualified.lastIndexOf('.');
		return dot < 0 ? qualified : qualified.substring(dot + 1);
	}

	private EClass named(EClass c, String name) {
		c.setName(name);
		return c;
	}

	private void register(EPackage pkg, Map<String, EClassifier> byName, EClassifier c) {
		pkg.getEClassifiers().add(c);
		byName.put(c.getName(), c);
	}
}
