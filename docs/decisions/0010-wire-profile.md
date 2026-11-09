# ADR 0010 – Wire-Profil pro Service-Root: OData-Dialekte als Konfiguration, Semantik beim Dialekt

| Feld     | Wert                                                                          |
|----------|-------------------------------------------------------------------------------|
| Status   | Akzeptiert (alle fünf Stufen umgesetzt)                                       |
| Datum    | 2026-11-09                                                                    |
| Betrifft | emf.odata#110, #115 (Entwurf), #121 (Entscheidungsprotokoll D1–D9), #119, #125, #126, #130; Bundle `org.eclipse.fennec.odata.wire.api`, `ODataServlet`, Query-Bundle |

## Kontext

OGC SensorThings API 1.1 (STA, OGC 18-088) ist ein OData-Dialekt. emf.streaming bietet ein
STA-Modell über emf.odata an. Bestehende STA-Clients sollen damit funktionieren: FROST-Tooling,
das QGIS-Plugin, Dashboards und die OGC-Testsuiten ets-sta10/11. STA unterscheidet sich von
OData auf dem Wire, nicht in der Abfragesprache:

* Kontrollinformation `@iot.id` (der **Wert** des Keys), `@iot.selfLink`,
  `<Nav>@iot.navigationLink`, `@iot.count`, `@iot.nextLink`; kein `@odata.context`, kein
  `@odata.type`
* der Key ist keine Property im Body, ist in Abfragen aber als `id` und `@iot.id` ansprechbar
* absolute Links, ein eigenes Root-Dokument, ein eigener Fehler-Body
* der Wert einer Property unter ihrem Namen (`{"name":…}`), `$value` auf jeder Property
* JSON-wertige Properties (`properties`, `parameters`, `location`, `feature`)
* eine Wire-Property über mehrere Modell-Attribute (`phenomenonTime` als Intervall,
  `result` als Wert in einem von mehreren typisierten Attributen)

emf.odata soll dabei frei von STA-Spezifischem bleiben.

## Entscheidung

### 1. Ein `WireProfile` pro Root, per Konfiguration gewählt

