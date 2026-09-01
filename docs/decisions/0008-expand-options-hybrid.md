# ADR 0008 – `$expand`-Optionen: Pushdown, wo er trägt; In-Memory als benannte Rückfallebene

| Feld     | Wert                                                                          |
|----------|-------------------------------------------------------------------------------|
| Status   | Akzeptiert                                                                    |
| Datum    | 2026-09-01                                                                    |
| Betrifft | emf.odata#64; persistence-jpa#238 (`Expand` mit Optionen), #254 (Mongo-EXPAND); [OData-URL] 5.1.2, Advanced §13.1.3/9.2+9.4–9.7 |

## Kontext

`$expand=Orders($filter=…;$orderby=…;$top=5;$skip=2;$count=true)` wird heute vollständig
**in memory** bedient: `CollectionOptions` sagt das in seinem eigenen Kontrakt („Evaluation
happens on SHAPED copies (`EntityShaper`), never on backend objects"). Das widerspricht der
stehenden Regel „Backend-Pushdown verpflichtend, kein In-Memory-Filtering, kein N+1 bei
`$expand`" — war aber unvermeidbar, solange `Query.expand` eine reine Pfadliste war.

persistence-jpa#238 hat das geschlossen: `Expand` trägt jetzt `path/filter/orderBy/top/skip/expand`,
abgesichert durch zwei getrennte Capabilities (`EXPAND_FILTER` = Prädikat auf einer ohnehin
laufenden Query, `EXPAND_PAGE` = Paging je Elternzeile, braucht eine Fensterfunktion). #254 gibt
Mongo erstmals überhaupt `EXPAND`.

Ein naiver Umbau wäre trotzdem ein Rückschritt, denn Upstream verweigert zwei Dinge bewusst:

* **D2** — `$count` innerhalb `$expand` wird gar nicht bedient und bekommt bewusst kein
  Capability-Literal (#207 verbietet einen Vorrat unbedienter Literale).
* **D3** — `orderBy` allein ist refused; es ist Selektor-Eingabe und wird nur zusammen mit
  `top`/`skip` bedient. Unter D1 gehört die Listenordnung dem Store.

Beides bedienen wir heute korrekt. Ein Alles-oder-nichts-Umbau tauschte also zwei funktionierende
Advanced-Fähigkeiten gegen zwei 501er — bei einem Conformance-Level, das wir beanspruchen.

Dazu die eigentliche Naht, Upstreams **D1**: Expand-Optionen wählen aus, **welche Proxies
aufgelöst werden**, nie welche Einträge existieren. Nach `$expand=Orders($filter=…;$top=5)` hält
`customer.getOrders()` weiterhin alle Bestellungen; fünf sind materialisiert, der Rest bleibt
Proxy. Diskriminator ist `eIsProxy()`. OData braucht das Gegenteil: die Antwort muss genau die
Treffermenge enthalten.

## Entscheidung

**Hybrid, und die Entscheidung fällt pro Navigation, nicht pro Request.**

1. **Gepusht wird, was das Backend deklariert**: `$filter` bei `EXPAND_FILTER`, `$top`/`$skip`
   bei `EXPAND_PAGE`, `$orderby` ausschließlich als Selektor-Eingabe neben `top`/`skip` (D3).
   Was das Backend nicht deklariert, bleibt beim In-Memory-Pfad — für diese eine Navigation.
2. **Teil-Pushdown ist erlaubt und erwünscht, in genau einer Richtung**: Filter gepusht +
   Paging in memory ist korrekt, weil die aufgelöste Teilmenge bereits die Treffermenge ist und
   die Store-Ordnung erhalten bleibt. Die Gegenrichtung ist verboten: Paging pushen und danach
   in memory filtern würde eine bereits gekappte Menge filtern und ein falsches Ergebnis liefern.
3. **`$count` in `$expand` bleibt bedient — und wird dabei genauer statt ungenauer.** Ist der
   Filter gepusht und *nicht* gepagt, ist die Zahl der aufgelösten Einträge exakt die
   Treffermenge, der Count also ableitbar. Upstreams „nicht ableitbar" gilt für den Fall MIT
   Paging; den bedienen wir weiter aus dem In-Memory-Pfad.
4. **`$orderby` allein** wird nie gepusht (D3) und bleibt in memory. Kein 501.
5. **Der Shaper lernt den Diskriminator, ohne aufzulösen.** Wo Optionen gepusht wurden, geht
   `EntityShaper` über `InternalEList#basicList()`/`basicIterator()` und übernimmt nur die
   Einträge mit `!eIsProxy()`. `source.eGet(reference)` (heute Zeile 79–103) würde genau die
   Proxies auflösen, die ungelöst bleiben müssen — das N+1 käme durch die Hintertür zurück und
   die Teilmengen-Information ginge verloren.
6. **Voraussetzung für (5), und sie ist erfüllt**: der Diskriminator trägt nur, wenn innerhalb
   eines Requests niemand sonst Proxies auflöst. Der Read-Pfad baut pro Query ein eigenes
   `ResourceSet` (`CommandPersistenceService`), es gibt keinen geteilten Cache über Requests.
   Upstreams Warnung „‚die wurden für dich in einer Query aufgelöst' ist ein Cache-Warm-Zustand,
   keine Garantie" ist damit für uns eingehalten — wer das ändert, bricht diese ADR.

## Konsequenzen

* Kein 501 kommt neu dazu; keine heute bediente Fähigkeit fällt weg. Das N+1 verschwindet dort,
  wo das Backend die Optionen trägt — auf Mongo erstmals überhaupt (#254).
* Es gibt zwei Pfade, die einig bleiben müssen. Der Nachweis ist deshalb **differenziell**: pro
  Options-Kombination das In-Memory-Ergebnis gegen das gepushte, inklusive der Naht (Filter
  gepusht + Paging in memory), nicht nur der beiden Endpunkte.
* `EntityQuery.expand` kann keine reine Pfadmenge bleiben; die Optionen müssen bis in die
  Persistence-Schicht reisen. Das Options-Record gehört dafür nach `odata.persistence.api` —
  `CollectionOptions` liegt heute im Runtime-Bundle, auf das die API nicht zeigen darf.
* Das Ergebnis muss zurückmelden, **welche Navigation gepusht wurde**: ohne dieses Signal kann
  der Shaper nicht entscheiden, ob er den Proxy-Diskriminator anwenden oder in memory filtern
  muss. Rät er, ist beides falsch.
* Wenn Upstream D2/D3 je aufweicht, wird aus der Rückfallebene ein weiterer Pushdown-Fall; die
  Schnittstelle ändert sich dadurch nicht.
