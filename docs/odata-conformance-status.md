# OData 4.01 Conformance-Status (Spec Part 1, §13)

Status: 2026-07-08 (Neubewertung; siehe **Gesamturteil** unten), nachgeführt 2026-07-13
(Multipart-`$batch`, CSDL-JSON, `metadata=full/none`, IEEE754Compatible, Key-as-Segment,
gefiltertes `$count`, `/$filter`-Segment, Delta/Change-Tracking sind seitdem umgesetzt).
**Re-Audit 2026-07-14: 4.0 UND 4.01 Advanced sind klauselweise erfüllt und werden beansprucht**
(`ConformanceLevel=Advanced` im `$metadata`) — Klausel-Nachweis im Gesamturteil unten. Bewertet gegen die vendorten Spec-Artefakte in `reference/specs/`:
Part 1 Protocol (§13.1/§13.2), Part 2 URL Conventions, `odata-abnf-construction-rules.txt`
(normative Grammatik) und `odata-abnf-testcases.xml` (identisch mit der Test-Kopie im
Query-Bundle, per MD5 verifiziert).
**„Updatable OData Service“ (13.1.1 Nr. 18–33): seit 2026-07-07 BEANSPRUCHBAR (4.0 UND
4.01)** — 18 Edit-Links (minimal metadata: aus Context-URL+Key berechenbar) · 19 POST neue
Entities ✅ · 20 POST related (createRelated, Containment+Non-Containment) ✅ · 21 POST
$ref ✅ · 22 PUT $ref ✅ · 23 PATCH ✅ · 24 DELETE ✅ · 25 DELETE $ref (single clear +
Collection via `$id`) ✅ · 26 If-Match ✅ (schwache ETags aus dem serialisierten Zustand,
Einzel-GET liefert `ETag`, Update/Delete existierender Entities verlangt If-Match: 428/412)
· 27 Location ✅ · 28 OData-EntityId ✅ · 29 Upserts ✅ · 30/31 Property-Writes ✅
(Replace-basiert — Merge kann in EMF kein „null/Default setzen“ ausdrücken) · 32 Deep
Inserts (Containments) ✅ · 33 MAY offen. 4.01-Zusatz-MUST 13.2.1/19 (DELETE
Collection-Member-Ref per Key im URL) ✅. Offen (SHOULDs/MAYs): PUT mit nested content,
Deep Updates, `@odata.bind`, PATCH-Delta auf Sets.

## Gesamturteil (Neubewertung 2026-07-08, klauselweise gegen Spec §13)

Bewertet gegen den vollständigen §13-Klauselkatalog (Part 1 Protocol, 4.0-Level §13.1.x,
4.01-Level §13.2.x) mit einem zeilengenauen Code-Inventar als Beleg.

