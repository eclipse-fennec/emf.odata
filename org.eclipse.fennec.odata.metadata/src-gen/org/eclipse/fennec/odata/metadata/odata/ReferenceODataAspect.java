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
package org.eclipse.fennec.odata.metadata.odata;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Reference OData Aspect</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * OData feature-level aspect for an EReference → CSDL NavigationProperty.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#isContainsTarget <em>Contains Target</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#getPartner <em>Partner</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#getOnDelete <em>On Delete</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getReferenceODataAspect()
 * @model
 * @generated
 */
@ProviderType
public interface ReferenceODataAspect extends FeatureODataAspect {
	/**
	 * Returns the value of the '<em><b>Contains Target</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL ContainsTarget (defaults to EReference.containment). From @OData.NavigationProperty.ContainsTarget.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Contains Target</em>' attribute.
	 * @see #setContainsTarget(boolean)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getReferenceODataAspect_ContainsTarget()
	 * @model
	 * @generated
	 */
	boolean isContainsTarget();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#isContainsTarget <em>Contains Target</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contains Target</em>' attribute.
	 * @see #isContainsTarget()
	 * @generated
	 */
	void setContainsTarget(boolean value);

	/**
	 * Returns the value of the '<em><b>Partner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL Partner navigation name (from EReference.eOpposite).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Partner</em>' attribute.
	 * @see #setPartner(String)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getReferenceODataAspect_Partner()
	 * @model
	 * @generated
	 */
	String getPartner();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#getPartner <em>Partner</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Partner</em>' attribute.
	 * @see #getPartner()
	 * @generated
	 */
	void setPartner(String value);

	/**
	 * Returns the value of the '<em><b>On Delete</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL OnDelete action (Cascade | None | SetNull | SetDefault); unset = none.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>On Delete</em>' attribute.
	 * @see #setOnDelete(String)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getReferenceODataAspect_OnDelete()
	 * @model
	 * @generated
	 */
	String getOnDelete();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect#getOnDelete <em>On Delete</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>On Delete</em>' attribute.
	 * @see #getOnDelete()
	 * @generated
	 */
	void setOnDelete(String value);

} // ReferenceODataAspect
