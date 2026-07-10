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
package org.eclipse.fennec.odata.query.apply;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Bottom Top Method</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.odata.query.apply.ApplyPackage#getBottomTopMethod()
 * @model
 * @generated
 */
@ProviderType
public enum BottomTopMethod implements Enumerator {
	/**
	 * The '<em><b>TOP COUNT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TOP_COUNT_VALUE
	 * @generated
	 * @ordered
	 */
	TOP_COUNT(0, "TOP_COUNT", "TOP_COUNT"),

	/**
	 * The '<em><b>TOP SUM</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TOP_SUM_VALUE
	 * @generated
	 * @ordered
	 */
	TOP_SUM(1, "TOP_SUM", "TOP_SUM"),

	/**
	 * The '<em><b>TOP PERCENT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TOP_PERCENT_VALUE
	 * @generated
	 * @ordered
	 */
	TOP_PERCENT(2, "TOP_PERCENT", "TOP_PERCENT"),

	/**
	 * The '<em><b>BOTTOM COUNT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #BOTTOM_COUNT_VALUE
	 * @generated
	 * @ordered
	 */
	BOTTOM_COUNT(3, "BOTTOM_COUNT", "BOTTOM_COUNT"),

	/**
	 * The '<em><b>BOTTOM SUM</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #BOTTOM_SUM_VALUE
	 * @generated
	 * @ordered
	 */
	BOTTOM_SUM(4, "BOTTOM_SUM", "BOTTOM_SUM"),

	/**
	 * The '<em><b>BOTTOM PERCENT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #BOTTOM_PERCENT_VALUE
	 * @generated
	 * @ordered
	 */
	BOTTOM_PERCENT(5, "BOTTOM_PERCENT", "BOTTOM_PERCENT");

	/**
	 * The '<em><b>TOP COUNT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TOP_COUNT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TOP_COUNT_VALUE = 0;

	/**
	 * The '<em><b>TOP SUM</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TOP_SUM
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TOP_SUM_VALUE = 1;

	/**
	 * The '<em><b>TOP PERCENT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TOP_PERCENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TOP_PERCENT_VALUE = 2;

	/**
	 * The '<em><b>BOTTOM COUNT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #BOTTOM_COUNT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int BOTTOM_COUNT_VALUE = 3;

	/**
	 * The '<em><b>BOTTOM SUM</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #BOTTOM_SUM
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int BOTTOM_SUM_VALUE = 4;

	/**
	 * The '<em><b>BOTTOM PERCENT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #BOTTOM_PERCENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int BOTTOM_PERCENT_VALUE = 5;

	/**
	 * An array of all the '<em><b>Bottom Top Method</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final BottomTopMethod[] VALUES_ARRAY =
		new BottomTopMethod[] {
			TOP_COUNT,
			TOP_SUM,
			TOP_PERCENT,
			BOTTOM_COUNT,
			BOTTOM_SUM,
			BOTTOM_PERCENT,
		};

	/**
	 * A public read-only list of all the '<em><b>Bottom Top Method</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<BottomTopMethod> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Bottom Top Method</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static BottomTopMethod get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			BottomTopMethod result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Bottom Top Method</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static BottomTopMethod getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			BottomTopMethod result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Bottom Top Method</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static BottomTopMethod get(int value) {
		switch (value) {
			case TOP_COUNT_VALUE: return TOP_COUNT;
			case TOP_SUM_VALUE: return TOP_SUM;
			case TOP_PERCENT_VALUE: return TOP_PERCENT;
			case BOTTOM_COUNT_VALUE: return BOTTOM_COUNT;
			case BOTTOM_SUM_VALUE: return BOTTOM_SUM;
			case BOTTOM_PERCENT_VALUE: return BOTTOM_PERCENT;
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
	private BottomTopMethod(int value, String name, String literal) {
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
	
} //BottomTopMethod
