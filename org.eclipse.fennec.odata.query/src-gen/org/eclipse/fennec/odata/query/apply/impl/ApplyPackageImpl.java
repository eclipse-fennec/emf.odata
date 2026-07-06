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
import org.eclipse.fennec.odata.query.apply.AggregateMethod;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyFactory;
import org.eclipse.fennec.odata.query.apply.ApplyPackage;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ApplyTransformation;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.eclipse.fennec.odata.query.apply.FilterTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;

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

		aggregateTransformationEClass = createEClass(AGGREGATE_TRANSFORMATION);
		createEReference(aggregateTransformationEClass, AGGREGATE_TRANSFORMATION__AGGREGATIONS);

		aggregateExpressionEClass = createEClass(AGGREGATE_EXPRESSION);
		createEReference(aggregateExpressionEClass, AGGREGATE_EXPRESSION__EXPRESSION);
		createEAttribute(aggregateExpressionEClass, AGGREGATE_EXPRESSION__METHOD);
		createEAttribute(aggregateExpressionEClass, AGGREGATE_EXPRESSION__ALIAS);

		computeTransformationEClass = createEClass(COMPUTE_TRANSFORMATION);
		createEReference(computeTransformationEClass, COMPUTE_TRANSFORMATION__COMPUTE_EXPRESSIONS);

		computeExpressionEClass = createEClass(COMPUTE_EXPRESSION);
		createEReference(computeExpressionEClass, COMPUTE_EXPRESSION__EXPRESSION);
		createEAttribute(computeExpressionEClass, COMPUTE_EXPRESSION__ALIAS);

		// Create enums
		aggregateMethodEEnum = createEEnum(AGGREGATE_METHOD);
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

		initEClass(aggregateTransformationEClass, AggregateTransformation.class, "AggregateTransformation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getAggregateTransformation_Aggregations(), this.getAggregateExpression(), null, "aggregations", null, 0, -1, AggregateTransformation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(aggregateExpressionEClass, AggregateExpression.class, "AggregateExpression", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getAggregateExpression_Expression(), theOclPackage.getOclExpression(), null, "expression", null, 0, 1, AggregateExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAggregateExpression_Method(), this.getAggregateMethod(), "method", null, 0, 1, AggregateExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAggregateExpression_Alias(), ecorePackage.getEString(), "alias", null, 0, 1, AggregateExpression.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

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

		// Create resource
		createResource(eNS_URI);
	}

} //ApplyPackageImpl
