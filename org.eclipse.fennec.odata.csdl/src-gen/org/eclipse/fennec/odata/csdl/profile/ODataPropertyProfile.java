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
package org.eclipse.fennec.odata.csdl.profile;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>OData Property Profile</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Resolved CSDL Property (from an EAttribute, or an EReference to a complex type).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getTypeName <em>Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isNullable <em>Nullable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isComputed <em>Computed</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isImmutable <em>Immutable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getMaxLength <em>Max Length</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getPrecision <em>Precision</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getScale <em>Scale</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getDefaultValue <em>Default Value</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getSrid <em>Srid</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getUnicode <em>Unicode</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getAnnotations <em>Annotations</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile()
 * @model
 * @generated
 */
@ProviderType
public interface ODataPropertyProfile extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type Name</em>' attribute.
	 * @see #setTypeName(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_TypeName()
	 * @model
	 * @generated
	 */
	String getTypeName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getTypeName <em>Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type Name</em>' attribute.
	 * @see #getTypeName()
	 * @generated
	 */
	void setTypeName(String value);

	/**
	 * Returns the value of the '<em><b>Nullable</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Nullable</em>' attribute.
	 * @see #setNullable(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_Nullable()
	 * @model default="true"
	 * @generated
	 */
	boolean isNullable();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isNullable <em>Nullable</em>}' attribute.
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
	 * @return the value of the '<em>Computed</em>' attribute.
	 * @see #setComputed(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_Computed()
	 * @model
	 * @generated
	 */
	boolean isComputed();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isComputed <em>Computed</em>}' attribute.
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
	 * @return the value of the '<em>Immutable</em>' attribute.
	 * @see #setImmutable(boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_Immutable()
	 * @model
	 * @generated
	 */
	boolean isImmutable();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#isImmutable <em>Immutable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Immutable</em>' attribute.
	 * @see #isImmutable()
	 * @generated
	 */
	void setImmutable(boolean value);

	/**
	 * Returns the value of the '<em><b>Max Length</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Max Length</em>' attribute.
	 * @see #setMaxLength(int)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_MaxLength()
	 * @model default="-1"
	 * @generated
	 */
	int getMaxLength();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getMaxLength <em>Max Length</em>}' attribute.
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
	 * @return the value of the '<em>Precision</em>' attribute.
	 * @see #setPrecision(int)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_Precision()
	 * @model default="-1"
	 * @generated
	 */
	int getPrecision();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getPrecision <em>Precision</em>}' attribute.
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
	 * @return the value of the '<em>Scale</em>' attribute.
	 * @see #setScale(int)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_Scale()
	 * @model default="-1"
	 * @generated
	 */
	int getScale();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getScale <em>Scale</em>}' attribute.
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
	 * @return the value of the '<em>Default Value</em>' attribute.
	 * @see #setDefaultValue(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_DefaultValue()
	 * @model
	 * @generated
	 */
	String getDefaultValue();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getDefaultValue <em>Default Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Default Value</em>' attribute.
	 * @see #getDefaultValue()
	 * @generated
	 */
	void setDefaultValue(String value);

	/**
	 * Returns the value of the '<em><b>Srid</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * SRID facet — numeric or the symbolic "variable"; null when absent.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Srid</em>' attribute.
	 * @see #setSrid(String)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_Srid()
	 * @model
	 * @generated
	 */
	String getSrid();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getSrid <em>Srid</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Srid</em>' attribute.
	 * @see #getSrid()
	 * @generated
	 */
	void setSrid(String value);

	/**
	 * Returns the value of the '<em><b>Unicode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unicode facet ([OData-CSDL] 7.2.5); null when absent (default true).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Unicode</em>' attribute.
	 * @see #setUnicode(Boolean)
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_Unicode()
	 * @model
	 * @generated
	 */
	Boolean getUnicode();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile#getUnicode <em>Unicode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Unicode</em>' attribute.
	 * @see #getUnicode()
	 * @generated
	 */
	void setUnicode(Boolean value);

	/**
	 * Returns the value of the '<em><b>Annotations</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.odata.csdl.profile.ODataAnnotation}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Annotations</em>' containment reference list.
	 * @see org.eclipse.fennec.odata.csdl.profile.ProfilePackage#getODataPropertyProfile_Annotations()
	 * @model containment="true"
	 * @generated
	 */
	EList<ODataAnnotation> getAnnotations();

} // ODataPropertyProfile
