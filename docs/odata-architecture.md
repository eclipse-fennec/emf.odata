# Fennec OData – Architektur

Status: 2026-07-15, konsolidierter Ist-Stand (die frühere Änderungs-Chronik liegt in der
Git-Historie dieses Dokuments). Publizierte EN-Doku: `manual/` (GitHub Pages, u. a.
`06-feature-matrix.md`). Conformance-Nachweis: `odata-conformance-status.md`.
Offene Punkte: `odata-open-tasks.md`. Entscheidungen: `decisions/` (ADRs).
Beispiel/Einstieg: `org.eclipse.fennec.odata.example` (startbarer `example.bndrun`, Port 8080).

Conformance-Stand: **OData 4.0 und 4.01, Minimal bis Advanced, vollständig beansprucht**
(`ConformanceLevel=Advanced` im `$metadata`); einziger offener Spec-SHOULD ist die
`$crossjoin`-Engine (parst → 501).

## Big Picture

```
                HTTP (OSGi HTTP Whiteboard, z. B. Jetty12)
                               │
              ┌────────────────▼─────────────────┐
              │  ODataRequestFilter (Whiteboard)  │  RequestLimits VOR jeder Dispatch-Arbeit
              └────────────────┬─────────────────┘
              ┌────────────────▼─────────────────┐   Servlet statt Jakarta REST:
              │   ODataServlet (Catch-All /odata/*)│  Catch-All parst selbst — Routing/
              │   GET/POST/PATCH/PUT/DELETE,      │   Negotiation eines REST-Frameworks
              │   $batch (multipart+JSON), async  │   liefen leer (Servlet = ehrlicher)
              └──┬──────────┬───────────┬─────────┘
   RequestLimits─┤          │           ├─ EntityShaper ($select/$expand-Kopien,
   (Defence in   │          │           │   nested Options, $levels, $ref-Splice)
    depth)       │          │           └─ ODataJson (Rows/Fehler, Sanitizer)
        ┌────────▼───┐  ┌───▼───────────────────┐
        │ $metadata  │  │ eigener URI-/Query-    │  ANTLR4 (Olingo ist archiviert —
        │ E2: csdl   │  │ Parser (E4: query)     │  Eigenbau, OASIS-ABNF-Suiten als
        │ Ecore↔EDM  │  │ Resource-Path + Expr   │  Akzeptanzbasis: 710 Fälle)
        │ (OASIS-EDM-│  │ → typisiertes OCL-IR   │── CachingODataQueryParser (LRU
        │  Modell)   │  │ + $apply-Submodell     │   pro EClass, weak keys)
        └──────▲─────┘  └───────────┬────────────┘
               │                    │ OCL-IR (m2x ocl.model)
        ┌──────┴──────┐  ┌──────────▼─────────────────────────────┐
        │ E1: metadata│  │ SPIs (persistence.api): QueryService · │
        │ ODataAspect-│  │ WriteService · MediaService ·          │
        │ Provider +  │  │ DeltaService (+ ChangeJournal) ·       │
        │ vocabularies│  │ EntityRepository · ODataOperationHandler│
        └─────────────┘  └───┬──────────────────────────┬─────────┘
                     ┌───────▼────────┐        ┌────────▼────────┐
                     │ inmemory       │        │ JPA (Criteria-  │
                     │ OclEvaluator = │        │ Pushdown, kein  │
                     │ REFERENZ-      │◄─diff──│ In-Memory-      │
                     │ Semantik       │  Tests │ Filtering)      │
                     └────────────────┘        └─────────────────┘
   Antworten: E3 codec.json (OData-JSON) | XMI ($format=xml) | CSDL-XML/-JSON ($metadata)

   Client (E8): ODataClient + fluenter EntitySetRequest — liest fremde $metadata über den
   E2-Read-Pfad zu Ecore (Schema-Registry-SPI, ADR-0007), dekodiert durch DASSELBE
   Codec-Profil wie der Server serialisiert. JDK HttpClient (injizierbar), kein JAX-RS.
```

