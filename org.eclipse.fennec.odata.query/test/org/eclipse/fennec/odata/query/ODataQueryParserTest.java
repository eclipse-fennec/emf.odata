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
package org.eclipse.fennec.odata.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.m2x.model.ocl.BooleanLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.ClassifierType;
import org.eclipse.fennec.m2x.model.ocl.CollectionLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.IntegerLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;
import org.eclipse.fennec.m2x.model.ocl.IteratorExp;
import org.eclipse.fennec.m2x.model.ocl.EnumLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.RealLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.TypeExp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E4 vertical slice: {@code $filter}/{@code $orderby} → own ANTLR4 grammar → m2x OCL AST
 * (req §3.5/§3.6). Asserts the mapping table shapes: comparison → {@code OperationCallExp('=')}
 * over {@code PropertyCallExp}, logical fold, function name mapping, {@code in} → Set literal
 * {@code includes}, path navigation with eager property resolution, and parse failures for
 * bad syntax / unknown properties / unknown functions.
 */
@DisplayName("$filter/$orderby → OCL predicate IR")
class ODataQueryParserTest {

	private EcoreHelper ecoreHelper;
	private EClass productClass;

	private final ODataQueryParser parser = new ODataQueryParser();

	@BeforeEach
	void setUp() throws Exception {
		Path ecore = findResource("testdata/webshop.ecore",
				"org.eclipse.fennec.odata.query/testdata/webshop.ecore");
		ecoreHelper = new EcoreHelper();
		EPackage pkg = ecoreHelper.loadEcore(ecore);
		productClass = EcoreHelper.getEClass(pkg, "Product");
	}

	@AfterEach
	void tearDown() {
		ecoreHelper.releaseAll();
	}

	@Test
	@DisplayName("comparison: name eq 'Milk' → OperationCallExp('=') over PropertyCallExp")
	void simpleComparison() {
		OclExpression exp = parser.parseFilter("name eq 'Milk'", productClass);

		OperationCallExp eq = assertInstanceOf(OperationCallExp.class, exp);
		assertEquals("=", eq.getName());
		PropertyCallExp property = assertInstanceOf(PropertyCallExp.class, eq.getOwnedSource());
		assertSame(productClass.getEStructuralFeature("name"), property.getReferredProperty());
		assertTrue(property.isIsImplicit(), "root segment sources the implicit self");
		StringLiteralExp literal = assertInstanceOf(StringLiteralExp.class, eq.getOwnedArguments().get(0));
		assertEquals("Milk", literal.getStringSymbol());
	}

	@Test
	@DisplayName("logical fold + precedence: A and B or C parses as (A and B) or C")
	void logicalPrecedence() {
		OperationCallExp or = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("active eq true and rating ge 4 or price lt 2.5", productClass));
		assertEquals("or", or.getName());
		OperationCallExp and = assertInstanceOf(OperationCallExp.class, or.getOwnedSource());
		assertEquals("and", and.getName());
		OperationCallExp lt = assertInstanceOf(OperationCallExp.class, or.getOwnedArguments().get(0));
		assertEquals("<", lt.getName());
		assertInstanceOf(RealLiteralExp.class, lt.getOwnedArguments().get(0));

