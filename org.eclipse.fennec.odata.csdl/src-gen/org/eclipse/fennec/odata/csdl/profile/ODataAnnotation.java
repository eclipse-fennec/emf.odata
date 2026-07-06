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
 * A representation of the model object '<em><b>OData Annotation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Generic vocabulary-term annotation (constant-expression subset, AP-5): term = qualified term name (e.g. Core.Description), value = the constant value in lexical form. Resolved from the EAnnotation source https://eclipse.org/fennec/odata/annotations.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation#getTerm <em>Term</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataAnnotation()
 * @model
 * @generated
 */
@ProviderType
public interface ODataAnnotation extends EObject {
	/**
	 * Returns the value of the '<em><b>Term</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Term</em>' attribute.
	 * @see #setTerm(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataAnnotation_Term()
	 * @model
	 * @generated
	 */
	String getTerm();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation#getTerm <em>Term</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Term</em>' attribute.
	 * @see #getTerm()
	 * @generated
	 */
	void setTerm(String value);

	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataAnnotation_Value()
	 * @model
	 * @generated
	 */
	String getValue();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(String value);

} // ODataAnnotation
