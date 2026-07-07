# Eclipse Fennec — EMF OData

An **OData v4.01 server and client** for the Eclipse Fennec ecosystem: any EMF model
(Ecore), registered as an OSGi service, becomes a spec-conformant OData API — metadata,
queries, aggregation and writes included. No code generation, no Olingo at runtime
(ADR-0005), pure OSGi R8 / Jakarta Servlet / EMF.

## Conformance (OASIS OData, Part 1 §13)

| | 4.0 | 4.01 |
|---|---|---|
| Minimal (read) | ✅ | ✅ |
| Intermediate (read) | ✅ all MUSTs | ✅ all MUSTs |
| Updatable Service | ✅ | ✅ |

Details and per-item status: [`docs/odata-conformance-status.md`](docs/odata-conformance-status.md).
Acceptance: 899 official OASIS ABNF test cases run as JUnit suites, differential tests
between the in-memory reference backend and the JPA pushdown backend, end-to-end tests
over real HTTP.

## What works

- **`$metadata`**: bidirectional Ecore ↔ CSDL-XML incl. inheritance, vocabulary references,
  Capabilities self-description
- **Queries**: `$filter` (functions, lambdas, `in`, casts, parameter aliases, date parts),
  `$orderby`, `$top`/`$skip`, `$count`, `$select` (incl. nested), `$expand`,
  derived-type casts in URLs, `Prefer: maxpagesize`
- **Aggregation**: `$apply` pipelines (`filter`/`groupby`/`aggregate`/`compute`)
- **Writes**: POST/PATCH/PUT/DELETE, upserts, `$ref` operations, deep inserts,
  property-level writes, weak ETags with `If-Match` enforcement
- **Backends** (pluggable `QueryService`/`WriteService` SPI): in-memory/file reference
  backend and a JPA backend ([Fennec Persistence JPA](https://github.com/eclipse-fennec/emf.persistence-jpa))
  with full SQL pushdown — filters, paging, `TYPE()` casts, grouped aggregation, batched
  prefetch (no N+1)
- **Security posture**: hard request limits before parsing, typed query IR (no string
  concatenation into backends), sanitized errors, body-size caps

## Getting started

```bash
./gradlew clean build testOSGi   # full build incl. OSGi integration tests (Java 21)
```

Open the workspace in Eclipse/bndtools and resolve
`org.eclipse.fennec.odata.example/example.bndrun` — a demo shop model on
`http://localhost:8080/odata/`:

```bash
curl 'http://localhost:8080/odata/$metadata'
curl 'http://localhost:8080/odata/Product?$filter=price lt 3.00&$orderby=name'
curl 'http://localhost:8080/odata/Product?$apply=groupby((category/name),aggregate(price with sum as Total))'
```

## Architecture

Entry point: [`docs/odata-architecture.md`](docs/odata-architecture.md) — bundle map,
request lifecycle, security defaults, ADRs (`docs/decisions/`), backlog. Requirements:
[`docs/odata-basic-requirements.md`](docs/odata-basic-requirements.md).

## License

[Eclipse Public License 2.0](LICENSE) — an [Eclipse Fennec](https://projects.eclipse.org/projects/technology.fennec) project.
