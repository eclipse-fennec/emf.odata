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
package org.eclipse.fennec.odata.query.apply.impl;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.fennec.odata.query.apply.ApplyPackage;
import org.eclipse.fennec.odata.query.apply.IdentityTransformation;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Identity Transformation</b></em>'.
 * <!-- end-user-doc -->
 *
 * @generated
 */
public class IdentityTransformationImpl extends ApplyTransformationImpl implements IdentityTransformation {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IdentityTransformationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ApplyPackage.Literals.IDENTITY_TRANSFORMATION;
	}

} //IdentityTransformationImpl
