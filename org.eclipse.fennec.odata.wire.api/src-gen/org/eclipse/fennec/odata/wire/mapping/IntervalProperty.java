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

import org.eclipse.emf.ecore.EAttribute;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Interval Property</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A time interval over two date attributes, written as ISO 8601 "start/end", or "start" for an instant (end null or equal to start).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.wire.mapping.IntervalProperty#getStart <em>Start</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.wire.mapping.IntervalProperty#getEnd <em>End</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.wire.mapping.WireMappingPackage#getIntervalProperty()
 * @model
 * @generated
 */
@ProviderType
public interface IntervalProperty extends WireProperty {
	/**
	 * Returns the value of the '<em><b>Start</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Start</em>' reference.
	 * @see #setStart(EAttribute)
	 * @see org.eclipse.fennec.odata.wire.mapping.WireMappingPackage#getIntervalProperty_Start()
	 * @model required="true"
	 * @generated
	 */
	EAttribute getStart();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.wire.mapping.IntervalProperty#getStart <em>Start</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Start</em>' reference.
	 * @see #getStart()
	 * @generated
	 */
	void setStart(EAttribute value);

	/**
	 * Returns the value of the '<em><b>End</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>End</em>' reference.
	 * @see #setEnd(EAttribute)
	 * @see org.eclipse.fennec.odata.wire.mapping.WireMappingPackage#getIntervalProperty_End()
	 * @model required="true"
	 * @generated
	 */
	EAttribute getEnd();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.wire.mapping.IntervalProperty#getEnd <em>End</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>End</em>' reference.
	 * @see #getEnd()
	 * @generated
	 */
	void setEnd(EAttribute value);

} // IntervalProperty
