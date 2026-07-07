# E9 acceptance: third-party tooling against the running server

`e9_acceptance.py` runs REAL external tooling against the demo server — no code from this
workspace is involved on the client side:

1. **[python-odata](https://pypi.org/project/python-odata/)** (an independent OData V4
   client): reflects `$metadata` into Python entity classes, reads entity sets, builds
   `$filter`/`$orderby` queries through its own query DSL, creates an entity via POST.
   The ETag handshake (GET → `If-Match` → DELETE, 428 without a precondition) runs over
   raw HTTP because the client does not send preconditions.
2. **[OASIS odata-json-schema](https://github.com/oasis-tcs/odata-json-schema)** (the
   official OData TC tooling): its `V4-CSDL-to-JSONSchema.xsl` transforms OUR `$metadata`
   into a JSON Schema; live responses (collection, single entity, `$filter`, `$expand`,
   `$select`) must validate against it. A final hygiene check asserts that no fields
   outside the model properties and `@odata.*` annotations appear in payloads (this
   caught a leaking codec-internal `_id` field, fixed in `ODataJsonResourceImpl`).

## Run

```bash
# 1. server (from the workspace root)
./gradlew :org.eclipse.fennec.odata.example:export.example
java -Dgosh.args=--nointeractive \
  -jar org.eclipse.fennec.odata.example/generated/distributions/executable/example.jar &

# 2. acceptance
python3 -m venv .venv && .venv/bin/pip install python-odata jsonschema requests lxml
ODATA_JSON_SCHEMA_XSL=/path/to/odata-json-schema/tools/V4-CSDL-to-JSONSchema.xsl \
  .venv/bin/python org.eclipse.fennec.odata.example/acceptance/e9_acceptance.py
```

`ODATA_BASE` overrides the service root (default `http://localhost:8080/odata/`).
Exit code 0 = all checks passed; failures are listed per check.
