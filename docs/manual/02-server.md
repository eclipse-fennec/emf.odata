# Server

The server is a single catch-all servlet (`ODataServlet`) registered on the OSGi HTTP
Whiteboard under `/odata/*`. It turns Ecore `EPackage`s into an OData v4.01 service.

## Running

The `org.eclipse.fennec.odata.example` bundle is a runnable demo (a shop model with
programmatic demo data). Its resolved `example.bndrun` starts an HTTP server on port 8080:

```bash
# from the workspace root, after a build
./gradlew :org.eclipse.fennec.odata.example:export.example
java -jar org.eclipse.fennec.odata.example/generated/distributions/executable/example.jar
```

A minimal wiring needs three things in the OSGi runtime:

1. one or more **`EPackage` services** (your model),
2. a **`MetadataService`** (from `codec.metadata`) plus the `ODataAspectProvider`,
3. a **`QueryService`** backend (in-memory or JPA) with an `EntityRepository`/data source.

The servlet binds them via Declarative Services; the JPA backend advertises the service
property `fennec.odata.backend=jpa`.

## Endpoints

| Path | Result |
|---|---|
| `GET /odata/` | service document |
| `GET /odata/$metadata` | CSDL XML (default) or **CSDL JSON** via `$format=json` / `Accept: application/json` (multi-schema; `Core.ODataVersions` + Capabilities annotations) |
| `GET /odata/{Set}` | entity collection |
| `GET /odata/{Set}({key})` | single entity by key |
| `GET /odata/{Set}({key})/{nav}` | navigation (entity or collection) |
| `GET /odata/{Set}({key})/{prop}/$value` | raw property/enum value |
| `GET /odata/{Set}({key})[/{nav}]/$ref` | entity reference(s) — ids only |
| `GET /odata/{Set}/$count`, `…/{nav}/$count` | count (honours `$filter`) |
| `GET /odata/{Set}(k1=v1,k2=v2)` | composite / named key predicate |
| `GET /odata/{Set}/{key}` | key-as-segment (4.01 MAY; properties win on ambiguity) |
| `GET /odata/{Set}(@k)?@k=…` | key alias (value from a query parameter) |
| `GET /odata/{Set}/{Ns.Type}` | derived-type cast segment |
| `GET /odata/{Set}?$deltatoken=…` | delta link — changes since the token (see **Change tracking**) |
| `POST /odata/$batch` | batch — OData JSON (4.01) **and** multipart/mixed (4.0) |

## Query options

```
GET /odata/Product?$filter=price lt 3.00 and startswith(name,'M')
                  &$orderby=price desc,name
                  &$select=name,price&$expand=category($filter=active eq true)
                  &$top=10&$skip=20&$count=true
```

| Option | Notes |
|---|---|
| `$filter` | full expression surface → OCL IR (see below) |
| `$orderby` | multi-key, `asc`/`desc` |
| `$top` / `$skip` | with a `$top` ceiling and server-driven paging (`@odata.nextLink`) |
| `$count` | `$count=true` inline, the `/$count` segment, filtered/searched counts in expressions (`$count($filter=…)`, `$count($search=…)`) |
| `$select` | nested `$select` plus `$filter`/`$search`/`$orderby`/`$top`/`$skip`/`$count` on selected collections (nav collections against the target type, primitive collections via `$it`) |
| `$expand` | nested `$filter`/`$search`/`$orderby`/`$top`/`$skip`/`$count`, **`$levels`** on self-recursive navigations (1..8 or `max`), **`nav/$ref`** reference expansion, **cast-in-expand** `nav/Ns.Type` (nested `$select`-in-`$expand` → 501) |
| `$search` | free-text, pushed to the backend |
| `$compute` | server-computed properties (`price mul 1.19 as gross`) |
| `$apply` | aggregation (see below) |
| `$format` | `json` (default), `xml` (EMF XMI) |
| `$deltatoken` | follows a delta link (see **Change tracking** below) |
| `odata.metadata=full`/`none` | via `Accept`/`$format` → per-entity `@odata.type`/`@odata.id` resp. no context/discriminators at all (default `minimal`) |
| `IEEE754Compatible=true` | via `Accept`/`$format` → Int64/Decimal values, `@odata.count` and `$apply` rows as strings; echoed in the Content-Type |
| `@name` | 4.01 parameter aliases referenced from `$filter`/`$orderby` |

