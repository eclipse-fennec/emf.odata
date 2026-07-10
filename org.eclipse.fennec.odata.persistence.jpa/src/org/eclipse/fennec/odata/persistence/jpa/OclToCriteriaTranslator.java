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
package org.eclipse.fennec.odata.persistence.jpa;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.fennec.m2x.model.ocl.BooleanLiteralExp;
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
import org.eclipse.fennec.m2x.model.ocl.Variable;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;
import org.eclipse.persistence.jpa.JpaCriteriaBuilder;

import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

/**
 * Translates the OCL predicate IR (req §3.5) into Jakarta Persistence Criteria expressions —
 * the E5 pushdown: every construct either becomes SQL via the Criteria API or FAILS loudly
 * with {@link UnsupportedOperationException}; there is no silent in-memory fallback (the
 * servlet maps the failure to 501).
 *
 * <p>The operation names and semantics mirror the in-memory {@code OclEvaluator} (the
 * reference backend): comparisons with {@code null} become {@code IS [NOT] NULL},
 * {@code includes} becomes {@code IN}, OData lambdas ({@code exists}/{@code forAll} over
 * collection features) become correlated {@code EXISTS} subqueries, string functions map to
 * their JPQL counterparts (with OData's 0-based indexes shifted to JPQL's 1-based ones and
 * LIKE wildcards escaped).
 *
 * <p>Property paths use the EMF feature names — Fennec Persistence JPA maps dynamic entities
 * with exactly these attribute names, and single-valued navigation paths become implicit
 * joins. Constant operands are coerced to the attribute's Java type (typed OData literals
 * arrive as ISO strings).
 */
public class OclToCriteriaTranslator {

	/**
	 * Translation state: the criteria nodes, lambda-variable bindings and named expressions —
	 * {@code $apply} aliases and grouped paths (by slash-joined path name), resolvable in
	 * post-pipeline predicates (HAVING) and order keys.
	 */
	private record Context(CriteriaBuilder cb, AbstractQuery<?> query, From<?, ?> self,
			Map<Variable, From<?, ?>> variables, Map<String, Expression<?>> named) {

		Context nested(AbstractQuery<?> subquery, Variable variable, From<?, ?> binding) {
			Map<Variable, From<?, ?>> inner = new HashMap<>(variables);
			inner.put(variable, binding);
			return new Context(cb, subquery, self, inner, named);
		}
	}

	/** A translated operand: either a criteria expression or a plain constant. */
	private sealed interface Operand {
	}

	private record Expr(Expression<?> expression) implements Operand {
	}

	private record Const(Object value) implements Operand {
	}

	/** Translates a boolean-typed OCL expression into a criteria predicate. */
	public Predicate predicate(OclExpression expression, CriteriaBuilder cb,
			AbstractQuery<?> query, From<?, ?> root) {
		return predicate(expression, cb, query, root, Map.of());
	}

	/** {@link #predicate} with named expressions ($apply aliases / grouped paths) in scope. */
	public Predicate predicate(OclExpression expression, CriteriaBuilder cb,
			AbstractQuery<?> query, From<?, ?> root, Map<String, Expression<?>> named) {
		return asPredicate(
				operand(expression, new Context(cb, query, root, new HashMap<>(), named)), cb);
	}

	/** Translates a value-typed OCL expression (e.g. an {@code $orderby} key). */
	public Expression<?> expression(OclExpression expression, CriteriaBuilder cb,
			AbstractQuery<?> query, From<?, ?> root) {
		return expression(expression, cb, query, root, Map.of());
	}

	/** {@link #expression} with named expressions ($apply aliases / grouped paths) in scope. */
	public Expression<?> expression(OclExpression expression, CriteriaBuilder cb,
			AbstractQuery<?> query, From<?, ?> root, Map<String, Expression<?>> named) {
		Operand operand = operand(expression, new Context(cb, query, root, new HashMap<>(), named));
		return operand instanceof Expr expr ? expr.expression()
				: cb.literal(((Const) operand).value());
	}

