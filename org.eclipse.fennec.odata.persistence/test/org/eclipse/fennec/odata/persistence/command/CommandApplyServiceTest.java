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

import java.util.List;
import java.util.Map;

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
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code $apply} through the command backend (#12): the parsed pipeline maps onto the
 * query envelope's pipeline stages and executes in the persistence engine — no in-memory
 * aggregation in the OData layer. Row shape follows the reference backend contract:
 * grouping paths nest, aggregate/compute aliases stay flat.
 */
@DisplayName("CommandPersistenceService: $apply on the pipeline stages")
class CommandApplyServiceTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/commandapply";

	private EPackage pkg;
	private EClass productClass;
	private EClass categoryClass;
	private EAttribute productId;
	private EAttribute productName;
	private EAttribute productPrice;
	private EAttribute productRating;
	private EReference productCategory;
	private EAttribute categoryName;

	private FakeCommandBackend backend;
	private CommandPersistenceService service;
	private final ODataQueryParser parser = new ODataQueryParser();

	@BeforeEach
	void setUp() {
		buildModel();
		backend = new FakeCommandBackend();
		ResourceSetFactory factory = () -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("fake", backend);
			return resourceSet;
		};
		service = new CommandPersistenceService();
		service.setResourceSetFactory(factory);
		service.activate(Map.of(CommandPersistenceService.URI_PROPERTY, "fake://store",
				CommandPersistenceService.PACKAGES_PROPERTY, NS_URI));
		seed(product(1, "Milk", 12, 3, "Dairy"));
		seed(product(2, "Cheese", 45, 5, "Dairy"));
		seed(product(3, "Bread", 28, 4, "Bakery"));
		seed(product(4, "Salt", null, 3, "Pantry"));
	}

	private ApplyResult apply(String applyText) {
		return apply(applyText, null, null, 0, -1, false);
	}

	private ApplyResult apply(String applyText, String rowFilter, String orderBy, int skip, int top,
			boolean count) {
		ApplyPipeline pipeline = parser.parseApply(applyText, productClass);
		return service.executeApply(new ApplyQuery(productClass, pipeline,
				rowFilter == null ? null
						: parser.parseFilterAfterApply(rowFilter, productClass, pipeline),
				orderBy == null ? List.of()
						: parser.parseOrderByAfterApply(orderBy, productClass, pipeline),
				skip, top, count));
	}

	@Test
	@DisplayName("whole-set aggregate: sum, min, max, $count, countdistinct")
	void wholeSetAggregate() {
		ApplyResult result = apply("aggregate(price with sum as total,price with min as cheapest,"
				+ "price with max as dearest,$count as n,rating with countdistinct as ratings)");
		assertThat(result.rows()).hasSize(1);
		Map<String, Object> row = result.rows().get(0);
		assertThat(numeric(row.get("total"))).isEqualTo(85L);
		assertThat(numeric(row.get("cheapest"))).isEqualTo(12L);
		assertThat(numeric(row.get("dearest"))).isEqualTo(45L);
		assertThat(numeric(row.get("n"))).isEqualTo(4L);
		assertThat(numeric(row.get("ratings"))).as("ratings 3,5,4,3 → 3 distinct").isEqualTo(3L);
	}

	@Test
	@DisplayName("groupby over a navigation path nests the grouping key in the row")
	void groupByNavigationPath() {
		ApplyResult result = apply("groupby((category/name),aggregate(price with sum as total))");
		assertThat(result.rows()).hasSize(3);
		Map<String, Object> dairy = rowFor(result, "Dairy");
		assertThat(numeric(dairy.get("total"))).isEqualTo(57L);
		assertThat(dairy.get("category")).isInstanceOf(Map.class);
		assertThat(numeric(rowFor(result, "Bakery").get("total"))).isEqualTo(28L);
	}

	@Test
	@DisplayName("leading filter folds into WHERE, post-group filter becomes a HAVING stage")
	void filtersFoldByPosition() {
		ApplyResult lead = apply(
				"filter(rating gt 3)/groupby((category/name),aggregate(price with sum as total))");
		assertThat(lead.rows()).extracting(row -> categoryOf(row))
				.containsExactlyInAnyOrder("Dairy", "Bakery");

		ApplyResult having = apply(
				"groupby((category/name),aggregate(price with sum as total))/filter(total gt 30)");
		assertThat(having.rows()).hasSize(1);
		assertThat(categoryOf(having.rows().get(0))).isEqualTo("Dairy");
	}

	@Test
	@DisplayName("compute after groupby adds alias columns (arithmetic over aggregates)")
	void computeAfterGroupBy() {
		ApplyResult result = apply("groupby((category/name),aggregate(price with sum as total))"
				+ "/compute(total mul 2 as doubled)");
		Map<String, Object> dairy = rowFor(result, "Dairy");
		assertThat(numeric(dairy.get("doubled"))).isEqualTo(114L);
	}

	@Test
	@DisplayName("groupby without aggregates is a DISTINCT projection")
	void distinctGroupBy() {
		ApplyResult result = apply("groupby((category/name))");
		assertThat(result.rows()).extracting(row -> categoryOf(row))
				.containsExactlyInAnyOrder("Dairy", "Bakery", "Pantry");
	}

	@Test
	@DisplayName("post-apply options: alias filter, alias sort, paging, count")
	void postApplyOptions() {
		ApplyResult result = apply("groupby((category/name),aggregate(price with sum as total))",
				"total gt 20", "total desc", 0, 1, true);
		assertThat(result.totalCount()).as("two groups pass total gt 20").isEqualTo(2);
		assertThat(result.rows()).hasSize(1);
		assertThat(categoryOf(result.rows().get(0))).as("Dairy(57) before Bakery(28)")
				.isEqualTo("Dairy");
	}

	@Test
	@DisplayName("a filter-only pipeline keeps the entity shape and flattens attributes")
	void filterOnlyPipeline() {
		ApplyResult result = apply("filter(rating ge 4)");
		assertThat(result.rows()).hasSize(2);
		assertThat(result.rows()).extracting(row -> row.get("name"))
				.containsExactlyInAnyOrder("Cheese", "Bread");
	}

	@Test
	@DisplayName("transformations outside the stage model are honest 501s")
	// topcount/bottomcount left this list with #63 — see ApplyWindowDifferentialTest for what
	// they map to and which of their siblings stayed behind
	void refusals() {
		assertThatThrownBy(() -> apply(
				"concat(groupby((category/name),aggregate(price with sum as t)),aggregate(price with sum as t))"))
				.isInstanceOf(UnsupportedOperationException.class);
		assertThatThrownBy(() -> apply("compute(price mul 2 as doubled)"))
				.as("entity-space compute is not expressible in the stage model yet")
				.isInstanceOf(UnsupportedOperationException.class);
	}

	private static String categoryOf(Map<String, Object> row) {
		Object category = row.get("category");
		assertThat(category).as("grouping paths nest: " + row).isInstanceOf(Map.class);
		return String.valueOf(((Map<?, ?>) category).get("name"));
	}

	private static Map<String, Object> rowFor(ApplyResult result, String categoryName) {
		return result.rows().stream().filter(row -> categoryName.equals(categoryOf(row)))
				.findFirst().orElseThrow(() -> new AssertionError(
						"no row for " + categoryName + " in " + result.rows()));
	}

	/** Engines answer numerics in engine-specific types — compare on the long value. */
	private static long numeric(Object value) {
		assertThat(value).as("numeric cell").isInstanceOf(Number.class);
		return ((Number) value).longValue();
	}

	private void seed(EObject product) {
		backend.storeFor("Product").put(product.eGet(productId), product);
	}

	private EObject product(int id, String name, Integer price, int rating, String category) {
		EObject product = pkg.getEFactoryInstance().create(productClass);
		product.eSet(productId, id);
		product.eSet(productName, name);
		if (price != null) {
			product.eSet(productPrice, price);
		}
		product.eSet(productRating, rating);
		if (category != null) {
			EObject categoryObject = pkg.getEFactoryInstance().create(categoryClass);
			categoryObject.eSet(categoryName, category);
			product.eSet(productCategory, categoryObject);
		}
		return product;
	}

	private void buildModel() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		pkg = ecore.createEPackage();
		pkg.setName("commandapply");
		pkg.setNsPrefix("capply");
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