**`$filter` surface:** comparison/logical/arithmetic operators (`eq ne gt ge lt le`,
`and or not`, `add sub mul div mod`, `divby`, unary minus), string functions
(`contains`/`startswith`/`endswith`/`tolower`/`toupper`/`trim`/`length`/`indexof`/`substring`/`concat`),
date functions (`year`…`second`), rounding functions (`round`/`floor`/`ceiling`), `in` (incl. the JSON-array form), lambdas (`any`/`all`),
`cast`/`isof` (also inside expression paths), `$count` on collection paths (incl. filtered
`$count($filter=…)`), bound functions in member paths, `$it`/`$this`, and typed literals
(Date/DateTimeOffset/TimeOfDay/Guid/Duration/enum incl. flags, `NaN`/`INF`, `binary'…'`).
Operator and function names are case-insensitive (4.01).

**`$apply`:**

```
GET /odata/Product?$apply=groupby((category/name),aggregate(price with sum as Total))
```

Transformations: `groupby` (incl. `rollup` grouping sets), `aggregate`
(`sum`/`min`/`max`/`average`/`countdistinct`/`$count`), `compute`, `filter`,
`topcount`/`topsum`/`toppercent`/`bottom*`, `concat`, `top`/`skip`, `orderby`, `identity`
(in-memory; the JPA backend pushes `groupby`/`aggregate`/`filter`/`compute` down and answers 501
for the rest). It combines with `$filter`/`$orderby`/`$skip`/`$top`/`$count`, which run
*after* the pipeline (aggregate/compute aliases are in scope).

## Write (Updatable Service)

```
POST   /odata/Product                        # 201 + Location + created entity; deep insert
PATCH  /odata/Product('p1')                  # merge (204); If-Match required if it exists
PUT    /odata/Product('p1')                  # replace (204)
DELETE /odata/Product('p1')                  # 204 / 404; If-Match required
POST   /odata/Product('p1')/reviews/$ref     # add a reference (collection nav)
PUT    /odata/Product('p1')/category/$ref    # set a reference (single nav)
DELETE /odata/Product('p1')/category/$ref    # clear / remove
```

- **Deep insert** of containment children rides along in the POST body.
- **`@odata.bind`**: a `"nav@odata.bind": "Category('c1')"` member links to an existing entity; it is extracted and validated before the codec sees the body, then applied as a reference operation after the write (entity level only).
- **ETags / If-Match**: weak ETags (SHA-256 over the serialized state) on single-entity GET; updates/deletes of existing entities require `If-Match` → `428`/`412`.
- **`Prefer: return=minimal`** answers a create with `204` (headers only) and an update with `204`; **`return=representation`** returns the full entity (`201`/`200`). The honoured choice is echoed in `Preference-Applied`.
- **Composite keys**: entity-level `PATCH`/`PUT`/`DELETE` accept compound predicates (`(k1=v1,k2=v2)`, all key properties named); below-entity composite writes answer 501. Key literals whose form contradicts the key type (quoted vs. numeric) are rejected with 400.
- Guards: `415` (non-JSON), `413` (body limit), `400` (empty/malformed), `405` (wrong target / no `WriteService`), `409` (conflict).

## Batch

`POST /odata/$batch` accepts **both** wire forms and answers in kind: the OData **JSON** batch
(4.01 — `requests` array with `dependsOn` and `atomicityGroup`) and **multipart/mixed** (4.0 —
the format SAP-era clients speak; change sets map onto the same transactional atomicity
groups, `Content-ID` correlates the responses). Sub-requests dispatch through the normal
pipeline, so every feature behaves exactly as in a direct call.

## Operations

Registered via the `ODataOperationHandler` SPI: unbound **function imports**
(`GET Name(p=…)`), unbound **action imports** (`POST Name` with a JSON body), **bound
functions** (`GET Set(key)/Ns.Func(p=…)`) and **bound actions** (`POST Set(key)/Ns.Action`
with the parameters in the JSON body).

