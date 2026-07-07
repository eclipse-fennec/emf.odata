# ADR 0005 – Kein Olingo als Laufzeit-Abhängigkeit; eigener URI-Parser und Batch-Splitter

| Feld     | Wert                                                                     |
|----------|--------------------------------------------------------------------------|
| Status   | Akzeptiert                                                               |
| Datum    | 2026-07-04                                                               |
| Betrifft | req-doc §2.4 (externe Bibliotheken), Tech-Eckpunkte („Olingo NUR für URI-Parser + Batch-Splitter"), E7-Roadmap |

## Kontext

Die Tech-Eckpunkte sahen Apache Olingo als Binärabhängigkeit für genau zwei Bausteine vor:
den **URI-Parser** (Resource-Pfade) und den **Batch-Splitter** (multipart/mixed). Alles andere
(Expression-Parsing, Codecs, Runtime) war ohnehin Eigenentwicklung.

Apache Olingo ist inzwischen **als Projekt archiviert** — es wird keine Weiterentwicklung,
keine Security-Fixes und keine Jakarta-/JDK-Anpassungen mehr geben. Eine archivierte
Bibliothek als Laufzeit-Abhängigkeit in einen neuen Stack aufzunehmen widerspricht den
eigenen Nachhaltigkeits- und Security-Zielen (req §4.5/§4.6).

## Entscheidung

1. **Olingo wird NICHT als Laufzeit-Abhängigkeit aufgenommen** — weder für den URI-Parser
   noch für den Batch-Splitter.
2. Der Olingo-Quellcode bleibt **Studien-Referenz** (Mechanik, Kantenfälle, Testideen) —
   wie bereits für den JPA-Processor praktiziert (`reference/`-Regeln beachten: studieren,
   nicht kopieren; Apache-2.0 vs. EPL-2.0).
3. **Eigener Resource-Path-Parser**: Erweiterung der bestehenden ANTLR4-Infrastruktur im
   Query-Bundle (eigene Grammatik-Regeln für `Set(key)/nav/property/$value/$count/$ref` und
   Type-Cast-Segmente) statt des heutigen handgerollten Mini-Routers im Servlet.
   **Normative Vorlagen (vendored in `reference/specs/`):** die offiziellen
   `odata-abnf-construction-rules.txt` (4.01-Grammatik, Blaupause für die ANTLR-Regeln) und
   „Part 2: URL Conventions" (Semantik der Pfad-Segmente). Die OASIS-ABNF-Testfälle
   (`odata-abnf-testcases.xml`, byte-identisch mit der Kopie im Query-Bundle; Rules
   `resourcePath` 37 + `odataRelativeUri` 154) werden — wie beim Expression-Parser — als
   Akzeptanz-Testsuite eingebunden.
4. **Eigener Batch-Splitter**: multipart/mixed-Zerlegung + JSON-Batch als Teil des künftigen
   Schreib-/Batch-Pakets (E7). Umfang überschaubar; Olingos `BatchParser` dient als
   Verhaltensreferenz.

## Konsequenzen

- Kein Fremdcode im Kernpfad; ein Repo-/Lizenz-/CVE-Risiko weniger; dafür Mehraufwand
  URI-Parser (mittel — die ABNF-Testfälle machen den Umfang messbar, Rules `resourcePath` 37
  + `odataRelativeUri` 154 Fälle) und Batch-Splitter (klein–mittel).
- Die Intermediate-Conformance-Lücken (`/$value`, `/$count`-Segment, Pfad-Navigation,
  Derived-Type-Casts) hängen jetzt an DIESEM Eigenbau statt an einer Olingo-Integration —
  Priorisierung siehe `odata-conformance-status.md`.
- req-doc §2.4 und die CLAUDE.md-Tech-Eckpunkte sind entsprechend zu aktualisieren (erledigt
  für CLAUDE.md mit diesem ADR).
