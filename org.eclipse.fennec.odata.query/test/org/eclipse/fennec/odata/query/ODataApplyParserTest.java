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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;
import org.eclipse.fennec.odata.query.apply.AggregateExpression;
import org.eclipse.fennec.odata.query.apply.AggregateMethod;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.BottomTopMethod;
import org.eclipse.fennec.odata.query.apply.BottomTopTransformation;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.eclipse.fennec.odata.query.apply.ConcatTransformation;
import org.eclipse.fennec.odata.query.apply.FilterTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;
import org.eclipse.fennec.odata.query.apply.IdentityTransformation;
import org.eclipse.fennec.odata.query.apply.OrderByTransformation;
import org.eclipse.fennec.odata.query.apply.SkipTransformation;
import org.eclipse.fennec.odata.query.apply.TopTransformation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E4-AP-4 (req §3.5): {@code $apply} parses into the aggregation submodel — pipeline stages as
 * first-class objects, embedded expressions as (typed) OCL, aliases from {@code aggregate}/
 * {@code compute} referable in later stages as {@code VariableExp}s.
 */
@DisplayName("$apply → aggregation pipeline submodel")
class ODataApplyParserTest {

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
	@DisplayName("filter/groupby(aggregate)/filter pipeline with alias reference")
	void fullPipeline() {
		ApplyPipeline pipeline = parser.parseApply(
				"filter(price gt 1)"
						+ "/groupby((category/name),aggregate(price with sum as Total,$count as Cnt))"
						+ "/filter(Total gt 100)",
				productClass);

		assertEquals(3, pipeline.getTransformations().size());

		FilterTransformation pre = assertInstanceOf(FilterTransformation.class,
				pipeline.getTransformations().get(0));
		assertEquals("Boolean", pre.getPredicate().getType().getName(), "predicate is typed");

		GroupByTransformation groupBy = assertInstanceOf(GroupByTransformation.class,
				pipeline.getTransformations().get(1));
		assertEquals(1, groupBy.getGroupingProperties().size());
		PropertyCallExp grouping = assertInstanceOf(PropertyCallExp.class,
				groupBy.getGroupingProperties().get(0));
		assertEquals("name", grouping.getReferredProperty().getName());

		AggregateTransformation aggregate = assertInstanceOf(AggregateTransformation.class,
				groupBy.getThen());
		assertEquals(AggregateMethod.SUM, aggregate.getAggregations().get(0).getMethod());
		assertEquals("Total", aggregate.getAggregations().get(0).getAlias());
		assertEquals("Real", aggregate.getAggregations().get(0).getExpression().getType().getName());
		assertEquals(AggregateMethod.COUNT, aggregate.getAggregations().get(1).getMethod());
		assertEquals("Cnt", aggregate.getAggregations().get(1).getAlias());
		assertNull(aggregate.getAggregations().get(1).getExpression(), "$count has no operand");

		FilterTransformation post = assertInstanceOf(FilterTransformation.class,
				pipeline.getTransformations().get(2));
		OperationCallExp gt = assertInstanceOf(OperationCallExp.class, post.getPredicate());
		VariableExp total = assertInstanceOf(VariableExp.class, gt.getOwnedSource());
		assertEquals("Total", total.getReferredVariable().getName(),
				"aggregate alias is referable in later stages");
	}

	@Test
	@DisplayName("compute introduces referable aliases")
	void compute() {
		ApplyPipeline pipeline = parser.parseApply(
				"compute(price mul 2 as DoublePrice)/filter(DoublePrice lt 50)", productClass);

		ComputeTransformation compute = assertInstanceOf(ComputeTransformation.class,
				pipeline.getTransformations().get(0));
		assertEquals("DoublePrice", compute.getComputeExpressions().get(0).getAlias());
		assertEquals("Real", compute.getComputeExpressions().get(0).getExpression().getType().getName());

		FilterTransformation filter = assertInstanceOf(FilterTransformation.class,
				pipeline.getTransformations().get(1));
		assertInstanceOf(VariableExp.class,
				((OperationCallExp) filter.getPredicate()).getOwnedSource());
	}

