# OData E4 – Query Translation: Stand und offene Arbeitspakete

Status: 2026-07-03. Bezieht sich auf `org.eclipse.fennec.odata.query`.
Grundlage: req-doc §3.5 (OCL als Predicate-IR, Mapping-Tabelle), §3.6 (eigene ANTLR4-Grammatik,
kein Olingo-Expression-Parsing), §3.6.1 (Caching am Metadata-Service).

## Erledigt (Einstieg, 2026-07-03)
- **Bundle `org.eclipse.fennec.odata.query`** mit eigener ANTLR4-Grammatik
  (`grammar/ODataFilter.g4`, generierter Parser in `src-gen` eingecheckt — Konvention wie
  `m2x.ocl.parser`; Regenerier-Rezept im `bnd.bnd`).
- **`ODataQueryParser.parseFilter/parseOrderBy(String, EClass)`** → m2x-OCL-AST
  (`OclExpression`), Mapping per §3.5-Tabelle in `ODataToOclBuilder`:
  eq/ne/gt/ge/lt/le → `=`,`<>`,`>`,`>=`,`<`,`<=`; and/or/not; add/sub/mul/div/mod →
  `+`,`-`,`*`,`/`,`mod`; canonical functions (contains/startswith/endswith/tolower→toLower/
  toupper→toUpper/trim/length→size/indexof→indexOf/substring/concat/year…second/date/time/
  round/floor/ceiling); `in` → `Set{…}->includes(prop)`; `has` als Custom-Op-Name.
- **Eager Property-Resolution** gegen die Kontext-EClass (`PropertyCallExp.referredProperty`),
  Pfad-Navigation über `/`; unbekannte Property/Funktion/Syntaxfehler →
  `ODataQueryParseException` (→ 400). 10 Unit-Tests (`webshop.ecore`-Fixture).

## Offene APs
- **E4-AP-1 Lambdas ✅ erledigt (2026-07-03):** `path/any(v: …)` → `IteratorExp('exists')`,
  `all(v: …)` → `'forAll'` (Variable + Scope-Stack im Builder, Body-Auflösung gegen den
  Element-Typ der Collection), parameterloses `any()` → `notEmpty`; `all()` ohne Lambda lehnt
  schon die Grammatik ab (OASIS 5.1.1.13.2). Typisierung: exists/forAll/notEmpty → Boolean,
  `VariableExp` aus dem Variablentyp.
- **E4-AP-2 Type-Operatoren ✅ erledigt (2026-07-03):** `cast(x,T)`/`isof(x,T)` (auch unbound
  `cast(T)`/`isof(T)` → `isImplicit`) → `oclAsType`/`oclIsKindOf` mit `TypeExp`-Argument;
  Typnamen qualifiziert oder simpel, `Edm.*` → OCL-Primitive (Int*→Integer, Decimal/Double/
  Single→Real, Rest per Name), Modelltypen gegen das Kontext-EPackage (unbekannt → 400).
  Typisierung: `oclIsKindOf`→Boolean, `oclAsType`→Zieltyp.
- **E4-AP-3 Literale ✅ Kern erledigt (2026-07-03):** Date/DateTimeOffset/TimeOfDay/Guid als
  Lexer-Tokens → vor-typisierte `StringLiteralExp` (PrimitiveType-Namen `Date`/`DateTimeOffset`/
  `TimeOfDay`/`Guid` — Backend-Dispatch über den Typnamen), `duration'…'` → `Duration`,
  `Ns.Enum'Value'` → `EnumLiteralExp` (aufgelöst gegen das Kontext-EPackage). Außerdem:
  Operator-/Funktionsnamen jetzt CASE-INSENSITIVE (4.01, OASIS 5.1.1.1.12).
  **Restlücken:** Enum-FLAG-Kombinationen (`'Red,Green'`), `binary'…'`, Geo-Literale,
  NaN/INF, unäres Minus, `$it`/`$this`/`$root`.
