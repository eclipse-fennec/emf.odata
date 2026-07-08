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
package org.eclipse.fennec.odata.persistence.inmemory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.m2x.model.ocl.OclFactory;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * In-memory backend tests (req §3.5: mandatory reference backend). The data path is the REAL
 * file path: instances are written as XMI into a temp directory and served by the
 * {@link FileEntityRepository} — proving the persistence abstraction reads from files.
 * Includes destructive cases: evaluation errors must surface as errors, never as silently
 * wrong results.
 */
@DisplayName("In-memory QueryService over file-backed repository")
class InMemoryQueryServiceTest {

	@TempDir
	Path dataDirectory;

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private EClass productClass;

	private final ODataQueryParser parser = new ODataQueryParser();
	private InMemoryQueryService service;

	@BeforeEach
	void setUp() throws Exception {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(findResource("testdata/webshop.ecore",
				"org.eclipse.fennec.odata.persistence.inmemory/testdata/webshop.ecore"));
		productClass = EcoreHelper.getEClass(pkg, "Product");

		writeDataFile();

		service = new InMemoryQueryService();
		service.addRepository(new FileEntityRepository(dataDirectory, List.of(pkg)));
	}

	@AfterEach
	void tearDown() {
		ecoreHelper.releaseAll();
	}

	/** Milk 1.20, Cheese 4.50 (rating 5, Green, reviews 5/4), Bread 2.80, Salt without price. */
	private void writeDataFile() throws Exception {
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");
		EClass reviewClass = EcoreHelper.getEClass(pkg, "Review");
		EEnum color = (EEnum) pkg.getEClassifier("Color");

		EObject dairy = create(categoryClass, "id", "c1", "name", "Dairy");
		EObject bakery = create(categoryClass, "id", "c2", "name", "Bakery");

		EObject milk = create(productClass, "id", "p1", "name", "Milk", "price", new BigDecimal("1.20"),
				"rating", 3, "active", true, "category", dairy,
				"released", Date.from(Instant.parse("2024-05-03T10:15:30Z")));
		EObject cheese = create(productClass, "id", "p2", "name", "Cheese", "price", new BigDecimal("4.50"),
				"rating", 5, "active", true, "category", dairy,
				"color", color.getEEnumLiteral("Green"));
		cheese.eSet(productClass.getEStructuralFeature("reviews"), List.of(
				create(reviewClass, "stars", 5, "comment", "great"),
				create(reviewClass, "stars", 4, "comment", "good")));
		EObject bread = create(productClass, "id", "p3", "name", "Bread", "price", new BigDecimal("2.80"),
				"rating", 4, "active", false, "category", bakery);
		EObject salt = create(productClass, "id", "p4", "name", "Salt"); // no price → null semantics

		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		rs.getPackageRegistry().put(pkg.getNsURI(), pkg);
		Resource resource = rs.createResource(URI.createFileURI(dataDirectory.resolve("webshop-data.xmi").toString()));
		resource.getContents().addAll(List.of(dairy, bakery, milk, cheese, bread, salt));
		resource.save(null);
	}

	private EObject create(EClass type, Object... featureValuePairs) {
		EObject object = pkg.getEFactoryInstance().create(type);
		for (int i = 0; i < featureValuePairs.length; i += 2) {
			object.eSet(type.getEStructuralFeature((String) featureValuePairs[i]), featureValuePairs[i + 1]);
		}
		return object;
	}

	private QueryResult query(String filter, String orderBy, int skip, int top, boolean count) {
		return service.execute(new EntityQuery(productClass,
				filter == null ? null : parser.parseFilter(filter, productClass),
				orderBy == null ? List.of() : parser.parseOrderBy(orderBy, productClass),
				skip, top, count));
	}

	private static List<String> names(QueryResult result) {
		return result.entities().stream()
				.map(e -> String.valueOf(e.eGet(e.eClass().getEStructuralFeature("name")))).toList();
	}

