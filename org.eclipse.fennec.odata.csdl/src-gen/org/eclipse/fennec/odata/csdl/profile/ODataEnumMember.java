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
 * A representation of the model object '<em><b>OData Enum Member</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumMember#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumMember#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataEnumMember()
 * @model
 * @generated
 */
@ProviderType
public interface ODataEnumMember extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataEnumMember_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumMember#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(long)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataEnumMember_Value()
	 * @model
	 * @generated
	 */
	long getValue();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumMember#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(long value);

} // ODataEnumMember