## Bundles (17)

| Bundle | Etappe | Inhalt |
|---|---|---|
| `odata.metadata` | E1 | `ODataMetadataHandler` (dünner Adapter, ADR-0003): hängt das aufgelöste CSDL-Profil als `"odata"`-`AspectEntry` an jede registrierte `EPackage`. Kein eigenes Ecore |
| `odata.vocabularies` | E1 | OASIS Core/Capabilities/Validation/Measures als EPackages (CSDL-Read-Bootstrap, abhängigkeits-geordnetes `loadAll()`) |
| `odata.csdl` | E2 | `OdataResolver` (EPackage+`@OData.*`→Profil), Ecore↔EDM-Converter, `$metadata` XML+JSON, `CsdlXmlLoad` (XXE-gehärteter EINZIGER Ladepfad) |
| `odata.codec.json` | E3 | OData-JSON-Codec-Profil (`@odata.type`/`@odata.id`, `Edm.*`-Werteformate, IEEE754, metadata-Level) |
| `odata.ocl.evaluator` | #14 | OData-neutraler `OclEvaluator` (IR-Referenz-Semantik über m2x-OCL-ASTs, Kleene-3VL) + `OclEvaluationException`; hängt NUR am OCL-Modell — kein ANTLR, kein OData; konsumierbar von der persistence-jpa-Differential-TCK und dem Ingest-Mapping (persistence-jpa#96 §6.1) |
| `odata.query` | E4 | ANTLR4-Grammatik → OCL-IR, Resource-Path-Parser, `$apply`-Submodell, `OclTypeResolver` (ADR-0004), LRU-Cache; `ODataQueryParseException` erbt von der neutralen `OclEvaluationException` |
| `odata.operation.api` | E4 | `ODataOperationHandler`-SPI (Functions/Actions) |
| `odata.persistence.api` | E5 | `QueryService`/`ApplyQuery`/`WriteService`/`MediaService`/`DeltaService`-SPIs, `ChangeJournal`, `EntityRepository` |
| `odata.persistence.inmemory` | E5 | Referenz-Backend, `ApplyExecutor`, `FileEntityRepository` (XMI-Dir), `MemoryWriteRepository` (Write+Media+Journal) |
| `odata.persistence.jpa` | E5 | `OclToCriteriaTranslator`, `JpaApplyExecutor`, Write+Delta (Jakarta-Criteria-Pushdown, ADR-0006) |
| `odata.persistence` | #13/#11 | Backend-neutrale Query+Write+Delta über die Fennec-Persistence-SPI (`CommandPersistenceService`: Reads via `OclToExpr`→`QueryableResource.query` mit Capability-Vorvalidierung, Writes via Insert/Update/DeleteCommand + ChangeSet-Templates, Change-Tracking via Service-Layer-`ChangeJournal` + `key IN`-Requery durch den Read-Path; Nichtübersetzbares → 501) — bedient JPA UND Mongo, Ziel-Nachfolger von `odata.persistence.jpa` |
| `odata.runtime` | E6/E7 | `ODataServlet` + `ODataRequestFilter`/`RequestLimits`/`EntityShaper`/`ODataJson` |
| `odata.schema.api` | E8 | Client-Schema-Registry-SPI (Reader/Registrar/Resolver, ADR-0007) |
| `odata.client` | E8 | `ODataClient`, `EntitySetRequest`, `$batch` beide Wire-Formen, CSRF, Delta, Media |
| `odata.metadata.tests`, `odata.itests` | — | OSGi-Integrationstests (Whiteboard-Kette / echtes HTTP e2e) |
| `odata.live.tests` | — | Live-Interop (`liveTest`): TripPin/Demo/Northwind + Mirror-Round-Trip (s. publizierte Findings-Seite) |
| `odata.example` | — | Demo-Modell + Daten + `example.bndrun` + E9-Akzeptanz (python-odata, OASIS-XSL) |

