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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EPackage;
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
import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TAction;
import org.open.oasis.docs.odata.ns.edm.TActionFunctionParameter;
import org.open.oasis.docs.odata.ns.edm.TActionFunctionReturnType;
import org.open.oasis.docs.odata.ns.edm.TComplexType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TSingleton;
import org.open.oasis.docs.odata.ns.edm.TEntityKeyElement;
import org.open.oasis.docs.odata.ns.edm.TEntitySet;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TEnumType;
import org.open.oasis.docs.odata.ns.edm.TEnumTypeMember;
import org.open.oasis.docs.odata.ns.edm.TActionImport;
import org.open.oasis.docs.odata.ns.edm.TFunction;
import org.open.oasis.docs.odata.ns.edm.TFunctionImport;
import org.open.oasis.docs.odata.ns.edm.TNavigationProperty;
import org.open.oasis.docs.odata.ns.edm.TNavigationPropertyBinding;
import org.open.oasis.docs.odata.ns.edm.TProperty;
import org.open.oasis.docs.odata.ns.edm.TVariable;
import org.open.oasis.docs.odata.ns.edm.TPropertyRef;
import org.open.oasis.docs.odata.ns.edm.TReferentialConstraint;
import org.open.oasis.docs.odata.ns.edmx.EdmxFactory;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;
import org.open.oasis.docs.odata.ns.edmx.TInclude;
import org.open.oasis.docs.odata.ns.edmx.TReference;
import org.open.oasis.docs.odata.ns.edmx.TVersion;

/**
 * Step 2 of the CSDL conversion (ADR-0003): serialize a resolved {@link ODataPackageProfile}
 * (produced by {@link OdataResolver}) into an OASIS EDM/EDMX model instance — the write half of
 * the CSDL bridge ({@link EdmToEcoreConverter} is the inverse). This builder is a thin, Ecore-free
 * mapper: ODataClassProfile → EntityType/ComplexType, ODataPropertyProfile → Property,
 * ODataNavigationProfile → NavigationProperty, ODataEnumProfile → EnumType, ODataOperationProfile
 * → bound Function/Action, plus a default EntityContainer with one EntitySet per concrete entity.
 *
 * <p>The {@code toSchema(EPackage)}/{@code toEdmx(EPackage)} convenience overloads chain
 * resolve→build so callers (and the round-trip tests) keep a one-shot API.
 *
 * <p>Open work packages tracked in {@code docs/odata-e2-converter-open-points.md}
 * (annotations, navigation bindings, facets, cross-package references, unbound operations, …).
 */
public class EcoreToEdmConverter {

	/** OASIS Core vocabulary (AP-5): terms referenced by emitted {@code <Annotation>} elements. */
	private static final String CORE_NAMESPACE = "Org.OData.Core.V1";
	private static final String CORE_ALIAS = "Core";
	private static final String CORE_VOCABULARY_URI =
			"https://docs.oasis-open.org/odata/odata/v4.0/errata03/csd01/complete/vocabularies/Org.OData.Core.V1.xml";
	private static final String CORE_COMPUTED = CORE_NAMESPACE + ".Computed";
	private static final String CORE_IMMUTABLE = CORE_NAMESPACE + ".Immutable";

	private final EdmFactory edm = EdmFactory.eINSTANCE;
	private final EdmxFactory edmx = EdmxFactory.eINSTANCE;

	// --- convenience: resolve (step 1) + build (step 2) in one call ---

	/** Build a full {@code <edmx:Edmx Version="4.0">} document for the package. */
	public EdmxRoot toEdmx(EPackage pkg) {
		ODataPackageProfile profile = new OdataResolver().resolve(pkg);
		EdmxRoot root = toEdmx(profile);
		// container-level singletons/set names are driven by EPackage annotations, not the profile
		root.getEdmx().getDataServices().getSchema()
				.forEach(schema -> {
					addSingletons(pkg, profile, schema);
					renameEntitySets(pkg, schema);
				});
		return root;
	}

	/** Build the {@code <Schema>} for the package. */
	public SchemaType toSchema(EPackage pkg) {
		ODataPackageProfile profile = new OdataResolver().resolve(pkg);
		SchemaType schema = toSchema(profile);
		addSingletons(pkg, profile, schema);
		renameEntitySets(pkg, schema);
		return schema;
	}

