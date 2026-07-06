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

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage
 * @generated
 */
@ProviderType
public interface ApplyFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ApplyFactory eINSTANCE = org.eclipse.fennec.odata.query.apply.impl.ApplyFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Pipeline</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Pipeline</em>'.
	 * @generated
	 */
	ApplyPipeline createApplyPipeline();

	/**
	 * Returns a new object of class '<em>Filter Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Filter Transformation</em>'.
	 * @generated
	 */
	FilterTransformation createFilterTransformation();

	/**
	 * Returns a new object of class '<em>Group By Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Group By Transformation</em>'.
	 * @generated
	 */
	GroupByTransformation createGroupByTransformation();

	/**
	 * Returns a new object of class '<em>Aggregate Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Aggregate Transformation</em>'.
	 * @generated
	 */
	AggregateTransformation createAggregateTransformation();

	/**
	 * Returns a new object of class '<em>Aggregate Expression</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Aggregate Expression</em>'.
	 * @generated
	 */
	AggregateExpression createAggregateExpression();

	/**
	 * Returns a new object of class '<em>Compute Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Compute Transformation</em>'.
	 * @generated
	 */
	ComputeTransformation createComputeTransformation();

	/**
	 * Returns a new object of class '<em>Compute Expression</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Compute Expression</em>'.
	 * @generated
	 */
	ComputeExpression createComputeExpression();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ApplyPackage getApplyPackage();

} //ApplyFactory
