# Fennec OData – Production-Readiness Gap-Analyse & Arbeitsplan

Status: 2026-07-16. **Arbeits- und Trackingdokument** für den Pre-Merge-Härtungslauf
(Ergebnis des 6-fach parallelen Reviews über alle 16 Bundles). Kein `main`-Merge, bevor
Tier 0 + Tier 1 grün sind. Abgeschlossene Punkte werden hier abgehakt (`[x]`).

## Stehende Arbeitsaufgabe (Leitplanken für JEDEN Fix)

- **Code-Qualität & Architektur:** saubere OO/Kapselung, SoC, SOLID; passende Java-21-Features
  (records, sealed, pattern-switch, virtual threads); keine vollqualifizierten Klassennamen —
  immer Imports (Projektregel).
- **Sicherheit zuerst (security by design):** jede DoS-Restriktion ist **konfigurierbar** UND
  **dokumentiert**; der **Default ist ein sicherer Wert** (Schutz standardmäßig AN). „Schutz aus"
  ist immer ein bewusster, dokumentierter Foot-gun, nie der Default.
- **Konfig-Konvention:** neue Limits am Servlet-PID `org.eclipse.fennec.odata.servlet` (bzw. am
  jeweiligen Backend-PID). Wert `<= 0` = unbeschränkt/deaktiviert (Foot-gun, dokumentiert),
  positiver Default = Schutz an. Register unten pflegen.
- **Tests:** jeder Fix mit Test; für Limits/Fehlerpfade **immer auch der Fehlerfall** und
  **absichtliche Falschwerte** (Fuzz/negativ) — lieber ein Test zu viel. Wenn unsicher, ob ein
  Test ausreicht: Aufgabe rekapitulieren und neu entscheiden.
- **Seiteneffekte:** potenzielle Wechselwirkungen der Einstellungen prüfen. Sinnlose
  **user-setzbare** Kombinationen → im Manual für User dokumentieren. Sinnlose Kombinationen im
  **Default-Modus** → hier als TODO (Abschnitt unten) festhalten, gemeinsam mit dem User lösen.
- **Commits:** selbstständig auf `snapshot`, `Signed-off-by: Mark Hoffmann
  <m.hoffmann@datainmotion.com>` + `Co-Authored-By: Claude …`. Finaler Push durch den User.

---

## Konfigurations-Register (Sicherheits-/DoS-Limits)

| Key | PID | Default | Bedeutung | `<=0` |
|---|---|---|---|---|
| `odata.max.top` | servlet | 1000 | $top-Ceiling (greift auch ohne Client-$top) | unbegrenzt (Foot-gun) |
| `odata.max.expression.length` | servlet | 4096 | max. Länge $filter/$orderby/$apply/$expand/nested/@alias | unbegrenzt |
| `odata.max.nesting.depth` | servlet | 64 | max. Klammer-Tiefe (Parser-Bomb-Guard) | unbegrenzt |
| `odata.max.body.size` | servlet | 1 MiB | max. Write-Body | unbegrenzt |
| `odata.max.batch.operations` | servlet | **✅ 100** | max. Sub-Requests je $batch (beide Wire-Formen) | unbegrenzt (Foot-gun) |
| `odata.max.async.inflight` | servlet | **✅ 16** | max. gleichzeitig laufende respond-async-Ausführungen (sonst 503+Retry-After) | unbegrenzt (Foot-gun) |
| `odata.max.async.monitors` | servlet | **✅ 100** | max. geparkte async-Status-Monitore (LRU) | unbegrenzt |
| `odata.jpa.max.page.size` | persistence.jpa | ✅ 1000 | server-driven Page-Cap (Lesepfad UND $apply) | unbegrenzt |
| (CSDL-XML-Größe) | client | ✅ via `maxResponseBytes` (16 MiB) | fremdes $metadata ist über den Response-Cap größenbegrenzt; zusätzlich Secure-Processing + StackOverflow-Guard | — |

(NEU = in diesem Härtungslauf hinzuzufügen.)

---

## TODO / offene Entscheidungen (sinnlose Default-Kombinationen — mit User klären)

- [x] **T2.7 God-Object-Refactor** — ✅ 2026-07-17 doch VOR dem Merge gezogen
  (User-Entscheid), Details in der Tier-2-Liste.
