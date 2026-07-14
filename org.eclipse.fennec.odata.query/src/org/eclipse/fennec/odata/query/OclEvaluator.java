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

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.m2x.model.ocl.BooleanLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.ClassifierType;
import org.eclipse.fennec.m2x.model.ocl.CollectionItem;
import org.eclipse.fennec.m2x.model.ocl.CollectionLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.EnumLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.IntegerLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.IteratorExp;
import org.eclipse.fennec.m2x.model.ocl.NullLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.RealLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.TypeExp;
import org.eclipse.fennec.m2x.model.ocl.Variable;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;

/**
 * The in-memory "translator" (req §3.5): instead of translating the OCL predicate IR into a
 * native query language, it evaluates the AST directly against an {@link EObject}. Covers the
 * operation names the {@code ODataToOclBuilder} emits (comparison/logic/arithmetic, string and
 * date functions, {@code includes}, {@code exists}/{@code forAll}, {@code oclIsKindOf}/
 * {@code oclAsType}, {@code size}, {@code notEmpty}, {@code has}).
 *
 * <p>Semantics deliberately simple for v1: null comparisons are false (except {@code eq}/
 * {@code ne} against the null literal), logic treats null as false, numeric comparison happens
 * on {@link BigDecimal}. Contexts are EObjects or $apply row maps.
 * Unknown operations and unresolvable variables raise {@link IllegalArgumentException},
 * never silently evaluate to a value — wrong results are worse than errors.
 */
public class OclEvaluator {

	/**
	 * Evaluates a boolean predicate against the given context (an EObject or a $apply row map).
	 * Null-valued relational comparisons yield {@code false} (three-valued logic collapsed), and
	 * evaluation-time type errors from a query that is invalid for the actual data are surfaced as
	 * {@link ODataQueryParseException} (→ 400), never as an internal fault (→ 500).
	 */
	public boolean matches(OclExpression predicate, Object self) {
		try {
			return Boolean.TRUE.equals(evaluate(predicate, self, Map.of()));
		} catch (NullComparison e) {
			return false;
		} catch (RuntimeException e) {
			throw asClientError(e);
		}
	}

	/** Evaluates any expression against the given context (for $orderby keys, aggregates etc.). */
	public Object evaluate(OclExpression expression, Object self) {
		try {
			return evaluate(expression, self, Map.of());
		} catch (NullComparison e) {
			return null;
		} catch (RuntimeException e) {
			throw asClientError(e);
		}
	}

	/**
	 * Translates an evaluation fault into a client-facing 400: a type/format mismatch means the
	 * query is invalid for the data (e.g. {@code contains} on a numeric property, an out-of-range
	 * literal). Genuine internal faults are left untouched so they still surface as a 500.
	 */
	private static RuntimeException asClientError(RuntimeException e) {
		if (e instanceof ODataQueryParseException parse) {
			return parse;
		}
		if (e instanceof IllegalArgumentException || e instanceof ClassCastException
				|| e instanceof ArithmeticException || e instanceof DateTimeException) {
			return new ODataQueryParseException(
					"the $filter/$orderby expression is not valid for the data: " + e.getMessage(), e);
		}
		return e;
	}

	private Object evaluate(OclExpression exp, Object self, Map<Variable, Object> bindings) {
		return switch (exp) {
			case null -> null;
			case StringLiteralExp s -> s.getStringSymbol();
			case IntegerLiteralExp i -> i.getIntegerSymbol();
			case RealLiteralExp r -> r.getRealSymbol();
			case BooleanLiteralExp b -> b.isBooleanSymbol();
			case NullLiteralExp n -> null;
			case EnumLiteralExp e -> e.getReferredLiteral();
			case TypeExp t -> t.getReferredType();
			case CollectionLiteralExp c -> c.getOwnedParts().stream()
					.filter(CollectionItem.class::isInstance).map(CollectionItem.class::cast)
					.map(item -> evaluate(item.getOwnedItem(), self, bindings)).toList();
			case VariableExp v -> {
				Variable variable = v.getReferredVariable();
				if (variable != null && bindings.containsKey(variable)) {
					yield bindings.get(variable);
				}
				// $apply alias: later pipeline stages evaluate against row maps carrying the alias
				if (variable != null && self instanceof Map<?, ?> row && row.containsKey(variable.getName())) {
					yield row.get(variable.getName());
				}
				throw new IllegalArgumentException("unresolvable variable '"
						+ (variable == null ? "?" : variable.getName()) + "'");
			}
			case PropertyCallExp p -> {
				EStructuralFeature property = p.getReferredProperty();
				if (property == null) {
					throw new IllegalArgumentException("property reference is unresolved");
				}
				Object source = p.getOwnedSource() == null ? self : evaluate(p.getOwnedSource(), self, bindings);
				if (source == null) {
					yield null; // null propagation along the path
				}
				if (source instanceof EObject eObject) {
					yield eObject.eGet(property);
				}
				if (source instanceof Map<?, ?> row) { // $apply row: grouping paths are nested maps
					yield row.get(property.getName());
				}
				throw new IllegalArgumentException(
						"property '" + property.getName() + "' called on a non-object value");
			}
			case IteratorExp it -> iterate(it, self, bindings);
			case OperationCallExp op -> operation(op, self, bindings);
			default -> throw new IllegalArgumentException(
					"unsupported expression kind " + exp.eClass().getName());
		};
	}

