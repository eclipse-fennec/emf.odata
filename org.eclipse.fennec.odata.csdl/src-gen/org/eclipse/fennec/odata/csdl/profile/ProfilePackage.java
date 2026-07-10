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
package org.eclipse.fennec.odata.csdl.profile;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Standalone, fully-resolved OData profile model owned by the CSDL converter (org.eclipse.fennec.odata.csdl). It is the precomputed artifact produced by resolving an EPackage (+ @OData.* / external config): the OdataResolver fills it, the EDM builder serializes it 1:1 to CSDL. Has NO dependency on the Model Metadata Service — the metadata layer (emf.odata.metadata) composes this model by containment (ADR-0003).
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.odata.csdl.profile.ProfileFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = ProfilePackage.eNS_URI, genModel = "/model/odata-profile.genmodel", genModelSourceLocations = {"model/odata-profile.genmodel","org.eclipse.fennec.odata.csdl/model/odata-profile.genmodel"}, ecore = "/model/odata-profile.ecore", ecoreSourceLocations = "/model/odata-profile.ecore")
public interface ProfilePackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "profile";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/fennec/odata/profile/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "odataprofile";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ProfilePackage eINSTANCE = org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPackageProfileImpl <em>OData Package Profile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataPackageProfileImpl
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataPackageProfile()
	 * @generated
	 */
	int ODATA_PACKAGE_PROFILE = 0;

	/**
	 * The feature id for the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE__NAMESPACE = 0;

	/**
	 * The feature id for the '<em><b>Alias</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE__ALIAS = 1;

	/**
	 * The feature id for the '<em><b>Container Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE__CONTAINER_NAME = 2;

	/**
	 * The feature id for the '<em><b>Enums</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE__ENUMS = 3;

	/**
	 * The feature id for the '<em><b>Classes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE__CLASSES = 4;

	/**
	 * The feature id for the '<em><b>Annotations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE__ANNOTATIONS = 5;

	/**
	 * The number of structural features of the '<em>OData Package Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>OData Package Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataEnumProfileImpl <em>OData Enum Profile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataEnumProfileImpl
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataEnumProfile()
	 * @generated
	 */
	int ODATA_ENUM_PROFILE = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ENUM_PROFILE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Members</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ENUM_PROFILE__MEMBERS = 1;

	/**
	 * The number of structural features of the '<em>OData Enum Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ENUM_PROFILE_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>OData Enum Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ENUM_PROFILE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataEnumMemberImpl <em>OData Enum Member</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataEnumMemberImpl
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataEnumMember()
	 * @generated
	 */
	int ODATA_ENUM_MEMBER = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ENUM_MEMBER__NAME = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ENUM_MEMBER__VALUE = 1;

	/**
	 * The number of structural features of the '<em>OData Enum Member</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ENUM_MEMBER_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>OData Enum Member</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ENUM_MEMBER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl <em>OData Class Profile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataClassProfile()
	 * @generated
	 */
	int ODATA_CLASS_PROFILE = 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Qualified Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__QUALIFIED_NAME = 1;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__KIND = 2;

	/**
	 * The feature id for the '<em><b>Abstract</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__ABSTRACT = 3;

	/**
	 * The feature id for the '<em><b>Open Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__OPEN_TYPE = 4;

	/**
	 * The feature id for the '<em><b>Has Stream</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__HAS_STREAM = 5;

	/**
	 * The feature id for the '<em><b>Base Type Qualified Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME = 6;

	/**
	 * The feature id for the '<em><b>Key Property Names</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES = 7;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__PROPERTIES = 8;

	/**
	 * The feature id for the '<em><b>Navigation Properties</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__NAVIGATION_PROPERTIES = 9;

	/**
	 * The feature id for the '<em><b>Operations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__OPERATIONS = 10;

	/**
	 * The feature id for the '<em><b>Annotations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__ANNOTATIONS = 11;

	/**
	 * The number of structural features of the '<em>OData Class Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE_FEATURE_COUNT = 12;

	/**
	 * The number of operations of the '<em>OData Class Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl <em>OData Property Profile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataPropertyProfile()
	 * @generated
	 */
	int ODATA_PROPERTY_PROFILE = 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__TYPE_NAME = 1;

	/**
	 * The feature id for the '<em><b>Nullable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__NULLABLE = 2;

	/**
	 * The feature id for the '<em><b>Computed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__COMPUTED = 3;

	/**
	 * The feature id for the '<em><b>Immutable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__IMMUTABLE = 4;

	/**
	 * The feature id for the '<em><b>Max Length</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__MAX_LENGTH = 5;

	/**
	 * The feature id for the '<em><b>Precision</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__PRECISION = 6;

	/**
	 * The feature id for the '<em><b>Scale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__SCALE = 7;

	/**
	 * The feature id for the '<em><b>Default Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__DEFAULT_VALUE = 8;

	/**
	 * The feature id for the '<em><b>Srid</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__SRID = 9;

	/**
	 * The feature id for the '<em><b>Unicode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__UNICODE = 10;

	/**
	 * The feature id for the '<em><b>Annotations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE__ANNOTATIONS = 11;

	/**
	 * The number of structural features of the '<em>OData Property Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE_FEATURE_COUNT = 12;

	/**
	 * The number of operations of the '<em>OData Property Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PROPERTY_PROFILE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl <em>OData Navigation Profile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataNavigationProfile()
	 * @generated
	 */
	int ODATA_NAVIGATION_PROFILE = 5;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_NAVIGATION_PROFILE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_NAVIGATION_PROFILE__TYPE_NAME = 1;

	/**
	 * The feature id for the '<em><b>Nullable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_NAVIGATION_PROFILE__NULLABLE = 2;

	/**
	 * The feature id for the '<em><b>Contains Target</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_NAVIGATION_PROFILE__CONTAINS_TARGET = 3;

	/**
	 * The feature id for the '<em><b>Partner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_NAVIGATION_PROFILE__PARTNER = 4;

	/**
	 * The feature id for the '<em><b>On Delete</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_NAVIGATION_PROFILE__ON_DELETE = 5;

	/**
	 * The feature id for the '<em><b>Referential Constraints</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_NAVIGATION_PROFILE__REFERENTIAL_CONSTRAINTS = 6;

	/**
	 * The feature id for the '<em><b>Annotations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_NAVIGATION_PROFILE__ANNOTATIONS = 7;

	/**
	 * The number of structural features of the '<em>OData Navigation Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_NAVIGATION_PROFILE_FEATURE_COUNT = 8;

	/**
	 * The number of operations of the '<em>OData Navigation Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_NAVIGATION_PROFILE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataAnnotationImpl <em>OData Annotation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataAnnotationImpl
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataAnnotation()
	 * @generated
	 */
	int ODATA_ANNOTATION = 6;

	/**
	 * The feature id for the '<em><b>Term</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ANNOTATION__TERM = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ANNOTATION__VALUE = 1;

	/**
	 * The number of structural features of the '<em>OData Annotation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ANNOTATION_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>OData Annotation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_ANNOTATION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataReferentialConstraintImpl <em>OData Referential Constraint</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataReferentialConstraintImpl
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataReferentialConstraint()
	 * @generated
	 */
	int ODATA_REFERENTIAL_CONSTRAINT = 7;

	/**
	 * The feature id for the '<em><b>Property</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_REFERENTIAL_CONSTRAINT__PROPERTY = 0;

	/**
	 * The feature id for the '<em><b>Referenced Property</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_REFERENTIAL_CONSTRAINT__REFERENCED_PROPERTY = 1;

	/**
	 * The number of structural features of the '<em>OData Referential Constraint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_REFERENTIAL_CONSTRAINT_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>OData Referential Constraint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_REFERENTIAL_CONSTRAINT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl <em>OData Operation Profile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataOperationProfile()
	 * @generated
	 */
	int ODATA_OPERATION_PROFILE = 8;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE__KIND = 1;

	/**
	 * The feature id for the '<em><b>Bound</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE__BOUND = 2;

	/**
	 * The feature id for the '<em><b>Composable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE__COMPOSABLE = 3;

	/**
	 * The feature id for the '<em><b>Binding Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE__BINDING_TYPE_NAME = 4;

	/**
	 * The feature id for the '<em><b>Return Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE__RETURN_TYPE_NAME = 5;

	/**
	 * The feature id for the '<em><b>Return Nullable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE__RETURN_NULLABLE = 6;

	/**
	 * The feature id for the '<em><b>Entity Set Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE__ENTITY_SET_PATH = 7;

	/**
	 * The feature id for the '<em><b>Parameters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE__PARAMETERS = 8;

	/**
	 * The number of structural features of the '<em>OData Operation Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE_FEATURE_COUNT = 9;

	/**
	 * The number of operations of the '<em>OData Operation Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_OPERATION_PROFILE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataParameterProfileImpl <em>OData Parameter Profile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataParameterProfileImpl
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataParameterProfile()
	 * @generated
	 */
	int ODATA_PARAMETER_PROFILE = 9;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PARAMETER_PROFILE__NAME = 0;

	/**
	 * The feature id for the '<em><b>Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PARAMETER_PROFILE__TYPE_NAME = 1;

	/**
	 * The feature id for the '<em><b>Nullable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PARAMETER_PROFILE__NULLABLE = 2;

	/**
	 * The number of structural features of the '<em>OData Parameter Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PARAMETER_PROFILE_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>OData Parameter Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PARAMETER_PROFILE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.ODataTypeKind <em>OData Type Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataTypeKind
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataTypeKind()
	 * @generated
	 */
	int ODATA_TYPE_KIND = 10;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationKind <em>OData Operation Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationKind
	 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataOperationKind()
	 * @generated
	 */
	int ODATA_OPERATION_KIND = 11;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile <em>OData Package Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Package Profile</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile
	 * @generated
	 */
	EClass getODataPackageProfile();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getNamespace <em>Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Namespace</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getNamespace()
	 * @see #getODataPackageProfile()
	 * @generated
	 */
	EAttribute getODataPackageProfile_Namespace();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getAlias <em>Alias</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Alias</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getAlias()
	 * @see #getODataPackageProfile()
	 * @generated
	 */
	EAttribute getODataPackageProfile_Alias();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getContainerName <em>Container Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Container Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getContainerName()
	 * @see #getODataPackageProfile()
	 * @generated
	 */
	EAttribute getODataPackageProfile_ContainerName();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getEnums <em>Enums</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Enums</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getEnums()
	 * @see #getODataPackageProfile()
	 * @generated
	 */
	EReference getODataPackageProfile_Enums();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getClasses <em>Classes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Classes</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getClasses()
	 * @see #getODataPackageProfile()
	 * @generated
	 */
	EReference getODataPackageProfile_Classes();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getAnnotations <em>Annotations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Annotations</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getAnnotations()
	 * @see #getODataPackageProfile()
	 * @generated
	 */
	EReference getODataPackageProfile_Annotations();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile <em>OData Enum Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Enum Profile</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile
	 * @generated
	 */
	EClass getODataEnumProfile();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile#getName()
	 * @see #getODataEnumProfile()
	 * @generated
	 */
	EAttribute getODataEnumProfile_Name();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile#getMembers <em>Members</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Members</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile#getMembers()
	 * @see #getODataEnumProfile()
	 * @generated
	 */
	EReference getODataEnumProfile_Members();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumMember <em>OData Enum Member</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Enum Member</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataEnumMember
	 * @generated
	 */
	EClass getODataEnumMember();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumMember#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataEnumMember#getName()
	 * @see #getODataEnumMember()
	 * @generated
	 */
	EAttribute getODataEnumMember_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumMember#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataEnumMember#getValue()
	 * @see #getODataEnumMember()
	 * @generated
	 */
	EAttribute getODataEnumMember_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile <em>OData Class Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Class Profile</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile
	 * @generated
	 */
	EClass getODataClassProfile();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getName()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getQualifiedName <em>Qualified Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Qualified Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getQualifiedName()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_QualifiedName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getKind()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_Kind();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isAbstract <em>Abstract</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Abstract</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isAbstract()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_Abstract();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isOpenType <em>Open Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Open Type</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isOpenType()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_OpenType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isHasStream <em>Has Stream</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Has Stream</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isHasStream()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_HasStream();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getBaseTypeQualifiedName <em>Base Type Qualified Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Base Type Qualified Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getBaseTypeQualifiedName()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_BaseTypeQualifiedName();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getKeyPropertyNames <em>Key Property Names</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Key Property Names</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getKeyPropertyNames()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_KeyPropertyNames();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getProperties <em>Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Properties</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getProperties()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EReference getODataClassProfile_Properties();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getNavigationProperties <em>Navigation Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Navigation Properties</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getNavigationProperties()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EReference getODataClassProfile_NavigationProperties();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getOperations <em>Operations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Operations</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getOperations()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EReference getODataClassProfile_Operations();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getAnnotations <em>Annotations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Annotations</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getAnnotations()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EReference getODataClassProfile_Annotations();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile <em>OData Property Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Property Profile</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile
	 * @generated
	 */
	EClass getODataPropertyProfile();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getName()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getTypeName <em>Type Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getTypeName()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_TypeName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isNullable <em>Nullable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Nullable</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isNullable()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_Nullable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isComputed <em>Computed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Computed</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isComputed()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_Computed();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isImmutable <em>Immutable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Immutable</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isImmutable()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_Immutable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getMaxLength <em>Max Length</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Length</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getMaxLength()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_MaxLength();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getPrecision <em>Precision</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Precision</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getPrecision()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_Precision();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getScale <em>Scale</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scale</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getScale()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_Scale();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getDefaultValue <em>Default Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Default Value</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getDefaultValue()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_DefaultValue();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getSrid <em>Srid</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Srid</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getSrid()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_Srid();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getUnicode <em>Unicode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Unicode</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getUnicode()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EAttribute getODataPropertyProfile_Unicode();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getAnnotations <em>Annotations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Annotations</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getAnnotations()
	 * @see #getODataPropertyProfile()
	 * @generated
	 */
	EReference getODataPropertyProfile_Annotations();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile <em>OData Navigation Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Navigation Profile</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile
	 * @generated
	 */
	EClass getODataNavigationProfile();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getName()
	 * @see #getODataNavigationProfile()
	 * @generated
	 */
	EAttribute getODataNavigationProfile_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getTypeName <em>Type Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getTypeName()
	 * @see #getODataNavigationProfile()
	 * @generated
	 */
	EAttribute getODataNavigationProfile_TypeName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#isNullable <em>Nullable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Nullable</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#isNullable()
	 * @see #getODataNavigationProfile()
	 * @generated
	 */
	EAttribute getODataNavigationProfile_Nullable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#isContainsTarget <em>Contains Target</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Contains Target</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#isContainsTarget()
	 * @see #getODataNavigationProfile()
	 * @generated
	 */
	EAttribute getODataNavigationProfile_ContainsTarget();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getPartner <em>Partner</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Partner</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getPartner()
	 * @see #getODataNavigationProfile()
	 * @generated
	 */
	EAttribute getODataNavigationProfile_Partner();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getOnDelete <em>On Delete</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>On Delete</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getOnDelete()
	 * @see #getODataNavigationProfile()
	 * @generated
	 */
	EAttribute getODataNavigationProfile_OnDelete();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getReferentialConstraints <em>Referential Constraints</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Referential Constraints</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getReferentialConstraints()
	 * @see #getODataNavigationProfile()
	 * @generated
	 */
	EReference getODataNavigationProfile_ReferentialConstraints();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getAnnotations <em>Annotations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Annotations</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getAnnotations()
	 * @see #getODataNavigationProfile()
	 * @generated
	 */
	EReference getODataNavigationProfile_Annotations();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation <em>OData Annotation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Annotation</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataAnnotation
	 * @generated
	 */
	EClass getODataAnnotation();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation#getTerm <em>Term</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Term</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataAnnotation#getTerm()
	 * @see #getODataAnnotation()
	 * @generated
	 */
	EAttribute getODataAnnotation_Term();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataAnnotation#getValue()
	 * @see #getODataAnnotation()
	 * @generated
	 */
	EAttribute getODataAnnotation_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint <em>OData Referential Constraint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Referential Constraint</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint
	 * @generated
	 */
	EClass getODataReferentialConstraint();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint#getProperty <em>Property</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Property</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint#getProperty()
	 * @see #getODataReferentialConstraint()
	 * @generated
	 */
	EAttribute getODataReferentialConstraint_Property();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint#getReferencedProperty <em>Referenced Property</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Referenced Property</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint#getReferencedProperty()
	 * @see #getODataReferentialConstraint()
	 * @generated
	 */
	EAttribute getODataReferentialConstraint_ReferencedProperty();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile <em>OData Operation Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Operation Profile</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile
	 * @generated
	 */
	EClass getODataOperationProfile();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getName()
	 * @see #getODataOperationProfile()
	 * @generated
	 */
	EAttribute getODataOperationProfile_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getKind()
	 * @see #getODataOperationProfile()
	 * @generated
	 */
	EAttribute getODataOperationProfile_Kind();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isBound <em>Bound</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Bound</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isBound()
	 * @see #getODataOperationProfile()
	 * @generated
	 */
	EAttribute getODataOperationProfile_Bound();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isComposable <em>Composable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Composable</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isComposable()
	 * @see #getODataOperationProfile()
	 * @generated
	 */
	EAttribute getODataOperationProfile_Composable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getBindingTypeName <em>Binding Type Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Binding Type Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getBindingTypeName()
	 * @see #getODataOperationProfile()
	 * @generated
	 */
	EAttribute getODataOperationProfile_BindingTypeName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getReturnTypeName <em>Return Type Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Return Type Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getReturnTypeName()
	 * @see #getODataOperationProfile()
	 * @generated
	 */
	EAttribute getODataOperationProfile_ReturnTypeName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isReturnNullable <em>Return Nullable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Return Nullable</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isReturnNullable()
	 * @see #getODataOperationProfile()
	 * @generated
	 */
	EAttribute getODataOperationProfile_ReturnNullable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getEntitySetPath <em>Entity Set Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Entity Set Path</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getEntitySetPath()
	 * @see #getODataOperationProfile()
	 * @generated
	 */
	EAttribute getODataOperationProfile_EntitySetPath();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getParameters <em>Parameters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Parameters</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getParameters()
	 * @see #getODataOperationProfile()
	 * @generated
	 */
	EReference getODataOperationProfile_Parameters();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile <em>OData Parameter Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Parameter Profile</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile
	 * @generated
	 */
	EClass getODataParameterProfile();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#getName()
	 * @see #getODataParameterProfile()
	 * @generated
	 */
	EAttribute getODataParameterProfile_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#getTypeName <em>Type Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type Name</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#getTypeName()
	 * @see #getODataParameterProfile()
	 * @generated
	 */
	EAttribute getODataParameterProfile_TypeName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#isNullable <em>Nullable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Nullable</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#isNullable()
	 * @see #getODataParameterProfile()
	 * @generated
	 */
	EAttribute getODataParameterProfile_Nullable();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.odata.csdl.profile.ODataTypeKind <em>OData Type Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>OData Type Kind</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataTypeKind
	 * @generated
	 */
	EEnum getODataTypeKind();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationKind <em>OData Operation Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>OData Operation Kind</em>'.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationKind
	 * @generated
	 */
	EEnum getODataOperationKind();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ProfileFactory getProfileFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPackageProfileImpl <em>OData Package Profile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataPackageProfileImpl
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataPackageProfile()
		 * @generated
		 */
		EClass ODATA_PACKAGE_PROFILE = eINSTANCE.getODataPackageProfile();

		/**
		 * The meta object literal for the '<em><b>Namespace</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PACKAGE_PROFILE__NAMESPACE = eINSTANCE.getODataPackageProfile_Namespace();

		/**
		 * The meta object literal for the '<em><b>Alias</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PACKAGE_PROFILE__ALIAS = eINSTANCE.getODataPackageProfile_Alias();

		/**
		 * The meta object literal for the '<em><b>Container Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PACKAGE_PROFILE__CONTAINER_NAME = eINSTANCE.getODataPackageProfile_ContainerName();

		/**
		 * The meta object literal for the '<em><b>Enums</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_PACKAGE_PROFILE__ENUMS = eINSTANCE.getODataPackageProfile_Enums();

		/**
		 * The meta object literal for the '<em><b>Classes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_PACKAGE_PROFILE__CLASSES = eINSTANCE.getODataPackageProfile_Classes();

		/**
		 * The meta object literal for the '<em><b>Annotations</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_PACKAGE_PROFILE__ANNOTATIONS = eINSTANCE.getODataPackageProfile_Annotations();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataEnumProfileImpl <em>OData Enum Profile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataEnumProfileImpl
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataEnumProfile()
		 * @generated
		 */
		EClass ODATA_ENUM_PROFILE = eINSTANCE.getODataEnumProfile();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_ENUM_PROFILE__NAME = eINSTANCE.getODataEnumProfile_Name();

		/**
		 * The meta object literal for the '<em><b>Members</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_ENUM_PROFILE__MEMBERS = eINSTANCE.getODataEnumProfile_Members();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataEnumMemberImpl <em>OData Enum Member</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataEnumMemberImpl
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataEnumMember()
		 * @generated
		 */
		EClass ODATA_ENUM_MEMBER = eINSTANCE.getODataEnumMember();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_ENUM_MEMBER__NAME = eINSTANCE.getODataEnumMember_Name();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_ENUM_MEMBER__VALUE = eINSTANCE.getODataEnumMember_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl <em>OData Class Profile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataClassProfile()
		 * @generated
		 */
		EClass ODATA_CLASS_PROFILE = eINSTANCE.getODataClassProfile();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__NAME = eINSTANCE.getODataClassProfile_Name();

		/**
		 * The meta object literal for the '<em><b>Qualified Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__QUALIFIED_NAME = eINSTANCE.getODataClassProfile_QualifiedName();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__KIND = eINSTANCE.getODataClassProfile_Kind();

		/**
		 * The meta object literal for the '<em><b>Abstract</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__ABSTRACT = eINSTANCE.getODataClassProfile_Abstract();

		/**
		 * The meta object literal for the '<em><b>Open Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__OPEN_TYPE = eINSTANCE.getODataClassProfile_OpenType();

		/**
		 * The meta object literal for the '<em><b>Has Stream</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__HAS_STREAM = eINSTANCE.getODataClassProfile_HasStream();

		/**
		 * The meta object literal for the '<em><b>Base Type Qualified Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME = eINSTANCE.getODataClassProfile_BaseTypeQualifiedName();

		/**
		 * The meta object literal for the '<em><b>Key Property Names</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES = eINSTANCE.getODataClassProfile_KeyPropertyNames();

		/**
		 * The meta object literal for the '<em><b>Properties</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_CLASS_PROFILE__PROPERTIES = eINSTANCE.getODataClassProfile_Properties();

		/**
		 * The meta object literal for the '<em><b>Navigation Properties</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_CLASS_PROFILE__NAVIGATION_PROPERTIES = eINSTANCE.getODataClassProfile_NavigationProperties();

		/**
		 * The meta object literal for the '<em><b>Operations</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_CLASS_PROFILE__OPERATIONS = eINSTANCE.getODataClassProfile_Operations();

		/**
		 * The meta object literal for the '<em><b>Annotations</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_CLASS_PROFILE__ANNOTATIONS = eINSTANCE.getODataClassProfile_Annotations();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl <em>OData Property Profile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataPropertyProfile()
		 * @generated
		 */
		EClass ODATA_PROPERTY_PROFILE = eINSTANCE.getODataPropertyProfile();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__NAME = eINSTANCE.getODataPropertyProfile_Name();

		/**
		 * The meta object literal for the '<em><b>Type Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__TYPE_NAME = eINSTANCE.getODataPropertyProfile_TypeName();

		/**
		 * The meta object literal for the '<em><b>Nullable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__NULLABLE = eINSTANCE.getODataPropertyProfile_Nullable();

		/**
		 * The meta object literal for the '<em><b>Computed</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__COMPUTED = eINSTANCE.getODataPropertyProfile_Computed();

		/**
		 * The meta object literal for the '<em><b>Immutable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__IMMUTABLE = eINSTANCE.getODataPropertyProfile_Immutable();

		/**
		 * The meta object literal for the '<em><b>Max Length</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__MAX_LENGTH = eINSTANCE.getODataPropertyProfile_MaxLength();

		/**
		 * The meta object literal for the '<em><b>Precision</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__PRECISION = eINSTANCE.getODataPropertyProfile_Precision();

		/**
		 * The meta object literal for the '<em><b>Scale</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__SCALE = eINSTANCE.getODataPropertyProfile_Scale();

		/**
		 * The meta object literal for the '<em><b>Default Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__DEFAULT_VALUE = eINSTANCE.getODataPropertyProfile_DefaultValue();

		/**
		 * The meta object literal for the '<em><b>Srid</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__SRID = eINSTANCE.getODataPropertyProfile_Srid();

		/**
		 * The meta object literal for the '<em><b>Unicode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PROPERTY_PROFILE__UNICODE = eINSTANCE.getODataPropertyProfile_Unicode();

		/**
		 * The meta object literal for the '<em><b>Annotations</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_PROPERTY_PROFILE__ANNOTATIONS = eINSTANCE.getODataPropertyProfile_Annotations();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl <em>OData Navigation Profile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataNavigationProfile()
		 * @generated
		 */
		EClass ODATA_NAVIGATION_PROFILE = eINSTANCE.getODataNavigationProfile();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_NAVIGATION_PROFILE__NAME = eINSTANCE.getODataNavigationProfile_Name();

		/**
		 * The meta object literal for the '<em><b>Type Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_NAVIGATION_PROFILE__TYPE_NAME = eINSTANCE.getODataNavigationProfile_TypeName();

		/**
		 * The meta object literal for the '<em><b>Nullable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_NAVIGATION_PROFILE__NULLABLE = eINSTANCE.getODataNavigationProfile_Nullable();

		/**
		 * The meta object literal for the '<em><b>Contains Target</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_NAVIGATION_PROFILE__CONTAINS_TARGET = eINSTANCE.getODataNavigationProfile_ContainsTarget();

		/**
		 * The meta object literal for the '<em><b>Partner</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_NAVIGATION_PROFILE__PARTNER = eINSTANCE.getODataNavigationProfile_Partner();

		/**
		 * The meta object literal for the '<em><b>On Delete</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_NAVIGATION_PROFILE__ON_DELETE = eINSTANCE.getODataNavigationProfile_OnDelete();

		/**
		 * The meta object literal for the '<em><b>Referential Constraints</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_NAVIGATION_PROFILE__REFERENTIAL_CONSTRAINTS = eINSTANCE.getODataNavigationProfile_ReferentialConstraints();

		/**
		 * The meta object literal for the '<em><b>Annotations</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_NAVIGATION_PROFILE__ANNOTATIONS = eINSTANCE.getODataNavigationProfile_Annotations();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataAnnotationImpl <em>OData Annotation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataAnnotationImpl
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataAnnotation()
		 * @generated
		 */
		EClass ODATA_ANNOTATION = eINSTANCE.getODataAnnotation();

		/**
		 * The meta object literal for the '<em><b>Term</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_ANNOTATION__TERM = eINSTANCE.getODataAnnotation_Term();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_ANNOTATION__VALUE = eINSTANCE.getODataAnnotation_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataReferentialConstraintImpl <em>OData Referential Constraint</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataReferentialConstraintImpl
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataReferentialConstraint()
		 * @generated
		 */
		EClass ODATA_REFERENTIAL_CONSTRAINT = eINSTANCE.getODataReferentialConstraint();

		/**
		 * The meta object literal for the '<em><b>Property</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_REFERENTIAL_CONSTRAINT__PROPERTY = eINSTANCE.getODataReferentialConstraint_Property();

		/**
		 * The meta object literal for the '<em><b>Referenced Property</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_REFERENTIAL_CONSTRAINT__REFERENCED_PROPERTY = eINSTANCE.getODataReferentialConstraint_ReferencedProperty();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl <em>OData Operation Profile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataOperationProfile()
		 * @generated
		 */
		EClass ODATA_OPERATION_PROFILE = eINSTANCE.getODataOperationProfile();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_OPERATION_PROFILE__NAME = eINSTANCE.getODataOperationProfile_Name();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_OPERATION_PROFILE__KIND = eINSTANCE.getODataOperationProfile_Kind();

		/**
		 * The meta object literal for the '<em><b>Bound</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_OPERATION_PROFILE__BOUND = eINSTANCE.getODataOperationProfile_Bound();

		/**
		 * The meta object literal for the '<em><b>Composable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_OPERATION_PROFILE__COMPOSABLE = eINSTANCE.getODataOperationProfile_Composable();

		/**
		 * The meta object literal for the '<em><b>Binding Type Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_OPERATION_PROFILE__BINDING_TYPE_NAME = eINSTANCE.getODataOperationProfile_BindingTypeName();

		/**
		 * The meta object literal for the '<em><b>Return Type Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_OPERATION_PROFILE__RETURN_TYPE_NAME = eINSTANCE.getODataOperationProfile_ReturnTypeName();

		/**
		 * The meta object literal for the '<em><b>Return Nullable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_OPERATION_PROFILE__RETURN_NULLABLE = eINSTANCE.getODataOperationProfile_ReturnNullable();

		/**
		 * The meta object literal for the '<em><b>Entity Set Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_OPERATION_PROFILE__ENTITY_SET_PATH = eINSTANCE.getODataOperationProfile_EntitySetPath();

		/**
		 * The meta object literal for the '<em><b>Parameters</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_OPERATION_PROFILE__PARAMETERS = eINSTANCE.getODataOperationProfile_Parameters();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataParameterProfileImpl <em>OData Parameter Profile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ODataParameterProfileImpl
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataParameterProfile()
		 * @generated
		 */
		EClass ODATA_PARAMETER_PROFILE = eINSTANCE.getODataParameterProfile();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PARAMETER_PROFILE__NAME = eINSTANCE.getODataParameterProfile_Name();

		/**
		 * The meta object literal for the '<em><b>Type Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PARAMETER_PROFILE__TYPE_NAME = eINSTANCE.getODataParameterProfile_TypeName();

		/**
		 * The meta object literal for the '<em><b>Nullable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_PARAMETER_PROFILE__NULLABLE = eINSTANCE.getODataParameterProfile_Nullable();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.ODataTypeKind <em>OData Type Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.ODataTypeKind
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataTypeKind()
		 * @generated
		 */
		EEnum ODATA_TYPE_KIND = eINSTANCE.getODataTypeKind();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationKind <em>OData Operation Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationKind
		 * @see org.eclipse.fennec.odata.csdl.profile.impl.ProfilePackageImpl#getODataOperationKind()
		 * @generated
		 */
		EEnum ODATA_OPERATION_KIND = eINSTANCE.getODataOperationKind();

	}

} //ProfilePackage
