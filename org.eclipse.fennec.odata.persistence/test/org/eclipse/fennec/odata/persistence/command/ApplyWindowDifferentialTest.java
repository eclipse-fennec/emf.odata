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
package org.eclipse.fennec.odata.persistence.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.inmemory.ApplyExecutor;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * {@code topcount}/{@code bottomcount} (#63) pushed down, against the in-memory
 * {@link ApplyExecutor} as the reference — the same corpus through both engines, plus the
 * expected rows written out, because parity alone is also satisfied by two engines that are
 * wrong together.
 *
 * <p>The mapping is an ordered window: order by the value expression, keep the first n. What
 * that has to reproduce from the reference is not only the membership but the ORDER (the
 * reference returns its selection value-sorted) and the treatment of null values, which never
 * enter the window.
 */
@DisplayName("$apply: topcount/bottomcount as a pushed-down sort window")
class ApplyWindowDifferentialTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/applywindow";

	private EPackage pkg;
	private EClass productClass;
	private EClass categoryClass;
	private EAttribute productId;
	private EAttribute productName;
	private EAttribute productPrice;
	private EAttribute productRating;
	private EReference productCategory;
	private EAttribute categoryName;

	private final ODataQueryParser parser = new ODataQueryParser();
	private final ApplyExecutor reference = new ApplyExecutor();
	private FakeCommandBackend backend;
	private CommandPersistenceService service;
	private List<EObject> seed;

	@BeforeEach
	void setUp() {
		buildModel();
		backend = new FakeCommandBackend();
		seed = new ArrayList<>();
		seed(product(1, "Milk", 12, 3, "Dairy"));
		seed(product(2, "Cheese", 45, 5, "Dairy"));
		seed(product(3, "Bread", 28, 4, "Bakery"));
		seed(product(4, "Salt", null, 2, "Pantry"));

		ResourceSetFactory factory = () -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("fake", backend);
			return resourceSet;
		};
		service = new CommandPersistenceService();
		service.setResourceSetFactory(factory);
		service.activate(Map.of(CommandPersistenceService.URI_PROPERTY, "fake://store",
				CommandPersistenceService.PACKAGES_PROPERTY, NS_URI));
	}

	private record Case(String label, String apply, String key, List<String> expected) {
	}

	@TestFactory
	List<DynamicTest> windowParity() {
		List<Case> corpus = List.of(
				// entity space: the window keeps the row shape, so the answer is entities
				new Case("topcount", "topcount(2,price)", "name", List.of("Cheese", "Bread")),
				new Case("bottomcount", "bottomcount(2,price)", "name", List.of("Milk", "Bread")),
				// Salt has no price: a null value contributes no rank and must not fill the
				// window, not even when the window is larger than the set
				new Case("topcount wider than the set", "topcount(5,price)", "name",
						List.of("Cheese", "Bread", "Milk")),
				new Case("bottomcount of one", "bottomcount(1,price)", "name", List.of("Milk")),
				// a leading filter folds into WHERE and the window applies to what is left
				new Case("after a filter", "filter(rating ge 3)/topcount(2,price)", "name",
						List.of("Cheese", "Bread")),
				// row space: the window orders the GROUPED rows by an aggregate alias
				new Case("after groupby/aggregate",
						"groupby((category/name),aggregate(rating with sum as total))/topcount(2,total)",
						"category/name", List.of("Dairy", "Bakery")),
				new Case("bottomcount after groupby/aggregate",
						"groupby((category/name),aggregate(rating with sum as total))/bottomcount(1,total)",
						"category/name", List.of("Pantry")));
		return corpus.stream().map(testCase -> DynamicTest.dynamicTest(testCase.label(),
				() -> assertWindow(testCase))).toList();
	}

	private void assertWindow(Case testCase) {
		ApplyPipeline pipeline = parser.parseApply(testCase.apply(), productClass);
		Function<Map<String, Object>, String> key = keyReader(testCase.key());

		List<String> pushedDown = service
				.executeApply(new ApplyQuery(productClass, pipeline, null, List.of(), 0, -1, false))
				.rows().stream().map(key).toList();
		List<String> inMemory = reference
				.execute(parser.parseApply(testCase.apply(), productClass), seed).stream()
				.map(key).toList();

		assertThat(pushedDown).as("pushed down").isEqualTo(testCase.expected());
		assertThat(inMemory).as("the in-memory ApplyExecutor reference")
				.isEqualTo(testCase.expected());
	}

	@SuppressWarnings("unchecked")
	private static Function<Map<String, Object>, String> keyReader(String key) {
		if (!key.contains("/")) {
			return row -> String.valueOf(row.get(key));
		}
		String[] segments = key.split("/");
		return row -> String.valueOf(
				((Map<String, Object>) row.get(segments[0])).get(segments[1]));
	}

	@Test
	@DisplayName("$top narrows the window, it never widens or reorders it")
	void topComposesWithTheWindow() {
		ApplyPipeline pipeline = parser.parseApply("topcount(3,price)", productClass);
		ApplyResult result = service.executeApply(
				new ApplyQuery(productClass, pipeline, null, List.of(), 0, 2, false));

		assertThat(result.rows()).extracting(row -> row.get("name"))
				.containsExactly("Cheese", "Bread");
	}

	@Test
	@DisplayName("the running-total members and the combinations we cannot express are 501s")
	void refusals() {
		assertThatThrownBy(() -> apply("topsum(50,price)"))
				.as("the smallest prefix reaching a running sum has no IR expression")
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("topsum");
		assertThatThrownBy(() -> apply("toppercent(50,price)"))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("toppercent");
		assertThatThrownBy(() -> apply("bottomsum(20,price)"))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("bottomsum");
		assertThatThrownBy(() -> apply("topcount(2,price)/filter(rating ge 4)"))
				.as("a later transformation would see the unlimited set")
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("final transformation");
		assertThatThrownBy(() -> service.executeApply(new ApplyQuery(productClass,
				parser.parseApply("topcount(2,price)", productClass), null,
				parser.parseOrderByAfterApply("name asc", productClass,
						parser.parseApply("topcount(2,price)", productClass)),
				0, -1, false)))
				.as("one ORDER BY list cannot both pick the window and sort the answer")
				.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> service.executeApply(new ApplyQuery(productClass,
				parser.parseApply("topcount(2,price)", productClass), null, List.of(), 1, -1,
				false)))
				.as("$skip would page the underlying set, not the window")
				.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> service.executeApply(new ApplyQuery(productClass,
				parser.parseApply("topcount(2,price)", productClass), null, List.of(), 0, -1,
				true)))
				.as("the count is taken before the window")
				.isInstanceOf(UnsupportedOperationException.class);
	}

	private ApplyResult apply(String applyText) {
		return service.executeApply(new ApplyQuery(productClass,
				parser.parseApply(applyText, productClass), null, List.of(), 0, -1, false));
	}

	private void seed(EObject product) {
		backend.storeFor("Product").put(product.eGet(productId), product);
		seed.add(product);
	}

	private EObject product(int id, String name, Integer price, int rating, String category) {
		EObject product = pkg.getEFactoryInstance().create(productClass);
		product.eSet(productId, id);
		product.eSet(productName, name);
		if (price != null) {
			product.eSet(productPrice, price);
		}
		product.eSet(productRating, rating);
		EObject categoryObject = pkg.getEFactoryInstance().create(categoryClass);
		categoryObject.eSet(categoryName, category);
		product.eSet(productCategory, categoryObject);
		return product;
	}

	private void buildModel() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		pkg = ecore.createEPackage();
		pkg.setName("applywindow");
		pkg.setNsPrefix("awin");
		pkg.setNsURI(NS_URI);

		categoryClass = ecore.createEClass();
		categoryClass.setName("Category");
		categoryName = ecore.createEAttribute();
		categoryName.setName("name");
		categoryName.setEType(EcorePackage.Literals.ESTRING);
		categoryClass.getEStructuralFeatures().add(categoryName);

		productClass = ecore.createEClass();
		productClass.setName("Product");
		productId = ecore.createEAttribute();
		productId.setName("id");
		productId.setEType(EcorePackage.Literals.EINT);
		productId.setID(true);
		productName = ecore.createEAttribute();
		productName.setName("name");
		productName.setEType(EcorePackage.Literals.ESTRING);
		productPrice = ecore.createEAttribute();
		productPrice.setName("price");
		productPrice.setEType(EcorePackage.Literals.EINTEGER_OBJECT);
		productRating = ecore.createEAttribute();
		productRating.setName("rating");
		productRating.setEType(EcorePackage.Literals.EINT);
		productCategory = ecore.createEReference();
		productCategory.setName("category");
		productCategory.setEType(categoryClass);
		productCategory.setContainment(true);
		productClass.getEStructuralFeatures().addAll(List.of(productId, productName, productPrice,
				productRating, productCategory));

		pkg.getEClassifiers().addAll(List.of(productClass, categoryClass));
	}
}
