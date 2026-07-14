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

/**
 * OData v4.01 $filter / $orderby expression grammar (E4, req §3.6) — deliberately its OWN
 * grammar (no Olingo expression parsing, ADR/req constraint), producing a parse tree that
 * ODataToOclBuilder maps onto the m2x OCL AST (the internal predicate IR, req §3.5).
 *
 * v1 subset of the OASIS ABNF: logical (and/or/not), comparison (eq/ne/gt/ge/lt/le),
 * in-list, has, arithmetic (add/sub/mul/div/divby/mod), grouping, member paths, function
 * calls, parameter aliases (@name), primitive literals (string/int/decimal/boolean/null).
 * Lambdas (any/all), cast/isof and date/guid literals follow (tracked in the E4 backlog).
 */
grammar ODataFilter;

@parser::members {
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
}

filter  : expr EOF ;
orderby : orderbyItem (COMMA orderbyItem)* EOF ;
orderbyItem : expr direction=(ASC | DESC)? ;

// resource paths (ADR-0005, own URI parser — no Olingo): Set | Set(key) | …/nav(key)/prop
// with terminal $count/$value/$ref segments and derived-type casts (/Ns.Type, [OData-URL]
// 4.11); key literals reuse the expression tokens
resource : IDENT keyPredicate? (SLASH resourceSegment)* EOF   # EntitySetResource
         | CROSSJOIN LPAREN IDENT (COMMA IDENT)* RPAREN EOF   # CrossjoinResource
         | ALLRES (SLASH castName)? EOF                       # AllResource
         | ENTITYRES (SLASH castName)? EOF                    # EntityResource
         ;
// simple positional key OR named key-value pairs (ABNF compoundKey — composite keys and the
// named single-key form Set(id='x') both use it)
keyPredicate : LPAREN ( keyLiteral | namedKeyValue (COMMA namedKeyValue)* ) RPAREN ;
namedKeyValue : IDENT EQUALS keyLiteral ;
keyLiteral : STRING | INT | DECIMAL | GUID | DATETIMEOFFSET | DATE | TIMEOFDAY | ALIAS ;
// key-as-segment ([OData-URL] 4.3.3, 4.01 MAY — the Microsoft Graph style): a bare literal
// segment is a key value; bare IDENT string keys parse as PropertySegment and are
// disambiguated against the model at the protocol layer (declared properties win)
resourceSegment : castName keyPredicate?  # CastSegment
                | IDENT keyPredicate?     # PropertySegment
                | COUNT                   # CountSegment
                | VALUE                   # ValueSegment
                | REF                     # RefSegment
                | keyLiteral              # KeyValueSegment
                ;
castName : IDENT (DOT IDENT)+ ;

// $apply pipeline (E4-AP-4 + the deferred aggregation block): slash-separated
// transformations. The names are soft keywords gated by semantic predicates on the upcoming
// identifier — pure shape dispatch became ambiguous once custom aggregates (aggregate(Path
// as X) vs compute items) and nested pipelines (concat args vs function-call expressions)
// joined. Still open: search, nest/addnested, join/outerjoin, ancestors/descendants/
// traverse, rolluprecursive, $these.
apply : applySeq EOF ;
applySeq : applyTrafo (SLASH applyTrafo)* ;
applyTrafo
    : {trafo("groupby")}?   name=IDENT LPAREN LPAREN groupbyElement (COMMA groupbyElement)* RPAREN (COMMA applySeq)? RPAREN  # GroupByTrafo
    | {trafo("aggregate")}? name=IDENT LPAREN aggregateItem (COMMA aggregateItem)* RPAREN    # AggregateTrafo
    | {trafo("compute")}?   name=IDENT LPAREN computeItem (COMMA computeItem)* RPAREN        # ComputeTrafo
    | {trafo("concat")}?    name=IDENT LPAREN applySeq (COMMA applySeq)+ RPAREN              # ConcatTrafo
    | {trafo("filter")}?    name=IDENT LPAREN expr RPAREN                                    # FilterTrafo
    | {bottomTopTrafo()}?   name=IDENT LPAREN expr COMMA expr RPAREN                         # BottomTopTrafo
    | {trafo("orderby")}?   name=IDENT LPAREN orderbyItem (COMMA orderbyItem)* RPAREN        # OrderByTrafo
    | {trafo("top") || trafo("skip")}? name=IDENT LPAREN INT RPAREN                          # RowLimitTrafo
    | {trafo("identity")}?  name=IDENT                                                       # IdentityTrafo
    | {trafo("search")}?    name=IDENT LPAREN searchExpr RPAREN                              # SearchTrafo
    | {trafo("nest")}?      name=IDENT LPAREN applySeq AS IDENT
                            (COMMA applySeq AS IDENT)* RPAREN                                # NestTrafo
    | {trafo("addnested")}? name=IDENT LPAREN memberPath
                            (COMMA applySeq AS IDENT)+ RPAREN                                # AddNestedTrafo
    | {trafo("join") || trafo("outerjoin")}?
                            name=IDENT LPAREN memberPath AS IDENT (COMMA applySeq)? RPAREN   # JoinTrafo
    | {trafo("ancestors") || trafo("descendants")}?
                            name=IDENT LPAREN rootedPath COMMA IDENT COMMA memberPath
                            COMMA applySeq (COMMA INT)? (COMMA IDENT IDENT)? RPAREN          # HierarchyTrafo
    | {trafo("traverse")}?  name=IDENT LPAREN rootedPath COMMA IDENT COMMA memberPath
                            COMMA IDENT (COMMA applySeq)?
                            (COMMA orderbyItem (COMMA orderbyItem)*)? RPAREN                 # TraverseTrafo
    | {customTrafo()}?      IDENT (DOT IDENT)+ LPAREN boundCallArgs? RPAREN                  # CustomFunctionTrafo
    ;

