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
import org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataReferentialConstraint;
import org.eclipse.fennec.odata.csdl.profile.ProfilePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>OData Navigation Profile</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl#getTypeName <em>Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl#isNullable <em>Nullable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl#isContainsTarget <em>Contains Target</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl#getPartner <em>Partner</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl#getOnDelete <em>On Delete</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl#getReferentialConstraints <em>Referential Constraints</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataNavigationProfileImpl#getAnnotations <em>Annotations</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ODataNavigationProfileImpl extends MinimalEObjectImpl.Container implements ODataNavigationProfile {
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
	 * The default value of the '{@link #isContainsTarget() <em>Contains Target</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isContainsTarget()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CONTAINS_TARGET_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isContainsTarget() <em>Contains Target</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isContainsTarget()
	 * @generated
	 * @ordered
	 */
	protected boolean containsTarget = CONTAINS_TARGET_EDEFAULT;

	/**
	 * The default value of the '{@link #getPartner() <em>Partner</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPartner()
	 * @generated
	 * @ordered
	 */
	protected static final String PARTNER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPartner() <em>Partner</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPartner()
	 * @generated
	 * @ordered
	 */
	protected String partner = PARTNER_EDEFAULT;

	/**
	 * The default value of the '{@link #getOnDelete() <em>On Delete</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOnDelete()
	 * @generated
	 * @ordered
	 */
	protected static final String ON_DELETE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getOnDelete() <em>On Delete</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOnDelete()
	 * @generated
	 * @ordered
	 */
	protected String onDelete = ON_DELETE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getReferentialConstraints() <em>Referential Constraints</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReferentialConstraints()
	 * @generated
	 * @ordered
	 */
	protected EList<ODataReferentialConstraint> referentialConstraints;

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
	protected ODataNavigationProfileImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ProfilePackage.Literals.ODATA_NAVIGATION_PROFILE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_NAVIGATION_PROFILE__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_NAVIGATION_PROFILE__TYPE_NAME, oldTypeName, typeName));
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_NAVIGATION_PROFILE__NULLABLE, oldNullable, nullable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isContainsTarget() {
		return containsTarget;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContainsTarget(boolean newContainsTarget) {
		boolean oldContainsTarget = containsTarget;
		containsTarget = newContainsTarget;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_NAVIGATION_PROFILE__CONTAINS_TARGET, oldContainsTarget, containsTarget));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getPartner() {
		return partner;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPartner(String newPartner) {
		String oldPartner = partner;
		partner = newPartner;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_NAVIGATION_PROFILE__PARTNER, oldPartner, partner));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getOnDelete() {
		return onDelete;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOnDelete(String newOnDelete) {
		String oldOnDelete = onDelete;
		onDelete = newOnDelete;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_NAVIGATION_PROFILE__ON_DELETE, oldOnDelete, onDelete));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataReferentialConstraint> getReferentialConstraints() {
		if (referentialConstraints == null) {
			referentialConstraints = new EObjectContainmentEList<ODataReferentialConstraint>(ODataReferentialConstraint.class, this, ProfilePackage.ODATA_NAVIGATION_PROFILE__REFERENTIAL_CONSTRAINTS);
		}
		return referentialConstraints;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataAnnotation> getAnnotations() {
		if (annotations == null) {
			annotations = new EObjectContainmentEList<ODataAnnotation>(ODataAnnotation.class, this, ProfilePackage.ODATA_NAVIGATION_PROFILE__ANNOTATIONS);
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
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__REFERENTIAL_CONSTRAINTS:
				return ((InternalEList<?>)getReferentialConstraints()).basicRemove(otherEnd, msgs);
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__NAME:
				return getName();
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__TYPE_NAME:
				return getTypeName();
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__NULLABLE:
				return isNullable();
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__CONTAINS_TARGET:
				return isContainsTarget();
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__PARTNER:
				return getPartner();
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__ON_DELETE:
				return getOnDelete();
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__REFERENTIAL_CONSTRAINTS:
				return getReferentialConstraints();
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__NAME:
				setName((String)newValue);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__TYPE_NAME:
				setTypeName((String)newValue);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__NULLABLE:
				setNullable((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__CONTAINS_TARGET:
				setContainsTarget((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__PARTNER:
				setPartner((String)newValue);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__ON_DELETE:
				setOnDelete((String)newValue);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__REFERENTIAL_CONSTRAINTS:
				getReferentialConstraints().clear();
				getReferentialConstraints().addAll((Collection<? extends ODataReferentialConstraint>)newValue);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__TYPE_NAME:
				setTypeName(TYPE_NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__NULLABLE:
				setNullable(NULLABLE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__CONTAINS_TARGET:
				setContainsTarget(CONTAINS_TARGET_EDEFAULT);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__PARTNER:
				setPartner(PARTNER_EDEFAULT);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__ON_DELETE:
				setOnDelete(ON_DELETE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__REFERENTIAL_CONSTRAINTS:
				getReferentialConstraints().clear();
				return;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__TYPE_NAME:
				return TYPE_NAME_EDEFAULT == null ? typeName != null : !TYPE_NAME_EDEFAULT.equals(typeName);
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__NULLABLE:
				return nullable != NULLABLE_EDEFAULT;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__CONTAINS_TARGET:
				return containsTarget != CONTAINS_TARGET_EDEFAULT;
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__PARTNER:
				return PARTNER_EDEFAULT == null ? partner != null : !PARTNER_EDEFAULT.equals(partner);
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__ON_DELETE:
				return ON_DELETE_EDEFAULT == null ? onDelete != null : !ON_DELETE_EDEFAULT.equals(onDelete);
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__REFERENTIAL_CONSTRAINTS:
				return referentialConstraints != null && !referentialConstraints.isEmpty();
			case ProfilePackage.ODATA_NAVIGATION_PROFILE__ANNOTATIONS:
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
		result.append(", containsTarget: ");
		result.append(containsTarget);
		result.append(", partner: ");
		result.append(partner);
		result.append(", onDelete: ");
		result.append(onDelete);
		result.append(')');
		return result.toString();
	}

} //ODataNavigationProfileImpl
