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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.m2x.model.ocl.BooleanLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.CollectionItem;
import org.eclipse.fennec.m2x.model.ocl.CollectionKind;
import org.eclipse.fennec.m2x.model.ocl.CollectionLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.EnumLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.IntegerLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.IteratorExp;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OclFactory;
import org.eclipse.fennec.m2x.model.ocl.OclType;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.RealLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.TypeExp;
import org.eclipse.fennec.m2x.model.ocl.Variable;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;
import org.eclipse.fennec.odata.query.antlr.ODataFilterBaseVisitor;
import org.eclipse.fennec.odata.query.antlr.ODataFilterParser;

/**
 * Maps the {@code ODataFilter.g4} parse tree onto the m2x OCL AST (the internal predicate IR),
 * following the req §3.5 mapping table: comparisons become {@code OperationCallExp} with the OCL
 * operator name ({@code eq}→{@code =}, …), logical/arithmetic operators keep their OCL names,
 * canonical functions map to OCL-stdlib names ({@code length}→{@code size}, {@code tolower}→
 * {@code toLower}, …), OData-only operators stay as custom names ({@code has}, {@code contains})
 * for the backend dispatch table, and {@code in} becomes {@code includes} over a Set literal.
 *
 * <p>Property paths are resolved eagerly against the request's context {@link EClass}
 * ({@code referredProperty}); an unknown segment fails the parse (400 at the protocol layer,
 * mandatory for backend pushdown). {@code OclExpression.type} stays unset — type resolution is
 * the {@code OclAspectProvider}'s job (VA1, req §3.6.1) once available.
 */
class ODataToOclBuilder extends ODataFilterBaseVisitor<OclExpression> {

	private static final OclFactory FACTORY = OclFactory.eINSTANCE;

	private static final Map<String, String> COMPARISON_OPS = Map.of(
			"eq", "=", "ne", "<>", "gt", ">", "ge", ">=", "lt", "<", "le", "<=",
			"has", "has");
	// div and divby both map to OCL "/": divby IS decimal division (4.01 5.1.1.2), and the
	// current div mapping deliberately skips integer truncation (documented simplification)
	private static final Map<String, String> ARITHMETIC_OPS = Map.of(
			"add", "+", "sub", "-", "mul", "*", "div", "/", "divby", "/", "mod", "mod");
	/** OData canonical function → OCL(-stdlib or custom) operation name. */
	private static final Map<String, String> FUNCTIONS = Map.ofEntries(
			Map.entry("contains", "contains"),
			Map.entry("startswith", "startsWith"),
			Map.entry("endswith", "endsWith"),
			Map.entry("tolower", "toLower"),
			Map.entry("toupper", "toUpper"),
			Map.entry("trim", "trim"),
			Map.entry("length", "size"),
			Map.entry("indexof", "indexOf"),
			Map.entry("substring", "substring"),
			Map.entry("concat", "concat"),
			Map.entry("year", "year"),
			Map.entry("month", "month"),
			Map.entry("day", "day"),
			Map.entry("hour", "hour"),
			Map.entry("minute", "minute"),
			Map.entry("second", "second"),
			Map.entry("date", "date"),
			Map.entry("time", "time"),
			Map.entry("round", "round"),
			Map.entry("floor", "floor"),
			Map.entry("ceiling", "ceiling"));

	private final EClass context;
	/** Lambda variable scopes, innermost last: name → (Variable, element type when structured). */
	private final Deque<LambdaScope> scopes = new ArrayDeque<>();
	/** Aliases introduced by $apply aggregate/compute stages — referable in later stages. */
	private final Map<String, Variable> aliases = new HashMap<>();
	/** Resolves a {@code @name} parameter alias to its expression (4.01 11.2.5.1.3). */
	private Function<String, OclExpression> parameterAliasResolver = name -> {
		throw new ODataQueryParseException("unresolved parameter alias '" + name + "'");
	};

	private record LambdaScope(String name, Variable variable, EClass elementClass) {
	}

	ODataToOclBuilder(EClass context) {
		this.context = context;
	}

	/** Makes an aggregate/compute alias visible to subsequently built expressions. */
	void registerAlias(String alias) {
		Variable variable = FACTORY.createVariable();
		variable.setName(alias);
		aliases.put(alias, variable);
	}

	/** Installs the {@code @name} lookup — without one, any alias reference is an error. */
	void parameterAliasResolver(Function<String, OclExpression> resolver) {
		this.parameterAliasResolver = resolver;
	}

	// --- structure ---

	@Override
	public OclExpression visitFilter(ODataFilterParser.FilterContext ctx) {
		return visit(ctx.expr());
	}

