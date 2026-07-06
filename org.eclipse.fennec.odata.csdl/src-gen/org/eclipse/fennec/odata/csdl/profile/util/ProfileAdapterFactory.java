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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.odata.csdl.profile.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage
 * @generated
 */
public class ProfileAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ProfilePackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ProfileAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = ProfilePackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ProfileSwitch<Adapter> modelSwitch =
		new ProfileSwitch<Adapter>() {
			@Override
			public Adapter caseODataPackageProfile(ODataPackageProfile object) {
				return createODataPackageProfileAdapter();
			}
			@Override
			public Adapter caseODataEnumProfile(ODataEnumProfile object) {
				return createODataEnumProfileAdapter();
			}
			@Override
			public Adapter caseODataEnumMember(ODataEnumMember object) {
				return createODataEnumMemberAdapter();
			}
			@Override
			public Adapter caseODataClassProfile(ODataClassProfile object) {
				return createODataClassProfileAdapter();
			}
			@Override
			public Adapter caseODataPropertyProfile(ODataPropertyProfile object) {
				return createODataPropertyProfileAdapter();
			}
			@Override
			public Adapter caseODataNavigationProfile(ODataNavigationProfile object) {
				return createODataNavigationProfileAdapter();
			}
			@Override
			public Adapter caseODataAnnotation(ODataAnnotation object) {
				return createODataAnnotationAdapter();
			}
			@Override
			public Adapter caseODataReferentialConstraint(ODataReferentialConstraint object) {
				return createODataReferentialConstraintAdapter();
			}
			@Override
			public Adapter caseODataOperationProfile(ODataOperationProfile object) {
				return createODataOperationProfileAdapter();
			}
			@Override
			public Adapter caseODataParameterProfile(ODataParameterProfile object) {
				return createODataParameterProfileAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile <em>OData Package Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile
	 * @generated
	 */
	public Adapter createODataPackageProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile <em>OData Enum Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile
	 * @generated
	 */
	public Adapter createODataEnumProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.csdl.profile.ODataEnumMember <em>OData Enum Member</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataEnumMember
	 * @generated
	 */
	public Adapter createODataEnumMemberAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.csdl.profile.ODataClassProfile <em>OData Class Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataClassProfile
	 * @generated
	 */
	public Adapter createODataClassProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile <em>OData Property Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile
	 * @generated
	 */
	public Adapter createODataPropertyProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile <em>OData Navigation Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile
	 * @generated
	 */
	public Adapter createODataNavigationProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation <em>OData Annotation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataAnnotation
	 * @generated
	 */
	public Adapter createODataAnnotationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint <em>OData Referential Constraint</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint
	 * @generated
	 */
	public Adapter createODataReferentialConstraintAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile <em>OData Operation Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile
	 * @generated
	 */
	public Adapter createODataOperationProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile <em>OData Parameter Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile
	 * @generated
	 */
	public Adapter createODataParameterProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //ProfileAdapterFactory
