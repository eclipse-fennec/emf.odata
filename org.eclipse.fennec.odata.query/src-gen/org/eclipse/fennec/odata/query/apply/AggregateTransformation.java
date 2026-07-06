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
 * A representation of the model object '<em><b>Aggregate Transformation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.AggregateTransformation#getAggregations <em>Aggregations</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getAggregateTransformation()
 * @model
 * @generated
 */
@ProviderType
public interface AggregateTransformation extends ApplyTransformation {
	/**
	 * Returns the value of the '<em><b>Aggregations</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.query.apply.AggregateExpression}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Aggregations</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getAggregateTransformation_Aggregations()
	 * @model containment="true"
	 * @generated
	 */
	EList<AggregateExpression> getAggregations();

} // AggregateTransformation