	@Override
	public OclExpression visitOrExpr(ODataFilterParser.OrExprContext ctx) {
		return foldLeft("or", ctx.andExpr());
	}

	@Override
	public OclExpression visitAndExpr(ODataFilterParser.AndExprContext ctx) {
		return foldLeft("and", ctx.notExpr());
	}

	@Override
	public OclExpression visitNotExpression(ODataFilterParser.NotExpressionContext ctx) {
		OperationCallExp exp = FACTORY.createOperationCallExp();
		exp.setName("not");
		exp.setOwnedSource(visit(ctx.notExpr()));
		return exp;
	}

	@Override
	public OclExpression visitBinaryComparison(ODataFilterParser.BinaryComparisonContext ctx) {
		return binary(COMPARISON_OPS.get(ctx.op.getText().toLowerCase()), visit(ctx.additive(0)), visit(ctx.additive(1)));
	}

	@Override
	public OclExpression visitInListComparison(ODataFilterParser.InListComparisonContext ctx) {
		// Set{...}->includes(<lhs>) — per the req §3.5 mapping for v4.01 'in'
		return includes(ctx.literal().stream().map(this::visit).toList(), visit(ctx.additive()));
	}

	@Override
	public OclExpression visitInComparison(ODataFilterParser.InComparisonContext ctx) {
		// single parenthesized expression form (4.01 inExpr with commonExpr operand)
		return includes(List.of(visit(ctx.expr())), visit(ctx.additive()));
	}

	@Override
	public OclExpression visitInEmptyListComparison(ODataFilterParser.InEmptyListComparisonContext ctx) {
		// empty list (current TC listExpr): no member matches — Set{}->includes(<lhs>)
		return includes(List.of(), visit(ctx.additive()));
	}

	private OclExpression includes(List<OclExpression> members, OclExpression lhs) {
		CollectionLiteralExp set = FACTORY.createCollectionLiteralExp();
		set.setKind(CollectionKind.SET);
		for (OclExpression member : members) {
			CollectionItem item = FACTORY.createCollectionItem();
			item.setOwnedItem(member);
			set.getOwnedParts().add(item);
		}
		return binary("includes", set, lhs);
	}

	@Override
	public OclExpression visitAddSub(ODataFilterParser.AddSubContext ctx) {
		return binary(ARITHMETIC_OPS.get(ctx.op.getText().toLowerCase()), visit(ctx.additive()), visit(ctx.multiplicative()));
	}

	@Override
	public OclExpression visitMulDivMod(ODataFilterParser.MulDivModContext ctx) {
		return binary(ARITHMETIC_OPS.get(ctx.op.getText().toLowerCase()), visit(ctx.multiplicative()), visit(ctx.primary()));
	}

	@Override
	public OclExpression visitAliasPrimary(ODataFilterParser.AliasPrimaryContext ctx) {
		return parameterAliasResolver.apply(ctx.ALIAS().getText());
	}

	@Override
	public OclExpression visitParenPrimary(ODataFilterParser.ParenPrimaryContext ctx) {
		return visit(ctx.expr());
	}

	// --- type operators (E4-AP-2) ---

	/** {@code cast(x,T)} → {@code oclAsType}, {@code isof(x,T)} → {@code oclIsKindOf} + TypeExp. */
	@Override
	public OclExpression visitTypeFunc(ODataFilterParser.TypeFuncContext ctx) {
		OperationCallExp exp = FACTORY.createOperationCallExp();
		exp.setName("cast".equalsIgnoreCase(ctx.op.getText()) ? "oclAsType" : "oclIsKindOf");
		if (ctx.expr() != null) {
			exp.setOwnedSource(visit(ctx.expr()));
		} else {
			exp.setIsImplicit(true); // unbound form tests/casts the implicit instance
		}
		TypeExp typeExp = FACTORY.createTypeExp();
		typeExp.setReferredType(resolveTypeName(ctx.qualifiedTypeName().getText()));
		exp.getOwnedArguments().add(typeExp);
		return exp;
	}

	/** {@code Edm.*} → OCL primitive; model types resolve against the context package. */
	private OclType resolveTypeName(String qualifiedName) {
		String simple = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
		if (qualifiedName.startsWith("Edm.")) {
			var primitive = FACTORY.createPrimitiveType();
			primitive.setName(switch (simple) {
				case "Int16", "Int32", "Int64", "SByte", "Byte" -> "Integer";
				case "Decimal", "Double", "Single" -> "Real";
				default -> simple; // String, Boolean, Date, Guid, ... — backend dispatch by name
			});
			return primitive;
		}
		EClassifier classifier = context.getEPackage().getEClassifier(simple);
		if (classifier == null) {
			throw new ODataQueryParseException(
					"unknown type '" + qualifiedName + "' in package " + context.getEPackage().getName());
		}
		var type = FACTORY.createClassifierType();
		type.setReferredClassifier(classifier);
		type.setName(classifier.getName());
		return type;
	}

