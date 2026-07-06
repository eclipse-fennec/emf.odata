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
package org.eclipse.fennec.odata.metadata.odata;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.eclipse.fennec.model.metadata.MetadataPackage;

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
 * OData metadata aspects and profiles for the Fennec Model Metadata Service. These types subtype the metadata aspect/profile base types (https://eclipse.org/fennec/metadata/1.0.0) and are populated by the ODataAspectProvider during registerPackage(): per-EClass/-feature @OData.* annotations are resolved into aspects, then consolidated into an ODataClassProfile that the CSDL layer (org.eclipse.fennec.odata.csdl) consumes to build CSDL via the EDM model.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.odata.metadata.odata.OdataFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = OdataPackage.eNS_URI, genModel = "/model/odata.genmodel", genModelSourceLocations = {"model/odata.genmodel","org.eclipse.fennec.odata.metadata/model/odata.genmodel"}, ecore = "/model/odata.ecore", ecoreSourceLocations = "/model/odata.ecore")
public interface OdataPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "odata";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/fennec/odata/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "odata";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	OdataPackage eINSTANCE = org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.PackageODataAspectImpl <em>Package OData Aspect</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.PackageODataAspectImpl
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getPackageODataAspect()
	 * @generated
	 */
	int PACKAGE_ODATA_ASPECT = 0;

	/**
	 * The feature id for the '<em><b>Type Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ODATA_ASPECT__TYPE_ID = MetadataPackage.PACKAGE_ASPECT__TYPE_ID;

	/**
	 * The feature id for the '<em><b>Diagnostics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ODATA_ASPECT__DIAGNOSTICS = MetadataPackage.PACKAGE_ASPECT__DIAGNOSTICS;

	/**
	 * The feature id for the '<em><b>Package Metadata</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ODATA_ASPECT__PACKAGE_METADATA = MetadataPackage.PACKAGE_ASPECT__PACKAGE_METADATA;

	/**
	 * The feature id for the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ODATA_ASPECT__NAMESPACE = MetadataPackage.PACKAGE_ASPECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Alias</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ODATA_ASPECT__ALIAS = MetadataPackage.PACKAGE_ASPECT_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Package OData Aspect</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ODATA_ASPECT_FEATURE_COUNT = MetadataPackage.PACKAGE_ASPECT_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>Package OData Aspect</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PACKAGE_ODATA_ASPECT_OPERATION_COUNT = MetadataPackage.PACKAGE_ASPECT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.ClassODataAspectImpl <em>Class OData Aspect</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.ClassODataAspectImpl
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getClassODataAspect()
	 * @generated
	 */
	int CLASS_ODATA_ASPECT = 1;

	/**
	 * The feature id for the '<em><b>Type Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_ODATA_ASPECT__TYPE_ID = MetadataPackage.CLASS_ASPECT__TYPE_ID;

	/**
	 * The feature id for the '<em><b>Diagnostics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_ODATA_ASPECT__DIAGNOSTICS = MetadataPackage.CLASS_ASPECT__DIAGNOSTICS;

	/**
	 * The feature id for the '<em><b>Class Metadata</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_ODATA_ASPECT__CLASS_METADATA = MetadataPackage.CLASS_ASPECT__CLASS_METADATA;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_ODATA_ASPECT__KIND = MetadataPackage.CLASS_ASPECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Has Stream</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_ODATA_ASPECT__HAS_STREAM = MetadataPackage.CLASS_ASPECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Open Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_ODATA_ASPECT__OPEN_TYPE = MetadataPackage.CLASS_ASPECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Abstract</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_ODATA_ASPECT__ABSTRACT = MetadataPackage.CLASS_ASPECT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>Class OData Aspect</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_ODATA_ASPECT_FEATURE_COUNT = MetadataPackage.CLASS_ASPECT_FEATURE_COUNT + 4;

	/**
	 * The number of operations of the '<em>Class OData Aspect</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_ODATA_ASPECT_OPERATION_COUNT = MetadataPackage.CLASS_ASPECT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl <em>Feature OData Aspect</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getFeatureODataAspect()
	 * @generated
	 */
	int FEATURE_ODATA_ASPECT = 2;

	/**
	 * The feature id for the '<em><b>Type Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__TYPE_ID = MetadataPackage.FEATURE_ASPECT__TYPE_ID;

	/**
	 * The feature id for the '<em><b>Diagnostics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__DIAGNOSTICS = MetadataPackage.FEATURE_ASPECT__DIAGNOSTICS;

	/**
	 * The feature id for the '<em><b>Feature Metadata</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__FEATURE_METADATA = MetadataPackage.FEATURE_ASPECT__FEATURE_METADATA;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__KEY = MetadataPackage.FEATURE_ASPECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Nullable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__NULLABLE = MetadataPackage.FEATURE_ASPECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Computed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__COMPUTED = MetadataPackage.FEATURE_ASPECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Immutable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__IMMUTABLE = MetadataPackage.FEATURE_ASPECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Edm Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__EDM_TYPE = MetadataPackage.FEATURE_ASPECT_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Max Length</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__MAX_LENGTH = MetadataPackage.FEATURE_ASPECT_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Precision</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__PRECISION = MetadataPackage.FEATURE_ASPECT_FEATURE_COUNT + 6;

	/**
	 * The feature id for the '<em><b>Scale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__SCALE = MetadataPackage.FEATURE_ASPECT_FEATURE_COUNT + 7;

	/**
	 * The feature id for the '<em><b>Default Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT__DEFAULT_VALUE = MetadataPackage.FEATURE_ASPECT_FEATURE_COUNT + 8;

	/**
	 * The number of structural features of the '<em>Feature OData Aspect</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT_FEATURE_COUNT = MetadataPackage.FEATURE_ASPECT_FEATURE_COUNT + 9;

	/**
	 * The number of operations of the '<em>Feature OData Aspect</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FEATURE_ODATA_ASPECT_OPERATION_COUNT = MetadataPackage.FEATURE_ASPECT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.ReferenceODataAspectImpl <em>Reference OData Aspect</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.ReferenceODataAspectImpl
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getReferenceODataAspect()
	 * @generated
	 */
	int REFERENCE_ODATA_ASPECT = 3;

	/**
	 * The feature id for the '<em><b>Type Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__TYPE_ID = FEATURE_ODATA_ASPECT__TYPE_ID;

	/**
	 * The feature id for the '<em><b>Diagnostics</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__DIAGNOSTICS = FEATURE_ODATA_ASPECT__DIAGNOSTICS;

	/**
	 * The feature id for the '<em><b>Feature Metadata</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__FEATURE_METADATA = FEATURE_ODATA_ASPECT__FEATURE_METADATA;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__KEY = FEATURE_ODATA_ASPECT__KEY;

	/**
	 * The feature id for the '<em><b>Nullable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__NULLABLE = FEATURE_ODATA_ASPECT__NULLABLE;

	/**
	 * The feature id for the '<em><b>Computed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__COMPUTED = FEATURE_ODATA_ASPECT__COMPUTED;

	/**
	 * The feature id for the '<em><b>Immutable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__IMMUTABLE = FEATURE_ODATA_ASPECT__IMMUTABLE;

	/**
	 * The feature id for the '<em><b>Edm Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__EDM_TYPE = FEATURE_ODATA_ASPECT__EDM_TYPE;

	/**
	 * The feature id for the '<em><b>Max Length</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__MAX_LENGTH = FEATURE_ODATA_ASPECT__MAX_LENGTH;

	/**
	 * The feature id for the '<em><b>Precision</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__PRECISION = FEATURE_ODATA_ASPECT__PRECISION;

	/**
	 * The feature id for the '<em><b>Scale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__SCALE = FEATURE_ODATA_ASPECT__SCALE;

	/**
	 * The feature id for the '<em><b>Default Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__DEFAULT_VALUE = FEATURE_ODATA_ASPECT__DEFAULT_VALUE;

	/**
	 * The feature id for the '<em><b>Contains Target</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__CONTAINS_TARGET = FEATURE_ODATA_ASPECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Partner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__PARTNER = FEATURE_ODATA_ASPECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>On Delete</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT__ON_DELETE = FEATURE_ODATA_ASPECT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Reference OData Aspect</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT_FEATURE_COUNT = FEATURE_ODATA_ASPECT_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Reference OData Aspect</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int REFERENCE_ODATA_ASPECT_OPERATION_COUNT = FEATURE_ODATA_ASPECT_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataPackageProfileImpl <em>OData Package Profile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.ODataPackageProfileImpl
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getODataPackageProfile()
	 * @generated
	 */
	int ODATA_PACKAGE_PROFILE = 4;

	/**
	 * The feature id for the '<em><b>Type Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE__TYPE_ID = MetadataPackage.PACKAGE_PROFILE__TYPE_ID;

	/**
	 * The feature id for the '<em><b>Class Profiles</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE__CLASS_PROFILES = MetadataPackage.PACKAGE_PROFILE__CLASS_PROFILES;

	/**
	 * The feature id for the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE__NAMESPACE = MetadataPackage.PACKAGE_PROFILE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Odata Profile</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE__ODATA_PROFILE = MetadataPackage.PACKAGE_PROFILE_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>OData Package Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE_FEATURE_COUNT = MetadataPackage.PACKAGE_PROFILE_FEATURE_COUNT + 2;

	/**
	 * The number of operations of the '<em>OData Package Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_PACKAGE_PROFILE_OPERATION_COUNT = MetadataPackage.PACKAGE_PROFILE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl <em>OData Class Profile</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getODataClassProfile()
	 * @generated
	 */
	int ODATA_CLASS_PROFILE = 5;

	/**
	 * The feature id for the '<em><b>EClass</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__ECLASS = MetadataPackage.CLASS_PROFILE__ECLASS;

	/**
	 * The feature id for the '<em><b>Kind</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__KIND = MetadataPackage.CLASS_PROFILE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Qualified Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__QUALIFIED_NAME = MetadataPackage.CLASS_PROFILE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Key Property Names</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES = MetadataPackage.CLASS_PROFILE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Open Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__OPEN_TYPE = MetadataPackage.CLASS_PROFILE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Has Stream</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__HAS_STREAM = MetadataPackage.CLASS_PROFILE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Base Type Qualified Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME = MetadataPackage.CLASS_PROFILE_FEATURE_COUNT + 5;

	/**
	 * The feature id for the '<em><b>Odata Profile</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE__ODATA_PROFILE = MetadataPackage.CLASS_PROFILE_FEATURE_COUNT + 6;

	/**
	 * The number of structural features of the '<em>OData Class Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE_FEATURE_COUNT = MetadataPackage.CLASS_PROFILE_FEATURE_COUNT + 7;

	/**
	 * The number of operations of the '<em>OData Class Profile</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ODATA_CLASS_PROFILE_OPERATION_COUNT = MetadataPackage.CLASS_PROFILE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.metadata.odata.ODataTypeKind <em>OData Type Kind</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataTypeKind
	 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getODataTypeKind()
	 * @generated
	 */
	int ODATA_TYPE_KIND = 6;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.metadata.odata.PackageODataAspect <em>Package OData Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Package OData Aspect</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.PackageODataAspect
	 * @generated
	 */
	EClass getPackageODataAspect();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.PackageODataAspect#getNamespace <em>Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Namespace</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.PackageODataAspect#getNamespace()
	 * @see #getPackageODataAspect()
	 * @generated
	 */
	EAttribute getPackageODataAspect_Namespace();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.PackageODataAspect#getAlias <em>Alias</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Alias</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.PackageODataAspect#getAlias()
	 * @see #getPackageODataAspect()
	 * @generated
	 */
	EAttribute getPackageODataAspect_Alias();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect <em>Class OData Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Class OData Aspect</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ClassODataAspect
	 * @generated
	 */
	EClass getClassODataAspect();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#getKind()
	 * @see #getClassODataAspect()
	 * @generated
	 */
	EAttribute getClassODataAspect_Kind();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isHasStream <em>Has Stream</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Has Stream</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isHasStream()
	 * @see #getClassODataAspect()
	 * @generated
	 */
	EAttribute getClassODataAspect_HasStream();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isOpenType <em>Open Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Open Type</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isOpenType()
	 * @see #getClassODataAspect()
	 * @generated
	 */
	EAttribute getClassODataAspect_OpenType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isAbstract <em>Abstract</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Abstract</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isAbstract()
	 * @see #getClassODataAspect()
	 * @generated
	 */
	EAttribute getClassODataAspect_Abstract();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect <em>Feature OData Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Feature OData Aspect</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect
	 * @generated
	 */
	EClass getFeatureODataAspect();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isKey <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isKey()
	 * @see #getFeatureODataAspect()
	 * @generated
	 */
	EAttribute getFeatureODataAspect_Key();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isNullable <em>Nullable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Nullable</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isNullable()
	 * @see #getFeatureODataAspect()
	 * @generated
	 */
	EAttribute getFeatureODataAspect_Nullable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isComputed <em>Computed</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Computed</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isComputed()
	 * @see #getFeatureODataAspect()
	 * @generated
	 */
	EAttribute getFeatureODataAspect_Computed();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isImmutable <em>Immutable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Immutable</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isImmutable()
	 * @see #getFeatureODataAspect()
	 * @generated
	 */
	EAttribute getFeatureODataAspect_Immutable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getEdmType <em>Edm Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Edm Type</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getEdmType()
	 * @see #getFeatureODataAspect()
	 * @generated
	 */
	EAttribute getFeatureODataAspect_EdmType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getMaxLength <em>Max Length</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Length</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getMaxLength()
	 * @see #getFeatureODataAspect()
	 * @generated
	 */
	EAttribute getFeatureODataAspect_MaxLength();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getPrecision <em>Precision</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Precision</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getPrecision()
	 * @see #getFeatureODataAspect()
	 * @generated
	 */
	EAttribute getFeatureODataAspect_Precision();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getScale <em>Scale</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scale</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getScale()
	 * @see #getFeatureODataAspect()
	 * @generated
	 */
	EAttribute getFeatureODataAspect_Scale();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getDefaultValue <em>Default Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Default Value</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getDefaultValue()
	 * @see #getFeatureODataAspect()
	 * @generated
	 */
	EAttribute getFeatureODataAspect_DefaultValue();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect <em>Reference OData Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Reference OData Aspect</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect
	 * @generated
	 */
	EClass getReferenceODataAspect();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#isContainsTarget <em>Contains Target</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Contains Target</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#isContainsTarget()
	 * @see #getReferenceODataAspect()
	 * @generated
	 */
	EAttribute getReferenceODataAspect_ContainsTarget();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#getPartner <em>Partner</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Partner</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#getPartner()
	 * @see #getReferenceODataAspect()
	 * @generated
	 */
	EAttribute getReferenceODataAspect_Partner();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#getOnDelete <em>On Delete</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>On Delete</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#getOnDelete()
	 * @see #getReferenceODataAspect()
	 * @generated
	 */
	EAttribute getReferenceODataAspect_OnDelete();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile <em>OData Package Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Package Profile</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile
	 * @generated
	 */
	EClass getODataPackageProfile();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile#getNamespace <em>Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Namespace</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile#getNamespace()
	 * @see #getODataPackageProfile()
	 * @generated
	 */
	EAttribute getODataPackageProfile_Namespace();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile#getOdataProfile <em>Odata Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Odata Profile</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile#getOdataProfile()
	 * @see #getODataPackageProfile()
	 * @generated
	 */
	EReference getODataPackageProfile_OdataProfile();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile <em>OData Class Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>OData Class Profile</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataClassProfile
	 * @generated
	 */
	EClass getODataClassProfile();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getKind <em>Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Kind</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getKind()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_Kind();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getQualifiedName <em>Qualified Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Qualified Name</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getQualifiedName()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_QualifiedName();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getKeyPropertyNames <em>Key Property Names</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Key Property Names</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getKeyPropertyNames()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_KeyPropertyNames();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#isOpenType <em>Open Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Open Type</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#isOpenType()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_OpenType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#isHasStream <em>Has Stream</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Has Stream</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#isHasStream()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_HasStream();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getBaseTypeQualifiedName <em>Base Type Qualified Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Base Type Qualified Name</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getBaseTypeQualifiedName()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EAttribute getODataClassProfile_BaseTypeQualifiedName();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getOdataProfile <em>Odata Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Odata Profile</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getOdataProfile()
	 * @see #getODataClassProfile()
	 * @generated
	 */
	EReference getODataClassProfile_OdataProfile();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.odata.metadata.odata.ODataTypeKind <em>OData Type Kind</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>OData Type Kind</em>'.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataTypeKind
	 * @generated
	 */
	EEnum getODataTypeKind();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	OdataFactory getOdataFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.PackageODataAspectImpl <em>Package OData Aspect</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.PackageODataAspectImpl
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getPackageODataAspect()
		 * @generated
		 */
		EClass PACKAGE_ODATA_ASPECT = eINSTANCE.getPackageODataAspect();

		/**
		 * The meta object literal for the '<em><b>Namespace</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_ODATA_ASPECT__NAMESPACE = eINSTANCE.getPackageODataAspect_Namespace();

		/**
		 * The meta object literal for the '<em><b>Alias</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PACKAGE_ODATA_ASPECT__ALIAS = eINSTANCE.getPackageODataAspect_Alias();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.ClassODataAspectImpl <em>Class OData Aspect</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.ClassODataAspectImpl
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getClassODataAspect()
		 * @generated
		 */
		EClass CLASS_ODATA_ASPECT = eINSTANCE.getClassODataAspect();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLASS_ODATA_ASPECT__KIND = eINSTANCE.getClassODataAspect_Kind();

		/**
		 * The meta object literal for the '<em><b>Has Stream</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLASS_ODATA_ASPECT__HAS_STREAM = eINSTANCE.getClassODataAspect_HasStream();

		/**
		 * The meta object literal for the '<em><b>Open Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLASS_ODATA_ASPECT__OPEN_TYPE = eINSTANCE.getClassODataAspect_OpenType();

		/**
		 * The meta object literal for the '<em><b>Abstract</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CLASS_ODATA_ASPECT__ABSTRACT = eINSTANCE.getClassODataAspect_Abstract();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl <em>Feature OData Aspect</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getFeatureODataAspect()
		 * @generated
		 */
		EClass FEATURE_ODATA_ASPECT = eINSTANCE.getFeatureODataAspect();

		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_ODATA_ASPECT__KEY = eINSTANCE.getFeatureODataAspect_Key();

		/**
		 * The meta object literal for the '<em><b>Nullable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_ODATA_ASPECT__NULLABLE = eINSTANCE.getFeatureODataAspect_Nullable();

		/**
		 * The meta object literal for the '<em><b>Computed</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_ODATA_ASPECT__COMPUTED = eINSTANCE.getFeatureODataAspect_Computed();

		/**
		 * The meta object literal for the '<em><b>Immutable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_ODATA_ASPECT__IMMUTABLE = eINSTANCE.getFeatureODataAspect_Immutable();

		/**
		 * The meta object literal for the '<em><b>Edm Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_ODATA_ASPECT__EDM_TYPE = eINSTANCE.getFeatureODataAspect_EdmType();

		/**
		 * The meta object literal for the '<em><b>Max Length</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_ODATA_ASPECT__MAX_LENGTH = eINSTANCE.getFeatureODataAspect_MaxLength();

		/**
		 * The meta object literal for the '<em><b>Precision</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_ODATA_ASPECT__PRECISION = eINSTANCE.getFeatureODataAspect_Precision();

		/**
		 * The meta object literal for the '<em><b>Scale</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_ODATA_ASPECT__SCALE = eINSTANCE.getFeatureODataAspect_Scale();

		/**
		 * The meta object literal for the '<em><b>Default Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FEATURE_ODATA_ASPECT__DEFAULT_VALUE = eINSTANCE.getFeatureODataAspect_DefaultValue();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.ReferenceODataAspectImpl <em>Reference OData Aspect</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.ReferenceODataAspectImpl
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getReferenceODataAspect()
		 * @generated
		 */
		EClass REFERENCE_ODATA_ASPECT = eINSTANCE.getReferenceODataAspect();

		/**
		 * The meta object literal for the '<em><b>Contains Target</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_ODATA_ASPECT__CONTAINS_TARGET = eINSTANCE.getReferenceODataAspect_ContainsTarget();

		/**
		 * The meta object literal for the '<em><b>Partner</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_ODATA_ASPECT__PARTNER = eINSTANCE.getReferenceODataAspect_Partner();

		/**
		 * The meta object literal for the '<em><b>On Delete</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute REFERENCE_ODATA_ASPECT__ON_DELETE = eINSTANCE.getReferenceODataAspect_OnDelete();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataPackageProfileImpl <em>OData Package Profile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.ODataPackageProfileImpl
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getODataPackageProfile()
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
		 * The meta object literal for the '<em><b>Odata Profile</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_PACKAGE_PROFILE__ODATA_PROFILE = eINSTANCE.getODataPackageProfile_OdataProfile();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl <em>OData Class Profile</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getODataClassProfile()
		 * @generated
		 */
		EClass ODATA_CLASS_PROFILE = eINSTANCE.getODataClassProfile();

		/**
		 * The meta object literal for the '<em><b>Kind</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__KIND = eINSTANCE.getODataClassProfile_Kind();

		/**
		 * The meta object literal for the '<em><b>Qualified Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__QUALIFIED_NAME = eINSTANCE.getODataClassProfile_QualifiedName();

		/**
		 * The meta object literal for the '<em><b>Key Property Names</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES = eINSTANCE.getODataClassProfile_KeyPropertyNames();

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
		 * The meta object literal for the '<em><b>Odata Profile</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ODATA_CLASS_PROFILE__ODATA_PROFILE = eINSTANCE.getODataClassProfile_OdataProfile();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.metadata.odata.ODataTypeKind <em>OData Type Kind</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.metadata.odata.ODataTypeKind
		 * @see org.eclipse.fennec.odata.metadata.odata.impl.OdataPackageImpl#getODataTypeKind()
		 * @generated
		 */
		EEnum ODATA_TYPE_KIND = eINSTANCE.getODataTypeKind();

	}

} //OdataPackage
