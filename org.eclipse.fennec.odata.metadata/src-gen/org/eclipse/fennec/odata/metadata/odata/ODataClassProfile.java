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

import org.eclipse.emf.common.util.EList;

import org.eclipse.fennec.model.metadata.ClassProfile;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>OData Class Profile</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Pre-computed OData profile for one EClass: everything the CSDL EDM mapper needs without re-resolving annotations.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getQualifiedName <em>Qualified Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getKeyPropertyNames <em>Key Property Names</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#isOpenType <em>Open Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#isHasStream <em>Has Stream</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getBaseTypeQualifiedName <em>Base Type Qualified Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getOdataProfile <em>Odata Profile</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataClassProfile()
 * @model
 * @generated
 */
@ProviderType
public interface ODataClassProfile extends ClassProfile {
	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.odata.metadata.odata.ODataTypeKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * ENTITY or COMPLEX (resolved).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataTypeKind
	 * @see #setKind(ODataTypeKind)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataClassProfile_Kind()
	 * @model
	 * @generated
	 */
	ODataTypeKind getKind();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataTypeKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(ODataTypeKind value);

	/**
	 * Returns the value of the '<em><b>Qualified Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Namespace-qualified type name (e.g. Demo.Person).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Qualified Name</em>' attribute.
	 * @see #setQualifiedName(String)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataClassProfile_QualifiedName()
	 * @model
	 * @generated
	 */
	String getQualifiedName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getQualifiedName <em>Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Qualified Name</em>' attribute.
	 * @see #getQualifiedName()
	 * @generated
	 */
	void setQualifiedName(String value);

	/**
	 * Returns the value of the '<em><b>Key Property Names</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Resolved key property names (empty for complex types).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Key Property Names</em>' attribute list.
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataClassProfile_KeyPropertyNames()
	 * @model
	 * @generated
	 */
	EList<String> getKeyPropertyNames();

	/**
	 * Returns the value of the '<em><b>Open Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Resolved OpenType.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Open Type</em>' attribute.
	 * @see #setOpenType(boolean)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataClassProfile_OpenType()
	 * @model
	 * @generated
	 */
	boolean isOpenType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#isOpenType <em>Open Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Open Type</em>' attribute.
	 * @see #isOpenType()
	 * @generated
	 */
	void setOpenType(boolean value);

	/**
	 * Returns the value of the '<em><b>Has Stream</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Resolved HasStream.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Has Stream</em>' attribute.
	 * @see #setHasStream(boolean)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataClassProfile_HasStream()
	 * @model
	 * @generated
	 */
	boolean isHasStream();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#isHasStream <em>Has Stream</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Has Stream</em>' attribute.
	 * @see #isHasStream()
	 * @generated
	 */
	void setHasStream(boolean value);

	/**
	 * Returns the value of the '<em><b>Base Type Qualified Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Qualified name of the CSDL BaseType, or unset for a root type.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Base Type Qualified Name</em>' attribute.
	 * @see #setBaseTypeQualifiedName(String)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataClassProfile_BaseTypeQualifiedName()
	 * @model
	 * @generated
	 */
	String getBaseTypeQualifiedName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getBaseTypeQualifiedName <em>Base Type Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Base Type Qualified Name</em>' attribute.
	 * @see #getBaseTypeQualifiedName()
	 * @generated
	 */
	void setBaseTypeQualifiedName(String value);

	/**
	 * Returns the value of the '<em><b>Odata Profile</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * ADR-0003 Phase 2: cross-reference to the composed CSDL class profile, which lives inside the package profile's odataProfile containment tree. Non-containment.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Odata Profile</em>' reference.
	 * @see #setOdataProfile(org.eclipse.fennec.odata.csdl.profile.ODataClassProfile)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataClassProfile_OdataProfile()
	 * @model
	 * @generated
	 */
	org.eclipse.fennec.odata.csdl.profile.ODataClassProfile getOdataProfile();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile#getOdataProfile <em>Odata Profile</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Odata Profile</em>' reference.
	 * @see #getOdataProfile()
	 * @generated
	 */
	void setOdataProfile(org.eclipse.fennec.odata.csdl.profile.ODataClassProfile value);

} // ODataClassProfile
