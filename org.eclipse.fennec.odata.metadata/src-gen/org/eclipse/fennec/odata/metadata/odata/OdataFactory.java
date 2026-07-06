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

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage
 * @generated
 */
@ProviderType
public interface OdataFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	OdataFactory eINSTANCE = org.eclipse.fennec.odata.metadata.odata.impl.OdataFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Package OData Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Package OData Aspect</em>'.
	 * @generated
	 */
	PackageODataAspect createPackageODataAspect();

	/**
	 * Returns a new object of class '<em>Class OData Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Class OData Aspect</em>'.
	 * @generated
	 */
	ClassODataAspect createClassODataAspect();

	/**
	 * Returns a new object of class '<em>Feature OData Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Feature OData Aspect</em>'.
	 * @generated
	 */
	FeatureODataAspect createFeatureODataAspect();

	/**
	 * Returns a new object of class '<em>Reference OData Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Reference OData Aspect</em>'.
	 * @generated
	 */
	ReferenceODataAspect createReferenceODataAspect();

	/**
	 * Returns a new object of class '<em>OData Package Profile</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Package Profile</em>'.
	 * @generated
	 */
	ODataPackageProfile createODataPackageProfile();

	/**
	 * Returns a new object of class '<em>OData Class Profile</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Class Profile</em>'.
	 * @generated
	 */
	ODataClassProfile createODataClassProfile();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	OdataPackage getOdataPackage();

} //OdataFactory
