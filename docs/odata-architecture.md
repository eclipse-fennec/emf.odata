# Fennec OData – Architektur

> **Aktueller Funktionsstand (Server & Client), englisch:** siehe
> [`odata-features.md`](odata-features.md). Dieses Dokument ist der (deutsche) Architektur- und
> Änderungs-Log mit den historischen Detail-Notizen; die Meilenstein-Einträge unten sind datiert.

Status: 2026-07-04. Einstiegsdokument — Details in den verlinkten ADRs/Backlogs.
Conformance: **4.0 Minimal (read-only) erfüllt, 4.01 Minimal im Wesentlichen erfüllt**
(`odata-conformance-status.md`); Spec-Artefakte/Tooling: `odata-spec-repos-gap-analysis.md`.
Requirements: `odata-basic-requirements.md` (v0.9). Beispiel: `org.eclipse.fennec.odata.example`.

## Big Picture

```
                    HTTP (Jetty12, OSGi HTTP Whiteboard)
                                   │
                       ┌───────────▼───────────┐   ADR-0001: plain Jakarta Servlet,
                       │      ODataServlet     │   kein Jakarta REST
                       │  (runtime, Catch-All) │
                       └──┬───────┬───────┬────┘
        RequestLimits ────┤       │       ├──── EntityShaper ($select/$expand-Kopien)
        (vor dem Parsen)  │       │       └──── ODataJson (Rows/Fehler, Sanitizer)
                          │       │
             ┌────────────▼──┐   ┌▼──────────────────┐
             │ $metadata     │   │ Query-Optionen    │
             │ E2: csdl      │   │ E4: query         │
             │ Ecore↔EDM     │   │ ANTLR4 → OCL-IR   │──── CachingODataQueryParser
             │ (OASIS-Modell)│   │ + $apply-Submodell│     (LRU §3.6.1, pro EClass)
             └───────▲───────┘   └────────┬──────────┘
                     │                    │ typisiertes OCL-IR (m2x ocl.model)
             ┌───────┴───────┐   ┌────────▼──────────┐
             │ E1: metadata  │   │ E5: QueryService  │  SPI (persistence.api)
             │ ODataAspect-  │   │ execute/          │◄─── JPA/Mongo später (Pushdown)
             │ Provider +    │   │ executeApply      │
             │ vocabularies  │   └────────┬──────────┘
             └───────────────┘   ┌────────▼──────────┐
                                 │ inmemory-Backend  │  OclEvaluator (AST-Interpreter)
                                 │ + ApplyExecutor   │  = Referenz-Semantik für Backends
                                 └────────┬──────────┘
                                 ┌────────▼──────────┐
                                 │ EntityRepository  │  SPI: Datenquelle (Zwischenschicht)
                                 │ File (XMI-Dir) /  │
                                 │ programmatisch    │
                                 └───────────────────┘
        Antworten: E3 codec.json (OData-JSON pro Entity) | XMI ($format=xml) | CSDL-XML
```

## Bundles

| Bundle | Etappe | Inhalt |
|---|---|---|
| `odata.metadata` | E1 | `odata.ecore`-Aspekte/Profile, `ODataAspectProvider` (dünner Adapter, ADR-0003) |
| `odata.vocabularies` | E1 | OASIS Core/Capabilities/Validation/Measures als EPackages (CSDL-Read-Bootstrap) |
| `odata.csdl` | E2 | `OdataResolver` (EPackage+`@OData.*`→Profil), Ecore↔EDM-Converter, `$metadata` |
| `odata.codec.json` | E3 | OData-JSON-Codec-Profil (@odata.type/id, Edm.*-Werteformate) |
| `odata.query` | E4 | ANTLR4-Grammatik → OCL-IR, `$apply`-Submodell, TypeResolver (ADR-0004), LRU-Cache |
| `odata.persistence.api` | E5 | `QueryService`/`ApplyQuery`-SPI + `EntityRepository`-Datenquellen-Abstraktion |
| `odata.persistence.inmemory` | E5 | Referenz-Backend: `OclEvaluator`, `ApplyExecutor`, `FileEntityRepository` |
| `odata.runtime` | E6/E7 | `ODataServlet` + `RequestLimits`/`EntityShaper`/`ODataJson` |
| `odata.metadata.tests`, `odata.itests` | — | OSGi-Integrationstests (Whiteboard-Kette bzw. echtes HTTP) |
| `odata.example` | — | Dummy-Modell + Demo-Daten + startbarer `example.bndrun` |

ADRs: 0001 Servlet-Transport · 0002 CSDL via EDM-Modell · 0003 Converter besitzt die
Auflösung (standalone Kern + Provider-Adapter) · 0004 Type-Resolution standalone ·
0005 kein Olingo zur Laufzeit (archiviert) — URI-Parser/Batch-Splitter Eigenbau.
Backlogs: `odata-e2-converter-open-points.md`, `odata-e4-query-open-points.md`,
`odata-e5-e6-server-state.md`.

## Request-Lifecycle (GET /odata/{Set}?…)

1. `OData-Version`-Negotiation (4.01, außer Client pinnt `OData-MaxVersion: 4.0`); Nicht-GET → 405.
2. Options-Normalisierung (4.01: case-insensitiv, `$`-Präfix optional; Whitelist: bekannt-unsupported
   → 501, unbekannte `$x` → 400, Custom-Options ignoriert) und Routing: `/` Service-Doc ·
   `/$metadata` CSDL (mit `Core.ODataVersions` am Container) · `Set(key)…` via eigener
   Resource-Path-Parser (ADR-0005) · `Set` Collection.
3. **Limits VOR dem Parsen** (`RequestLimits`): Expression-Länge, Klammer-Tiefe (O(n)-Scan),
   `$top`-Ceiling, Paging-Validierung.
