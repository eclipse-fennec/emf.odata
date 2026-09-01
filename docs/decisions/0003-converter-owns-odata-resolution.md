# ADR 0003 – Der CSDL-Converter besitzt die OData-Auflösung; Profile-Modell ist eigenständig

| Feld     | Wert                                                                 |
|----------|----------------------------------------------------------------------|
| Status   | Akzeptiert                                                           |
| Datum    | 2026-06-26                                                          |
| Betrifft | req-doc §3.4 (E1/E2), §3.3 (Composition); verfeinert ADR-0002 §65    |

## Kontext

ADR-0002 (§65) formulierte den Schreibpfad als „`EPackage` + `ODataClassProfile` (**aus E1**)
→ EDM-Modell". Das implizierte: der `ODataAspectProvider` (E1, im Bundle `emf.odata.metadata`,
am Metadata-Service-Whiteboard) löst `@OData.*`-Annotationen zum `ODataClassProfile` auf, und
der Converter (E2, Bundle `org.eclipse.fennec.odata.csdl`) konsumiert dieses Profile.

Daraus folgte eine Abhängigkeit `csdl → model.metadata` und – schlimmer – die faktische
Abhängigkeit, dass eine `EPackage → CSDL`-Konvertierung **nur mit laufendem Metadata-Service +
AspectProvider** möglich ist. Für eine reine Konvertierung (Tools, Tests, Client ohne Service)
ist das unnötige Kopplung.

Klärung der Rollen in der Diskussion:
- Die **AspectProvider-Mechanik ist Middleware** – ein Mediator/Callback, der beim
  Registrieren/Deregistrieren von Packages am Metadata-Service feuert. Sie ist **kein**
  Mit-Konverter.
- Die **Konversionslogik gehört vollständig in den Converter** und soll *full-featured* sein:
  „ganzes EPackage rein, ganzes Schema raus".
- Es gibt **keinen Teil-Konvertierungs-Fall** auf dem Schreibpfad: ein CSDL-Dokument ist immer
  das ganze Schema. Die einzige Vielfachheit ist EPackage-Composition (req §3.3) = *mehrere
  volle* Packages → mehrere `<Schema>` in einem EDMX, nicht „Teil eines Packages".

## Entscheidung

Der **Converter besitzt die gesamte OData-Logik** und ist **eigenständig** (keine Abhängigkeit
zum AspectProvider oder zum Metadata-Service). Die Konvertierung sind zwei interne Schritte:

1. **Resolve:** `EPackage` (+ optional externe Config) → `ODataClassProfile[]`
   (Auflösung von `@OData.*` + Ecore-Defaults wie `isID`→Key, `lowerBound==0`→Nullable).
2. **Build:** `profiles` → EDM/EDMX-Instanz → CSDL.

`toEdmx(EPackage)` verkettet beide; ohne gesetzte OData-Annotationen greifen reine
Ecore-Defaults (Standalone-Betrieb, wie in den Round-Trip-Tests).

**Profile-Modell-Strategie: B mit A als Komposition.**
- **(B)** Das kanonische `ODataClassProfile`-Modell ist ein **eigenständiges EMF-Modell im
  csdl-Bundle** – reine EClasses, **keine** `metadata.*`-Supertypen, **null** Metadata-Deps.
- **(A via Komposition)** Das Bundle `emf.odata.metadata` definiert einen
  `metadata.ClassProfile`-Subtyp, der das csdl-`ODataClassProfile` per **Containment-Referenz
  komponiert** (nicht per Vererbung, nicht dupliziert). `ODataAspectProvider.buildProfiles()`
  ruft den csdl-Resolver, legt das Ergebnis in diesen Wrapper und übergibt es dem
  Metadata-Service (für Query/$filter/OCL-Type-Resolution). Die granularen
  `build*Aspect`-Hooks dürfen `null` liefern.

