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

- (wird beim Umsetzen befüllt)

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
- [ ] **T1.4 [Beispiele] lauffähiges JPA-Beispiel** — OFFEN. Der JPA-über-HTTP-Pfad ist bereits
  **bewiesen** (T0.6 `httpEndToEndOverJpaBackend` = lauffähige, verifizierte Demonstration).
  Für ein *startbares* `example-jpa.bndrun` braucht es eine Beispiel-Komponente (ConfigAdmin:
  H2-DataSource + PersistenceUnit + EPackage + Demo-Seed, Muster = `JpaWiringIntegrationTest`)
  und einen `-resolve: auto`-bndrun; die Lauffähigkeit muss per `bnd run`/Eclipse verifiziert
  werden (in dieser Umgebung nicht startbar). Bewusst NICHT als unverifiziertes Artefakt committet.

## Tier 2 — Mittel

- [ ] **T2.1 [Atomicity] JPA Multi-PU** — bei erster Commit-Exception restliche EMs rollbacken
  (kein Teil-Commit), oder Multi-PU-Write dokumentiert ablehnen. Test.
- [ ] **T2.2 [Korrektheit] CSDL-Annotation-Typinferenz** — numerische Interpretation nur bei
  bekanntem Term-Typ, sonst String (kein `"1.0"`→Zahl). Test: String-Annotation `"007"`/`"1.0"` round-trip.
- [ ] **T2.3 [Security] Client `reference()` JSON-Escaping** — via Mapper statt String-Concat. Test.
- [ ] **T2.4 [Exception] Client `ODataBatch` Jackson-Wrapping** — `JacksonException` → `ODataClientException`. Test.
- [ ] **T2.5 [Security] Servlet 406** — nicht json/xml-kompatibler Accept → 406. Test.
- [ ] **T2.6 [Concurrency] `ODataClient.maxResponseBytes`** — `volatile` oder aus config. Test/Review.
- [ ] **T2.7 [SOLID] `ODataServlet` God-Object** — Batch-/Async-/Write-Dispatcher extrahieren.
- [ ] **T2.8 [Concurrency/Leak] `CachingODataQueryParser.invalidate` verdrahten** — Provider-Unregister-Hook oder Klassen-Zahl-Cap.
- [ ] **T2.9 [Coverage] Floor anheben + `persistence.api` gaten; `$apply`-Ausschluss prüfen.**
- [ ] **T2.10 [Coverage] `ChangeJournal` direkte Unit-Tests** — Eviction→410, rollback, Token-Staleness.
- [ ] **T2.11 [E2E] Concurrency-/Last-Test über HTTP** — paralleler HttpClient-Fan-out inkl. konkurrierender Writes.
- [ ] **T2.12 [E2E] Große-Payload-/Tiefpaging-Test** — >1000 Rows, mehrseitiger nextLink über HTTP.
- [ ] **T2.13 [Beispiele] README-Widerspruch** — read-only vs. POST 201 auflösen.
- [x] **T2.14 [Performance] `MemoryWriteRepository.begin()`** — ✅ mit T0.3 erledigt (Lazy-Capture statt Whole-Store-Kopie).

## Tier 3 — Niedrig / Politur

- [ ] **T3.1 [Style] FQN → Imports (systematischer Sweep)** — Runtime (~38), JPA, CSDL/Codec, Query, Client.
- [ ] **T3.2 [Robustheit] `OclEvaluator.dateTime()`** — TimeOfDay nach Edm-Typ statt Länge; substring-int-Range-Check.
- [ ] **T3.3 [DeadCode] `EcoreToEdmConverter.isEntity` entfernen.**
- [ ] **T3.4 [Kopplung] Aspect-Id-/COLLECTION_OPEN-Literale exportieren statt duplizieren.**
- [ ] **T3.5 [Security] CORS `Vary: Origin` beim Origin-Echo.**
- [ ] **T3.6 [Security] IAE→400 feste Meldung für Nicht-Parser-IAE (kein EMF-Detailleak).**
- [ ] **T3.7 [Client] Funktions-Literale URL-encoden; `parseInt`-Wrapping; injizierter HttpClient Redirect-Kontrakt; Error-Body-Auszug deckeln.**
- [ ] **T3.8 [Kapselung] defensive Kopien: `ODataPage`, `ComputedRow`, `ChangeJournal.Change`.**
- [ ] **T3.9 [Robustheit] `ODataVocabularies` Leer-Schema-Check; Enum long→int-Range.**
- [ ] **T3.10 [Style] Magic Numbers → benannte Konstanten/Config; gebrochene `{@value}`-Javadoc.**

---

## Verifizierte Nicht-Probleme (nicht anfassen)
- XXE auf dem Server-Request-Pfad: Writes sind JSON-only (415 sonst), kein fremdes XML geparst.
- Geteilte `OclEvaluator`/`OclTypeResolver`-Singletons: stateless/thread-safe; `CachingODataQueryParser` synchronisiert.
- `metadata`/`operation.api`/`schema.api` test=0: unkritisch (via metadata.tests bzw. reine Interfaces/Records).

## Fortschritts-Log
- 2026-07-16: Dokument angelegt; Review abgeschlossen (6 Bereiche), Blocker-Satz fixiert.
- 2026-07-16: **Tier 0 KOMPLETT** (T0.1–T0.6) + T2.14, je mit Tests committet; voller
  `build testOSGi` grün.
- 2026-07-16: **Tier 1 Sicherheits-/DoS-Kern KOMPLETT** (T1.1 CSDL-XML-Härtung, T1.2 async
  In-Flight-Cap, T1.3 $apply-Page-Cap) mit Tests committet. **Offen T1.4** (lauffähiges
  JPA-Beispiel — nur launch-verifizierbar, s. o.). Nächster: Tier 2 + Tier 3.
