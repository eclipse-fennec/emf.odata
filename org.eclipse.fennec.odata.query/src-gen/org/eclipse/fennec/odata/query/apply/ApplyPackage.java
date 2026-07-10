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
package org.eclipse.fennec.odata.query.apply;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * OData $apply aggregation pipeline (req §3.5): pipeline semantics are deliberately NOT expressed in OCL — transformation stages are first-class objects, only their embedded expressions (filter predicates, grouping properties, aggregate/compute operands) are OclExpressions from the m2x OCL model (the shared predicate IR). Covers filter/groupby(+rollup)/aggregate(+from, custom methods/aggregates)/compute plus the preserving transformations topcount/topsum/toppercent (and their bottom mirrors), concat, top, skip, orderby and identity; still open: search, nest/addnested, join/outerjoin, ancestors/descendants/traverse, rolluprecursive (E4 backlog).
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.odata.query.apply.ApplyFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = ApplyPackage.eNS_URI, genModel = "/model/apply.genmodel", genModelSourceLocations = {"model/apply.genmodel","org.eclipse.fennec.odata.query/model/apply.genmodel"}, ecore = "/model/apply.ecore", ecoreSourceLocations = "/model/apply.ecore")
public interface ApplyPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "apply";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://eclipse.org/fennec/odata/apply/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "odataapply";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ApplyPackage eINSTANCE = org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.ApplyPipelineImpl <em>Pipeline</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPipelineImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getApplyPipeline()
	 * @generated
	 */
	int APPLY_PIPELINE = 0;

	/**
	 * The feature id for the '<em><b>Transformations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLY_PIPELINE__TRANSFORMATIONS = 0;

	/**
	 * The number of structural features of the '<em>Pipeline</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLY_PIPELINE_FEATURE_COUNT = 1;

	/**
	 * The number of operations of the '<em>Pipeline</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLY_PIPELINE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.ApplyTransformationImpl <em>Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getApplyTransformation()
	 * @generated
	 */
	int APPLY_TRANSFORMATION = 1;

	/**
	 * The number of structural features of the '<em>Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLY_TRANSFORMATION_FEATURE_COUNT = 0;

	/**
	 * The number of operations of the '<em>Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int APPLY_TRANSFORMATION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.FilterTransformationImpl <em>Filter Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.FilterTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getFilterTransformation()
	 * @generated
	 */
	int FILTER_TRANSFORMATION = 2;

	/**
	 * The feature id for the '<em><b>Predicate</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILTER_TRANSFORMATION__PREDICATE = APPLY_TRANSFORMATION_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Filter Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILTER_TRANSFORMATION_FEATURE_COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Filter Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FILTER_TRANSFORMATION_OPERATION_COUNT = APPLY_TRANSFORMATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.GroupByTransformationImpl <em>Group By Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.GroupByTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getGroupByTransformation()
	 * @generated
	 */
	int GROUP_BY_TRANSFORMATION = 3;

	/**
	 * The feature id for the '<em><b>Grouping Properties</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GROUP_BY_TRANSFORMATION__GROUPING_PROPERTIES = APPLY_TRANSFORMATION_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Then</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GROUP_BY_TRANSFORMATION__THEN = APPLY_TRANSFORMATION_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Rollups</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GROUP_BY_TRANSFORMATION__ROLLUPS = APPLY_TRANSFORMATION_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Group By Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GROUP_BY_TRANSFORMATION_FEATURE_COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Group By Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int GROUP_BY_TRANSFORMATION_OPERATION_COUNT = APPLY_TRANSFORMATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.RollupHierarchyImpl <em>Rollup Hierarchy</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.RollupHierarchyImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getRollupHierarchy()
	 * @generated
	 */
	int ROLLUP_HIERARCHY = 4;

	/**
	 * The feature id for the '<em><b>Levels</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROLLUP_HIERARCHY__LEVELS = 0;

	/**
	 * The feature id for the '<em><b>Hierarchy</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROLLUP_HIERARCHY__HIERARCHY = 1;

	/**
	 * The number of structural features of the '<em>Rollup Hierarchy</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROLLUP_HIERARCHY_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Rollup Hierarchy</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ROLLUP_HIERARCHY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.AggregateTransformationImpl <em>Aggregate Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.AggregateTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getAggregateTransformation()
	 * @generated
	 */
	int AGGREGATE_TRANSFORMATION = 5;

	/**
	 * The feature id for the '<em><b>Aggregations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_TRANSFORMATION__AGGREGATIONS = APPLY_TRANSFORMATION_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Aggregate Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_TRANSFORMATION_FEATURE_COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Aggregate Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_TRANSFORMATION_OPERATION_COUNT = APPLY_TRANSFORMATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.AggregateExpressionImpl <em>Aggregate Expression</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.AggregateExpressionImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getAggregateExpression()
	 * @generated
	 */
	int AGGREGATE_EXPRESSION = 6;

	/**
	 * The feature id for the '<em><b>Expression</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_EXPRESSION__EXPRESSION = 0;

	/**
	 * The feature id for the '<em><b>Method</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_EXPRESSION__METHOD = 1;

	/**
	 * The feature id for the '<em><b>Alias</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_EXPRESSION__ALIAS = 2;

	/**
	 * The feature id for the '<em><b>Custom Method</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_EXPRESSION__CUSTOM_METHOD = 3;

	/**
	 * The feature id for the '<em><b>From</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_EXPRESSION__FROM = 4;

	/**
	 * The number of structural features of the '<em>Aggregate Expression</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_EXPRESSION_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Aggregate Expression</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_EXPRESSION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.AggregateFromImpl <em>Aggregate From</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.AggregateFromImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getAggregateFrom()
	 * @generated
	 */
	int AGGREGATE_FROM = 7;

	/**
	 * The feature id for the '<em><b>Grouping Properties</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_FROM__GROUPING_PROPERTIES = 0;

	/**
	 * The feature id for the '<em><b>Method</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_FROM__METHOD = 1;

	/**
	 * The feature id for the '<em><b>Custom Method</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_FROM__CUSTOM_METHOD = 2;

	/**
	 * The number of structural features of the '<em>Aggregate From</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_FROM_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Aggregate From</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int AGGREGATE_FROM_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.BottomTopTransformationImpl <em>Bottom Top Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.BottomTopTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getBottomTopTransformation()
	 * @generated
	 */
	int BOTTOM_TOP_TRANSFORMATION = 8;

	/**
	 * The feature id for the '<em><b>Method</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOTTOM_TOP_TRANSFORMATION__METHOD = APPLY_TRANSFORMATION_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Threshold</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOTTOM_TOP_TRANSFORMATION__THRESHOLD = APPLY_TRANSFORMATION_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOTTOM_TOP_TRANSFORMATION__VALUE = APPLY_TRANSFORMATION_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>Bottom Top Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOTTOM_TOP_TRANSFORMATION_FEATURE_COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 3;

	/**
	 * The number of operations of the '<em>Bottom Top Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int BOTTOM_TOP_TRANSFORMATION_OPERATION_COUNT = APPLY_TRANSFORMATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.ConcatTransformationImpl <em>Concat Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.ConcatTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getConcatTransformation()
	 * @generated
	 */
	int CONCAT_TRANSFORMATION = 9;

	/**
	 * The feature id for the '<em><b>Pipelines</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONCAT_TRANSFORMATION__PIPELINES = APPLY_TRANSFORMATION_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Concat Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONCAT_TRANSFORMATION_FEATURE_COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Concat Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONCAT_TRANSFORMATION_OPERATION_COUNT = APPLY_TRANSFORMATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.TopTransformationImpl <em>Top Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.TopTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getTopTransformation()
	 * @generated
	 */
	int TOP_TRANSFORMATION = 10;

	/**
	 * The feature id for the '<em><b>Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOP_TRANSFORMATION__COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Top Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOP_TRANSFORMATION_FEATURE_COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Top Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TOP_TRANSFORMATION_OPERATION_COUNT = APPLY_TRANSFORMATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.SkipTransformationImpl <em>Skip Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.SkipTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getSkipTransformation()
	 * @generated
	 */
	int SKIP_TRANSFORMATION = 11;

	/**
	 * The feature id for the '<em><b>Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SKIP_TRANSFORMATION__COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Skip Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SKIP_TRANSFORMATION_FEATURE_COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Skip Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int SKIP_TRANSFORMATION_OPERATION_COUNT = APPLY_TRANSFORMATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.IdentityTransformationImpl <em>Identity Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.IdentityTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getIdentityTransformation()
	 * @generated
	 */
	int IDENTITY_TRANSFORMATION = 12;

	/**
	 * The number of structural features of the '<em>Identity Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTITY_TRANSFORMATION_FEATURE_COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 0;

	/**
	 * The number of operations of the '<em>Identity Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IDENTITY_TRANSFORMATION_OPERATION_COUNT = APPLY_TRANSFORMATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.OrderByTransformationImpl <em>Order By Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.OrderByTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getOrderByTransformation()
	 * @generated
	 */
	int ORDER_BY_TRANSFORMATION = 13;

	/**
	 * The feature id for the '<em><b>Items</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORDER_BY_TRANSFORMATION__ITEMS = APPLY_TRANSFORMATION_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Order By Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORDER_BY_TRANSFORMATION_FEATURE_COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Order By Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORDER_BY_TRANSFORMATION_OPERATION_COUNT = APPLY_TRANSFORMATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.OrderByExpressionImpl <em>Order By Expression</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.OrderByExpressionImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getOrderByExpression()
	 * @generated
	 */
	int ORDER_BY_EXPRESSION = 14;

	/**
	 * The feature id for the '<em><b>Expression</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORDER_BY_EXPRESSION__EXPRESSION = 0;

	/**
	 * The feature id for the '<em><b>Ascending</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORDER_BY_EXPRESSION__ASCENDING = 1;

	/**
	 * The number of structural features of the '<em>Order By Expression</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORDER_BY_EXPRESSION_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Order By Expression</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ORDER_BY_EXPRESSION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.ComputeTransformationImpl <em>Compute Transformation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.ComputeTransformationImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getComputeTransformation()
	 * @generated
	 */
	int COMPUTE_TRANSFORMATION = 15;

	/**
	 * The feature id for the '<em><b>Compute Expressions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPUTE_TRANSFORMATION__COMPUTE_EXPRESSIONS = APPLY_TRANSFORMATION_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>Compute Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPUTE_TRANSFORMATION_FEATURE_COUNT = APPLY_TRANSFORMATION_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>Compute Transformation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPUTE_TRANSFORMATION_OPERATION_COUNT = APPLY_TRANSFORMATION_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.impl.ComputeExpressionImpl <em>Compute Expression</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.impl.ComputeExpressionImpl
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getComputeExpression()
	 * @generated
	 */
	int COMPUTE_EXPRESSION = 16;

	/**
	 * The feature id for the '<em><b>Expression</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPUTE_EXPRESSION__EXPRESSION = 0;

	/**
	 * The feature id for the '<em><b>Alias</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPUTE_EXPRESSION__ALIAS = 1;

	/**
	 * The number of structural features of the '<em>Compute Expression</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPUTE_EXPRESSION_FEATURE_COUNT = 2;

	/**
	 * The number of operations of the '<em>Compute Expression</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COMPUTE_EXPRESSION_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.AggregateMethod <em>Aggregate Method</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.AggregateMethod
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getAggregateMethod()
	 * @generated
	 */
	int AGGREGATE_METHOD = 17;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.odata.query.apply.BottomTopMethod <em>Bottom Top Method</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.odata.query.apply.BottomTopMethod
	 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getBottomTopMethod()
	 * @generated
	 */
	int BOTTOM_TOP_METHOD = 18;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.ApplyPipeline <em>Pipeline</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Pipeline</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPipeline
	 * @generated
	 */
	EClass getApplyPipeline();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.query.apply.ApplyPipeline#getTransformations <em>Transformations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Transformations</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPipeline#getTransformations()
	 * @see #getApplyPipeline()
	 * @generated
	 */
	EReference getApplyPipeline_Transformations();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.ApplyTransformation <em>Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.ApplyTransformation
	 * @generated
	 */
	EClass getApplyTransformation();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.FilterTransformation <em>Filter Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Filter Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.FilterTransformation
	 * @generated
	 */
	EClass getFilterTransformation();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.odata.query.apply.FilterTransformation#getPredicate <em>Predicate</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Predicate</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.FilterTransformation#getPredicate()
	 * @see #getFilterTransformation()
	 * @generated
	 */
	EReference getFilterTransformation_Predicate();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.GroupByTransformation <em>Group By Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Group By Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.GroupByTransformation
	 * @generated
	 */
	EClass getGroupByTransformation();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.query.apply.GroupByTransformation#getGroupingProperties <em>Grouping Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Grouping Properties</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.GroupByTransformation#getGroupingProperties()
	 * @see #getGroupByTransformation()
	 * @generated
	 */
	EReference getGroupByTransformation_GroupingProperties();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.odata.query.apply.GroupByTransformation#getThen <em>Then</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Then</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.GroupByTransformation#getThen()
	 * @see #getGroupByTransformation()
	 * @generated
	 */
	EReference getGroupByTransformation_Then();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.query.apply.GroupByTransformation#getRollups <em>Rollups</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Rollups</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.GroupByTransformation#getRollups()
	 * @see #getGroupByTransformation()
	 * @generated
	 */
	EReference getGroupByTransformation_Rollups();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.RollupHierarchy <em>Rollup Hierarchy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Rollup Hierarchy</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.RollupHierarchy
	 * @generated
	 */
	EClass getRollupHierarchy();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.query.apply.RollupHierarchy#getLevels <em>Levels</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Levels</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.RollupHierarchy#getLevels()
	 * @see #getRollupHierarchy()
	 * @generated
	 */
	EReference getRollupHierarchy_Levels();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.RollupHierarchy#getHierarchy <em>Hierarchy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Hierarchy</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.RollupHierarchy#getHierarchy()
	 * @see #getRollupHierarchy()
	 * @generated
	 */
	EAttribute getRollupHierarchy_Hierarchy();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.AggregateTransformation <em>Aggregate Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Aggregate Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateTransformation
	 * @generated
	 */
	EClass getAggregateTransformation();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.query.apply.AggregateTransformation#getAggregations <em>Aggregations</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Aggregations</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateTransformation#getAggregations()
	 * @see #getAggregateTransformation()
	 * @generated
	 */
	EReference getAggregateTransformation_Aggregations();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.AggregateExpression <em>Aggregate Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Aggregate Expression</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateExpression
	 * @generated
	 */
	EClass getAggregateExpression();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getExpression <em>Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Expression</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateExpression#getExpression()
	 * @see #getAggregateExpression()
	 * @generated
	 */
	EReference getAggregateExpression_Expression();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getMethod <em>Method</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Method</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateExpression#getMethod()
	 * @see #getAggregateExpression()
	 * @generated
	 */
	EAttribute getAggregateExpression_Method();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getAlias <em>Alias</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Alias</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateExpression#getAlias()
	 * @see #getAggregateExpression()
	 * @generated
	 */
	EAttribute getAggregateExpression_Alias();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getCustomMethod <em>Custom Method</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Custom Method</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateExpression#getCustomMethod()
	 * @see #getAggregateExpression()
	 * @generated
	 */
	EAttribute getAggregateExpression_CustomMethod();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.query.apply.AggregateExpression#getFrom <em>From</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>From</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateExpression#getFrom()
	 * @see #getAggregateExpression()
	 * @generated
	 */
	EReference getAggregateExpression_From();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.AggregateFrom <em>Aggregate From</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Aggregate From</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateFrom
	 * @generated
	 */
	EClass getAggregateFrom();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.query.apply.AggregateFrom#getGroupingProperties <em>Grouping Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Grouping Properties</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateFrom#getGroupingProperties()
	 * @see #getAggregateFrom()
	 * @generated
	 */
	EReference getAggregateFrom_GroupingProperties();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.AggregateFrom#getMethod <em>Method</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Method</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateFrom#getMethod()
	 * @see #getAggregateFrom()
	 * @generated
	 */
	EAttribute getAggregateFrom_Method();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.AggregateFrom#getCustomMethod <em>Custom Method</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Custom Method</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateFrom#getCustomMethod()
	 * @see #getAggregateFrom()
	 * @generated
	 */
	EAttribute getAggregateFrom_CustomMethod();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation <em>Bottom Top Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Bottom Top Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.BottomTopTransformation
	 * @generated
	 */
	EClass getBottomTopTransformation();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getMethod <em>Method</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Method</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getMethod()
	 * @see #getBottomTopTransformation()
	 * @generated
	 */
	EAttribute getBottomTopTransformation_Method();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getThreshold <em>Threshold</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Threshold</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getThreshold()
	 * @see #getBottomTopTransformation()
	 * @generated
	 */
	EReference getBottomTopTransformation_Threshold();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.BottomTopTransformation#getValue()
	 * @see #getBottomTopTransformation()
	 * @generated
	 */
	EReference getBottomTopTransformation_Value();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.ConcatTransformation <em>Concat Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Concat Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.ConcatTransformation
	 * @generated
	 */
	EClass getConcatTransformation();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.query.apply.ConcatTransformation#getPipelines <em>Pipelines</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Pipelines</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.ConcatTransformation#getPipelines()
	 * @see #getConcatTransformation()
	 * @generated
	 */
	EReference getConcatTransformation_Pipelines();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.TopTransformation <em>Top Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Top Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.TopTransformation
	 * @generated
	 */
	EClass getTopTransformation();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.TopTransformation#getCount <em>Count</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Count</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.TopTransformation#getCount()
	 * @see #getTopTransformation()
	 * @generated
	 */
	EAttribute getTopTransformation_Count();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.SkipTransformation <em>Skip Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Skip Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.SkipTransformation
	 * @generated
	 */
	EClass getSkipTransformation();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.SkipTransformation#getCount <em>Count</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Count</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.SkipTransformation#getCount()
	 * @see #getSkipTransformation()
	 * @generated
	 */
	EAttribute getSkipTransformation_Count();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.IdentityTransformation <em>Identity Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Identity Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.IdentityTransformation
	 * @generated
	 */
	EClass getIdentityTransformation();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.OrderByTransformation <em>Order By Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Order By Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.OrderByTransformation
	 * @generated
	 */
	EClass getOrderByTransformation();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.query.apply.OrderByTransformation#getItems <em>Items</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Items</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.OrderByTransformation#getItems()
	 * @see #getOrderByTransformation()
	 * @generated
	 */
	EReference getOrderByTransformation_Items();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.OrderByExpression <em>Order By Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Order By Expression</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.OrderByExpression
	 * @generated
	 */
	EClass getOrderByExpression();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.odata.query.apply.OrderByExpression#getExpression <em>Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Expression</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.OrderByExpression#getExpression()
	 * @see #getOrderByExpression()
	 * @generated
	 */
	EReference getOrderByExpression_Expression();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.OrderByExpression#isAscending <em>Ascending</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ascending</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.OrderByExpression#isAscending()
	 * @see #getOrderByExpression()
	 * @generated
	 */
	EAttribute getOrderByExpression_Ascending();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.ComputeTransformation <em>Compute Transformation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Compute Transformation</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.ComputeTransformation
	 * @generated
	 */
	EClass getComputeTransformation();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.odata.query.apply.ComputeTransformation#getComputeExpressions <em>Compute Expressions</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Compute Expressions</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.ComputeTransformation#getComputeExpressions()
	 * @see #getComputeTransformation()
	 * @generated
	 */
	EReference getComputeTransformation_ComputeExpressions();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.odata.query.apply.ComputeExpression <em>Compute Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Compute Expression</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.ComputeExpression
	 * @generated
	 */
	EClass getComputeExpression();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.odata.query.apply.ComputeExpression#getExpression <em>Expression</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Expression</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.ComputeExpression#getExpression()
	 * @see #getComputeExpression()
	 * @generated
	 */
	EReference getComputeExpression_Expression();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.odata.query.apply.ComputeExpression#getAlias <em>Alias</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Alias</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.ComputeExpression#getAlias()
	 * @see #getComputeExpression()
	 * @generated
	 */
	EAttribute getComputeExpression_Alias();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.odata.query.apply.AggregateMethod <em>Aggregate Method</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Aggregate Method</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.AggregateMethod
	 * @generated
	 */
	EEnum getAggregateMethod();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.odata.query.apply.BottomTopMethod <em>Bottom Top Method</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Bottom Top Method</em>'.
	 * @see org.eclipse.fennec.odata.query.apply.BottomTopMethod
	 * @generated
	 */
	EEnum getBottomTopMethod();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	ApplyFactory getApplyFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.ApplyPipelineImpl <em>Pipeline</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPipelineImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getApplyPipeline()
		 * @generated
		 */
		EClass APPLY_PIPELINE = eINSTANCE.getApplyPipeline();

		/**
		 * The meta object literal for the '<em><b>Transformations</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference APPLY_PIPELINE__TRANSFORMATIONS = eINSTANCE.getApplyPipeline_Transformations();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.ApplyTransformationImpl <em>Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getApplyTransformation()
		 * @generated
		 */
		EClass APPLY_TRANSFORMATION = eINSTANCE.getApplyTransformation();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.FilterTransformationImpl <em>Filter Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.FilterTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getFilterTransformation()
		 * @generated
		 */
		EClass FILTER_TRANSFORMATION = eINSTANCE.getFilterTransformation();

		/**
		 * The meta object literal for the '<em><b>Predicate</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference FILTER_TRANSFORMATION__PREDICATE = eINSTANCE.getFilterTransformation_Predicate();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.GroupByTransformationImpl <em>Group By Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.GroupByTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getGroupByTransformation()
		 * @generated
		 */
		EClass GROUP_BY_TRANSFORMATION = eINSTANCE.getGroupByTransformation();

		/**
		 * The meta object literal for the '<em><b>Grouping Properties</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GROUP_BY_TRANSFORMATION__GROUPING_PROPERTIES = eINSTANCE.getGroupByTransformation_GroupingProperties();

		/**
		 * The meta object literal for the '<em><b>Then</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GROUP_BY_TRANSFORMATION__THEN = eINSTANCE.getGroupByTransformation_Then();

		/**
		 * The meta object literal for the '<em><b>Rollups</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference GROUP_BY_TRANSFORMATION__ROLLUPS = eINSTANCE.getGroupByTransformation_Rollups();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.RollupHierarchyImpl <em>Rollup Hierarchy</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.RollupHierarchyImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getRollupHierarchy()
		 * @generated
		 */
		EClass ROLLUP_HIERARCHY = eINSTANCE.getRollupHierarchy();

		/**
		 * The meta object literal for the '<em><b>Levels</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ROLLUP_HIERARCHY__LEVELS = eINSTANCE.getRollupHierarchy_Levels();

		/**
		 * The meta object literal for the '<em><b>Hierarchy</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ROLLUP_HIERARCHY__HIERARCHY = eINSTANCE.getRollupHierarchy_Hierarchy();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.AggregateTransformationImpl <em>Aggregate Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.AggregateTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getAggregateTransformation()
		 * @generated
		 */
		EClass AGGREGATE_TRANSFORMATION = eINSTANCE.getAggregateTransformation();

		/**
		 * The meta object literal for the '<em><b>Aggregations</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference AGGREGATE_TRANSFORMATION__AGGREGATIONS = eINSTANCE.getAggregateTransformation_Aggregations();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.AggregateExpressionImpl <em>Aggregate Expression</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.AggregateExpressionImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getAggregateExpression()
		 * @generated
		 */
		EClass AGGREGATE_EXPRESSION = eINSTANCE.getAggregateExpression();

		/**
		 * The meta object literal for the '<em><b>Expression</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference AGGREGATE_EXPRESSION__EXPRESSION = eINSTANCE.getAggregateExpression_Expression();

		/**
		 * The meta object literal for the '<em><b>Method</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AGGREGATE_EXPRESSION__METHOD = eINSTANCE.getAggregateExpression_Method();

		/**
		 * The meta object literal for the '<em><b>Alias</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AGGREGATE_EXPRESSION__ALIAS = eINSTANCE.getAggregateExpression_Alias();

		/**
		 * The meta object literal for the '<em><b>Custom Method</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AGGREGATE_EXPRESSION__CUSTOM_METHOD = eINSTANCE.getAggregateExpression_CustomMethod();

		/**
		 * The meta object literal for the '<em><b>From</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference AGGREGATE_EXPRESSION__FROM = eINSTANCE.getAggregateExpression_From();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.AggregateFromImpl <em>Aggregate From</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.AggregateFromImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getAggregateFrom()
		 * @generated
		 */
		EClass AGGREGATE_FROM = eINSTANCE.getAggregateFrom();

		/**
		 * The meta object literal for the '<em><b>Grouping Properties</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference AGGREGATE_FROM__GROUPING_PROPERTIES = eINSTANCE.getAggregateFrom_GroupingProperties();

		/**
		 * The meta object literal for the '<em><b>Method</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AGGREGATE_FROM__METHOD = eINSTANCE.getAggregateFrom_Method();

		/**
		 * The meta object literal for the '<em><b>Custom Method</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute AGGREGATE_FROM__CUSTOM_METHOD = eINSTANCE.getAggregateFrom_CustomMethod();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.BottomTopTransformationImpl <em>Bottom Top Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.BottomTopTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getBottomTopTransformation()
		 * @generated
		 */
		EClass BOTTOM_TOP_TRANSFORMATION = eINSTANCE.getBottomTopTransformation();

		/**
		 * The meta object literal for the '<em><b>Method</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute BOTTOM_TOP_TRANSFORMATION__METHOD = eINSTANCE.getBottomTopTransformation_Method();

		/**
		 * The meta object literal for the '<em><b>Threshold</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BOTTOM_TOP_TRANSFORMATION__THRESHOLD = eINSTANCE.getBottomTopTransformation_Threshold();

		/**
		 * The meta object literal for the '<em><b>Value</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference BOTTOM_TOP_TRANSFORMATION__VALUE = eINSTANCE.getBottomTopTransformation_Value();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.ConcatTransformationImpl <em>Concat Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.ConcatTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getConcatTransformation()
		 * @generated
		 */
		EClass CONCAT_TRANSFORMATION = eINSTANCE.getConcatTransformation();

		/**
		 * The meta object literal for the '<em><b>Pipelines</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONCAT_TRANSFORMATION__PIPELINES = eINSTANCE.getConcatTransformation_Pipelines();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.TopTransformationImpl <em>Top Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.TopTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getTopTransformation()
		 * @generated
		 */
		EClass TOP_TRANSFORMATION = eINSTANCE.getTopTransformation();

		/**
		 * The meta object literal for the '<em><b>Count</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TOP_TRANSFORMATION__COUNT = eINSTANCE.getTopTransformation_Count();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.SkipTransformationImpl <em>Skip Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.SkipTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getSkipTransformation()
		 * @generated
		 */
		EClass SKIP_TRANSFORMATION = eINSTANCE.getSkipTransformation();

		/**
		 * The meta object literal for the '<em><b>Count</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute SKIP_TRANSFORMATION__COUNT = eINSTANCE.getSkipTransformation_Count();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.IdentityTransformationImpl <em>Identity Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.IdentityTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getIdentityTransformation()
		 * @generated
		 */
		EClass IDENTITY_TRANSFORMATION = eINSTANCE.getIdentityTransformation();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.OrderByTransformationImpl <em>Order By Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.OrderByTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getOrderByTransformation()
		 * @generated
		 */
		EClass ORDER_BY_TRANSFORMATION = eINSTANCE.getOrderByTransformation();

		/**
		 * The meta object literal for the '<em><b>Items</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ORDER_BY_TRANSFORMATION__ITEMS = eINSTANCE.getOrderByTransformation_Items();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.OrderByExpressionImpl <em>Order By Expression</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.OrderByExpressionImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getOrderByExpression()
		 * @generated
		 */
		EClass ORDER_BY_EXPRESSION = eINSTANCE.getOrderByExpression();

		/**
		 * The meta object literal for the '<em><b>Expression</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference ORDER_BY_EXPRESSION__EXPRESSION = eINSTANCE.getOrderByExpression_Expression();

		/**
		 * The meta object literal for the '<em><b>Ascending</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ORDER_BY_EXPRESSION__ASCENDING = eINSTANCE.getOrderByExpression_Ascending();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.ComputeTransformationImpl <em>Compute Transformation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.ComputeTransformationImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getComputeTransformation()
		 * @generated
		 */
		EClass COMPUTE_TRANSFORMATION = eINSTANCE.getComputeTransformation();

		/**
		 * The meta object literal for the '<em><b>Compute Expressions</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference COMPUTE_TRANSFORMATION__COMPUTE_EXPRESSIONS = eINSTANCE.getComputeTransformation_ComputeExpressions();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.impl.ComputeExpressionImpl <em>Compute Expression</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.impl.ComputeExpressionImpl
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getComputeExpression()
		 * @generated
		 */
		EClass COMPUTE_EXPRESSION = eINSTANCE.getComputeExpression();

		/**
		 * The meta object literal for the '<em><b>Expression</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference COMPUTE_EXPRESSION__EXPRESSION = eINSTANCE.getComputeExpression_Expression();

		/**
		 * The meta object literal for the '<em><b>Alias</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COMPUTE_EXPRESSION__ALIAS = eINSTANCE.getComputeExpression_Alias();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.AggregateMethod <em>Aggregate Method</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.AggregateMethod
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getAggregateMethod()
		 * @generated
		 */
		EEnum AGGREGATE_METHOD = eINSTANCE.getAggregateMethod();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.odata.query.apply.BottomTopMethod <em>Bottom Top Method</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.odata.query.apply.BottomTopMethod
		 * @see org.eclipse.fennec.odata.query.apply.impl.ApplyPackageImpl#getBottomTopMethod()
		 * @generated
		 */
		EEnum BOTTOM_TOP_METHOD = eINSTANCE.getBottomTopMethod();

	}

} //ApplyPackage
