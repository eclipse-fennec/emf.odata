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

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.odata.query.apply.ApplyPackage;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Compute Transformation</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.ComputeTransformationImpl#getComputeExpressions <em>Compute Expressions</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ComputeTransformationImpl extends ApplyTransformationImpl implements ComputeTransformation {
	/**
	 * The cached value of the '{@link #getComputeExpressions() <em>Compute Expressions</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComputeExpressions()
	 * @generated
	 * @ordered
	 */
	protected EList<ComputeExpression> computeExpressions;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ComputeTransformationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplyPackage.Literals.COMPUTE_TRANSFORMATION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ComputeExpression> getComputeExpressions() {
		if (computeExpressions == null) {
			computeExpressions = new EObjectContainmentEList<ComputeExpression>(ComputeExpression.class, this, ApplyPackage.COMPUTE_TRANSFORMATION__COMPUTE_EXPRESSIONS);
		}
		return computeExpressions;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplyPackage.COMPUTE_TRANSFORMATION__COMPUTE_EXPRESSIONS:
				return ((InternalEList<?>)getComputeExpressions()).basicRemove(otherEnd, msgs);
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
			case ApplyPackage.COMPUTE_TRANSFORMATION__COMPUTE_EXPRESSIONS:
				return getComputeExpressions();
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
			case ApplyPackage.COMPUTE_TRANSFORMATION__COMPUTE_EXPRESSIONS:
				getComputeExpressions().clear();
				getComputeExpressions().addAll((Collection<? extends ComputeExpression>)newValue);
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
			case ApplyPackage.COMPUTE_TRANSFORMATION__COMPUTE_EXPRESSIONS:
				getComputeExpressions().clear();
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
			case ApplyPackage.COMPUTE_TRANSFORMATION__COMPUTE_EXPRESSIONS:
				return computeExpressions != null && !computeExpressions.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //ComputeTransformationImpl