	/**
	 * Applies container entity-set NAMES from the {@code EPackage} annotation
	 * ({@link ODataAnnotationConstants#ENTITY_SETS_SOURCE}, {@code setName -> EClass name}): the
	 * default convention names a set after its type; a service whose sets differ (TripPin
	 * {@code People -> Person}) keeps its names through the round trip. Navigation-binding targets
	 * reference set names, so they are renamed alongside.
	 */
	private void renameEntitySets(EPackage pkg, SchemaType schema) {
		applyEntitySetNames(entitySetNames(pkg), schema);
	}

	/** The {@code type name -> set name} renames declared on the package, or an empty map. */
	public static java.util.Map<String, String> entitySetNames(EPackage pkg) {
		EAnnotation annotation = pkg.getEAnnotation(ODataAnnotationConstants.ENTITY_SETS_SOURCE);
		java.util.Map<String, String> typeToSet = new java.util.HashMap<>();
		if (annotation != null) {
			annotation.getDetails().forEach(entry -> typeToSet.put(entry.getValue(), entry.getKey()));
		}
		return typeToSet;
	}

	/**
	 * Applies {@code type name -> set name} renames to the schema's containers (sets and their
	 * binding targets). Public because the runtime applies the renames of ALL registered packages
	 * to every schema — the container may live in a different schema than its types (Northwind).
	 * Idempotent: an already-renamed set no longer matches a type-name key.
	 */
	public void applyEntitySetNames(java.util.Map<String, String> typeToSet, SchemaType schema) {
		if (typeToSet.isEmpty()) {
			return;
		}
		for (TEntityContainer container : schema.getEntityContainer()) {
			for (TEntitySet set : container.getEntitySet()) {
				String renamed = typeToSet.get(set.getName());
				if (renamed != null) {
					set.setName(renamed);
				}
			}
			for (TEntitySet set : container.getEntitySet()) {
				set.getNavigationPropertyBinding().forEach(binding -> {
					String renamed = typeToSet.get(String.valueOf(binding.getTarget()));
					if (renamed != null) {
						binding.setTarget(renamed);
					}
				});
			}
		}
	}

	/**
	 * Emits container-level {@code <Singleton>}s from the {@code EPackage} annotation
	 * ({@link ODataAnnotationConstants#SINGLETONS_SOURCE}, {@code name -> EClass name}); the type is
	 * qualified with the schema namespace ([OData-CSDL] 13.5).
	 */
	private void addSingletons(EPackage pkg, ODataPackageProfile profile, SchemaType schema) {
		EAnnotation annotation = pkg.getEAnnotation(ODataAnnotationConstants.SINGLETONS_SOURCE);
		if (annotation == null || annotation.getDetails().isEmpty()) {
			return;
		}
		TEntityContainer container = schema.getEntityContainer().isEmpty()
				? null : schema.getEntityContainer().get(0);
		if (container == null) {
			container = edm.createTEntityContainer();
			container.setName(profile.getContainerName());
			schema.getEntityContainer().add(container);
		}
		for (String name : new java.util.ArrayList<>(annotation.getDetails().keySet())) {
			TSingleton singleton = edm.createTSingleton();
			singleton.setName(name);
			singleton.setType(profile.getNamespace() + "." + annotation.getDetails().get(name));
			container.getSingleton().add(singleton);
		}
	}

	// --- build (step 2): profile -> EDM, no Ecore access ---

	public EdmxRoot toEdmx(ODataPackageProfile profile) {
		EdmxRoot root = edmx.createEdmxRoot();
		TEdmx edmxElement = edmx.createTEdmx();
		edmxElement.setVersion(TVersion._40);
		if (usesCoreTerms(profile)) { // Reference must precede DataServices in the edmx sequence
			TInclude include = edmx.createTInclude();
			include.setNamespace(CORE_NAMESPACE);
			include.setAlias(CORE_ALIAS);
			TReference reference = edmx.createTReference();
			reference.setUri(CORE_VOCABULARY_URI);
			reference.getInclude().add(include);
			edmxElement.getReference().add(reference);
		}
		TDataServices dataServices = edmx.createTDataServices();
		dataServices.getSchema().add(toSchema(profile));
		edmxElement.setDataServices(dataServices);
		root.setEdmx(edmxElement);
		return root;
	}