## Architektur-Entscheidungen (Kurzform)

Gehaltene ADRs in `decisions/`: **0002** CSDL über das OASIS-EDM/EDMX-EMF-Modell
(`org.odata.csdl.model`, 761 ExtendedMetaData-Annotationen = fertiges XSD-Mapping) ·
**0003** Konversionslogik vollständig im Converter, der Metadata-Handler nur
dünner Kompositions-Adapter · **0004** Type-Resolution standalone im Query-Bundle ·
**0006** JPA-Backend als Criteria-Eigenbau hier im Repo · **0007** Client-Schema-Registry
als SPI (Fetch/Convert ↔ Persistenz/Lookup entkoppelt).

Als Fakt (frühere Gegen-ADRs 0001/0005 entfernt): Transport ist ein **planes Jakarta-Servlet
am OSGi HTTP Whiteboard** (kein Jakarta REST — bei einer selbst parsenden Catch-All-Ressource
laufen Routing/Negotiation eines REST-Frameworks leer); **kein Olingo zur Laufzeit** (Projekt
archiviert) — URI-Parser und Batch-Splitter sind Eigenbau auf der ANTLR4-Infrastruktur, die
normativen OASIS-ABNF-Testfälle (vendored) sind die Akzeptanzbasis; Olingo-Quellcode bleibt
Studien-Referenz (`reference/`-Regeln: studieren, nicht kopieren — Apache-2.0 vs. EPL-2.0).

Weitere Grundsätze: **OCL ist das interne Predicate-IR** für `$filter`/`$orderby` (m2x
`ocl.model`); **`$apply` ist ein eigenes Submodell** (`apply.ecore`), NICHT in OCL gepresst;
**Backend-Pushdown ist Pflicht** — Übersetzungslücken werfen `UnsupportedOperationException`
→ ehrliches 501, nie stilles In-Memory-Filtern oder falsche Antworten; der In-Memory-
`OclEvaluator` definiert die Referenz-Semantik, Differenzialtests halten JPA dagegen.

## Request-Lifecycle

**Lesen (GET):**
1. `ODataRequestFilter` erzwingt die `RequestLimits` VOR jeder Dispatch-Arbeit (Servlet
   prüft nochmal — Defence in depth).
2. CORS (optional, config-gated) · `OData-Version`-Negotiation (4.01, außer Client pinnt 4.0) ·
   Options-Normalisierung (4.01: case-insensitiv, `$`-präfixlos; Whitelist: bekannt-unsupported
   → 501, unbekannt `$x` → 400, Custom ignoriert) · Metadata-Level + IEEE754 request-scoped
   (ThreadLocals, save/restore wegen `$batch`-Sub-Requests).
3. Routing: `/` Service-Doc · `/$metadata` CSDL XML/JSON · `/$async/<id>` Status-Monitor ·
   `/$batch` · sonst eigener Resource-Path-Parser (`Set(key)`, Walks, Casts, Key-as-Segment,
   `$value`/`$count`/`$ref`, Operations, `$crossjoin`/`$all`/`$entity` → 501).
4. Parsen: `$filter`/`$orderby`/`$apply`/nested Options → typisiertes OCL-IR gegen die
   Kontext-EClass (unbekannte Namen → 400). **Keys werden als Literal-AST GEBAUT, nie
   geparst** (Injection bleibt ein Literalwert). `@`-Aliase rekursiv mit Tiefen-Cap.
5. `QueryService.execute/executeApply` — Backends sehen NUR das IR; `$expand`-Pfade gehen
   als Prefetch-Hint mit (`EntityQuery.expand` — Backends MÜSSEN expandierte Navigationen
   effizient materialisieren, kein N+1, keine Lazy-Proxies nach außen).
6. Antwort: `EntityShaper`-Kopien (select/expand/nested Options/$levels VOR dem Pruning) →
   E3-JSON oder XMI; Paging → `@odata.nextLink` (peek top+1); Delta → `@odata.deltaLink`.
   Fehler → sanitisiertes OData-Error-JSON (nie Stacktraces/Klassennamen; 500er werden
   serverseitig ERROR-geloggt).

