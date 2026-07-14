# Fennec OData — Feature Reference (Server & Client)

Status: 2026-07-14. This document describes the **current, implemented** capabilities of the
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
| `odata.persistence.api` | E5 | `QueryService` / `ApplyQuery` / `WriteService` / `MediaService` / `DeltaService` SPIs + `EntityRepository` data-source abstraction |
| `odata.persistence.inmemory` | E5 | Reference backend: in-memory query + `ApplyExecutor`, `FileEntityRepository`, `MemoryWriteRepository` (write + media + change journal) |
| `odata.persistence.jpa` | E5 | JPA backend: `OclToCriteriaTranslator`, `JpaApplyExecutor`, `WriteService` (Jakarta Criteria pushdown, ADR-0006) |
| `odata.runtime` | E6/E7 | `ODataServlet` catch-all + `RequestLimits` / `EntityShaper` / `ODataJson` / resource-path parser |
| `odata.schema.api` | E8 | Client schema-registry SPI: `ODataSchemaReader` / `Registrar` / `Resolver` / `ODataSchema` / `SchemaScope` (ADR-0007) |
| `odata.client` | E8 | `ODataClient`, fluent `EntitySetRequest`, `$batch`, CSRF, schema registry impl |
| `odata.metadata.tests`, `odata.itests` | — | OSGi integration tests (whiteboard chain / real HTTP end-to-end) |
| `odata.live.tests` | — | live interop suite (`liveTest`): client vs TripPin/OData demo/Northwind + mirror round-trip (see `odata-live-interop-findings.md`) |
| `odata.example` | — | Demo model + data + runnable `example.bndrun` (port 8080) + E9 acceptance scripts |

---

## Server capabilities

