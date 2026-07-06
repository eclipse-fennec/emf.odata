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
 * A representation of the model object '<em><b>OData Class Profile</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Resolved profile for one EClass: kind, key, facets, base type, and the resolved property/navigation/operation lists.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getQualifiedName <em>Qualified Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isAbstract <em>Abstract</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isOpenType <em>Open Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isHasStream <em>Has Stream</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getBaseTypeQualifiedName <em>Base Type Qualified Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getKeyPropertyNames <em>Key Property Names</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getNavigationProperties <em>Navigation Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getOperations <em>Operations</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getAnnotations <em>Annotations</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile()
 * @model
 * @generated
 */
@ProviderType
public interface ODataClassProfile extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Qualified Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Qualified Name</em>' attribute.
	 * @see #setQualifiedName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_QualifiedName()
	 * @model
	 * @generated
	 */
	String getQualifiedName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getQualifiedName <em>Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Qualified Name</em>' attribute.
	 * @see #getQualifiedName()
	 * @generated
	 */
	void setQualifiedName(String value);

	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.odata.csdl.profile.ODataTypeKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataTypeKind
	 * @see #setKind(ODataTypeKind)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_Kind()
	 * @model
	 * @generated
	 */
	ODataTypeKind getKind();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataTypeKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(ODataTypeKind value);

	/**
	 * Returns the value of the '<em><b>Abstract</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Abstract</em>' attribute.
	 * @see #setAbstract(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_Abstract()
	 * @model
	 * @generated
	 */
	boolean isAbstract();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isAbstract <em>Abstract</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Abstract</em>' attribute.
	 * @see #isAbstract()
	 * @generated
	 */
	void setAbstract(boolean value);

	/**
	 * Returns the value of the '<em><b>Open Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Open Type</em>' attribute.
	 * @see #setOpenType(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_OpenType()
	 * @model
	 * @generated
	 */
	boolean isOpenType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isOpenType <em>Open Type</em>}' attribute.
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
	 * @return the value of the '<em>Has Stream</em>' attribute.
	 * @see #setHasStream(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_HasStream()
	 * @model
	 * @generated
	 */
	boolean isHasStream();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#isHasStream <em>Has Stream</em>}' attribute.
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
	 * @return the value of the '<em>Base Type Qualified Name</em>' attribute.
	 * @see #setBaseTypeQualifiedName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_BaseTypeQualifiedName()
	 * @model
	 * @generated
	 */
	String getBaseTypeQualifiedName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile#getBaseTypeQualifiedName <em>Base Type Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Base Type Qualified Name</em>' attribute.
	 * @see #getBaseTypeQualifiedName()
	 * @generated
	 */
	void setBaseTypeQualifiedName(String value);

	/**
	 * Returns the value of the '<em><b>Key Property Names</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Key Property Names</em>' attribute list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_KeyPropertyNames()
	 * @model
	 * @generated
	 */
	EList<String> getKeyPropertyNames();

	/**
	 * Returns the value of the '<em><b>Properties</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Properties</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_Properties()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataPropertyProfile> getProperties();

	/**
	 * Returns the value of the '<em><b>Navigation Properties</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Navigation Properties</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_NavigationProperties()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataNavigationProfile> getNavigationProperties();

	/**
	 * Returns the value of the '<em><b>Operations</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Operations</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_Operations()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataOperationProfile> getOperations();

	/**
	 * Returns the value of the '<em><b>Annotations</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Annotations</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataClassProfile_Annotations()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataAnnotation> getAnnotations();

} // ODataClassProfile
