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
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.m2x.model.ocl.OclExpression;

import org.eclipse.fennec.odata.query.apply.ApplyPackage;
import org.eclipse.fennec.odata.query.apply.OrderByExpression;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Order By Expression</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.OrderByExpressionImpl#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.OrderByExpressionImpl#isAscending <em>Ascending</em>}</li>
 * </ul>
 *
 * @generated
 */
public class OrderByExpressionImpl extends MinimalEObjectImpl.Container implements OrderByExpression {
	/**
	 * The cached value of the '{@link #getExpression() <em>Expression</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExpression()
	 * @generated
	 * @ordered
	 */
	protected OclExpression expression;

	/**
	 * The default value of the '{@link #isAscending() <em>Ascending</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAscending()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ASCENDING_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isAscending() <em>Ascending</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAscending()
	 * @generated
	 * @ordered
	 */
	protected boolean ascending = ASCENDING_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OrderByExpressionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplyPackage.Literals.ORDER_BY_EXPRESSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OclExpression getExpression() {
		return expression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetExpression(OclExpression newExpression, NotificationChain msgs) {
		OclExpression oldExpression = expression;
		expression = newExpression;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplyPackage.ORDER_BY_EXPRESSION__EXPRESSION, oldExpression, newExpression);
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
	public void setExpression(OclExpression newExpression) {
		if (newExpression != expression) {
			NotificationChain msgs = null;
			if (expression != null)
				msgs = ((InternalEObject)expression).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplyPackage.ORDER_BY_EXPRESSION__EXPRESSION, null, msgs);
			if (newExpression != null)
				msgs = ((InternalEObject)newExpression).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplyPackage.ORDER_BY_EXPRESSION__EXPRESSION, null, msgs);
			msgs = basicSetExpression(newExpression, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplyPackage.ORDER_BY_EXPRESSION__EXPRESSION, newExpression, newExpression));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAscending() {
		return ascending;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAscending(boolean newAscending) {
		boolean oldAscending = ascending;
		ascending = newAscending;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplyPackage.ORDER_BY_EXPRESSION__ASCENDING, oldAscending, ascending));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplyPackage.ORDER_BY_EXPRESSION__EXPRESSION:
				return basicSetExpression(null, msgs);
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
			case ApplyPackage.ORDER_BY_EXPRESSION__EXPRESSION:
				return getExpression();
			case ApplyPackage.ORDER_BY_EXPRESSION__ASCENDING:
				return isAscending();
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
			case ApplyPackage.ORDER_BY_EXPRESSION__EXPRESSION:
				setExpression((OclExpression)newValue);
				return;
			case ApplyPackage.ORDER_BY_EXPRESSION__ASCENDING:
				setAscending((Boolean)newValue);
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
			case ApplyPackage.ORDER_BY_EXPRESSION__EXPRESSION:
				setExpression((OclExpression)null);
				return;
			case ApplyPackage.ORDER_BY_EXPRESSION__ASCENDING:
				setAscending(ASCENDING_EDEFAULT);
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
			case ApplyPackage.ORDER_BY_EXPRESSION__EXPRESSION:
				return expression != null;
			case ApplyPackage.ORDER_BY_EXPRESSION__ASCENDING:
				return ascending != ASCENDING_EDEFAULT;
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
		result.append(" (ascending: ");
		result.append(ascending);
		result.append(')');
		return result.toString();
	}

} //OrderByExpressionImpl
