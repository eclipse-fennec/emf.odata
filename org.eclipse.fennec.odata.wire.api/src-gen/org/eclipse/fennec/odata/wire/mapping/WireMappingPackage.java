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
package org.eclipse.fennec.odata.wire.mapping;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
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
 * How a wire profile shows a model on the wire (ADR-0010, #119): wire properties that stand for one or more model attributes, given meaning by the profile. The mapping belongs to the profile, not to the model: a plain OData root of the same model sees the attributes as they are.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.odata.wire.mapping.WireMappingFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = WireMappingPackage.eNS_URI, fingerprint = "fp1:a5eb418e88cbf185738ed5c9428e4aa2b0085463d34e3a150f50b5e17f8b7985", genModel = "/model/wire-mapping.genmodel", genModelSourceLocations = {"model/wire-mapping.genmodel","org.eclipse.fennec.odata.wire.api/model/wire-mapping.genmodel"}, ecore = "/model/wire-mapping.ecore", ecoreSourceLocations = "/model/wire-mapping.ecore")
public interface WireMappingPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "mapping";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/fennec/odata/wire/mapping/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "wiremapping";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	WireMappingPackage eINSTANCE = org.eclipse.fennec.odata.wire.mapping.impl.WireMappingPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.wire.mapping.impl.WireMappingImpl <em>Wire Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.wire.mapping.impl.WireMappingImpl
	 * @see org.eclipse.fennec.odata.wire.mapping.impl.WireMappingPackageImpl#getWireMapping()
	 * @generated
	 */
	int WIRE_MAPPING = 0;

	/**
	 * The feature id for the '<em><b>Classes</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WIRE_MAPPING__CLASSES = 0;

	/**
	 * The number of structural features of the '<em>Wire Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WIRE_MAPPING_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Wire Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WIRE_MAPPING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.wire.mapping.impl.ClassMappingImpl <em>Class Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.wire.mapping.impl.ClassMappingImpl
	 * @see org.eclipse.fennec.odata.wire.mapping.impl.WireMappingPackageImpl#getClassMapping()
	 * @generated
	 */
	int CLASS_MAPPING = 1;

	/**
	 * The feature id for the '<em><b>EClass</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_MAPPING__ECLASS = 0;

	/**
	 * The feature id for the '<em><b>Properties</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_MAPPING__PROPERTIES = 1;

	/**
	 * The number of structural features of the '<em>Class Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_MAPPING_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Class Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CLASS_MAPPING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.wire.mapping.impl.WirePropertyImpl <em>Wire Property</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.wire.mapping.impl.WirePropertyImpl
	 * @see org.eclipse.fennec.odata.wire.mapping.impl.WireMappingPackageImpl#getWireProperty()
	 * @generated
	 */
	int WIRE_PROPERTY = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WIRE_PROPERTY__NAME = 0;

	/**
	 * The feature id for the '<em><b>Parts</b></em>' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WIRE_PROPERTY__PARTS = 1;

	/**
	 * The number of structural features of the '<em>Wire Property</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WIRE_PROPERTY_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Wire Property</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int WIRE_PROPERTY_OPERATION_COUNT = 0;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.wire.mapping.WireMapping <em>Wire Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Wire Mapping</em>'.
	 * @see org.eclipse.fennec.odata.wire.mapping.WireMapping
	 * @generated
	 */
	EClass getWireMapping();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.wire.mapping.WireMapping#getClasses <em>Classes</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Classes</em>'.
	 * @see org.eclipse.fennec.odata.wire.mapping.WireMapping#getClasses()
	 * @see #getWireMapping()
	 * @generated
	 */
	EReference getWireMapping_Classes();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.wire.mapping.ClassMapping <em>Class Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Class Mapping</em>'.
	 * @see org.eclipse.fennec.odata.wire.mapping.ClassMapping
	 * @generated
	 */
	EClass getClassMapping();

	/**
	 * Returns the meta object for the reference '{@link org.eclipse.fennec.odata.wire.mapping.ClassMapping#getEClass <em>EClass</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>EClass</em>'.
	 * @see org.eclipse.fennec.odata.wire.mapping.ClassMapping#getEClass()
	 * @see #getClassMapping()
	 * @generated
	 */
	EReference getClassMapping_EClass();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.wire.mapping.ClassMapping#getProperties <em>Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Properties</em>'.
	 * @see org.eclipse.fennec.odata.wire.mapping.ClassMapping#getProperties()
	 * @see #getClassMapping()
	 * @generated
	 */
	EReference getClassMapping_Properties();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.wire.mapping.WireProperty <em>Wire Property</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Wire Property</em>'.
	 * @see org.eclipse.fennec.odata.wire.mapping.WireProperty
	 * @generated
	 */
	EClass getWireProperty();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.wire.mapping.WireProperty#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.odata.wire.mapping.WireProperty#getName()
	 * @see #getWireProperty()
	 * @generated
	 */
	EAttribute getWireProperty_Name();

	/**
	 * Returns the meta object for the reference list '{@link org.eclipse.fennec.odata.wire.mapping.WireProperty#getParts <em>Parts</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference list '<em>Parts</em>'.
	 * @see org.eclipse.fennec.odata.wire.mapping.WireProperty#getParts()
	 * @see #getWireProperty()
	 * @generated
	 */
	EReference getWireProperty_Parts();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	WireMappingFactory getWireMappingFactory();

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
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.wire.mapping.impl.WireMappingImpl <em>Wire Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.wire.mapping.impl.WireMappingImpl
		 * @see org.eclipse.fennec.odata.wire.mapping.impl.WireMappingPackageImpl#getWireMapping()
		 * @generated
		 */
		EClass WIRE_MAPPING = eINSTANCE.getWireMapping();

		/**
		 * The meta object literal for the '<em><b>Classes</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WIRE_MAPPING__CLASSES = eINSTANCE.getWireMapping_Classes();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.wire.mapping.impl.ClassMappingImpl <em>Class Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.wire.mapping.impl.ClassMappingImpl
		 * @see org.eclipse.fennec.odata.wire.mapping.impl.WireMappingPackageImpl#getClassMapping()
		 * @generated
		 */
		EClass CLASS_MAPPING = eINSTANCE.getClassMapping();

		/**
		 * The meta object literal for the '<em><b>EClass</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CLASS_MAPPING__ECLASS = eINSTANCE.getClassMapping_EClass();

		/**
		 * The meta object literal for the '<em><b>Properties</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CLASS_MAPPING__PROPERTIES = eINSTANCE.getClassMapping_Properties();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.wire.mapping.impl.WirePropertyImpl <em>Wire Property</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.wire.mapping.impl.WirePropertyImpl
		 * @see org.eclipse.fennec.odata.wire.mapping.impl.WireMappingPackageImpl#getWireProperty()
		 * @generated
		 */
		EClass WIRE_PROPERTY = eINSTANCE.getWireProperty();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute WIRE_PROPERTY__NAME = eINSTANCE.getWireProperty_Name();

		/**
		 * The meta object literal for the '<em><b>Parts</b></em>' reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference WIRE_PROPERTY__PARTS = eINSTANCE.getWireProperty_Parts();

	}

} //WireMappingPackage