4. Parsen (`CachingODataQueryParser`): `$filter`/`$orderby`/`$apply` → typisiertes OCL-IR;
   Property-Pfade werden eager gegen die Kontext-EClass aufgelöst — unbekannte Namen → 400.
   Single-Entity-Keys werden als Literal-AST **gebaut**, nie geparst.
5. `QueryService.execute(EntityQuery)` bzw. `executeApply(ApplyQuery)` — das Backend bekommt
   ausschließlich das IR; `$filter`/`$orderby` nach `$apply` laufen auf dem transformierten Set
   (Aliase als `VariableExp`).
6. Antwort: `EntityShaper`-Kopien ($select/$expand) → E3-JSON (`odata.metadata=minimal`:
   berechenbare Control-Info wie `@odata.type`/`@odata.id` wird weggelassen, Key-Properties
   bleiben) oder XMI; `$apply`-Rows → `ODataJson`; Seitenüberlauf → `@odata.nextLink`.
   Fehler → sanitisiertes OData-Error-JSON.

## Security-Defaults (req §4.5 / Q6-Vorschlag)

| Schutz | Default | Konfiguration (PID `org.eclipse.fennec.odata.servlet`) |
|---|---|---|
| `$top`-Ceiling (greift auch ohne Client-`$top`) | 1000 | `odata.max.top` |
| Max. Expression-Länge ($filter/$orderby/$apply) | 4096 | `odata.max.expression.length` |
| Max. Klammer-Tiefe (Parser-Bomb-Guard, vor dem Parsen, underflow-fest) | 64 | `odata.max.nesting.depth` |
| Injection | strukturell: einziger Query-Pfad ist das typisierte IR; kein String-Concat in Backends; unbekannte Properties/Funktionen → 400 | — |
| Fehler-Leaks | 500 generisch; Meldungen JSON-escaped, control-chars entfernt, 500 Zeichen max (`ODataJson.sanitize`) | — |
| Pfad-Leaks | Serialisierungs-Kopien mit MITkopierten Expand-Zielen → nur interne Referenzen | — |
| Dateizugriff | `FileEntityRepository` liest NUR das konfigurierte Verzeichnis, einmalig bei Aktivierung; Request-Input beeinflusst nie Pfade | `directory` (Factory-PID `…repository.file`) |
| Schreibzugriffe | Write-Path v1 (2026-07-07): POST/PATCH/PUT/DELETE nur auf Set/Entity-Ebene, nur `application/json` (415), Body-Größenlimit (413), Payload via Codec (kein manuelles JSON-Parsen), Konflikte → 409; ohne registrierten `WriteService` → 405 | `odata.max.body.size` (Default 1 MiB) |
| Server-Driven-Paging (JPA) | ohne Client-`$top` deckelt das JPA-Backend die Zeilenzahl, statt eine ganze Tabelle in den Heap zu materialisieren; `<= 0` = unbegrenzt | Default 1000, `odata.jpa.max.page.size` (PID `org.eclipse.fennec.odata.persistence.jpa`) |
| Auth/TLS | out-of-scope (req §4.5) — vorgelagerte Infrastruktur | — |

Getestet unit- (Mockito) UND e2e-seitig (echtes HTTP): Injection-Strings, Parser-Bomben,
überlange Filter, `$top`-Exhaustion, Leak-Freiheit von 500ern, Key-Injection.

### Härtungs-Pass 2026-07-08 (Concurrency/Injection/NPE/Exception-Review)

Konsolidierte Absicherung aus dem Multi-Dimensions-Review; jede Änderung ist mit Tests belegt:

- **Parser-Bomb-Guard vollständig:** `RequestLimits.checkExpression` wird jetzt AUCH auf `$expand`
  und dessen verschachteltes `$filter` angewandt (vorher umgangen). Zusätzlich fängt der
  Query-Parser klammerfreie Tiefen-Rekursion (`not not …`, tiefe Member-Pfade) über einen
  intrinsischen `StackOverflowError`-Guard ab → 400 statt 500/Crash; Integer-Literal-Overflow und
  Alias-Wert-Länge werden ebenfalls im Query-Bundle selbst begrenzt. Resource-Pfad hat einen
  Längen-Cap (`ODataResourceParser.MAX_PATH_LENGTH`, 4096) VOR dem Parsen.
- **XXE-Härtung (zentral):** `CsdlXmlLoad.secureOptions()` (Bundle `…csdl`) deaktiviert DOCTYPE
  sowie externe Entities und ist der EINE Ladepfad für CSDL/EDMX-XML — genutzt von den vendored
  Vocabularies UND vom Client, der das (nicht vertrauenswürdige) `$metadata` eines fremden Service
  liest. Neutralisiert XXE (Datei-Read/SSRF) und Billion-Laughs in einem Zug.
- **Evaluation-Fehler → 400:** Typ-/Format-Fehler im In-Memory-`OclEvaluator` (z. B. `contains`
  auf Zahl, kaputtes Datum) sowie nicht-vergleichbare `$orderby`-Schlüssel werden als
  `ODataQueryParseException` (→ 400) statt als interner 500 gemeldet.
- **Optimistic Concurrency:** ein Backend-FEHLER beim If-Match-Read wird nicht mehr still zu
  „Entity fehlt → Upsert" degradiert (Lost-Update-Risiko), sondern propagiert (→ geloggter 500).
- **Concurrency In-Memory-Backend:** `createRelated`/`link`/`unlink` mutieren die geteilte EMF-EList
  jetzt unter dem Per-Klassen-Lock (wie create/update/delete), deadlock-frei (kein verschachteltes
  Lock).
- **JPA-Transaktionen:** jeder Write rollt bei einer Ausnahme zwischen `begin()` und `commit()`
  explizit zurück (statt sich auf `close()` zu verlassen).
- **Logging:** unerwartete 500er werden serverseitig auf ERROR geloggt (vorher spurlos), Client
  sieht weiterhin nur die generische Meldung.
- **Header-Injection:** Entity-Key im `Location`/`OData-EntityId`-Header wird control-char-escaped
  (kein Response-Splitting).