	/** True when any property carries a Core-vocabulary term ({@code Computed}/{@code Immutable}). */
	private static boolean usesCoreTerms(ODataPackageProfile profile) {
		return profile.getClasses().stream()
				.flatMap(c -> c.getProperties().stream())
				.anyMatch(p -> p.isComputed() || p.isImmutable());
	}

	public SchemaType toSchema(ODataPackageProfile profile) {
		SchemaType schema = edm.createSchemaType();
		schema.setNamespace(profile.getNamespace());
		addAnnotations(profile.getAnnotations(), schema.getAnnotation());

		for (ODataEnumProfile en : profile.getEnums()) {
			schema.getEnumType().add(toEnum(en));
		}

		TEntityContainer container = edm.createTEntityContainer();
		container.setName(profile.getContainerName());

		for (ODataClassProfile c : profile.getClasses()) {
			if (c.getKind() == ODataTypeKind.ENTITY) {
				schema.getEntityType().add(toEntity(c));
				if (!c.isAbstract()) { // abstract entity types are inherited, never exposed as a set
					TEntitySet set = edm.createTEntitySet();
					set.setName(c.getName());
					set.setEntityType(c.getQualifiedName());
					container.getEntitySet().add(set);
				}
			} else {
				schema.getComplexType().add(toComplex(c));
			}
			addOperations(c, schema, container);
		}

		addNavigationBindings(profile, container);

		if (!container.getEntitySet().isEmpty() || !container.getActionImport().isEmpty()
				|| !container.getFunctionImport().isEmpty()) {
			schema.getEntityContainer().add(container);
		}
		return schema;
	}

	/**
	 * Container-level {@code NavigationPropertyBinding}s (AP-3): each entity set binds every
	 * (declared or inherited) non-containment navigation whose target type has an entity set in
	 * this container. Containment navigations need no binding (contained entities are addressed
	 * through their parent); targets without a set (abstract types, foreign schemas — AP-6) are
	 * skipped.
	 */
	private void addNavigationBindings(ODataPackageProfile profile, TEntityContainer container) {
		Map<String, ODataClassProfile> byQualifiedName = new HashMap<>();
		profile.getClasses().forEach(c -> byQualifiedName.put(c.getQualifiedName(), c));
		Map<String, TEntitySet> setByEntityType = new HashMap<>();
		container.getEntitySet().forEach(s -> setByEntityType.put(s.getEntityType(), s));

		for (TEntitySet set : container.getEntitySet()) {
			java.util.Set<String> visited = new java.util.HashSet<>();
			for (ODataClassProfile c = byQualifiedName.get(set.getEntityType());
					c != null && visited.add(c.getQualifiedName());
					c = byQualifiedName.get(c.getBaseTypeQualifiedName())) {
				for (ODataNavigationProfile n : c.getNavigationProperties()) {
					if (n.isContainsTarget()) {
						continue;
					}
					TEntitySet target = setByEntityType.get(unwrapCollection(n.getTypeName()));
					if (target == null) {
						continue;
					}
					TNavigationPropertyBinding binding = edm.createTNavigationPropertyBinding();
					binding.setPath(n.getName());
					binding.setTarget(target.getName());
					set.getNavigationPropertyBinding().add(binding);
				}
			}
		}
	}

	private TEntityType toEntity(ODataClassProfile c) {
		TEntityType t = edm.createTEntityType();
		t.setName(c.getName());
		if (c.isAbstract()) {
			t.setAbstract(true);
		}
		if (c.isOpenType()) {
			t.setOpenType(true);
		}
		if (c.isHasStream()) {
			t.setHasStream(true);
		}
		baseType(c, t::setBaseType);
		addProperties(c, t.getProperty());
		addNavigation(c, t.getNavigationProperty());
		addAnnotations(c.getAnnotations(), t.getAnnotation());
		if (!c.getKeyPropertyNames().isEmpty()) {
			TEntityKeyElement key = edm.createTEntityKeyElement();
			for (String name : c.getKeyPropertyNames()) {
				TPropertyRef ref = edm.createTPropertyRef();
				ref.setName(name);
				key.getPropertyRef().add(ref);
			}
			t.getKey().add(key);
		}
		return t;
	}

