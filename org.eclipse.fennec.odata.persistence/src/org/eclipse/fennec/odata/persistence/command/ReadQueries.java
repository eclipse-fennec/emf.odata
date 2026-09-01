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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.expression.ocl.OclToExpr;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;
import org.eclipse.fennec.model.expression.AliasRef;
import org.eclipse.fennec.model.expression.Comparison;
import org.eclipse.fennec.model.expression.ComparisonOperator;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.expression.ExpressionFactory;
import org.eclipse.fennec.model.expression.IsNull;
import org.eclipse.fennec.model.expression.NullLiteral;
import org.eclipse.fennec.model.expression.PropertyPath;
import org.eclipse.fennec.model.expression.Variable;
import org.eclipse.fennec.model.expression.VariableRef;
import org.eclipse.fennec.model.query.OrderBy;
import org.eclipse.fennec.model.query.QueryFactory;
import org.eclipse.fennec.model.query.SortDirection;
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
 * backends emit {@code TREAT}/discriminator logic. Dialect renaming and enum-literal
 * coercion moved upstream (persistence-jpa#92/#93). OCL constructs outside the
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
		for (OrderBy sort : orderByList(orderBy, entityType, castType)) {
			boolean ascending = sort.getDirection() == SortDirection.ASC;
			if (sort.getPath() != null) {
				EStructuralFeature[] segments = sort.getPath().getSegments()
						.toArray(EStructuralFeature[]::new);
				if (ascending) {
					builder.orderByAsc(segments);
				} else {
					builder.orderByDesc(segments);
				}
			} else if (ascending) {
				builder.orderByAsc(sort.getKey());
			} else {
				builder.orderByDesc(sort.getKey());
			}
		}
	}

	/**
	 * The same translation as {@link #applyOrderBy}, detached from the envelope — an
	 * {@code Expand} carries its own {@code OrderBy} list (ADR-0008).
	 */
	static List<OrderBy> orderByList(List<OrderBySegment> orderBy, EClass entityType,
			EClass castType) {
		List<OrderBy> sorts = new ArrayList<>(orderBy.size());
		for (OrderBySegment segment : orderBy) {
			Expression key = bridge(segment.expression(), entityType, castType);
			OrderBy sort = QueryFactory.eINSTANCE.createOrderBy();
			sort.setDirection(segment.ascending() ? SortDirection.ASC : SortDirection.DESC);
			if (key instanceof PropertyPath path && path.getBase() == null
					&& path.getCastBase() == null) {
				// keep plain paths as OrderBy.path — SORT_EXPRESSION is a scarcer capability
				sort.setPath(path);
			} else {
				sort.setKey(key);
			}
			sorts.add(sort);
		}
		return sorts;
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

	/**
	 * Bridges a ROW-LEVEL expression ([OASIS-Aggregation]: query options after
	 * {@code $apply} and post-group pipeline predicates address the transformed set):
	 * free OCL variables naming pipeline aliases bind through the bridge's scope
	 * mechanism and come back as {@link AliasRef}s; everything else translates like a
	 * regular predicate. No cast handling — rows have no entity type.
	 */
	static Expression rowExpression(OclExpression ocl, Set<String> aliases) {
		Map<org.eclipse.fennec.m2x.model.ocl.Variable, Variable> scope = new LinkedHashMap<>();
		bindAliasVariable(ocl, aliases, scope);
		ocl.eAllContents().forEachRemaining(candidate -> bindAliasVariable(candidate, aliases, scope));
		Expression bridged;
		try {
			bridged = OclToExpr.toExpr(ocl, scope);
		} catch (QueryException e) {
			throw new UnsupportedOperationException(e.getMessage(), e);
		}
		Map<Variable, String> aliasByVariable = new LinkedHashMap<>();
		scope.forEach((oclVariable, variable) -> aliasByVariable.put(variable, variable.getName()));
		bridged = rewriteAliasReferences(bridged, aliasByVariable);
		return rewriteNullComparisons(bridged);
	}

	private static void bindAliasVariable(Object candidate, Set<String> aliases,
			Map<org.eclipse.fennec.m2x.model.ocl.Variable, Variable> scope) {
		if (candidate instanceof VariableExp variableExp
				&& variableExp.getReferredVariable() instanceof org.eclipse.fennec.m2x.model.ocl.Variable referred
				&& referred.getName() != null && aliases.contains(referred.getName())
				&& !scope.containsKey(referred)) {
			Variable variable = ExpressionFactory.eINSTANCE.createVariable();
			variable.setName(referred.getName());
			scope.put(referred, variable);
		}
	}

	/** The bridge emits {@link VariableRef}s for scope-bound variables — rows need {@link AliasRef}s. */
	private static Expression rewriteAliasReferences(Expression root, Map<Variable, String> aliases) {
		if (root instanceof VariableRef ref && aliases.containsKey(ref.getVariable())) {
			return aliasRef(aliases.get(ref.getVariable()));
		}
		List<VariableRef> hits = new ArrayList<>();
		root.eAllContents().forEachRemaining(candidate -> {
			if (candidate instanceof VariableRef ref && aliases.containsKey(ref.getVariable())) {
				hits.add(ref);
			}
		});
		for (VariableRef ref : hits) {
			EcoreUtil.replace(ref, aliasRef(aliases.get(ref.getVariable())));
		}
		return root;
	}

	private static AliasRef aliasRef(String alias) {
		AliasRef ref = ExpressionFactory.eINSTANCE.createAliasRef();
		ref.setAlias(alias);
		return ref;
	}

	private static Expression bridge(OclExpression ocl, EClass entityType, EClass castType) {
		Expression bridged;
		try {
			// the bridge accepts the evaluator dialect (toLower/toUpper, persistence-jpa#92)
			// and the engines coerce string literals against enum-typed features (#93)
			bridged = OclToExpr.toExpr(ocl);
		} catch (QueryException e) {
			throw new UnsupportedOperationException(e.getMessage(), e);
		}
		bridged = rewriteNullComparisons(bridged);
		applyCastBase(bridged, entityType, castType);
		return bridged;
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
