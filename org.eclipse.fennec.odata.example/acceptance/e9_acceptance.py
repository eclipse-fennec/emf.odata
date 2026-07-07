"""E9-light acceptance: real third-party tooling against the running Fennec OData server.

1. python-odata (PyPI, OData V4 client): reflects $metadata, queries with filters/order,
   creates and deletes an entity through the client.
2. OASIS odata-json-schema: the official V4-CSDL-to-JSONSchema.xsl transforms OUR
   $metadata into a JSON Schema; live responses must validate against it.
"""
import json
import os
import sys

import requests
from lxml import etree
from jsonschema import Draft7Validator

BASE = os.environ.get('ODATA_BASE', 'http://localhost:8080/odata/')
# checkout of https://github.com/oasis-tcs/odata-json-schema (official OASIS tooling)
XSL = os.environ.get('ODATA_JSON_SCHEMA_XSL',
                     '/opt/git/fennec-odata/reference/specs/odata-json-schema/tools/V4-CSDL-to-JSONSchema.xsl')

failures = []


def check(name, condition, detail=''):
    print(('PASS ' if condition else 'FAIL ') + name + ('' if condition else ' — ' + str(detail)[:300]))
    if not condition:
        failures.append(name)


# --- 1. python-odata client acceptance -------------------------------------------------
from odata import ODataService

service = ODataService(BASE, reflect_entities=True)
check('client reflects $metadata into entity classes',
      {'Product', 'Category'} <= set(service.entities), sorted(service.entities))

Product = service.entities['Product']
rows = service.query(Product).all()
check('client reads the demo entity set', {r.name for r in rows} == {'Milk', 'Cheese', 'Bread'},
      [r.name for r in rows])

q = service.query(Product).filter(Product.price < 3.00).order_by(Product.price.asc())
cheap = q.all()
check('client-built $filter/$orderby round-trips',
      [p.name for p in cheap] == ['Milk', 'Bread'], [(p.name, str(p.price)) for p in cheap])

first = service.query(Product).filter(Product.name == 'Cheese').first()
check('client filters by string equality', first is not None and first.id == 'p2',
      first and (first.id, first.name))

# write path through the client
new = Product()
new.id = 'e9'
new.name = 'AcceptanceWidget'
new.price = 9.99
service.save(new)
created = service.query(Product).filter(Product.id == 'e9').first()
check('client create (POST) lands in the store',
      created is not None and created.name == 'AcceptanceWidget',
      created and created.name)

# cleanup via raw HTTP (DELETE needs If-Match; the client does not send preconditions)
entity = requests.get(BASE + "Product('e9')")
etag = entity.headers.get('ETag')
check('single entity GET serves an ETag', etag is not None and etag.startswith('W/"'), entity.headers)
deleted = requests.delete(BASE + "Product('e9')", headers={'If-Match': etag})
check('DELETE with If-Match succeeds', deleted.status_code == 204, deleted.status_code)
check('DELETE without If-Match is refused (428)',
      requests.delete(BASE + "Product('p1')").status_code == 428, 'precondition enforcement')

# --- 2. official OASIS JSON-Schema validation ------------------------------------------
metadata = requests.get(BASE + '$metadata')
check('$metadata answers as XML', metadata.status_code == 200
      and metadata.text.lstrip().startswith('<?xml'), metadata.status_code)

transform = etree.XSLT(etree.parse(XSL))
schema_doc = json.loads(str(transform(etree.fromstring(metadata.content))))
check('OASIS V4-CSDL-to-JSONSchema.xsl digests our $metadata',
      'webshop.Product' in schema_doc.get('definitions', {}),
      list(schema_doc.get('definitions', {}))[:5])

validator = Draft7Validator(schema_doc)


def validate(name, url):
    payload = requests.get(BASE + url).json()
    ok = validator.is_valid(payload)
    detail = ''
    if not ok:
        errors = sorted(validator.iter_errors(payload), key=lambda e: -len(list(e.path)))
        detail = ' | '.join(f'{list(e.path)}: {e.message}' for e in errors[:3])
    check('payload validates: ' + name, ok, detail)
    return payload


collection = validate('GET Product (collection)', 'Product')
validate('GET single entity', "Product('p1')")
validate('GET with $filter+$orderby', 'Product?$filter=price%20lt%203.00&$orderby=name')
validate('GET with $expand', "Product?$expand=category")
validate('GET with $select', "Product?$select=name")

# control-information hygiene: nothing outside model properties + @odata.* annotations
entity = collection['value'][0]
strays = [k for k in entity if not (k in schema_doc['definitions']['webshop.Product']['properties']
                                    or k.startswith('@odata.') or '@odata.' in k)]
check('no codec-internal fields leak into payloads (e.g. _id)', not strays, strays)

print()
if failures:
    print('E9 acceptance: %d failure(s): %s' % (len(failures), failures))
    sys.exit(1)
print('E9 acceptance: all checks passed')