	private TComplexType toComplex(ODataClassProfile c) {
		TComplexType t = edm.createTComplexType();
		t.setName(c.getName());
		if (c.isAbstract()) {
			t.setAbstract(true);
		}
		if (c.isOpenType()) {
			t.setOpenType(true);
		}
		baseType(c, t::setBaseType);
		addProperties(c, t.getProperty());
		addNavigation(c, t.getNavigationProperty());
		addAnnotations(c.getAnnotations(), t.getAnnotation());
		return t;
	}

	private void addProperties(ODataClassProfile c, List<TProperty> out) {
		for (ODataPropertyProfile p : c.getProperties()) {
			TProperty tp = edm.createTProperty();
			tp.setName(p.getName());
			tp.setType(p.getTypeName());
			tp.setNullable(p.isNullable());
			if (p.getMaxLength() >= 0) {
				tp.setMaxLength(BigInteger.valueOf(p.getMaxLength()));
			}
			if (p.getPrecision() >= 0) {
				tp.setPrecision(BigInteger.valueOf(p.getPrecision()));
			}
			if (p.getScale() >= 0) {
				tp.setScale(BigInteger.valueOf(p.getScale()));
			}
			if (p.getDefaultValue() != null) {
				tp.setDefaultValue(p.getDefaultValue());
			}
			if (p.getSrid() != null) {
				// the model's union takes the TVariable enumerator or a number, not raw text
				if ("variable".equalsIgnoreCase(p.getSrid())) {
					tp.setSRID(TVariable.VARIABLE);
				} else {
					try {
						tp.setSRID(new BigInteger(p.getSrid().trim()));
					} catch (NumberFormatException e) {
						// lenient like the annotation resolver: malformed SRID is skipped
					}
				}
			}
			if (p.getUnicode() != null) {
				tp.setUnicode(p.getUnicode());
			}
			if (p.isComputed()) {
				tp.getAnnotation().add(coreAnnotation(CORE_COMPUTED));
			}
			if (p.isImmutable()) {
				tp.getAnnotation().add(coreAnnotation(CORE_IMMUTABLE));
			}
			addAnnotations(p.getAnnotations(), tp.getAnnotation());
			out.add(tp);
		}
	}

	private AnnotationType coreAnnotation(String term) {
		AnnotationType a = edm.createAnnotationType();
		a.setTerm(term);
		a.setBool1(true);
		return a;
	}

	/**
	 * Generic vocabulary-term annotations (AP-5). Rich expressions (Record/Collection/path
	 * forms/EnumMember) arrive as their [OData-CSDL-JSON] value encoding in the detail string
	 * and become the corresponding EDM expression tree; plain constants keep the lexical
	 * typing: {@code true|false} → Bool, integral → Int, decimal → Decimal, else String.
	 */
	private void addAnnotations(List<ODataAnnotation> annotations, List<AnnotationType> out) {
		for (ODataAnnotation annotation : annotations) {
			AnnotationType a = edm.createAnnotationType();
			a.setTerm(annotation.getTerm());
			String value = annotation.getValue() == null ? "" : annotation.getValue();
			if (JacksonSupport.PRESENT && CsdlAnnotationExpressions.isRich(value)
					&& CsdlAnnotationExpressions.apply(value, a)) {
				out.add(a);
				continue;
			}
			if ("true".equals(value) || "false".equals(value)) {
				a.setBool1(Boolean.parseBoolean(value));
			} else if (value.matches("[+-]?\\d+")) {
				a.setInt1(new BigInteger(value));
			} else if (value.matches("[+-]?\\d+\\.\\d+")) {
				a.setDecimal1(value);
			} else {
				a.setString1(value);
			}
			out.add(a);
		}
	}

	private void addNavigation(ODataClassProfile c, List<TNavigationProperty> out) {
		for (ODataNavigationProfile n : c.getNavigationProperties()) {
			TNavigationProperty tn = edm.createTNavigationProperty();
			tn.setName(n.getName());
			tn.setType(n.getTypeName());
			tn.setContainsTarget(n.isContainsTarget());
			if (!isCollection(n.getTypeName())) { // Nullable is not permitted on collection navigation
				tn.setNullable(n.isNullable());
			}
			if (n.getPartner() != null) {
				tn.setPartner(n.getPartner());
			}
			for (ODataReferentialConstraint rc : n.getReferentialConstraints()) {
				TReferentialConstraint tc = edm.createTReferentialConstraint();
				tc.setProperty(rc.getProperty());
				tc.setReferencedProperty(rc.getReferencedProperty());
				tn.getReferentialConstraint().add(tc);
			}
			addAnnotations(n.getAnnotations(), tn.getAnnotation());
			out.add(tn);
		}
	}