	// --- dispatch ---

	private Operand operand(OclExpression exp, Context ctx) {
		return switch (exp) {
			case null -> throw new UnsupportedOperationException("null expression");
			case StringLiteralExp s -> new Const(s.getStringSymbol());
			case IntegerLiteralExp i -> new Const(i.getIntegerSymbol());
			case RealLiteralExp r -> new Const(r.getRealSymbol());
			case BooleanLiteralExp b -> new Const(b.isBooleanSymbol());
			case NullLiteralExp n -> new Const(null);
			case EnumLiteralExp e -> new Const(e.getReferredLiteral() == null ? null
					: e.getReferredLiteral().getName()); // enums compare by NAME (like the evaluator)
			case PropertyCallExp p -> {
				// a grouped path referenced post-pipeline resolves to the GROUPED expression
				String pathName = ctx.named().isEmpty() ? null : pathName(p);
				Expression<?> grouped = pathName == null ? null : ctx.named().get(pathName);
				yield grouped != null ? new Expr(grouped) : new Expr(path(p, ctx));
			}
			case OperationCallExp op -> operation(op, ctx);
			case IteratorExp it -> new Expr(lambda(it, ctx));
			case VariableExp v -> {
				String name = v.getReferredVariable() == null ? null
						: v.getReferredVariable().getName();
				Expression<?> named = name == null ? null : ctx.named().get(name);
				if (named == null) {
					throw new UnsupportedOperationException(
							"variable '" + (name == null ? "?" : name) + "' outside a lambda scope");
				}
				yield new Expr(named);
			}
			default -> throw new UnsupportedOperationException(
					"expression kind " + exp.eClass().getName());
		};
	}

	/** The slash-joined feature path of a plain property chain, or null for other shapes. */
	static String pathName(PropertyCallExp p) {
		if (p.getReferredProperty() == null) {
			return null;
		}
		if (p.getOwnedSource() == null) {
			return p.getReferredProperty().getName();
		}
		if (p.getOwnedSource() instanceof PropertyCallExp source) {
			String prefix = pathName(source);
			return prefix == null ? null : prefix + "/" + p.getReferredProperty().getName();
		}
		return null;
	}

	/** EMF feature path → criteria path; single-valued navigations become implicit joins. */
	private Path<?> path(PropertyCallExp p, Context ctx) {
		OclExpression source = p.getOwnedSource();
		Path<?> base = switch (source) {
			case null -> ctx.self();
			case VariableExp v -> {
				From<?, ?> bound = ctx.variables().get(v.getReferredVariable());
				if (bound == null) {
					throw new UnsupportedOperationException("unbound lambda variable");
				}
				yield bound;
			}
			case PropertyCallExp chained -> path(chained, ctx);
			default -> throw new UnsupportedOperationException(
					"property source kind " + source.eClass().getName());
		};
		if (p.getReferredProperty() == null) {
			throw new UnsupportedOperationException("unresolved property reference in $filter/$orderby");
		}
		return base.get(p.getReferredProperty().getName());
	}

	// --- operations ---