- [x] **WriteService Nicht-Containment-Referenzen** — ✅ 2026-07-17: BEIDE Backends binden
  Payload-Member per Key an EXISTIERENDE Entitäten (JPA: `em.find` im offenen EM; In-Memory:
  Store-Auflösung VOR dem Klassen-Lock); unbekanntes Ziel → 400 (nie silent Deep Insert),
  ausgelassene Navigation bleibt bei PATCH UND PUT erhalten (11.4.3: replace ist
  structural-only — In-Memory-`apply` entsprechend korrigiert). Tests: JPA (2), In-Memory (1),
  Differential-Write-Parität (1); Beispiel-Seed bindet Kategorien jetzt mit.
- [x] **T2.9 / T3.10** — ✅ 2026-07-17 erledigt (Details in den Tier-Listen). U3 (BSI-TRs)
  und U4 (Limit-Defaults) am selben Tag vom User bestätigt → `manual/04-configuration.md`.
  *(Bereinigt 2026-07-17: T2.2/T3.1/T3.4 standen hier noch als offen — sind in den
  Tier-Listen längst ✅.)*

---

## Tier 0 — Merge-Blocker (Korrektheit / Datenverlust / Security)

- [x] **T0.1 [Korrektheit] `OclEvaluator` 3VL für `and`/`or`/`not`** — ✅ Kleene-Tri-State
  (`triState()`-Helfer), Short-Circuit auf dominantem Wert, UNKNOWN propagiert. Tests:
  `OclEvaluatorTest` (11 Fälle, volle Tabelle + Nesting + null-Literal) + 4 Differenzial-Fälle
  (In-Memory ↔ JPA-Parität). Commit auf snapshot.
- [x] **T0.2 [Concurrency] `JpaQueryService` ambient EM/TX ThreadLocal** — ✅ `begin()` verwirft
  ein geleaktes ambientes TX sicher (rollback+close+WARN) bevor es ein frisches öffnet;
  Teil-Fehler in `begin()` schließt bereits geöffnete EMs (`discard()`). Servlet-`finally`-Garantie
  folgt in T0.5. Tests: `JpaTransactionRobustnessTest` (3). FQN-Bereinigung dieser Datei mit erledigt.
- [x] **T0.3 [Datenverlust/Concurrency] `MemoryWriteRepository`** — ✅ per-TX Undo-Log
  (Lazy-Capture beim ersten Touch, nur berührte Keys zurückgerollt, Media inklusive; ersetzt
  Whole-Store-Snapshot → behebt auch T2.14 Perf); `entities()`/`changesSince()` liefern defensive
  Kopien UNTER dem Klassen-Lock (CME-sicher). Tests: 3 neue (fremder Commit überlebt Rollback,
  kein CME bei parallelem Read/Mutation, Media-Rollback). **Zugleich T2.14 erledigt.**
- [x] **T0.4 [Security/DoS] `$batch` Sub-Request-Cap** — ✅ `odata.max.batch.operations`
  (Default 100, `<=0`=aus) in `RequestLimits`, geprüft VOR Ausführung, beide Wire-Formen.
  (Depth-Cap verworfen: $batch ist in dieser Impl nicht geschachtelt; ein Operations-Cap genügt.)
  Tests: Cap+Grenzwert, Foot-gun `0`.
- [x] **T0.5 [Security] `$batch` Catch-All** — ✅ Ausführung in sanitisiertem try/catch, offene
  Atomicity-Gruppe wird zurückgerollt (`rollbackQuietly`), generisches 500 + internes Logging.
  Test: fehlschlagender transaktionaler Commit → 500 ohne Detail-Leak + Rollback.
- [x] **T0.6 [E2E-Nachweis] JPA über HTTP-Servlet** — ✅ `JpaWiringIntegrationTest.httpEndToEndOverJpaBackend`:
  Write (POST→JPA), $filter+$orderby+$count und $apply-Aggregat über echtes HTTP → SQL-Pushdown,
  Single-Entity. 17/17 itests grün, voller `build testOSGi` grün.

## Tier 1 — Hoch

- [x] **T1.1 [Security/DoS] CSDL-XML Tiefe/Größe** — ✅ `FEATURE_SECURE_PROCESSING` +
  `loadSecurely()` (StackOverflow→IAE), Client-`$metadata`-Read fängt SOF→ODataClientException;
  Größe bereits über Client-Response-Cap gedeckelt. (SAX ist ereignisbasiert → Tiefe erreicht
  selten das Stack-Limit; Haupt-DoS = Größe, gedeckelt.) Test: `CsdlXmlLoadTest` (XXE-Nachweis + secure options + Load).
