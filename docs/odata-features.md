# Fennec OData — Feature Reference (Server & Client)

Status: 2026-07-09. This document describes the **current, implemented** capabilities of the
Fennec OData server and client. It is a capability reference, not a changelog — for the dated
implementation history and the architecture rationale see
[`odata-architecture.md`](odata-architecture.md); for the clause-by-clause conformance verdict see
[`odata-conformance-status.md`](odata-conformance-status.md); for design decisions see
[`decisions/`](decisions/).

The stack is a self-contained OData v4.01 **server and client** in the Eclipse Fennec / EMF
ecosystem: no Olingo at runtime (ADR-0005), no Jakarta REST (ADR-0001) — a catch-all servlet on the
OSGi HTTP Whiteboard on the server side, `java.net.http` on the client side. Queries are parsed by
an own ANTLR4 grammar into an OCL predicate IR (m2x `ocl.model`); `$metadata` is a direct
Ecore↔EDM conversion against the OASIS CSDL model (no intermediate EDM object model, ADR-0002).

## Bundles

| Bundle | Stage | Content |
|---|---|---|
| `odata.metadata` | E1 | `odata.ecore` aspects/profile, `ODataAspectProvider` (thin adapter, ADR-0003) |
| `odata.vocabularies` | E1 | OASIS Core/Capabilities/Validation/Measures as EPackages (CSDL read bootstrap) |
| `odata.csdl` | E2 | `OdataResolver` (EPackage + `@OData.*` → profile), Ecore↔EDM converters, `$metadata`, `CsdlXmlLoad` (XXE-hardened load path) |
| `odata.codec.json` | E3 | OData-JSON codec profile (`@odata.type`/`@odata.id`, `Edm.*` value formats) |
| `odata.query` | E4 | ANTLR4 grammar → OCL IR, `$apply` submodel, standalone type resolver (ADR-0004), `OclEvaluator` (IR reference semantics), LRU cache |
| `odata.operation.api` | E4 | `ODataOperationHandler` SPI — pluggable function/action implementations |
| `odata.persistence.api` | E5 | `QueryService` / `ApplyQuery` / `WriteService` SPIs + `EntityRepository` data-source abstraction |
| `odata.persistence.inmemory` | E5 | Reference backend: in-memory query + `ApplyExecutor`, `FileEntityRepository`, `MemoryWriteRepository` |
| `odata.persistence.jpa` | E5 | JPA backend: `OclToCriteriaTranslator`, `JpaApplyExecutor`, `WriteService` (Jakarta Criteria pushdown, ADR-0006) |
| `odata.runtime` | E6/E7 | `ODataServlet` catch-all + `RequestLimits` / `EntityShaper` / `ODataJson` / resource-path parser |
| `odata.schema.api` | E8 | Client schema-registry SPI: `ODataSchemaReader` / `Registrar` / `Resolver` / `ODataSchema` / `SchemaScope` (ADR-0007) |
| `odata.client` | E8 | `ODataClient`, fluent `EntitySetRequest`, `$batch`, CSRF, schema registry impl |
| `odata.metadata.tests`, `odata.itests` | — | OSGi integration tests (whiteboard chain / real HTTP end-to-end) |
| `odata.example` | — | Demo model + data + runnable `example.bndrun` (port 8080) + E9 acceptance scripts |

---

## Server capabilities

### Metadata & model
- **`GET /$metadata`** → CSDL XML, multi-schema, one EDM schema per EPackage. Emits
  `Core.ODataVersions="4.0 4.01"` and Capabilities annotations on the entity container
  (`ConformanceLevel`, `BatchSupported=true`, `AsynchronousRequestsSupported=false`,
  `KeyAsSegmentSupported=false`) plus the Core/Capabilities `edmx:Reference`s.
- **`GET /`** → service document.
- Ecore↔EDM covers entity/complex types, properties, navigation properties, enums, single
  inheritance (base types), entity sets, composite keys, `Partner`/`eOpposite`, bound operations,
  navigation-property bindings + referential constraints, and a constant-subset annotation layer.
  Round-trip is XSD-validated. (CSDL-JSON is a known SHOULD gap — Q9.)

