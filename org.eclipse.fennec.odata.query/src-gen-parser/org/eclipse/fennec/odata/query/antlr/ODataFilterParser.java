// Generated from /opt/git/fennec-odata/fennec-odata/org.eclipse.fennec.odata.query/grammar/ODataFilter.g4 by ANTLR 4.13.2
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
		ADD=12, SUB=13, MUL=14, DIV=15, MOD=16, ASC=17, DESC=18, TRUE=19, FALSE=20, 
		NULL=21, ANY=22, ALL=23, CAST=24, ISOF=25, COUNT=26, VALUE=27, REF=28, 
		WITH=29, AS=30, LPAREN=31, RPAREN=32, COMMA=33, SLASH=34, COLON=35, DOT=36, 
		DURATION=37, ENUM=38, GUID=39, DATETIMEOFFSET=40, DATE=41, TIMEOFDAY=42, 
		STRING=43, DECIMAL=44, INT=45, IDENT=46, WS=47;
	public static final int
		RULE_filter = 0, RULE_orderby = 1, RULE_orderbyItem = 2, RULE_resource = 3, 
		RULE_keyPredicate = 4, RULE_keyLiteral = 5, RULE_resourceSegment = 6, 
		RULE_apply = 7, RULE_applyTrafo = 8, RULE_aggregateItem = 9, RULE_computeItem = 10, 
		RULE_expr = 11, RULE_orExpr = 12, RULE_andExpr = 13, RULE_notExpr = 14, 
		RULE_comparison = 15, RULE_additive = 16, RULE_multiplicative = 17, RULE_primary = 18, 
		RULE_typeFunc = 19, RULE_qualifiedTypeName = 20, RULE_functionCall = 21, 
		RULE_memberPath = 22, RULE_lambdaCall = 23, RULE_literal = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"filter", "orderby", "orderbyItem", "resource", "keyPredicate", "keyLiteral", 
			"resourceSegment", "apply", "applyTrafo", "aggregateItem", "computeItem", 
			"expr", "orExpr", "andExpr", "notExpr", "comparison", "additive", "multiplicative", 
			"primary", "typeFunc", "qualifiedTypeName", "functionCall", "memberPath", 
			"lambdaCall", "literal"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, "'true'", "'false'", "'null'", 
			"'any'", "'all'", null, null, "'$count'", "'$value'", "'$ref'", "'with'", 
			"'as'", "'('", "')'", "','", "'/'", "':'", "'.'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "OR", "AND", "NOT", "EQ", "NE", "GT", "GE", "LT", "LE", "HAS", 
			"IN", "ADD", "SUB", "MUL", "DIV", "MOD", "ASC", "DESC", "TRUE", "FALSE", 
			"NULL", "ANY", "ALL", "CAST", "ISOF", "COUNT", "VALUE", "REF", "WITH", 
			"AS", "LPAREN", "RPAREN", "COMMA", "SLASH", "COLON", "DOT", "DURATION", 
			"ENUM", "GUID", "DATETIMEOFFSET", "DATE", "TIMEOFDAY", "STRING", "DECIMAL", 
			"INT", "IDENT", "WS"
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
			setState(50);
			expr();
			setState(51);
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
			setState(53);
			orderbyItem();
			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(54);
				match(COMMA);
				setState(55);
				orderbyItem();
				}
				}
				setState(60);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(61);
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
			setState(63);
			expr();
			setState(65);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ASC || _la==DESC) {
				{
				setState(64);
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
			setState(67);
			match(IDENT);
			setState(69);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(68);
				keyPredicate();
				}
			}

			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SLASH) {
				{
				{
				setState(71);
				match(SLASH);
				setState(72);
				resourceSegment();
				}
				}
				setState(77);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(78);
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
		public KeyLiteralContext keyLiteral() {
			return getRuleContext(KeyLiteralContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ODataFilterParser.RPAREN, 0); }
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			match(LPAREN);
			setState(81);
			keyLiteral();
			setState(82);
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
		enterRule(_localctx, 10, RULE_keyLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(84);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 69818988363776L) != 0)) ) {
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
		enterRule(_localctx, 12, RULE_resourceSegment);
		int _la;
		try {
			setState(93);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENT:
				_localctx = new PropertySegmentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(86);
				match(IDENT);
				setState(88);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LPAREN) {
					{
					setState(87);
					keyPredicate();
					}
				}

				}
				break;
			case COUNT:
				_localctx = new CountSegmentContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(90);
				match(COUNT);
				}
				break;
			case VALUE:
				_localctx = new ValueSegmentContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(91);
				match(VALUE);
				}
				break;
			case REF:
				_localctx = new RefSegmentContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(92);
				match(REF);
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
	public static class ApplyContext extends ParserRuleContext {
		public List<ApplyTrafoContext> applyTrafo() {
			return getRuleContexts(ApplyTrafoContext.class);
		}
		public ApplyTrafoContext applyTrafo(int i) {
			return getRuleContext(ApplyTrafoContext.class,i);
		}
		public TerminalNode EOF() { return getToken(ODataFilterParser.EOF, 0); }
		public List<TerminalNode> SLASH() { return getTokens(ODataFilterParser.SLASH); }
		public TerminalNode SLASH(int i) {
			return getToken(ODataFilterParser.SLASH, i);
		}
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
		enterRule(_localctx, 14, RULE_apply);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(95);
			applyTrafo();
			setState(100);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SLASH) {
				{
				{
				setState(96);
				match(SLASH);
				setState(97);
				applyTrafo();
				}
				}
				setState(102);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(103);
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
		public List<MemberPathContext> memberPath() {
			return getRuleContexts(MemberPathContext.class);
		}
		public MemberPathContext memberPath(int i) {
			return getRuleContext(MemberPathContext.class,i);
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
		public ApplyTrafoContext applyTrafo() {
			return getRuleContext(ApplyTrafoContext.class,0);
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
		enterRule(_localctx, 16, RULE_applyTrafo);
		int _la;
		try {
			setState(152);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				_localctx = new GroupByTrafoContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(105);
				((GroupByTrafoContext)_localctx).name = match(IDENT);
				setState(106);
				match(LPAREN);
				setState(107);
				match(LPAREN);
				setState(108);
				memberPath();
				setState(113);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(109);
					match(COMMA);
					setState(110);
					memberPath();
					}
					}
					setState(115);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(116);
				match(RPAREN);
				setState(119);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==COMMA) {
					{
					setState(117);
					match(COMMA);
					setState(118);
					applyTrafo();
					}
				}

				setState(121);
				match(RPAREN);
				}
				break;
			case 2:
				_localctx = new AggregateTrafoContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(123);
				((AggregateTrafoContext)_localctx).name = match(IDENT);
				setState(124);
				match(LPAREN);
				setState(125);
				aggregateItem();
				setState(130);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(126);
					match(COMMA);
					setState(127);
					aggregateItem();
					}
					}
					setState(132);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(133);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new ComputeTrafoContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(135);
				((ComputeTrafoContext)_localctx).name = match(IDENT);
				setState(136);
				match(LPAREN);
				setState(137);
				computeItem();
				setState(142);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(138);
					match(COMMA);
					setState(139);
					computeItem();
					}
					}
					setState(144);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(145);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new FilterTrafoContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(147);
				((FilterTrafoContext)_localctx).name = match(IDENT);
				setState(148);
				match(LPAREN);
				setState(149);
				expr();
				setState(150);
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
		public Token method;
		public Token alias;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode WITH() { return getToken(ODataFilterParser.WITH, 0); }
		public TerminalNode AS() { return getToken(ODataFilterParser.AS, 0); }
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
		}
		public AggregateWithItemContext(AggregateItemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAggregateWithItem(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AggregateCountItemContext extends AggregateItemContext {
		public Token alias;
		public TerminalNode COUNT() { return getToken(ODataFilterParser.COUNT, 0); }
		public TerminalNode AS() { return getToken(ODataFilterParser.AS, 0); }
		public TerminalNode IDENT() { return getToken(ODataFilterParser.IDENT, 0); }
		public AggregateCountItemContext(AggregateItemContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ODataFilterVisitor ) return ((ODataFilterVisitor<? extends T>)visitor).visitAggregateCountItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AggregateItemContext aggregateItem() throws RecognitionException {
		AggregateItemContext _localctx = new AggregateItemContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_aggregateItem);
		try {
			setState(163);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
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
				_localctx = new AggregateWithItemContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(154);
				expr();
				setState(155);
				match(WITH);
				setState(156);
				((AggregateWithItemContext)_localctx).method = match(IDENT);
				setState(157);
				match(AS);
				setState(158);
				((AggregateWithItemContext)_localctx).alias = match(IDENT);
				}
				break;
			case COUNT:
				_localctx = new AggregateCountItemContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(160);
				match(COUNT);
				setState(161);
				match(AS);
				setState(162);
				((AggregateCountItemContext)_localctx).alias = match(IDENT);
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
		enterRule(_localctx, 20, RULE_computeItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			expr();
			setState(166);
			match(AS);
			setState(167);
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
		enterRule(_localctx, 22, RULE_expr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
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
		enterRule(_localctx, 24, RULE_orExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(171);
			andExpr();
			setState(176);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==OR) {
				{
				{
				setState(172);
				match(OR);
				setState(173);
				andExpr();
				}
				}
				setState(178);
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
		enterRule(_localctx, 26, RULE_andExpr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			notExpr();
			setState(184);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(180);
				match(AND);
				setState(181);
				notExpr();
				}
				}
				setState(186);
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
		enterRule(_localctx, 28, RULE_notExpr);
		try {
			setState(190);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				_localctx = new NotExpressionContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(187);
				match(NOT);
				setState(188);
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
				_localctx = new ComparisonLevelContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(189);
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
		enterRule(_localctx, 30, RULE_comparison);
		int _la;
		try {
			setState(220);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				_localctx = new BinaryComparisonContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(192);
				additive(0);
				setState(193);
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
				setState(194);
				additive(0);
				}
				break;
			case 2:
				_localctx = new InListComparisonContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(196);
				additive(0);
				setState(197);
				match(IN);
				setState(198);
				match(LPAREN);
				setState(199);
				literal();
				setState(202); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(200);
					match(COMMA);
					setState(201);
					literal();
					}
					}
					setState(204); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==COMMA );
				setState(206);
				match(RPAREN);
				}
				break;
			case 3:
				_localctx = new InEmptyListComparisonContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(208);
				additive(0);
				setState(209);
				match(IN);
				setState(210);
				match(LPAREN);
				setState(211);
				match(RPAREN);
				}
				break;
			case 4:
				_localctx = new InComparisonContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(213);
				additive(0);
				setState(214);
				match(IN);
				setState(215);
				match(LPAREN);
				setState(216);
				expr();
				setState(217);
				match(RPAREN);
				}
				break;
			case 5:
				_localctx = new PassThroughContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(219);
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
		int _startState = 32;
		enterRecursionRule(_localctx, 32, RULE_additive, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new ToMultiplicativeContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(223);
			multiplicative(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(230);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new AddSubContext(new AdditiveContext(_parentctx, _parentState));
					pushNewRecursionContext(_localctx, _startState, RULE_additive);
					setState(225);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(226);
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
					setState(227);
					multiplicative(0);
					}
					} 
				}
				setState(232);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
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
		int _startState = 34;
		enterRecursionRule(_localctx, 34, RULE_multiplicative, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			_localctx = new ToPrimaryContext(_localctx);
			_ctx = _localctx;
			_prevctx = _localctx;

			setState(234);
			primary();
			}
			_ctx.stop = _input.LT(-1);
			setState(241);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new MulDivModContext(new MultiplicativeContext(_parentctx, _parentState));
					pushNewRecursionContext(_localctx, _startState, RULE_multiplicative);
					setState(236);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(237);
					((MulDivModContext)_localctx).op = _input.LT(1);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 114688L) != 0)) ) {
						((MulDivModContext)_localctx).op = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(238);
					primary();
					}
					} 
				}
				setState(243);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
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
		enterRule(_localctx, 36, RULE_primary);
		try {
			setState(252);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				_localctx = new LiteralPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(244);
				literal();
				}
				break;
			case 2:
				_localctx = new TypeFuncPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(245);
				typeFunc();
				}
				break;
			case 3:
				_localctx = new FunctionPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(246);
				functionCall();
				}
				break;
			case 4:
				_localctx = new MemberPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(247);
				memberPath();
				}
				break;
			case 5:
				_localctx = new ParenPrimaryContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(248);
				match(LPAREN);
				setState(249);
				expr();
				setState(250);
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
		enterRule(_localctx, 38, RULE_typeFunc);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(254);
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
			setState(255);
			match(LPAREN);
			setState(259);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				{
				setState(256);
				expr();
				setState(257);
				match(COMMA);
				}
				break;
			}
			setState(261);
			qualifiedTypeName();
			setState(262);
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
		enterRule(_localctx, 40, RULE_qualifiedTypeName);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(264);
			match(IDENT);
			setState(269);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(265);
				match(DOT);
				setState(266);
				match(IDENT);
				}
				}
				setState(271);
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
		enterRule(_localctx, 42, RULE_functionCall);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(272);
			match(IDENT);
			setState(273);
			match(LPAREN);
			setState(282);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 140602250887176L) != 0)) {
				{
				setState(274);
				expr();
				setState(279);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(275);
					match(COMMA);
					setState(276);
					expr();
					}
					}
					setState(281);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(284);
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
		public List<TerminalNode> IDENT() { return getTokens(ODataFilterParser.IDENT); }
		public TerminalNode IDENT(int i) {
			return getToken(ODataFilterParser.IDENT, i);
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
		enterRule(_localctx, 44, RULE_memberPath);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			match(IDENT);
			setState(291);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(287);
					match(SLASH);
					setState(288);
					match(IDENT);
					}
					} 
				}
				setState(293);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			}
			setState(298);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				{
				setState(294);
				match(SLASH);
				setState(295);
				lambdaCall();
				}
				break;
			case 2:
				{
				setState(296);
				match(SLASH);
				setState(297);
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
		enterRule(_localctx, 46, RULE_lambdaCall);
		int _la;
		try {
			setState(315);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ANY:
				enterOuterAlt(_localctx, 1);
				{
				setState(300);
				((LambdaCallContext)_localctx).op = match(ANY);
				setState(301);
				match(LPAREN);
				setState(305);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==IDENT) {
					{
					setState(302);
					match(IDENT);
					setState(303);
					match(COLON);
					setState(304);
					expr();
					}
				}

				setState(307);
				match(RPAREN);
				}
				break;
			case ALL:
				enterOuterAlt(_localctx, 2);
				{
				setState(308);
				((LambdaCallContext)_localctx).op = match(ALL);
				setState(309);
				match(LPAREN);
				setState(310);
				match(IDENT);
				setState(311);
				match(COLON);
				setState(312);
				expr();
				setState(313);
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
		enterRule(_localctx, 48, RULE_literal);
		int _la;
		try {
			setState(328);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case STRING:
				_localctx = new StringLiteralContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(317);
				match(STRING);
				}
				break;
			case DECIMAL:
				_localctx = new DecimalLiteralContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(318);
				match(DECIMAL);
				}
				break;
			case INT:
				_localctx = new IntLiteralContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(319);
				match(INT);
				}
				break;
			case TRUE:
			case FALSE:
				_localctx = new BooleanLiteralContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(320);
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
				setState(321);
				match(NULL);
				}
				break;
			case GUID:
				_localctx = new GuidLiteralContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(322);
				match(GUID);
				}
				break;
			case DATETIMEOFFSET:
				_localctx = new DateTimeOffsetLiteralContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(323);
				match(DATETIMEOFFSET);
				}
				break;
			case DATE:
				_localctx = new DateLiteralContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(324);
				match(DATE);
				}
				break;
			case TIMEOFDAY:
				_localctx = new TimeOfDayLiteralContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(325);
				match(TIMEOFDAY);
				}
				break;
			case DURATION:
				_localctx = new DurationLiteralContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(326);
				match(DURATION);
				}
				break;
			case ENUM:
				_localctx = new EnumLiteralContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(327);
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
		case 16:
			return additive_sempred((AdditiveContext)_localctx, predIndex);
		case 17:
			return multiplicative_sempred((MultiplicativeContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean additive_sempred(AdditiveContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean multiplicative_sempred(MultiplicativeContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001/\u014b\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0005\u00019\b\u0001\n\u0001\f\u0001<\t\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0002\u0001\u0002\u0003\u0002B\b\u0002\u0001\u0003\u0001\u0003"+
		"\u0003\u0003F\b\u0003\u0001\u0003\u0001\u0003\u0005\u0003J\b\u0003\n\u0003"+
		"\f\u0003M\t\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0003"+
		"\u0006Y\b\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006^\b\u0006"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0005\u0007c\b\u0007\n\u0007\f\u0007"+
		"f\t\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0005\bp\b\b\n\b\f\bs\t\b\u0001\b\u0001\b\u0001\b\u0003\bx"+
		"\b\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0005\b\u0081"+
		"\b\b\n\b\f\b\u0084\t\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b"+
		"\u0001\b\u0005\b\u008d\b\b\n\b\f\b\u0090\t\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0003\b\u0099\b\b\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u00a4\b\t\u0001\n\u0001"+
		"\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0005"+
		"\f\u00af\b\f\n\f\f\f\u00b2\t\f\u0001\r\u0001\r\u0001\r\u0005\r\u00b7\b"+
		"\r\n\r\f\r\u00ba\t\r\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e\u00bf"+
		"\b\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0004\u000f\u00cb"+
		"\b\u000f\u000b\u000f\f\u000f\u00cc\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f"+
		"\u00dd\b\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0005\u0010\u00e5\b\u0010\n\u0010\f\u0010\u00e8\t\u0010\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0005"+
		"\u0011\u00f0\b\u0011\n\u0011\f\u0011\u00f3\t\u0011\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012"+
		"\u0003\u0012\u00fd\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0003\u0013\u0104\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u010c\b\u0014\n\u0014"+
		"\f\u0014\u010f\t\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0005\u0015\u0116\b\u0015\n\u0015\f\u0015\u0119\t\u0015\u0003"+
		"\u0015\u011b\b\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001"+
		"\u0016\u0005\u0016\u0122\b\u0016\n\u0016\f\u0016\u0125\t\u0016\u0001\u0016"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u012b\b\u0016\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u0132\b\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0003\u0017\u013c\b\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0003\u0018\u0149\b\u0018\u0001\u0018"+
		"\u0000\u0002 \"\u0019\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012"+
		"\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.0\u0000\u0007\u0001\u0000"+
		"\u0011\u0012\u0001\u0000\'-\u0001\u0000\u0004\n\u0001\u0000\f\r\u0001"+
		"\u0000\u000e\u0010\u0001\u0000\u0018\u0019\u0001\u0000\u0013\u0014\u0163"+
		"\u00002\u0001\u0000\u0000\u0000\u00025\u0001\u0000\u0000\u0000\u0004?"+
		"\u0001\u0000\u0000\u0000\u0006C\u0001\u0000\u0000\u0000\bP\u0001\u0000"+
		"\u0000\u0000\nT\u0001\u0000\u0000\u0000\f]\u0001\u0000\u0000\u0000\u000e"+
		"_\u0001\u0000\u0000\u0000\u0010\u0098\u0001\u0000\u0000\u0000\u0012\u00a3"+
		"\u0001\u0000\u0000\u0000\u0014\u00a5\u0001\u0000\u0000\u0000\u0016\u00a9"+
		"\u0001\u0000\u0000\u0000\u0018\u00ab\u0001\u0000\u0000\u0000\u001a\u00b3"+
		"\u0001\u0000\u0000\u0000\u001c\u00be\u0001\u0000\u0000\u0000\u001e\u00dc"+
		"\u0001\u0000\u0000\u0000 \u00de\u0001\u0000\u0000\u0000\"\u00e9\u0001"+
		"\u0000\u0000\u0000$\u00fc\u0001\u0000\u0000\u0000&\u00fe\u0001\u0000\u0000"+
		"\u0000(\u0108\u0001\u0000\u0000\u0000*\u0110\u0001\u0000\u0000\u0000,"+
		"\u011e\u0001\u0000\u0000\u0000.\u013b\u0001\u0000\u0000\u00000\u0148\u0001"+
		"\u0000\u0000\u000023\u0003\u0016\u000b\u000034\u0005\u0000\u0000\u0001"+
		"4\u0001\u0001\u0000\u0000\u00005:\u0003\u0004\u0002\u000067\u0005!\u0000"+
		"\u000079\u0003\u0004\u0002\u000086\u0001\u0000\u0000\u00009<\u0001\u0000"+
		"\u0000\u0000:8\u0001\u0000\u0000\u0000:;\u0001\u0000\u0000\u0000;=\u0001"+
		"\u0000\u0000\u0000<:\u0001\u0000\u0000\u0000=>\u0005\u0000\u0000\u0001"+
		">\u0003\u0001\u0000\u0000\u0000?A\u0003\u0016\u000b\u0000@B\u0007\u0000"+
		"\u0000\u0000A@\u0001\u0000\u0000\u0000AB\u0001\u0000\u0000\u0000B\u0005"+
		"\u0001\u0000\u0000\u0000CE\u0005.\u0000\u0000DF\u0003\b\u0004\u0000ED"+
		"\u0001\u0000\u0000\u0000EF\u0001\u0000\u0000\u0000FK\u0001\u0000\u0000"+
		"\u0000GH\u0005\"\u0000\u0000HJ\u0003\f\u0006\u0000IG\u0001\u0000\u0000"+
		"\u0000JM\u0001\u0000\u0000\u0000KI\u0001\u0000\u0000\u0000KL\u0001\u0000"+
		"\u0000\u0000LN\u0001\u0000\u0000\u0000MK\u0001\u0000\u0000\u0000NO\u0005"+
		"\u0000\u0000\u0001O\u0007\u0001\u0000\u0000\u0000PQ\u0005\u001f\u0000"+
		"\u0000QR\u0003\n\u0005\u0000RS\u0005 \u0000\u0000S\t\u0001\u0000\u0000"+
		"\u0000TU\u0007\u0001\u0000\u0000U\u000b\u0001\u0000\u0000\u0000VX\u0005"+
		".\u0000\u0000WY\u0003\b\u0004\u0000XW\u0001\u0000\u0000\u0000XY\u0001"+
		"\u0000\u0000\u0000Y^\u0001\u0000\u0000\u0000Z^\u0005\u001a\u0000\u0000"+
		"[^\u0005\u001b\u0000\u0000\\^\u0005\u001c\u0000\u0000]V\u0001\u0000\u0000"+
		"\u0000]Z\u0001\u0000\u0000\u0000][\u0001\u0000\u0000\u0000]\\\u0001\u0000"+
		"\u0000\u0000^\r\u0001\u0000\u0000\u0000_d\u0003\u0010\b\u0000`a\u0005"+
		"\"\u0000\u0000ac\u0003\u0010\b\u0000b`\u0001\u0000\u0000\u0000cf\u0001"+
		"\u0000\u0000\u0000db\u0001\u0000\u0000\u0000de\u0001\u0000\u0000\u0000"+
		"eg\u0001\u0000\u0000\u0000fd\u0001\u0000\u0000\u0000gh\u0005\u0000\u0000"+
		"\u0001h\u000f\u0001\u0000\u0000\u0000ij\u0005.\u0000\u0000jk\u0005\u001f"+
		"\u0000\u0000kl\u0005\u001f\u0000\u0000lq\u0003,\u0016\u0000mn\u0005!\u0000"+
		"\u0000np\u0003,\u0016\u0000om\u0001\u0000\u0000\u0000ps\u0001\u0000\u0000"+
		"\u0000qo\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000\u0000rt\u0001\u0000"+
		"\u0000\u0000sq\u0001\u0000\u0000\u0000tw\u0005 \u0000\u0000uv\u0005!\u0000"+
		"\u0000vx\u0003\u0010\b\u0000wu\u0001\u0000\u0000\u0000wx\u0001\u0000\u0000"+
		"\u0000xy\u0001\u0000\u0000\u0000yz\u0005 \u0000\u0000z\u0099\u0001\u0000"+
		"\u0000\u0000{|\u0005.\u0000\u0000|}\u0005\u001f\u0000\u0000}\u0082\u0003"+
		"\u0012\t\u0000~\u007f\u0005!\u0000\u0000\u007f\u0081\u0003\u0012\t\u0000"+
		"\u0080~\u0001\u0000\u0000\u0000\u0081\u0084\u0001\u0000\u0000\u0000\u0082"+
		"\u0080\u0001\u0000\u0000\u0000\u0082\u0083\u0001\u0000\u0000\u0000\u0083"+
		"\u0085\u0001\u0000\u0000\u0000\u0084\u0082\u0001\u0000\u0000\u0000\u0085"+
		"\u0086\u0005 \u0000\u0000\u0086\u0099\u0001\u0000\u0000\u0000\u0087\u0088"+
		"\u0005.\u0000\u0000\u0088\u0089\u0005\u001f\u0000\u0000\u0089\u008e\u0003"+
		"\u0014\n\u0000\u008a\u008b\u0005!\u0000\u0000\u008b\u008d\u0003\u0014"+
		"\n\u0000\u008c\u008a\u0001\u0000\u0000\u0000\u008d\u0090\u0001\u0000\u0000"+
		"\u0000\u008e\u008c\u0001\u0000\u0000\u0000\u008e\u008f\u0001\u0000\u0000"+
		"\u0000\u008f\u0091\u0001\u0000\u0000\u0000\u0090\u008e\u0001\u0000\u0000"+
		"\u0000\u0091\u0092\u0005 \u0000\u0000\u0092\u0099\u0001\u0000\u0000\u0000"+
		"\u0093\u0094\u0005.\u0000\u0000\u0094\u0095\u0005\u001f\u0000\u0000\u0095"+
		"\u0096\u0003\u0016\u000b\u0000\u0096\u0097\u0005 \u0000\u0000\u0097\u0099"+
		"\u0001\u0000\u0000\u0000\u0098i\u0001\u0000\u0000\u0000\u0098{\u0001\u0000"+
		"\u0000\u0000\u0098\u0087\u0001\u0000\u0000\u0000\u0098\u0093\u0001\u0000"+
		"\u0000\u0000\u0099\u0011\u0001\u0000\u0000\u0000\u009a\u009b\u0003\u0016"+
		"\u000b\u0000\u009b\u009c\u0005\u001d\u0000\u0000\u009c\u009d\u0005.\u0000"+
		"\u0000\u009d\u009e\u0005\u001e\u0000\u0000\u009e\u009f\u0005.\u0000\u0000"+
		"\u009f\u00a4\u0001\u0000\u0000\u0000\u00a0\u00a1\u0005\u001a\u0000\u0000"+
		"\u00a1\u00a2\u0005\u001e\u0000\u0000\u00a2\u00a4\u0005.\u0000\u0000\u00a3"+
		"\u009a\u0001\u0000\u0000\u0000\u00a3\u00a0\u0001\u0000\u0000\u0000\u00a4"+
		"\u0013\u0001\u0000\u0000\u0000\u00a5\u00a6\u0003\u0016\u000b\u0000\u00a6"+
		"\u00a7\u0005\u001e\u0000\u0000\u00a7\u00a8\u0005.\u0000\u0000\u00a8\u0015"+
		"\u0001\u0000\u0000\u0000\u00a9\u00aa\u0003\u0018\f\u0000\u00aa\u0017\u0001"+
		"\u0000\u0000\u0000\u00ab\u00b0\u0003\u001a\r\u0000\u00ac\u00ad\u0005\u0001"+
		"\u0000\u0000\u00ad\u00af\u0003\u001a\r\u0000\u00ae\u00ac\u0001\u0000\u0000"+
		"\u0000\u00af\u00b2\u0001\u0000\u0000\u0000\u00b0\u00ae\u0001\u0000\u0000"+
		"\u0000\u00b0\u00b1\u0001\u0000\u0000\u0000\u00b1\u0019\u0001\u0000\u0000"+
		"\u0000\u00b2\u00b0\u0001\u0000\u0000\u0000\u00b3\u00b8\u0003\u001c\u000e"+
		"\u0000\u00b4\u00b5\u0005\u0002\u0000\u0000\u00b5\u00b7\u0003\u001c\u000e"+
		"\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b7\u00ba\u0001\u0000\u0000"+
		"\u0000\u00b8\u00b6\u0001\u0000\u0000\u0000\u00b8\u00b9\u0001\u0000\u0000"+
		"\u0000\u00b9\u001b\u0001\u0000\u0000\u0000\u00ba\u00b8\u0001\u0000\u0000"+
		"\u0000\u00bb\u00bc\u0005\u0003\u0000\u0000\u00bc\u00bf\u0003\u001c\u000e"+
		"\u0000\u00bd\u00bf\u0003\u001e\u000f\u0000\u00be\u00bb\u0001\u0000\u0000"+
		"\u0000\u00be\u00bd\u0001\u0000\u0000\u0000\u00bf\u001d\u0001\u0000\u0000"+
		"\u0000\u00c0\u00c1\u0003 \u0010\u0000\u00c1\u00c2\u0007\u0002\u0000\u0000"+
		"\u00c2\u00c3\u0003 \u0010\u0000\u00c3\u00dd\u0001\u0000\u0000\u0000\u00c4"+
		"\u00c5\u0003 \u0010\u0000\u00c5\u00c6\u0005\u000b\u0000\u0000\u00c6\u00c7"+
		"\u0005\u001f\u0000\u0000\u00c7\u00ca\u00030\u0018\u0000\u00c8\u00c9\u0005"+
		"!\u0000\u0000\u00c9\u00cb\u00030\u0018\u0000\u00ca\u00c8\u0001\u0000\u0000"+
		"\u0000\u00cb\u00cc\u0001\u0000\u0000\u0000\u00cc\u00ca\u0001\u0000\u0000"+
		"\u0000\u00cc\u00cd\u0001\u0000\u0000\u0000\u00cd\u00ce\u0001\u0000\u0000"+
		"\u0000\u00ce\u00cf\u0005 \u0000\u0000\u00cf\u00dd\u0001\u0000\u0000\u0000"+
		"\u00d0\u00d1\u0003 \u0010\u0000\u00d1\u00d2\u0005\u000b\u0000\u0000\u00d2"+
		"\u00d3\u0005\u001f\u0000\u0000\u00d3\u00d4\u0005 \u0000\u0000\u00d4\u00dd"+
		"\u0001\u0000\u0000\u0000\u00d5\u00d6\u0003 \u0010\u0000\u00d6\u00d7\u0005"+
		"\u000b\u0000\u0000\u00d7\u00d8\u0005\u001f\u0000\u0000\u00d8\u00d9\u0003"+
		"\u0016\u000b\u0000\u00d9\u00da\u0005 \u0000\u0000\u00da\u00dd\u0001\u0000"+
		"\u0000\u0000\u00db\u00dd\u0003 \u0010\u0000\u00dc\u00c0\u0001\u0000\u0000"+
		"\u0000\u00dc\u00c4\u0001\u0000\u0000\u0000\u00dc\u00d0\u0001\u0000\u0000"+
		"\u0000\u00dc\u00d5\u0001\u0000\u0000\u0000\u00dc\u00db\u0001\u0000\u0000"+
		"\u0000\u00dd\u001f\u0001\u0000\u0000\u0000\u00de\u00df\u0006\u0010\uffff"+
		"\uffff\u0000\u00df\u00e0\u0003\"\u0011\u0000\u00e0\u00e6\u0001\u0000\u0000"+
		"\u0000\u00e1\u00e2\n\u0002\u0000\u0000\u00e2\u00e3\u0007\u0003\u0000\u0000"+
		"\u00e3\u00e5\u0003\"\u0011\u0000\u00e4\u00e1\u0001\u0000\u0000\u0000\u00e5"+
		"\u00e8\u0001\u0000\u0000\u0000\u00e6\u00e4\u0001\u0000\u0000\u0000\u00e6"+
		"\u00e7\u0001\u0000\u0000\u0000\u00e7!\u0001\u0000\u0000\u0000\u00e8\u00e6"+
		"\u0001\u0000\u0000\u0000\u00e9\u00ea\u0006\u0011\uffff\uffff\u0000\u00ea"+
		"\u00eb\u0003$\u0012\u0000\u00eb\u00f1\u0001\u0000\u0000\u0000\u00ec\u00ed"+
		"\n\u0002\u0000\u0000\u00ed\u00ee\u0007\u0004\u0000\u0000\u00ee\u00f0\u0003"+
		"$\u0012\u0000\u00ef\u00ec\u0001\u0000\u0000\u0000\u00f0\u00f3\u0001\u0000"+
		"\u0000\u0000\u00f1\u00ef\u0001\u0000\u0000\u0000\u00f1\u00f2\u0001\u0000"+
		"\u0000\u0000\u00f2#\u0001\u0000\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000"+
		"\u0000\u00f4\u00fd\u00030\u0018\u0000\u00f5\u00fd\u0003&\u0013\u0000\u00f6"+
		"\u00fd\u0003*\u0015\u0000\u00f7\u00fd\u0003,\u0016\u0000\u00f8\u00f9\u0005"+
		"\u001f\u0000\u0000\u00f9\u00fa\u0003\u0016\u000b\u0000\u00fa\u00fb\u0005"+
		" \u0000\u0000\u00fb\u00fd\u0001\u0000\u0000\u0000\u00fc\u00f4\u0001\u0000"+
		"\u0000\u0000\u00fc\u00f5\u0001\u0000\u0000\u0000\u00fc\u00f6\u0001\u0000"+
		"\u0000\u0000\u00fc\u00f7\u0001\u0000\u0000\u0000\u00fc\u00f8\u0001\u0000"+
		"\u0000\u0000\u00fd%\u0001\u0000\u0000\u0000\u00fe\u00ff\u0007\u0005\u0000"+
		"\u0000\u00ff\u0103\u0005\u001f\u0000\u0000\u0100\u0101\u0003\u0016\u000b"+
		"\u0000\u0101\u0102\u0005!\u0000\u0000\u0102\u0104\u0001\u0000\u0000\u0000"+
		"\u0103\u0100\u0001\u0000\u0000\u0000\u0103\u0104\u0001\u0000\u0000\u0000"+
		"\u0104\u0105\u0001\u0000\u0000\u0000\u0105\u0106\u0003(\u0014\u0000\u0106"+
		"\u0107\u0005 \u0000\u0000\u0107\'\u0001\u0000\u0000\u0000\u0108\u010d"+
		"\u0005.\u0000\u0000\u0109\u010a\u0005$\u0000\u0000\u010a\u010c\u0005."+
		"\u0000\u0000\u010b\u0109\u0001\u0000\u0000\u0000\u010c\u010f\u0001\u0000"+
		"\u0000\u0000\u010d\u010b\u0001\u0000\u0000\u0000\u010d\u010e\u0001\u0000"+
		"\u0000\u0000\u010e)\u0001\u0000\u0000\u0000\u010f\u010d\u0001\u0000\u0000"+
		"\u0000\u0110\u0111\u0005.\u0000\u0000\u0111\u011a\u0005\u001f\u0000\u0000"+
		"\u0112\u0117\u0003\u0016\u000b\u0000\u0113\u0114\u0005!\u0000\u0000\u0114"+
		"\u0116\u0003\u0016\u000b\u0000\u0115\u0113\u0001\u0000\u0000\u0000\u0116"+
		"\u0119\u0001\u0000\u0000\u0000\u0117\u0115\u0001\u0000\u0000\u0000\u0117"+
		"\u0118\u0001\u0000\u0000\u0000\u0118\u011b\u0001\u0000\u0000\u0000\u0119"+
		"\u0117\u0001\u0000\u0000\u0000\u011a\u0112\u0001\u0000\u0000\u0000\u011a"+
		"\u011b\u0001\u0000\u0000\u0000\u011b\u011c\u0001\u0000\u0000\u0000\u011c"+
		"\u011d\u0005 \u0000\u0000\u011d+\u0001\u0000\u0000\u0000\u011e\u0123\u0005"+
		".\u0000\u0000\u011f\u0120\u0005\"\u0000\u0000\u0120\u0122\u0005.\u0000"+
		"\u0000\u0121\u011f\u0001\u0000\u0000\u0000\u0122\u0125\u0001\u0000\u0000"+
		"\u0000\u0123\u0121\u0001\u0000\u0000\u0000\u0123\u0124\u0001\u0000\u0000"+
		"\u0000\u0124\u012a\u0001\u0000\u0000\u0000\u0125\u0123\u0001\u0000\u0000"+
		"\u0000\u0126\u0127\u0005\"\u0000\u0000\u0127\u012b\u0003.\u0017\u0000"+
		"\u0128\u0129\u0005\"\u0000\u0000\u0129\u012b\u0005\u001a\u0000\u0000\u012a"+
		"\u0126\u0001\u0000\u0000\u0000\u012a\u0128\u0001\u0000\u0000\u0000\u012a"+
		"\u012b\u0001\u0000\u0000\u0000\u012b-\u0001\u0000\u0000\u0000\u012c\u012d"+
		"\u0005\u0016\u0000\u0000\u012d\u0131\u0005\u001f\u0000\u0000\u012e\u012f"+
		"\u0005.\u0000\u0000\u012f\u0130\u0005#\u0000\u0000\u0130\u0132\u0003\u0016"+
		"\u000b\u0000\u0131\u012e\u0001\u0000\u0000\u0000\u0131\u0132\u0001\u0000"+
		"\u0000\u0000\u0132\u0133\u0001\u0000\u0000\u0000\u0133\u013c\u0005 \u0000"+
		"\u0000\u0134\u0135\u0005\u0017\u0000\u0000\u0135\u0136\u0005\u001f\u0000"+
		"\u0000\u0136\u0137\u0005.\u0000\u0000\u0137\u0138\u0005#\u0000\u0000\u0138"+
		"\u0139\u0003\u0016\u000b\u0000\u0139\u013a\u0005 \u0000\u0000\u013a\u013c"+
		"\u0001\u0000\u0000\u0000\u013b\u012c\u0001\u0000\u0000\u0000\u013b\u0134"+
		"\u0001\u0000\u0000\u0000\u013c/\u0001\u0000\u0000\u0000\u013d\u0149\u0005"+
		"+\u0000\u0000\u013e\u0149\u0005,\u0000\u0000\u013f\u0149\u0005-\u0000"+
		"\u0000\u0140\u0149\u0007\u0006\u0000\u0000\u0141\u0149\u0005\u0015\u0000"+
		"\u0000\u0142\u0149\u0005\'\u0000\u0000\u0143\u0149\u0005(\u0000\u0000"+
		"\u0144\u0149\u0005)\u0000\u0000\u0145\u0149\u0005*\u0000\u0000\u0146\u0149"+
		"\u0005%\u0000\u0000\u0147\u0149\u0005&\u0000\u0000\u0148\u013d\u0001\u0000"+
		"\u0000\u0000\u0148\u013e\u0001\u0000\u0000\u0000\u0148\u013f\u0001\u0000"+
		"\u0000\u0000\u0148\u0140\u0001\u0000\u0000\u0000\u0148\u0141\u0001\u0000"+
		"\u0000\u0000\u0148\u0142\u0001\u0000\u0000\u0000\u0148\u0143\u0001\u0000"+
		"\u0000\u0000\u0148\u0144\u0001\u0000\u0000\u0000\u0148\u0145\u0001\u0000"+
		"\u0000\u0000\u0148\u0146\u0001\u0000\u0000\u0000\u0148\u0147\u0001\u0000"+
		"\u0000\u0000\u01491\u0001\u0000\u0000\u0000\u001e:AEKX]dqw\u0082\u008e"+
		"\u0098\u00a3\u00b0\u00b8\u00be\u00cc\u00dc\u00e6\u00f1\u00fc\u0103\u010d"+
		"\u0117\u011a\u0123\u012a\u0131\u013b\u0148";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}