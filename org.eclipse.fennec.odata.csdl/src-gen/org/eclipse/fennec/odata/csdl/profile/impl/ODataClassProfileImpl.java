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

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.odata.csdl.profile.ODataAnnotation;
import org.eclipse.fennec.odata.csdl.profile.ODataClassProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataTypeKind;
import org.eclipse.fennec.odata.csdl.profile.ProfilePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>OData Class Profile</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#getQualifiedName <em>Qualified Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#isAbstract <em>Abstract</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#isOpenType <em>Open Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#isHasStream <em>Has Stream</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#getBaseTypeQualifiedName <em>Base Type Qualified Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#getKeyPropertyNames <em>Key Property Names</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#getProperties <em>Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#getNavigationProperties <em>Navigation Properties</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#getOperations <em>Operations</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataClassProfileImpl#getAnnotations <em>Annotations</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ODataClassProfileImpl extends MinimalEObjectImpl.Container implements ODataClassProfile {
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
	 * The default value of the '{@link #getQualifiedName() <em>Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQualifiedName()
	 * @generated
	 * @ordered
	 */
	protected static final String QUALIFIED_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getQualifiedName() <em>Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getQualifiedName()
	 * @generated
	 * @ordered
	 */
	protected String qualifiedName = QUALIFIED_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected static final ODataTypeKind KIND_EDEFAULT = ODataTypeKind.ENTITY;

	/**
	 * The cached value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected ODataTypeKind kind = KIND_EDEFAULT;

	/**
	 * The default value of the '{@link #isAbstract() <em>Abstract</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAbstract()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ABSTRACT_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isAbstract() <em>Abstract</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isAbstract()
	 * @generated
	 * @ordered
	 */
	protected boolean abstract_ = ABSTRACT_EDEFAULT;

	/**
	 * The default value of the '{@link #isOpenType() <em>Open Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOpenType()
	 * @generated
	 * @ordered
	 */
	protected static final boolean OPEN_TYPE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isOpenType() <em>Open Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOpenType()
	 * @generated
	 * @ordered
	 */
	protected boolean openType = OPEN_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #isHasStream() <em>Has Stream</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHasStream()
	 * @generated
	 * @ordered
	 */
	protected static final boolean HAS_STREAM_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isHasStream() <em>Has Stream</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isHasStream()
	 * @generated
	 * @ordered
	 */
	protected boolean hasStream = HAS_STREAM_EDEFAULT;

	/**
	 * The default value of the '{@link #getBaseTypeQualifiedName() <em>Base Type Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseTypeQualifiedName()
	 * @generated
	 * @ordered
	 */
	protected static final String BASE_TYPE_QUALIFIED_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBaseTypeQualifiedName() <em>Base Type Qualified Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBaseTypeQualifiedName()
	 * @generated
	 * @ordered
	 */
	protected String baseTypeQualifiedName = BASE_TYPE_QUALIFIED_NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getKeyPropertyNames() <em>Key Property Names</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKeyPropertyNames()
	 * @generated
	 * @ordered
	 */
	protected EList<String> keyPropertyNames;

	/**
	 * The cached value of the '{@link #getProperties() <em>Properties</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProperties()
	 * @generated
	 * @ordered
	 */
	protected EList<ODataPropertyProfile> properties;

	/**
	 * The cached value of the '{@link #getNavigationProperties() <em>Navigation Properties</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNavigationProperties()
	 * @generated
	 * @ordered
	 */
	protected EList<ODataNavigationProfile> navigationProperties;

	/**
	 * The cached value of the '{@link #getOperations() <em>Operations</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOperations()
	 * @generated
	 * @ordered
	 */
	protected EList<ODataOperationProfile> operations;

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
	protected ODataClassProfileImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ProfilePackage.Literals.ODATA_CLASS_PROFILE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_CLASS_PROFILE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getQualifiedName() {
		return qualifiedName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setQualifiedName(String newQualifiedName) {
		String oldQualifiedName = qualifiedName;
		qualifiedName = newQualifiedName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_CLASS_PROFILE__QUALIFIED_NAME, oldQualifiedName, qualifiedName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataTypeKind getKind() {
		return kind;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKind(ODataTypeKind newKind) {
		ODataTypeKind oldKind = kind;
		kind = newKind == null ? KIND_EDEFAULT : newKind;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_CLASS_PROFILE__KIND, oldKind, kind));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isAbstract() {
		return abstract_;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAbstract(boolean newAbstract) {
		boolean oldAbstract = abstract_;
		abstract_ = newAbstract;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_CLASS_PROFILE__ABSTRACT, oldAbstract, abstract_));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isOpenType() {
		return openType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOpenType(boolean newOpenType) {
		boolean oldOpenType = openType;
		openType = newOpenType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_CLASS_PROFILE__OPEN_TYPE, oldOpenType, openType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isHasStream() {
		return hasStream;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setHasStream(boolean newHasStream) {
		boolean oldHasStream = hasStream;
		hasStream = newHasStream;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_CLASS_PROFILE__HAS_STREAM, oldHasStream, hasStream));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getBaseTypeQualifiedName() {
		return baseTypeQualifiedName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBaseTypeQualifiedName(String newBaseTypeQualifiedName) {
		String oldBaseTypeQualifiedName = baseTypeQualifiedName;
		baseTypeQualifiedName = newBaseTypeQualifiedName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME, oldBaseTypeQualifiedName, baseTypeQualifiedName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getKeyPropertyNames() {
		if (keyPropertyNames == null) {
			keyPropertyNames = new EDataTypeUniqueEList<String>(String.class, this, ProfilePackage.ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES);
		}
		return keyPropertyNames;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataPropertyProfile> getProperties() {
		if (properties == null) {
			properties = new EObjectContainmentEList<ODataPropertyProfile>(ODataPropertyProfile.class, this, ProfilePackage.ODATA_CLASS_PROFILE__PROPERTIES);
		}
		return properties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataNavigationProfile> getNavigationProperties() {
		if (navigationProperties == null) {
			navigationProperties = new EObjectContainmentEList<ODataNavigationProfile>(ODataNavigationProfile.class, this, ProfilePackage.ODATA_CLASS_PROFILE__NAVIGATION_PROPERTIES);
		}
		return navigationProperties;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataOperationProfile> getOperations() {
		if (operations == null) {
			operations = new EObjectContainmentEList<ODataOperationProfile>(ODataOperationProfile.class, this, ProfilePackage.ODATA_CLASS_PROFILE__OPERATIONS);
		}
		return operations;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataAnnotation> getAnnotations() {
		if (annotations == null) {
			annotations = new EObjectContainmentEList<ODataAnnotation>(ODataAnnotation.class, this, ProfilePackage.ODATA_CLASS_PROFILE__ANNOTATIONS);
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
			case ProfilePackage.ODATA_CLASS_PROFILE__PROPERTIES:
				return ((InternalEList<?>)getProperties()).basicRemove(otherEnd, msgs);
			case ProfilePackage.ODATA_CLASS_PROFILE__NAVIGATION_PROPERTIES:
				return ((InternalEList<?>)getNavigationProperties()).basicRemove(otherEnd, msgs);
			case ProfilePackage.ODATA_CLASS_PROFILE__OPERATIONS:
				return ((InternalEList<?>)getOperations()).basicRemove(otherEnd, msgs);
			case ProfilePackage.ODATA_CLASS_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_CLASS_PROFILE__NAME:
				return getName();
			case ProfilePackage.ODATA_CLASS_PROFILE__QUALIFIED_NAME:
				return getQualifiedName();
			case ProfilePackage.ODATA_CLASS_PROFILE__KIND:
				return getKind();
			case ProfilePackage.ODATA_CLASS_PROFILE__ABSTRACT:
				return isAbstract();
			case ProfilePackage.ODATA_CLASS_PROFILE__OPEN_TYPE:
				return isOpenType();
			case ProfilePackage.ODATA_CLASS_PROFILE__HAS_STREAM:
				return isHasStream();
			case ProfilePackage.ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME:
				return getBaseTypeQualifiedName();
			case ProfilePackage.ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES:
				return getKeyPropertyNames();
			case ProfilePackage.ODATA_CLASS_PROFILE__PROPERTIES:
				return getProperties();
			case ProfilePackage.ODATA_CLASS_PROFILE__NAVIGATION_PROPERTIES:
				return getNavigationProperties();
			case ProfilePackage.ODATA_CLASS_PROFILE__OPERATIONS:
				return getOperations();
			case ProfilePackage.ODATA_CLASS_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_CLASS_PROFILE__NAME:
				setName((String)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__QUALIFIED_NAME:
				setQualifiedName((String)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__KIND:
				setKind((ODataTypeKind)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__ABSTRACT:
				setAbstract((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__OPEN_TYPE:
				setOpenType((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__HAS_STREAM:
				setHasStream((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME:
				setBaseTypeQualifiedName((String)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES:
				getKeyPropertyNames().clear();
				getKeyPropertyNames().addAll((Collection<? extends String>)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__PROPERTIES:
				getProperties().clear();
				getProperties().addAll((Collection<? extends ODataPropertyProfile>)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__NAVIGATION_PROPERTIES:
				getNavigationProperties().clear();
				getNavigationProperties().addAll((Collection<? extends ODataNavigationProfile>)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__OPERATIONS:
				getOperations().clear();
				getOperations().addAll((Collection<? extends ODataOperationProfile>)newValue);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_CLASS_PROFILE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__QUALIFIED_NAME:
				setQualifiedName(QUALIFIED_NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__KIND:
				setKind(KIND_EDEFAULT);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__ABSTRACT:
				setAbstract(ABSTRACT_EDEFAULT);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__OPEN_TYPE:
				setOpenType(OPEN_TYPE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__HAS_STREAM:
				setHasStream(HAS_STREAM_EDEFAULT);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME:
				setBaseTypeQualifiedName(BASE_TYPE_QUALIFIED_NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES:
				getKeyPropertyNames().clear();
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__PROPERTIES:
				getProperties().clear();
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__NAVIGATION_PROPERTIES:
				getNavigationProperties().clear();
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__OPERATIONS:
				getOperations().clear();
				return;
			case ProfilePackage.ODATA_CLASS_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_CLASS_PROFILE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ProfilePackage.ODATA_CLASS_PROFILE__QUALIFIED_NAME:
				return QUALIFIED_NAME_EDEFAULT == null ? qualifiedName != null : !QUALIFIED_NAME_EDEFAULT.equals(qualifiedName);
			case ProfilePackage.ODATA_CLASS_PROFILE__KIND:
				return kind != KIND_EDEFAULT;
			case ProfilePackage.ODATA_CLASS_PROFILE__ABSTRACT:
				return abstract_ != ABSTRACT_EDEFAULT;
			case ProfilePackage.ODATA_CLASS_PROFILE__OPEN_TYPE:
				return openType != OPEN_TYPE_EDEFAULT;
			case ProfilePackage.ODATA_CLASS_PROFILE__HAS_STREAM:
				return hasStream != HAS_STREAM_EDEFAULT;
			case ProfilePackage.ODATA_CLASS_PROFILE__BASE_TYPE_QUALIFIED_NAME:
				return BASE_TYPE_QUALIFIED_NAME_EDEFAULT == null ? baseTypeQualifiedName != null : !BASE_TYPE_QUALIFIED_NAME_EDEFAULT.equals(baseTypeQualifiedName);
			case ProfilePackage.ODATA_CLASS_PROFILE__KEY_PROPERTY_NAMES:
				return keyPropertyNames != null && !keyPropertyNames.isEmpty();
			case ProfilePackage.ODATA_CLASS_PROFILE__PROPERTIES:
				return properties != null && !properties.isEmpty();
			case ProfilePackage.ODATA_CLASS_PROFILE__NAVIGATION_PROPERTIES:
				return navigationProperties != null && !navigationProperties.isEmpty();
			case ProfilePackage.ODATA_CLASS_PROFILE__OPERATIONS:
				return operations != null && !operations.isEmpty();
			case ProfilePackage.ODATA_CLASS_PROFILE__ANNOTATIONS:
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
		result.append(", qualifiedName: ");
		result.append(qualifiedName);
		result.append(", kind: ");
		result.append(kind);
		result.append(", abstract: ");
		result.append(abstract_);
		result.append(", openType: ");
		result.append(openType);
		result.append(", hasStream: ");
		result.append(hasStream);
		result.append(", baseTypeQualifiedName: ");
		result.append(baseTypeQualifiedName);
		result.append(", keyPropertyNames: ");
		result.append(keyPropertyNames);
		result.append(')');
		return result.toString();
	}

} //ODataClassProfileImpl
