# Fennec OData – Offene Aufgaben

Status: 2026-07-15. **Das eine Aufgaben-Dokument**: alle offenen Punkte — Spec-Lücken,
Backlog-Reste, Findings, offene Fragen — unabhängig von ihrer Herkunft. Konsolidiert aus den
früheren Backlog-Dokumenten (`odata-e2-converter-open-points.md`, `odata-e4-query-open-points.md`,
`odata-e5-e6-server-state.md`, `odata-spec-repos-gap-analysis.md`, Requirements-Doc §7 —
alle entfernt, Historie in Git). Architektur: `odata-architecture.md` ·
Conformance-Nachweis: `odata-conformance-status.md` · Features: `manual/06-feature-matrix.md`.

Konvention: Punkte, die eine **User-Entscheidung/-Aktion** brauchen, stehen zuerst. Alles
andere ist nach Komponente gruppiert. Erledigtes wird GELÖSCHT, nicht durchgestrichen
(Nachweis führt `odata-conformance-status.md` bzw. Git).

---

## 1. Braucht den User (Entscheidung oder externe Ressourcen)

| # | Aufgabe | Kontext |
|---|---|---|
| U1 | **Interop-Eskalation**: Microsoft-Graph-Test (braucht OAuth-Zugang), SAP-V4-Endpoint (Demo-Account, ehem. Q18), XOData/Power BI/Excel als Fremd-Clients gegen unseren Server (ehem. Q4) | ZURÜCKGESTELLT 2026-07-17: keine Zugangsdaten/Portale verfügbar. Live-Suite deckt TripPin/RESTier/Demo/Northwind ab; die kommerziellen Ökosysteme fehlen |
| U2 | **Release auf `main`** (Central-Release via `release.yml` steht bereit; Branch `initial` bleibt unangetastet) | ZURÜCKGESTELLT 2026-07-17. CI/Versionierung verifiziert, nur der Push/Merge ist User-Sache |

*(U3 BSI-TRs und U4 Limit-Defaults: 2026-07-17 vom User bestätigt — dokumentiert in
`manual/04-configuration.md` (Crypto-Baseline-Zeile bzw. Default-Bestätigungsabsatz);
Defaults sind Startwerte, Feintuning zeigt die Praxis. Einträge gemäß Konvention gelöscht.)*

## 2. Spec-Lücken (Conformance-Kür)

Alle Level 4.0+4.01 Minimal–Advanced sind beansprucht; das hier ist der Rest darüber hinaus:

- **`$crossjoin`/`$all`/`$entity`-Engine** — der EINZIGE offene Spec-SHOULD (13.1.3/15).
  Parst bereits → 501; die Engine bräuchte einen synthetischen Tupel-EClass als
  Ausdruckskontext. Bewusst eigenes Paket.
- **Delta-Wire-Formen**: nested `nav@delta` (wir emittieren die spec-legale
  Full-Representation), 4.0-flattened-Delta-Payloads, `continue-on-error`,
  `@odata.bind` im `#$delta`-PATCH-Payload (alles ehrliche 501/nicht-angewendet).
- **Ausdrucks-Restlücken** (die 13 verbleibenden ABNF-Skips von 710 Fällen):
  Geo-Literale (`geo.*` — eigenes Spatial-Paket nötig), 4.02 `case()`,
  roher Apostroph im Key-Segment (URL-Decode/Lexer-Randfall).
- **`$root`**-Backend-Semantik und **`@Ns.Term`**-Laufzeitwerte in Ausdrücken
  (beides parst → 501; Termwerte bräuchten Vokabular-Auswertung zur Laufzeit,
  niedriger Praxisnutzen).
- **Strukturelle Vergleiche** (§13.2.2/7, 4.01-Intermediate-SHOULD): NICHT unterstützt
  (2026-07-15 auditiert). Ein JSON-Objekt-Literal wird als opaker JSON-Text behandelt
  (`ODataToOclBuilder.visitJsonObjectPrimary`), die Evaluator-Gleichheit kennt keinen Pfad
  für komplexe/Entity-Instanzen — `complexProp eq {…}` ist immer `false`. Bräuchte
  strukturelle Wertgleichheit über EObjects.

