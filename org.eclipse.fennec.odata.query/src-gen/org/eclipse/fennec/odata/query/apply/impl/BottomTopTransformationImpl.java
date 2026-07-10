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
package org.eclipse.fennec.odata.query.apply.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.m2x.model.ocl.OclExpression;

import org.eclipse.fennec.odata.query.apply.ApplyPackage;
import org.eclipse.fennec.odata.query.apply.BottomTopMethod;
import org.eclipse.fennec.odata.query.apply.BottomTopTransformation;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Bottom Top Transformation</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.BottomTopTransformationImpl#getMethod <em>Method</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.BottomTopTransformationImpl#getThreshold <em>Threshold</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.BottomTopTransformationImpl#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @generated
 */
public class BottomTopTransformationImpl extends ApplyTransformationImpl implements BottomTopTransformation {
	/**
	 * The default value of the '{@link #getMethod() <em>Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMethod()
	 * @generated
	 * @ordered
	 */
	protected static final BottomTopMethod METHOD_EDEFAULT = BottomTopMethod.TOP_COUNT;

	/**
	 * The cached value of the '{@link #getMethod() <em>Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMethod()
	 * @generated
	 * @ordered
	 */
	protected BottomTopMethod method = METHOD_EDEFAULT;

	/**
	 * The cached value of the '{@link #getThreshold() <em>Threshold</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThreshold()
	 * @generated
	 * @ordered
	 */
	protected OclExpression threshold;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected OclExpression value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BottomTopTransformationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplyPackage.Literals.BOTTOM_TOP_TRANSFORMATION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BottomTopMethod getMethod() {
		return method;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMethod(BottomTopMethod newMethod) {
		BottomTopMethod oldMethod = method;
		method = newMethod == null ? METHOD_EDEFAULT : newMethod;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplyPackage.BOTTOM_TOP_TRANSFORMATION__METHOD, oldMethod, method));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OclExpression getThreshold() {
		return threshold;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetThreshold(OclExpression newThreshold, NotificationChain msgs) {
		OclExpression oldThreshold = threshold;
		threshold = newThreshold;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplyPackage.BOTTOM_TOP_TRANSFORMATION__THRESHOLD, oldThreshold, newThreshold);
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
	public void setThreshold(OclExpression newThreshold) {
		if (newThreshold != threshold) {
			NotificationChain msgs = null;
			if (threshold != null)
				msgs = ((InternalEObject)threshold).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplyPackage.BOTTOM_TOP_TRANSFORMATION__THRESHOLD, null, msgs);
			if (newThreshold != null)
				msgs = ((InternalEObject)newThreshold).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplyPackage.BOTTOM_TOP_TRANSFORMATION__THRESHOLD, null, msgs);
			msgs = basicSetThreshold(newThreshold, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplyPackage.BOTTOM_TOP_TRANSFORMATION__THRESHOLD, newThreshold, newThreshold));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OclExpression getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetValue(OclExpression newValue, NotificationChain msgs) {
		OclExpression oldValue = value;
		value = newValue;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplyPackage.BOTTOM_TOP_TRANSFORMATION__VALUE, oldValue, newValue);
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
	public void setValue(OclExpression newValue) {
		if (newValue != value) {
			NotificationChain msgs = null;
			if (value != null)
				msgs = ((InternalEObject)value).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplyPackage.BOTTOM_TOP_TRANSFORMATION__VALUE, null, msgs);
			if (newValue != null)
				msgs = ((InternalEObject)newValue).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplyPackage.BOTTOM_TOP_TRANSFORMATION__VALUE, null, msgs);
			msgs = basicSetValue(newValue, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplyPackage.BOTTOM_TOP_TRANSFORMATION__VALUE, newValue, newValue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__THRESHOLD:
				return basicSetThreshold(null, msgs);
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__VALUE:
				return basicSetValue(null, msgs);
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
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__METHOD:
				return getMethod();
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__THRESHOLD:
				return getThreshold();
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__VALUE:
				return getValue();
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
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__METHOD:
				setMethod((BottomTopMethod)newValue);
				return;
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__THRESHOLD:
				setThreshold((OclExpression)newValue);
				return;
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__VALUE:
				setValue((OclExpression)newValue);
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
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__METHOD:
				setMethod(METHOD_EDEFAULT);
				return;
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__THRESHOLD:
				setThreshold((OclExpression)null);
				return;
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__VALUE:
				setValue((OclExpression)null);
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
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__METHOD:
				return method != METHOD_EDEFAULT;
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__THRESHOLD:
				return threshold != null;
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION__VALUE:
				return value != null;
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
		result.append(" (method: ");
		result.append(method);
		result.append(')');
		return result.toString();
	}

} //BottomTopTransformationImpl
