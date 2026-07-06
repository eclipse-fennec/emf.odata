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
 * A representation of the model object '<em><b>Filter Transformation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.FilterTransformation#getPredicate <em>Predicate</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getFilterTransformation()
 * @model
 * @generated
 */
@ProviderType
public interface FilterTransformation extends ApplyTransformation {
	/**
	 * Returns the value of the '<em><b>Predicate</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Predicate</em>' containment reference.
	 * @see #setPredicate(OclExpression)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getFilterTransformation_Predicate()
	 * @model containment="true"
	 * @generated
	 */
	OclExpression getPredicate();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.FilterTransformation#getPredicate <em>Predicate</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Predicate</em>' containment reference.
	 * @see #getPredicate()
	 * @generated
	 */
	void setPredicate(OclExpression value);

} // FilterTransformation
