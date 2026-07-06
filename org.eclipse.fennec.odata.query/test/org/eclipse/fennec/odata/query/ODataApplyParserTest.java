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
import org.eclipse.fennec.odata.query.apply.AggregateMethod;
import org.eclipse.fennec.odata.query.apply.AggregateTransformation;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.eclipse.fennec.odata.query.apply.FilterTransformation;
import org.eclipse.fennec.odata.query.apply.GroupByTransformation;
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
