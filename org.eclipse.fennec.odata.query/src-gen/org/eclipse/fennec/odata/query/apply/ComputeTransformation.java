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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Compute Transformation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.ComputeTransformation#getComputeExpressions <em>Compute Expressions</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getComputeTransformation()
 * @model
 * @generated
 */
@ProviderType
public interface ComputeTransformation extends ApplyTransformation {
	/**
	 * Returns the value of the '<em><b>Compute Expressions</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.query.apply.ComputeExpression}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Compute Expressions</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getComputeTransformation_ComputeExpressions()
	 * @model containment="true"
	 * @generated
	 */
	EList<ComputeExpression> getComputeExpressions();

} // ComputeTransformation