	private Object iterate(IteratorExp it, Object self, Map<Variable, Object> bindings) {
		Object source = evaluate(it.getOwnedSource(), self, bindings);
		Collection<?> elements = asCollection(source);
		Variable variable = it.getOwnedIterators().get(0);
		if ("select".equals(it.getName())) { // filtered $count: keep matching elements
			List<Object> selected = new ArrayList<>();
			for (Object element : elements) {
				Map<Variable, Object> inner = new HashMap<>(bindings);
				inner.put(variable, element);
				try {
					if (Boolean.TRUE.equals(evaluate(it.getOwnedBody(), self, inner))) {
						selected.add(element);
					}
				} catch (NullComparison unknown) {
					// 3VL: an unknown predicate excludes the element, never the whole query
				}
			}
			return selected;
		}
		boolean forAll = "forAll".equals(it.getName());
		if (!forAll && !"exists".equals(it.getName())) {
			throw new IllegalArgumentException("unsupported iterator '" + it.getName() + "'");
		}
		for (Object element : elements) {
			Map<Variable, Object> inner = new HashMap<>(bindings);
			inner.put(variable, element);
			boolean matches = Boolean.TRUE.equals(evaluate(it.getOwnedBody(), self, inner));
			if (forAll && !matches) {
				return false;
			}
			if (!forAll && matches) {
				return true;
			}
		}
		return forAll;
	}

	private Object operation(OperationCallExp op, Object self, Map<Variable, Object> bindings) {
		String name = op.getName();
		List<OclExpression> args = op.getOwnedArguments();

		// logic short-circuits before evaluating the other side
		switch (name) {
			case "and" -> {
				return Boolean.TRUE.equals(evaluate(op.getOwnedSource(), self, bindings))
						&& Boolean.TRUE.equals(evaluate(args.get(0), self, bindings));
			}
			case "or" -> {
				return Boolean.TRUE.equals(evaluate(op.getOwnedSource(), self, bindings))
						|| Boolean.TRUE.equals(evaluate(args.get(0), self, bindings));
			}
			case "not" -> {
				return !Boolean.TRUE.equals(evaluate(op.getOwnedSource(), self, bindings));
			}
			case "oclIsKindOf", "oclAsType" -> {
				// unbound cast(T)/isof(T) has no source and tests the instance itself
				Object value = op.getOwnedSource() == null ? self
						: evaluate(op.getOwnedSource(), self, bindings);
				return typeOperation(name, value, args);
			}
			case "$it" -> {
				// bare instance reference ([OData-URL] 5.1.1.13.1): the evaluated row/item
				// itself — e.g. a primitive collection item in a nested $select filter
				return self;
			}
			default -> { /* fall through to value-based dispatch */ }
		}

		Object source = evaluate(op.getOwnedSource(), self, bindings);
		// three-valued logic: any function/operator applied to a null operand is UNKNOWN, which
		// collapses to a false predicate (row excluded) — matching SQL and the JPA pushdown, rather
		// than erroring or spuriously succeeding. eq/ne handle the null LITERAL separately (the only
		// defined null test); relational comparisons signal unknown inside comparison().
		boolean equality = "=".equals(name) || "<>".equals(name) || "has".equals(name);
		if (op.getOwnedSource() != null && source == null && !equality) {
			throw new NullComparison(); // a present operand that resolved to null → unknown
		}
		return switch (name) {
			case "=" -> equalTo3vl(op, source, evaluate(args.get(0), self, bindings));
			case "<>" -> !equalTo3vl(op, source, evaluate(args.get(0), self, bindings));
			case "<" -> comparison(source, evaluate(args.get(0), self, bindings)) < 0;
			case "<=" -> comparison(source, evaluate(args.get(0), self, bindings)) <= 0;
			case ">" -> comparison(source, evaluate(args.get(0), self, bindings)) > 0;
			case ">=" -> comparison(source, evaluate(args.get(0), self, bindings)) >= 0;
			case "has" -> {
				Object flags = evaluate(args.get(0), self, bindings);
				if (flags instanceof Collection<?>) {
					// EMF enums carry ONE literal — a multi-flag test cannot be answered here
					throw new IllegalArgumentException(
							"enum flag combinations are not evaluable on single-literal enums");
				}
				yield equalTo3vl(op, source, flags); // single-flag semantics
			}
			case "+" -> arithmetic(name, source, evaluate(args.get(0), self, bindings));
			case "-" -> args.isEmpty() ? negate(source)
					: arithmetic(name, source, evaluate(args.get(0), self, bindings));
			case "*" -> arithmetic(name, source, evaluate(args.get(0), self, bindings));
			case "/" -> arithmetic(name, source, evaluate(args.get(0), self, bindings));
			case "mod" -> arithmetic(name, source, evaluate(args.get(0), self, bindings));
			case "includes" -> asCollection(source).stream()
					.anyMatch(member -> equalTo(member, evaluate(args.get(0), self, bindings)));
			case "notEmpty" -> !asCollection(source).isEmpty();
			case "size" -> source instanceof Collection<?> c ? c.size() : text(source).length();
			case "contains" -> text(source).contains(text(evaluate(args.get(0), self, bindings)));
			case "startsWith" -> text(source).startsWith(text(evaluate(args.get(0), self, bindings)));
			case "endsWith" -> text(source).endsWith(text(evaluate(args.get(0), self, bindings)));
			case "toLower" -> text(source).toLowerCase();
			case "toUpper" -> text(source).toUpperCase();
			case "trim" -> text(source).trim();
			case "indexOf" -> text(source).indexOf(text(evaluate(args.get(0), self, bindings)));
			case "concat" -> text(source) + text(evaluate(args.get(0), self, bindings));
			case "substring" -> substring(source, args, self, bindings);
			case "year" -> dateTime(source).getYear();
			case "month" -> dateTime(source).getMonthValue();
			case "day" -> dateTime(source).getDayOfMonth();
			case "hour" -> dateTime(source).getHour();
			case "minute" -> dateTime(source).getMinute();
			case "second" -> dateTime(source).getSecond();
			case "date" -> dateTime(source).toLocalDate().toString();
			case "time" -> dateTime(source).toLocalTime().toString();
			case "round" -> decimal(source).setScale(0, java.math.RoundingMode.HALF_UP).longValue();
			case "floor" -> decimal(source).setScale(0, java.math.RoundingMode.FLOOR).longValue();
			case "ceiling" -> decimal(source).setScale(0, java.math.RoundingMode.CEILING).longValue();
			default -> throw new IllegalArgumentException("unsupported operation '" + name + "'");
		};
	}

