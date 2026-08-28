# Live Interop Findings — Spec Trace-Back

Status: 2026-07-09. The live suite (`org.eclipse.fennec.odata.live.tests`, `./gradlew liveTest`)
ran the client against the public reference services (TripPin, OData demo, Northwind) and
mirrored their schemas onto our server (see `MirrorRoundTripTest`). Every initial failure was
traced back to the OASIS spec (vendored in `reference/specs/`): **was our implementation
non-conformant, or was the service exercising a spec right we did not cover?** Each finding is
now guarded by an OFFLINE unit test, so conformance no longer depends on the live systems.

| # | Finding (live symptom) | Spec clause (vendored source) | Root cause | Fix | Offline guard |
|---|---|---|---|---|---|
| 1 | `entitySet("People")` → "no entity type" (TripPin), same for Northwind `Customers` | **[OData-CSDL-XML] §13.2**: "The value of `Name` is the entity set's name" — independent of the `EntityType` attribute | **Ours**: client and server assumed the convention *set name = type name* and ignored the container's `EntitySet` declarations | Set names round-trip via the EPackage annotation `…/odata/entitysets` (read: XML+JSON → annotation; write: renames sets + binding targets); client `entityType()` and servlet `resolveEntityType()` resolve through it | client `foreignMetadataQuirks`, runtime `renamedEntitySets`, csdl `entitySetNamesRoundTrip` (+ JSON round-trip) |
| 2 | Northwind: mapping resolved but type not found; mirror `$metadata` lost the set names | **[OData-CSDL-XML] §5/§13**: the container references types by *qualified name* — it may live in a different schema than the types | **Ours**: set-mapping and type lookup were package-local (client, servlet resolve AND `$metadata` emit) | Two-phase lookup across ALL packages; the servlet applies the renames of all packages to every emitted schema (`EcoreToEdmConverter.applyEntitySetNames`) | client `foreignMetadataQuirks` (container in a second schema), Northwind mirror in `MirrorRoundTripTest` |
| 3 | `connect(TripPin)` failed — the root answers `302` to a session URL | **[OData-Protocol] §9.1.5**: "a 3xx Redirection indicates that further action needs to be taken by the client … SHOULD include a Location header" | **Ours**: the client did not handle 3xx on the service root | `resolveServiceRoot`: follows up to 3 same-host hops, never downgrades https→http, pins the FINAL root as the data root (otherwise every request would mint a fresh session) | client `serviceRootRedirect` |
| 4 | `nextPage()` refused TripPin's `@odata.nextLink` — plain `http://` behind an https root | **[OData-Protocol] §11.2.5.7**: the client MUST follow `nextLink` (opaque URL); scheme consistency is not mandated | **Service quirk**, but the spec obliges the client to follow; refusing broke paging, following verbatim would leak headers over cleartext | Same-host `http://` links are UPGRADED to the root's `https://` before the SSRF guard (downgrade still refused) | client `schemeUpgrade` |
| 5 | TripPin answered `500` to `$filter=Gender eq 'Female'` | **ABNF** (`odata-abnf-construction-rules.txt`): `enum = [ qualifiedEnumTypeName ] SQUOTE enumValue SQUOTE` — the prefix-free form is 4.01; TripPin RW is a **4.0** service | **Our test**: sent 4.01 literal syntax to a 4.0 service (the client forwards `$filter` opaquely — correct) | Live test uses the 4.0 form (`…PersonGender'Female'`); our own server accepts both (parser E4-AP-3) | query-bundle enum-literal tests (existing) |
| 6 | OData demo `$metadata` unparseable: `Value 'Variable' is not legal` (`Scale`, then `SRID`) | **[OData-CSDL-XML] §7.2.4/§7.2.6**: Scale/SRID are "a number or one of the symbolic values `floating` or `variable`" (+ "clients SHOULD accept values in a case-insensitive manner") | **Ours**: the XSD-generated EDM model only accepts integers — we failed on spec-legal values | Symbolic `Scale`/`SRID` values are stripped before parsing (lossy facets for the Ecore mapping anyway) | client `foreignMetadataQuirks` (`Scale="Variable"`, `SRID="Variable"`) |
| 7 | OData demo `$metadata` unparseable: `Feature 'ConcurrencyMode' not found` (an OData **V3** relic) | **[OData-Protocol] §6.2**: "clients and services MUST be prepared to handle or safely ignore any content not specifically defined in the version of the payload" | Service serves legacy content — but the spec obliges **us** to tolerate it | `$metadata` load records unknown features instead of failing (`OPTION_RECORD_UNKNOWN_FEATURE`) | client `foreignMetadataQuirks` (`ConcurrencyMode="Fixed"`) |

