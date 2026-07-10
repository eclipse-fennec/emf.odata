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
  **Nachtrag 2026-07-10:** BottomTop (`topcount/topsum/toppercent` + bottom-Spiegel),
  `concat`, `top`/`skip`, `orderby`, `identity` und `rollup`-Grouping-Sets sind im Submodell
  UND in-memory ausführbar; `aggregate … from`, Custom-Methoden/-Aggregates und benannte
  Hierarchien parsen ins Modell, Ausführung beidseitig ehrlich 501. Die Trafo-Namen sind
  jetzt prädikat-gesteuerte Soft-Keywords (Shape-Ambiguität war mit Custom-Aggregates nicht
  mehr auflösbar). **Offen:** search/nest/addnested/join/outerjoin/ancestors/descendants/
  traverse/rolluprecursive, `$these`, `$compute` als eigene Query-Option, Keyword-Kollisionen
  (Properties namens `with`/`as`/`from`/Transformationsnamen).
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
- **E4-AP-10 Bound/Composed Functions in Member-Pfaden ✅ erledigt (2026-07-10):**
  `boundCall`-Pfadsegmente (qualifiziert oder unqualifiziert, named/positional Argumente)
  → `OperationCallExp` mit qualifiziertem Namen (Backend-Dispatch), eager gegen
  `EClass.getEAllOperations()` aufgelöst, Argumente in Deklarationsreihenfolge validiert;
  `/$count`- und Lambda-Tails auf Operations-Ergebnissen. Am Expression-KOPF gewinnen
  weiterhin die canonical functions (unqualifizierte Calls dort = functionCall).
  Ausführung: Evaluator/Backends kennen die Operationen nicht → ehrlicher Fehler, kein
  stilles Raten. XML-ABNF-Harness: 161 aktiv (vorher 126).

---

## Priorisierung der Rest-Lücken (Plan, 2026-07-10)

Die 211 Harness-Skips = exakt diese Features. Vorschlag in drei Wellen (strategisch bleiben
Delta/Change-Tracking und der Release-Weg auf `main` VOR Welle 2/3 einsortiert):

**Welle 1 — Quick Wins (~1–2 Tage, ≈40 Skips):**
1. Typ-Casts in AUSDRUCKS-Pfaden `…/Ns.SubType/prop` (18) — Grammatik-Segment + Builder
   (Kontext-EClass-Wechsel wie im Resource-Parser), Evaluator = instanceof-Filter, JPA =
   `cb.treat()` auf dem Join (Root-Cast-Pushdown existiert schon). Rundet Derived-Types ab.
2. Gefiltertes `$count($filter=…)` + Inline-`$filter(…)`-Segmente (6) — OCL
   `select(...)->size()`; JPA korrelierte Count-Subquery.
3. Literal-Reste (≈12): Enum-FLAGS (`'Read,Write'` → Set von EnumLiteralExp), unäres Minus,
   NaN/INF, `binary'…'`; dazu die Lexer-Randfälle (roher Apostroph im Key-Segment,
   keyed Segmente in Aggregat-Pfaden).

**Welle 2 — mittlere Pakete (je 1–2 Tage, ≈85 Skips):**
4. `$it`/`$this` (implizite Variable ist im AST schon da) und `$these` im $apply-Kontext;
   `$root` braucht Backend-Semantik (Cross-Ressourcen-Zugriff) → ggf. splitten (34).
5. JSON-Literale als Funktions-/Key-Argumente (51) — Lexer/Grammatik für Objekt/Array-
   Literale; Hauptnutzen: Collection-Parameter für bound functions.
6. Parameter-/Key-Aliase in Pfaden (3, fällt bei 4/5 quasi mit ab).

**Welle 3 — Advanced (eigene Arbeitspakete, ≈74 Skips):**
7. `$apply`-Hierarchie-/Struktur-Trafos: search, nest/addnested, join/outerjoin,
   ancestors/descendants/traverse, rolluprecursive (41) + Custom-Functions als Trafo (27) —
   braucht RecHier-Modelle bzw. Operations-Dispatch; Praxisnutzen prüfen, bevor gebaut wird.
8. `$crossjoin`/`$all`/`$entity` (6) — zusammen mit async/`$skiptoken`/`$levels` als
   Advanced-URL-Paket (Next-Steps Punkt 5).
9. `@Ns.Term`-Annotations-Auswertung in Ausdrücken (12) — braucht Vokabular-Werte zur
   Laufzeit; niedriger Praxisnutzen, ans Ende.
