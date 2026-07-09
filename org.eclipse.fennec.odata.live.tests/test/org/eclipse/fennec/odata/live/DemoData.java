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
package org.eclipse.fennec.odata.live;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.odata.persistence.inmemory.MemoryWriteRepository;

/**
 * Synthetic demo data for the mirror server: for every concrete, keyed entity type of the
 * mirrored schema a handful of instances with generated attribute values — enough for the
 * request parity checks (the DATA is expected to differ from the live service; only the
 * behaviour must match).
 */
final class DemoData {

	private static final int INSTANCES = 3;

	private DemoData() {
	}

	/** Fills the repository; returns the number of created instances. */
	static int fill(MemoryWriteRepository repository, List<EPackage> packages) {
		int created = 0;
		for (EPackage pkg : packages) {
			for (EClassifier classifier : pkg.getEClassifiers()) {
				if (!(classifier instanceof EClass entityType) || entityType.isAbstract()
						|| keyAttribute(entityType) == null) {
					continue;
				}
				for (int i = 1; i <= INSTANCES; i++) {
					EObject instance = instance(entityType, i);
					if (instance != null) {
						repository.create(entityType, instance);
						created++;
					}
				}
			}
		}
		return created;
	}

	/**
	 * The deterministic value an attribute of the {@code index}-th demo instance carries — the
	 * oracle for value-fidelity checks (serialize → decode must reproduce it exactly).
	 */
	static Object valueFor(EAttribute attribute, EClass entityType, int index) {
		return attribute.isID() ? keyValue(attribute, entityType, index)
				: syntheticValue(attribute, index);
	}

	private static EAttribute keyAttribute(EClass entityType) {
		return entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
	}

	private static EObject instance(EClass entityType, int index) {
		EObject instance = entityType.getEPackage().getEFactoryInstance().create(entityType);
		for (EAttribute attribute : entityType.getEAllAttributes()) {
			if (attribute.isMany() || !attribute.isChangeable()) {
				continue;
			}
			Object value = attribute.isID()
					? keyValue(attribute, entityType, index)
					: syntheticValue(attribute, index);
			if (value != null) {
				try {
					instance.eSet(attribute, value);
				} catch (RuntimeException e) {
					// defensive: an unconvertible foreign type stays unset (nullable)
				}
			}
		}
		boolean allKeysSet = entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).allMatch(instance::eIsSet);
		return allKeysSet ? instance : null; // skip types whose (composite) key cannot be generated
	}

	private static Object keyValue(EAttribute key, EClass entityType, int index) {
		Class<?> type = key.getEAttributeType().getInstanceClass();
		if (type == String.class) {
			return entityType.getName().toLowerCase() + "-" + index;
		}
		if (type == Boolean.class || type == boolean.class) {
			return index % 2 == 0; // exotic view types (Northwind) carry boolean key parts
		}
		return numeric(type, index);
	}

	private static Object syntheticValue(EAttribute attribute, int index) {
		if (attribute.getEAttributeType() instanceof EEnum eEnum) {
			return eEnum.getELiterals().get(index % eEnum.getELiterals().size()).getInstance();
		}
		Class<?> type = attribute.getEAttributeType().getInstanceClass();
		if (type == String.class) {
			return "demo-" + attribute.getName() + "-" + index;
		}
		if (type == Boolean.class || type == boolean.class) {
			return index % 2 == 0;
		}
		if (type == Date.class) {
			return new Date(1_700_000_000_000L + index * 86_400_000L);
		}
		return numeric(type, index);
	}

	private static Object numeric(Class<?> type, int index) {
		if (type == Integer.class || type == int.class) {
			return index;
		}
		if (type == Long.class || type == long.class) {
			return (long) index;
		}
		if (type == Short.class || type == short.class) {
			return (short) index;
		}
		if (type == Byte.class || type == byte.class) {
			return (byte) index;
		}
		if (type == Double.class || type == double.class) {
			return index + 0.5d;
		}
		if (type == Float.class || type == float.class) {
			return index + 0.5f;
		}
		if (type == BigDecimal.class) {
			return new BigDecimal(index + ".50");
		}
		if (type == BigInteger.class) {
			return BigInteger.valueOf(index);
		}
		return null; // unknown foreign type — leave unset
	}
}
