# OData E5/E6/E7 – Server-Slice: Stand und offene Punkte

Status: 2026-07-03. Bundles: `org.eclipse.fennec.odata.persistence.api`,
`…persistence.inmemory`, `…runtime`, `…itests`.

## Architektur (umgesetzt)

```
HTTP (Jetty12-Whiteboard)
  └─ ODataServlet (runtime, Catch-All /odata/*, ADR-0001)
       ├─ /$metadata  → EcoreToEdmConverter (E2), ein Schema pro registriertem EPackage
       ├─ /{Set}?$filter&$orderby&$top&$skip&$count
       │    ├─ Limits VOR dem Parsen (Länge/Nesting/$top-Ceiling)
       │    ├─ CachingODataQueryParser (E4) → typisiertes OCL-IR
       │    ├─ QueryService-SPI (persistence.api, E5)     ← Backend-agnostisch
       │    │    └─ InMemoryQueryService (OclEvaluator = In-Memory-"Translator")
       │    │         └─ EntityRepository-SPI (ZWISCHENSCHICHT: Datenquelle)
       │    │              └─ FileEntityRepository (XMI-Dateien aus konfiguriertem Verzeichnis)
       │    └─ Antwort: OData-JSON (E3-Codec pro Entity + Envelope)
       └─ Fehler: OData-Error-JSON, sanitisiert (keine Stacktraces/Klassennamen)
```

- **Persistenz-Abstraktion:** `QueryService` (Ausführung, ein Service pro Backend; JPA/Mongo
  implementieren ihn direkt mit Pushdown) getrennt von `EntityRepository` (Datenquelle für den
  In-Memory-Weg; Datei-Implementierung vorhanden, Factory-PID
  `org.eclipse.fennec.odata.repository.file`, Property `directory`).
- **Security-Posture (getestet, unit + e2e über HTTP):** kein String-Concat in Backends — einziger
  Query-Pfad ist das typisierte IR; unbekannte Properties/Funktionen sterben beim Parsen (400).
  Limits: Expression-Länge (Default 4096), Klammer-Nesting (64, Parser-Bomb-Guard, geprüft VOR
  dem Parsen), `$top`-Ceiling (1000, greift auch ohne Client-`$top`). Fehlerantworten
  JSON-escaped + gekürzt, 500 generisch. Dateizugriff ausschließlich config-gesteuert, nie aus
  Request-Input. Nur GET (405 sonst). Auth bleibt out-of-scope (req §4.5).

## Nachtrag 2026-07-03 (zweiter Ausbau)
- **✅ Single-Entity** `/{Set}({key})`: Key wird als typisierter Gleichheits-AST gebaut
  (String-Literal mit `''`-Unescape oder Zahl) — NIE als Expression geparst; Quote-Injection
  bleibt EIN Literalwert (getestet), kaputte Keys → 400, kein Treffer → 404.
- **✅ `$select`** (validiert, Key überlebt immer) und **`$expand`** (validiert; Copier-basiertes
  Shaping: expandierte Ziele werden MITkopiert → interne Referenzen, keine Server-URIs im
  Output; nicht-expandierte Non-Containment-Navs werden ausgelassen = OData-Default).
- **✅ `$apply` end-to-end**: `QueryService.executeApply` (default → 501), `ApplyExecutor`
  im In-Memory-Backend (filter/groupby(+aggregate)/compute; Rows als Maps, Grouping-Pfade
  verschachtelt, OASIS-Semantik: nulls in sum/min/max/avg ignoriert, leere Gruppe → null,
  nicht-numerische Aggregate → Fehler). Kombination mit $filter/$orderby/$count/$select/$expand
  vorerst 400.
- **✅ Content-Negotiation JSON/XML**: `$format=json|xml` + Accept-Header. XML = **EMF-XMI**
  der (geshapten) Entities — bewusst NICHT OData-Atom (seit 4.01 deprecated); `$metadata`
  bleibt CSDL-XML (CSDL-JSON = Q9). `$apply` ist JSON-only.
  **$select+XMI**: funktioniert, weil XMI sparse ist (nur `eIsSet`-Features werden
  serialisiert; deselektiert = eUnset auf der Kopie). ABER: (1) unset ist im XMI nicht von
  Default-Werten unterscheidbar — ein ladender EMF-Client bekommt für weggelassene Features
  stillschweigend Defaults; (2) deselektierte Pflicht-Features (lowerBound=1) ergeben ladbare,
  aber nicht Diagnostician-valide Instanzen. XMI-Antworten unter $select sind daher
  **Projektionen, keine validierbaren Voll-Instanzen** (Key überlebt immer → Identität intakt).

## Offene Punkte
- **@odata.id/@odata.context** sind noch nicht Entity-URL-förmig (Service-Root nötig, E3-Lücke).
- **$apply kombiniert** mit $filter/$orderby/$top (Spec: $apply zuerst, Rest danach) +
  BottomTop/Concat-Stufen; $expand nur eine Ebene (keine Nested-Expands `a($expand=b)`).
- **In-Memory-Evaluator:** null-Semantik vereinfacht (3-wertige Logik kollabiert);
  `has` = Gleichheit (keine Flag-Sets).
- **JPA-QueryService** (E5-Hauptausbau, olingo-jpa-processor als Referenz) — dank SPI additiv;
  `executeApply` dort via Criteria-GroupBy.
- **Servlet:** Whiteboard-Pattern/Kontext konfigurierbar machen; CSDL-JSON (Q9);
  Rate-Limiting/Timeouts (Container-Ebene) dokumentieren.
- Metadata-Doc: `edmx:Reference` (Core) fehlt im Multi-Schema-Pfad des Servlets
  (nutzt `toSchema`, nicht `toEdmx`).