## 3. Backend-Pushdown (ehrliche 501er, nach Praxisnutzen priorisieren)

**JPA** (`OclToCriteriaTranslator`/`JpaApplyExecutor` — Übersetzungslücken werfen UOE → 501,
nie still falsch). Verbleibend:
- `date()`/`time()` (die ISO-String-Formen; `year`…`second` sind gepusht). Grund für die
  Zurückhaltung: die In-Memory-Referenz liefert ISO-STRINGS, ein SQL-DATE/TIME-Cast müsste
  bit-genau dieselbe Darstellung erzeugen — Divergenzrisiko, niedriger Praxisnutzen.
- **Mehrfach-`groupby`** (mehr als eine Grouping-Stufe) — braucht verschachtelte Subqueries.
- **`rollup`-Grouping-Sets, `aggregate … from`, Custom-Aggregates/-Methoden** — die Jakarta
  Criteria API hat kein portables `GROUPING SETS`/`ROLLUP`; genuin nicht portabel abbildbar,
  bleibt bewusst 501.

*(Geschlossen 2026-07-15: gefilterter/gesuchter `$count` in Ausdrücken → korrelierte
COUNT-Subquery; Casts in Ausdrucks-Pfaden `Ns.Sub/prop` → `treat()`; `compute` NACH `groupby`.
GOTCHA dabei — EclipseLink: `greaterThanOrEqualTo`/`lessThan` u. a. casten auf `ExpressionImpl`
und lehnen eine `SubQueryImpl` ab; die numerischen `ge/gt/lt/le`-Overloads casten auf
`InternalSelection`, das die Subquery implementiert. `treat()` in einem OR liefert korrekt
`null` (3VL), statt die ganze Query auf den Subtyp einzuschränken — per OR-Probe verifiziert.)*

**$apply-Struktur-Transformationen** (beidseitig parse→501; vor dem Bau Praxisnutzen prüfen —
braucht RecHier-Modelle bzw. Operations-Dispatch): `search`, `nest`/`addnested`,
`join`/`outerjoin`, `ancestors`/`descendants`/`traverse`/`rolluprecursive`,
benannte Hierarchien, `$these`-Ausführung.

**Server-501er (kleinere)**: nested `$select`-in-`$expand`; `$levels` innerhalb nested
Options; Select-`$count` unterhalb Top-Level (Splice nicht ausdrückbar); keylose
Containment-Kinder in `$ref`; keyed Inline-`$filter(…)`-Segmente; keyed Pfad-Segmente
in Ausdrücken.

## 4. Client (E8)

- **Decode von `$expand=nav/$ref`-Antworten**: Referenzen erscheinen als leere Entities —
  ein dedizierter Referenz-Objekt-Accessor fehlt.
- **Atlas-backed Schema-Registry** (ADR-0007): persistente `ODataSchemaRegistrar`/`Resolver`-
  Implementierung ist DOWNSTREAM (Model-Atlas-Repo), nicht hier.
- **Typed-Proxy-Codegen** (ehem. Q17): Idee, nie entschieden — generierte typisierte
  Client-Stubs aus `$metadata`.

## 5. CSDL-Converter (E2-Reste)

- **Operations Read-Pfad**: der WRITE-Pfad ist vollständig (Kind-Override
  `@OData.Function`/`@OData.Action`, unbound → Function/ActionImport, bound); OFFEN ist nur,
  dass `EdmToEcoreConverter` beim LESEN Functions/Actions ignoriert (keine EOperation-
  Rekonstruktion aus fremdem `$metadata`).
- **Annotations**: Ausdrucks-Arten außerhalb des Subsets (`Apply`/`If`/Casts/
  `LabeledElement`/`UrlRef`), `<Annotations Target="…">`-Blöcke (external targeting),
  Container-Level-Annotations; automatische `edmx:Reference`-Emission nur für Core.
- **OpenType/HasStream Read-Rückweg**: WRITE-Pfad erzeugt beide (`EcoreToEdmConverter`);
  OFFEN ist nur der Rückweg — `EdmToEcoreConverter` liest sie nicht wieder als
  Annotation/Profile zurück.
