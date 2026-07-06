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

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage
 * @generated
 */
@ProviderType
public interface ProfileFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ProfileFactory eINSTANCE = org.eclipse.fennec.odata.csdl.profile.impl.ProfileFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>OData Package Profile</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Package Profile</em>'.
	 * @generated
	 */
	ODataPackageProfile createODataPackageProfile();

	/**
	 * Returns a new object of class '<em>OData Enum Profile</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Enum Profile</em>'.
	 * @generated
	 */
	ODataEnumProfile createODataEnumProfile();

	/**
	 * Returns a new object of class '<em>OData Enum Member</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Enum Member</em>'.
	 * @generated
	 */
	ODataEnumMember createODataEnumMember();

	/**
	 * Returns a new object of class '<em>OData Class Profile</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Class Profile</em>'.
	 * @generated
	 */
	ODataClassProfile createODataClassProfile();

	/**
	 * Returns a new object of class '<em>OData Property Profile</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Property Profile</em>'.
	 * @generated
	 */
	ODataPropertyProfile createODataPropertyProfile();

	/**
	 * Returns a new object of class '<em>OData Navigation Profile</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Navigation Profile</em>'.
	 * @generated
	 */
	ODataNavigationProfile createODataNavigationProfile();

	/**
	 * Returns a new object of class '<em>OData Annotation</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Annotation</em>'.
	 * @generated
	 */
	ODataAnnotation createODataAnnotation();

	/**
	 * Returns a new object of class '<em>OData Referential Constraint</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Referential Constraint</em>'.
	 * @generated
	 */
	ODataReferentialConstraint createODataReferentialConstraint();

	/**
	 * Returns a new object of class '<em>OData Operation Profile</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Operation Profile</em>'.
	 * @generated
	 */
	ODataOperationProfile createODataOperationProfile();

	/**
	 * Returns a new object of class '<em>OData Parameter Profile</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>OData Parameter Profile</em>'.
	 * @generated
	 */
	ODataParameterProfile createODataParameterProfile();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ProfilePackage getProfilePackage();

} //ProfileFactory
