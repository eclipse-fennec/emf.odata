// Generated from /opt/git/fennec-odata/fennec-odata/org.eclipse.fennec.odata.query/grammar/ODataFilter.g4 by ANTLR 4.13.2
package org.eclipse.fennec.odata.query.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ODataFilterParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ODataFilterVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#filter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilter(ODataFilterParser.FilterContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#orderby}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderby(ODataFilterParser.OrderbyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#orderbyItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderbyItem(ODataFilterParser.OrderbyItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#resource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource(ODataFilterParser.ResourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#keyPredicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyPredicate(ODataFilterParser.KeyPredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#keyLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeyLiteral(ODataFilterParser.KeyLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PropertySegment}
	 * labeled alternative in {@link ODataFilterParser#resourceSegment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPropertySegment(ODataFilterParser.PropertySegmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code CountSegment}
	 * labeled alternative in {@link ODataFilterParser#resourceSegment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountSegment(ODataFilterParser.CountSegmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ValueSegment}
	 * labeled alternative in {@link ODataFilterParser#resourceSegment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueSegment(ODataFilterParser.ValueSegmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code RefSegment}
	 * labeled alternative in {@link ODataFilterParser#resourceSegment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRefSegment(ODataFilterParser.RefSegmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#apply}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitApply(ODataFilterParser.ApplyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code GroupByTrafo}
	 * labeled alternative in {@link ODataFilterParser#applyTrafo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByTrafo(ODataFilterParser.GroupByTrafoContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AggregateTrafo}
	 * labeled alternative in {@link ODataFilterParser#applyTrafo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregateTrafo(ODataFilterParser.AggregateTrafoContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ComputeTrafo}
	 * labeled alternative in {@link ODataFilterParser#applyTrafo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComputeTrafo(ODataFilterParser.ComputeTrafoContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FilterTrafo}
	 * labeled alternative in {@link ODataFilterParser#applyTrafo}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilterTrafo(ODataFilterParser.FilterTrafoContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AggregateWithItem}
	 * labeled alternative in {@link ODataFilterParser#aggregateItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregateWithItem(ODataFilterParser.AggregateWithItemContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AggregateCountItem}
	 * labeled alternative in {@link ODataFilterParser#aggregateItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregateCountItem(ODataFilterParser.AggregateCountItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#computeItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComputeItem(ODataFilterParser.ComputeItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(ODataFilterParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#orExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpr(ODataFilterParser.OrExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#andExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpr(ODataFilterParser.AndExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NotExpression}
	 * labeled alternative in {@link ODataFilterParser#notExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpression(ODataFilterParser.NotExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ComparisonLevel}
	 * labeled alternative in {@link ODataFilterParser#notExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonLevel(ODataFilterParser.ComparisonLevelContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BinaryComparison}
	 * labeled alternative in {@link ODataFilterParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryComparison(ODataFilterParser.BinaryComparisonContext ctx);
	/**
	 * Visit a parse tree produced by the {@code InListComparison}
	 * labeled alternative in {@link ODataFilterParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInListComparison(ODataFilterParser.InListComparisonContext ctx);
	/**
	 * Visit a parse tree produced by the {@code InEmptyListComparison}
	 * labeled alternative in {@link ODataFilterParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInEmptyListComparison(ODataFilterParser.InEmptyListComparisonContext ctx);
	/**
	 * Visit a parse tree produced by the {@code InComparison}
	 * labeled alternative in {@link ODataFilterParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInComparison(ODataFilterParser.InComparisonContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PassThrough}
	 * labeled alternative in {@link ODataFilterParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPassThrough(ODataFilterParser.PassThroughContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ToMultiplicative}
	 * labeled alternative in {@link ODataFilterParser#additive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToMultiplicative(ODataFilterParser.ToMultiplicativeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link ODataFilterParser#additive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAddSub(ODataFilterParser.AddSubContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MulDivMod}
	 * labeled alternative in {@link ODataFilterParser#multiplicative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMulDivMod(ODataFilterParser.MulDivModContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ToPrimary}
	 * labeled alternative in {@link ODataFilterParser#multiplicative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToPrimary(ODataFilterParser.ToPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LiteralPrimary}
	 * labeled alternative in {@link ODataFilterParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralPrimary(ODataFilterParser.LiteralPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TypeFuncPrimary}
	 * labeled alternative in {@link ODataFilterParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeFuncPrimary(ODataFilterParser.TypeFuncPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FunctionPrimary}
	 * labeled alternative in {@link ODataFilterParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionPrimary(ODataFilterParser.FunctionPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MemberPrimary}
	 * labeled alternative in {@link ODataFilterParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberPrimary(ODataFilterParser.MemberPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AliasPrimary}
	 * labeled alternative in {@link ODataFilterParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAliasPrimary(ODataFilterParser.AliasPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParenPrimary}
	 * labeled alternative in {@link ODataFilterParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenPrimary(ODataFilterParser.ParenPrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#typeFunc}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeFunc(ODataFilterParser.TypeFuncContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#qualifiedTypeName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualifiedTypeName(ODataFilterParser.QualifiedTypeNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(ODataFilterParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#memberPath}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberPath(ODataFilterParser.MemberPathContext ctx);
	/**
	 * Visit a parse tree produced by {@link ODataFilterParser#lambdaCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaCall(ODataFilterParser.LambdaCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StringLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(ODataFilterParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code DecimalLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimalLiteral(ODataFilterParser.DecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IntLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntLiteral(ODataFilterParser.IntLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BooleanLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(ODataFilterParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NullLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullLiteral(ODataFilterParser.NullLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code GuidLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGuidLiteral(ODataFilterParser.GuidLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code DateTimeOffsetLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateTimeOffsetLiteral(ODataFilterParser.DateTimeOffsetLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code DateLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDateLiteral(ODataFilterParser.DateLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code TimeOfDayLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTimeOfDayLiteral(ODataFilterParser.TimeOfDayLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code DurationLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDurationLiteral(ODataFilterParser.DurationLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EnumLiteral}
	 * labeled alternative in {@link ODataFilterParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEnumLiteral(ODataFilterParser.EnumLiteralContext ctx);
}