/**
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
package org.eclipse.fennec.odata.metadata.odata.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.metadata.MetadataPackage;

import org.eclipse.fennec.odata.csdl.profile.ProfilePackage;

import org.eclipse.fennec.odata.metadata.odata.ClassODataAspect;
import org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect;
import org.eclipse.fennec.odata.metadata.odata.ODataClassProfile;
import org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile;
import org.eclipse.fennec.odata.metadata.odata.ODataTypeKind;
import org.eclipse.fennec.odata.metadata.odata.OdataFactory;
import org.eclipse.fennec.odata.metadata.odata.OdataPackage;
import org.eclipse.fennec.odata.metadata.odata.PackageODataAspect;
import org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class OdataPackageImpl extends EPackageImpl implements OdataPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass packageODataAspectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass classODataAspectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass featureODataAspectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass referenceODataAspectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oDataPackageProfileEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oDataClassProfileEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum oDataTypeKindEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private OdataPackageImpl() {
		super(eNS_URI, OdataFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link OdataPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static OdataPackage init() {
		if (isInited) return (OdataPackage)EPackage.Registry.INSTANCE.getEPackage(OdataPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredOdataPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		OdataPackageImpl theOdataPackage = registeredOdataPackage instanceof OdataPackageImpl ? (OdataPackageImpl)registeredOdataPackage : new OdataPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		MetadataPackage.eINSTANCE.eClass();
		ProfilePackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theOdataPackage.createPackageContents();

		// Initialize created meta-data
		theOdataPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theOdataPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(OdataPackage.eNS_URI, theOdataPackage);
		return theOdataPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPackageODataAspect() {
		return packageODataAspectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageODataAspect_Namespace() {
		return (EAttribute)packageODataAspectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPackageODataAspect_Alias() {
		return (EAttribute)packageODataAspectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getClassODataAspect() {
		return classODataAspectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClassODataAspect_Kind() {
		return (EAttribute)classODataAspectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClassODataAspect_HasStream() {
		return (EAttribute)classODataAspectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClassODataAspect_OpenType() {
		return (EAttribute)classODataAspectEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getClassODataAspect_Abstract() {
		return (EAttribute)classODataAspectEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFeatureODataAspect() {
		return featureODataAspectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureODataAspect_Key() {
		return (EAttribute)featureODataAspectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureODataAspect_Nullable() {
		return (EAttribute)featureODataAspectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureODataAspect_Computed() {
		return (EAttribute)featureODataAspectEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureODataAspect_Immutable() {
		return (EAttribute)featureODataAspectEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureODataAspect_EdmType() {
		return (EAttribute)featureODataAspectEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureODataAspect_MaxLength() {
		return (EAttribute)featureODataAspectEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureODataAspect_Precision() {
		return (EAttribute)featureODataAspectEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureODataAspect_Scale() {
		return (EAttribute)featureODataAspectEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureODataAspect_DefaultValue() {
		return (EAttribute)featureODataAspectEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getReferenceODataAspect() {
		return referenceODataAspectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceODataAspect_ContainsTarget() {
		return (EAttribute)referenceODataAspectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceODataAspect_Partner() {
		return (EAttribute)referenceODataAspectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getReferenceODataAspect_OnDelete() {
		return (EAttribute)referenceODataAspectEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getODataPackageProfile() {
		return oDataPackageProfileEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPackageProfile_Namespace() {
		return (EAttribute)oDataPackageProfileEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataPackageProfile_OdataProfile() {
		return (EReference)oDataPackageProfileEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getODataClassProfile() {
		return oDataClassProfileEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_Kind() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_QualifiedName() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_KeyPropertyNames() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_OpenType() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_HasStream() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_BaseTypeQualifiedName() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataClassProfile_OdataProfile() {
		return (EReference)oDataClassProfileEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getODataTypeKind() {
		return oDataTypeKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OdataFactory getOdataFactory() {
		return (OdataFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		packageODataAspectEClass = createEClass(PACKAGE_ODATA_ASPECT);
		createEAttribute(packageODataAspectEClass, PACKAGE_ODATA_ASPECT__NAMESPACE);
		createEAttribute(packageODataAspectEClass, PACKAGE_ODATA_ASPECT__ALIAS);

		classODataAspectEClass = createEClass(CLASS_ODATA_ASPECT);
		createEAttribute(classODataAspectEClass, CLASS_ODATA_ASPECT__KIND);
		createEAttribute(classODataAspectEClass, CLASS_ODATA_ASPECT__HAS_STREAM);
		createEAttribute(classODataAspectEClass, CLASS_ODATA_ASPECT__OPEN_TYPE);
		createEAttribute(classODataAspectEClass, CLASS_ODATA_ASPECT__ABSTRACT);

		featureODataAspectEClass = createEClass(FEATURE_ODATA_ASPECT);
		createEAttribute(featureODataAspectEClass, FEATURE_ODATA_ASPECT__KEY);
		createEAttribute(featureODataAspectEClass, FEATURE_ODATA_ASPECT__NULLABLE);
		createEAttribute(featureODataAspectEClass, FEATURE_ODATA_ASPECT__COMPUTED);
		createEAttribute(featureODataAspectEClass, FEATURE_ODATA_ASPECT__IMMUTABLE);
		createEAttribute(featureODataAspectEClass, FEATURE_ODATA_ASPECT__EDM_TYPE);
		createEAttribute(featureODataAspectEClass, FEATURE_ODATA_ASPECT__MAX_LENGTH);
		createEAttribute(featureODataAspectEClass, FEATURE_ODATA_ASPECT__PRECISION);
		createEAttribute(featureODataAspectEClass, FEATURE_ODATA_ASPECT__SCALE);
		createEAttribute(featureODataAspectEClass, FEATURE_ODATA_ASPECT__DEFAULT_VALUE);

		referenceODataAspectEClass = createEClass(REFERENCE_ODATA_ASPECT);
		createEAttribute(referenceODataAspectEClass, REFERENCE_ODATA_ASPECT__CONTAINS_TARGET);
		createEAttribute(referenceODataAspectEClass, REFERENCE_ODATA_ASPECT__PARTNER);
		createEAttribute(referenceODataAspectEClass, REFERENCE_ODATA_ASPECT__ON_DELETE);

		oDataPackageProfileEClass = createEClass(ODATA_PACKAGE_PROFILE);
		createEAttribute(oDataPackageProfileEClass, ODATA_PACKAGE_PROFILE__NAMESPACE);
		createEReference(oDataPackageProfileEClass, ODATA_PACKAGE_PROFILE__ODATA_PROFILE);

		oDataClassProfileEClass = createEClass(ODATA_CLASS_PROFILE);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__KIND);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__QUALIFIED_NAME);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__OPEN_TYPE);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__HAS_STREAM);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME);
		createEReference(oDataClassProfileEClass, ODATA_CLASS_PROFILE__ODATA_PROFILE);

		// Create enums
		oDataTypeKindEEnum = createEEnum(ODATA_TYPE_KIND);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		MetadataPackage theMetadataPackage = (MetadataPackage)EPackage.Registry.INSTANCE.getEPackage(MetadataPackage.eNS_URI);
		ProfilePackage theProfilePackage = (ProfilePackage)EPackage.Registry.INSTANCE.getEPackage(ProfilePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		packageODataAspectEClass.getESuperTypes().add(theMetadataPackage.getPackageAspect());
		classODataAspectEClass.getESuperTypes().add(theMetadataPackage.getClassAspect());
		featureODataAspectEClass.getESuperTypes().add(theMetadataPackage.getFeatureAspect());
		referenceODataAspectEClass.getESuperTypes().add(this.getFeatureODataAspect());
		oDataPackageProfileEClass.getESuperTypes().add(theMetadataPackage.getPackageProfile());
		oDataClassProfileEClass.getESuperTypes().add(theMetadataPackage.getClassProfile());

		// Initialize classes, features, and operations; add parameters
		initEClass(packageODataAspectEClass, PackageODataAspect.class, "PackageODataAspect", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPackageODataAspect_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, PackageODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPackageODataAspect_Alias(), ecorePackage.getEString(), "alias", null, 0, 1, PackageODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(classODataAspectEClass, ClassODataAspect.class, "ClassODataAspect", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getClassODataAspect_Kind(), this.getODataTypeKind(), "kind", null, 0, 1, ClassODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClassODataAspect_HasStream(), ecorePackage.getEBoolean(), "hasStream", null, 0, 1, ClassODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClassODataAspect_OpenType(), ecorePackage.getEBoolean(), "openType", null, 0, 1, ClassODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getClassODataAspect_Abstract(), ecorePackage.getEBoolean(), "abstract", null, 0, 1, ClassODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(featureODataAspectEClass, FeatureODataAspect.class, "FeatureODataAspect", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFeatureODataAspect_Key(), ecorePackage.getEBoolean(), "key", null, 0, 1, FeatureODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFeatureODataAspect_Nullable(), ecorePackage.getEBoolean(), "nullable", "true", 0, 1, FeatureODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFeatureODataAspect_Computed(), ecorePackage.getEBoolean(), "computed", null, 0, 1, FeatureODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFeatureODataAspect_Immutable(), ecorePackage.getEBoolean(), "immutable", null, 0, 1, FeatureODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFeatureODataAspect_EdmType(), ecorePackage.getEString(), "edmType", null, 0, 1, FeatureODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFeatureODataAspect_MaxLength(), ecorePackage.getEInt(), "maxLength", "-1", 0, 1, FeatureODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFeatureODataAspect_Precision(), ecorePackage.getEInt(), "precision", "-1", 0, 1, FeatureODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFeatureODataAspect_Scale(), ecorePackage.getEInt(), "scale", "-1", 0, 1, FeatureODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFeatureODataAspect_DefaultValue(), ecorePackage.getEString(), "defaultValue", null, 0, 1, FeatureODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(referenceODataAspectEClass, ReferenceODataAspect.class, "ReferenceODataAspect", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getReferenceODataAspect_ContainsTarget(), ecorePackage.getEBoolean(), "containsTarget", null, 0, 1, ReferenceODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReferenceODataAspect_Partner(), ecorePackage.getEString(), "partner", null, 0, 1, ReferenceODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getReferenceODataAspect_OnDelete(), ecorePackage.getEString(), "onDelete", null, 0, 1, ReferenceODataAspect.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataPackageProfileEClass, ODataPackageProfile.class, "ODataPackageProfile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataPackageProfile_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, ODataPackageProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataPackageProfile_OdataProfile(), theProfilePackage.getODataPackageProfile(), null, "odataProfile", null, 0, 1, ODataPackageProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataClassProfileEClass, ODataClassProfile.class, "ODataClassProfile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataClassProfile_Kind(), this.getODataTypeKind(), "kind", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_QualifiedName(), ecorePackage.getEString(), "qualifiedName", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_KeyPropertyNames(), ecorePackage.getEString(), "keyPropertyNames", null, 0, -1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_OpenType(), ecorePackage.getEBoolean(), "openType", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_HasStream(), ecorePackage.getEBoolean(), "hasStream", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_BaseTypeQualifiedName(), ecorePackage.getEString(), "baseTypeQualifiedName", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataClassProfile_OdataProfile(), theProfilePackage.getODataClassProfile(), null, "odataProfile", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(oDataTypeKindEEnum, ODataTypeKind.class, "ODataTypeKind");
		addEEnumLiteral(oDataTypeKindEEnum, ODataTypeKind.ENTITY);
		addEEnumLiteral(oDataTypeKindEEnum, ODataTypeKind.COMPLEX);
		addEEnumLiteral(oDataTypeKindEEnum, ODataTypeKind.ENUM);

		// Create resource
		createResource(eNS_URI);
	}

} //OdataPackageImpl
