# Fennec OData – Empfohlene Projekt-Ablage für Claude Code

> Ablage-Regeln für den Fennec-OData-Workspace (entstanden als Begleitdokument der
> ursprünglichen Anforderungsphase; das Requirements-Doc ist inzwischen in
> `odata-open-tasks.md` + Git-Historie aufgegangen).
> Zweck: alle technischen und fachlichen Ressourcen so lokal ablegen, dass im Arbeitsalltag **keine Web-Suche** nötig ist – Specs, Schemata, Referenz-Code und Test-Material liegen offline bereit.

---

## 1. Leitprinzipien

**Vier Klassen von Inhalten, klar getrennt nach Pfad:**

| Klasse | Ort | Bedeutung |
|--------|-----|-----------|
| **Haupt-Repo** | `fennec-odata/` (top-level) | **DAS Liefer-Repo.** Alle `emf.odata.*`-Bundles inkl. `emf.odata.metadata`. Read-Write. |
| **Quell-Abhängigkeit (RW)** | `fennec-metadata/` (top-level) | `emf.model.metadata` als **Quelle**, falls am *generischen* Framework etwas anzupassen ist. Read-Write. **OData-spezifischer Code gehört hier NICHT rein** – der landet im Haupt-Repo. |
| **Binär-Abhängigkeiten** | über Build-Repository (bnd/Maven) + Quelle read-only unter `reference/fennec/` | `emf.osgi`, `emf.codec`, `emf.persistence-jpa`, `emf.m2x`: im Build **nur binär** referenziert. Quelle nur zum Studieren. |
| **Read-Only-Material** | `reference/`, `testdata/` | Fremde Implementierungen, Specs, Schemata, Test-Daten. Niemals editieren. |

**Warum diese Trennung:**
- `fennec-odata/` steht ganz oben und allein → unmissverständlich das Haupt-Repo.
- Build-Abhängigkeiten kommen als **Binärartefakte** (bnd-/Maven-Repository), nicht aus den Quellordnern. Die read-only-Quellen unter `reference/fennec/` sind ausschließlich **Nachschlage-Material** und werden nicht gebaut.
- Claude erkennt am Pfad sofort die Schreibzone: nur `fennec-odata/`, `fennec-metadata/` und `docs/`. Alles unter `reference/` und `testdata/` ist tabu.
- `reference/` und `testdata/` sind reproduzierbar (klonen/laden) → komplett per `.gitignore` aus dem Git heraushaltbar.

---

## 2. Ordnerstruktur (Gesamtbaum)

