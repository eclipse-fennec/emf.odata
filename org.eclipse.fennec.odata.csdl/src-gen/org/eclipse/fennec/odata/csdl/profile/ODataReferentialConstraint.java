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
package org.eclipse.fennec.odata.csdl.profile;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>OData Referential Constraint</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Resolved CSDL ReferentialConstraint on a NavigationProperty: the dependent property (foreign key on the declaring type) references the principal key property on the navigation target (from @OData.ReferentialConstraint, value "property=referencedProperty[,...]").
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint#getProperty <em>Property</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint#getReferencedProperty <em>Referenced Property</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataReferentialConstraint()
 * @model
 * @generated
 */
@ProviderType
public interface ODataReferentialConstraint extends EObject {
	/**
	 * Returns the value of the '<em><b>Property</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Property</em>' attribute.
	 * @see #setProperty(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataReferentialConstraint_Property()
	 * @model
	 * @generated
	 */
	String getProperty();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint#getProperty <em>Property</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Property</em>' attribute.
	 * @see #getProperty()
	 * @generated
	 */
	void setProperty(String value);

	/**
	 * Returns the value of the '<em><b>Referenced Property</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Referenced Property</em>' attribute.
	 * @see #setReferencedProperty(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataReferentialConstraint_ReferencedProperty()
	 * @model
	 * @generated
	 */
	String getReferencedProperty();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint#getReferencedProperty <em>Referenced Property</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Referenced Property</em>' attribute.
	 * @see #getReferencedProperty()
	 * @generated
	 */
	void setReferencedProperty(String value);

} // ODataReferentialConstraint
