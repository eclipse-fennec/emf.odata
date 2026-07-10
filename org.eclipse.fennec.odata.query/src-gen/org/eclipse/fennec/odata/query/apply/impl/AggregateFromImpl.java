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
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.m2x.model.ocl.OclExpression;

import org.eclipse.fennec.odata.query.apply.AggregateFrom;
import org.eclipse.fennec.odata.query.apply.AggregateMethod;
import org.eclipse.fennec.odata.query.apply.ApplyPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Aggregate From</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.AggregateFromImpl#getGroupingProperties <em>Grouping Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.AggregateFromImpl#getMethod <em>Method</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.impl.AggregateFromImpl#getCustomMethod <em>Custom Method</em>}</li>
 * </ul>
 *
 * @generated
 */
public class AggregateFromImpl extends MinimalEObjectImpl.Container implements AggregateFrom {
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
	 * The default value of the '{@link #getMethod() <em>Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMethod()
	 * @generated
	 * @ordered
	 */
	protected static final AggregateMethod METHOD_EDEFAULT = AggregateMethod.SUM;

	/**
	 * The cached value of the '{@link #getMethod() <em>Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMethod()
	 * @generated
	 * @ordered
	 */
	protected AggregateMethod method = METHOD_EDEFAULT;

	/**
	 * This is true if the Method attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean methodESet;

	/**
	 * The default value of the '{@link #getCustomMethod() <em>Custom Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCustomMethod()
	 * @generated
	 * @ordered
	 */
	protected static final String CUSTOM_METHOD_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCustomMethod() <em>Custom Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCustomMethod()
	 * @generated
	 * @ordered
	 */
	protected String customMethod = CUSTOM_METHOD_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected AggregateFromImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplyPackage.Literals.AGGREGATE_FROM;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<OclExpression> getGroupingProperties() {
		if (groupingProperties == null) {
			groupingProperties = new EObjectContainmentEList<OclExpression>(OclExpression.class, this, ApplyPackage.AGGREGATE_FROM__GROUPING_PROPERTIES);
		}
		return groupingProperties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AggregateMethod getMethod() {
		return method;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMethod(AggregateMethod newMethod) {
		AggregateMethod oldMethod = method;
		method = newMethod == null ? METHOD_EDEFAULT : newMethod;
		boolean oldMethodESet = methodESet;
		methodESet = true;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplyPackage.AGGREGATE_FROM__METHOD, oldMethod, method, !oldMethodESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetMethod() {
		AggregateMethod oldMethod = method;
		boolean oldMethodESet = methodESet;
		method = METHOD_EDEFAULT;
		methodESet = false;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.UNSET, ApplyPackage.AGGREGATE_FROM__METHOD, oldMethod, METHOD_EDEFAULT, oldMethodESet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetMethod() {
		return methodESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCustomMethod() {
		return customMethod;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCustomMethod(String newCustomMethod) {
		String oldCustomMethod = customMethod;
		customMethod = newCustomMethod;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ApplyPackage.AGGREGATE_FROM__CUSTOM_METHOD, oldCustomMethod, customMethod));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ApplyPackage.AGGREGATE_FROM__GROUPING_PROPERTIES:
				return ((InternalEList<?>)getGroupingProperties()).basicRemove(otherEnd, msgs);
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
			case ApplyPackage.AGGREGATE_FROM__GROUPING_PROPERTIES:
				return getGroupingProperties();
			case ApplyPackage.AGGREGATE_FROM__METHOD:
				return getMethod();
			case ApplyPackage.AGGREGATE_FROM__CUSTOM_METHOD:
				return getCustomMethod();
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
			case ApplyPackage.AGGREGATE_FROM__GROUPING_PROPERTIES:
				getGroupingProperties().clear();
				getGroupingProperties().addAll((Collection<? extends OclExpression>)newValue);
				return;
			case ApplyPackage.AGGREGATE_FROM__METHOD:
				setMethod((AggregateMethod)newValue);
				return;
			case ApplyPackage.AGGREGATE_FROM__CUSTOM_METHOD:
				setCustomMethod((String)newValue);
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
			case ApplyPackage.AGGREGATE_FROM__GROUPING_PROPERTIES:
				getGroupingProperties().clear();
				return;
			case ApplyPackage.AGGREGATE_FROM__METHOD:
				unsetMethod();
				return;
			case ApplyPackage.AGGREGATE_FROM__CUSTOM_METHOD:
				setCustomMethod(CUSTOM_METHOD_EDEFAULT);
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
			case ApplyPackage.AGGREGATE_FROM__GROUPING_PROPERTIES:
				return groupingProperties != null && !groupingProperties.isEmpty();
			case ApplyPackage.AGGREGATE_FROM__METHOD:
				return isSetMethod();
			case ApplyPackage.AGGREGATE_FROM__CUSTOM_METHOD:
				return CUSTOM_METHOD_EDEFAULT == null ? customMethod != null : !CUSTOM_METHOD_EDEFAULT.equals(customMethod);
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
		if (methodESet) result.append(method); else result.append("<unset>");
		result.append(", customMethod: ");
		result.append(customMethod);
		result.append(')');
		return result.toString();
	}

} //AggregateFromImpl
