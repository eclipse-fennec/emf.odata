# ADR 0002 – CSDL über das OASIS-EDM/EDMX-Modell statt direktem Konverter

| Feld        | Wert                                                        |
|-------------|-------------------------------------------------------------|
| Status      | Akzeptiert                                                  |
| Datum       | 2026-06-24                                                  |
| Betrifft    | req-doc §3.4, §9.2 (E2), Q16                                 |
| Ersetzt     | Q16-Entscheidung aus req-doc v0.7 (Variante β, direkter Konverter) |

## Kontext

In req-doc v0.7 wurde Q16 zugunsten von **Variante β** entschieden: ein direkter
Konverter `EPackage ↔ CSDL-Text` ohne EDM-EObject-Zwischenstufe. Die Begründung war,
ein EDM-Zwischenmodell zu vermeiden, das man selbst hätte bauen und pflegen müssen.

Bei der Arbeit an E1/E2 zeigte sich, dass in den Fennec Common Models bereits ein
**vollständiges, aus den OASIS-XSDs generiertes EDM/EDMX-EMF-Modell** existiert:
`org.odata.csdl.model` (`org.eclipse.fennec.models:org.odata.csdl.model:4.0.1-SNAPSHOT`),
ausgeliefert über die bereits aktivierte `fennecEMFModels`-bnd-Library. Es enthält
`edm.ecore` (nsURI `http://docs.oasis-open.org/odata/ns/edm`, ~48 EClasses:
`TEntityType`, `TComplexType`, `TEntitySet`, `TEntityContainer`, `TNavigationProperty`,
`TProperty`, `TEnumType`, `TAction`/`TFunction`, der komplette Annotation-/Expression-Zoo)
und `edmx.ecore` — mit **761 `ExtendedMetaData`-Annotationen**, d. h. dem vollständigen
XSD→XML-Mapping. Damit kann EMF eine EDM-Instanz direkt als CSDL-XML serialisieren und
CSDL-XML symmetrisch zurücklesen.

Das ändert die Q16-Rechnung: das Zwischenmodell ist nicht mehr „selbst zu bauen", sondern
existiert fertig, OASIS-konform, gepflegt und publiziert.

## Spike-Belege

Im Bundle `org.eclipse.fennec.odata.csdl` (Eclipse-bnd-Projekt, `-buildpath:
org.odata.csdl.model;version=latest`), beide Tests grün:

1. **Schreibpfad** (`CsdlSerializationSpikeTest`): eine von Hand aufgebaute EDM-Instanz
   (`EdmxRoot → TEdmx(Version 4.0) → TDataServices → SchemaType → TEntityType + Key +
   TProperty + TEntityContainer`) wird via `XMLResource.save(..., OPTION_EXTENDED_META_DATA)`
   zu strukturell spec-konformem CSDL-XML serialisiert:

   ```xml
   <edmx:Edmx xmlns:edm="…/edm" xmlns:edmx="…/edmx" Version="4.0">
     <edmx:DataServices>
       <edm:Schema Namespace="Demo">
         <edm:EntityType Name="Person">
           <edm:Key><edm:PropertyRef Name="Id"/></edm:Key>
           <edm:Property Name="Id" Nullable="false" Type="Edm.String"/>
           …
   ```

2. **Lesepfad** (`CsdlReadSpikeTest`): das echte TripPin-`$metadata`
   (`testdata/metadata-samples/trippin-v4-metadata.xml`) lädt fehlerfrei in das EDM-Modell —
   9 EntityTypes, 4 ComplexTypes, 1 EnumType, 4 Functions, 2 Actions; `Person.Key == UserName`,
   `OpenType == true`, `EntityContainer == DefaultContainer` mit EntitySet `People`.

Einzige kosmetische Abweichung im Write-Output: EMF präfigiert jedes Element mit `edm:`,
während kanonisches `$metadata` den edm-Namespace als Default führt (unpräfigierte Elemente).
Semantisch/spec-technisch identisch und von Olingo & Co. akzeptiert; bei Bedarf über eine
XML-Serialisierungs-Option (Default-Namespace ohne Präfix) steuerbar.

## Entscheidung

CSDL wird **nicht** über einen handgeschriebenen Konverter erzeugt/gelesen, sondern über
eine **Instanz des EDM/EDMX-Modells `org.odata.csdl.model`**:

- **Write:** `EPackage` + `ODataClassProfile` (aus E1) → EDM-Modell-Instanz → EMF-Serialisierung.
  CSDL-XML kommt aus der XSD-ExtendedMetaData; CSDL-JSON als eigenes JSON-Codec-Profil auf
  demselben Modell.
- **Read:** EMF lädt `$metadata` → EDM-Modell-Instanz → Mapper baut EPackage + ODataAspects.

E2 wird entsprechend von „CSDL-Konverter schreiben" zu „EPackage↔EDM-Mapper +
Serialisierungs-Konfiguration" umformuliert.

## Konsequenzen

**Gewonnen:**
- Kein handgeschriebener CSDL-Serializer/-Parser. XML-Struktur-Konformität (Elemente,
  Reihenfolge, Namespaces, edmx-Wrapper) garantiert die OASIS-XSD.
- Symmetrischer Lese-/Schreibpfad (der Client braucht ohnehin den Lesepfad) aus einem Modell.
- Der Annotation-/Expression-Zoo ist bereits modelliert — kein fehleranfälliger Handcode.
- „Validierungs-Referenz" und Produktionspfad fallen zusammen → keine Drift.
- Konsistent mit dem Fennec-Grundprinzip (alles ist EMF-Modell + Codec).

**Akzeptierte Kosten / offen:**
- Das Mapping `EPackage+Profile ↔ EDM-Instanz` bleibt zu schreiben — fällt bei β aber
  genauso an (dort als Text-Emission, hier als typisierte EObjects, besser testbar).
- **CSDL-JSON** ist nicht von der XSD abgedeckt (eigene OASIS-JSON-Repräsentation mit
  `$`-Keys) → eigenes JSON-Codec-Profil auf dem EDM-Modell (Folge-Spike).
- Exakte Client-Akzeptanz (Power BI / SAP) am echten Output prüfen; ggf. Default-Namespace
  ohne Präfix konfigurieren.
- Abhängigkeit auf ein Common-Models-Artefakt (`org.odata.csdl.model`); Versionspflege dort.

## Verworfene Alternative

**Variante β – direkter Konverter ohne EDM-Zwischenstufe (v0.7-Entscheidung).** Verworfen,
weil ihre tragende Begründung (kein EDM-Modell bauen/pflegen wollen) hinfällig ist: das
Modell existiert fertig, XSD-generiert und publiziert, und liefert die Serialisierung gratis.
Variante γ (volle EDM-Materialisierung als Pflichtformat) war nie nötig und bleibt verworfen.
