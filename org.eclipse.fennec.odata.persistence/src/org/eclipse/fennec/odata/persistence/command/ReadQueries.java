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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.expression.ocl.OclToExpr;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.model.expression.Comparison;
import org.eclipse.fennec.model.expression.ComparisonOperator;
import org.eclipse.fennec.model.expression.EnumLiteral;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.expression.ExpressionFactory;
import org.eclipse.fennec.model.expression.IsNull;
import org.eclipse.fennec.model.expression.NullLiteral;
import org.eclipse.fennec.model.expression.PropertyPath;
import org.eclipse.fennec.model.expression.StringLiteral;
import org.eclipse.fennec.model.query.builder.Expressions;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.persistence.query.QueryException;

/**
 * Translates the OData read vocabulary into the Fennec Expression IR.
 *
 * <p>The heavy lifting is the upstream {@link OclToExpr} bridge; on top of it live
 * two OData-mandated rewrites: {@code eq null}/{@code ne null} become {@link IsNull}
 * (an IR {@code Comparison} against the null literal collapses to {@code false} on
 * every backend, SQL-style), and property paths that address derived-type features
 * after a URL cast get the cast type as {@code PropertyPath.castBase} so the
 * backends emit {@code TREAT}/discriminator logic. OCL constructs outside the
 * bridge's subset surface as {@link UnsupportedOperationException} — the servlet
 * maps that to an honest 501.
 */
final class ReadQueries {

	private ReadQueries() {
	}

	/**
	 * Bridges the parsed {@code $filter} and folds the URL cast in: the result is
	 * {@code isof(castType) [and <filter>]}, or {@code null} for an unrestricted read.
	 */
	static Expression predicate(OclExpression filter, EClass entityType, EClass castType) {
		Expression bridged = filter == null ? null : bridge(filter, entityType, castType);
		if (castType == null) {
			return bridged;
		}
		Expression cast = Expressions.isOf(castType);
		return bridged == null ? cast : Expressions.and(cast, bridged);
	}

	/** Bridges one {@code $orderby} key and appends it to the builder. */
	static void applyOrderBy(QueryBuilder builder, List<OrderBySegment> orderBy, EClass entityType,
			EClass castType) {
		for (OrderBySegment segment : orderBy) {
			Expression key = bridge(segment.expression(), entityType, castType);
			if (key instanceof PropertyPath path && path.getBase() == null
					&& path.getCastBase() == null) {
				// keep plain paths as OrderBy.path — SORT_EXPRESSION is a scarcer capability
				EStructuralFeature[] segments = path.getSegments()
						.toArray(EStructuralFeature[]::new);
				if (segment.ascending()) {
					builder.orderByAsc(segments);
				} else {
					builder.orderByDesc(segments);
				}
			} else if (segment.ascending()) {
				builder.orderByAsc(key);
			} else {
				builder.orderByDesc(key);
			}
		}
	}

	/**
	 * Resolves a slash-separated {@code $expand}/walk path against the (cast-aware)
	 * context type into its navigation chain; the first non-reference segment (an
	 * attribute or a cast segment) ends the chain.
	 */
	static List<EReference> referenceChain(EClass context, String path) {
		List<EReference> chain = new ArrayList<>();
		EClass current = context;
		for (String segment : path.split("/")) {
			if (!(current.getEStructuralFeature(segment) instanceof EReference reference)) {
				break;
			}
			chain.add(reference);
			current = reference.getEReferenceType();
		}
		return chain;
	}

	private static Expression bridge(OclExpression ocl, EClass entityType, EClass castType) {
		Expression bridged;
		try {
			bridged = OclToExpr.toExpr(normalized(ocl));
		} catch (QueryException e) {
			throw new UnsupportedOperationException(e.getMessage(), e);
		}
		bridged = rewriteNullComparisons(bridged);
		rewriteEnumComparisons(bridged);
		applyCastBase(bridged, entityType, castType);
		return bridged;
	}

	/**
	 * The OData parser speaks the evaluator dialect ({@code toLower}/{@code toUpper});
	 * the bridge expects canonical OCL. Renaming needs a copy — parsed ASTs come from
	 * a shared cache and are read-only.
	 */
	private static OclExpression normalized(OclExpression ocl) {
		boolean[] rename = { needsRename(ocl) };
		if (!rename[0]) {
			ocl.eAllContents().forEachRemaining(candidate -> rename[0] |= needsRename(candidate));
		}
		if (!rename[0]) {
			return ocl;
		}
		OclExpression copy = EcoreUtil.copy(ocl);
		renameOperation(copy);
		copy.eAllContents().forEachRemaining(ReadQueries::renameOperation);
		return copy;
	}

