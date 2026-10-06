# Configuration & Security

The server ships secure-by-default: all limits apply *before* parsing, the only query path
is the typed IR (no string concatenation), and errors never leak internals.

## Servlet limits (PID `org.eclipse.fennec.odata.servlet`)

| Protection | Default | Key |
|---|---|---|
| `$top` ceiling (applies even without a client `$top`) | 1000 | `odata.max.top` |
| Max. expression length (`$filter`/`$orderby`/`$apply`/`$expand`) | 4096 | `odata.max.expression.length` |
| Max. parenthesis depth (parser-bomb guard, pre-parse, underflow-safe) | 64 | `odata.max.nesting.depth` |
| Max. write body size | 1 MiB | `odata.max.body.size` |
| Max. sub-requests per `$batch` (both wire formats) | 100 | `odata.max.batch.operations` |
| Max. concurrent respond-async executions (beyond → 503 + Retry-After) | 16 | `odata.max.async.inflight` |
| Max. parked async status monitors (LRU) | 100 | `odata.max.async.monitors` |
| CORS origin(s) for browser clients (`*` or space-separated allowlist; empty = CORS off) | off | `odata.cors.origin` |

For every limit, `<= 0` disables the protection — a deliberate, documented foot-gun, never
the default. The defaults above were reviewed and **confirmed as the shipped baseline on
2026-07-17**; they are starting values validated by the test corpus, expected to be tuned
per deployment as production experience accrues.

## Service roots & published model

Both PIDs — servlet `org.eclipse.fennec.odata.servlet` and request-limits filter
`org.eclipse.fennec.odata.request.filter` — are `configurationPolicy = optional` components
that accept **factory configurations**: one configuration is one instance. Every property of
a configuration becomes a service property, so the standard HTTP-whiteboard keys select where
an instance lives; the `odata.model.*` keys select what it publishes.

| Setting | Key | Default |
|---|---|---|
| Whiteboard pattern of the servlet instance | `osgi.http.whiteboard.servlet.pattern` | `/odata/*` |
| Whiteboard pattern of the filter instance | `osgi.http.whiteboard.filter.pattern` | `/odata/*` |
| The HTTP runtime to bind to (filter on the `HttpServiceRuntime` properties) | `osgi.http.whiteboard.target` | all runtimes |
| The servlet context to bind to | `osgi.http.whiteboard.context.select` | the default context |
| Packages (nsURIs) published as schemas | `odata.model.packages` | every bound `EPackage` |
| Entity sets published: `[SetName=]nsURI#EClass` or `[SetName=]EClass` | `odata.model.entitysets` | every concrete class, named by type (or its `@OData` rename annotation) |

Multi-valued keys take a `String[]`, a collection, or one comma/whitespace-separated string.
Both allowlists apply consistently to `$metadata` (schemas, container sets, navigation
bindings), the service document, entity-set routing (an unlisted set is a **404**), cast and
operation resolution — so `QueryService`/`WriteService` selection only ever sees published
types. An `odata.model.entitysets` entry whose package is not (yet) bound is skipped with a
warning and picked up when the package arrives. Entity **types** stay in their schema even
when their set is not published: navigation properties still reference them.

A root that publishes **several packages** emits one schema per package but exactly **one**
`EntityContainer` (as CSDL requires): it holds the sets of all packages, each typed by its
own namespace, binds navigations across packages, and lives in the schema of the first
package — the first `odata.model.packages` entry, or the lowest nsURI when the key is unset.
Two sets of the same name keep the first one and log a warning; rename the other in
`odata.model.entitysets`.

While factory configurations exist there is no unconfigured default root; deleting the last
one brings `/odata/*` back. A singleton configuration of the PID (as in the example bundle)
configures that single default instance. Independently of the allowlist, the DS reference
filters `EPackage.target` and `QueryService.target` (e.g. `(emf.nsURI=…)`, `(fennec.odata.backend=jpa)`)
narrow what an instance binds in the first place.

```json
"org.eclipse.fennec.odata.servlet~shop": {
    "osgi.http.whiteboard.servlet.pattern": "/atlas/shop/*",
    "osgi.http.whiteboard.target": "(id=dataAtlasHttp)",
    "odata.model.packages": ["http://example.org/webshop"],
    "odata.model.entitysets": ["Products=http://example.org/webshop#Product", "Category"]
},
"org.eclipse.fennec.odata.request.filter~shop": {
    "osgi.http.whiteboard.filter.pattern": "/atlas/shop/*",
    "osgi.http.whiteboard.target": "(id=dataAtlasHttp)",
    "odata.max.expression.length": 1024
}
```