### Metadata & model
- **`GET /$metadata`** → CSDL XML, multi-schema, one EDM schema per EPackage. Emits
  `Core.ODataVersions="4.0 4.01"` and Capabilities annotations on the entity container
  (`ConformanceLevel=Advanced`, `BatchSupported=true`,
  `KeyAsSegmentSupported=false`, `AsynchronousRequestsSupported=true`, `ChangeTracking` with
  the backend's actual support) plus the
  Core/Capabilities `edmx:Reference`s.
- **`GET /`** → service document.
- Ecore↔EDM covers entity/complex types, properties, navigation properties, enums, single
  inheritance (base types), entity sets, composite keys, `Partner`/`eOpposite`, bound operations,
  navigation-property bindings + referential constraints, and an annotation layer covering
  constants **and rich expressions** (`<Record>`/`<Collection>`/path forms/`EnumMember`, XML and
  JSON; `<Annotations Target>` still out).
  Round-trip is XSD-validated. `$metadata` is served as CSDL XML (default) or **CSDL JSON**
  (`$format=json` / `Accept: application/json`); the client reads both forms.

### Read — system query options
All accepted options are declared in `ODataServlet.SUPPORTED_OPTIONS`; spec options that are known
but unimplemented (`$skiptoken`, `$id`, `$index`, `$schemaversion`, `$levels`) return
**501**, unknown `$x` options return **400**, custom (`$`-less) options are ignored.

| Option | Notes |
|---|---|
| `$filter` | OData expression → typed OCL IR; property paths resolved eagerly against the context EClass |
| `$orderby` | multi-key, `asc`/`desc` |
| `$top` / `$skip` | with a `$top` ceiling and server-driven paging (`@odata.nextLink`) |
| `$count` | `$count=true` inline, the `/$count` path segment (with `$filter`), and filtered/**searched** counts in expressions (`path/$count($filter=…)`, `path/$count($search=…)`) |
| `$select` | **nested `$select`** (`SelectTree`, key survives at every level) plus the collection options **`$filter`/`$search`/`$orderby`/`$top`/`$skip`/`$count`** on selected collections (nav collections against the target type, primitive collections via `$it`; everything runs BEFORE pruning, so expressions may reference projected-away properties) |
| `$expand` | nested **`$filter`/`$search`/`$orderby`/`$top`/`$skip`/`$count`** (applied to shaped copies against the target type; the inline count splices as `nav@odata.count`), **`$levels`** on self-recursive navigations (1..8 or `max`; the shaper's reference-dropping copier IS the recursion cutoff), **`nav/$ref`** reference expansion and **cast-in-expand**; nested `$select`-in-`$expand` → 501 |
| `$search` | server-side, pushed down to both backends |
| `$compute` | server-side computed properties |
| `$apply` | aggregation submodel: `groupby` (incl. `rollup` grouping sets), `aggregate` (sum/min/max/average/countdistinct/$count), `compute`, `filter`, `topcount`/`topsum`/`toppercent`+`bottom*`, `concat`, `top`/`skip`, `orderby`, `identity` (in-memory; JPA pushes groupby/aggregate/filter/compute down, rest → 501); `from`/custom aggregates/structure trafos parse → 501; combinable with `$filter`/`$orderby`/`$skip`/`$top`/`$count` (run after the pipeline) |
| `$format` | `json` (default) and `xml` (EMF XMI; Atom is deprecated in 4.01 and not emitted) |
| `$deltatoken` | follows a delta link (see **Change tracking**) |
| `odata.metadata=minimal/full/none` | via Accept/`$format`; real `none` (no context/discriminators) |
| `IEEE754Compatible=true` | via Accept/`$format`: Int64/Decimal values, `@odata.count` and `$apply` rows as strings, Content-Type echo, payload decode |
| `@name` param aliases | referenced from `$filter`/`$orderby` — including NESTED options inside `$expand`/`$select`; recursive with a depth cap |
| `divby` | maps to OCL `/` |

Filter/query expression coverage: comparison + logical + arithmetic operators (incl. unary
minus), canonical string functions (`contains`/`startswith`/`endswith`/`tolower`/`toupper`/
`trim`/`length`/`indexof`/`substring` incl. negative index/`concat`), date functions
(`year`…`second` → SQL `EXTRACT` on JPA), rounding functions (`round`/`floor`/`ceiling`,
JPA pushdown via jakarta Criteria), `in` (incl. JSON-array form `in [...]` and the empty
list), lambdas (`any`/`all`), `cast`/`isof` **including casts inside expression paths** (mid-path
and terminal), `$count` on collection paths **including filtered `$count($filter=…)`**, bound
functions in member paths, `$it`/`$this` (request-instance anchor, escapes lambda scopes), typed
literals (Date/DateTimeOffset/TimeOfDay/Guid/Duration/enum incl. **flag combinations**,
`NaN`/`INF`, `binary'…'`, JSON object/array literals). Operator/function names are
case-insensitive (4.01). Parses-but-refuses (501): `$root`/`$these`, `@Ns.Term` runtime values.
Open: geo literals (own spatial package), `case()` (4.02). ABNF acceptance state: **697
verified / 13 skips** across the three OASIS suites.

### Resource-path addressing (own parser, ADR-0005)
`Set(key)`, single/collection navigation paths (`Set(key)/nav/...`, keyed nav segments `nav(k)`),
`/$value` (primitive + enum), `/$count` on sets and navigations, derived-type **cast segments**
(`Set/Ns.Type`, `Set/Ns.Type(key)`, casts inside nav paths, max one cast per step), composite/named
key predicates (`(k1=v1,k2=v2)`, reads and entity-level writes; type-mismatched key literals → 400),
**key-as-segment** (`Set/key`, 4.01 MAY — properties win on ambiguity; also for writes and bound
functions), **key aliases** (`Set(@k)` with the value in a query parameter), inline **`/$filter(…)`
segments** (in-memory executable; keyed → 501), and `/$ref` — reads (single and collection
entity references, with `$filter`/paging on collections) as well as the reference writes.
`$crossjoin`/`$all`/`$entity` parse but have no engine yet (501).
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
body), **bound functions** (`GET Set(key)/Ns.Func(p=…)`) and **bound actions**
(`POST Set(key)/Ns.Action` with the parameters in the JSON body). 4.01 URL variants
(13.2.1/9.3–9.5): parameterless invocations WITHOUT parentheses (`GET /Func`,
`Set(key)/[Ns.]Func`), actions without a body, and unqualified default-namespace calls
(functions and actions).

### Change tracking ([OData-Protocol] 11.3, via `DeltaService` SPI)
- `Prefer: odata.track-changes` on a collection GET → `Preference-Applied` and an
  **`@odata.deltaLink`** in place of the next link on the last page. The link is
  **self-describing**: it re-encodes the defining query's `$filter`/`$search`/`$select`/`$compute`
  and `@`-aliases around an opaque `$deltatoken` — the server keeps **no per-client state**.
- `GET Set?$deltatoken=…` → a **delta payload**: upserts with their current state, deleted
  entities as **4.01 `@removed`** objects or the **4.0 `#Set/$deletedEntity`** form (negotiated
  version), plus the fresh delta link. Filter membership follows the spec: an entity that changed
  and no longer matches is reported as `@removed` with `reason="changed"`.
- An aged-out/invalid token → **410 Gone** with the refetch URL in `Location`. Appending other
  query options to a delta link → **400**; `Set/$count?$deltatoken=…` → 501 (MAY).
- Backends: BOTH ship a bounded, transaction-aware `ChangeJournal` (rolled-back `$batch` change
  sets never surface). The journals are SERVICE-LAYER: only writes that went through the SPI are
  visible (direct database changes bypass the JPA journal — documented). JPA answers
  `changesSince` with ONE pushed-down membership query (`defining filter AND key IN (touched)`).
- **Expanded defining queries** (`$expand` + `track-changes`, 4.01 clients): membership and
  member changes inside an expanded navigation report the OWNER, serialized with the FULL
  current representation of the expanded navigation (the spec-legal alternative to nested
  `nav@delta`). In-memory only (`DeltaService.supportsExpandTracking()`); 4.0 clients keep the
  preference unapplied (4.0 requires the flattened form we do not emit).
- **`PATCH Set` collection updates** ([OData-JSON] `"@context":"#$delta"` write payload):
  upserts + `@removed` deletes in one request, all-or-nothing on transactional backends.
  Not implemented (501): 4.0 flattened link objects, nested `nav@delta`, `@odata.bind` inside
  the payload, `continue-on-error`.
- **Delta paging**: `Prefer: maxpagesize` pages the delta server-driven — a truncated window
  continues via `@odata.nextLink` (the boundary token), the final page carries the delta link.
  `GET Set/$count?$deltatoken=…` answers the number of changes.
- Not covered: nested `nav@delta` on the wire (full representations are emitted instead),
  4.0 flattened delta payloads.

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
**All four levels hold: 4.0 and 4.01, Minimal through ADVANCED** (clause re-audit 2026-07-14;
`$metadata` advertises `ConformanceLevel=Advanced`). Remaining SHOULDs/MAYs: async,
`$crossjoin`/`$all` engines, `$orderby`/`$top`/`$skip`/`$count`/`$search`/`$levels` inside
`$expand`/`$select`, nested parameter aliases.

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

**URL/format opt-ins:** `keyAsSegment()` addresses entities as `Set/key` ([OData-URL] 4.3.1);
`ieee754()` negotiates `IEEE754Compatible=true` and decodes the string-encoded Int64/Decimal
values exactly.

**`$ref` reads:** `references(keyLiteral, navigation)` returns the entity ids of a navigation
(`GET Set(key)/nav/$ref`) — single and collection form, `null` navigation for the entity's own
reference.

**Change tracking:** `trackChanges()` sends `Prefer: odata.track-changes`; the tracked page's
`deltaLink()` feeds `changes(deltaLink)` → an `ODataDelta` of upserts (typed `EObject`s with their
current state) and `Removal`s (id + reason). Both the 4.01 `@removed` and the 4.0
`#Set/$deletedEntity` deleted-entity forms decode, prefixed or 4.01 prefix-free. A `410 Gone`
(aged-out token) surfaces as an `ODataClientException` with status 410 — refetch the set.

### Operations
Unbound function imports (`function` / `functionAsEntity` / `functionAsCollection`), unbound action
imports (`action*`, `POST` with a JSON body, `204` → null), bound functions (`boundFunction*` on
`Set(key)`) and bound actions (`boundAction` / `boundActionAsEntity` / `boundActionAsCollection` —
`POST Set(key)/Ns.Action`, mirrored by the server route).

### Write
`create` (deep insert of containment children), `update` (PATCH, minimal payload — only `eIsSet`
features), `replace` (PUT), `delete`, `$ref` management (`setReference` / `addReference` /
`removeReference`), and **`updateCollection(upserts, removedIds)`** — one `#$delta` PATCH that
upserts and removes in a single, backend-transactional request. `If-Match` is passed through.
Encoding uses the same E3 codec.

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

**Server:** every conformance clause short of the `$crossjoin`/`$all` engines (the single
remaining spec SHOULD; they parse and refuse honestly). **Async** is delivery-async: execution
completes inline, the result parks behind a one-shot `application/http` status monitor.
**Delta**: no nested `nav@delta` wire form (full expanded representations instead), no 4.0
flattened delta payloads. Plus: a few 4.01 Intermediate SHOULDs (query options on nav paths);
the `ODataRequestFilter` refactor (req §5.1.1).

**Client:** the Atlas-backed schema registry impl is downstream (ADR-0007). Decoding a
`$expand=nav/$ref` response surfaces the references as empty entities (no dedicated
reference-object accessor yet).

Media entities are supported on both sides: a `HasStream` type's binary stream is served and
replaced at `GET/PUT Set(key)/$value` (server: `MediaService` SPI, in-memory reference impl;
client: `mediaRead`/`mediaWrite` over a binary transport path with the same origin/size guards).

Container singletons are supported on both sides: declared via an `EPackage` annotation
(`…/odata/singletons`, name → type), emitted as `<Singleton>` in `$metadata`, served at `GET /Me`
(the backend supplies the instance via `QueryService.singleton`), and read by the client through
`ODataClient.singleton(name)`.

Client↔server functional parity now holds across the read/query surface, resource addressing
(incl. **derived-type casts** and **composite keys**), writes (incl. `@odata.bind`,
**`Prefer: return=`** and composite-key updates), **both `$batch` wire forms**, media, singletons,
**change tracking** (track-changes preference, delta links, delta payload both wire forms) and
operations (unbound imports, bound functions and **bound actions**). Opt-in **CORS**
(`odata.cors.origin`) serves browser clients. Field-proof: `docs/odata-live-interop-findings.md`.

## References
ADR-0001 servlet transport · 0002 CSDL via EDM model · 0003 converter owns resolution ·
0004 standalone type resolution · 0005 no Olingo at runtime · 0006 JPA backend (Criteria) ·
0007 client schema registry SPI.