**Schreiben:** POST/PATCH/PUT/DELETE auf Set/Entity/Property/`$ref`/Media; Payload-Dekodierung
über den E3-Codec (kein manuelles JSON-Parsen), `@odata.bind` wird vor dem Codec extrahiert;
ETags/If-Match (428/412); Deep Insert; PATCH-Collection-Update (`#$delta`) all-or-nothing;
Guards 415/413/400/405/409.

**`$batch`:** beide Wire-Formen (multipart + JSON), Change-Sets atomar über die
WriteService-Transaktion; Sub-Requests laufen re-entrant durch dasselbe Servlet
(`BatchHttpRequest`/`BatchHttpResponse`-Captures).

**Async (`Prefer: respond-async`, 11.6):** Ausführung auf einem Virtual-Thread-Worker
(`Future`-Semantik: Poll-Monitor `isDone()`, DELETE = `cancel(true)` unterbricht wirklich);
sofortige 202 + Monitor-URL; Monitor liefert 202 solange läuft, das Ergebnis GENAU EINMAL
als `application/http`, danach 404; bounded LRU (100), Eviction cancelt.

**Delta (`Prefer: odata.track-changes`, 11.3):** zustandsloser Server — der Delta-Link
re-encodiert die Defining-Query um einen opaken Journal-Token; `ChangeJournal` in beiden
Backends (TX-fest: Seq-Vergabe erst beim Commit); Removals in 4.0- UND 4.01-Form
(Versions-Negotiation); `$expand`-Deltas als Full-Representation; Delta-Paging via
`Prefer: maxpagesize`; Retention überschritten → ehrlich 410 + Refetch-URL.

## Security-Defaults

| Schutz | Default | Konfiguration |
|---|---|---|
| `$top`-Ceiling (greift auch ohne Client-`$top`) | 1000 | `odata.max.top` (PID `org.eclipse.fennec.odata.servlet`) |
| Max. Expression-Länge (alle Options inkl. nested + `@`-Aliase) | 4096 | `odata.max.expression.length` |
| Max. Klammer-Tiefe (Parser-Bomb-Guard, O(n)-Scan VOR dem Parsen, underflow-fest) | 64 | `odata.max.nesting.depth` |
| Pfadlänge Resource-Path / Segment-Zahl | 4096 / 16 | (fix) |
| Write-Body-Limit | 1 MiB | `odata.max.body.size` |
| JPA Server-Driven-Paging-Cap (ohne Client-`$top`) | 1000 | `odata.jpa.max.page.size` (PID `…persistence.jpa`) |
| Injection | strukturell: einziger Query-Pfad ist das typisierte IR, kein String-Concat in Backends; unbekannte Properties/Funktionen → 400; klammerfreie Tiefen-Rekursion via StackOverflow-Guard → 400 | — |
| XXE | `CsdlXmlLoad.secureOptions()` = der EINE CSDL/EDMX-Ladepfad (DOCTYPE + externe Entities aus) — gilt auch für fremdes `$metadata` im Client | — |
| Fehler-Leaks | 500 generisch, Meldungen JSON-escaped, control-chars raus, 500-Zeichen-Cap | — |
| Header-Injection | Entity-Keys in `Location`/`OData-EntityId` control-char-escaped | — |
| Pfad-/Objekt-Leaks | Serialisierung nur über Shaper-KOPIEN mit mitkopierten Expand-Zielen (interne Referenzen, keine Server-Originale) | — |
| Dateizugriff | `FileEntityRepository` liest NUR das konfigurierte Verzeichnis, einmalig bei Aktivierung | `directory` (Factory-PID `…repository.file`) |
| CORS | aus, bis Origin(s) konfiguriert | `odata.cors.origin` |
| Client-SSRF | fremder Origin in `@odata.nextLink` abgelehnt; Same-Host-http→https-Upgrade, nie Downgrade; Response-Read gedeckelt/gestreamt; BOM-Strip | — |
| Auth/TLS | out-of-scope — vorgelagerte Infrastruktur | — |