### Read — system query options
All accepted options are declared in `ODataServlet.SUPPORTED_OPTIONS`; spec options that are known
but unimplemented (`$skiptoken`, `$deltatoken`, `$id`, `$index`, `$schemaversion`, `$levels`) return
**501**, unknown `$x` options return **400**, custom (`$`-less) options are ignored.

| Option | Notes |
|---|---|
| `$filter` | OData expression → typed OCL IR; property paths resolved eagerly against the context EClass |
| `$orderby` | multi-key, `asc`/`desc` |
| `$top` / `$skip` | with a `$top` ceiling and server-driven paging (`@odata.nextLink`) |
| `$count` | `$count=true` inline and the `/$count` path segment (with `$filter`) |
| `$select` | including **nested `$select`** (`SelectTree`, key survives at every level) |
| `$expand` | including **`$filter` inside `$expand`** (parsed against the target type, applied to shaped copies) |
| `$search` | server-side, pushed down to both backends |
| `$compute` | server-side computed properties |
| `$apply` | aggregation submodel: `groupby`, `aggregate` (sum/min/max/average/countdistinct/$count), `compute`, `filter`; combinable with `$filter`/`$orderby`/`$skip`/`$top`/`$count` (run after the pipeline) |
| `$format` | `json` (default) and `xml` (EMF XMI; Atom is deprecated in 4.01 and not emitted) |
| `@name` param aliases | referenced from `$filter`/`$orderby`, recursive with a depth cap |
| `divby` | maps to OCL `/` |

Filter/query expression coverage: comparison + logical + arithmetic operators, canonical string
functions (`contains`/`startswith`/`endswith`/`tolower`/`toupper`/`trim`/`length`/`indexof`/
`substring`/`concat`), date functions (`year`…`second` → SQL `EXTRACT` on JPA), `in`, lambdas
(`any`/`all`), `cast`/`isof`, `$count` on collection paths, typed literals
(Date/DateTimeOffset/TimeOfDay/Guid/Duration/enum). Operator/function names are case-insensitive
(4.01).

### Resource-path addressing (own parser, ADR-0005)
`Set(key)`, single/collection navigation paths (`Set(key)/nav/...`, keyed nav segments `nav(k)`),
`/$value` (primitive + enum), `/$count` on sets and navigations, derived-type **cast segments**
(`Set/Ns.Type`, `Set/Ns.Type(key)`, casts inside nav paths, max one cast per step), and `/$ref`.
Path length and segment count are capped before parsing.

### Write — "Updatable OData Service" (4.0 + 4.01)
- `POST Set` → **201** + `Location` / `OData-EntityId` + created entity; **deep insert** of
  containment children.
- `PATCH` (merge) / `PUT` (replace) entity → **204** (upsert → 201); property-level writes
  (replace-based, because EMF `eIsSet` cannot express "set to null" via merge).
- `DELETE` entity → **204/404**.
- `$ref`: `POST`/`PUT`/`DELETE` on `nav/$ref` (single clear + collection member via `$id`, or 4.01
  key-in-URL).
- **`@odata.bind`** in write payloads: `"nav@odata.bind"` members are extracted and validated
  before the codec sees the body, then applied as `link()` after the write (entity level only,
  below → 501).
- **ETags / If-Match**: weak ETags (SHA-256 over the serialized state) on single-entity GET;
  updates/deletes of existing entities require `If-Match` (**428/412**).
- Guards: **415** (non-JSON), **413** (`odata.max.body.size`), **400** (empty/malformed),
  **405** (wrong target / no `WriteService`), **409** (`WriteConflictException`).

### Operations (via `ODataOperationHandler` SPI)
Unbound **function imports** (`GET Name(p=…)`), unbound **action imports** (`POST Name` with a JSON
body), and **bound functions** (`GET Set(key)/Ns.Func(p=…)`). *Bound actions* (`POST
Set(key)/Ns.Action`) are not yet routed (see gaps).

