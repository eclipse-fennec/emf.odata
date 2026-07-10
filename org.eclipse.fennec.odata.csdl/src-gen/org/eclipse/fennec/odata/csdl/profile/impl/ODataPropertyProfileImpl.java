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
package org.eclipse.fennec.odata.csdl.profile.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.odata.csdl.profile.ODataAnnotation;
import org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile;
import org.eclipse.fennec.odata.csdl.profile.ProfilePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>OData Property Profile</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#getTypeName <em>Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#isNullable <em>Nullable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#isComputed <em>Computed</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#isImmutable <em>Immutable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#getMaxLength <em>Max Length</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#getPrecision <em>Precision</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#getScale <em>Scale</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#getDefaultValue <em>Default Value</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#getSrid <em>Srid</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#getUnicode <em>Unicode</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPropertyProfileImpl#getAnnotations <em>Annotations</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ODataPropertyProfileImpl extends MinimalEObjectImpl.Container implements ODataPropertyProfile {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getTypeName() <em>Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeName()
	 * @generated
	 * @ordered
	 */
	protected static final String TYPE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTypeName() <em>Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTypeName()
	 * @generated
	 * @ordered
	 */
	protected String typeName = TYPE_NAME_EDEFAULT;

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
	 * The default value of the '{@link #getSrid() <em>Srid</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSrid()
	 * @generated
	 * @ordered
	 */
	protected static final String SRID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSrid() <em>Srid</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSrid()
	 * @generated
	 * @ordered
	 */
	protected String srid = SRID_EDEFAULT;

	/**
	 * The default value of the '{@link #getUnicode() <em>Unicode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnicode()
	 * @generated
	 * @ordered
	 */
	protected static final Boolean UNICODE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUnicode() <em>Unicode</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUnicode()
	 * @generated
	 * @ordered
	 */
	protected Boolean unicode = UNICODE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAnnotations() <em>Annotations</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAnnotations()
	 * @generated
	 * @ordered
	 */
	protected EList<ODataAnnotation> annotations;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ODataPropertyProfileImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ProfilePackage.Literals.ODATA_PROPERTY_PROFILE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTypeName() {
		return typeName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTypeName(String newTypeName) {
		String oldTypeName = typeName;
		typeName = newTypeName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__TYPE_NAME, oldTypeName, typeName));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__NULLABLE, oldNullable, nullable));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__COMPUTED, oldComputed, computed));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__IMMUTABLE, oldImmutable, immutable));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__MAX_LENGTH, oldMaxLength, maxLength));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__PRECISION, oldPrecision, precision));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__SCALE, oldScale, scale));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__DEFAULT_VALUE, oldDefaultValue, defaultValue));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSrid() {
		return srid;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSrid(String newSrid) {
		String oldSrid = srid;
		srid = newSrid;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__SRID, oldSrid, srid));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Boolean getUnicode() {
		return unicode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUnicode(Boolean newUnicode) {
		Boolean oldUnicode = unicode;
		unicode = newUnicode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PROPERTY_PROFILE__UNICODE, oldUnicode, unicode));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataAnnotation> getAnnotations() {
		if (annotations == null) {
			annotations = new EObjectContainmentEList<ODataAnnotation>(ODataAnnotation.class, this, ProfilePackage.ODATA_PROPERTY_PROFILE__ANNOTATIONS);
		}
		return annotations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ProfilePackage.ODATA_PROPERTY_PROFILE__ANNOTATIONS:
				return ((InternalEList<?>)getAnnotations()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ProfilePackage.ODATA_PROPERTY_PROFILE__NAME:
				return getName();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__TYPE_NAME:
				return getTypeName();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__NULLABLE:
				return isNullable();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__COMPUTED:
				return isComputed();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__IMMUTABLE:
				return isImmutable();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__MAX_LENGTH:
				return getMaxLength();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__PRECISION:
				return getPrecision();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__SCALE:
				return getScale();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__DEFAULT_VALUE:
				return getDefaultValue();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__SRID:
				return getSrid();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__UNICODE:
				return getUnicode();
			case ProfilePackage.ODATA_PROPERTY_PROFILE__ANNOTATIONS:
				return getAnnotations();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ProfilePackage.ODATA_PROPERTY_PROFILE__NAME:
				setName((String)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__TYPE_NAME:
				setTypeName((String)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__NULLABLE:
				setNullable((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__COMPUTED:
				setComputed((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__IMMUTABLE:
				setImmutable((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__MAX_LENGTH:
				setMaxLength((Integer)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__PRECISION:
				setPrecision((Integer)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__SCALE:
				setScale((Integer)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__DEFAULT_VALUE:
				setDefaultValue((String)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__SRID:
				setSrid((String)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__UNICODE:
				setUnicode((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__ANNOTATIONS:
				getAnnotations().clear();
				getAnnotations().addAll((Collection<? extends ODataAnnotation>)newValue);
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
			case ProfilePackage.ODATA_PROPERTY_PROFILE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__TYPE_NAME:
				setTypeName(TYPE_NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__NULLABLE:
				setNullable(NULLABLE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__COMPUTED:
				setComputed(COMPUTED_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__IMMUTABLE:
				setImmutable(IMMUTABLE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__MAX_LENGTH:
				setMaxLength(MAX_LENGTH_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__PRECISION:
				setPrecision(PRECISION_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__SCALE:
				setScale(SCALE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__DEFAULT_VALUE:
				setDefaultValue(DEFAULT_VALUE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__SRID:
				setSrid(SRID_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__UNICODE:
				setUnicode(UNICODE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__ANNOTATIONS:
				getAnnotations().clear();
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
			case ProfilePackage.ODATA_PROPERTY_PROFILE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ProfilePackage.ODATA_PROPERTY_PROFILE__TYPE_NAME:
				return TYPE_NAME_EDEFAULT == null ? typeName != null : !TYPE_NAME_EDEFAULT.equals(typeName);
			case ProfilePackage.ODATA_PROPERTY_PROFILE__NULLABLE:
				return nullable != NULLABLE_EDEFAULT;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__COMPUTED:
				return computed != COMPUTED_EDEFAULT;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__IMMUTABLE:
				return immutable != IMMUTABLE_EDEFAULT;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__MAX_LENGTH:
				return maxLength != MAX_LENGTH_EDEFAULT;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__PRECISION:
				return precision != PRECISION_EDEFAULT;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__SCALE:
				return scale != SCALE_EDEFAULT;
			case ProfilePackage.ODATA_PROPERTY_PROFILE__DEFAULT_VALUE:
				return DEFAULT_VALUE_EDEFAULT == null ? defaultValue != null : !DEFAULT_VALUE_EDEFAULT.equals(defaultValue);
			case ProfilePackage.ODATA_PROPERTY_PROFILE__SRID:
				return SRID_EDEFAULT == null ? srid != null : !SRID_EDEFAULT.equals(srid);
			case ProfilePackage.ODATA_PROPERTY_PROFILE__UNICODE:
				return UNICODE_EDEFAULT == null ? unicode != null : !UNICODE_EDEFAULT.equals(unicode);
			case ProfilePackage.ODATA_PROPERTY_PROFILE__ANNOTATIONS:
				return annotations != null && !annotations.isEmpty();
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
		result.append(" (name: ");
		result.append(name);
		result.append(", typeName: ");
		result.append(typeName);
		result.append(", nullable: ");
		result.append(nullable);
		result.append(", computed: ");
		result.append(computed);
		result.append(", immutable: ");
		result.append(immutable);
		result.append(", maxLength: ");
		result.append(maxLength);
		result.append(", precision: ");
		result.append(precision);
		result.append(", scale: ");
		result.append(scale);
		result.append(", defaultValue: ");
		result.append(defaultValue);
		result.append(", srid: ");
		result.append(srid);
		result.append(", unicode: ");
		result.append(unicode);
		result.append(')');
		return result.toString();
	}

} //ODataPropertyProfileImpl