	@Test
	@DisplayName("filter + orderby + paging + count")
	void filterOrderPage() {
		assertEquals(List.of("Milk", "Bread"), names(query("price lt 3.00", "price asc", 0, -1, false)));
		assertEquals(List.of("Cheese", "Bread"), names(query("price gt 1.50", "price desc", 0, -1, false)));

		QueryResult page = query(null, "name asc", 1, 2, true);
		assertEquals(List.of("Cheese", "Milk"), names(page), "Bread,Cheese,Milk,Salt → skip 1 top 2");
		assertEquals(4, page.totalCount(), "count is the total before paging");
	}

	@Test
	@DisplayName("eq/ne null on single-valued navigations (4.01 Intermediate MUST)")
	void navigationNullComparison() {
		assertEquals(List.of("Salt"), names(query("category eq null", null, 0, -1, false)),
				"only the product without a category matches");
		assertEquals(List.of("Milk", "Cheese", "Bread"),
				names(query("category ne null", null, 0, -1, false)));
		assertEquals(List.of("Salt"), names(query("null eq category", null, 0, -1, false)),
				"null on the left works too");
	}

	@Test
	@DisplayName("paths, lambdas, functions, enum and date literals evaluate")
	void richPredicates() {
		assertEquals(List.of("Milk", "Cheese"), names(query("category/name eq 'Dairy'", null, 0, -1, false)));
		assertEquals(List.of("Cheese"), names(query("reviews/any(r: r/stars ge 5)", null, 0, -1, false)));
		assertEquals(List.of("Cheese"), names(query("reviews/$count gt 1", null, 0, -1, false)));
		assertEquals(List.of("Cheese"), names(query("contains(tolower(name), 'e') and active eq true",
				null, 0, -1, false)), "Bread contains 'e' but is inactive");
		assertEquals(List.of("Cheese"), names(query("color eq webshop.Color'Green'", null, 0, -1, false)));
		assertEquals(List.of("Cheese"), names(query("color eq 'Green'", null, 0, -1, false)),
				"4.01: enum literals in URLs work WITHOUT the type prefix (13.1.2)");
		assertEquals(List.of("Milk"), names(query("released ge 2024-05-03 and released lt 2024-05-04",
				null, 0, -1, false)));
		assertEquals(List.of("Milk", "Cheese", "Bread", "Salt"),
				names(query("isof(webshop.Product)", null, 0, -1, false)));
		// [OData-URL] 5.1.1.7 substring edge cases: negative start counts from the end
		// (clamped), start beyond the end yields the empty string
		assertEquals(List.of("Milk"), names(query("substring(name,-4) eq 'Milk'", null, 0, -1, false)));
		assertEquals(List.of("Milk"), names(query("substring(name,-3,2) eq 'il'", null, 0, -1, false)));
		assertEquals(4, names(query("substring(name,-99) eq name", null, 0, -1, false)).size(),
				"negative start beyond the length clamps to the whole string");
		assertEquals(4, names(query("substring(name,99) eq ''", null, 0, -1, false)).size(),
				"start beyond the end is the empty string, not an error");
	}

	@Test
	@DisplayName("null semantics: comparisons with absent values are false, eq null matches")
	void nullSemantics() {
		assertEquals(List.of("Milk", "Bread"), names(query("price lt 3.00", null, 0, -1, false)),
				"Salt has no price — excluded, not an error");
		assertEquals(List.of("Salt"), names(query("price eq null", null, 0, -1, false)));
		assertEquals(List.of("Milk", "Cheese", "Bread"), names(query("price ne null", null, 0, -1, false)));
	}

