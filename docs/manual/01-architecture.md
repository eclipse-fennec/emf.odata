# Architecture

Fennec OData is split into small OSGi bundles along the OData processing stages (E1–E8).
The server is a catch-all servlet; the query surface is an OCL IR; the persistence is an
SPI. The client mirrors the server's codec and metadata wiring.

## Big picture (server)

```
                    HTTP (Jetty12, OSGi HTTP Whiteboard)
                                   │
                       ┌───────────▼───────────┐   plain Jakarta servlet,
                       │      ODataServlet      │   no Jakarta REST
                       │  (runtime, catch-all)  │
                       └──┬───────┬───────┬─────┘
        RequestLimits ────┤       │       ├──── EntityShaper ($select/$expand copies)
        (before parsing)  │       │       └──── ODataJson (rows / errors, sanitizer)
                          │       │
             ┌────────────▼──┐   ┌▼──────────────────┐
             │ $metadata     │   │ Query options     │
             │ E2: csdl      │   │ E4: query         │
             │ Ecore↔EDM     │   │ ANTLR4 → OCL IR   │──── CachingODataQueryParser
             │ (OASIS model) │   │ + $apply submodel │     (LRU, per EClass)
             └───────▲───────┘   └────────┬──────────┘
                     │                    │ typed OCL IR (m2x ocl.model)
             ┌───────┴───────┐   ┌────────▼──────────┐
             │ E1: metadata  │   │ E5: QueryService  │  SPI (persistence.api)
             │ ODataAspect-  │   │ execute /         │
             │ Provider +    │   │ executeApply      │
             │ vocabularies  │   └────────┬──────────┘
             └───────────────┘   ┌────────▼──────────┐
                                 │ backend           │  in-memory (reference) / JPA
                                 │ + ApplyExecutor   │
                                 └────────┬──────────┘
                                 ┌────────▼──────────┐
                                 │ EntityRepository  │  SPI: data source
                                 │ File (XMI) /      │
                                 │ programmatic / JPA│
                                 └───────────────────┘
   Responses: E3 codec.json (OData-JSON per entity) | XMI ($format=xml) | CSDL-XML ($metadata)
```

## Bundles

| Bundle | Stage | Content |
|---|---|---|
| `odata.metadata` | E1 | `ODataMetadataHandler` (thin adapter, ADR-0003): attaches the resolved CSDL profile as the `"odata"` `AspectEntry` of every registered `EPackage`. No Ecore of its own |
| `odata.vocabularies` | E1 | OASIS Core/Capabilities/Validation/Measures as EPackages (CSDL read bootstrap) |
| `odata.csdl` | E2 | `OdataResolver` (EPackage + `@OData.*` → profile), Ecore↔EDM converters, `$metadata`, XXE-hardened `CsdlXmlLoad` |
| `odata.codec.json` | E3 | OData-JSON codec profile (`@odata.type`/`@odata.id`, `Edm.*` value formats) |
| `odata.query` | E4 | ANTLR4 grammar → OCL IR, `$apply` submodel, standalone type resolver (ADR-0004), `OclEvaluator`, LRU cache |
| `odata.operation.api` | E4 | `ODataOperationHandler` SPI — pluggable function/action implementations |
| `odata.persistence.api` | E5 | `QueryService` / `ApplyQuery` / `WriteService` / `MediaService` / `DeltaService` SPIs + `EntityRepository` data-source abstraction |
| `odata.persistence.inmemory` | E5 | Reference backend: in-memory query + `ApplyExecutor`, `FileEntityRepository`, `MemoryWriteRepository` (write + media + change journal) |
| `odata.persistence.jpa` | E5 | JPA backend: `OclToCriteriaTranslator`, `JpaApplyExecutor`, `WriteService` (Jakarta Criteria, ADR-0006) |
| `odata.runtime` | E6/E7 | `ODataServlet` catch-all + `RequestLimits` / `EntityShaper` / `ODataJson` / resource-path parser |
| `odata.schema.api` | E8 | Client schema-registry SPI: `ODataSchemaReader` / `Registrar` / `Resolver` / `ODataSchema` / `SchemaScope` (ADR-0007) |
| `odata.client` | E8 | `ODataClient`, fluent `EntitySetRequest`, `$batch`, CSRF, schema registry impl |
| `odata.example` | — | Demo model + data + runnable `example.bndrun` (port 8080) |

