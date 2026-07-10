# OData E2 – CSDL-Converter: offene Arbeitspakete

Status: 2026-07-03. Bezieht sich auf `org.eclipse.fennec.odata.csdl`
(`EcoreToEdmConverter` / `EdmToEcoreConverter` / `EdmTypes`).
Architektur-Grundlage: ADR `docs/decisions/0002` (CSDL via EDM-Modell `org.odata.csdl.model`).

Dies ist ein **Backlog zur Besprechung**, keine fixierte Roadmap – Priorisierung offen.

## Erledigt (Stand 2026-06-26)
- Bidirektionaler Struktur-Kern: EntityType/ComplexType, Property, NavigationProperty, EnumType,
  Single-Inheritance via BaseType, EntitySets, Key (inkl. **Composite Key**).
- **Opposite/Partner** round-trippt (`Partner` ↔ `eOpposite`, Pass 3 in `EdmToEcoreConverter`).
- **Complex-typisierte EReferences** → CSDL `Property` (nicht NavigationProperty).
- **Abstract** wird beidseitig geschrieben/gelesen; abstrakte Entities bekommen **keinen** EntitySet.
- **XSD-Validierung** der Serialisierung gegen die vendorten OASIS-XSDs
  (`testdata/schemas/edmx.xsd` + `edm.xsd`); Converter-Output für `company.ecore` ist valide.
- Test-Modell `testdata/company.ecore`, geladen via emf.osgi `EcoreHelper`.
- **(ADR-0003 Phase 1)** Zwei-Schritt-Converter: eigenständiges Profile-Modell
  (`model/odata-profile.ecore` → `org.eclipse.fennec.odata.csdl.profile`, generiert nach
  `src-gen`), `OdataResolver` (EPackage→Profile, alle Ecore-Logik zentral) + `EcoreToEdmConverter`
  als reiner Profile→EDM-Builder. **Null Metadata-Deps.** Alle 5 csdl-Tests grün.
- **Bound Operations (AP-2):** `EOperation`→bound Function/Action (void→Action, sonst Function),
  Binding-Parameter synthetisiert; in `company.ecore` exerziert und **XSD-valide**.
- **(2026-07-03) OSGi-Integrationstest** `org.eclipse.fennec.odata.metadata.tests` (osgi-test,
  Felix, `test.bndrun`): EPackage-Service → MetadataService-Whiteboard → ODataAspectProvider →
  komponiertes Package-/Class-Profile, inkl. Unregister-Pfad. Schließt den offenen Punkt aus AP-1c.
- **(2026-07-03) AP-3 NavigationPropertyBinding + ReferentialConstraint** — siehe unten.

---

## Offene APs

### AP-1 — Zwei-Schritt-Converter mit eigenständigem `ODataClassProfile`  (ADR-0003) ✅ Phase 1 + 1b erledigt
Struktur + Annotation-Layer stehen. `ODataAnnotationConstants` (single source
`https://eclipse.org/fennec/odata` + dotted keys) und `OdataResolver` lösen jetzt `@OData.*` auf:
Namespace/Alias, OpenType, HasStream, expliziter Key (Entity ohne `iD`), Computed/Immutable,
`Type`-Override, MaxLength/Precision/Scale/DefaultValue, ContainsTarget/OnDelete, OperationKind,
Bound (unbound→Import), Composable, EntitySetPath. Getestet via `testdata/catalog.ecore`
(`OdataAnnotationResolutionTest`, XSD-valide). **Offen:** Computed/Immutable werden ins Profile
aufgelöst, aber noch NICHT als CSDL `<Annotation Term="Org.OData.Core.V1.*">` emittiert (→ AP-5);
externe/programmatische Config-Layer (3-Layer-Cascade, req §3.4) fehlen noch.

