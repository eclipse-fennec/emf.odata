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
 * A representation of the model object '<em><b>Order By Expression</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.OrderByExpression#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.OrderByExpression#isAscending <em>Ascending</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getOrderByExpression()
 * @model
 * @generated
 */
@ProviderType
public interface OrderByExpression extends EObject {
	/**
	 * Returns the value of the '<em><b>Expression</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Expression</em>' containment reference.
	 * @see #setExpression(OclExpression)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getOrderByExpression_Expression()
	 * @model containment="true"
	 * @generated
	 */
	OclExpression getExpression();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.OrderByExpression#getExpression <em>Expression</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Expression</em>' containment reference.
	 * @see #getExpression()
	 * @generated
	 */
	void setExpression(OclExpression value);

	/**
	 * Returns the value of the '<em><b>Ascending</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ascending</em>' attribute.
	 * @see #setAscending(boolean)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getOrderByExpression_Ascending()
	 * @model default="true"
	 * @generated
	 */
	boolean isAscending();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.OrderByExpression#isAscending <em>Ascending</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ascending</em>' attribute.
	 * @see #isAscending()
	 * @generated
	 */
	void setAscending(boolean value);

} // OrderByExpression
