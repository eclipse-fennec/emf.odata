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

import org.eclipse.fennec.model.metadata.PackageProfile;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>OData Package Profile</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Pre-computed OData profile for an EPackage: the Schema namespace plus the ODataClassProfiles (inherited classProfiles). Consumed by the CSDL layer for O(1) build.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile#getNamespace <em>Namespace</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile#getOdataProfile <em>Odata Profile</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataPackageProfile()
 * @model
 * @generated
 */
@ProviderType
public interface ODataPackageProfile extends PackageProfile {
	/**
	 * Returns the value of the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Resolved CSDL Schema Namespace.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Namespace</em>' attribute.
	 * @see #setNamespace(String)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataPackageProfile_Namespace()
	 * @model
	 * @generated
	 */
	String getNamespace();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile#getNamespace <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Namespace</em>' attribute.
	 * @see #getNamespace()
	 * @generated
	 */
	void setNamespace(String value);

	/**
	 * Returns the value of the '<em><b>Odata Profile</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * ADR-0003 Phase 2: the composed standalone CSDL profile, produced by the converter's OdataResolver and set by the ODataAspectProvider.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Odata Profile</em>' containment reference.
	 * @see #setOdataProfile(org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataPackageProfile_OdataProfile()
	 * @model containment="true"
	 * @generated
	 */
	org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile getOdataProfile();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile#getOdataProfile <em>Odata Profile</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Odata Profile</em>' containment reference.
	 * @see #getOdataProfile()
	 * @generated
	 */
	void setOdataProfile(org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile value);

} // ODataPackageProfile
