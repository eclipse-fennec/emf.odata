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
| U1 | **Interop-Eskalation**: Microsoft-Graph-Test (braucht OAuth-Zugang), SAP-V4-Endpoint (Demo-Account, ehem. Q18), XOData/Power BI/Excel als Fremd-Clients gegen unseren Server (ehem. Q4) | Live-Suite deckt TripPin/RESTier/Demo/Northwind ab; die kommerziellen Ökosysteme fehlen |
| U2 | **Release auf `main`** (Central-Release via `release.yml` steht bereit; Branch `initial` bleibt unangetastet) | CI/Versionierung verifiziert, nur der Push/Merge ist User-Sache |
| U3 | **BSI-TRs bestätigen** (TR-02102-1/-2, TR-03116 angenommen; ehem. Q1) — betrifft nur Doku/Deployment-Empfehlungen, Auth/TLS sind out-of-scope (vorgelagerte Infrastruktur) | |
| U4 | **Limit-Defaults formal absegnen** (ehem. Q6): `$top`-Ceiling 1000, Expression-Länge 4096, Nesting 64, Body 1 MiB, JPA-Page-Cap 1000 — implementiert und konfigurierbar, Defaults nie formal beschlossen | `manual/04-configuration.md` |

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

## 8. Cross-Repo / Upstream

- **`QWhere` → `predicate: OclExpression`** im Persistence-Query-Modell
  (ehem. E4-AP-9/Q11) — Cross-Repo-Abstimmung mit `emf.persistence-jpa`.
- **Cache-/Lifecycle-Adapter nach `emf.m2x`** verlagern (`OclAspectProvider`,
  ADR-0004; Nachfolger der alten VA1-Vorarbeit).
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