## Backend / repository

| Setting | PID / key | Default |
|---|---|---|
| File repository directory (read once at activation; never influenced by request input) | `org.eclipse.fennec.odata.repository.file` · `directory` | — |
| Command backend (factory): persistence unit, e.g. `jpa://shop` or `mongodb://assets` | `org.eclipse.fennec.odata.persistence.command` · `backend.uri` | — (required) |
| Command backend: packages served (nsURIs); without it every keyed EClass is claimed | · `emf.nsURIs` | all |
| Command backend: server-driven page cap for unbounded reads (`<= 0` = unlimited) | · `max.page.size` | 1000 |
| Repository backend (factory): the emf.persistence-jpa `ReadRepository` to serve, as a DS target on its `persistence.repository.id` | `org.eclipse.fennec.odata.persistence.repository` · `repository.target` | — (required) |
| Repository backend: packages served (nsURIs) / page cap | · `emf.nsURIs` / `max.page.size` | all / 1000 |

The repository backend reads through the facade only (`find`/`count`, `$apply` as a pipeline
query); capabilities are gated exactly as the command backend gates them (undeclared feature →
501, structural violation → 400). Each request takes its own prototype-scoped repository
instance and releases it afterwards — the facade's `ResourceSet` is not thread-safe.

```json
"fennec.repository.jpa~shop": { "repositoryId": "shop", "unit.target": "(osgi.unit.name=shop)", "readOnly": true },
"org.eclipse.fennec.odata.persistence.repository~shop": {
    "repository.target": "(persistence.repository.id=shop)",
    "emf.nsURIs": ["http://example.org/webshop"]
}
```

## Security model

| Concern | Handling |
|---|---|
| **Injection** | Structural: the only query path is the typed OCL IR; no string concatenation in backends; unknown properties/functions → 400. Single-entity keys are built as a literal AST, never expression-parsed. |
| **Parser bombs** | Parenthesis depth capped before parsing (O(n) scan); the parser also traps bracket-free deep recursion (`not not …`, deep member paths) via a `StackOverflowError` guard → 400. Resource paths have a length cap before parsing. |
| **XXE / billion-laughs** | `CsdlXmlLoad.secureOptions()` disables DOCTYPE and external entities and is the single CSDL/EDMX load path — used by the vendored vocabularies **and** by the client reading a foreign service's (untrusted) `$metadata`. |
| **Evaluation errors** | Type/format errors in the in-memory evaluator and non-comparable `$orderby` keys surface as 400, not an internal 500. |
| **Error leaks** | 500 is generic; messages are JSON-escaped, control chars stripped, capped at 500 chars; unexpected 500s are logged server-side (never spilled to the client). |
| **Path leaks** | Serialization copies carry co-copied expand targets → only internal references, never server URLs. |
| **Optimistic concurrency** | A backend error on the If-Match read propagates (logged 500) rather than silently degrading to an upsert (lost-update risk). |
| **Header injection** | Entity keys in `Location`/`OData-EntityId` are control-char escaped (no response splitting). |
| **Client SSRF** | A server-supplied absolute link (e.g. `@odata.nextLink`) to a different origin than the service root is refused. |
| **Auth / TLS** | Out of scope — upstream infrastructure (reverse proxy / gateway). The client carries bearer/basic headers and a SAP CSRF handshake. |
| **Crypto baseline (deployment)** | Deployments SHOULD follow **BSI TR-02102-1/-2** (cryptographic mechanisms, TLS configuration of the fronting proxy) and **BSI TR-03116** (profiles for federal projects) — confirmed as the assumed baseline 2026-07-17. The server itself terminates no TLS, so the requirements land entirely on the upstream infrastructure. |

All of the above is covered by unit (Mockito) and end-to-end (real HTTP) tests: injection
strings, parser bombs, oversized filters, `$top` exhaustion, leak-freedom of 500s, and
key-injection.

## Build-time gates

- **JaCoCo coverage floor** wired into `check` for the hand-written library bundles (a catastrophic-regression tripwire; generated code excluded). Per-bundle floors from the 2026-07-17 measurement run, each ~10 points below the measured ratio (`build.gradle` documents both values).
- **Structural scaling asserts** (`@Tag("perf")`, `./gradlew perfTest`): constant SQL-statement counts at 50k rows — these gate the build; timing is only logged.
