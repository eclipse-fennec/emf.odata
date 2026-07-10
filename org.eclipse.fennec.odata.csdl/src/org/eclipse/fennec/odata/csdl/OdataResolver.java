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

import java.util.List;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.fennec.odata.csdl.profile.ODataAnnotation;
import org.eclipse.fennec.odata.csdl.profile.ODataClassProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataEnumMember;
import org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataOperationKind;
import org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint;
import org.eclipse.fennec.odata.csdl.profile.ODataTypeKind;
import org.eclipse.fennec.odata.csdl.profile.ProfileFactory;

/**
 * Step 1 of the CSDL conversion (ADR-0003): resolve an EMF {@link EPackage} into a fully
 * precomputed {@link ODataPackageProfile}. This is the single place that reads Ecore <em>and</em>
 * the {@code @OData.*} {@link EAnnotation} layer ({@link ODataAnnotationConstants}); the
 * {@link EcoreToEdmConverter} builder then serializes the profile 1:1 to an EDM/EDMX instance —
 * it never touches Ecore.
 *
 * <p>Plain-Ecore defaults: {@code isID}→key, {@code lowerBound==0}→nullable,
 * {@code isContainment}→ContainsTarget, an {@code eOpposite}→Partner, an EClass with a key→entity,
 * each {@link EOperation}→a <em>bound</em> Function/Action (void return→Action, else Function).
 * Annotation overrides on top: OpenType, HasStream, explicit Key, Computed/Immutable, Type override,
 * MaxLength/Precision/Scale/DefaultValue facets, ContainsTarget/OnDelete, operation kind/bound/
 * composable/entitySetPath. External/programmatic config layers (req §3.4) are a later WP.
 */
public class OdataResolver {

	private final ProfileFactory factory = ProfileFactory.eINSTANCE;

	public ODataPackageProfile resolve(EPackage pkg) {
		ODataPackageProfile profile = factory.createODataPackageProfile();
		String ns = annOr(pkg, ODataAnnotationConstants.NAMESPACE, namespace(pkg));
		profile.setNamespace(ns);
		String alias = ann(pkg, ODataAnnotationConstants.ALIAS);
		if (alias != null) {
			profile.setAlias(alias);
		}

		resolveAnnotations(pkg, profile.getAnnotations());

		for (EClassifier classifier : pkg.getEClassifiers()) {
			if (classifier instanceof EEnum en) {
				profile.getEnums().add(resolveEnum(en));
			} else if (classifier instanceof EClass cl && !cl.isInterface()) {
				profile.getClasses().add(resolveClass(cl, ns));
			}
		}
		return profile;
	}

	/** Generic vocabulary-term annotations (AP-5): {@code @…/annotations} details → profile. */
	private void resolveAnnotations(EModelElement element, List<ODataAnnotation> out) {
		EAnnotation source = element.getEAnnotation(ODataAnnotationConstants.ANNOTATIONS_SOURCE);
		if (source == null) {
			return;
		}
		source.getDetails().forEach(entry -> {
			ODataAnnotation annotation = factory.createODataAnnotation();
			annotation.setTerm(entry.getKey());
			annotation.setValue(entry.getValue());
			out.add(annotation);
		});
	}

	private ODataEnumProfile resolveEnum(EEnum en) {
		ODataEnumProfile e = factory.createODataEnumProfile();
		e.setName(en.getName());
		for (EEnumLiteral lit : en.getELiterals()) {
			ODataEnumMember m = factory.createODataEnumMember();
			m.setName(lit.getName());
			m.setValue(lit.getValue());
			e.getMembers().add(m);
		}
		return e;
	}

