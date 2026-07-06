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
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.metadata.impl.PackageProfileImpl;

import org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile;
import org.eclipse.fennec.odata.metadata.odata.OdataPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>OData Package Profile</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataPackageProfileImpl#getNamespace <em>Namespace</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataPackageProfileImpl#getOdataProfile <em>Odata Profile</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ODataPackageProfileImpl extends PackageProfileImpl implements ODataPackageProfile {
	/**
	 * The default value of the '{@link #getNamespace() <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespace()
	 * @generated
	 * @ordered
	 */
	protected static final String NAMESPACE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNamespace() <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespace()
	 * @generated
	 * @ordered
	 */
	protected String namespace = NAMESPACE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getOdataProfile() <em>Odata Profile</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOdataProfile()
	 * @generated
	 * @ordered
	 */
	protected org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile odataProfile;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ODataPackageProfileImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OdataPackage.Literals.ODATA_PACKAGE_PROFILE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNamespace() {
		return namespace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNamespace(String newNamespace) {
		String oldNamespace = namespace;
		namespace = newNamespace;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.ODATA_PACKAGE_PROFILE__NAMESPACE, oldNamespace, namespace));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile getOdataProfile() {
		return odataProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetOdataProfile(org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile newOdataProfile, NotificationChain msgs) {
		org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile oldOdataProfile = odataProfile;
		odataProfile = newOdataProfile;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, OdataPackage.ODATA_PACKAGE_PROFILE__ODATA_PROFILE, oldOdataProfile, newOdataProfile);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOdataProfile(org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile newOdataProfile) {
		if (newOdataProfile != odataProfile) {
			NotificationChain msgs = null;
			if (odataProfile != null)
				msgs = ((InternalEObject)odataProfile).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - OdataPackage.ODATA_PACKAGE_PROFILE__ODATA_PROFILE, null, msgs);
			if (newOdataProfile != null)
				msgs = ((InternalEObject)newOdataProfile).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - OdataPackage.ODATA_PACKAGE_PROFILE__ODATA_PROFILE, null, msgs);
			msgs = basicSetOdataProfile(newOdataProfile, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.ODATA_PACKAGE_PROFILE__ODATA_PROFILE, newOdataProfile, newOdataProfile));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case OdataPackage.ODATA_PACKAGE_PROFILE__ODATA_PROFILE:
				return basicSetOdataProfile(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case OdataPackage.ODATA_PACKAGE_PROFILE__NAMESPACE:
				return getNamespace();
			case OdataPackage.ODATA_PACKAGE_PROFILE__ODATA_PROFILE:
				return getOdataProfile();
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
			case OdataPackage.ODATA_PACKAGE_PROFILE__NAMESPACE:
				setNamespace((String)newValue);
				return;
			case OdataPackage.ODATA_PACKAGE_PROFILE__ODATA_PROFILE:
				setOdataProfile((org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile)newValue);
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
			case OdataPackage.ODATA_PACKAGE_PROFILE__NAMESPACE:
				setNamespace(NAMESPACE_EDEFAULT);
				return;
			case OdataPackage.ODATA_PACKAGE_PROFILE__ODATA_PROFILE:
				setOdataProfile((org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile)null);
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
			case OdataPackage.ODATA_PACKAGE_PROFILE__NAMESPACE:
				return NAMESPACE_EDEFAULT == null ? namespace != null : !NAMESPACE_EDEFAULT.equals(namespace);
			case OdataPackage.ODATA_PACKAGE_PROFILE__ODATA_PROFILE:
				return odataProfile != null;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (namespace: ");
		result.append(namespace);
		result.append(')');
		return result.toString();
	}

} //ODataPackageProfileImpl