		OperationCallExp eq = assertInstanceOf(OperationCallExp.class, and.getOwnedSource());
		assertInstanceOf(BooleanLiteralExp.class, eq.getOwnedArguments().get(0));
		OperationCallExp ge = assertInstanceOf(OperationCallExp.class, and.getOwnedArguments().get(0));
		assertEquals(">=", ge.getName());
		assertEquals(4L, ((IntegerLiteralExp) ge.getOwnedArguments().get(0)).getIntegerSymbol());
	}

	@Test
	@DisplayName("not + parens: not (price gt 10)")
	void notExpression() {
		OperationCallExp not = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("not (price gt 10)", productClass));
		assertEquals("not", not.getName());
		assertEquals(">", ((OperationCallExp) not.getOwnedSource()).getName());
		assertTrue(not.getOwnedArguments().isEmpty(), "not is unary");
	}

	@Test
	@DisplayName("functions map to OCL-stdlib names: contains, tolower→toLower, length→size")
	void functionMapping() {
		OperationCallExp contains = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("contains(tolower(name), 'milk')", productClass));
		assertEquals("contains", contains.getName());
		OperationCallExp toLower = assertInstanceOf(OperationCallExp.class, contains.getOwnedSource());
		assertEquals("toLower", toLower.getName());
		assertInstanceOf(PropertyCallExp.class, toLower.getOwnedSource());

		OperationCallExp size = assertInstanceOf(OperationCallExp.class,
				((OperationCallExp) parser.parseFilter("length(name) gt 3", productClass)).getOwnedSource());
		assertEquals("size", size.getName());
	}

	@Test
	@DisplayName("in → Set{...}->includes(property)")
	void inList() {
		OperationCallExp includes = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("name in ('Milk', 'Cheese')", productClass));
		assertEquals("includes", includes.getName());
		CollectionLiteralExp set = assertInstanceOf(CollectionLiteralExp.class, includes.getOwnedSource());
		assertEquals(2, set.getOwnedParts().size());
		assertInstanceOf(PropertyCallExp.class, includes.getOwnedArguments().get(0));
	}

	@Test
	@DisplayName("in with empty list (current TC listExpr) → Set{}->includes(property)")
	void inEmptyList() {
		OperationCallExp includes = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("name in ()", productClass));
		assertEquals("includes", includes.getName());
		CollectionLiteralExp set = assertInstanceOf(CollectionLiteralExp.class, includes.getOwnedSource());
		assertEquals(0, set.getOwnedParts().size());
		assertInstanceOf(PropertyCallExp.class, includes.getOwnedArguments().get(0));
	}

	@Test
	@DisplayName("parameter alias: @name expands to its expression (4.01 11.2.5.1.3)")
	void parameterAlias() {
		OperationCallExp eq = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("name eq @wanted", productClass, Map.of("@wanted", "'Milk'")));
		assertEquals("=", eq.getName());
		assertEquals("Milk", assertInstanceOf(StringLiteralExp.class,
				eq.getOwnedArguments().get(0)).getStringSymbol());
	}

	@Test
	@DisplayName("parameter alias: values may reference aliases, cycles are capped")
	void parameterAliasNestingAndCycles() {
		OperationCallExp gt = assertInstanceOf(OperationCallExp.class, parser.parseFilter(
				"price gt @limit", productClass, Map.of("@limit", "@base add 1", "@base", "2")));
		assertEquals(">", gt.getName());
		assertThrows(ODataQueryParseException.class, () -> parser.parseFilter(
				"price gt @self", productClass, Map.of("@self", "@self")), "alias cycle");
	}

	@Test
	@DisplayName("parameter alias: unresolved @name is a client error, not a property")
	void parameterAliasUnresolved() {
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("name eq @missing", productClass));
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("name eq @missing", productClass, Map.of("@other", "1")));
	}

	@Test
	@DisplayName("divby (4.01 decimal division) → OCL '/'")
	void divby() {
		OperationCallExp eq = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("price divby 2 eq 5", productClass));
		OperationCallExp division = assertInstanceOf(OperationCallExp.class, eq.getOwnedSource());
		assertEquals("/", division.getName());
	}

	@Test
	@DisplayName("path navigation: category/name resolves both segments")
	void pathNavigation() {
		OperationCallExp eq = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("category/name eq 'Dairy'", productClass));
		PropertyCallExp name = assertInstanceOf(PropertyCallExp.class, eq.getOwnedSource());
		assertEquals("name", name.getReferredProperty().getName());
		PropertyCallExp category = assertInstanceOf(PropertyCallExp.class, name.getOwnedSource());
		assertSame(productClass.getEStructuralFeature("category"), category.getReferredProperty());
	}

	@Test
	@DisplayName("arithmetic: price mul 2 lt 10")
	void arithmetic() {
		OperationCallExp lt = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("price mul 2 lt 10", productClass));
		OperationCallExp mul = assertInstanceOf(OperationCallExp.class, lt.getOwnedSource());
		assertEquals("*", mul.getName());
	}

	@Test
	@DisplayName("$orderby: 'price desc,name' → two segments with directions")
	void orderBy() {
		List<OrderBySegment> segments = parser.parseOrderBy("price desc,name", productClass);
		assertEquals(2, segments.size());
		assertFalse(segments.get(0).ascending());
		assertEquals("price",
				((PropertyCallExp) segments.get(0).expression()).getReferredProperty().getName());
		assertTrue(segments.get(1).ascending(), "OData default direction is ascending");
	}

	@Test
	@DisplayName("null literal and string escaping")
	void literals() {
		OperationCallExp ne = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("name ne null", productClass));
		assertEquals("<>", ne.getName());
		assertEquals("OclVoid", ne.getOwnedArguments().get(0).getType().getName());

		OperationCallExp eq = (OperationCallExp) parser.parseFilter("name eq 'O''Brien'", productClass);
		assertEquals("O'Brien", ((StringLiteralExp) eq.getOwnedArguments().get(0)).getStringSymbol());
	}

	@Test
	@DisplayName("type resolution (ADR-0004): Boolean root, String/Real/classifier property types")
	void typeResolution() {
		OclExpression filter = parser.parseFilter(
				"contains(tolower(name), 'milk') and price mul 2 lt 10 or category/name eq 'Dairy'", productClass);
		assertEquals("Boolean", filter.getType().getName(), "a $filter is boolean-typed");

		OperationCallExp eq = (OperationCallExp) parser.parseFilter("name eq 'Milk'", productClass);
		assertEquals("String", eq.getOwnedSource().getType().getName(), "EString property → String");
		assertEquals("String", eq.getOwnedArguments().get(0).getType().getName());

		OperationCallExp lt = (OperationCallExp) parser.parseFilter("price mul 2 lt 10", productClass);
		OperationCallExp mul = (OperationCallExp) lt.getOwnedSource();
		assertEquals("Real", mul.getType().getName(), "EBigDecimal operand widens the product to Real");

		OperationCallExp catEq = (OperationCallExp) parser.parseFilter("category/name eq 'x'", productClass);
		PropertyCallExp name = (PropertyCallExp) catEq.getOwnedSource();
		ClassifierType category = assertInstanceOf(ClassifierType.class, name.getOwnedSource().getType());
		assertEquals("Category", category.getReferredClassifier().getName(), "reference → ClassifierType");

		OperationCallExp size = (OperationCallExp) ((OperationCallExp) parser
				.parseFilter("length(name) gt 3", productClass)).getOwnedSource();
		assertEquals("Integer", size.getType().getName(), "size → Integer");

		OperationCallExp ne = (OperationCallExp) parser.parseFilter("name ne null", productClass);
		assertEquals("OclVoid", ne.getOwnedArguments().get(0).getType().getName());

		List<OrderBySegment> orderBy = parser.parseOrderBy("price desc", productClass);
		assertEquals("Real", orderBy.get(0).expression().getType().getName(), "$orderby segments are typed too");
	}

	@Test
	@DisplayName("lambdas (E4-AP-1): any→exists, all→forAll, any()→notEmpty, variable scope")
	void lambdas() {
		IteratorExp exists = assertInstanceOf(IteratorExp.class,
				parser.parseFilter("reviews/any(r: r/stars ge 4)", productClass));
		assertEquals("exists", exists.getName());
		assertEquals("Boolean", exists.getType().getName());
		assertEquals("r", exists.getOwnedIterators().get(0).getName());
		OperationCallExp ge = assertInstanceOf(OperationCallExp.class, exists.getOwnedBody());
		PropertyCallExp stars = assertInstanceOf(PropertyCallExp.class, ge.getOwnedSource());
		assertEquals("stars", stars.getReferredProperty().getName());
		VariableExp lambdaVar = assertInstanceOf(VariableExp.class, stars.getOwnedSource());
		assertSame(exists.getOwnedIterators().get(0), lambdaVar.getReferredVariable());

		IteratorExp forAll = assertInstanceOf(IteratorExp.class,
				parser.parseFilter("tags/all(t: t ne 'internal')", productClass));
		assertEquals("forAll", forAll.getName());

		OperationCallExp notEmpty = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("tags/any()", productClass));
		assertEquals("notEmpty", notEmpty.getName());
		assertEquals("Boolean", notEmpty.getType().getName());

		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("name/any(x: true)", productClass), "single-valued path");
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("tags/all()", productClass), "all requires a lambda");
	}

	@Test
	@DisplayName("typed literals (E4-AP-3): date, dateTimeOffset, guid, duration, enum")
	void typedLiterals() {
		OclExpression date = ((OperationCallExp) parser
				.parseFilter("released ge 2024-05-03", productClass)).getOwnedArguments().get(0);
		assertEquals("Date", date.getType().getName());
		assertEquals("2024-05-03", ((StringLiteralExp) date).getStringSymbol());

		assertEquals("DateTimeOffset", ((OperationCallExp) parser
				.parseFilter("released lt 2024-05-03T10:15:30Z", productClass))
				.getOwnedArguments().get(0).getType().getName());

		assertEquals("Guid", ((OperationCallExp) parser
				.parseFilter("id eq 0050568D-393C-1ED4-9D97-E65F0F3FCC23", productClass))
				.getOwnedArguments().get(0).getType().getName());

		OclExpression duration = ((OperationCallExp) parser
				.parseFilter("name eq duration'P1DT2H'", productClass)).getOwnedArguments().get(0);
		assertEquals("Duration", duration.getType().getName());
		assertEquals("P1DT2H", ((StringLiteralExp) duration).getStringSymbol());

		EnumLiteralExp color = assertInstanceOf(EnumLiteralExp.class, ((OperationCallExp) parser
				.parseFilter("color eq webshop.Color'Green'", productClass)).getOwnedArguments().get(0));
		assertEquals(1, color.getReferredLiteral().getValue());

		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("color eq webshop.Color'Cyan'", productClass), "unknown enum literal");
		// flag combinations become a Set literal of the member literals
		OperationCallExp has = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("color has webshop.Color'Red,Green'", productClass));
		CollectionLiteralExp flags = assertInstanceOf(CollectionLiteralExp.class,
				has.getOwnedArguments().get(0));
		assertEquals(2, flags.getOwnedParts().size());
	}

	@Test
	@DisplayName("wave 1: cast path segments, filtered $count, unary minus, NaN/INF, binary")
	void waveOneConstructs() {
		// derived-type cast segment in an expression path → oclAsType + continued navigation
		OperationCallExp gt = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("webshop.DiscountedProduct/discount gt 5", productClass));
		PropertyCallExp discount = assertInstanceOf(PropertyCallExp.class, gt.getOwnedSource());
		OperationCallExp cast = assertInstanceOf(OperationCallExp.class, discount.getOwnedSource());
		assertEquals("oclAsType", cast.getName());
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("webshop.Category/name eq 'x'", productClass),
				"a cast to an UNRELATED type is a client error");

		// filtered $count → size over select, body against the ELEMENT type
		OperationCallExp size = assertInstanceOf(OperationCallExp.class,
				((OperationCallExp) parser.parseFilter(
						"reviews/$count($filter=stars ge 4) gt 0", productClass)).getOwnedSource());
		assertEquals("size", size.getName());
		IteratorExp select = assertInstanceOf(IteratorExp.class, size.getOwnedSource());
		assertEquals("select", select.getName());
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("tags/$count($filter=stars ge 4) gt 0", productClass),
				"filtered $count needs a structured collection");

		// unary minus, NaN/INF, binary
		OperationCallExp negated = assertInstanceOf(OperationCallExp.class,
				((OperationCallExp) parser.parseFilter("-rating lt 0", productClass)).getOwnedSource());
		assertEquals("-", negated.getName());
		assertEquals(0, negated.getOwnedArguments().size(), "unary form carries no argument");
		parser.parseFilter("price ne NaN", productClass);
		parser.parseFilter("price lt INF and price gt -INF", productClass);
		assertEquals("Binary", ((StringLiteralExp) ((OperationCallExp) parser
				.parseFilter("name ne binary'T0RhdGE'", productClass))
				.getOwnedArguments().get(0)).getType().getName());
	}

	@Test
	@DisplayName("type operators (E4-AP-2): isof→oclIsKindOf, cast→oclAsType + TypeExp")
	void typeOperators() {
		OperationCallExp isof = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("isof(category, webshop.Category)", productClass));
		assertEquals("oclIsKindOf", isof.getName());
		assertEquals("Boolean", isof.getType().getName());
		TypeExp typeArg = assertInstanceOf(TypeExp.class, isof.getOwnedArguments().get(0));
		assertEquals("Category",
				((ClassifierType) typeArg.getReferredType()).getReferredClassifier().getName());

		OperationCallExp cast = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("cast(price, Edm.String)", productClass));
		assertEquals("oclAsType", cast.getName());
		assertEquals("String", cast.getType().getName(), "cast result carries the target type");

		OperationCallExp unbound = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("isof(webshop.Product)", productClass));
		assertTrue(unbound.isIsImplicit(), "unbound form tests the implicit instance");
		assertNull(unbound.getOwnedSource());

		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("isof(category, webshop.NoSuchType)", productClass));
	}

	@Test
	@DisplayName("$count segment (E4-AP-8): reviews/$count gt 2 → size, Integer-typed")
	void countSegment() {
		OperationCallExp gt = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("reviews/$count gt 2", productClass));
		OperationCallExp size = assertInstanceOf(OperationCallExp.class, gt.getOwnedSource());
		assertEquals("size", size.getName());
		assertEquals("Integer", size.getType().getName());
		assertEquals("reviews",
				((PropertyCallExp) size.getOwnedSource()).getReferredProperty().getName());

		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("name/$count gt 0", productClass), "single-valued path");
	}

	@Test
	@DisplayName("rejects: bad syntax, unknown property, unknown function")
	void parseFailures() {
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("name eq", productClass));
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("nosuchproperty eq 1", productClass));
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("frobnicate(name) eq 1", productClass));
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("", productClass));
	}

	private static Path findResource(String... candidatesRelative) {
		Path start = Paths.get("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidatesRelative) {
				Path p = dir.resolve(rel);
				if (Files.exists(p)) {
					return p;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
