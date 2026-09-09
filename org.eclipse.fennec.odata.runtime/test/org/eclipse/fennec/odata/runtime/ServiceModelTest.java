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
package org.eclipse.fennec.odata.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.odata.csdl.ODataAnnotationConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The per-root allowlist ({@link ServiceModel}): which bound packages become schemas, which
 * concrete classes become (renamed) entity sets, and how the configuration values parse.
 */
class ServiceModelTest {

	private final EPackage shop = pkg("shop", "http://example.org/shop", "Product", "Category");
	private final EPackage atlas = pkg("atlas", "http://example.org/atlas", "DataSet", "Endpoint");

	@Test
	@DisplayName("unconfigured: every bound package, every concrete class, in binding order")
	void everythingBound() {
		ServiceModel model = ServiceModel.of(List.of(shop, atlas));
		assertEquals(List.of(shop, atlas), model.packages());
		assertEquals(List.of("Category", "DataSet", "Endpoint", "Product"), model.entitySetNames());
		assertSame(shop.getEClassifier("Product"), model.entityType("Product"));
		assertNull(model.entityType("Abstract"), "abstract classes are never sets");
	}

	@Test
	@DisplayName("odata.model.packages: a bound package outside the list contributes nothing")
	void packageAllowlist() {
		ServiceModel model = ServiceModel.of(List.of(shop, atlas), ServiceModel.Selection
				.fromConfiguration(Map.of(ServiceModel.PACKAGES_KEY, "http://example.org/shop")));
		assertEquals(List.of(shop), model.packages());
		assertEquals(List.of("Category", "Product"), model.entitySetNames());
		assertNull(model.entityType("DataSet"), "an unpublished set is unknown, not a leak");
		assertFalse(model.publishes((EClass) atlas.getEClassifier("DataSet")));
	}

	@Test
	@DisplayName("odata.model.entitysets: exactly the listed sets, under their configured names")
	void entitySetAllowlistAndRename() {
		ServiceModel model = ServiceModel.of(List.of(shop, atlas),
				ServiceModel.Selection.fromConfiguration(Map.of(ServiceModel.ENTITY_SETS_KEY,
						new String[] { "Products=http://example.org/shop#Product", "DataSet" })));
		assertEquals(List.of(shop, atlas), model.packages(), "schemas are not narrowed by the set list");
		assertEquals(List.of("DataSet", "Products"), model.entitySetNames());
		EClass product = (EClass) shop.getEClassifier("Product");
		assertSame(product, model.entityType("Products"));
		assertNull(model.entityType("Product"), "the type name is no longer a set name");
		assertNull(model.entityType("Category"), "unlisted → 404");
		assertEquals("Products", model.setNameOf(product));
		assertEquals("Category", model.setNameOf((EClass) shop.getEClassifier("Category")),
				"a type without a set answers with its type name (navigation targets)");
		assertEquals(Map.of("Product", "Products", "DataSet", "DataSet"), model.typeToSetNames());
	}

	@Test
	@DisplayName("an entry naming no published class is skipped, not fatal — packages arrive dynamically")
	void unresolvableEntrySkipped() {
		ServiceModel.Selection selection = ServiceModel.Selection.fromConfiguration(Map.of(
				ServiceModel.PACKAGES_KEY, "http://example.org/shop",
				ServiceModel.ENTITY_SETS_KEY, "Product, DataSet, http://example.org/nowhere#Thing"));
		ServiceModel model = ServiceModel.of(List.of(shop, atlas), selection);
		assertEquals(List.of("Product"), model.entitySetNames(),
				"DataSet's package is not published and Thing does not exist");
		ServiceModel later = ServiceModel.of(List.of(shop, atlas), ServiceModel.Selection
				.fromConfiguration(Map.of(ServiceModel.ENTITY_SETS_KEY, "Product, DataSet")));
		assertEquals(List.of("DataSet", "Product"), later.entitySetNames(),
				"the same entry resolves once its package is published");
	}

	@Test
	@DisplayName("package rename annotations apply where no configured name overrides them")
	void annotatedRenames() {
		EAnnotation sets = EcoreFactory.eINSTANCE.createEAnnotation();
		sets.setSource(ODataAnnotationConstants.ENTITY_SETS_SOURCE);
		sets.getDetails().put("Items", "Product");
		shop.getEAnnotations().add(sets);

		ServiceModel unconfigured = ServiceModel.of(List.of(shop));
		assertEquals(List.of("Category", "Items"), unconfigured.entitySetNames());
		assertSame(shop.getEClassifier("Product"), unconfigured.entityType("Items"));

		ServiceModel listed = ServiceModel.of(List.of(shop), ServiceModel.Selection
				.fromConfiguration(Map.of(ServiceModel.ENTITY_SETS_KEY, "Product")));
		assertEquals(List.of("Items"), listed.entitySetNames(), "listed by type, named by annotation");

		ServiceModel overridden = ServiceModel.of(List.of(shop), ServiceModel.Selection
				.fromConfiguration(Map.of(ServiceModel.ENTITY_SETS_KEY, "Products=Product")));
		assertEquals(List.of("Products"), overridden.entitySetNames(), "configuration wins");
	}

	@Test
	@DisplayName("singletons follow the package allowlist and, when sets are listed, their type")
	void singletons() {
		EAnnotation singletons = EcoreFactory.eINSTANCE.createEAnnotation();
		singletons.setSource(ODataAnnotationConstants.SINGLETONS_SOURCE);
		singletons.getDetails().put("Me", "Product");
		shop.getEAnnotations().add(singletons);

		assertEquals(Set.of("Me"), ServiceModel.of(List.of(shop, atlas)).singletons().keySet());
		assertTrue(ServiceModel.of(List.of(shop, atlas), ServiceModel.Selection.fromConfiguration(
				Map.of(ServiceModel.PACKAGES_KEY, "http://example.org/atlas"))).singletons().isEmpty(),
				"the singleton's package is not published");
		assertTrue(ServiceModel.of(List.of(shop), ServiceModel.Selection.fromConfiguration(
				Map.of(ServiceModel.ENTITY_SETS_KEY, "Category"))).singletons().isEmpty(),
				"the singleton's type is not among the listed sets");
	}

	@Test
	@DisplayName("multi-valued properties: String[], Collection, or one comma/space separated string")
	void valueParsing() {
		assertEquals(List.of("a", "b", "c"), ServiceModel.Selection.values("a, b c"));
		assertEquals(List.of("a", "b"), ServiceModel.Selection.values(new String[] { "a", " b " }));
		assertEquals(List.of("a", "b"), ServiceModel.Selection.values(List.of("a", "b")));
		assertEquals(List.of(), ServiceModel.Selection.values(null));
		assertEquals(List.of(), ServiceModel.Selection.values(" "));
		assertSame(ServiceModel.Selection.ALL, ServiceModel.Selection.fromConfiguration(null));
		assertFalse(ServiceModel.Selection.fromConfiguration(Map.of()).restrictsPackages());
	}

	private static EPackage pkg(String name, String nsUri, String... concreteClasses) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName(name);
		pkg.setNsPrefix(name);
		pkg.setNsURI(nsUri);
		for (String className : concreteClasses) {
			EClass eClass = EcoreFactory.eINSTANCE.createEClass();
			eClass.setName(className);
			pkg.getEClassifiers().add(eClass);
		}
		EClass abstractClass = EcoreFactory.eINSTANCE.createEClass();
		abstractClass.setName("Abstract");
		abstractClass.setAbstract(true);
		pkg.getEClassifiers().add(abstractClass);
		return pkg;
	}
}
