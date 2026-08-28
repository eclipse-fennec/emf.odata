# Introduction

**Fennec OData** (`org.eclipse.fennec.odata`) is a self-contained **OData v4.01 server
and client** for the [Eclipse Modeling Framework](https://eclipse.dev/modeling/emf/),
built in the [Eclipse Fennec](https://github.com/eclipse-fennec) ecosystem. Domain models
are plain Ecore `EPackage`s: the server exposes them as an OData service, and the client
consumes any conformant OData v4 service back into Ecore.

The queries are parsed by an own ANTLR4 grammar into an OCL predicate IR (from Fennec
`m2x`); `$metadata` (CSDL) is produced by a direct Ecore↔EDM conversion against the OASIS
CSDL model. Both server and client decode payloads through the same
[Fennec Codec](https://github.com/eclipse-fennec/emf.codec) OData-JSON profile.

## Design decisions

The load-bearing decisions are captured as ADRs (in
[`docs/decisions/`](https://github.com/eclipse-fennec/emf.odata/tree/main/docs/decisions)):

| ADR | Decision |
|---|---|
| 0002 | **CSDL via a direct converter** against the OASIS CSDL model — no intermediate EDM object model. |
| 0003 | The **converter owns OData resolution** (a standalone core + a thin provider adapter). |
| 0004 | **OCL type resolution is standalone** — it does not depend on an external type checker. |
| 0006 | The **JPA backend hand-writes Criteria queries** (no ORM query abstraction leaks into the mapping). |
| 0007 | The **client schema registry** decouples fetch/convert, persistence/lookup and the data path behind an SPI. |

Two further choices are architecture facts rather than ADRs: the transport is a **plain
Jakarta servlet** on the OSGi HTTP Whiteboard (no Jakarta REST / Jersey), and there is
**no Olingo at runtime** (the project is archived) — the URI parser and batch splitter are
own ANTLR4 builds validated against the official OASIS ABNF test suites.

## What it is not

- Not an Olingo wrapper — there is no Olingo dependency at runtime.
- Not a JAX-RS application — there is no REST framework in the request path.
- Not tied to a single persistence — the backend is an SPI (in-memory and JPA ship today).

## Repository layout

A bnd/OSGi + Gradle hybrid workspace. Each bundle is a directory with a `bnd.bnd`. See the
[architecture guide](/guides/01-architecture) for the full bundle map. The documentation
source of truth is `docs/`; the VitePress site under `docs-site/` publishes a curated
subset of it.

## Building

Requires **Java 21** (CI also builds on Java 25).

```bash
./gradlew clean build            # compile, unit tests, coverage floors
./gradlew build testOSGi         # full build incl. OSGi integration tests
./gradlew :<bundle>:test --tests '*Name*'   # a single test
./gradlew perfTest               # the @Tag("perf") scaling tests (kept out of the fast build)
```

Dependencies are resolved from Maven Central through bnd, not through Gradle. Development
flows through the `snapshot` branch (published to Maven Central snapshots and to the docs
site); releases are cut from the protected `main` branch. Each snapshot deploy is
timestamped on Maven Central, so a specific development build can be pinned by its
timestamped version instead of the moving `-SNAPSHOT` alias.

Continue with the [architecture](/guides/01-architecture), the [server](/guides/02-server)
and [client](/guides/03-client) manuals, or jump to the
[feature matrix](/guides/06-feature-matrix).
