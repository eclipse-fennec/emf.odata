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
import org.eclipse.fennec.odata.csdl.profile.ODataClassProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataEnumProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.csdl.profile.ProfilePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>OData Package Profile</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPackageProfileImpl#getNamespace <em>Namespace</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPackageProfileImpl#getAlias <em>Alias</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPackageProfileImpl#getContainerName <em>Container Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPackageProfileImpl#getEnums <em>Enums</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPackageProfileImpl#getClasses <em>Classes</em>}</li>
 *   <li>{@link org.eclipse.fennec.odata.csdl.profile.impl.ODataPackageProfileImpl#getAnnotations <em>Annotations</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ODataPackageProfileImpl extends MinimalEObjectImpl.Container implements ODataPackageProfile {
	/**
	 * The default value of the '{@link #getNamespace() <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespace()
	 * @generated
	 * @ordered
	 */
	protected static final String NAMESPACE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNamespace() <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespace()
	 * @generated
	 * @ordered
	 */
	protected String namespace = NAMESPACE_EDEFAULT;

	/**
	 * The default value of the '{@link #getAlias() <em>Alias</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAlias()
	 * @generated
	 * @ordered
	 */
	protected static final String ALIAS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getAlias() <em>Alias</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAlias()
	 * @generated
	 * @ordered
	 */
	protected String alias = ALIAS_EDEFAULT;

	/**
	 * The default value of the '{@link #getContainerName() <em>Container Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContainerName()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTAINER_NAME_EDEFAULT = "DefaultContainer";

	/**
	 * The cached value of the '{@link #getContainerName() <em>Container Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContainerName()
	 * @generated
	 * @ordered
	 */
	protected String containerName = CONTAINER_NAME_EDEFAULT;

	/**
	 * The cached value of the '{@link #getEnums() <em>Enums</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEnums()
	 * @generated
	 * @ordered
	 */
	protected EList<ODataEnumProfile> enums;

	/**
	 * The cached value of the '{@link #getClasses() <em>Classes</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClasses()
	 * @generated
	 * @ordered
	 */
	protected EList<ODataClassProfile> classes;

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
	protected ODataPackageProfileImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ProfilePackage.Literals.ODATA_PACKAGE_PROFILE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNamespace() {
		return namespace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNamespace(String newNamespace) {
		String oldNamespace = namespace;
		namespace = newNamespace;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PACKAGE_PROFILE__NAMESPACE, oldNamespace, namespace));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getAlias() {
		return alias;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAlias(String newAlias) {
		String oldAlias = alias;
		alias = newAlias;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PACKAGE_PROFILE__ALIAS, oldAlias, alias));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getContainerName() {
		return containerName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContainerName(String newContainerName) {
		String oldContainerName = containerName;
		containerName = newContainerName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ProfilePackage.ODATA_PACKAGE_PROFILE__CONTAINER_NAME, oldContainerName, containerName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataEnumProfile> getEnums() {
		if (enums == null) {
			enums = new EObjectContainmentEList<ODataEnumProfile>(ODataEnumProfile.class, this, ProfilePackage.ODATA_PACKAGE_PROFILE__ENUMS);
		}
		return enums;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataClassProfile> getClasses() {
		if (classes == null) {
			classes = new EObjectContainmentEList<ODataClassProfile>(ODataClassProfile.class, this, ProfilePackage.ODATA_PACKAGE_PROFILE__CLASSES);
		}
		return classes;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ODataAnnotation> getAnnotations() {
		if (annotations == null) {
			annotations = new EObjectContainmentEList<ODataAnnotation>(ODataAnnotation.class, this, ProfilePackage.ODATA_PACKAGE_PROFILE__ANNOTATIONS);
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
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ENUMS:
				return ((InternalEList<?>)getEnums()).basicRemove(otherEnd, msgs);
			case ProfilePackage.ODATA_PACKAGE_PROFILE__CLASSES:
				return ((InternalEList<?>)getClasses()).basicRemove(otherEnd, msgs);
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_PACKAGE_PROFILE__NAMESPACE:
				return getNamespace();
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ALIAS:
				return getAlias();
			case ProfilePackage.ODATA_PACKAGE_PROFILE__CONTAINER_NAME:
				return getContainerName();
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ENUMS:
				return getEnums();
			case ProfilePackage.ODATA_PACKAGE_PROFILE__CLASSES:
				return getClasses();
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_PACKAGE_PROFILE__NAMESPACE:
				setNamespace((String)newValue);
				return;
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ALIAS:
				setAlias((String)newValue);
				return;
			case ProfilePackage.ODATA_PACKAGE_PROFILE__CONTAINER_NAME:
				setContainerName((String)newValue);
				return;
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ENUMS:
				getEnums().clear();
				getEnums().addAll((Collection<? extends ODataEnumProfile>)newValue);
				return;
			case ProfilePackage.ODATA_PACKAGE_PROFILE__CLASSES:
				getClasses().clear();
				getClasses().addAll((Collection<? extends ODataClassProfile>)newValue);
				return;
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_PACKAGE_PROFILE__NAMESPACE:
				setNamespace(NAMESPACE_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ALIAS:
				setAlias(ALIAS_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PACKAGE_PROFILE__CONTAINER_NAME:
				setContainerName(CONTAINER_NAME_EDEFAULT);
				return;
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ENUMS:
				getEnums().clear();
				return;
			case ProfilePackage.ODATA_PACKAGE_PROFILE__CLASSES:
				getClasses().clear();
				return;
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ANNOTATIONS:
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
			case ProfilePackage.ODATA_PACKAGE_PROFILE__NAMESPACE:
				return NAMESPACE_EDEFAULT == null ? namespace != null : !NAMESPACE_EDEFAULT.equals(namespace);
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ALIAS:
				return ALIAS_EDEFAULT == null ? alias != null : !ALIAS_EDEFAULT.equals(alias);
			case ProfilePackage.ODATA_PACKAGE_PROFILE__CONTAINER_NAME:
				return CONTAINER_NAME_EDEFAULT == null ? containerName != null : !CONTAINER_NAME_EDEFAULT.equals(containerName);
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ENUMS:
				return enums != null && !enums.isEmpty();
			case ProfilePackage.ODATA_PACKAGE_PROFILE__CLASSES:
				return classes != null && !classes.isEmpty();
			case ProfilePackage.ODATA_PACKAGE_PROFILE__ANNOTATIONS:
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
		result.append(" (namespace: ");
		result.append(namespace);
		result.append(", alias: ");
		result.append(alias);
		result.append(", containerName: ");
		result.append(containerName);
		result.append(')');
		return result.toString();
	}

} //ODataPackageProfileImpl