- **Client:** `ODataClient` ist `AutoCloseable` (schließt nur einen selbst erzeugten `HttpClient`);
  ein server-geliefertes `@odata.nextLink` auf einen fremden Origin wird abgelehnt (SSRF-Guard).

### Verlässlichkeits-/Coverage-Pass 2026-07-08

Ergebnis einer Testabdeckungs-Analyse (Fokus: „HTTP 200 mit falschen Zeilen"). Neu:

- **Differenzieller Backend-Harness** (`DifferentialBackendTest`): EIN Datensatz (Edge-Fälle:
  null, `%`/`_`, Unicode, gleiche Sortierschlüssel) in JPA UND In-Memory geladen, ein Query-Corpus
  (Vergleich/Logik/`in`/String-/Datums-Funktionen/`$orderby`/`$apply` mit allen sechs Aggregaten)
  über beide, Ergebnis-Parität asserted. **Deckte einen echten Bug auf:** der In-Memory-Evaluator
  behandelte Null nicht 3-wertig — `not (price eq 5)` schloss Null-Zeilen fälschlich EIN und
  `year(null)` warf einen Fehler (JPA/SQL korrekt). Behoben in `OclEvaluator` (Null-Operand ⇒
  UNKNOWN ⇒ Prädikat falsch; `eq/ne null`-Literal bleibt definierter Test) — jetzt Parität.
- **Wertformate** (`ODataJsonRoundTripTest`): Int64/TimeOfDay/Duration/Enum/Collection/Null
  round-trip. Enum serialisiert per Namen, Collections korrekt. **Bekannte Lücke (dokumentiert):**
  `Edm.Int64 > 2^53` geht als blanke JSON-Zahl über die Leitung (Java-Round-Trip exakt, aber
  `IEEE754Compatible`-String-Form für JS-Clients fehlt).
- **CI-Gates:** strukturelle Skalierungs-Asserts (konstante Statement-Zahl bei 50k Zeilen) failen
  jetzt den Build (`perfTest` ohne `ignoreFailures`; Timing nur geloggt); JaCoCo-Coverage-Floor
  (30 % Instructions, generierter Code exkludiert) an `check` verdrahtet — als hochziehbare Basis.
- **Protokoll-Ränder** (`ODataServletTest`): `OData-Version: 4.0`-Downgrade, `$orderby` als IR +
  parse-or-fail→400, Deep-Insert (verschachtelte Containment-Kinder erreichen das Write-Backend).
- **Client:** Real-World-`$metadata` (TripPin) parst; `nextPage`-Happy-Path; Request-Timeout +
  gedeckelter (gestreamter) Response-Read gegen OOM.
- **Write-Atomarität** (`JpaWriteRollbackTest`): ein Fehler zwischen `begin()` und `commit()` lässt
  keinen Teilzustand zurück (beobachtet, nicht nur Exception-Typ).

**Bewusst offen (nächste Runde):** In-Memory-Backend ist NICHT subtyp-polymorph (`entities(Typ)`
liefert nur den exakten Typ; JPA liefert Subtypen) — Base-Type-Queries divergieren, daher aus dem
Differenz-Corpus ausgeklammert. Ebenso: `Edm.Int64` `IEEE754Compatible`-Aushandlung, System-weite
Concurrency-/Fuzz-Tests, 406/`return=`-Preference.

## Review-Notizen (2026-07-03)

**SOLID:** SRP durch Bundle-Schnitt + Servlet-Extraktion (`RequestLimits`/`EntityShaper`/
`ODataJson` einzeln testbar). OCP über SPIs (`QueryService`, `EntityRepository`,
`AspectProvider`) und Whiteboard — neue Backends/Modelle ohne Core-Änderung. DIP: runtime kennt
nur `persistence.api`; csdl kennt keinen Metadata-Service (ADR-0003/0004). LSP/ISP: kleine
Interfaces, `executeApply` als optionale Default-Methode (501-Mapping statt Zwangs-Implementierung).

**Java 21:** Records für Wertobjekte (EntityQuery/ApplyQuery/RequestLimits/OrderBySegment),
Pattern-Switches über AST/Transformationen (Evaluator/TypeResolver/ApplyExecutor),
Pattern-Matching in instanceof durchgängig. Keine Virtual-Thread-Nutzung (Servlet-Container-Sache).

**Memory:** `ODataJsonResourceImpl.RESOLVED` auf WeakHashMap umgestellt (Profile referenzieren
das EPackage nicht → sammelbar). `CachingODataQueryParser`: weak keys reichen NICHT (AST →
referredProperty → EClass = value→key); begrenzt durch LRU-Kapazität, echtes Freigeben via
`invalidate(EClass)` — übernimmt der Provider-Adapter beim Package-Unregister (ADR-0004 Phase 2,
offen). `FileEntityRepository` hält seine Resources bewusst (Lebensdauer = Komponente).

**Servlet-Filter (req §5.1.1):** Die Limits-+Parse-Validierung gehört perspektivisch in einen
vorgeschalteten `ODataRequestFilter` (Whiteboard-Filter), damit sie vor JEDEM OData-Endpoint
läuft und der Dispatcher schlank bleibt. Die Extraktion von `RequestLimits` +
`CachingODataQueryParser` ist genau dieser Schnitt — der Filter ist damit ein reines
Verpacken (offener Punkt, kein Blocker; aktuell macht das Servlet beides in fester Reihenfolge).

**Testabdeckung (Stand 2026-07-06 EOD):** csdl 13 · metadata.tests 4 (OSGi) · query 929 (30 Unit +
899 OASIS-ABNF-Fälle: 229 Core-XML + 504 Core-YAML (aktuelle TC-Fälle inkl. 4.02-Vorarbeiten,
davon 194 aktiv) + 166 Aggregation-YAML; aktive/Skip-Zählung = Backlog-Radar) ·
codec.json 2 · vocabularies 4 · inmemory 7 · **persistence.jpa 8 (Differenzial gegen H2,
EclipseLink-Dynamic-Entities, inkl. $apply-Pushdown)** · runtime 24 · itests 7 (echtes HTTP). Jacoco ist workspace-weit aktiv (fennecJacoco). Bekannte Lücken:
OSGi-Test für codec.json/vocabularies-Komponente, `EntityShaper` nur indirekt über
Servlet-Tests abgedeckt, Client (E8) und Schreibpfad ungetestet weil nicht vorhanden.

## Stand & Wiedereinstieg (2026-07-06)

Alles grün: `./gradlew clean build testOSGi` über 11 Bundles + Example. Der read-only
Server-Slice E1–E7 läuft e2e über echtes HTTP; **4.0 Minimal Conformance erfüllt, 4.01
Minimal im Wesentlichen** — dort offen nur Capabilities-Annotations (SHOULD) und
präfixlose Preference-Namen (Preferences werden in v1 nicht ausgewertet).

2026-07-06: Core-ABNF-YAML-Harness übernommen (`CoreYamlAbnfAcceptanceTest`, 504 Fälle,
Details in `odata-spec-repos-gap-analysis.md`); dabei zwei Fixes: `$count`/`$value`/`$ref`
jetzt als terminale Pfadsegmente erzwungen, `in ()` (leere Liste) in Grammatik + Builder.

2026-07-06 (2): **4.01-Minimal-Paket — 4.01 Minimal (read-only) damit erfüllt**
(`odata-conformance-status.md`): Parameter-Aliase `@name` in `$filter`/`$orderby`
(Grammatik-Token ALIAS, rekursive Wert-Auflösung im Parser, Servlet sammelt `@`-Parameter,
Cache-Bypass bei Aliasen), `divby` (→ OCL `/`), `Prefer: odata.maxpagesize`/`maxpagesize`
+ `Preference-Applied`, Capabilities-Annotations am Container (ConformanceLevel=Minimal,
Batch/Async/KeyAsSegment=false) inkl. Capabilities-`edmx:Reference`. Code auf GitHub:
`eclipse-fennec/emf.odata`, Branch `initial` (Stand vor diesem Paket).

2026-07-06 (4): **Rest-Intermediate-MUSTs — 4.01 Intermediate (read-only) im Wesentlichen
erfüllt**: `eq/ne null` auf Single-Navigationen funktionierte bereits end-to-end (per
Regressionstest belegt); **nested `$select`** neu: `SelectTree` (Parser + Modell-Validierung,
Klammer-bewusstes Splitting, nur `$select`-Sub-Optionen — Rest klar abgelehnt),
`EntityShaper.prune()` rekursiv über strukturierte Werte, `$select`-Werte durchlaufen
`RequestLimits.checkExpression`. Offene Intermediate-SHOULDs: `$search`, `$filter` auf
expandierten Entities, count-of-filtered-collection, `$compute`.

2026-07-06 (3): **Vererbung/Derived Types — 4.0 Intermediate (read-only) im Wesentlichen
erfüllt** (Intermediate-Plan Schritt 1, bewusst VOR dem JPA-Backend, damit das JPA-Mapping
gegen das vollständige IR entsteht): CSDL-BaseType-Round-Trip war vorhanden (E2); NEU:
Cast-Segmente im URI-Parser (`Set/Ns.T`, `Set/Ns.T(key)`, Casts in Nav-Pfaden, max. einer
pro Schritt), `EntityQuery.castType` (SPI — JPA mappt später auf `TYPE()`/Discriminator),
InMemory `isInstance`-Filter, Servlet-Cast-Routing (abgeleiteter Typ = Options-Kontext,
Context-URL `#Set/Ns.T`, 404 bei Typ-Mismatch, `$apply`+Cast → 501),
`#Ns.Type`-Discriminator im minimal-metadata-JSON (`ODataJsonResourceImpl.typeDiscriminator`),
webshop-Fixture: `DiscountedProduct extends Product`.

2026-07-06 (5): **JPA-Backend v1 (E5) — Q21 entschieden (ADR-0006)**: neues Bundle
`org.eclipse.fennec.odata.persistence.jpa` — `JpaQueryService` konsumiert
`EntityManagerFactory`-Services (Fennec Persistence JPA, dynamische EMF-Entities) und
übersetzt das OCL-IR per `OclToCriteriaTranslator` in Jakarta-Criteria-Queries: Vergleiche
(inkl. `IS [NOT] NULL` auf Navigationen), Logik, Arithmetik (`divby`), String-/LIKE-Funktionen
(Wildcards escaped, 0-basiert→1-basiert verschoben), `in`, Pfad-Joins, Lambdas →
korrelierte `EXISTS`-Subqueries, `castType` → `treat()` + `TYPE()-IN`, Paging
(`setFirstResult`/`setMaxResults`), `$count` als separate COUNT-Query. Literal-Koersion auf
den Attribut-Java-Typ (ISO-Datumsstrings, BigDecimal, Enum-Namen). Ohne Übersetzung →
`UnsupportedOperationException` → Servlet 501 (nie still falsch); `executeApply` = Folge-AP.
Differenzialtests gegen H2 spiegeln die In-Memory-Referenz. Dabei ZWEI Bugs in
`emf.persistence-jpa` gefixt (Feature-Branch `fix/metamodel-refresh-dynamic-types`, je Fix
ein Test, NICHT gepusht): Metamodel-Sichtbarkeit dynamischer Typen (Criteria ging gar nicht)
und `EBigDecimal`-Nachkommastellen-Verlust (NUMERIC-Scale-0 → (38,19)-Default).

2026-07-07: **`executeApply`-Pushdown (JPA)**: `JpaApplyExecutor` übersetzt die Pipeline in
EINE gruppierte Criteria-Query — `filter`-Stufen vor dem Groupby → WHERE, Groupby+Aggregate
→ GROUP BY mit Aggregat-Selections (sum/min/max/average/countdistinct/$count),
`filter`-Stufen danach + Post-Pipeline-`$filter` → HAVING (Aliase UND Gruppierungspfade
lösen auf die Criteria-Ausdrücke auf), `$orderby`/Paging in der DB. Gruppierungspfade via
LEFT JOIN (Null-Navigationen bilden eigene Gruppe, wie die In-Memory-Referenz);
Row-Shape identisch (verschachtelte Maps + Alias-Keys). Gruppen-`$count` = schlanke
Key-only-Query. Ohne Pushdown (compute, Mehrfach-Groupby, …) → UOE → 501.

2026-07-07 (2): **Read-Path-Performance-Paket** — deterministische SQL-Count-Tests statt
Wanduhr (Zähl-SessionLog im H2-Harness): dabei ECHTES N+1 GEFUNDEN UND GEFIXT — eager
Collection-Features (reviews-Containment, tags-ElementCollection) luden pro Zeile (41
Statements für 20 Entities); jetzt EclipseLink-BATCH-IN-Hints für ALLE to-many-Features
(paging-sicher) + LEFT-Fetch-Join für single-valued `$expand`-Navigationen → konstant
1 Haupt-SELECT + 1 Batch pro Collection-Feature (+1 für `$count`). Dafür SPI erweitert:
**`EntityQuery.expand`** (Backends MÜSSEN expandierte Navigationen effizient materialisieren
— keine Lazy-Proxies nach außen); Servlet reicht `$expand` durch (Set + Einzel-Entity).
Dazu: `ParserWorstCaseTest` (pathologische Inputs an den Request-Limits mit Zeit-Schranke)
und `JpaScalePerfTest` (@Tag("perf"), `./gradlew perfTest`, nicht im Normal-Build): 50k
Zeilen — Statement-Zahlen konstant, Paged Read/Groupby-Aggregation in Sekundenbruchteilen.
OFFEN dabei notiert: Navigation-WALKS (`Set(1)/nav/...`) auf dem JPA-Backend berühren lazy
Referenzen nach EM-Close — braucht eigene Materialisierungs-Strategie (Folge-AP).

2026-07-07 (3): **Walk-Materialisierung**: `EntityQuery.expand` akzeptiert jetzt auch
slash-separierte Navigations-PFADE; das Servlet schickt den Navigations-Präfix eines
Resource-Pfad-Walks (`Set(key)/nav1/nav2/attr`, Cast-Segmente wechseln den Kontexttyp) als
Prefetch-Hint mit. JPA-Backend: single-valued Ketten als verschachtelte, per Präfix
deduplizierte LEFT-Fetch-Joins; to-many-Segmente per Batch-Hint; Materialisierungs-Walk
(descend) berührt die Kette solange die Session offen ist. Der zuvor dokumentierte
Korrektheits-Bug (Walk auf lazy Referenzen nach EM-Close → unaufgelöste Proxies, per rotem
Test belegt: `name = null`) ist damit geschlossen. Fixture: Category.parent-Selbstreferenz
für Ketten der Tiefe 2.

2026-07-07 (4): **Write-Path v1 (OASIS "Updatable Service", Teilmenge)**: neue SPI
`WriteService` (create/update/delete; Payload = EObject mit eIsSet = "war im Payload";
PATCH merged, PUT ersetzt, Update auf unbekannten Key = Upsert; Konflikte →
`WriteConflictException` → 409). Servlet: POST Set → 201 + Location/OData-EntityId +
Entity-Body, PATCH/PUT Entity → 204 (Upsert → 201), DELETE → 204/404; Guards: 415
(Media-Type), 413 (Body-Limit `odata.max.body.size`), 400 (leer/malformed), 405 (falsches
Ziel/kein Write-Backend), 501 (unterhalb Entity-Ebene). Payload-Dekodierung über den
E3-Codec (`CODEC_ROOT_TYPE`). Backends: `MemoryWriteRepository` (Referenz,
EntityRepository+WriteService über demselben Store) und JPA (`JpaQueryService` implementiert
WriteService: Instanzen via Descriptor-InstantiationPolicy, Attribute + Containment-Kinder
rekursiv [= Deep Insert], Transaktionen, Duplikat-Check → 409). e2e-Write-Roundtrip über
echtes HTTP in den itests. 2026-07-07 (5): $ref-Operationen (link/unlink/createRelated in der SPI, Default-UOE→501),
ETags/If-Match (schwache Hashes, 428/412) und Property-Level-Writes (Replace-basiert wegen
EMF-eIsSet-Semantik) nachgezogen — **„Updatable OData Service“ 4.0+4.01 damit beanspruchbar**
(Details `odata-conformance-status.md`). e2e prüft ETag-Pflicht über echtes HTTP.

2026-07-07 (6): **Kleinere offene Punkte geschlossen**: (a) `$apply`-**compute-Pushdown** im
JPA-Backend (`JpaApplyExecutor`: compute-Aliase landen VOR Grouping/Aggregat/HAVING/orderby
in der `named`-Scope; terminales compute selektiert alle single-valued-Attribute + Aliase;
Post-Pipeline-Filter ohne Grouping → WHERE statt HAVING; compute NACH groupby weiterhin ehrlich
UOE→501). (b) **OSGi-Verdrahtungstest** `JpaWiringIntegrationTest` (itests, eigenes
`wiringshop.ecore`): EPackage-Service + ConfigAdmin (daanse-H2-DataSource +
`fennec.jpa.PersistenceUnit` mit per `EntityMapper` generierter Mapping-Datei) →
`EntityManagerFactory`-Service → `JpaQueryService` bindet dynamisch (Service-Property
`fennec.odata.backend=jpa`) → Write+Read über die Services. (c) **negatives substring**
(4.01-SHOULD): negativer Start zählt vom Ende, geklemmt; Start > Länge → Leerstring; in-memory
UND JPA (CASE über LENGTH). (d) **`@odata.bind`** in Write-Payloads (POST/PATCH/PUT): Members
werden vor dem Codec extrahiert/validiert (Nav-Typ-Prüfung, Ziel-URL gegen den Zieltyp) und
nach dem Write als `link()`-Operationen angewandt; unterhalb der Entity-Ebene 501.
(e) **`$filter` in `$expand`** (`reviews($filter=stars ge 4)`): Klammer-/Literal-bewusstes
Item-Splitting, nested Expression parst gegen den ZIELTYP, Filterung läuft auf den GESHAPTEN
Kopien (Backend-Objekte unangetastet, Prefetch unverändert); andere nested Optionen → 501,
`$filter` auf single-valued Navs → 400. Dafür zog `OclEvaluator` von persistence.inmemory
nach `org.eclipse.fennec.odata.query` um (Referenz-Semantik der IR gehört zum IR-Bundle).

2026-07-07 (7): **E9-light — Akzeptanz mit echtem Fremd-Tooling**
(`org.eclipse.fennec.odata.example/acceptance/`): python-odata (unabhängiger V4-Client von
PyPI) reflektiert unser `$metadata` zu Entity-Klassen, liest/filtert/sortiert über seine
eigene Query-DSL und legt per POST an; ETag-Handshake (GET → If-Match → DELETE, 428 ohne
Precondition) über rohes HTTP. Die offizielle OASIS-`V4-CSDL-to-JSONSchema.xsl`
(odata-json-schema) verdaut unser `$metadata` (214 Typ-Definitionen) und alle Live-Payloads
(Collection, Single, `$filter`, `$expand`, `$select`) validieren gegen das generierte Schema.
**Fund + Fix**: der Fennec-Codec leckte ein internes `_id`-Feld in die minimal-metadata-
Payloads — `useId(false)` schaltet nur die Strategy, der Serializer gated aber auf den
KeyMode → `idKeyMode("FEATURE_ONLY")` in `ODataJsonResourceImpl` (Key-Property bleibt,
Kontrollfeld weg; Regressionstest im codec.json-Bundle). Hinweis pyodata (SAP): V2-only,
daher python-odata als V4-Werkzeug.

2026-07-07 (8): **E8 — Client-Grundstein** (neues Bundle `org.eclipse.fennec.odata.client`):
`ODataClient.connect(serviceRoot)` lädt `$metadata` über `java.net.http` (bewusst KEIN
JAX-RS-Client: nur-API + Impl-Stack-Zwang, Jersey-in-OSGi-Schmerzen — gleiche Linie wie
ADR-0001 serverseitig; Transport liegt hinter der `fetch`-Naht, `HttpClient` injizierbar)
und konvertiert es über den E2-Read-Pfad zu Ecore. NEU dafür:
`EdmToEcoreConverter.toEPackages(TEdmx)` — Multi-Schema-Konvertierung mit
**Cross-Schema-Auflösung** von Navigationszielen über qualified names (Namespace ODER
Alias); Unauflösbares wird ENTFERNT statt als EReference ohne Typ liegen zu bleiben (das
NPE-te vorher in der Metadata-Registrierung). Fluenter `EntitySetRequest` (filter/orderBy/
top/skip/count/select/expand, `get(key)`, `totalCount()`, `nextPage()`) mit exaktem
URL-Encoding; Antworten dekodieren durch DASSELBE Codec-Profil wie der Server (Envelope via
Jackson, `@odata.*`-Kontrollinfos werden vor dem Decode gestrippt). Metadata-Verdrahtung
entkoppelt wie im Server: `MetadataWhiteboard` injizierbar (OSGi), Default ist eine
ISOLIERTE Plain-Java-Instanz (Remote-Packages landen nie im geteilten Whiteboard).
Tests: Unit gegen HTTP-Stub (CSDL aus echtem E2-Write-Pfad, URL-Assertions, Fehler-Mapping)
+ e2e in den itests: unser Client konsumiert unseren Server über echtes HTTP
(Discovery, Filter-Pushdown, Single-Entity, `$count`, 404/400-Mapping).

Nächste Schritte (Priorität, Intermediate-Plan 2026-07-06):

1. ~~JPA-Backend Rest~~ ✅ 2026-07-07: OSGi-Verdrahtungstest UND compute-Pushdown (s.o.).
   Datums-Funktionen ✅ 2026-07-07: year/month/day/hour/minute/second → SQL EXTRACT über
   die EclipseLink-Expression-Brücke (JpaCriteriaBuilder.toExpression/extract/fromExpression
   — jakarta CriteriaBuilder.extract existiert erst ab Persistence-3.2-Implementierungen);
   date()/time() (ISO-String-Formen) bleiben ohne Pushdown → 501.
2. ~~Rest-Intermediate 4.01 (MUSTs)~~ ✅ 2026-07-06 (eq/ne null belegt, nested `$select`
   gebaut); `$filter` auf expandierten Entities ✅ 2026-07-07 (s.o.); verbleibende
   Intermediate-SHOULDs: Query-Optionen auf Nav-Pfaden, `$search` (s. Punkt 8), `$compute`.
3. `ODataRequestFilter` als Whiteboard-Filter um `RequestLimits` (req §5.1.1, reines Verpacken).
4. Nested `$expand`; `@odata.context`-Entity-URL-Formen.
5. Aggregation-Ausbau entlang der 135 Harness-Skips (`AggregationAbnfAcceptanceTest`):
   topcount/…, concat, rollup, from, Custom Aggregates, `$these`.
6. CSDL-JSON (Q9) — Validierungsbasis `csdl.schema.json` liegt in `reference/specs/`.
7. OSGi-Tests für codec.json/vocabularies; E4 AP-10 (bound functions); E2 AP-2/4/6.
8. `$search` zuletzt (Intermediate-SHOULD), erst mit echter Pushdown-Story.

## Backlog-Block „Punkte 2+6“ (2026-07-10)

Zehn verschobene Kleinpakete geschlossen (alles grün: `build testOSGi`, 16 Bundles):

1. **$apply-Erweiterungen (E4):** Grammatik jetzt prädikat-gesteuert (Soft-Keywords via
   `_input.LT(1)`), Submodell + In-Memory-Ausführung für `topcount/topsum/toppercent`
   (+bottom-Spiegel), `concat`, `top/skip`, `orderby`, `identity` und `rollup`-Grouping-Sets;
   `aggregate … from`, Custom-Methoden (`with Ns.Method`), Custom-Aggregates und benannte
   Hierarchien parsen ins Modell und werden in BEIDEN Backends ehrlich mit 501 beantwortet.
   `path/$count`-Aggregate (Summe der Größen) in-memory. Aggregation-ABNF: 82 statt 31
   verifiziert (84 Skips = Radar). JPA: rollup/from/custom → UOE.
2. **Echtes `odata.metadata=none`:** alle Envelope-Stellen über `envelopeHead()/withContext()`
   — kein `@odata.context`, kein Typ-Diskriminator; `@odata.count`/`@odata.nextLink` bleiben.
   ETag ist jetzt auf die kanonische (minimal-)Serialisierung GEPINNT, variiert also nicht
   mehr mit dem angefragten Level.
3. **IEEE754Compatible:** Codec-Opt-in (`ODataJsonResourceImpl.ieee754Compatible(true)`),
   Server verhandelt über Accept/`$format`, echot den Content-Type-Parameter, `@odata.count`
   und $apply-Zeilen als Strings, Write-Payloads mit deklariertem Parameter dekodieren exakt.
   Nebenfund: der Codec schrieb `Edm.Decimal` IMMER als String — jetzt default JSON-Zahl
   ([OData-JSON] 7.1), Strings nur unter IEEE754Compatible.
4. **Key-as-Segment ([OData-URL] 4.3.3, Graph-Stil):** Grammatik-`KeySegment` für Literale,
   Servlet-Normalisierung `keyAsSegment()` (deklarierte Properties GEWINNEN die Ambiguität,
   unbekannte Namen auf Collections falten als String-Key, quotiert zum Backend). Gilt für
   GET und Writes. Nebenfix: `contextRoot()` = RequestURI minus PathInfo (Context-URLs auf
   mehrsegmentigen Pfaden waren schief).
5. **Rich-Expression-Annotations (E2 AP-5-Rest):** `CsdlAnnotationExpressions` mappt
   `<Record>`/`<Collection>`/Path-Formen/`<EnumMember>` ↔ kompaktes [OData-CSDL-JSON]-
   Value-Encoding als EAnnotation-Detail-String — dieselben Knoten speisen CSDL-XML UND
   CSDL-JSON (Writer/Reader). Jackson bleibt optional (`JacksonSupport.PRESENT`-Gate).
6. **`ODataRequestFilter` (req §5.1.1):** Whiteboard-Servlet-Filter vor dem Servlet,
   erzwingt die RequestLimits (Expression-Länge/-Tiefe aller Options + @Aliase, Pfadlänge)
   als 400 VOR jeder Dispatch-Arbeit; Servlet-Guards bleiben (Defence in depth).
7. **OSGi-Tests codec.json + vocabularies** (in `metadata.tests`): odatajson-Factory über
   Whiteboard + Vocabulary-EPackage-Services. FUND dabei: Vocabulary-Bootstrap ließ
   Cross-Vocabulary-Referenzen (Capabilities→Core) unaufgelöst → NPE in der Metadata-
   Registrierung. Fix: Abhängigkeits-geordnetes `loadAll()` + öffentliches
   `EdmToEcoreConverter.resolveReferences(pkg, byNamespace)` (Rest wird entfernt, wie E8).
8. **E4 AP-10 Bound Functions in Member-Pfaden:** `boundCall`-Segmente (qualifiziert ODER
   unqualifiziert, named/positional Args) → `OperationCallExp` mit qualifiziertem Namen,
   eager gegen `EClass.getEAllOperations()` aufgelöst, Args in Deklarationsreihenfolge;
   `/$count`- und Lambda-Tails auf Operations-Ergebnissen. XML-ABNF: 161 statt 126 verifiziert.
9. **E2 AP-4/6-Reste:** SRID- (`OData.SRID`, numerisch oder symbolisch `variable` →
   TVariable-Enumerator!) und Unicode-Facette beidseitig; ALLE Facetten kommen beim Read
   jetzt als `@OData.*`-Details zurück (Round-Trip statt verlustbehaftet). Cross-Package:
   `typeName`/`baseType` qualifizieren mit dem Namespace des ZIEL-Pakets (`typeNamespace()`).
   Profile-Modell-Kopien in emf.odata.metadata synchronisiert.
10. **AP-1c-Bereinigung:** die nie befüllten resolved-Felder (`kind`, `qualifiedName`,
    `keyPropertyNames`, `openType`, `hasStream`, `baseTypeQualifiedName`) am metadata-seitigen
    `ODataClassProfile` entfernt — Single Source ist der csdl-Profil-Cross-Ref.

**BUILD-GOTCHA (Ursache der „zufälligen“ resolve.test-Fehler, bestand schon vor dem Block):**
bnd-`-generate` (und IDE-Builds) löschen `generated/buildfiles` — den Marker, über den das
Workspace-Repository die Bundles eines Projekts sieht. Ist das jar-Task gleichzeitig
gradle-up-to-date, wird der Marker nie neu geschrieben → Bundle unsichtbar → Resolution
schlägt mit wechselnden „cannot be resolved“ fehl. Guard im Root-`build.gradle`:
`jar` gilt nur als up-to-date, solange `generated/buildfiles` existiert. Parallel laufende
Eclipse-IDE auf demselben Workspace verschärft das (baut Jars ohne Marker).

### Harness-Bereinigung: Skip-Semantik geschärft (2026-07-10, Nachtrag)

Die drei ABNF-Harnesses unterscheiden jetzt ZWEI Auslassungs-Arten mit klarer Semantik:
`OUT_OF_SCOPE` = das Thema gehört einer ANDEREN Schicht (Percent-Encoding = URL-Decoding,
Nicht-Expression-Options/$search/Funktions-Routing = Servlet, Modell-Kategorie-Negative =
syntax-only nicht beurteilbar) → diese Fälle werden GAR NICHT mehr generiert (dokumentierte
Auslassung, `System.out`-Zeile pro Factory). `BACKLOG` = geplante Parser-Features →
weiterhin Assumption-Skip als Radar. Dabei die im Core-YAML-Harness STALE gewordenen Skips
freigeschaltet (Key-as-Segment, Compound-Keys, AP-10-Funktionen in Pfaden, positive
odataRelativeUri deren Query-Teil fremd ist — der geprüfte PFAD zählt jetzt).

Stand: 716 generierte ABNF-Fälle, 505 verifiziert (vorher 437), 211 Skips — und JEDER
verbleibende Skip ist echtes Backlog: 111 Expression-Grammatik (E4: $it/$root, Casts in
Pfaden, JSON-Literale, Geo/Binary/NaN, Enum-Flags, unäres Minus, @Ns.Term), 73 $apply-
Submodell (search/nest/join/traverse/rolluprecursive, Custom-Functions, $these), 27
Resource-Path-Parser (Key-Aliase, $crossjoin/$all/$entity, roher Apostroph im Key-Segment).

## Delta/Change-Tracking (2026-07-13)

Die letzte beidseitige Client/Server-Lücke ist geschlossen ([OData-Protocol] 11.3,
[OData-JSON] Delta Payloads):

- **SPI `DeltaService`** (persistence.api, Muster `MediaService`): `trackingToken(EClass)` →
  opaker Token („jetzt“), `changesSince(EntityQuery, token)` → `DeltaResult` (Upserts als
  EObjects im aktuellen Zustand, `Removal`s mit Key-Werten + Grund, Folge-Token).
  `DeltaGoneException` → 410. Kein Backend-Support ⇒ Preference wird schlicht nicht angewendet.
- **Zustandsloses Server-Design**: Der Delta-Link ist SELBSTBESCHREIBEND — er re-encodiert die
  Defining-Query-Optionen (`$filter`/`$search`/`$select`/`$compute` + `@`-Aliase) um den
  `$deltatoken`; der Server hält KEINEN Client-Zustand. Token = Journal-Sequenznummer (opak
  dokumentiert). `$top/$skip/$count/$orderby` werden NICHT encodiert (Spec-MUST/SHOULD),
  fremde Optionen auf Delta-Links → 400, `/$count` auf Delta-Link → 501, keyed Resource mit
  Token → 400 (Nav-Pfade laufen in den bestehenden Options-Guard → 501).
- **Servlet**: `Prefer: odata.track-changes` (präfix-optional) ⇒ `Preference-Applied` +
  `@odata.deltaLink` STATT nextLink auf der letzten Seite; Token wird VOR der Query gezogen
  (Race: Write zwischen Snapshot und Antwort wird im ersten Delta erneut gemeldet statt
  verloren). Delta-Antwort: Kontext `#Set/$delta`, Removals versionsabhängig — 4.01
  `@removed`+`@id`, 4.0 `#Set/$deletedEntity`+`id` (negotiateVersion). 410 mit Refetch-URL in
  `Location`. `$metadata`: `Capabilities.ChangeTracking`-Record am Container (echter
  Backend-Support). `$expand`+track-changes ⇒ Preference nicht angewendet (v1).
- **In-Memory-Journal** (`MemoryWriteRepository`): bounded Deque (10k), Einträge (seq, Store-
  EClass, Store-Key, Key-Werte, deleted) bei create/update/delete (link/unlink NICHT — Spec:
  nur strukturelle Änderungen). TX-fest: Änderungen in offener `$batch`-Transaktion werden
  thread-lokal gepuffert, Seq-Vergabe erst beim COMMIT (sonst könnten parallel gezogene Tokens
  uncommittete Änderungen überspringen), Rollback verwirft. Membership: Filter/Cast der
  Defining Query via `OclEvaluator` — geändert-und-passt ⇒ Upsert, passt-nicht-mehr ⇒
  `@removed reason=changed`, gelöscht ⇒ `reason=deleted`; Mehrfachänderungen kollabieren auf
  den letzten Zustand; polymorphe Sichtbarkeit (Derived-Instanzen im Basis-Set).
  Retention-Fenster überschritten ⇒ ehrlich 410 statt stiller Lücken.
- **Client**: `trackChanges()` (Prefer-Header), `ODataPage.deltaLink()` (neue Record-
  Komponente, Kompat-Konstruktor), `changes(deltaLink)` → `ODataDelta` (Upserts typisiert,
  `Removal(id, reason)`); Decoder versteht BEIDE Wire-Formen inkl. 4.01-präfixfreier
  Control-Annotations; 410 → `ODataClientException.status()==410`.
- **Tests**: inmemory 6 (Journal/Filter-Membership/Kollaps/Polymorphie/TX/Gone inkl.
  10k-Eviction), runtime +7 (Preference, beide Payload-Formen, 410+Location, Guards,
  $metadata-Capability), client +3 (Roundtrip, 4.0-Form, 410), itests e2e-Roundtrip
  (track → create → delta → delete → `@removed`, echter HTTP-Stack). GOTCHA erneut bestätigt:
  itests laufen gegen die BUNDLE-JARS — nach Servlet-Änderungen `:runtime:jar` bauen, sonst
  testet man den alten Stand.
- **Bewusst v1-außen-vor** (dokumentiert in features/conformance): `$expand`-Deltas (nested
  `@delta`/Links), `PATCH`-Collection-Update (`"@context":"#$delta"`), Delta-Paging,
  `/$count` auf Delta-Links, JPA-`DeltaService` (bräuchte DB-Change-Log/Envers-Äquivalent).
