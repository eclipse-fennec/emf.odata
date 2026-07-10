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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.fennec.odata.query.apply.*;

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
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage
 * @generated
 */
public class ApplySwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ApplyPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ApplySwitch() {
		if (modelPackage == null) {
			modelPackage = ApplyPackage.eINSTANCE;
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
			case ApplyPackage.APPLY_PIPELINE: {
				ApplyPipeline applyPipeline = (ApplyPipeline)theEObject;
				T result = caseApplyPipeline(applyPipeline);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.APPLY_TRANSFORMATION: {
				ApplyTransformation applyTransformation = (ApplyTransformation)theEObject;
				T result = caseApplyTransformation(applyTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.FILTER_TRANSFORMATION: {
				FilterTransformation filterTransformation = (FilterTransformation)theEObject;
				T result = caseFilterTransformation(filterTransformation);
				if (result == null) result = caseApplyTransformation(filterTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.GROUP_BY_TRANSFORMATION: {
				GroupByTransformation groupByTransformation = (GroupByTransformation)theEObject;
				T result = caseGroupByTransformation(groupByTransformation);
				if (result == null) result = caseApplyTransformation(groupByTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.ROLLUP_HIERARCHY: {
				RollupHierarchy rollupHierarchy = (RollupHierarchy)theEObject;
				T result = caseRollupHierarchy(rollupHierarchy);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.AGGREGATE_TRANSFORMATION: {
				AggregateTransformation aggregateTransformation = (AggregateTransformation)theEObject;
				T result = caseAggregateTransformation(aggregateTransformation);
				if (result == null) result = caseApplyTransformation(aggregateTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.AGGREGATE_EXPRESSION: {
				AggregateExpression aggregateExpression = (AggregateExpression)theEObject;
				T result = caseAggregateExpression(aggregateExpression);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.AGGREGATE_FROM: {
				AggregateFrom aggregateFrom = (AggregateFrom)theEObject;
				T result = caseAggregateFrom(aggregateFrom);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION: {
				BottomTopTransformation bottomTopTransformation = (BottomTopTransformation)theEObject;
				T result = caseBottomTopTransformation(bottomTopTransformation);
				if (result == null) result = caseApplyTransformation(bottomTopTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.CONCAT_TRANSFORMATION: {
				ConcatTransformation concatTransformation = (ConcatTransformation)theEObject;
				T result = caseConcatTransformation(concatTransformation);
				if (result == null) result = caseApplyTransformation(concatTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.TOP_TRANSFORMATION: {
				TopTransformation topTransformation = (TopTransformation)theEObject;
				T result = caseTopTransformation(topTransformation);
				if (result == null) result = caseApplyTransformation(topTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.SKIP_TRANSFORMATION: {
				SkipTransformation skipTransformation = (SkipTransformation)theEObject;
				T result = caseSkipTransformation(skipTransformation);
				if (result == null) result = caseApplyTransformation(skipTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.IDENTITY_TRANSFORMATION: {
				IdentityTransformation identityTransformation = (IdentityTransformation)theEObject;
				T result = caseIdentityTransformation(identityTransformation);
				if (result == null) result = caseApplyTransformation(identityTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.ORDER_BY_TRANSFORMATION: {
				OrderByTransformation orderByTransformation = (OrderByTransformation)theEObject;
				T result = caseOrderByTransformation(orderByTransformation);
				if (result == null) result = caseApplyTransformation(orderByTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.ORDER_BY_EXPRESSION: {
				OrderByExpression orderByExpression = (OrderByExpression)theEObject;
				T result = caseOrderByExpression(orderByExpression);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.COMPUTE_TRANSFORMATION: {
				ComputeTransformation computeTransformation = (ComputeTransformation)theEObject;
				T result = caseComputeTransformation(computeTransformation);
				if (result == null) result = caseApplyTransformation(computeTransformation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ApplyPackage.COMPUTE_EXPRESSION: {
				ComputeExpression computeExpression = (ComputeExpression)theEObject;
				T result = caseComputeExpression(computeExpression);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Pipeline</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Pipeline</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseApplyPipeline(ApplyPipeline object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseApplyTransformation(ApplyTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Filter Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Filter Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFilterTransformation(FilterTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Group By Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Group By Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGroupByTransformation(GroupByTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Rollup Hierarchy</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Rollup Hierarchy</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRollupHierarchy(RollupHierarchy object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Aggregate Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Aggregate Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAggregateTransformation(AggregateTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Aggregate Expression</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Aggregate Expression</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAggregateExpression(AggregateExpression object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Aggregate From</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Aggregate From</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAggregateFrom(AggregateFrom object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Bottom Top Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Bottom Top Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseBottomTopTransformation(BottomTopTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Concat Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Concat Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseConcatTransformation(ConcatTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Top Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Top Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTopTransformation(TopTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Skip Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Skip Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSkipTransformation(SkipTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Identity Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Identity Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseIdentityTransformation(IdentityTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Order By Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Order By Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOrderByTransformation(OrderByTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Order By Expression</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Order By Expression</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseOrderByExpression(OrderByExpression object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Compute Transformation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Compute Transformation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseComputeTransformation(ComputeTransformation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Compute Expression</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Compute Expression</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseComputeExpression(ComputeExpression object) {
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

} //ApplySwitch