```
fennec-odata-dev/                      ← Claude-Code-Arbeitsverzeichnis (Workspace-Root)
│
├── CLAUDE.md                          ← Kontext-/Regeldatei für Claude Code (s. §7)
├── README.md
├── .gitignore                         ← hält reference/ + testdata/ aus dem Git (s. §8)
├── setup-references.sh                ← klont/lädt alle Read-Only-Ressourcen (s. §6)
│
├── fennec-odata/                      ★═══════ HAUPT-REPO (Read-Write) ═══════★
│   │                                    Bundle-Layout nach req-doc §5.2:
│   ├── emf.odata.api/                   OData-Annotations, Public SPIs
│   ├── emf.odata.metadata/              ODataMetadataHandler  ← hier, NICHT im Metadata-Framework
│   ├── emf.odata.query/                 ANTLR4-Grammar, Query-Modell-Erweiterung
│   ├── emf.odata.codec.csdl/            CSDL-Meta-Codec (direkter Konverter, β)
│   ├── emf.odata.codec.json/            OData-JSON Daten-Codec-Profil
│   ├── emf.odata.vocabularies/          OASIS-Standard-Vocabularies als EPackages
│   ├── emf.odata.protocol.v4/           Servlet + Servlet-Filter, Olingo-Wrapper, Batch
│   ├── emf.odata.runtime/               Dispatcher, EdmRegistry, Composition
│   ├── emf.odata.persistence.api/       QueryService-SPI, OCL-AST-Visitor-Basis
│   ├── emf.odata.persistence.jpa/       JPA-Impl
│   ├── emf.odata.client/                Client-Komponente (HTTP, DSL, Cache)
│   └── emf.odata.test/                  Konformitäts- + Akzeptanztests
│
├── fennec-metadata/                   ═══════ QUELL-ABHÄNGIGKEIT (Read-Write) ═══════
│   └── (emf.model.metadata)             nur generisches Framework anpassen, falls nötig
│
├── docs/                              ← eigene Doku (Read-Write; liegt IM Repo, außen Symlink)
│   ├── odata-architecture.md            ← Architektur (konsolidierter Ist-Stand)
│   ├── odata-open-tasks.md              ← DAS Aufgaben-Dokument (alle offenen Punkte)
│   ├── odata-conformance-status.md      ← Klausel-Nachweis OASIS §13
│   ├── odata-live-interop-findings.md   ← Live-Findings (publiziert)
│   ├── manual/                          ← publizierte EN-Doku (GitHub Pages via docs-site/)
│   ├── decisions/                       ← ADRs (nur gehaltene Architektur-Entscheidungen)
│   └── fennec-odata-workspace-layout.md ← DIESES Dokument
│
├── reference/                         ═══════ READ-ONLY ═══════
│   ├── fennec/                          Quelle der binär-referenzierten Fennec-Repos –
│   │   │                                NUR zum Studieren, NICHT Build-Input
│   │   ├── emf.osgi/
│   │   ├── emf.codec/                   (Vorbild-Codecs jsonschema/openapi; codec.metadata)
│   │   ├── emf.persistence-jpa/
│   │   └── emf.m2x/                     (OCL-Modell, EDM/EDMX-Ecore, OclLruExpressionCache)
│   ├── code/                            fremde Implementierungen (Code-Studium)
│   │   ├── java/  ├── dotnet/  ├── js/  ├── python/  └── go/
│   ├── specs/                           normative Specs als PDF/HTML
│   │   ├── odata/  ├── bsi/  ├── ocl/  └── platform/
│   └── schemas/                         maschinenlesbare Schemata
│       ├── csdl-xml/  └── csdl-json/
│
└── testdata/                          ═══════ TEST-MATERIAL (Read-Only) ═══════
    ├── abnf-test-cases/                 OASIS ABNF-Akzeptanz-Cases → E4-Parser
    ├── metadata-samples/                $metadata-Beispiele (TripPin, Northwind …)
    ├── edm-models/                      Sample-EDM/EPackages (ODataSamples)
    └── service-endpoints.md             Live-Test-Services (URLs, nur Doku, s. §5.5)
```

---

## 3. Schreibzonen im Detail

### 3.1 `fennec-odata/` – das Haupt-Repo

Das eigentliche Liefer-Repo, eigenes Git (Branch `snapshot` analog Fennec-Konvention). Bundle-Struktur exakt nach req-doc §5.2 (s. Baum oben).

**Wichtig:** `emf.odata.metadata` (der `ODataMetadataHandler`) ist ein **Bundle dieses Repos** – die OData-spezifische Metadata-Implementierung lebt hier, nicht im generischen Metadata-Framework. Der Handler dockt zur Laufzeit über die `MetadataHandler`-SPI an den Metadata-Service an, ohne dort Code zu hinterlassen.

### 3.2 Metadata-Framework – seit 2026-07 Teil von `emf.osgi`

Das generische Framework lag ursprünglich im eigenen Repo `eclipse-fennec/emf.model.metadata`
(Workspace-Ordner `fennec-metadata/`). Seit `emf.osgi` 1.1.0 lebt es dort: Modell und
Service-API in `org.eclipse.fennec.emf.osgi.metadata`, Fingerprint/Artifact-Store in
`org.eclipse.fennec.emf.osgi.api`. Damit ist es eine **rein binäre** Abhängigkeit wie die
anderen Fennec-Repos (§3.3) – der Ordner `fennec-metadata/` und das Spender-Repo sind
Altlast. Migrations-Mapping (Pakete, `Optional`-Rückgaben, `AspectProvider` →
`MetadataHandler`, Profile → `AspectEntry`): `emf.osgi/docs/metadata-migration-from-model-metadata.md`.

### 3.3 Binär-referenzierte Fennec-Repos

`emf.osgi`, `emf.codec`, `emf.persistence-jpa`, `emf.m2x` werden im Build als **Binärartefakte** über die bnd-/Maven-Repository-Konfiguration des `fennec-odata`-Workspace bezogen – **nicht** aus den Quellordnern gebaut. Die Quelle liegt read-only unter `reference/fennec/` und dient ausschließlich dem Nachschlagen (Patterns, Kommentare, Tests studieren).