## Teststrategie

- **Unit + e2e**: Mockito-Servlet-Tests UND OSGi-itests über echtes HTTP (Jetty12); Security
  destruktiv getestet (Injection-Strings, Parser-Bomben, Exhaustion, Leak-Freiheit).
- **OASIS-ABNF-Harnesses** (3 Suiten, `@TestFactory` über vendorte offizielle Testfälle):
  710 Fälle, 697 verifiziert / 13 Skips — jeder Skip ist benanntes Backlog
  (`odata-open-tasks.md` §2). Skip-Semantik: `OUT_OF_SCOPE` wird gar nicht generiert
  (dokumentierte Auslassung), `BACKLOG` = Assumption-Skip als Regressions-Radar.
- **Differenzial-Backend-Harness**: EIN Edge-Case-Datensatz in JPA (H2) und In-Memory, ein
  Query-Corpus über beide, Ergebnis-Parität asserted (fand den 3-wertige-Null-Logik-Bug).
- **Deterministische Performance**: SQL-Statement-COUNTS statt Wanduhr (fand ein echtes N+1);
  `perfTest` (50k Zeilen, konstante Statement-Zahl) als CI-Gate; JaCoCo-Floor an `check`.
- **Live-Interop** (`liveTest`, `@Tag("live")`, nicht in CI): Client gegen TripPin/Demo/
  Northwind + Mirror-Round-Trip (fremdes Schema auf UNSEREM Server, identische Request-Specs
  gegen beide). Jeder Live-Fund ist per Spec-Trace-Back als OFFLINE-Guard fixiert
  (publizierte Seite „Live Interop Findings").
- **Fremd-Tooling-Akzeptanz** (E9-light): python-odata (unabhängiger V4-Client) und die
  offizielle OASIS `V4-CSDL-to-JSONSchema.xsl` gegen unser `$metadata` + Live-Payloads.

## Erfahrungswissen (Gotchas, dauerhaft gültig)

**Build/Workspace:**
- bnd `-generate` und IDE-Builds löschen `generated/buildfiles` — der Marker, über den das
  Workspace-Repository Bundles sieht. Ist `jar` gleichzeitig up-to-date, bleibt das Bundle
  unsichtbar → wechselnde „cannot be resolved". Guard im Root-`build.gradle`: `jar` nur
  up-to-date solange der Marker existiert. Parallel offene Eclipse-IDE verschärft das.
- fennecEMF `-generate` BESITZT und LEERT `src-gen` — der ANTLR-Parser lebt deshalb im
  eigenen Source-Folder `src-gen-parser`.
- ANTLR-Regeneration: Rezept im `bnd.bnd` des Query-Bundles; braucht `-Xexact-output-dir`
  und die Tool-Jars antlr4 + ST4 + antlr-runtime-3.5.3 aus `~/.m2`. Die Grammatik ist als
  Resource in der Bundle-Jar (`grammar/ODataFilter.g4`) — Rettungsanker.
- itests laufen gegen die BUNDLE-JARS: nach Servlet-Änderungen `:runtime:jar` bauen, sonst
  testet man den alten Stand. itests-`build.gradle` braucht explizites, null-gefiltertes
  `dependsOn` auf die Projekt-Jars (clean-build-Ordnung).

**EMF/Codec:**
- `EcoreUtil.Copier` default `useOriginalReferences=true` — nicht mitkopierte Ziele bleiben
  als ORIGINALE referenziert (Server-Objekt-Leak in die Serialisierung). `Copier(true, false)`:
  gedroppte Refs = Rekursions-Cutoff UND erzwungene Payload-Internität.
- Der Parser-Cache teilt AST-INSTANZEN (read-only-Kontrakt). Wer einen gecachten AST in ein
  EMF-Containment einbettet, REPARENTET ihn — vorher kopieren (biss beim JPA-Delta-Filter).
- `EcorePackage.Literals.EOBJECT.isSuperTypeOf(…)` greift bei DYNAMISCHEN EClasses nicht
  (impliziter Supertyp) — explizit behandeln.
- Codec: `useId(false)` setzt nur die idStrategy und darf nicht danach überschrieben werden
  (Builder-Reihenfolge); gegen `_id`-Leaks zusätzlich `idKeyMode("FEATURE_ONLY")`.
- XMI unter `$select` ist eine PROJEKTION: unset ≠ Default nicht unterscheidbar, Pflicht-
  Features fehlen ggf. — ladbar, aber nicht Diagnostician-valide (Key überlebt immer).

**Servlet/Container:**
- Der Container RECYCELT Request/Response, sobald der annehmende Thread zurückkehrt —
  Hintergrund-Worker (async) arbeiten auf einem Snapshot (`BatchHttpRequest.asyncSnapshot`),
  ThreadLocals (`METADATA_LEVEL`/`IEEE754`) reisen explizit mit.
- `Location` ist eine relative URI-Referenz (RFC 7231) — `java.net.http.HttpRequest`
  braucht absolute URIs, Clients müssen gegen den Service-Host resolven.
- Mockito-Stubs für `getParameterNames()` liefern EINE erschöpfbare Enumeration —
  Servlet-Code iteriert deshalb über `getParameterMap()`.

**JPA/EclipseLink:**
- `CriteriaBuilder.extract` existiert erst ab Persistence 3.2 — Datums-Funktionen laufen
  über die EclipseLink-Expression-Brücke (`JpaCriteriaBuilder.toExpression/fromExpression`).
- Eager Collection-Features laden PRO ZEILE (N+1) — Batch-IN-Hints für alle to-many,
  LEFT-Fetch-Joins für single-valued `$expand`-Ketten (per Präfix dedupliziert).
- Walks auf lazy Referenzen NACH EM-Close liefern unaufgelöste Proxies — Materialisierung
  (descend) muss bei offener Session passieren; `EntityQuery.expand` ist der Vertrag dafür.
- **Subquery als Vergleichsoperand**: `CriteriaBuilder.greaterThanOrEqualTo`/`lessThan` (die
  `Comparable`-Overloads) casten ihre Operanden auf `ExpressionImpl` — eine `SubQueryImpl` ist
  keine und fliegt mit `ClassCastException`. Die NUMERISCHEN `ge/gt/lt/le`-Overloads casten auf
  `InternalSelection`, das die Subquery implementiert. Darum geht der gefilterte `$count`
  (korrelierte COUNT-Subquery, Long) über `cb.ge/gt/lt/le`, nicht über `greaterThanOrEqualTo`.
- **`treat()` in einem OR**: EclipseLinks `cb.treat(root, Sub).get(attr)` liefert für
  Nicht-`Sub`-Zeilen `null` (3VL-Ausschluss dieses Disjunkts), statt die ganze Query auf den
  Subtyp einzuschränken — eine Nicht-Subtyp-Zeile, die den anderen OR-Zweig erfüllt, überlebt
  also korrekt. Per OR-Probe im Cast-Test abgesichert (sonst wäre der Cast-Pushdown unsicher).
- Lokale `emf.persistence-jpa`-Fixes (Metamodel-Sichtbarkeit dynamischer Typen,
  `EBigDecimal`-Scale) liegen auf Feature-Branch — s. `odata-open-tasks.md` §8.

**Speicher:**
- `ODataJsonResourceImpl.RESOLVED` ist eine WeakHashMap (Profile referenzieren das EPackage
  nicht zurück). Beim `CachingODataQueryParser` reichen weak keys NICHT
  (AST→referredProperty→EClass = value→key) — LRU-Kapazität begrenzt, echtes Freigeben via
  `invalidate(EClass)` (Anbindung ans Provider-Lifecycle: offen, Tasks §6).