	private static boolean needsRename(Object candidate) {
		return candidate instanceof OperationCallExp call
				&& ("toLower".equals(call.getName()) || "toUpper".equals(call.getName()));
	}

	private static void renameOperation(Object candidate) {
		if (candidate instanceof OperationCallExp call) {
			if ("toLower".equals(call.getName())) {
				call.setName("toLowerCase");
			} else if ("toUpper".equals(call.getName())) {
				call.setName("toUpperCase");
			}
		}
	}

	/**
	 * OData transports enum values as quoted strings, so the parser yields string
	 * literals; the IR engines compare them strictly against {@code Enumerator}s and
	 * never match. Coerce string literals compared against enum-typed paths into
	 * {@link EnumLiteral}s (which the engines resolve against the target feature).
	 */
	private static void rewriteEnumComparisons(Expression root) {
		List<Comparison> comparisons = new ArrayList<>();
		if (root instanceof Comparison comparison) {
			comparisons.add(comparison);
		}
		root.eAllContents().forEachRemaining(candidate -> {
			if (candidate instanceof Comparison comparison) {
				comparisons.add(comparison);
			}
		});
		for (Comparison comparison : comparisons) {
			EEnum leftEnum = enumType(comparison.getLeft());
			EEnum rightEnum = enumType(comparison.getRight());
			if (leftEnum != null && comparison.getRight() instanceof StringLiteral literal) {
				EcoreUtil.replace(literal, enumLiteral(literal.getValue()));
			} else if (rightEnum != null && comparison.getLeft() instanceof StringLiteral literal) {
				EcoreUtil.replace(literal, enumLiteral(literal.getValue()));
			}
		}
	}

	private static EEnum enumType(Expression expression) {
		if (expression instanceof PropertyPath path && !path.getSegments().isEmpty()
				&& path.getSegments().get(path.getSegments().size() - 1) instanceof EAttribute attribute
				&& attribute.getEAttributeType() instanceof EEnum enumType) {
			return enumType;
		}
		return null;
	}

	private static EnumLiteral enumLiteral(String literalName) {
		EnumLiteral literal = ExpressionFactory.eINSTANCE.createEnumLiteral();
		literal.setLiteralName(literalName);
		return literal;
	}

	/**
	 * {@code x eq null} / {@code x ne null} must be null PROBES ([OData-URL] 5.1.1.1),
	 * but an IR comparison against the null literal collapses to {@code false} on
	 * every backend — rewrite to {@link IsNull}.
	 */
	private static Expression rewriteNullComparisons(Expression root) {
		if (root instanceof Comparison comparison && isNullComparison(comparison)) {
			return nullProbe(comparison);
		}
		List<Comparison> hits = new ArrayList<>();
		root.eAllContents().forEachRemaining(candidate -> {
			if (candidate instanceof Comparison comparison && isNullComparison(comparison)) {
				hits.add(comparison);
			}
		});
		for (Comparison comparison : hits) {
			EcoreUtil.replace(comparison, nullProbe(comparison));
		}
		return root;
	}

	private static boolean isNullComparison(Comparison comparison) {
		boolean equality = comparison.getOperator() == ComparisonOperator.EQ
				|| comparison.getOperator() == ComparisonOperator.NE;
		return equality && (comparison.getLeft() instanceof NullLiteral
				|| comparison.getRight() instanceof NullLiteral);
	}

	private static IsNull nullProbe(Comparison comparison) {
		Expression subject = comparison.getLeft() instanceof NullLiteral ? comparison.getRight()
				: comparison.getLeft();
		IsNull probe = ExpressionFactory.eINSTANCE.createIsNull();
		probe.setSource(subject); // moves the containment out of the comparison
		probe.setNegated(comparison.getOperator() == ComparisonOperator.NE);
		return probe;
	}

	/**
	 * After a URL cast the servlet parses expressions against the derived type, so
	 * paths may start with derived-only features. Backends need the cast made
	 * explicit ({@code PropertyPath.castBase} → {@code TREAT}); paths that already
	 * exist on the set's declared type stay untouched.
	 */
	private static void applyCastBase(Expression root, EClass entityType, EClass castType) {
		if (castType == null) {
			return;
		}
		castBaseFor(root, entityType, castType);
		root.eAllContents().forEachRemaining(candidate -> castBaseFor(candidate, entityType, castType));
	}

	private static void castBaseFor(Object candidate, EClass entityType, EClass castType) {
		if (candidate instanceof PropertyPath path && path.getBase() == null
				&& path.getCastBase() == null && !path.getSegments().isEmpty()) {
			EClass owner = path.getSegments().get(0).getEContainingClass();
			if (owner != null && !owner.isSuperTypeOf(entityType)) {
				path.setCastBase(castType);
			}
		}
	}

}
