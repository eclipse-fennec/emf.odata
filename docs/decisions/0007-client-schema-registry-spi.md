# ADR 0007 – Client-Schema-Registry: Fetch/Convert entkoppelt von Persistenz/Lookup

| Feld     | Wert                                                                     |
|----------|--------------------------------------------------------------------------|
| Status   | Akzeptiert                                                               |
| Datum    | 2026-07-08                                                               |
| Betrifft | E8 (OData-Client), Anbindung an Fennec Model Atlas, req §3.4 (Modelle)   |

## Kontext

Der OData-Client soll fremde Services (z. B. SAP) andocken: deren `$metadata` lesen und in
Ecore-EPackages wandeln. Diese Modelle sollen **nicht bei jedem Request** neu geholt werden,
sondern **einmal beim Registrieren** eines Endpunkts im Software-System entstehen und
persistiert werden — mit gelegentlichem Re-Check auf Änderungen. Zielbild: die konvertierten
Ecores landen im **Model Atlas** (persistiert, versioniert, durchsuchbar), sollen aber ohne
Client-Änderung auch in anderen Ablagen (Default: EPackage-Registry des Clients) registrierbar
sein.

Ist-Zustand: `ODataClient.connect()` macht alles in einem Rutsch — `$metadata` fetchen →
CSDL→Ecore (`CsdlMetadataReader`, package-private) → Packages in ein `MetadataWhiteboard`
registrieren → `MetadataService` bauen → `List<EPackage>` halten — **bei jedem `connect`**.
Der Datenpfad (`entityType(setName)`, `EntitySetRequest`, `ODataJsonDecoder`) braucht davon nur
die **EPackages** (Set→EClass) und den **`MetadataService`** (Codec-Profil); Letzterer ist aus
den Packages **ableitbar**.

## Entscheidung

Drei Belange werden entkoppelt, hinter einer SPI im neuen Bundle
`org.eclipse.fennec.odata.schema.api` (Client hängt an der SPI, nie am Atlas):

1. **Ecores holen** — seiteneffektfrei: `ODataSchemaReader.read(scope[, conditional])`
   fetcht `$metadata` (Conditional-GET) und konvertiert, **ohne** zu registrieren.
2. **Persistieren/Finden** — `ODataSchemaRegistrar.register/remove` (Schreib-Seite) und
   `ODataSchemaResolver.lookup/version` (Lese-Seite, ISP-getrennt). Gekeyt auf `SchemaScope`
   (Service-Root-URI, normalisiert).
3. **Datenpfad** — `ODataClient.forEndpoint(scope, resolver, http)` fragt nur `resolver.lookup`
   und macht **keinen** `$metadata`-Fetch mehr.

### Getroffene Festlegungen (Design-Forks)

- **Eigenes SPI-Bundle** `…schema.api` (nur Interfaces/Records, keine Logik).
- **Modell-Identität = OData-Namespace** (nsURI semantisch aus dem Namespace abgeleitet,
  **nicht** aus der Endpunkt-URL). Multi-Schema pro Endpunkt ist damit automatisch kollisionsfrei
  (je Namespace ein EPackage). **Kollisionsfreiheit über verschiedene Endpunkte** liefert der
  **Registry-Scope**: Default-Impl isoliert je Endpunkt; Atlas-Impl **ein Scope je Endpunkt**.
  (Server-Umzug ⇒ URL ändert sich, Modell-Identität bleibt.)
- **Change-Detection zweistufig:** Content-Hash (SHA-256 der CSDL) ist die maßgebliche „Version";
  ETag/Last-Modified nur für den Conditional-GET beim Re-Check (`304` ⇒ kein Transfer).
- **Datenpfad-Policy:** Default **fail-fast** (fehlt das Schema ⇒ Fehler „erst registrieren"),
  **opt-in lazy** (holen+registrieren bei Bedarf) als Überladung. Das heutige `connect(...)`
  bleibt als Standalone-Convenience = lazy.
- **SAP-Auth/CSRF** ist **nicht** Teil dieses Pakets (separates, angrenzendes Thema).

### Orchestrierung

Ein `ODataSchemaManager` (Impl, nicht SPI) bindet Reader + Registrar + Resolver:
`onRegister(scope)` (holen→registrieren), `refresh(scope)` (Conditional-GET; nur bei geändertem
Hash neu registrieren → `UNCHANGED`/`UPDATED`/`NOT_FOUND`), `onDeregister(scope)`.

### Impls

- **Default (Client-Bundle):** `EPackageSchemaRegistry` — isolierte `Map<SchemaScope, ODataSchema>`
  je Client (optional Spiegelung in eine EMF-`EPackage.Registry`). Reader `HttpODataSchemaReader`
  = heutiges `CsdlMetadataReader` + `EdmToEcoreConverter`, öffentlich, liefert `ODataSchema`
  (Packages + contentHash + ETag/Last-Modified), Conditional-GET.
- **Atlas (im Atlas-Repo, downstream):** `AtlasSchemaRegistry` — Atlas-Scope je Endpunkt, jedes
  EPackage als `.ecore` in den Atlas-Storage; `lookup` lädt zurück. Hängt an `…schema.api` (binär)
  + Atlas-API; das odata-Repo hängt **nicht** am Atlas.

### OSGi

SPI `@ProviderType`; Impls als DS-Services. Client referenziert den `ODataSchemaResolver` per DS.
Default-Impl kommt out-of-the-box aus dem Client-Bundle; die Atlas-Impl übersteuert per
Service-Ranking/Target — ohne Client-Änderung.

## Konsequenzen

- **Positiv:** Fetch/Convert, Persistenz/Lookup und Datenpfad sind entkoppelt und je einzeln
  ersetzbar; Modelle werden einmalig registriert statt pro Request geholt; Atlas-Anbindung ohne
  Client-Kopplung; Change-Detection ohne unnötigen Transfer.
- **Negativ / offen:** `nsURI = OData-Namespace` dedupliziert identische Schemas über Endpunkte
  hinweg nur innerhalb desselben Scopes — bewusst so. Der Atlas-Impl (Cross-Repo) und die
  SAP-Auth/CSRF-Schicht sind eigene Arbeitspakete.

## Status der Umsetzung

**Vollständig umgesetzt** (Stand 2026-07-09): SPI-Bundle `org.eclipse.fennec.odata.schema.api`
(Reader/Registrar/Resolver/`ODataSchema`/`SchemaScope`), Impls im Client-Bundle
(`HttpODataSchemaReader` mit Conditional-GET + SHA-256-Hash, `EPackageSchemaRegistry` mit
negativem Ranking, `ODataSchemaManager`, `ODataSchemaRefresher`, `DefaultODataClientFactory`),
`ODataClient`-Split (`connect` = lazy Convenience, `forEndpoint(scope, resolver, http)` =
registry-entkoppelter Datenpfad). Getestet in `ClientSchemaRegistryTest` + e2e-itest. Offen
bleiben nur die beiden hier bereits ausgegliederten Pakete: die Atlas-Impl (downstream) und
die SAP-Auth-Schicht (der generische CSRF-Handshake ist im Client vorhanden).
