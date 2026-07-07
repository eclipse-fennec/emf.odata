# Gap-Analyse: OASIS-Spec-Repositories vs. Fennec-OData-Implementierung

Status: 2026-07-06. Bezieht sich auf die Checkouts in `reference/specs/` (read-only).
Ergänzt `odata-conformance-status.md` (Spec-§13-Sicht) um die Artefakt-/Tooling-Sicht.

## Repo-Übersicht und Bewertung

| Repo | Inhalt | Nutzen für uns | Status |
|---|---|---|---|
| `odata-abnf` | AKTUELLE TC-Grammatiken + Testfälle: Core (840 Fälle, YAML), **Aggregation (201)**, Temporal | Testdaten-Goldgrube | ✅ **Aggregation-Fälle übernommen** (s.u.); ✅ **Core-YAML übernommen 2026-07-06** (s.u.), XML-Harness (4.01 OS) läuft parallel weiter |
| `odata-csdl-schemas` | **4.01-XSDs** (edm/edmx), `csdl.schema.json` (CSDL-JSON!), Java-`Validator` | XSD-Validierung, Q9-Grundlage | ✅ **vendorte XSDs auf 4.01 aktualisiert** (Tests grün); `csdl.schema.json` vorgemerkt für Q9 |
| `odata-vocabularies` | AKTUELLE Vocabularies (Core mit `ODataVersions`, `SchemaVersion`, `AlternateKeys`, …) + neue Vokabulare (Aggregation, Authorization, JSON, Repeatability, Temporal) | E1-Versorgung | ✅ **vendorte Core/Capabilities/Validation/Measures aktualisiert** (Tests grün); neue Vokabulare bei Bedarf ergänzbar (nur XML kopieren) |
| `odata-json-schema` | JSON-Schemas für OData-JSON-Payloads + Beispiele | Antwort-Validierung (E3/E6-Tests) | 🟡 vorgemerkt: bräuchte einen JSON-Schema-Validator als Test-Dep (nicht in central.mvn) — lohnt, sobald @odata.context/`#Ns.Type` umgesetzt sind |
| `odata-specs` | Spec-QUELLEN (Markdown) inkl. **`odata-data-aggregation-ext`** (das $apply-Narrativ!) und temporal-ext | Nachschlagewerk | ✅ Referenz — Aggregation-Semantik ($these, rollup, from, custom aggregates) für den $apply-Ausbau hier nachlesen |
| `odata-openapi` | CSDL→OpenAPI-Konverter (Node/XSLT) | mögliches späteres Feature (`$metadata`→OpenAPI-Endpoint) | 🔵 Ideen-Backlog, kein Testbezug |
| `odata-v4.01-os` | Die OS-Standard-Artefakte (Duplikat unserer Einzeldateien) | kanonische Referenz | ✅ deckungsgleich mit bereits vendorten Dateien |

## Tooling-Bewertung

- Die Repo-eigenen Testrunner (`lib/`, `test/`) sind **Node.js/apg-js-basiert** und testen die
  *ABNF-Grammatiken selbst* — für unseren Java/bnd-Build nicht integrierbar und auch nicht
  nötig: **wir konsumieren die Testfall-DATEN direkt** in JUnit-`@TestFactory`-Harnessen
  (bewährtes Muster, jetzt 3 Suiten). `odata-csdl-schemas/java/Validator.java` macht exakt
  das, was unser `assertValidCsdl` schon tut — bestätigt den Ansatz, nichts zu übernehmen.
- Wichtiges Detail aus dem ABNF-Repo: die Grammatiken kodieren **Modell-Kategorien** in den
  Regelnamen (Identifier-Klassen wie `entitySetName`, `complexTypeName` werden vom TC-Runner
  per Substitution eingespeist). Negative Testfälle, die daran hängen („collection-valued …"),
  sind syntax-only nicht beurteilbar — im Harness als eigene Skip-Klasse ausgewiesen.

## Übernommen (2026-07-04)

1. **Aggregation-ABNF-Testfälle** → `AggregationAbnfAcceptanceTest` im Query-Bundle
   (YAML vendored; toleranter Zeilen-Reader, kein YAML-Dep): 166 `$apply=`-Fälle,
   **31 aktiv verifiziert / 135 Skips** = präzises Radar für den Aggregation-Backlog
   (BottomTop/Concat, `expand`/`nest`/`addnested`, `rollup`/`$all`, `from`,
   Custom Aggregates, `$these`, keyed path segments).
2. **4.01-XSDs** ersetzen die errata03-Versionen in `csdl/testdata/schemas/` — unsere
   `$metadata`-Ausgabe validiert auch gegen die aktuellen Schemas (alle Tests grün).
3. **Aktuelle Vocabularies** ersetzen die errata03-Versionen — u.a. ist `Core.ODataVersions`
   jetzt verfügbar (Voraussetzung für ein 4.01-Minimal-SHOULD).

## Übernommen (2026-07-06)

4. **Core-ABNF-YAML** (840 Fälle, inkl. 4.02-Vorarbeiten — bewusst akzeptiert, Beschluss
   2026-07-06) → `CoreYamlAbnfAcceptanceTest` im Query-Bundle, gemeinsamer Zeilen-Reader
   `OasisAbnfYaml` (auch vom Aggregation-Harness genutzt). 504 generierte Fälle über die
   Regeln mit v1-Entry-Point (Expressions, `resourcePath`, `queryOptions`,
   `odataRelativeUri` mit `?`-Split und FailAt-basierter Options-Lokalisierung):
   **180 aktiv / 324 Skips** = Radar für E4-/ADR-0005-Backlog. Regeln ohne Entry-Point
   (Literale, header/preference, `context`, select/expand-Bäume, geo) werden nicht
   generiert; sie folgen mit der jeweiligen Schicht. Der Erstlauf fand zwei echte Lücken,
   beide behoben: `$count`/`$value`/`$ref` waren nicht als terminale Segmente erzwungen
   (`ODataResourceParser`), und `in ()` (leere Liste, aktuelle TC-Grammatik) fehlte in der
   Expression-Grammatik (neue Alternative + `ODataToOclBuilder` → leeres Set, matcht nie).

## Identifizierte Gaps (führen die bestehenden Backlogs fort)

- **Aggregation-Ausbau** (E4): die 135 Skips oben — größte Blöcke: Set-Transformationen
  (topcount/bottomcount/…, concat), `rollup`, Custom Aggregates, `$these`.
- ~~**Core-YAML-Migration**~~ ✅ 2026-07-06 übernommen (s.o.); der XML-Harness
  (`AbnfAcceptanceTest`, 4.01 OS) läuft als Referenzlinie parallel weiter.
- **CSDL-JSON (Q9)**: mit `csdl.schema.json` existiert jetzt die Validierungsgrundlage —
  Implementierung + Schema-Validierungstest als Paket planen.
- **Payload-Validierung**: `odata-json-schema` gegen unsere Antworten, gekoppelt an die
  E3-Lücken (@odata.context, `#Ns.Type`); braucht Validator-Dependency-Entscheidung.
- **Temporal-Extension**: Grammatik+Fälle vorhanden; für uns Phase 3+ (nicht im req-Scope).
