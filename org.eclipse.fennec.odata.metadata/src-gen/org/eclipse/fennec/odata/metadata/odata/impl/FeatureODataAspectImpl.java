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
package org.eclipse.fennec.odata.metadata.odata.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.metadata.impl.FeatureAspectImpl;

import org.eclipse.fennec.odata.metadata.odata.FeatureODataAspect;
import org.eclipse.fennec.odata.metadata.odata.OdataPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Feature OData Aspect</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl#isKey <em>Key</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl#isNullable <em>Nullable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl#isComputed <em>Computed</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl#isImmutable <em>Immutable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl#getEdmType <em>Edm Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl#getMaxLength <em>Max Length</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl#getPrecision <em>Precision</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl#getScale <em>Scale</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.FeatureODataAspectImpl#getDefaultValue <em>Default Value</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FeatureODataAspectImpl extends FeatureAspectImpl implements FeatureODataAspect {
	/**
	 * The default value of the '{@link #isKey() <em>Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isKey()
	 * @generated
	 * @ordered
	 */
	protected static final boolean KEY_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isKey() <em>Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isKey()
	 * @generated
	 * @ordered
	 */
	protected boolean key = KEY_EDEFAULT;

	/**
	 * The default value of the '{@link #isNullable() <em>Nullable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNullable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NULLABLE_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isNullable() <em>Nullable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNullable()
	 * @generated
	 * @ordered
	 */
	protected boolean nullable = NULLABLE_EDEFAULT;

	/**
	 * The default value of the '{@link #isComputed() <em>Computed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isComputed()
	 * @generated
	 * @ordered
	 */
	protected static final boolean COMPUTED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isComputed() <em>Computed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isComputed()
	 * @generated
	 * @ordered
	 */
	protected boolean computed = COMPUTED_EDEFAULT;

	/**
	 * The default value of the '{@link #isImmutable() <em>Immutable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isImmutable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IMMUTABLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isImmutable() <em>Immutable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isImmutable()
	 * @generated
	 * @ordered
	 */
	protected boolean immutable = IMMUTABLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getEdmType() <em>Edm Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEdmType()
	 * @generated
	 * @ordered
	 */
	protected static final String EDM_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getEdmType() <em>Edm Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEdmType()
	 * @generated
	 * @ordered
	 */
	protected String edmType = EDM_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getMaxLength() <em>Max Length</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxLength()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_LENGTH_EDEFAULT = -1;

	/**
	 * The cached value of the '{@link #getMaxLength() <em>Max Length</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxLength()
	 * @generated
	 * @ordered
	 */
	protected int maxLength = MAX_LENGTH_EDEFAULT;

	/**
	 * The default value of the '{@link #getPrecision() <em>Precision</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrecision()
	 * @generated
	 * @ordered
	 */
	protected static final int PRECISION_EDEFAULT = -1;

	/**
	 * The cached value of the '{@link #getPrecision() <em>Precision</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrecision()
	 * @generated
	 * @ordered
	 */
	protected int precision = PRECISION_EDEFAULT;

	/**
	 * The default value of the '{@link #getScale() <em>Scale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScale()
	 * @generated
	 * @ordered
	 */
	protected static final int SCALE_EDEFAULT = -1;

	/**
	 * The cached value of the '{@link #getScale() <em>Scale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScale()
	 * @generated
	 * @ordered
	 */
	protected int scale = SCALE_EDEFAULT;

	/**
	 * The default value of the '{@link #getDefaultValue() <em>Default Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefaultValue()
	 * @generated
	 * @ordered
	 */
	protected static final String DEFAULT_VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDefaultValue() <em>Default Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDefaultValue()
	 * @generated
	 * @ordered
	 */
	protected String defaultValue = DEFAULT_VALUE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FeatureODataAspectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OdataPackage.Literals.FEATURE_ODATA_ASPECT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isKey() {
		return key;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKey(boolean newKey) {
		boolean oldKey = key;
		key = newKey;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.FEATURE_ODATA_ASPECT__KEY, oldKey, key));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isNullable() {
		return nullable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNullable(boolean newNullable) {
		boolean oldNullable = nullable;
		nullable = newNullable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.FEATURE_ODATA_ASPECT__NULLABLE, oldNullable, nullable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isComputed() {
		return computed;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setComputed(boolean newComputed) {
		boolean oldComputed = computed;
		computed = newComputed;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.FEATURE_ODATA_ASPECT__COMPUTED, oldComputed, computed));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isImmutable() {
		return immutable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setImmutable(boolean newImmutable) {
		boolean oldImmutable = immutable;
		immutable = newImmutable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.FEATURE_ODATA_ASPECT__IMMUTABLE, oldImmutable, immutable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getEdmType() {
		return edmType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEdmType(String newEdmType) {
		String oldEdmType = edmType;
		edmType = newEdmType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.FEATURE_ODATA_ASPECT__EDM_TYPE, oldEdmType, edmType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxLength(int newMaxLength) {
		int oldMaxLength = maxLength;
		maxLength = newMaxLength;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.FEATURE_ODATA_ASPECT__MAX_LENGTH, oldMaxLength, maxLength));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getPrecision() {
		return precision;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPrecision(int newPrecision) {
		int oldPrecision = precision;
		precision = newPrecision;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.FEATURE_ODATA_ASPECT__PRECISION, oldPrecision, precision));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getScale() {
		return scale;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setScale(int newScale) {
		int oldScale = scale;
		scale = newScale;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.FEATURE_ODATA_ASPECT__SCALE, oldScale, scale));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDefaultValue(String newDefaultValue) {
		String oldDefaultValue = defaultValue;
		defaultValue = newDefaultValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.FEATURE_ODATA_ASPECT__DEFAULT_VALUE, oldDefaultValue, defaultValue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case OdataPackage.FEATURE_ODATA_ASPECT__KEY:
				return isKey();
			case OdataPackage.FEATURE_ODATA_ASPECT__NULLABLE:
				return isNullable();
			case OdataPackage.FEATURE_ODATA_ASPECT__COMPUTED:
				return isComputed();
			case OdataPackage.FEATURE_ODATA_ASPECT__IMMUTABLE:
				return isImmutable();
			case OdataPackage.FEATURE_ODATA_ASPECT__EDM_TYPE:
				return getEdmType();
			case OdataPackage.FEATURE_ODATA_ASPECT__MAX_LENGTH:
				return getMaxLength();
			case OdataPackage.FEATURE_ODATA_ASPECT__PRECISION:
				return getPrecision();
			case OdataPackage.FEATURE_ODATA_ASPECT__SCALE:
				return getScale();
			case OdataPackage.FEATURE_ODATA_ASPECT__DEFAULT_VALUE:
				return getDefaultValue();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case OdataPackage.FEATURE_ODATA_ASPECT__KEY:
				setKey((Boolean)newValue);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__NULLABLE:
				setNullable((Boolean)newValue);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__COMPUTED:
				setComputed((Boolean)newValue);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__IMMUTABLE:
				setImmutable((Boolean)newValue);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__EDM_TYPE:
				setEdmType((String)newValue);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__MAX_LENGTH:
				setMaxLength((Integer)newValue);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__PRECISION:
				setPrecision((Integer)newValue);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__SCALE:
				setScale((Integer)newValue);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__DEFAULT_VALUE:
				setDefaultValue((String)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case OdataPackage.FEATURE_ODATA_ASPECT__KEY:
				setKey(KEY_EDEFAULT);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__NULLABLE:
				setNullable(NULLABLE_EDEFAULT);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__COMPUTED:
				setComputed(COMPUTED_EDEFAULT);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__IMMUTABLE:
				setImmutable(IMMUTABLE_EDEFAULT);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__EDM_TYPE:
				setEdmType(EDM_TYPE_EDEFAULT);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__MAX_LENGTH:
				setMaxLength(MAX_LENGTH_EDEFAULT);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__PRECISION:
				setPrecision(PRECISION_EDEFAULT);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__SCALE:
				setScale(SCALE_EDEFAULT);
				return;
			case OdataPackage.FEATURE_ODATA_ASPECT__DEFAULT_VALUE:
				setDefaultValue(DEFAULT_VALUE_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case OdataPackage.FEATURE_ODATA_ASPECT__KEY:
				return key != KEY_EDEFAULT;
			case OdataPackage.FEATURE_ODATA_ASPECT__NULLABLE:
				return nullable != NULLABLE_EDEFAULT;
			case OdataPackage.FEATURE_ODATA_ASPECT__COMPUTED:
				return computed != COMPUTED_EDEFAULT;
			case OdataPackage.FEATURE_ODATA_ASPECT__IMMUTABLE:
				return immutable != IMMUTABLE_EDEFAULT;
			case OdataPackage.FEATURE_ODATA_ASPECT__EDM_TYPE:
				return EDM_TYPE_EDEFAULT == null ? edmType != null : !EDM_TYPE_EDEFAULT.equals(edmType);
			case OdataPackage.FEATURE_ODATA_ASPECT__MAX_LENGTH:
				return maxLength != MAX_LENGTH_EDEFAULT;
			case OdataPackage.FEATURE_ODATA_ASPECT__PRECISION:
				return precision != PRECISION_EDEFAULT;
			case OdataPackage.FEATURE_ODATA_ASPECT__SCALE:
				return scale != SCALE_EDEFAULT;
			case OdataPackage.FEATURE_ODATA_ASPECT__DEFAULT_VALUE:
				return DEFAULT_VALUE_EDEFAULT == null ? defaultValue != null : !DEFAULT_VALUE_EDEFAULT.equals(defaultValue);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (key: ");
		result.append(key);
		result.append(", nullable: ");
		result.append(nullable);
		result.append(", computed: ");
		result.append(computed);
		result.append(", immutable: ");
		result.append(immutable);
		result.append(", edmType: ");
		result.append(edmType);
		result.append(", maxLength: ");
		result.append(maxLength);
		result.append(", precision: ");
		result.append(precision);
		result.append(", scale: ");
		result.append(scale);
		result.append(", defaultValue: ");
		result.append(defaultValue);
		result.append(')');
		return result.toString();
	}

} //FeatureODataAspectImpl