	// --- functions ---

	@Override
	public OclExpression visitFunctionCall(ODataFilterParser.FunctionCallContext ctx) {
		String odataName = ctx.IDENT().getText();
		String oclName = FUNCTIONS.get(odataName.toLowerCase()); // 4.01: names are case-insensitive
		if (oclName == null) {
			throw new ODataQueryParseException("unknown function '" + odataName + "'");
		}
		List<ODataFilterParser.ExprContext> args = ctx.expr();
		if (args.isEmpty()) {
			throw new ODataQueryParseException("function '" + odataName + "' needs at least one argument");
		}
		OperationCallExp exp = FACTORY.createOperationCallExp();
		exp.setName(oclName);
		exp.setOwnedSource(visit(args.get(0)));
		for (int i = 1; i < args.size(); i++) {
			exp.getOwnedArguments().add(visit(args.get(i)));
		}
		return exp;
	}

	// --- member paths + lambdas + bound calls ---

	@Override
	public OclExpression visitMemberPath(ODataFilterParser.MemberPathContext ctx) {
		OclExpression source = null;
		EClass current = context;
		EClassifier valueType = null;
		boolean many = false;
		List<ODataFilterParser.PathSegmentContext> segments = ctx.pathSegment();
		int start = 0;

		if (segments.get(0) instanceof ODataFilterParser.PropertyPathSegmentContext first) {
			String name = first.IDENT().getText();
			LambdaScope scope = scopeOf(name);
			if (scope != null) { // first segment is a lambda variable, not a property
				VariableExp variable = FACTORY.createVariableExp();
				variable.setReferredVariable(scope.variable());
				source = variable;
				current = scope.elementClass();
				start = 1;
			} else if (aliases.containsKey(name)) { // $apply alias reference
				VariableExp variable = FACTORY.createVariableExp();
				variable.setReferredVariable(aliases.get(name));
				source = variable;
				current = null;
				start = 1;
			}
		}

		for (int i = start; i < segments.size(); i++) {
			if (segments.get(i) instanceof ODataFilterParser.BoundCallSegmentContext bound) {
				source = boundCall(bound.boundCall(), source, current);
				EOperation operation = resolveOperation(bound.boundCall(), current);
				valueType = operation.getEType();
				many = operation.isMany();
			} else {
				String name = ((ODataFilterParser.PropertyPathSegmentContext) segments.get(i))
						.IDENT().getText();
				if (current == null) {
					throw new ODataQueryParseException("cannot navigate into '" + name
							+ "' — the previous segment is not a structured type");
				}
				EStructuralFeature feature = current.getEStructuralFeature(name);
				if (feature == null) {
					throw new ODataQueryParseException(
							"unknown property '" + name + "' on " + current.getName());
				}
				PropertyCallExp call = FACTORY.createPropertyCallExp();
				call.setReferredProperty(feature);
				if (source == null) {
					call.setIsImplicit(true); // source is the implicit iteration variable (self)
				} else {
					call.setOwnedSource(source);
				}
				source = call;
				valueType = feature.getEType();
				many = feature.isMany();
			}
			current = valueType instanceof EClass structured ? structured : null;
		}

		if (ctx.lambdaCall() != null) {
			return lambda(ctx.lambdaCall(), source, valueType, many);
		}
		if (ctx.COUNT() != null) { // path/$count → size (E4-AP-8)
			if (!many) {
				throw new ODataQueryParseException("'$count' requires a collection-valued path");
			}
			OperationCallExp size = FACTORY.createOperationCallExp();
			size.setName("size");
			size.setOwnedSource(source);
			return size;
		}
		return source;
	}