- **Primitive-Typ-Treue** (`EdmTypes`): `EBigInteger`→`Edm.Decimal` verlustbehaftet;
  `Edm.Guid`/`Duration`/`TimeOfDay` nur reverse — Forward-Mapping fehlt, Round-Trip nicht
  idempotent. Strategie (eigene EDataTypes?) klären.
- **Enum-Details**: `UnderlyingType`/`IsFlags` fehlen beidseitig.
- **Bindings-Ränder** (bewusst offen): Bindings auf Sets abgeleiteter Typen bei abstraktem
  Nav-Ziel; qualifizierte Binding-Pfade; Read-Pfad ignoriert Bindings (deterministisch
  regenerierbar).
- **Config-Layer**: externe/programmatische Annotation-Quellen (3-Layer-Cascade) über die
  `@OData.*`-EAnnotations hinaus.

## 6. Query/Parser & Caching (E4-Reste)

- **Cache-Lifecycle** (ADR-0004 Phase 2): `invalidate(EClass)`/`invalidateAll()` existieren
  bereits, werden aber nur von Tests aufgerufen — OFFEN ist die Anbindung ans Provider-Profil
  (Aspect-Slot, Aufruf beim Package-Unregister; weak keys reichen strukturell nicht, weil
  AST→referredProperty→EClass ein value→key hält).
- **Cache-Key-Kanonisierung** (ehem. Q20): erst bei Hit-Rate-Daten entscheiden.
- **Keyword-Kollisionen**: Properties namens `with`/`as`/`from`/Trafo-Namen im
  `$apply`-Kontext (Lexer-Keywords gewinnen derzeit).

## 7. Runtime/Infrastruktur (klein)

- Whiteboard-**Pattern/Kontext konfigurierbar** machen (derzeit fix `/odata/*`).
- **Rate-Limiting/Timeouts** auf Container-Ebene dokumentieren (Manual-Abschnitt).
- **406-Handling** prüfen (nicht-erfüllbare Accept-Header systematisch).
- Systemweite **Concurrency-/Fuzz-Tests** (bisher gezielte Unit-/e2e-Absicherung).

## 8. Cross-Repo / Persistence-Integration

**Richtungsentscheidung (User, 2026-08-03):** OData bleibt intern auf OCL; die
Fennec-Expression-IR (`emf.persistence-jpa`, expression/query/command.ecore) ist das
Grenzformat zum Storage — Übersetzung via `OclToExpr`/`ExprToOcl` (leben in der
Persistence, wandern mit jeder IR-Evolution dort mit). Die im Redesign-Doc als R10
skizzierte „Phase 2" (`ODataToExprBuilder`, OData emittiert IR direkt) ist **verworfen**,
nicht vertagt. OData ist **Use-Case-Geber** für die IR: fehlende Konstrukte werden als
**Issues in `emf.persistence-jpa`** eingekippt (keine Direktänderungen in fremden Repos);
jede IR-Erweiterung zieht **Mongo mit** (neues `QueryFeature`-Literal + Übersetzung/
Capability-Deklaration in JPA UND Mongo + Bridge + TCK). Zielbild: `odata.persistence.jpa`
entfällt zugunsten eines backend-neutralen **`odata.persistence`** über die
`QueryProcessor`/`QueryableResource`/`CommandResource`-SPI (Backend-Auswahl per
`persistence.query.backend`); Unified-Persistence-Konzept im Blick behalten
(#13-Write-Path → `CommandResource`; die Patch-Apply-Engine `ChangeTemplates` läuft dort
bereits für Attribut-Deltas auf beiden Backends, Referenz-Patching wird refused).