## Round 2 (broadened suite: RESTier stack, live writes, multipart batch, composite keys)

| # | Finding (live symptom) | Spec clause | Root cause | Fix | Offline guard |
|---|---|---|---|---|---|
| 8 | Northwind `Order_Details(OrderID=…,ProductID=…)` not addressable | **[OData-URL/ABNF]** `compoundKey` — named key components; composite keys | **Ours**: grammar only knew the positional single-literal predicate (ironically the CSDL converter round-trips composite keys) | Grammar `(k1=v1,k2=v2)` + `ResourcePath.namedKeys`; servlet builds a typed AND-of-equalities (all key properties must be named, non-key components → 400; composite-key writes → honest 501); client `get(Map)`; ABNF compound-key skips activated | runtime `compoundKeys`, client `compoundKeyGet`, live Northwind probe |
| 9 | TripPin rejects our `$batch` (415: only `multipart/mixed`) | **[OData-Protocol] §11.7**: 4.0 batches are multipart; JSON is 4.01 | **Ours**: client only spoke the JSON batch format — unusable against the whole 4.0/SAP world | `ODataBatch.multipart()`: parts for reads, change sets with `Content-ID` for writes, nested multipart response parsing | client `multipartBatch` + live TripPin batch |
| 10 | Write to TripPin → 500 "a type named 'Person' could not be resolved" | **[OData-JSON] §4.5.3**: `@odata.type`, if present, must be the QUALIFIED name | **Ours**: write payloads carried the full-metadata profile with a non-qualified type discriminator | Write payloads use the MINIMAL profile (the type follows from the request URL) | client write tests (payload asserts) + live write cycle |
| 11 | RESTier + `OData-MaxVersion: 4.01` → `@odata.count` "missing" | **[OData-JSON] 4.01 §4.1**: control information MAY omit the `odata.` prefix (`@count`, `@context`) | **Ours**: decoder only read the `@odata.*` form | Decoder accepts both; control-info stripping treats a dot-free `@suffix` as control, `@Ns.Term` annotations survive | client `csdlJsonMetadata` (prefix-free `@count` stub) |
| 12 | RESTier `/$count` answer unparseable: `\uFEFF20` | (HTTP/encoding hygiene — not OData) | Service prefixes responses with a UTF-8 BOM | The client strips a leading BOM centrally in the bounded reader | covered by live `totalCount` probes |
| 13 | "Unknown key" behaviour differs per stack: TripPin **204**, WCF **400** (type-mismatched literal), spec **404** | [OData-Protocol] §9.2.1 | Live services disagree — no parity possible | The probe is mirror-only: OUR server must answer the spec-conformant 404 | mirror `unknown key` assertion |

Also proven live in round 2: the full WRITE cycle (create → read → patch with If-Match → delete)
against a foreign 4.0 server, a SECOND server stack (TripPin RESTier), multipart `$batch`
round-trips, and per-attribute VALUE fidelity on the mirror (deterministic demo data must
survive serialize → decode exactly, for whatever Edm types the foreign schema uses).

## The maximal round trip (phase 2)

`MirrorRoundTripTest` closes the loop for each service: the live schema — read through our
client — is mirrored onto OUR server (in-memory backend, synthetic demo data via `DemoData`),
the mirror client re-discovers OUR `$metadata` (the schema round-trips a second time), and the
SAME request specs (discovery, `$top`, `$orderby`, `$filter … ne null`, `$select`, inline
`$count`, `/$count`, keyed read) run against both. Asserted: identical behaviour — success,
decoded type, response shape. The data intentionally differs.

Green as of 2026-07-09 for **TripPin, OData demo and Northwind**.

## Running

```bash
./gradlew liveTest         # live suite — external services; unreachable endpoints SKIP
./gradlew build testOSGi   # normal build — the offline guards above are part of it
```

Live tests are `@Tag("live")` and excluded from the normal build and CI (external systems are
not CI material).