- **E4-AP-4 `$apply` ✅ v1 erledigt (2026-07-03):** eigenes Submodell `model/apply.ecore`
  (`ApplyPipeline`/`Filter`/`GroupBy`(+`then`)/`Aggregate`(sum/min/max/average/countdistinct/
  $count)/`Compute` — eingebettete Expressions sind OclExpressions per Cross-Model-Containment,
  generiert via fennecEMF `-generate`; usedGenPackages löst `ocl.genmodel` aus dem m2x-Binary-Jar).
  Grammatik: `apply`-Regel mit Soft-Keyword-Transformationen (Shape-basierte Disambiguierung,
  Namen validiert der Builder), `with`/`as` als Tokens. `ODataQueryParser.parseApply(String,
  EClass)` → typisierte Pipeline; **Aggregate-/Compute-Aliase sind in Folgestufen referenzierbar**
  (als `VariableExp` — Backend löst gegen den Stufen-Output auf). Caching in
  `CachingODataQueryParser`. WICHTIG: ANTLR-Parser lebt jetzt in `src-gen-parser` (eigener
  Source-Folder), weil fennecEMF `-generate` `src-gen` exklusiv besitzt und leert.
  **Offen:** BottomTop/Concat/expand-Transformationen, `$compute` als eigene Query-Option
  (Grammatik-Regel vorhanden via compute-Trafo), Keyword-Kollisionen (Properties namens
  `with`/`as`/Transformationsnamen).
- **E4-AP-5 ABNF-Akzeptanztests ✅ Harness erledigt (2026-07-03):** offizielle OASIS ABNF Test
  Cases 4.01 vendored (`testdata/odata-abnf-testcases.xml` im Query-Bundle; das vorbereitete
  `testdata/abnf-test-cases/` im Workspace-Root ist read-only und leer — Umzug dorthin wäre
  User-Aktion). `AbnfAcceptanceTest` (JUnit-`@TestFactory`) fährt alle 192 Expression-Fälle
  (filter/boolCommonExpr/commonExpr/orderby) syntax-only. Stand nach AP-1+AP-3:
  **103 aktiv verifiziert, 89 per Assumption übersprungen** (Stand nach AP-1/2/3/8) = exakt die dokumentierten
  Restlücken (cast/isof, $count/$it/$this/$root, Aliase, unäres Minus, Geo-/Binary-Literale,
  Enum-Flags, NaN/INF, divby, NS-qualifizierte Calls/Casts, Percent-Encoding [URL-Layer],
  Bound Functions in Pfaden). Die Skip-Zahl SINKT mit jedem geschlossenen AP — Regressions-Radar
  inklusive. Funde aus den Cases bereits gefixt: `in`-Semantik (ein Paren-Ausdruck ODER Liste
  2+ primitiver Literale), Case-Insensitivity der Operatoren (4.01), `all()` ohne Lambda
  grammatikalisch invalide.
- **E4-AP-6 Type-Resolution ✅ erledigt (2026-07-03, ADR-0004):** standalone `OclTypeResolver`
  im Query-Bundle, in `ODataQueryParser` integriert — bottom-up: Property-Typen aus
  `referredProperty` (EMF-Liste → CollectionKind wie im m2x-Parser), Named-Op-Ergebnistypen
  per Dispatch-Tabelle (Boolean/Integer/String, `div`→Real, numerisches Widening), Literale
  direkt, Lambda-Variablen aus dem Variablentyp. Hängt NICHT an VA1; der `OclAspectProvider`
  ist nur noch Cache-/Lifecycle-Adapter (Relocation nach emf.m2x später).
- **E4-AP-7 Caching ✅ Kern erledigt (2026-07-03):** `ODataQueryLruCache` (Vorbild
  `OclLruExpressionCache`: access-ordered LinkedHashMap, synchronized, Hit/Miss-Stats, Default
  1024) + `CachingODataQueryParser` (per-EClass-Caches, weak keys; Sharing-Kontrakt: Hits liefern
  DIESELBE AST-Instanz — read-only, bei Bedarf kopieren). **Offen:** Anbindung ans
  `ODataAspectProvider`-Profil (Aspect-Slot `parsedQueryCache`, Whiteboard-Invalidierung beim
  Unregister — der Adapter-Teil aus ADR-0004); Cache-Key-Kanonisierung = Q20.
- **E4-AP-8 `$count`-Segment ✅ Kern erledigt (2026-07-03):** `path/$count` → `size` (nur auf
  collection-valued Pfaden, sonst 400). **Offen:** gefilterte Form `$count($filter=…)` und
  Inline-`/$filter(…)`-Segmente.
- **E4-AP-9 Query-Modell-Refactoring Stufe 1** (req §3.5): `QWhere` → `predicate: OclExpression`
  im Persistence-Query-Modell — Cross-Repo-Abstimmung nötig (emf.persistence-jpa).
- **E4-AP-10 Bound/Composed Functions in Member-Pfaden:** `Products/BestProduct()/Name`
  (aus den ABNF-Cases) — Grammatik + Builder-Anbindung an die E1-Operation-Profile.