// $search word grammar subset for the search transformation: words, OData strings,
// NOT/AND/OR (case-sensitive per ABNF, matched as plain words here), grouping
searchExpr : searchAtom+ ;
searchAtom : NOT? (IDENT | STRING | DQSTRING | INT | DECIMAL | LPAREN searchExpr RPAREN) ;
// rollup: 2+ paths = unnamed leveled hierarchy; ONE simple identifier = named hierarchy
// (Aggregation.LeveledHierarchy qualifier) — disambiguated in the builder
groupbyElement
    : {trafo("rollup")}? name=IDENT LPAREN memberPath (COMMA memberPath)* RPAREN  # RollupElement
    | {trafo("rolluprecursive")}? name=IDENT LPAREN rootedPath COMMA IDENT
      COMMA memberPath (COMMA applySeq)? RPAREN                                   # RollupRecursiveElement
    | memberPath                                                                  # PathElement
    ;
// ABNF aggregateExpr: 'with'-items and $count require the alias and their from clauses a
// method (aggregateFrom); custom aggregates (bare path, no 'with') may omit both (customFrom)
aggregateItem
    : expr WITH method=methodName aggrFrom* AS alias=IDENT   # AggregateWithItem
    | COUNT aggrFrom* AS alias=IDENT                         # AggregateCountItem
    | expr customFrom* AS alias=IDENT                        # AggregateCustomAliased
    | expr                                                   # AggregateCustomBare
    ;
methodName : IDENT (DOT IDENT)* ;
aggrFrom   : FROM memberPath (COMMA memberPath)* WITH method=methodName ;
customFrom : FROM memberPath (COMMA memberPath)* (WITH method=methodName)? ;
computeItem : expr AS alias=IDENT ;

expr    : orExpr ;
orExpr  : andExpr (OR andExpr)* ;
andExpr : notExpr (AND notExpr)* ;
notExpr : NOT notExpr        # NotExpression
        | comparison         # ComparisonLevel
        ;

// 'in' (4.01): the right operand is either ONE parenthesized expression or a list of
// TWO OR MORE primitive literals (ABNF: inExpr = "in" ( listExpr / commonExpr ));
// the CURRENT TC listExpr additionally allows the EMPTY list (matches nothing)
comparison
        : additive op=(EQ | NE | GT | GE | LT | LE | HAS) additive  # BinaryComparison
        | additive IN LPAREN literal (COMMA literal)+ RPAREN        # InListComparison
        | additive IN LPAREN RPAREN                                 # InEmptyListComparison
        | additive IN LPAREN expr RPAREN                            # InComparison
        | additive IN jsonArray                                     # InArrayComparison
        | additive                                                  # PassThrough
        ;

additive : additive op=(ADD | SUB) multiplicative   # AddSub
         | multiplicative                           # ToMultiplicative
         ;
multiplicative : multiplicative op=(MUL | DIV | DIVBY | MOD) primary  # MulDivMod
               | primary                                              # ToPrimary
               ;

primary : literal                 # LiteralPrimary
        | typeFunc                # TypeFuncPrimary
        | functionCall            # FunctionPrimary
        | rootedPath              # RootedPrimary
        | ALIAS                   # AliasPrimary
        | memberPath              # MemberPrimary
        | LPAREN expr RPAREN      # ParenPrimary
        | MINUS primary           # NegatedPrimary
        | jsonArray               # JsonArrayPrimary
        | jsonObject              # JsonObjectPrimary
        ;

