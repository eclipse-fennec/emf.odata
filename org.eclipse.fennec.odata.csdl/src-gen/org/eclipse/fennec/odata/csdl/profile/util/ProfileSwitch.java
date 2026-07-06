/**
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
package org.eclipse.fennec.odata.csdl.profile.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.fennec.odata.csdl.profile.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage
 * @generated
 */
public class ProfileSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ProfilePackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ProfileSwitch() {
		if (modelPackage == null) {
			modelPackage = ProfilePackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case ProfilePackage.ODATA_PACKAGE_PROFILE: {
				ODataPackageProfile oDataPackageProfile = (ODataPackageProfile)theEObject;
				T result = caseODataPackageProfile(oDataPackageProfile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ProfilePackage.ODATA_ENUM_PROFILE: {
				ODataEnumProfile oDataEnumProfile = (ODataEnumProfile)theEObject;
				T result = caseODataEnumProfile(oDataEnumProfile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ProfilePackage.ODATA_ENUM_MEMBER: {
				ODataEnumMember oDataEnumMember = (ODataEnumMember)theEObject;
				T result = caseODataEnumMember(oDataEnumMember);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ProfilePackage.ODATA_CLASS_PROFILE: {
				ODataClassProfile oDataClassProfile = (ODataClassProfile)theEObject;
				T result = caseODataClassProfile(oDataClassProfile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ProfilePackage.ODATA_PROPERTY_PROFILE: {
				ODataPropertyProfile oDataPropertyProfile = (ODataPropertyProfile)theEObject;
				T result = caseODataPropertyProfile(oDataPropertyProfile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ProfilePackage.ODATA_NAVIGATION_PROFILE: {
				ODataNavigationProfile oDataNavigationProfile = (ODataNavigationProfile)theEObject;
				T result = caseODataNavigationProfile(oDataNavigationProfile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ProfilePackage.ODATA_ANNOTATION: {
				ODataAnnotation oDataAnnotation = (ODataAnnotation)theEObject;
				T result = caseODataAnnotation(oDataAnnotation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ProfilePackage.ODATA_REFERENTIAL_CONSTRAINT: {
				ODataReferentialConstraint oDataReferentialConstraint = (ODataReferentialConstraint)theEObject;
				T result = caseODataReferentialConstraint(oDataReferentialConstraint);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ProfilePackage.ODATA_OPERATION_PROFILE: {
				ODataOperationProfile oDataOperationProfile = (ODataOperationProfile)theEObject;
				T result = caseODataOperationProfile(oDataOperationProfile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ProfilePackage.ODATA_PARAMETER_PROFILE: {
				ODataParameterProfile oDataParameterProfile = (ODataParameterProfile)theEObject;
				T result = caseODataParameterProfile(oDataParameterProfile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>OData Package Profile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>OData Package Profile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseODataPackageProfile(ODataPackageProfile object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>OData Enum Profile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>OData Enum Profile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseODataEnumProfile(ODataEnumProfile object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>OData Enum Member</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>OData Enum Member</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseODataEnumMember(ODataEnumMember object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>OData Class Profile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>OData Class Profile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseODataClassProfile(ODataClassProfile object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>OData Property Profile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>OData Property Profile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseODataPropertyProfile(ODataPropertyProfile object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>OData Navigation Profile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>OData Navigation Profile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseODataNavigationProfile(ODataNavigationProfile object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>OData Annotation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>OData Annotation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseODataAnnotation(ODataAnnotation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>OData Referential Constraint</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>OData Referential Constraint</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseODataReferentialConstraint(ODataReferentialConstraint object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>OData Operation Profile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>OData Operation Profile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseODataOperationProfile(ODataOperationProfile object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>OData Parameter Profile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>OData Parameter Profile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseODataParameterProfile(ODataParameterProfile object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //ProfileSwitch
