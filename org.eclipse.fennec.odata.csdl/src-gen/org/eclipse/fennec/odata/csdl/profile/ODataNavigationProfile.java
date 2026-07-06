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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>OData Navigation Profile</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Resolved CSDL NavigationProperty (from an EReference to an entity type).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getTypeName <em>Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#isNullable <em>Nullable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#isContainsTarget <em>Contains Target</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getPartner <em>Partner</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getOnDelete <em>On Delete</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getReferentialConstraints <em>Referential Constraints</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getAnnotations <em>Annotations</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataNavigationProfile()
 * @model
 * @generated
 */
@ProviderType
public interface ODataNavigationProfile extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataNavigationProfile_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getName <em>Name</em>}' attribute.
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
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataNavigationProfile_TypeName()
	 * @model
	 * @generated
	 */
	String getTypeName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getTypeName <em>Type Name</em>}' attribute.
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
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataNavigationProfile_Nullable()
	 * @model default="true"
	 * @generated
	 */
	boolean isNullable();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#isNullable <em>Nullable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Nullable</em>' attribute.
	 * @see #isNullable()
	 * @generated
	 */
	void setNullable(boolean value);

	/**
	 * Returns the value of the '<em><b>Contains Target</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Contains Target</em>' attribute.
	 * @see #setContainsTarget(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataNavigationProfile_ContainsTarget()
	 * @model
	 * @generated
	 */
	boolean isContainsTarget();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#isContainsTarget <em>Contains Target</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contains Target</em>' attribute.
	 * @see #isContainsTarget()
	 * @generated
	 */
	void setContainsTarget(boolean value);

	/**
	 * Returns the value of the '<em><b>Partner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Partner</em>' attribute.
	 * @see #setPartner(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataNavigationProfile_Partner()
	 * @model
	 * @generated
	 */
	String getPartner();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getPartner <em>Partner</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Partner</em>' attribute.
	 * @see #getPartner()
	 * @generated
	 */
	void setPartner(String value);

	/**
	 * Returns the value of the '<em><b>On Delete</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>On Delete</em>' attribute.
	 * @see #setOnDelete(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataNavigationProfile_OnDelete()
	 * @model
	 * @generated
	 */
	String getOnDelete();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile#getOnDelete <em>On Delete</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>On Delete</em>' attribute.
	 * @see #getOnDelete()
	 * @generated
	 */
	void setOnDelete(String value);

	/**
	 * Returns the value of the '<em><b>Referential Constraints</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Referential Constraints</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataNavigationProfile_ReferentialConstraints()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataReferentialConstraint> getReferentialConstraints();

	/**
	 * Returns the value of the '<em><b>Annotations</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Annotations</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataNavigationProfile_Annotations()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataAnnotation> getAnnotations();

} // ODataNavigationProfile
