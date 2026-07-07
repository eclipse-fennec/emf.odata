# ADR 0001 – Transport-Schicht: OSGi HTTP Whiteboard (Servlet) statt Jakarta REST

| Feld        | Wert                                              |
|-------------|---------------------------------------------------|
| Status      | Akzeptiert                                        |
| Datum       | 2026-06-24                                        |
| Betrifft    | req-doc §2.1, §5.1 / §5.1.1, §9.2 (E7), Q3        |
| Ersetzt     | Q3-Entscheidung aus req-doc v0.6 (Jakarta REST)   |

## Kontext

In req-doc v0.6 wurde Q3 zugunsten des **Jakarta REST Whiteboard** (Jersey-basiert,
Eclipse OSGi Technology) entschieden. Begründung damals: Wiederverwendung des
Codec-basierten MBR/MBW, ohnehin gewartetes Whiteboard, Filter+Context+Resource-Pattern
als multi-protokoll-fähiger Eintrittspunkt.

Diese Entscheidung wurde erneut geprüft, weil der OData-Eintrittspunkt architektonisch eine
**einzige Catch-All-Ressource** (`/odata/{rest:.+}`) ist, die URL, Format und Query-Optionen
**selbst** parst (Olingo-URI-Parser + eigener ANTLR4-AST, s. §3.6). Die Frage war, ob der
JAX-RS-Mehrwert in diesem Zuschnitt real ist oder ein leichtgewichtiger Servlet die ehrlichere
Wahl ist.

## Befund (objektiv)

1. **Routing/Negotiation laufen leer.** Bei einer Catch-All-Ressource, die selbst parst, sind
   JAX-RS-Routing, Path-Parameter-Binding und ein großer Teil der Content-Negotiation
   ungenutzt. OData wählt das Format über `$format`/`Accept` + Metadata-Levels in eigener Logik.

2. **Die Filter-Chain ist kein Unterscheidungsmerkmal.** Servlet-`Filter` über das HTTP
   Whiteboard leisten dasselbe wie JAX-RS-`ContainerRequestFilter`; JAX-RS bietet nur
   `@Priority`/Name-Binding als Komfort, das HTTP Whiteboard ordnet über `service.ranking`.

3. **Der Codec ist zweischichtig.** Die eigentliche Arbeit (EMF ↔ JSON/XML) steckt im
   transport-neutralen Kern `org.eclipse.fennec.codec` (~62 Java-Dateien). Die JAX-RS-Anbindung
   `org.eclipse.fennec.codec.rest` (~30 Dateien) ist ein **dünner Adapter** über diesem Kern –
   `BaseJakartaCodecMessageBodyReaderWriter` ~200 Zeilen. Der Kern ist direkt aus einem Servlet
   aufrufbar.

4. **Viel von `codec.rest` ist für OData irrelevant.** Die Schicht ist auf annotierte
   Endpoints zugeschnitten: `@EMFResourceOptions`/`@ResourceOption`/`@CodecConfig` pro
   Resource-Methode, `@Produces`-Content-Negotiation, `Codec-Options`-Header-Override via
   `ClientCodecOptionsFilter`. Nichts davon nutzt ein Catch-All-OData-Dispatcher.

5. **Servlet ist objektiv besser bei Stream-Kontrolle.** Direkter Zugriff auf
   `HttpServletRequest`/`-Response`-Streams ist für `$batch` (`multipart/mixed`, JSON-Batch) und
   große Payloads direkter als die zusätzliche JAX-RS-Lage.

6. **„Leichter" gilt nur isoliert.** Läuft Jersey im selben Runtime wegen anderer
   Fennec-Services ohnehin, spart Servlet kein Gewicht, sondern fügt ein zweites
   Programmiermodell hinzu. Für die OData-Auslieferung wird ein schlanker Servlet-Stack
   ohne Jersey-Abhängigkeit angestrebt.

## Entscheidung

Transport ist das **OSGi HTTP Whiteboard** (Jakarta Servlet). Konkret:

- Ein `ODataServlet`, registriert via `@HttpWhiteboardServletPattern("/odata/*")`.
- Eine Servlet-`Filter`-Chain (Tracing → Auth → CSRF → ODataRequestFilter → Limits), geordnet
  über `service.ranking`; jeder Filter befüllt seinen Anteil des `ODataRequestContext`, der per
  Request-Attribut weitergereicht wird.
- Marshalling ruft den **transport-neutralen Kern von `emf.codec`** direkt auf
  (`req.getInputStream()`/`resp.getOutputStream()`), nicht die `codec.rest`-MBR/MBW-Schicht.

Das Filter+Context-Pattern bleibt multi-protokoll-fähig: STA bekommt einen eigenen Servlet +
Filter-Block unter eigenem Pfad-Prefix (`/sta/*`) und teilt den protokoll-agnostischen
E6-Dispatcher.

## Konsequenzen

**Akzeptierte Kosten:**

- Eigenbau eines schlanken Servlet-Wrappers über den Codec-Kern (Marshalling, ~dünn).
- Eigenbau des Per-Request-**ResourceSet-Lifecycle + Cleanup** (vormals `CodecResourceSetFeature`
  / `CodecResourceSetCleanupFilter` / `ResourceSetProvider`) – überschaubar, aber real.
- Fehler → OData-Error-Payload selbst gemappt, statt JAX-RS-`ExceptionMapper`.
- Abweichung vom Jakarta-REST-Programmiermodell der übrigen Fennec-Services (bewusst).

**Gewonnen:** geringeres Gewicht (kein Jersey im OData-Deployment), direkte Stream-Kontrolle für
`$batch`/große Payloads, keine ungenutzte JAX-RS-Maschinerie, direkter Codec-Kern-Aufruf.

## Verworfene Alternative

**Jakarta REST Whiteboard (v0.6-Entscheidung).** Verworfen, weil der JAX-RS-Mehrwert beim
Catch-All-Zuschnitt weitgehend verdampft (Punkte 1–4) und die einzigen echten Restvorteile
(fertiger `codec.rest`-Adapter, `ExceptionMapper`-Ergonomie, Modell-Konsistenz) die schwerere
Lage nicht aufwiegen. Sollte sich der OData-Server doch im selben Deployment wie viele
JAX-RS-Services wiederfinden und Modell-Einheitlichkeit höher gewichtet werden, ist diese
Entscheidung der erste Kandidat zur Re-Evaluierung.
