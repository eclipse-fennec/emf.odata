# ADR 0009 – Eingeschränkte Root beschreibt die Typ-Hülle ihrer Sets, keine Exclude-Liste

| Feld     | Wert                                                                          |
|----------|-------------------------------------------------------------------------------|
| Status   | Akzeptiert                                                                    |
| Datum    | 2026-10-07                                                                    |
| Betrifft | emf.odata#91; `ServiceModel` (`odata.model.entitysets`); [OData-CSDL] §3, [OData-Protocol] §11.1.2 |

## Kontext

Eine Service-Root wählt mit `odata.model.entitysets` aus, welche Entity Sets sie veröffentlicht.
Bis hierhin wurden aber die **Schemas** nicht eingeschränkt: Sobald eine Klasse eines Pakets ein
Set trägt, gab `$metadata` das ganze Paket aus. Auf einer öffentlichen Open-Data-Root mit zwei
Sets beschrieb `$metadata` deshalb 13 Entity-Typen, darunter das interne Modell eines
Ticketsystems mit personenbezogenen Daten (`KarteninhaberBesuch`: Name, Geburtsdatum, E-Mail;
`Feedback`). Daten waren nicht erreichbar, aber die Beschreibung der Typen war öffentlich.

Zur Wahl standen zwei Wege:

* **Exclude-Liste** (`odata.model.exclude`): Der Betreiber zählt auf, welche Typen `$metadata`
  verschweigen soll.
* **Typ-Hülle**: Die Root beschreibt genau die Typen, die von ihren veröffentlichten Sets und
  Singletons aus erreichbar sind.

## Entscheidung

**Typ-Hülle, automatisch, sobald `odata.model.entitysets` gesetzt ist. Keine Exclude-Liste.**

Die Hülle startet bei den Typen der veröffentlichten Sets und Singletons und nimmt transitiv auf:

* Basistypen und abgeleitete Typen (ein Set kann abgeleitete Instanzen liefern, ein Cast kann
  sie nennen);
* die Typen aller Properties: Complex Types, Enums;
* die Ziele aller Navigationen (`$expand` liefert deren Daten);
* Parameter- und Rückgabetypen der Operationen dieser Typen.

Nur Classifier veröffentlichter Pakete zählen. Ein Paket ohne Typ in der Hülle trägt kein
Schema bei. Operationen bleiben, wenn ihr Binding-Typ (gebunden) bzw. ihr deklarierender Typ
(ungebunden) in der Hülle liegt; mit ihnen fallen die Container-Imports weg.

Laufzeit und Metadaten folgen derselben Hülle (`ServiceModel.describes`): Ein Cast auf einen
Typ außerhalb ist so unbekannt wie ein Tippfehler, eine ungebundene Operation außerhalb ist
nicht aufrufbar (404).

Ohne `odata.model.entitysets` bleibt alles wie bisher: Dann ist jede konkrete Klasse ein Set,
und die Pakete werden ganz beschrieben.

## Begründung

* **Allowlist statt Denylist.** Eine Exclude-Liste versagt offen: Ein neuer interner Typ ist
  öffentlich, bis jemand an die Liste denkt. Genau das ist #91 passiert, nur ohne Liste. Die
  Allowlist existiert bereits (`odata.model.entitysets`); die Hülle leitet aus ihr ab, was
  beschrieben werden muss.
* **Was erreichbar ist, muss beschrieben sein.** Ein Typ, der über Navigation, Property oder
  Cast in einer Antwort auftauchen kann, darf `$metadata` nicht fehlen, sonst können Clients
  die Antworten nicht interpretieren. Eine Exclude-Liste könnte also sinnvoll ohnehin nur
  unerreichbare Typen verbergen, und die lassen sich berechnen.
* **`$metadata` beschreibt den Service, nicht das Paket.** Nach der Änderung sagt es genau, was
  die Root ausliefern kann.

## Konsequenzen

* Eine Root mit `odata.model.entitysets` verliert Typen aus `$metadata`, die vorher (ohne
  Nutzen) beschrieben waren. Clients, die solche Typen per Cast angesprochen haben, bekommen 404.
  Ein solcher Cast konnte vorher nur auf einen Typ zielen, der von keinem Set geliefert wird.
* Gleichnamige Operationen werden über den Binding-Typ unterschieden; ungebundene Operationen
  über ihren Namen. Zwei gleichnamige ungebundene Operationen, eine innerhalb und eine außerhalb
  der Hülle, bleiben beide im Schema (die Laufzeit löst ohnehin nur die innerhalb auf).
* **Nicht gelöst:** personenbezogene *Properties* auf einem veröffentlichten Typ. Die Hülle
  arbeitet auf Typ-Ebene. Für Properties bleibt der Weg über einen eigenen Veröffentlichungstyp
  in einem Publikationspaket; ein Ausschluss pro Property wäre ein eigenes Issue.
