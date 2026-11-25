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
| `odata.persistence` | E5 | Fennec Persistence backends: the **command** backend (`CommandPersistenceService`: `QueryService`/`WriteService`/`DeltaService` over a persistence unit's `QueryableResource`/`CommandResource`) and the **repository** backend (`RepositoryQueryService`: `QueryService` over the emf.persistence-jpa `ReadRepository` facade, #79); both share the OCL-IR → `Query`/`Expression` translation (`ReadQueries`, `ReadPlans`, `ApplyQueries`) |
| `odata.runtime` | E6/E7 | `ODataServlet` catch-all + `RequestLimits` / `EntityShaper` / `ODataJson` / resource-path parser |
| `odata.wire.api` | — | `WireProfile` SPI — the wire format of a root for OData dialects such as SensorThings (ADR-0010) |
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

## Primitive types

`EdmTypes` maps the datatype of an attribute to the `Edm.*` primitive `$metadata` declares, and
the JSON codec writes each value in the form of THAT type — so the declared type and the payload
never disagree. Both the Ecore primitives and the XSD datatypes of `XMLTypePackage` are mapped;
the latter matter because an Ecore **generated from an XSD** types nearly every feature with them.

| Attribute datatype | `Edm.*` |
|---|---|
| `EString`, `EChar`; `xs:string` and its restrictions (`anyURI`, `token`, `NCName`, `ID`, …), `xs:QName`, the list types (`IDREFS`, `NMTOKENS`, …), `xs:gYear`/`gMonth`/`gDay`/`gMonthDay`/`gYearMonth` | `Edm.String` |
| `EBoolean`, `xs:boolean` | `Edm.Boolean` |
| `EByte` / `xs:byte` · `EShort` / `xs:short` · `EInt` / `xs:int`, `xs:unsignedShort` · `ELong` / `xs:long`, `xs:unsignedInt` | `Edm.SByte` · `Edm.Int16` · `Edm.Int32` · `Edm.Int64` |
| `xs:unsignedByte` (0…255) | `Edm.Byte` |
| `EBigDecimal`, `xs:decimal`; `EBigInteger`, `xs:integer` and its restrictions (`nonNegativeInteger`, `positiveInteger`, …), `xs:unsignedLong` | `Edm.Decimal` |
| `EFloat` / `xs:float` · `EDouble` / `xs:double` | `Edm.Single` · `Edm.Double` |
| `EByteArray`, `xs:base64Binary`, `xs:hexBinary` | `Edm.Binary` (base64url) |
| `EDate`, `xs:dateTime` · `xs:date` · `xs:time` · `xs:duration` | `Edm.DateTimeOffset` · `Edm.Date` · `Edm.TimeOfDay` · `Edm.Duration` |

Two rules behind the table: an attribute never gets an Edm type whose range its values could
exceed — the unbounded integer families stay `Edm.Decimal`, which carries them exactly and still
travels as a JSON **number**, so consumers compare numerically rather than lexicographically; and
a datatype with no Edm counterpart stays `Edm.String` instead of being declared as something it
cannot serialize. `@OData.Type` on the attribute overrides the mapping where a model knows better
(e.g. a string attribute that holds `Edm.Guid`).

The reverse direction — reading a foreign `$metadata` into Ecore — stays on the Ecore primitives:
`Edm.Decimal` becomes `EBigDecimal`, never an XSD datatype.

### Comparing primitive values

A declared type is a promise about ordering too, so `$filter` and `$orderby` evaluate these values
**by value, never by representation**:

- The date and time types compare as **instants**. The same moment written in two offsets is one
  value — `2024-05-03T12:15:30+02:00 eq 2024-05-03T10:15:30Z` is true. A date-only value ignores a
  timezone it happens to carry, because `Edm.Date` has none and the payload shows the date alone.
- The component functions (`year`, `month`, `day`, `hour`, `minute`, `second`, `date`, `time`)
  read a value **in its own time zone**, as [OData-URL] 5.1.1.8.4 requires — so `hour()` of the
  value above is 12, while the comparison above still treats it as 10:15 UTC.
- A duration compares by its **length**: `duration'PT10H' gt duration'PT9H'` is true, although as
  text `PT10H` sorts first. A duration in years or months has no fixed length (`P1M` is neither
  longer nor shorter than `P30D`), so the service answers **400** instead of ranking it on a guess.
  Such values can only come from `xs:duration` data; `Edm.Duration` itself is day-time only.
  Equality is decidable even there — `P1M eq P1M` is true and `P1M eq P30D` is simply false — so
  only `lt`/`gt`/`$orderby` refuse.
- `$orderby` derives its sort key from the same semantics, so sorting and filtering cannot
  disagree. An absent value ranks first ascending and last descending.

One consequence worth knowing: an `Edm.Duration` forced onto a *string* attribute via
`@OData.Type` still compares as a string, because its runtime value is one. Model a duration with
`xs:duration` to get length comparison.

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
- **Command** (Fennec Persistence, JPA or Mongo) — the OCL IR is bridged to the Fennec `Expression`/`Query` IR (`OclToExpr` plus OData-specific rewrites) and executed through a persistence unit's `QueryableResource`; writes become `Insert`/`Update`/`DeleteCommand`s; `$apply` becomes pipeline stages; `$expand` options are pushed down where the backend declares the capability (ADR-0008). Everything is validated against the backend's declared `QueryCapabilities` first — unsupported → `UnsupportedOperationException` → 501 (never silently wrong). Differential tests mirror the in-memory reference against the IR engines.
- **Repository** (#79) — the same translation, executed through the emf.persistence-jpa **repository facade** (`ReadRepository.find`/`count`) instead of the unit directly, so whatever a repository layers over its backend (bridged inputs, transformations, query-defined data sets) stays in the loop. One factory configuration per repository, selected by `persistence.repository.id`; read-only (`QueryService` incl. `$apply`), capability-gated like the command backend.

All sit behind the `QueryService`/`WriteService`/`EntityRepository` SPIs, so a new backend
is an additive bundle with no core change.

> For the dated implementation history and deeper rationale, see the internal architecture
> architecture notes at [`docs/odata-architecture.md`](../odata-architecture.md) and the ADRs.