**Sonderfall VA1 (`OclAspectProvider` in `emf.m2x`):**
Da `emf.m2x` binär referenziert wird, muss VA1 **upstream** im m2x-Repo landen – durch den Stack-Owner oder per PR von uns – und wir konsumieren anschließend das Binary mit dem neuen Provider. Das passt zum Hinweis im Anforderungsdokument („Aufwand laut Stack-Owner überschaubar", §7.1). VA1 bleibt damit Voraussetzung für E1/E4, ist aber eine **externe Vorab-Lieferung**, kein Arbeitsschritt im Haupt-Repo. → Im Phase-0-Block tracken (req-doc §9.3).

---

## 4. `reference/code/` – fremde Implementierungen (Read-Only, Studium)

Aus req-doc §10.3. **Zweck ist Studium, nicht Übernahme** – wir lesen Architektur, Lösungsansätze und Test-Ideen, bauen aber eigenständig nach. Empfehlung: shallow klonen (`--depth 1`).

### 4.1 Java – `reference/code/java/`
| Repo | Lizenz | Zweck |
|------|--------|-------|
| `apache/olingo-odata4` | Apache-2.0 | URI-Parser + Batch-Splitter (auch produktiv genutzt) |
| `apache/olingo-odata2` | Apache-2.0 | v2-Architektur (Phase-2-Vorbereitung) |
| `SAP/olingo-jpa-processor-v4` | Apache-2.0 | **Goldstandard E5** – JPA-Mapping, `$expand`-N+1-Vermeidung, Pagination |

> Cache-Pattern (`OclLruExpressionCache`) und das OCL-Modell studierst du in `reference/fennec/emf.m2x/`.

### 4.2 .NET / C# – `reference/code/dotnet/`
| Repo | Lizenz | Zweck |
|------|--------|-------|
| `OData/odata.net` | MIT | Wire-Format-Edge-Cases (v.a. JSON) |
| `OData/WebApi` | MIT | C#-Server; Quervergleich Server-Verhalten |
| `OData/RESTier` | MIT | Backend hinter den TripPin-Reference-Services |
| `OData/ODataSamples` | MIT | **TripPin-Sources + Sample-EDM** (Quelle für `testdata/edm-models/`) |
| `OData/odata-openapi` | MIT | CSDL ↔ OpenAPI |

### 4.3 JavaScript / TypeScript – `reference/code/js/`
| Repo | Lizenz | Zweck |
|------|--------|-------|
| `apache/olingo-odata4-js` (`odatajs`) | Apache-2.0 | Client-Konzept-Inspiration für E8-DSL |
| `OData/MetadataParser` | MIT | Beispiel-CSDL-Parser; Edge-Cases |

### 4.4 Python – `reference/code/python/`
| Repo | Lizenz | Zweck |
|------|--------|-------|
| `SAP/python-pyodata` | Apache-2.0 | **SAP-Quirks + CSRF-Token-Handling**; Tests gegen reale SAP-Backends |
| `tuomur/python-odata` | MIT | Schlanker v4-Client; CSDL-Konsumption |

### 4.5 Go – `reference/code/go/`
| Repo | Lizenz | Zweck |
|------|--------|-------|
| `CloudyKit/go-odata` | MIT | Schlanke v4-Server-Implementierung; alternative Architektur |

---

## 5. `reference/specs/`, `reference/schemas/`, Test-Services

### 5.1 OData-Specs – `reference/specs/odata/`
Aus req-doc §10.1. OASIS bietet meist HTML **und** PDF – nimm die Variante deiner Wahl (PDF offline, HTML `grep`-bar).

| Spec | Quelle | Datei |
|------|--------|-------|
| OData v4.01 Part 1 – Protocol | `docs.oasis-open.org/odata/odata/v4.01/os/part1-protocol/` | `odata-v4.01-part1-protocol.*` |
| OData v4.01 Part 2 – URL Conventions | `…/v4.01/os/part2-url-conventions/` | `odata-v4.01-part2-url.*` |
| OData CSDL XML 4.01 | `docs.oasis-open.org/odata/odata-csdl-xml/v4.01/os/` | `odata-csdl-xml-v4.01.*` |
| OData CSDL JSON 4.01 | `docs.oasis-open.org/odata/odata-csdl-json/v4.01/os/` | `odata-csdl-json-v4.01.*` |
| OData JSON Format 4.01 | `docs.oasis-open.org/odata/odata-json-format/v4.01/os/` | `odata-json-format-v4.01.*` |
| OData ABNF Construction Rules 4.01 | `docs.oasis-open.org/odata/odata/v4.01/os/abnf/` | `odata-abnf-v4.01.txt` |
| OData Vocabularies 4.0 | `docs.oasis-open.org/odata/odata-vocabularies/v4.0/` | `odata-vocabularies-v4.0.*` |
| OData Data Aggregation Ext. 4.0 | `docs.oasis-open.org/odata/odata-data-aggregation-ext/v4.0/cs03/` | `odata-aggregation-ext-v4.0.*` |
| OData v2 Spec | `www.odata.org/documentation/odata-version-2-0/` | `odata-v2.*` |

### 5.2 BSI-TRs – `reference/specs/bsi/`
Aus §8.2 (Annahme, im Review zu bestätigen – Q1). Als PDF von der BSI-Website: `tr-02102-1.pdf`, `tr-02102-2.pdf`, `tr-03116.pdf`.

### 5.3 Plattform & OCL – `reference/specs/ocl/`, `reference/specs/platform/`
OMG OCL v2.4 (`ocl/omg-ocl-2.4.pdf`), OSGi Core R8 (`platform/osgi-core-r8.pdf`), Java 21 JLS (`platform/`, ggf. nur Link – sehr groß).

### 5.4 Schemata – `reference/schemas/`
Autoritative Maschinenschemata, direkter Input für Q15 / CSDL-Round-Trip:
| Inhalt | Quelle | Ziel |
|--------|--------|------|
| CSDL-XML XSDs | `…/odata-csdl-xml/v4.01/os/schemas/` | `reference/schemas/csdl-xml/` |
| CSDL-JSON JSON-Schemas | `…/odata-csdl-json/v4.01/os/schemas/` | `reference/schemas/csdl-json/` |

### 5.5 Live-Test-Services – `testdata/service-endpoints.md`
Online, nicht klonbar – aber dokumentiert, damit die URLs ohne Suche bekannt sind (§10.4):
| Service | Version | Auth | Zweck |
|---------|---------|------|-------|
| `services.odata.org/V4/TripPinServiceRW/` | v4 | keine | **Primär** Client-Akzeptanz (RW) |
| `services.odata.org/V4/Northwind/Northwind.svc/` | v4 | keine | Basics |
| `services.odata.org/V2/Northwind/Northwind.svc/` | v2 | keine | Phase 2 |
| SAP ES5 Demo Gateway | v2 (SAP) | Free Account | Phase-2-v2 (CSRF, `sap:`) – Q18 |
| OASIS Open Data Demo | v4 | keine | Reference |

---

## 6. `testdata/` – Test-Input
Aus §10.5. Statisches Material, das Tests konsumieren.
| Ordner | Inhalt | Quelle |
|--------|--------|--------|
| `testdata/abnf-test-cases/` | OASIS ABNF-Cases → **E4-Parser-Akzeptanztests** | `…/v4.01/os/abnf/` |
| `testdata/metadata-samples/` | `$metadata`: TripPin, Northwind (v2 **und** v4) | Live abziehen → als Datei |
| `testdata/edm-models/` | TripPin-EDM + Sample-Modelle | aus `OData/ODataSamples` |

> Die `$metadata`-Beispiele einmal per `curl` ziehen und als Datei ablegen → Round-Trip-Tests laufen reproduzierbar offline.

---

## 7. Setup-Skript (`setup-references.sh`)

Klont die Quell-Repos und zieht die `$metadata`-Beispiele. Specs/PDFs lädst du manuell. Fennec-Branch ist `snapshot`. **`fennec-odata/` selbst legst du an / klonst du separat** – es ist dein Haupt-Repo, nicht Teil des Referenz-Setups.

```bash
#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"

# ---- Read-Write Quell-Abhängigkeit: nur emf.model.metadata (volle Historie) ----
git clone -b snapshot "https://github.com/eclipse-fennec/emf.model.metadata.git" \
  "$ROOT/fennec-metadata" || echo "  -> fennec-metadata existiert schon, übersprungen"

# ---- Read-Only: Quelle der binär-referenzierten Fennec-Repos (nur Studium) ----
mkdir -p "$ROOT/reference/fennec"
clone_fennec_ref () { # repo  zielname
  git clone --depth 1 -b snapshot "https://github.com/eclipse-fennec/$1.git" \
    "$ROOT/reference/fennec/$2" || echo "  -> $2 existiert schon, übersprungen"
}
clone_fennec_ref emf.osgi             emf.osgi
clone_fennec_ref emf.codec            emf.codec
clone_fennec_ref emf.persistence-jpa  emf.persistence-jpa
clone_fennec_ref emf.m2x              emf.m2x

# ---- Read-Only: fremde Implementierungen (shallow, Studium) ----
clone_ref () { # org/repo  sprachordner  zielname
  git clone --depth 1 "https://github.com/$1.git" \
    "$ROOT/reference/code/$2/$3" || echo "  -> $3 existiert schon, übersprungen"
}
mkdir -p "$ROOT/reference/code"/{java,dotnet,js,python,go}

clone_ref apache/olingo-odata4         java   olingo-odata4
clone_ref apache/olingo-odata2         java   olingo-odata2
clone_ref SAP/olingo-jpa-processor-v4  java   olingo-jpa-processor-v4
clone_ref OData/odata.net              dotnet odata.net
clone_ref OData/WebApi                 dotnet WebApi
clone_ref OData/RESTier                dotnet RESTier
clone_ref OData/ODataSamples           dotnet ODataSamples
clone_ref OData/odata-openapi          dotnet odata-openapi
clone_ref apache/olingo-odata4-js      js     odatajs
clone_ref OData/MetadataParser         js     MetadataParser
clone_ref SAP/python-pyodata           python python-pyodata
clone_ref tuomur/python-odata          python python-odata
clone_ref CloudyKit/go-odata           go     go-odata

# ---- $metadata-Beispiele für Round-Trip-Tests ----
mkdir -p "$ROOT/testdata/metadata-samples"
curl -fsSL "https://services.odata.org/V4/TripPinServiceRW/\$metadata" \
  -o "$ROOT/testdata/metadata-samples/trippin-v4-metadata.xml" || true
curl -fsSL "https://services.odata.org/V4/Northwind/Northwind.svc/\$metadata" \
  -o "$ROOT/testdata/metadata-samples/northwind-v4-metadata.xml" || true
curl -fsSL "https://services.odata.org/V2/Northwind/Northwind.svc/\$metadata" \
  -o "$ROOT/testdata/metadata-samples/northwind-v2-metadata.xml" || true

echo "Fertig. fennec-odata/ separat anlegen. Specs/PDFs manuell in reference/specs + reference/schemas."
```

> Repo-URLs folgen aus den `org/repo`-Namen im Anforderungsdokument. Scheitert ein Klon (Repo verschoben), kurz prüfen und im Skript korrigieren – die Struktur bleibt gleich. Ob `emf.model.metadata` ein eigenes Repo oder Teil eines anderen ist, im Zweifel verifizieren (req-doc §2.3 nennt „Eigenes Repo vorhanden").

---

## 8. `CLAUDE.md` – Kontext für Claude Code

Die gepflegte Fassung liegt im Workspace-Root (`/opt/git/fennec-odata/CLAUDE.md`) und wird
dort aktualisiert — hier nicht mehr dupliziert (die frühere Vorlagen-Kopie veraltete
regelmäßig gegenüber dem Original).

---

## 9. `.gitignore` (Workspace-Root)

```gitignore
# Reproduzierbare Read-Only-Ressourcen (per setup-references.sh wiederherstellbar)
/reference/
/testdata/metadata-samples/

# Eigene Git-Repos – nicht ins Eltern-Repo verschachteln
/fennec-odata/
/fennec-metadata/

# Build-Output
**/target/
**/bin/
**/.bnd/
```

> `fennec-odata/` und `fennec-metadata/` sind eigene Repos und werden ausgeblendet, um verschachtelte Git-Repos zu vermeiden. Falls du lieber Submodule willst, sag Bescheid – dann mache ich daraus eine `.gitmodules`-Variante.

---

## 10. Lizenz-/Studienhinweis

Der Referenz-Code dient dem **Studieren**, nicht der 1:1-Übernahme – das vereinfacht die Lizenzlage deutlich. Apache-2.0/MIT-Code als Vorbild lesen und eigenständig nachbauen ist unkritisch; relevant würde Attribution erst beim wörtlichen Übernehmen von Quell- oder Testcode ins EPL-2.0-Projekt. ABNF-Cases, `$metadata`-Beispiele und die OASIS-XSDs/JSON-Schemas werden als Test-Eingabe bzw. Validierungs-Referenz eingebunden (nicht modifiziert), Herkunft im Test dokumentieren.

---

## 11. Quirks gezielt nachschlagen (req-doc §10.6)
- **Power-BI `OData-Version`-Header (MUST statt SHOULD)** → `reference/code/dotnet/odata.net`.
- **SAP CSRF-Round-Trip** → `reference/code/python/python-pyodata`.
- **SAP doppelte Vocabularies (`Org.OData.*` + `sap:`)** → SAP-`$metadata` (ES5, Q18) als Sample ziehen.
- **`null` vs. fehlend bei PATCH** → JSON-Format-Spec in `reference/specs/odata/`.
