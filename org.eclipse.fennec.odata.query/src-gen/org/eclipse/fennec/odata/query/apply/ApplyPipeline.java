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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Pipeline</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One $apply value: the slash-separated sequence of transformations, applied left to right.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.query.apply.ApplyPipeline#getTransformations <em>Transformations</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getApplyPipeline()
 * @model
 * @generated
 */
@ProviderType
public interface ApplyPipeline extends EObject {
	/**
	 * Returns the value of the '<em><b>Transformations</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.query.apply.ApplyTransformation}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Transformations</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getApplyPipeline_Transformations()
	 * @model containment="true"
	 * @generated
	 */
	EList<ApplyTransformation> getTransformations();

} // ApplyPipeline
