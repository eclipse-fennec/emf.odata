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

## Backend / repository

| Setting | PID / key | Default |
|---|---|---|
| File repository directory (read once at activation; never influenced by request input) | `org.eclipse.fennec.odata.repository.file` · `directory` | — |
| JPA server-driven page size (`<= 0` = unlimited) | `org.eclipse.fennec.odata.persistence.jpa` · `odata.jpa.max.page.size` | 1000 |

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
