# Eclipse Fennec – OData Implementation
## Basic Requirements Document
 
| Feld         | Wert                                                                       |
|--------------|----------------------------------------------------------------------------|
| Status       | Draft v0.9 – Diskussionsgrundlage                                          |
| Scope        | OData v4.01 Server **und Client**, v2-Berücksichtigung im Design, multi-protokoll-fähige Server-Architektur (perspektivisch OGC SensorThings) |
| Zielprojekt  | `eclipse-fennec/emf.odata` (neu)                                           |
| Standards    | OData V4.01 (OASIS), CSDL 4.01, BSI-TR (s. §8)                             |
| Prinzipien   | SOLID, OSGi-Service-Orientierung, Model-First                              |
 
---
 
## 1. Überblick
 
### 1.1 Was ist OData?
 
OData (Open Data Protocol) ist ein offener Standard, mit dem strukturierte Daten so über HTTP veröffentlicht werden, dass beliebige Werkzeuge sie generisch verstehen können. Vereinfacht gesagt: dieselbe Idee, mit der EMF lokal Modelle, Instanzen und Beziehungen handhabt – gegossen in eine standardisierte, netzwerkfähige Form.
 
#### Wer nutzt OData
 
Microsoft hat OData 2007 entwickelt und nutzt es heute zentral in Power BI, Dynamics 365 und Microsoft Graph (den Office-365-APIs). SAP nutzt es als primäres API-Format ihrer NetWeaver- und S/4HANA-Plattformen – praktisch jede SAP-Fiori-Oberfläche spricht OData. Salesforce, IBM Cloud und viele weitere Enterprise-Plattformen unterstützen es. Seit 2014 ist es OASIS-Standard, seit 2016 ISO/IEC-zertifiziert.
 
#### Wie es konzeptionell funktioniert
 
Ein OData-Service ist im Wesentlichen ein netzwerk-zugängliches **EPackage mit Daten dahinter**. Genau wie ein `.ecore`-File einem Werkzeug sagt "hier sind meine EClasses und EReferences", liefert ein OData-Service ein selbstbeschreibendes Schema-Dokument unter einer festen URL (`.../$metadata`), das genau das in einer standardisierten XML- oder JSON-Form ausdrückt. Werkzeuge wie Power BI laden dieses Dokument und wissen sofort, welche Datenarten und welche Beziehungen der Service anbietet, ohne dass jemand einen Adapter oder ein Plugin schreibt.
 
Auf die Daten wird über URLs nach festen Mustern zugegriffen:
 
- `.../Customers` – alle Instanzen einer EClass
- `.../Customers(42)` – eine bestimmte Instanz, identifiziert über ihren Schlüssel
- `.../Customers(42)/Orders` – einer EReference folgen (das Pendant zu `eGet`)
- `.../Customers(42)/Orders(7)/Items` – verschachtelt, mehrere Schritte
Auf jeder dieser URLs kann der Aufrufer per Query-Parameter weiter einschränken: nur bestimmte Properties zurückgeben (Projektion), nach Bedingungen filtern (etwa `Name eq 'Müller' and Age gt 30`), sortieren, seitenweise abrufen, Beziehungen direkt mit-auflösen statt sie nachzuladen (analog zu eager fetch). Die Filter-Sprache ist eine eigene kleine Ausdruckssprache, die strukturell sehr nah an OCL liegt – mit Pfad-Navigation, Vergleichsoperatoren und Lambda-Operatoren über Sammlungen.
 
Schreiben funktioniert über Standard-HTTP-Methoden (POST anlegen, PATCH ändern, DELETE löschen). Was REST-APIs sonst jeder selbst neu erfindet, ist hier festgelegt: optimistische Concurrency-Kontrolle, Sammeloperationen mehrerer Requests in einem (Batch), atomare Transaktionsgruppen, Methoden-Aufrufe auf Entitäten (analog zu `EOperation`-Invocation), standardisierte Fehlerformate.
 
#### Erweiterbar wie Ecore
 
OData hat ein Annotations-Konzept, das `EAnnotation` aus Ecore strukturell sehr ähnlich ist. Hersteller wie SAP definieren darüber eigene Vokabularien, mit denen Modelle zusätzliche Metadaten tragen können – etwa "dieses Feld ist filterbar", "Anzeigename auf Deutsch", "diese Sammlung ist read-only". Diese Annotationen werden als eigene Schema-Pakete standardisiert; der OData-Standard selbst bleibt unverändert.
 
#### Bidirektionalität
 
Aus dem Schema-Dokument können Werkzeuge typsichere Client-Klassen generieren – analog EMF Codegen, nur aus dem Netzwerk-Schema heraus statt aus einem lokalen `.ecore`. Das funktioniert für C#, Java, JavaScript, Python und weitere Sprachen.
 
#### Warum es uns interessiert
 
Wer einen Service in OData anbietet, wird mit einem riesigen Tooling-Ökosystem automatisch kompatibel: Power BI als Datenquelle, Excel-Tabellen mit Live-Daten, jedes SAP-Frontend, generische REST-Clients. Wer umgekehrt einen Client baut, kann gegen jedes OData-Backend reden, ohne pro Backend einen Adapter zu schreiben. Standardisierung ist hier der Hebel: einmal investiert, beliebig oft eingelöst.
 
### 1.2 Zweck dieses Dokuments
Definition der grundlegenden funktionalen und nicht-funktionalen Anforderungen für eine OData-Implementierung im Eclipse-Fennec-Ökosystem. Dient als Diskussions- und Planungsgrundlage; ersetzt keine detaillierte Architektur- oder Schnittstellenspezifikation.
 
### 1.3 Scope (Phase 1)
- Vollständiger OData-V4.01-**Server** (Read + Write + Batch)
- OData-V4.01-**Client** (Konsumieren externer OData-Services, inkl. SAP/PowerBI-Backends)
- CSDL-Generierung und CSDL-Konsumption über bidirektionalen Codec
- Query-Translation OData → bestehendes OCL-Modell + erweitertes Query-Modell
- OSGi-Service-basierte Composition mehrerer EPackages (Server-Seite)
- Persistenz initial via `emf.persistence-jpa` (EclipseLink)
### 1.4 Out of Scope (Phase 1)
- OData v2-Implementierung (nur Design-Berücksichtigung, s. §3.2)
- OData v3 (dauerhaft out of scope)
- Authentifizierung und Autorisierung (über Whiteboard-Filter, separat)
- Geo-Funktionen (`geo.distance` etc.)
- Vocabulary-Authoring-Tooling
- Code-Generierung typed Proxies aus konsumiertem `$metadata` (Phase 2 möglich)
### 1.5 Begriffe
| Term     | Bedeutung                                                                                       |
|----------|-------------------------------------------------------------------------------------------------|
| CSDL     | Common Schema Definition Language – das Schema-Dokument-Format aus §1.1, in OData-Diktion       |
| EDM      | Entity Data Model (OData-Metamodell); hier auch das Fennec-Ecore unter `http://docs.oasis-open.org/odata/ns/edm` |
| EDMX     | Entity Data Model XML Wrapper; hier auch das Fennec-Ecore unter `http://docs.oasis-open.org/odata/ns/edmx` |
| Codec    | Fennec-Serialisierungs-/Deserialisierungs-Framework (`eclipse-fennec/emf.codec`)                |
| Container| OData-Service-Container, Aggregation mehrerer Schemas                                           |
| OCL      | Object Constraint Language; hier das Fennec-OCL unter `http://www.eclipse.org/fennec/m2x/ocl/1.0` |
| MBR/MBW  | Jakarta REST MessageBodyReader / MessageBodyWriter                                              |
| TCK      | Technology Compatibility Kit; hier nicht offiziell verfügbar, eigene Konstruktion (s. §10.2)    |
 
---
 
## 2. Technologische Basis
 
### 2.1 Pflicht-Stack
- **Java 21** (Sprach-Level und Runtime; Records, Pattern Matching, Sealed Types nutzen)
- **OSGi R8** (Mindestens; R9-Features wie Component-Property-Types willkommen)
- **EMF** (Core, Common, Edit nach Bedarf; nicht UI)
- **OSGi HTTP Whiteboard** (Jakarta Servlet) als Transport: ein `ODataServlet` unter `/odata/*` plus eine Servlet-`Filter`-Chain. Begründung: Der OData-Eintrittspunkt ist eine Catch-All-Ressource, die URL, Format und Query-Optionen ohnehin selbst parst (Olingo-URI-Parser + eigener AST); JAX-RS-Routing, Path-Param-Binding und Content-Negotiation laufen dabei leer. Der Servlet-Stack ist leichtgewichtiger, gibt direkte Stream-Kontrolle (wichtig für `$batch`/`multipart` und große Payloads) und ruft den **transport-neutralen Kern von `emf.codec`** direkt auf, statt über die JAX-RS-MBR/MBW-Schicht. Bewusst akzeptierter Trade-off: Abweichung vom Jakarta-REST-Programmiermodell der übrigen Fennec-Services sowie Eigenbau von ResourceSet-Lifecycle + Fehler→OData-Error-Mapping (vormals `CodecResourceSetFeature`/`ExceptionMapper`). Das Filter+Context-Pattern bleibt multi-protokoll-fähig (perspektivisch OGC SensorThings parallel zu OData). Herleitung + verworfene Alternative: `docs/decisions/0001-transport-servlet-vs-jakarta-rest.md`
### 2.2 Fennec-Komponenten (Wiederverwendung verpflichtend, sofern sinnvoll)
| Komponente              | Verwendung                                                              |
|-------------------------|-------------------------------------------------------------------------|
| `emf.osgi`              | EPackage-Service-Registry, Resource-Set-Factory                         |
| **`emf.model.metadata`**| **Architektonisches Fundament** für alle modell-getriebenen Aspekte (s. §2.3) |
| `emf.codec` (s. unten)  | Daten-Serialisierung (JSON/XML), CSDL-Codec, Daten-OData-JSON-Codec      |
| **EDM-Ecore**           | Validierungs-Referenz für CSDL-Read-Pfad; aus OASIS-XSD generiert       |
| **EDMX-Ecore**          | CSDL-Wrapper-Modell                                                     |
| **OCL-Ecore (m2x)**     | AST für `$filter`/`$orderby`-Predicates; spec-nah, ohne Pivot-Overhead  |
| `emf.persistence-jpa`   | Default-Persistence-Backend (EclipseLink)                               |
| Bestehendes Query-Modell| Backend-Query-Repräsentation; wird erweitert (s. §3.5)                  |
| `emf.m2x` (OCL/QVT)     | OCL-Parser + Type-Checker; QVT für Modell-Generatoren                   |
| `emf.codec` (Kern, transport-neutral) | HTTP-Marshalling (EMF ↔ JSON/XML), direkt aus dem Servlet aufgerufen |
 
#### 2.2.1 emf.codec – Architektur-relevante Eigenschaften
Der Codec ist ein vollständig schichten-getrenntes Format-Abstraction-Framework mit folgendem Aufbau (von oben nach unten): Application Layer (EMF Resource) → Codec Logic Layer (`SerializationEntry`/`DeserializationEntry` pro Concern) → Value Transformation Layer (`CodecValueReader`/`CodecValueWriter`) → Stream Abstraction Layer (`CodecStreamReader`/`CodecStreamWriter` mit eigenem Token-Modell) → Format Adapter (JSON/BSON/CSV/...).
 
Konfiguration läuft zweidimensional: **Source-Hierarchie** (Load/Save-Options → ResourceFactory → Module → Properties → EAnnotations → Defaults) × **Scope-Chain** (Feature → Class → Global). `EffectiveCodecConfig` ist der immutable Snapshot pro Operation.
 