	private Object typeOperation(String name, Object value, List<OclExpression> args) {
		if (!(args.get(0) instanceof TypeExp typeExp)) {
			throw new IllegalArgumentException(name + " expects a type argument");
		}
		if ("oclAsType".equals(name)) {
			// a failed structured cast yields null ([OData-URL] 5.1.1.10.1) — 3VL exclusion;
			// primitive casts stay representation-preserving assertions
			if (typeExp.getReferredType() instanceof ClassifierType classifierType
					&& classifierType.getReferredClassifier() != null) {
				return classifierType.getReferredClassifier().isInstance(value) ? value : null;
			}
			return value;
		}
		if (typeExp.getReferredType() instanceof ClassifierType classifierType) {
			EClassifier classifier = classifierType.getReferredClassifier();
			return classifier != null && classifier.isInstance(value);
		}
		String primitive = typeExp.getReferredType() == null ? null : typeExp.getReferredType().getName();
		return switch (primitive) {
			case "String" -> value instanceof String;
			case "Boolean" -> value instanceof Boolean;
			case "Integer" -> value instanceof Integer || value instanceof Long || value instanceof Short
					|| value instanceof Byte || value instanceof java.math.BigInteger;
			case "Real" -> value instanceof Number;
			case null, default -> false;
		};
	}

	private Object substring(Object source, List<OclExpression> args, Object self,
			Map<Variable, Object> bindings) {
		String value = text(source);
		int start = decimal(evaluate(args.get(0), self, bindings)).intValue();
		// [OData-URL] 5.1.1.7: start beyond the end → empty string; a negative start counts
		// from the end of the string (clamped to the full string)
		int effectiveStart = start < 0
				? Math.max(0, value.length() + start)
				: Math.min(start, value.length());
		if (args.size() > 1) {
			int length = decimal(evaluate(args.get(1), self, bindings)).intValue();
			int end = Math.min(value.length(), Math.max(effectiveStart, effectiveStart + length));
			return value.substring(effectiveStart, end);
		}
		return value.substring(effectiveStart);
	}

	// --- value coercion ---