**Gap-Inventur (2026-08-03, Vorbedingung für #11)** — eigener JPA-Pushdown heute vs.
IR v2 (`expression.ecore`/`query.ecore`); ohne Schließung wäre #11 eine
Pushdown-Regression:

| Lücke | OData-JPA-Pushdown heute | IR v2 |
|---|---|---|
| Arithmetik `add/sub/mul/div/mod`, unäres Minus | ✅ | fehlt (bewusst v1-absent) |
| `concat`/`indexof`/`substring` | ✅ (OData-0-basiert ↔ JPQL-1-basiert gelöst) | nur TO_LOWER/TO_UPPER/TRIM/LENGTH |
| `round`/`floor`/`ceiling` | ✅ | fehlt |
| `year`…`second` (EXTRACT; EclipseLink `JpaCriteriaBuilder`) | ✅ | fehlt |
| Cast als Pfad-Segment (`treat()`, self-only) / `isof` | ✅ / ❌ | fehlt (kein Cast-Segment in `PropertyPath`) |
| `coll/$count` (SIZE) + gefiltertes `$count` (korrelierte COUNT-Subquery, vergleichbar) | ✅ | fehlt |
| `$apply` `compute` (vor/nach Grouping, terminal) | ✅ | Pipeline ohne Compute-Stage (D3 = „später") |
| HAVING (`filter` nach `groupby`, Alias-auflösend) | ✅ | FilterStage existiert; JPA-Backend deklariert `PIPELINE` nicht |
| Guid-Literal (UUID-Coercion) / Duration | ✅ / ⚠️ String | `Temporal`/`Enum` ja; Guid/Duration fehlen |
| `$orderby` über Ausdrücke (nicht nur Pfade) | ✅ | `OrderBy.path` = nur `PropertyPath` |
| `notEmpty` (IS NOT EMPTY) | ✅ | via `Exists` mit `true`-Prädikat abbildbar (klären) |

Von OData bewusst NICHT gefordert (dort selbst 501/nicht geparst): `rollup`/`from`/
Custom-Aggregates, `topcount`&Co., Geo, `case()`, `matchespattern`, `now`&Co. — keine
IR-Issues nötig. `$search` faltet unser Parser in `contains`-OR-Ketten → von
`StringMatch`+`Or` bereits abgedeckt.

Issue-Paket in `emf.persistence-jpa` (angelegt 2026-08-03): **#76** Arithmetik,
**#77** concat/indexOf/substring, **#78** round/floor/ceiling, **#79** year…second,
**#80** treat/isof, **#81** CollectionCount (plain+gefiltert), **#82** ComputeStage +
JPA-`PIPELINE` (revidiert D3), **#83** Guid/Duration-Literale, **#84** OrderBy über
Ausdrücke (niedrige Prio). Minimalset für $filter-Parität: #76–#79+#81; $apply braucht
zusätzlich #82. Neuzuschnitt von emf.odata#11 als Kommentar dort dokumentiert:
backend-neutrales `odata.persistence` (JPA UND Mongo) statt Umbau von
`odata.persistence.jpa`; Letzteres bleibt bis Feature-Parität.

**Status 2026-08-04: #76–#84 sind KOMPLETT umgesetzt und geschlossen** (upstream
2026-08-03/04, inkl. Mongo-Folgearbeiten #86 gefiltertes `$count`-Rendering und #88
treat/isof über den Codec-Typ-Diskriminator). Die IR-Vorbedingung für emf.odata#11
ist damit erfüllt; vor Start frische Persistence-Snapshots ziehen.

**#13 v1 (2026-08-04): neues Bundle `odata.persistence`** — `CommandWriteService`
implementiert den `WriteService` über die `CommandResource`-SPI (POST→`InsertCommand`,
DELETE→`DeleteCommand` mit Key-Selector als Expression-IR, PATCH/PUT→`UpdateCommand`
mit `ChangeSet`-Template: SET/UNSET einwertig, deterministisches REMOVE/ADD-Rewrite
mehrwertig; Upsert per Requery). Ehrliche 501s: Referenz-Patching/-Binding (upstream
refused), `createRelated`/`link`/`unlink`, Composite-Keys; `transactional()=false`
(je Command eine Backend-TX, kein `$batch`-Join). Erkenntnis aus der SPI-Kartierung:
**upstream existiert KEIN Change-Feed** (stream.ecore ist nur das Patch-Vokabular) —
der `DeltaService` dieses Bundles kommt erst mit #11 (Journal im Adapter + Requery
über den Read-Path), der Delta-Teil von #13 ist darauf verschoben. Offene Folgethemen:
explizite JSON-Nulls brauchen unsettable Attribute im Modell, kein ETag-Enforcement
in der v1-Engine.