### AP-1c — Metadata-Komposition (ADR-0003 Phase 2) ✅ erledigt
`odata.ecore`: `ODataPackageProfile` bekam eine **Containment**-Ref `odataProfile` (typed `EObject`),
`ODataClassProfile` eine **Non-Containment**-Ref `odataProfile` — additiv, bestehende Felder + der
temp `metadata.ecore`-Hack unangetastet. `EObject`-Typ vermeidet eine Ecore-Cross-Dep aufs
csdl-Profile-Modell. `ODataAspectProvider.buildProfiles()` ist jetzt ein dünner Adapter: ruft
`new OdataResolver().resolve(ePackage)`, komponiert das csdl-`ODataPackageProfile` (Containment) und
hängt pro EClass eine Cross-Ref `ODataClassProfile.odataProfile` ein; `build*Aspect` → null.
Buildpath `emf.odata.metadata → org.eclipse.fennec.odata.csdl;version=project` (csdl exportiert
jetzt `…csdl` + `…csdl.profile`). Beide Bundles bauen grün; OSGi-Import-Wiring stimmt.
- ~~Offen: kein Integrationstest im Metadata-Bundle~~ → **erledigt 2026-07-03**:
  `org.eclipse.fennec.odata.metadata.tests` (osgi-test/Felix) testet die komplette
  Whiteboard-Kette. **Noch offen:** Feld-Bereinigung (redundante resolved-Felder am
  metadata-`ODataClassProfile`) — die Modell-Kopien in `org.eclipse.fennec.odata.metadata/model/`
  (`metadata.ecore`, `odata-profile.ecore` + genmodels) sind inzwischen DAUERHAFT (relative
  eSuperTypes/eTypes statt nsURI, analog emf.codec) und müssen bei Upstream-Änderungen
  synchron gehalten werden (odata-profile.* wird aus dem csdl-Bundle kopiert).

### AP-2 — Operations: Functions / Actions (+ FunctionImport / ActionImport)
Read-Pfad parst sie bereits (siehe `CsdlReadSpikeTest`), der Converter ignoriert sie komplett.

**Ansatz (2026-06-26, entschieden im Prinzip): über die EClass-Aspekte, nicht über einen neuen
Provider-Hook.** Eine *bound* Function/Action ist an einen Typ gebunden; eine `EOperation` lebt in
Ecore auf einer EClass — also 1:1. `AspectProvider.buildClassAspect(ClassMetadata)` liefert via
`getEClass().getEOperations()` bereits alles Nötige; kein eigener Operation-Walk im MetadataService
erforderlich. Im Converter analog ein heuristischer `getEOperations()`-Pass (E1-unabhängig,
Profile verfeinert später — wie bei `isID`/`lowerBound`).

**✅ Bound erledigt:** `ODataOperationProfile` im csdl-Profile-Modell; `OdataResolver` liest
`EClass.getEOperations()` (void→Action, sonst Function), `EcoreToEdmConverter` baut TFunction/
TAction mit synthetisiertem Binding-Parameter; XSD-valide in `company.ecore` getestet.

Offen:
- **Function vs. Action exakt:** Default (void→Action) steht; `@OData.Function`/`@OData.Action`
  als Override fehlt (hängt an AP-1b Annotation-Layer).
- **Unbound** Operations: Konvention `@OData.Function(bound=false)` (Binding-Parameter entfällt,
  Operation surface als Function/ActionImport im Container) — noch nicht implementiert.
- **Read-Pfad:** `EdmToEcoreConverter` ignoriert Functions/Actions weiterhin (→ EOperations).

### AP-3 — NavigationPropertyBinding + ReferentialConstraint ✅ erledigt (2026-07-03)
- **Bindings (write):** `EcoreToEdmConverter.addNavigationBindings` — jedes EntitySet bindet alle
  (deklarierten + geerbten, via `baseTypeQualifiedName`-Kette) Non-Containment-Navigations, deren
  Zieltyp ein EntitySet im Container hat. Containment-Navs bekommen bewusst kein Binding;
  Ziele ohne Set (abstrakt / fremdes Schema) werden übersprungen (→ AP-6).
