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

import org.eclipse.fennec.odata.query.apply.AggregateExpression;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Aggregate Transformation</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.AggregateTransformationImpl#getAggregations <em>Aggregations</em>}</li>
 * </ul>
 *
 * @generated
 */
public class AggregateTransformationImpl extends ApplyTransformationImpl implements AggregateTransformation {
	/**
	 * The cached value of the '{@link #getAggregations() <em>Aggregations</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAggregations()
	 * @generated
	 * @ordered
	 */
	protected EList<AggregateExpression> aggregations;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AggregateTransformationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplyPackage.Literals.AGGREGATE_TRANSFORMATION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<AggregateExpression> getAggregations() {
		if (aggregations == null) {
			aggregations = new EObjectContainmentEList<AggregateExpression>(AggregateExpression.class, this, ApplyPackage.AGGREGATE_TRANSFORMATION__AGGREGATIONS);
		}
		return aggregations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplyPackage.AGGREGATE_TRANSFORMATION__AGGREGATIONS:
				return ((InternalEList<?>)getAggregations()).basicRemove(otherEnd, msgs);
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
			case ApplyPackage.AGGREGATE_TRANSFORMATION__AGGREGATIONS:
				return getAggregations();
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
			case ApplyPackage.AGGREGATE_TRANSFORMATION__AGGREGATIONS:
				getAggregations().clear();
				getAggregations().addAll((Collection<? extends AggregateExpression>)newValue);
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
			case ApplyPackage.AGGREGATE_TRANSFORMATION__AGGREGATIONS:
				getAggregations().clear();
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
			case ApplyPackage.AGGREGATE_TRANSFORMATION__AGGREGATIONS:
				return aggregations != null && !aggregations.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //AggregateTransformationImpl
