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

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.m2x.model.ocl.OclExpression;

import org.eclipse.fennec.odata.query.apply.ApplyPackage;
import org.eclipse.fennec.odata.query.apply.ApplyTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Group By Transformation</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.GroupByTransformationImpl#getGroupingProperties <em>Grouping Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.GroupByTransformationImpl#getThen <em>Then</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GroupByTransformationImpl extends ApplyTransformationImpl implements GroupByTransformation {
	/**
	 * The cached value of the '{@link #getGroupingProperties() <em>Grouping Properties</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGroupingProperties()
	 * @generated
	 * @ordered
	 */
	protected EList<OclExpression> groupingProperties;

	/**
	 * The cached value of the '{@link #getThen() <em>Then</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getThen()
	 * @generated
	 * @ordered
	 */
	protected ApplyTransformation then;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GroupByTransformationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplyPackage.Literals.GROUP_BY_TRANSFORMATION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<OclExpression> getGroupingProperties() {
		if (groupingProperties == null) {
			groupingProperties = new EObjectContainmentEList<OclExpression>(OclExpression.class, this, ApplyPackage.GROUP_BY_TRANSFORMATION__GROUPING_PROPERTIES);
		}
		return groupingProperties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ApplyTransformation getThen() {
		return then;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetThen(ApplyTransformation newThen, NotificationChain msgs) {
		ApplyTransformation oldThen = then;
		then = newThen;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, ApplyPackage.GROUP_BY_TRANSFORMATION__THEN, oldThen, newThen);
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
	public void setThen(ApplyTransformation newThen) {
		if (newThen != then) {
			NotificationChain msgs = null;
			if (then != null)
				msgs = ((InternalEObject)then).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - ApplyPackage.GROUP_BY_TRANSFORMATION__THEN, null, msgs);
			if (newThen != null)
				msgs = ((InternalEObject)newThen).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - ApplyPackage.GROUP_BY_TRANSFORMATION__THEN, null, msgs);
			msgs = basicSetThen(newThen, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplyPackage.GROUP_BY_TRANSFORMATION__THEN, newThen, newThen));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplyPackage.GROUP_BY_TRANSFORMATION__GROUPING_PROPERTIES:
				return ((InternalEList<?>)getGroupingProperties()).basicRemove(otherEnd, msgs);
			case ApplyPackage.GROUP_BY_TRANSFORMATION__THEN:
				return basicSetThen(null, msgs);
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
			case ApplyPackage.GROUP_BY_TRANSFORMATION__GROUPING_PROPERTIES:
				return getGroupingProperties();
			case ApplyPackage.GROUP_BY_TRANSFORMATION__THEN:
				return getThen();
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
			case ApplyPackage.GROUP_BY_TRANSFORMATION__GROUPING_PROPERTIES:
				getGroupingProperties().clear();
				getGroupingProperties().addAll((Collection<? extends OclExpression>)newValue);
				return;
			case ApplyPackage.GROUP_BY_TRANSFORMATION__THEN:
				setThen((ApplyTransformation)newValue);
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
			case ApplyPackage.GROUP_BY_TRANSFORMATION__GROUPING_PROPERTIES:
				getGroupingProperties().clear();
				return;
			case ApplyPackage.GROUP_BY_TRANSFORMATION__THEN:
				setThen((ApplyTransformation)null);
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
			case ApplyPackage.GROUP_BY_TRANSFORMATION__GROUPING_PROPERTIES:
				return groupingProperties != null && !groupingProperties.isEmpty();
			case ApplyPackage.GROUP_BY_TRANSFORMATION__THEN:
				return then != null;
		}
		return super.eIsSet(featureID);
	}

} //GroupByTransformationImpl
