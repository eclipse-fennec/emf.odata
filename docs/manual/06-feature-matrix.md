# Feature Matrix

What the OData v4.01 specification defines, what Fennec OData implements, and the spec
reference for each item. Legend: **✅** supported · **◑** partial · **❌** not yet.

Spec documents (OASIS OData **v4.01**):
[Protocol](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html) (Part 1) ·
[URL Conventions](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part2-url-conventions.html) (Part 2) ·
[CSDL XML](https://docs.oasis-open.org/odata/odata-csdl-xml/v4.01/odata-csdl-xml-v4.01.html) ·
[JSON Format](https://docs.oasis-open.org/odata/odata-json-format/v4.01/odata-json-format-v4.01.html) ·
[Data Aggregation](https://docs.oasis-open.org/odata/odata-data-aggregation-ext/v4.0/odata-data-aggregation-ext-v4.0.html) (ext) ·
[ABNF](https://docs.oasis-open.org/odata/odata/v4.01/odata-abnf-construction-rules.txt).

## Metadata & CSDL

| Feature | Spec requires | Support | Reference |
|---|---|---|---|
| `$metadata` document (CSDL XML) | Service exposes an EDM metadata document | ✅ | Protocol §11.1.2; CSDL XML §3 |
| Multi-schema (one EDM schema per EPackage) | Multiple schemas per document | ✅ | CSDL XML §5 |
| Entity / complex types, properties | — | ✅ | CSDL §8–9 |
| Enumeration types | — | ✅ | CSDL §10 |
| Single inheritance (base types) | — | ✅ | CSDL §8.3 |
| Keys, composite keys | Entity types have a key | ✅ | CSDL §8.2 |
| Navigation properties, partners | — | ✅ | CSDL §8.6 |
| Navigation-property bindings + referential constraints | — | ✅ | CSDL §8.6, §13.4 |
| Bound operations in `$metadata` | — | ✅ | CSDL §12 |
| `Core.ODataVersions` + Capabilities annotations | Advertise supported versions/capabilities | ✅ | CSDL §14; Core/Capabilities vocab. |
| Vocabulary annotation layer (constants + rich `<Record>`/`<Collection>`/path forms/`EnumMember`, XML **and** JSON) | — | ✅ (`<Annotations Target>` / container-level still out) | CSDL §14 |
| CSDL **JSON** representation (`$metadata?$format=json` / Accept) | SHOULD | ✅ (server emits; client reads) | CSDL JSON |

## System query options

| Option | Spec requires | Support | Reference |
|---|---|---|---|
| `$filter` | Filter a collection | ✅ | URL Conv. §5.1.1 |
| `$orderby` | Order a collection | ✅ | URL Conv. §5.1.4 |
| `$top` / `$skip` | Paging | ✅ | URL Conv. §5.1.5 / §5.1.6 |
| `$count` (inline + `/$count`) | Count | ✅ | URL Conv. §5.1.7 |
| `$select` (nested `$select`; `$filter`/`$search`/`$orderby`/`$top`/`$skip`/`$count` on selected collections) | Project properties | ✅ (`$count` below top level → 501) | URL Conv. §5.1.3 |
| `$expand` (nested `$filter`/`$search`/`$orderby`/`$top`/`$skip`/`$count`, `$levels` on self-recursive navigations, `nav/$ref` reference expansion, cast-in-expand) | Expand related | ✅ (nested `$select`-in-`$expand` → 501) | URL Conv. §5.1.2/5.1.3 |
| `$search` | Free-text search | ✅ — folded into `$filter` as a `contains` OR-chain over the type's string properties; **matches case-sensitively** ([#40](https://github.com/eclipse-fennec/emf.odata/issues/40)) | URL Conv. §5.1.8 |
| `$compute` | Computed properties | ✅ | URL Conv. §5.1.9 |
| `$apply` | Aggregation | ✅ (see below) | Data Aggregation §3 |
| `$format` | Response format | ✅ `json` / `xml` (XMI) | Protocol §11.2.10 |
| Parameter aliases `@name` (incl. NESTED — inside `$expand`/`$select` options) | 4.01 aliases in options | ✅ | URL Conv. §5.1.1.15 |
| Case-insensitive, `$`-less option names | 4.01 | ✅ | URL Conv. §5.1 |
| Unsupported system option → 501; unknown `$x` → 400 | Reject unsupported | ✅ | Protocol §13.1.1/7 |
| `$deltatoken` (follow a delta link) | Change tracking | ✅ | Protocol §11.3 |
| `$skiptoken`/`$index`/`$schemaversion`/`$levels`/`$id` | (various) | ❌ → 501 | URL Conv. §5.1 |

## Resource path (URL conventions)

| Segment | Spec requires | Support | Reference |
|---|---|---|---|
| `Set` / `Set(key)` (incl. composite/named keys `(k1=v1,k2=v2)`) | Address sets and entities | ✅ (reads AND entity-level writes; below-entity composite writes → 501). Key literal type mismatches → 400 | URL Conv. §4.3 |
| Navigation paths `Set(key)/nav[/…]`, keyed nav | — | ✅ | URL Conv. §4.4 |
| `/$value` (primitive + enum) | Raw value | ✅ | URL Conv. §4.6 |
| `/$count` on sets and navigations | — | ✅ | URL Conv. §4.8 |
| Derived-type cast `Set/Ns.Type[(key)]` | Type-cast segments | ✅ (one cast per step) | URL Conv. §4.11 |
| Container singleton `GET /Me[/…]` | Address a singleton | ✅ (declared via EPackage annotation; backend supplies the instance) | URL Conv. §4.3; CSDL §13.5 |
| Media entity stream `GET/PUT Set(key)/$value` | HasStream types serve/replace their binary stream | ✅ (via the `MediaService` SPI; 501 without a backend) | Protocol §11.2.4/11.4.7 |
| `/$ref` | Reference to an entity | ✅ reads (single, collection with `$filter`/paging) and writes | URL Conv. §4.9 |
| Key-as-segment (`Set/key`, also for writes and bound functions) | MAY | ✅ (client opt-in `keyAsSegment()`) | URL Conv. §4.3.1 |
| Key aliases `Set(@k)` (value from a query parameter) | 4.01 | ✅ | URL Conv. §4.3.1 |
| Inline `/$filter(…)` path segment | 4.01 | ✅ (in-memory; keyed → 501) | URL Conv. §4.13 |
| `$crossjoin` / `$all` / `$entity` | MAY | ◑ parse → 501 (no engine) | URL Conv. §4.10/4.14/4.15 |

## `$filter` / expression surface

| Feature | Support | Reference |
|---|---|---|
| Comparison `eq ne gt ge lt le` | ✅ | URL Conv. §5.1.1.1 |
| Logical `and or not`; arithmetic `add sub mul div divby mod` | ✅ | §5.1.1.1 |
| `has`, `in` | ✅ | §5.1.1.1 |
| String funcs `contains startswith endswith tolower toupper trim length indexof substring concat` | ✅ | §5.1.1.5 |
| Date/time funcs `year month day hour minute second …` | ✅ (EXTRACT on JPA) | §5.1.1.7 |
| Rounding funcs `round floor ceiling` | ✅ (JPA pushdown too) | §5.1.1.8 |
| Lambda `any`/`all` | ✅ | §5.1.1.13 |
| `cast` / `isof` | ✅ | §5.1.1.10/11 |
| `$count` in a path (incl. `$count($filter=…)` / `$count($search=…)`) | ✅ (collection paths) | §5.1.1.14 |
| Typed literals Date/DateTimeOffset/TimeOfDay/Guid/Duration/enum | ✅ | ABNF |
| Case-insensitive operators/functions (4.01) | ✅ | §5.1.1 |
| `$it`/`$this` (request-instance anchor, escapes lambda scopes) | ✅ | §5.1.1.13 |
| Enum flag combinations, `NaN`/`INF`, unary minus, `binary'…'`, JSON literals (`in [...]`) | ✅ | ABNF |
| Casts in expression paths (mid-path + terminal), filtered `$count($filter=…)` | ✅ (in-memory; JPA → 501) | §5.1.1.10/14 |
| Bound functions in member paths (`nav/Ns.Func(...)`) | ✅ | §5.1.1 |
| `$root`/`$these`, `@Ns.Term` runtime values, geo literals, `case()` (4.02) | ❌ (parse → 501 resp. open) | ABNF |

## `$apply` (Data Aggregation)

| Transformation | Support | Reference |
|---|---|---|
| `groupby` (incl. grouping paths) | ✅ | Data Aggregation §3.11 |
| `aggregate` (`sum min max average countdistinct $count`) | ✅ | §3.1.2 |
| `compute` | ✅ (JPA pushdown; not after groupby) | §3.4 |
| `filter` (before/after pipeline) | ✅ | §3.5 |
| `topcount`/`topsum`/`toppercent` + `bottom*`, `concat`, `top`/`skip`, `orderby`, `identity`, `rollup` (grouping sets) | ✅ (in-memory; JPA → 501) | §3 |
| `from`, custom aggregates, `$these`, structure trafos (`nest`/`join`/`traverse`/`rolluprecursive`/…) | ◑ parse → 501 | §3/§6 |

## Data modification (Updatable Service)

| Feature | Spec requires | Support | Reference |
|---|---|---|---|
| `POST` create entity → 201 + Location/OData-EntityId | Create | ✅ | Protocol §11.4.2 |
| Deep insert (containment children) | — | ✅ | Protocol §11.4.2.2 |
| `PATCH` (merge) / `PUT` (replace) | Update | ✅ | Protocol §11.4.3 |
| Upsert | 4.01 | ✅ | Protocol §11.4.4 |
| `DELETE` | Delete | ✅ | Protocol §11.4.5 |
| Property-level writes | — | ✅ (replace-based) | Protocol §11.4.9 |
| `$ref` link / unlink (single + collection, `$id`, 4.01 key-in-URL) | Manage references | ✅ | Protocol §11.4.6 |
| `@odata.bind` (link to existing on create/update) | SHOULD | ✅ (server receives, client emits) | JSON Format §8.5; Protocol §11.4.2.1 |
| ETags / `If-Match` (weak, 428/412) | Optimistic concurrency | ✅ | Protocol §11.4.1.1 |
| `Prefer: return=minimal/representation` | SHOULD | ✅ (server honours; client requests) | Protocol §8.2.8.7 |
| `PATCH` collection update (`#$delta` payload: upserts + `@removed` deletes, all-or-nothing) | MAY | ✅ (4.0 link objects / nested `nav@delta` / `continue-on-error` → 501) | JSON Format §15.4 |
| Deep updates | SHOULD | ❌ | Protocol §11.4.3 |

## Operations

| Feature | Support | Reference |
|---|---|---|
| Unbound function import (`GET Name(...)`) | ✅ | URL Conv. §4.5; Protocol §11.5.4 |
| Unbound action import (`POST Name`) | ✅ | Protocol §11.5.4 |
| Bound function (`GET Set(key)/Ns.Func(...)`) | ✅ | Protocol §11.5.4.1 |
| Bound action (`POST Set(key)/Ns.Action`) | ✅ | Protocol §11.5.4.2 |
| Parenless parameterless calls (`GET /Func`, `Set(key)/Func`) and unqualified default-namespace calls | ✅ (4.01 13.2.1/9.3+9.5) | Protocol §11.5.4 |

## Formats, headers & versioning

| Feature | Support | Reference |
|---|---|---|
| OData-JSON payloads, `odata.metadata=minimal` (default) | ✅ | JSON Format §3 |
| `odata.metadata=full` / `none` (via Accept / `$format`) | ✅ (real `none`: no context/discriminators; ETag pinned to the minimal form) | JSON Format §3.1 |
| `OData-Version` / `OData-MaxVersion` negotiation (4.0/4.01) | ✅ | Protocol §8.1.5/8.2.6 |
| Server-driven paging `@odata.nextLink` | ✅ | Protocol §11.2.5.7 |
| `#Ns.Type` discriminator for derived types | ✅ | JSON Format §4.5.8 |
| `IEEE754Compatible=true` (Int64/Decimal as strings: values, `@odata.count`, `$apply` rows; Content-Type echo; payload decode) | ✅ (client opt-in `ieee754()`) | JSON Format §4.3/§8.1 |
| `$batch` JSON (4.01) | ✅ (server + client) | Protocol §11.7 |
| `$batch` multipart/mixed (4.0, SAP world) | ✅ (server accepts + answers multipart; client `.multipart()`) | Protocol §11.7 |
| Change tracking: `Prefer: odata.track-changes`, self-describing delta links, delta payloads (4.01 `@removed` + 4.0 `$deletedEntity`), server-driven delta paging (`Prefer: maxpagesize`), `/$count` on delta links, 410 Gone | ✅ in-memory AND JPA (`ChangeJournal`, service-layer); expanded defining queries serve FULL expanded representations (4.01, both backends) | Protocol §11.3 |
| Asynchronous requests (`Prefer: respond-async` → immediate 202 + status monitor: 202 while running, one-shot `application/http` delivery; DELETE interrupts a running execution) | ✅ (background execution on virtual threads) | Protocol §11.6 |
| CORS for browser clients (XOData & co.) | ✅ opt-in (`odata.cors.origin`: `*` or allowlist; preflight, expose headers) | (not OData — HTTP) |

## Client (E8)

| Feature | Support | Reference |
|---|---|---|
| `$metadata` → Ecore, all read query options, navigation, `$value`, `$count`, `nextPage` | ✅ | — |
| Derived-type cast addressing (`cast(Ns.Type)` → `Set/Ns.Type[(key)]`) | ✅ | URL Conv. §4.11 |
| Composite/named keys (`get(Map)`, `updateByKeys`/`deleteByKeys`) | ✅ | URL Conv. §4.3 |
| Write: create (deep insert) / update / replace / delete / `$ref` / `@odata.bind` | ✅ | — |
| Unbound function/action imports, bound functions, **bound actions** | ✅ | — |
| `Prefer: return=minimal/representation` (`preferReturn`) | ✅ | Protocol §8.2.8.7 |
| `$batch` JSON (dependsOn, atomicityGroup) + multipart/mixed (change sets, Content-ID) | ✅ | Protocol §11.7 |
| Schema registry (fetch/persist/lookup decoupled, conditional GET) | ✅ | ADR-0007 |
| CSRF (SAP) handshake, SSRF guard, XXE-hardened metadata, size cap | ✅ | — |
| Container singletons (`singleton(name)` → `GET /Me`) | ✅ | URL Conv. §4.3 |
| Media entities (`mediaRead`/`mediaWrite` → `Set(key)/$value`) | ✅ | Protocol §11.2.4/11.4.7 |
| Delta / change tracking (`trackChanges()` → `ODataPage.deltaLink()`, `changes(link)` → `ODataDelta`; decodes both wire forms) | ✅ | Protocol §11.3 |
| Collection update (`updateCollection(upserts, removedIds)` → one `#$delta` PATCH) | ✅ | JSON Format §15.4 |
| `$ref` reads (`references(key, nav)` → entity ids) | ✅ | Protocol §11.2.8 |
| Key-as-segment emission (`keyAsSegment()`), `IEEE754Compatible` negotiation + exact decode (`ieee754()`) | ✅ | URL Conv. §4.3.1; JSON Format §8.1 |

All of the above is proven against real systems: the live interop suite runs the client against
TripPin, TripPin RESTier, the OData demo and Northwind, and mirrors their schemas onto our
server for behaviour-parity and value-fidelity checks — see
[Live Interop Findings](/guides/odata-live-interop-findings).

See [Conformance Levels](/guides/05-conformance) for the roll-up and
[`docs/odata-conformance-status.md`](../odata-conformance-status.md) for the full
clause-by-clause evidence.
