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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.m2x.model.ocl.OclPackage;

import org.eclipse.fennec.odata.query.apply.AggregateExpression;
import org.eclipse.fennec.odata.query.apply.AggregateFrom;
import org.eclipse.fennec.odata.query.apply.AggregateMethod;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyFactory;
import org.eclipse.fennec.odata.query.apply.ApplyPackage;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ApplyTransformation;
import org.eclipse.fennec.odata.query.apply.BottomTopMethod;
import org.eclipse.fennec.odata.query.apply.BottomTopTransformation;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.eclipse.fennec.odata.query.apply.ConcatTransformation;
import org.eclipse.fennec.odata.query.apply.FilterTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;
import org.eclipse.fennec.odata.query.apply.IdentityTransformation;
import org.eclipse.fennec.odata.query.apply.OrderByExpression;
import org.eclipse.fennec.odata.query.apply.OrderByTransformation;
import org.eclipse.fennec.odata.query.apply.RollupHierarchy;
import org.eclipse.fennec.odata.query.apply.SkipTransformation;
import org.eclipse.fennec.odata.query.apply.TopTransformation;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ApplyPackageImpl extends EPackageImpl implements ApplyPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass applyPipelineEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass applyTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass filterTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass groupByTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass rollupHierarchyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass aggregateTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass aggregateExpressionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass aggregateFromEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass bottomTopTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass concatTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass topTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass skipTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass identityTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass orderByTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass orderByExpressionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass computeTransformationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass computeExpressionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum aggregateMethodEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum bottomTopMethodEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private ApplyPackageImpl() {
		super(eNS_URI, ApplyFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link ApplyPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static ApplyPackage init() {
		if (isInited) return (ApplyPackage)EPackage.Registry.INSTANCE.getEPackage(ApplyPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredApplyPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		ApplyPackageImpl theApplyPackage = registeredApplyPackage instanceof ApplyPackageImpl ? (ApplyPackageImpl)registeredApplyPackage : new ApplyPackageImpl();

		isInited = true;

		// Initialize simple dependencies
		OclPackage.eINSTANCE.eClass();

		// Create package meta-data objects
		theApplyPackage.createPackageContents();

		// Initialize created meta-data
		theApplyPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theApplyPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(ApplyPackage.eNS_URI, theApplyPackage);
		return theApplyPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getApplyPipeline() {
		return applyPipelineEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getApplyPipeline_Transformations() {
		return (EReference)applyPipelineEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getApplyTransformation() {
		return applyTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFilterTransformation() {
		return filterTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getFilterTransformation_Predicate() {
		return (EReference)filterTransformationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGroupByTransformation() {
		return groupByTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGroupByTransformation_GroupingProperties() {
		return (EReference)groupByTransformationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGroupByTransformation_Then() {
		return (EReference)groupByTransformationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGroupByTransformation_Rollups() {
		return (EReference)groupByTransformationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRollupHierarchy() {
		return rollupHierarchyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRollupHierarchy_Levels() {
		return (EReference)rollupHierarchyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRollupHierarchy_Hierarchy() {
		return (EAttribute)rollupHierarchyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAggregateTransformation() {
		return aggregateTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAggregateTransformation_Aggregations() {
		return (EReference)aggregateTransformationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAggregateExpression() {
		return aggregateExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAggregateExpression_Expression() {
		return (EReference)aggregateExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAggregateExpression_Method() {
		return (EAttribute)aggregateExpressionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAggregateExpression_Alias() {
		return (EAttribute)aggregateExpressionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAggregateExpression_CustomMethod() {
		return (EAttribute)aggregateExpressionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAggregateExpression_From() {
		return (EReference)aggregateExpressionEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAggregateFrom() {
		return aggregateFromEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getAggregateFrom_GroupingProperties() {
		return (EReference)aggregateFromEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAggregateFrom_Method() {
		return (EAttribute)aggregateFromEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAggregateFrom_CustomMethod() {
		return (EAttribute)aggregateFromEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getBottomTopTransformation() {
		return bottomTopTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getBottomTopTransformation_Method() {
		return (EAttribute)bottomTopTransformationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBottomTopTransformation_Threshold() {
		return (EReference)bottomTopTransformationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getBottomTopTransformation_Value() {
		return (EReference)bottomTopTransformationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getConcatTransformation() {
		return concatTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getConcatTransformation_Pipelines() {
		return (EReference)concatTransformationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTopTransformation() {
		return topTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTopTransformation_Count() {
		return (EAttribute)topTransformationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSkipTransformation() {
		return skipTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSkipTransformation_Count() {
		return (EAttribute)skipTransformationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIdentityTransformation() {
		return identityTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOrderByTransformation() {
		return orderByTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getOrderByTransformation_Items() {
		return (EReference)orderByTransformationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getOrderByExpression() {
		return orderByExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getOrderByExpression_Expression() {
		return (EReference)orderByExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getOrderByExpression_Ascending() {
		return (EAttribute)orderByExpressionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getComputeTransformation() {
		return computeTransformationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComputeTransformation_ComputeExpressions() {
		return (EReference)computeTransformationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getComputeExpression() {
		return computeExpressionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComputeExpression_Expression() {
		return (EReference)computeExpressionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getComputeExpression_Alias() {
		return (EAttribute)computeExpressionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getAggregateMethod() {
		return aggregateMethodEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getBottomTopMethod() {
		return bottomTopMethodEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ApplyFactory getApplyFactory() {
		return (ApplyFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		applyPipelineEClass = createEClass(APPLY_PIPELINE);
		createEReference(applyPipelineEClass, APPLY_PIPELINE__TRANSFORMATIONS);

		applyTransformationEClass = createEClass(APPLY_TRANSFORMATION);

		filterTransformationEClass = createEClass(FILTER_TRANSFORMATION);
		createEReference(filterTransformationEClass, FILTER_TRANSFORMATION__PREDICATE);

		groupByTransformationEClass = createEClass(GROUP_BY_TRANSFORMATION);
		createEReference(groupByTransformationEClass, GROUP_BY_TRANSFORMATION__GROUPING_PROPERTIES);
		createEReference(groupByTransformationEClass, GROUP_BY_TRANSFORMATION__THEN);
		createEReference(groupByTransformationEClass, GROUP_BY_TRANSFORMATION__ROLLUPS);

		rollupHierarchyEClass = createEClass(ROLLUP_HIERARCHY);
		createEReference(rollupHierarchyEClass, ROLLUP_HIERARCHY__LEVELS);
		createEAttribute(rollupHierarchyEClass, ROLLUP_HIERARCHY__HIERARCHY);

		aggregateTransformationEClass = createEClass(AGGREGATE_TRANSFORMATION);
		createEReference(aggregateTransformationEClass, AGGREGATE_TRANSFORMATION__AGGREGATIONS);

		aggregateExpressionEClass = createEClass(AGGREGATE_EXPRESSION);
		createEReference(aggregateExpressionEClass, AGGREGATE_EXPRESSION__EXPRESSION);
		createEAttribute(aggregateExpressionEClass, AGGREGATE_EXPRESSION__METHOD);
		createEAttribute(aggregateExpressionEClass, AGGREGATE_EXPRESSION__ALIAS);
		createEAttribute(aggregateExpressionEClass, AGGREGATE_EXPRESSION__CUSTOM_METHOD);
		createEReference(aggregateExpressionEClass, AGGREGATE_EXPRESSION__FROM);

		aggregateFromEClass = createEClass(AGGREGATE_FROM);
		createEReference(aggregateFromEClass, AGGREGATE_FROM__GROUPING_PROPERTIES);
		createEAttribute(aggregateFromEClass, AGGREGATE_FROM__METHOD);
		createEAttribute(aggregateFromEClass, AGGREGATE_FROM__CUSTOM_METHOD);

		bottomTopTransformationEClass = createEClass(BOTTOM_TOP_TRANSFORMATION);
		createEAttribute(bottomTopTransformationEClass, BOTTOM_TOP_TRANSFORMATION__METHOD);
		createEReference(bottomTopTransformationEClass, BOTTOM_TOP_TRANSFORMATION__THRESHOLD);
		createEReference(bottomTopTransformationEClass, BOTTOM_TOP_TRANSFORMATION__VALUE);

		concatTransformationEClass = createEClass(CONCAT_TRANSFORMATION);
		createEReference(concatTransformationEClass, CONCAT_TRANSFORMATION__PIPELINES);

		topTransformationEClass = createEClass(TOP_TRANSFORMATION);
		createEAttribute(topTransformationEClass, TOP_TRANSFORMATION__COUNT);

		skipTransformationEClass = createEClass(SKIP_TRANSFORMATION);
		createEAttribute(skipTransformationEClass, SKIP_TRANSFORMATION__COUNT);

		identityTransformationEClass = createEClass(IDENTITY_TRANSFORMATION);

		orderByTransformationEClass = createEClass(ORDER_BY_TRANSFORMATION);
		createEReference(orderByTransformationEClass, ORDER_BY_TRANSFORMATION__ITEMS);

		orderByExpressionEClass = createEClass(ORDER_BY_EXPRESSION);
		createEReference(orderByExpressionEClass, ORDER_BY_EXPRESSION__EXPRESSION);
		createEAttribute(orderByExpressionEClass, ORDER_BY_EXPRESSION__ASCENDING);

		computeTransformationEClass = createEClass(COMPUTE_TRANSFORMATION);
		createEReference(computeTransformationEClass, COMPUTE_TRANSFORMATION__COMPUTE_EXPRESSIONS);

		computeExpressionEClass = createEClass(COMPUTE_EXPRESSION);
		createEReference(computeExpressionEClass, COMPUTE_EXPRESSION__EXPRESSION);
		createEAttribute(computeExpressionEClass, COMPUTE_EXPRESSION__ALIAS);

		// Create enums
		aggregateMethodEEnum = createEEnum(AGGREGATE_METHOD);
		bottomTopMethodEEnum = createEEnum(BOTTOM_TOP_METHOD);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		OclPackage theOclPackage = (OclPackage)EPackage.Registry.INSTANCE.getEPackage(OclPackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		filterTransformationEClass.getESuperTypes().add(this.getApplyTransformation());
		groupByTransformationEClass.getESuperTypes().add(this.getApplyTransformation());
		aggregateTransformationEClass.getESuperTypes().add(this.getApplyTransformation());
		bottomTopTransformationEClass.getESuperTypes().add(this.getApplyTransformation());
		concatTransformationEClass.getESuperTypes().add(this.getApplyTransformation());
		topTransformationEClass.getESuperTypes().add(this.getApplyTransformation());
		skipTransformationEClass.getESuperTypes().add(this.getApplyTransformation());
		identityTransformationEClass.getESuperTypes().add(this.getApplyTransformation());
		orderByTransformationEClass.getESuperTypes().add(this.getApplyTransformation());
		computeTransformationEClass.getESuperTypes().add(this.getApplyTransformation());

		// Initialize classes, features, and operations; add parameters
		initEClass(applyPipelineEClass, ApplyPipeline.class, "ApplyPipeline", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getApplyPipeline_Transformations(), this.getApplyTransformation(), null, "transformations", null, 0, -1, ApplyPipeline.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(applyTransformationEClass, ApplyTransformation.class, "ApplyTransformation", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(filterTransformationEClass, FilterTransformation.class, "FilterTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getFilterTransformation_Predicate(), theOclPackage.getOclExpression(), null, "predicate", null, 0, 1, FilterTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(groupByTransformationEClass, GroupByTransformation.class, "GroupByTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGroupByTransformation_GroupingProperties(), theOclPackage.getOclExpression(), null, "groupingProperties", null, 0, -1, GroupByTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGroupByTransformation_Then(), this.getApplyTransformation(), null, "then", null, 0, 1, GroupByTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGroupByTransformation_Rollups(), this.getRollupHierarchy(), null, "rollups", null, 0, -1, GroupByTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(rollupHierarchyEClass, RollupHierarchy.class, "RollupHierarchy", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRollupHierarchy_Levels(), theOclPackage.getOclExpression(), null, "levels", null, 0, -1, RollupHierarchy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRollupHierarchy_Hierarchy(), ecorePackage.getEString(), "hierarchy", null, 0, 1, RollupHierarchy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(aggregateTransformationEClass, AggregateTransformation.class, "AggregateTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getAggregateTransformation_Aggregations(), this.getAggregateExpression(), null, "aggregations", null, 0, -1, AggregateTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(aggregateExpressionEClass, AggregateExpression.class, "AggregateExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getAggregateExpression_Expression(), theOclPackage.getOclExpression(), null, "expression", null, 0, 1, AggregateExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAggregateExpression_Method(), this.getAggregateMethod(), "method", null, 0, 1, AggregateExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAggregateExpression_Alias(), ecorePackage.getEString(), "alias", null, 0, 1, AggregateExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAggregateExpression_CustomMethod(), ecorePackage.getEString(), "customMethod", null, 0, 1, AggregateExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getAggregateExpression_From(), this.getAggregateFrom(), null, "from", null, 0, -1, AggregateExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(aggregateFromEClass, AggregateFrom.class, "AggregateFrom", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getAggregateFrom_GroupingProperties(), theOclPackage.getOclExpression(), null, "groupingProperties", null, 0, -1, AggregateFrom.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAggregateFrom_Method(), this.getAggregateMethod(), "method", null, 0, 1, AggregateFrom.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAggregateFrom_CustomMethod(), ecorePackage.getEString(), "customMethod", null, 0, 1, AggregateFrom.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(bottomTopTransformationEClass, BottomTopTransformation.class, "BottomTopTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getBottomTopTransformation_Method(), this.getBottomTopMethod(), "method", null, 0, 1, BottomTopTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBottomTopTransformation_Threshold(), theOclPackage.getOclExpression(), null, "threshold", null, 0, 1, BottomTopTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getBottomTopTransformation_Value(), theOclPackage.getOclExpression(), null, "value", null, 0, 1, BottomTopTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(concatTransformationEClass, ConcatTransformation.class, "ConcatTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getConcatTransformation_Pipelines(), this.getApplyPipeline(), null, "pipelines", null, 0, -1, ConcatTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(topTransformationEClass, TopTransformation.class, "TopTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTopTransformation_Count(), ecorePackage.getELong(), "count", null, 0, 1, TopTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(skipTransformationEClass, SkipTransformation.class, "SkipTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSkipTransformation_Count(), ecorePackage.getELong(), "count", null, 0, 1, SkipTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(identityTransformationEClass, IdentityTransformation.class, "IdentityTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(orderByTransformationEClass, OrderByTransformation.class, "OrderByTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getOrderByTransformation_Items(), this.getOrderByExpression(), null, "items", null, 0, -1, OrderByTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(orderByExpressionEClass, OrderByExpression.class, "OrderByExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getOrderByExpression_Expression(), theOclPackage.getOclExpression(), null, "expression", null, 0, 1, OrderByExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getOrderByExpression_Ascending(), ecorePackage.getEBoolean(), "ascending", "true", 0, 1, OrderByExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(computeTransformationEClass, ComputeTransformation.class, "ComputeTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getComputeTransformation_ComputeExpressions(), this.getComputeExpression(), null, "computeExpressions", null, 0, -1, ComputeTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(computeExpressionEClass, ComputeExpression.class, "ComputeExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getComputeExpression_Expression(), theOclPackage.getOclExpression(), null, "expression", null, 0, 1, ComputeExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getComputeExpression_Alias(), ecorePackage.getEString(), "alias", null, 0, 1, ComputeExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(aggregateMethodEEnum, AggregateMethod.class, "AggregateMethod");
		addEEnumLiteral(aggregateMethodEEnum, AggregateMethod.SUM);
		addEEnumLiteral(aggregateMethodEEnum, AggregateMethod.MIN);
		addEEnumLiteral(aggregateMethodEEnum, AggregateMethod.MAX);
		addEEnumLiteral(aggregateMethodEEnum, AggregateMethod.AVERAGE);
		addEEnumLiteral(aggregateMethodEEnum, AggregateMethod.COUNT_DISTINCT);
		addEEnumLiteral(aggregateMethodEEnum, AggregateMethod.COUNT);
		addEEnumLiteral(aggregateMethodEEnum, AggregateMethod.CUSTOM);
		addEEnumLiteral(aggregateMethodEEnum, AggregateMethod.CUSTOM_AGGREGATE);

		initEEnum(bottomTopMethodEEnum, BottomTopMethod.class, "BottomTopMethod");
		addEEnumLiteral(bottomTopMethodEEnum, BottomTopMethod.TOP_COUNT);
		addEEnumLiteral(bottomTopMethodEEnum, BottomTopMethod.TOP_SUM);
		addEEnumLiteral(bottomTopMethodEEnum, BottomTopMethod.TOP_PERCENT);
		addEEnumLiteral(bottomTopMethodEEnum, BottomTopMethod.BOTTOM_COUNT);
		addEEnumLiteral(bottomTopMethodEEnum, BottomTopMethod.BOTTOM_SUM);
		addEEnumLiteral(bottomTopMethodEEnum, BottomTopMethod.BOTTOM_PERCENT);

		// Create resource
		createResource(eNS_URI);
	}

} //ApplyPackageImpl