## Modelling keys

Which Ecore features become the OData key of an entity type — the one modelling decision the whole
stack hangs off, since it drives `<Key>` in `$metadata`, `@odata.id`, the `Location` header, the
key predicates in URLs and the fragment the backends store.

| Model declaration | Meaning |
|---|---|
| one attribute `iD="true"` | single-part key — EMF's own eID attribute, nothing else needed |
| `idFeatures` on the **EClass** | the key properties by name, in canonical key order — the ONE way to declare a multi-part key |
| `@OData.Key="true"` on an attribute | OData-only key marker for models that cannot set `iD` |

The identity annotation carries the source `http://eclipse.org/fennec/persistence/1.0` and the
detail `idFeatures` (a comma-separated name list). It is deliberately the same declaration the
Fennec persistence stack reads, so an entity addresses the same row in the store as it does over
HTTP. Several `iD="true"` attributes are **not** a composite key: Ecore allows at most one
(`validateEClass_AtMostOneID`), and the backends refuse that shape.

`OdataResolver` is the only component that reads these three forms; everything downstream takes
the key from the resolved profile (`ODataClassProfile.getKeyPropertyNames()`), and the backends
from the same declaration via `CompositeIds`. A type with no key at all is a **complex type**, not
an entity type.

## Request lifecycle (`GET /odata/{Set}?…`)

1. **`OData-Version` negotiation** — responds 4.01 unless the client pins `OData-MaxVersion: 4.0`; non-GET on read paths → 405.
2. **Option normalisation & routing** — 4.01 case-insensitive, `$`-prefix optional; a whitelist maps known-unsupported options → 501 and unknown `$x` → 400. Routes: `/` service doc · `/$metadata` CSDL · `Set(key)…` via the own resource-path parser · `Set` collection.
3. **Limits before parsing** (`RequestLimits`) — expression length, parenthesis depth (O(n) scan), `$top` ceiling, paging validation.
4. **Parsing** (`CachingODataQueryParser`) — `$filter`/`$orderby`/`$apply` → typed OCL IR; property paths resolved eagerly against the context EClass (unknown names → 400). Single-entity keys are **built** as a literal AST, never expression-parsed (quote-injection stays one literal).
5. **Backend** — `QueryService.execute(EntityQuery)` / `executeApply(ApplyQuery)` receives only the IR. Options after `$apply` run on the transformed set.
6. **Response** — `EntityShaper` copies ($select/$expand) → E3 OData-JSON (`odata.metadata=minimal` by default, `full`/`none` on request) or XMI; `$apply` rows → `ODataJson`; page overflow → `@odata.nextLink` (or `@odata.deltaLink` under `Prefer: odata.track-changes`); errors → sanitized OData error JSON.

## The query IR

`$filter`/`$orderby` are parsed into a typed **OCL expression tree** (Fennec `m2x`
`ocl.model`). This single IR is what every backend consumes — there is no
string-concatenated SQL/query path anywhere, which is the structural basis of the
injection safety (see [Configuration & Security](/guides/04-configuration)). `$apply` is a
separate aggregation submodel, not part of the OCL tree.

## Backends

- **In-memory** — the reference semantics. `OclEvaluator` interprets the IR directly (three-valued null logic, lambdas, cast, `$count`, typed literals) and never fails silently: type/format errors surface as 400, not 500.
- **JPA** (ADR-0006) — `OclToCriteriaTranslator` turns the IR into Jakarta Criteria queries; `$apply` becomes a single grouped Criteria query; `$expand` and navigation walks materialize via batched IN-hints / LEFT fetch joins (no N+1). Anything not translatable → `UnsupportedOperationException` → 501 (never silently wrong). Differential tests mirror the in-memory reference against H2.

Both sit behind the `QueryService`/`WriteService`/`EntityRepository` SPIs, so a new backend
(e.g. Mongo) is an additive bundle with no core change.

> For the dated implementation history and deeper rationale, see the internal architecture
> architecture notes at [`docs/odata-architecture.md`](../odata-architecture.md) and the ADRs.
