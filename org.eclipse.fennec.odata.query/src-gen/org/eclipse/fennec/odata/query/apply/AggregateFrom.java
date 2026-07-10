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
package org.eclipse.fennec.odata.query.apply;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.m2x.model.ocl.OclExpression;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Aggregate From</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One 'from <groupingProperties> [with <method>]' clause of an aggregate expression; the method may be absent for custom aggregates (customFrom).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.AggregateFrom#getGroupingProperties <em>Grouping Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.AggregateFrom#getMethod <em>Method</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.AggregateFrom#getCustomMethod <em>Custom Method</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getAggregateFrom()
 * @model
 * @generated
 */
@ProviderType
public interface AggregateFrom extends EObject {
	/**
	 * Returns the value of the '<em><b>Grouping Properties</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.m2x.model.ocl.OclExpression}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Grouping Properties</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getAggregateFrom_GroupingProperties()
	 * @model containment="true"
	 * @generated
	 */
	EList<OclExpression> getGroupingProperties();

	/**
	 * Returns the value of the '<em><b>Method</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.odata.query.apply.AggregateMethod}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Method</em>' attribute.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateMethod
	 * @see #isSetMethod()
	 * @see #unsetMethod()
	 * @see #setMethod(AggregateMethod)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getAggregateFrom_Method()
	 * @model unsettable="true"
	 * @generated
	 */
	AggregateMethod getMethod();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.AggregateFrom#getMethod <em>Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Method</em>' attribute.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateMethod
	 * @see #isSetMethod()
	 * @see #unsetMethod()
	 * @see #getMethod()
	 * @generated
	 */
	void setMethod(AggregateMethod value);

	/**
	 * Unsets the value of the '{@link org.eclipse.fennec.odata.query.apply.AggregateFrom#getMethod <em>Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetMethod()
	 * @see #getMethod()
	 * @see #setMethod(AggregateMethod)
	 * @generated
	 */
	void unsetMethod();

	/**
	 * Returns whether the value of the '{@link org.eclipse.fennec.odata.query.apply.AggregateFrom#getMethod <em>Method</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Method</em>' attribute is set.
	 * @see #unsetMethod()
	 * @see #getMethod()
	 * @see #setMethod(AggregateMethod)
	 * @generated
	 */
	boolean isSetMethod();

	/**
	 * Returns the value of the '<em><b>Custom Method</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Custom Method</em>' attribute.
	 * @see #setCustomMethod(String)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getAggregateFrom_CustomMethod()
	 * @model
	 * @generated
	 */
	String getCustomMethod();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.AggregateFrom#getCustomMethod <em>Custom Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Custom Method</em>' attribute.
	 * @see #getCustomMethod()
	 * @generated
	 */
	void setCustomMethod(String value);

} // AggregateFrom