	@Test
	@DisplayName("rejects unknown transformations and aggregate methods")
	void rejects() {
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseApply("frobnicate(price gt 1)", productClass));
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseApply("aggregate(price with median as M)", productClass));
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseApply("filter(Total gt 1)", productClass),
				"alias unknown without a preceding aggregate");
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseApply("top(-1)", productClass),
				"top/skip need a non-negative integer");
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseApply("groupby((rollup(category/name)))", productClass),
				"a single rollup PATH is neither the named nor the unnamed hierarchy form");
		assertThrows(UnsupportedOperationException.class,
				() -> parser.parseApply(
						"groupby((name),filter(price gt 1)/aggregate(price with sum as T))",
						productClass),
				"multi-stage nested groupby pipelines are syntactically valid but 501");
	}

	@Test
	@DisplayName("preserving transformations: bottom/top, concat, top/skip, orderby, identity")
	void preservingTransformations() {
		ApplyPipeline pipeline = parser.parseApply(
				"topcount(2,price)/identity/top(5)/skip(1)/orderby(price desc,name)", productClass);
		assertEquals(5, pipeline.getTransformations().size());

		BottomTopTransformation topCount = assertInstanceOf(BottomTopTransformation.class,
				pipeline.getTransformations().get(0));
		assertEquals(BottomTopMethod.TOP_COUNT, topCount.getMethod());
		assertEquals("Integer", topCount.getThreshold().getType().getName());
		assertInstanceOf(PropertyCallExp.class, topCount.getValue());

		assertInstanceOf(IdentityTransformation.class, pipeline.getTransformations().get(1));
		assertEquals(5, assertInstanceOf(TopTransformation.class,
				pipeline.getTransformations().get(2)).getCount());
		assertEquals(1, assertInstanceOf(SkipTransformation.class,
				pipeline.getTransformations().get(3)).getCount());

		OrderByTransformation orderBy = assertInstanceOf(OrderByTransformation.class,
				pipeline.getTransformations().get(4));
		assertEquals(2, orderBy.getItems().size());
		assertEquals(false, orderBy.getItems().get(0).isAscending());
		assertEquals(true, orderBy.getItems().get(1).isAscending());

		ConcatTransformation concat = assertInstanceOf(ConcatTransformation.class,
				parser.parseApply("concat(topcount(1,price),bottomcount(1,price)/identity)",
						productClass).getTransformations().get(0));
		assertEquals(2, concat.getPipelines().size());
		assertEquals(2, concat.getPipelines().get(1).getTransformations().size());
	}

	@Test
	@DisplayName("rollup grouping elements: unnamed levels and named hierarchies")
	void rollup() {
		GroupByTransformation groupBy = assertInstanceOf(GroupByTransformation.class,
				parser.parseApply(
						"groupby((rollup(category/name,name),active),aggregate(price with sum as T))",
						productClass).getTransformations().get(0));
		assertEquals(1, groupBy.getGroupingProperties().size(), "active stays a plain property");
		assertEquals(1, groupBy.getRollups().size());
		assertEquals(2, groupBy.getRollups().get(0).getLevels().size());
		assertNull(groupBy.getRollups().get(0).getHierarchy());

		GroupByTransformation named = assertInstanceOf(GroupByTransformation.class,
				parser.parseApply("groupby((rollup(ProductHierarchy)))", productClass)
						.getTransformations().get(0));
		assertEquals("ProductHierarchy", named.getRollups().get(0).getHierarchy(),
				"ONE simple identifier is a named hierarchy qualifier, not a property");
		assertEquals(0, named.getRollups().get(0).getLevels().size());
	}

	@Test
	@DisplayName("aggregate: from clauses, custom methods, custom aggregates, path/$count")
	void aggregateExtensions() {
		AggregateExpression from = firstAggregation(
				"aggregate(price with sum from category/name with average as A)");
		assertEquals(AggregateMethod.SUM, from.getMethod());
		assertEquals("A", from.getAlias());
		assertEquals(1, from.getFrom().size());
		assertEquals(AggregateMethod.AVERAGE, from.getFrom().get(0).getMethod());
		assertEquals(1, from.getFrom().get(0).getGroupingProperties().size());

		AggregateExpression custom = firstAggregation("aggregate(price with Custom.median as M)");
		assertEquals(AggregateMethod.CUSTOM, custom.getMethod());
		assertEquals("Custom.median", custom.getCustomMethod());

		AggregateExpression customAggregate = firstAggregation("aggregate(Forecast as F)");
		assertEquals(AggregateMethod.CUSTOM_AGGREGATE, customAggregate.getMethod());
		assertEquals("Forecast", customAggregate.getCustomMethod());
		assertNull(customAggregate.getExpression(), "custom aggregates are deliberately unresolved");

		AggregateExpression pathCount = firstAggregation("aggregate(reviews/$count as ReviewCount)");
		assertEquals(AggregateMethod.COUNT, pathCount.getMethod());
		assertInstanceOf(OperationCallExp.class, pathCount.getExpression(),
				"path/$count keeps the size expression as operand");

		// custom aggregate 'from' without a method (ABNF customFrom) parses too
		AggregateExpression customFrom = firstAggregation("aggregate(Forecast from category/name as F)");
		assertEquals(1, customFrom.getFrom().size());
		assertEquals(false, customFrom.getFrom().get(0).isSetMethod());
	}

	@Test
	@DisplayName("bound operations in member paths resolve against EOperations (AP-10)")
	void boundOperationsInPaths() {
		ODataQueryParser parser = new ODataQueryParser();
		// single-valued result → property navigation continues after the call
		OperationCallExp gt = assertInstanceOf(OperationCallExp.class,
				parser.parseFilter("webshop.bestReview()/stars gt 3", productClass));
		PropertyCallExp stars = assertInstanceOf(PropertyCallExp.class, gt.getOwnedSource());
		OperationCallExp call = assertInstanceOf(OperationCallExp.class, stars.getOwnedSource());
		assertEquals("webshop.bestReview", call.getName(),
				"the qualified name travels for backend dispatch");
		assertEquals(true, call.isIsImplicit(), "first segment binds to the implicit instance");

		// collection-valued result: named argument, /$count and lambda tails work
		OperationCallExp size = assertInstanceOf(OperationCallExp.class, ((OperationCallExp)
				parser.parseFilter("webshop.topReviews(count=2)/$count gt 1", productClass))
				.getOwnedSource());
		assertEquals("size", size.getName());
		assertEquals(1, ((OperationCallExp) size.getOwnedSource()).getOwnedArguments().size());
		parser.parseFilter("webshop.topReviews(count=2)/any(r: r/stars gt 3)", productClass);
		parser.parseFilter("webshop.topReviews(2)/$count gt 1", productClass);

		// unknown operation / wrong parameters are client errors
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("webshop.noSuchOp()/stars gt 3", productClass));
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("webshop.topReviews(limit=2)/$count gt 1", productClass),
				"unknown parameter name");
		assertThrows(ODataQueryParseException.class,
				() -> parser.parseFilter("webshop.topReviews()/$count gt 1", productClass),
				"missing parameter");
	}

	private AggregateExpression firstAggregation(String apply) {
		return assertInstanceOf(AggregateTransformation.class,
				parser.parseApply(apply, productClass).getTransformations().get(0))
				.getAggregations().get(0);
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