	@Test
	@DisplayName("destructive: evaluation errors surface, never silent wrong results")
	void destructiveEvaluation() {
		// evaluation-time faults surface as the 400-mapped domain exception (not an internal 500):
		// division by zero inside the predicate
		assertThrows(ODataQueryParseException.class,
				() -> query("price div 0 gt 1", null, 0, -1, false));
		// type confusion: string function on a number
		assertThrows(ODataQueryParseException.class,
				() -> query("contains(price, '1')", null, 0, -1, false));
		// a malformed date must be a 400 whether it fails at parse or at evaluation (never a 500)
		assertThrows(ODataQueryParseException.class,
				() -> query("released ge 2024-13-99", null, 0, -1, false));
		// hand-built AST with an unknown operation name (nothing may 'default' to a value)
		OperationCallExp unknown = OclFactory.eINSTANCE.createOperationCallExp();
		unknown.setName("dropTable");
		assertThrows(ODataQueryParseException.class, () -> service.execute(
				new EntityQuery(productClass, unknown, List.of(), 0, -1, false)));
		// invalid paging is rejected at the API boundary
		assertThrows(IllegalArgumentException.class,
				() -> new EntityQuery(productClass, null, List.of(), -1, -1, false));
		assertThrows(IllegalArgumentException.class,
				() -> new EntityQuery(productClass, null, List.of(), 0, -2, false));
	}

	@Test
	@DisplayName("$apply pipeline: filter/groupby(aggregate)/alias filter over file data")
	@SuppressWarnings("unchecked")
	void applyPipeline() {
		var pipeline = parser.parseApply(
				"filter(price ne null)/groupby((category/name),aggregate(price with sum as Total,$count as Cnt))",
				productClass);
		List<Map<String, Object>> rows = service.executeApply(
				new ApplyQuery(productClass, pipeline, null, List.of(), 0, -1, false)).rows();

		assertEquals(2, rows.size());
		Map<String, Object> dairy = rows.stream()
				.filter(r -> "Dairy".equals(((Map<String, Object>) r.get("category")).get("name")))
				.findFirst().orElseThrow();
		assertEquals(0, new BigDecimal("5.70").compareTo((BigDecimal) dairy.get("Total")),
				"Milk 1.20 + Cheese 4.50");
		assertEquals(2L, dairy.get("Cnt"));

		// alias usable in a subsequent stage
		var filtered = parser.parseApply(
				"groupby((category/name),aggregate(price with sum as Total))/filter(Total gt 3.00)",
				productClass);
		assertEquals(1, service.executeApply(
				new ApplyQuery(productClass, filtered, null, List.of(), 0, -1, false)).rows().size(),
				"only Dairy exceeds 3.00");

		// post-pipeline options: row filter with alias + orderby + paging + count
		var grouped = parser.parseApply(
				"groupby((category/name),aggregate(price with sum as Total))", productClass);
		var post = service.executeApply(new ApplyQuery(productClass, grouped,
				parser.parseFilterAfterApply("Total gt 1.00", productClass, grouped),
				parser.parseOrderByAfterApply("Total desc", productClass, grouped), 0, 1, true));
		assertEquals(2, post.totalCount(), "Dairy 5.70 + Bakery 2.80 match, count before paging");
		assertEquals(1, post.rows().size(), "top 1");
		assertEquals(0, new BigDecimal("5.70").compareTo((BigDecimal) post.rows().get(0).get("Total")),
				"ordered desc → Dairy first");

		// destructive: aggregating a non-numeric property must error, never silently zero
		var broken = parser.parseApply("aggregate(name with sum as S)", productClass);
		assertThrows(IllegalArgumentException.class, () -> service.executeApply(
				new ApplyQuery(productClass, broken, null, List.of(), 0, -1, false)));
	}

	@Test
	@DisplayName("repository boundary: unknown types yield empty, directory must exist")
	void repositoryBoundaries() {
		EClass categoryClass = EcoreHelper.getEClass(pkg, "Category");
		assertTrue(service.supports(categoryClass), "categories are in the data file");
		assertEquals(2, service.execute(EntityQuery.all(categoryClass)).entities().size());

		assertThrows(IllegalArgumentException.class,
				() -> new FileEntityRepository(dataDirectory.resolve("no-such-dir"), List.of(pkg)));
	}

	private static Path findResource(String... candidatesRelative) {
		Path start = Path.of("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidatesRelative) {
				Path p = dir.resolve(rel);
				if (java.nio.file.Files.exists(p)) {
					return p;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