	@SuppressWarnings("unchecked")
	private Operand operation(OperationCallExp op, Context ctx) {
		String name = op.getName();
		List<OclExpression> args = op.getOwnedArguments();
		CriteriaBuilder cb = ctx.cb();

		return switch (name) {
			case "and" -> new Expr(cb.and(asPredicate(operand(op.getOwnedSource(), ctx), cb),
					asPredicate(operand(args.get(0), ctx), cb)));
			case "or" -> new Expr(cb.or(asPredicate(operand(op.getOwnedSource(), ctx), cb),
					asPredicate(operand(args.get(0), ctx), cb)));
			case "not" -> new Expr(cb.not(asPredicate(operand(op.getOwnedSource(), ctx), cb)));

			case "=" -> new Expr(equality(operand(op.getOwnedSource(), ctx),
					operand(args.get(0), ctx), cb, false));
			case "<>" -> new Expr(equality(operand(op.getOwnedSource(), ctx),
					operand(args.get(0), ctx), cb, true));
			// "has" carries single-flag semantics in the evaluator — plain equality
			case "has" -> new Expr(equality(operand(op.getOwnedSource(), ctx),
					operand(args.get(0), ctx), cb, false));

			case "<", "<=", ">", ">=" -> new Expr(ordering(name,
					operand(op.getOwnedSource(), ctx), operand(args.get(0), ctx), cb));

			case "+", "-", "*", "/", "mod" -> args.isEmpty() // unary minus: negate the source
					? new Expr(cb.neg(numeric(operand(op.getOwnedSource(), ctx), cb)))
					: arithmetic(name,
							operand(op.getOwnedSource(), ctx), operand(args.get(0), ctx), cb);

			case "includes" -> new Expr(inList(op, ctx));
			case "notEmpty" -> new Expr(cb.isNotEmpty(collection(op.getOwnedSource(), ctx)));
			case "size" -> size(op, ctx);

			case "contains" -> new Expr(like(op, ctx, "%", "%"));
			case "startsWith" -> new Expr(like(op, ctx, "", "%"));
			case "endsWith" -> new Expr(like(op, ctx, "%", ""));
			case "toLower" -> new Expr(cb.lower(string(operand(op.getOwnedSource(), ctx), cb)));
			case "toUpper" -> new Expr(cb.upper(string(operand(op.getOwnedSource(), ctx), cb)));
			case "trim" -> new Expr(cb.trim(string(operand(op.getOwnedSource(), ctx), cb)));
			case "concat" -> new Expr(cb.concat(string(operand(op.getOwnedSource(), ctx), cb),
					string(operand(args.get(0), ctx), cb)));
			// OData indexOf is 0-based (-1 = absent), JPQL LOCATE is 1-based (0 = absent)
			case "indexOf" -> new Expr(cb.diff(
					cb.locate(string(operand(op.getOwnedSource(), ctx), cb),
							string(operand(args.get(0), ctx), cb)),
					1));
			case "substring" -> new Expr(substring(op, ctx));

			case "year", "month", "day", "hour", "minute", "second" ->
				new Expr(extractDatePart(name, op, ctx));

			default -> throw new UnsupportedOperationException(
					"operation '" + name + "' has no JPA pushdown");
		};
	}

	/**
	 * OData date-part functions → SQL {@code EXTRACT(PART FROM …)} (portable across H2 and
	 * PostgreSQL). The jakarta {@code CriteriaBuilder.extract} only exists from Persistence
	 * 3.2 implementations on — EclipseLink 4.0 offers the same through its native expression
	 * bridge ({@code JpaCriteriaBuilder.toExpression / Expression.extract / fromExpression}).
	 */
	private Expression<Integer> extractDatePart(String part, OperationCallExp op, Context ctx) {
		if (!(ctx.cb() instanceof JpaCriteriaBuilder jpaBuilder)) {
			throw new UnsupportedOperationException(
					"date part '" + part + "' needs the EclipseLink criteria builder");
		}
		Expression<?> source = expression(operand(op.getOwnedSource(), ctx), ctx.cb());
		return jpaBuilder.fromExpression(
				jpaBuilder.toExpression(source).extract(part.toUpperCase(Locale.ROOT)),
				Integer.class);
	}