- **ReferentialConstraint:** neue Annotation `OData.NavigationProperty.ReferentialConstraint`
  (Wert `"property=referencedProperty[,…]"`), Profile-Klasse `ODataReferentialConstraint`
  (Containment an `ODataNavigationProfile`), Resolver parst, Builder emittiert
  `<ReferentialConstraint>`; Read-Pfad (`EdmToEcoreConverter`) schreibt sie als `@OData`-Annotation
  auf die EReference zurück (Round-Trip-Fidelity). Getestet via `catalog.ecore`
  (`Document.ownerId`/`owner` → `Account.id`), XSD-valide.
- **Offen (bewusst):** Bindings auf Sets abgeleiteter Typen bei abstraktem Nav-Ziel sowie
  qualifizierte Binding-Pfade für derived types; Read-Pfad ignoriert Bindings (kein
  Ecore-Gegenstück, deterministisch regenerierbar).

### AP-4 — Facets: MaxLength / Precision / Scale / SRID / Unicode ✅ erledigt (2026-07-10)
Quelle = `@OData.*`-EAnnotations am Feature (AP-1-Muster). NEU: `OData.SRID` (numerisch oder
symbolisch `variable` → TVariable-Enumerator im EDM-Union-Typ) und `OData.Unicode`; Profile
(`srid`/`unicode`, Kopien in emf.odata.metadata synchron). Read-Pfad schreibt jetzt ALLE
Facetten (MaxLength/Precision/Scale/DefaultValue/SRID/Unicode) als `@OData.*`-Details zurück
— Facetten sind nicht mehr verlustbehaftet. Symbolisches `Scale="Variable"` bleibt beim
Rückschreiben lenient (annInt überspringt).

### AP-5 — Annotations (Vocabulary Terms): `EAnnotation` ↔ CSDL `Annotation` — TEILWEISE erledigt (2026-07-03)
- **✅ Write (Core-Terms):** `Computed`/`Immutable` aus dem Profile → `<Annotation
  Term="Org.OData.Core.V1.Computed/Immutable" Bool="true"/>` auf der Property; bei Nutzung wird
  automatisch `<edmx:Reference>` + `<edmx:Include Namespace="Org.OData.Core.V1" Alias="Core"/>`
  emittiert. XSD-valide, getestet via `catalog.ecore`.
- **✅ Vocabulary-Versorgung (E1/Q5):** neues Bundle `org.eclipse.fennec.odata.vocabularies` —
  OASIS Core/Capabilities/Validation/Measures V1 vendored (Quelle: docs.oasis-open.org errata03
  bzw. oasis-tcs GitHub für Validation), via CSDL-Read-Pfad zu EPackages gebootstrappt
  (`ODataVocabularies.getEPackage(ns)`, DS-Komponente registriert sie als EPackage-Services).
  Read-Pfad-Erweiterungen dafür: Schema-`Alias`→Package-Name, `TypeDefinition`→`EDataType`,
  `<Term>`→EAnnotation am EPackage (`…/odata/term/<Name>`, Details type/appliesTo/defaultValue/baseTerm).
- **✅ Generisches Mapping (Konstanten-Subset, 2026-07-03):** EAnnotation-Source
  `https://eclipse.org/fennec/odata/annotations` (Details `term → constantValue`) auf
  EPackage/EClass/EAttribute/EReference ↔ CSDL `<Annotation>` auf Schema/EntityType/ComplexType/
  Property/NavigationProperty. Profile-Klasse `ODataAnnotation` (Containments an Package-/Class-/
  Property-/NavigationProfile). Konstanten-Typisierung lexikalisch (true/false→Bool, integral→Int,
  dezimal→Decimal, sonst String); Read extrahiert alle Attribut-Konstantenformen (String/Bool/Int/
  Decimal/Float/Guid/Date/DateTimeOffset/Duration/TimeOfDay/Binary). Getestet: catalog.ecore
  Round-Trip inkl. XSD + TripPin-Read (`Trip.Concurrency` → Core.Computed).