	private ODataClassProfile resolveClass(EClass cl, String ns) {
		ODataClassProfile c = factory.createODataClassProfile();
		c.setName(cl.getName());
		c.setQualifiedName(ns + "." + cl.getName());
		c.setKind(isEntity(cl) ? ODataTypeKind.ENTITY : ODataTypeKind.COMPLEX);
		c.setAbstract(cl.isAbstract());
		c.setOpenType(annFlag(cl, ODataAnnotationConstants.OPEN_TYPE));
		c.setHasStream(annFlag(cl, ODataAnnotationConstants.HAS_STREAM));
		resolveAnnotations(cl, c.getAnnotations());

		cl.getESuperTypes().stream().filter(s -> !s.isInterface()).findFirst()
				.ifPresent(s -> c.setBaseTypeQualifiedName(
						typeNamespace(s, ns) + "." + s.getName())); // cross-package aware (AP-6)

		// key: declared ID attributes, or attributes flagged @OData.Key (subtype inherits the root key)
		cl.getEAttributes().stream().filter(this::isKey)
				.forEach(a -> c.getKeyPropertyNames().add(a.getName()));

		for (EAttribute a : cl.getEAttributes()) {
			c.getProperties().add(property(a, ns));
		}
		for (EReference r : cl.getEReferences()) {
			EClassifier target = r.getEReferenceType();
			if (target instanceof EClass tc && isEntity(tc)) {
				c.getNavigationProperties().add(navigation(r, ns));
			} else {
				c.getProperties().add(complexProperty(r, target, ns)); // keyless complex → Property
			}
		}
		for (EOperation op : cl.getEOperations()) {
			c.getOperations().add(operation(op, c.getQualifiedName(), ns));
		}
		return c;
	}

	private ODataPropertyProfile property(EAttribute a, String ns) {
		ODataPropertyProfile p = factory.createODataPropertyProfile();
		p.setName(a.getName());
		String edmTypeOverride = ann(a, ODataAnnotationConstants.TYPE);
		p.setTypeName(edmTypeOverride != null
				? collection(edmTypeOverride, a.isMany())
				: typeName(a.getEAttributeType(), a.isMany(), ns));
		p.setNullable(a.getLowerBound() == 0);
		p.setComputed(annFlag(a, ODataAnnotationConstants.COMPUTED));
		p.setImmutable(annFlag(a, ODataAnnotationConstants.IMMUTABLE));
		annInt(a, ODataAnnotationConstants.MAX_LENGTH, p::setMaxLength);
		annInt(a, ODataAnnotationConstants.PRECISION, p::setPrecision);
		annInt(a, ODataAnnotationConstants.SCALE, p::setScale);
		String def = ann(a, ODataAnnotationConstants.DEFAULT_VALUE);
		if (def != null) {
			p.setDefaultValue(def);
		}
		String srid = ann(a, ODataAnnotationConstants.SRID);
		if (srid != null) {
			p.setSrid(srid);
		}
		String unicode = ann(a, ODataAnnotationConstants.UNICODE);
		if (unicode != null) {
			p.setUnicode(Boolean.valueOf(unicode));
		}
		resolveAnnotations(a, p.getAnnotations());
		return p;
	}

	private ODataPropertyProfile complexProperty(EReference r, EClassifier target, String ns) {
		ODataPropertyProfile p = factory.createODataPropertyProfile();
		p.setName(r.getName());
		p.setTypeName(typeName(target, r.isMany(), ns));
		p.setNullable(r.getLowerBound() == 0);
		resolveAnnotations(r, p.getAnnotations());
		return p;
	}

	private ODataNavigationProfile navigation(EReference r, String ns) {
		ODataNavigationProfile n = factory.createODataNavigationProfile();
		n.setName(r.getName());
		n.setTypeName(typeName(r.getEReferenceType(), r.isMany(), ns));
		n.setContainsTarget(annOr(r, ODataAnnotationConstants.CONTAINS_TARGET, r.isContainment()));
		if (!r.isMany()) {
			n.setNullable(r.getLowerBound() == 0);
		}
		if (r.getEOpposite() != null) {
			n.setPartner(r.getEOpposite().getName());
		}
		String onDelete = ann(r, ODataAnnotationConstants.ON_DELETE);
		if (onDelete != null) {
			n.setOnDelete(onDelete);
		}
		String constraints = ann(r, ODataAnnotationConstants.REFERENTIAL_CONSTRAINT);
		if (constraints != null) {
			for (String pair : constraints.split(",")) {
				int eq = pair.indexOf('=');
				if (eq <= 0 || eq == pair.length() - 1) {
					continue; // malformed pair — resolver is lenient, the CSDL XSD is the gate
				}
				ODataReferentialConstraint rc = factory.createODataReferentialConstraint();
				rc.setProperty(pair.substring(0, eq).trim());
				rc.setReferencedProperty(pair.substring(eq + 1).trim());
				n.getReferentialConstraints().add(rc);
			}
		}
		resolveAnnotations(r, n.getAnnotations());
		return n;
	}