| Ebene | Urteil | Grundlage |
|---|---|---|
| **4.0 Minimal (§13.1.1)** | ✅ **erfüllt** inkl. *Updatable* (18–32) | Items 1–15 erfüllt; 16–17 async N/A; 33 ist MAY |
| **4.01 Minimal (§13.2.1)** | ✅ **erfüllt** | MUSTs 1–9 erfüllt/N-A; offen nur SHOULD (CSDL-JSON) / MAY |
| **4.0 Intermediate (§13.1.2)** | ✅ **erfüllt** | alle MUSTs **und** alle SHOULDs (1–17) |
| **4.01 Intermediate (§13.2.2)** | ✅ **erfüllt** | MUSTs 1–5 erledigt; SHOULDs 6/7/9 teilweise |
| **4.0 Advanced (§13.1.3)** | ✅ **erfüllt** (Re-Audit 2026-07-14) | MUSTs 1–12 komplett: 1 Intermediate ✅ · 2 CSDL-XML-$metadata (XSD-validiert) · 3 OData-JSON (E3-Codec) · 4 `/$count` auf Navs (walk) · 5 `any`/`all` (Lambdas) · 6–8 `$skip`/`$count`/`$orderby` · 9 `$expand` mit 9.1 `nav/$ref` ✅ 2026-07-13, 9.2 `$filter`-in-Expand, 9.3 Cast-in-Expand ✅ 2026-07-13 · 10 `$search` (beide Backends) · 11 Multipart-`$batch` ✅ 2026-07-09 · 12 Resource-Path-Konventionen (eigener Parser, ABNF-Suite). Offene SHOULDs (nicht blockierend): 13 async, 15 Cross-Join, 9.4–9.8 Expand-Sub-Optionen; 14 Delta ✅ |
| **4.01 Advanced (§13.2.3)** | ✅ **erfüllt** (Re-Audit 2026-07-14) | MUSTs komplett: 1 4.01 Intermediate ✅ · 2 4.0 Advanced ✅ · 3 Count gefilterter UND gesuchter Collections in Common Expressions (`$count($filter=…)` ✅ Welle 1, `$count($search=…)` ✅ 2026-07-14) · 4 `$compute` ✅ · 5.1 `$filter` auf selektierten Collections ✅ 2026-07-14 · 6 CSDL-JSON ✅ · 7 beide `$batch`-Formate ✅. Offene SHOULDs: 5.2–5.5 Select-Sub-Optionen, 9 verschachtelte Parameter-Aliase; 8 `/$filter`-Segment ✅ |

**Fazit: ALLE vier Level stehen — 4.0 und 4.01, Minimal bis ADVANCED (Re-Audit 2026-07-14).
Das `$metadata` annonciert `Capabilities.ConformanceLevel=Advanced`. Offen sind nur noch
SHOULDs/MAYs (async, Cross-Join, Expand-/Select-Sub-Optionen 9.4–9.8 bzw. 5.2–5.5,
verschachtelte Parameter-Aliase).**

### Ehemalige Advanced-Blocker (MUST-Ebene — inzwischen ALLE aufgelöst)

1. ~~**Multipart-`$batch`** (§13.1.3/11, §13.2.3/7)~~ — ✅ 2026-07-09: Server akzeptiert und
   beantwortet `multipart/mixed` (Change-Sets → atomicityGroups, Content-ID-Korrelation).
2. **`$expand`-Sub-Optionstiefe** (§13.1.3/9): die MUSTs sind seit 2026-07-13 komplett —
   verschachteltes `$filter` (9.2), **`$expand=nav/$ref`** (Referenz-Expansion, 9.1) und
   **Cast-in-Expand `nav/Ns.Type`** (9.3, auch kombiniert mit `$filter` gegen den abgeleiteten
   Typ). Offen bleiben die SHOULDs `$top/$skip/$orderby/$count/$search/$levels`-in-Expand
   (9.4–9.8) → 501.
3. ~~**CSDL-JSON-`$metadata`** (§13.2.3/6)~~ — ✅ 2026-07-09: `CsdlJsonWriter`/`CsdlJsonReader`,
   Server emittiert via `$format=json`/Accept, Client liest beide Formen.
4. ~~**Count einer gefilterten Collection in einer Common Expression** (§13.2.3/3)~~ — ✅ Welle 1
   (2026-07-10, `$count($filter=…)` → select→size); der GESUCHTE Count (`$count($search=…)`) folgte
   2026-07-14 (Suchworte → `contains`-Disjunktion über die String-Attribute des Element-Typs,
   gleiche Abbildung wie das Top-Level-`$search`). ~~**`$filter` auf selektierten Collections in
   `$select`** (§13.2.3/5.1)~~ — ✅ 2026-07-14: `SelectTree` trägt nested `$filter`
   (Nav-Collections gegen den Zieltyp, primitive Collections via `$it`), Anwendung im
   `EntityShaper` VOR dem Pruning (Prädikate dürfen wegprojizierte Properties referenzieren).