// instance references ([OData-URL] 5.1.1.13): $it/$this anchor at the REQUEST instance
// (escaping lambda scopes), $these at the current collection ($apply), $root addresses
// another resource from the service root
rootedPath : anchor=(ITREF | THISREF | THESEREF)
             (SLASH memberPath | SLASH countCall | SLASH aggregateCall)?                # InstanceRef
           | ROOTREF SLASH IDENT (keyPredicate | LPAREN RPAREN)?
             (SLASH memberPath | SLASH countCall)?                                      # RootRef
           ;
// the 4.02 aggregate FUNCTION ($these/aggregate(Amount with sum)) — items carry no alias
aggregateCall : {trafo("aggregate")}? IDENT LPAREN aggregateFunctionItem RPAREN ;
aggregateFunctionItem : expr WITH method=methodName aggrFrom*
                      | COUNT aggrFrom*
                      ;

// JSON-style literals (ABNF arrayOrObject / 4.01 listExpr): array members are FULL
// expressions (properties, OData strings, nested arrays); objects stay opaque JSON text
jsonArray  : LBRACKET (expr (COMMA expr)*)? RBRACKET ;
jsonObject : LBRACE (jsonMember (COMMA jsonMember)*)? RBRACE ;
jsonMember : DQSTRING COLON expr ;

// cast(T) / cast(x,T) / isof(T) / isof(x,T) — the type name may be namespace-qualified
typeFunc : op=(CAST | ISOF) LPAREN (expr COMMA)? qualifiedTypeName RPAREN ;
qualifiedTypeName : IDENT (DOT IDENT)* ;

functionCall : IDENT LPAREN (expr (COMMA expr)*)? RPAREN ;
// member path with optional lambda or /$count tail: Items/any(d: d/Qty gt 5), Tags/any(),
// Products/$count. Parameterless any() = "has members"; all() REQUIRES a lambda (5.1.1.13.2).
// Segments may be bound/composed function calls (E4-AP-10): namespace-qualified, with named
// (bound operations) or positional (built-ins like geo.*) arguments
memberPath   : (pathStep SLASH)* lastSegment (SLASH lambdaCall | SLASH countCall)? ;
pathStep     : IDENT keyPredicate? | boundCall | castName | ANNOTATION | ALIAS | filterSegment ;
// terminal casts are legal (aggregate operands, …/Cast/$count); whether a bare qualified
// name is a cast or a parenless function is a MODEL question — the harnesses omit those
lastSegment  : IDENT keyPredicate? | boundCall | castName | ANNOTATION | ALIAS | filterSegment ;
// inline collection filter ([OData-URL] 4.12): Products/$filter(Age gt 3)[(key)]
filterSegment : FILTERQ LPAREN expr RPAREN keyPredicate? ;
// path/$count with the optional filtered/searched forms path/$count($filter=...) and
// path/$count($search=...) ([OData-URL] 4.8, 5.1.1.14; the searched count is §13.2.3/3)
countCall    : COUNT (LPAREN (FILTERQ EQUALS expr | SEARCHQ EQUALS searchExpr) RPAREN)? ;
// the name may be unqualified (4.01 allows it when unambiguous); at the EXPRESSION HEAD a
// simple call parses as functionCall first (canonical functions win the ambiguity there)
boundCall    : IDENT (DOT IDENT)* LPAREN boundCallArgs? RPAREN ;
boundCallArgs : namedArg (COMMA namedArg)*
              | expr (COMMA expr)*
              ;
namedArg     : IDENT EQUALS expr ;
lambdaCall   : op=ANY LPAREN (IDENT COLON expr)? RPAREN
             | op=ALL LPAREN IDENT COLON expr RPAREN
             ;

literal : STRING          # StringLiteral
        | DQSTRING        # JsonStringLiteral
        | BINARY          # BinaryLiteral
        | (NANLIT | INF)  # NanInfLiteral
        | DECIMAL         # DecimalLiteral
        | INT             # IntLiteral
        | (TRUE | FALSE)  # BooleanLiteral
        | NULL            # NullLiteral
        | GUID            # GuidLiteral
        | DATETIMEOFFSET  # DateTimeOffsetLiteral
        | DATE            # DateLiteral
        | TIMEOFDAY       # TimeOfDayLiteral
        | DURATION        # DurationLiteral
        | ENUM            # EnumLiteral
        ;