- **✅ Rich Expressions (2026-07-10):** `<Record>`/`<Collection>`/Path-Formen (`Path`,
  `PropertyPath`, `NavigationPropertyPath`, `AnnotationPath`)/`<EnumMember>` round-trippen
  über `CsdlAnnotationExpressions` — Ecore-seitig als kompaktes [OData-CSDL-JSON]-Value-
  Encoding im Detail-String, EDM-seitig strukturell; dieselben Knoten speisen CSDL-XML und
  CSDL-JSON. Jackson bleibt OPTIONAL (ohne Jackson werden rich values wie früher übersprungen).
- **Offen:** Ausdrucks-Arten außerhalb des Subsets (`Apply`/`If`/Casts/LabeledElement/UrlRef),
  `<Annotations Target="…">`-Blöcke (external targeting) und Container-Level-Annotations;
  automatische `edmx:Reference`-Emission gibt es nur für Core.

### AP-6 — Cross-Package / Cross-Schema Typreferenzen ✅ erledigt (2026-07-10)
Read-Seite (Multi-Schema, Alias-Auflösung, Entfernen unauflösbarer Navs) seit E8
(`toEPackages(TEdmx)` + jetzt öffentliches `resolveReferences(pkg, byNamespace)` — auch vom
Vocabulary-Bootstrap genutzt). Write-Seite: `OdataResolver.typeName`/`baseType` qualifizieren
mit dem Namespace des ZIEL-Pakets (`typeNamespace()`, `@OData.Namespace`-override-aware) —
Supertypen und Navigationen über Paketgrenzen schreiben korrekt.

### AP-7 — OpenType / HasStream
`isOpenType`/`isHasStream` werden gelesen (read-path), aber nicht erzeugt/zurückgeschrieben.
Quelle = Annotation/Profile (E1).
- **Umfang:** klein (sobald Quelle steht).

### AP-8 — Singletons
Container-Singletons (read-path sieht `Me`) fehlen im Write-Pfad und beim Rückbau.
- **Umfang:** klein.

### AP-9 — Primitive-Typ-Treue (`EdmTypes`)
Widening verlustbehaftet: `EBigInteger`→`Edm.Decimal`; `Edm.Guid/Duration/TimeOfDay` nur
reverse-only → Forward-Mapping fehlt, Round-Trip nicht idempotent. Strategie + ggf. eigene
EDataTypes klären.
- **Umfang:** klein–mittel.

### AP-10 — Enum-Details: UnderlyingType / IsFlags
`EEnum` ↔ `EnumType` ohne UnderlyingType und IsFlags-Semantik.
- **Umfang:** klein.

### AP-11 — XSD-Round-Trip-Prüfung ausweiten (req-doc Q15) ✅ erledigt (2026-07-03)
`XsdRoundTripRegressionTest`: (1) vendortes `trippin-v4-metadata.xml` validiert gegen die
OASIS-XSDs, (2) voller Round-Trip Ecore→EDM→**XML**→EDM→Ecore für `company.ecore`
(Struktur-Spot-Checks: abstract, Composite Key, Vererbung, Partner/eOpposite, Containment, Enum)
und `catalog.ecore` (Annotation-Layer: Key, ReferentialConstraint, Bindings, Core-Annotations),
(3) TripPin-Read der Konstanten-Annotations. Q15 damit belegt.

---

## Vorschlag Reihenfolge (zur Diskussion)
1. **AP-1** zuerst (sobald E1 steht) — alles andere baut auf Profile-getriebenem Mapping auf.
2. Dann **AP-3** + **AP-2** (Bindings/Constraints, Operations) — nötig für reale Services.
3. **AP-4/5/6/7** (Facets, Annotations, Cross-Package, OpenType/HasStream).
4. **AP-8/9/10/11** (Singletons, Typ-Treue, Enum-Flags, Round-Trip-Tests).