	/**
	 * Equality under three-valued logic: comparing against the null LITERAL ({@code x eq null}) is a
	 * defined test, but a null VALUE flowing in from a property is UNKNOWN — signalled as
	 * {@link NullComparison} so it propagates through {@code not}/{@code and}/{@code or} and collapses
	 * the predicate to false, exactly as SQL (and the JPA backend) treat it.
	 */
	private boolean equalTo3vl(OperationCallExp op, Object left, Object right) {
		if (left == null || right == null) {
			boolean nullLiteral = isNullLiteral(op.getOwnedSource())
					|| op.getOwnedArguments().stream().anyMatch(OclEvaluator::isNullLiteral);
			if (nullLiteral) {
				return left == right;
			}
			throw new NullComparison();
		}
		return equalTo(left, right);
	}

	private static boolean isNullLiteral(OclExpression expression) {
		return expression instanceof NullLiteralExp;
	}

	private boolean equalTo(Object left, Object right) {
		if (left == null || right == null) {
			return left == right;
		}
		if (left instanceof Number && right instanceof Number) {
			return decimal(left).compareTo(decimal(right)) == 0;
		}
		if (left instanceof Enumerator e) {
			return right instanceof Enumerator other
					? e.getName().equals(other.getName())
					: e.getName().equals(String.valueOf(right));
		}
		if (right instanceof Enumerator) {
			return equalTo(right, left);
		}
		if (left instanceof Date || right instanceof Date) {
			return comparison(left, right) == 0;
		}
		return left.equals(right);
	}

	private int comparison(Object left, Object right) {
		if (left == null || right == null) {
			// three-valued logic collapsed: comparisons with null are never true
			throw new NullComparison();
		}
		if (left instanceof Number && right instanceof Number) {
			return decimal(left).compareTo(decimal(right));
		}
		if (left instanceof Date || right instanceof Date) {
			return dateTime(left).compareTo(dateTime(right));
		}
		if (left instanceof String l && right instanceof String r) {
			return l.compareTo(r);
		}
		if (left instanceof Comparable<?> && left.getClass().isInstance(right)) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			int result = ((Comparable) left).compareTo(right);
			return result;
		}
		throw new IllegalArgumentException("cannot compare " + left.getClass().getSimpleName()
				+ " with " + right.getClass().getSimpleName());
	}

	/** Internal signal: a relational comparison hit null — the enclosing predicate is false. */
	private static final class NullComparison extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Retained for API compatibility: {@link #matches} is itself null-safe, so this is a plain
	 * delegation — null-valued relational comparisons yield {@code false}.
	 */
	public boolean matchesNullSafe(OclExpression predicate, Object self) {
		return matches(predicate, self);
	}

	/** Unary minus; NaN/±INF live as doubles (BigDecimal cannot carry them). */
	private static Object negate(Object value) {
		if (value instanceof Double d && (d.isNaN() || d.isInfinite())) {
			return -d;
		}
		return decimal(value).negate();
	}

	private static BigDecimal decimal(Object value) {
		if (value instanceof BigDecimal bd) {
			return bd;
		}
		if (value instanceof Number number) {
			return new BigDecimal(number.toString());
		}
		throw new IllegalArgumentException("not a number: " + value);
	}

	private Object arithmetic(String name, Object left, Object right) {
		BigDecimal l = decimal(left);
		BigDecimal r = decimal(right);
		return switch (name) {
			case "+" -> l.add(r);
			case "-" -> l.subtract(r);
			case "*" -> l.multiply(r);
			case "/" -> {
				if (r.signum() == 0) {
					throw new IllegalArgumentException("division by zero");
				}
				yield l.divide(r, MathContext.DECIMAL64);
			}
			case "mod" -> {
				if (r.signum() == 0) {
					throw new IllegalArgumentException("division by zero");
				}
				yield l.remainder(r);
			}
			default -> throw new IllegalArgumentException("unsupported arithmetic '" + name + "'");
		};
	}

	private static ZonedDateTime dateTime(Object value) {
		if (value instanceof Date date) {
			return date.toInstant().atZone(ZoneOffset.UTC);
		}
		if (value instanceof String text) { // pre-typed Date/DateTimeOffset literals stay strings
			if (text.length() == 10) {
				return LocalDate.parse(text).atStartOfDay(ZoneOffset.UTC);
			}
			return OffsetDateTime.parse(text).toZonedDateTime();
		}
		throw new IllegalArgumentException("not a date value: " + value);
	}

	private static Collection<?> asCollection(Object value) {
		if (value instanceof Collection<?> collection) {
			return collection;
		}
		return value == null ? List.of() : List.of(value);
	}

	private static String text(Object value) {
		if (value instanceof String s) {
			return s;
		}
		throw new IllegalArgumentException("not a string value: " + value);
	}
}
