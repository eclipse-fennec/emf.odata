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
 * A representation of the model object '<em><b>OData Package Profile</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Resolved profile for one EPackage = one CSDL Schema. Everything the EDM builder needs, no Ecore re-walk required.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getNamespace <em>Namespace</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getAlias <em>Alias</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getContainerName <em>Container Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getEnums <em>Enums</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getClasses <em>Classes</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getAnnotations <em>Annotations</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPackageProfile()
 * @model
 * @generated
 */
@ProviderType
public interface ODataPackageProfile extends EObject {
	/**
	 * Returns the value of the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Namespace</em>' attribute.
	 * @see #setNamespace(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPackageProfile_Namespace()
	 * @model
	 * @generated
	 */
	String getNamespace();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getNamespace <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Namespace</em>' attribute.
	 * @see #getNamespace()
	 * @generated
	 */
	void setNamespace(String value);

	/**
	 * Returns the value of the '<em><b>Alias</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Alias</em>' attribute.
	 * @see #setAlias(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPackageProfile_Alias()
	 * @model
	 * @generated
	 */
	String getAlias();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getAlias <em>Alias</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Alias</em>' attribute.
	 * @see #getAlias()
	 * @generated
	 */
	void setAlias(String value);

	/**
	 * Returns the value of the '<em><b>Container Name</b></em>' attribute.
	 * The default value is <code>"DefaultContainer"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Container Name</em>' attribute.
	 * @see #setContainerName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPackageProfile_ContainerName()
	 * @model default="DefaultContainer"
	 * @generated
	 */
	String getContainerName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile#getContainerName <em>Container Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Container Name</em>' attribute.
	 * @see #getContainerName()
	 * @generated
	 */
	void setContainerName(String value);

	/**
	 * Returns the value of the '<em><b>Enums</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Enums</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPackageProfile_Enums()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataEnumProfile> getEnums();

	/**
	 * Returns the value of the '<em><b>Classes</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Classes</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPackageProfile_Classes()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataClassProfile> getClasses();

	/**
	 * Returns the value of the '<em><b>Annotations</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Annotations</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPackageProfile_Annotations()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataAnnotation> getAnnotations();

} // ODataPackageProfile
