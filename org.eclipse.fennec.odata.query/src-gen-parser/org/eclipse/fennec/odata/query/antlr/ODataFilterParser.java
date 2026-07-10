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
		REF=29, FILTERQ=30, WITH=31, AS=32, FROM=33, MINUS=34, LPAREN=35, RPAREN=36, 
		COMMA=37, EQUALS=38, SLASH=39, COLON=40, DOT=41, NANLIT=42, INF=43, BINARY=44, 
		DURATION=45, ENUM=46, GUID=47, DATETIMEOFFSET=48, DATE=49, TIMEOFDAY=50, 
		STRING=51, DECIMAL=52, INT=53, IDENT=54, ALIAS=55, WS=56;
	public static final int
		RULE_filter = 0, RULE_orderby = 1, RULE_orderbyItem = 2, RULE_resource = 3, 
		RULE_keyPredicate = 4, RULE_namedKeyValue = 5, RULE_keyLiteral = 6, RULE_resourceSegment = 7, 
		RULE_castName = 8, RULE_apply = 9, RULE_applySeq = 10, RULE_applyTrafo = 11, 
		RULE_groupbyElement = 12, RULE_aggregateItem = 13, RULE_methodName = 14, 
		RULE_aggrFrom = 15, RULE_customFrom = 16, RULE_computeItem = 17, RULE_expr = 18, 
		RULE_orExpr = 19, RULE_andExpr = 20, RULE_notExpr = 21, RULE_comparison = 22, 
		RULE_additive = 23, RULE_multiplicative = 24, RULE_primary = 25, RULE_typeFunc = 26, 
		RULE_qualifiedTypeName = 27, RULE_functionCall = 28, RULE_memberPath = 29, 
		RULE_pathStep = 30, RULE_lastSegment = 31, RULE_countCall = 32, RULE_boundCall = 33, 
		RULE_boundCallArgs = 34, RULE_namedArg = 35, RULE_lambdaCall = 36, RULE_literal = 37;
	private static String[] makeRuleNames() {
		return new String[] {
			"filter", "orderby", "orderbyItem", "resource", "keyPredicate", "namedKeyValue", 
			"keyLiteral", "resourceSegment", "castName", "apply", "applySeq", "applyTrafo", 
			"groupbyElement", "aggregateItem", "methodName", "aggrFrom", "customFrom", 
			"computeItem", "expr", "orExpr", "andExpr", "notExpr", "comparison", 
			"additive", "multiplicative", "primary", "typeFunc", "qualifiedTypeName", 
			"functionCall", "memberPath", "pathStep", "lastSegment", "countCall", 
			"boundCall", "boundCallArgs", "namedArg", "lambdaCall", "literal"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, "'true'", "'false'", 
			"'null'", "'any'", "'all'", null, null, "'$count'", "'$value'", "'$ref'", 
			"'$filter'", "'with'", "'as'", "'from'", "'-'", "'('", "')'", "','", 
			"'='", "'/'", "':'", "'.'", "'NaN'", "'INF'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "OR", "AND", "NOT", "EQ", "NE", "GT", "GE", "LT", "LE", "HAS", 
			"IN", "ADD", "SUB", "MUL", "DIVBY", "DIV", "MOD", "ASC", "DESC", "TRUE", 
			"FALSE", "NULL", "ANY", "ALL", "CAST", "ISOF", "COUNT", "VALUE", "REF", 
			"FILTERQ", "WITH", "AS", "FROM", "MINUS", "LPAREN", "RPAREN", "COMMA", 
			"EQUALS", "SLASH", "COLON", "DOT", "NANLIT", "INF", "BINARY", "DURATION", 
			"ENUM", "GUID", "DATETIMEOFFSET", "DATE", "TIMEOFDAY", "STRING", "DECIMAL", 
			"INT", "IDENT", "ALIAS", "WS"
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
			setState(76);
			expr();
			setState(77);
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
			setState(79);
			orderbyItem();
			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(80);
				match(COMMA);
				setState(81);
				orderbyItem();
				}
				}
				setState(86);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(87);
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
			setState(89);
			expr();
			setState(91);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(90);
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
			setState(93);
			match(IDENT);
			setState(95);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(94);
				keyPredicate();
				}
			}

			setState(101);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SLASH) {
				{
				{
				setState(97);
				match(SLASH);
				setState(98);
				resourceSegment();
				}
				}
				setState(103);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(104);
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
			setState(106);
			match(LPAREN);
			setState(116);
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
				setState(107);
				keyLiteral();
				}
				break;
			case IDENT:
				{
				setState(108);
				namedKeyValue();
				setState(113);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(109);
					match(COMMA);
					setState(110);
					namedKeyValue();
					}
					}
					setState(115);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(118);
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
			setState(120);
			match(IDENT);
			setState(121);
			match(EQUALS);
			setState(122);
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
			setState(124);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 17873661021126656L) != 0)) ) {
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
			setState(138);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				_localctx = new CastSegmentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(126);
				castName();
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
			case 2:
				_localctx = new PropertySegmentContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(130);
				match(IDENT);
				setState(132);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(131);
					keyPredicate();
					}
				}

				}
				break;
			case 3:
				_localctx = new CountSegmentContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(134);
				match(COUNT);
				}
				break;
			case 4:
				_localctx = new ValueSegmentContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(135);
				match(VALUE);
				}
				break;
			case 5:
				_localctx = new RefSegmentContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(136);
				match(REF);
				}
				break;
			case 6:
				_localctx = new KeyValueSegmentContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(137);
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
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(140);
			match(IDENT);
			setState(143); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(141);
					match(DOT);
					setState(142);
					match(IDENT);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(145); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
			setState(147);
			applySeq();
			setState(148);
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
			setState(150);
			applyTrafo();
			setState(155);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SLASH) {
				{
				{
				setState(151);
				match(SLASH);
				setState(152);
				applyTrafo();
				}
				}
				setState(157);
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
			setState(249);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				_localctx = new GroupByTrafoContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(158);
				if (!(trafo("groupby"))) throw new FailedPredicateException(this, "trafo(\"groupby\")");
				setState(159);
				((GroupByTrafoContext)_localctx).name = match(IDENT);
				setState(160);
				match(LPAREN);
				setState(161);
				match(LPAREN);
				setState(162);
				groupbyElement();
				setState(167);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(163);
					match(COMMA);
					setState(164);
					groupbyElement();
					}
					}
					setState(169);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(170);
				match(RPAREN);
				setState(173);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(171);
					match(COMMA);
					setState(172);
					applySeq();
					}
				}

				setState(175);
				match(RPAREN);
				}
				break;
			case 2:
				_localctx = new AggregateTrafoContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(177);
				if (!(trafo("aggregate"))) throw new FailedPredicateException(this, "trafo(\"aggregate\")");
				setState(178);
				((AggregateTrafoContext)_localctx).name = match(IDENT);
				setState(179);
				match(LPAREN);
				setState(180);
				aggregateItem();
				setState(185);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(181);
					match(COMMA);
					setState(182);
					aggregateItem();
					}
					}
					setState(187);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(188);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new ComputeTrafoContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(190);
				if (!(trafo("compute"))) throw new FailedPredicateException(this, "trafo(\"compute\")");
				setState(191);
				((ComputeTrafoContext)_localctx).name = match(IDENT);
				setState(192);
				match(LPAREN);
				setState(193);
				computeItem();
				setState(198);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(194);
					match(COMMA);
					setState(195);
					computeItem();
					}
					}
					setState(200);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(201);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new ConcatTrafoContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(203);
				if (!(trafo("concat"))) throw new FailedPredicateException(this, "trafo(\"concat\")");
				setState(204);
				((ConcatTrafoContext)_localctx).name = match(IDENT);
				setState(205);
				match(LPAREN);
				setState(206);
				applySeq();
				setState(209); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(207);
					match(COMMA);
					setState(208);
					applySeq();
					}
					}
					setState(211); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(213);
				match(RPAREN);
				}
				break;
			case 5:
				_localctx = new FilterTrafoContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(215);
				if (!(trafo("filter"))) throw new FailedPredicateException(this, "trafo(\"filter\")");
				setState(216);
				((FilterTrafoContext)_localctx).name = match(IDENT);
				setState(217);
				match(LPAREN);
				setState(218);
				expr();
				setState(219);
				match(RPAREN);
				}
				break;
			case 6:
				_localctx = new BottomTopTrafoContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(221);
				if (!(bottomTopTrafo())) throw new FailedPredicateException(this, "bottomTopTrafo()");
				setState(222);
				((BottomTopTrafoContext)_localctx).name = match(IDENT);
				setState(223);
				match(LPAREN);
				setState(224);
				expr();
				setState(225);
				match(COMMA);
				setState(226);
				expr();
				setState(227);
				match(RPAREN);
				}
				break;
			case 7:
				_localctx = new OrderByTrafoContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(229);
				if (!(trafo("orderby"))) throw new FailedPredicateException(this, "trafo(\"orderby\")");
				setState(230);
				((OrderByTrafoContext)_localctx).name = match(IDENT);
				setState(231);
				match(LPAREN);
				setState(232);
				orderbyItem();
				setState(237);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(233);
					match(COMMA);
					setState(234);
					orderbyItem();
					}
					}
					setState(239);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(240);
				match(RPAREN);
				}
				break;
			case 8:
				_localctx = new RowLimitTrafoContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(242);
				if (!(trafo("top") || trafo("skip"))) throw new FailedPredicateException(this, "trafo(\"top\") || trafo(\"skip\")");
				setState(243);
				((RowLimitTrafoContext)_localctx).name = match(IDENT);
				setState(244);
				match(LPAREN);
				setState(245);
				match(INT);
				setState(246);
				match(RPAREN);
				}
				break;
			case 9:
				_localctx = new IdentityTrafoContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(247);
				if (!(trafo("identity"))) throw new FailedPredicateException(this, "trafo(\"identity\")");
				setState(248);
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
			setState(265);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				_localctx = new RollupElementContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(251);
				if (!(trafo("rollup"))) throw new FailedPredicateException(this, "trafo(\"rollup\")");
				setState(252);
				((RollupElementContext)_localctx).name = match(IDENT);
				setState(253);
				match(LPAREN);
				setState(254);
				memberPath();
				setState(259);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(255);
					match(COMMA);
					setState(256);
					memberPath();
					}
					}
					setState(261);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(262);
				match(RPAREN);
				}
				break;
			case 2:
				_localctx = new PathElementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(264);
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
			setState(299);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				_localctx = new AggregateWithItemContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(267);
				expr();
				setState(268);
				match(WITH);
				setState(269);
				((AggregateWithItemContext)_localctx).method = methodName();
				setState(273);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(270);
					aggrFrom();
					}
					}
					setState(275);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(276);
				match(AS);
				setState(277);
				((AggregateWithItemContext)_localctx).alias = match(IDENT);
				}
				break;
			case 2:
				_localctx = new AggregateCountItemContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(279);
				match(COUNT);
				setState(283);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(280);
					aggrFrom();
					}
					}
					setState(285);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(286);
				match(AS);
				setState(287);
				((AggregateCountItemContext)_localctx).alias = match(IDENT);
				}
				break;
			case 3:
				_localctx = new AggregateCustomAliasedContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(288);
				expr();
				setState(292);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(289);
					customFrom();
					}
					}
					setState(294);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(295);
				match(AS);
				setState(296);
				((AggregateCustomAliasedContext)_localctx).alias = match(IDENT);
				}
				break;
			case 4:
				_localctx = new AggregateCustomBareContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(298);
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
			setState(301);
			match(IDENT);
			setState(306);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(302);
				match(DOT);
				setState(303);
				match(IDENT);
				}
				}
				setState(308);
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
			setState(309);
			match(FROM);
			setState(310);
			memberPath();
			setState(315);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(311);
				match(COMMA);
				setState(312);
				memberPath();
				}
				}
				setState(317);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(318);
			match(WITH);
			setState(319);
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
			setState(321);
			match(FROM);
			setState(322);
			memberPath();
			setState(327);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(323);
				match(COMMA);
				setState(324);
				memberPath();
				}
				}
				setState(329);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(332);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(330);
				match(WITH);
				setState(331);
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
			setState(334);
			expr();
			setState(335);
			match(AS);
			setState(336);
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
			setState(338);
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
			setState(340);
			andExpr();
			setState(345);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(341);
				match(OR);
				setState(342);
				andExpr();
				}
				}
				setState(347);
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
			setState(348);
			notExpr();
			setState(353);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(349);
				match(AND);
				setState(350);
				notExpr();
				}
				}
				setState(355);
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
			setState(359);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				_localctx = new NotExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(356);
				match(NOT);
				setState(357);
				notExpr();
				}
				break;
			case TRUE:
			case FALSE:
			case NULL:
			case CAST:
			case ISOF:
			case MINUS:
			case LPAREN:
			case NANLIT:
			case INF:
			case BINARY:
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
				setState(358);
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
			setState(389);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				_localctx = new BinaryComparisonContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(361);
				additive(0);
				setState(362);
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
				setState(363);
				additive(0);
				}
				break;
			case 2:
				_localctx = new InListComparisonContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(365);
				additive(0);
				setState(366);
				match(IN);
				setState(367);
				match(LPAREN);
				setState(368);
				literal();
				setState(371); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(369);
					match(COMMA);
					setState(370);
					literal();
					}
					}
					setState(373); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(375);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new InEmptyListComparisonContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(377);
				additive(0);
				setState(378);
				match(IN);
				setState(379);
				match(LPAREN);
				setState(380);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new InComparisonContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(382);
				additive(0);
				setState(383);
				match(IN);
				setState(384);
				match(LPAREN);
				setState(385);
				expr();
				setState(386);
				match(RPAREN);
				}
				break;
			case 5:
				_localctx = new PassThroughContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(388);
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

			setState(392);
			multiplicative(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(399);
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
					setState(394);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(395);
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
					setState(396);
					multiplicative(0);
					}
					} 
				}
				setState(401);
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

			setState(403);
			primary();
			}
			_ctx.stop = _input.LT(-1);
			setState(410);
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
					setState(405);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(406);
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
					setState(407);
					primary();
					}
					} 
				}
				setState(412);
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
	public static class NegatedPrimaryContext extends PrimaryContext {
		public TerminalNode MINUS() { return getToken(ODataFilterParser.MINUS, 0); }
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public NegatedPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitNegatedPrimary(this);
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
			setState(424);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				_localctx = new LiteralPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(413);
				literal();
				}
				break;
			case 2:
				_localctx = new TypeFuncPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(414);
				typeFunc();
				}
				break;
			case 3:
				_localctx = new FunctionPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(415);
				functionCall();
				}
				break;
			case 4:
				_localctx = new MemberPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(416);
				memberPath();
				}
				break;
			case 5:
				_localctx = new AliasPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(417);
				match(ALIAS);
				}
				break;
			case 6:
				_localctx = new ParenPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(418);
				match(LPAREN);
				setState(419);
				expr();
				setState(420);
				match(RPAREN);
				}
				break;
			case 7:
				_localctx = new NegatedPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(422);
				match(MINUS);
				setState(423);
				primary();
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
			setState(426);
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
			setState(427);
			match(LPAREN);
			setState(431);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(428);
				expr();
				setState(429);
				match(COMMA);
				}
				break;
			}
			setState(433);
			qualifiedTypeName();
			setState(434);
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
			setState(436);
			match(IDENT);
			setState(441);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(437);
				match(DOT);
				setState(438);
				match(IDENT);
				}
				}
				setState(443);
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
			setState(444);
			match(IDENT);
			setState(445);
			match(LPAREN);
			setState(454);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 72053247639027720L) != 0)) {
				{
				setState(446);
				expr();
				setState(451);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(447);
					match(COMMA);
					setState(448);
					expr();
					}
					}
					setState(453);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(456);
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
		public LastSegmentContext lastSegment() {
			return getRuleContext(LastSegmentContext.class,0);
		}
		public List<PathStepContext> pathStep() {
			return getRuleContexts(PathStepContext.class);
		}
		public PathStepContext pathStep(int i) {
			return getRuleContext(PathStepContext.class,i);
		}
		public List<TerminalNode> SLASH() { return getTokens(ODataFilterParser.SLASH); }
		public TerminalNode SLASH(int i) {
			return getToken(ODataFilterParser.SLASH, i);
		}
		public LambdaCallContext lambdaCall() {
			return getRuleContext(LambdaCallContext.class,0);
		}
		public CountCallContext countCall() {
			return getRuleContext(CountCallContext.class,0);
		}
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
			setState(463);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(458);
					pathStep();
					setState(459);
					match(SLASH);
					}
					} 
				}
				setState(465);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,40,_ctx);
			}
			setState(466);
			lastSegment();
			setState(471);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				{
				setState(467);
				match(SLASH);
				setState(468);
				lambdaCall();
				}
				break;
			case 2:
				{
				setState(469);
				match(SLASH);
				setState(470);
				countCall();
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
	public static class PathStepContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public BoundCallContext boundCall() {
			return getRuleContext(BoundCallContext.class,0);
		}
		public CastNameContext castName() {
			return getRuleContext(CastNameContext.class,0);
		}
		public PathStepContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pathStep; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitPathStep(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PathStepContext pathStep() throws RecognitionException {
		PathStepContext _localctx = new PathStepContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_pathStep);
		try {
			setState(476);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(473);
				match(IDENT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(474);
				boundCall();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(475);
				castName();
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
	public static class LastSegmentContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public BoundCallContext boundCall() {
			return getRuleContext(BoundCallContext.class,0);
		}
		public CastNameContext castName() {
			return getRuleContext(CastNameContext.class,0);
		}
		public LastSegmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lastSegment; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitLastSegment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LastSegmentContext lastSegment() throws RecognitionException {
		LastSegmentContext _localctx = new LastSegmentContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_lastSegment);
		try {
			setState(481);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(478);
				match(IDENT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(479);
				boundCall();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(480);
				castName();
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
	public static class CountCallContext extends ParserRuleContext {
		public TerminalNode COUNT() { return getToken(ODataFilterParser.COUNT, 0); }
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public TerminalNode FILTERQ() { return getToken(ODataFilterParser.FILTERQ, 0); }
		public TerminalNode EQUALS() { return getToken(ODataFilterParser.EQUALS, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public CountCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_countCall; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitCountCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CountCallContext countCall() throws RecognitionException {
		CountCallContext _localctx = new CountCallContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_countCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(483);
			match(COUNT);
			setState(490);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
			case 1:
				{
				setState(484);
				match(LPAREN);
				setState(485);
				match(FILTERQ);
				setState(486);
				match(EQUALS);
				setState(487);
				expr();
				setState(488);
				match(RPAREN);
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
		enterRule(_localctx, 66, RULE_boundCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(492);
			match(IDENT);
			setState(497);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(493);
				match(DOT);
				setState(494);
				match(IDENT);
				}
				}
				setState(499);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(500);
			match(LPAREN);
			setState(502);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 72053247639027720L) != 0)) {
				{
				setState(501);
				boundCallArgs();
				}
			}

			setState(504);
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
		enterRule(_localctx, 68, RULE_boundCallArgs);
		int _la;
		try {
			setState(522);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,49,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(506);
				namedArg();
				setState(511);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(507);
					match(COMMA);
					setState(508);
					namedArg();
					}
					}
					setState(513);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(514);
				expr();
				setState(519);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(515);
					match(COMMA);
					setState(516);
					expr();
					}
					}
					setState(521);
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
		enterRule(_localctx, 70, RULE_namedArg);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(524);
			match(IDENT);
			setState(525);
			match(EQUALS);
			setState(526);
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
		enterRule(_localctx, 72, RULE_lambdaCall);
		int _la;
		try {
			setState(543);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANY:
				enterOuterAlt(_localctx, 1);
				{
				setState(528);
				((LambdaCallContext)_localctx).op = match(ANY);
				setState(529);
				match(LPAREN);
				setState(533);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IDENT) {
					{
					setState(530);
					match(IDENT);
					setState(531);
					match(COLON);
					setState(532);
					expr();
					}
				}

				setState(535);
				match(RPAREN);
				}
				break;
			case ALL:
				enterOuterAlt(_localctx, 2);
				{
				setState(536);
				((LambdaCallContext)_localctx).op = match(ALL);
				setState(537);
				match(LPAREN);
				setState(538);
				match(IDENT);
				setState(539);
				match(COLON);
				setState(540);
				expr();
				setState(541);
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
	public static class NanInfLiteralContext extends LiteralContext {
		public TerminalNode NANLIT() { return getToken(ODataFilterParser.NANLIT, 0); }
		public TerminalNode INF() { return getToken(ODataFilterParser.INF, 0); }
		public NanInfLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitNanInfLiteral(this);
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
	public static class DurationLiteralContext extends LiteralContext {
		public TerminalNode DURATION() { return getToken(ODataFilterParser.DURATION, 0); }
		public DurationLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitDurationLiteral(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BinaryLiteralContext extends LiteralContext {
		public TerminalNode BINARY() { return getToken(ODataFilterParser.BINARY, 0); }
		public BinaryLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitBinaryLiteral(this);
			else return visitor.visitChildren(this);
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

	public final LiteralContext literal() throws RecognitionException {
		LiteralContext _localctx = new LiteralContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_literal);
		int _la;
		try {
			setState(558);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(545);
				match(STRING);
				}
				break;
			case BINARY:
				_localctx = new BinaryLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(546);
				match(BINARY);
				}
				break;
			case NANLIT:
			case INF:
				_localctx = new NanInfLiteralContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(547);
				_la = _input.LA(1);
				if ( !(_la==NANLIT || _la==INF) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case DECIMAL:
				_localctx = new DecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(548);
				match(DECIMAL);
				}
				break;
			case INT:
				_localctx = new IntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(549);
				match(INT);
				}
				break;
			case TRUE:
			case FALSE:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(550);
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
				enterOuterAlt(_localctx, 7);
				{
				setState(551);
				match(NULL);
				}
				break;
			case GUID:
				_localctx = new GuidLiteralContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(552);
				match(GUID);
				}
				break;
			case DATETIMEOFFSET:
				_localctx = new DateTimeOffsetLiteralContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(553);
				match(DATETIMEOFFSET);
				}
				break;
			case DATE:
				_localctx = new DateLiteralContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(554);
				match(DATE);
				}
				break;
			case TIMEOFDAY:
				_localctx = new TimeOfDayLiteralContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(555);
				match(TIMEOFDAY);
				}
				break;
			case DURATION:
				_localctx = new DurationLiteralContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(556);
				match(DURATION);
				}
				break;
			case ENUM:
				_localctx = new EnumLiteralContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(557);
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
		"\u0004\u00018\u0231\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u0001S\b\u0001\n\u0001\f\u0001"+
		"V\t\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0003\u0002"+
		"\\\b\u0002\u0001\u0003\u0001\u0003\u0003\u0003`\b\u0003\u0001\u0003\u0001"+
		"\u0003\u0005\u0003d\b\u0003\n\u0003\f\u0003g\t\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0005"+
		"\u0004p\b\u0004\n\u0004\f\u0004s\t\u0004\u0003\u0004u\b\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0007\u0001\u0007\u0003\u0007\u0081\b\u0007\u0001\u0007"+
		"\u0001\u0007\u0003\u0007\u0085\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0003\u0007\u008b\b\u0007\u0001\b\u0001\b\u0001\b\u0004\b"+
		"\u0090\b\b\u000b\b\f\b\u0091\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001"+
		"\n\u0005\n\u009a\b\n\n\n\f\n\u009d\t\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u00a6\b\u000b"+
		"\n\u000b\f\u000b\u00a9\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003"+
		"\u000b\u00ae\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u00b8\b\u000b\n"+
		"\u000b\f\u000b\u00bb\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u00c5"+
		"\b\u000b\n\u000b\f\u000b\u00c8\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0004\u000b"+
		"\u00d2\b\u000b\u000b\u000b\f\u000b\u00d3\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0005\u000b\u00ec\b\u000b\n\u000b\f\u000b\u00ef\t\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u00fa\b\u000b\u0001\f"+
		"\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u0102\b\f\n\f\f\f\u0105"+
		"\t\f\u0001\f\u0001\f\u0001\f\u0003\f\u010a\b\f\u0001\r\u0001\r\u0001\r"+
		"\u0001\r\u0005\r\u0110\b\r\n\r\f\r\u0113\t\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0005\r\u011a\b\r\n\r\f\r\u011d\t\r\u0001\r\u0001\r\u0001\r"+
		"\u0001\r\u0005\r\u0123\b\r\n\r\f\r\u0126\t\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0003\r\u012c\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0005\u000e\u0131"+
		"\b\u000e\n\u000e\f\u000e\u0134\t\u000e\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0005\u000f\u013a\b\u000f\n\u000f\f\u000f\u013d\t\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0005\u0010\u0146\b\u0010\n\u0010\f\u0010\u0149\t\u0010\u0001\u0010"+
		"\u0001\u0010\u0003\u0010\u014d\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0005\u0013\u0158\b\u0013\n\u0013\f\u0013\u015b\t\u0013\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0005\u0014\u0160\b\u0014\n\u0014\f\u0014\u0163\t\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u0168\b\u0015\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0004\u0016\u0174\b\u0016\u000b\u0016"+
		"\f\u0016\u0175\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0186\b\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017"+
		"\u018e\b\u0017\n\u0017\f\u0017\u0191\t\u0017\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u0199\b\u0018\n"+
		"\u0018\f\u0018\u019c\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0003\u0019\u01a9\b\u0019\u0001\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u01b0\b\u001a\u0001\u001a\u0001"+
		"\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0005\u001b\u01b8"+
		"\b\u001b\n\u001b\f\u001b\u01bb\t\u001b\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0005\u001c\u01c2\b\u001c\n\u001c\f\u001c\u01c5"+
		"\t\u001c\u0003\u001c\u01c7\b\u001c\u0001\u001c\u0001\u001c\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0005\u001d\u01ce\b\u001d\n\u001d\f\u001d\u01d1"+
		"\t\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0003"+
		"\u001d\u01d8\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001e\u01dd"+
		"\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001f\u01e2\b\u001f"+
		"\u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0001 \u0003 \u01eb\b \u0001"+
		"!\u0001!\u0001!\u0005!\u01f0\b!\n!\f!\u01f3\t!\u0001!\u0001!\u0003!\u01f7"+
		"\b!\u0001!\u0001!\u0001\"\u0001\"\u0001\"\u0005\"\u01fe\b\"\n\"\f\"\u0201"+
		"\t\"\u0001\"\u0001\"\u0001\"\u0005\"\u0206\b\"\n\"\f\"\u0209\t\"\u0003"+
		"\"\u020b\b\"\u0001#\u0001#\u0001#\u0001#\u0001$\u0001$\u0001$\u0001$\u0001"+
		"$\u0003$\u0216\b$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001$\u0001"+
		"$\u0003$\u0220\b$\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001%\u0001"+
		"%\u0001%\u0001%\u0001%\u0001%\u0001%\u0003%\u022f\b%\u0001%\u0000\u0002"+
		".0&\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018"+
		"\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJ\u0000\b\u0001\u0000\u0012\u0013"+
		"\u0001\u0000/5\u0001\u0000\u0004\n\u0001\u0000\f\r\u0001\u0000\u000e\u0011"+
		"\u0001\u0000\u0019\u001a\u0001\u0000*+\u0001\u0000\u0014\u0015\u0262\u0000"+
		"L\u0001\u0000\u0000\u0000\u0002O\u0001\u0000\u0000\u0000\u0004Y\u0001"+
		"\u0000\u0000\u0000\u0006]\u0001\u0000\u0000\u0000\bj\u0001\u0000\u0000"+
		"\u0000\nx\u0001\u0000\u0000\u0000\f|\u0001\u0000\u0000\u0000\u000e\u008a"+
		"\u0001\u0000\u0000\u0000\u0010\u008c\u0001\u0000\u0000\u0000\u0012\u0093"+
		"\u0001\u0000\u0000\u0000\u0014\u0096\u0001\u0000\u0000\u0000\u0016\u00f9"+
		"\u0001\u0000\u0000\u0000\u0018\u0109\u0001\u0000\u0000\u0000\u001a\u012b"+
		"\u0001\u0000\u0000\u0000\u001c\u012d\u0001\u0000\u0000\u0000\u001e\u0135"+
		"\u0001\u0000\u0000\u0000 \u0141\u0001\u0000\u0000\u0000\"\u014e\u0001"+
		"\u0000\u0000\u0000$\u0152\u0001\u0000\u0000\u0000&\u0154\u0001\u0000\u0000"+
		"\u0000(\u015c\u0001\u0000\u0000\u0000*\u0167\u0001\u0000\u0000\u0000,"+
		"\u0185\u0001\u0000\u0000\u0000.\u0187\u0001\u0000\u0000\u00000\u0192\u0001"+
		"\u0000\u0000\u00002\u01a8\u0001\u0000\u0000\u00004\u01aa\u0001\u0000\u0000"+
		"\u00006\u01b4\u0001\u0000\u0000\u00008\u01bc\u0001\u0000\u0000\u0000:"+
		"\u01cf\u0001\u0000\u0000\u0000<\u01dc\u0001\u0000\u0000\u0000>\u01e1\u0001"+
		"\u0000\u0000\u0000@\u01e3\u0001\u0000\u0000\u0000B\u01ec\u0001\u0000\u0000"+
		"\u0000D\u020a\u0001\u0000\u0000\u0000F\u020c\u0001\u0000\u0000\u0000H"+
		"\u021f\u0001\u0000\u0000\u0000J\u022e\u0001\u0000\u0000\u0000LM\u0003"+
		"$\u0012\u0000MN\u0005\u0000\u0000\u0001N\u0001\u0001\u0000\u0000\u0000"+
		"OT\u0003\u0004\u0002\u0000PQ\u0005%\u0000\u0000QS\u0003\u0004\u0002\u0000"+
		"RP\u0001\u0000\u0000\u0000SV\u0001\u0000\u0000\u0000TR\u0001\u0000\u0000"+
		"\u0000TU\u0001\u0000\u0000\u0000UW\u0001\u0000\u0000\u0000VT\u0001\u0000"+
		"\u0000\u0000WX\u0005\u0000\u0000\u0001X\u0003\u0001\u0000\u0000\u0000"+
		"Y[\u0003$\u0012\u0000Z\\\u0007\u0000\u0000\u0000[Z\u0001\u0000\u0000\u0000"+
		"[\\\u0001\u0000\u0000\u0000\\\u0005\u0001\u0000\u0000\u0000]_\u00056\u0000"+
		"\u0000^`\u0003\b\u0004\u0000_^\u0001\u0000\u0000\u0000_`\u0001\u0000\u0000"+
		"\u0000`e\u0001\u0000\u0000\u0000ab\u0005\'\u0000\u0000bd\u0003\u000e\u0007"+
		"\u0000ca\u0001\u0000\u0000\u0000dg\u0001\u0000\u0000\u0000ec\u0001\u0000"+
		"\u0000\u0000ef\u0001\u0000\u0000\u0000fh\u0001\u0000\u0000\u0000ge\u0001"+
		"\u0000\u0000\u0000hi\u0005\u0000\u0000\u0001i\u0007\u0001\u0000\u0000"+
		"\u0000jt\u0005#\u0000\u0000ku\u0003\f\u0006\u0000lq\u0003\n\u0005\u0000"+
		"mn\u0005%\u0000\u0000np\u0003\n\u0005\u0000om\u0001\u0000\u0000\u0000"+
		"ps\u0001\u0000\u0000\u0000qo\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000"+
		"\u0000ru\u0001\u0000\u0000\u0000sq\u0001\u0000\u0000\u0000tk\u0001\u0000"+
		"\u0000\u0000tl\u0001\u0000\u0000\u0000uv\u0001\u0000\u0000\u0000vw\u0005"+
		"$\u0000\u0000w\t\u0001\u0000\u0000\u0000xy\u00056\u0000\u0000yz\u0005"+
		"&\u0000\u0000z{\u0003\f\u0006\u0000{\u000b\u0001\u0000\u0000\u0000|}\u0007"+
		"\u0001\u0000\u0000}\r\u0001\u0000\u0000\u0000~\u0080\u0003\u0010\b\u0000"+
		"\u007f\u0081\u0003\b\u0004\u0000\u0080\u007f\u0001\u0000\u0000\u0000\u0080"+
		"\u0081\u0001\u0000\u0000\u0000\u0081\u008b\u0001\u0000\u0000\u0000\u0082"+
		"\u0084\u00056\u0000\u0000\u0083\u0085\u0003\b\u0004\u0000\u0084\u0083"+
		"\u0001\u0000\u0000\u0000\u0084\u0085\u0001\u0000\u0000\u0000\u0085\u008b"+
		"\u0001\u0000\u0000\u0000\u0086\u008b\u0005\u001b\u0000\u0000\u0087\u008b"+
		"\u0005\u001c\u0000\u0000\u0088\u008b\u0005\u001d\u0000\u0000\u0089\u008b"+
		"\u0003\f\u0006\u0000\u008a~\u0001\u0000\u0000\u0000\u008a\u0082\u0001"+
		"\u0000\u0000\u0000\u008a\u0086\u0001\u0000\u0000\u0000\u008a\u0087\u0001"+
		"\u0000\u0000\u0000\u008a\u0088\u0001\u0000\u0000\u0000\u008a\u0089\u0001"+
		"\u0000\u0000\u0000\u008b\u000f\u0001\u0000\u0000\u0000\u008c\u008f\u0005"+
		"6\u0000\u0000\u008d\u008e\u0005)\u0000\u0000\u008e\u0090\u00056\u0000"+
		"\u0000\u008f\u008d\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000"+
		"\u0000\u0091\u008f\u0001\u0000\u0000\u0000\u0091\u0092\u0001\u0000\u0000"+
		"\u0000\u0092\u0011\u0001\u0000\u0000\u0000\u0093\u0094\u0003\u0014\n\u0000"+
		"\u0094\u0095\u0005\u0000\u0000\u0001\u0095\u0013\u0001\u0000\u0000\u0000"+
		"\u0096\u009b\u0003\u0016\u000b\u0000\u0097\u0098\u0005\'\u0000\u0000\u0098"+
		"\u009a\u0003\u0016\u000b\u0000\u0099\u0097\u0001\u0000\u0000\u0000\u009a"+
		"\u009d\u0001\u0000\u0000\u0000\u009b\u0099\u0001\u0000\u0000\u0000\u009b"+
		"\u009c\u0001\u0000\u0000\u0000\u009c\u0015\u0001\u0000\u0000\u0000\u009d"+
		"\u009b\u0001\u0000\u0000\u0000\u009e\u009f\u0004\u000b\u0000\u0000\u009f"+
		"\u00a0\u00056\u0000\u0000\u00a0\u00a1\u0005#\u0000\u0000\u00a1\u00a2\u0005"+
		"#\u0000\u0000\u00a2\u00a7\u0003\u0018\f\u0000\u00a3\u00a4\u0005%\u0000"+
		"\u0000\u00a4\u00a6\u0003\u0018\f\u0000\u00a5\u00a3\u0001\u0000\u0000\u0000"+
		"\u00a6\u00a9\u0001\u0000\u0000\u0000\u00a7\u00a5\u0001\u0000\u0000\u0000"+
		"\u00a7\u00a8\u0001\u0000\u0000\u0000\u00a8\u00aa\u0001\u0000\u0000\u0000"+
		"\u00a9\u00a7\u0001\u0000\u0000\u0000\u00aa\u00ad\u0005$\u0000\u0000\u00ab"+
		"\u00ac\u0005%\u0000\u0000\u00ac\u00ae\u0003\u0014\n\u0000\u00ad\u00ab"+
		"\u0001\u0000\u0000\u0000\u00ad\u00ae\u0001\u0000\u0000\u0000\u00ae\u00af"+
		"\u0001\u0000\u0000\u0000\u00af\u00b0\u0005$\u0000\u0000\u00b0\u00fa\u0001"+
		"\u0000\u0000\u0000\u00b1\u00b2\u0004\u000b\u0001\u0000\u00b2\u00b3\u0005"+
		"6\u0000\u0000\u00b3\u00b4\u0005#\u0000\u0000\u00b4\u00b9\u0003\u001a\r"+
		"\u0000\u00b5\u00b6\u0005%\u0000\u0000\u00b6\u00b8\u0003\u001a\r\u0000"+
		"\u00b7\u00b5\u0001\u0000\u0000\u0000\u00b8\u00bb\u0001\u0000\u0000\u0000"+
		"\u00b9\u00b7\u0001\u0000\u0000\u0000\u00b9\u00ba\u0001\u0000\u0000\u0000"+
		"\u00ba\u00bc\u0001\u0000\u0000\u0000\u00bb\u00b9\u0001\u0000\u0000\u0000"+
		"\u00bc\u00bd\u0005$\u0000\u0000\u00bd\u00fa\u0001\u0000\u0000\u0000\u00be"+
		"\u00bf\u0004\u000b\u0002\u0000\u00bf\u00c0\u00056\u0000\u0000\u00c0\u00c1"+
		"\u0005#\u0000\u0000\u00c1\u00c6\u0003\"\u0011\u0000\u00c2\u00c3\u0005"+
		"%\u0000\u0000\u00c3\u00c5\u0003\"\u0011\u0000\u00c4\u00c2\u0001\u0000"+
		"\u0000\u0000\u00c5\u00c8\u0001\u0000\u0000\u0000\u00c6\u00c4\u0001\u0000"+
		"\u0000\u0000\u00c6\u00c7\u0001\u0000\u0000\u0000\u00c7\u00c9\u0001\u0000"+
		"\u0000\u0000\u00c8\u00c6\u0001\u0000\u0000\u0000\u00c9\u00ca\u0005$\u0000"+
		"\u0000\u00ca\u00fa\u0001\u0000\u0000\u0000\u00cb\u00cc\u0004\u000b\u0003"+
		"\u0000\u00cc\u00cd\u00056\u0000\u0000\u00cd\u00ce\u0005#\u0000\u0000\u00ce"+
		"\u00d1\u0003\u0014\n\u0000\u00cf\u00d0\u0005%\u0000\u0000\u00d0\u00d2"+
		"\u0003\u0014\n\u0000\u00d1\u00cf\u0001\u0000\u0000\u0000\u00d2\u00d3\u0001"+
		"\u0000\u0000\u0000\u00d3\u00d1\u0001\u0000\u0000\u0000\u00d3\u00d4\u0001"+
		"\u0000\u0000\u0000\u00d4\u00d5\u0001\u0000\u0000\u0000\u00d5\u00d6\u0005"+
		"$\u0000\u0000\u00d6\u00fa\u0001\u0000\u0000\u0000\u00d7\u00d8\u0004\u000b"+
		"\u0004\u0000\u00d8\u00d9\u00056\u0000\u0000\u00d9\u00da\u0005#\u0000\u0000"+
		"\u00da\u00db\u0003$\u0012\u0000\u00db\u00dc\u0005$\u0000\u0000\u00dc\u00fa"+
		"\u0001\u0000\u0000\u0000\u00dd\u00de\u0004\u000b\u0005\u0000\u00de\u00df"+
		"\u00056\u0000\u0000\u00df\u00e0\u0005#\u0000\u0000\u00e0\u00e1\u0003$"+
		"\u0012\u0000\u00e1\u00e2\u0005%\u0000\u0000\u00e2\u00e3\u0003$\u0012\u0000"+
		"\u00e3\u00e4\u0005$\u0000\u0000\u00e4\u00fa\u0001\u0000\u0000\u0000\u00e5"+
		"\u00e6\u0004\u000b\u0006\u0000\u00e6\u00e7\u00056\u0000\u0000\u00e7\u00e8"+
		"\u0005#\u0000\u0000\u00e8\u00ed\u0003\u0004\u0002\u0000\u00e9\u00ea\u0005"+
		"%\u0000\u0000\u00ea\u00ec\u0003\u0004\u0002\u0000\u00eb\u00e9\u0001\u0000"+
		"\u0000\u0000\u00ec\u00ef\u0001\u0000\u0000\u0000\u00ed\u00eb\u0001\u0000"+
		"\u0000\u0000\u00ed\u00ee\u0001\u0000\u0000\u0000\u00ee\u00f0\u0001\u0000"+
		"\u0000\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000\u00f0\u00f1\u0005$\u0000"+
		"\u0000\u00f1\u00fa\u0001\u0000\u0000\u0000\u00f2\u00f3\u0004\u000b\u0007"+
		"\u0000\u00f3\u00f4\u00056\u0000\u0000\u00f4\u00f5\u0005#\u0000\u0000\u00f5"+
		"\u00f6\u00055\u0000\u0000\u00f6\u00fa\u0005$\u0000\u0000\u00f7\u00f8\u0004"+
		"\u000b\b\u0000\u00f8\u00fa\u00056\u0000\u0000\u00f9\u009e\u0001\u0000"+
		"\u0000\u0000\u00f9\u00b1\u0001\u0000\u0000\u0000\u00f9\u00be\u0001\u0000"+
		"\u0000\u0000\u00f9\u00cb\u0001\u0000\u0000\u0000\u00f9\u00d7\u0001\u0000"+
		"\u0000\u0000\u00f9\u00dd\u0001\u0000\u0000\u0000\u00f9\u00e5\u0001\u0000"+
		"\u0000\u0000\u00f9\u00f2\u0001\u0000\u0000\u0000\u00f9\u00f7\u0001\u0000"+
		"\u0000\u0000\u00fa\u0017\u0001\u0000\u0000\u0000\u00fb\u00fc\u0004\f\t"+
		"\u0000\u00fc\u00fd\u00056\u0000\u0000\u00fd\u00fe\u0005#\u0000\u0000\u00fe"+
		"\u0103\u0003:\u001d\u0000\u00ff\u0100\u0005%\u0000\u0000\u0100\u0102\u0003"+
		":\u001d\u0000\u0101\u00ff\u0001\u0000\u0000\u0000\u0102\u0105\u0001\u0000"+
		"\u0000\u0000\u0103\u0101\u0001\u0000\u0000\u0000\u0103\u0104\u0001\u0000"+
		"\u0000\u0000\u0104\u0106\u0001\u0000\u0000\u0000\u0105\u0103\u0001\u0000"+
		"\u0000\u0000\u0106\u0107\u0005$\u0000\u0000\u0107\u010a\u0001\u0000\u0000"+
		"\u0000\u0108\u010a\u0003:\u001d\u0000\u0109\u00fb\u0001\u0000\u0000\u0000"+
		"\u0109\u0108\u0001\u0000\u0000\u0000\u010a\u0019\u0001\u0000\u0000\u0000"+
		"\u010b\u010c\u0003$\u0012\u0000\u010c\u010d\u0005\u001f\u0000\u0000\u010d"+
		"\u0111\u0003\u001c\u000e\u0000\u010e\u0110\u0003\u001e\u000f\u0000\u010f"+
		"\u010e\u0001\u0000\u0000\u0000\u0110\u0113\u0001\u0000\u0000\u0000\u0111"+
		"\u010f\u0001\u0000\u0000\u0000\u0111\u0112\u0001\u0000\u0000\u0000\u0112"+
		"\u0114\u0001\u0000\u0000\u0000\u0113\u0111\u0001\u0000\u0000\u0000\u0114"+
		"\u0115\u0005 \u0000\u0000\u0115\u0116\u00056\u0000\u0000\u0116\u012c\u0001"+
		"\u0000\u0000\u0000\u0117\u011b\u0005\u001b\u0000\u0000\u0118\u011a\u0003"+
		"\u001e\u000f\u0000\u0119\u0118\u0001\u0000\u0000\u0000\u011a\u011d\u0001"+
		"\u0000\u0000\u0000\u011b\u0119\u0001\u0000\u0000\u0000\u011b\u011c\u0001"+
		"\u0000\u0000\u0000\u011c\u011e\u0001\u0000\u0000\u0000\u011d\u011b\u0001"+
		"\u0000\u0000\u0000\u011e\u011f\u0005 \u0000\u0000\u011f\u012c\u00056\u0000"+
		"\u0000\u0120\u0124\u0003$\u0012\u0000\u0121\u0123\u0003 \u0010\u0000\u0122"+
		"\u0121\u0001\u0000\u0000\u0000\u0123\u0126\u0001\u0000\u0000\u0000\u0124"+
		"\u0122\u0001\u0000\u0000\u0000\u0124\u0125\u0001\u0000\u0000\u0000\u0125"+
		"\u0127\u0001\u0000\u0000\u0000\u0126\u0124\u0001\u0000\u0000\u0000\u0127"+
		"\u0128\u0005 \u0000\u0000\u0128\u0129\u00056\u0000\u0000\u0129\u012c\u0001"+
		"\u0000\u0000\u0000\u012a\u012c\u0003$\u0012\u0000\u012b\u010b\u0001\u0000"+
		"\u0000\u0000\u012b\u0117\u0001\u0000\u0000\u0000\u012b\u0120\u0001\u0000"+
		"\u0000\u0000\u012b\u012a\u0001\u0000\u0000\u0000\u012c\u001b\u0001\u0000"+
		"\u0000\u0000\u012d\u0132\u00056\u0000\u0000\u012e\u012f\u0005)\u0000\u0000"+
		"\u012f\u0131\u00056\u0000\u0000\u0130\u012e\u0001\u0000\u0000\u0000\u0131"+
		"\u0134\u0001\u0000\u0000\u0000\u0132\u0130\u0001\u0000\u0000\u0000\u0132"+
		"\u0133\u0001\u0000\u0000\u0000\u0133\u001d\u0001\u0000\u0000\u0000\u0134"+
		"\u0132\u0001\u0000\u0000\u0000\u0135\u0136\u0005!\u0000\u0000\u0136\u013b"+
		"\u0003:\u001d\u0000\u0137\u0138\u0005%\u0000\u0000\u0138\u013a\u0003:"+
		"\u001d\u0000\u0139\u0137\u0001\u0000\u0000\u0000\u013a\u013d\u0001\u0000"+
		"\u0000\u0000\u013b\u0139\u0001\u0000\u0000\u0000\u013b\u013c\u0001\u0000"+
		"\u0000\u0000\u013c\u013e\u0001\u0000\u0000\u0000\u013d\u013b\u0001\u0000"+
		"\u0000\u0000\u013e\u013f\u0005\u001f\u0000\u0000\u013f\u0140\u0003\u001c"+
		"\u000e\u0000\u0140\u001f\u0001\u0000\u0000\u0000\u0141\u0142\u0005!\u0000"+
		"\u0000\u0142\u0147\u0003:\u001d\u0000\u0143\u0144\u0005%\u0000\u0000\u0144"+
		"\u0146\u0003:\u001d\u0000\u0145\u0143\u0001\u0000\u0000\u0000\u0146\u0149"+
		"\u0001\u0000\u0000\u0000\u0147\u0145\u0001\u0000\u0000\u0000\u0147\u0148"+
		"\u0001\u0000\u0000\u0000\u0148\u014c\u0001\u0000\u0000\u0000\u0149\u0147"+
		"\u0001\u0000\u0000\u0000\u014a\u014b\u0005\u001f\u0000\u0000\u014b\u014d"+
		"\u0003\u001c\u000e\u0000\u014c\u014a\u0001\u0000\u0000\u0000\u014c\u014d"+
		"\u0001\u0000\u0000\u0000\u014d!\u0001\u0000\u0000\u0000\u014e\u014f\u0003"+
		"$\u0012\u0000\u014f\u0150\u0005 \u0000\u0000\u0150\u0151\u00056\u0000"+
		"\u0000\u0151#\u0001\u0000\u0000\u0000\u0152\u0153\u0003&\u0013\u0000\u0153"+
		"%\u0001\u0000\u0000\u0000\u0154\u0159\u0003(\u0014\u0000\u0155\u0156\u0005"+
		"\u0001\u0000\u0000\u0156\u0158\u0003(\u0014\u0000\u0157\u0155\u0001\u0000"+
		"\u0000\u0000\u0158\u015b\u0001\u0000\u0000\u0000\u0159\u0157\u0001\u0000"+
		"\u0000\u0000\u0159\u015a\u0001\u0000\u0000\u0000\u015a\'\u0001\u0000\u0000"+
		"\u0000\u015b\u0159\u0001\u0000\u0000\u0000\u015c\u0161\u0003*\u0015\u0000"+
		"\u015d\u015e\u0005\u0002\u0000\u0000\u015e\u0160\u0003*\u0015\u0000\u015f"+
		"\u015d\u0001\u0000\u0000\u0000\u0160\u0163\u0001\u0000\u0000\u0000\u0161"+
		"\u015f\u0001\u0000\u0000\u0000\u0161\u0162\u0001\u0000\u0000\u0000\u0162"+
		")\u0001\u0000\u0000\u0000\u0163\u0161\u0001\u0000\u0000\u0000\u0164\u0165"+
		"\u0005\u0003\u0000\u0000\u0165\u0168\u0003*\u0015\u0000\u0166\u0168\u0003"+
		",\u0016\u0000\u0167\u0164\u0001\u0000\u0000\u0000\u0167\u0166\u0001\u0000"+
		"\u0000\u0000\u0168+\u0001\u0000\u0000\u0000\u0169\u016a\u0003.\u0017\u0000"+
		"\u016a\u016b\u0007\u0002\u0000\u0000\u016b\u016c\u0003.\u0017\u0000\u016c"+
		"\u0186\u0001\u0000\u0000\u0000\u016d\u016e\u0003.\u0017\u0000\u016e\u016f"+
		"\u0005\u000b\u0000\u0000\u016f\u0170\u0005#\u0000\u0000\u0170\u0173\u0003"+
		"J%\u0000\u0171\u0172\u0005%\u0000\u0000\u0172\u0174\u0003J%\u0000\u0173"+
		"\u0171\u0001\u0000\u0000\u0000\u0174\u0175\u0001\u0000\u0000\u0000\u0175"+
		"\u0173\u0001\u0000\u0000\u0000\u0175\u0176\u0001\u0000\u0000\u0000\u0176"+
		"\u0177\u0001\u0000\u0000\u0000\u0177\u0178\u0005$\u0000\u0000\u0178\u0186"+
		"\u0001\u0000\u0000\u0000\u0179\u017a\u0003.\u0017\u0000\u017a\u017b\u0005"+
		"\u000b\u0000\u0000\u017b\u017c\u0005#\u0000\u0000\u017c\u017d\u0005$\u0000"+
		"\u0000\u017d\u0186\u0001\u0000\u0000\u0000\u017e\u017f\u0003.\u0017\u0000"+
		"\u017f\u0180\u0005\u000b\u0000\u0000\u0180\u0181\u0005#\u0000\u0000\u0181"+
		"\u0182\u0003$\u0012\u0000\u0182\u0183\u0005$\u0000\u0000\u0183\u0186\u0001"+
		"\u0000\u0000\u0000\u0184\u0186\u0003.\u0017\u0000\u0185\u0169\u0001\u0000"+
		"\u0000\u0000\u0185\u016d\u0001\u0000\u0000\u0000\u0185\u0179\u0001\u0000"+
		"\u0000\u0000\u0185\u017e\u0001\u0000\u0000\u0000\u0185\u0184\u0001\u0000"+
		"\u0000\u0000\u0186-\u0001\u0000\u0000\u0000\u0187\u0188\u0006\u0017\uffff"+
		"\uffff\u0000\u0188\u0189\u00030\u0018\u0000\u0189\u018f\u0001\u0000\u0000"+
		"\u0000\u018a\u018b\n\u0002\u0000\u0000\u018b\u018c\u0007\u0003\u0000\u0000"+
		"\u018c\u018e\u00030\u0018\u0000\u018d\u018a\u0001\u0000\u0000\u0000\u018e"+
		"\u0191\u0001\u0000\u0000\u0000\u018f\u018d\u0001\u0000\u0000\u0000\u018f"+
		"\u0190\u0001\u0000\u0000\u0000\u0190/\u0001\u0000\u0000\u0000\u0191\u018f"+
		"\u0001\u0000\u0000\u0000\u0192\u0193\u0006\u0018\uffff\uffff\u0000\u0193"+
		"\u0194\u00032\u0019\u0000\u0194\u019a\u0001\u0000\u0000\u0000\u0195\u0196"+
		"\n\u0002\u0000\u0000\u0196\u0197\u0007\u0004\u0000\u0000\u0197\u0199\u0003"+
		"2\u0019\u0000\u0198\u0195\u0001\u0000\u0000\u0000\u0199\u019c\u0001\u0000"+
		"\u0000\u0000\u019a\u0198\u0001\u0000\u0000\u0000\u019a\u019b\u0001\u0000"+
		"\u0000\u0000\u019b1\u0001\u0000\u0000\u0000\u019c\u019a\u0001\u0000\u0000"+
		"\u0000\u019d\u01a9\u0003J%\u0000\u019e\u01a9\u00034\u001a\u0000\u019f"+
		"\u01a9\u00038\u001c\u0000\u01a0\u01a9\u0003:\u001d\u0000\u01a1\u01a9\u0005"+
		"7\u0000\u0000\u01a2\u01a3\u0005#\u0000\u0000\u01a3\u01a4\u0003$\u0012"+
		"\u0000\u01a4\u01a5\u0005$\u0000\u0000\u01a5\u01a9\u0001\u0000\u0000\u0000"+
		"\u01a6\u01a7\u0005\"\u0000\u0000\u01a7\u01a9\u00032\u0019\u0000\u01a8"+
		"\u019d\u0001\u0000\u0000\u0000\u01a8\u019e\u0001\u0000\u0000\u0000\u01a8"+
		"\u019f\u0001\u0000\u0000\u0000\u01a8\u01a0\u0001\u0000\u0000\u0000\u01a8"+
		"\u01a1\u0001\u0000\u0000\u0000\u01a8\u01a2\u0001\u0000\u0000\u0000\u01a8"+
		"\u01a6\u0001\u0000\u0000\u0000\u01a93\u0001\u0000\u0000\u0000\u01aa\u01ab"+
		"\u0007\u0005\u0000\u0000\u01ab\u01af\u0005#\u0000\u0000\u01ac\u01ad\u0003"+
		"$\u0012\u0000\u01ad\u01ae\u0005%\u0000\u0000\u01ae\u01b0\u0001\u0000\u0000"+
		"\u0000\u01af\u01ac\u0001\u0000\u0000\u0000\u01af\u01b0\u0001\u0000\u0000"+
		"\u0000\u01b0\u01b1\u0001\u0000\u0000\u0000\u01b1\u01b2\u00036\u001b\u0000"+
		"\u01b2\u01b3\u0005$\u0000\u0000\u01b35\u0001\u0000\u0000\u0000\u01b4\u01b9"+
		"\u00056\u0000\u0000\u01b5\u01b6\u0005)\u0000\u0000\u01b6\u01b8\u00056"+
		"\u0000\u0000\u01b7\u01b5\u0001\u0000\u0000\u0000\u01b8\u01bb\u0001\u0000"+
		"\u0000\u0000\u01b9\u01b7\u0001\u0000\u0000\u0000\u01b9\u01ba\u0001\u0000"+
		"\u0000\u0000\u01ba7\u0001\u0000\u0000\u0000\u01bb\u01b9\u0001\u0000\u0000"+
		"\u0000\u01bc\u01bd\u00056\u0000\u0000\u01bd\u01c6\u0005#\u0000\u0000\u01be"+
		"\u01c3\u0003$\u0012\u0000\u01bf\u01c0\u0005%\u0000\u0000\u01c0\u01c2\u0003"+
		"$\u0012\u0000\u01c1\u01bf\u0001\u0000\u0000\u0000\u01c2\u01c5\u0001\u0000"+
		"\u0000\u0000\u01c3\u01c1\u0001\u0000\u0000\u0000\u01c3\u01c4\u0001\u0000"+
		"\u0000\u0000\u01c4\u01c7\u0001\u0000\u0000\u0000\u01c5\u01c3\u0001\u0000"+
		"\u0000\u0000\u01c6\u01be\u0001\u0000\u0000\u0000\u01c6\u01c7\u0001\u0000"+
		"\u0000\u0000\u01c7\u01c8\u0001\u0000\u0000\u0000\u01c8\u01c9\u0005$\u0000"+
		"\u0000\u01c99\u0001\u0000\u0000\u0000\u01ca\u01cb\u0003<\u001e\u0000\u01cb"+
		"\u01cc\u0005\'\u0000\u0000\u01cc\u01ce\u0001\u0000\u0000\u0000\u01cd\u01ca"+
		"\u0001\u0000\u0000\u0000\u01ce\u01d1\u0001\u0000\u0000\u0000\u01cf\u01cd"+
		"\u0001\u0000\u0000\u0000\u01cf\u01d0\u0001\u0000\u0000\u0000\u01d0\u01d2"+
		"\u0001\u0000\u0000\u0000\u01d1\u01cf\u0001\u0000\u0000\u0000\u01d2\u01d7"+
		"\u0003>\u001f\u0000\u01d3\u01d4\u0005\'\u0000\u0000\u01d4\u01d8\u0003"+
		"H$\u0000\u01d5\u01d6\u0005\'\u0000\u0000\u01d6\u01d8\u0003@ \u0000\u01d7"+
		"\u01d3\u0001\u0000\u0000\u0000\u01d7\u01d5\u0001\u0000\u0000\u0000\u01d7"+
		"\u01d8\u0001\u0000\u0000\u0000\u01d8;\u0001\u0000\u0000\u0000\u01d9\u01dd"+
		"\u00056\u0000\u0000\u01da\u01dd\u0003B!\u0000\u01db\u01dd\u0003\u0010"+
		"\b\u0000\u01dc\u01d9\u0001\u0000\u0000\u0000\u01dc\u01da\u0001\u0000\u0000"+
		"\u0000\u01dc\u01db\u0001\u0000\u0000\u0000\u01dd=\u0001\u0000\u0000\u0000"+
		"\u01de\u01e2\u00056\u0000\u0000\u01df\u01e2\u0003B!\u0000\u01e0\u01e2"+
		"\u0003\u0010\b\u0000\u01e1\u01de\u0001\u0000\u0000\u0000\u01e1\u01df\u0001"+
		"\u0000\u0000\u0000\u01e1\u01e0\u0001\u0000\u0000\u0000\u01e2?\u0001\u0000"+
		"\u0000\u0000\u01e3\u01ea\u0005\u001b\u0000\u0000\u01e4\u01e5\u0005#\u0000"+
		"\u0000\u01e5\u01e6\u0005\u001e\u0000\u0000\u01e6\u01e7\u0005&\u0000\u0000"+
		"\u01e7\u01e8\u0003$\u0012\u0000\u01e8\u01e9\u0005$\u0000\u0000\u01e9\u01eb"+
		"\u0001\u0000\u0000\u0000\u01ea\u01e4\u0001\u0000\u0000\u0000\u01ea\u01eb"+
		"\u0001\u0000\u0000\u0000\u01ebA\u0001\u0000\u0000\u0000\u01ec\u01f1\u0005"+
		"6\u0000\u0000\u01ed\u01ee\u0005)\u0000\u0000\u01ee\u01f0\u00056\u0000"+
		"\u0000\u01ef\u01ed\u0001\u0000\u0000\u0000\u01f0\u01f3\u0001\u0000\u0000"+
		"\u0000\u01f1\u01ef\u0001\u0000\u0000\u0000\u01f1\u01f2\u0001\u0000\u0000"+
		"\u0000\u01f2\u01f4\u0001\u0000\u0000\u0000\u01f3\u01f1\u0001\u0000\u0000"+
		"\u0000\u01f4\u01f6\u0005#\u0000\u0000\u01f5\u01f7\u0003D\"\u0000\u01f6"+
		"\u01f5\u0001\u0000\u0000\u0000\u01f6\u01f7\u0001\u0000\u0000\u0000\u01f7"+
		"\u01f8\u0001\u0000\u0000\u0000\u01f8\u01f9\u0005$\u0000\u0000\u01f9C\u0001"+
		"\u0000\u0000\u0000\u01fa\u01ff\u0003F#\u0000\u01fb\u01fc\u0005%\u0000"+
		"\u0000\u01fc\u01fe\u0003F#\u0000\u01fd\u01fb\u0001\u0000\u0000\u0000\u01fe"+
		"\u0201\u0001\u0000\u0000\u0000\u01ff\u01fd\u0001\u0000\u0000\u0000\u01ff"+
		"\u0200\u0001\u0000\u0000\u0000\u0200\u020b\u0001\u0000\u0000\u0000\u0201"+
		"\u01ff\u0001\u0000\u0000\u0000\u0202\u0207\u0003$\u0012\u0000\u0203\u0204"+
		"\u0005%\u0000\u0000\u0204\u0206\u0003$\u0012\u0000\u0205\u0203\u0001\u0000"+
		"\u0000\u0000\u0206\u0209\u0001\u0000\u0000\u0000\u0207\u0205\u0001\u0000"+
		"\u0000\u0000\u0207\u0208\u0001\u0000\u0000\u0000\u0208\u020b\u0001\u0000"+
		"\u0000\u0000\u0209\u0207\u0001\u0000\u0000\u0000\u020a\u01fa\u0001\u0000"+
		"\u0000\u0000\u020a\u0202\u0001\u0000\u0000\u0000\u020bE\u0001\u0000\u0000"+
		"\u0000\u020c\u020d\u00056\u0000\u0000\u020d\u020e\u0005&\u0000\u0000\u020e"+
		"\u020f\u0003$\u0012\u0000\u020fG\u0001\u0000\u0000\u0000\u0210\u0211\u0005"+
		"\u0017\u0000\u0000\u0211\u0215\u0005#\u0000\u0000\u0212\u0213\u00056\u0000"+
		"\u0000\u0213\u0214\u0005(\u0000\u0000\u0214\u0216\u0003$\u0012\u0000\u0215"+
		"\u0212\u0001\u0000\u0000\u0000\u0215\u0216\u0001\u0000\u0000\u0000\u0216"+
		"\u0217\u0001\u0000\u0000\u0000\u0217\u0220\u0005$\u0000\u0000\u0218\u0219"+
		"\u0005\u0018\u0000\u0000\u0219\u021a\u0005#\u0000\u0000\u021a\u021b\u0005"+
		"6\u0000\u0000\u021b\u021c\u0005(\u0000\u0000\u021c\u021d\u0003$\u0012"+
		"\u0000\u021d\u021e\u0005$\u0000\u0000\u021e\u0220\u0001\u0000\u0000\u0000"+
		"\u021f\u0210\u0001\u0000\u0000\u0000\u021f\u0218\u0001\u0000\u0000\u0000"+
		"\u0220I\u0001\u0000\u0000\u0000\u0221\u022f\u00053\u0000\u0000\u0222\u022f"+
		"\u0005,\u0000\u0000\u0223\u022f\u0007\u0006\u0000\u0000\u0224\u022f\u0005"+
		"4\u0000\u0000\u0225\u022f\u00055\u0000\u0000\u0226\u022f\u0007\u0007\u0000"+
		"\u0000\u0227\u022f\u0005\u0016\u0000\u0000\u0228\u022f\u0005/\u0000\u0000"+
		"\u0229\u022f\u00050\u0000\u0000\u022a\u022f\u00051\u0000\u0000\u022b\u022f"+
		"\u00052\u0000\u0000\u022c\u022f\u0005-\u0000\u0000\u022d\u022f\u0005."+
		"\u0000\u0000\u022e\u0221\u0001\u0000\u0000\u0000\u022e\u0222\u0001\u0000"+
		"\u0000\u0000\u022e\u0223\u0001\u0000\u0000\u0000\u022e\u0224\u0001\u0000"+
		"\u0000\u0000\u022e\u0225\u0001\u0000\u0000\u0000\u022e\u0226\u0001\u0000"+
		"\u0000\u0000\u022e\u0227\u0001\u0000\u0000\u0000\u022e\u0228\u0001\u0000"+
		"\u0000\u0000\u022e\u0229\u0001\u0000\u0000\u0000\u022e\u022a\u0001\u0000"+
		"\u0000\u0000\u022e\u022b\u0001\u0000\u0000\u0000\u022e\u022c\u0001\u0000"+
		"\u0000\u0000\u022e\u022d\u0001\u0000\u0000\u0000\u022fK\u0001\u0000\u0000"+
		"\u00005T[_eqt\u0080\u0084\u008a\u0091\u009b\u00a7\u00ad\u00b9\u00c6\u00d3"+
		"\u00ed\u00f9\u0103\u0109\u0111\u011b\u0124\u012b\u0132\u013b\u0147\u014c"+
		"\u0159\u0161\u0167\u0175\u0185\u018f\u019a\u01a8\u01af\u01b9\u01c3\u01c6"+
		"\u01cf\u01d7\u01dc\u01e1\u01ea\u01f1\u01f6\u01ff\u0207\u020a\u0215\u021f"+
		"\u022e";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}