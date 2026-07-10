// Generated from grammar/ODataFilter.g4 by ANTLR 4.13.2
package org.eclipse.fennec.odata.query.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class ODataFilterParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		OR=1, AND=2, NOT=3, EQ=4, NE=5, GT=6, GE=7, LT=8, LE=9, HAS=10, IN=11, 
		ADD=12, SUB=13, MUL=14, DIVBY=15, DIV=16, MOD=17, ASC=18, DESC=19, TRUE=20, 
		FALSE=21, NULL=22, ANY=23, ALL=24, CAST=25, ISOF=26, COUNT=27, VALUE=28, 
		REF=29, WITH=30, AS=31, FROM=32, LPAREN=33, RPAREN=34, COMMA=35, EQUALS=36, 
		SLASH=37, COLON=38, DOT=39, DURATION=40, ENUM=41, GUID=42, DATETIMEOFFSET=43, 
		DATE=44, TIMEOFDAY=45, STRING=46, DECIMAL=47, INT=48, IDENT=49, ALIAS=50, 
		WS=51;
	public static final int
		RULE_filter = 0, RULE_orderby = 1, RULE_orderbyItem = 2, RULE_resource = 3, 
		RULE_keyPredicate = 4, RULE_namedKeyValue = 5, RULE_keyLiteral = 6, RULE_resourceSegment = 7, 
		RULE_castName = 8, RULE_apply = 9, RULE_applySeq = 10, RULE_applyTrafo = 11, 
		RULE_groupbyElement = 12, RULE_aggregateItem = 13, RULE_methodName = 14, 
		RULE_aggrFrom = 15, RULE_customFrom = 16, RULE_computeItem = 17, RULE_expr = 18, 
		RULE_orExpr = 19, RULE_andExpr = 20, RULE_notExpr = 21, RULE_comparison = 22, 
		RULE_additive = 23, RULE_multiplicative = 24, RULE_primary = 25, RULE_typeFunc = 26, 
		RULE_qualifiedTypeName = 27, RULE_functionCall = 28, RULE_memberPath = 29, 
		RULE_pathSegment = 30, RULE_boundCall = 31, RULE_boundCallArgs = 32, RULE_namedArg = 33, 
		RULE_lambdaCall = 34, RULE_literal = 35;
	private static String[] makeRuleNames() {
		return new String[] {
			"filter", "orderby", "orderbyItem", "resource", "keyPredicate", "namedKeyValue", 
			"keyLiteral", "resourceSegment", "castName", "apply", "applySeq", "applyTrafo", 
			"groupbyElement", "aggregateItem", "methodName", "aggrFrom", "customFrom", 
			"computeItem", "expr", "orExpr", "andExpr", "notExpr", "comparison", 
			"additive", "multiplicative", "primary", "typeFunc", "qualifiedTypeName", 
			"functionCall", "memberPath", "pathSegment", "boundCall", "boundCallArgs", 
			"namedArg", "lambdaCall", "literal"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, "'true'", "'false'", 
			"'null'", "'any'", "'all'", null, null, "'$count'", "'$value'", "'$ref'", 
			"'with'", "'as'", "'from'", "'('", "')'", "','", "'='", "'/'", "':'", 
			"'.'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "OR", "AND", "NOT", "EQ", "NE", "GT", "GE", "LT", "LE", "HAS", 
			"IN", "ADD", "SUB", "MUL", "DIVBY", "DIV", "MOD", "ASC", "DESC", "TRUE", 
			"FALSE", "NULL", "ANY", "ALL", "CAST", "ISOF", "COUNT", "VALUE", "REF", 
			"WITH", "AS", "FROM", "LPAREN", "RPAREN", "COMMA", "EQUALS", "SLASH", 
			"COLON", "DOT", "DURATION", "ENUM", "GUID", "DATETIMEOFFSET", "DATE", 
			"TIMEOFDAY", "STRING", "DECIMAL", "INT", "IDENT", "ALIAS", "WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "ODataFilter.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


		/** Soft-keyword gate: the upcoming identifier selects the apply transformation. */
		private boolean trafo(String name) {
			return name.equals(_input.LT(1).getText());
		}

		private boolean bottomTopTrafo() {
			return switch (_input.LT(1).getText()) {
				case "topcount", "topsum", "toppercent", "bottomcount", "bottomsum", "bottompercent" -> true;
				default -> false;
			};
		}

	public ODataFilterParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FilterContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ODataFilterParser.EOF, 0); }
		public FilterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filter; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitFilter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterContext filter() throws RecognitionException {
		FilterContext _localctx = new FilterContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_filter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(72);
			expr();
			setState(73);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrderbyContext extends ParserRuleContext {
		public List<OrderbyItemContext> orderbyItem() {
			return getRuleContexts(OrderbyItemContext.class);
		}
		public OrderbyItemContext orderbyItem(int i) {
			return getRuleContext(OrderbyItemContext.class,i);
		}
		public TerminalNode EOF() { return getToken(ODataFilterParser.EOF, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public OrderbyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderby; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitOrderby(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderbyContext orderby() throws RecognitionException {
		OrderbyContext _localctx = new OrderbyContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_orderby);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			orderbyItem();
			setState(80);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(76);
				match(COMMA);
				setState(77);
				orderbyItem();
				}
				}
				setState(82);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(83);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrderbyItemContext extends ParserRuleContext {
		public Token direction;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ASC() { return getToken(ODataFilterParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(ODataFilterParser.DESC, 0); }
		public OrderbyItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderbyItem; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitOrderbyItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderbyItemContext orderbyItem() throws RecognitionException {
		OrderbyItemContext _localctx = new OrderbyItemContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_orderbyItem);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(85);
			expr();
			setState(87);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(86);
				((OrderbyItemContext)_localctx).direction = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==ASC || _la==DESC) ) {
					((OrderbyItemContext)_localctx).direction = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ResourceContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public TerminalNode EOF() { return getToken(ODataFilterParser.EOF, 0); }
		public KeyPredicateContext keyPredicate() {
			return getRuleContext(KeyPredicateContext.class,0);
		}
		public List<TerminalNode> SLASH() { return getTokens(ODataFilterParser.SLASH); }
		public TerminalNode SLASH(int i) {
			return getToken(ODataFilterParser.SLASH, i);
		}
		public List<ResourceSegmentContext> resourceSegment() {
			return getRuleContexts(ResourceSegmentContext.class);
		}
		public ResourceSegmentContext resourceSegment(int i) {
			return getRuleContext(ResourceSegmentContext.class,i);
		}
		public ResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitResource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_resource);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			match(IDENT);
			setState(91);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(90);
				keyPredicate();
				}
			}

			setState(97);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SLASH) {
				{
				{
				setState(93);
				match(SLASH);
				setState(94);
				resourceSegment();
				}
				}
				setState(99);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(100);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class KeyPredicateContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public KeyLiteralContext keyLiteral() {
			return getRuleContext(KeyLiteralContext.class,0);
		}
		public List<NamedKeyValueContext> namedKeyValue() {
			return getRuleContexts(NamedKeyValueContext.class);
		}
		public NamedKeyValueContext namedKeyValue(int i) {
			return getRuleContext(NamedKeyValueContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public KeyPredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyPredicate; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitKeyPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyPredicateContext keyPredicate() throws RecognitionException {
		KeyPredicateContext _localctx = new KeyPredicateContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_keyPredicate);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(102);
			match(LPAREN);
			setState(112);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GUID:
			case DATETIMEOFFSET:
			case DATE:
			case TIMEOFDAY:
			case STRING:
			case DECIMAL:
			case INT:
				{
				setState(103);
				keyLiteral();
				}
				break;
			case IDENT:
				{
				setState(104);
				namedKeyValue();
				setState(109);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(105);
					match(COMMA);
					setState(106);
					namedKeyValue();
					}
					}
					setState(111);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(114);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NamedKeyValueContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public TerminalNode EQUALS() { return getToken(ODataFilterParser.EQUALS, 0); }
		public KeyLiteralContext keyLiteral() {
			return getRuleContext(KeyLiteralContext.class,0);
		}
		public NamedKeyValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedKeyValue; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitNamedKeyValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedKeyValueContext namedKeyValue() throws RecognitionException {
		NamedKeyValueContext _localctx = new NamedKeyValueContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_namedKeyValue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(116);
			match(IDENT);
			setState(117);
			match(EQUALS);
			setState(118);
			keyLiteral();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class KeyLiteralContext extends ParserRuleContext {
		public TerminalNode STRING() { return getToken(ODataFilterParser.STRING, 0); }
		public TerminalNode INT() { return getToken(ODataFilterParser.INT, 0); }
		public TerminalNode DECIMAL() { return getToken(ODataFilterParser.DECIMAL, 0); }
		public TerminalNode GUID() { return getToken(ODataFilterParser.GUID, 0); }
		public TerminalNode DATETIMEOFFSET() { return getToken(ODataFilterParser.DATETIMEOFFSET, 0); }
		public TerminalNode DATE() { return getToken(ODataFilterParser.DATE, 0); }
		public TerminalNode TIMEOFDAY() { return getToken(ODataFilterParser.TIMEOFDAY, 0); }
		public KeyLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyLiteral; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitKeyLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final KeyLiteralContext keyLiteral() throws RecognitionException {
		KeyLiteralContext _localctx = new KeyLiteralContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_keyLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(120);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 558551906910208L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ResourceSegmentContext extends ParserRuleContext {
		public ResourceSegmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resourceSegment; }
	 
		public ResourceSegmentContext() { }
		public void copyFrom(ResourceSegmentContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CountSegmentContext extends ResourceSegmentContext {
		public TerminalNode COUNT() { return getToken(ODataFilterParser.COUNT, 0); }
		public CountSegmentContext(ResourceSegmentContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitCountSegment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class KeyValueSegmentContext extends ResourceSegmentContext {
		public KeyLiteralContext keyLiteral() {
			return getRuleContext(KeyLiteralContext.class,0);
		}
		public KeyValueSegmentContext(ResourceSegmentContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitKeyValueSegment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CastSegmentContext extends ResourceSegmentContext {
		public CastNameContext castName() {
			return getRuleContext(CastNameContext.class,0);
		}
		public KeyPredicateContext keyPredicate() {
			return getRuleContext(KeyPredicateContext.class,0);
		}
		public CastSegmentContext(ResourceSegmentContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitCastSegment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PropertySegmentContext extends ResourceSegmentContext {
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public KeyPredicateContext keyPredicate() {
			return getRuleContext(KeyPredicateContext.class,0);
		}
		public PropertySegmentContext(ResourceSegmentContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitPropertySegment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ValueSegmentContext extends ResourceSegmentContext {
		public TerminalNode VALUE() { return getToken(ODataFilterParser.VALUE, 0); }
		public ValueSegmentContext(ResourceSegmentContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitValueSegment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RefSegmentContext extends ResourceSegmentContext {
		public TerminalNode REF() { return getToken(ODataFilterParser.REF, 0); }
		public RefSegmentContext(ResourceSegmentContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitRefSegment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceSegmentContext resourceSegment() throws RecognitionException {
		ResourceSegmentContext _localctx = new ResourceSegmentContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_resourceSegment);
		int _la;
		try {
			setState(134);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				_localctx = new CastSegmentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(122);
				castName();
				setState(124);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(123);
					keyPredicate();
					}
				}

				}
				break;
			case 2:
				_localctx = new PropertySegmentContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(126);
				match(IDENT);
				setState(128);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(127);
					keyPredicate();
					}
				}

				}
				break;
			case 3:
				_localctx = new CountSegmentContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(130);
				match(COUNT);
				}
				break;
			case 4:
				_localctx = new ValueSegmentContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(131);
				match(VALUE);
				}
				break;
			case 5:
				_localctx = new RefSegmentContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(132);
				match(REF);
				}
				break;
			case 6:
				_localctx = new KeyValueSegmentContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(133);
				keyLiteral();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CastNameContext extends ParserRuleContext {
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public List<TerminalNode> DOT() { return getTokens(ODataFilterParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ODataFilterParser.DOT, i);
		}
		public CastNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_castName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitCastName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CastNameContext castName() throws RecognitionException {
		CastNameContext _localctx = new CastNameContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_castName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			match(IDENT);
			setState(139); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(137);
				match(DOT);
				setState(138);
				match(IDENT);
				}
				}
				setState(141); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==DOT );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ApplyContext extends ParserRuleContext {
		public ApplySeqContext applySeq() {
			return getRuleContext(ApplySeqContext.class,0);
		}
		public TerminalNode EOF() { return getToken(ODataFilterParser.EOF, 0); }
		public ApplyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_apply; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitApply(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ApplyContext apply() throws RecognitionException {
		ApplyContext _localctx = new ApplyContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_apply);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			applySeq();
			setState(144);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ApplySeqContext extends ParserRuleContext {
		public List<ApplyTrafoContext> applyTrafo() {
			return getRuleContexts(ApplyTrafoContext.class);
		}
		public ApplyTrafoContext applyTrafo(int i) {
			return getRuleContext(ApplyTrafoContext.class,i);
		}
		public List<TerminalNode> SLASH() { return getTokens(ODataFilterParser.SLASH); }
		public TerminalNode SLASH(int i) {
			return getToken(ODataFilterParser.SLASH, i);
		}
		public ApplySeqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_applySeq; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitApplySeq(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ApplySeqContext applySeq() throws RecognitionException {
		ApplySeqContext _localctx = new ApplySeqContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_applySeq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			applyTrafo();
			setState(151);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SLASH) {
				{
				{
				setState(147);
				match(SLASH);
				setState(148);
				applyTrafo();
				}
				}
				setState(153);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ApplyTrafoContext extends ParserRuleContext {
		public ApplyTrafoContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_applyTrafo; }
	 
		public ApplyTrafoContext() { }
		public void copyFrom(ApplyTrafoContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BottomTopTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(ODataFilterParser.COMMA, 0); }
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public BottomTopTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitBottomTopTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AggregateTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public List<AggregateItemContext> aggregateItem() {
			return getRuleContexts(AggregateItemContext.class);
		}
		public AggregateItemContext aggregateItem(int i) {
			return getRuleContext(AggregateItemContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public AggregateTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAggregateTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IdentityTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public IdentityTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitIdentityTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FilterTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public FilterTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitFilterTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RowLimitTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public TerminalNode INT() { return getToken(ODataFilterParser.INT, 0); }
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public RowLimitTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitRowLimitTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConcatTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public List<ApplySeqContext> applySeq() {
			return getRuleContexts(ApplySeqContext.class);
		}
		public ApplySeqContext applySeq(int i) {
			return getRuleContext(ApplySeqContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public ConcatTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitConcatTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ComputeTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public List<ComputeItemContext> computeItem() {
			return getRuleContexts(ComputeItemContext.class);
		}
		public ComputeItemContext computeItem(int i) {
			return getRuleContext(ComputeItemContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public ComputeTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitComputeTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class OrderByTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public List<OrderbyItemContext> orderbyItem() {
			return getRuleContexts(OrderbyItemContext.class);
		}
		public OrderbyItemContext orderbyItem(int i) {
			return getRuleContext(OrderbyItemContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public OrderByTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitOrderByTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GroupByTrafoContext extends ApplyTrafoContext {
		public Token name;
		public List<TerminalNode> LPAREN() { return getTokens(ODataFilterParser.LPAREN); }
		public TerminalNode LPAREN(int i) {
			return getToken(ODataFilterParser.LPAREN, i);
		}
		public List<GroupbyElementContext> groupbyElement() {
			return getRuleContexts(GroupbyElementContext.class);
		}
		public GroupbyElementContext groupbyElement(int i) {
			return getRuleContext(GroupbyElementContext.class,i);
		}
		public List<TerminalNode> RPAREN() { return getTokens(ODataFilterParser.RPAREN); }
		public TerminalNode RPAREN(int i) {
			return getToken(ODataFilterParser.RPAREN, i);
		}
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public ApplySeqContext applySeq() {
			return getRuleContext(ApplySeqContext.class,0);
		}
		public GroupByTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitGroupByTrafo(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ApplyTrafoContext applyTrafo() throws RecognitionException {
		ApplyTrafoContext _localctx = new ApplyTrafoContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_applyTrafo);
		int _la;
		try {
			setState(245);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				_localctx = new GroupByTrafoContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(154);
				if (!(trafo("groupby"))) throw new FailedPredicateException(this, "trafo(\"groupby\")");
				setState(155);
				((GroupByTrafoContext)_localctx).name = match(IDENT);
				setState(156);
				match(LPAREN);
				setState(157);
				match(LPAREN);
				setState(158);
				groupbyElement();
				setState(163);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(159);
					match(COMMA);
					setState(160);
					groupbyElement();
					}
					}
					setState(165);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(166);
				match(RPAREN);
				setState(169);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(167);
					match(COMMA);
					setState(168);
					applySeq();
					}
				}

				setState(171);
				match(RPAREN);
				}
				break;
			case 2:
				_localctx = new AggregateTrafoContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(173);
				if (!(trafo("aggregate"))) throw new FailedPredicateException(this, "trafo(\"aggregate\")");
				setState(174);
				((AggregateTrafoContext)_localctx).name = match(IDENT);
				setState(175);
				match(LPAREN);
				setState(176);
				aggregateItem();
				setState(181);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(177);
					match(COMMA);
					setState(178);
					aggregateItem();
					}
					}
					setState(183);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(184);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new ComputeTrafoContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(186);
				if (!(trafo("compute"))) throw new FailedPredicateException(this, "trafo(\"compute\")");
				setState(187);
				((ComputeTrafoContext)_localctx).name = match(IDENT);
				setState(188);
				match(LPAREN);
				setState(189);
				computeItem();
				setState(194);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(190);
					match(COMMA);
					setState(191);
					computeItem();
					}
					}
					setState(196);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(197);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new ConcatTrafoContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(199);
				if (!(trafo("concat"))) throw new FailedPredicateException(this, "trafo(\"concat\")");
				setState(200);
				((ConcatTrafoContext)_localctx).name = match(IDENT);
				setState(201);
				match(LPAREN);
				setState(202);
				applySeq();
				setState(205); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(203);
					match(COMMA);
					setState(204);
					applySeq();
					}
					}
					setState(207); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(209);
				match(RPAREN);
				}
				break;
			case 5:
				_localctx = new FilterTrafoContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(211);
				if (!(trafo("filter"))) throw new FailedPredicateException(this, "trafo(\"filter\")");
				setState(212);
				((FilterTrafoContext)_localctx).name = match(IDENT);
				setState(213);
				match(LPAREN);
				setState(214);
				expr();
				setState(215);
				match(RPAREN);
				}
				break;
			case 6:
				_localctx = new BottomTopTrafoContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(217);
				if (!(bottomTopTrafo())) throw new FailedPredicateException(this, "bottomTopTrafo()");
				setState(218);
				((BottomTopTrafoContext)_localctx).name = match(IDENT);
				setState(219);
				match(LPAREN);
				setState(220);
				expr();
				setState(221);
				match(COMMA);
				setState(222);
				expr();
				setState(223);
				match(RPAREN);
				}
				break;
			case 7:
				_localctx = new OrderByTrafoContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(225);
				if (!(trafo("orderby"))) throw new FailedPredicateException(this, "trafo(\"orderby\")");
				setState(226);
				((OrderByTrafoContext)_localctx).name = match(IDENT);
				setState(227);
				match(LPAREN);
				setState(228);
				orderbyItem();
				setState(233);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(229);
					match(COMMA);
					setState(230);
					orderbyItem();
					}
					}
					setState(235);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(236);
				match(RPAREN);
				}
				break;
			case 8:
				_localctx = new RowLimitTrafoContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(238);
				if (!(trafo("top") || trafo("skip"))) throw new FailedPredicateException(this, "trafo(\"top\") || trafo(\"skip\")");
				setState(239);
				((RowLimitTrafoContext)_localctx).name = match(IDENT);
				setState(240);
				match(LPAREN);
				setState(241);
				match(INT);
				setState(242);
				match(RPAREN);
				}
				break;
			case 9:
				_localctx = new IdentityTrafoContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(243);
				if (!(trafo("identity"))) throw new FailedPredicateException(this, "trafo(\"identity\")");
				setState(244);
				((IdentityTrafoContext)_localctx).name = match(IDENT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GroupbyElementContext extends ParserRuleContext {
		public GroupbyElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupbyElement; }
	 
		public GroupbyElementContext() { }
		public void copyFrom(GroupbyElementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PathElementContext extends GroupbyElementContext {
		public MemberPathContext memberPath() {
			return getRuleContext(MemberPathContext.class,0);
		}
		public PathElementContext(GroupbyElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitPathElement(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RollupElementContext extends GroupbyElementContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public List<MemberPathContext> memberPath() {
			return getRuleContexts(MemberPathContext.class);
		}
		public MemberPathContext memberPath(int i) {
			return getRuleContext(MemberPathContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public RollupElementContext(GroupbyElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitRollupElement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GroupbyElementContext groupbyElement() throws RecognitionException {
		GroupbyElementContext _localctx = new GroupbyElementContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_groupbyElement);
		int _la;
		try {
			setState(261);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				_localctx = new RollupElementContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(247);
				if (!(trafo("rollup"))) throw new FailedPredicateException(this, "trafo(\"rollup\")");
				setState(248);
				((RollupElementContext)_localctx).name = match(IDENT);
				setState(249);
				match(LPAREN);
				setState(250);
				memberPath();
				setState(255);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(251);
					match(COMMA);
					setState(252);
					memberPath();
					}
					}
					setState(257);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(258);
				match(RPAREN);
				}
				break;
			case 2:
				_localctx = new PathElementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(260);
				memberPath();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AggregateItemContext extends ParserRuleContext {
		public AggregateItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregateItem; }
	 
		public AggregateItemContext() { }
		public void copyFrom(AggregateItemContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AggregateWithItemContext extends AggregateItemContext {
		public MethodNameContext method;
		public Token alias;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode WITH() { return getToken(ODataFilterParser.WITH, 0); }
		public TerminalNode AS() { return getToken(ODataFilterParser.AS, 0); }
		public MethodNameContext methodName() {
			return getRuleContext(MethodNameContext.class,0);
		}
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public List<AggrFromContext> aggrFrom() {
			return getRuleContexts(AggrFromContext.class);
		}
		public AggrFromContext aggrFrom(int i) {
			return getRuleContext(AggrFromContext.class,i);
		}
		public AggregateWithItemContext(AggregateItemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAggregateWithItem(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AggregateCustomBareContext extends AggregateItemContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public AggregateCustomBareContext(AggregateItemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAggregateCustomBare(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AggregateCountItemContext extends AggregateItemContext {
		public Token alias;
		public TerminalNode COUNT() { return getToken(ODataFilterParser.COUNT, 0); }
		public TerminalNode AS() { return getToken(ODataFilterParser.AS, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public List<AggrFromContext> aggrFrom() {
			return getRuleContexts(AggrFromContext.class);
		}
		public AggrFromContext aggrFrom(int i) {
			return getRuleContext(AggrFromContext.class,i);
		}
		public AggregateCountItemContext(AggregateItemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAggregateCountItem(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AggregateCustomAliasedContext extends AggregateItemContext {
		public Token alias;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode AS() { return getToken(ODataFilterParser.AS, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public List<CustomFromContext> customFrom() {
			return getRuleContexts(CustomFromContext.class);
		}
		public CustomFromContext customFrom(int i) {
			return getRuleContext(CustomFromContext.class,i);
		}
		public AggregateCustomAliasedContext(AggregateItemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAggregateCustomAliased(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregateItemContext aggregateItem() throws RecognitionException {
		AggregateItemContext _localctx = new AggregateItemContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_aggregateItem);
		int _la;
		try {
			setState(295);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				_localctx = new AggregateWithItemContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(263);
				expr();
				setState(264);
				match(WITH);
				setState(265);
				((AggregateWithItemContext)_localctx).method = methodName();
				setState(269);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(266);
					aggrFrom();
					}
					}
					setState(271);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(272);
				match(AS);
				setState(273);
				((AggregateWithItemContext)_localctx).alias = match(IDENT);
				}
				break;
			case 2:
				_localctx = new AggregateCountItemContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(275);
				match(COUNT);
				setState(279);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(276);
					aggrFrom();
					}
					}
					setState(281);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(282);
				match(AS);
				setState(283);
				((AggregateCountItemContext)_localctx).alias = match(IDENT);
				}
				break;
			case 3:
				_localctx = new AggregateCustomAliasedContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(284);
				expr();
				setState(288);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(285);
					customFrom();
					}
					}
					setState(290);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(291);
				match(AS);
				setState(292);
				((AggregateCustomAliasedContext)_localctx).alias = match(IDENT);
				}
				break;
			case 4:
				_localctx = new AggregateCustomBareContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(294);
				expr();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MethodNameContext extends ParserRuleContext {
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public List<TerminalNode> DOT() { return getTokens(ODataFilterParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ODataFilterParser.DOT, i);
		}
		public MethodNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_methodName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitMethodName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MethodNameContext methodName() throws RecognitionException {
		MethodNameContext _localctx = new MethodNameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_methodName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(297);
			match(IDENT);
			setState(302);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(298);
				match(DOT);
				setState(299);
				match(IDENT);
				}
				}
				setState(304);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AggrFromContext extends ParserRuleContext {
		public MethodNameContext method;
		public TerminalNode FROM() { return getToken(ODataFilterParser.FROM, 0); }
		public List<MemberPathContext> memberPath() {
			return getRuleContexts(MemberPathContext.class);
		}
		public MemberPathContext memberPath(int i) {
			return getRuleContext(MemberPathContext.class,i);
		}
		public TerminalNode WITH() { return getToken(ODataFilterParser.WITH, 0); }
		public MethodNameContext methodName() {
			return getRuleContext(MethodNameContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public AggrFromContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggrFrom; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAggrFrom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggrFromContext aggrFrom() throws RecognitionException {
		AggrFromContext _localctx = new AggrFromContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_aggrFrom);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(305);
			match(FROM);
			setState(306);
			memberPath();
			setState(311);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(307);
				match(COMMA);
				setState(308);
				memberPath();
				}
				}
				setState(313);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(314);
			match(WITH);
			setState(315);
			((AggrFromContext)_localctx).method = methodName();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CustomFromContext extends ParserRuleContext {
		public MethodNameContext method;
		public TerminalNode FROM() { return getToken(ODataFilterParser.FROM, 0); }
		public List<MemberPathContext> memberPath() {
			return getRuleContexts(MemberPathContext.class);
		}
		public MemberPathContext memberPath(int i) {
			return getRuleContext(MemberPathContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public TerminalNode WITH() { return getToken(ODataFilterParser.WITH, 0); }
		public MethodNameContext methodName() {
			return getRuleContext(MethodNameContext.class,0);
		}
		public CustomFromContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_customFrom; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitCustomFrom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CustomFromContext customFrom() throws RecognitionException {
		CustomFromContext _localctx = new CustomFromContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_customFrom);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(317);
			match(FROM);
			setState(318);
			memberPath();
			setState(323);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(319);
				match(COMMA);
				setState(320);
				memberPath();
				}
				}
				setState(325);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(328);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(326);
				match(WITH);
				setState(327);
				((CustomFromContext)_localctx).method = methodName();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComputeItemContext extends ParserRuleContext {
		public Token alias;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode AS() { return getToken(ODataFilterParser.AS, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public ComputeItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_computeItem; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitComputeItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComputeItemContext computeItem() throws RecognitionException {
		ComputeItemContext _localctx = new ComputeItemContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_computeItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(330);
			expr();
			setState(331);
			match(AS);
			setState(332);
			((ComputeItemContext)_localctx).alias = match(IDENT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExprContext extends ParserRuleContext {
		public OrExprContext orExpr() {
			return getRuleContext(OrExprContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(334);
			orExpr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrExprContext extends ParserRuleContext {
		public List<AndExprContext> andExpr() {
			return getRuleContexts(AndExprContext.class);
		}
		public AndExprContext andExpr(int i) {
			return getRuleContext(AndExprContext.class,i);
		}
		public List<TerminalNode> OR() { return getTokens(ODataFilterParser.OR); }
		public TerminalNode OR(int i) {
			return getToken(ODataFilterParser.OR, i);
		}
		public OrExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orExpr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitOrExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrExprContext orExpr() throws RecognitionException {
		OrExprContext _localctx = new OrExprContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_orExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(336);
			andExpr();
			setState(341);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(337);
				match(OR);
				setState(338);
				andExpr();
				}
				}
				setState(343);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AndExprContext extends ParserRuleContext {
		public List<NotExprContext> notExpr() {
			return getRuleContexts(NotExprContext.class);
		}
		public NotExprContext notExpr(int i) {
			return getRuleContext(NotExprContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(ODataFilterParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(ODataFilterParser.AND, i);
		}
		public AndExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_andExpr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAndExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AndExprContext andExpr() throws RecognitionException {
		AndExprContext _localctx = new AndExprContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_andExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(344);
			notExpr();
			setState(349);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(345);
				match(AND);
				setState(346);
				notExpr();
				}
				}
				setState(351);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NotExprContext extends ParserRuleContext {
		public NotExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notExpr; }
	 
		public NotExprContext() { }
		public void copyFrom(NotExprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotExpressionContext extends NotExprContext {
		public TerminalNode NOT() { return getToken(ODataFilterParser.NOT, 0); }
		public NotExprContext notExpr() {
			return getRuleContext(NotExprContext.class,0);
		}
		public NotExpressionContext(NotExprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitNotExpression(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonLevelContext extends NotExprContext {
		public ComparisonContext comparison() {
			return getRuleContext(ComparisonContext.class,0);
		}
		public ComparisonLevelContext(NotExprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitComparisonLevel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NotExprContext notExpr() throws RecognitionException {
		NotExprContext _localctx = new NotExprContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_notExpr);
		try {
			setState(355);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				_localctx = new NotExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(352);
				match(NOT);
				setState(353);
				notExpr();
				}
				break;
			case TRUE:
			case FALSE:
			case NULL:
			case CAST:
			case ISOF:
			case LPAREN:
			case DURATION:
			case ENUM:
			case GUID:
			case DATETIMEOFFSET:
			case DATE:
			case TIMEOFDAY:
			case STRING:
			case DECIMAL:
			case INT:
			case IDENT:
			case ALIAS:
				_localctx = new ComparisonLevelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(354);
				comparison();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonContext extends ParserRuleContext {
		public ComparisonContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_comparison; }
	 
		public ComparisonContext() { }
		public void copyFrom(ComparisonContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BinaryComparisonContext extends ComparisonContext {
		public Token op;
		public List<AdditiveContext> additive() {
			return getRuleContexts(AdditiveContext.class);
		}
		public AdditiveContext additive(int i) {
			return getRuleContext(AdditiveContext.class,i);
		}
		public TerminalNode EQ() { return getToken(ODataFilterParser.EQ, 0); }
		public TerminalNode NE() { return getToken(ODataFilterParser.NE, 0); }
		public TerminalNode GT() { return getToken(ODataFilterParser.GT, 0); }
		public TerminalNode GE() { return getToken(ODataFilterParser.GE, 0); }
		public TerminalNode LT() { return getToken(ODataFilterParser.LT, 0); }
		public TerminalNode LE() { return getToken(ODataFilterParser.LE, 0); }
		public TerminalNode HAS() { return getToken(ODataFilterParser.HAS, 0); }
		public BinaryComparisonContext(ComparisonContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitBinaryComparison(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InComparisonContext extends ComparisonContext {
		public AdditiveContext additive() {
			return getRuleContext(AdditiveContext.class,0);
		}
		public TerminalNode IN() { return getToken(ODataFilterParser.IN, 0); }
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public InComparisonContext(ComparisonContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitInComparison(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PassThroughContext extends ComparisonContext {
		public AdditiveContext additive() {
			return getRuleContext(AdditiveContext.class,0);
		}
		public PassThroughContext(ComparisonContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitPassThrough(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InListComparisonContext extends ComparisonContext {
		public AdditiveContext additive() {
			return getRuleContext(AdditiveContext.class,0);
		}
		public TerminalNode IN() { return getToken(ODataFilterParser.IN, 0); }
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public List<LiteralContext> literal() {
			return getRuleContexts(LiteralContext.class);
		}
		public LiteralContext literal(int i) {
			return getRuleContext(LiteralContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public InListComparisonContext(ComparisonContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitInListComparison(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InEmptyListComparisonContext extends ComparisonContext {
		public AdditiveContext additive() {
			return getRuleContext(AdditiveContext.class,0);
		}
		public TerminalNode IN() { return getToken(ODataFilterParser.IN, 0); }
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public InEmptyListComparisonContext(ComparisonContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitInEmptyListComparison(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComparisonContext comparison() throws RecognitionException {
		ComparisonContext _localctx = new ComparisonContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_comparison);
		int _la;
		try {
			setState(385);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				_localctx = new BinaryComparisonContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(357);
				additive(0);
				setState(358);
				((BinaryComparisonContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 2032L) != 0)) ) {
					((BinaryComparisonContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(359);
				additive(0);
				}
				break;
			case 2:
				_localctx = new InListComparisonContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(361);
				additive(0);
				setState(362);
				match(IN);
				setState(363);
				match(LPAREN);
				setState(364);
				literal();
				setState(367); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(365);
					match(COMMA);
					setState(366);
					literal();
					}
					}
					setState(369); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(371);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new InEmptyListComparisonContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(373);
				additive(0);
				setState(374);
				match(IN);
				setState(375);
				match(LPAREN);
				setState(376);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new InComparisonContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(378);
				additive(0);
				setState(379);
				match(IN);
				setState(380);
				match(LPAREN);
				setState(381);
				expr();
				setState(382);
				match(RPAREN);
				}
				break;
			case 5:
				_localctx = new PassThroughContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(384);
				additive(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AdditiveContext extends ParserRuleContext {
		public AdditiveContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_additive; }
	 
		public AdditiveContext() { }
		public void copyFrom(AdditiveContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ToMultiplicativeContext extends AdditiveContext {
		public MultiplicativeContext multiplicative() {
			return getRuleContext(MultiplicativeContext.class,0);
		}
		public ToMultiplicativeContext(AdditiveContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitToMultiplicative(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AddSubContext extends AdditiveContext {
		public Token op;
		public AdditiveContext additive() {
			return getRuleContext(AdditiveContext.class,0);
		}
		public MultiplicativeContext multiplicative() {
			return getRuleContext(MultiplicativeContext.class,0);
		}
		public TerminalNode ADD() { return getToken(ODataFilterParser.ADD, 0); }
		public TerminalNode SUB() { return getToken(ODataFilterParser.SUB, 0); }
		public AddSubContext(AdditiveContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAddSub(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AdditiveContext additive() throws RecognitionException {
		return additive(0);
	}

	private AdditiveContext additive(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		AdditiveContext _localctx = new AdditiveContext(_ctx, _parentState);
		AdditiveContext _prevctx = _localctx;
		int _startState = 46;
		enterRecursionRule(_localctx, 46, RULE_additive, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new ToMultiplicativeContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(388);
			multiplicative(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(395);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new AddSubContext(new AdditiveContext(_parentctx, _parentState));
					pushNewRecursionContext(_localctx, _startState, RULE_additive);
					setState(390);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(391);
					((AddSubContext)_localctx).op = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==ADD || _la==SUB) ) {
						((AddSubContext)_localctx).op = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(392);
					multiplicative(0);
					}
					} 
				}
				setState(397);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicativeContext extends ParserRuleContext {
		public MultiplicativeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multiplicative; }
	 
		public MultiplicativeContext() { }
		public void copyFrom(MultiplicativeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MulDivModContext extends MultiplicativeContext {
		public Token op;
		public MultiplicativeContext multiplicative() {
			return getRuleContext(MultiplicativeContext.class,0);
		}
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public TerminalNode MUL() { return getToken(ODataFilterParser.MUL, 0); }
		public TerminalNode DIV() { return getToken(ODataFilterParser.DIV, 0); }
		public TerminalNode DIVBY() { return getToken(ODataFilterParser.DIVBY, 0); }
		public TerminalNode MOD() { return getToken(ODataFilterParser.MOD, 0); }
		public MulDivModContext(MultiplicativeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitMulDivMod(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ToPrimaryContext extends MultiplicativeContext {
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public ToPrimaryContext(MultiplicativeContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitToPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultiplicativeContext multiplicative() throws RecognitionException {
		return multiplicative(0);
	}

	private MultiplicativeContext multiplicative(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		MultiplicativeContext _localctx = new MultiplicativeContext(_ctx, _parentState);
		MultiplicativeContext _prevctx = _localctx;
		int _startState = 48;
		enterRecursionRule(_localctx, 48, RULE_multiplicative, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new ToPrimaryContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(399);
			primary();
			}
			_ctx.stop = _input.LT(-1);
			setState(406);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new MulDivModContext(new MultiplicativeContext(_parentctx, _parentState));
					pushNewRecursionContext(_localctx, _startState, RULE_multiplicative);
					setState(401);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(402);
					((MulDivModContext)_localctx).op = _input.LT(1);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 245760L) != 0)) ) {
						((MulDivModContext)_localctx).op = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(403);
					primary();
					}
					} 
				}
				setState(408);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,34,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
	 
		public PrimaryContext() { }
		public void copyFrom(PrimaryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MemberPrimaryContext extends PrimaryContext {
		public MemberPathContext memberPath() {
			return getRuleContext(MemberPathContext.class,0);
		}
		public MemberPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitMemberPrimary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AliasPrimaryContext extends PrimaryContext {
		public TerminalNode ALIAS() { return getToken(ODataFilterParser.ALIAS, 0); }
		public AliasPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAliasPrimary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TypeFuncPrimaryContext extends PrimaryContext {
		public TypeFuncContext typeFunc() {
			return getRuleContext(TypeFuncContext.class,0);
		}
		public TypeFuncPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitTypeFuncPrimary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FunctionPrimaryContext extends PrimaryContext {
		public FunctionCallContext functionCall() {
			return getRuleContext(FunctionCallContext.class,0);
		}
		public FunctionPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitFunctionPrimary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LiteralPrimaryContext extends PrimaryContext {
		public LiteralContext literal() {
			return getRuleContext(LiteralContext.class,0);
		}
		public LiteralPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitLiteralPrimary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ParenPrimaryContext extends PrimaryContext {
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public ParenPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitParenPrimary(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_primary);
		try {
			setState(418);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				_localctx = new LiteralPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(409);
				literal();
				}
				break;
			case 2:
				_localctx = new TypeFuncPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(410);
				typeFunc();
				}
				break;
			case 3:
				_localctx = new FunctionPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(411);
				functionCall();
				}
				break;
			case 4:
				_localctx = new MemberPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(412);
				memberPath();
				}
				break;
			case 5:
				_localctx = new AliasPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(413);
				match(ALIAS);
				}
				break;
			case 6:
				_localctx = new ParenPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(414);
				match(LPAREN);
				setState(415);
				expr();
				setState(416);
				match(RPAREN);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeFuncContext extends ParserRuleContext {
		public Token op;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public QualifiedTypeNameContext qualifiedTypeName() {
			return getRuleContext(QualifiedTypeNameContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode CAST() { return getToken(ODataFilterParser.CAST, 0); }
		public TerminalNode ISOF() { return getToken(ODataFilterParser.ISOF, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(ODataFilterParser.COMMA, 0); }
		public TypeFuncContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeFunc; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitTypeFunc(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeFuncContext typeFunc() throws RecognitionException {
		TypeFuncContext _localctx = new TypeFuncContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_typeFunc);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(420);
			((TypeFuncContext)_localctx).op = _input.LT(1);
			_la = _input.LA(1);
			if ( !(_la==CAST || _la==ISOF) ) {
				((TypeFuncContext)_localctx).op = (Token)_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(421);
			match(LPAREN);
			setState(425);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(422);
				expr();
				setState(423);
				match(COMMA);
				}
				break;
			}
			setState(427);
			qualifiedTypeName();
			setState(428);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedTypeNameContext extends ParserRuleContext {
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public List<TerminalNode> DOT() { return getTokens(ODataFilterParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ODataFilterParser.DOT, i);
		}
		public QualifiedTypeNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedTypeName; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitQualifiedTypeName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QualifiedTypeNameContext qualifiedTypeName() throws RecognitionException {
		QualifiedTypeNameContext _localctx = new QualifiedTypeNameContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_qualifiedTypeName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(430);
			match(IDENT);
			setState(435);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(431);
				match(DOT);
				setState(432);
				match(IDENT);
				}
				}
				setState(437);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionCallContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public FunctionCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionCall; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitFunctionCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionCallContext functionCall() throws RecognitionException {
		FunctionCallContext _localctx = new FunctionCallContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(438);
			match(IDENT);
			setState(439);
			match(LPAREN);
			setState(448);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2250708999995400L) != 0)) {
				{
				setState(440);
				expr();
				setState(445);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(441);
					match(COMMA);
					setState(442);
					expr();
					}
					}
					setState(447);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(450);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MemberPathContext extends ParserRuleContext {
		public List<PathSegmentContext> pathSegment() {
			return getRuleContexts(PathSegmentContext.class);
		}
		public PathSegmentContext pathSegment(int i) {
			return getRuleContext(PathSegmentContext.class,i);
		}
		public List<TerminalNode> SLASH() { return getTokens(ODataFilterParser.SLASH); }
		public TerminalNode SLASH(int i) {
			return getToken(ODataFilterParser.SLASH, i);
		}
		public LambdaCallContext lambdaCall() {
			return getRuleContext(LambdaCallContext.class,0);
		}
		public TerminalNode COUNT() { return getToken(ODataFilterParser.COUNT, 0); }
		public MemberPathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberPath; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitMemberPath(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MemberPathContext memberPath() throws RecognitionException {
		MemberPathContext _localctx = new MemberPathContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_memberPath);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(452);
			pathSegment();
			setState(457);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(453);
					match(SLASH);
					setState(454);
					pathSegment();
					}
					} 
				}
				setState(459);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
			}
			setState(464);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				{
				setState(460);
				match(SLASH);
				setState(461);
				lambdaCall();
				}
				break;
			case 2:
				{
				setState(462);
				match(SLASH);
				setState(463);
				match(COUNT);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PathSegmentContext extends ParserRuleContext {
		public PathSegmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathSegment; }
	 
		public PathSegmentContext() { }
		public void copyFrom(PathSegmentContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BoundCallSegmentContext extends PathSegmentContext {
		public BoundCallContext boundCall() {
			return getRuleContext(BoundCallContext.class,0);
		}
		public BoundCallSegmentContext(PathSegmentContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitBoundCallSegment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PropertyPathSegmentContext extends PathSegmentContext {
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public PropertyPathSegmentContext(PathSegmentContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitPropertyPathSegment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathSegmentContext pathSegment() throws RecognitionException {
		PathSegmentContext _localctx = new PathSegmentContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_pathSegment);
		try {
			setState(468);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				_localctx = new PropertyPathSegmentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(466);
				match(IDENT);
				}
				break;
			case 2:
				_localctx = new BoundCallSegmentContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(467);
				boundCall();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BoundCallContext extends ParserRuleContext {
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public List<TerminalNode> DOT() { return getTokens(ODataFilterParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(ODataFilterParser.DOT, i);
		}
		public BoundCallArgsContext boundCallArgs() {
			return getRuleContext(BoundCallArgsContext.class,0);
		}
		public BoundCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boundCall; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitBoundCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BoundCallContext boundCall() throws RecognitionException {
		BoundCallContext _localctx = new BoundCallContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_boundCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(470);
			match(IDENT);
			setState(475);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(471);
				match(DOT);
				setState(472);
				match(IDENT);
				}
				}
				setState(477);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(478);
			match(LPAREN);
			setState(480);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 2250708999995400L) != 0)) {
				{
				setState(479);
				boundCallArgs();
				}
			}

			setState(482);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BoundCallArgsContext extends ParserRuleContext {
		public List<NamedArgContext> namedArg() {
			return getRuleContexts(NamedArgContext.class);
		}
		public NamedArgContext namedArg(int i) {
			return getRuleContext(NamedArgContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public BoundCallArgsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boundCallArgs; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitBoundCallArgs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BoundCallArgsContext boundCallArgs() throws RecognitionException {
		BoundCallArgsContext _localctx = new BoundCallArgsContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_boundCallArgs);
		int _la;
		try {
			setState(500);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(484);
				namedArg();
				setState(489);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(485);
					match(COMMA);
					setState(486);
					namedArg();
					}
					}
					setState(491);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(492);
				expr();
				setState(497);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(493);
					match(COMMA);
					setState(494);
					expr();
					}
					}
					setState(499);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NamedArgContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public TerminalNode EQUALS() { return getToken(ODataFilterParser.EQUALS, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public NamedArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_namedArg; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitNamedArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NamedArgContext namedArg() throws RecognitionException {
		NamedArgContext _localctx = new NamedArgContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_namedArg);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(502);
			match(IDENT);
			setState(503);
			match(EQUALS);
			setState(504);
			expr();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaCallContext extends ParserRuleContext {
		public Token op;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode ANY() { return getToken(ODataFilterParser.ANY, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public TerminalNode COLON() { return getToken(ODataFilterParser.COLON, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode ALL() { return getToken(ODataFilterParser.ALL, 0); }
		public LambdaCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaCall; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitLambdaCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaCallContext lambdaCall() throws RecognitionException {
		LambdaCallContext _localctx = new LambdaCallContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_lambdaCall);
		int _la;
		try {
			setState(521);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANY:
				enterOuterAlt(_localctx, 1);
				{
				setState(506);
				((LambdaCallContext)_localctx).op = match(ANY);
				setState(507);
				match(LPAREN);
				setState(511);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IDENT) {
					{
					setState(508);
					match(IDENT);
					setState(509);
					match(COLON);
					setState(510);
					expr();
					}
				}

				setState(513);
				match(RPAREN);
				}
				break;
			case ALL:
				enterOuterAlt(_localctx, 2);
				{
				setState(514);
				((LambdaCallContext)_localctx).op = match(ALL);
				setState(515);
				match(LPAREN);
				setState(516);
				match(IDENT);
				setState(517);
				match(COLON);
				setState(518);
				expr();
				setState(519);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LiteralContext extends ParserRuleContext {
		public LiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_literal; }
	 
		public LiteralContext() { }
		public void copyFrom(LiteralContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TimeOfDayLiteralContext extends LiteralContext {
		public TerminalNode TIMEOFDAY() { return getToken(ODataFilterParser.TIMEOFDAY, 0); }
		public TimeOfDayLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitTimeOfDayLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringLiteralContext extends LiteralContext {
		public TerminalNode STRING() { return getToken(ODataFilterParser.STRING, 0); }
		public StringLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitStringLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateLiteralContext extends LiteralContext {
		public TerminalNode DATE() { return getToken(ODataFilterParser.DATE, 0); }
		public DateLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitDateLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BooleanLiteralContext extends LiteralContext {
		public TerminalNode TRUE() { return getToken(ODataFilterParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(ODataFilterParser.FALSE, 0); }
		public BooleanLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitBooleanLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DecimalLiteralContext extends LiteralContext {
		public TerminalNode DECIMAL() { return getToken(ODataFilterParser.DECIMAL, 0); }
		public DecimalLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitDecimalLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GuidLiteralContext extends LiteralContext {
		public TerminalNode GUID() { return getToken(ODataFilterParser.GUID, 0); }
		public GuidLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitGuidLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DateTimeOffsetLiteralContext extends LiteralContext {
		public TerminalNode DATETIMEOFFSET() { return getToken(ODataFilterParser.DATETIMEOFFSET, 0); }
		public DateTimeOffsetLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitDateTimeOffsetLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IntLiteralContext extends LiteralContext {
		public TerminalNode INT() { return getToken(ODataFilterParser.INT, 0); }
		public IntLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitIntLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EnumLiteralContext extends LiteralContext {
		public TerminalNode ENUM() { return getToken(ODataFilterParser.ENUM, 0); }
		public EnumLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitEnumLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NullLiteralContext extends LiteralContext {
		public TerminalNode NULL() { return getToken(ODataFilterParser.NULL, 0); }
		public NullLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitNullLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DurationLiteralContext extends LiteralContext {
		public TerminalNode DURATION() { return getToken(ODataFilterParser.DURATION, 0); }
		public DurationLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitDurationLiteral(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_literal);
		int _la;
		try {
			setState(534);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(523);
				match(STRING);
				}
				break;
			case DECIMAL:
				_localctx = new DecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(524);
				match(DECIMAL);
				}
				break;
			case INT:
				_localctx = new IntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(525);
				match(INT);
				}
				break;
			case TRUE:
			case FALSE:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(526);
				_la = _input.LA(1);
				if ( !(_la==TRUE || _la==FALSE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case NULL:
				_localctx = new NullLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(527);
				match(NULL);
				}
				break;
			case GUID:
				_localctx = new GuidLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(528);
				match(GUID);
				}
				break;
			case DATETIMEOFFSET:
				_localctx = new DateTimeOffsetLiteralContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(529);
				match(DATETIMEOFFSET);
				}
				break;
			case DATE:
				_localctx = new DateLiteralContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(530);
				match(DATE);
				}
				break;
			case TIMEOFDAY:
				_localctx = new TimeOfDayLiteralContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(531);
				match(TIMEOFDAY);
				}
				break;
			case DURATION:
				_localctx = new DurationLiteralContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(532);
				match(DURATION);
				}
				break;
			case ENUM:
				_localctx = new EnumLiteralContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(533);
				match(ENUM);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 11:
			return applyTrafo_sempred((ApplyTrafoContext)_localctx, predIndex);
		case 12:
			return groupbyElement_sempred((GroupbyElementContext)_localctx, predIndex);
		case 23:
			return additive_sempred((AdditiveContext)_localctx, predIndex);
		case 24:
			return multiplicative_sempred((MultiplicativeContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean applyTrafo_sempred(ApplyTrafoContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return trafo("groupby");
		case 1:
			return trafo("aggregate");
		case 2:
			return trafo("compute");
		case 3:
			return trafo("concat");
		case 4:
			return trafo("filter");
		case 5:
			return bottomTopTrafo();
		case 6:
			return trafo("orderby");
		case 7:
			return trafo("top") || trafo("skip");
		case 8:
			return trafo("identity");
		}
		return true;
	}
	private boolean groupbyElement_sempred(GroupbyElementContext _localctx, int predIndex) {
		switch (predIndex) {
		case 9:
			return trafo("rollup");
		}
		return true;
	}
	private boolean additive_sempred(AdditiveContext _localctx, int predIndex) {
		switch (predIndex) {
		case 10:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean multiplicative_sempred(MultiplicativeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 11:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u00013\u0219\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0005\u0001O\b\u0001\n\u0001\f\u0001R\t\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0001\u0002\u0003\u0002X\b\u0002\u0001\u0003\u0001"+
		"\u0003\u0003\u0003\\\b\u0003\u0001\u0003\u0001\u0003\u0005\u0003`\b\u0003"+
		"\n\u0003\f\u0003c\t\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004l\b\u0004\n\u0004\f\u0004"+
		"o\t\u0004\u0003\u0004q\b\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0003\u0007}\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0081"+
		"\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0087"+
		"\b\u0007\u0001\b\u0001\b\u0001\b\u0004\b\u008c\b\b\u000b\b\f\b\u008d\u0001"+
		"\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0005\n\u0096\b\n\n\n\f\n\u0099"+
		"\t\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0005\u000b\u00a2\b\u000b\n\u000b\f\u000b\u00a5\t\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u00aa\b\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0005\u000b\u00b4\b\u000b\n\u000b\f\u000b\u00b7\t\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0005\u000b\u00c1\b\u000b\n\u000b\f\u000b\u00c4\t\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0004\u000b\u00ce\b\u000b\u000b\u000b\f\u000b"+
		"\u00cf\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u00e8"+
		"\b\u000b\n\u000b\f\u000b\u00eb\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0003\u000b\u00f6\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0005\f\u00fe\b\f\n\f\f\f\u0101\t\f\u0001\f\u0001\f\u0001\f\u0003\f"+
		"\u0106\b\f\u0001\r\u0001\r\u0001\r\u0001\r\u0005\r\u010c\b\r\n\r\f\r\u010f"+
		"\t\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0005\r\u0116\b\r\n\r\f\r"+
		"\u0119\t\r\u0001\r\u0001\r\u0001\r\u0001\r\u0005\r\u011f\b\r\n\r\f\r\u0122"+
		"\t\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u0128\b\r\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0005\u000e\u012d\b\u000e\n\u000e\f\u000e\u0130\t\u000e"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u0136\b\u000f"+
		"\n\u000f\f\u000f\u0139\t\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0005\u0010\u0142\b\u0010\n"+
		"\u0010\f\u0010\u0145\t\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u0149"+
		"\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001"+
		"\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u0154\b\u0013\n"+
		"\u0013\f\u0013\u0157\t\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0005"+
		"\u0014\u015c\b\u0014\n\u0014\f\u0014\u015f\t\u0014\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0003\u0015\u0164\b\u0015\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0004\u0016\u0170\b\u0016\u000b\u0016\f\u0016\u0171\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0001\u0016\u0003\u0016\u0182\b\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017\u018a\b\u0017\n"+
		"\u0017\f\u0017\u018d\t\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u0195\b\u0018\n\u0018\f\u0018"+
		"\u0198\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u01a3\b\u0019"+
		"\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a"+
		"\u01aa\b\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0005\u001b\u01b2\b\u001b\n\u001b\f\u001b\u01b5\t\u001b\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001c\u01bc"+
		"\b\u001c\n\u001c\f\u001c\u01bf\t\u001c\u0003\u001c\u01c1\b\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u01c8"+
		"\b\u001d\n\u001d\f\u001d\u01cb\t\u001d\u0001\u001d\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0003\u001d\u01d1\b\u001d\u0001\u001e\u0001\u001e\u0003\u001e"+
		"\u01d5\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0005\u001f\u01da\b"+
		"\u001f\n\u001f\f\u001f\u01dd\t\u001f\u0001\u001f\u0001\u001f\u0003\u001f"+
		"\u01e1\b\u001f\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0005 \u01e8"+
		"\b \n \f \u01eb\t \u0001 \u0001 \u0001 \u0005 \u01f0\b \n \f \u01f3\t"+
		" \u0003 \u01f5\b \u0001!\u0001!\u0001!\u0001!\u0001\"\u0001\"\u0001\""+
		"\u0001\"\u0001\"\u0003\"\u0200\b\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0001\"\u0001\"\u0001\"\u0003\"\u020a\b\"\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0003#\u0217\b#\u0001"+
		"#\u0000\u0002.0$\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014"+
		"\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDF\u0000\u0007\u0001"+
		"\u0000\u0012\u0013\u0001\u0000*0\u0001\u0000\u0004\n\u0001\u0000\f\r\u0001"+
		"\u0000\u000e\u0011\u0001\u0000\u0019\u001a\u0001\u0000\u0014\u0015\u0245"+
		"\u0000H\u0001\u0000\u0000\u0000\u0002K\u0001\u0000\u0000\u0000\u0004U"+
		"\u0001\u0000\u0000\u0000\u0006Y\u0001\u0000\u0000\u0000\bf\u0001\u0000"+
		"\u0000\u0000\nt\u0001\u0000\u0000\u0000\fx\u0001\u0000\u0000\u0000\u000e"+
		"\u0086\u0001\u0000\u0000\u0000\u0010\u0088\u0001\u0000\u0000\u0000\u0012"+
		"\u008f\u0001\u0000\u0000\u0000\u0014\u0092\u0001\u0000\u0000\u0000\u0016"+
		"\u00f5\u0001\u0000\u0000\u0000\u0018\u0105\u0001\u0000\u0000\u0000\u001a"+
		"\u0127\u0001\u0000\u0000\u0000\u001c\u0129\u0001\u0000\u0000\u0000\u001e"+
		"\u0131\u0001\u0000\u0000\u0000 \u013d\u0001\u0000\u0000\u0000\"\u014a"+
		"\u0001\u0000\u0000\u0000$\u014e\u0001\u0000\u0000\u0000&\u0150\u0001\u0000"+
		"\u0000\u0000(\u0158\u0001\u0000\u0000\u0000*\u0163\u0001\u0000\u0000\u0000"+
		",\u0181\u0001\u0000\u0000\u0000.\u0183\u0001\u0000\u0000\u00000\u018e"+
		"\u0001\u0000\u0000\u00002\u01a2\u0001\u0000\u0000\u00004\u01a4\u0001\u0000"+
		"\u0000\u00006\u01ae\u0001\u0000\u0000\u00008\u01b6\u0001\u0000\u0000\u0000"+
		":\u01c4\u0001\u0000\u0000\u0000<\u01d4\u0001\u0000\u0000\u0000>\u01d6"+
		"\u0001\u0000\u0000\u0000@\u01f4\u0001\u0000\u0000\u0000B\u01f6\u0001\u0000"+
		"\u0000\u0000D\u0209\u0001\u0000\u0000\u0000F\u0216\u0001\u0000\u0000\u0000"+
		"HI\u0003$\u0012\u0000IJ\u0005\u0000\u0000\u0001J\u0001\u0001\u0000\u0000"+
		"\u0000KP\u0003\u0004\u0002\u0000LM\u0005#\u0000\u0000MO\u0003\u0004\u0002"+
		"\u0000NL\u0001\u0000\u0000\u0000OR\u0001\u0000\u0000\u0000PN\u0001\u0000"+
		"\u0000\u0000PQ\u0001\u0000\u0000\u0000QS\u0001\u0000\u0000\u0000RP\u0001"+
		"\u0000\u0000\u0000ST\u0005\u0000\u0000\u0001T\u0003\u0001\u0000\u0000"+
		"\u0000UW\u0003$\u0012\u0000VX\u0007\u0000\u0000\u0000WV\u0001\u0000\u0000"+
		"\u0000WX\u0001\u0000\u0000\u0000X\u0005\u0001\u0000\u0000\u0000Y[\u0005"+
		"1\u0000\u0000Z\\\u0003\b\u0004\u0000[Z\u0001\u0000\u0000\u0000[\\\u0001"+
		"\u0000\u0000\u0000\\a\u0001\u0000\u0000\u0000]^\u0005%\u0000\u0000^`\u0003"+
		"\u000e\u0007\u0000_]\u0001\u0000\u0000\u0000`c\u0001\u0000\u0000\u0000"+
		"a_\u0001\u0000\u0000\u0000ab\u0001\u0000\u0000\u0000bd\u0001\u0000\u0000"+
		"\u0000ca\u0001\u0000\u0000\u0000de\u0005\u0000\u0000\u0001e\u0007\u0001"+
		"\u0000\u0000\u0000fp\u0005!\u0000\u0000gq\u0003\f\u0006\u0000hm\u0003"+
		"\n\u0005\u0000ij\u0005#\u0000\u0000jl\u0003\n\u0005\u0000ki\u0001\u0000"+
		"\u0000\u0000lo\u0001\u0000\u0000\u0000mk\u0001\u0000\u0000\u0000mn\u0001"+
		"\u0000\u0000\u0000nq\u0001\u0000\u0000\u0000om\u0001\u0000\u0000\u0000"+
		"pg\u0001\u0000\u0000\u0000ph\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000"+
		"\u0000rs\u0005\"\u0000\u0000s\t\u0001\u0000\u0000\u0000tu\u00051\u0000"+
		"\u0000uv\u0005$\u0000\u0000vw\u0003\f\u0006\u0000w\u000b\u0001\u0000\u0000"+
		"\u0000xy\u0007\u0001\u0000\u0000y\r\u0001\u0000\u0000\u0000z|\u0003\u0010"+
		"\b\u0000{}\u0003\b\u0004\u0000|{\u0001\u0000\u0000\u0000|}\u0001\u0000"+
		"\u0000\u0000}\u0087\u0001\u0000\u0000\u0000~\u0080\u00051\u0000\u0000"+
		"\u007f\u0081\u0003\b\u0004\u0000\u0080\u007f\u0001\u0000\u0000\u0000\u0080"+
		"\u0081\u0001\u0000\u0000\u0000\u0081\u0087\u0001\u0000\u0000\u0000\u0082"+
		"\u0087\u0005\u001b\u0000\u0000\u0083\u0087\u0005\u001c\u0000\u0000\u0084"+
		"\u0087\u0005\u001d\u0000\u0000\u0085\u0087\u0003\f\u0006\u0000\u0086z"+
		"\u0001\u0000\u0000\u0000\u0086~\u0001\u0000\u0000\u0000\u0086\u0082\u0001"+
		"\u0000\u0000\u0000\u0086\u0083\u0001\u0000\u0000\u0000\u0086\u0084\u0001"+
		"\u0000\u0000\u0000\u0086\u0085\u0001\u0000\u0000\u0000\u0087\u000f\u0001"+
		"\u0000\u0000\u0000\u0088\u008b\u00051\u0000\u0000\u0089\u008a\u0005\'"+
		"\u0000\u0000\u008a\u008c\u00051\u0000\u0000\u008b\u0089\u0001\u0000\u0000"+
		"\u0000\u008c\u008d\u0001\u0000\u0000\u0000\u008d\u008b\u0001\u0000\u0000"+
		"\u0000\u008d\u008e\u0001\u0000\u0000\u0000\u008e\u0011\u0001\u0000\u0000"+
		"\u0000\u008f\u0090\u0003\u0014\n\u0000\u0090\u0091\u0005\u0000\u0000\u0001"+
		"\u0091\u0013\u0001\u0000\u0000\u0000\u0092\u0097\u0003\u0016\u000b\u0000"+
		"\u0093\u0094\u0005%\u0000\u0000\u0094\u0096\u0003\u0016\u000b\u0000\u0095"+
		"\u0093\u0001\u0000\u0000\u0000\u0096\u0099\u0001\u0000\u0000\u0000\u0097"+
		"\u0095\u0001\u0000\u0000\u0000\u0097\u0098\u0001\u0000\u0000\u0000\u0098"+
		"\u0015\u0001\u0000\u0000\u0000\u0099\u0097\u0001\u0000\u0000\u0000\u009a"+
		"\u009b\u0004\u000b\u0000\u0000\u009b\u009c\u00051\u0000\u0000\u009c\u009d"+
		"\u0005!\u0000\u0000\u009d\u009e\u0005!\u0000\u0000\u009e\u00a3\u0003\u0018"+
		"\f\u0000\u009f\u00a0\u0005#\u0000\u0000\u00a0\u00a2\u0003\u0018\f\u0000"+
		"\u00a1\u009f\u0001\u0000\u0000\u0000\u00a2\u00a5\u0001\u0000\u0000\u0000"+
		"\u00a3\u00a1\u0001\u0000\u0000\u0000\u00a3\u00a4\u0001\u0000\u0000\u0000"+
		"\u00a4\u00a6\u0001\u0000\u0000\u0000\u00a5\u00a3\u0001\u0000\u0000\u0000"+
		"\u00a6\u00a9\u0005\"\u0000\u0000\u00a7\u00a8\u0005#\u0000\u0000\u00a8"+
		"\u00aa\u0003\u0014\n\u0000\u00a9\u00a7\u0001\u0000\u0000\u0000\u00a9\u00aa"+
		"\u0001\u0000\u0000\u0000\u00aa\u00ab\u0001\u0000\u0000\u0000\u00ab\u00ac"+
		"\u0005\"\u0000\u0000\u00ac\u00f6\u0001\u0000\u0000\u0000\u00ad\u00ae\u0004"+
		"\u000b\u0001\u0000\u00ae\u00af\u00051\u0000\u0000\u00af\u00b0\u0005!\u0000"+
		"\u0000\u00b0\u00b5\u0003\u001a\r\u0000\u00b1\u00b2\u0005#\u0000\u0000"+
		"\u00b2\u00b4\u0003\u001a\r\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000\u00b4"+
		"\u00b7\u0001\u0000\u0000\u0000\u00b5\u00b3\u0001\u0000\u0000\u0000\u00b5"+
		"\u00b6\u0001\u0000\u0000\u0000\u00b6\u00b8\u0001\u0000\u0000\u0000\u00b7"+
		"\u00b5\u0001\u0000\u0000\u0000\u00b8\u00b9\u0005\"\u0000\u0000\u00b9\u00f6"+
		"\u0001\u0000\u0000\u0000\u00ba\u00bb\u0004\u000b\u0002\u0000\u00bb\u00bc"+
		"\u00051\u0000\u0000\u00bc\u00bd\u0005!\u0000\u0000\u00bd\u00c2\u0003\""+
		"\u0011\u0000\u00be\u00bf\u0005#\u0000\u0000\u00bf\u00c1\u0003\"\u0011"+
		"\u0000\u00c0\u00be\u0001\u0000\u0000\u0000\u00c1\u00c4\u0001\u0000\u0000"+
		"\u0000\u00c2\u00c0\u0001\u0000\u0000\u0000\u00c2\u00c3\u0001\u0000\u0000"+
		"\u0000\u00c3\u00c5\u0001\u0000\u0000\u0000\u00c4\u00c2\u0001\u0000\u0000"+
		"\u0000\u00c5\u00c6\u0005\"\u0000\u0000\u00c6\u00f6\u0001\u0000\u0000\u0000"+
		"\u00c7\u00c8\u0004\u000b\u0003\u0000\u00c8\u00c9\u00051\u0000\u0000\u00c9"+
		"\u00ca\u0005!\u0000\u0000\u00ca\u00cd\u0003\u0014\n\u0000\u00cb\u00cc"+
		"\u0005#\u0000\u0000\u00cc\u00ce\u0003\u0014\n\u0000\u00cd\u00cb\u0001"+
		"\u0000\u0000\u0000\u00ce\u00cf\u0001\u0000\u0000\u0000\u00cf\u00cd\u0001"+
		"\u0000\u0000\u0000\u00cf\u00d0\u0001\u0000\u0000\u0000\u00d0\u00d1\u0001"+
		"\u0000\u0000\u0000\u00d1\u00d2\u0005\"\u0000\u0000\u00d2\u00f6\u0001\u0000"+
		"\u0000\u0000\u00d3\u00d4\u0004\u000b\u0004\u0000\u00d4\u00d5\u00051\u0000"+
		"\u0000\u00d5\u00d6\u0005!\u0000\u0000\u00d6\u00d7\u0003$\u0012\u0000\u00d7"+
		"\u00d8\u0005\"\u0000\u0000\u00d8\u00f6\u0001\u0000\u0000\u0000\u00d9\u00da"+
		"\u0004\u000b\u0005\u0000\u00da\u00db\u00051\u0000\u0000\u00db\u00dc\u0005"+
		"!\u0000\u0000\u00dc\u00dd\u0003$\u0012\u0000\u00dd\u00de\u0005#\u0000"+
		"\u0000\u00de\u00df\u0003$\u0012\u0000\u00df\u00e0\u0005\"\u0000\u0000"+
		"\u00e0\u00f6\u0001\u0000\u0000\u0000\u00e1\u00e2\u0004\u000b\u0006\u0000"+
		"\u00e2\u00e3\u00051\u0000\u0000\u00e3\u00e4\u0005!\u0000\u0000\u00e4\u00e9"+
		"\u0003\u0004\u0002\u0000\u00e5\u00e6\u0005#\u0000\u0000\u00e6\u00e8\u0003"+
		"\u0004\u0002\u0000\u00e7\u00e5\u0001\u0000\u0000\u0000\u00e8\u00eb\u0001"+
		"\u0000\u0000\u0000\u00e9\u00e7\u0001\u0000\u0000\u0000\u00e9\u00ea\u0001"+
		"\u0000\u0000\u0000\u00ea\u00ec\u0001\u0000\u0000\u0000\u00eb\u00e9\u0001"+
		"\u0000\u0000\u0000\u00ec\u00ed\u0005\"\u0000\u0000\u00ed\u00f6\u0001\u0000"+
		"\u0000\u0000\u00ee\u00ef\u0004\u000b\u0007\u0000\u00ef\u00f0\u00051\u0000"+
		"\u0000\u00f0\u00f1\u0005!\u0000\u0000\u00f1\u00f2\u00050\u0000\u0000\u00f2"+
		"\u00f6\u0005\"\u0000\u0000\u00f3\u00f4\u0004\u000b\b\u0000\u00f4\u00f6"+
		"\u00051\u0000\u0000\u00f5\u009a\u0001\u0000\u0000\u0000\u00f5\u00ad\u0001"+
		"\u0000\u0000\u0000\u00f5\u00ba\u0001\u0000\u0000\u0000\u00f5\u00c7\u0001"+
		"\u0000\u0000\u0000\u00f5\u00d3\u0001\u0000\u0000\u0000\u00f5\u00d9\u0001"+
		"\u0000\u0000\u0000\u00f5\u00e1\u0001\u0000\u0000\u0000\u00f5\u00ee\u0001"+
		"\u0000\u0000\u0000\u00f5\u00f3\u0001\u0000\u0000\u0000\u00f6\u0017\u0001"+
		"\u0000\u0000\u0000\u00f7\u00f8\u0004\f\t\u0000\u00f8\u00f9\u00051\u0000"+
		"\u0000\u00f9\u00fa\u0005!\u0000\u0000\u00fa\u00ff\u0003:\u001d\u0000\u00fb"+
		"\u00fc\u0005#\u0000\u0000\u00fc\u00fe\u0003:\u001d\u0000\u00fd\u00fb\u0001"+
		"\u0000\u0000\u0000\u00fe\u0101\u0001\u0000\u0000\u0000\u00ff\u00fd\u0001"+
		"\u0000\u0000\u0000\u00ff\u0100\u0001\u0000\u0000\u0000\u0100\u0102\u0001"+
		"\u0000\u0000\u0000\u0101\u00ff\u0001\u0000\u0000\u0000\u0102\u0103\u0005"+
		"\"\u0000\u0000\u0103\u0106\u0001\u0000\u0000\u0000\u0104\u0106\u0003:"+
		"\u001d\u0000\u0105\u00f7\u0001\u0000\u0000\u0000\u0105\u0104\u0001\u0000"+
		"\u0000\u0000\u0106\u0019\u0001\u0000\u0000\u0000\u0107\u0108\u0003$\u0012"+
		"\u0000\u0108\u0109\u0005\u001e\u0000\u0000\u0109\u010d\u0003\u001c\u000e"+
		"\u0000\u010a\u010c\u0003\u001e\u000f\u0000\u010b\u010a\u0001\u0000\u0000"+
		"\u0000\u010c\u010f\u0001\u0000\u0000\u0000\u010d\u010b\u0001\u0000\u0000"+
		"\u0000\u010d\u010e\u0001\u0000\u0000\u0000\u010e\u0110\u0001\u0000\u0000"+
		"\u0000\u010f\u010d\u0001\u0000\u0000\u0000\u0110\u0111\u0005\u001f\u0000"+
		"\u0000\u0111\u0112\u00051\u0000\u0000\u0112\u0128\u0001\u0000\u0000\u0000"+
		"\u0113\u0117\u0005\u001b\u0000\u0000\u0114\u0116\u0003\u001e\u000f\u0000"+
		"\u0115\u0114\u0001\u0000\u0000\u0000\u0116\u0119\u0001\u0000\u0000\u0000"+
		"\u0117\u0115\u0001\u0000\u0000\u0000\u0117\u0118\u0001\u0000\u0000\u0000"+
		"\u0118\u011a\u0001\u0000\u0000\u0000\u0119\u0117\u0001\u0000\u0000\u0000"+
		"\u011a\u011b\u0005\u001f\u0000\u0000\u011b\u0128\u00051\u0000\u0000\u011c"+
		"\u0120\u0003$\u0012\u0000\u011d\u011f\u0003 \u0010\u0000\u011e\u011d\u0001"+
		"\u0000\u0000\u0000\u011f\u0122\u0001\u0000\u0000\u0000\u0120\u011e\u0001"+
		"\u0000\u0000\u0000\u0120\u0121\u0001\u0000\u0000\u0000\u0121\u0123\u0001"+
		"\u0000\u0000\u0000\u0122\u0120\u0001\u0000\u0000\u0000\u0123\u0124\u0005"+
		"\u001f\u0000\u0000\u0124\u0125\u00051\u0000\u0000\u0125\u0128\u0001\u0000"+
		"\u0000\u0000\u0126\u0128\u0003$\u0012\u0000\u0127\u0107\u0001\u0000\u0000"+
		"\u0000\u0127\u0113\u0001\u0000\u0000\u0000\u0127\u011c\u0001\u0000\u0000"+
		"\u0000\u0127\u0126\u0001\u0000\u0000\u0000\u0128\u001b\u0001\u0000\u0000"+
		"\u0000\u0129\u012e\u00051\u0000\u0000\u012a\u012b\u0005\'\u0000\u0000"+
		"\u012b\u012d\u00051\u0000\u0000\u012c\u012a\u0001\u0000\u0000\u0000\u012d"+
		"\u0130\u0001\u0000\u0000\u0000\u012e\u012c\u0001\u0000\u0000\u0000\u012e"+
		"\u012f\u0001\u0000\u0000\u0000\u012f\u001d\u0001\u0000\u0000\u0000\u0130"+
		"\u012e\u0001\u0000\u0000\u0000\u0131\u0132\u0005 \u0000\u0000\u0132\u0137"+
		"\u0003:\u001d\u0000\u0133\u0134\u0005#\u0000\u0000\u0134\u0136\u0003:"+
		"\u001d\u0000\u0135\u0133\u0001\u0000\u0000\u0000\u0136\u0139\u0001\u0000"+
		"\u0000\u0000\u0137\u0135\u0001\u0000\u0000\u0000\u0137\u0138\u0001\u0000"+
		"\u0000\u0000\u0138\u013a\u0001\u0000\u0000\u0000\u0139\u0137\u0001\u0000"+
		"\u0000\u0000\u013a\u013b\u0005\u001e\u0000\u0000\u013b\u013c\u0003\u001c"+
		"\u000e\u0000\u013c\u001f\u0001\u0000\u0000\u0000\u013d\u013e\u0005 \u0000"+
		"\u0000\u013e\u0143\u0003:\u001d\u0000\u013f\u0140\u0005#\u0000\u0000\u0140"+
		"\u0142\u0003:\u001d\u0000\u0141\u013f\u0001\u0000\u0000\u0000\u0142\u0145"+
		"\u0001\u0000\u0000\u0000\u0143\u0141\u0001\u0000\u0000\u0000\u0143\u0144"+
		"\u0001\u0000\u0000\u0000\u0144\u0148\u0001\u0000\u0000\u0000\u0145\u0143"+
		"\u0001\u0000\u0000\u0000\u0146\u0147\u0005\u001e\u0000\u0000\u0147\u0149"+
		"\u0003\u001c\u000e\u0000\u0148\u0146\u0001\u0000\u0000\u0000\u0148\u0149"+
		"\u0001\u0000\u0000\u0000\u0149!\u0001\u0000\u0000\u0000\u014a\u014b\u0003"+
		"$\u0012\u0000\u014b\u014c\u0005\u001f\u0000\u0000\u014c\u014d\u00051\u0000"+
		"\u0000\u014d#\u0001\u0000\u0000\u0000\u014e\u014f\u0003&\u0013\u0000\u014f"+
		"%\u0001\u0000\u0000\u0000\u0150\u0155\u0003(\u0014\u0000\u0151\u0152\u0005"+
		"\u0001\u0000\u0000\u0152\u0154\u0003(\u0014\u0000\u0153\u0151\u0001\u0000"+
		"\u0000\u0000\u0154\u0157\u0001\u0000\u0000\u0000\u0155\u0153\u0001\u0000"+
		"\u0000\u0000\u0155\u0156\u0001\u0000\u0000\u0000\u0156\'\u0001\u0000\u0000"+
		"\u0000\u0157\u0155\u0001\u0000\u0000\u0000\u0158\u015d\u0003*\u0015\u0000"+
		"\u0159\u015a\u0005\u0002\u0000\u0000\u015a\u015c\u0003*\u0015\u0000\u015b"+
		"\u0159\u0001\u0000\u0000\u0000\u015c\u015f\u0001\u0000\u0000\u0000\u015d"+
		"\u015b\u0001\u0000\u0000\u0000\u015d\u015e\u0001\u0000\u0000\u0000\u015e"+
		")\u0001\u0000\u0000\u0000\u015f\u015d\u0001\u0000\u0000\u0000\u0160\u0161"+
		"\u0005\u0003\u0000\u0000\u0161\u0164\u0003*\u0015\u0000\u0162\u0164\u0003"+
		",\u0016\u0000\u0163\u0160\u0001\u0000\u0000\u0000\u0163\u0162\u0001\u0000"+
		"\u0000\u0000\u0164+\u0001\u0000\u0000\u0000\u0165\u0166\u0003.\u0017\u0000"+
		"\u0166\u0167\u0007\u0002\u0000\u0000\u0167\u0168\u0003.\u0017\u0000\u0168"+
		"\u0182\u0001\u0000\u0000\u0000\u0169\u016a\u0003.\u0017\u0000\u016a\u016b"+
		"\u0005\u000b\u0000\u0000\u016b\u016c\u0005!\u0000\u0000\u016c\u016f\u0003"+
		"F#\u0000\u016d\u016e\u0005#\u0000\u0000\u016e\u0170\u0003F#\u0000\u016f"+
		"\u016d\u0001\u0000\u0000\u0000\u0170\u0171\u0001\u0000\u0000\u0000\u0171"+
		"\u016f\u0001\u0000\u0000\u0000\u0171\u0172\u0001\u0000\u0000\u0000\u0172"+
		"\u0173\u0001\u0000\u0000\u0000\u0173\u0174\u0005\"\u0000\u0000\u0174\u0182"+
		"\u0001\u0000\u0000\u0000\u0175\u0176\u0003.\u0017\u0000\u0176\u0177\u0005"+
		"\u000b\u0000\u0000\u0177\u0178\u0005!\u0000\u0000\u0178\u0179\u0005\""+
		"\u0000\u0000\u0179\u0182\u0001\u0000\u0000\u0000\u017a\u017b\u0003.\u0017"+
		"\u0000\u017b\u017c\u0005\u000b\u0000\u0000\u017c\u017d\u0005!\u0000\u0000"+
		"\u017d\u017e\u0003$\u0012\u0000\u017e\u017f\u0005\"\u0000\u0000\u017f"+
		"\u0182\u0001\u0000\u0000\u0000\u0180\u0182\u0003.\u0017\u0000\u0181\u0165"+
		"\u0001\u0000\u0000\u0000\u0181\u0169\u0001\u0000\u0000\u0000\u0181\u0175"+
		"\u0001\u0000\u0000\u0000\u0181\u017a\u0001\u0000\u0000\u0000\u0181\u0180"+
		"\u0001\u0000\u0000\u0000\u0182-\u0001\u0000\u0000\u0000\u0183\u0184\u0006"+
		"\u0017\uffff\uffff\u0000\u0184\u0185\u00030\u0018\u0000\u0185\u018b\u0001"+
		"\u0000\u0000\u0000\u0186\u0187\n\u0002\u0000\u0000\u0187\u0188\u0007\u0003"+
		"\u0000\u0000\u0188\u018a\u00030\u0018\u0000\u0189\u0186\u0001\u0000\u0000"+
		"\u0000\u018a\u018d\u0001\u0000\u0000\u0000\u018b\u0189\u0001\u0000\u0000"+
		"\u0000\u018b\u018c\u0001\u0000\u0000\u0000\u018c/\u0001\u0000\u0000\u0000"+
		"\u018d\u018b\u0001\u0000\u0000\u0000\u018e\u018f\u0006\u0018\uffff\uffff"+
		"\u0000\u018f\u0190\u00032\u0019\u0000\u0190\u0196\u0001\u0000\u0000\u0000"+
		"\u0191\u0192\n\u0002\u0000\u0000\u0192\u0193\u0007\u0004\u0000\u0000\u0193"+
		"\u0195\u00032\u0019\u0000\u0194\u0191\u0001\u0000\u0000\u0000\u0195\u0198"+
		"\u0001\u0000\u0000\u0000\u0196\u0194\u0001\u0000\u0000\u0000\u0196\u0197"+
		"\u0001\u0000\u0000\u0000\u01971\u0001\u0000\u0000\u0000\u0198\u0196\u0001"+
		"\u0000\u0000\u0000\u0199\u01a3\u0003F#\u0000\u019a\u01a3\u00034\u001a"+
		"\u0000\u019b\u01a3\u00038\u001c\u0000\u019c\u01a3\u0003:\u001d\u0000\u019d"+
		"\u01a3\u00052\u0000\u0000\u019e\u019f\u0005!\u0000\u0000\u019f\u01a0\u0003"+
		"$\u0012\u0000\u01a0\u01a1\u0005\"\u0000\u0000\u01a1\u01a3\u0001\u0000"+
		"\u0000\u0000\u01a2\u0199\u0001\u0000\u0000\u0000\u01a2\u019a\u0001\u0000"+
		"\u0000\u0000\u01a2\u019b\u0001\u0000\u0000\u0000\u01a2\u019c\u0001\u0000"+
		"\u0000\u0000\u01a2\u019d\u0001\u0000\u0000\u0000\u01a2\u019e\u0001\u0000"+
		"\u0000\u0000\u01a33\u0001\u0000\u0000\u0000\u01a4\u01a5\u0007\u0005\u0000"+
		"\u0000\u01a5\u01a9\u0005!\u0000\u0000\u01a6\u01a7\u0003$\u0012\u0000\u01a7"+
		"\u01a8\u0005#\u0000\u0000\u01a8\u01aa\u0001\u0000\u0000\u0000\u01a9\u01a6"+
		"\u0001\u0000\u0000\u0000\u01a9\u01aa\u0001\u0000\u0000\u0000\u01aa\u01ab"+
		"\u0001\u0000\u0000\u0000\u01ab\u01ac\u00036\u001b\u0000\u01ac\u01ad\u0005"+
		"\"\u0000\u0000\u01ad5\u0001\u0000\u0000\u0000\u01ae\u01b3\u00051\u0000"+
		"\u0000\u01af\u01b0\u0005\'\u0000\u0000\u01b0\u01b2\u00051\u0000\u0000"+
		"\u01b1\u01af\u0001\u0000\u0000\u0000\u01b2\u01b5\u0001\u0000\u0000\u0000"+
		"\u01b3\u01b1\u0001\u0000\u0000\u0000\u01b3\u01b4\u0001\u0000\u0000\u0000"+
		"\u01b47\u0001\u0000\u0000\u0000\u01b5\u01b3\u0001\u0000\u0000\u0000\u01b6"+
		"\u01b7\u00051\u0000\u0000\u01b7\u01c0\u0005!\u0000\u0000\u01b8\u01bd\u0003"+
		"$\u0012\u0000\u01b9\u01ba\u0005#\u0000\u0000\u01ba\u01bc\u0003$\u0012"+
		"\u0000\u01bb\u01b9\u0001\u0000\u0000\u0000\u01bc\u01bf\u0001\u0000\u0000"+
		"\u0000\u01bd\u01bb\u0001\u0000\u0000\u0000\u01bd\u01be\u0001\u0000\u0000"+
		"\u0000\u01be\u01c1\u0001\u0000\u0000\u0000\u01bf\u01bd\u0001\u0000\u0000"+
		"\u0000\u01c0\u01b8\u0001\u0000\u0000\u0000\u01c0\u01c1\u0001\u0000\u0000"+
		"\u0000\u01c1\u01c2\u0001\u0000\u0000\u0000\u01c2\u01c3\u0005\"\u0000\u0000"+
		"\u01c39\u0001\u0000\u0000\u0000\u01c4\u01c9\u0003<\u001e\u0000\u01c5\u01c6"+
		"\u0005%\u0000\u0000\u01c6\u01c8\u0003<\u001e\u0000\u01c7\u01c5\u0001\u0000"+
		"\u0000\u0000\u01c8\u01cb\u0001\u0000\u0000\u0000\u01c9\u01c7\u0001\u0000"+
		"\u0000\u0000\u01c9\u01ca\u0001\u0000\u0000\u0000\u01ca\u01d0\u0001\u0000"+
		"\u0000\u0000\u01cb\u01c9\u0001\u0000\u0000\u0000\u01cc\u01cd\u0005%\u0000"+
		"\u0000\u01cd\u01d1\u0003D\"\u0000\u01ce\u01cf\u0005%\u0000\u0000\u01cf"+
		"\u01d1\u0005\u001b\u0000\u0000\u01d0\u01cc\u0001\u0000\u0000\u0000\u01d0"+
		"\u01ce\u0001\u0000\u0000\u0000\u01d0\u01d1\u0001\u0000\u0000\u0000\u01d1"+
		";\u0001\u0000\u0000\u0000\u01d2\u01d5\u00051\u0000\u0000\u01d3\u01d5\u0003"+
		">\u001f\u0000\u01d4\u01d2\u0001\u0000\u0000\u0000\u01d4\u01d3\u0001\u0000"+
		"\u0000\u0000\u01d5=\u0001\u0000\u0000\u0000\u01d6\u01db\u00051\u0000\u0000"+
		"\u01d7\u01d8\u0005\'\u0000\u0000\u01d8\u01da\u00051\u0000\u0000\u01d9"+
		"\u01d7\u0001\u0000\u0000\u0000\u01da\u01dd\u0001\u0000\u0000\u0000\u01db"+
		"\u01d9\u0001\u0000\u0000\u0000\u01db\u01dc\u0001\u0000\u0000\u0000\u01dc"+
		"\u01de\u0001\u0000\u0000\u0000\u01dd\u01db\u0001\u0000\u0000\u0000\u01de"+
		"\u01e0\u0005!\u0000\u0000\u01df\u01e1\u0003@ \u0000\u01e0\u01df\u0001"+
		"\u0000\u0000\u0000\u01e0\u01e1\u0001\u0000\u0000\u0000\u01e1\u01e2\u0001"+
		"\u0000\u0000\u0000\u01e2\u01e3\u0005\"\u0000\u0000\u01e3?\u0001\u0000"+
		"\u0000\u0000\u01e4\u01e9\u0003B!\u0000\u01e5\u01e6\u0005#\u0000\u0000"+
		"\u01e6\u01e8\u0003B!\u0000\u01e7\u01e5\u0001\u0000\u0000\u0000\u01e8\u01eb"+
		"\u0001\u0000\u0000\u0000\u01e9\u01e7\u0001\u0000\u0000\u0000\u01e9\u01ea"+
		"\u0001\u0000\u0000\u0000\u01ea\u01f5\u0001\u0000\u0000\u0000\u01eb\u01e9"+
		"\u0001\u0000\u0000\u0000\u01ec\u01f1\u0003$\u0012\u0000\u01ed\u01ee\u0005"+
		"#\u0000\u0000\u01ee\u01f0\u0003$\u0012\u0000\u01ef\u01ed\u0001\u0000\u0000"+
		"\u0000\u01f0\u01f3\u0001\u0000\u0000\u0000\u01f1\u01ef\u0001\u0000\u0000"+
		"\u0000\u01f1\u01f2\u0001\u0000\u0000\u0000\u01f2\u01f5\u0001\u0000\u0000"+
		"\u0000\u01f3\u01f1\u0001\u0000\u0000\u0000\u01f4\u01e4\u0001\u0000\u0000"+
		"\u0000\u01f4\u01ec\u0001\u0000\u0000\u0000\u01f5A\u0001\u0000\u0000\u0000"+
		"\u01f6\u01f7\u00051\u0000\u0000\u01f7\u01f8\u0005$\u0000\u0000\u01f8\u01f9"+
		"\u0003$\u0012\u0000\u01f9C\u0001\u0000\u0000\u0000\u01fa\u01fb\u0005\u0017"+
		"\u0000\u0000\u01fb\u01ff\u0005!\u0000\u0000\u01fc\u01fd\u00051\u0000\u0000"+
		"\u01fd\u01fe\u0005&\u0000\u0000\u01fe\u0200\u0003$\u0012\u0000\u01ff\u01fc"+
		"\u0001\u0000\u0000\u0000\u01ff\u0200\u0001\u0000\u0000\u0000\u0200\u0201"+
		"\u0001\u0000\u0000\u0000\u0201\u020a\u0005\"\u0000\u0000\u0202\u0203\u0005"+
		"\u0018\u0000\u0000\u0203\u0204\u0005!\u0000\u0000\u0204\u0205\u00051\u0000"+
		"\u0000\u0205\u0206\u0005&\u0000\u0000\u0206\u0207\u0003$\u0012\u0000\u0207"+
		"\u0208\u0005\"\u0000\u0000\u0208\u020a\u0001\u0000\u0000\u0000\u0209\u01fa"+
		"\u0001\u0000\u0000\u0000\u0209\u0202\u0001\u0000\u0000\u0000\u020aE\u0001"+
		"\u0000\u0000\u0000\u020b\u0217\u0005.\u0000\u0000\u020c\u0217\u0005/\u0000"+
		"\u0000\u020d\u0217\u00050\u0000\u0000\u020e\u0217\u0007\u0006\u0000\u0000"+
		"\u020f\u0217\u0005\u0016\u0000\u0000\u0210\u0217\u0005*\u0000\u0000\u0211"+
		"\u0217\u0005+\u0000\u0000\u0212\u0217\u0005,\u0000\u0000\u0213\u0217\u0005"+
		"-\u0000\u0000\u0214\u0217\u0005(\u0000\u0000\u0215\u0217\u0005)\u0000"+
		"\u0000\u0216\u020b\u0001\u0000\u0000\u0000\u0216\u020c\u0001\u0000\u0000"+
		"\u0000\u0216\u020d\u0001\u0000\u0000\u0000\u0216\u020e\u0001\u0000\u0000"+
		"\u0000\u0216\u020f\u0001\u0000\u0000\u0000\u0216\u0210\u0001\u0000\u0000"+
		"\u0000\u0216\u0211\u0001\u0000\u0000\u0000\u0216\u0212\u0001\u0000\u0000"+
		"\u0000\u0216\u0213\u0001\u0000\u0000\u0000\u0216\u0214\u0001\u0000\u0000"+
		"\u0000\u0216\u0215\u0001\u0000\u0000\u0000\u0217G\u0001\u0000\u0000\u0000"+
		"3PW[amp|\u0080\u0086\u008d\u0097\u00a3\u00a9\u00b5\u00c2\u00cf\u00e9\u00f5"+
		"\u00ff\u0105\u010d\u0117\u0120\u0127\u012e\u0137\u0143\u0148\u0155\u015d"+
		"\u0163\u0171\u0181\u018b\u0196\u01a2\u01a9\u01b3\u01bd\u01c0\u01c9\u01d0"+
		"\u01d4\u01db\u01e0\u01e9\u01f1\u01f4\u01ff\u0209\u0216";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}