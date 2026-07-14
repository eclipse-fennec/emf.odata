# Client

The client (`org.eclipse.fennec.odata.client`) consumes **any** conformant OData v4
service — SAP, a foreign service, or this project's own server — reading its `$metadata`
into Ecore and decoding responses through the same codec profile the server serializes
with. Transport is `java.net.http` (no JAX-RS client, per ADR-0001's line).

## Connecting

The convenience path reads `$metadata` now and builds a data client:

```java
try (ODataClient client = ODataClient.connect("http://localhost:8080/odata/")) {
    EClass product = client.entityType("Product");
    ODataPage page = client.entitySet("Product")
        .filter("price lt 3.00").orderBy("name asc").top(10).count()
        .list();
    for (EObject p : page.entities()) {
        System.out.println(p.eGet(product.getEStructuralFeature("name")));
    }
}
```

`connect` owns the `HttpClient` it creates and closes it in `close()`. Overloads accept an
`ODataClientConfig` (auth, version, timeout, size cap), an injected `HttpClient`, and/or a
`MetadataWhiteboard`.

## Schema registry (ADR-0007)

For a service that is registered once and consumed many times, fetch/convert,
persistence/lookup and the data path are decoupled behind the `odata.schema.api` SPI:

```java
// register once (fetch $metadata → convert → persist), then re-check on a schedule
manager.onRegister(scope);                 // ODataSchemaManager
// data path: no $metadata fetch — the schema is looked up in the registry
ODataClient client = ODataClient.forEndpoint(scope, resolver, http);
```

- `ODataSchemaReader` (`HttpODataSchemaReader`) — reads `$metadata` (conditional GET, `304` → empty), identity is the SHA-256 content hash.
- `ODataSchemaRegistrar` + `ODataSchemaResolver` (`EPackageSchemaRegistry`, negative ranking so a persistent impl can override) — persist/lookup keyed on `SchemaScope`.
- `ODataSchemaManager` orchestrates `onRegister`/`refresh`/`ensureRegistered`; `ODataSchemaRefresher` re-checks on a schedule.

The default `forEndpoint` fails fast if the scope is not registered; a manager overload
registers lazily. Metadata wiring is decoupled like the server: the default is an isolated
plain-Java `MetadataService` so a remote service's packages never leak into a shared
whiteboard.

## Reading

`EntitySetRequest` is fluent and mirrors the server's query options:

```java
ODataPage page = client.entitySet("Product")
    .filter("contains(name,'Milk')").select("name", "price").expand("category")
    .top(20).count().list();

EObject one   = client.entitySet("Product").get("'p1'");
long    total = client.entitySet("Product").filter("active eq true").totalCount();
ODataPage next = client.entitySet("Product").nextPage(page);   // follows @odata.nextLink

// navigation addressing
EObject cat  = client.entitySet("Product").navigateEntity("'p1'", "category");
ODataPage rv = client.entitySet("Product").navigateCollection("'p1'", "reviews");
String name  = client.entitySet("Product").propertyValue("'p1'", "name");   // /$value
List<String> refs = client.entitySet("Product").references("'p1'", "reviews"); // /$ref → ids

// $compute / $apply as rows
List<ComputedRow> rows = client.entitySet("Product").compute("price mul 1.19 as gross").listComputed();
List<Map<String,Object>> agg = client.entitySet("Product")
    .apply("groupby((category/name),aggregate(price with sum as Total))");
```

Expanded navigations decode inline into the typed `EObject` graph. Two per-request opt-ins:
`keyAsSegment()` addresses entities as `Set/key` instead of `Set(key)`, and `ieee754()`
negotiates `IEEE754Compatible=true` so `Edm.Int64`/`Edm.Decimal` above 2^53 survive exactly.

## Change tracking

`trackChanges()` sends `Prefer: odata.track-changes`; a supporting service answers the last page
with a delta link, which `changes(deltaLink)` follows ([OData-Protocol] 11.3):

```java
EntitySetRequest request = client.entitySet("Product").trackChanges();
ODataPage tracked = request.list();                 // tracked.deltaLink() != null when applied

ODataDelta delta = request.changes(tracked.deltaLink());
delta.changed();                                    // upserts, typed EObjects (current state)
delta.removals();                                   // id + reason ("deleted" | "changed")
delta.deltaLink();                                  // follow again for the next round
```

Both wire forms decode — the 4.01 `@removed` objects and the 4.0 `#Set/$deletedEntity` context
form, prefixed or 4.01 prefix-free. A `410 Gone` (the token aged out) surfaces as an
`ODataClientException` with status 410: refetch the full set and track anew.

The write-side counterpart is `updateCollection` — one `#$delta` PATCH:

```java
client.entitySet("Product").updateCollection(
    List.of(changedOrNew),            // PATCH upserts (unknown keys create)
    List.of("Product('p2')"));        // @removed deletes, by entity id
```

## Operations

```java
Object v        = client.function("doubleOf", Map.of("x", 21));          // GET Name(...)
EObject entity  = client.functionAsEntity("featured", Map.of(), product);
Object result   = client.action("recalculate", Map.of("factor", 2));      // POST Name, body
Object bfn      = client.entitySet("Product").boundFunction("'p1'", "My.Shop.label", Map.of());
Object bact     = client.entitySet("Product")                             // POST Set(key)/Ns.Action
    .boundAction("'p1'", "My.Shop.recalc", Map.of("factor", 2));          // params in the JSON body
```

## Derived-type casts

`cast(qualifiedTypeName)` addresses a type-cast segment and decodes into the derived type:

```java
ODataPage sale = client.entitySet("Product").cast("My.Shop.DiscountedProduct").list();   // Set/Ns.Type
EObject one    = client.entitySet("Product").cast("My.Shop.DiscountedProduct").get("'p1'"); // Set/Ns.Type(key)

// composite / named keys ([OData-URL] compoundKey):
EObject line = client.entitySet("Order_Details").get(Map.of("OrderID", 10248, "ProductID", 11));
client.entitySet("Order_Details").updateByKeys(Map.of("OrderID", 10248, "ProductID", 11), patch, "*");
```

## Singletons

`singleton(name)` reads a container singleton (`GET /Me`), decoded into its declared type:

```java
EObject me = client.singleton("Me");
```

## Writing

```java
EObject p = /* an EObject of the Product EClass */;
EObject created = client.entitySet("Product").create(p);                 // POST, deep insert
client.entitySet("Product").update("'p1'", patch, "W/\"etag\"");         // PATCH (minimal)
client.entitySet("Product").replace("'p1'", full, ifMatch);              // PUT
client.entitySet("Product").delete("'p1'", ifMatch);
client.entitySet("Product").setReference("'p1'", "category", "Category('c1')");
```

**`@odata.bind`** — link the written entity to already-existing related entities:

```java
// single-valued nav → one URL; collection-valued → an Iterable/array of URLs
client.entitySet("Product").create(p, Map.of("category", "Category('c1')"));
client.entitySet("Order").create(o, Map.of("items", List.of("Product('p1')", "Product('p2')")));
```

The encoder injects the `"nav@odata.bind"` member(s) and validates that the navigation
exists, is a non-containment reference (containment children go via deep insert), and
matches the target cardinality.

**`Prefer: return=…`** controls the write response. `preferReturn("minimal")` asks the
server for `204` (no body — `create` then returns `null`); `preferReturn("representation")`
returns the written entity (`update`/`replace` then decode and return it):

```java
client.entitySet("Product").preferReturn("minimal").create(p);                 // → null
EObject back = client.entitySet("Product").preferReturn("representation")
    .update("'p1'", patch, ifMatch);                                            // → the updated entity
```

## Media entities

`mediaRead`/`mediaWrite` transfer a HasStream entity's binary stream over a dedicated binary
transport path (same origin/size guards; media bytes never pass through a string):

```java
MediaContent photo = client.entitySet("Photos").mediaRead("'p1'");     // GET  Set(key)/$value
client.entitySet("Photos").mediaWrite("'p1'", bytes, "image/png", etag); // PUT Set(key)/$value
```

## Batch

```java
ODataBatch batch = client.batch();
String g = batch.create("Product", newProduct);          // atomicity group / dependsOn supported
batch.update("Product('p1')", patch);
List<ODataBatch.Result> results = batch.execute();        // one round-trip (JSON, 4.01)

// 4.0 servers (SAP world, TripPin) only speak multipart/mixed:
List<ODataBatch.Result> r40 = client.batch().multipart()
    .read("People('russellwhyte')").read("People?$top=2").execute();
```

## Transport & hardening (`ODataClientConfig`)

Bearer/basic auth headers, `OData-MaxVersion` negotiation, request timeout, and a bounded
(streamed) response read guard against OOM. A **CSRF** handshake is available for SAP
(`X-CSRF-Token: Fetch` → cached token → `403 Required` → refetch-and-retry-once, session
cookie preserved). An **SSRF guard** refuses a server-supplied absolute link (e.g.
`@odata.nextLink`) to a different origin, and the `$metadata` parse is XXE-hardened.

See the [feature matrix](/guides/06-feature-matrix) for the full client surface and its
known gaps.