**#11 Phase 1 (2026-08-05): `QueryService` über die Expression-IR** — die Komponente heißt
jetzt `CommandPersistenceService` (eine Instanz, Query+Write; Delta dockt später an) und
bedient Reads über `QueryableResource.query`: `$filter` via `OclToExpr`-Bridge (mit drei
odata-seitigen Normalisierungen: `eq/ne null`→`IsNull`, `toLower/toUpper`-Dialekt-Rename
auf AST-Kopie, String-Literal→`EnumLiteral` an enum-typisierten Pfaden), `$orderby` als
Pfad oder `SORT_EXPRESSION`, Cast als `isof`-Prädikat + `castBase` auf abgeleiteten Pfaden,
`$count` als separate countOnly-Query, Page-Cap (Default 1000, `max.page.size`), `$expand`
Level 1 als IR-Fetch-Hint plus eager Proxy-Materialisierung über den `getEObject`-Vertrag
(dedupliziert). Vorvalidierung über den `QueryProcessor` (`persistence.query.backend` per
URI-Scheme) trennt 501 (Capability) von 400 (strukturell). **Abnahmekriterium erfüllt:**
Differential-Suite `OclEvaluator` vs. IR/Memory-Engine über den edge-lastigen Korpus (38
Fälle) grün. `$apply`/`singleton` bleiben SPI-Default-501s (Phase 2 = #12-Zuschnitt).
Upstream-Findings als Issues: **#92** (Bridge-Dialekt toLower/toUpper), **#93**
(String-Literal vs. EEnum-Koersion), **#94** (Memory-`Not` zweiwertig vs. SQL-3VL —
im Korpus dokumentiert ausgenommen), **#95** (JPA-Expand nur Tiefe 1). Bekannte
dokumentierte Divergenz: Null-Platzierung bei `$orderby` (memory nulls-last, H2/Mongo
nulls-first).

