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

import org.eclipse.fennec.odata.csdl.profile.ODataOperationKind;
import org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataParameterProfile;
import org.eclipse.fennec.odata.csdl.profile.ProfilePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>OData Operation Profile</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl#getKind <em>Kind</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl#isBound <em>Bound</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl#isComposable <em>Composable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl#getBindingTypeName <em>Binding Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl#getReturnTypeName <em>Return Type Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl#isReturnNullable <em>Return Nullable</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl#getEntitySetPath <em>Entity Set Path</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataOperationProfileImpl#getParameters <em>Parameters</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ODataOperationProfileImpl extends MinimalEObjectImpl.Container implements ODataOperationProfile {
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
	 * The default value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected static final ODataOperationKind KIND_EDEFAULT = ODataOperationKind.FUNCTION;

	/**
	 * The cached value of the '{@link #getKind() <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getKind()
	 * @generated
	 * @ordered
	 */
	protected ODataOperationKind kind = KIND_EDEFAULT;

	/**
	 * The default value of the '{@link #isBound() <em>Bound</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isBound()
	 * @generated
	 * @ordered
	 */
	protected static final boolean BOUND_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isBound() <em>Bound</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isBound()
	 * @generated
	 * @ordered
	 */
	protected boolean bound = BOUND_EDEFAULT;

	/**
	 * The default value of the '{@link #isComposable() <em>Composable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isComposable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean COMPOSABLE_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isComposable() <em>Composable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isComposable()
	 * @generated
	 * @ordered
	 */
	protected boolean composable = COMPOSABLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getBindingTypeName() <em>Binding Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindingTypeName()
	 * @generated
	 * @ordered
	 */
	protected static final String BINDING_TYPE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getBindingTypeName() <em>Binding Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBindingTypeName()
	 * @generated
	 * @ordered
	 */
	protected String bindingTypeName = BINDING_TYPE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getReturnTypeName() <em>Return Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReturnTypeName()
	 * @generated
	 * @ordered
	 */
	protected static final String RETURN_TYPE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getReturnTypeName() <em>Return Type Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReturnTypeName()
	 * @generated
	 * @ordered
	 */
	protected String returnTypeName = RETURN_TYPE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #isReturnNullable() <em>Return Nullable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isReturnNullable()
	 * @generated
	 * @ordered
	 */
	protected static final boolean RETURN_NULLABLE_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isReturnNullable() <em>Return Nullable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isReturnNullable()
	 * @generated
	 * @ordered
	 */
	protected boolean returnNullable = RETURN_NULLABLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getEntitySetPath() <em>Entity Set Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEntitySetPath()
	 * @generated
	 * @ordered
	 */
	protected static final String ENTITY_SET_PATH_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getEntitySetPath() <em>Entity Set Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEntitySetPath()
	 * @generated
	 * @ordered
	 */
	protected String entitySetPath = ENTITY_SET_PATH_EDEFAULT;

	/**
	 * The cached value of the '{@link #getParameters() <em>Parameters</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParameters()
	 * @generated
	 * @ordered
	 */
	protected EList<ODataParameterProfile> parameters;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ODataOperationProfileImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ProfilePackage.Literals.ODATA_OPERATION_PROFILE;
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
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_OPERATION_PROFILE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ODataOperationKind getKind() {
		return kind;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setKind(ODataOperationKind newKind) {
		ODataOperationKind oldKind = kind;
		kind = newKind == null ? KIND_EDEFAULT : newKind;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_OPERATION_PROFILE__KIND, oldKind, kind));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isBound() {
		return bound;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBound(boolean newBound) {
		boolean oldBound = bound;
		bound = newBound;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_OPERATION_PROFILE__BOUND, oldBound, bound));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isComposable() {
		return composable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setComposable(boolean newComposable) {
		boolean oldComposable = composable;
		composable = newComposable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_OPERATION_PROFILE__COMPOSABLE, oldComposable, composable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getBindingTypeName() {
		return bindingTypeName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setBindingTypeName(String newBindingTypeName) {
		String oldBindingTypeName = bindingTypeName;
		bindingTypeName = newBindingTypeName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_OPERATION_PROFILE__BINDING_TYPE_NAME, oldBindingTypeName, bindingTypeName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getReturnTypeName() {
		return returnTypeName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReturnTypeName(String newReturnTypeName) {
		String oldReturnTypeName = returnTypeName;
		returnTypeName = newReturnTypeName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_OPERATION_PROFILE__RETURN_TYPE_NAME, oldReturnTypeName, returnTypeName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isReturnNullable() {
		return returnNullable;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setReturnNullable(boolean newReturnNullable) {
		boolean oldReturnNullable = returnNullable;
		returnNullable = newReturnNullable;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_OPERATION_PROFILE__RETURN_NULLABLE, oldReturnNullable, returnNullable));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getEntitySetPath() {
		return entitySetPath;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEntitySetPath(String newEntitySetPath) {
		String oldEntitySetPath = entitySetPath;
		entitySetPath = newEntitySetPath;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_OPERATION_PROFILE__ENTITY_SET_PATH, oldEntitySetPath, entitySetPath));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataParameterProfile> getParameters() {
		if (parameters == null) {
			parameters = new EObjectContainmentEList<ODataParameterProfile>(ODataParameterProfile.class, this, ProfilePackage.ODATA_OPERATION_PROFILE__PARAMETERS);
		}
		return parameters;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ProfilePackage.ODATA_OPERATION_PROFILE__PARAMETERS:
				return ((InternalEList<?>)getParameters()).basicRemove(otherEnd, msgs);
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
			case ProfilePackage.ODATA_OPERATION_PROFILE__NAME:
				return getName();
			case ProfilePackage.ODATA_OPERATION_PROFILE__KIND:
				return getKind();
			case ProfilePackage.ODATA_OPERATION_PROFILE__BOUND:
				return isBound();
			case ProfilePackage.ODATA_OPERATION_PROFILE__COMPOSABLE:
				return isComposable();
			case ProfilePackage.ODATA_OPERATION_PROFILE__BINDING_TYPE_NAME:
				return getBindingTypeName();
			case ProfilePackage.ODATA_OPERATION_PROFILE__RETURN_TYPE_NAME:
				return getReturnTypeName();
			case ProfilePackage.ODATA_OPERATION_PROFILE__RETURN_NULLABLE:
				return isReturnNullable();
			case ProfilePackage.ODATA_OPERATION_PROFILE__ENTITY_SET_PATH:
				return getEntitySetPath();
			case ProfilePackage.ODATA_OPERATION_PROFILE__PARAMETERS:
				return getParameters();
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
			case ProfilePackage.ODATA_OPERATION_PROFILE__NAME:
				setName((String)newValue);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__KIND:
				setKind((ODataOperationKind)newValue);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__BOUND:
				setBound((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__COMPOSABLE:
				setComposable((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__BINDING_TYPE_NAME:
				setBindingTypeName((String)newValue);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__RETURN_TYPE_NAME:
				setReturnTypeName((String)newValue);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__RETURN_NULLABLE:
				setReturnNullable((Boolean)newValue);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__ENTITY_SET_PATH:
				setEntitySetPath((String)newValue);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__PARAMETERS:
				getParameters().clear();
				getParameters().addAll((Collection<? extends ODataParameterProfile>)newValue);
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
			case ProfilePackage.ODATA_OPERATION_PROFILE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__KIND:
				setKind(KIND_EDEFAULT);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__BOUND:
				setBound(BOUND_EDEFAULT);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__COMPOSABLE:
				setComposable(COMPOSABLE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__BINDING_TYPE_NAME:
				setBindingTypeName(BINDING_TYPE_NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__RETURN_TYPE_NAME:
				setReturnTypeName(RETURN_TYPE_NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__RETURN_NULLABLE:
				setReturnNullable(RETURN_NULLABLE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__ENTITY_SET_PATH:
				setEntitySetPath(ENTITY_SET_PATH_EDEFAULT);
				return;
			case ProfilePackage.ODATA_OPERATION_PROFILE__PARAMETERS:
				getParameters().clear();
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
			case ProfilePackage.ODATA_OPERATION_PROFILE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ProfilePackage.ODATA_OPERATION_PROFILE__KIND:
				return kind != KIND_EDEFAULT;
			case ProfilePackage.ODATA_OPERATION_PROFILE__BOUND:
				return bound != BOUND_EDEFAULT;
			case ProfilePackage.ODATA_OPERATION_PROFILE__COMPOSABLE:
				return composable != COMPOSABLE_EDEFAULT;
			case ProfilePackage.ODATA_OPERATION_PROFILE__BINDING_TYPE_NAME:
				return BINDING_TYPE_NAME_EDEFAULT == null ? bindingTypeName != null : !BINDING_TYPE_NAME_EDEFAULT.equals(bindingTypeName);
			case ProfilePackage.ODATA_OPERATION_PROFILE__RETURN_TYPE_NAME:
				return RETURN_TYPE_NAME_EDEFAULT == null ? returnTypeName != null : !RETURN_TYPE_NAME_EDEFAULT.equals(returnTypeName);
			case ProfilePackage.ODATA_OPERATION_PROFILE__RETURN_NULLABLE:
				return returnNullable != RETURN_NULLABLE_EDEFAULT;
			case ProfilePackage.ODATA_OPERATION_PROFILE__ENTITY_SET_PATH:
				return ENTITY_SET_PATH_EDEFAULT == null ? entitySetPath != null : !ENTITY_SET_PATH_EDEFAULT.equals(entitySetPath);
			case ProfilePackage.ODATA_OPERATION_PROFILE__PARAMETERS:
				return parameters != null && !parameters.isEmpty();
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
		result.append(", kind: ");
		result.append(kind);
		result.append(", bound: ");
		result.append(bound);
		result.append(", composable: ");
		result.append(composable);
		result.append(", bindingTypeName: ");
		result.append(bindingTypeName);
		result.append(", returnTypeName: ");
		result.append(returnTypeName);
		result.append(", returnNullable: ");
		result.append(returnNullable);
		result.append(", entitySetPath: ");
		result.append(entitySetPath);
		result.append(')');
		return result.toString();
	}

} //ODataOperationProfileImpl