// operator keywords are CASE-INSENSITIVE since OData 4.01 (OASIS 5.1.1.1.12);
// value literals (true/false/null) and lambda keywords stay lowercase per the ABNF
OR   : O R ;
AND  : A N D ;
NOT  : N O T ;
EQ   : E Q ;
NE   : N E ;
GT   : G T ;
GE   : G E ;
LT   : L T ;
LE   : L E ;
HAS  : H A S ;
IN   : I N ;
ADD  : A D D ;
SUB  : S U B ;
MUL  : M U L ;
DIVBY : D I V B Y ;
DIV  : D I V ;
MOD  : M O D ;
ASC  : A S C ;
DESC : D E S C ;
TRUE : 'true' ;
FALSE: 'false' ;
NULL : 'null' ;
ANY  : 'any' ;
ALL  : 'all' ;
CAST : C A S T ;
ISOF : I S O F ;
CROSSJOIN : '$crossjoin' ;
ALLRES    : '$all' ;
ENTITYRES : '$entity' ;
ITREF   : '$it' ;
THISREF : '$this' ;
THESEREF : '$these' ;
ROOTREF : '$root' ;
COUNT : '$count' ;
VALUE : '$value' ;
REF   : '$ref' ;
FILTERQ : '$filter' ;
SEARCHQ : '$search' ;
WITH : 'with' ;
AS   : 'as' ;
FROM : 'from' ;

MINUS  : '-' ;
LBRACE : '{' ;
RBRACE : '}' ;
LBRACKET : '[' ;
RBRACKET : ']' ;
LPAREN : '(' ;
RPAREN : ')' ;
COMMA  : ',' ;
EQUALS : '=' ;
SLASH  : '/' ;
COLON  : ':' ;
DOT    : '.' ;

// typed literals (order matters: longest/most specific before INT/IDENT)
DQSTRING : '"' ('\\' . | ~["\\])* '"' ;
NANLIT   : 'NaN' ;
INF      : 'INF' ;
BINARY   : 'binary' '\'' ~'\''* '\'' ;
DURATION : 'duration' '\'' ~'\''* '\'' ;
ENUM     : [a-zA-Z_] [a-zA-Z0-9_]* ('.' [a-zA-Z_] [a-zA-Z0-9_]*)+ '\'' ~'\''* '\'' ;
GUID     : HEX HEX HEX HEX HEX HEX HEX HEX '-' HEX HEX HEX HEX '-' HEX HEX HEX HEX '-'
           HEX HEX HEX HEX '-' HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX HEX ;
DATETIMEOFFSET : DATEPART 'T' TIMEPART ('Z' | [+-] [0-9] [0-9] ':' [0-9] [0-9]) ;
DATE     : DATEPART ;
TIMEOFDAY : TIMEPART ;

STRING  : '\'' ( ~'\'' | '\'\'' )* '\'' ;
DECIMAL : '-'? [0-9]+ '.' [0-9]+ ;
INT     : '-'? [0-9]+ ;
IDENT   : [a-zA-Z_] [a-zA-Z0-9_]* ;
// annotation value references in paths (Price/@Measures.ISOCurrency) vs plain @alias
ANNOTATION : '@' [a-zA-Z_] [a-zA-Z0-9_]* ('.' [a-zA-Z_] [a-zA-Z0-9_]*)+ ;
// parameter alias (4.01 11.2.5.1.3): plain @name
ALIAS   : '@' [a-zA-Z_] [a-zA-Z0-9_]* ;

fragment HEX : [0-9a-fA-F] ;
fragment DATEPART : [0-9] [0-9] [0-9] [0-9] '-' [0-9] [0-9] '-' [0-9] [0-9] ;
fragment TIMEPART : [0-9] [0-9] ':' [0-9] [0-9] (':' [0-9] [0-9] ('.' [0-9]+)?)? ;

fragment A : [aA] ;
fragment B : [bB] ;
fragment C : [cC] ;
fragment D : [dD] ;
fragment E : [eE] ;
fragment F : [fF] ;
fragment G : [gG] ;
fragment H : [hH] ;
fragment I : [iI] ;
fragment L : [lL] ;
fragment M : [mM] ;
fragment N : [nN] ;
fragment O : [oO] ;
fragment Q : [qQ] ;
fragment R : [rR] ;
fragment S : [sS] ;
fragment T : [tT] ;
fragment U : [uU] ;
fragment V : [vV] ;
fragment Y : [yY] ;

WS : [ \t\r\n]+ -> skip ;