### Backends (behind the persistence SPIs)
- **In-memory** (reference semantics): the `OclEvaluator` interprets the IR directly (three-valued
  null logic, lambdas, cast, `$count`, typed literals); errors are never silent (type/format errors
  → 400). `FileEntityRepository` (XMI directory) and `MemoryWriteRepository`.
- **JPA** (ADR-0006): `OclToCriteriaTranslator` turns the IR into Jakarta Criteria queries
  (comparisons incl. `IS [NOT] NULL` on navigations, logic, arithmetic, LIKE with escaping, `in`,
  path joins, lambdas → correlated `EXISTS`, `cast` → `treat()` + `TYPE() IN`, paging, separate
  COUNT). `$apply` is a single grouped Criteria query (`JpaApplyExecutor`). `$expand` and navigation
  walks materialize via batched IN-hints / LEFT fetch joins (no N+1, no lazy proxies leak). Anything
  not translatable → `UnsupportedOperationException` → **501** (never silently wrong). Differential
  tests mirror the in-memory reference against H2.

### Conformance (see `odata-conformance-status.md`)
**4.0 Minimal ✅** (incl. Updatable), **4.01 Minimal ✅**, **4.0 Intermediate ✅** (all MUSTs + all
SHOULDs), **4.01 Intermediate ✅** (all MUSTs; SHOULDs 6/7/9 partial). Advanced is not met.

### Security defaults (PID `org.eclipse.fennec.odata.servlet`)
Pre-parse limits (`$top` ceiling `odata.max.top`=1000, expression length
`odata.max.expression.length`=4096, nesting depth `odata.max.nesting.depth`=64, applied to
`$filter`/`$orderby`/`$apply` **and** `$expand`); structural injection safety (the only query path
is the typed IR — no string concatenation in backends; unknown properties/functions → 400); error
sanitization (generic 500, JSON-escaped messages, control chars stripped, 500-char cap); XXE
hardening (`CsdlXmlLoad.secureOptions()` is the single CSDL/EDMX load path); write body limit
(`odata.max.body.size`, 1 MiB); `Location`/`OData-EntityId` header control-char escaping;
`FileEntityRepository` reads only its configured directory, never influenced by request input. Auth
/ TLS are out of scope (upstream infrastructure). Tested unit (Mockito) and end-to-end (real HTTP).

---

## Client capabilities (E8)