Bestehende Format- und Meta-Format-Erweiterungen, die als **Vorbilder** für unsere OData-Codecs dienen:
- `org.eclipse.fennec.codec.bson` – BSON für MongoDB (relevant für spätere Mongo-Persistenz)
- `org.eclipse.fennec.codec.cbor`, `.yaml`, `.geojson` – weitere Format-Adapter
- `org.eclipse.fennec.codec.jsonschema` – **Meta-Format** EPackage ↔ JSON Schema, bidirektional
- `org.eclipse.fennec.codec.openapi` – OpenAPI mit eingebetteter Schema-Konvertierung via `EPackageValueReader`/`EPackageValueWriter`
Der CSDL-Codec (siehe §3.4) wird konzeptionell ein direkter Verwandter von `jsonschema`/`openapi`: ein Meta-Format-Codec für EPackage ↔ CSDL.
 
### 2.3 Model Metadata Service – architektonisches Fundament
 
Der Fennec-Stack stellt mit `emf.model.metadata` ein generisches **Aspect-/Profile-Framework** bereit, an dem alle modell-getriebenen technischen Aspekte hängen können (Codec, OData, OCL, ORM, Historization, Units). Wir nutzen das verbindlich – es löst gleich mehrere Architekturfragen, die wir sonst pro Concern einzeln lösen müssten.
 
**Funktionsprinzip:**
Beim Registrieren eines EPackages baut der Service ein paralleles Shadow-Modell aus `PackageMetadata` / `ClassMetadata` / `FeatureMetadata`. Jeder registrierte `AspectProvider` (codec, odata, ocl, orm, …) attacht pro Element einen Concern-spezifischen Aspect (mit Annotations-Layer-Werten) und baut anschließend ein vollständig aufgelöstes `Profile` (mit Defaults + Cross-Reference-Resolution). Zur Laufzeit liefert der Service O(1)-Lookups; Profiles können als XMI persistiert und beim Startup geladen werden.
 
**Drei Eigenschaften, die für unser OData-Vorhaben strukturell wichtig sind:**
 
1. **Provider-Isolation:** Der `CodecAspectProvider` sieht den `ODataAspectProvider` nicht. Damit können Codec-Annotationen (`@Codec.serialize`) und OData-Annotationen (`@OData.Key`) parallel und unabhängig am selben EClass leben.
2. **3-Layer-Cascade:** External Config (DSL-Datei) → EAnnotations → Defaults. Damit lassen sich Drittanbieter-Modelle (SAP-Vocabularies, OASIS-Vocabularies) anreichern, ohne deren Ecore zu modifizieren.
3. **Lifecycle-Callbacks:** `MetadataHandler.onPackageRegistered/Unregistered` ermöglicht reaktive Cache-Verwaltung. Hot-Add/Remove von EPackages invalidiert automatisch alle abhängigen Caches.
**Konsequenz für unsere Bundles:**
- `emf.odata.metadata` (neu): definiert `odata.ecore` mit `ClassODataAspect`/`FeatureODataAspect`/`ReferenceODataAspect` plus `ODataAspectProvider` – analog `org.eclipse.fennec.codec.metadata`
- Der CSDL-Codec (§3.4) konsumiert das fertige `ODataClassProfile` und serialisiert direkt ohne Zwischenmodell
- Der OCL-Cache (§3.5/§3.6) ist eine Aspekt-Erweiterung an `emf.m2x` (OclAspectProvider) und kein eigenes Konstrukt mehr
**Status der Cross-Repo-Vorarbeiten:**
| Komponente            | Status                                  |
|-----------------------|-----------------------------------------|
| `emf.model.metadata`  | Eigenes Repo vorhanden, im Codec verdrahtet, im Stack als Service-Schicht etabliert |
| `CodecAspectProvider` | Vorhanden, produktiv genutzt            |
| `OclAspectProvider`   | **Nicht vorhanden** – Vorarbeit für unser E1 (siehe §7.1 VA1) |
| `OrmAspectProvider`   | Im Architektur-Doc als Future markiert; nicht zwingend für Phase 1 (siehe Q21) |
 