	/** {@code eq/ne} incl. the null forms ({@code IS [NOT] NULL} — also on navigations). */
	private Predicate equality(Operand left, Operand right, CriteriaBuilder cb, boolean negate) {
		if (isNullConstant(right)) {
			Expression<?> expr = expression(left, cb);
			return negate ? cb.isNotNull(expr) : cb.isNull(expr);
		}
		if (isNullConstant(left)) {
			return equality(right, left, cb, negate);
		}
		if (left instanceof Expr expr && right instanceof Const constant) {
			Object value = coerce(constant.value(), expr.expression().getJavaType());
			return negate ? cb.notEqual(expr.expression(), value)
					: cb.equal(expr.expression(), value);
		}
		if (left instanceof Const && right instanceof Expr) {
			return equality(right, left, cb, negate);
		}
		Expression<?> l = expression(left, cb);
		Expression<?> r = expression(right, cb);
		return negate ? cb.notEqual(l, r) : cb.equal(l, r);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Predicate ordering(String name, Operand left, Operand right, CriteriaBuilder cb) {
		if (isNullConstant(left) || isNullConstant(right)) {
			// three-valued logic collapsed, like the evaluator: ordering vs null is never true
			return cb.disjunction();
		}
		Expression l = expression(left, cb);
		Object r = right instanceof Const constant ? coerce(constant.value(), l.getJavaType())
				: ((Expr) right).expression();
		Expression rExpr = r instanceof Expression e ? e : cb.literal((Comparable) r);
		return switch (name) {
			case "<" -> cb.lessThan(l, rExpr);
			case "<=" -> cb.lessThanOrEqualTo(l, rExpr);
			case ">" -> cb.greaterThan(l, rExpr);
			default -> cb.greaterThanOrEqualTo(l, rExpr);
		};
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Operand arithmetic(String name, Operand left, Operand right, CriteriaBuilder cb) {
		Expression l = numeric(left, cb);
		Expression r = numeric(right, cb);
		return new Expr(switch (name) {
			case "+" -> cb.sum(l, r);
			case "-" -> cb.diff(l, r);
			case "*" -> cb.prod(l, r);
			case "/" -> cb.quot(l, r);
			default -> cb.mod(l.as(Integer.class), r.as(Integer.class));
		});
	}

	/** {@code Set{…}->includes(x)} → {@code x IN (…)}; the empty set matches nothing. */
	private Predicate inList(OperationCallExp op, Context ctx) {
		if (!(op.getOwnedSource() instanceof CollectionLiteralExp literal)) {
			throw new UnsupportedOperationException("'in' requires a literal member list");
		}
		Expression<?> tested = expression(operand(op.getOwnedArguments().get(0), ctx), ctx.cb());
		List<Object> members = new ArrayList<>();
		for (var part : literal.getOwnedParts()) {
			if (part instanceof CollectionItem item) {
				Operand member = operand(item.getOwnedItem(), ctx);
				if (!(member instanceof Const constant)) {
					throw new UnsupportedOperationException("'in' members must be literals");
				}
				members.add(coerce(constant.value(), tested.getJavaType()));
			}
		}
		return members.isEmpty() ? ctx.cb().disjunction() : tested.in(members);
	}

	/** String {@code size} → LENGTH, collection {@code size} → SIZE. */
	@SuppressWarnings("unchecked")
	private Operand size(OperationCallExp op, Context ctx) {
		if (op.getOwnedSource() instanceof PropertyCallExp property
				&& property.getReferredProperty() != null && property.getReferredProperty().isMany()) {
			return new Expr(ctx.cb().size(collection(op.getOwnedSource(), ctx)));
		}
		return new Expr(ctx.cb().length(string(operand(op.getOwnedSource(), ctx), ctx.cb())));
	}

	/**
	 * OData substring is 0-based, JPQL SUBSTRING is 1-based. A negative start counts from
	 * the end of the string, clamped to position 1 ([OData-URL] 5.1.1.7 SHOULD) — pushed
	 * down as a CASE over LENGTH(source).
	 */
	private Expression<String> substring(OperationCallExp op, Context ctx) {
		CriteriaBuilder cb = ctx.cb();
		Expression<String> source = string(operand(op.getOwnedSource(), ctx), cb);
		Expression<Integer> requested = intExpr(op.getOwnedArguments().get(0), ctx);
		Expression<Integer> fromEnd = cb.sum(cb.sum(cb.length(source), requested), 1);
		Expression<Integer> clampedFromEnd = cb.<Integer>selectCase()
				.when(cb.greaterThan(fromEnd, 0), fromEnd)
				.otherwise(cb.literal(1));
		Expression<Integer> start = cb.<Integer>selectCase()
				.when(cb.greaterThanOrEqualTo(requested, 0), cb.sum(requested, 1))
				.otherwise(clampedFromEnd);
		if (op.getOwnedArguments().size() > 1) {
			return cb.substring(source, start, intExpr(op.getOwnedArguments().get(1), ctx));
		}
		return cb.substring(source, start);
	}

	/** contains/startsWith/endsWith → LIKE with escaped wildcards (constant patterns only). */
	private Predicate like(OperationCallExp op, Context ctx, String prefix, String suffix) {
		Operand pattern = operand(op.getOwnedArguments().get(0), ctx);
		if (!(pattern instanceof Const constant) || !(constant.value() instanceof String text)) {
			throw new UnsupportedOperationException("string match requires a literal pattern");
		}
		String escaped = text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
		return ctx.cb().like(string(operand(op.getOwnedSource(), ctx), ctx.cb()),
				prefix + escaped + suffix, '\\');
	}

	/** OData lambdas: {@code exists} → EXISTS, {@code forAll} → NOT EXISTS(NOT body). */
	private Predicate lambda(IteratorExp it, Context ctx) {
		boolean forAll = "forAll".equals(it.getName());
		if (!forAll && !"exists".equals(it.getName())) {
			throw new UnsupportedOperationException("iterator '" + it.getName() + "'");
		}
		if (!(it.getOwnedSource() instanceof PropertyCallExp property)) {
			throw new UnsupportedOperationException("lambda source must be a collection path");
		}
		CriteriaBuilder cb = ctx.cb();
		Subquery<Integer> sub = ctx.query().subquery(Integer.class);
		Join<?, ?> element = correlated(sub, property, ctx)
				.join(property.getReferredProperty().getName());
		Context inner = ctx.nested(sub, it.getOwnedIterators().get(0), element);
		Predicate body = asPredicate(operand(it.getOwnedBody(), inner), cb);
		sub.select(cb.literal(1)).where(forAll ? cb.not(body) : body);
		return forAll ? cb.not(cb.exists(sub)) : cb.exists(sub);
	}

	/** Correlates the lambda's collection owner into the subquery (v1: paths on the root). */
	private From<?, ?> correlated(Subquery<Integer> sub, PropertyCallExp property, Context ctx) {
		if (property.getOwnedSource() != null) {
			throw new UnsupportedOperationException("lambda over a nested path (v1 limit)");
		}
		return switch (ctx.self()) {
			case jakarta.persistence.criteria.Root<?> root -> sub.correlate(root);
			case Join<?, ?> join -> sub.correlate(join);
			default -> throw new UnsupportedOperationException("lambda in this position");
		};
	}

	// --- operand helpers ---

	private static boolean isNullConstant(Operand operand) {
		return operand instanceof Const constant && constant.value() == null;
	}

	private Expression<?> expression(Operand operand, CriteriaBuilder cb) {
		return operand instanceof Expr expr ? expr.expression()
				: cb.literal(((Const) operand).value());
	}

	private Predicate asPredicate(Operand operand, CriteriaBuilder cb) {
		return asPredicate(expression(operand, cb), cb);
	}

	@SuppressWarnings("unchecked")
	private Predicate asPredicate(Expression<?> expression, CriteriaBuilder cb) {
		if (expression instanceof Predicate predicate) {
			return predicate;
		}
		if (Boolean.class.equals(expression.getJavaType())
				|| boolean.class.equals(expression.getJavaType())) {
			return cb.isTrue((Expression<Boolean>) expression);
		}
		throw new UnsupportedOperationException("expression is not boolean-typed");
	}

	@SuppressWarnings("unchecked")
	private Expression<String> string(Operand operand, CriteriaBuilder cb) {
		return (Expression<String>) expression(operand, cb);
	}

	@SuppressWarnings("rawtypes")
	private Expression numeric(Operand operand, CriteriaBuilder cb) {
		return (Expression) expression(operand, cb);
	}

	@SuppressWarnings("unchecked")
	private Expression<Integer> intExpr(OclExpression exp, Context ctx) {
		Operand operand = operand(exp, ctx);
		if (operand instanceof Const constant && constant.value() instanceof Number number) {
			return ctx.cb().literal(number.intValue());
		}
		return numeric(operand, ctx.cb()).as(Integer.class);
	}

	@SuppressWarnings("rawtypes")
	private Expression collection(OclExpression exp, Context ctx) {
		Operand operand = operand(exp, ctx);
		if (operand instanceof Expr expr) {
			return (Expression) expr.expression();
		}
		throw new UnsupportedOperationException("collection operand must be a property path");
	}

	// --- constant coercion (typed OData literals arrive as ISO strings / Long / Double) ---

	/** Converts a literal to the attribute's Java type so the JDBC binding matches. */
	private static Object coerce(Object value, Class<?> target) {
		if (value == null || target == null || target.isInstance(value) || target == Object.class) {
			return value;
		}
		if (value instanceof Enumerator literal) { // enum literals compare by name
			return coerce(literal.getName(), target);
		}
		if (value instanceof Number number) {
			return coerceNumber(number, target);
		}
		if (value instanceof String text) {
			return coerceText(text, target);
		}
		return value;
	}

	private static Object coerceNumber(Number number, Class<?> target) {
		if (target == BigDecimal.class) {
			return new BigDecimal(number.toString());
		}
		if (target == Integer.class || target == int.class) {
			return number.intValue();
		}
		if (target == Long.class || target == long.class) {
			return number.longValue();
		}
		if (target == Double.class || target == double.class) {
			return number.doubleValue();
		}
		if (target == Float.class || target == float.class) {
			return number.floatValue();
		}
		if (target == Short.class || target == short.class) {
			return number.shortValue();
		}
		if (target == Byte.class || target == byte.class) {
			return number.byteValue();
		}
		if (target == java.math.BigInteger.class) {
			return java.math.BigInteger.valueOf(number.longValue());
		}
		return number;
	}

	private static Object coerceText(String text, Class<?> target) {
		try {
			if (target == Date.class) { // EMF EDate — OData date / dateTimeOffset literals
				return Date.from(instantOf(text));
			}
			if (target == Instant.class) {
				return instantOf(text);
			}
			if (target == LocalDate.class) {
				return LocalDate.parse(text);
			}
			if (target == LocalTime.class) {
				return LocalTime.parse(text);
			}
			if (target == OffsetDateTime.class) {
				return OffsetDateTime.parse(text);
			}
			if (target == UUID.class) {
				return UUID.fromString(text);
			}
			if (target == BigDecimal.class) {
				return new BigDecimal(text);
			}
			if (target == Integer.class) {
				return Integer.valueOf(text);
			}
			if (target == Long.class) {
				return Long.valueOf(text);
			}
			if (target == Double.class) {
				return Double.valueOf(text);
			}
			if (target == Boolean.class) {
				return Boolean.valueOf(text);
			}
		} catch (DateTimeParseException | NumberFormatException e) {
			throw new IllegalArgumentException(
					"literal '" + text + "' does not convert to " + target.getSimpleName(), e);
		}
		return text;
	}

	private static Instant instantOf(String text) {
		if (text.contains("T")) {
			return OffsetDateTime.parse(text).toInstant();
		}
		return LocalDate.parse(text).atStartOfDay(ZoneOffset.UTC).toInstant();
	}
}