	private void addOperations(ODataClassProfile c, SchemaType schema, TEntityContainer container) {
		String qualified = schema.getNamespace() + ".";
		for (ODataOperationProfile op : c.getOperations()) {
			if (op.getKind() == ODataOperationKind.ACTION) {
				TAction a = edm.createTAction();
				a.setName(op.getName());
				a.setIsBound(op.isBound());
				addParameters(op, a.getParameter());
				if (op.getReturnTypeName() != null) {
					a.getReturnType().add(returnType(op));
				}
				if (op.getEntitySetPath() != null) {
					a.setEntitySetPath(op.getEntitySetPath());
				}
				schema.getAction().add(a);
				if (!op.isBound()) { // unbound operations are reachable via a container import
					TActionImport imp = edm.createTActionImport();
					imp.setName(op.getName());
					imp.setAction(qualified + op.getName());
					container.getActionImport().add(imp);
				}
			} else {
				TFunction f = edm.createTFunction();
				f.setName(op.getName());
				f.setIsBound(op.isBound());
				if (op.isComposable()) {
					f.setIsComposable(true);
				}
				addParameters(op, f.getParameter());
				f.setReturnType(returnType(op)); // a Function must declare a ReturnType
				if (op.getEntitySetPath() != null) {
					f.setEntitySetPath(op.getEntitySetPath());
				}
				schema.getFunction().add(f);
				if (!op.isBound()) {
					TFunctionImport imp = edm.createTFunctionImport();
					imp.setName(op.getName());
					imp.setFunction(qualified + op.getName());
					container.getFunctionImport().add(imp);
				}
			}
		}
	}

	private TActionFunctionReturnType returnType(ODataOperationProfile op) {
		TActionFunctionReturnType rt = edm.createTActionFunctionReturnType();
		String type = op.getReturnTypeName() != null ? op.getReturnTypeName() : "Edm.String";
		rt.setType(type);
		if (!isCollection(type)) {
			rt.setNullable(op.isReturnNullable());
		}
		return rt;
	}

	private void addParameters(ODataOperationProfile op, List<TActionFunctionParameter> out) {
		if (op.isBound() && op.getBindingTypeName() != null) {
			TActionFunctionParameter bp = edm.createTActionFunctionParameter();
			bp.setName("bindingParameter"); // the binding parameter must be first
			bp.setType(op.getBindingTypeName());
			out.add(bp);
		}
		for (ODataParameterProfile p : op.getParameters()) {
			TActionFunctionParameter tp = edm.createTActionFunctionParameter();
			tp.setName(p.getName());
			tp.setType(p.getTypeName());
			if (!isCollection(p.getTypeName())) {
				tp.setNullable(p.isNullable());
			}
			out.add(tp);
		}
	}

	private TEnumType toEnum(ODataEnumProfile en) {
		TEnumType t = edm.createTEnumType();
		t.setName(en.getName());
		for (ODataEnumMember m : en.getMembers()) {
			TEnumTypeMember tm = edm.createTEnumTypeMember();
			tm.setName(m.getName());
			tm.setValue(m.getValue());
			t.getMember().add(tm);
		}
		return t;
	}

	private void baseType(ODataClassProfile c, java.util.function.Consumer<String> setter) {
		if (c.getBaseTypeQualifiedName() != null && !c.getBaseTypeQualifiedName().isBlank()) {
			setter.accept(c.getBaseTypeQualifiedName());
		}
	}

	private static boolean isCollection(String typeName) {
		return typeName != null && typeName.startsWith(EdmTypes.COLLECTION_OPEN);
	}

	private static String unwrapCollection(String typeName) {
		return isCollection(typeName)
				? typeName.substring(EdmTypes.COLLECTION_OPEN.length(), typeName.length() - 1)
				: typeName;
	}

	/** An EClass becomes an OData entity type iff it declares (or inherits) an {@code ID} attribute. */
	static boolean isEntity(EClass cl) {
		return cl.getEAllAttributes().stream().anyMatch(EAttribute::isID);
	}
}
