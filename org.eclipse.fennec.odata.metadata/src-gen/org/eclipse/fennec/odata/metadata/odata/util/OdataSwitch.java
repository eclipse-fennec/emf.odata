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
package org.eclipse.fennec.odata.metadata.odata.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.fennec.model.metadata.Aspect;
import org.eclipse.fennec.model.metadata.ClassAspect;
import org.eclipse.fennec.model.metadata.ClassProfile;
import org.eclipse.fennec.model.metadata.FeatureAspect;
import org.eclipse.fennec.model.metadata.PackageAspect;
import org.eclipse.fennec.model.metadata.PackageProfile;

import org.eclipse.fennec.odata.metadata.odata.*;

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
 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage
 * @generated
 */
public class OdataSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static OdataPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OdataSwitch() {
		if (modelPackage == null) {
			modelPackage = OdataPackage.eINSTANCE;
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
			case OdataPackage.PACKAGE_ODATA_ASPECT: {
				PackageODataAspect packageODataAspect = (PackageODataAspect)theEObject;
				T result = casePackageODataAspect(packageODataAspect);
				if (result == null) result = casePackageAspect(packageODataAspect);
				if (result == null) result = caseAspect(packageODataAspect);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case OdataPackage.CLASS_ODATA_ASPECT: {
				ClassODataAspect classODataAspect = (ClassODataAspect)theEObject;
				T result = caseClassODataAspect(classODataAspect);
				if (result == null) result = caseClassAspect(classODataAspect);
				if (result == null) result = caseAspect(classODataAspect);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case OdataPackage.FEATURE_ODATA_ASPECT: {
				FeatureODataAspect featureODataAspect = (FeatureODataAspect)theEObject;
				T result = caseFeatureODataAspect(featureODataAspect);
				if (result == null) result = caseFeatureAspect(featureODataAspect);
				if (result == null) result = caseAspect(featureODataAspect);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case OdataPackage.REFERENCE_ODATA_ASPECT: {
				ReferenceODataAspect referenceODataAspect = (ReferenceODataAspect)theEObject;
				T result = caseReferenceODataAspect(referenceODataAspect);
				if (result == null) result = caseFeatureODataAspect(referenceODataAspect);
				if (result == null) result = caseFeatureAspect(referenceODataAspect);
				if (result == null) result = caseAspect(referenceODataAspect);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case OdataPackage.ODATA_PACKAGE_PROFILE: {
				ODataPackageProfile oDataPackageProfile = (ODataPackageProfile)theEObject;
				T result = caseODataPackageProfile(oDataPackageProfile);
				if (result == null) result = casePackageProfile(oDataPackageProfile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case OdataPackage.ODATA_CLASS_PROFILE: {
				ODataClassProfile oDataClassProfile = (ODataClassProfile)theEObject;
				T result = caseODataClassProfile(oDataClassProfile);
				if (result == null) result = caseClassProfile(oDataClassProfile);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Package OData Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Package OData Aspect</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePackageODataAspect(PackageODataAspect object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Class OData Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Class OData Aspect</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClassODataAspect(ClassODataAspect object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Feature OData Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Feature OData Aspect</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFeatureODataAspect(FeatureODataAspect object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Reference OData Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Reference OData Aspect</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseReferenceODataAspect(ReferenceODataAspect object) {
		return null;
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
	 * Returns the result of interpreting the object as an instance of '<em>Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Aspect</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAspect(Aspect object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Package Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Package Aspect</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePackageAspect(PackageAspect object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Class Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Class Aspect</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClassAspect(ClassAspect object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Feature Aspect</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Feature Aspect</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFeatureAspect(FeatureAspect object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Package Profile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Package Profile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePackageProfile(PackageProfile object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Class Profile</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Class Profile</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseClassProfile(ClassProfile object) {
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

} //OdataSwitch