Ein Profil ist ein Dienst (`@ConsumerType WireProfile`) mit der Service-Property
`odata.wire.profile=<name>`. Jede Root bindet **genau ein** Profil über die statische,
obligatorische Referenz `wireProfile`; ihre Konfiguration wählt es mit `wireProfile.target`.
Reines OData ist selbst ein Profil (`odata.wire.profile=odata`, von der Laufzeit registriert)
und der Default-Target. Eine Root ohne Target spricht also OData, Byte für Byte wie zuvor.
Passt der Target auf kein Profil, startet die Root nicht. Sie fällt nie still auf OData zurück.
Der Request-Filter der Root bekommt denselben Target, damit auch seine Fehler im Dialekt
geschrieben werden (#123). Wer denselben Datenbestand als STA und als OData anbieten will,
konfiguriert zwei Roots (#121 D1).

### 2. Was ein Profil festlegt

Die Defaults des Interfaces sind das OData-Verhalten:

| Methode | OData | STA (Beispiel) |
|---|---|---|
| `controls()` | `ControlNames.ODATA` | `@iot.*`; `null` lässt eine Kontrollinformation weg |
| `keyInBody(type)` | `true` | `false`: der Key nur als `@iot.id` |
| `absoluteLinks()` | `false` | `true`: Self-, Navigations- und Next-Links absolut (#121 D8) |
| `keyLiteral(key)` | OData-Literal | – |
| `propertyMember(property)` | `null` → `{"value":…}` | `{"name":…}` (#125) |
| `structuredRawValue()` | `false` → `$value` nur primitiv | `true`: JSON eines komplexen Werts (#130) |
| `serviceDocument(root)` | OData-Service-Dokument | STA-Root-Dokument |
| `errorBody(status, message)` | OData-Fehler | STA-Fehler |
| `mapping()` / `handler(property)` | keine Wire-Properties | `phenomenonTime`, `result` (Abschnitt 5) |

Unter `$select` verhält sich die Kontrollinformation wie Properties, so wie bei FROST
(#121 D4): `@iot.id` kommt mit dem gewählten Key, `@iot.selfLink` mit seinem eigenen Namen,
ein Navigationslink mit der gewählten Navigation. Eine Referenz (`$ref`) ist der Self-Link des
Profils, sonst `@odata.id` (#126). Der `keyValue`-Name (`@iot.id`) ist in `$filter`,
`$orderby` und `$select` ein Alias eines einfachen Keys. Das ersetzt das verworfene
`keyQueryName` (#121 D7, #119 E9).

### 3. Basis-URL absoluter Links

Die Basis-URL wird in dieser Reihenfolge bestimmt:

1. `odata.base.url` der Root
2. `X-Forwarded-Proto/Host/Prefix`, aber **nur**, wenn die Root ihnen traut
   (`odata.forwarded.trust=true`); sonst könnte jeder Client den Host aller Links setzen
3. der Request

Die Doku empfiehlt zuerst den Proxy-Schalter des HTTP-Containers (Felix Jetty), der für alle
Servlets gilt (#121 D3).

### 4. Ein Ort für die Kontrollinformation

Der request-gebundene `Wire` (Profil, Namen, Basis-URL, Wire-Sicht) ist die einzige Stelle, die
Namen und Form der Kontrollinformation kennt. Neue OData-Kontrollinformation muss ebenfalls
durch ihn laufen. Das ETag wird über die OData-Form gebildet und hängt nicht vom Wire ab.

### 5. Wire-Properties: Mechanik in emf.odata, Semantik beim Dialekt

Eine Wire-Property steht für Modell-Attribute, ihre *Parts* (#119).

* **Mapping-Modell statt Annotationen.** Das Profil liefert ein `WireMapping` (EMF,
  `https://eclipse.org/fennec/odata/wire/mapping/1.0`) mit `ClassMapping(eClass)` und
  `WireProperty(name, parts)`. Das Mapping gehört dem Profil, nicht dem Domänenmodell. Eine
  OData-Root desselben Modells sieht die Attribute unverändert, und mehrere Profile können
  dasselbe Modell verschieden zeigen.
* **Die Bedeutung liefert der Dialekt.** Für jede Property gibt das Profil einen
  `WirePropertyHandler`:
  * `write`: JSON-Skalar
  * `read`: Part-Werte
  * `compare(op, operandKind)`: Bedingung für `$filter`
  * `operandOf(function)`: Part für Funktionen und Arithmetik
  * `orderBy()`: Sortierschlüssel

  Ob ein Intervall mit `lt` am Ende verglichen wird oder welcher Part ein String-Literal
  trifft, entscheidet der STA-Handler in emf.streaming. emf.odata enthält keine Zeile davon.
* **`WireCondition` statt Query-IR.** `compare` gibt eine kleine geschlossene Algebra zurück
  (`Compare`, `IsNull`, `Not`, `And`, `Or`, `Constant`). Darin kommen nur die Parts und der
  andere Operand vor. Die Laufzeit übersetzt sie ins OCL-IR. Damit wird jede Bedingung
  garantiert an das Backend weitergegeben, und das IR bleibt intern. Eine Bedingung, die keine
  Parts nennt, gibt es nicht.
* **Was die Laufzeit macht.**
  * Sie prüft das Mapping beim Aktivieren der Root: Die Parts müssen Attribute des Typs sein,
    jede Property braucht einen Handler, Namen müssen eindeutig sein. Ein kaputtes Mapping
    lässt die Root scheitern.
  * Der Wire-Name ersetzt die Parts in Payload, `$select` (lädt die Parts), `$expand`,
    Property-Requests und `/$value`. Die Parts sind nicht adressierbar (400).
  * In Vergleichen steht die Property links, `in` wird zu `eq`.
  * Ein PATCH, der Parts leert, wird als Replace des aktuellen Stands geschrieben.
  * Der Handler meldet 400 mit `IllegalArgumentException` und 501 mit
    `UnsupportedOperationException`.
  * Zwei Wire-Properties im selben Vergleich sowie Wire-Properties in `$apply` ergeben 501.

### 6. Nicht in emf.odata

Folgendes liegt in emf.streaming:

* STA-Namen und das Profil selbst
* Inhalt des Root-Dokuments, Konformitätsklassen
* die Semantik der Wire-Properties
* `$resultFormat=dataArray`, MQTT

Abnahme ist der STA-TCK (ets-sta10/11) gegen das Pool-Beispiel von emf.streaming.

## Verworfen

* **Profilwahl mit Mehrfach-Referenz und 503 bis zum Erscheinen des Profils** (#121 D1): eine
  statische Referenz mit OData als Default ist einfacher und scheitert sichtbar.
* **`keyQueryName`** (#121 D7): ersetzt durch den Alias des `keyValue`-Namens.
* **Annotationen im Domänenmodell** (`interval`/`part`/`name`, Entwurf #115 Abschnitte 3 und 4,
  #119 E1): Sie hätten jede Root des Modells verändert und die STA-Semantik in emf.odata
  gezogen.
* **Intervall und Alternativen als Typen im Mapping-Modell** (#128): neutrale Namen für
  STA-Begriffe; zurückgenommen mit #131.
* **Das generische Fennec-Mapping** (`org.eclipse.fennec.mapping.model`, emf.util): Es
  erzeugt Werte in eine Richtung über undurchsichtiges OCL oder Funktionen. #119 braucht
  Semantik, die sich umkehren (Lesen) und an das Backend weitergeben lässt (Filter, Sortierung).
* **Das OCL-IR im SPI**: Es hätte die interne Darstellung zur API gemacht, und der
  Handler hätte beliebige Ausdrücke an Backend-Pushdown vorbei bauen können.

## Stufen

| Stufe | Inhalt | Stand |
|---|---|---|
| 1 | SPI, Kontrollnamen an einem Ort, Navigationslinks, Basis-URL, Root-Dokument, Fehler-Body (#116; PRs #122, #124, #127) | umgesetzt |
| 2 | Lesen: `@iot.id` in Bodies als Key, Verlinken; Deep Insert neuer Entities generisch in emf.odata (#117, #135) | umgesetzt |
| 3 | JSON-wertige Properties: `WireJson` und der mitgelieferte `JsonTextHandler`, keine Annotation (#118, #135) | umgesetzt |
| 4 | Wire-Properties über mehrere Attribute (#119, #128, #131, #132), `$value` strukturiert (#130) | umgesetzt; STA-Handler in emf.streaming#69–71 |
| 5 | Paging auf Navigations-Collections und innerhalb `$expand` (#120, #135) | umgesetzt |

## Konsequenzen

* Ein zweites Wire-Format ist eine Konfiguration einer Root, kein Fork.
* emf.odata testet die Mechanik mit neutralen Test-Profilen und -Handlern. Die STA-Semantik
  wird in emf.streaming gegen den TCK getestet. Der TCK kann dabei Entscheidungen des Dialekts
  korrigieren, siehe den Befund zu `result` in emf.streaming#71.
* Wer das Query-Bundle nutzt, braucht die Wire-API auf dem Klassenpfad.
* **Offen:** `$metadata` einer Root mit Wire-Properties zeigt weiter das Modell mit seinen Parts
  (STA verlangt kein `$metadata`). Ein Vergleich zweier Wire-Properties bräuchte eine
  SPI-Erweiterung (#129, geparkt: ets-sta10 prüft ihn nicht).
