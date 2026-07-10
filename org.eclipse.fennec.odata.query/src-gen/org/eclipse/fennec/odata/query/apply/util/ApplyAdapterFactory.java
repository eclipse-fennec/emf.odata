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
package org.eclipse.fennec.odata.query.apply.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.odata.query.apply.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage
 * @generated
 */
public class ApplyAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ApplyPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ApplyAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = ApplyPackage.eINSTANCE;
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
	protected ApplySwitch<Adapter> modelSwitch =
		new ApplySwitch<Adapter>() {
			@Override
			public Adapter caseApplyPipeline(ApplyPipeline object) {
				return createApplyPipelineAdapter();
			}
			@Override
			public Adapter caseApplyTransformation(ApplyTransformation object) {
				return createApplyTransformationAdapter();
			}
			@Override
			public Adapter caseFilterTransformation(FilterTransformation object) {
				return createFilterTransformationAdapter();
			}
			@Override
			public Adapter caseGroupByTransformation(GroupByTransformation object) {
				return createGroupByTransformationAdapter();
			}
			@Override
			public Adapter caseRollupHierarchy(RollupHierarchy object) {
				return createRollupHierarchyAdapter();
			}
			@Override
			public Adapter caseAggregateTransformation(AggregateTransformation object) {
				return createAggregateTransformationAdapter();
			}
			@Override
			public Adapter caseAggregateExpression(AggregateExpression object) {
				return createAggregateExpressionAdapter();
			}
			@Override
			public Adapter caseAggregateFrom(AggregateFrom object) {
				return createAggregateFromAdapter();
			}
			@Override
			public Adapter caseBottomTopTransformation(BottomTopTransformation object) {
				return createBottomTopTransformationAdapter();
			}
			@Override
			public Adapter caseConcatTransformation(ConcatTransformation object) {
				return createConcatTransformationAdapter();
			}
			@Override
			public Adapter caseTopTransformation(TopTransformation object) {
				return createTopTransformationAdapter();
			}
			@Override
			public Adapter caseSkipTransformation(SkipTransformation object) {
				return createSkipTransformationAdapter();
			}
			@Override
			public Adapter caseIdentityTransformation(IdentityTransformation object) {
				return createIdentityTransformationAdapter();
			}
			@Override
			public Adapter caseOrderByTransformation(OrderByTransformation object) {
				return createOrderByTransformationAdapter();
			}
			@Override
			public Adapter caseOrderByExpression(OrderByExpression object) {
				return createOrderByExpressionAdapter();
			}
			@Override
			public Adapter caseComputeTransformation(ComputeTransformation object) {
				return createComputeTransformationAdapter();
			}
			@Override
			public Adapter caseComputeExpression(ComputeExpression object) {
				return createComputeExpressionAdapter();
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
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.ApplyPipeline <em>Pipeline</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPipeline
	 * @generated
	 */
	public Adapter createApplyPipelineAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.ApplyTransformation <em>Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyTransformation
	 * @generated
	 */
	public Adapter createApplyTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.FilterTransformation <em>Filter Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.FilterTransformation
	 * @generated
	 */
	public Adapter createFilterTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.GroupByTransformation <em>Group By Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.GroupByTransformation
	 * @generated
	 */
	public Adapter createGroupByTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.RollupHierarchy <em>Rollup Hierarchy</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.RollupHierarchy
	 * @generated
	 */
	public Adapter createRollupHierarchyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.AggregateTransformation <em>Aggregate Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateTransformation
	 * @generated
	 */
	public Adapter createAggregateTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.AggregateExpression <em>Aggregate Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateExpression
	 * @generated
	 */
	public Adapter createAggregateExpressionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.AggregateFrom <em>Aggregate From</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateFrom
	 * @generated
	 */
	public Adapter createAggregateFromAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation <em>Bottom Top Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.BottomTopTransformation
	 * @generated
	 */
	public Adapter createBottomTopTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.ConcatTransformation <em>Concat Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.ConcatTransformation
	 * @generated
	 */
	public Adapter createConcatTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.TopTransformation <em>Top Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.TopTransformation
	 * @generated
	 */
	public Adapter createTopTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.SkipTransformation <em>Skip Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.SkipTransformation
	 * @generated
	 */
	public Adapter createSkipTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.IdentityTransformation <em>Identity Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.IdentityTransformation
	 * @generated
	 */
	public Adapter createIdentityTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.OrderByTransformation <em>Order By Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.OrderByTransformation
	 * @generated
	 */
	public Adapter createOrderByTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.OrderByExpression <em>Order By Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.OrderByExpression
	 * @generated
	 */
	public Adapter createOrderByExpressionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.ComputeTransformation <em>Compute Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.ComputeTransformation
	 * @generated
	 */
	public Adapter createComputeTransformationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.eclipse.fennec.odata.query.apply.ComputeExpression <em>Compute Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.eclipse.fennec.odata.query.apply.ComputeExpression
	 * @generated
	 */
	public Adapter createComputeExpressionAdapter() {
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

} //ApplyAdapterFactory
