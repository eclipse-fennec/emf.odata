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
package org.eclipse.fennec.odata.metadata.odata.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.metadata.impl.ClassProfileImpl;

import org.eclipse.fennec.odata.metadata.odata.ODataClassProfile;
import org.eclipse.fennec.odata.metadata.odata.OdataPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>OData Class Profile</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl#getOdataProfile <em>Odata Profile</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ODataClassProfileImpl extends ClassProfileImpl implements ODataClassProfile {
	/**
	 * The cached value of the '{@link #getOdataProfile() <em>Odata Profile</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOdataProfile()
	 * @generated
	 * @ordered
	 */
	protected org.eclipse.fennec.odata.csdl.profile.ODataClassProfile odataProfile;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ODataClassProfileImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OdataPackage.Literals.ODATA_CLASS_PROFILE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public org.eclipse.fennec.odata.csdl.profile.ODataClassProfile getOdataProfile() {
		if (odataProfile != null && odataProfile.eIsProxy()) {
			InternalEObject oldOdataProfile = (InternalEObject)odataProfile;
			odataProfile = (org.eclipse.fennec.odata.csdl.profile.ODataClassProfile)eResolveProxy(oldOdataProfile);
			if (odataProfile != oldOdataProfile) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, OdataPackage.ODATA_CLASS_PROFILE__ODATA_PROFILE, oldOdataProfile, odataProfile));
			}
		}
		return odataProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public org.eclipse.fennec.odata.csdl.profile.ODataClassProfile basicGetOdataProfile() {
		return odataProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOdataProfile(org.eclipse.fennec.odata.csdl.profile.ODataClassProfile newOdataProfile) {
		org.eclipse.fennec.odata.csdl.profile.ODataClassProfile oldOdataProfile = odataProfile;
		odataProfile = newOdataProfile;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.ODATA_CLASS_PROFILE__ODATA_PROFILE, oldOdataProfile, odataProfile));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case OdataPackage.ODATA_CLASS_PROFILE__ODATA_PROFILE:
				if (resolve) return getOdataProfile();
				return basicGetOdataProfile();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case OdataPackage.ODATA_CLASS_PROFILE__ODATA_PROFILE:
				setOdataProfile((org.eclipse.fennec.odata.csdl.profile.ODataClassProfile)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case OdataPackage.ODATA_CLASS_PROFILE__ODATA_PROFILE:
				setOdataProfile((org.eclipse.fennec.odata.csdl.profile.ODataClassProfile)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case OdataPackage.ODATA_CLASS_PROFILE__ODATA_PROFILE:
				return odataProfile != null;
		}
		return super.eIsSet(featureID);
	}

} //ODataClassProfileImpl
