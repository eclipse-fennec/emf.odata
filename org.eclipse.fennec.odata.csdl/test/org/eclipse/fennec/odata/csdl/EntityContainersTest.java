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
package org.eclipse.fennec.odata.csdl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TEntitySet;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TFunctionImport;
import org.open.oasis.docs.odata.ns.edm.TNavigationProperty;
import org.open.oasis.docs.odata.ns.edm.TNavigationPropertyBinding;

/** {@link EntityContainers}: one container per service document, [OData-CSDL] §13 (#84). */
class EntityContainersTest {

	private static final EdmFactory EDM = EdmFactory.eINSTANCE;

	@Test
	@DisplayName("the containers of all schemas merge into the first schema's; the others are removed")
	void mergesIntoFirstSchema() {
		SchemaType a = schema("a", entity("Order", nav("customer", "b.Customer", false)));
		SchemaType b = schema("b", entity("Customer"), entity("Address"));
		container(a, set("Order", "a.Order"));
		container(b, set("Customer", "b.Customer"));

		TEntityContainer merged = EntityContainers.merge(List.of(a, b));

		assertSame(a, merged.eContainer());
		assertEquals(1, a.getEntityContainer().size());
		assertTrue(b.getEntityContainer().isEmpty());
		assertEquals(List.of("Order", "Customer"), merged.getEntitySet().stream().map(TEntitySet::getName).toList());
		assertEquals(2, b.getEntityType().size(), "the types stay in their own schema");
	}

	@Test
	@DisplayName("a first schema without a container of its own still hosts the merged one")
	void firstSchemaHostsEvenWithoutOwnContainer() {
		SchemaType a = schema("a");
		SchemaType b = schema("b", entity("Customer"));
		container(b, set("Customer", "b.Customer"));

		TEntityContainer merged = EntityContainers.merge(List.of(a, b));

		assertSame(a, merged.eContainer());
		assertTrue(b.getEntityContainer().isEmpty());
	}

	@Test
	@DisplayName("navigations across schemas are bound — inherited ones, collections; containment and existing paths are not")
	void bindsCrossSchemaNavigations() {
		SchemaType a = schema("a",
				entity("Base", nav("owner", "b.Customer", false)),
				entity("Order", "a.Base", nav("lines", "Collection(b.Line)", true),
						nav("items", "Collection(b.Customer)", false)));
		SchemaType b = schema("b", entity("Customer"), entity("Line"));
		TEntitySet orders = set("Orders", "a.Order");
		TNavigationPropertyBinding existing = EDM.createTNavigationPropertyBinding();
		existing.setPath("items");
		existing.setTarget("Elsewhere");
		orders.getNavigationPropertyBinding().add(existing);
		container(a, orders);
		container(b, set("Customers", "b.Customer"), set("Lines", "b.Line"));

		EntityContainers.merge(List.of(a, b));

		assertEquals(List.of("items->Elsewhere", "owner->Customers"),
				orders.getNavigationPropertyBinding().stream()
						.map(binding -> binding.getPath() + "->" + binding.getTarget()).toList());
	}

	@Test
	@DisplayName("a member name declared twice is kept once")
	void duplicateNamesKeptOnce() {
		SchemaType a = schema("a");
		SchemaType b = schema("b");
		TEntityContainer ca = container(a);
		ca.getFunctionImport().add(functionImport("top"));
		TEntityContainer cb = container(b);
		cb.getFunctionImport().add(functionImport("top"));

		TEntityContainer merged = EntityContainers.merge(List.of(a, b));

		assertEquals(1, merged.getFunctionImport().size());
		assertTrue(b.getEntityContainer().isEmpty());
	}

	@Test
	@DisplayName("no container anywhere → nothing to merge")
	void noContainer() {
		assertNull(EntityContainers.merge(List.of(schema("a"), schema("b"))));
	}

	private static SchemaType schema(String namespace, TEntityType... types) {
		SchemaType schema = EDM.createSchemaType();
		schema.setNamespace(namespace);
		schema.getEntityType().addAll(List.of(types));
		return schema;
	}

	private static TEntityType entity(String name, TNavigationProperty... navigations) {
		return entity(name, null, navigations);
	}

	private static TEntityType entity(String name, String baseType, TNavigationProperty... navigations) {
		TEntityType type = EDM.createTEntityType();
		type.setName(name);
		if (baseType != null) {
			type.setBaseType(baseType);
		}
		type.getNavigationProperty().addAll(List.of(navigations));
		return type;
	}

	private static TNavigationProperty nav(String name, String type, boolean containsTarget) {
		TNavigationProperty navigation = EDM.createTNavigationProperty();
		navigation.setName(name);
		navigation.setType(type);
		navigation.setContainsTarget(containsTarget);
		return navigation;
	}

	private static TEntitySet set(String name, String entityType) {
		TEntitySet set = EDM.createTEntitySet();
		set.setName(name);
		set.setEntityType(entityType);
		return set;
	}

	private static TFunctionImport functionImport(String name) {
		TFunctionImport functionImport = EDM.createTFunctionImport();
		functionImport.setName(name);
		functionImport.setFunction("a." + name);
		return functionImport;
	}

	private static TEntityContainer container(SchemaType schema, TEntitySet... sets) {
		TEntityContainer container = EDM.createTEntityContainer();
		container.setName("DefaultContainer");
		container.getEntitySet().addAll(List.of(sets));
		schema.getEntityContainer().add(container);
		return container;
	}
}