SHOULD-Ebene (nicht blockierend, aber Advanced-Qualität): async / `Respond-Async` (§13.1.3/13),
Cross-Join (15), strukturelle Vergleiche (§13.2.2/7), verschachtelte
Parameter-Aliase und `/$filter`-Pfadsegment (§13.2.2/10-11, §13.2.3/8-9).
**Delta-Change-Tracking (14) ist seit 2026-07-13 erfüllt** (`Prefer: odata.track-changes`,
selbstbeschreibende Delta-Links, Delta-Payloads in 4.0- UND 4.01-Form, 410 Gone,
`Capabilities.ChangeTracking`; v1-Grenzen: keine `$expand`-Deltas, kein PATCH-Collection-Update,
kein `/$count` auf Delta-Links, kein JPA-Backend — Details `odata-features.md`).

### Offene Verifizierungspunkte (eng, prüfenswert)

- ~~**`metadata=full`-Profil**~~ — ✅ 2026-07-09/10: `full` UND echtes `none` via
  Accept/`$format` (request-scoped), Power-BI-Interop-Lücke zu.
- **4.01-Minimal Item 9 Unterpunkte** (MUST für *unterstützte* Funktionalität): parameterlose
  Function-Imports OHNE Klammern (9.3), Action-Aufruf ohne Body (9.4), unqualifizierte
  Default-Namespace-Aufrufe (9.5) — Functions/Actions sind neu, diese URL-Syntaxvarianten sind
  ungetestet und könnten kleine Lücken sein.
- ~~**Backend-Paritätslücke**~~ — ✅ 2026-07-13: `round`/`floor`/`ceiling` pushen als
  jakarta-Criteria (Persistence 3.1+) — beide Backends decken jetzt dieselbe Funktionsfläche ab.
- ~~**`$ref` auf dem Read-Pfad**~~ — ✅ 2026-07-13: `GET …/nav/$ref` liefert
  Entity-Referenzen (single + collection inkl. `$filter`/Paging; keylose Containment-Kinder
  ehrlich 501).
- **Client** („Interoperable OData Client"): stark (typisierte Reads/Writes, Functions/Actions inkl.
  typisierter Varianten, Batch-Builder mit `dependsOn`/`atomicityGroup`, beide `$batch`-Wire-Formen,
  CSRF, Bearer-/Basic-Auth, Schema-Registry, Delta/Change-Tracking); liest CSDL **XML und JSON**
  (Shape-Detection). Ob er bei Payload-Requests `OData-Version` setzt, ist ungeprüft.

### Weg zu Advanced (Backlog, nach Aufwand × Nutzen)

1. ~~**CSDL-JSON-`$metadata`**~~ — ✅ 2026-07-09.
2. ~~**`metadata=full`**~~ — ✅ 2026-07-09 (+ echtes `none` 2026-07-10).
3. ~~**Multipart-`$batch`**~~ — ✅ 2026-07-09.
4. ~~**`$expand`-Sub-Optionen (MUSTs)**~~ — ✅ 2026-07-13: `$ref` + Cast; die SHOULDs
   (`$top/$skip/$orderby/$count/$search/$levels` in Expand) bleiben 501.
5. ~~**`$ref`-Read-Pfad**~~ — ✅ 2026-07-13 (~~Count gefilterter Collection~~ ✅ Welle 1,
   ~~`/$filter`-Segment~~ ✅ Welle 2/3).
6. ~~**JPA-Pushdown für round/floor/ceiling**~~ — ✅ 2026-07-13 (date-Funktionen ✅ 2026-07-07).
7. async / ~~Delta~~ (✅ 2026-07-13) / Cross-Join (SHOULDs).
8. ~~**4.01-Advanced-MUST 5.1**~~ — ✅ 2026-07-14 (womit ALLE Advanced-MUSTs stehen; siehe
   Gesamturteil).

## 13.1.1 — OData 4.0 Minimal Conformance (read-only-relevante Items)