**Resultierende Abhängigkeitsrichtung:**
```
emf.odata.metadata  ──►  org.eclipse.fennec.odata.csdl  ──►  (nichts Metadata)
   (ODataAspectProvider:        (Profile-Modell B + Resolver + EDM-Builder)
    dünner Adapter, komponiert B)
        └─►  model.metadata.api
```
Kein Zyklus; `csdl` referenziert **nichts** aus dem Metadata-Stack. Eine einzige
Profile-*Daten*-Repräsentation (Komposition statt Parallelklasse).

**Folge-Umbauten:**
- Annotation-Key-Konstanten (`@OData.Key`, `@OData.Type`, `@OData.OpenType`, `@OData.HasStream`,
  `@OData.Property.Computed/.Immutable`, `@OData.NavigationProperty.ContainsTarget`,
  `@OData.Function/Action.*`) wandern **in den Converter** (dort sitzt die Auflösung).
- Das eigenständige Profile-Modell zieht aus `odata.ecore` (Metadata-Bundle) in ein neues
  Ecore im csdl-Bundle. In `odata.ecore` bleibt nur der komponierende `ClassProfile`-Wrapper
  (+ ggf. die Aspekt-Typen, falls weiter gebraucht).
- Operations: der Resolver liest `EClass.getEOperations()` direkt (siehe AP-2); bound
  Function/Action 1:1, `@OData.Function/Action` disambiguiert.

## Konsequenzen

**Gewonnen:**
- Converter ist standalone nutzbar (Tools/Tests/Client) – keine Laufzeit-Kopplung an den Service.
- Saubere, azyklische Dep-Richtung; `csdl` bleibt das wiederverwendbare Fundament.
- Single Source of Truth für die Profile-Daten; der Service-Pfad komponiert nur.
- Der `ODataAspectProvider` schrumpft auf einen dünnen Adapter.

**Akzeptierte Kosten / offen:**
- Ein kleiner Wrapper-Typ + Adapter-Code im Metadata-Bundle (Preis der Entkopplung).
- Modell-Migration: Profile-Ecore von `emf.odata.metadata` nach `csdl`; EMF-Codegen neu (Ecore-
  Edits liegen beim User).
- Externe-/programmatische Config-Layer (3-Layer-Cascade, req §3.4): der Resolver muss einen
  Einspeise-Punkt dafür anbieten, ohne den Service vorauszusetzen (z. B. optionaler Config-
  Parameter am Resolver).

## Verworfene Alternativen

- **Profile subtypt `metadata.ClassProfile` (Variante A pur).** Zwingt `csdl → model.metadata`
  und damit Bundle-Nähe zum AspectProvider-Interface. Verworfen zugunsten der Komposition.
- **Zwei getrennte Profile-Repräsentationen (standalone + service, ohne Komposition).**
  Duplizierte Daten/Logik. Verworfen.
- **Auflösung bleibt im AspectProvider (ADR-0002-Lesart).** Macht Konvertierung service-
  abhängig. Verworfen.

## Nachtrag 2026-07-31 (Entscheidung unverändert, Mechanik anders)

`emf.model.metadata` ist in `emf.osgi` 1.1.0 aufgegangen. Die Entscheidung — Auflösung
vollständig im Converter, Metadata-Seite nur dünner Adapter — bleibt gültig; die SPI darunter
sieht anders aus:

- `AspectProvider` (Callback pro Element + `buildProfiles`) → `MetadataHandler` mit dem einen
  Hook `onPackageRegistered(PackageMetadata)`.
- Profile als Modelltypen sind ersatzlos weg. Stattdessen `AspectEntry { typeId, content:
  EObject (containment) }`.
- Damit entfällt der Wrapper-Typ aus „Akzeptierte Kosten": `content` trägt direkt das
  standalone `csdl`-Profil, `odata.ecore` im Metadata-Bundle wurde aufgelöst. Das Bundle hat
  jetzt kein eigenes Ecore mehr — die Komposition, die die Entscheidung wollte, ist damit sogar
  billiger als geplant.
- Per-EClass-Kreuzreferenzen für O(1)-Lookup gibt es nicht mehr (`content` ist Containment,
  ein Klassen-Eintrag könnte die Klassenprofile des Paket-Profils nicht halten); Konsumenten
  greifen über die Namensindizierung im Profil zu.