The client works against **any** spec-conformant OData v4 service (SAP, foreign services, or this
project's own server), reading a service's `$metadata` into Ecore and decoding responses through the
**same** codec profile the server serializes with.

### Schema registry & connection (ADR-0007)
Fetch/convert, persistence/lookup and the data path are three decoupled concerns behind the
`odata.schema.api` SPI:
- **`ODataSchemaReader`** (`HttpODataSchemaReader`) — reads `$metadata` (conditional GET, `304` →
  empty) and converts to Ecore, side-effect-free; identity is the SHA-256 content hash.
- **`ODataSchemaRegistrar` + `ODataSchemaResolver`** (`EPackageSchemaRegistry`, negative service
  ranking so a persistent impl can override) — persist/lookup keyed on `SchemaScope`.
- **`ODataSchemaManager`** — orchestrates `onRegister` / `refresh` (UNCHANGED/UPDATED/NOT_FOUND) /
  `ensureRegistered` / `onDeregister`; **`ODataSchemaRefresher`** re-checks on a schedule.

Two entry points:
- `ODataClient.connect(root[, config])` — lazy/standalone: reads the schema now and builds the data
  client. Owns the `HttpClient` it creates.
- `ODataClient.forEndpoint(scope, resolver|manager, http)` — registry-decoupled data path: the
  schema is looked up (no `$metadata` fetch); a missing scope is a hard error unless the manager
  overload is used (opt-in lazy). Also exposed as `ODataClientFactory` (DS component).

Metadata wiring is decoupled like the server: a `MetadataWhiteboard` is injectable (OSGi); the
default is an isolated plain-Java instance so a remote service's packages never leak into a shared
whiteboard. The Atlas-backed registry impl is a downstream work package (out of this repo).

### Read (`EntitySetRequest`, fluent)
All server query options (`filter`/`orderBy`/`top`/`skip`/`count`/`select`/`expand`/`search`/
`compute`/`format`/`parameterAlias`/`apply`) with exact URL/percent-encoding; `list()`,
`get(keyLiteral)`, `totalCount()`, `nextPage(page)` (following `@odata.nextLink`), `listRaw()` /
`listComputed()` (`$compute` rows), `apply()` (aggregation rows as maps). Navigation addressing:
`navigateEntity` / `navigateCollection` / `propertyValue` (`/$value`) / `navigationCount`
(`/$count`). Expanded navigations decode inline into the typed `EObject` graph.

### Operations
Unbound function imports (`function` / `functionAsEntity` / `functionAsCollection`), unbound action
imports (`action*`, `POST` with a JSON body, `204` → null), and bound functions (`boundFunction*` on
`Set(key)`). Bound actions are a gap (mirrors the server).

### Write
`create` (deep insert of containment children), `update` (PATCH, minimal payload — only `eIsSet`
features), `replace` (PUT), `delete`, and `$ref` management (`setReference` / `addReference` /
`removeReference`). `If-Match` is passed through. Encoding uses the same E3 codec.

**`@odata.bind` (2026-07-09):** `create` / `update` / `replace` take an optional
`Map<String,?> bindings` that links the written entity to **already-existing** related entities —
`Map.of("category", "Category('c1')")` for a single-valued navigation, an `Iterable<String>` /
`String[]` for a collection-valued one. The encoder injects the `"nav@odata.bind"` member(s) after
the codec pass and validates that the navigation exists, is a non-containment reference (containment
children go via deep insert), and matches the target cardinality. This is the client counterpart of
the server's `@odata.bind` handling.

### `$batch`
OData v4.01 JSON `$batch` (`ODataBatch`): `read` / `create` / `update` / `delete` sub-requests with
`dependsOn` and `atomicityGroup`; `execute()` returns per-request results (`isSuccess` / `asEntity`
/ `asPage`).

### Transport & hardening (`ODataClientConfig`)
Bearer/basic auth headers, `OData-MaxVersion` negotiation, request timeout, bounded (streamed)
response read against OOM, XXE-hardened `$metadata` parse. **CSRF** handshake for SAP
(`X-CSRF-Token: Fetch` → cached token → `403 Required` → refetch-and-retry-once; session cookie
preserved). **SSRF guard**: a server-supplied absolute link (e.g. `@odata.nextLink`) to a different
origin is refused. `ODataClient` is `AutoCloseable` and closes only an `HttpClient` it owns.

---

## Known gaps / not yet

**Server:** CSDL-JSON (`$metadata` in JSON, Q9 — a Minimal SHOULD); `metadata=none` (served as
minimal — `metadata=full` is supported via Accept/`$format`); `Edm.Int64 > 2^53`
`IEEE754Compatible` string form;
Advanced conformance (async, `$delta`, deltas); a few 4.01 Intermediate SHOULDs (query options on
nav paths, some in-`$expand` options); the `ODataRequestFilter` refactor (req §5.1.1 — wrapping
`RequestLimits`/parse validation in a whiteboard filter); in-memory backend is not
subtype-polymorphic (JPA is).

**Client:** media streams (`$value` binary), delta/change-tracking. The Atlas-backed schema
registry impl is downstream (ADR-0007).

Container singletons are supported on both sides: declared via an `EPackage` annotation
(`…/odata/singletons`, name → type), emitted as `<Singleton>` in `$metadata`, served at `GET /Me`
(the backend supplies the instance via `QueryService.singleton`), and read by the client through
`ODataClient.singleton(name)`.

Client↔server functional parity now holds across the read/query surface, resource addressing
(incl. **derived-type casts**), writes (incl. `@odata.bind` and **`Prefer: return=`**), `$batch`, and
operations (unbound imports, bound functions and **bound actions**).

## References
ADR-0001 servlet transport · 0002 CSDL via EDM model · 0003 converter owns resolution ·
0004 standalone type resolution · 0005 no Olingo at runtime · 0006 JPA backend (Criteria) ·
0007 client schema registry SPI.
