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
		FALSE=21, NULL=22, ANY=23, ALL=24, CAST=25, ISOF=26, CROSSJOIN=27, ALLRES=28, 
		ENTITYRES=29, ITREF=30, THISREF=31, THESEREF=32, ROOTREF=33, COUNT=34, 
		VALUE=35, REF=36, FILTERQ=37, WITH=38, AS=39, FROM=40, MINUS=41, LBRACE=42, 
		RBRACE=43, LBRACKET=44, RBRACKET=45, LPAREN=46, RPAREN=47, COMMA=48, EQUALS=49, 
		SLASH=50, COLON=51, DOT=52, DQSTRING=53, NANLIT=54, INF=55, BINARY=56, 
		DURATION=57, ENUM=58, GUID=59, DATETIMEOFFSET=60, DATE=61, TIMEOFDAY=62, 
		STRING=63, DECIMAL=64, INT=65, IDENT=66, ANNOTATION=67, ALIAS=68, WS=69;
	public static final int
		RULE_filter = 0, RULE_orderby = 1, RULE_orderbyItem = 2, RULE_resource = 3, 
		RULE_keyPredicate = 4, RULE_namedKeyValue = 5, RULE_keyLiteral = 6, RULE_resourceSegment = 7, 
		RULE_castName = 8, RULE_apply = 9, RULE_applySeq = 10, RULE_applyTrafo = 11, 
		RULE_searchExpr = 12, RULE_searchAtom = 13, RULE_groupbyElement = 14, 
		RULE_aggregateItem = 15, RULE_methodName = 16, RULE_aggrFrom = 17, RULE_customFrom = 18, 
		RULE_computeItem = 19, RULE_expr = 20, RULE_orExpr = 21, RULE_andExpr = 22, 
		RULE_notExpr = 23, RULE_comparison = 24, RULE_additive = 25, RULE_multiplicative = 26, 
		RULE_primary = 27, RULE_rootedPath = 28, RULE_aggregateCall = 29, RULE_aggregateFunctionItem = 30, 
		RULE_jsonArray = 31, RULE_jsonObject = 32, RULE_jsonMember = 33, RULE_typeFunc = 34, 
		RULE_qualifiedTypeName = 35, RULE_functionCall = 36, RULE_memberPath = 37, 
		RULE_pathStep = 38, RULE_lastSegment = 39, RULE_filterSegment = 40, RULE_countCall = 41, 
		RULE_boundCall = 42, RULE_boundCallArgs = 43, RULE_namedArg = 44, RULE_lambdaCall = 45, 
		RULE_literal = 46;
	private static String[] makeRuleNames() {
		return new String[] {
			"filter", "orderby", "orderbyItem", "resource", "keyPredicate", "namedKeyValue", 
			"keyLiteral", "resourceSegment", "castName", "apply", "applySeq", "applyTrafo", 
			"searchExpr", "searchAtom", "groupbyElement", "aggregateItem", "methodName", 
			"aggrFrom", "customFrom", "computeItem", "expr", "orExpr", "andExpr", 
			"notExpr", "comparison", "additive", "multiplicative", "primary", "rootedPath", 
			"aggregateCall", "aggregateFunctionItem", "jsonArray", "jsonObject", 
			"jsonMember", "typeFunc", "qualifiedTypeName", "functionCall", "memberPath", 
			"pathStep", "lastSegment", "filterSegment", "countCall", "boundCall", 
			"boundCallArgs", "namedArg", "lambdaCall", "literal"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, "'true'", "'false'", 
			"'null'", "'any'", "'all'", null, null, "'$crossjoin'", "'$all'", "'$entity'", 
			"'$it'", "'$this'", "'$these'", "'$root'", "'$count'", "'$value'", "'$ref'", 
			"'$filter'", "'with'", "'as'", "'from'", "'-'", "'{'", "'}'", "'['", 
			"']'", "'('", "')'", "','", "'='", "'/'", "':'", "'.'", null, "'NaN'", 
			"'INF'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "OR", "AND", "NOT", "EQ", "NE", "GT", "GE", "LT", "LE", "HAS", 
			"IN", "ADD", "SUB", "MUL", "DIVBY", "DIV", "MOD", "ASC", "DESC", "TRUE", 
			"FALSE", "NULL", "ANY", "ALL", "CAST", "ISOF", "CROSSJOIN", "ALLRES", 
			"ENTITYRES", "ITREF", "THISREF", "THESEREF", "ROOTREF", "COUNT", "VALUE", 
			"REF", "FILTERQ", "WITH", "AS", "FROM", "MINUS", "LBRACE", "RBRACE", 
			"LBRACKET", "RBRACKET", "LPAREN", "RPAREN", "COMMA", "EQUALS", "SLASH", 
			"COLON", "DOT", "DQSTRING", "NANLIT", "INF", "BINARY", "DURATION", "ENUM", 
			"GUID", "DATETIMEOFFSET", "DATE", "TIMEOFDAY", "STRING", "DECIMAL", "INT", 
			"IDENT", "ANNOTATION", "ALIAS", "WS"
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

		private boolean customTrafo() {
			return _input.LT(2) != null && ".".equals(_input.LT(2).getText());
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
			setState(94);
			expr();
			setState(95);
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
			setState(97);
			orderbyItem();
			setState(102);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(98);
				match(COMMA);
				setState(99);
				orderbyItem();
				}
				}
				setState(104);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(105);
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
			setState(107);
			expr();
			setState(109);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(108);
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
		public ResourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resource; }
	 
		public ResourceContext() { }
		public void copyFrom(ResourceContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EntitySetResourceContext extends ResourceContext {
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
		public EntitySetResourceContext(ResourceContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitEntitySetResource(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CrossjoinResourceContext extends ResourceContext {
		public TerminalNode CROSSJOIN() { return getToken(ODataFilterParser.CROSSJOIN, 0); }
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode EOF() { return getToken(ODataFilterParser.EOF, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public CrossjoinResourceContext(ResourceContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitCrossjoinResource(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EntityResourceContext extends ResourceContext {
		public TerminalNode ENTITYRES() { return getToken(ODataFilterParser.ENTITYRES, 0); }
		public TerminalNode EOF() { return getToken(ODataFilterParser.EOF, 0); }
		public TerminalNode SLASH() { return getToken(ODataFilterParser.SLASH, 0); }
		public CastNameContext castName() {
			return getRuleContext(CastNameContext.class,0);
		}
		public EntityResourceContext(ResourceContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitEntityResource(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AllResourceContext extends ResourceContext {
		public TerminalNode ALLRES() { return getToken(ODataFilterParser.ALLRES, 0); }
		public TerminalNode EOF() { return getToken(ODataFilterParser.EOF, 0); }
		public TerminalNode SLASH() { return getToken(ODataFilterParser.SLASH, 0); }
		public CastNameContext castName() {
			return getRuleContext(CastNameContext.class,0);
		}
		public AllResourceContext(ResourceContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAllResource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ResourceContext resource() throws RecognitionException {
		ResourceContext _localctx = new ResourceContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_resource);
		int _la;
		try {
			setState(147);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENT:
				_localctx = new EntitySetResourceContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(111);
				match(IDENT);
				setState(113);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(112);
					keyPredicate();
					}
				}

				setState(119);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==SLASH) {
					{
					{
					setState(115);
					match(SLASH);
					setState(116);
					resourceSegment();
					}
					}
					setState(121);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(122);
				match(EOF);
				}
				break;
			case CROSSJOIN:
				_localctx = new CrossjoinResourceContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(123);
				match(CROSSJOIN);
				setState(124);
				match(LPAREN);
				setState(125);
				match(IDENT);
				setState(130);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(126);
					match(COMMA);
					setState(127);
					match(IDENT);
					}
					}
					setState(132);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(133);
				match(RPAREN);
				setState(134);
				match(EOF);
				}
				break;
			case ALLRES:
				_localctx = new AllResourceContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(135);
				match(ALLRES);
				setState(138);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SLASH) {
					{
					setState(136);
					match(SLASH);
					setState(137);
					castName();
					}
				}

				setState(140);
				match(EOF);
				}
				break;
			case ENTITYRES:
				_localctx = new EntityResourceContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(141);
				match(ENTITYRES);
				setState(144);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==SLASH) {
					{
					setState(142);
					match(SLASH);
					setState(143);
					castName();
					}
				}

				setState(146);
				match(EOF);
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
			setState(149);
			match(LPAREN);
			setState(159);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case GUID:
			case DATETIMEOFFSET:
			case DATE:
			case TIMEOFDAY:
			case STRING:
			case DECIMAL:
			case INT:
			case ALIAS:
				{
				setState(150);
				keyLiteral();
				}
				break;
			case IDENT:
				{
				setState(151);
				namedKeyValue();
				setState(156);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(152);
					match(COMMA);
					setState(153);
					namedKeyValue();
					}
					}
					setState(158);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(161);
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
			setState(163);
			match(IDENT);
			setState(164);
			match(EQUALS);
			setState(165);
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
		public TerminalNode ALIAS() { return getToken(ODataFilterParser.ALIAS, 0); }
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
			setState(167);
			_la = _input.LA(1);
			if ( !(((((_la - 59)) & ~0x3f) == 0 && ((1L << (_la - 59)) & 639L) != 0)) ) {
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
			setState(181);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				_localctx = new CastSegmentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(169);
				castName();
				setState(171);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(170);
					keyPredicate();
					}
				}

				}
				break;
			case 2:
				_localctx = new PropertySegmentContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(173);
				match(IDENT);
				setState(175);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(174);
					keyPredicate();
					}
				}

				}
				break;
			case 3:
				_localctx = new CountSegmentContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(177);
				match(COUNT);
				}
				break;
			case 4:
				_localctx = new ValueSegmentContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(178);
				match(VALUE);
				}
				break;
			case 5:
				_localctx = new RefSegmentContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(179);
				match(REF);
				}
				break;
			case 6:
				_localctx = new KeyValueSegmentContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(180);
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
			setState(183);
			match(IDENT);
			setState(186); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(184);
					match(DOT);
					setState(185);
					match(IDENT);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(188); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
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
			setState(190);
			applySeq();
			setState(191);
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
			setState(193);
			applyTrafo();
			setState(198);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SLASH) {
				{
				{
				setState(194);
				match(SLASH);
				setState(195);
				applyTrafo();
				}
				}
				setState(200);
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
	public static class JoinTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public MemberPathContext memberPath() {
			return getRuleContext(MemberPathContext.class,0);
		}
		public TerminalNode AS() { return getToken(ODataFilterParser.AS, 0); }
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode COMMA() { return getToken(ODataFilterParser.COMMA, 0); }
		public ApplySeqContext applySeq() {
			return getRuleContext(ApplySeqContext.class,0);
		}
		public JoinTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitJoinTrafo(this);
			else return visitor.visitChildren(this);
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
	public static class TraverseTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public RootedPathContext rootedPath() {
			return getRuleContext(RootedPathContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public MemberPathContext memberPath() {
			return getRuleContext(MemberPathContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public ApplySeqContext applySeq() {
			return getRuleContext(ApplySeqContext.class,0);
		}
		public List<OrderbyItemContext> orderbyItem() {
			return getRuleContexts(OrderbyItemContext.class);
		}
		public OrderbyItemContext orderbyItem(int i) {
			return getRuleContext(OrderbyItemContext.class,i);
		}
		public TraverseTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitTraverseTrafo(this);
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
	public static class CustomFunctionTrafoContext extends ApplyTrafoContext {
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
		public CustomFunctionTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitCustomFunctionTrafo(this);
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
	public static class SearchTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public SearchExprContext searchExpr() {
			return getRuleContext(SearchExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public SearchTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitSearchTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NestTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public List<ApplySeqContext> applySeq() {
			return getRuleContexts(ApplySeqContext.class);
		}
		public ApplySeqContext applySeq(int i) {
			return getRuleContext(ApplySeqContext.class,i);
		}
		public List<TerminalNode> AS() { return getTokens(ODataFilterParser.AS); }
		public TerminalNode AS(int i) {
			return getToken(ODataFilterParser.AS, i);
		}
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public NestTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitNestTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HierarchyTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public RootedPathContext rootedPath() {
			return getRuleContext(RootedPathContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public MemberPathContext memberPath() {
			return getRuleContext(MemberPathContext.class,0);
		}
		public ApplySeqContext applySeq() {
			return getRuleContext(ApplySeqContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode INT() { return getToken(ODataFilterParser.INT, 0); }
		public HierarchyTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitHierarchyTrafo(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AddNestedTrafoContext extends ApplyTrafoContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public MemberPathContext memberPath() {
			return getRuleContext(MemberPathContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public List<ApplySeqContext> applySeq() {
			return getRuleContexts(ApplySeqContext.class);
		}
		public ApplySeqContext applySeq(int i) {
			return getRuleContext(ApplySeqContext.class,i);
		}
		public List<TerminalNode> AS() { return getTokens(ODataFilterParser.AS); }
		public TerminalNode AS(int i) {
			return getToken(ODataFilterParser.AS, i);
		}
		public AddNestedTrafoContext(ApplyTrafoContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAddNestedTrafo(this);
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
			setState(404);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				_localctx = new GroupByTrafoContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(201);
				if (!(trafo("groupby"))) throw new FailedPredicateException(this, "trafo(\"groupby\")");
				setState(202);
				((GroupByTrafoContext)_localctx).name = match(IDENT);
				setState(203);
				match(LPAREN);
				setState(204);
				match(LPAREN);
				setState(205);
				groupbyElement();
				setState(210);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(206);
					match(COMMA);
					setState(207);
					groupbyElement();
					}
					}
					setState(212);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(213);
				match(RPAREN);
				setState(216);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(214);
					match(COMMA);
					setState(215);
					applySeq();
					}
				}

				setState(218);
				match(RPAREN);
				}
				break;
			case 2:
				_localctx = new AggregateTrafoContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(220);
				if (!(trafo("aggregate"))) throw new FailedPredicateException(this, "trafo(\"aggregate\")");
				setState(221);
				((AggregateTrafoContext)_localctx).name = match(IDENT);
				setState(222);
				match(LPAREN);
				setState(223);
				aggregateItem();
				setState(228);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(224);
					match(COMMA);
					setState(225);
					aggregateItem();
					}
					}
					setState(230);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(231);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new ComputeTrafoContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(233);
				if (!(trafo("compute"))) throw new FailedPredicateException(this, "trafo(\"compute\")");
				setState(234);
				((ComputeTrafoContext)_localctx).name = match(IDENT);
				setState(235);
				match(LPAREN);
				setState(236);
				computeItem();
				setState(241);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(237);
					match(COMMA);
					setState(238);
					computeItem();
					}
					}
					setState(243);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(244);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new ConcatTrafoContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(246);
				if (!(trafo("concat"))) throw new FailedPredicateException(this, "trafo(\"concat\")");
				setState(247);
				((ConcatTrafoContext)_localctx).name = match(IDENT);
				setState(248);
				match(LPAREN);
				setState(249);
				applySeq();
				setState(252); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(250);
					match(COMMA);
					setState(251);
					applySeq();
					}
					}
					setState(254); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(256);
				match(RPAREN);
				}
				break;
			case 5:
				_localctx = new FilterTrafoContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(258);
				if (!(trafo("filter"))) throw new FailedPredicateException(this, "trafo(\"filter\")");
				setState(259);
				((FilterTrafoContext)_localctx).name = match(IDENT);
				setState(260);
				match(LPAREN);
				setState(261);
				expr();
				setState(262);
				match(RPAREN);
				}
				break;
			case 6:
				_localctx = new BottomTopTrafoContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(264);
				if (!(bottomTopTrafo())) throw new FailedPredicateException(this, "bottomTopTrafo()");
				setState(265);
				((BottomTopTrafoContext)_localctx).name = match(IDENT);
				setState(266);
				match(LPAREN);
				setState(267);
				expr();
				setState(268);
				match(COMMA);
				setState(269);
				expr();
				setState(270);
				match(RPAREN);
				}
				break;
			case 7:
				_localctx = new OrderByTrafoContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(272);
				if (!(trafo("orderby"))) throw new FailedPredicateException(this, "trafo(\"orderby\")");
				setState(273);
				((OrderByTrafoContext)_localctx).name = match(IDENT);
				setState(274);
				match(LPAREN);
				setState(275);
				orderbyItem();
				setState(280);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(276);
					match(COMMA);
					setState(277);
					orderbyItem();
					}
					}
					setState(282);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(283);
				match(RPAREN);
				}
				break;
			case 8:
				_localctx = new RowLimitTrafoContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(285);
				if (!(trafo("top") || trafo("skip"))) throw new FailedPredicateException(this, "trafo(\"top\") || trafo(\"skip\")");
				setState(286);
				((RowLimitTrafoContext)_localctx).name = match(IDENT);
				setState(287);
				match(LPAREN);
				setState(288);
				match(INT);
				setState(289);
				match(RPAREN);
				}
				break;
			case 9:
				_localctx = new IdentityTrafoContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(290);
				if (!(trafo("identity"))) throw new FailedPredicateException(this, "trafo(\"identity\")");
				setState(291);
				((IdentityTrafoContext)_localctx).name = match(IDENT);
				}
				break;
			case 10:
				_localctx = new SearchTrafoContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(292);
				if (!(trafo("search"))) throw new FailedPredicateException(this, "trafo(\"search\")");
				setState(293);
				((SearchTrafoContext)_localctx).name = match(IDENT);
				setState(294);
				match(LPAREN);
				setState(295);
				searchExpr();
				setState(296);
				match(RPAREN);
				}
				break;
			case 11:
				_localctx = new NestTrafoContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(298);
				if (!(trafo("nest"))) throw new FailedPredicateException(this, "trafo(\"nest\")");
				setState(299);
				((NestTrafoContext)_localctx).name = match(IDENT);
				setState(300);
				match(LPAREN);
				setState(301);
				applySeq();
				setState(302);
				match(AS);
				setState(303);
				match(IDENT);
				setState(311);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(304);
					match(COMMA);
					setState(305);
					applySeq();
					setState(306);
					match(AS);
					setState(307);
					match(IDENT);
					}
					}
					setState(313);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(314);
				match(RPAREN);
				}
				break;
			case 12:
				_localctx = new AddNestedTrafoContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(316);
				if (!(trafo("addnested"))) throw new FailedPredicateException(this, "trafo(\"addnested\")");
				setState(317);
				((AddNestedTrafoContext)_localctx).name = match(IDENT);
				setState(318);
				match(LPAREN);
				setState(319);
				memberPath();
				setState(325); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(320);
					match(COMMA);
					setState(321);
					applySeq();
					setState(322);
					match(AS);
					setState(323);
					match(IDENT);
					}
					}
					setState(327); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(329);
				match(RPAREN);
				}
				break;
			case 13:
				_localctx = new JoinTrafoContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(331);
				if (!(trafo("join") || trafo("outerjoin"))) throw new FailedPredicateException(this, "trafo(\"join\") || trafo(\"outerjoin\")");
				setState(332);
				((JoinTrafoContext)_localctx).name = match(IDENT);
				setState(333);
				match(LPAREN);
				setState(334);
				memberPath();
				setState(335);
				match(AS);
				setState(336);
				match(IDENT);
				setState(339);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(337);
					match(COMMA);
					setState(338);
					applySeq();
					}
				}

				setState(341);
				match(RPAREN);
				}
				break;
			case 14:
				_localctx = new HierarchyTrafoContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(343);
				if (!(trafo("ancestors") || trafo("descendants"))) throw new FailedPredicateException(this, "trafo(\"ancestors\") || trafo(\"descendants\")");
				setState(344);
				((HierarchyTrafoContext)_localctx).name = match(IDENT);
				setState(345);
				match(LPAREN);
				setState(346);
				rootedPath();
				setState(347);
				match(COMMA);
				setState(348);
				match(IDENT);
				setState(349);
				match(COMMA);
				setState(350);
				memberPath();
				setState(351);
				match(COMMA);
				setState(352);
				applySeq();
				setState(355);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
				case 1:
					{
					setState(353);
					match(COMMA);
					setState(354);
					match(INT);
					}
					break;
				}
				setState(360);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(357);
					match(COMMA);
					setState(358);
					match(IDENT);
					setState(359);
					match(IDENT);
					}
				}

				setState(362);
				match(RPAREN);
				}
				break;
			case 15:
				_localctx = new TraverseTrafoContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(364);
				if (!(trafo("traverse"))) throw new FailedPredicateException(this, "trafo(\"traverse\")");
				setState(365);
				((TraverseTrafoContext)_localctx).name = match(IDENT);
				setState(366);
				match(LPAREN);
				setState(367);
				rootedPath();
				setState(368);
				match(COMMA);
				setState(369);
				match(IDENT);
				setState(370);
				match(COMMA);
				setState(371);
				memberPath();
				setState(372);
				match(COMMA);
				setState(373);
				match(IDENT);
				setState(376);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
				case 1:
					{
					setState(374);
					match(COMMA);
					setState(375);
					applySeq();
					}
					break;
				}
				setState(387);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(378);
					match(COMMA);
					setState(379);
					orderbyItem();
					setState(384);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(380);
						match(COMMA);
						setState(381);
						orderbyItem();
						}
						}
						setState(386);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(389);
				match(RPAREN);
				}
				break;
			case 16:
				_localctx = new CustomFunctionTrafoContext(_localctx);
				enterOuterAlt(_localctx, 16);
				{
				setState(391);
				if (!(customTrafo())) throw new FailedPredicateException(this, "customTrafo()");
				setState(392);
				match(IDENT);
				setState(395); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(393);
					match(DOT);
					setState(394);
					match(IDENT);
					}
					}
					setState(397); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==DOT );
				setState(399);
				match(LPAREN);
				setState(401);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -8912487601668088L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 31L) != 0)) {
					{
					setState(400);
					boundCallArgs();
					}
				}

				setState(403);
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
	public static class SearchExprContext extends ParserRuleContext {
		public List<SearchAtomContext> searchAtom() {
			return getRuleContexts(SearchAtomContext.class);
		}
		public SearchAtomContext searchAtom(int i) {
			return getRuleContext(SearchAtomContext.class,i);
		}
		public SearchExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searchExpr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitSearchExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SearchExprContext searchExpr() throws RecognitionException {
		SearchExprContext _localctx = new SearchExprContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_searchExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(407); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(406);
				searchAtom();
				}
				}
				setState(409); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((((_la - 3)) & ~0x3f) == 0 && ((1L << (_la - 3)) & -1151786808606982143L) != 0) );
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
	public static class SearchAtomContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public TerminalNode STRING() { return getToken(ODataFilterParser.STRING, 0); }
		public TerminalNode DQSTRING() { return getToken(ODataFilterParser.DQSTRING, 0); }
		public TerminalNode INT() { return getToken(ODataFilterParser.INT, 0); }
		public TerminalNode DECIMAL() { return getToken(ODataFilterParser.DECIMAL, 0); }
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public SearchExprContext searchExpr() {
			return getRuleContext(SearchExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public TerminalNode NOT() { return getToken(ODataFilterParser.NOT, 0); }
		public SearchAtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_searchAtom; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitSearchAtom(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SearchAtomContext searchAtom() throws RecognitionException {
		SearchAtomContext _localctx = new SearchAtomContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_searchAtom);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(412);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(411);
				match(NOT);
				}
			}

			setState(423);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENT:
				{
				setState(414);
				match(IDENT);
				}
				break;
			case STRING:
				{
				setState(415);
				match(STRING);
				}
				break;
			case DQSTRING:
				{
				setState(416);
				match(DQSTRING);
				}
				break;
			case INT:
				{
				setState(417);
				match(INT);
				}
				break;
			case DECIMAL:
				{
				setState(418);
				match(DECIMAL);
				}
				break;
			case LPAREN:
				{
				setState(419);
				match(LPAREN);
				setState(420);
				searchExpr();
				setState(421);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
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
	public static class RollupRecursiveElementContext extends GroupbyElementContext {
		public Token name;
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public RootedPathContext rootedPath() {
			return getRuleContext(RootedPathContext.class,0);
		}
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public MemberPathContext memberPath() {
			return getRuleContext(MemberPathContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public ApplySeqContext applySeq() {
			return getRuleContext(ApplySeqContext.class,0);
		}
		public RollupRecursiveElementContext(GroupbyElementContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitRollupRecursiveElement(this);
			else return visitor.visitChildren(this);
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
		enterRule(_localctx, 28, RULE_groupbyElement);
		int _la;
		try {
			setState(453);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
			case 1:
				_localctx = new RollupElementContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(425);
				if (!(trafo("rollup"))) throw new FailedPredicateException(this, "trafo(\"rollup\")");
				setState(426);
				((RollupElementContext)_localctx).name = match(IDENT);
				setState(427);
				match(LPAREN);
				setState(428);
				memberPath();
				setState(433);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(429);
					match(COMMA);
					setState(430);
					memberPath();
					}
					}
					setState(435);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(436);
				match(RPAREN);
				}
				break;
			case 2:
				_localctx = new RollupRecursiveElementContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(438);
				if (!(trafo("rolluprecursive"))) throw new FailedPredicateException(this, "trafo(\"rolluprecursive\")");
				setState(439);
				((RollupRecursiveElementContext)_localctx).name = match(IDENT);
				setState(440);
				match(LPAREN);
				setState(441);
				rootedPath();
				setState(442);
				match(COMMA);
				setState(443);
				match(IDENT);
				setState(444);
				match(COMMA);
				setState(445);
				memberPath();
				setState(448);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(446);
					match(COMMA);
					setState(447);
					applySeq();
					}
				}

				setState(450);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new PathElementContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(452);
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
		enterRule(_localctx, 30, RULE_aggregateItem);
		int _la;
		try {
			setState(487);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				_localctx = new AggregateWithItemContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(455);
				expr();
				setState(456);
				match(WITH);
				setState(457);
				((AggregateWithItemContext)_localctx).method = methodName();
				setState(461);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(458);
					aggrFrom();
					}
					}
					setState(463);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(464);
				match(AS);
				setState(465);
				((AggregateWithItemContext)_localctx).alias = match(IDENT);
				}
				break;
			case 2:
				_localctx = new AggregateCountItemContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(467);
				match(COUNT);
				setState(471);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(468);
					aggrFrom();
					}
					}
					setState(473);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(474);
				match(AS);
				setState(475);
				((AggregateCountItemContext)_localctx).alias = match(IDENT);
				}
				break;
			case 3:
				_localctx = new AggregateCustomAliasedContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(476);
				expr();
				setState(480);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(477);
					customFrom();
					}
					}
					setState(482);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(483);
				match(AS);
				setState(484);
				((AggregateCustomAliasedContext)_localctx).alias = match(IDENT);
				}
				break;
			case 4:
				_localctx = new AggregateCustomBareContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(486);
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
		enterRule(_localctx, 32, RULE_methodName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(489);
			match(IDENT);
			setState(494);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(490);
				match(DOT);
				setState(491);
				match(IDENT);
				}
				}
				setState(496);
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
		enterRule(_localctx, 34, RULE_aggrFrom);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(497);
			match(FROM);
			setState(498);
			memberPath();
			setState(503);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(499);
				match(COMMA);
				setState(500);
				memberPath();
				}
				}
				setState(505);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(506);
			match(WITH);
			setState(507);
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
		enterRule(_localctx, 36, RULE_customFrom);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(509);
			match(FROM);
			setState(510);
			memberPath();
			setState(515);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(511);
				match(COMMA);
				setState(512);
				memberPath();
				}
				}
				setState(517);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(520);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==WITH) {
				{
				setState(518);
				match(WITH);
				setState(519);
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
		enterRule(_localctx, 38, RULE_computeItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(522);
			expr();
			setState(523);
			match(AS);
			setState(524);
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
		enterRule(_localctx, 40, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(526);
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
		enterRule(_localctx, 42, RULE_orExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(528);
			andExpr();
			setState(533);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(529);
				match(OR);
				setState(530);
				andExpr();
				}
				}
				setState(535);
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
		enterRule(_localctx, 44, RULE_andExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(536);
			notExpr();
			setState(541);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(537);
				match(AND);
				setState(538);
				notExpr();
				}
				}
				setState(543);
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
		enterRule(_localctx, 46, RULE_notExpr);
		try {
			setState(547);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				_localctx = new NotExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(544);
				match(NOT);
				setState(545);
				notExpr();
				}
				break;
			case TRUE:
			case FALSE:
			case NULL:
			case CAST:
			case ISOF:
			case ITREF:
			case THISREF:
			case THESEREF:
			case ROOTREF:
			case FILTERQ:
			case MINUS:
			case LBRACE:
			case LBRACKET:
			case LPAREN:
			case DQSTRING:
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
			case ANNOTATION:
			case ALIAS:
				_localctx = new ComparisonLevelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(546);
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
	public static class InArrayComparisonContext extends ComparisonContext {
		public AdditiveContext additive() {
			return getRuleContext(AdditiveContext.class,0);
		}
		public TerminalNode IN() { return getToken(ODataFilterParser.IN, 0); }
		public JsonArrayContext jsonArray() {
			return getRuleContext(JsonArrayContext.class,0);
		}
		public InArrayComparisonContext(ComparisonContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitInArrayComparison(this);
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
		enterRule(_localctx, 48, RULE_comparison);
		int _la;
		try {
			setState(581);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,50,_ctx) ) {
			case 1:
				_localctx = new BinaryComparisonContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(549);
				additive(0);
				setState(550);
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
				setState(551);
				additive(0);
				}
				break;
			case 2:
				_localctx = new InListComparisonContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(553);
				additive(0);
				setState(554);
				match(IN);
				setState(555);
				match(LPAREN);
				setState(556);
				literal();
				setState(559); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(557);
					match(COMMA);
					setState(558);
					literal();
					}
					}
					setState(561); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(563);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new InEmptyListComparisonContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(565);
				additive(0);
				setState(566);
				match(IN);
				setState(567);
				match(LPAREN);
				setState(568);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new InComparisonContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(570);
				additive(0);
				setState(571);
				match(IN);
				setState(572);
				match(LPAREN);
				setState(573);
				expr();
				setState(574);
				match(RPAREN);
				}
				break;
			case 5:
				_localctx = new InArrayComparisonContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(576);
				additive(0);
				setState(577);
				match(IN);
				setState(578);
				jsonArray();
				}
				break;
			case 6:
				_localctx = new PassThroughContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(580);
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
		int _startState = 50;
		enterRecursionRule(_localctx, 50, RULE_additive, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new ToMultiplicativeContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(584);
			multiplicative(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(591);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new AddSubContext(new AdditiveContext(_parentctx, _parentState));
					pushNewRecursionContext(_localctx, _startState, RULE_additive);
					setState(586);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(587);
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
					setState(588);
					multiplicative(0);
					}
					} 
				}
				setState(593);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,51,_ctx);
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
		int _startState = 52;
		enterRecursionRule(_localctx, 52, RULE_multiplicative, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new ToPrimaryContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(595);
			primary();
			}
			_ctx.stop = _input.LT(-1);
			setState(602);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,52,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new MulDivModContext(new MultiplicativeContext(_parentctx, _parentState));
					pushNewRecursionContext(_localctx, _startState, RULE_multiplicative);
					setState(597);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(598);
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
					setState(599);
					primary();
					}
					} 
				}
				setState(604);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,52,_ctx);
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
	public static class JsonObjectPrimaryContext extends PrimaryContext {
		public JsonObjectContext jsonObject() {
			return getRuleContext(JsonObjectContext.class,0);
		}
		public JsonObjectPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitJsonObjectPrimary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RootedPrimaryContext extends PrimaryContext {
		public RootedPathContext rootedPath() {
			return getRuleContext(RootedPathContext.class,0);
		}
		public RootedPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitRootedPrimary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class JsonArrayPrimaryContext extends PrimaryContext {
		public JsonArrayContext jsonArray() {
			return getRuleContext(JsonArrayContext.class,0);
		}
		public JsonArrayPrimaryContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitJsonArrayPrimary(this);
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
		enterRule(_localctx, 54, RULE_primary);
		try {
			setState(619);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				_localctx = new LiteralPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(605);
				literal();
				}
				break;
			case 2:
				_localctx = new TypeFuncPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(606);
				typeFunc();
				}
				break;
			case 3:
				_localctx = new FunctionPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(607);
				functionCall();
				}
				break;
			case 4:
				_localctx = new RootedPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(608);
				rootedPath();
				}
				break;
			case 5:
				_localctx = new AliasPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(609);
				match(ALIAS);
				}
				break;
			case 6:
				_localctx = new MemberPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(610);
				memberPath();
				}
				break;
			case 7:
				_localctx = new ParenPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(611);
				match(LPAREN);
				setState(612);
				expr();
				setState(613);
				match(RPAREN);
				}
				break;
			case 8:
				_localctx = new NegatedPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(615);
				match(MINUS);
				setState(616);
				primary();
				}
				break;
			case 9:
				_localctx = new JsonArrayPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(617);
				jsonArray();
				}
				break;
			case 10:
				_localctx = new JsonObjectPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(618);
				jsonObject();
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
	public static class RootedPathContext extends ParserRuleContext {
		public RootedPathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rootedPath; }
	 
		public RootedPathContext() { }
		public void copyFrom(RootedPathContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InstanceRefContext extends RootedPathContext {
		public Token anchor;
		public TerminalNode ITREF() { return getToken(ODataFilterParser.ITREF, 0); }
		public TerminalNode THISREF() { return getToken(ODataFilterParser.THISREF, 0); }
		public TerminalNode THESEREF() { return getToken(ODataFilterParser.THESEREF, 0); }
		public TerminalNode SLASH() { return getToken(ODataFilterParser.SLASH, 0); }
		public MemberPathContext memberPath() {
			return getRuleContext(MemberPathContext.class,0);
		}
		public CountCallContext countCall() {
			return getRuleContext(CountCallContext.class,0);
		}
		public AggregateCallContext aggregateCall() {
			return getRuleContext(AggregateCallContext.class,0);
		}
		public InstanceRefContext(RootedPathContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitInstanceRef(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RootRefContext extends RootedPathContext {
		public TerminalNode ROOTREF() { return getToken(ODataFilterParser.ROOTREF, 0); }
		public List<TerminalNode> SLASH() { return getTokens(ODataFilterParser.SLASH); }
		public TerminalNode SLASH(int i) {
			return getToken(ODataFilterParser.SLASH, i);
		}
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public KeyPredicateContext keyPredicate() {
			return getRuleContext(KeyPredicateContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public MemberPathContext memberPath() {
			return getRuleContext(MemberPathContext.class,0);
		}
		public CountCallContext countCall() {
			return getRuleContext(CountCallContext.class,0);
		}
		public RootRefContext(RootedPathContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitRootRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RootedPathContext rootedPath() throws RecognitionException {
		RootedPathContext _localctx = new RootedPathContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_rootedPath);
		int _la;
		try {
			setState(644);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ITREF:
			case THISREF:
			case THESEREF:
				_localctx = new InstanceRefContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(621);
				((InstanceRefContext)_localctx).anchor = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 7516192768L) != 0)) ) {
					((InstanceRefContext)_localctx).anchor = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(628);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
				case 1:
					{
					setState(622);
					match(SLASH);
					setState(623);
					memberPath();
					}
					break;
				case 2:
					{
					setState(624);
					match(SLASH);
					setState(625);
					countCall();
					}
					break;
				case 3:
					{
					setState(626);
					match(SLASH);
					setState(627);
					aggregateCall();
					}
					break;
				}
				}
				break;
			case ROOTREF:
				_localctx = new RootRefContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(630);
				match(ROOTREF);
				setState(631);
				match(SLASH);
				setState(632);
				match(IDENT);
				setState(636);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,55,_ctx) ) {
				case 1:
					{
					setState(633);
					keyPredicate();
					}
					break;
				case 2:
					{
					setState(634);
					match(LPAREN);
					setState(635);
					match(RPAREN);
					}
					break;
				}
				setState(642);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
				case 1:
					{
					setState(638);
					match(SLASH);
					setState(639);
					memberPath();
					}
					break;
				case 2:
					{
					setState(640);
					match(SLASH);
					setState(641);
					countCall();
					}
					break;
				}
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
	public static class AggregateCallContext extends ParserRuleContext {
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public AggregateFunctionItemContext aggregateFunctionItem() {
			return getRuleContext(AggregateFunctionItemContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public AggregateCallContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregateCall; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAggregateCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregateCallContext aggregateCall() throws RecognitionException {
		AggregateCallContext _localctx = new AggregateCallContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_aggregateCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(646);
			if (!(trafo("aggregate"))) throw new FailedPredicateException(this, "trafo(\"aggregate\")");
			setState(647);
			match(IDENT);
			setState(648);
			match(LPAREN);
			setState(649);
			aggregateFunctionItem();
			setState(650);
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
	public static class AggregateFunctionItemContext extends ParserRuleContext {
		public MethodNameContext method;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode WITH() { return getToken(ODataFilterParser.WITH, 0); }
		public MethodNameContext methodName() {
			return getRuleContext(MethodNameContext.class,0);
		}
		public List<AggrFromContext> aggrFrom() {
			return getRuleContexts(AggrFromContext.class);
		}
		public AggrFromContext aggrFrom(int i) {
			return getRuleContext(AggrFromContext.class,i);
		}
		public TerminalNode COUNT() { return getToken(ODataFilterParser.COUNT, 0); }
		public AggregateFunctionItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_aggregateFunctionItem; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAggregateFunctionItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregateFunctionItemContext aggregateFunctionItem() throws RecognitionException {
		AggregateFunctionItemContext _localctx = new AggregateFunctionItemContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_aggregateFunctionItem);
		int _la;
		try {
			setState(668);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
			case TRUE:
			case FALSE:
			case NULL:
			case CAST:
			case ISOF:
			case ITREF:
			case THISREF:
			case THESEREF:
			case ROOTREF:
			case FILTERQ:
			case MINUS:
			case LBRACE:
			case LBRACKET:
			case LPAREN:
			case DQSTRING:
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
			case ANNOTATION:
			case ALIAS:
				enterOuterAlt(_localctx, 1);
				{
				setState(652);
				expr();
				setState(653);
				match(WITH);
				setState(654);
				((AggregateFunctionItemContext)_localctx).method = methodName();
				setState(658);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(655);
					aggrFrom();
					}
					}
					setState(660);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case COUNT:
				enterOuterAlt(_localctx, 2);
				{
				setState(661);
				match(COUNT);
				setState(665);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==FROM) {
					{
					{
					setState(662);
					aggrFrom();
					}
					}
					setState(667);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
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
	public static class JsonArrayContext extends ParserRuleContext {
		public TerminalNode LBRACKET() { return getToken(ODataFilterParser.LBRACKET, 0); }
		public TerminalNode RBRACKET() { return getToken(ODataFilterParser.RBRACKET, 0); }
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
		public JsonArrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonArray; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitJsonArray(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JsonArrayContext jsonArray() throws RecognitionException {
		JsonArrayContext _localctx = new JsonArrayContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_jsonArray);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(670);
			match(LBRACKET);
			setState(679);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -8912487601668088L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 31L) != 0)) {
				{
				setState(671);
				expr();
				setState(676);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(672);
					match(COMMA);
					setState(673);
					expr();
					}
					}
					setState(678);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(681);
			match(RBRACKET);
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
	public static class JsonObjectContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(ODataFilterParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(ODataFilterParser.RBRACE, 0); }
		public List<JsonMemberContext> jsonMember() {
			return getRuleContexts(JsonMemberContext.class);
		}
		public JsonMemberContext jsonMember(int i) {
			return getRuleContext(JsonMemberContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ODataFilterParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ODataFilterParser.COMMA, i);
		}
		public JsonObjectContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonObject; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitJsonObject(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JsonObjectContext jsonObject() throws RecognitionException {
		JsonObjectContext _localctx = new JsonObjectContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_jsonObject);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(683);
			match(LBRACE);
			setState(692);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DQSTRING) {
				{
				setState(684);
				jsonMember();
				setState(689);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(685);
					match(COMMA);
					setState(686);
					jsonMember();
					}
					}
					setState(691);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(694);
			match(RBRACE);
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
	public static class JsonMemberContext extends ParserRuleContext {
		public TerminalNode DQSTRING() { return getToken(ODataFilterParser.DQSTRING, 0); }
		public TerminalNode COLON() { return getToken(ODataFilterParser.COLON, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public JsonMemberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonMember; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitJsonMember(this);
			else return visitor.visitChildren(this);
		}
	}

	public final JsonMemberContext jsonMember() throws RecognitionException {
		JsonMemberContext _localctx = new JsonMemberContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_jsonMember);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(696);
			match(DQSTRING);
			setState(697);
			match(COLON);
			setState(698);
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
		enterRule(_localctx, 68, RULE_typeFunc);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(700);
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
			setState(701);
			match(LPAREN);
			setState(705);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,65,_ctx) ) {
			case 1:
				{
				setState(702);
				expr();
				setState(703);
				match(COMMA);
				}
				break;
			}
			setState(707);
			qualifiedTypeName();
			setState(708);
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
		enterRule(_localctx, 70, RULE_qualifiedTypeName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(710);
			match(IDENT);
			setState(715);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(711);
				match(DOT);
				setState(712);
				match(IDENT);
				}
				}
				setState(717);
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
		enterRule(_localctx, 72, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(718);
			match(IDENT);
			setState(719);
			match(LPAREN);
			setState(728);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -8912487601668088L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 31L) != 0)) {
				{
				setState(720);
				expr();
				setState(725);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(721);
					match(COMMA);
					setState(722);
					expr();
					}
					}
					setState(727);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(730);
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
		enterRule(_localctx, 74, RULE_memberPath);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(737);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,69,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(732);
					pathStep();
					setState(733);
					match(SLASH);
					}
					} 
				}
				setState(739);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,69,_ctx);
			}
			setState(740);
			lastSegment();
			setState(745);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				{
				setState(741);
				match(SLASH);
				setState(742);
				lambdaCall();
				}
				break;
			case 2:
				{
				setState(743);
				match(SLASH);
				setState(744);
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
		public KeyPredicateContext keyPredicate() {
			return getRuleContext(KeyPredicateContext.class,0);
		}
		public BoundCallContext boundCall() {
			return getRuleContext(BoundCallContext.class,0);
		}
		public CastNameContext castName() {
			return getRuleContext(CastNameContext.class,0);
		}
		public TerminalNode ANNOTATION() { return getToken(ODataFilterParser.ANNOTATION, 0); }
		public TerminalNode ALIAS() { return getToken(ODataFilterParser.ALIAS, 0); }
		public FilterSegmentContext filterSegment() {
			return getRuleContext(FilterSegmentContext.class,0);
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
		enterRule(_localctx, 76, RULE_pathStep);
		int _la;
		try {
			setState(756);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,72,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(747);
				match(IDENT);
				setState(749);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(748);
					keyPredicate();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(751);
				boundCall();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(752);
				castName();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(753);
				match(ANNOTATION);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(754);
				match(ALIAS);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(755);
				filterSegment();
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
		public KeyPredicateContext keyPredicate() {
			return getRuleContext(KeyPredicateContext.class,0);
		}
		public BoundCallContext boundCall() {
			return getRuleContext(BoundCallContext.class,0);
		}
		public CastNameContext castName() {
			return getRuleContext(CastNameContext.class,0);
		}
		public TerminalNode ANNOTATION() { return getToken(ODataFilterParser.ANNOTATION, 0); }
		public TerminalNode ALIAS() { return getToken(ODataFilterParser.ALIAS, 0); }
		public FilterSegmentContext filterSegment() {
			return getRuleContext(FilterSegmentContext.class,0);
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
		enterRule(_localctx, 78, RULE_lastSegment);
		try {
			setState(767);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,74,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(758);
				match(IDENT);
				setState(760);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,73,_ctx) ) {
				case 1:
					{
					setState(759);
					keyPredicate();
					}
					break;
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(762);
				boundCall();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(763);
				castName();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(764);
				match(ANNOTATION);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(765);
				match(ALIAS);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(766);
				filterSegment();
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
	public static class FilterSegmentContext extends ParserRuleContext {
		public TerminalNode FILTERQ() { return getToken(ODataFilterParser.FILTERQ, 0); }
		public TerminalNode LPAREN() { return getToken(ODataFilterParser.LPAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
		public KeyPredicateContext keyPredicate() {
			return getRuleContext(KeyPredicateContext.class,0);
		}
		public FilterSegmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterSegment; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitFilterSegment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterSegmentContext filterSegment() throws RecognitionException {
		FilterSegmentContext _localctx = new FilterSegmentContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_filterSegment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(769);
			match(FILTERQ);
			setState(770);
			match(LPAREN);
			setState(771);
			expr();
			setState(772);
			match(RPAREN);
			setState(774);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,75,_ctx) ) {
			case 1:
				{
				setState(773);
				keyPredicate();
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
		enterRule(_localctx, 82, RULE_countCall);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(776);
			match(COUNT);
			setState(783);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
			case 1:
				{
				setState(777);
				match(LPAREN);
				setState(778);
				match(FILTERQ);
				setState(779);
				match(EQUALS);
				setState(780);
				expr();
				setState(781);
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
		enterRule(_localctx, 84, RULE_boundCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(785);
			match(IDENT);
			setState(790);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(786);
				match(DOT);
				setState(787);
				match(IDENT);
				}
				}
				setState(792);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(793);
			match(LPAREN);
			setState(795);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & -8912487601668088L) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & 31L) != 0)) {
				{
				setState(794);
				boundCallArgs();
				}
			}

			setState(797);
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
		enterRule(_localctx, 86, RULE_boundCallArgs);
		int _la;
		try {
			setState(815);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,81,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(799);
				namedArg();
				setState(804);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(800);
					match(COMMA);
					setState(801);
					namedArg();
					}
					}
					setState(806);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(807);
				expr();
				setState(812);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(808);
					match(COMMA);
					setState(809);
					expr();
					}
					}
					setState(814);
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
		enterRule(_localctx, 88, RULE_namedArg);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(817);
			match(IDENT);
			setState(818);
			match(EQUALS);
			setState(819);
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
		enterRule(_localctx, 90, RULE_lambdaCall);
		int _la;
		try {
			setState(836);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANY:
				enterOuterAlt(_localctx, 1);
				{
				setState(821);
				((LambdaCallContext)_localctx).op = match(ANY);
				setState(822);
				match(LPAREN);
				setState(826);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IDENT) {
					{
					setState(823);
					match(IDENT);
					setState(824);
					match(COLON);
					setState(825);
					expr();
					}
				}

				setState(828);
				match(RPAREN);
				}
				break;
			case ALL:
				enterOuterAlt(_localctx, 2);
				{
				setState(829);
				((LambdaCallContext)_localctx).op = match(ALL);
				setState(830);
				match(LPAREN);
				setState(831);
				match(IDENT);
				setState(832);
				match(COLON);
				setState(833);
				expr();
				setState(834);
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
	public static class JsonStringLiteralContext extends LiteralContext {
		public TerminalNode DQSTRING() { return getToken(ODataFilterParser.DQSTRING, 0); }
		public JsonStringLiteralContext(LiteralContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitJsonStringLiteral(this);
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
		enterRule(_localctx, 92, RULE_literal);
		int _la;
		try {
			setState(852);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(838);
				match(STRING);
				}
				break;
			case DQSTRING:
				_localctx = new JsonStringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(839);
				match(DQSTRING);
				}
				break;
			case BINARY:
				_localctx = new BinaryLiteralContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(840);
				match(BINARY);
				}
				break;
			case NANLIT:
			case INF:
				_localctx = new NanInfLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(841);
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
				enterOuterAlt(_localctx, 5);
				{
				setState(842);
				match(DECIMAL);
				}
				break;
			case INT:
				_localctx = new IntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(843);
				match(INT);
				}
				break;
			case TRUE:
			case FALSE:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(844);
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
				enterOuterAlt(_localctx, 8);
				{
				setState(845);
				match(NULL);
				}
				break;
			case GUID:
				_localctx = new GuidLiteralContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(846);
				match(GUID);
				}
				break;
			case DATETIMEOFFSET:
				_localctx = new DateTimeOffsetLiteralContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(847);
				match(DATETIMEOFFSET);
				}
				break;
			case DATE:
				_localctx = new DateLiteralContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(848);
				match(DATE);
				}
				break;
			case TIMEOFDAY:
				_localctx = new TimeOfDayLiteralContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(849);
				match(TIMEOFDAY);
				}
				break;
			case DURATION:
				_localctx = new DurationLiteralContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(850);
				match(DURATION);
				}
				break;
			case ENUM:
				_localctx = new EnumLiteralContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(851);
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
		case 14:
			return groupbyElement_sempred((GroupbyElementContext)_localctx, predIndex);
		case 25:
			return additive_sempred((AdditiveContext)_localctx, predIndex);
		case 26:
			return multiplicative_sempred((MultiplicativeContext)_localctx, predIndex);
		case 29:
			return aggregateCall_sempred((AggregateCallContext)_localctx, predIndex);
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
		case 9:
			return trafo("search");
		case 10:
			return trafo("nest");
		case 11:
			return trafo("addnested");
		case 12:
			return trafo("join") || trafo("outerjoin");
		case 13:
			return trafo("ancestors") || trafo("descendants");
		case 14:
			return trafo("traverse");
		case 15:
			return customTrafo();
		}
		return true;
	}
	private boolean groupbyElement_sempred(GroupbyElementContext _localctx, int predIndex) {
		switch (predIndex) {
		case 16:
			return trafo("rollup");
		case 17:
			return trafo("rolluprecursive");
		}
		return true;
	}
	private boolean additive_sempred(AdditiveContext _localctx, int predIndex) {
		switch (predIndex) {
		case 18:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean multiplicative_sempred(MultiplicativeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 19:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean aggregateCall_sempred(AggregateCallContext _localctx, int predIndex) {
		switch (predIndex) {
		case 20:
			return trafo("aggregate");
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001E\u0357\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0005\u0001e\b\u0001\n\u0001\f\u0001h\t\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0003\u0002n\b\u0002"+
		"\u0001\u0003\u0001\u0003\u0003\u0003r\b\u0003\u0001\u0003\u0001\u0003"+
		"\u0005\u0003v\b\u0003\n\u0003\f\u0003y\t\u0003\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0005\u0003\u0081\b\u0003"+
		"\n\u0003\f\u0003\u0084\t\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0003\u0003\u008b\b\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0003\u0003\u0091\b\u0003\u0001\u0003\u0003\u0003\u0094"+
		"\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0005"+
		"\u0004\u009b\b\u0004\n\u0004\f\u0004\u009e\t\u0004\u0003\u0004\u00a0\b"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0003\u0007\u00ac"+
		"\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u00b0\b\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u00b6\b\u0007\u0001\b"+
		"\u0001\b\u0001\b\u0004\b\u00bb\b\b\u000b\b\f\b\u00bc\u0001\t\u0001\t\u0001"+
		"\t\u0001\n\u0001\n\u0001\n\u0005\n\u00c5\b\n\n\n\f\n\u00c8\t\n\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0005\u000b\u00d1\b\u000b\n\u000b\f\u000b\u00d4\t\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0003\u000b\u00d9\b\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005"+
		"\u000b\u00e3\b\u000b\n\u000b\f\u000b\u00e6\t\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0005\u000b\u00f0\b\u000b\n\u000b\f\u000b\u00f3\t\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0004\u000b\u00fd\b\u000b\u000b\u000b\f\u000b\u00fe\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u0117\b\u000b\n\u000b"+
		"\f\u000b\u011a\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u0136\b\u000b"+
		"\n\u000b\f\u000b\u0139\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\u000b\u0001\u000b\u0004\u000b\u0146\b\u000b\u000b\u000b\f\u000b\u0147"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u0154\b\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0003\u000b\u0164\b\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0003\u000b\u0169\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b"+
		"\u0179\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b"+
		"\u017f\b\u000b\n\u000b\f\u000b\u0182\t\u000b\u0003\u000b\u0184\b\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0004\u000b\u018c\b\u000b\u000b\u000b\f\u000b\u018d\u0001\u000b\u0001"+
		"\u000b\u0003\u000b\u0192\b\u000b\u0001\u000b\u0003\u000b\u0195\b\u000b"+
		"\u0001\f\u0004\f\u0198\b\f\u000b\f\f\f\u0199\u0001\r\u0003\r\u019d\b\r"+
		"\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0003\r\u01a8\b\r\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0005\u000e\u01b0\b\u000e\n\u000e\f\u000e\u01b3\t\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0003\u000e\u01c1\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e"+
		"\u01c6\b\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f"+
		"\u01cc\b\u000f\n\u000f\f\u000f\u01cf\t\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0005\u000f\u01d6\b\u000f\n\u000f\f\u000f"+
		"\u01d9\t\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0005\u000f"+
		"\u01df\b\u000f\n\u000f\f\u000f\u01e2\t\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0003\u000f\u01e8\b\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0005\u0010\u01ed\b\u0010\n\u0010\f\u0010\u01f0\t\u0010\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u01f6\b\u0011\n\u0011"+
		"\f\u0011\u01f9\t\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0005\u0012\u0202\b\u0012\n\u0012"+
		"\f\u0012\u0205\t\u0012\u0001\u0012\u0001\u0012\u0003\u0012\u0209\b\u0012"+
		"\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u0214\b\u0015\n\u0015"+
		"\f\u0015\u0217\t\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016"+
		"\u021c\b\u0016\n\u0016\f\u0016\u021f\t\u0016\u0001\u0017\u0001\u0017\u0001"+
		"\u0017\u0003\u0017\u0224\b\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0004\u0018\u0230\b\u0018\u000b\u0018\f\u0018\u0231\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018"+
		"\u0246\b\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019"+
		"\u0001\u0019\u0005\u0019\u024e\b\u0019\n\u0019\f\u0019\u0251\t\u0019\u0001"+
		"\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0005"+
		"\u001a\u0259\b\u001a\n\u001a\f\u001a\u025c\t\u001a\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0003\u001b\u026c\b\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u0275\b\u001c\u0001\u001c"+
		"\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c"+
		"\u027d\b\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c"+
		"\u0283\b\u001c\u0003\u001c\u0285\b\u001c\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0005\u001e\u0291\b\u001e\n\u001e\f\u001e\u0294\t\u001e"+
		"\u0001\u001e\u0001\u001e\u0005\u001e\u0298\b\u001e\n\u001e\f\u001e\u029b"+
		"\t\u001e\u0003\u001e\u029d\b\u001e\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0001\u001f\u0005\u001f\u02a3\b\u001f\n\u001f\f\u001f\u02a6\t\u001f\u0003"+
		"\u001f\u02a8\b\u001f\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0001"+
		" \u0005 \u02b0\b \n \f \u02b3\t \u0003 \u02b5\b \u0001 \u0001 \u0001!"+
		"\u0001!\u0001!\u0001!\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0003\""+
		"\u02c2\b\"\u0001\"\u0001\"\u0001\"\u0001#\u0001#\u0001#\u0005#\u02ca\b"+
		"#\n#\f#\u02cd\t#\u0001$\u0001$\u0001$\u0001$\u0001$\u0005$\u02d4\b$\n"+
		"$\f$\u02d7\t$\u0003$\u02d9\b$\u0001$\u0001$\u0001%\u0001%\u0001%\u0005"+
		"%\u02e0\b%\n%\f%\u02e3\t%\u0001%\u0001%\u0001%\u0001%\u0001%\u0003%\u02ea"+
		"\b%\u0001&\u0001&\u0003&\u02ee\b&\u0001&\u0001&\u0001&\u0001&\u0001&\u0003"+
		"&\u02f5\b&\u0001\'\u0001\'\u0003\'\u02f9\b\'\u0001\'\u0001\'\u0001\'\u0001"+
		"\'\u0001\'\u0003\'\u0300\b\'\u0001(\u0001(\u0001(\u0001(\u0001(\u0003"+
		"(\u0307\b(\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0001)\u0003)\u0310"+
		"\b)\u0001*\u0001*\u0001*\u0005*\u0315\b*\n*\f*\u0318\t*\u0001*\u0001*"+
		"\u0003*\u031c\b*\u0001*\u0001*\u0001+\u0001+\u0001+\u0005+\u0323\b+\n"+
		"+\f+\u0326\t+\u0001+\u0001+\u0001+\u0005+\u032b\b+\n+\f+\u032e\t+\u0003"+
		"+\u0330\b+\u0001,\u0001,\u0001,\u0001,\u0001-\u0001-\u0001-\u0001-\u0001"+
		"-\u0003-\u033b\b-\u0001-\u0001-\u0001-\u0001-\u0001-\u0001-\u0001-\u0001"+
		"-\u0003-\u0345\b-\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0001"+
		".\u0001.\u0001.\u0001.\u0001.\u0001.\u0001.\u0003.\u0355\b.\u0001.\u0000"+
		"\u000224/\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016"+
		"\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@BDFHJLNPRTVXZ\\\u0000\t\u0001"+
		"\u0000\u0012\u0013\u0002\u0000;ADD\u0001\u0000\u0004\n\u0001\u0000\f\r"+
		"\u0001\u0000\u000e\u0011\u0001\u0000\u001e \u0001\u0000\u0019\u001a\u0001"+
		"\u000067\u0001\u0000\u0014\u0015\u03bc\u0000^\u0001\u0000\u0000\u0000"+
		"\u0002a\u0001\u0000\u0000\u0000\u0004k\u0001\u0000\u0000\u0000\u0006\u0093"+
		"\u0001\u0000\u0000\u0000\b\u0095\u0001\u0000\u0000\u0000\n\u00a3\u0001"+
		"\u0000\u0000\u0000\f\u00a7\u0001\u0000\u0000\u0000\u000e\u00b5\u0001\u0000"+
		"\u0000\u0000\u0010\u00b7\u0001\u0000\u0000\u0000\u0012\u00be\u0001\u0000"+
		"\u0000\u0000\u0014\u00c1\u0001\u0000\u0000\u0000\u0016\u0194\u0001\u0000"+
		"\u0000\u0000\u0018\u0197\u0001\u0000\u0000\u0000\u001a\u019c\u0001\u0000"+
		"\u0000\u0000\u001c\u01c5\u0001\u0000\u0000\u0000\u001e\u01e7\u0001\u0000"+
		"\u0000\u0000 \u01e9\u0001\u0000\u0000\u0000\"\u01f1\u0001\u0000\u0000"+
		"\u0000$\u01fd\u0001\u0000\u0000\u0000&\u020a\u0001\u0000\u0000\u0000("+
		"\u020e\u0001\u0000\u0000\u0000*\u0210\u0001\u0000\u0000\u0000,\u0218\u0001"+
		"\u0000\u0000\u0000.\u0223\u0001\u0000\u0000\u00000\u0245\u0001\u0000\u0000"+
		"\u00002\u0247\u0001\u0000\u0000\u00004\u0252\u0001\u0000\u0000\u00006"+
		"\u026b\u0001\u0000\u0000\u00008\u0284\u0001\u0000\u0000\u0000:\u0286\u0001"+
		"\u0000\u0000\u0000<\u029c\u0001\u0000\u0000\u0000>\u029e\u0001\u0000\u0000"+
		"\u0000@\u02ab\u0001\u0000\u0000\u0000B\u02b8\u0001\u0000\u0000\u0000D"+
		"\u02bc\u0001\u0000\u0000\u0000F\u02c6\u0001\u0000\u0000\u0000H\u02ce\u0001"+
		"\u0000\u0000\u0000J\u02e1\u0001\u0000\u0000\u0000L\u02f4\u0001\u0000\u0000"+
		"\u0000N\u02ff\u0001\u0000\u0000\u0000P\u0301\u0001\u0000\u0000\u0000R"+
		"\u0308\u0001\u0000\u0000\u0000T\u0311\u0001\u0000\u0000\u0000V\u032f\u0001"+
		"\u0000\u0000\u0000X\u0331\u0001\u0000\u0000\u0000Z\u0344\u0001\u0000\u0000"+
		"\u0000\\\u0354\u0001\u0000\u0000\u0000^_\u0003(\u0014\u0000_`\u0005\u0000"+
		"\u0000\u0001`\u0001\u0001\u0000\u0000\u0000af\u0003\u0004\u0002\u0000"+
		"bc\u00050\u0000\u0000ce\u0003\u0004\u0002\u0000db\u0001\u0000\u0000\u0000"+
		"eh\u0001\u0000\u0000\u0000fd\u0001\u0000\u0000\u0000fg\u0001\u0000\u0000"+
		"\u0000gi\u0001\u0000\u0000\u0000hf\u0001\u0000\u0000\u0000ij\u0005\u0000"+
		"\u0000\u0001j\u0003\u0001\u0000\u0000\u0000km\u0003(\u0014\u0000ln\u0007"+
		"\u0000\u0000\u0000ml\u0001\u0000\u0000\u0000mn\u0001\u0000\u0000\u0000"+
		"n\u0005\u0001\u0000\u0000\u0000oq\u0005B\u0000\u0000pr\u0003\b\u0004\u0000"+
		"qp\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000\u0000rw\u0001\u0000\u0000"+
		"\u0000st\u00052\u0000\u0000tv\u0003\u000e\u0007\u0000us\u0001\u0000\u0000"+
		"\u0000vy\u0001\u0000\u0000\u0000wu\u0001\u0000\u0000\u0000wx\u0001\u0000"+
		"\u0000\u0000xz\u0001\u0000\u0000\u0000yw\u0001\u0000\u0000\u0000z\u0094"+
		"\u0005\u0000\u0000\u0001{|\u0005\u001b\u0000\u0000|}\u0005.\u0000\u0000"+
		"}\u0082\u0005B\u0000\u0000~\u007f\u00050\u0000\u0000\u007f\u0081\u0005"+
		"B\u0000\u0000\u0080~\u0001\u0000\u0000\u0000\u0081\u0084\u0001\u0000\u0000"+
		"\u0000\u0082\u0080\u0001\u0000\u0000\u0000\u0082\u0083\u0001\u0000\u0000"+
		"\u0000\u0083\u0085\u0001\u0000\u0000\u0000\u0084\u0082\u0001\u0000\u0000"+
		"\u0000\u0085\u0086\u0005/\u0000\u0000\u0086\u0094\u0005\u0000\u0000\u0001"+
		"\u0087\u008a\u0005\u001c\u0000\u0000\u0088\u0089\u00052\u0000\u0000\u0089"+
		"\u008b\u0003\u0010\b\u0000\u008a\u0088\u0001\u0000\u0000\u0000\u008a\u008b"+
		"\u0001\u0000\u0000\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u0094"+
		"\u0005\u0000\u0000\u0001\u008d\u0090\u0005\u001d\u0000\u0000\u008e\u008f"+
		"\u00052\u0000\u0000\u008f\u0091\u0003\u0010\b\u0000\u0090\u008e\u0001"+
		"\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091\u0092\u0001"+
		"\u0000\u0000\u0000\u0092\u0094\u0005\u0000\u0000\u0001\u0093o\u0001\u0000"+
		"\u0000\u0000\u0093{\u0001\u0000\u0000\u0000\u0093\u0087\u0001\u0000\u0000"+
		"\u0000\u0093\u008d\u0001\u0000\u0000\u0000\u0094\u0007\u0001\u0000\u0000"+
		"\u0000\u0095\u009f\u0005.\u0000\u0000\u0096\u00a0\u0003\f\u0006\u0000"+
		"\u0097\u009c\u0003\n\u0005\u0000\u0098\u0099\u00050\u0000\u0000\u0099"+
		"\u009b\u0003\n\u0005\u0000\u009a\u0098\u0001\u0000\u0000\u0000\u009b\u009e"+
		"\u0001\u0000\u0000\u0000\u009c\u009a\u0001\u0000\u0000\u0000\u009c\u009d"+
		"\u0001\u0000\u0000\u0000\u009d\u00a0\u0001\u0000\u0000\u0000\u009e\u009c"+
		"\u0001\u0000\u0000\u0000\u009f\u0096\u0001\u0000\u0000\u0000\u009f\u0097"+
		"\u0001\u0000\u0000\u0000\u00a0\u00a1\u0001\u0000\u0000\u0000\u00a1\u00a2"+
		"\u0005/\u0000\u0000\u00a2\t\u0001\u0000\u0000\u0000\u00a3\u00a4\u0005"+
		"B\u0000\u0000\u00a4\u00a5\u00051\u0000\u0000\u00a5\u00a6\u0003\f\u0006"+
		"\u0000\u00a6\u000b\u0001\u0000\u0000\u0000\u00a7\u00a8\u0007\u0001\u0000"+
		"\u0000\u00a8\r\u0001\u0000\u0000\u0000\u00a9\u00ab\u0003\u0010\b\u0000"+
		"\u00aa\u00ac\u0003\b\u0004\u0000\u00ab\u00aa\u0001\u0000\u0000\u0000\u00ab"+
		"\u00ac\u0001\u0000\u0000\u0000\u00ac\u00b6\u0001\u0000\u0000\u0000\u00ad"+
		"\u00af\u0005B\u0000\u0000\u00ae\u00b0\u0003\b\u0004\u0000\u00af\u00ae"+
		"\u0001\u0000\u0000\u0000\u00af\u00b0\u0001\u0000\u0000\u0000\u00b0\u00b6"+
		"\u0001\u0000\u0000\u0000\u00b1\u00b6\u0005\"\u0000\u0000\u00b2\u00b6\u0005"+
		"#\u0000\u0000\u00b3\u00b6\u0005$\u0000\u0000\u00b4\u00b6\u0003\f\u0006"+
		"\u0000\u00b5\u00a9\u0001\u0000\u0000\u0000\u00b5\u00ad\u0001\u0000\u0000"+
		"\u0000\u00b5\u00b1\u0001\u0000\u0000\u0000\u00b5\u00b2\u0001\u0000\u0000"+
		"\u0000\u00b5\u00b3\u0001\u0000\u0000\u0000\u00b5\u00b4\u0001\u0000\u0000"+
		"\u0000\u00b6\u000f\u0001\u0000\u0000\u0000\u00b7\u00ba\u0005B\u0000\u0000"+
		"\u00b8\u00b9\u00054\u0000\u0000\u00b9\u00bb\u0005B\u0000\u0000\u00ba\u00b8"+
		"\u0001\u0000\u0000\u0000\u00bb\u00bc\u0001\u0000\u0000\u0000\u00bc\u00ba"+
		"\u0001\u0000\u0000\u0000\u00bc\u00bd\u0001\u0000\u0000\u0000\u00bd\u0011"+
		"\u0001\u0000\u0000\u0000\u00be\u00bf\u0003\u0014\n\u0000\u00bf\u00c0\u0005"+
		"\u0000\u0000\u0001\u00c0\u0013\u0001\u0000\u0000\u0000\u00c1\u00c6\u0003"+
		"\u0016\u000b\u0000\u00c2\u00c3\u00052\u0000\u0000\u00c3\u00c5\u0003\u0016"+
		"\u000b\u0000\u00c4\u00c2\u0001\u0000\u0000\u0000\u00c5\u00c8\u0001\u0000"+
		"\u0000\u0000\u00c6\u00c4\u0001\u0000\u0000\u0000\u00c6\u00c7\u0001\u0000"+
		"\u0000\u0000\u00c7\u0015\u0001\u0000\u0000\u0000\u00c8\u00c6\u0001\u0000"+
		"\u0000\u0000\u00c9\u00ca\u0004\u000b\u0000\u0000\u00ca\u00cb\u0005B\u0000"+
		"\u0000\u00cb\u00cc\u0005.\u0000\u0000\u00cc\u00cd\u0005.\u0000\u0000\u00cd"+
		"\u00d2\u0003\u001c\u000e\u0000\u00ce\u00cf\u00050\u0000\u0000\u00cf\u00d1"+
		"\u0003\u001c\u000e\u0000\u00d0\u00ce\u0001\u0000\u0000\u0000\u00d1\u00d4"+
		"\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001\u0000\u0000\u0000\u00d2\u00d3"+
		"\u0001\u0000\u0000\u0000\u00d3\u00d5\u0001\u0000\u0000\u0000\u00d4\u00d2"+
		"\u0001\u0000\u0000\u0000\u00d5\u00d8\u0005/\u0000\u0000\u00d6\u00d7\u0005"+
		"0\u0000\u0000\u00d7\u00d9\u0003\u0014\n\u0000\u00d8\u00d6\u0001\u0000"+
		"\u0000\u0000\u00d8\u00d9\u0001\u0000\u0000\u0000\u00d9\u00da\u0001\u0000"+
		"\u0000\u0000\u00da\u00db\u0005/\u0000\u0000\u00db\u0195\u0001\u0000\u0000"+
		"\u0000\u00dc\u00dd\u0004\u000b\u0001\u0000\u00dd\u00de\u0005B\u0000\u0000"+
		"\u00de\u00df\u0005.\u0000\u0000\u00df\u00e4\u0003\u001e\u000f\u0000\u00e0"+
		"\u00e1\u00050\u0000\u0000\u00e1\u00e3\u0003\u001e\u000f\u0000\u00e2\u00e0"+
		"\u0001\u0000\u0000\u0000\u00e3\u00e6\u0001\u0000\u0000\u0000\u00e4\u00e2"+
		"\u0001\u0000\u0000\u0000\u00e4\u00e5\u0001\u0000\u0000\u0000\u00e5\u00e7"+
		"\u0001\u0000\u0000\u0000\u00e6\u00e4\u0001\u0000\u0000\u0000\u00e7\u00e8"+
		"\u0005/\u0000\u0000\u00e8\u0195\u0001\u0000\u0000\u0000\u00e9\u00ea\u0004"+
		"\u000b\u0002\u0000\u00ea\u00eb\u0005B\u0000\u0000\u00eb\u00ec\u0005.\u0000"+
		"\u0000\u00ec\u00f1\u0003&\u0013\u0000\u00ed\u00ee\u00050\u0000\u0000\u00ee"+
		"\u00f0\u0003&\u0013\u0000\u00ef\u00ed\u0001\u0000\u0000\u0000\u00f0\u00f3"+
		"\u0001\u0000\u0000\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f1\u00f2"+
		"\u0001\u0000\u0000\u0000\u00f2\u00f4\u0001\u0000\u0000\u0000\u00f3\u00f1"+
		"\u0001\u0000\u0000\u0000\u00f4\u00f5\u0005/\u0000\u0000\u00f5\u0195\u0001"+
		"\u0000\u0000\u0000\u00f6\u00f7\u0004\u000b\u0003\u0000\u00f7\u00f8\u0005"+
		"B\u0000\u0000\u00f8\u00f9\u0005.\u0000\u0000\u00f9\u00fc\u0003\u0014\n"+
		"\u0000\u00fa\u00fb\u00050\u0000\u0000\u00fb\u00fd\u0003\u0014\n\u0000"+
		"\u00fc\u00fa\u0001\u0000\u0000\u0000\u00fd\u00fe\u0001\u0000\u0000\u0000"+
		"\u00fe\u00fc\u0001\u0000\u0000\u0000\u00fe\u00ff\u0001\u0000\u0000\u0000"+
		"\u00ff\u0100\u0001\u0000\u0000\u0000\u0100\u0101\u0005/\u0000\u0000\u0101"+
		"\u0195\u0001\u0000\u0000\u0000\u0102\u0103\u0004\u000b\u0004\u0000\u0103"+
		"\u0104\u0005B\u0000\u0000\u0104\u0105\u0005.\u0000\u0000\u0105\u0106\u0003"+
		"(\u0014\u0000\u0106\u0107\u0005/\u0000\u0000\u0107\u0195\u0001\u0000\u0000"+
		"\u0000\u0108\u0109\u0004\u000b\u0005\u0000\u0109\u010a\u0005B\u0000\u0000"+
		"\u010a\u010b\u0005.\u0000\u0000\u010b\u010c\u0003(\u0014\u0000\u010c\u010d"+
		"\u00050\u0000\u0000\u010d\u010e\u0003(\u0014\u0000\u010e\u010f\u0005/"+
		"\u0000\u0000\u010f\u0195\u0001\u0000\u0000\u0000\u0110\u0111\u0004\u000b"+
		"\u0006\u0000\u0111\u0112\u0005B\u0000\u0000\u0112\u0113\u0005.\u0000\u0000"+
		"\u0113\u0118\u0003\u0004\u0002\u0000\u0114\u0115\u00050\u0000\u0000\u0115"+
		"\u0117\u0003\u0004\u0002\u0000\u0116\u0114\u0001\u0000\u0000\u0000\u0117"+
		"\u011a\u0001\u0000\u0000\u0000\u0118\u0116\u0001\u0000\u0000\u0000\u0118"+
		"\u0119\u0001\u0000\u0000\u0000\u0119\u011b\u0001\u0000\u0000\u0000\u011a"+
		"\u0118\u0001\u0000\u0000\u0000\u011b\u011c\u0005/\u0000\u0000\u011c\u0195"+
		"\u0001\u0000\u0000\u0000\u011d\u011e\u0004\u000b\u0007\u0000\u011e\u011f"+
		"\u0005B\u0000\u0000\u011f\u0120\u0005.\u0000\u0000\u0120\u0121\u0005A"+
		"\u0000\u0000\u0121\u0195\u0005/\u0000\u0000\u0122\u0123\u0004\u000b\b"+
		"\u0000\u0123\u0195\u0005B\u0000\u0000\u0124\u0125\u0004\u000b\t\u0000"+
		"\u0125\u0126\u0005B\u0000\u0000\u0126\u0127\u0005.\u0000\u0000\u0127\u0128"+
		"\u0003\u0018\f\u0000\u0128\u0129\u0005/\u0000\u0000\u0129\u0195\u0001"+
		"\u0000\u0000\u0000\u012a\u012b\u0004\u000b\n\u0000\u012b\u012c\u0005B"+
		"\u0000\u0000\u012c\u012d\u0005.\u0000\u0000\u012d\u012e\u0003\u0014\n"+
		"\u0000\u012e\u012f\u0005\'\u0000\u0000\u012f\u0137\u0005B\u0000\u0000"+
		"\u0130\u0131\u00050\u0000\u0000\u0131\u0132\u0003\u0014\n\u0000\u0132"+
		"\u0133\u0005\'\u0000\u0000\u0133\u0134\u0005B\u0000\u0000\u0134\u0136"+
		"\u0001\u0000\u0000\u0000\u0135\u0130\u0001\u0000\u0000\u0000\u0136\u0139"+
		"\u0001\u0000\u0000\u0000\u0137\u0135\u0001\u0000\u0000\u0000\u0137\u0138"+
		"\u0001\u0000\u0000\u0000\u0138\u013a\u0001\u0000\u0000\u0000\u0139\u0137"+
		"\u0001\u0000\u0000\u0000\u013a\u013b\u0005/\u0000\u0000\u013b\u0195\u0001"+
		"\u0000\u0000\u0000\u013c\u013d\u0004\u000b\u000b\u0000\u013d\u013e\u0005"+
		"B\u0000\u0000\u013e\u013f\u0005.\u0000\u0000\u013f\u0145\u0003J%\u0000"+
		"\u0140\u0141\u00050\u0000\u0000\u0141\u0142\u0003\u0014\n\u0000\u0142"+
		"\u0143\u0005\'\u0000\u0000\u0143\u0144\u0005B\u0000\u0000\u0144\u0146"+
		"\u0001\u0000\u0000\u0000\u0145\u0140\u0001\u0000\u0000\u0000\u0146\u0147"+
		"\u0001\u0000\u0000\u0000\u0147\u0145\u0001\u0000\u0000\u0000\u0147\u0148"+
		"\u0001\u0000\u0000\u0000\u0148\u0149\u0001\u0000\u0000\u0000\u0149\u014a"+
		"\u0005/\u0000\u0000\u014a\u0195\u0001\u0000\u0000\u0000\u014b\u014c\u0004"+
		"\u000b\f\u0000\u014c\u014d\u0005B\u0000\u0000\u014d\u014e\u0005.\u0000"+
		"\u0000\u014e\u014f\u0003J%\u0000\u014f\u0150\u0005\'\u0000\u0000\u0150"+
		"\u0153\u0005B\u0000\u0000\u0151\u0152\u00050\u0000\u0000\u0152\u0154\u0003"+
		"\u0014\n\u0000\u0153\u0151\u0001\u0000\u0000\u0000\u0153\u0154\u0001\u0000"+
		"\u0000\u0000\u0154\u0155\u0001\u0000\u0000\u0000\u0155\u0156\u0005/\u0000"+
		"\u0000\u0156\u0195\u0001\u0000\u0000\u0000\u0157\u0158\u0004\u000b\r\u0000"+
		"\u0158\u0159\u0005B\u0000\u0000\u0159\u015a\u0005.\u0000\u0000\u015a\u015b"+
		"\u00038\u001c\u0000\u015b\u015c\u00050\u0000\u0000\u015c\u015d\u0005B"+
		"\u0000\u0000\u015d\u015e\u00050\u0000\u0000\u015e\u015f\u0003J%\u0000"+
		"\u015f\u0160\u00050\u0000\u0000\u0160\u0163\u0003\u0014\n\u0000\u0161"+
		"\u0162\u00050\u0000\u0000\u0162\u0164\u0005A\u0000\u0000\u0163\u0161\u0001"+
		"\u0000\u0000\u0000\u0163\u0164\u0001\u0000\u0000\u0000\u0164\u0168\u0001"+
		"\u0000\u0000\u0000\u0165\u0166\u00050\u0000\u0000\u0166\u0167\u0005B\u0000"+
		"\u0000\u0167\u0169\u0005B\u0000\u0000\u0168\u0165\u0001\u0000\u0000\u0000"+
		"\u0168\u0169\u0001\u0000\u0000\u0000\u0169\u016a\u0001\u0000\u0000\u0000"+
		"\u016a\u016b\u0005/\u0000\u0000\u016b\u0195\u0001\u0000\u0000\u0000\u016c"+
		"\u016d\u0004\u000b\u000e\u0000\u016d\u016e\u0005B\u0000\u0000\u016e\u016f"+
		"\u0005.\u0000\u0000\u016f\u0170\u00038\u001c\u0000\u0170\u0171\u00050"+
		"\u0000\u0000\u0171\u0172\u0005B\u0000\u0000\u0172\u0173\u00050\u0000\u0000"+
		"\u0173\u0174\u0003J%\u0000\u0174\u0175\u00050\u0000\u0000\u0175\u0178"+
		"\u0005B\u0000\u0000\u0176\u0177\u00050\u0000\u0000\u0177\u0179\u0003\u0014"+
		"\n\u0000\u0178\u0176\u0001\u0000\u0000\u0000\u0178\u0179\u0001\u0000\u0000"+
		"\u0000\u0179\u0183\u0001\u0000\u0000\u0000\u017a\u017b\u00050\u0000\u0000"+
		"\u017b\u0180\u0003\u0004\u0002\u0000\u017c\u017d\u00050\u0000\u0000\u017d"+
		"\u017f\u0003\u0004\u0002\u0000\u017e\u017c\u0001\u0000\u0000\u0000\u017f"+
		"\u0182\u0001\u0000\u0000\u0000\u0180\u017e\u0001\u0000\u0000\u0000\u0180"+
		"\u0181\u0001\u0000\u0000\u0000\u0181\u0184\u0001\u0000\u0000\u0000\u0182"+
		"\u0180\u0001\u0000\u0000\u0000\u0183\u017a\u0001\u0000\u0000\u0000\u0183"+
		"\u0184\u0001\u0000\u0000\u0000\u0184\u0185\u0001\u0000\u0000\u0000\u0185"+
		"\u0186\u0005/\u0000\u0000\u0186\u0195\u0001\u0000\u0000\u0000\u0187\u0188"+
		"\u0004\u000b\u000f\u0000\u0188\u018b\u0005B\u0000\u0000\u0189\u018a\u0005"+
		"4\u0000\u0000\u018a\u018c\u0005B\u0000\u0000\u018b\u0189\u0001\u0000\u0000"+
		"\u0000\u018c\u018d\u0001\u0000\u0000\u0000\u018d\u018b\u0001\u0000\u0000"+
		"\u0000\u018d\u018e\u0001\u0000\u0000\u0000\u018e\u018f\u0001\u0000\u0000"+
		"\u0000\u018f\u0191\u0005.\u0000\u0000\u0190\u0192\u0003V+\u0000\u0191"+
		"\u0190\u0001\u0000\u0000\u0000\u0191\u0192\u0001\u0000\u0000\u0000\u0192"+
		"\u0193\u0001\u0000\u0000\u0000\u0193\u0195\u0005/\u0000\u0000\u0194\u00c9"+
		"\u0001\u0000\u0000\u0000\u0194\u00dc\u0001\u0000\u0000\u0000\u0194\u00e9"+
		"\u0001\u0000\u0000\u0000\u0194\u00f6\u0001\u0000\u0000\u0000\u0194\u0102"+
		"\u0001\u0000\u0000\u0000\u0194\u0108\u0001\u0000\u0000\u0000\u0194\u0110"+
		"\u0001\u0000\u0000\u0000\u0194\u011d\u0001\u0000\u0000\u0000\u0194\u0122"+
		"\u0001\u0000\u0000\u0000\u0194\u0124\u0001\u0000\u0000\u0000\u0194\u012a"+
		"\u0001\u0000\u0000\u0000\u0194\u013c\u0001\u0000\u0000\u0000\u0194\u014b"+
		"\u0001\u0000\u0000\u0000\u0194\u0157\u0001\u0000\u0000\u0000\u0194\u016c"+
		"\u0001\u0000\u0000\u0000\u0194\u0187\u0001\u0000\u0000\u0000\u0195\u0017"+
		"\u0001\u0000\u0000\u0000\u0196\u0198\u0003\u001a\r\u0000\u0197\u0196\u0001"+
		"\u0000\u0000\u0000\u0198\u0199\u0001\u0000\u0000\u0000\u0199\u0197\u0001"+
		"\u0000\u0000\u0000\u0199\u019a\u0001\u0000\u0000\u0000\u019a\u0019\u0001"+
		"\u0000\u0000\u0000\u019b\u019d\u0005\u0003\u0000\u0000\u019c\u019b\u0001"+
		"\u0000\u0000\u0000\u019c\u019d\u0001\u0000\u0000\u0000\u019d\u01a7\u0001"+
		"\u0000\u0000\u0000\u019e\u01a8\u0005B\u0000\u0000\u019f\u01a8\u0005?\u0000"+
		"\u0000\u01a0\u01a8\u00055\u0000\u0000\u01a1\u01a8\u0005A\u0000\u0000\u01a2"+
		"\u01a8\u0005@\u0000\u0000\u01a3\u01a4\u0005.\u0000\u0000\u01a4\u01a5\u0003"+
		"\u0018\f\u0000\u01a5\u01a6\u0005/\u0000\u0000\u01a6\u01a8\u0001\u0000"+
		"\u0000\u0000\u01a7\u019e\u0001\u0000\u0000\u0000\u01a7\u019f\u0001\u0000"+
		"\u0000\u0000\u01a7\u01a0\u0001\u0000\u0000\u0000\u01a7\u01a1\u0001\u0000"+
		"\u0000\u0000\u01a7\u01a2\u0001\u0000\u0000\u0000\u01a7\u01a3\u0001\u0000"+
		"\u0000\u0000\u01a8\u001b\u0001\u0000\u0000\u0000\u01a9\u01aa\u0004\u000e"+
		"\u0010\u0000\u01aa\u01ab\u0005B\u0000\u0000\u01ab\u01ac\u0005.\u0000\u0000"+
		"\u01ac\u01b1\u0003J%\u0000\u01ad\u01ae\u00050\u0000\u0000\u01ae\u01b0"+
		"\u0003J%\u0000\u01af\u01ad\u0001\u0000\u0000\u0000\u01b0\u01b3\u0001\u0000"+
		"\u0000\u0000\u01b1\u01af\u0001\u0000\u0000\u0000\u01b1\u01b2\u0001\u0000"+
		"\u0000\u0000\u01b2\u01b4\u0001\u0000\u0000\u0000\u01b3\u01b1\u0001\u0000"+
		"\u0000\u0000\u01b4\u01b5\u0005/\u0000\u0000\u01b5\u01c6\u0001\u0000\u0000"+
		"\u0000\u01b6\u01b7\u0004\u000e\u0011\u0000\u01b7\u01b8\u0005B\u0000\u0000"+
		"\u01b8\u01b9\u0005.\u0000\u0000\u01b9\u01ba\u00038\u001c\u0000\u01ba\u01bb"+
		"\u00050\u0000\u0000\u01bb\u01bc\u0005B\u0000\u0000\u01bc\u01bd\u00050"+
		"\u0000\u0000\u01bd\u01c0\u0003J%\u0000\u01be\u01bf\u00050\u0000\u0000"+
		"\u01bf\u01c1\u0003\u0014\n\u0000\u01c0\u01be\u0001\u0000\u0000\u0000\u01c0"+
		"\u01c1\u0001\u0000\u0000\u0000\u01c1\u01c2\u0001\u0000\u0000\u0000\u01c2"+
		"\u01c3\u0005/\u0000\u0000\u01c3\u01c6\u0001\u0000\u0000\u0000\u01c4\u01c6"+
		"\u0003J%\u0000\u01c5\u01a9\u0001\u0000\u0000\u0000\u01c5\u01b6\u0001\u0000"+
		"\u0000\u0000\u01c5\u01c4\u0001\u0000\u0000\u0000\u01c6\u001d\u0001\u0000"+
		"\u0000\u0000\u01c7\u01c8\u0003(\u0014\u0000\u01c8\u01c9\u0005&\u0000\u0000"+
		"\u01c9\u01cd\u0003 \u0010\u0000\u01ca\u01cc\u0003\"\u0011\u0000\u01cb"+
		"\u01ca\u0001\u0000\u0000\u0000\u01cc\u01cf\u0001\u0000\u0000\u0000\u01cd"+
		"\u01cb\u0001\u0000\u0000\u0000\u01cd\u01ce\u0001\u0000\u0000\u0000\u01ce"+
		"\u01d0\u0001\u0000\u0000\u0000\u01cf\u01cd\u0001\u0000\u0000\u0000\u01d0"+
		"\u01d1\u0005\'\u0000\u0000\u01d1\u01d2\u0005B\u0000\u0000\u01d2\u01e8"+
		"\u0001\u0000\u0000\u0000\u01d3\u01d7\u0005\"\u0000\u0000\u01d4\u01d6\u0003"+
		"\"\u0011\u0000\u01d5\u01d4\u0001\u0000\u0000\u0000\u01d6\u01d9\u0001\u0000"+
		"\u0000\u0000\u01d7\u01d5\u0001\u0000\u0000\u0000\u01d7\u01d8\u0001\u0000"+
		"\u0000\u0000\u01d8\u01da\u0001\u0000\u0000\u0000\u01d9\u01d7\u0001\u0000"+
		"\u0000\u0000\u01da\u01db\u0005\'\u0000\u0000\u01db\u01e8\u0005B\u0000"+
		"\u0000\u01dc\u01e0\u0003(\u0014\u0000\u01dd\u01df\u0003$\u0012\u0000\u01de"+
		"\u01dd\u0001\u0000\u0000\u0000\u01df\u01e2\u0001\u0000\u0000\u0000\u01e0"+
		"\u01de\u0001\u0000\u0000\u0000\u01e0\u01e1\u0001\u0000\u0000\u0000\u01e1"+
		"\u01e3\u0001\u0000\u0000\u0000\u01e2\u01e0\u0001\u0000\u0000\u0000\u01e3"+
		"\u01e4\u0005\'\u0000\u0000\u01e4\u01e5\u0005B\u0000\u0000\u01e5\u01e8"+
		"\u0001\u0000\u0000\u0000\u01e6\u01e8\u0003(\u0014\u0000\u01e7\u01c7\u0001"+
		"\u0000\u0000\u0000\u01e7\u01d3\u0001\u0000\u0000\u0000\u01e7\u01dc\u0001"+
		"\u0000\u0000\u0000\u01e7\u01e6\u0001\u0000\u0000\u0000\u01e8\u001f\u0001"+
		"\u0000\u0000\u0000\u01e9\u01ee\u0005B\u0000\u0000\u01ea\u01eb\u00054\u0000"+
		"\u0000\u01eb\u01ed\u0005B\u0000\u0000\u01ec\u01ea\u0001\u0000\u0000\u0000"+
		"\u01ed\u01f0\u0001\u0000\u0000\u0000\u01ee\u01ec\u0001\u0000\u0000\u0000"+
		"\u01ee\u01ef\u0001\u0000\u0000\u0000\u01ef!\u0001\u0000\u0000\u0000\u01f0"+
		"\u01ee\u0001\u0000\u0000\u0000\u01f1\u01f2\u0005(\u0000\u0000\u01f2\u01f7"+
		"\u0003J%\u0000\u01f3\u01f4\u00050\u0000\u0000\u01f4\u01f6\u0003J%\u0000"+
		"\u01f5\u01f3\u0001\u0000\u0000\u0000\u01f6\u01f9\u0001\u0000\u0000\u0000"+
		"\u01f7\u01f5\u0001\u0000\u0000\u0000\u01f7\u01f8\u0001\u0000\u0000\u0000"+
		"\u01f8\u01fa\u0001\u0000\u0000\u0000\u01f9\u01f7\u0001\u0000\u0000\u0000"+
		"\u01fa\u01fb\u0005&\u0000\u0000\u01fb\u01fc\u0003 \u0010\u0000\u01fc#"+
		"\u0001\u0000\u0000\u0000\u01fd\u01fe\u0005(\u0000\u0000\u01fe\u0203\u0003"+
		"J%\u0000\u01ff\u0200\u00050\u0000\u0000\u0200\u0202\u0003J%\u0000\u0201"+
		"\u01ff\u0001\u0000\u0000\u0000\u0202\u0205\u0001\u0000\u0000\u0000\u0203"+
		"\u0201\u0001\u0000\u0000\u0000\u0203\u0204\u0001\u0000\u0000\u0000\u0204"+
		"\u0208\u0001\u0000\u0000\u0000\u0205\u0203\u0001\u0000\u0000\u0000\u0206"+
		"\u0207\u0005&\u0000\u0000\u0207\u0209\u0003 \u0010\u0000\u0208\u0206\u0001"+
		"\u0000\u0000\u0000\u0208\u0209\u0001\u0000\u0000\u0000\u0209%\u0001\u0000"+
		"\u0000\u0000\u020a\u020b\u0003(\u0014\u0000\u020b\u020c\u0005\'\u0000"+
		"\u0000\u020c\u020d\u0005B\u0000\u0000\u020d\'\u0001\u0000\u0000\u0000"+
		"\u020e\u020f\u0003*\u0015\u0000\u020f)\u0001\u0000\u0000\u0000\u0210\u0215"+
		"\u0003,\u0016\u0000\u0211\u0212\u0005\u0001\u0000\u0000\u0212\u0214\u0003"+
		",\u0016\u0000\u0213\u0211\u0001\u0000\u0000\u0000\u0214\u0217\u0001\u0000"+
		"\u0000\u0000\u0215\u0213\u0001\u0000\u0000\u0000\u0215\u0216\u0001\u0000"+
		"\u0000\u0000\u0216+\u0001\u0000\u0000\u0000\u0217\u0215\u0001\u0000\u0000"+
		"\u0000\u0218\u021d\u0003.\u0017\u0000\u0219\u021a\u0005\u0002\u0000\u0000"+
		"\u021a\u021c\u0003.\u0017\u0000\u021b\u0219\u0001\u0000\u0000\u0000\u021c"+
		"\u021f\u0001\u0000\u0000\u0000\u021d\u021b\u0001\u0000\u0000\u0000\u021d"+
		"\u021e\u0001\u0000\u0000\u0000\u021e-\u0001\u0000\u0000\u0000\u021f\u021d"+
		"\u0001\u0000\u0000\u0000\u0220\u0221\u0005\u0003\u0000\u0000\u0221\u0224"+
		"\u0003.\u0017\u0000\u0222\u0224\u00030\u0018\u0000\u0223\u0220\u0001\u0000"+
		"\u0000\u0000\u0223\u0222\u0001\u0000\u0000\u0000\u0224/\u0001\u0000\u0000"+
		"\u0000\u0225\u0226\u00032\u0019\u0000\u0226\u0227\u0007\u0002\u0000\u0000"+
		"\u0227\u0228\u00032\u0019\u0000\u0228\u0246\u0001\u0000\u0000\u0000\u0229"+
		"\u022a\u00032\u0019\u0000\u022a\u022b\u0005\u000b\u0000\u0000\u022b\u022c"+
		"\u0005.\u0000\u0000\u022c\u022f\u0003\\.\u0000\u022d\u022e\u00050\u0000"+
		"\u0000\u022e\u0230\u0003\\.\u0000\u022f\u022d\u0001\u0000\u0000\u0000"+
		"\u0230\u0231\u0001\u0000\u0000\u0000\u0231\u022f\u0001\u0000\u0000\u0000"+
		"\u0231\u0232\u0001\u0000\u0000\u0000\u0232\u0233\u0001\u0000\u0000\u0000"+
		"\u0233\u0234\u0005/\u0000\u0000\u0234\u0246\u0001\u0000\u0000\u0000\u0235"+
		"\u0236\u00032\u0019\u0000\u0236\u0237\u0005\u000b\u0000\u0000\u0237\u0238"+
		"\u0005.\u0000\u0000\u0238\u0239\u0005/\u0000\u0000\u0239\u0246\u0001\u0000"+
		"\u0000\u0000\u023a\u023b\u00032\u0019\u0000\u023b\u023c\u0005\u000b\u0000"+
		"\u0000\u023c\u023d\u0005.\u0000\u0000\u023d\u023e\u0003(\u0014\u0000\u023e"+
		"\u023f\u0005/\u0000\u0000\u023f\u0246\u0001\u0000\u0000\u0000\u0240\u0241"+
		"\u00032\u0019\u0000\u0241\u0242\u0005\u000b\u0000\u0000\u0242\u0243\u0003"+
		">\u001f\u0000\u0243\u0246\u0001\u0000\u0000\u0000\u0244\u0246\u00032\u0019"+
		"\u0000\u0245\u0225\u0001\u0000\u0000\u0000\u0245\u0229\u0001\u0000\u0000"+
		"\u0000\u0245\u0235\u0001\u0000\u0000\u0000\u0245\u023a\u0001\u0000\u0000"+
		"\u0000\u0245\u0240\u0001\u0000\u0000\u0000\u0245\u0244\u0001\u0000\u0000"+
		"\u0000\u02461\u0001\u0000\u0000\u0000\u0247\u0248\u0006\u0019\uffff\uffff"+
		"\u0000\u0248\u0249\u00034\u001a\u0000\u0249\u024f\u0001\u0000\u0000\u0000"+
		"\u024a\u024b\n\u0002\u0000\u0000\u024b\u024c\u0007\u0003\u0000\u0000\u024c"+
		"\u024e\u00034\u001a\u0000\u024d\u024a\u0001\u0000\u0000\u0000\u024e\u0251"+
		"\u0001\u0000\u0000\u0000\u024f\u024d\u0001\u0000\u0000\u0000\u024f\u0250"+
		"\u0001\u0000\u0000\u0000\u02503\u0001\u0000\u0000\u0000\u0251\u024f\u0001"+
		"\u0000\u0000\u0000\u0252\u0253\u0006\u001a\uffff\uffff\u0000\u0253\u0254"+
		"\u00036\u001b\u0000\u0254\u025a\u0001\u0000\u0000\u0000\u0255\u0256\n"+
		"\u0002\u0000\u0000\u0256\u0257\u0007\u0004\u0000\u0000\u0257\u0259\u0003"+
		"6\u001b\u0000\u0258\u0255\u0001\u0000\u0000\u0000\u0259\u025c\u0001\u0000"+
		"\u0000\u0000\u025a\u0258\u0001\u0000\u0000\u0000\u025a\u025b\u0001\u0000"+
		"\u0000\u0000\u025b5\u0001\u0000\u0000\u0000\u025c\u025a\u0001\u0000\u0000"+
		"\u0000\u025d\u026c\u0003\\.\u0000\u025e\u026c\u0003D\"\u0000\u025f\u026c"+
		"\u0003H$\u0000\u0260\u026c\u00038\u001c\u0000\u0261\u026c\u0005D\u0000"+
		"\u0000\u0262\u026c\u0003J%\u0000\u0263\u0264\u0005.\u0000\u0000\u0264"+
		"\u0265\u0003(\u0014\u0000\u0265\u0266\u0005/\u0000\u0000\u0266\u026c\u0001"+
		"\u0000\u0000\u0000\u0267\u0268\u0005)\u0000\u0000\u0268\u026c\u00036\u001b"+
		"\u0000\u0269\u026c\u0003>\u001f\u0000\u026a\u026c\u0003@ \u0000\u026b"+
		"\u025d\u0001\u0000\u0000\u0000\u026b\u025e\u0001\u0000\u0000\u0000\u026b"+
		"\u025f\u0001\u0000\u0000\u0000\u026b\u0260\u0001\u0000\u0000\u0000\u026b"+
		"\u0261\u0001\u0000\u0000\u0000\u026b\u0262\u0001\u0000\u0000\u0000\u026b"+
		"\u0263\u0001\u0000\u0000\u0000\u026b\u0267\u0001\u0000\u0000\u0000\u026b"+
		"\u0269\u0001\u0000\u0000\u0000\u026b\u026a\u0001\u0000\u0000\u0000\u026c"+
		"7\u0001\u0000\u0000\u0000\u026d\u0274\u0007\u0005\u0000\u0000\u026e\u026f"+
		"\u00052\u0000\u0000\u026f\u0275\u0003J%\u0000\u0270\u0271\u00052\u0000"+
		"\u0000\u0271\u0275\u0003R)\u0000\u0272\u0273\u00052\u0000\u0000\u0273"+
		"\u0275\u0003:\u001d\u0000\u0274\u026e\u0001\u0000\u0000\u0000\u0274\u0270"+
		"\u0001\u0000\u0000\u0000\u0274\u0272\u0001\u0000\u0000\u0000\u0274\u0275"+
		"\u0001\u0000\u0000\u0000\u0275\u0285\u0001\u0000\u0000\u0000\u0276\u0277"+
		"\u0005!\u0000\u0000\u0277\u0278\u00052\u0000\u0000\u0278\u027c\u0005B"+
		"\u0000\u0000\u0279\u027d\u0003\b\u0004\u0000\u027a\u027b\u0005.\u0000"+
		"\u0000\u027b\u027d\u0005/\u0000\u0000\u027c\u0279\u0001\u0000\u0000\u0000"+
		"\u027c\u027a\u0001\u0000\u0000\u0000\u027c\u027d\u0001\u0000\u0000\u0000"+
		"\u027d\u0282\u0001\u0000\u0000\u0000\u027e\u027f\u00052\u0000\u0000\u027f"+
		"\u0283\u0003J%\u0000\u0280\u0281\u00052\u0000\u0000\u0281\u0283\u0003"+
		"R)\u0000\u0282\u027e\u0001\u0000\u0000\u0000\u0282\u0280\u0001\u0000\u0000"+
		"\u0000\u0282\u0283\u0001\u0000\u0000\u0000\u0283\u0285\u0001\u0000\u0000"+
		"\u0000\u0284\u026d\u0001\u0000\u0000\u0000\u0284\u0276\u0001\u0000\u0000"+
		"\u0000\u02859\u0001\u0000\u0000\u0000\u0286\u0287\u0004\u001d\u0014\u0000"+
		"\u0287\u0288\u0005B\u0000\u0000\u0288\u0289\u0005.\u0000\u0000\u0289\u028a"+
		"\u0003<\u001e\u0000\u028a\u028b\u0005/\u0000\u0000\u028b;\u0001\u0000"+
		"\u0000\u0000\u028c\u028d\u0003(\u0014\u0000\u028d\u028e\u0005&\u0000\u0000"+
		"\u028e\u0292\u0003 \u0010\u0000\u028f\u0291\u0003\"\u0011\u0000\u0290"+
		"\u028f\u0001\u0000\u0000\u0000\u0291\u0294\u0001\u0000\u0000\u0000\u0292"+
		"\u0290\u0001\u0000\u0000\u0000\u0292\u0293\u0001\u0000\u0000\u0000\u0293"+
		"\u029d\u0001\u0000\u0000\u0000\u0294\u0292\u0001\u0000\u0000\u0000\u0295"+
		"\u0299\u0005\"\u0000\u0000\u0296\u0298\u0003\"\u0011\u0000\u0297\u0296"+
		"\u0001\u0000\u0000\u0000\u0298\u029b\u0001\u0000\u0000\u0000\u0299\u0297"+
		"\u0001\u0000\u0000\u0000\u0299\u029a\u0001\u0000\u0000\u0000\u029a\u029d"+
		"\u0001\u0000\u0000\u0000\u029b\u0299\u0001\u0000\u0000\u0000\u029c\u028c"+
		"\u0001\u0000\u0000\u0000\u029c\u0295\u0001\u0000\u0000\u0000\u029d=\u0001"+
		"\u0000\u0000\u0000\u029e\u02a7\u0005,\u0000\u0000\u029f\u02a4\u0003(\u0014"+
		"\u0000\u02a0\u02a1\u00050\u0000\u0000\u02a1\u02a3\u0003(\u0014\u0000\u02a2"+
		"\u02a0\u0001\u0000\u0000\u0000\u02a3\u02a6\u0001\u0000\u0000\u0000\u02a4"+
		"\u02a2\u0001\u0000\u0000\u0000\u02a4\u02a5\u0001\u0000\u0000\u0000\u02a5"+
		"\u02a8\u0001\u0000\u0000\u0000\u02a6\u02a4\u0001\u0000\u0000\u0000\u02a7"+
		"\u029f\u0001\u0000\u0000\u0000\u02a7\u02a8\u0001\u0000\u0000\u0000\u02a8"+
		"\u02a9\u0001\u0000\u0000\u0000\u02a9\u02aa\u0005-\u0000\u0000\u02aa?\u0001"+
		"\u0000\u0000\u0000\u02ab\u02b4\u0005*\u0000\u0000\u02ac\u02b1\u0003B!"+
		"\u0000\u02ad\u02ae\u00050\u0000\u0000\u02ae\u02b0\u0003B!\u0000\u02af"+
		"\u02ad\u0001\u0000\u0000\u0000\u02b0\u02b3\u0001\u0000\u0000\u0000\u02b1"+
		"\u02af\u0001\u0000\u0000\u0000\u02b1\u02b2\u0001\u0000\u0000\u0000\u02b2"+
		"\u02b5\u0001\u0000\u0000\u0000\u02b3\u02b1\u0001\u0000\u0000\u0000\u02b4"+
		"\u02ac\u0001\u0000\u0000\u0000\u02b4\u02b5\u0001\u0000\u0000\u0000\u02b5"+
		"\u02b6\u0001\u0000\u0000\u0000\u02b6\u02b7\u0005+\u0000\u0000\u02b7A\u0001"+
		"\u0000\u0000\u0000\u02b8\u02b9\u00055\u0000\u0000\u02b9\u02ba\u00053\u0000"+
		"\u0000\u02ba\u02bb\u0003(\u0014\u0000\u02bbC\u0001\u0000\u0000\u0000\u02bc"+
		"\u02bd\u0007\u0006\u0000\u0000\u02bd\u02c1\u0005.\u0000\u0000\u02be\u02bf"+
		"\u0003(\u0014\u0000\u02bf\u02c0\u00050\u0000\u0000\u02c0\u02c2\u0001\u0000"+
		"\u0000\u0000\u02c1\u02be\u0001\u0000\u0000\u0000\u02c1\u02c2\u0001\u0000"+
		"\u0000\u0000\u02c2\u02c3\u0001\u0000\u0000\u0000\u02c3\u02c4\u0003F#\u0000"+
		"\u02c4\u02c5\u0005/\u0000\u0000\u02c5E\u0001\u0000\u0000\u0000\u02c6\u02cb"+
		"\u0005B\u0000\u0000\u02c7\u02c8\u00054\u0000\u0000\u02c8\u02ca\u0005B"+
		"\u0000\u0000\u02c9\u02c7\u0001\u0000\u0000\u0000\u02ca\u02cd\u0001\u0000"+
		"\u0000\u0000\u02cb\u02c9\u0001\u0000\u0000\u0000\u02cb\u02cc\u0001\u0000"+
		"\u0000\u0000\u02ccG\u0001\u0000\u0000\u0000\u02cd\u02cb\u0001\u0000\u0000"+
		"\u0000\u02ce\u02cf\u0005B\u0000\u0000\u02cf\u02d8\u0005.\u0000\u0000\u02d0"+
		"\u02d5\u0003(\u0014\u0000\u02d1\u02d2\u00050\u0000\u0000\u02d2\u02d4\u0003"+
		"(\u0014\u0000\u02d3\u02d1\u0001\u0000\u0000\u0000\u02d4\u02d7\u0001\u0000"+
		"\u0000\u0000\u02d5\u02d3\u0001\u0000\u0000\u0000\u02d5\u02d6\u0001\u0000"+
		"\u0000\u0000\u02d6\u02d9\u0001\u0000\u0000\u0000\u02d7\u02d5\u0001\u0000"+
		"\u0000\u0000\u02d8\u02d0\u0001\u0000\u0000\u0000\u02d8\u02d9\u0001\u0000"+
		"\u0000\u0000\u02d9\u02da\u0001\u0000\u0000\u0000\u02da\u02db\u0005/\u0000"+
		"\u0000\u02dbI\u0001\u0000\u0000\u0000\u02dc\u02dd\u0003L&\u0000\u02dd"+
		"\u02de\u00052\u0000\u0000\u02de\u02e0\u0001\u0000\u0000\u0000\u02df\u02dc"+
		"\u0001\u0000\u0000\u0000\u02e0\u02e3\u0001\u0000\u0000\u0000\u02e1\u02df"+
		"\u0001\u0000\u0000\u0000\u02e1\u02e2\u0001\u0000\u0000\u0000\u02e2\u02e4"+
		"\u0001\u0000\u0000\u0000\u02e3\u02e1\u0001\u0000\u0000\u0000\u02e4\u02e9"+
		"\u0003N\'\u0000\u02e5\u02e6\u00052\u0000\u0000\u02e6\u02ea\u0003Z-\u0000"+
		"\u02e7\u02e8\u00052\u0000\u0000\u02e8\u02ea\u0003R)\u0000\u02e9\u02e5"+
		"\u0001\u0000\u0000\u0000\u02e9\u02e7\u0001\u0000\u0000\u0000\u02e9\u02ea"+
		"\u0001\u0000\u0000\u0000\u02eaK\u0001\u0000\u0000\u0000\u02eb\u02ed\u0005"+
		"B\u0000\u0000\u02ec\u02ee\u0003\b\u0004\u0000\u02ed\u02ec\u0001\u0000"+
		"\u0000\u0000\u02ed\u02ee\u0001\u0000\u0000\u0000\u02ee\u02f5\u0001\u0000"+
		"\u0000\u0000\u02ef\u02f5\u0003T*\u0000\u02f0\u02f5\u0003\u0010\b\u0000"+
		"\u02f1\u02f5\u0005C\u0000\u0000\u02f2\u02f5\u0005D\u0000\u0000\u02f3\u02f5"+
		"\u0003P(\u0000\u02f4\u02eb\u0001\u0000\u0000\u0000\u02f4\u02ef\u0001\u0000"+
		"\u0000\u0000\u02f4\u02f0\u0001\u0000\u0000\u0000\u02f4\u02f1\u0001\u0000"+
		"\u0000\u0000\u02f4\u02f2\u0001\u0000\u0000\u0000\u02f4\u02f3\u0001\u0000"+
		"\u0000\u0000\u02f5M\u0001\u0000\u0000\u0000\u02f6\u02f8\u0005B\u0000\u0000"+
		"\u02f7\u02f9\u0003\b\u0004\u0000\u02f8\u02f7\u0001\u0000\u0000\u0000\u02f8"+
		"\u02f9\u0001\u0000\u0000\u0000\u02f9\u0300\u0001\u0000\u0000\u0000\u02fa"+
		"\u0300\u0003T*\u0000\u02fb\u0300\u0003\u0010\b\u0000\u02fc\u0300\u0005"+
		"C\u0000\u0000\u02fd\u0300\u0005D\u0000\u0000\u02fe\u0300\u0003P(\u0000"+
		"\u02ff\u02f6\u0001\u0000\u0000\u0000\u02ff\u02fa\u0001\u0000\u0000\u0000"+
		"\u02ff\u02fb\u0001\u0000\u0000\u0000\u02ff\u02fc\u0001\u0000\u0000\u0000"+
		"\u02ff\u02fd\u0001\u0000\u0000\u0000\u02ff\u02fe\u0001\u0000\u0000\u0000"+
		"\u0300O\u0001\u0000\u0000\u0000\u0301\u0302\u0005%\u0000\u0000\u0302\u0303"+
		"\u0005.\u0000\u0000\u0303\u0304\u0003(\u0014\u0000\u0304\u0306\u0005/"+
		"\u0000\u0000\u0305\u0307\u0003\b\u0004\u0000\u0306\u0305\u0001\u0000\u0000"+
		"\u0000\u0306\u0307\u0001\u0000\u0000\u0000\u0307Q\u0001\u0000\u0000\u0000"+
		"\u0308\u030f\u0005\"\u0000\u0000\u0309\u030a\u0005.\u0000\u0000\u030a"+
		"\u030b\u0005%\u0000\u0000\u030b\u030c\u00051\u0000\u0000\u030c\u030d\u0003"+
		"(\u0014\u0000\u030d\u030e\u0005/\u0000\u0000\u030e\u0310\u0001\u0000\u0000"+
		"\u0000\u030f\u0309\u0001\u0000\u0000\u0000\u030f\u0310\u0001\u0000\u0000"+
		"\u0000\u0310S\u0001\u0000\u0000\u0000\u0311\u0316\u0005B\u0000\u0000\u0312"+
		"\u0313\u00054\u0000\u0000\u0313\u0315\u0005B\u0000\u0000\u0314\u0312\u0001"+
		"\u0000\u0000\u0000\u0315\u0318\u0001\u0000\u0000\u0000\u0316\u0314\u0001"+
		"\u0000\u0000\u0000\u0316\u0317\u0001\u0000\u0000\u0000\u0317\u0319\u0001"+
		"\u0000\u0000\u0000\u0318\u0316\u0001\u0000\u0000\u0000\u0319\u031b\u0005"+
		".\u0000\u0000\u031a\u031c\u0003V+\u0000\u031b\u031a\u0001\u0000\u0000"+
		"\u0000\u031b\u031c\u0001\u0000\u0000\u0000\u031c\u031d\u0001\u0000\u0000"+
		"\u0000\u031d\u031e\u0005/\u0000\u0000\u031eU\u0001\u0000\u0000\u0000\u031f"+
		"\u0324\u0003X,\u0000\u0320\u0321\u00050\u0000\u0000\u0321\u0323\u0003"+
		"X,\u0000\u0322\u0320\u0001\u0000\u0000\u0000\u0323\u0326\u0001\u0000\u0000"+
		"\u0000\u0324\u0322\u0001\u0000\u0000\u0000\u0324\u0325\u0001\u0000\u0000"+
		"\u0000\u0325\u0330\u0001\u0000\u0000\u0000\u0326\u0324\u0001\u0000\u0000"+
		"\u0000\u0327\u032c\u0003(\u0014\u0000\u0328\u0329\u00050\u0000\u0000\u0329"+
		"\u032b\u0003(\u0014\u0000\u032a\u0328\u0001\u0000\u0000\u0000\u032b\u032e"+
		"\u0001\u0000\u0000\u0000\u032c\u032a\u0001\u0000\u0000\u0000\u032c\u032d"+
		"\u0001\u0000\u0000\u0000\u032d\u0330\u0001\u0000\u0000\u0000\u032e\u032c"+
		"\u0001\u0000\u0000\u0000\u032f\u031f\u0001\u0000\u0000\u0000\u032f\u0327"+
		"\u0001\u0000\u0000\u0000\u0330W\u0001\u0000\u0000\u0000\u0331\u0332\u0005"+
		"B\u0000\u0000\u0332\u0333\u00051\u0000\u0000\u0333\u0334\u0003(\u0014"+
		"\u0000\u0334Y\u0001\u0000\u0000\u0000\u0335\u0336\u0005\u0017\u0000\u0000"+
		"\u0336\u033a\u0005.\u0000\u0000\u0337\u0338\u0005B\u0000\u0000\u0338\u0339"+
		"\u00053\u0000\u0000\u0339\u033b\u0003(\u0014\u0000\u033a\u0337\u0001\u0000"+
		"\u0000\u0000\u033a\u033b\u0001\u0000\u0000\u0000\u033b\u033c\u0001\u0000"+
		"\u0000\u0000\u033c\u0345\u0005/\u0000\u0000\u033d\u033e\u0005\u0018\u0000"+
		"\u0000\u033e\u033f\u0005.\u0000\u0000\u033f\u0340\u0005B\u0000\u0000\u0340"+
		"\u0341\u00053\u0000\u0000\u0341\u0342\u0003(\u0014\u0000\u0342\u0343\u0005"+
		"/\u0000\u0000\u0343\u0345\u0001\u0000\u0000\u0000\u0344\u0335\u0001\u0000"+
		"\u0000\u0000\u0344\u033d\u0001\u0000\u0000\u0000\u0345[\u0001\u0000\u0000"+
		"\u0000\u0346\u0355\u0005?\u0000\u0000\u0347\u0355\u00055\u0000\u0000\u0348"+
		"\u0355\u00058\u0000\u0000\u0349\u0355\u0007\u0007\u0000\u0000\u034a\u0355"+
		"\u0005@\u0000\u0000\u034b\u0355\u0005A\u0000\u0000\u034c\u0355\u0007\b"+
		"\u0000\u0000\u034d\u0355\u0005\u0016\u0000\u0000\u034e\u0355\u0005;\u0000"+
		"\u0000\u034f\u0355\u0005<\u0000\u0000\u0350\u0355\u0005=\u0000\u0000\u0351"+
		"\u0355\u0005>\u0000\u0000\u0352\u0355\u00059\u0000\u0000\u0353\u0355\u0005"+
		":\u0000\u0000\u0354\u0346\u0001\u0000\u0000\u0000\u0354\u0347\u0001\u0000"+
		"\u0000\u0000\u0354\u0348\u0001\u0000\u0000\u0000\u0354\u0349\u0001\u0000"+
		"\u0000\u0000\u0354\u034a\u0001\u0000\u0000\u0000\u0354\u034b\u0001\u0000"+
		"\u0000\u0000\u0354\u034c\u0001\u0000\u0000\u0000\u0354\u034d\u0001\u0000"+
		"\u0000\u0000\u0354\u034e\u0001\u0000\u0000\u0000\u0354\u034f\u0001\u0000"+
		"\u0000\u0000\u0354\u0350\u0001\u0000\u0000\u0000\u0354\u0351\u0001\u0000"+
		"\u0000\u0000\u0354\u0352\u0001\u0000\u0000\u0000\u0354\u0353\u0001\u0000"+
		"\u0000\u0000\u0355]\u0001\u0000\u0000\u0000Ufmqw\u0082\u008a\u0090\u0093"+
		"\u009c\u009f\u00ab\u00af\u00b5\u00bc\u00c6\u00d2\u00d8\u00e4\u00f1\u00fe"+
		"\u0118\u0137\u0147\u0153\u0163\u0168\u0178\u0180\u0183\u018d\u0191\u0194"+
		"\u0199\u019c\u01a7\u01b1\u01c0\u01c5\u01cd\u01d7\u01e0\u01e7\u01ee\u01f7"+
		"\u0203\u0208\u0215\u021d\u0223\u0231\u0245\u024f\u025a\u026b\u0274\u027c"+
		"\u0282\u0284\u0292\u0299\u029c\u02a4\u02a7\u02b1\u02b4\u02c1\u02cb\u02d5"+
		"\u02d8\u02e1\u02e9\u02ed\u02f4\u02f8\u02ff\u0306\u030f\u0316\u031b\u0324"+
		"\u032c\u032f\u033a\u0344\u0354";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}