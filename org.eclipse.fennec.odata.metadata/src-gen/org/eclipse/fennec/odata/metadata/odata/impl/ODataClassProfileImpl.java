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

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.fennec.model.metadata.impl.ClassProfileImpl;

import org.eclipse.fennec.odata.metadata.odata.ODataClassProfile;
import org.eclipse.fennec.odata.metadata.odata.ODataTypeKind;
import org.eclipse.fennec.odata.metadata.odata.OdataPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>OData Class Profile</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl#getQualifiedName <em>Qualified Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl#getKeyPropertyNames <em>Key Property Names</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl#isOpenType <em>Open Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl#isHasStream <em>Has Stream</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl#getBaseTypeQualifiedName <em>Base Type Qualified Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ODataClassProfileImpl#getOdataProfile <em>Odata Profile</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ODataClassProfileImpl extends ClassProfileImpl implements ODataClassProfile {
	/**
	 * The default value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected static final ODataTypeKind KIND_EDEFAULT = ODataTypeKind.ENTITY;

	/**
	 * The cached value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected ODataTypeKind kind = KIND_EDEFAULT;

	/**
	 * The default value of the '{@link #getQualifiedName() <em>Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQualifiedName()
	 * @generated
	 * @ordered
	 */
	protected static final String QUALIFIED_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getQualifiedName() <em>Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQualifiedName()
	 * @generated
	 * @ordered
	 */
	protected String qualifiedName = QUALIFIED_NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getKeyPropertyNames() <em>Key Property Names</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeyPropertyNames()
	 * @generated
	 * @ordered
	 */
	protected EList<String> keyPropertyNames;

	/**
	 * The default value of the '{@link #isOpenType() <em>Open Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOpenType()
	 * @generated
	 * @ordered
	 */
	protected static final boolean OPEN_TYPE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isOpenType() <em>Open Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOpenType()
	 * @generated
	 * @ordered
	 */
	protected boolean openType = OPEN_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #isHasStream() <em>Has Stream</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHasStream()
	 * @generated
	 * @ordered
	 */
	protected static final boolean HAS_STREAM_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isHasStream() <em>Has Stream</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHasStream()
	 * @generated
	 * @ordered
	 */
	protected boolean hasStream = HAS_STREAM_EDEFAULT;

	/**
	 * The default value of the '{@link #getBaseTypeQualifiedName() <em>Base Type Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseTypeQualifiedName()
	 * @generated
	 * @ordered
	 */
	protected static final String BASE_TYPE_QUALIFIED_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBaseTypeQualifiedName() <em>Base Type Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseTypeQualifiedName()
	 * @generated
	 * @ordered
	 */
	protected String baseTypeQualifiedName = BASE_TYPE_QUALIFIED_NAME_EDEFAULT;

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
	public ODataTypeKind getKind() {
		return kind;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKind(ODataTypeKind newKind) {
		ODataTypeKind oldKind = kind;
		kind = newKind == null ? KIND_EDEFAULT : newKind;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.ODATA_CLASS_PROFILE__KIND, oldKind, kind));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getQualifiedName() {
		return qualifiedName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setQualifiedName(String newQualifiedName) {
		String oldQualifiedName = qualifiedName;
		qualifiedName = newQualifiedName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.ODATA_CLASS_PROFILE__QUALIFIED_NAME, oldQualifiedName, qualifiedName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getKeyPropertyNames() {
		if (keyPropertyNames == null) {
			keyPropertyNames = new EDataTypeUniqueEList<String>(String.class, this, OdataPackage.ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES);
		}
		return keyPropertyNames;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isOpenType() {
		return openType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOpenType(boolean newOpenType) {
		boolean oldOpenType = openType;
		openType = newOpenType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.ODATA_CLASS_PROFILE__OPEN_TYPE, oldOpenType, openType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isHasStream() {
		return hasStream;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHasStream(boolean newHasStream) {
		boolean oldHasStream = hasStream;
		hasStream = newHasStream;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.ODATA_CLASS_PROFILE__HAS_STREAM, oldHasStream, hasStream));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getBaseTypeQualifiedName() {
		return baseTypeQualifiedName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBaseTypeQualifiedName(String newBaseTypeQualifiedName) {
		String oldBaseTypeQualifiedName = baseTypeQualifiedName;
		baseTypeQualifiedName = newBaseTypeQualifiedName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME, oldBaseTypeQualifiedName, baseTypeQualifiedName));
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
			case OdataPackage.ODATA_CLASS_PROFILE__KIND:
				return getKind();
			case OdataPackage.ODATA_CLASS_PROFILE__QUALIFIED_NAME:
				return getQualifiedName();
			case OdataPackage.ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES:
				return getKeyPropertyNames();
			case OdataPackage.ODATA_CLASS_PROFILE__OPEN_TYPE:
				return isOpenType();
			case OdataPackage.ODATA_CLASS_PROFILE__HAS_STREAM:
				return isHasStream();
			case OdataPackage.ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME:
				return getBaseTypeQualifiedName();
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
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case OdataPackage.ODATA_CLASS_PROFILE__KIND:
				setKind((ODataTypeKind)newValue);
				return;
			case OdataPackage.ODATA_CLASS_PROFILE__QUALIFIED_NAME:
				setQualifiedName((String)newValue);
				return;
			case OdataPackage.ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES:
				getKeyPropertyNames().clear();
				getKeyPropertyNames().addAll((Collection<? extends String>)newValue);
				return;
			case OdataPackage.ODATA_CLASS_PROFILE__OPEN_TYPE:
				setOpenType((Boolean)newValue);
				return;
			case OdataPackage.ODATA_CLASS_PROFILE__HAS_STREAM:
				setHasStream((Boolean)newValue);
				return;
			case OdataPackage.ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME:
				setBaseTypeQualifiedName((String)newValue);
				return;
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
			case OdataPackage.ODATA_CLASS_PROFILE__KIND:
				setKind(KIND_EDEFAULT);
				return;
			case OdataPackage.ODATA_CLASS_PROFILE__QUALIFIED_NAME:
				setQualifiedName(QUALIFIED_NAME_EDEFAULT);
				return;
			case OdataPackage.ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES:
				getKeyPropertyNames().clear();
				return;
			case OdataPackage.ODATA_CLASS_PROFILE__OPEN_TYPE:
				setOpenType(OPEN_TYPE_EDEFAULT);
				return;
			case OdataPackage.ODATA_CLASS_PROFILE__HAS_STREAM:
				setHasStream(HAS_STREAM_EDEFAULT);
				return;
			case OdataPackage.ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME:
				setBaseTypeQualifiedName(BASE_TYPE_QUALIFIED_NAME_EDEFAULT);
				return;
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
			case OdataPackage.ODATA_CLASS_PROFILE__KIND:
				return kind != KIND_EDEFAULT;
			case OdataPackage.ODATA_CLASS_PROFILE__QUALIFIED_NAME:
				return QUALIFIED_NAME_EDEFAULT == null ? qualifiedName != null : !QUALIFIED_NAME_EDEFAULT.equals(qualifiedName);
			case OdataPackage.ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES:
				return keyPropertyNames != null && !keyPropertyNames.isEmpty();
			case OdataPackage.ODATA_CLASS_PROFILE__OPEN_TYPE:
				return openType != OPEN_TYPE_EDEFAULT;
			case OdataPackage.ODATA_CLASS_PROFILE__HAS_STREAM:
				return hasStream != HAS_STREAM_EDEFAULT;
			case OdataPackage.ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME:
				return BASE_TYPE_QUALIFIED_NAME_EDEFAULT == null ? baseTypeQualifiedName != null : !BASE_TYPE_QUALIFIED_NAME_EDEFAULT.equals(baseTypeQualifiedName);
			case OdataPackage.ODATA_CLASS_PROFILE__ODATA_PROFILE:
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
		result.append(" (kind: ");
		result.append(kind);
		result.append(", qualifiedName: ");
		result.append(qualifiedName);
		result.append(", keyPropertyNames: ");
		result.append(keyPropertyNames);
		result.append(", openType: ");
		result.append(openType);
		result.append(", hasStream: ");
		result.append(hasStream);
		result.append(", baseTypeQualifiedName: ");
		result.append(baseTypeQualifiedName);
		result.append(')');
		return result.toString();
	}

} //ODataClassProfileImpl
