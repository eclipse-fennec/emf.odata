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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>OData Parameter Profile</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#getTypeName <em>Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#isNullable <em>Nullable</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataParameterProfile()
 * @model
 * @generated
 */
@ProviderType
public interface ODataParameterProfile extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataParameterProfile_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type Name</em>' attribute.
	 * @see #setTypeName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataParameterProfile_TypeName()
	 * @model
	 * @generated
	 */
	String getTypeName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#getTypeName <em>Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type Name</em>' attribute.
	 * @see #getTypeName()
	 * @generated
	 */
	void setTypeName(String value);

	/**
	 * Returns the value of the '<em><b>Nullable</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Nullable</em>' attribute.
	 * @see #setNullable(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataParameterProfile_Nullable()
	 * @model default="true"
	 * @generated
	 */
	boolean isNullable();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile#isNullable <em>Nullable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Nullable</em>' attribute.
	 * @see #isNullable()
	 * @generated
	 */
	void setNullable(boolean value);

} // ODataParameterProfile