	/** An EOperation maps to a bound (default) Function (returns a value) or Action (void). */
	private ODataOperationProfile operation(EOperation op, String bindingQualifiedName, String ns) {
		ODataOperationProfile o = factory.createODataOperationProfile();
		o.setName(op.getName());
		boolean bound = annOr(op, ODataAnnotationConstants.BOUND, true);
		o.setBound(bound);
		if (bound) {
			o.setBindingTypeName(bindingQualifiedName);
		}
		o.setComposable(annFlag(op, ODataAnnotationConstants.COMPOSABLE));
		String kindOverride = ann(op, ODataAnnotationConstants.OPERATION_KIND);
		ODataOperationKind kind = operationKind(kindOverride, op.getEType() == null);
		o.setKind(kind);
		if (op.getEType() != null) {
			o.setReturnTypeName(typeName(op.getEType(), op.isMany(), ns));
			o.setReturnNullable(op.getLowerBound() == 0);
		}
		String esp = ann(op, ODataAnnotationConstants.ENTITY_SET_PATH);
		if (esp != null) {
			o.setEntitySetPath(esp);
		}
		for (EParameter p : op.getEParameters()) {
			ODataParameterProfile pp = factory.createODataParameterProfile();
			pp.setName(p.getName());
			pp.setTypeName(typeName(p.getEType(), p.isMany(), ns));
			pp.setNullable(p.getLowerBound() == 0);
			o.getParameters().add(pp);
		}
		return o;
	}

	/** An EClass is an entity iff it (or a super type) carries a key attribute (ID or @OData.Key). */
	private boolean isEntity(EClass cl) {
		return cl.getEAllAttributes().stream().anyMatch(this::isKey);
	}

	private boolean isKey(EAttribute a) {
		return a.isID() || annFlag(a, ODataAnnotationConstants.KEY);
	}

	/** Qualified OData type name for a classifier, wrapped in {@code Collection(...)} when many. */
	private String typeName(EClassifier type, boolean many, String ns) {
		String base;
		if (type instanceof EEnum en) {
			base = typeNamespace(en, ns) + "." + en.getName();
		} else if (type instanceof EDataType dt) {
			base = EdmTypes.edm(dt);
		} else if (type != null) {
			// EClass used as a complex/entity type — qualified with ITS OWN package's
			// namespace, so cross-package references write correctly (AP-6)
			base = typeNamespace(type, ns) + "." + type.getName();
		} else {
			base = "Edm.String";
		}
		return collection(base, many);
	}

	/** The schema namespace of the classifier's OWN package (override-aware), {@code ns} as fallback. */
	private String typeNamespace(EClassifier type, String ns) {
		EPackage pkg = type.getEPackage();
		if (pkg == null) {
			return ns;
		}
		return annOr(pkg, ODataAnnotationConstants.NAMESPACE, namespace(pkg));
	}

	private static String collection(String base, boolean many) {
		return many ? EdmTypes.COLLECTION_OPEN + base + ")" : base;
	}

	private String namespace(EPackage pkg) {
		String n = pkg.getName();
		return (n == null || n.isBlank()) ? "Default" : n;
	}

	// --- @OData.* annotation access (single source, string details) ---

	private static String ann(EModelElement element, String key) {
		EAnnotation a = element.getEAnnotation(ODataAnnotationConstants.SOURCE);
		return a == null ? null : a.getDetails().get(key);
	}

	private static boolean annFlag(EModelElement element, String key) {
		return Boolean.parseBoolean(ann(element, key));
	}

	private static String annOr(EModelElement element, String key, String fallback) {
		String v = ann(element, key);
		return v != null ? v : fallback;
	}

	private static boolean annOr(EModelElement element, String key, boolean fallback) {
		String v = ann(element, key);
		return v != null ? Boolean.parseBoolean(v) : fallback;
	}

	/** Operation kind from the (lenient) annotation override, falling back by return type. */
	private static ODataOperationKind operationKind(String override, boolean noReturnType) {
		ODataOperationKind byReturn = noReturnType ? ODataOperationKind.ACTION : ODataOperationKind.FUNCTION;
		if (override == null) {
			return byReturn;
		}
		try {
			return ODataOperationKind.valueOf(override.toUpperCase(java.util.Locale.ROOT));
		} catch (IllegalArgumentException e) {
			return byReturn; // malformed override → fall back, resolver stays lenient
		}
	}

	private static void annInt(EModelElement element, String key, java.util.function.IntConsumer setter) {
		String v = ann(element, key);
		if (v != null) {
			try {
				setter.accept(Integer.parseInt(v.trim()));
			} catch (NumberFormatException e) {
				// the resolver is lenient: a malformed developer-authored annotation is skipped,
				// not fatal to resolving the rest of the model
			}
		}
	}
}
