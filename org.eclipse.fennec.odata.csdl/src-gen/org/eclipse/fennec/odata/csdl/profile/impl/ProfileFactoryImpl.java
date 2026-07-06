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
package org.eclipse.fennec.odata.csdl.profile.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.odata.csdl.profile.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class ProfileFactoryImpl extends EFactoryImpl implements ProfileFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static ProfileFactory init() {
		try {
			ProfileFactory theProfileFactory = (ProfileFactory)EPackage.Registry.INSTANCE.getEFactory(ProfilePackage.eNS_URI);
			if (theProfileFactory != null) {
				return theProfileFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new ProfileFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ProfileFactoryImpl() {
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
			case ProfilePackage.ODATA_PACKAGE_PROFILE: return createODataPackageProfile();
			case ProfilePackage.ODATA_ENUM_PROFILE: return createODataEnumProfile();
			case ProfilePackage.ODATA_ENUM_MEMBER: return createODataEnumMember();
			case ProfilePackage.ODATA_CLASS_PROFILE: return createODataClassProfile();
			case ProfilePackage.ODATA_PROPERTY_PROFILE: return createODataPropertyProfile();
			case ProfilePackage.ODATA_NAVIGATION_PROFILE: return createODataNavigationProfile();
			case ProfilePackage.ODATA_ANNOTATION: return createODataAnnotation();
			case ProfilePackage.ODATA_REFERENTIAL_CONSTRAINT: return createODataReferentialConstraint();
			case ProfilePackage.ODATA_OPERATION_PROFILE: return createODataOperationProfile();
			case ProfilePackage.ODATA_PARAMETER_PROFILE: return createODataParameterProfile();
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
			case ProfilePackage.ODATA_TYPE_KIND:
				return createODataTypeKindFromString(eDataType, initialValue);
			case ProfilePackage.ODATA_OPERATION_KIND:
				return createODataOperationKindFromString(eDataType, initialValue);
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
			case ProfilePackage.ODATA_TYPE_KIND:
				return convertODataTypeKindToString(eDataType, instanceValue);
			case ProfilePackage.ODATA_OPERATION_KIND:
				return convertODataOperationKindToString(eDataType, instanceValue);
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
	public ODataPackageProfile createODataPackageProfile() {
		ODataPackageProfileImpl oDataPackageProfile = new ODataPackageProfileImpl();
		return oDataPackageProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataEnumProfile createODataEnumProfile() {
		ODataEnumProfileImpl oDataEnumProfile = new ODataEnumProfileImpl();
		return oDataEnumProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataEnumMember createODataEnumMember() {
		ODataEnumMemberImpl oDataEnumMember = new ODataEnumMemberImpl();
		return oDataEnumMember;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataClassProfile createODataClassProfile() {
		ODataClassProfileImpl oDataClassProfile = new ODataClassProfileImpl();
		return oDataClassProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataPropertyProfile createODataPropertyProfile() {
		ODataPropertyProfileImpl oDataPropertyProfile = new ODataPropertyProfileImpl();
		return oDataPropertyProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataNavigationProfile createODataNavigationProfile() {
		ODataNavigationProfileImpl oDataNavigationProfile = new ODataNavigationProfileImpl();
		return oDataNavigationProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataAnnotation createODataAnnotation() {
		ODataAnnotationImpl oDataAnnotation = new ODataAnnotationImpl();
		return oDataAnnotation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataReferentialConstraint createODataReferentialConstraint() {
		ODataReferentialConstraintImpl oDataReferentialConstraint = new ODataReferentialConstraintImpl();
		return oDataReferentialConstraint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataOperationProfile createODataOperationProfile() {
		ODataOperationProfileImpl oDataOperationProfile = new ODataOperationProfileImpl();
		return oDataOperationProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataParameterProfile createODataParameterProfile() {
		ODataParameterProfileImpl oDataParameterProfile = new ODataParameterProfileImpl();
		return oDataParameterProfile;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ODataTypeKind createODataTypeKindFromString(EDataType eDataType, String initialValue) {
		ODataTypeKind result = ODataTypeKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertODataTypeKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ODataOperationKind createODataOperationKindFromString(EDataType eDataType, String initialValue) {
		ODataOperationKind result = ODataOperationKind.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertODataOperationKindToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ProfilePackage getProfilePackage() {
		return (ProfilePackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static ProfilePackage getPackage() {
		return ProfilePackage.eINSTANCE;
	}

} //ProfileFactoryImpl
