# ADR 0011 – Entity-Id pro Service-Root: kanonische URL oder IRI-Key, Encoding immer

| Feld     | Wert                                                                          |
|----------|-------------------------------------------------------------------------------|
| Status   | Akzeptiert                                                                    |
| Datum    | 2026-11-26                                                                    |
| Betrifft | emf.odata#153 (Emission), #152 (Auflösung); `ODataServlet`, `ResponseFormatter`, `WriteDispatcher`, `DeltaDispatcher` |

## Kontext

Jede URL, die der Service mit einem Key-Prädikat schreibt, baute dieses Prädikat aus dem
ROHEN Key-Wert. Ein Key mit einem Zeichen, das kein `pchar` ist (`/`, `?`, `#`, `%`,
Leerzeichen), ergab damit eine URL, die kein Client dereferenzieren kann — in einem
RDF-abgeleiteten Modell (DCAT: der Key IST das `rdf:about`-IRI) ist das der Normalfall:

```
GET /dcat/odata/Dataset/$ref
{"@odata.id":"Dataset('http://localhost:8085/dcat/rest/datasets/air')"}
```

Gleichzeitig nannte derselbe Service drei verschiedene Entity-Ids für dieselbe Entity:
`$ref` die (unkodierte) kanonische URL, `metadata=full` den nackten Key-Wert — der Codec
kennt die Service-Root nicht und schreibt als `@odata.id` schlicht den Key, also
`"@odata.id":"p1"` auch im Shop-Modell —, `metadata=minimal` gar nichts, was „die
kanonische URL" behauptet. [OData-Protocol] §4.1 kennt aber genau EINE Entity-Id pro
Entity, und [OData-JSON] §4.5.8 verlangt `@odata.id` auch unter `minimal`, sobald die
Entity-Id nicht die kanonische URL ist.

## Entscheidung

### 1. Das Percent-Encoding gehört in die Aufrufer, nicht ins Profil

`WireProfile.keyLiteral` wählt die **Literalform** (OData-Literal vs. SensorThings). Das
Encoding schreibt die URI-Grammatik vor, gleich welche Form gewählt wurde — es liegt also
nicht beim Profil, sonst müsste es jeder Profil-Autor erneut richtig machen. Kodiert wird
der **Wert**, bevor das Literal gebaut wird (`ODataServlet.urlKeyValue`), nicht das fertige
Literal: Anführungszeichen, `=` und `,` eines zusammengesetzten Prädikats sind Struktur und
müssen roh bleiben.

* kodiert wird alles außerhalb der *unreserved*-Menge (`A–Z a–z 0–9 - . _ ~`), UTF-8-weise
* das einfache Anführungszeichen NICHT: es wird vom Literal-Builder verdoppelt
  ([OData-URL] 4.3.4), also eine Schicht UNTER der Percent-Kodierung
* nur STRING-Werte: ein Zahl-, Datums- oder GUID-Literal enthält nichts Kodierpflichtiges,
  und ein kodiertes verlöre beim Zurücklesen seinen Typ (`2026-01-01T00%3A00%3A00Z` ist
  kein `DATETIMEOFFSET`-Token mehr)
* Emitter: `entityIdOf`, `entityPathOf` (Self-/Navigation-Links, Kontext-URLs),
  `ODataServlet.keyLiteral` (Delta-Removals), `WriteDispatcher` (`Location`)

Der Key, den ein **Backend** adressiert, wird NICHT kodiert: er ist exakt das, was der
Resource-Pfad für dieselbe Entity liefert (den hat der Container schon dekodiert). Deshalb
sind `rawKeyOf` (SPI) und `urlKeyOf` (URL) im `WriteDispatcher` getrennte Methoden — vorher
eine, mit zwei Aufrufern und zwei Bedeutungen.

### 2. Die Form der Entity-Id ist eine Eigenschaft der Root (`odata.entity.id`)

Keine der beiden Formen ist vorgeschrieben: §4.1 „strongly encourages" die kanonische URL
und erlaubt zugleich jedes IRI. Welche richtig ist, hängt am Modell der Root, nicht an
OData — dieselbe Entscheidung also wie beim Wire-Profil (ADR-0010), und darum dieselbe
Stelle: die Konfiguration der Root.

