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

import org.eclipse.fennec.odata.metadata.odata.OdataPackage;
import org.eclipse.fennec.odata.metadata.odata.ReferenceODataAspect;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reference OData Aspect</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ReferenceODataAspectImpl#isContainsTarget <em>Contains Target</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ReferenceODataAspectImpl#getPartner <em>Partner</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.metadata.odata.impl.ReferenceODataAspectImpl#getOnDelete <em>On Delete</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ReferenceODataAspectImpl extends FeatureODataAspectImpl implements ReferenceODataAspect {
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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReferenceODataAspectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return OdataPackage.Literals.REFERENCE_ODATA_ASPECT;
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
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.REFERENCE_ODATA_ASPECT__CONTAINS_TARGET, oldContainsTarget, containsTarget));
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
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.REFERENCE_ODATA_ASPECT__PARTNER, oldPartner, partner));
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
			eNotify(new ENotificationImpl(this, Notification.SET, OdataPackage.REFERENCE_ODATA_ASPECT__ON_DELETE, oldOnDelete, onDelete));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case OdataPackage.REFERENCE_ODATA_ASPECT__CONTAINS_TARGET:
				return isContainsTarget();
			case OdataPackage.REFERENCE_ODATA_ASPECT__PARTNER:
				return getPartner();
			case OdataPackage.REFERENCE_ODATA_ASPECT__ON_DELETE:
				return getOnDelete();
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
			case OdataPackage.REFERENCE_ODATA_ASPECT__CONTAINS_TARGET:
				setContainsTarget((Boolean)newValue);
				return;
			case OdataPackage.REFERENCE_ODATA_ASPECT__PARTNER:
				setPartner((String)newValue);
				return;
			case OdataPackage.REFERENCE_ODATA_ASPECT__ON_DELETE:
				setOnDelete((String)newValue);
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
			case OdataPackage.REFERENCE_ODATA_ASPECT__CONTAINS_TARGET:
				setContainsTarget(CONTAINS_TARGET_EDEFAULT);
				return;
			case OdataPackage.REFERENCE_ODATA_ASPECT__PARTNER:
				setPartner(PARTNER_EDEFAULT);
				return;
			case OdataPackage.REFERENCE_ODATA_ASPECT__ON_DELETE:
				setOnDelete(ON_DELETE_EDEFAULT);
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
			case OdataPackage.REFERENCE_ODATA_ASPECT__CONTAINS_TARGET:
				return containsTarget != CONTAINS_TARGET_EDEFAULT;
			case OdataPackage.REFERENCE_ODATA_ASPECT__PARTNER:
				return PARTNER_EDEFAULT == null ? partner != null : !PARTNER_EDEFAULT.equals(partner);
			case OdataPackage.REFERENCE_ODATA_ASPECT__ON_DELETE:
				return ON_DELETE_EDEFAULT == null ? onDelete != null : !ON_DELETE_EDEFAULT.equals(onDelete);
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
		result.append(" (containsTarget: ");
		result.append(containsTarget);
		result.append(", partner: ");
		result.append(partner);
		result.append(", onDelete: ");
		result.append(onDelete);
		result.append(')');
		return result.toString();
	}

} //ReferenceODataAspectImpl
