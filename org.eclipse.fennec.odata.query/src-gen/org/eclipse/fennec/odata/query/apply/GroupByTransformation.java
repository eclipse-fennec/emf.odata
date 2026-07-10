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

import org.eclipse.fennec.m2x.model.ocl.OclExpression;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Group By Transformation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.GroupByTransformation#getGroupingProperties <em>Grouping Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.GroupByTransformation#getThen <em>Then</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.GroupByTransformation#getRollups <em>Rollups</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getGroupByTransformation()
 * @model
 * @generated
 */
@ProviderType
public interface GroupByTransformation extends ApplyTransformation {
	/**
	 * Returns the value of the '<em><b>Grouping Properties</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.m2x.model.ocl.OclExpression}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Grouping Properties</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getGroupByTransformation_GroupingProperties()
	 * @model containment="true"
	 * @generated
	 */
	EList<OclExpression> getGroupingProperties();

	/**
	 * Returns the value of the '<em><b>Then</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional nested transformation applied per group (typically an Aggregate).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Then</em>' containment reference.
	 * @see #setThen(ApplyTransformation)
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getGroupByTransformation_Then()
	 * @model containment="true"
	 * @generated
	 */
	ApplyTransformation getThen();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.query.apply.GroupByTransformation#getThen <em>Then</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Then</em>' containment reference.
	 * @see #getThen()
	 * @generated
	 */
	void setThen(ApplyTransformation value);

	/**
	 * Returns the value of the '<em><b>Rollups</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.query.apply.RollupHierarchy}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * rollup(...) grouping elements: each hierarchy contributes its level prefixes as additional grouping sets (leveled hierarchies; rolluprecursive is not covered).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Rollups</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getGroupByTransformation_Rollups()
	 * @model containment="true"
	 * @generated
	 */
	EList<RollupHierarchy> getRollups();

} // GroupByTransformation
