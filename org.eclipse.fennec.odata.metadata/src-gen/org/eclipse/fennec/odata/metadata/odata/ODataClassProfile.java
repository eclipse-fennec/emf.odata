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

import org.eclipse.fennec.model.metadata.ClassProfile;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>OData Class Profile</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Pre-computed OData profile for one EClass: a cross-reference into the composed CSDL class profile (single source — the previously duplicated resolved fields were removed, AP-1c cleanup).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
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
