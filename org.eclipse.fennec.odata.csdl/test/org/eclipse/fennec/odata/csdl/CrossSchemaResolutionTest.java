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

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TNavigationProperty;
import org.open.oasis.docs.odata.ns.edmx.EdmxFactory;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;

/**
 * Multi-schema conversion ({@code toEPackages}): navigation targets in ANOTHER schema
 * resolve through their qualified name (namespace or alias) in the final pass; targets no
 * schema declares are dropped WITH their reference — never an EReference without a type.
 */
@DisplayName("EDM→Ecore: cross-schema navigation resolution")
class CrossSchemaResolutionTest {

	@Test
	@DisplayName("qualified targets resolve across schemas, unresolvable navigations vanish")
	void crossSchemaNavigation() {
		SchemaType shop = schema("com.example.shop", null);
		TEntityType order = entityType("Order");
		order.getNavigationProperty().add(navigation("customer", "com.example.crm.Customer"));
		order.getNavigationProperty().add(navigation("aliased", "CRM.Customer"));
		order.getNavigationProperty().add(navigation("ghost", "com.example.nosuch.Ghost"));
		shop.getEntityType().add(order);

		SchemaType crm = schema("com.example.crm", "CRM");
		crm.getEntityType().add(entityType("Customer"));

		TDataServices services = EdmxFactory.eINSTANCE.createTDataServices();
		services.getSchema().add(shop);
		services.getSchema().add(crm);
		TEdmx edmx = EdmxFactory.eINSTANCE.createTEdmx();
		edmx.setDataServices(services);

		List<EPackage> packages = new EdmToEcoreConverter().toEPackages(edmx);
		assertEquals(2, packages.size());
		EClass orderClass = (EClass) packages.get(0).getEClassifier("Order");
		EClass customerClass = (EClass) packages.get(1).getEClassifier("Customer");

		EReference customer = (EReference) orderClass.getEStructuralFeature("customer");
		assertSame(customerClass, customer.getEType(), "namespace-qualified target resolves");
		EReference aliased = (EReference) orderClass.getEStructuralFeature("aliased");
		assertSame(customerClass, aliased.getEType(), "alias-qualified target resolves");
		assertNull(orderClass.getEStructuralFeature("ghost"),
				"an unresolvable navigation is removed, not left without a type");
	}

	private static SchemaType schema(String namespace, String alias) {
		SchemaType schema = EdmFactory.eINSTANCE.createSchemaType();
		schema.setNamespace(namespace);
		if (alias != null) {
			schema.setAlias(alias);
		}
		return schema;
	}

	private static TEntityType entityType(String name) {
		TEntityType type = EdmFactory.eINSTANCE.createTEntityType();
		type.setName(name);
		return type;
	}

	private static TNavigationProperty navigation(String name, String qualifiedTarget) {
		TNavigationProperty navigation = EdmFactory.eINSTANCE.createTNavigationProperty();
		navigation.setName(name);
		navigation.setType(qualifiedTarget);
		return navigation;
	}
}
