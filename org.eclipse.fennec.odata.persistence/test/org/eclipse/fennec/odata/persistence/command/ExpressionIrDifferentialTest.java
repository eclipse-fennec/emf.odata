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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.eclipse.fennec.odata.ocl.evaluator.OclEvaluator;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * The #11 acceptance criterion: the SAME parsed {@code $filter}/{@code $orderby}
 * corpus evaluated through the legacy OCL path ({@link OclEvaluator}, the normative
 * reference semantics) and through the Expression IR (the full production pipeline:
 * {@link CommandPersistenceService} → {@code OclToExpr} bridge → {@code Query}
 * envelope → the persistence memory query engine behind {@link FakeCommandBackend})
 * must yield identical result sets.
 *
 * <p>Deliberately edge-heavy dataset (nulls, LIKE metacharacters, non-ASCII, ties) —
 * this shape found the three-valued-logic bug in the JPA backend. Two DOCUMENTED
 * divergences are excluded from the corpus: {@code not(...)} over a null-valued
 * comparison (the memory engine negates two-valued where SQL and the evaluator
 * exclude the row — upstream issue) and {@code $orderby} over nullable keys (the
 * memory engine sorts nulls last, the evaluator first).
 */
public class ExpressionIrDifferentialTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/diffshop";

	private EPackage shopPackage;
	private EClass productClass;
	private EClass categoryClass;
	private EClass reviewClass;
	private EAttribute productId;
	private EAttribute productName;
	private EAttribute productPrice;
	private EAttribute productColor;
	private EReference productCategory;
	private EReference productReviews;
	private EAttribute reviewStars;
	private EAttribute categoryName;

	private final ODataQueryParser parser = new ODataQueryParser();
	private final OclEvaluator evaluator = new OclEvaluator();
	private FakeCommandBackend backend;
	private CommandPersistenceService service;
	private List<EObject> seed;

	@BeforeEach
	void setUp() {
		buildModel();
		seed = buildSeed();
		backend = new FakeCommandBackend();
		for (EObject product : seed) {
			backend.storeFor("Product").put(product.eGet(productId), product);
		}
		service = new CommandPersistenceService();
		service.setResourceSetFactory(() -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("fake", backend);
			return resourceSet;
		});
		service.activate(Map.of(CommandPersistenceService.URI_PROPERTY, "fake://store",
				CommandPersistenceService.PACKAGES_PROPERTY, NS_URI));
	}

	private void buildModel() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		shopPackage = ecore.createEPackage();
		shopPackage.setName("diffshop");
		shopPackage.setNsPrefix("diff");
		shopPackage.setNsURI(NS_URI);

		EEnum color = ecore.createEEnum();
		color.setName("Color");
		color.getELiterals().add(literal(ecore, "Green", 0));
		color.getELiterals().add(literal(ecore, "Red", 1));

		categoryClass = ecore.createEClass();
		categoryClass.setName("Category");
		categoryName = attribute(ecore, "name", EcorePackage.Literals.ESTRING);
		categoryClass.getEStructuralFeatures().add(categoryName);

		reviewClass = ecore.createEClass();
		reviewClass.setName("Review");
		reviewStars = attribute(ecore, "stars", EcorePackage.Literals.EINT);
		reviewClass.getEStructuralFeatures().add(reviewStars);

		productClass = ecore.createEClass();
		productClass.setName("Product");
		productId = attribute(ecore, "id", EcorePackage.Literals.EINT);
		productId.setID(true);
		productName = attribute(ecore, "name", EcorePackage.Literals.ESTRING);
		productPrice = attribute(ecore, "price", EcorePackage.Literals.EBIG_DECIMAL);
		productColor = attribute(ecore, "color", color);
		productCategory = ecore.createEReference();
		productCategory.setName("category");
		productCategory.setEType(categoryClass);
		productReviews = ecore.createEReference();
		productReviews.setName("reviews");
		productReviews.setEType(reviewClass);
		productReviews.setContainment(true);
		productReviews.setUpperBound(-1);
		productClass.getEStructuralFeatures().addAll(List.of(productId, productName, productPrice,
				productColor, productCategory, productReviews));

		shopPackage.getEClassifiers().addAll(List.of(color, categoryClass, reviewClass, productClass));
	}

	private static EEnumLiteral literal(EcoreFactory ecore, String name, int value) {
		EEnumLiteral literal = ecore.createEEnumLiteral();
		literal.setName(name);
		literal.setValue(value);
		return literal;
	}

	private static EAttribute attribute(EcoreFactory ecore, String name, Object type) {
		EAttribute attribute = ecore.createEAttribute();
		attribute.setName(name);
		attribute.setEType((org.eclipse.emf.ecore.EClassifier) type);
		return attribute;
	}

	private List<EObject> buildSeed() {
		EObject dairy = create(categoryClass, categoryName, "Dairy");
		EObject pantry = create(categoryClass, categoryName, "Pantry");

		List<EObject> products = new ArrayList<>();
		products.add(product(1, "Cheese", "2.00", "Green", dairy, 5, 3));
		products.add(product(2, "Milk", "2.00", "Red", dairy, 4));
		products.add(product(3, "Salt", null, null, null));
		products.add(product(4, "50%_off", "1.00", null, pantry, 1));
		products.add(product(5, "Süßware", "3.50", "Green", pantry));
		return products;
	}

	private EObject create(EClass type, EAttribute attribute, Object value) {
		EObject object = shopPackage.getEFactoryInstance().create(type);
		object.eSet(attribute, value);
		return object;
	}

	private EObject product(int id, String name, String price, String colorLiteral,
			EObject category, int... stars) {
		EObject product = shopPackage.getEFactoryInstance().create(productClass);
		product.eSet(productId, id);
		product.eSet(productName, name);
		if (price != null) {
			product.eSet(productPrice, new BigDecimal(price));
		}
		if (colorLiteral != null) {
			EEnum color = (EEnum) productColor.getEAttributeType();
			product.eSet(productColor, color.getEEnumLiteral(colorLiteral));
		}
		if (category != null) {
			product.eSet(productCategory, category);
		}
		@SuppressWarnings("unchecked")
		List<EObject> reviews = (List<EObject>) product.eGet(productReviews);
		for (int value : stars) {
			EObject review = shopPackage.getEFactoryInstance().create(reviewClass);
			review.eSet(reviewStars, value);
			reviews.add(review);
		}
		return product;
	}

	private record Case(String label, String filter, String orderBy, boolean ordered) {
		Case(String label, String filter) {
			this(label, filter, null, false);
		}
	}

	@TestFactory
	List<DynamicTest> filterAndOrderParity() {
		List<Case> corpus = List.of(
				new Case("gt", "price gt 1.50"),
				new Case("ge", "price ge 2.00"),
				new Case("lt", "price lt 2.00"),
				new Case("le with and", "price le 2.00 and price gt 0"),
				new Case("ne literal", "name ne 'Milk'"),
				new Case("eq null probe", "price eq null"),
				new Case("ne null probe", "price ne null"),
				new Case("category eq null", "category eq null"),
				new Case("or chain", "name eq 'Milk' or name eq 'Salt' or price gt 3"),
				new Case("in", "name in ('Milk','Salt')"),
				new Case("not over non-null field", "not (name eq 'Milk')"),
				new Case("contains percent", "contains(name,'%')"),
				new Case("contains underscore", "contains(name,'_')"),
				new Case("contains non-ascii", "contains(name,'ß')"),
				new Case("startswith", "startswith(name,'S')"),
				new Case("endswith", "endswith(name,'ware')"),
				new Case("tolower", "tolower(name) eq 'cheese'"),
				new Case("toupper", "toupper(name) eq 'MILK'"),
				new Case("length", "length(name) gt 4"),
				new Case("substring", "substring(name,1) eq 'alt'"),
				new Case("substring negative start", "substring(name,-3) eq 'ilk'"),
				new Case("indexof", "indexof(name,'ee') ge 0"),
				new Case("concat", "concat(name,'!') eq 'Milk!'"),
				new Case("arithmetic add", "price add 1 gt 2.5"),
				new Case("arithmetic mul", "price mul 2 eq 4.00"),
				new Case("arithmetic divby", "price divby 2 lt 1.01"),
				new Case("round", "round(price) eq 2"),
				new Case("floor", "floor(price) eq 3"),
				new Case("ceiling", "ceiling(price) eq 4"),
				new Case("navigation path", "category/name eq 'Dairy'"),
				new Case("any lambda", "reviews/any(r: r/stars ge 5)"),
				new Case("all lambda vacuous truth", "reviews/all(r: r/stars ge 4)"),
				new Case("collection count", "reviews/$count ge 2"),
				new Case("filtered collection count", "reviews/$count($filter=stars ge 4) eq 1"),
				new Case("enum by name", "color eq 'Green'"),
				new Case("ordered by name", null, "name asc", true),
				new Case("ordered desc with tiebreak", "price ne null", "price desc,id asc", true),
				new Case("ordered by expression", "price ne null", "length(name) asc,id asc", true));
		return corpus.stream().map(testCase -> DynamicTest.dynamicTest(testCase.label(),
				() -> assertParity(testCase))).toList();
	}

	private void assertParity(Case testCase) {
		OclExpression filter = testCase.filter() == null ? null
				: parser.parseFilter(testCase.filter(), productClass);
		List<OrderBySegment> orderBy = testCase.orderBy() == null ? List.of()
				: parser.parseOrderBy(testCase.orderBy(), productClass);

		List<Integer> reference = referenceKeys(filter, orderBy);
		List<Integer> viaIr = service
				.execute(new EntityQuery(productClass, null, filter, orderBy, 0, -1, false))
				.entities().stream().map(entity -> (Integer) entity.eGet(productId)).toList();

		if (testCase.ordered()) {
			assertThat(viaIr).isEqualTo(reference);
		} else {
			assertThat(viaIr).containsExactlyInAnyOrderElementsOf(reference);
		}
	}

	/** The legacy path: OclEvaluator filter + evaluator-keyed sort — the oracle. */
	private List<Integer> referenceKeys(OclExpression filter, List<OrderBySegment> orderBy) {
		List<EObject> matches = new ArrayList<>();
		for (EObject product : seed) {
			if (filter == null || evaluator.matchesNullSafe(filter, product)) {
				matches.add(product);
			}
		}
		if (!orderBy.isEmpty()) {
			Comparator<EObject> comparator = null;
			for (OrderBySegment segment : orderBy) {
				Comparator<EObject> key = Comparator.comparing(
						entity -> asComparable(evaluator.evaluate(segment.expression(), entity)));
				if (!segment.ascending()) {
					key = key.reversed();
				}
				comparator = comparator == null ? key : comparator.thenComparing(key);
			}
			matches.sort(comparator);
		}
		return matches.stream().map(entity -> (Integer) entity.eGet(productId)).toList();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Comparable asComparable(Object value) {
		if (value instanceof Number number) {
			return new BigDecimal(number.toString());
		}
		return (Comparable) value;
	}
}