	/**
	 * Bound/composed function call in a member path (E4-AP-10): resolved against the current
	 * type's {@link EOperation}s (the same source the E1 operation profiles read), arguments
	 * mapped into declaration order — named form for model operations, positional as sent.
	 * The qualified name stays on the {@link OperationCallExp} for the backend dispatch.
	 */
	private OclExpression boundCall(ODataFilterParser.BoundCallContext ctx, OclExpression source,
			EClass current) {
		EOperation operation = resolveOperation(ctx, current);
		OperationCallExp call = FACTORY.createOperationCallExp();
		call.setName(qualifiedName(ctx));
		if (source == null) {
			call.setIsImplicit(true);
		} else {
			call.setOwnedSource(source);
		}
		ODataFilterParser.BoundCallArgsContext args = ctx.boundCallArgs();
		if (args != null && !args.namedArg().isEmpty()) {
			Map<String, ODataFilterParser.ExprContext> byName = new HashMap<>();
			for (ODataFilterParser.NamedArgContext named : args.namedArg()) {
				if (byName.put(named.IDENT().getText(), named.expr()) != null) {
					throw new ODataQueryParseException(
							"duplicate parameter '" + named.IDENT().getText() + "'");
				}
			}
			for (EParameter parameter : operation.getEParameters()) {
				ODataFilterParser.ExprContext expr = byName.remove(parameter.getName());
				if (expr == null) {
					throw new ODataQueryParseException("missing parameter '" + parameter.getName()
							+ "' of operation " + operation.getName());
				}
				call.getOwnedArguments().add(visit(expr));
			}
			if (!byName.isEmpty()) {
				throw new ODataQueryParseException("unknown parameter '"
						+ byName.keySet().iterator().next() + "' of operation " + operation.getName());
			}
		} else {
			List<ODataFilterParser.ExprContext> positional =
					args == null ? List.of() : args.expr();
			if (positional.size() != operation.getEParameters().size()) {
				throw new ODataQueryParseException("operation " + operation.getName() + " takes "
						+ operation.getEParameters().size() + " parameters");
			}
			for (ODataFilterParser.ExprContext expr : positional) {
				call.getOwnedArguments().add(visit(expr));
			}
		}
		return call;
	}

	private EOperation resolveOperation(ODataFilterParser.BoundCallContext ctx, EClass current) {
		String qualified = qualifiedName(ctx);
		String simple = qualified.substring(qualified.lastIndexOf('.') + 1);
		if (current == null) {
			throw new ODataQueryParseException("cannot call '" + qualified
					+ "' — the previous segment is not a structured type");
		}
		return current.getEAllOperations().stream()
				.filter(operation -> simple.equals(operation.getName())).findFirst()
				.orElseThrow(() -> new ODataQueryParseException(
						"unknown bound operation '" + qualified + "' on " + current.getName()));
	}

	private static String qualifiedName(ODataFilterParser.BoundCallContext ctx) {
		return ctx.IDENT().stream().map(TerminalNode::getText)
				.reduce((a, b) -> a + "." + b).orElseThrow();
	}

	/** OData lambda → OCL iterator: any(v: …) → exists, all(v: …) → forAll, any() → notEmpty. */
	private OclExpression lambda(ODataFilterParser.LambdaCallContext ctx, OclExpression source,
			EClassifier elementType, boolean many) {
		if (!many || elementType == null) {
			throw new ODataQueryParseException(
					"'" + ctx.op.getText() + "' requires a collection-valued path");
		}
		if (ctx.expr() == null) {
			if ("all".equals(ctx.op.getText())) {
				throw new ODataQueryParseException("'all' requires a lambda expression");
			}
			OperationCallExp notEmpty = FACTORY.createOperationCallExp();
			notEmpty.setName("notEmpty"); // parameterless any() = "collection has members"
			notEmpty.setOwnedSource(source);
			return notEmpty;
		}

		Variable variable = FACTORY.createVariable();
		variable.setName(ctx.IDENT().getText());
		var variableType = FACTORY.createClassifierType();
		variableType.setReferredClassifier(elementType);
		variableType.setName(elementType.getName());
		variable.setType(variableType);

		scopes.push(new LambdaScope(variable.getName(), variable,
				elementType instanceof EClass structured ? structured : null));
		OclExpression body;
		try {
			body = visit(ctx.expr());
		} finally {
			scopes.pop();
		}

		IteratorExp iterator = FACTORY.createIteratorExp();
		iterator.setName("any".equals(ctx.op.getText()) ? "exists" : "forAll");
		iterator.setOwnedSource(source);
		iterator.getOwnedIterators().add(variable);
		iterator.setOwnedBody(body);
		return iterator;
	}

	private LambdaScope scopeOf(String name) {
		return scopes.stream().filter(s -> s.name().equals(name)).findFirst().orElse(null);
	}

	// --- literals ---

	@Override
	public OclExpression visitStringLiteral(ODataFilterParser.StringLiteralContext ctx) {
		String raw = ctx.getText();
		StringLiteralExp exp = FACTORY.createStringLiteralExp();
		exp.setStringSymbol(raw.substring(1, raw.length() - 1).replace("''", "'"));
		return exp;
	}

