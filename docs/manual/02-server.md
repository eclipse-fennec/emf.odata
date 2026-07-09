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
| `GET /odata/{Set}/$count`, `…/{nav}/$count` | count (honours `$filter`) |
| `GET /odata/{Set}(k1=v1,k2=v2)` | composite / named key predicate |
| `GET /odata/{Set}/{Ns.Type}` | derived-type cast segment |
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
| `$count` | `$count=true` inline and the `/$count` segment |
| `$select` | including **nested** `$select` |
| `$expand` | including **`$filter` inside `$expand`** (parsed against the target type) |
| `$search` | free-text, pushed to the backend |
| `$compute` | server-computed properties (`price mul 1.19 as gross`) |
| `$apply` | aggregation (see below) |
| `$format` | `json` (default), `xml` (EMF XMI) |
| `odata.metadata=full` | via `Accept`/`$format` → per-entity `@odata.type`/`@odata.id` (default `minimal`; `none` served as minimal) |
| `@name` | 4.01 parameter aliases referenced from `$filter`/`$orderby` |

**`$filter` surface:** comparison/logical/arithmetic operators (`eq ne gt ge lt le`,
`and or not`, `add sub mul div mod`, `divby`), string functions
(`contains`/`startswith`/`endswith`/`tolower`/`toupper`/`trim`/`length`/`indexof`/`substring`/`concat`),
date functions (`year`…`second`), `in`, lambdas (`any`/`all`), `cast`/`isof`, `$count` on
collection paths, and typed literals (Date/DateTimeOffset/TimeOfDay/Guid/Duration/enum).
Operator and function names are case-insensitive (4.01).

**`$apply`:**

```
GET /odata/Product?$apply=groupby((category/name),aggregate(price with sum as Total))
```

Transformations: `groupby`, `aggregate` (`sum`/`min`/`max`/`average`/`countdistinct`/`$count`),
`compute`, `filter`. It combines with `$filter`/`$orderby`/`$skip`/`$top`/`$count`, which run
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

## Backend configuration

| Setting | PID | Default |
|---|---|---|
| File repository directory | `org.eclipse.fennec.odata.repository.file` (`directory`) | — |
| JPA server-driven page size | `org.eclipse.fennec.odata.persistence.jpa` (`odata.jpa.max.page.size`) | 1000 |

See [Configuration & Security](/guides/04-configuration) for the servlet limits and
security defaults, and the [feature matrix](/guides/06-feature-matrix) for the full spec
coverage.