- [x] **T1.2 [Security/DoS] respond-async In-Flight-Cap** — ✅ Semaphore (`odata.max.async.inflight`,
  Default 16, `<=0`=aus) → 503+Retry-After; Monitor-LRU konfigurierbar (`odata.max.async.monitors`).
  Foot-gun dokumentiert (monitors ≥ inflight). Tests: 503-Cap + Foot-gun-`0`.
- [x] **T1.3 [Security/DoS] `$apply` Page-Cap** — ✅ `JpaApplyExecutor` honoriert `odata.jpa.max.page.size`.
  Test: groupby ohne $top (3 Gruppen, Cap 2) → gedeckelt.
- [x] **T1.4 [Beispiele] Lauffähiger JPA-Beispiel + proper Modell-Registrierung** — ✅ End-to-end
  headless verifiziert (Port 8092): `$metadata`=200, `GET /Product` liefert geseedete Daten aus H2,
  `$filter=price gt 2.00 & $orderby=price desc & $count=true` → `@odata.count:2` mit SQL-Pushdown,
  `POST /Product`→201 + Round-Trip, `GET /Review` (Containment, gekeyt). Umsetzung:
  - **Proper Modell-Registrierung** (`ShopExampleComponent`, shared): statt nacktem `EPackage` jetzt
    `EPackageConfigurator` + `EPackage` mit `emf.name`/`emf.nsURI`/`emf.registration=provided`/
    `emf.model.scope=resourceset` + `EPackage.Registry.INSTANCE` — Voraussetzung, damit die
    emf.osgi-Registry das Modell trackt und die PU es über `(emf.name=webshop)` findet.
  - **Neues Bundle `org.eclipse.fennec.odata.example.jpa`** (`ShopJpaBackendComponent`): generiert
    das `.eorm`-Mapping (`EntityMapper`) in den Bundle-Datenbereich, legt DataSource- +
    PersistenceUnit-Factory-Configs automatisch an, seedet asynchron sobald `JpaQueryService` bindet.
  - **Ecore**: `Review` erhielt ein `id`-Schlüsselattribut — keylose Entität ist im EclipseLink-
    Descriptor nicht baubar (`EntityMapper`-Synthese-PK → `DescriptorException` „no non-read-only
    mapping for primary key REVIEW.PK_REVIEW"); Embeddable-Mapping wird von `EntityMapper` nicht
    generisch unterstützt (empirisch geprüft).
  - ~~Bekannte Grenze~~ 2026-07-17 GESCHLOSSEN: der `WriteService` bindet Nicht-Containment-Refs
    per Key (s. TODO-Abschnitt) — der Demo-Seed bindet `Product.category` jetzt mit;
    `Product.reviews` (Containment) reitet weiterhin mit.

## Tier 2 — Mittel

- [x] **T2.1 [Atomicity] JPA Multi-PU** — bei erster Commit-Exception restliche EMs rollbacken
  (kein Teil-Commit), oder Multi-PU-Write dokumentiert ablehnen. Test.
- [x] **T2.2 [Korrektheit] CSDL-Annotation-Typinferenz** — ✅ gelöst (modell-frei, round-trip-stabil): der Read-Pfad quotet einen typ-aussehenden String, der Write-Pfad behandelt ihn explizit als String; `String "1.0"/"007"` bleibt String, echte Zahl bleibt Zahl. Test `numericLookingStringAnnotationStaysString`.

- [x] **T2.3 [Security] Client `reference()` JSON-Escaping** — ✅ (gleiche Escaping wie updateCollection). — via Mapper statt String-Concat. Test.
- [x] **T2.4 [Exception] Client `ODataBatch` Jackson-Wrapping** — ✅ readTree + parseInt → ODataClientException. — `JacksonException` → `ODataClientException`. Test.
- [x] **T2.5 [Security] Servlet 406** — ✅ `notAcceptable`, Test `contentNegotiation406`. — nicht json/xml-kompatibler Accept → 406. Test.
- [x] **T2.6 [Concurrency] `ODataClient.maxResponseBytes`** — ✅ volatile. — `volatile` oder aus config. Test/Review.
- [x] **T2.7 [SOLID] `ODataServlet` God-Object** — ✅ 2026-07-17 (vorgezogen VOR den
  main-Merge, User-Entscheid; Branch `refactor/servlet-decomposition`, 4 mechanische
  Schritte je mit grünem Build): `BatchHttpRequest`/`BatchHttpResponse` (geteilte
  synthetische Wrapper), `BatchDispatcher` ($batch beide Wire-Formen, Atomicity-Groups),
  `AsyncDispatcher` (respond-async: Executor, Monitor-LRU, In-Flight-Cap,
  accept/monitor/cancel/shutdown; Re-Entry über neue package-private
  `dispatchDirectly`-Brücke statt `super.service`), `WriteDispatcher` (POST/PATCH/PUT/
  DELETE-Dispatch, Payload-Decode inkl. `@odata.bind`, Delta-Collection-Update,
  created/updated-Responses). Geteilte Helfer (ETags/Preconditions, Routing, Media,
  Actions, `entityJson`) bleiben am Servlet, Zugriff package-private. Servlet
  4969→3222 Zeilen; Verhalten unverändert (voller `build testOSGi` grün, 85/0
  Servlet-Tests, itests 0 failed).
- [x] **T2.8 [Concurrency/Leak] `CachingODataQueryParser.invalidate` verdrahten** — Provider-Unregister-Hook oder Klassen-Zahl-Cap.
- [x] **T2.9 [Coverage] Floor anheben + `persistence.api` gaten; `$apply`-Ausschluss prüfen** —
  ✅ 2026-07-17: Messlauf (Floor temporär 0.99 → Ist-Werte aus den Violations, exakt die
  Gate-Sicht) → per-Bundle-Floors je ~10 Punkte unter Ist (csdl .90→.80, jpa .86→.75,
  runtime .84→.75, inmemory .83→.75, client .79→.70, codec.json .77→.65, vocabularies
  .64→.55, query .56→.45, persistence.api .47→.40). `persistence.api` gegated —
  `ChangeJournalTest` dafür ins api-Bundle umgezogen (testet eine api-Klasse; lag im
  inmemory-Bundle). `query/apply`-Ausschluss verifiziert: rein src-gen, kein Handcode dahinter.
- [x] **T2.10 [Coverage] `ChangeJournal` direkte Unit-Tests** — Eviction→410, rollback, Token-Staleness.
- [x] **T2.11 [E2E] Concurrency-/Last-Test über HTTP** — paralleler HttpClient-Fan-out inkl. konkurrierender Writes.
- [x] **T2.12 [E2E] Große-Payload-/Tiefpaging-Test** — >1000 Rows, mehrseitiger nextLink über HTTP.
- [x] **T2.13 [Beispiele] README-Widerspruch** — read-only vs. POST 201 auflösen.
- [x] **T2.14 [Performance] `MemoryWriteRepository.begin()`** — ✅ mit T0.3 erledigt (Lazy-Capture statt Whole-Store-Kopie).

## Tier 3 — Niedrig / Politur

- [x] **T3.1 [Style] FQN → Imports** — ✅ Runtime, Query, CSDL, Client bereinigt; die letzten kollisions-bedingten Fälle (ODataPackageProfile csdl.profile vs metadata.odata) sind mit der Auflösung von `odata.ecore` (emf.osgi-1.1.0-Migration) entfallen.

- [x] **T3.2 [Robustheit] `OclEvaluator.dateTime()`** — TimeOfDay nach Edm-Typ statt Länge; substring-int-Range-Check.
- [x] **T3.3 [DeadCode] `EcoreToEdmConverter.isEntity`** — KEIN toter Code (Test nutzt es), belassen.
- [x] **T3.4 [Kopplung] Aspect-Id/COLLECTION_OPEN** — ✅ bewusste Entscheidung: beide Literale bleiben mit „mirrors <Quelle>"-Doc-Notiz dupliziert, statt internes API (metadata.provider-Package / package-private EdmTypes) nur für zwei kurze Konstanten zu exportieren.

- [x] **T3.5 [Security] CORS `Vary: Origin` beim Origin-Echo.**
- [x] **T3.6 [Security] IAE→400 feste Meldung für Nicht-Parser-IAE (kein EMF-Detailleak).**
- [x] **T3.7 [Client] Funktions-Literale URL-encoden; `parseInt`-Wrapping; injizierter HttpClient Redirect-Kontrakt; Error-Body-Auszug deckeln.**
- [x] **T3.8 [Kapselung] defensive Kopien: `ODataPage`, `ComputedRow`, `ChangeJournal.Change`.**
- [x] **T3.9 [Robustheit] `ODataVocabularies` Leer-Schema-Check; Enum long→int-Range.**
- [x] **T3.10 [Style] Magic Numbers → benannte Konstanten/Config; gebrochene `{@value}`-Javadoc** —
  ✅ 2026-07-17: `CONTENT_ID_HEADER` (Batch-Parser, Servlet + Client, ersetzt `11`/Literal-Duplikate),
  `ODataClient.DEFAULT_CONNECT_TIMEOUT` (4× `Duration.ofSeconds(10)`),
  `OclEvaluator.ISO_LOCAL_DATE_LENGTH` (Datum-Heuristik). HTTP-Statuscodes bewusst literal
  (idiomatisch). Alle verbliebenen `{@value}`-Tags verifiziert auflösbar (3 Fundstellen,
  Konstanten existieren) — die gebrochenen waren durch frühere Pässe bereits gefixt.

---

## Verifizierte Nicht-Probleme (nicht anfassen)
- XXE auf dem Server-Request-Pfad: Writes sind JSON-only (415 sonst), kein fremdes XML geparst.
- Geteilte `OclEvaluator`/`OclTypeResolver`-Singletons: stateless/thread-safe; `CachingODataQueryParser` synchronisiert.
- `metadata`/`operation.api`/`schema.api` test=0: unkritisch (via metadata.tests bzw. reine Interfaces/Records).

## Fortschritts-Log
- 2026-07-16: Dokument angelegt; Review abgeschlossen (6 Bereiche), Blocker-Satz fixiert.
- 2026-07-16: **Tier 0 KOMPLETT** (T0.1–T0.6) + T2.14, je mit Tests committet; voller
  `build testOSGi` grün.
- 2026-07-16: **Tier 2 + Tier 3 im Wesentlichen KOMPLETT** — alle korrektheits-/security-/robustheits-
  relevanten Punkte umgesetzt+getestet+committet; verbleibend nur bewusste Deferrals (T2.7 God-Object,
  T2.2 Annotation-Typinferenz, Rest-FQN/Konstanten) + T1.4-Scaffold (launch-zu-verifizieren). Voller
  `build testOSGi` grün.
- 2026-07-16: **Tier 1 Sicherheits-/DoS-Kern KOMPLETT** (T1.1 CSDL-XML-Härtung, T1.2 async
  In-Flight-Cap, T1.3 $apply-Page-Cap) mit Tests committet. **Offen T1.4** (lauffähiges
  JPA-Beispiel — nur launch-verifizierbar, s. o.). Nächster: Tier 2 + Tier 3.
- 2026-07-17: **Nicht-Containment-Write-Bindung geschlossen** (beide Backends, s. TODO-Abschnitt)
  — Gegenstück zum Read-Pfad-Fix vom selben Tag (`$expand`-Proxy-Auflösung via
  JPAResourceFactory-ResourceSet, emf.persistence-jpa#17). Demo-Seed bindet Kategorien;
  Manual 02-server dokumentiert die Nested-Member-Semantik.
- 2026-07-17 (2): **T2.9 + T3.10 erledigt** (Messlauf-basierte per-Bundle-Floors inkl.
  persistence.api-Gate; Konstanten-Pass) und **U3/U4 vom User bestätigt** (BSI-Baseline +
  Limit-Defaults → Manual 04). U1/U2 zurückgestellt (keine Zugangsdaten bzw. später).
  **Verbleibend im Härtungslauf: NUR T2.7** (God-Object, bewusst post-merge).
- 2026-07-17 (3): **T2.7 erledigt** (vorgezogen, s. Tier-2-Eintrag) — **der Härtungslauf
  ist damit KOMPLETT**; nächster Schritt ist U2 (Release auf `main`), sobald der User
  so weit ist.
- 2026-07-17 (4): **T2.7 zweite Welle** (User-Wunsch „noch kleiner, SoC"): zusätzlich
  `OperationDispatcher` (Functions/Actions: bound/unbound, Imports, Parameter-Koersion,
  Result-Shape), `DeltaDispatcher` (track-changes, Delta-Links, Delta-Response beide
  Wire-Formen, Delta-$count) und `ResponseFormatter` ($select/$expand-Parsing inkl.
  nested/$levels, Entity-JSON-Serialisierung, XML/XMI-Negotiation) extrahiert.
  **Servlet 4969 → 2288 Zeilen (−54 %)**; verbleibender Kern = DS-Wiring, Routing,
  Read-Pipeline (fetch/walk/casts) und geteilte URL-/ETag-Helfer. Bewusst NICHT weiter
  zerlegt (abnehmender Ertrag): Read-Pipeline (~350 Z., vom Routing untrennbar),
  ETag/Preconditions (~110), Media (~100). Voller `build testOSGi` grün.