| # | Anforderung | Status |
|---|---|---|
| 1 | Service Document am Root | ✅ |
| 2 | Daten gemäß [OData-JSON] | ✅ **behoben 2026-07-04**: Server antwortet mit `odata.metadata=minimal`-Semantik — berechenbare Control-Info (`@odata.type` nicht-abgeleiteter Typen, `@odata.id` bei Key im Payload) wird gemäß [OData-JSON] 4.5.8 WEGGELASSEN statt in falscher Form emittiert; Key-Properties bleiben immer im Payload. `#Ns.Type` für abgeleitete Typen folgt mit Vererbungs-Support |
| 3 | **Server-Driven Paging bei partiellen Ergebnissen (`@odata.nextLink`)** | ✅ **behoben 2026-07-04**: Peek top+1, `$skip`-fortschreibender nextLink (Collections UND $apply-Rows); e2e-Roundtrip getestet |
| 4 | `OData-Version`-Header | ✅ **verbessert 2026-07-04**: Antwort ist `4.01`, außer der Client pinnt `OData-MaxVersion: 4.0` (8.1.5-Negotiation) |
| 5 | `Accept` / **`OData-MaxVersion`** Semantik | ✅ **behoben 2026-07-04**: MaxVersion validiert (4.0/4.01, sonst 400); Accept json/xml |
| 6 | Extensibility-Regeln | 🟡 |
| 7 | **ABNF-konform parsen; unsupported System-Options → `501`** | ✅ **behoben 2026-07-04**: Whitelist — bekannte-unsupported (`$search`,`$compute`,…) → 501, unbekannte `$`-Optionen → 400 |
| 8 | Nur CSDL-Typen | ✅ |
| 9 | Keine Pflicht-Annotations für Clients | ✅ |
| 10/11 | Update-Semantik nicht verletzen | ✅ (trivial, read-only) |
| 12 | SHOULD `$expand` | ✅ (eine Ebene) |
| 13 | SHOULD `$metadata` CSDL-XML | ✅ |
| 14 | Prefixed Header-/Preference-Varianten | 🟡 (kein `Prefer`-Support) |
| 15 | Enum-/Duration-Literale MIT Typ-Präfix | ✅ |

**Bewertung (Stand 2026-07-04): die MUST-Verstöße sind behoben — 4.0 Minimal Conformance
(read-only) ist damit beanspruchbar** (Rest-Gelb: OData-JSON-Formfeinheiten Nr. 2, Prefer Nr. 14).

## 13.2.1 — OData 4.01 Minimal (Zusatz-Items, Auswahl)

