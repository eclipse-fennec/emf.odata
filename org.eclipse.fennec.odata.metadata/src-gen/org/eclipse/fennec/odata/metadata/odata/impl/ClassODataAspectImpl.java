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

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.metadata.impl.ClassAspectImpl;

import org.eclipse.fennec.odata.metadata.odata.ClassODataAspect;
import org.eclipse.fennec.odata.metadata.odata.ODataTypeKind;
import org.eclipse.fennec.odata.metadata.odata.OdataPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Class OData Aspect</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ClassODataAspectImpl#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ClassODataAspectImpl#isHasStream <em>Has Stream</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ClassODataAspectImpl#isOpenType <em>Open Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ClassODataAspectImpl#isAbstract <em>Abstract</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ClassODataAspectImpl extends ClassAspectImpl implements ClassODataAspect {
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
	 * The default value of the '{@link #isAbstract() <em>Abstract</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAbstract()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ABSTRACT_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isAbstract() <em>Abstract</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAbstract()
	 * @generated
	 * @ordered
	 */
	protected boolean abstract_ = ABSTRACT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ClassODataAspectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OdataPackage.Literals.CLASS_ODATA_ASPECT;
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
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.CLASS_ODATA_ASPECT__KIND, oldKind, kind));
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
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.CLASS_ODATA_ASPECT__HAS_STREAM, oldHasStream, hasStream));
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
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.CLASS_ODATA_ASPECT__OPEN_TYPE, oldOpenType, openType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAbstract() {
		return abstract_;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAbstract(boolean newAbstract) {
		boolean oldAbstract = abstract_;
		abstract_ = newAbstract;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.CLASS_ODATA_ASPECT__ABSTRACT, oldAbstract, abstract_));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case OdataPackage.CLASS_ODATA_ASPECT__KIND:
				return getKind();
			case OdataPackage.CLASS_ODATA_ASPECT__HAS_STREAM:
				return isHasStream();
			case OdataPackage.CLASS_ODATA_ASPECT__OPEN_TYPE:
				return isOpenType();
			case OdataPackage.CLASS_ODATA_ASPECT__ABSTRACT:
				return isAbstract();
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
			case OdataPackage.CLASS_ODATA_ASPECT__KIND:
				setKind((ODataTypeKind)newValue);
				return;
			case OdataPackage.CLASS_ODATA_ASPECT__HAS_STREAM:
				setHasStream((Boolean)newValue);
				return;
			case OdataPackage.CLASS_ODATA_ASPECT__OPEN_TYPE:
				setOpenType((Boolean)newValue);
				return;
			case OdataPackage.CLASS_ODATA_ASPECT__ABSTRACT:
				setAbstract((Boolean)newValue);
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
			case OdataPackage.CLASS_ODATA_ASPECT__KIND:
				setKind(KIND_EDEFAULT);
				return;
			case OdataPackage.CLASS_ODATA_ASPECT__HAS_STREAM:
				setHasStream(HAS_STREAM_EDEFAULT);
				return;
			case OdataPackage.CLASS_ODATA_ASPECT__OPEN_TYPE:
				setOpenType(OPEN_TYPE_EDEFAULT);
				return;
			case OdataPackage.CLASS_ODATA_ASPECT__ABSTRACT:
				setAbstract(ABSTRACT_EDEFAULT);
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
			case OdataPackage.CLASS_ODATA_ASPECT__KIND:
				return kind != KIND_EDEFAULT;
			case OdataPackage.CLASS_ODATA_ASPECT__HAS_STREAM:
				return hasStream != HAS_STREAM_EDEFAULT;
			case OdataPackage.CLASS_ODATA_ASPECT__OPEN_TYPE:
				return openType != OPEN_TYPE_EDEFAULT;
			case OdataPackage.CLASS_ODATA_ASPECT__ABSTRACT:
				return abstract_ != ABSTRACT_EDEFAULT;
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
		result.append(", hasStream: ");
		result.append(hasStream);
		result.append(", openType: ");
		result.append(openType);
		result.append(", abstract: ");
		result.append(abstract_);
		result.append(')');
		return result.toString();
	}

} //ClassODataAspectImpl
