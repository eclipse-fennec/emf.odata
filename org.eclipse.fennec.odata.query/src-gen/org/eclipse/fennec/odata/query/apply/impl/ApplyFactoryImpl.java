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
package org.eclipse.fennec.odata.query.apply.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.odata.query.apply.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ApplyFactoryImpl extends EFactoryImpl implements ApplyFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ApplyFactory init() {
		try {
			ApplyFactory theApplyFactory = (ApplyFactory)EPackage.Registry.INSTANCE.getEFactory(ApplyPackage.eNS_URI);
			if (theApplyFactory != null) {
				return theApplyFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ApplyFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ApplyFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case ApplyPackage.APPLY_PIPELINE: return createApplyPipeline();
			case ApplyPackage.FILTER_TRANSFORMATION: return createFilterTransformation();
			case ApplyPackage.GROUP_BY_TRANSFORMATION: return createGroupByTransformation();
			case ApplyPackage.ROLLUP_HIERARCHY: return createRollupHierarchy();
			case ApplyPackage.AGGREGATE_TRANSFORMATION: return createAggregateTransformation();
			case ApplyPackage.AGGREGATE_EXPRESSION: return createAggregateExpression();
			case ApplyPackage.AGGREGATE_FROM: return createAggregateFrom();
			case ApplyPackage.BOTTOM_TOP_TRANSFORMATION: return createBottomTopTransformation();
			case ApplyPackage.CONCAT_TRANSFORMATION: return createConcatTransformation();
			case ApplyPackage.TOP_TRANSFORMATION: return createTopTransformation();
			case ApplyPackage.SKIP_TRANSFORMATION: return createSkipTransformation();
			case ApplyPackage.IDENTITY_TRANSFORMATION: return createIdentityTransformation();
			case ApplyPackage.ORDER_BY_TRANSFORMATION: return createOrderByTransformation();
			case ApplyPackage.ORDER_BY_EXPRESSION: return createOrderByExpression();
			case ApplyPackage.COMPUTE_TRANSFORMATION: return createComputeTransformation();
			case ApplyPackage.COMPUTE_EXPRESSION: return createComputeExpression();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case ApplyPackage.AGGREGATE_METHOD:
				return createAggregateMethodFromString(eDataType, initialValue);
			case ApplyPackage.BOTTOM_TOP_METHOD:
				return createBottomTopMethodFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case ApplyPackage.AGGREGATE_METHOD:
				return convertAggregateMethodToString(eDataType, instanceValue);
			case ApplyPackage.BOTTOM_TOP_METHOD:
				return convertBottomTopMethodToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ApplyPipeline createApplyPipeline() {
		ApplyPipelineImpl applyPipeline = new ApplyPipelineImpl();
		return applyPipeline;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FilterTransformation createFilterTransformation() {
		FilterTransformationImpl filterTransformation = new FilterTransformationImpl();
		return filterTransformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GroupByTransformation createGroupByTransformation() {
		GroupByTransformationImpl groupByTransformation = new GroupByTransformationImpl();
		return groupByTransformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RollupHierarchy createRollupHierarchy() {
		RollupHierarchyImpl rollupHierarchy = new RollupHierarchyImpl();
		return rollupHierarchy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AggregateTransformation createAggregateTransformation() {
		AggregateTransformationImpl aggregateTransformation = new AggregateTransformationImpl();
		return aggregateTransformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AggregateExpression createAggregateExpression() {
		AggregateExpressionImpl aggregateExpression = new AggregateExpressionImpl();
		return aggregateExpression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AggregateFrom createAggregateFrom() {
		AggregateFromImpl aggregateFrom = new AggregateFromImpl();
		return aggregateFrom;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public BottomTopTransformation createBottomTopTransformation() {
		BottomTopTransformationImpl bottomTopTransformation = new BottomTopTransformationImpl();
		return bottomTopTransformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ConcatTransformation createConcatTransformation() {
		ConcatTransformationImpl concatTransformation = new ConcatTransformationImpl();
		return concatTransformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TopTransformation createTopTransformation() {
		TopTransformationImpl topTransformation = new TopTransformationImpl();
		return topTransformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SkipTransformation createSkipTransformation() {
		SkipTransformationImpl skipTransformation = new SkipTransformationImpl();
		return skipTransformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IdentityTransformation createIdentityTransformation() {
		IdentityTransformationImpl identityTransformation = new IdentityTransformationImpl();
		return identityTransformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OrderByTransformation createOrderByTransformation() {
		OrderByTransformationImpl orderByTransformation = new OrderByTransformationImpl();
		return orderByTransformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OrderByExpression createOrderByExpression() {
		OrderByExpressionImpl orderByExpression = new OrderByExpressionImpl();
		return orderByExpression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ComputeTransformation createComputeTransformation() {
		ComputeTransformationImpl computeTransformation = new ComputeTransformationImpl();
		return computeTransformation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ComputeExpression createComputeExpression() {
		ComputeExpressionImpl computeExpression = new ComputeExpressionImpl();
		return computeExpression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public AggregateMethod createAggregateMethodFromString(EDataType eDataType, String initialValue) {
		AggregateMethod result = AggregateMethod.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertAggregateMethodToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BottomTopMethod createBottomTopMethodFromString(EDataType eDataType, String initialValue) {
		BottomTopMethod result = BottomTopMethod.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertBottomTopMethodToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ApplyPackage getApplyPackage() {
		return (ApplyPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ApplyPackage getPackage() {
		return ApplyPackage.eINSTANCE;
	}

} //ApplyFactoryImpl