| Anforderung | Status |
|---|---|
| 4.01-[OData-JSON]-Compliance | 🟡 (wie oben) |
| Header/Preferences mit UND ohne Präfix | ✅ **substanziell seit 2026-07-06**: `Prefer: odata.maxpagesize` wird ausgewertet — MIT und OHNE `odata.`-Präfix, angewandter Wert via `Preference-Applied` (8.2.8.7); weitere Preferences unbekannt → ignoriert (Hint-Semantik) |
| **System-Options mit und ohne `$`-Präfix** | ✅ **behoben 2026-07-04**: `option()`-Normalisierung im Servlet (`filter=` = `$filter=`); Custom-Options ohne `$` und ohne Options-Namen werden ignoriert (11.2.12) |
| **Case-insensitive Options-/Operator-/Funktionsnamen** | ✅ **behoben 2026-07-04**: Operatoren/Funktionen (4.01-Fix 2026-07-03) UND Options-Namen (`$FILTER` = `$filter`) |
| Identifier case-treu zurückgeben | ✅ |
| Enum-/Duration-Literale OHNE Präfix | ✅ **belegt 2026-07-04**: präfixlose Form ist per ABNF ein quoted Literal — parst als String, Evaluator vergleicht Enums namensbasiert (`color eq 'Green'`, Test in InMemoryQueryServiceTest); Duration analog als String |
| String→Primitive-Casts in URLs | 🟡 (`cast()` vorhanden) |
| Parameter-Aliase (`@p`) | ✅ **seit 2026-07-06**: `@name` in `$filter`/`$orderby` (auch nach `$apply`), Werte sind Expressions, rekursiv auflösbar (Tiefen-Cap 8 gegen Zyklen), unaufgelöst → 400; Alias-Werte durchlaufen die Pre-Parse-Limits; Cache wird bei Alias-Queries umgangen. Aliase als KEY-Werte offen (Key-Predicate-Parser, brauchts erst mit `/`\|`\`-fähigen String-Keys) |
| SHOULD `in` | ✅ · SHOULD `divby` ✅ **2026-07-06** (→ OCL `/`, Dezimaldivision) · SHOULD negatives substring ✅ **2026-07-07**: negativer Start zählt vom Ende (geklemmt), Start > Länge → Leerstring; in-memory UND JPA-Pushdown (CASE über LENGTH), Tests in beiden Differentialtests |
| SHOULD `eq/ne null` auf Single-Navs | ✅ **belegt 2026-07-06**: funktionierte end-to-end (terminale EReference → ClassifierType, Evaluator-Objektgleichheit mit null); Regressionstest `navigationNullComparison` in InMemoryQueryServiceTest |
| SHOULD CSDL-XML **und** CSDL-JSON | ❌ (JSON fehlt, Q9) |
| SHOULD `Core.ODataVersions` / Capabilities-Annotations | ✅ **komplett seit 2026-07-06**: `Core.ODataVersions="4.0 4.01"` + Capabilities am EntityContainer (`ConformanceLevel=Minimal`, `BatchSupported=true` seit 2026-07-08, `AsynchronousRequestsSupported=false`, `KeyAsSegmentSupported=false`) + Core-/Capabilities-`edmx:Reference` |

**Bewertung: 4.01 Minimal (read-only) erfüllt** — verbleibendes ❌ ist ein SHOULD (CSDL-JSON/Q9).

## 13.1.2 — Intermediate (Ausblick, read-only-relevant)

✅ `$select`, `$top`, `$skip`, `$count`(-Option), `$orderby`, eq/ne-Filter, Lambdas any/all
· ✅ **NEU 2026-07-04 (eigener URI-Parser, ADR-0005)**: `/$value` auf Properties,
`/$count`-URL-Segment (Set + Navigation, inkl. gefiltertem Set-Count), Pfad-Navigation
(`Set(key)/nav/prop`), Property-Adressierung · ✅ **NEU 2026-07-06**: Aliase in `$filter`
(13.1.2/7.2 MUST) und **Derived-Type-Casts in URLs** (13.1.2/4 MUST): `Set/Ns.T` (Typ-Filter
via `EntityQuery.castType`, abgeleiteter Typ = Options-Kontext, Context-URL `#Set/Ns.T`),
`Set/Ns.T(key)` (404 bei Typ-Mismatch), Casts in Navigationspfaden (Collection-Filter/
Instanz-Check), max. ein Cast pro Schritt, `#Ns.Type`-Discriminator im minimal-metadata-JSON
· ✅ **NEU 2026-07-06 (4.01-Intermediate-MUSTs)**: `eq/ne null` auf Single-Navigationen
(funktionierte bereits end-to-end — Builder/TypeResolver/Evaluator konnten es; per
Regressionstest belegt statt gebaut, analog präfixlose Enum-Literale) und **nested `$select`**
(`$select=name,category($select=name)`: `SelectTree`-Parser mit Klammer-bewusstem Splitting +
Modell-Validierung, `EntityShaper` prunt rekursiv, Sub-Optionen außer `$select` → klare
Ablehnung, `$select`-Werte laufen durch die Pre-Parse-Limits)
· ✅ **NEU 2026-07-08 (Intermediate-SHOULDs nachgezogen)**: `$search` (server-seitig als
`contains`-OR über String-Properties synthetisiert, AND mit `$filter`, über den bestehenden
Pushdown → beide Backends; Client-`.search()`), `$compute` (server-seitig: `compute(…)`-Grammatik
wiederverwendet, pro Entity im Servlet via `OclEvaluator` ausgewertet und in die Antwort gespleißt
— backend-agnostisch; Client-`.compute()` sendet es), `$filter` auf expandierten Entities
(`$expand=reviews($filter=…)`, Test `filterInExpand`; die frühere ❌ war veraltet)
· ✅ **NEU 2026-07-08**: **`$compute`-Alias in `$filter`/`$orderby`/`$select`**: der Alias wird über
`parseFilterAfterApply`/`parseOrderByAfterApply` aufgelöst und dann **in die OCL eingesetzt** (die
`VariableExp` des Alias wird durch eine Kopie des definierenden Ausdrucks ersetzt) → referenziert nur
reale Properties, wird also normal ins Backend gepusht (kein In-Memory-Nachfiltern, Paging bleibt
korrekt). `$select` toleriert Alias-Token (reale Properties validieren, Alias-Member werden projiziert).
End-to-end-itest `computeAliasQuery` (`price mul 2 as doubled` → `$filter=doubled ge 8`)
· ✅ **NEU 2026-07-08**: **Functions & Actions** (Advanced): unbound+bound Functions (GET,
primitiv/Entity/Collection), unbound Actions (POST) via `ODataOperationHandler`-SPI; Client
`function`/`boundFunction`/`action` **plus typisierte Varianten** `functionAsEntity`/
`functionAsCollection`/`actionAsEntity`/`actionAsCollection`/`boundFunctionAsEntity`/
`boundFunctionAsCollection` (Entity → `EObject`, Collection → `ODataPage` über den E3-Codec)
· ✅ **NEU 2026-07-08**: **Query-Optionen auf Navigationspfaden**: `$filter`/`$orderby`/`$top`/`$skip`/
`$count` auf einer Navigations-Collection (`Set(key)/nav?$filter=…&$orderby=…`), `/$count` zählt die
**gefilterte** Collection, `$orderby` sortiert in-memory (multi-key, null-first); nicht implementierte
Optionen (`$select`, `$expand`, …) auf Nav-Pfaden weiterhin 501; Client threadet die
Query-Option-Builder über `navigateCollection`
· ✅ **NEU 2026-07-08**: **`$batch` (OData-v4.01-JSON-Batch-Format)**: `POST <root>/$batch` mit
`{"requests":[…]}`; jede Sub-Request wird über eine synthetische Request/Response durch dieselbe
`service()`-Pipeline dispatcht (Query-Optionen, Writes, Functions verhalten sich wie top-level);
`dependsOn` → `424 Failed Dependency` bei fehlgeschlagenem Vorgänger; klassisches
`multipart/mixed` → `415`; Client-Builder `ODataClient.batch()` (`read`/`create`/`update`/`delete`/
`add`, `Result.asEntity`/`asPage`).
· ✅ **NEU 2026-07-08**: **atomare `$batch`-Change-Sets (`atomicityGroup`)**: ein zusammenhängender
Lauf gleicher-Gruppen-Requests läuft in einer Transaktion auf jedem transaktionalen Write-Backend
(`WriteService.transactional()`/`begin`/`commit`/`rollback`, thread-gebunden). Alle Mitglieder
erfolgreich → commit; ein Fehler → rollback und jedes nicht-fehlerhafte Mitglied wird zu `424`
(all-or-nothing). Das In-Memory-Referenz-Backend implementiert es per Store-Snapshot (atomar, aber
nicht voll isoliert gegen gleichzeitige Schreiber — ein echtes JPA-Write-Backend würde das liefern).
Client: `ODataBatch.add(…, atomicityGroup)`. End-to-end-itest `batchAtomicityGroupRollback`.
· ✅ **NEU 2026-07-08**: **JPA-Write-Backend transaktional**: `JpaQueryService` implementierte den
`WriteService` schon (create/update/delete/link/unlink, je eigene EM-Transaktion); jetzt zusätzlich
die thread-gebundenen Hooks — `begin()` öffnet je Factory eine EM mit aktiver Transaktion, jeder
Write auf dem Thread tritt ihr bei (statt selbst zu committen), `commit()`/`rollback()` schließen
alle ab. Damit sind `$batch`-`atomicityGroup`s auch gegen JPA atomar UND isoliert (echte
DB-Transaktion). Tests `ambientTransactionRollback`/`ambientTransactionCommit` gegen H2.
· ✅ **NEU 2026-07-08**: **`$compute`-Werte client-seitig typisiert lesen**: `EntitySetRequest.listComputed()`
liefert `ComputedRow`s — die Entity typisiert als `EObject` (Modell-Properties) plus die berechneten
Member; `ComputedRow.value(alias, Class)` coerct auf den gewünschten Java-Typ (Member, die keine
Struktur-Property des Typs sind, gelten als berechnet). `listRaw()`/`Map` bleibt als generischer Weg.
**Alle MUSTs für 4.0 UND 4.01 Intermediate erfüllt; die zentralen Intermediate-SHOULDs (`$search`,
`$compute` inkl. Alias in `$filter`/`$orderby`/`$select` + typisiertes Client-Lesen,
`$filter`-in-`$expand`, Nav-Pfad-Query-Optionen inkl. `$orderby`) plus `$batch` mit atomaren
Change-Sets (In-Memory und JPA) jetzt ebenfalls. Offene Nicht-SHOULD-Punkte: `@odata.bind` /
Non-Containment-Writes, CSDL-JSON-`$metadata` (Q9), asynchrone Requests.**

## Priorisierte Findings (Conformance-Fixes, alle klein bis mittel)

1. ~~`@odata.nextLink`~~ ✅ 2026-07-04
2. ~~`OData-MaxVersion`~~ ✅ 2026-07-04
3. ~~501 statt Ignorieren~~ ✅ 2026-07-04
4. ~~Query-Options case-insensitiv und ohne `$`-Präfix~~ ✅ behoben 2026-07-04.
5. ~~Enum-/Duration-Literale ohne Typ-Präfix~~ ✅ belegt 2026-07-04 (präfixlos = String-Literal, namensbasierte Enum-Evaluation).
6. ~~`@odata.type`/`@odata.id` in falscher Form~~ ✅ behoben 2026-07-04: minimal-metadata-Semantik lässt berechenbare Control-Info weg; `#Ns.Type` wird erst mit abgeleiteten Typen (Vererbung) nötig.
7. ~~Capabilities-Annotations im `$metadata`~~ ✅ 2026-07-06; CSDL-JSON (Q9) bleibt offen (SHOULD).
8. ~~Parameter-Aliase, `divby`, `Prefer: maxpagesize`~~ ✅ 2026-07-06.

## Einordnung Q10 (Ziel-Level)

**Stand 2026-07-08: Q10-Ziel „mindestens Intermediate" erreicht — 4.0 + 4.01 Minimal UND
4.0 + 4.01 Intermediate (read-only + Updatable) erfüllt** (Details klauselweise im **Gesamturteil**
oben). Alle MUSTs beider Intermediate-Level stehen (13.1.2: ABNF-parse-or-fail, `$select`,
Derived-Type-Casts, `$top`, `/$value`, `$filter` inkl. eq/ne + Aliase + fail-unsupported, Lambdas,
`/$count`-Segment, `$orderby`, `$search`, `$expand` inkl. `$filter`; 13.2.2: `eq/ne null` auf
Single-Navs, `in`, nested `$select`, `$compute`). **Advanced (4.0 §13.1.3 / 4.01 §13.2.3) ist
bewusst noch offen** — Blocker und priorisierter Backlog stehen im Gesamturteil (Multipart-`$batch`,
`$expand`-Sub-Optionen inkl. `$ref`/Cast, CSDL-JSON; SHOULDs async/Delta/Cross-Join).

Historie: der Weg dahin lief über den Resource-Path-Parser (`/$value`, `/$count`-Segment,
Pfad-Navigation) — seit ADR-0005 (Olingo archiviert) Eigenbau auf der bestehenden
ANTLR4-Infrastruktur, mit den vendorten ABNF-Fällen (`resourcePath` 37, `odataRelativeUri` 154) als
Akzeptanz-Testsuite. „Updatable" ist ein eigenes Paket (Schreibpfad + ETags + Location/EntityId-Header)
und wurde als solches umgesetzt.
