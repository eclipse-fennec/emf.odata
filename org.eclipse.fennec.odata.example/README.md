# Fennec OData — Beispiel

Ein selbständiges Beispiel: das Dummy-Modell `shop.ecore` (Product/Category/Review, dynamisches
EMF — **keine Codegenerierung nötig**) mit Demo-Daten, ausgeliefert als OData-v4-Service.

## Starten

In Eclipse/bndtools: `example.bndrun` öffnen → *Resolve* → *Run OSGi*.
Headless: `./gradlew :org.eclipse.fennec.odata.example:resolve.example`, dann den Run-Export
starten — der Service lauscht auf `http://localhost:8080/odata`.

## Ausprobieren

```bash
# Service-Dokument und Metadaten (CSDL-XML)
curl 'http://localhost:8080/odata/'
curl 'http://localhost:8080/odata/$metadata'

# Collections: Filter, Sortierung, Paging, Count
curl 'http://localhost:8080/odata/Product?$filter=price%20lt%203.00&$orderby=price%20desc&$count=true'
curl 'http://localhost:8080/odata/Product?$filter=category/name%20eq%20%27Dairy%27'
curl 'http://localhost:8080/odata/Product?$filter=reviews/any(r:%20r/stars%20ge%205)'

# Einzelne Entity, Projektion, Expand
curl "http://localhost:8080/odata/Product('p2')"
curl "http://localhost:8080/odata/Product('p2')?\$select=name,price"
curl "http://localhost:8080/odata/Product?\$expand=category"

# Aggregation ($apply) mit Post-Filter auf dem Aggregat-Alias
curl 'http://localhost:8080/odata/Product?$apply=groupby((category/name),aggregate(price%20with%20sum%20as%20Total,$count%20as%20Cnt))'
curl 'http://localhost:8080/odata/Product?$apply=groupby((category/name),aggregate(price%20with%20sum%20as%20Total))&$filter=Total%20gt%203.00'

# XML statt JSON (EMF-XMI — OData-Atom ist seit 4.01 deprecated)
curl 'http://localhost:8080/odata/Product?$format=xml'

# Schreiben (das in-memory-Backend ist ein WriteService): anlegen, ändern, löschen
curl -X POST 'http://localhost:8080/odata/Product' -H 'Content-Type: application/json' \
     -d '{"id":"p99","name":"New Product","price":9.99}'          # 201 Created
curl -X PATCH "http://localhost:8080/odata/Product('p99')" -H 'Content-Type: application/json' \
     -d '{"price":7.99}'                                          # 204 No Content
curl -X DELETE "http://localhost:8080/odata/Product('p99')"       # 204 No Content

# Fehlerverhalten (Security): Injection-Versuche und Bomben sterben mit 400
curl 'http://localhost:8080/odata/Product?$filter=name%20eq%20%27a%27%20or%201=1%20--'
curl 'http://localhost:8080/odata/NoSuchSet'                      # 404
curl -X POST 'http://localhost:8080/odata/Product' -H 'Content-Type: text/plain' -d 'x'  # 415
```

## Daten aus Dateien statt aus dem Code

`ShopExampleComponent` registriert die Demo-Daten programmatisch als `EntityRepository`.
Alternativ liest das dateibasierte Repository XMI-Dateien aus einem Verzeichnis — per
Konfiguration (z.B. via Felix Configurator oder ConfigAdmin):

```json
{
  "org.eclipse.fennec.odata.repository.file~shop": {
    "directory": "/pfad/zu/den/xmi-dateien"
  }
}
```

Die Security-Limits des Servlets (Default: `$top`-Ceiling 1000, Expression-Länge 4096,
Klammer-Tiefe 64, `$batch`-Operationen 100, async-In-Flight 16) sind über die PID
`org.eclipse.fennec.odata.servlet` konfigurierbar (`odata.max.top`,
`odata.max.expression.length`, `odata.max.nesting.depth`, `odata.max.batch.operations`,
`odata.max.async.inflight`); ein Wert `<= 0` schaltet die jeweilige Schranke ab (Foot-gun).
Details: `docs/odata-architecture.md` und `docs/odata-production-readiness-gaps.md`.

## Gegen das JPA-Backend (H2) fahren

`example.bndrun` nutzt das In-Memory-Backend. Für das Produktions-Backend (JPA/H2) gibt es
`example-jpa.bndrun`: es zieht den JPA-Stack (EclipseLink + H2 + DataSource) hinzu. Die komplette
Verdrahtung ist **automatisch** und braucht keine manuelle ConfigAdmin-Einrichtung — das Bundle
`org.eclipse.fennec.odata.example.jpa` (`ShopJpaBackendComponent`) erledigt beim Start:

1. leitet aus dem `webshop`-Modell per `EntityMapper` das ORM-Mapping ab und schreibt es als
   `.eorm`-Datei in den Bundle-Datenbereich,
2. legt die beiden Factory-Configs an — H2-`DataSource` (`daanse.jdbc.datasource.h2.DataSource`)
   und `fennec.jpa.PersistenceUnit` (Modell-Auswahl über `(emf.name=webshop)`) —, die zusammen
   eine `EntityManagerFactory` materialisieren, die `JpaQueryService` bindet,
3. seedet die Demo-Daten, sobald das Backend oben ist.

Dieselben curl-Beispiele oben werden dann aus H2 beantwortet, mit vollem SQL-Pushdown für
`$filter`/`$orderby`/`$count`/`$apply`. Verifiziert (end-to-end über HTTP, headless gestartet):

```bash
curl 'http://localhost:8080/odata/Product?$filter=price%20gt%202.00&$orderby=price%20desc&$count=true'
# -> @odata.count:2, Cheese (4.50) vor Bread (2.80), Milk (1.20) herausgefiltert — SQL-seitig
curl -X POST 'http://localhost:8080/odata/Product' -H 'Content-Type: application/json' \
     -d '{"id":"p9","name":"Yogurt","price":1.99,"rating":4,"active":true}'   # -> 201, in H2 persistiert
```

Voraussetzung für JPA: `Review` trägt (wie `Product`/`Category`) ein `id`-Schlüsselattribut —
eine keylose Entität lässt sich im EclipseLink-Descriptor nicht bauen. `Product.category` ist eine
Nicht-Containment-Referenz, die der JPA-`WriteService` noch nicht bindet (Follow-up); die Demo
seedet Produkte daher flach, `Product.reviews` (Containment) reitet jedoch mit.

Dieselbe deklarative Verdrahtung ist end-to-end auch durch
`org.eclipse.fennec.odata.itests/JpaWiringIntegrationTest#httpEndToEndOverJpaBackend` abgesichert.
