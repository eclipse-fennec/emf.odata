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

import org.eclipse.fennec.m2x.model.ocl.OclExpression;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Bottom Top Transformation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * topcount/topsum/toppercent/bottomcount/bottomsum/bottompercent(threshold, value): keeps the subset with the highest/lowest values.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getMethod <em>Method</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getThreshold <em>Threshold</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getBottomTopTransformation()
 * @model
 * @generated
 */
@ProviderType
public interface BottomTopTransformation extends ApplyTransformation {
	/**
	 * Returns the value of the '<em><b>Method</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.odata.query.apply.BottomTopMethod}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Method</em>' attribute.
	 * @see org.eclipse.fennec.odata.query.apply.BottomTopMethod
	 * @see #setMethod(BottomTopMethod)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getBottomTopTransformation_Method()
	 * @model
	 * @generated
	 */
	BottomTopMethod getMethod();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getMethod <em>Method</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Method</em>' attribute.
	 * @see org.eclipse.fennec.odata.query.apply.BottomTopMethod
	 * @see #getMethod()
	 * @generated
	 */
	void setMethod(BottomTopMethod value);

	/**
	 * Returns the value of the '<em><b>Threshold</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Threshold</em>' containment reference.
	 * @see #setThreshold(OclExpression)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getBottomTopTransformation_Threshold()
	 * @model containment="true"
	 * @generated
	 */
	OclExpression getThreshold();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getThreshold <em>Threshold</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Threshold</em>' containment reference.
	 * @see #getThreshold()
	 * @generated
	 */
	void setThreshold(OclExpression value);

	/**
	 * Returns the value of the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' containment reference.
	 * @see #setValue(OclExpression)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getBottomTopTransformation_Value()
	 * @model containment="true"
	 * @generated
	 */
	OclExpression getValue();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getValue <em>Value</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' containment reference.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(OclExpression value);

} // BottomTopTransformation