| `odata.entity.id` | Entity-Id |
|---|---|
| `canonical` (Default) | die kanonische URL `Set(key)`, percent-kodiert |
| `key` | der Key-Wert selbst, wenn er ein absolutes IRI ist |

`key` greift **pro Entity**: nur bei genau einem String-Key, dessen Wert ein absolutes IRI
ist. Ein Modell, in dem nur ein Teil der Typen IRI-Keys trägt, bleibt damit konform —
§4.1 verlangt, dass eine Entity-Id ein IRI IST, und `p1` kann das nie sein. Für solche
Entities bleibt die kanonische URL die Entity-Id, und `minimal` schweigt weiter zu Recht.

### 3. Eine Entity-Id, überall dieselbe

`ResponseFormatter` setzt das `@odata.id`-Member dort, wo jeder JSON-Knoten noch seine
`EObject` kennt (expandierte Entities eingeschlossen):

* `metadata=full`: immer — der Wert des Codecs (der rohe Key) wird ersetzt
* `metadata=minimal`: genau dann, wenn die Entity-Id NICHT die kanonische URL ist
  ([OData-JSON] 4.5.8), also unter `odata.entity.id=key`
* `$ref` (`referenceJson`), `@id` einer Delta-Removal und `OData-EntityId` eines Create
  nehmen dieselbe Form

`Location` ist davon unberührt: es ist die **Edit-URL**, immer kanonisch und kodiert.
`OData-EntityId` ist die Entity-Id ([OData-Protocol] 8.3.2) — unter `canonical` dieselbe
Zeichenkette, unter `key` das IRI. Ein Wire-Profil schreibt seine eigene Kontrollinformation
und bleibt unangetastet.

### 4. Zurück: ein IRI-Ziel wird als Key-WERT aufgelöst

Was der Service als Entity-Id schreibt, bekommt er als `$ref`-Body, `@odata.bind`-Ziel und
`$id` zurück. Unter `canonical` ist das ein Resource-Pfad, und die Auflösung aus #152 ist
die Identität. Unter `key` ist es keiner: `refTargetKey` versucht deshalb ZUERST den Pfad
und fällt erst dann auf „das ganze Ziel ist der Key-Wert" zurück — nur bei `odata.entity.id=key`,
nur für ein absolutes IRI, nur wenn der Zieltyp einen einzelnen String-Key hat. Beide Formen
bleiben also adressierbar, und der Fallback existiert nur dort, wo die Root ihn selbst
provoziert hat.

## Konsequenzen

* Ein IRI-Key ergibt unter `canonical` eine korrekte, aber unlesbare URL
  (`Dataset('http%3A%2F%2F…%2Fair')`). Wer die IRIs sichtbar und dereferenzierbar halten
  will, schaltet die Root auf `key` — und zahlt mit `@odata.id` auf JEDER Entity JEDER
  Antwort, auch unter `minimal`.
* `metadata=full` liefert nicht mehr den rohen Key als `@odata.id`. Das war für jedes
  Modell falsch, nicht nur für IRI-Keys; kein Test hat den alten Wert behauptet.
* Unter `key` kann die Zuordnung Ziel → Entity nicht mehr am String abgelesen werden: ein
  IRI, das zufällig wie ein Pfad dieser Root aussieht, wird als Pfad gelesen. Dokumentiert,
  und der Grund, warum `canonical` der Default bleibt.
* `Core.DereferenceableIDs` kann unter `key` ehrlich annotiert werden — noch nicht umgesetzt.

## Alternativen

* **Encoding im `WireProfile`**: verworfen, siehe 1 — die URI-Grammatik ist keine
  Dialektfrage.
* **Nur eine Form, global**: verworfen. `key` ist für ein Modell ohne IRI-Keys nicht
  definierbar, `canonical` nimmt einem Linked-Data-Service die sprechende Id. Die Wahl
  gehört zur Root.
* **Entity-Id = IRI, Auflösung über eine Suche statt über den Key**: verworfen, solange der
  Key das IRI IST — dann ist die Auflösung ein Key-Lookup und braucht keinen zweiten Pfad.
