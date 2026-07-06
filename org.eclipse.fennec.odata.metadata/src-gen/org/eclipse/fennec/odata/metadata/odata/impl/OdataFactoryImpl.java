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
package org.eclipse.fennec.odata.metadata.odata.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.odata.metadata.odata.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class OdataFactoryImpl extends EFactoryImpl implements OdataFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static OdataFactory init() {
		try {
			OdataFactory theOdataFactory = (OdataFactory)EPackage.Registry.INSTANCE.getEFactory(OdataPackage.eNS_URI);
			if (theOdataFactory != null) {
				return theOdataFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new OdataFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public OdataFactoryImpl() {
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
			case OdataPackage.PACKAGE_ODATA_ASPECT: return createPackageODataAspect();
			case OdataPackage.CLASS_ODATA_ASPECT: return createClassODataAspect();
			case OdataPackage.FEATURE_ODATA_ASPECT: return createFeatureODataAspect();
			case OdataPackage.REFERENCE_ODATA_ASPECT: return createReferenceODataAspect();
			case OdataPackage.ODATA_PACKAGE_PROFILE: return createODataPackageProfile();
			case OdataPackage.ODATA_CLASS_PROFILE: return createODataClassProfile();
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
			case OdataPackage.ODATA_TYPE_KIND:
				return createODataTypeKindFromString(eDataType, initialValue);
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
			case OdataPackage.ODATA_TYPE_KIND:
				return convertODataTypeKindToString(eDataType, instanceValue);
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
	public PackageODataAspect createPackageODataAspect() {
		PackageODataAspectImpl packageODataAspect = new PackageODataAspectImpl();
		return packageODataAspect;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ClassODataAspect createClassODataAspect() {
		ClassODataAspectImpl classODataAspect = new ClassODataAspectImpl();
		return classODataAspect;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureODataAspect createFeatureODataAspect() {
		FeatureODataAspectImpl featureODataAspect = new FeatureODataAspectImpl();
		return featureODataAspect;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReferenceODataAspect createReferenceODataAspect() {
		ReferenceODataAspectImpl referenceODataAspect = new ReferenceODataAspectImpl();
		return referenceODataAspect;
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
	public ODataClassProfile createODataClassProfile() {
		ODataClassProfileImpl oDataClassProfile = new ODataClassProfileImpl();
		return oDataClassProfile;
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
	@Override
	public OdataPackage getOdataPackage() {
		return (OdataPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static OdataPackage getPackage() {
		return OdataPackage.eINSTANCE;
	}

} //OdataFactoryImpl
