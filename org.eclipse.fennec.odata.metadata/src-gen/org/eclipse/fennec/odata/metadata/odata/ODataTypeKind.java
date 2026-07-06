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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>OData Type Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * The OData structural-type kind a Fennec EClassifier maps to. ENTITY = has a key (EntityType), COMPLEX = no key (ComplexType), ENUM = EEnum (EnumType).
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.odata.metadata.odata.OdataPackage#getODataTypeKind()
 * @model
 * @generated
 */
@ProviderType
public enum ODataTypeKind implements Enumerator {
	/**
	 * The '<em><b>ENTITY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ENTITY_VALUE
	 * @generated
	 * @ordered
	 */
	ENTITY(0, "ENTITY", "ENTITY"),

	/**
	 * The '<em><b>COMPLEX</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #COMPLEX_VALUE
	 * @generated
	 * @ordered
	 */
	COMPLEX(1, "COMPLEX", "COMPLEX"),

	/**
	 * The '<em><b>ENUM</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ENUM_VALUE
	 * @generated
	 * @ordered
	 */
	ENUM(2, "ENUM", "ENUM");

	/**
	 * The '<em><b>ENTITY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ENTITY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ENTITY_VALUE = 0;

	/**
	 * The '<em><b>COMPLEX</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #COMPLEX
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int COMPLEX_VALUE = 1;

	/**
	 * The '<em><b>ENUM</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ENUM
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ENUM_VALUE = 2;

	/**
	 * An array of all the '<em><b>OData Type Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final ODataTypeKind[] VALUES_ARRAY =
		new ODataTypeKind[] {
			ENTITY,
			COMPLEX,
			ENUM,
		};

	/**
	 * A public read-only list of all the '<em><b>OData Type Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<ODataTypeKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>OData Type Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ODataTypeKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ODataTypeKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>OData Type Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ODataTypeKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ODataTypeKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>OData Type Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ODataTypeKind get(int value) {
		switch (value) {
			case ENTITY_VALUE: return ENTITY;
			case COMPLEX_VALUE: return COMPLEX;
			case ENUM_VALUE: return ENUM;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private ODataTypeKind(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getValue() {
	  return value;
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
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //ODataTypeKind
