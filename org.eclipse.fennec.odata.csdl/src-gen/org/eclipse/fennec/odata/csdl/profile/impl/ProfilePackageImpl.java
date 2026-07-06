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
package org.eclipse.fennec.odata.csdl.profile.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

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
import org.eclipse.fennec.odata.csdl.profile.ProfilePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ProfilePackageImpl extends EPackageImpl implements ProfilePackage {
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
	private EClass oDataEnumProfileEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oDataEnumMemberEClass = null;

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
	private EClass oDataPropertyProfileEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oDataNavigationProfileEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oDataAnnotationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oDataReferentialConstraintEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oDataOperationProfileEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass oDataParameterProfileEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum oDataTypeKindEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum oDataOperationKindEEnum = null;

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
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ProfilePackageImpl() {
		super(eNS_URI, ProfileFactory.eINSTANCE);
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
	 * <p>This method is used to initialize {@link ProfilePackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ProfilePackage init() {
		if (isInited) return (ProfilePackage)EPackage.Registry.INSTANCE.getEPackage(ProfilePackage.eNS_URI);

		// Obtain or create and register package
		Object registeredProfilePackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ProfilePackageImpl theProfilePackage = registeredProfilePackage instanceof ProfilePackageImpl ? (ProfilePackageImpl)registeredProfilePackage : new ProfilePackageImpl();

		isInited = true;

		// Create package meta-data objects
		theProfilePackage.createPackageContents();

		// Initialize created meta-data
		theProfilePackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theProfilePackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ProfilePackage.eNS_URI, theProfilePackage);
		return theProfilePackage;
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
	public EAttribute getODataPackageProfile_Alias() {
		return (EAttribute)oDataPackageProfileEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPackageProfile_ContainerName() {
		return (EAttribute)oDataPackageProfileEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataPackageProfile_Enums() {
		return (EReference)oDataPackageProfileEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataPackageProfile_Classes() {
		return (EReference)oDataPackageProfileEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataPackageProfile_Annotations() {
		return (EReference)oDataPackageProfileEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getODataEnumProfile() {
		return oDataEnumProfileEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataEnumProfile_Name() {
		return (EAttribute)oDataEnumProfileEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataEnumProfile_Members() {
		return (EReference)oDataEnumProfileEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getODataEnumMember() {
		return oDataEnumMemberEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataEnumMember_Name() {
		return (EAttribute)oDataEnumMemberEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataEnumMember_Value() {
		return (EAttribute)oDataEnumMemberEClass.getEStructuralFeatures().get(1);
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
	public EAttribute getODataClassProfile_Name() {
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
	public EAttribute getODataClassProfile_Kind() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_Abstract() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_OpenType() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_HasStream() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_BaseTypeQualifiedName() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataClassProfile_KeyPropertyNames() {
		return (EAttribute)oDataClassProfileEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataClassProfile_Properties() {
		return (EReference)oDataClassProfileEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataClassProfile_NavigationProperties() {
		return (EReference)oDataClassProfileEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataClassProfile_Operations() {
		return (EReference)oDataClassProfileEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataClassProfile_Annotations() {
		return (EReference)oDataClassProfileEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getODataPropertyProfile() {
		return oDataPropertyProfileEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPropertyProfile_Name() {
		return (EAttribute)oDataPropertyProfileEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPropertyProfile_TypeName() {
		return (EAttribute)oDataPropertyProfileEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPropertyProfile_Nullable() {
		return (EAttribute)oDataPropertyProfileEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPropertyProfile_Computed() {
		return (EAttribute)oDataPropertyProfileEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPropertyProfile_Immutable() {
		return (EAttribute)oDataPropertyProfileEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPropertyProfile_MaxLength() {
		return (EAttribute)oDataPropertyProfileEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPropertyProfile_Precision() {
		return (EAttribute)oDataPropertyProfileEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPropertyProfile_Scale() {
		return (EAttribute)oDataPropertyProfileEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataPropertyProfile_DefaultValue() {
		return (EAttribute)oDataPropertyProfileEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataPropertyProfile_Annotations() {
		return (EReference)oDataPropertyProfileEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getODataNavigationProfile() {
		return oDataNavigationProfileEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataNavigationProfile_Name() {
		return (EAttribute)oDataNavigationProfileEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataNavigationProfile_TypeName() {
		return (EAttribute)oDataNavigationProfileEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataNavigationProfile_Nullable() {
		return (EAttribute)oDataNavigationProfileEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataNavigationProfile_ContainsTarget() {
		return (EAttribute)oDataNavigationProfileEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataNavigationProfile_Partner() {
		return (EAttribute)oDataNavigationProfileEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataNavigationProfile_OnDelete() {
		return (EAttribute)oDataNavigationProfileEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataNavigationProfile_ReferentialConstraints() {
		return (EReference)oDataNavigationProfileEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataNavigationProfile_Annotations() {
		return (EReference)oDataNavigationProfileEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getODataAnnotation() {
		return oDataAnnotationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataAnnotation_Term() {
		return (EAttribute)oDataAnnotationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataAnnotation_Value() {
		return (EAttribute)oDataAnnotationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getODataReferentialConstraint() {
		return oDataReferentialConstraintEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataReferentialConstraint_Property() {
		return (EAttribute)oDataReferentialConstraintEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataReferentialConstraint_ReferencedProperty() {
		return (EAttribute)oDataReferentialConstraintEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getODataOperationProfile() {
		return oDataOperationProfileEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataOperationProfile_Name() {
		return (EAttribute)oDataOperationProfileEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataOperationProfile_Kind() {
		return (EAttribute)oDataOperationProfileEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataOperationProfile_Bound() {
		return (EAttribute)oDataOperationProfileEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataOperationProfile_Composable() {
		return (EAttribute)oDataOperationProfileEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataOperationProfile_BindingTypeName() {
		return (EAttribute)oDataOperationProfileEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataOperationProfile_ReturnTypeName() {
		return (EAttribute)oDataOperationProfileEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataOperationProfile_ReturnNullable() {
		return (EAttribute)oDataOperationProfileEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataOperationProfile_EntitySetPath() {
		return (EAttribute)oDataOperationProfileEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getODataOperationProfile_Parameters() {
		return (EReference)oDataOperationProfileEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getODataParameterProfile() {
		return oDataParameterProfileEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataParameterProfile_Name() {
		return (EAttribute)oDataParameterProfileEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataParameterProfile_TypeName() {
		return (EAttribute)oDataParameterProfileEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getODataParameterProfile_Nullable() {
		return (EAttribute)oDataParameterProfileEClass.getEStructuralFeatures().get(2);
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
	public EEnum getODataOperationKind() {
		return oDataOperationKindEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ProfileFactory getProfileFactory() {
		return (ProfileFactory)getEFactoryInstance();
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
		oDataPackageProfileEClass = createEClass(ODATA_PACKAGE_PROFILE);
		createEAttribute(oDataPackageProfileEClass, ODATA_PACKAGE_PROFILE__NAMESPACE);
		createEAttribute(oDataPackageProfileEClass, ODATA_PACKAGE_PROFILE__ALIAS);
		createEAttribute(oDataPackageProfileEClass, ODATA_PACKAGE_PROFILE__CONTAINER_NAME);
		createEReference(oDataPackageProfileEClass, ODATA_PACKAGE_PROFILE__ENUMS);
		createEReference(oDataPackageProfileEClass, ODATA_PACKAGE_PROFILE__CLASSES);
		createEReference(oDataPackageProfileEClass, ODATA_PACKAGE_PROFILE__ANNOTATIONS);

		oDataEnumProfileEClass = createEClass(ODATA_ENUM_PROFILE);
		createEAttribute(oDataEnumProfileEClass, ODATA_ENUM_PROFILE__NAME);
		createEReference(oDataEnumProfileEClass, ODATA_ENUM_PROFILE__MEMBERS);

		oDataEnumMemberEClass = createEClass(ODATA_ENUM_MEMBER);
		createEAttribute(oDataEnumMemberEClass, ODATA_ENUM_MEMBER__NAME);
		createEAttribute(oDataEnumMemberEClass, ODATA_ENUM_MEMBER__VALUE);

		oDataClassProfileEClass = createEClass(ODATA_CLASS_PROFILE);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__NAME);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__QUALIFIED_NAME);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__KIND);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__ABSTRACT);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__OPEN_TYPE);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__HAS_STREAM);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME);
		createEAttribute(oDataClassProfileEClass, ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES);
		createEReference(oDataClassProfileEClass, ODATA_CLASS_PROFILE__PROPERTIES);
		createEReference(oDataClassProfileEClass, ODATA_CLASS_PROFILE__NAVIGATION_PROPERTIES);
		createEReference(oDataClassProfileEClass, ODATA_CLASS_PROFILE__OPERATIONS);
		createEReference(oDataClassProfileEClass, ODATA_CLASS_PROFILE__ANNOTATIONS);

		oDataPropertyProfileEClass = createEClass(ODATA_PROPERTY_PROFILE);
		createEAttribute(oDataPropertyProfileEClass, ODATA_PROPERTY_PROFILE__NAME);
		createEAttribute(oDataPropertyProfileEClass, ODATA_PROPERTY_PROFILE__TYPE_NAME);
		createEAttribute(oDataPropertyProfileEClass, ODATA_PROPERTY_PROFILE__NULLABLE);
		createEAttribute(oDataPropertyProfileEClass, ODATA_PROPERTY_PROFILE__COMPUTED);
		createEAttribute(oDataPropertyProfileEClass, ODATA_PROPERTY_PROFILE__IMMUTABLE);
		createEAttribute(oDataPropertyProfileEClass, ODATA_PROPERTY_PROFILE__MAX_LENGTH);
		createEAttribute(oDataPropertyProfileEClass, ODATA_PROPERTY_PROFILE__PRECISION);
		createEAttribute(oDataPropertyProfileEClass, ODATA_PROPERTY_PROFILE__SCALE);
		createEAttribute(oDataPropertyProfileEClass, ODATA_PROPERTY_PROFILE__DEFAULT_VALUE);
		createEReference(oDataPropertyProfileEClass, ODATA_PROPERTY_PROFILE__ANNOTATIONS);

		oDataNavigationProfileEClass = createEClass(ODATA_NAVIGATION_PROFILE);
		createEAttribute(oDataNavigationProfileEClass, ODATA_NAVIGATION_PROFILE__NAME);
		createEAttribute(oDataNavigationProfileEClass, ODATA_NAVIGATION_PROFILE__TYPE_NAME);
		createEAttribute(oDataNavigationProfileEClass, ODATA_NAVIGATION_PROFILE__NULLABLE);
		createEAttribute(oDataNavigationProfileEClass, ODATA_NAVIGATION_PROFILE__CONTAINS_TARGET);
		createEAttribute(oDataNavigationProfileEClass, ODATA_NAVIGATION_PROFILE__PARTNER);
		createEAttribute(oDataNavigationProfileEClass, ODATA_NAVIGATION_PROFILE__ON_DELETE);
		createEReference(oDataNavigationProfileEClass, ODATA_NAVIGATION_PROFILE__REFERENTIAL_CONSTRAINTS);
		createEReference(oDataNavigationProfileEClass, ODATA_NAVIGATION_PROFILE__ANNOTATIONS);

		oDataAnnotationEClass = createEClass(ODATA_ANNOTATION);
		createEAttribute(oDataAnnotationEClass, ODATA_ANNOTATION__TERM);
		createEAttribute(oDataAnnotationEClass, ODATA_ANNOTATION__VALUE);

		oDataReferentialConstraintEClass = createEClass(ODATA_REFERENTIAL_CONSTRAINT);
		createEAttribute(oDataReferentialConstraintEClass, ODATA_REFERENTIAL_CONSTRAINT__PROPERTY);
		createEAttribute(oDataReferentialConstraintEClass, ODATA_REFERENTIAL_CONSTRAINT__REFERENCED_PROPERTY);

		oDataOperationProfileEClass = createEClass(ODATA_OPERATION_PROFILE);
		createEAttribute(oDataOperationProfileEClass, ODATA_OPERATION_PROFILE__NAME);
		createEAttribute(oDataOperationProfileEClass, ODATA_OPERATION_PROFILE__KIND);
		createEAttribute(oDataOperationProfileEClass, ODATA_OPERATION_PROFILE__BOUND);
		createEAttribute(oDataOperationProfileEClass, ODATA_OPERATION_PROFILE__COMPOSABLE);
		createEAttribute(oDataOperationProfileEClass, ODATA_OPERATION_PROFILE__BINDING_TYPE_NAME);
		createEAttribute(oDataOperationProfileEClass, ODATA_OPERATION_PROFILE__RETURN_TYPE_NAME);
		createEAttribute(oDataOperationProfileEClass, ODATA_OPERATION_PROFILE__RETURN_NULLABLE);
		createEAttribute(oDataOperationProfileEClass, ODATA_OPERATION_PROFILE__ENTITY_SET_PATH);
		createEReference(oDataOperationProfileEClass, ODATA_OPERATION_PROFILE__PARAMETERS);

		oDataParameterProfileEClass = createEClass(ODATA_PARAMETER_PROFILE);
		createEAttribute(oDataParameterProfileEClass, ODATA_PARAMETER_PROFILE__NAME);
		createEAttribute(oDataParameterProfileEClass, ODATA_PARAMETER_PROFILE__TYPE_NAME);
		createEAttribute(oDataParameterProfileEClass, ODATA_PARAMETER_PROFILE__NULLABLE);

		// Create enums
		oDataTypeKindEEnum = createEEnum(ODATA_TYPE_KIND);
		oDataOperationKindEEnum = createEEnum(ODATA_OPERATION_KIND);
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

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(oDataPackageProfileEClass, ODataPackageProfile.class, "ODataPackageProfile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataPackageProfile_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, ODataPackageProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataPackageProfile_Alias(), ecorePackage.getEString(), "alias", null, 0, 1, ODataPackageProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataPackageProfile_ContainerName(), ecorePackage.getEString(), "containerName", "DefaultContainer", 0, 1, ODataPackageProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataPackageProfile_Enums(), this.getODataEnumProfile(), null, "enums", null, 0, -1, ODataPackageProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataPackageProfile_Classes(), this.getODataClassProfile(), null, "classes", null, 0, -1, ODataPackageProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataPackageProfile_Annotations(), this.getODataAnnotation(), null, "annotations", null, 0, -1, ODataPackageProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataEnumProfileEClass, ODataEnumProfile.class, "ODataEnumProfile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataEnumProfile_Name(), ecorePackage.getEString(), "name", null, 0, 1, ODataEnumProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataEnumProfile_Members(), this.getODataEnumMember(), null, "members", null, 0, -1, ODataEnumProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataEnumMemberEClass, ODataEnumMember.class, "ODataEnumMember", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataEnumMember_Name(), ecorePackage.getEString(), "name", null, 0, 1, ODataEnumMember.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataEnumMember_Value(), ecorePackage.getELong(), "value", null, 0, 1, ODataEnumMember.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataClassProfileEClass, ODataClassProfile.class, "ODataClassProfile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataClassProfile_Name(), ecorePackage.getEString(), "name", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_QualifiedName(), ecorePackage.getEString(), "qualifiedName", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_Kind(), this.getODataTypeKind(), "kind", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_Abstract(), ecorePackage.getEBoolean(), "abstract", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_OpenType(), ecorePackage.getEBoolean(), "openType", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_HasStream(), ecorePackage.getEBoolean(), "hasStream", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_BaseTypeQualifiedName(), ecorePackage.getEString(), "baseTypeQualifiedName", null, 0, 1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataClassProfile_KeyPropertyNames(), ecorePackage.getEString(), "keyPropertyNames", null, 0, -1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataClassProfile_Properties(), this.getODataPropertyProfile(), null, "properties", null, 0, -1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataClassProfile_NavigationProperties(), this.getODataNavigationProfile(), null, "navigationProperties", null, 0, -1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataClassProfile_Operations(), this.getODataOperationProfile(), null, "operations", null, 0, -1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataClassProfile_Annotations(), this.getODataAnnotation(), null, "annotations", null, 0, -1, ODataClassProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataPropertyProfileEClass, ODataPropertyProfile.class, "ODataPropertyProfile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataPropertyProfile_Name(), ecorePackage.getEString(), "name", null, 0, 1, ODataPropertyProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataPropertyProfile_TypeName(), ecorePackage.getEString(), "typeName", null, 0, 1, ODataPropertyProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataPropertyProfile_Nullable(), ecorePackage.getEBoolean(), "nullable", "true", 0, 1, ODataPropertyProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataPropertyProfile_Computed(), ecorePackage.getEBoolean(), "computed", null, 0, 1, ODataPropertyProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataPropertyProfile_Immutable(), ecorePackage.getEBoolean(), "immutable", null, 0, 1, ODataPropertyProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataPropertyProfile_MaxLength(), ecorePackage.getEInt(), "maxLength", "-1", 0, 1, ODataPropertyProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataPropertyProfile_Precision(), ecorePackage.getEInt(), "precision", "-1", 0, 1, ODataPropertyProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataPropertyProfile_Scale(), ecorePackage.getEInt(), "scale", "-1", 0, 1, ODataPropertyProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataPropertyProfile_DefaultValue(), ecorePackage.getEString(), "defaultValue", null, 0, 1, ODataPropertyProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataPropertyProfile_Annotations(), this.getODataAnnotation(), null, "annotations", null, 0, -1, ODataPropertyProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataNavigationProfileEClass, ODataNavigationProfile.class, "ODataNavigationProfile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataNavigationProfile_Name(), ecorePackage.getEString(), "name", null, 0, 1, ODataNavigationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataNavigationProfile_TypeName(), ecorePackage.getEString(), "typeName", null, 0, 1, ODataNavigationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataNavigationProfile_Nullable(), ecorePackage.getEBoolean(), "nullable", "true", 0, 1, ODataNavigationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataNavigationProfile_ContainsTarget(), ecorePackage.getEBoolean(), "containsTarget", null, 0, 1, ODataNavigationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataNavigationProfile_Partner(), ecorePackage.getEString(), "partner", null, 0, 1, ODataNavigationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataNavigationProfile_OnDelete(), ecorePackage.getEString(), "onDelete", null, 0, 1, ODataNavigationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataNavigationProfile_ReferentialConstraints(), this.getODataReferentialConstraint(), null, "referentialConstraints", null, 0, -1, ODataNavigationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataNavigationProfile_Annotations(), this.getODataAnnotation(), null, "annotations", null, 0, -1, ODataNavigationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataAnnotationEClass, ODataAnnotation.class, "ODataAnnotation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataAnnotation_Term(), ecorePackage.getEString(), "term", null, 0, 1, ODataAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataAnnotation_Value(), ecorePackage.getEString(), "value", null, 0, 1, ODataAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataReferentialConstraintEClass, ODataReferentialConstraint.class, "ODataReferentialConstraint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataReferentialConstraint_Property(), ecorePackage.getEString(), "property", null, 0, 1, ODataReferentialConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataReferentialConstraint_ReferencedProperty(), ecorePackage.getEString(), "referencedProperty", null, 0, 1, ODataReferentialConstraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataOperationProfileEClass, ODataOperationProfile.class, "ODataOperationProfile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataOperationProfile_Name(), ecorePackage.getEString(), "name", null, 0, 1, ODataOperationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataOperationProfile_Kind(), this.getODataOperationKind(), "kind", null, 0, 1, ODataOperationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataOperationProfile_Bound(), ecorePackage.getEBoolean(), "bound", "true", 0, 1, ODataOperationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataOperationProfile_Composable(), ecorePackage.getEBoolean(), "composable", null, 0, 1, ODataOperationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataOperationProfile_BindingTypeName(), ecorePackage.getEString(), "bindingTypeName", null, 0, 1, ODataOperationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataOperationProfile_ReturnTypeName(), ecorePackage.getEString(), "returnTypeName", null, 0, 1, ODataOperationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataOperationProfile_ReturnNullable(), ecorePackage.getEBoolean(), "returnNullable", "true", 0, 1, ODataOperationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataOperationProfile_EntitySetPath(), ecorePackage.getEString(), "entitySetPath", null, 0, 1, ODataOperationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getODataOperationProfile_Parameters(), this.getODataParameterProfile(), null, "parameters", null, 0, -1, ODataOperationProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(oDataParameterProfileEClass, ODataParameterProfile.class, "ODataParameterProfile", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getODataParameterProfile_Name(), ecorePackage.getEString(), "name", null, 0, 1, ODataParameterProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataParameterProfile_TypeName(), ecorePackage.getEString(), "typeName", null, 0, 1, ODataParameterProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getODataParameterProfile_Nullable(), ecorePackage.getEBoolean(), "nullable", "true", 0, 1, ODataParameterProfile.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(oDataTypeKindEEnum, ODataTypeKind.class, "ODataTypeKind");
		addEEnumLiteral(oDataTypeKindEEnum, ODataTypeKind.ENTITY);
		addEEnumLiteral(oDataTypeKindEEnum, ODataTypeKind.COMPLEX);
		addEEnumLiteral(oDataTypeKindEEnum, ODataTypeKind.ENUM);

		initEEnum(oDataOperationKindEEnum, ODataOperationKind.class, "ODataOperationKind");
		addEEnumLiteral(oDataOperationKindEEnum, ODataOperationKind.FUNCTION);
		addEEnumLiteral(oDataOperationKindEEnum, ODataOperationKind.ACTION);

		// Create resource
		createResource(eNS_URI);
	}

} //ProfilePackageImpl
