# Conformance Levels

OData Part 1 defines three conformance levels per version (§13.1 for 4.0, §13.2 for 4.01):
**Minimal**, **Intermediate**, **Advanced**. Fennec OData is tracked clause-by-clause
against the vendored OASIS spec artifacts (`reference/specs/`): Part 1 (Protocol), Part 2
(URL Conventions), the normative ABNF construction rules and the OASIS ABNF test cases.

## Verdict

| Level | Status | Notes |
|---|---|---|
| **4.0 Minimal** (§13.1.1) | ✅ **met** — incl. *Updatable* | Items 1–15 met; 16–17 async N/A; 33 is MAY |
| **4.01 Minimal** (§13.2.1) | ✅ **met** | MUSTs met/N-A; CSDL-JSON SHOULD met too |
| **4.0 Intermediate** (§13.1.2) | ✅ **met** | all MUSTs **and** all SHOULDs (1–17) |
| **4.01 Intermediate** (§13.2.2) | ✅ **met** | MUSTs done; SHOULDs 6/7/9 partial |
| **4.0 Advanced** (§13.1.3) | ✅ **met** (clause re-audit 2026-07-14) | all MUSTs 1–12; SHOULDs 9.4–9.7 (expand `$orderby`/`$count`/`$top`/`$skip`/`$search`) and 14 (delta) met too; open SHOULDs: async (13), cross-join (15), `$levels` (9.8) |
| **4.01 Advanced** (§13.2.3) | ✅ **met** (clause re-audit 2026-07-14) | all MUSTs incl. filtered/**searched** `$count` in expressions and `$filter` on selected collections (5.1); SHOULDs 5.2–5.5 (select `$orderby`/`$count`/`$top`/`$skip`/`$search`) met too; open SHOULD: nested parameter aliases (9) |

**Bottom line:** all four levels hold — **4.0 and 4.01, Minimal through ADVANCED**
(clause re-audit 2026-07-14), including the **Updatable OData Service** (create / update
/ delete / `$ref` / upsert / deep insert / If-Match). `$metadata` advertises
`Capabilities.ConformanceLevel=Advanced`. What remains is SHOULD/MAY-level only.

## The Updatable Service (§13.1.1 items 18–33)

Edit links, `POST` new entities, `POST` related (`createRelated`, containment +
non-containment), `POST`/`PUT`/`DELETE` `$ref` (single clear + collection member via `$id`,
and 4.01 key-in-URL), `PATCH`, `DELETE`, `If-Match` (weak ETags; updates/deletes of
existing entities require it → 428/412), `Location`, `OData-EntityId`, upserts,
property-level writes (replace-based), and deep inserts of containments.

## Known SHOULD/MAY gaps

- A few **4.01 Intermediate SHOULDs** — some query options on navigation paths, some options inside `$expand`.
- **Advanced SHOULDs** — async / `Respond-Async`, `$crossjoin`, recursive `$levels`, nested parameter aliases (`$expand`/`$select` collection options are met).
- **Delta v1 limits** — change tracking (the Advanced SHOULD) is covered since 2026-07-13, with
  documented v1 limits: no `$expand` deltas, no `PATCH` collection-update, no JPA backend — see
  the [server guide](/guides/02-server#change-tracking).

The full clause-by-clause analysis (with per-item evidence and the spec references) lives
in [`docs/odata-conformance-status.md`](../odata-conformance-status.md); the
[feature matrix](/guides/06-feature-matrix) maps individual features to their spec
sections.