	@Override
	public OclExpression visitIntLiteral(ODataFilterParser.IntLiteralContext ctx) {
		IntegerLiteralExp exp = FACTORY.createIntegerLiteralExp();
		String literal = ctx.getText();
		try {
			exp.setIntegerSymbol(Long.parseLong(literal));
		} catch (NumberFormatException e) {
			throw new ODataQueryParseException(
					"integer literal is out of the supported 64-bit range: " + literal, e);
		}
		return exp;
	}

	@Override
	public OclExpression visitDecimalLiteral(ODataFilterParser.DecimalLiteralContext ctx) {
		RealLiteralExp exp = FACTORY.createRealLiteralExp();
		String literal = ctx.getText();
		try {
			exp.setRealSymbol(Double.parseDouble(literal));
		} catch (NumberFormatException e) {
			throw new ODataQueryParseException("decimal literal is not a valid number: " + literal, e);
		}
		return exp;
	}

	@Override
	public OclExpression visitBooleanLiteral(ODataFilterParser.BooleanLiteralContext ctx) {
		BooleanLiteralExp exp = FACTORY.createBooleanLiteralExp();
		exp.setBooleanSymbol(Boolean.parseBoolean(ctx.getText()));
		return exp;
	}

	@Override
	public OclExpression visitNullLiteral(ODataFilterParser.NullLiteralContext ctx) {
		return FACTORY.createNullLiteralExp();
	}

	// OData-typed literals without an OCL literal class keep their lexical form as a string
	// literal, pre-typed with the Edm-ish primitive name — the backend translators dispatch
	// on that type name, the OclTypeResolver leaves pre-typed nodes untouched.

	@Override
	public OclExpression visitGuidLiteral(ODataFilterParser.GuidLiteralContext ctx) {
		return typedString(ctx.getText(), "Guid");
	}

	@Override
	public OclExpression visitDateLiteral(ODataFilterParser.DateLiteralContext ctx) {
		return typedString(ctx.getText(), "Date");
	}

	@Override
	public OclExpression visitDateTimeOffsetLiteral(ODataFilterParser.DateTimeOffsetLiteralContext ctx) {
		return typedString(ctx.getText(), "DateTimeOffset");
	}

	@Override
	public OclExpression visitTimeOfDayLiteral(ODataFilterParser.TimeOfDayLiteralContext ctx) {
		return typedString(ctx.getText(), "TimeOfDay");
	}

	@Override
	public OclExpression visitDurationLiteral(ODataFilterParser.DurationLiteralContext ctx) {
		String raw = ctx.getText(); // duration'P12DT23H59M59S'
		return typedString(raw.substring("duration'".length(), raw.length() - 1), "Duration");
	}

	/** {@code Ns.Enum'Value'} resolved against the context package (flag combinations: E4 backlog). */
	@Override
	public OclExpression visitEnumLiteral(ODataFilterParser.EnumLiteralContext ctx) {
		String raw = ctx.getText();
		int quote = raw.indexOf('\'');
		String qualifiedName = raw.substring(0, quote);
		String value = raw.substring(quote + 1, raw.length() - 1);
		if (value.contains(",")) {
			throw new ODataQueryParseException("enum flag combinations are not supported yet: " + raw);
		}
		String enumName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
		if (!(context.getEPackage().getEClassifier(enumName) instanceof EEnum eEnum)) {
			throw new ODataQueryParseException(
					"unknown enum type '" + qualifiedName + "' in package " + context.getEPackage().getName());
		}
		EEnumLiteral literal = eEnum.getEEnumLiteral(value);
		if (literal == null) {
			throw new ODataQueryParseException("unknown literal '" + value + "' of enum " + enumName);
		}
		EnumLiteralExp exp = FACTORY.createEnumLiteralExp();
		exp.setReferredLiteral(literal);
		return exp;
	}

	private OclExpression typedString(String value, String typeName) {
		StringLiteralExp exp = FACTORY.createStringLiteralExp();
		exp.setStringSymbol(value);
		var type = FACTORY.createPrimitiveType();
		type.setName(typeName);
		exp.setType(type);
		return exp;
	}

	// --- helpers ---

	private OclExpression foldLeft(String opName, List<? extends ParserRuleContext> operands) {
		OclExpression result = visit(operands.get(0));
		for (int i = 1; i < operands.size(); i++) {
			result = binary(opName, result, visit(operands.get(i)));
		}
		return result;
	}

	private OperationCallExp binary(String name, OclExpression source, OclExpression argument) {
		OperationCallExp exp = FACTORY.createOperationCallExp();
		exp.setName(name);
		exp.setOwnedSource(source);
		exp.getOwnedArguments().add(argument);
		return exp;
	}
}
