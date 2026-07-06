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
package org.eclipse.fennec.odata.metadata.odata;

import org.eclipse.fennec.model.metadata.FeatureAspect;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Feature OData Aspect</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * OData feature-level aspect for an EAttribute → CSDL Property: facets and key membership.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isKey <em>Key</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isNullable <em>Nullable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isComputed <em>Computed</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isImmutable <em>Immutable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getEdmType <em>Edm Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getMaxLength <em>Max Length</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getPrecision <em>Precision</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getScale <em>Scale</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getDefaultValue <em>Default Value</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getFeatureODataAspect()
 * @model
 * @generated
 */
@ProviderType
public interface FeatureODataAspect extends FeatureAspect {
	/**
	 * Returns the value of the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Part of the entity key. From @OData.Key (defaults to EAttribute.iD).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Key</em>' attribute.
	 * @see #setKey(boolean)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getFeatureODataAspect_Key()
	 * @model
	 * @generated
	 */
	boolean isKey();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isKey <em>Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Key</em>' attribute.
	 * @see #isKey()
	 * @generated
	 */
	void setKey(boolean value);

	/**
	 * Returns the value of the '<em><b>Nullable</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL Nullable (defaults to EStructuralFeature.lowerBound == 0).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Nullable</em>' attribute.
	 * @see #setNullable(boolean)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getFeatureODataAspect_Nullable()
	 * @model default="true"
	 * @generated
	 */
	boolean isNullable();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isNullable <em>Nullable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Nullable</em>' attribute.
	 * @see #isNullable()
	 * @generated
	 */
	void setNullable(boolean value);

	/**
	 * Returns the value of the '<em><b>Computed</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Org.OData.Core.V1.Computed. From @OData.Property.Computed.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Computed</em>' attribute.
	 * @see #setComputed(boolean)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getFeatureODataAspect_Computed()
	 * @model
	 * @generated
	 */
	boolean isComputed();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isComputed <em>Computed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Computed</em>' attribute.
	 * @see #isComputed()
	 * @generated
	 */
	void setComputed(boolean value);

	/**
	 * Returns the value of the '<em><b>Immutable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Org.OData.Core.V1.Immutable. From @OData.Property.Immutable.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Immutable</em>' attribute.
	 * @see #setImmutable(boolean)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getFeatureODataAspect_Immutable()
	 * @model
	 * @generated
	 */
	boolean isImmutable();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#isImmutable <em>Immutable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Immutable</em>' attribute.
	 * @see #isImmutable()
	 * @generated
	 */
	void setImmutable(boolean value);

	/**
	 * Returns the value of the '<em><b>Edm Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Explicit Edm type name override (e.g. Edm.Guid). From @OData.Type; otherwise derived from the EDataType mapping.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Edm Type</em>' attribute.
	 * @see #setEdmType(String)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getFeatureODataAspect_EdmType()
	 * @model
	 * @generated
	 */
	String getEdmType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getEdmType <em>Edm Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Edm Type</em>' attribute.
	 * @see #getEdmType()
	 * @generated
	 */
	void setEdmType(String value);

	/**
	 * Returns the value of the '<em><b>Max Length</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL MaxLength facet; -1 = unset.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Max Length</em>' attribute.
	 * @see #setMaxLength(int)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getFeatureODataAspect_MaxLength()
	 * @model default="-1"
	 * @generated
	 */
	int getMaxLength();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getMaxLength <em>Max Length</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Length</em>' attribute.
	 * @see #getMaxLength()
	 * @generated
	 */
	void setMaxLength(int value);

	/**
	 * Returns the value of the '<em><b>Precision</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL Precision facet; -1 = unset.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Precision</em>' attribute.
	 * @see #setPrecision(int)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getFeatureODataAspect_Precision()
	 * @model default="-1"
	 * @generated
	 */
	int getPrecision();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getPrecision <em>Precision</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Precision</em>' attribute.
	 * @see #getPrecision()
	 * @generated
	 */
	void setPrecision(int value);

	/**
	 * Returns the value of the '<em><b>Scale</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL Scale facet; -1 = unset.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Scale</em>' attribute.
	 * @see #setScale(int)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getFeatureODataAspect_Scale()
	 * @model default="-1"
	 * @generated
	 */
	int getScale();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getScale <em>Scale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scale</em>' attribute.
	 * @see #getScale()
	 * @generated
	 */
	void setScale(int value);

	/**
	 * Returns the value of the '<em><b>Default Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * CSDL DefaultValue.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Default Value</em>' attribute.
	 * @see #setDefaultValue(String)
	 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getFeatureODataAspect_DefaultValue()
	 * @model
	 * @generated
	 */
	String getDefaultValue();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect#getDefaultValue <em>Default Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Default Value</em>' attribute.
	 * @see #getDefaultValue()
	 * @generated
	 */
	void setDefaultValue(String value);

} // FeatureODataAspect