Referenz-Architektur-Dokumente:
- [Model Metadata Architecture](https://github.com/eclipse-fennec/emf.codec/blob/snapshot/org.eclipse.fennec.model.metadata/model-metadata-architecture.md)
- [Codec Metadata Architecture](https://github.com/eclipse-fennec/emf.codec/blob/snapshot/org.eclipse.fennec.codec.metadata/codec-metadata-architecture.md)
### 2.4 Externe Bibliotheken (Evaluation erforderlich)
| Bibliothek         | Zweck                                          | Status                |
|--------------------|------------------------------------------------|-----------------------|
| Apache Olingo V4   | **Nur** URI-Parser und Batch-Splitter          | Empfohlen, eng eingegrenzt |
| ANTLR4             | Eigene Grammatiken für OData-Query-Syntax      | Vorhanden, nutzen     |
| CongoCC            | Alternative zu ANTLR4                          | Nur falls Performance-Engpass nachgewiesen |
| Olingo V2          | Phase 2 vorgemerkt                             | Nicht für Phase 1     |
| HTTP-Client        | Für Client-Komponente                          | OSGi-HTTP-Client-Service oder JDK 21 `HttpClient`; im Detail zu evaluieren |
 
**Olingo V4 Scope-Beschränkung:** Olingos `Edm`-Datenmodell, Metadata-Generator und Serializer werden *nicht* verwendet, weil mit dem EDM-Ecore und dem Codec-Framework bereits eine vollständigere und besser integrierte Lösung vorhanden ist. Verwendet werden ausschließlich:
- `UriParser` (Path-Segmente, Query-Optionen ohne `$filter`)
- `BatchParser` / `BatchSerializer` (Multipart-Framing)
- ggf. primitive Edm-Typ-Literal-Konversionen
`$filter`/`$orderby`/`$apply`/`$compute` werden durch eigene ANTLR4-Grammatik direkt in OCL-AST + Aggregations-AST geparst.
 
**Evaluationskriterien für externe Libs:**
1. Lizenzkompatibilität mit EPL-2.0
2. Wartungsstatus (letzte Releases, offene CVEs)
3. OSGi-Tauglichkeit (echte Bundles oder bndtools-wrappable)
4. Java-21-Kompatibilität
5. Transitive-Dependency-Hygiene
6. BSI-TR-Konformität bei Krypto-relevanten Bibliotheken
---
 
## 3. Funktionale Anforderungen
 
### 3.1 OData V4 – Coverage Phase 1 (Server)
- **Service Document** (`/`)
- **Metadata Document** (`/$metadata`) in CSDL-XML und CSDL-JSON
- **Read-Operationen**
  - EntityCollection, Single Entity, Property, `$value`
  - Navigation Properties (folgen, expandieren)
  - Containment Navigation (1:1 EMF-Containment-Mapping)
- **Query-Optionen**
  - `$filter` (Phase 1: vollständig außer Geo)
  - `$select`, `$expand` (inkl. verschachteltem `$filter`/`$top`/`$orderby`)
  - `$orderby`, `$top`, `$skip`, `$count`
  - `$search` (Backend-abhängig)
  - `$apply` (Aggregations) – Phase 2 akzeptabel; Modell-Slot in Phase 1 definieren
- **Schreib-Operationen**
  - POST (Create), PATCH (Update partial), PUT (Replace), DELETE
  - Deep Insert
- **Concurrency**
  - ETag-Generierung und `If-Match`/`If-None-Match`
- **Batch-Requests**
  - `multipart/mixed`
  - JSON-Batch
  - Atomic Change Sets
- **Functions / Actions**
  - Bound und Unbound
  - Mapping aus `EOperation` via Annotations
- **Server-Driven Paging**
  - `@odata.nextLink` mit Skip-Token
### 3.2 v2-Kompatibilität (Design-Constraint, kein Code)
Folgende Architekturentscheidungen müssen bereits in Phase 1 v2-tauglich sein:
 
1. **Type-Mapping** ist profil-parametrisiert – `EDataType → EdmType` ist kein hartcodiertes Mapping, sondern eine austauschbare Strategy.
2. **EAnnotation-Vocabulary** für OData-Mappings ist versionsneutral (`@OData.Key`, nicht `@ODataV4.Key`).
3. **CSDL-Codec-Architektur** trennt Modell-AST von Wire-Format. v4-XML, v4-JSON heute; EDMX-1.0-XML als zweiter Codec später möglich (wird ein zweites Ecore brauchen).
4. **JSON-Wrapping-Logik** (v2 `d`-Wrapper, `__metadata`-Block) ist als Interceptor/Filter implementierbar, nicht in den Core verdrahtet.
5. **Containment-Semantik** ist im Modell annotiert, nicht im Code unterschieden – Wire-Mapping pro Version unterschiedlich.
### 3.3 EPackage-Composition (Server)
- EPackages werden als OSGi-Services entdeckt (`emf.osgi`-Pattern).
- Service-Properties steuern Zuordnung zu OData-Containern und Namespaces.
- Mehrere Container parallel auf demselben Endpoint möglich (Multi-Tenancy-fähig).
- Cross-Schema-Referenzen werden über `TReference`/`TInclude` im EDMX-Modell generiert.
- Hot-Add/Remove von EPackages: Service-Tracker-basiert; CSDL wird invalidiert und neu generiert.
### 3.4 CSDL-Codec (Meta-Format, bidirektional)
 
**Architekturentscheidung:** CSDL-Generierung und CSDL-Konsumption werden als Codec-Erweiterung umgesetzt – konzeptionell direkter Verwandter von `org.eclipse.fennec.codec.jsonschema` und `.openapi`, baut aber auf den Metadata-Service (§2.3) auf. Der Codec ist bidirektional: er erzeugt CSDL-XML/JSON aus EPackage und liest externes `$metadata` zurück in EPackage-Strukturen.
 
**Realisierungs-Variante (revidiert v0.9, ADR 0002):** CSDL wird über eine **Instanz des OASIS-EDM/EDMX-EMF-Modells** (`org.odata.csdl.model`, aus der `fennecEMFModels`-Library) erzeugt und gelesen – **nicht** über einen handgeschriebenen Konverter (die frühere Variante β). Das Modell ist aus den OASIS-XSDs generiert und trägt das XML-Mapping als `ExtendedMetaData`; EMF serialisiert eine EDM-Instanz damit direkt zu spec-konformem CSDL-XML und liest CSDL-XML symmetrisch zurück. Beide Richtungen sind durch Spikes im Bundle `org.eclipse.fennec.odata.csdl` belegt. Begründung mit Metadata-Service:
 
- **Write-Pfad:** Der `ODataAspectProvider` (in `emf.odata.metadata`) hat beim `registerPackage()` alle Konfigurationen im `ODataClassProfile` aufgelöst. Der CSDL-Generator mappt `EPackage` + Profile auf eine EDM-Modell-Instanz (`TEntityType`/`TProperty`/`TEntityContainer`/…) und lässt EMF sie serialisieren – CSDL-XML out-of-the-box, CSDL-JSON über ein JSON-Codec-Profil auf demselben Modell.
- **Read-Pfad:** EMF lädt externes `$metadata` in eine EDM-Modell-Instanz; ein Mapper baut daraus ein EPackage plus die zugehörigen ODataAspects (über die 3-Layer-Cascade des Metadata-Services: External Config könnte hier sogar SAP-spezifische Anreicherung leisten). Da das Modell XSD-generiert ist, ist die Struktur-Validierung implizit; die XSD-Round-Trip-Prüfung (Q15) bleibt als Test.
**Eigenschaften:**
- Output-Formate: CSDL-4.01-XML (gratis aus dem EDM-Modell) und CSDL-4.01-JSON (eigenes JSON-Codec-Profil auf demselben Modell).
- Bidirektional, mit Round-Trip-Test als Akzeptanz (Ziel ≥ 95 % Fidelity nach Vorbild `jsonschema`-Codec).
- **EDM/EDMX-Modell-Rolle**: `org.odata.csdl.model` ist jetzt das **produktive Build-/Parse-Modell** für CSDL (nicht mehr nur Validierungs-Referenz). Dieselbe Artefakt-Quelle dient weiter der XSD-Round-Trip-Prüfung (Q15).
- Profile-Optionen (v4.0 / v4.01, mit/ohne SAP-Annotations) als Codec-Konfiguration über bestehenden Source-Hierarchie-Mechanismus.
- Format-Selection via Content-Type, File-Extension oder explizite Option (analog bestehender `CODEC_FORMAT`-Pattern).
**Vocabulary-Pakete:** OASIS-Standard-Vocabularies (`Org.OData.Core.V1`, `Org.OData.Capabilities.V1`, `Org.OData.Validation.V1`, `Org.OData.Measures.V1`) werden als eigene EPackages bereitgestellt – generiert aus ihren CSDL-Definitionen via Read-Pfad des CSDL-Codecs (Bootstrapping). SAP-Annotations (`Org.OData.SAP.*` und Legacy-Formen `sap:label`/`sap:filterable`/`sap:sortable`) folgen demselben Schema.
 
**EAnnotation-Vocabulary** (Fennec-eigen, formal in `odata.ecore` plus `ODataAnnotationConstants` analog `CodecAnnotationConstants`):
- `@OData.Key`
- `@OData.NavigationProperty.ContainsTarget` (für nicht-EMF-Containment-Fälle)
- `@OData.Function.IsBound`, `@OData.Function.EntitySetPath`
- `@OData.Action.IsBound`
- `@OData.Property.Computed`, `@OData.Property.Immutable`
- `@OData.Type` (Override für Edm-Type-Mapping)
Alle Annotationen werden vom `ODataAspectProvider` beim `registerPackage()` zu `ClassODataAspect`/`FeatureODataAspect`/`ReferenceODataAspect` aufgelöst und in `ODataClassProfile` mit Defaults konsolidiert. Spec-Detail folgt in einem separaten Sub-Doc (Q13).
 
### 3.5 Query-Modell und Predicate-IR
 
**Architekturentscheidung:** OCL-Modell aus `m2x` wird als Predicate-IR übernommen. Bestehendes Query-Modell wird erweitert; die spezifische Comparator-/Chaining-Hierarchie wird abgelöst.
 
**Mapping OData → OCL** (Auszug; vollständige Tabelle im Design-Doc):
 
| OData-Konstrukt                          | OCL-Konstrukt                                              |
|------------------------------------------|-------------------------------------------------------------|
| `Property eq Value`                      | `OperationCallExp(name='=')` über `PropertyCallExp`         |
| `not / and / or`                         | `OperationCallExp` mit jeweiligem Op-Namen                  |
| `tolower/toupper/contains/startswith/...`| `OperationCallExp` mit Stdlib-Op-Namen                      |
| `year/month/day/.../round/floor/ceiling` | `OperationCallExp` mit Stdlib-Op-Namen                      |
| `any(x: ...)` / `all(x: ...)`            | `IteratorExp(name='exists')` / `IteratorExp(name='forAll')` |
| `cast(x, T)` / `isof(x, T)`              | `OperationCallExp(name='oclAsType' / 'oclIsKindOf')` + `TypeExp` |
| `Items/$count`                           | `OperationCallExp(name='size')`                             |
| `in(...)` (v4.01)                        | `OperationCallExp(name='includes')` über Set-Literal        |
| `has` (Flag-Enum)                        | Custom Op-Name `'has'` (kein OCL-Pendant)                   |
 
OData-Operatoren ohne OCL-Pendant werden als benannte Operationen im Stil von `OperationCallExp.name = '<custom>'` modelliert; der Backend-Translator hat eine Dispatch-Tabelle, die solche Namen explizit kennt.
 
**Refactoring des Query-Modells – stufenweise:**
 
| Stufe | Änderung                                                                                                       | Status      |
|-------|----------------------------------------------------------------------------------------------------------------|-------------|
| 1     | `QWhere`-Hierarchie ersetzen durch `predicate: OclExpression` (boolean-typed)                                  | Phase 1, verbindlich |
| 2     | `QSubject.featurePath` durch `OclExpression`; `SortEntity.sortFeature` durch `OclExpression`                   | Phase 1 angestrebt, spätestens Phase 2 |
| 3     | `Query` insgesamt OCL-zentriert; weitere Felder als OCL-Expressions                                            | Hängt an `$apply`-Klärung; nicht vor Phase 2 |
 
**`$apply` (Aggregations-Pipeline):**
Pipeline-Semantik wird *nicht* in OCL ausgedrückt. Eigenes Aggregations-Submodell:
```
ApplyTransformation (abstract)
  ├── GroupBy        (groupingProperties: OclExpression*, then: ApplyTransformation)
  ├── Aggregate      (aggregations: AggregateExpression*)
  ├── Filter         (predicate: OclExpression)
  ├── Compute        (computeProperties: ComputeExpression*)
  ├── BottomTop      (kind, count, value: OclExpression)
  └── Concat         (transformations: ApplyTransformation*)
```
`Filter` und `Compute` enthalten intern `OclExpression`. Damit ist die saubere Trennung gewahrt: OCL für Ausdrücke, Pipeline-Klassen für Aggregations-Stufen.
 
**Verbleibende Lücken im Query-Ecore (Erweiterung in Stufe 1 erforderlich):**
- `Chaining`-Operanden (heute strukturell unklar) – auflösen, da OCL-Predicate die Chaining ohnehin übernimmt
- Operatoren `ne`, `in`, `has` werden über OCL bereits abgedeckt
- Lambda-Operatoren über OCL `IteratorExp` abgedeckt
- Reichere String-/Date-/Math-Funktionen über OCL `OperationCallExp.name` abgedeckt
- Type-Operatoren (`cast`, `isof`) über OCL abgedeckt
**Type-Resolution:**
Das `OclExpression.type`-Feld wird vom OCL-Type-Checker gesetzt. Pro EPackage hängt der OCL-Engine-Zustand am Metadata-Service: ein `OclAspectProvider` (siehe §3.6.1) registriert sich beim `MetadataWhiteboard` und baut beim `registerPackage()` die Type-Resolver-Strukturen für genau dieses Modell auf. Damit ist Type-Resolution zur Laufzeit ein O(1)-Lookup gegen das vorberechnete Profile, keine pro-Request-Neuberechnung mehr.
 
**Backend-Translation:**
- `QueryService`-OSGi-Interface mit Implementierungen pro Backend.
- Initial: JPA-Implementierung (auf `emf.persistence-jpa`).
- Vorgesehen: MongoDB-Implementierung.
- In-Memory-Implementierung für Tests verpflichtend.
- Translator ist OCL-AST-Visitor; Backends übersetzen den OCL-AST in ihre native Query-Sprache (JPA-Criteria, Mongo-BSON-Pipeline).
### 3.6 Parser
- Eigene ANTLR4-Grammatik für `$filter`, `$orderby`, `$apply`, `$compute`.
- AST-Output:
  - `$filter` und `$orderby` → **OCL-AST** (`OclExpression`)
  - `$apply` → **Aggregations-AST** mit eingebetteten OCL-Expressions
  - `$compute` → Liste von OCL-Expressions mit Alias-Namen
- Versions-Tolerant designen (v2-Differenzen wie `/Date(...)/`-Literale später als Mode/Variante).
- Olingos URI-Parser nicht für `$filter`/`$orderby`/`$apply` benutzen (eigener AST).
- Test-Material: OASIS ABNF Test Cases (s. §10.5) als Grammar-Akzeptanz-Test verbindlich.
- Aufruf erfolgt im Servlet-Filter (`ODataRequestFilter`, s. §5.1.1), nicht erst im Dispatcher – damit sind Type-Resolution und Limits-Validierung vor Backend-Aufruf abgeschlossen.
#### 3.6.1 Expression-Caching am Metadata-Service
 
Statt eines isolierten OData-eigenen LRU-Caches wird Caching am Metadata-Service-Lifecycle verankert (siehe §2.3). Drei Caching-Stufen, jeweils Concern-spezifisch, alle am `MetadataHandler.onPackageRegistered`-Callback aufgehängt:
 
| Cache-Ebene | Lebenszyklus | Verantwortlicher AspectProvider |
|---|---|---|
| **Type-Resolution-Cache** (EClass-Properties, Function-Signaturen) | pro EPackage, beim `registerPackage()` aufgebaut | `OclAspectProvider` (Vorarbeit, siehe §7.1 VA1) |
| **Parsed-Expression-Cache** (geparste OCL-Expressions aus EAnnotations) | pro EPackage, am Profile gehalten | `OclAspectProvider` |
| **Ad-hoc-Query-Cache** (geparste `$filter`/`$orderby`-Strings aus Requests) | pro EPackage, LRU mit Default 1024 Einträgen, am Profile attached | `ODataAspectProvider` |
 
**Vorteile gegenüber einem separaten globalen Cache:**
- **Hot-Add/Remove sicher:** `onPackageUnregistered` invalidiert automatisch alle Caches eines EPackages.
- **Type-sicher:** Cache-Key braucht keinen NsURI-Prefix als String, sondern hängt direkt am `PackageMetadata`-Knoten.
- **Konsistent über Concerns:** Codec, OCL, ORM nutzen denselben Lifecycle-Mechanismus.
- **Persistierbar:** Profiles sind XMI-serialisierbar, was Startup-Zeiten weiter reduzieren kann.
**Implementierungspfad in `emf.odata.metadata`:**
- `ODataAspectProvider` definiert einen Aspect-Slot `parsedQueryCache` an `ClassODataAspect`
- Der `ODataRequestFilter` (§5.1.1) holt die `MetadataService`-Referenz, ermittelt das `ClassODataAspect` für den resolved EClass, befragt dessen Cache vor dem ANTLR4-Parsing
- Cache-Implementation analog `OclLruExpressionCache` aus `emf.m2x` (LinkedHashMap-basiert, synchronized, Hit/Miss-Statistics)
**Q20 (Cache-Key-Kanonisierung)** bleibt offen, ändert sich strukturell nicht: Phase 1 raw-String, Phase 2 kanonisiert wenn Hit-Rate-Statistik es nahelegt.
 
### 3.7 OData-JSON Daten-Codec (separat vom CSDL-Codec)
 
**Abgrenzung:** Der CSDL-Codec aus §3.4 transportiert **Schemata** (EPackages). Der hier beschriebene Daten-Codec transportiert **Instanzen** (EObjects) im OData-JSON-Format und ist analog zu `org.eclipse.fennec.codec.geojson` ein Format-Profil.
 
**Konfiguration als Codec-Profil:**
- `typeKey = "@odata.type"`, `TypeStrategy.SCHEMA_AND_TYPE` für Schema-qualifizierte Type-Namen
- `idKey = "@odata.id"`, `IdStrategy.COMBINED` mit `IdKeyMode.BOTH` für composite Keys (mehrere Properties + Aggregat-Key)
- Reference-Strategy: `expand`-Option für `$expand`, ansonsten `@odata.id`-Form
- Metadata-Field-Ordering: `@odata.context` zuerst (Order-Independent-Parsing macht das robust für Konsumenten)
- Custom Value Readers/Writers für OData-Spezialtypen:
  - `Edm.Date` (ISO 8601 Date ohne Zeit)
  - `Edm.TimeOfDay` (ISO 8601 Time)
  - `Edm.DateTimeOffset` (ISO 8601 mit Offset)
  - `Edm.Duration` (ISO 8601 Duration)
  - `Edm.Guid` (Guid-Format)
  - `Edm.Decimal` (mit Precision/Scale)
  - `Edm.Binary` (Base64)
- Discriminator-Mapping für Type-Inheritance (Polymorphism in EntityCollections)
- Profil-Varianten für `metadata=minimal/full/none` (Content-Type-Parameter)
- **Pflicht-Header:** `OData-Version: 4.0` (siehe §10.6 Power-BI-Quirk)
**v2-Vorbereitung:** Der `d`-Wrapper und `__metadata`-Block werden in einem späteren v2-Profil als Codec-Interceptor implementiert, nicht im Core des Daten-Codecs.
 
### 3.8 Client-Komponente
 
**Scope:** Vollwertiger OData-V4.01-Client zum Konsumieren externer Services (insb. SAP-Backends, Power-BI-Datenquellen, beliebige OData-V4-Server). Wiederverwendung der Codec-Bausteine aus §3.4 und §3.7 maximal.
 
**Bausteine:**
 
1. **HTTP-Layer**
   - Synchron und asynchron (CompletableFuture-basiert)
   - OSGi-HTTP-Client-Service als Default-Provider, JDK-21-`HttpClient` als Fallback (Q16a)
   - Connection-Pooling, Timeouts, Retry mit Exponential Backoff
   - Auth-Hooks (Header-basiert), aber keine Auth-Implementierung in Phase 1
   - **CSRF-Token-Handling** für SAP-Backends (`X-CSRF-Token` Fetch + Submit)
2. **Service-Document- und `$metadata`-Discovery**
   - `$metadata`-Konsumption via CSDL-Codec (Read-Pfad aus §3.4) → EPackage als Ergebnis
   - In-Memory-Cache mit konfigurierbarer TTL und expliziter Invalidierung
   - Vocabulary-Auflösung (Cross-Schema-`Reference`-Includes)
3. **Request-Builder-DSL**
   - Fluent API: `client.entitySet(Customers).filter(...).select(...).expand(...).top(...).execute()`
   - **Wiederverwendung des Query-AST aus §3.5/3.6:** intern wird derselbe OCL-AST aufgebaut wie auf der Server-Seite, dann *in die andere Richtung* serialisiert – AST → `$filter`-String. Das ist die Spiegelseite des Server-Parsers.
   - Type-Safe gegen das aus `$metadata` abgeleitete EPackage; bei dynamischen Modellen Fallback auf String-basiert
4. **Response-Verarbeitung**
   - Daten-Codec (§3.7) im Read-Modus
   - `@odata.nextLink`-Auflösung für Paging
   - Streaming für große Collections
5. **Error-Format-Handling**
   - Spec-konformes Parsing der OData-Error-JSON-Struktur (`error.code`, `error.message`, `error.target`, `error.details`)
   - Mapping auf typisierte Exception-Hierarchie
6. **Schreib-Operationen**
   - POST/PATCH/PUT/DELETE mit ETag-Behandlung
   - Deep Insert
7. **Batch-Client**
   - Request-Bündelung in `multipart/mixed` und JSON-Batch
   - Atomic Change Sets als API-Konzept
8. **Optional Phase 2: Code-Generierung typed Proxies** aus konsumiertem `$metadata` (analog SAP Cloud SDK / Microsoft OData-Client)
**Aufwandsschätzung:** ca. 25–35 % des Server-Aufwands, weil Codecs, Vocabulary-Handling und Query-AST aus dem Server-Stream wiederverwendet werden.
 
---
 
## 4. Nicht-funktionale Anforderungen
 
### 4.1 Performance
- **Backend-Pushdown verpflichtend (Server):** `$filter`, `$orderby`, `$top`, `$skip`, `$count` müssen in Backend-Queries übersetzt werden, kein In-Memory-Filtering.
- **`$expand` ohne N+1 (Server):** Single-Query-Plan oder dokumentiert begründete Ausnahme.
- **Streaming** für große Collections (Response-Streaming Server-seitig, parsende Streams Client-seitig).
- **Lazy Materialization:** EObjects werden erst beim Serialisieren materialisiert.
- **Query-Parsing-Cache verpflichtend:** Caching am `ODataAspectProvider`-Profile (s. §3.6.1) eliminiert Parse- und Type-Check-Kosten bei wiederholten Queries. Erwartete Wirkung in der Größenordnung des `OclLruExpressionCache`-Vorbilds (Sub-Millisekunden-Latenz bei Cache-Hit).
- **Parser-Performance** (`$filter`) ist nur beim Cache-Miss relevant – ANTLR4-Parsing ist ausreichend schnell für initiale Anfragen.
- **Client-`$metadata`-Cache** vermeidet Redownload pro Request.
### 4.2 Skalierbarkeit
- Server-Endpoints sind stateless (Request-Scope-State erlaubt, kein Session-State).
- Server-Driven-Paging mit opaken Skip-Tokens.
- Resource-Limits (s. §4.5) sind harte Schutzschranken, keine SLOs.
- Client unterstützt Connection-Pooling und Concurrent Requests.
### 4.3 Erweiterbarkeit – SOLID-Anwendung
| Prinzip | Anwendung im OData-Stack                                                       |
|---------|--------------------------------------------------------------------------------|
| SRP     | Trennung Protocol / Query / Persistence / Codec; je ein Bundle pro Concern    |
| OCP     | Neue Backends, neue Codec-Formate, neue Vocabularies ohne Core-Änderung       |
| LSP     | Persistence-Implementierungen substituierbar gegen `QueryService`-Kontrakt    |
| ISP     | Kleine, fokussierte Service-Interfaces (`MetadataProvider`, `EntityReader`, `EntityWriter`, `QueryService`, `BatchProcessor`, `ODataClient`, `RequestBuilder`) |
| DIP     | Alle Abhängigkeiten gegen OSGi-Services, keine konkreten Klassenimporte über Bundle-Grenzen |
 
### 4.4 Beobachtbarkeit
- OSGi-LogService-Integration (alle Bundles).
- Strukturiertes Logging mit Korrelations-IDs pro Request (auch durch Batch-Subrequests durchgereicht).
- Metriken-Hooks: zählbar/messbar als OSGi-Service-Schnittstelle, Implementierungen (Micrometer, OpenTelemetry) optional.
- Audit-Log-Schnittstelle für Schreiboperationen (separat von Application-Log; siehe §4.5).
- Client-seitig: Request/Response-Tracing-Hooks für Debugging.
### 4.5 Sicherheit (Auth out-of-scope, übrige Aspekte in-scope)
- **Transport:** TLS-Konfiguration über HTTP-Whiteboard / Reverse-Proxy (Server) bzw. HTTP-Client-Konfiguration (Client); gemäß BSI TR-02102-2 (TBD bestätigen).
- **Crypto-Primitive:** Falls eingesetzt (z. B. ETag-Hashing, Skip-Token-Signing), gemäß BSI TR-02102-1 / TR-03116.
- **Input-Validation (Server):**
  - `$filter`-AST wird nicht als String an Backend gereicht – Injection durch Design ausgeschlossen.
  - Maximale Tiefe von `$expand`, `$filter`-Verschachtelung, OCL-AST-Knotenanzahl: konfigurierbar, Default zu definieren.
  - String-Längen-Limits in Path-Segments, Query-Parameter-Werten.
- **Resource-Limits Server (DoS-Schutz):**
  - `$top` Maximum (Default: 1000)
  - `$expand` Tiefe Maximum (Default: 5)
  - Batch-Subrequests Maximum (Default: 100)
  - Request-Body Maximum (Default: 10 MB, konfigurierbar)
  - `$filter`-AST Knotenanzahl Maximum (Default: 200)
  - OCL-Iterator-Verschachtelungstiefe (Default: 3)
- **Validierungs-Pipeline:** Limits werden im `LimitsFilter` der Servlet-Filter-Chain (s. §5.1.1) zwischen Parse und Dispatch geprüft. Verletzungen führen zu HTTP 400 vor jedem Backend-Aufruf.
- **Audit-Logging:** Schreiboperationen mit Subjekt-ID (aus Auth-Filter-Kontext), Resource, Operation, Zeitstempel, Resultat.
- **Secure Defaults:** alle Limits aktiv, ausführliche Fehlermeldungen nur bei Debug-Konfiguration.
### 4.6 Kompatibilität
- **OData-Konformitätslevel (Server):** Ziel mindestens "Intermediate" gemäß OData-Konformitätsstufen, "Advanced" wo ohne disproportionalen Aufwand möglich. Konkret prüfbare Klauseln aus Spec §13 (siehe §10.2).
- **Akzeptanz-Clients (für Server-Validierung):** Power BI v4-Konnektor, Microsoft OData Connected Service (C#), Olingo-Java-Client – mindestens diese drei als Akzeptanztests.
- **Akzeptanz-Server (für Client-Validierung):** TripPin RW (`services.odata.org`), OASIS-Reference-Service, mindestens ein selbst gehostetes SAP-ähnliches Backend (siehe §10.4).
---
 
## 5. Architektur-Constraints
 
### 5.1 Schichtung
```
═══════════════════════════ SERVER ═══════════════════════════
┌─────────────────────────────────────────────────────────┐
│  OSGi HTTP Whiteboard  (Jakarta Servlet)                │
│  ODataServlet  @HttpWhiteboardServletPattern /odata/*   │
├─────────────────────────────────────────────────────────┤
│  Filter-Chain (siehe §5.1.1)                            │
│   Tracing → Auth → CSRF → ODataRequestFilter → Limits   │
│   Aufbau des ODataRequestContext (parsed + validated)   │
├─────────────────────────────────────────────────────────┤
│  Servlet: dispatcher.dispatch(reqCtx aus Attribut)      │
├─────────────────────────────────────────────────────────┤
│  OData Runtime  (Dispatcher, Composition, EdmRegistry)  │
│   Protokoll-agnostisch, von STA wiederverwendbar        │
├─────────────────────────────────────────────────────────┤
│  Query Service SPI   ──┬──> JPA Impl  (emf.persistence) │
│   (OCL-AST-Visitor)    ├──> Mongo Impl (Phase 2)        │
│                        └──> InMemory Impl (Test)        │
├─────────────────────────────────────────────────────────┤
│  Codec Layer (emf.codec-Kern, direkt aufgerufen)        │
│   ├─ Daten-OData-JSON-Profil                            │
│   ├─ CSDL-Meta-Codec (direkter Konverter, β)            │
│   └─ EDM-Ecore als Validierungs-Referenz (Tests)        │
└─────────────────────────────────────────────────────────┘
 
         ┃ horizontal von allen Schichten konsumiert ┃
         ▼                                           ▼
┌──────────────────────────────────────────────────────────────────────┐
│  Model Metadata Service (emf.model.metadata)                         │
│    AspectProviders: codec, odata, ocl, [orm, history, units, …]      │
│    Profiles pro EPackage – O(1)-Lookup, persistierbar als XMI        │
│    Lifecycle-Callbacks für Hot-Add/Remove                            │
└──────────────────────────────────────────────────────────────────────┘
 
═══════════════════════════ CLIENT ═══════════════════════════
┌─────────────────────────────────────────────────────────┐
│  Application API (Request-Builder-DSL)                   │
├─────────────────────────────────────────────────────────┤
│  $filter-AST → $filter-String  (Spiegelseite zu Server) │
│  Plus: $metadata-Cache, Vocabulary-Resolver              │
├─────────────────────────────────────────────────────────┤
│  Codec Layer (gemeinsam mit Server)                      │
├─────────────────────────────────────────────────────────┤
│  HTTP-Client (OSGi-HTTP-Client-Service / JDK HttpClient) │
└─────────────────────────────────────────────────────────┘
```
 
### 5.1.1 Request-Lifecycle (Server)
 
Das Filter+Context+Servlet-Pattern trennt HTTP-Eintrittspunkt, Cross-Cutting, Request-Parsing und Dispatch sauber. Das Catch-All-Servlet ist ein dünner Adapter; die eigentliche Logik sitzt in Filtern (für Aufbau des Kontexts) und im Dispatcher (für die Geschäftslogik).
 
**Filter-Chain über `service.ranking` (höher = früher):**
 
| `service.ranking`   | Filter                | Zweck                                                     |
|---------------------|-----------------------|-----------------------------------------------------------|
| `1000`              | TracingFilter         | Korrelations-ID setzen, MDC-Logging-Kontext               |
| `800` (Phase 2)     | AuthenticationFilter  | Auth-Subject auflösen, in Context legen                   |
| `700`               | CsrfFilter            | CSRF-Token-Prüfung (für SAP-Backwards-Compatibility, opt) |
| `500`               | **ODataRequestFilter**| URL-Parse via Olingo, EClass-Resolve, Query-Optionen geparst (Cache am `ODataAspectProvider`-Profile, s. §3.6.1), Type-Resolution |
| `400`               | LimitsFilter          | `$expand`-Tiefe, AST-Knoten, `$top`-Maximum gegen Limits  |
 
Filter mit höherem `service.ranking` laufen zuerst. Oberhalb des `ODataRequestFilter` ist alles Cross-Cutting und protokoll-agnostisch; ab dem `ODataRequestFilter` wird der OData-spezifische Context aufgebaut. Wenn STA als zweites Protokoll dazukommt, hat es seinen eigenen Filter-Block unter eigenem Pfad-Prefix (`/sta/*`); die generischen Filter (Tracing, Auth) werden per Pattern-Matching geteilt.
 
**Inhalt des `ODataRequestContext`:**
 
| Slot                    | Inhalt                                                               |
|-------------------------|----------------------------------------------------------------------|
| `parsedUri`             | Path-Segmente, EntitySet, Key-Predicate, Navigations-Pfad, Flags     |
| `resolvedModel`         | `EClass`, `EReference`-Pfad, `EOperation` (für Function/Action), Schema, Container |
| `parsedQuery`           | Reference auf Cache-Hit oder neu erzeugt: `$filter`/`$orderby`/`$expand`/`$apply` als ASTs |
| `formatSelection`       | aufgelöster Content-Type/Accept inkl. `metadata=full|minimal|none`   |
| `concurrencyHeaders`    | `If-Match`, `If-None-Match`                                          |
| `correlationId`         | Tracing-ID (durch TracingFilter gesetzt)                             |
| `limitsCounter`         | Mutables-Objekt für Tiefen-/AST-Knotenzähler                         |
| `authSubject` (Phase 2) | Auth-Subject (durch AuthenticationFilter gesetzt)                    |
 
Der Context wird stufenweise befüllt – jeder Filter setzt seinen Anteil. Resource und Dispatcher arbeiten read-only auf dem fertigen Context.
 
**Servlet-Struktur (Catch-All, `service()`-Dispatch):**
 
```java
@Component(service = Servlet.class)
@HttpWhiteboardServletPattern("/odata/*")
@HttpWhiteboardServletName("odata")
public class ODataServlet extends HttpServlet {
    @Reference ODataDispatcher dispatcher;
 
    // service() überschrieben, weil HttpServlet kein doPatch() kennt;
    // ODataRequestContext wurde von der Filter-Chain als Request-Attribut abgelegt.
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ODataRequestContext ctx = (ODataRequestContext) req.getAttribute(ODataRequestContext.ATTR);
        switch (req.getMethod()) {
            case "GET"    -> dispatcher.read(ctx, resp);
            case "POST"   -> dispatcher.create(ctx, req.getInputStream(), resp);
            case "PATCH"  -> dispatcher.update(ctx, req.getInputStream(), resp);
            case "DELETE" -> dispatcher.delete(ctx, resp);
            default       -> resp.sendError(SC_METHOD_NOT_ALLOWED);
        }
    }
}
```
 
Der Dispatcher schreibt direkt in den Response-Stream (über den `emf.codec`-Kern), statt ein JAX-RS-`Response`-Objekt zurückzugeben.
 
**Sonderfälle:**
- `/$metadata` und Service-Document (`/`): `ODataRequestFilter` erkennt früh, baut dünnen Context ohne Query-Optionen-Parsing. Dispatcher routet auf `MetadataProvider`.
- `/$batch`: Body-Inhalt wird vom `BatchProcessor` (Olingo-`BatchParser`) gesplittet. Sub-Requests durchlaufen die parsing-relevante Logik via geteilte Library (`ODataRequestParsing`), nicht via re-entrante Servlet-Pipeline. Die Library wird ebenfalls vom `ODataRequestFilter` aufgerufen, nur der äußere Servlet-Wrapper unterscheidet sich.
- Functions/Actions (`/Sales.GetTopN(N=10)`): Filter erkennt im Path-Parsing, resolved zu `EOperation`; Dispatcher routet auf `FunctionInvoker`/`ActionInvoker`.
### 5.2 Repo-Layout (Vorschlag, im Review)
```
emf.odata/
├── emf.odata.api               OData-Annotations, Public SPIs
├── emf.odata.metadata          odata.ecore + ODataAspectProvider (analog emf.codec.metadata)
├── emf.odata.query             ANTLR4-Grammar, Query-Modell-Erweiterung
├── emf.odata.codec.csdl        CSDL-Meta-Codec (direkter Konverter, β)
├── emf.odata.codec.json        OData-JSON Daten-Codec-Profil
├── emf.odata.vocabularies      OASIS-Standard-Vocabularies als EPackages
├── emf.odata.protocol.v4       v4 Protocol-Adapter (Servlet + Servlet-Filter, Olingo-Wrapper, Batch)
├── emf.odata.runtime           Server: Dispatcher, EdmRegistry, Composition (protokoll-agnostisch)
├── emf.odata.persistence.api   QueryService-SPI, OCL-AST-Visitor-Basis
├── emf.odata.persistence.jpa   JPA-Impl auf emf.persistence-jpa
├── emf.odata.client            Client-Komponente (HTTP, DSL, Cache)
└── emf.odata.test              Konformitäts- und Akzeptanztests
```
 
### 5.3 OSGi
- Alle Module als OSGi-Bundles mit DS-Komponenten.
- Public SPIs in `*.api`-Packages exportiert; Implementierungs-Packages nicht exportiert.
- Service-Property-Konventionen unter `org.eclipse.fennec.odata.*`.
- Alle Bundles `Bundle-RequiredExecutionEnvironment` Java 21.
---
 
## 6. Test- und Qualitätsanforderungen
- **Unit-Tests** pro Modul, JUnit 5.
- **Integrationstests** mit OSGi-Test-Framework (PaxExam oder bnd-testing).
- **Konformitätstests:** Klausel-getriebene Test-Matrix nach Spec §13 (Konformitätslevel) – Detail in §10.2.
- **Akzeptanztests Server:** mit realen Clients (Power BI, Microsoft OData Connected Service, Olingo) – siehe §10.4.
- **Akzeptanztests Client:** gegen reale Server (TripPin RW, OASIS-Reference) – siehe §10.4.
- **CSDL-Round-Trip-Tests:** EPackage → CSDL → EPackage muss isomorph sein (Ziel ≥ 95 % Fidelity nach Vorbild `jsonschema`-Codec). Test-Material aus §10.5.
- **OCL-AST-Translation-Tests:** dieselbe `$filter`-Expression → JPA + Mongo + InMemory muss semantisch äquivalente Ergebnisse liefern.
- **Client-Server-Round-Trip-Tests:** Server-Response → Client-Parsing → Client-Re-Serialize → semantisch identisch.
- **Coverage**: Mindestens 80 % Line-Coverage in `*.api` und `*.runtime`-Bundles.
- **Statische Analyse**: SpotBugs, Checkstyle, OSGi-Bnd-Analyse als Build-Gates.
---
 
## 7. Offene Fragen / Decisions Pending
 
### 7.1 Phase-0-Block (blockierend für Architektur-Entscheidungen)
 
**Klärungspunkte:**
 
| #  | Frage                                                                                         | Eingang     |
|----|-----------------------------------------------------------------------------------------------|-------------|
| Q1 | Welche BSI-TRs sind konkret bindend? (TR-02102-1/-2, TR-03116 angenommen – bestätigen)        | §4.5, §8    |
| Q15| EDM-Ecore: XSD-Round-Trip validiert? OASIS-XSDs (siehe §10.5) als Referenz                    | §3.4, §6    |
 
**Cross-Repo-Vorarbeiten:**
 
| #   | Aufgabe                                                                                       | Repo            |
|-----|-----------------------------------------------------------------------------------------------|-----------------|
| VA1 | `OclAspectProvider` in `emf.m2x` anlegen (analog `CodecAspectProvider`): Aspect-Klassen für Class/Feature, Type-Resolver-Cache am Profile, Lifecycle gegen `MetadataWhiteboard`. Voraussetzung für unsere E1/E4. Aufwand laut Stack-Owner überschaubar. | `eclipse-fennec/emf.m2x` |
 
### 7.2 Nicht-blockierende offene Fragen
 
| #  | Frage                                                                                         | Eingang     | Status |
|----|-----------------------------------------------------------------------------------------------|-------------|--------|
| Q2 | ~~Bestehendes Persistence-Query-Modell teilen~~ – Modell vorhanden, Refactoring §3.5          | §3.5        | **geschlossen** v0.2 |
| Q3 | ~~Transport: Jakarta-REST-Whiteboard oder HTTP-Whiteboard~~ – **revidiert v0.8**: OSGi HTTP Whiteboard (Servlet) fixiert, Jakarta REST verworfen (Routing/Negotiation beim Catch-All ungenutzt, Servlet leichter + direkte Stream-Kontrolle); v0.6-Entscheidung für Jakarta REST damit ersetzt. Siehe §2.1, §5.1.1, `docs/decisions/0001` | §2.1, §5.1 | **geschlossen** v0.8 |
| Q4 | Konkrete Akzeptanz-Clients/Server-Versionen fixieren (Vorschlag in §10.4)                     | §4.6, §10.4 | offen  |
| Q5 | ~~Vocabulary-Versorgung~~ – Plan: per CSDL-Read-Pfad aus OASIS-Definitionen                   | §3.4        | **geschlossen** v0.2 |
| Q6 | Max-Defaults für `$top`, `$expand`-Tiefe, Batch-Größe finalisieren                            | §4.5        | offen  |
| Q7 | ~~OCL als alternative `$filter`-Syntax~~ – obsolet, OCL ist jetzt internes IR                 | §3.6        | **geschlossen** v0.2 |
| Q8 | Lizenz-/Distribution-Strategie: ein Aggregator-Repo oder mehrere Repos analog `emf.osgi`      | §5.2        | offen  |
| Q9 | Content-Negotiation für CSDL-XML vs. CSDL-JSON: Default und Override-Mechanismus              | §3.1        | offen  |
| Q10| Conformance-Level-Ziel: "Intermediate" reicht oder "Advanced" anstreben?                      | §4.6        | offen  |
| Q11| Query-Modell-Refactoring: Stufe 2 in Phase 1 oder Phase 2?                                    | §3.5        | offen (v0.2) |
| Q12| `$apply`-Aggregations-Submodell: in Phase 1 modellieren (Slot) oder Phase 2 ganz schieben?    | §3.5        | offen (v0.2) |
| Q13| ~~EAnnotation-Vocabulary für OData-Mappings: separates Sub-Doc~~ – strukturell durch `odata.ecore` + `ODataAspectProvider` (§3.4) gelöst. Schrumpft auf eine reine Annotations-Keys-Liste – als Tabelle in §3.4 zu führen, kein eigenes Architektur-Doc nötig. | §3.4 | **geschlossen** v0.7 |
| Q14| ~~OCL-Type-Checker: Performance-Spike?~~ – durch Type-Resolution-Cache am `OclAspectProvider` (§3.6.1) strukturell gelöst | §3.5, §4.1 | **geschlossen** v0.7 |
| Q16| ~~CSDL-Codec α/β/γ?~~ – **revidiert v0.9**: statt β (direkter Konverter) wird CSDL über eine Instanz des OASIS-EDM/EDMX-Modells (`org.odata.csdl.model`) + EMF-Serialisierung erzeugt/gelesen (bidirektionaler Ecore↔EDM-Converter); durch Schreib- und Lese-Spike belegt. Siehe §3.4, `docs/decisions/0002` | §3.4 | **geschlossen** v0.9 |
| Q16a| Client-HTTP-Provider: OSGi-HTTP-Client-Service oder JDK-`HttpClient` als Default?            | §3.8        | offen (v0.3) |
| Q17| Typed-Proxy-Code-Generierung Client: Phase 2 oder dauerhaft out of scope?                     | §3.8        | offen (v0.3) |
| Q18| SAP-ES5-Demo-Account: registrieren für Phase-2-v2-Akzeptanztests?                             | §10.4       | offen (v0.4) |
| Q19| OGC SensorThings (STA) als zweites Protokoll: STA-HTTP-Endpoint als zweiter Protokoll-Adapter (analog `emf.odata.protocol.v4`) plus `HistoryAspectProvider` mit OGC_STA-Standard – in welcher Phase einplanen? | §1, §2.3, §5.1 | offen (v0.6) |
| Q20| Cache-Key-Kanonisierung im `ODataAspectProvider`-Cache: Phase 1 oder Phase 2 (abhängig von Hit-Rate-Statistik)? | §3.6.1 | offen (v0.6) |
| Q21| `OrmAspectProvider` in `emf.persistence-jpa`: vorhanden, geplant oder unsere Verantwortung? Hat Auswirkungen auf E5-Aufwand. | §2.3, §9 (E5) | offen (v0.7) |
 
---
 
## 8. Referenzen / Standards
 
### 8.1 OData-Standards (OASIS)
- OData Version 4.01 Part 1: Protocol
- OData Version 4.01 Part 2: URL Conventions
- OData Version 4.01 Part 3: Common Schema Definition Language (CSDL) – XML und JSON
- OData JSON Format Version 4.01
- OData ABNF (für Query-Grammatik-Referenz)
- OData v2 Specification (für spätere v2-Phase)
(Konkrete URLs und Test-Material in §10)
 
### 8.2 BSI Technische Richtlinien (Annahme – im Review zu bestätigen)
- BSI TR-02102-1 – Kryptographische Verfahren: Empfehlungen und Schlüssellängen
- BSI TR-02102-2 – Kryptographische Verfahren: Verwendung von TLS
- BSI TR-03116 – Kryptographische Vorgaben für Projekte der Bundesregierung
- *(Weitere TRs je nach Einsatzkontext: TR-03161 für Gesundheit, TR-03107 für eIDs, …)*
### 8.3 OCL-Spezifikationen
- OMG OCL v2.4 – Object Constraint Language Specification
- Optional v2.5-Erweiterungen (Maps, Safe-Navigation) – im Fennec-OCL bereits enthalten
### 8.4 Plattform / Frameworks
- Java Language Specification – Java 21
- OSGi Core Specification R8 (R9 wo zugänglich)
- Eclipse Modeling Framework Documentation
- Apache Olingo V4 Documentation (URI-Parser, Batch-Splitter)
### 8.5 Fennec-interne Referenzen
- `eclipse-fennec/emf.osgi`
- `eclipse-fennec/emf.codec` – insbesondere Codec-V2-Spec, Module `jsonschema`/`openapi` als Vorbild für CSDL
- `eclipse-fennec/emf.persistence-jpa`
- `eclipse-fennec/emf.m2x` (OCL-Modell + Parser, EDM/EDMX-Modelle, QVT)
---
 
## 9. Umsetzungsplan (Epics und Reihenfolge)
 
### 9.1 Epic-Übersicht
 
| #   | Epic                              | Hauptabhängigkeit            |
|-----|-----------------------------------|------------------------------|
| E1  | Foundation & Metadata             | VA1 (Vorarbeit, siehe §7.1)  |
| E2  | CSDL Codec (Meta-Format)          | E1                           |
| E3  | OData-JSON Daten-Codec            | E1                           |
| E4  | Query Translation                 | E1                           |
| E5  | Persistence Adapter (JPA)         | E4                           |
| E6  | Server Runtime                    | E2 + E3                      |
| E7  | Server Protocol Adapter v4        | E6 + E4                      |
| E8  | Client                            | E2 + E3 (E4 für DSL)         |
| E9  | Conformance & Acceptance          | alles                        |
 
### 9.2 Inhaltliche Bedeutung der Epics
 
Die folgenden Beschreibungen knüpfen an die Konzepte aus §1.1 an und sollen jedem Beteiligten klar machen, was hinter jedem Epic inhaltlich steckt.
 
**E1 – Foundation & Metadata**
Hier wird das Übersetzungs-Wörterbuch zwischen unseren Ecore-Modellen und der OData-Welt aufgebaut – konkret als eigenes `odata.ecore` mit Aspect-Klassen plus ein `ODataAspectProvider`, vollständig analog zum bestehenden `org.eclipse.fennec.codec.metadata` (siehe §2.3). Damit ist OData-Annotations-Handling nicht ein Sonderweg, sondern derselbe Mechanismus, der schon für Codec produktiv läuft. Aspects definieren beispielsweise: welche EAttribute ist der Schlüssel, welche EOperation soll als aufrufbare Funktion sichtbar sein, welcher EDataType wird auf welchen Wire-Format-Typ abgebildet. Außerdem werden in diesem Epic die offiziellen OData-Annotations-Vokabularien (Org.OData.Core, Capabilities, Validation, Measures – plus SAP-Vocabularies) als EPackages bereitgestellt. Voraussetzung ist die Cross-Repo-Vorarbeit VA1 (siehe §7.1), die den `OclAspectProvider` in `emf.m2x` einführt.
 
**E2 – CSDL Codec (Meta-Format)**
Das in §1.1 erwähnte selbstbeschreibende Schema-Dokument unter `.../$metadata` ist die Netzwerk-Form einer `.ecore`-Datei. Im OData-Vokabular heißt das Format CSDL. Dieses Epic baut den bidirektionalen Übersetzer zwischen Ecore und CSDL, sowohl als XML als auch als JSON. Realisiert **nicht** als handgeschriebener Konverter, sondern über einen bidirektionalen **Ecore↔EDM-Converter** auf Basis des OASIS-generierten EDM/EDMX-Modells (`org.odata.csdl.model`): Server-seitig wird aus EPackage plus `ODataClassProfile` (aus E1) eine EDM-Modell-Instanz aufgebaut und von EMF serialisiert (CSDL-XML gratis aus der XSD-`ExtendedMetaData`; CSDL-JSON als Codec-Profil). Client-seitig lädt EMF ein fremdes CSDL-Dokument in dieselbe EDM-Instanz, aus der ein Mapper EPackage plus passende ODataAspects rekonstruiert. Schreib- und Lesepfad sind durch Spikes belegt (ADR 0002, Bundle `org.eclipse.fennec.odata.csdl`).
 
**E3 – OData-JSON Daten-Codec**
Während E2 Schemata transportiert, transportiert dieses Epic die eigentlichen Instanzen. Wenn der Server einen `Customer` zurückgibt – wie sieht das als JSON konkret aus? OData hat einen eigenen JSON-Dialekt mit Konventionen, die jedem Datensatz seinen Typ und seine eindeutige URL beilegen. Dieses Epic erweitert das Codec-Framework um ein passendes Profil für genau diesen Dialekt, plus Sonderbehandlung für Datentypen, die JSON nicht nativ kennt: Datum ohne Uhrzeit, Duration im ISO-8601-Format, Decimal mit Precision/Scale, Binary in Base64.
 
**E4 – Query Translation**
Aus §1.1: Aufrufer senden Filter-Ausdrücke wie `Name eq 'Müller' and Age gt 30` als String in der URL mit. Dieses Epic parst diese Strings in unser internes Modell – konkret in OCL, weil OCL strukturell exakt das ausdrückt, was OData-Filter sind: side-effect-freie Boolean-Ausdrücke über Modell-Pfade. Ergebnis: der Rest des Systems arbeitet auf einem strukturierten AST, nicht auf Strings. Damit werden Backend-Übersetzung, Validierung und Optimierung erst möglich. Auch komplexere Konstrukte wie Lambda-Operatoren über Sammlungen (`Orders/any(o: o/Total gt 100)`) und Aggregationen (`$apply`) werden hier abgedeckt. Caching der geparsten Expressions geschieht am Profile des `ODataAspectProvider` (siehe §3.6.1) – damit ist Cache-Invalidierung automatisch an den EPackage-Lifecycle gekoppelt.
 
**E5 – Persistence Adapter (JPA)**
Wenn die Anfrage als AST steht, muss sie an die Datenbank gestellt werden. `emf.persistence-jpa` macht das EMF-zu-JPA-Mapping bereits. Dieses Epic ergänzt: aus dem AST eine JPA-Criteria-Query erzeugen, mit Sorgfalt für Performance. Konkret: das in §1.1 erwähnte "Beziehungen direkt mit-auflösen" darf nicht zu einem N+1-Problem werden, alle Filter müssen an die Datenbank weitergereicht werden, kein In-Memory-Filtering. Das Interface ist abstrakt genug, dass später eine MongoDB-Implementierung dazukommen kann. Falls in `emf.persistence-jpa` ein `OrmAspectProvider` etabliert wird (siehe Q21), wird E5 noch schmaler – andernfalls bauen wir uns ein leichtgewichtiges Mapping selbst.
 
**E6 – Server Runtime**
Der protokoll-agnostische Kern: ein Dispatcher, der einen vollständig geparsten und validierten Request-Context bekommt und auf den richtigen Verarbeiter (Read/Create/Update/Delete/Function/Action) routet. Plus die `EdmRegistry`, die die EPackage-Composition aus OSGi-Services aufbaut: mehrere als OSGi-Service registrierte EPackages werden zu einem gemeinsamen Container aggregiert, sodass eine API mehrere Modelle gemeinsam exponieren kann. Dieser Kern weiß nichts über HTTP, OData oder STA – er ist die wiederverwendbare Mitte. Hot-Add/Remove von EPackages wird hier behandelt; CSDL-Generierung und Limits-Konfiguration sitzen ebenfalls hier.
 
**E7 – Server Protocol Adapter v4**
Die OData-spezifische HTTP-Schicht: Catch-All-`ODataServlet` unter `/odata/*` (OSGi HTTP Whiteboard), eine Servlet-Filter-Chain (Tracing → Auth → CSRF → ODataRequestFilter → Limits, siehe §5.1.1), die den Request-Context aufbaut, plus die OData-spezifischen Protokoll-Features, die in §1.1 angedeutet sind: ETags für optimistische Concurrency, Batch-Requests mit `multipart/mixed` und JSON-Batch, Deep Insert, Functions/Actions. Hier kommt auch der Olingo-URI-Parser zum Einsatz, der das Zerlegen der URLs in ihre Bestandteile übernimmt. Wichtig: dieses Epic ist OData-spezifisch und wird für STA durch ein eigenes `emf.sta.protocol`-Bundle gespiegelt – beide konsumieren denselben E6-Dispatcher.
 
**E8 – Client**
Das Spiegelbild von E2-E7: wir wollen einen fremden OData-Service konsumieren. Schema lesen, eine fluent Java-DSL anbieten, Anfragen senden, Antworten parsen. Großteile von E2/E3/E4 werden direkt wiederverwendet, weil das Protokoll symmetrisch ist – derselbe Codec, den der Server zum Schreiben verwendet, liest der Client; derselbe Query-AST, in den der Server Filter-Strings parst, serialisiert der Client zu Filter-Strings. Praktisch: ein Java-Entwickler schreibt `client.entitySet(Customers).filter(c -> c.age().gt(30)).execute()` und bekommt Java-Objekte zurück.
 
**E9 – Conformance & Acceptance**
Die OData-Spezifikation hat hunderte normative Aussagen ("ein Server MUSS X tun, SOLLTE Y tun"). Dieses Epic baut die Test-Matrix dazu, plus die Akzeptanztests gegen reale Werkzeuge: Power BI verbindet sich tatsächlich mit unserem Server und kann die Daten auswerten. Microsoft-Tooling generiert C#-Klassen aus unserem Schema und ruft sie erfolgreich auf. Unser Client spricht erfolgreich mit fremden Services wie TripPin (OData-Test-Server von Microsoft). Erst nach diesem Epic können wir glaubhaft behaupten, dass die Implementierung produktionsreif und mit dem bestehenden OData-Ökosystem kompatibel ist.
 
### 9.3 Phasen und Reihenfolge
 
**Phase 0 – Vorab-Klärungen und Vorarbeiten (2–3 Wochen):**
Zwei Klärungspunkte (Q1, Q15) aus §7.1, plus die Cross-Repo-Vorarbeit VA1 (`OclAspectProvider` in `emf.m2x`). VA1 ist Voraussetzung für E1 und E4 – läuft parallel zu Q1/Q15. Q3, Q14, Q16 wurden in v0.6 / v0.7 entschieden.
 
**Phase 1 – Foundations:**
- E1 zuerst (Voraussetzung für alle anderen): `odata.ecore`, `ODataAspectProvider`, OASIS-Vocabularies als EPackages
- E2 und E3 dann **parallel** (keine Abhängigkeit zueinander, beide auf E1)
- Beide haben klare Round-Trip-Tests als Akzeptanz, also gut isoliert lieferbar
**Phase 2 – Query & Persistence:**
- E4 baut auf E1 (OCL- und Aggregations-Modell-Erweiterungen)
- E5 baut auf E4
- E5 erst mit JPA, Mongo später als zweiter Anbieter
**Phase 3 – Server:**
- E6 vor E7 (Runtime muss stehen, bevor Protocol-Adapter aufsetzt)
- Walking Skeleton (s. §9.4) wird hier komplett
**Phase 4 – Client:**
- Kann **parallel zu Phase 3** starten (ab Ende Phase 2 möglich, weil E2 + E3 dann fertig)
- Zwei Ströme: Server-Stream (Phase 3) und Client-Stream (Phase 4) laufen nebeneinander
**Phase 5 – Conformance & Härtung:**
- E9 als finale Phase
- Plus Performance-Tuning der Phase-2-Komponenten
### 9.4 Walking Skeleton – frühster nutzbarer Stand
 
Vor Abschluss von Phase 3: ein read-only Endpoint, der eine `EntityCollection` mit `$filter`, `$select`, `$top`, `$skip`, `$count` ausliefert plus `/$metadata`. Erfordert minimale Versionen von E1, E2, E3, E4, E5, E6, E7.
 
**Umfang:**
- E1 minimal (OData-EAnnotations für Key + Type-Mapping)
- E2 minimal (CSDL-Generierung für simple EClasses; Annotations können noch eingeschränkt sein)
- E3 minimal (JSON-Daten-Output, kein PATCH/POST)
- E4 minimal (`$filter` ohne Lambdas, ohne `cast`/`isof`)
- E5 minimal (JPA-Backend für read-only)
- E6 minimal (ein Container, ein EPackage, kein Hot-Reload)
- E7 minimal (URI-Parser ohne Batch, ohne Functions)
**Wert:** Power BI kann diesen Stand bereits als Datenquelle nutzen. Externes Feedback wird sehr früh möglich, lange bevor Phase 3 abgeschlossen ist. Erreichbar nach ~40 % der Gesamtarbeit.
 
### 9.5 Querschnittsthemen
- **Tests** (Unit + Integration + Round-Trip) ab Phase 1
- **Konformitätstests** ab Walking Skeleton, eskalierend zu E9
- **Dokumentation** parallel zu jedem Epic
- **OSGi-Hygiene** als Build-Gate ab Phase 1
---
 
## 10. Verifikations-Ressourcen
 
### 10.1 Normative Specs (Pflicht-Quellen)
 
| Quelle                                | URL                                                                                              | Zweck                                |
|---------------------------------------|--------------------------------------------------------------------------------------------------|--------------------------------------|
| OData v4.01 Part 1 (Protocol)         | `https://docs.oasis-open.org/odata/odata/v4.01/os/part1-protocol/odata-v4.01-os-part1-protocol.html` | Server-Protokoll, Konformitätslevel  |
| OData v4.01 Part 2 (URL Conventions)  | `https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html` | URL-Syntax, Query-Optionen           |
| OData CSDL XML 4.01                   | `https://docs.oasis-open.org/odata/odata-csdl-xml/v4.01/os/odata-csdl-xml-v4.01-os.html`         | CSDL-XML-Spec                        |
| OData CSDL JSON 4.01                  | `https://docs.oasis-open.org/odata/odata-csdl-json/v4.01/os/odata-csdl-json-v4.01-os.html`       | CSDL-JSON-Spec                       |
| OData JSON Format 4.01                | `https://docs.oasis-open.org/odata/odata-json-format/v4.01/os/odata-json-format-v4.01-os.html`   | Daten-JSON-Format                    |
| OData ABNF Construction Rules 4.01    | `https://docs.oasis-open.org/odata/odata/v4.01/os/abnf/`                                         | Grammar-Referenz für E4              |
| OData ABNF Test Cases 4.01            | `https://docs.oasis-open.org/odata/odata/v4.01/os/abnf/`                                         | **Direkter Test-Input für E4**       |
| OData Vocabularies 4.0                | `http://docs.oasis-open.org/odata/odata-vocabularies/v4.0/odata-vocabularies-v4.0.html`          | Standard-Vocabularies                |
| OData Data Aggregation Extension 4.0  | `https://docs.oasis-open.org/odata/odata-data-aggregation-ext/v4.0/cs03/`                        | `$apply`-Spec mit eigener ABNF       |
| OData v2 Spec                         | `https://www.odata.org/documentation/odata-version-2-0/`                                         | Phase-2-Referenz                     |
 
**Hinweis zu CSDL XSDs/JSON-Schemas:** Unterhalb der CSDL-Spec-URLs liegen `/schemas/`-Verzeichnisse mit den autoritativen XSDs (CSDL-XML) und JSON-Schemas (CSDL-JSON). Diese sind direkter Input für Q15 (Round-Trip-Test des EDM-Ecore).
 
### 10.2 Konformitätsstrategie (kein offizielles TCK)
 
Es existiert **kein executables TCK** für OData wie bei Jakarta EE. OData v4.0 Part 1 §13 definiert stattdessen drei Konformitätslevel (Minimal, Intermediate, Advanced) mit MUST/SHOULD-Klauseln plus einen separaten Abschnitt für „Interoperable OData Clients".
 
**Strategie:**
1. **Klausel-Extraktion:** Alle MUST/SHOULD-Klauseln aus §13 in einer Tracking-Tabelle erfassen, geschätzt 150–250 Items pro Level.
2. **Test-Matrix:** Pro Klausel einen JUnit-5-Test in `emf.odata.test` mit Spec-Referenz (`@SpecRef("§13.1.2 OData 4.0 Intermediate")` als Custom-Annotation oder via Test-Display-Name).
3. **Coverage-Dashboard:** Tracking-Tabelle als CSV/Markdown im Repo, automatisch aus Test-Annotationen generiert. Macht Konformitätsstand sichtbar.
4. **ABNF-Test-Cases:** Die OASIS-ABNF-Test-Cases-Datei wird als Datenquelle in `emf.odata.test` eingebunden – Akzeptanz-Tests für E4 (Parser).
### 10.3 Code-Referenzen
 
#### 10.3.1 Java – Pflicht-Lektüre
 
| Repo                                  | Lizenz       | Zweck                                                                |
|---------------------------------------|--------------|----------------------------------------------------------------------|
| `apache/olingo-odata4`                | Apache-2.0   | URI-Parser + Batch-Splitter (Code-Studium, Verwendung)               |
| `apache/olingo-odata2`                | Apache-2.0   | v2-Architektur (Phase-2-Vorbereitung)                                |
| **`SAP/olingo-jpa-processor-v4`**     | Apache-2.0   | **Goldstandard-Referenz für E5** – JPA-Mapping, `$expand`-N+1-Vermeidung, Pagination |
| **`eclipse-fennec/emf.m2x`**          | EPL-2.0      | **Cache-Pattern-Vorbild** für die Profile-Cache-Strategie – siehe `OclLruExpressionCache` und [OCL User Guide](https://github.com/eclipse-fennec/emf.m2x/blob/snapshot/docs/ocl-user-guide.md) §7. Zukünftig: `OclAspectProvider` als Integrations-Anschluss an den Metadata-Service (siehe VA1 in §7.1) |
 
#### 10.3.2 .NET / C# – Wire-Format-Referenz
 
| Repo                          | Lizenz | Zweck                                                                  |
|-------------------------------|--------|------------------------------------------------------------------------|
| `OData/odata.net`             | MIT    | Microsoft-Core-Lib für Wire-Format-Edge-Cases (gerade JSON)            |
| `OData/WebApi`                | MIT    | C#-Server-Implementierung; Quervergleich Server-Verhalten              |
| `OData/RESTier`               | MIT    | Backend hinter den TripPin-Reference-Services; gut zum Reproduzieren   |
| `OData/ODataSamples`          | MIT    | TripPin-Sources, Sample-EDM-Modelle, Test-Patterns                     |
| `OData/odata-openapi`         | MIT    | Tooling CSDL ↔ OpenAPI – relevant für Cross-Format-Verständnis         |
 
#### 10.3.3 JavaScript / TypeScript
 
| Repo                                       | Lizenz      | Zweck                                                |
|--------------------------------------------|-------------|------------------------------------------------------|
| `apache/olingo-odata4-js` (`odatajs`)      | Apache-2.0  | Client-Konzept-Inspiration für E8 DSL                |
| `OData/MetadataParser` (TypeScript)        | MIT         | Beispiel-CSDL-Parser; nützlich für Edge-Cases        |
 
#### 10.3.4 Python
 
| Repo               | Lizenz       | Zweck                                                                            |
|--------------------|--------------|----------------------------------------------------------------------------------|
| `SAP/python-pyodata` | Apache-2.0 | SAP-Python-Client mit umfangreichem Test gegen reale SAP-Backends. Wertvoll für SAP-Quirks und CSRF-Token-Handling |
| `tuomur/python-odata` | MIT       | Schlanker v4-Client; gut für CSDL-Konsumption-Beispiele                           |
 
#### 10.3.5 Go
 
| Repo                       | Lizenz | Zweck                                                       |
|----------------------------|--------|-------------------------------------------------------------|
| `CloudyKit/go-odata`       | MIT    | Schlanke v4-Server-Implementierung; alternative Architektur |
 
### 10.4 Test-Services
 
#### 10.4.1 Server-Akzeptanz (für unseren Client)
 
| Service                                              | Version | Auth          | Eignung                                  |
|------------------------------------------------------|---------|---------------|------------------------------------------|
| `services.odata.org/V4/TripPinServiceRW/`            | v4      | None          | **Primär** – read/write, viele V4-Features |
| `services.odata.org/V4/(Read)Northwind/Northwind.svc/`| v4     | None          | Klassiker, Basics                        |
| `services.odata.org/V2/Northwind/Northwind.svc/`     | v2      | None          | Phase 2                                  |
| **SAP ES5 Demo Gateway**                             | v2 (SAP)| Free Account | Phase 2 v2-Akzeptanz mit echtem SAP-Verhalten (CSRF, sap:-Annotations) |
| OASIS Open Data Demo                                 | v4      | None          | Reference                                |
 
#### 10.4.2 Client-Akzeptanz (für unseren Server)
 
| Client                                  | Version       | Form           | Eignung                                |
|-----------------------------------------|---------------|----------------|----------------------------------------|
| Power BI Desktop OData v4-Konnektor    | aktuell       | Desktop        | **Primär** – externes Feedback früh   |
| Microsoft OData Connected Service       | VS-2022-Ext   | Code-Gen + Run | C#-Client-Validierung                  |
| Apache Olingo V4 Java Client            | 5.0.0         | Library        | Java-Client-Validierung                |
| `pyodata` / `python-odata`              | aktuell       | Python         | Cross-Plattform-Validierung            |
| Excel OData-Funktion (Daten → aus Web)  | aktuell       | Desktop        | Endbenutzer-Tooling                    |
 
### 10.5 Test-Daten und Schemata
 
| Quelle                                                    | Inhalt                                  | Nutzung                              |
|-----------------------------------------------------------|-----------------------------------------|--------------------------------------|
| OASIS CSDL-XSDs unter `odata-csdl-xml/v4.01/os/schemas/`  | XSDs für CSDL XML                       | Q15: XSD-Round-Trip des EDM-Ecore    |
| OASIS CSDL-JSON-Schemas unter `odata-csdl-json/v4.01/os/schemas/` | JSON Schemas für CSDL JSON      | Validierung CSDL-JSON-Output         |
| OASIS ABNF Test Cases                                     | Akzeptierte/abgelehnte URL-Strings      | E4 ANTLR4-Akzeptanztests             |
| `OData/ODataSamples` (GitHub)                             | TripPin-EDM, weitere Sample-Modelle     | Reference-Test-Modelle für CSDL-Codec|
| TripPin-`$metadata`                                       | `services.odata.org/V4/TripPinServiceRW/$metadata` | Live-CSDL-Beispiel für Round-Trip-Test |
| Northwind-`$metadata`                                     | v2 und v4                               | Cross-Version-Test-Material          |
 
### 10.6 Bekannte Quirks und Implementierungs-Fallen
 
**Power BI – `OData-Version`-Header als MUST**
Die Spec sagt SHOULD, Power-BI-Mashup-Engine behandelt es als MUST. Konsequenz: Server muss `OData-Version: 4.0` in jeder Antwort setzen. Default in unserem Daten-Codec verbindlich (siehe §3.7).
 
**SAP – CSRF-Token-Round-Trip**
SAP-Gateways verlangen für POST/PATCH/DELETE einen CSRF-Token, der zuvor per GET mit `X-CSRF-Token: Fetch` zu holen ist. Client-seitig in §3.8 als HTTP-Layer-Feature einzuplanen.
 
**SAP – Doppelte Annotation-Vocabularies**
SAP-Backends emittieren häufig sowohl OASIS-konforme `Org.OData.Capabilities.V1.*`- als auch Legacy-`sap:`-Annotationen. Der CSDL-Codec muss beide Schemas in der Read-Richtung tolerieren und in der Write-Richtung optional ausgeben können.
 
**Power BI – `metadata=full` für komplexe Modelle**
Power BI Mashup Engine arbeitet zuverlässig erst mit `Accept: application/json;odata.metadata=full` bei komplexen Modellen mit Vererbung. Server-Default ggf. `minimal`, aber `full` muss korrekt unterstützt sein.
 
**Olingo URI-Parser – Edge-Case in Path-Segmenten**
Bei verschachtelten `$expand` mit komplexen Filter-Klauseln zeigt Olingo gelegentlich abweichende AST-Strukturen. Unsere ANTLR4-Grammatik sollte ABNF-konform sein, nicht Olingo-konform; Cross-Check über ABNF Test Cases.
 
**JSON `null` vs. fehlend**
OData unterscheidet streng zwischen „Property mit Wert null" (gesetzt, leer) und „Property fehlt" (nicht modifiziert, relevant für PATCH). Der Daten-Codec muss beides unterscheidbar serialisieren und parsen.
 
**OData v2 Datums-Literale**
v2 verwendet `/Date(milliseconds)/`-Format, v4 ISO 8601. Bei v2-Phase als Codec-Profil-Spezifikum implementieren.
 
---
 
## Änderungshistorie
| Version | Datum       | Autor   | Änderung                                                                       |
|---------|-------------|---------|--------------------------------------------------------------------------------|
| 0.1     | 2026-05-08  | Draft   | Initiale Erfassung                                                             |
| 0.2     | 2026-05-08  | Draft   | OCL als Predicate-IR (Option C); CSDL-Pipeline auf vorhandenes EDM-Ecore + Codec konkretisiert; Olingo-Scope auf URI/Batch eingegrenzt; Query-Modell-Refactoring in 3 Stufen; `$apply`-Submodell skizziert; Q2/Q5/Q7 geschlossen, Q11–Q15 ergänzt |
| 0.3     | 2026-05-08  | Draft   | Client-Komponente in Scope (§3.8); Codec-Architektur basierend auf `emf.codec`-Spec präzisiert (§2.2.1, §3.4); CSDL-Realisierung als drei Optionen α/β/γ aufgenommen (Q16); Daten-OData-JSON-Codec separat ausgearbeitet (§3.7); Repo-Layout um `emf.odata.client` und `emf.odata.codec.*` erweitert; §9 Umsetzungsplan mit 9 Epics, 5 Phasen, Walking Skeleton; Phase-0-Block in §7.1 als blockierend markiert; Q16a, Q17 ergänzt |
| 0.4     | 2026-05-08  | Draft   | §10 Verifikations-Ressourcen mit Pflicht-Specs, Konformitätsstrategie (kein TCK – Klausel-Matrix), Code-Referenzen (Java, .NET, JS, Python, Go), Test-Services, Test-Daten und §10.6 bekannte Quirks (Power-BI-Header, SAP-CSRF, etc.); §3.6/§3.7/§4.6/§6 mit Pointern auf §10; Q18 (SAP-ES5-Account) ergänzt |
| 0.5     | 2026-05-08  | Draft   | §1 als "Überblick" umstrukturiert, §1.1 "Was ist OData?" für Unerfahrene mit EMF-Analogien hinzugefügt; §1.2–§1.5 entsprechend renumeriert; §9 in Übersichts-Tabelle (§9.1) und ausführliche Epic-Erklärungen (§9.2) aufgeteilt, mit Querverweisen auf §1.1 |
| 0.6     | 2026-05-08  | Draft   | Q3 (Transport) entschieden: Jakarta REST Whiteboard mit Filter+Context+Resource-Pattern, Catch-All-Resource (Singleton), §5.1.1 Request-Lifecycle neu mit Filter-Chain-Tabelle und ODataRequestContext-Slots; multi-protokoll-fähige Architektur (perspektivisch OGC SensorThings); Q14 (OCL-Performance) entschieden durch ODataQueryParseCache (§3.6.1) als verbindliches LRU-Cache-Konstrukt nach Vorbild OclLruExpressionCache aus emf.m2x; §3.5/§3.6/§4.1/§4.5 entsprechend angepasst; §9.2 E4/E6/E7 mit den neuen Architektur-Details; §10.3.1 emf.m2x als Cache-Pattern-Vorbild ergänzt; Phase-0-Block schrumpft auf Q1/Q15/Q16; Q19 (STA-Phasenzuordnung) und Q20 (Cache-Key-Kanonisierung) ergänzt |
| 0.8     | 2026-06-24  | Draft   | **Q3 (Transport) revidiert**: weg von Jakarta REST Whiteboard (v0.6) hin zum **OSGi HTTP Whiteboard (Servlet)**. Begründung: Catch-All-Eintrittspunkt parst URL/Format/Optionen selbst → JAX-RS-Routing/Negotiation laufen leer; Servlet leichter, direkte Stream-Kontrolle für `$batch`/große Payloads, Codec-**Kern** (transport-neutral) direkt aufgerufen statt `codec.rest`-MBR/MBW. §2.1, §2.2, §5.1/§5.1.1 (Servlet + `service.ranking`-Filter-Chain, Code-Snippet), §3.6/§4.5, §5.2-Layout und §9.2 E7 angepasst; neues ADR `docs/decisions/0001-transport-servlet-vs-jakarta-rest.md` mit Herleitung + verworfener Alternative |
| 0.9     | 2026-06-24  | Draft   | **Q16 (CSDL-Realisierung) revidiert**: weg von β (direkter Konverter) hin zu **CSDL über eine Instanz des OASIS-EDM/EDMX-Modells `org.odata.csdl.model` + EMF-Serialisierung**. Beide Richtungen durch Spikes belegt (Schreiben: EDM-Instanz → spec-konformes CSDL-XML; Lesen: echtes TripPin-`$metadata` → EDM-Instanz, 9 EntityTypes / 4 ComplexTypes / Functions / Container fehlerfrei). §3.4 und §9.2 E2 angepasst; neues Bundle `org.eclipse.fennec.odata.csdl` (bidirektionaler Ecore↔EDM-Converter + Spike-Tests); neues ADR `docs/decisions/0002-csdl-via-edm-model.md` |
| 0.7     | 2026-05-08  | Draft   | Model Metadata Service als architektonisches Fundament aufgenommen (§2.3 neu), basierend auf `emf.model.metadata` und Vorbild `org.eclipse.fennec.codec.metadata`; CSDL-Codec α/β/γ-Frage zu **β** (direkter Konverter) entschieden via Metadata-Service-Profile; Q16 geschlossen; §3.4 entsprechend umformuliert mit `odata.ecore` + `ODataAspectProvider`-Pattern; Q13 strukturell gelöst (kein eigenes Sub-Doc nötig); §3.6.1 vom isolierten ODataQueryParseCache zu Lifecycle-gekoppeltem Caching am Metadata-Service umgebaut, Hot-Add/Remove-Sicherheit erreicht; §5.1 mit Metadata-Service als horizontale Schicht; Repo-Layout um `emf.odata.metadata` ergänzt; §7.1 Phase-0 schrumpft auf Q1/Q15, Cross-Repo-Vorarbeit VA1 (`OclAspectProvider` in `emf.m2x`) als Voraussetzung für E1/E4 ergänzt; E1 von "Foundation & Vocabularies" zu "Foundation & Metadata" umbenannt; §9.3 Phase-0 auf 2–3 Wochen erweitert; Q21 (`OrmAspectProvider`-Status) ergänzt |
 
