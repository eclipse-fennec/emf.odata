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

import org.eclipse.fennec.model.metadata.ClassAspect;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Class OData Aspect</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * OData class-level aspect for an EClass: whether it is an EntityType or ComplexType plus the EntityType facets. The base type derives from ClassMetadata super types; the key derives from the feature aspects flagged 'key'.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isHasStream <em>Has Stream</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isOpenType <em>Open Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isAbstract <em>Abstract</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getClassODataAspect()
 * @model
 * @generated
 */
@ProviderType
public interface ClassODataAspect extends ClassAspect {
	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.odata.metadata.odata.ODataTypeKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * ENTITY if the class has at least one key feature, otherwise COMPLEX.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataTypeKind
	 * @see #setKind(ODataTypeKind)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getClassODataAspect_Kind()
	 * @model
	 * @generated
	 */
	ODataTypeKind getKind();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataTypeKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(ODataTypeKind value);

	/**
	 * Returns the value of the '<em><b>Has Stream</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL HasStream (media entity). From @OData.HasStream.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Has Stream</em>' attribute.
	 * @see #setHasStream(boolean)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getClassODataAspect_HasStream()
	 * @model
	 * @generated
	 */
	boolean isHasStream();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isHasStream <em>Has Stream</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Has Stream</em>' attribute.
	 * @see #isHasStream()
	 * @generated
	 */
	void setHasStream(boolean value);

	/**
	 * Returns the value of the '<em><b>Open Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL OpenType. From @OData.OpenType.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Open Type</em>' attribute.
	 * @see #setOpenType(boolean)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getClassODataAspect_OpenType()
	 * @model
	 * @generated
	 */
	boolean isOpenType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isOpenType <em>Open Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Open Type</em>' attribute.
	 * @see #isOpenType()
	 * @generated
	 */
	void setOpenType(boolean value);

	/**
	 * Returns the value of the '<em><b>Abstract</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL Abstract (mirrors EClass.abstract).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Abstract</em>' attribute.
	 * @see #setAbstract(boolean)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getClassODataAspect_Abstract()
	 * @model
	 * @generated
	 */
	boolean isAbstract();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect#isAbstract <em>Abstract</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Abstract</em>' attribute.
	 * @see #isAbstract()
	 * @generated
	 */
	void setAbstract(boolean value);

} // ClassODataAspect