## Singletons

Container singletons ([OData-CSDL] 13.5) are declared with an `EPackage` annotation
(source `https://eclipse.org/fennec/odata/singletons`, detail `singletonName → EClass name`).
They are emitted as `<Singleton>` in `$metadata`, listed in the service document, and served at
`GET /<name>` — the backend supplies the single instance through `QueryService.singleton(type,
name)` (a backend without that capability yields 501; a missing instance is 404). A path below a
singleton (`GET /Me/nav`) walks from that instance.

## Media entities

An entity type annotated `@OData.HasStream` is a media entity ([OData-Protocol] 11.2.4): its
binary stream is served at `GET Set(key)/$value` (with its media content type) and replaced with
`PUT Set(key)/$value` (raw body, any content type, If-Match applies, body-size limit enforced).
The stream comes from the `MediaService` SPI — a type without a media backend answers 501, a
missing entity or stream 404. The in-memory backend is the reference implementation.

## Change tracking

A client that sends `Prefer: odata.track-changes` on a collection GET receives — when the
addressed type's backend implements the `DeltaService` SPI — a `Preference-Applied` header and an
`@odata.deltaLink` in place of the next link on the last page ([OData-Protocol] 11.3). The delta
link is **self-describing**: it re-encodes the defining query's `$filter`/`$search`/`$select`/
`$compute` and `@`-aliases around an opaque `$deltatoken`, so the server keeps no per-client state.

Following the link (`GET Set?$deltatoken=…`) yields a delta payload: added/changed entities with
their current state, deleted entities as 4.01 `@removed` objects (or the 4.0 `#Set/$deletedEntity`
form for a client pinned to `OData-MaxVersion: 4.0`), and a fresh delta link. Membership follows
the defining query — an entity that changed and no longer matches the filter is reported as
removed with `reason="changed"`. An aged-out token answers **410 Gone** with the refetch URL in
`Location`; appending any other query option to a delta link is a 400.

BOTH backends implement the SPI with a bounded, transaction-aware change journal (rolled-back
`$batch` change sets never surface). The journals are service-layer: only writes through the
SPI are tracked — direct database changes bypass the JPA journal. JPA answers a delta with ONE
pushed-down membership query (`defining filter AND key IN (touched keys)`).

**Expanded defining queries** (4.01 clients, in-memory backend): membership and member changes
inside an expanded navigation report the OWNER, and the delta serializes it with the FULL
current representation of the expanded navigation — the spec-legal alternative to nested
`nav@delta`. 4.0 clients keep the preference unapplied (4.0 requires flattened deltas).

**Collection updates**: `PATCH Set` with a `"@context":"#$delta"` payload applies upserts and
`@removed` deletes in one request — all-or-nothing on transactional backends. 501: 4.0 link
objects, nested `nav@delta`, `@odata.bind`, `continue-on-error`.

`Prefer: maxpagesize` pages a delta response server-driven (a truncated window continues via
`@odata.nextLink`, the final page carries the delta link); `GET Set/$count?$deltatoken=…`
answers the number of changes. `$metadata` advertises the actual support via a
`Capabilities.ChangeTracking` annotation on the container.

## Asynchronous responses

`Prefer: respond-async` ([OData-Protocol] 11.6) answers **202 Accepted** immediately with a
`Location` status monitor — the request executes on a background worker (one Java 21 virtual
thread per request). Polling the monitor returns **202** while the execution is still
running, then the result exactly once as an `application/http` message (after that: 404).
`DELETE` on the monitor cancels: a still-running execution is interrupted (best effort) and
the result is discarded. Bounded LRU parking (unclaimed monitors age out, cancelled).

## Backend configuration

| Setting | PID | Default |
|---|---|---|
| File repository directory | `org.eclipse.fennec.odata.repository.file` (`directory`) | — |
| JPA server-driven page size | `org.eclipse.fennec.odata.persistence.jpa` (`odata.jpa.max.page.size`) | 1000 |

See [Configuration & Security](/guides/04-configuration) for the servlet limits and
security defaults, and the [feature matrix](/guides/06-feature-matrix) for the full spec
coverage.
