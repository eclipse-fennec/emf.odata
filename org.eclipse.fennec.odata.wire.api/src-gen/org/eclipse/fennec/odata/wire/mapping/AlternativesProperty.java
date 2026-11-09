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
package org.eclipse.fennec.odata.wire.mapping;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EAttribute;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Alternatives Property</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One value held in one of several attributes of different primitive types, e.g. result over a number, a boolean and a text attribute. At most one of them is set.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.wire.mapping.AlternativesProperty#getParts <em>Parts</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.wire.mapping.WireMappingPackage#getAlternativesProperty()
 * @model
 * @generated
 */
@ProviderType
public interface AlternativesProperty extends WireProperty {
	/**
	 * Returns the value of the '<em><b>Parts</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EAttribute}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The attributes, in the order of $orderby: a value in an earlier part sorts before a value in a later one.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parts</em>' reference list.
	 * @see org.eclipse.fennec.odata.wire.mapping.WireMappingPackage#getAlternativesProperty_Parts()
	 * @model required="true"
	 * @generated
	 */
	EList<EAttribute> getParts();

} // AlternativesProperty
