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
package org.eclipse.fennec.odata.wire.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** #118: a string attribute holding JSON text is JSON on the wire. */
@DisplayName("JsonTextHandler")
class JsonTextHandlerTest {

	private final EAttribute properties = EcoreFactory.eINSTANCE.createEAttribute();
	private final EClass thing = EcoreFactory.eINSTANCE.createEClass();
	private final JsonTextHandler handler = new JsonTextHandler(properties);

	JsonTextHandlerTest() {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("t");
		pkg.setNsURI("http://example.org/t");
		thing.setName("Thing");
		properties.setName("properties");
		properties.setEType(EcorePackage.Literals.ESTRING);
		thing.getEStructuralFeatures().add(properties);
		pkg.getEClassifiers().add(thing);
	}

	@Test
	@DisplayName("writes the text as JSON, null as null")
	void write() {
		EObject entity = thing.getEPackage().getEFactoryInstance().create(thing);
		assertNull(handler.write(entity));
		entity.eSet(properties, "{\"a\":1}");
		assertEquals(new WireJson("{\"a\":1}"), handler.write(entity));
	}

	@Test
	@DisplayName("reads an object or array, null clears; a string is 400")
	void read() {
		assertEquals(Map.of(properties, "[1]"), handler.read(new WireJson("[1]")));
		assertTrue(handler.read(null).containsKey(properties));
		assertNull(handler.read(null).get(properties));
		assertThrows(IllegalArgumentException.class, () -> handler.read("text"));
	}

	@Test
	@DisplayName("neither filterable nor sortable")
	void query() {
		assertThrows(UnsupportedOperationException.class, () -> handler.compare(WireOperator.EQ, OperandKind.STRING));
		assertNull(handler.operandOf("length"));
		assertTrue(handler.orderBy().isEmpty());
	}
}
