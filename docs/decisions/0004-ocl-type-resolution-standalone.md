# ADR 0004 – OCL-Type-Resolution standalone im Query-Bundle; VA1 nicht-blockierend

| Feld     | Wert                                                                    |
|----------|-------------------------------------------------------------------------|
| Status   | Akzeptiert                                                              |
| Datum    | 2026-07-03                                                              |
| Betrifft | req-doc §3.5 (Type-Resolution), §3.6.1 (Caching), §7.1 VA1; folgt ADR-0003 |

## Kontext

req-doc §7.1 führt VA1 („`OclAspectProvider` in `emf.m2x` anlegen") als **blockierende
Cross-Repo-Vorarbeit** für E1/E4: Type-Resolver-Strukturen pro EPackage, Parsed-Expression-Cache,
Lifecycle am `MetadataWhiteboard`. Der Provider existiert upstream bis heute nicht (Stand
2026-07-03).

Befund aus dem m2x-Code: Die eigentliche Type-Resolution-Maschinerie ist dort **bereits
standalone vorhanden** — `OclExpressionParser.parse(String, EClassifier contextType)` (ocl.api)
typisiert Expressions während des AST-Aufbaus gegen einen Kontext-Typ, ganz ohne Metadata-Service.
VA1 beschreibt also keinen fehlenden Kern, sondern nur die **Cache-/Lifecycle-Hülle** darum.

Das ist exakt die Konstellation aus ADR-0003: Kernlogik (dort `OdataResolver`) eigenständig und
service-frei; der AspectProvider ist ein dünner Kompositions-Adapter.

## Entscheidung

1. **Type-Resolution für den OData-Query-Pfad (E4) ist standalone**: ein `OclTypeResolver` im
   Bundle `org.eclipse.fennec.odata.query` typisiert den vom `ODataToOclBuilder` konstruierten
   OCL-AST — `PropertyCallExp.type` aus `referredProperty.getEType()`, Ergebnistypen der
   benannten Operationen über eine Dispatch-Tabelle, Literal-Typen direkt. Kein Whiteboard,
   keine Upstream-Abhängigkeit.
2. **VA1 wird von „blockierend" auf „nicht-blockierend / Relocation-Kandidat" herabgestuft.**
   Der `OclAspectProvider` bleibt gewollt (Cache-Vorberechnung pro EPackage, LRU für
   Ad-hoc-Queries, Invalidierung beim Unregister — §3.6.1), ist aber ein reiner Adapter um den
   standalone Kern. Er kann in jedem Bundle liegen (das Whiteboard nimmt jeden Provider);
   upstream `emf.m2x` bleibt das architektonisch richtige Zuhause, der Umzug ist trivial, weil
   der Kern den Provider nicht kennt.
3. Bis der Provider existiert, cached der Query-Pfad selbst schlicht pro EPackage
   (ConcurrentHashMap-Niveau); die §3.6.1-Cache-Hierarchie kommt mit dem Adapter.

## Konsequenzen

- E4 (Type-Resolution, E4-AP-6) ist sofort umsetzbar; Phase-0-Block schrumpft um VA1.
- Die Dispatch-Tabelle der Ergebnistypen lebt im Query-Bundle und ist dieselbe Tabelle, die
  die Backend-Translatoren (E5) für Custom-Ops (`has`, `contains`, …) ohnehin brauchen.
- Risiko Doppelarbeit: falls emf.m2x später einen eigenen Typ-Resolver-Visitor für
  programmatisch gebaute ASTs anbietet, ersetzen wir unseren — API-Oberfläche bewusst klein
  halten (`OclTypeResolver.resolve(OclExpression, EClass)`).
