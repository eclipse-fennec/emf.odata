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

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.m2x.model.ocl.OclExpression;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Aggregate Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getMethod <em>Method</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getAlias <em>Alias</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getAggregateExpression()
 * @model
 * @generated
 */
@ProviderType
public interface AggregateExpression extends EObject {
	/**
	 * Returns the value of the '<em><b>Expression</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The aggregated operand; absent for the $count virtual aggregate.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Expression</em>' containment reference.
	 * @see #setExpression(OclExpression)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getAggregateExpression_Expression()
	 * @model containment="true"
	 * @generated
	 */
	OclExpression getExpression();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getExpression <em>Expression</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Expression</em>' containment reference.
	 * @see #getExpression()
	 * @generated
	 */
	void setExpression(OclExpression value);

	/**
	 * Returns the value of the '<em><b>Method</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.odata.query.apply.AggregateMethod}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Method</em>' attribute.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateMethod
	 * @see #setMethod(AggregateMethod)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getAggregateExpression_Method()
	 * @model
	 * @generated
	 */
	AggregateMethod getMethod();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getMethod <em>Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Method</em>' attribute.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateMethod
	 * @see #getMethod()
	 * @generated
	 */
	void setMethod(AggregateMethod value);

	/**
	 * Returns the value of the '<em><b>Alias</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Alias</em>' attribute.
	 * @see #setAlias(String)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getAggregateExpression_Alias()
	 * @model
	 * @generated
	 */
	String getAlias();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getAlias <em>Alias</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Alias</em>' attribute.
	 * @see #getAlias()
	 * @generated
	 */
	void setAlias(String value);

} // AggregateExpression
