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
 * A representation of the model object '<em><b>OData Operation Profile</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Resolved CSDL Function/Action (from an EOperation). Bound operations carry bindingTypeName (the owning type); unbound ones omit it and surface as Function/ActionImport.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isBound <em>Bound</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isComposable <em>Composable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getBindingTypeName <em>Binding Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getReturnTypeName <em>Return Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isReturnNullable <em>Return Nullable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getEntitySetPath <em>Entity Set Path</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getParameters <em>Parameters</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataOperationProfile()
 * @model
 * @generated
 */
@ProviderType
public interface ODataOperationProfile extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataOperationProfile_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.odata.csdl.profile.ODataOperationKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationKind
	 * @see #setKind(ODataOperationKind)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataOperationProfile_Kind()
	 * @model
	 * @generated
	 */
	ODataOperationKind getKind();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(ODataOperationKind value);

	/**
	 * Returns the value of the '<em><b>Bound</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Bound</em>' attribute.
	 * @see #setBound(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataOperationProfile_Bound()
	 * @model default="true"
	 * @generated
	 */
	boolean isBound();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isBound <em>Bound</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Bound</em>' attribute.
	 * @see #isBound()
	 * @generated
	 */
	void setBound(boolean value);

	/**
	 * Returns the value of the '<em><b>Composable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Composable</em>' attribute.
	 * @see #setComposable(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataOperationProfile_Composable()
	 * @model
	 * @generated
	 */
	boolean isComposable();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isComposable <em>Composable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Composable</em>' attribute.
	 * @see #isComposable()
	 * @generated
	 */
	void setComposable(boolean value);

	/**
	 * Returns the value of the '<em><b>Binding Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Binding Type Name</em>' attribute.
	 * @see #setBindingTypeName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataOperationProfile_BindingTypeName()
	 * @model
	 * @generated
	 */
	String getBindingTypeName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getBindingTypeName <em>Binding Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Binding Type Name</em>' attribute.
	 * @see #getBindingTypeName()
	 * @generated
	 */
	void setBindingTypeName(String value);

	/**
	 * Returns the value of the '<em><b>Return Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Return Type Name</em>' attribute.
	 * @see #setReturnTypeName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataOperationProfile_ReturnTypeName()
	 * @model
	 * @generated
	 */
	String getReturnTypeName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getReturnTypeName <em>Return Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Return Type Name</em>' attribute.
	 * @see #getReturnTypeName()
	 * @generated
	 */
	void setReturnTypeName(String value);

	/**
	 * Returns the value of the '<em><b>Return Nullable</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Return Nullable</em>' attribute.
	 * @see #setReturnNullable(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataOperationProfile_ReturnNullable()
	 * @model default="true"
	 * @generated
	 */
	boolean isReturnNullable();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#isReturnNullable <em>Return Nullable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Return Nullable</em>' attribute.
	 * @see #isReturnNullable()
	 * @generated
	 */
	void setReturnNullable(boolean value);

	/**
	 * Returns the value of the '<em><b>Entity Set Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Entity Set Path</em>' attribute.
	 * @see #setEntitySetPath(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataOperationProfile_EntitySetPath()
	 * @model
	 * @generated
	 */
	String getEntitySetPath();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile#getEntitySetPath <em>Entity Set Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Entity Set Path</em>' attribute.
	 * @see #getEntitySetPath()
	 * @generated
	 */
	void setEntitySetPath(String value);

	/**
	 * Returns the value of the '<em><b>Parameters</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parameters</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataOperationProfile_Parameters()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataParameterProfile> getParameters();

} // ODataOperationProfile
