# OData 4.01 Conformance-Status (Spec Part 1, §13)

Status: 2026-07-06. Bewertet gegen die vendorten Spec-Artefakte in `reference/specs/`:
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
· ✅ **NEU 2026-07-08**: **Functions & Actions** (Advanced): unbound+bound Functions (GET,
primitiv/Entity/Collection), unbound Actions (POST) via `ODataOperationHandler`-SPI; Client
`function`/`boundFunction`/`action`
· ✅ **NEU 2026-07-08**: **Query-Optionen auf Navigationspfaden**: `$filter`/`$top`/`$skip`/`$count`
auf einer Navigations-Collection (`Set(key)/nav?$filter=…`), `/$count` zählt die **gefilterte**
Collection; nicht implementierte Optionen (`$orderby`, `$expand`, …) auf Nav-Pfaden weiterhin 501;
Client threadet die Query-Option-Builder über `navigateCollection`
· ✅ **NEU 2026-07-08**: **`$batch` (OData-v4.01-JSON-Batch-Format)**: `POST <root>/$batch` mit
`{"requests":[…]}`; jede Sub-Request wird über eine synthetische Request/Response durch dieselbe
`service()`-Pipeline dispatcht (Query-Optionen, Writes, Functions verhalten sich wie top-level);
`dependsOn` → `424 Failed Dependency` bei fehlgeschlagenem Vorgänger; klassisches
`multipart/mixed` → `415`; Client-Builder `ODataClient.batch()` (`read`/`create`/`update`/`delete`/
`add`, `Result.asEntity`/`asPage`). ⚠️ **Lücke**: `atomicityGroup` wird geparst, aber es gibt kein
Cross-Request-Rollback (WriteService committet pro Aufruf) → Change-Sets best-effort, nicht atomar.
· ❌ Rest-SHOULDs: `$compute`-Alias referenziert in `$filter`/`$select`/`$orderby`, `$orderby` auf
Navigationspfaden, `$compute`-Werte client-seitig typisiert lesen, atomare `$batch`-Change-Sets —
**alle MUSTs für 4.0 UND 4.01 Intermediate erfüllt; die zentralen Intermediate-SHOULDs (`$search`,
`$compute`, `$filter`-in-`$expand`, Nav-Pfad-Query-Optionen) plus `$batch` jetzt ebenfalls**.

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

**Stand 2026-07-06 EOD: 4.0 + 4.01 Minimal UND 4.0 + 4.01 Intermediate (read-only) im
Wesentlichen erfüllt** — alle MUSTs beider Intermediate-Level stehen (13.1.2:
ABNF-parse-or-fail, `$select`, Derived-Type-Casts, `$top`, `/$value`, `$filter` inkl. eq/ne +
Aliase + fail-unsupported; 13.2.2: `eq/ne null` auf Single-Navs, `in`, nested `$select`);
offen nur SHOULDs (`$search`, `$filter` auf expandierten Entities, count-of-filtered-
collection, `$compute`, strukturelle Vergleiche; Basic-Auth = Deployment-Thema). Historie:
der Weg dahin lief über den Resource-Path-Parser
(`/$value`, `/$count`-Segment, Pfad-Navigation) — seit ADR-0005 (Olingo archiviert)
Eigenbau auf der bestehenden ANTLR4-Infrastruktur, mit den vendorten ABNF-Fällen
(`resourcePath` 37, `odataRelativeUri` 154) als Akzeptanz-Testsuite — plus `$search`. „Updatable“ ist ein eigenes Paket (Schreibpfad + ETags
+ Location/EntityId-Header) und sollte als solches geplant werden.