**Mongo-Zweig belegt (2026-08-04, nach persistence-jpa#90+#91):** derselbe
`CommandWriteService` besteht den CRUD-Roundtrip gegen echtes MongoDB
(`MongoCommandWriteServiceTest`, Gating nach TCK-Muster: `-Dmongo.uri`/`MONGO_URI`
oder Docker/Podman-Wegwerf-Container, sonst Skip). OSGi-Verdrahtung läuft über die
neue `MongoResourceFactoryComponent` (Whiteboard, `protocol=mongodb`) — odata-seitig
nur Factory-Config `backend.uri=mongodb://<db>`. Der EclipseLink-Bug bei
ID-Gleichheits-Queries ist upstream gefixt (#91); der `getEObject`-Key-Lookup
bleibt trotzdem, er ist der billigere Pfad.

**Delta-Teil von #11 (2026-08-05): `DeltaService` im Command-Backend** — der
`CommandPersistenceService` implementiert jetzt auch Change-Tracking über das
angekündigte Muster „Journal im Adapter + Requery über den Read-Path": ein
Service-Layer-`ChangeJournal` (Kapazität 10.000, wie im JPA-Backend) protokolliert
jeden erfolgreich ausgeführten Command (Create/Upsert/Update mit Count>0/Delete;
leere Templates und Misses bleiben unsichtbar), `changesSince` faltet die
betroffenen Keys als `key IN (…)` in das Defining-Prädikat und läuft als EINE
Backend-Query durch den Read-Path (inkl. `$expand`-Materialisierung, ohne
Page-Cap — die Delta-Paginierung ist das Journal-Fenster). Expanded Tracking
(11.3.1): Owner-Lookup als `EXISTS(nav, key IN (geänderte Member-Keys))` bzw.
mehrsegmentiges `nav.key IN (…)` einwertig — der Command-Backend refused
Referenz-Patching, Member-CONTENT-Änderungen sind hier die einzige Quelle.
Schreibvorgänge am Journal vorbei (direkt auf der DB) sind unsichtbar —
dokumentierte Service-Layer-Eigenschaft, wie beim JPA-Backend. Belegt durch
9 Unit-Tests (Fake-Backend mit Upstream-Semantik), Delta-Roundtrip im OSGi-Itest
(H2/EclipseLink, 22 Itests) und gegen echtes MongoDB. emf.odata#13 kann damit
komplett zu, #11 behält $apply/Phase-2-Rest (=#12) und das
`odata.persistence.jpa`-Retirement.

**Workaround-Cleanup (2026-08-05, nach Publikation von persistence-jpa#92–#95):**
Die drei odata-seitigen Normalisierungen aus #11 Phase 1 sind auf zwei geschrumpft —
der `toLower/toUpper`-Rename (#92: Bridge akzeptiert den Dialekt) und der
String-Literal→`EnumLiteral`-Rewrite (#93: `ExpressionValues` koerziert zentral für
alle drei Engines) sind ENTFERNT; es bleiben `eq/ne null`→`IsNull` und `castBase`
(beides OData-Semantik, kein Workaround). `$expand` pusht jetzt die VOLLE
Mehrsegment-Kette als Fetch-Hint (#95: nested JOIN FETCH + Batch-Hints; die
Proxy-Materialisierung bleibt als backend-neutrales Sicherheitsnetz). Die Memory-Engine
ist seit #94 Kleene-3VL wie der Evaluator — die `not(nullable)`-Ausnahmen im
Differential-Korpus sind REAKTIVIERT (+3 Fälle, jetzt 41); als dokumentierte Divergenz
bleibt nur die `$orderby`-Null-Platzierung.

**#14 FERTIG (2026-08-05): `OclEvaluator` in neutrales Bundle extrahiert** — neues
Bundle `odata.ocl.evaluator` (Imports: nur java/EMF/m2x-OCL-Modell — kein ANTLR, kein
OData), Paket `org.eclipse.fennec.odata.ocl.evaluator`. Auswertungsfehler tragen jetzt
die neutrale `OclEvaluationException` (kuratierte Client-Message); die
`ODataQueryParseException` erbt davon, sodass die Protokollschicht Parse- UND
Auswertungsfehler mit einem Handler auf 400 mappt (Servlet-GET-Pfad und WriteDispatcher
fangen den Supertyp, die reinen Resource-Parse-Stellen weiter den Subtyp → 404-Semantik
unverändert). `OclTypeResolver` bleibt bewusst im Query-Bundle (ADR-0004, nur vom Parser
gebraucht). Damit ist die persistence-jpa-Differential-TCK entblockt (kein
Cross-Repo-Zyklus mehr) und das Ingest-Mapping (persistence-jpa#96 §6.1) hat seinen
Evaluator. Gotcha: `resolve.test`/`testOSGi` erkennen neue Workspace-Bundles nicht als
Input-Änderung — nach Bundle-Zuschnitt `resolve.test --rerun` (Einzeltask, NICHT
`--rerun-tasks`), sonst läuft testOSGi gegen den stalen Resolve scheinbar grün durch,
ohne Tests auszuführen.

**Weiter offen:**
- **Cache-/Lifecycle-Adapter nach `emf.m2x`** verlagern (`OclAspectProvider`,
  ADR-0004; Nachfolger der alten VA1-Vorarbeit) — Kandidat: nimmt den neuen
  `odata.ocl.evaluator` als Spender mit, wenn m2x ein Evaluator-Zuhause bekommt.
- **`emf.persistence-jpa`-Fixes upstreamen**: Feature-Branch
  `fix/metamodel-refresh-dynamic-types` (Metamodel-Sichtbarkeit dynamischer Typen,
  `EBigDecimal`-Scale-Verlust) liegt lokal, je Fix ein Test — PR-Weg klären (nie pushen
  ohne User).

## 9. Ideen / Später (kein Commitment)

- **OGC SensorThings** als zweites Protokoll hinter derselben Server-Architektur (ehem. Q19).
- **OData v2/SAP-Phase** (Design berücksichtigt v2; braucht U1-Ressourcen).
- **Temporal-Extension** (Grammatik + Testfälle liegen in `reference/specs/odata-abnf`).
- **CSDL→OpenAPI-Endpoint** (`reference/specs/odata-openapi` als Vorlage).
- **odata-json-schema-Payload-Validierung** als Test-Dependency (E9-light validiert bereits
  via offizieller XSL; die JSON-Schema-Variante bräuchte eine Validator-Dep-Entscheidung).
- **4.02 verfolgen**: Core-ABNF-YAML enthält 4.02-Vorarbeiten (u. a. `case()`); 4.02 Minimal =
  4.01 Minimal + JSON-Format 4.02 (Spec-Draft-Erkenntnis 2026-07-06).
