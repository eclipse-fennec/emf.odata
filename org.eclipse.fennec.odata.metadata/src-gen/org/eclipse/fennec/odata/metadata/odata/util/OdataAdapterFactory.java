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

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.model.metadata.Aspect;
import org.eclipse.fennec.model.metadata.ClassAspect;
import org.eclipse.fennec.model.metadata.ClassProfile;
import org.eclipse.fennec.model.metadata.FeatureAspect;
import org.eclipse.fennec.model.metadata.PackageAspect;
import org.eclipse.fennec.model.metadata.PackageProfile;

import org.eclipse.fennec.odata.metadata.odata.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage
 * @generated
 */
public class OdataAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static OdataPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OdataAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = OdataPackage.eINSTANCE;
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
	protected OdataSwitch<Adapter> modelSwitch =
		new OdataSwitch<Adapter>() {
			@Override
			public Adapter casePackageODataAspect(PackageODataAspect object) {
				return createPackageODataAspectAdapter();
			}
			@Override
			public Adapter caseClassODataAspect(ClassODataAspect object) {
				return createClassODataAspectAdapter();
			}
			@Override
			public Adapter caseFeatureODataAspect(FeatureODataAspect object) {
				return createFeatureODataAspectAdapter();
			}
			@Override
			public Adapter caseReferenceODataAspect(ReferenceODataAspect object) {
				return createReferenceODataAspectAdapter();
			}
			@Override
			public Adapter caseODataPackageProfile(ODataPackageProfile object) {
				return createODataPackageProfileAdapter();
			}
			@Override
			public Adapter caseODataClassProfile(ODataClassProfile object) {
				return createODataClassProfileAdapter();
			}
			@Override
			public Adapter caseAspect(Aspect object) {
				return createAspectAdapter();
			}
			@Override
			public Adapter casePackageAspect(PackageAspect object) {
				return createPackageAspectAdapter();
			}
			@Override
			public Adapter caseClassAspect(ClassAspect object) {
				return createClassAspectAdapter();
			}
			@Override
			public Adapter caseFeatureAspect(FeatureAspect object) {
				return createFeatureAspectAdapter();
			}
			@Override
			public Adapter casePackageProfile(PackageProfile object) {
				return createPackageProfileAdapter();
			}
			@Override
			public Adapter caseClassProfile(ClassProfile object) {
				return createClassProfileAdapter();
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
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.metadata.odata.PackageODataAspect <em>Package OData Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.metadata.odata.PackageODataAspect
	 * @generated
	 */
	public Adapter createPackageODataAspectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.metadata.odata.ClassODataAspect <em>Class OData Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.metadata.odata.ClassODataAspect
	 * @generated
	 */
	public Adapter createClassODataAspectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect <em>Feature OData Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect
	 * @generated
	 */
	public Adapter createFeatureODataAspectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect <em>Reference OData Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect
	 * @generated
	 */
	public Adapter createReferenceODataAspectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile <em>OData Package Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile
	 * @generated
	 */
	public Adapter createODataPackageProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.metadata.odata.ODataClassProfile <em>OData Class Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.metadata.odata.ODataClassProfile
	 * @generated
	 */
	public Adapter createODataClassProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.metadata.Aspect <em>Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.metadata.Aspect
	 * @generated
	 */
	public Adapter createAspectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.metadata.PackageAspect <em>Package Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.metadata.PackageAspect
	 * @generated
	 */
	public Adapter createPackageAspectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.metadata.ClassAspect <em>Class Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.metadata.ClassAspect
	 * @generated
	 */
	public Adapter createClassAspectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.metadata.FeatureAspect <em>Feature Aspect</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.metadata.FeatureAspect
	 * @generated
	 */
	public Adapter createFeatureAspectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.metadata.PackageProfile <em>Package Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.metadata.PackageProfile
	 * @generated
	 */
	public Adapter createPackageProfileAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.model.metadata.ClassProfile <em>Class Profile</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.model.metadata.ClassProfile
	 * @generated
	 */
	public Adapter createClassProfileAdapter() {
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

} //OdataAdapterFactory
