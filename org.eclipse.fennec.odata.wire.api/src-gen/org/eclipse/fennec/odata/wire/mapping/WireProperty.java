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
import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Wire Property</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A property on the wire that stands for model attributes, its parts. The parts are not addressable on the wire: the wire property replaces them in the JSON, $select, $filter and $orderby. What the property means — its JSON form, how it compares, how it sorts — is the profile's WirePropertyHandler; a profile may subclass this type to carry more.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.wire.mapping.WireProperty#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.wire.mapping.WireProperty#getParts <em>Parts</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.wire.mapping.WireMappingPackage#getWireProperty()
 * @model
 * @generated
 */
@ProviderType
public interface WireProperty extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The name on the wire, e.g. phenomenonTime.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.odata.wire.mapping.WireMappingPackage#getWireProperty_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.wire.mapping.WireProperty#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Parts</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.emf.ecore.EAttribute}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The model attributes the property stands for, attributes of the mapped type or its supertypes; the runtime loads them for $select and accepts conditions only on them.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parts</em>' reference list.
	 * @see org.eclipse.fennec.odata.wire.mapping.WireMappingPackage#getWireProperty_Parts()
	 * @model required="true"
	 * @generated
	 */
	EList<EAttribute> getParts();

} // WireProperty
