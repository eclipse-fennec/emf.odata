---
layout: home

hero:
  name: Fennec OData
  text: OData v4.01 for EMF — server and client
  tagline: A self-contained OData v4.01 server and client in the Eclipse Fennec / EMF ecosystem — CSDL generated from Ecore, an OCL query IR, JPA and in-memory backends, and a schema-aware EMF client.
  image:
    src: /fennec-logo.png
    alt: Eclipse Fennec logo
  actions:
    - theme: brand
      text: Introduction
      link: /guides/00-introduction
    - theme: alt
      text: Feature Matrix
      link: /guides/06-feature-matrix
    - theme: alt
      text: View on GitHub
      link: https://github.com/eclipse-fennec/emf.odata

features:
  - icon: 🗂️
    title: Model-driven metadata
    details: "$metadata (CSDL) is a direct Ecore↔EDM conversion against the OASIS CSDL model — entity/complex types, enums, inheritance, keys, navigation bindings and a vocabulary-annotation layer, XSD-validated round-trip."
    link: /guides/01-architecture
    linkText: Architecture
  - icon: 🔎
    title: Full query surface
    details: "$filter / $orderby / $select / $expand / $search / $compute / $apply / derived-type casts — parsed by an own ANTLR4 grammar into an OCL predicate IR and pushed down to the backend. No in-memory filtering, no N+1."
    link: /guides/02-server
    linkText: Server manual
  - icon: 🗄️
    title: Pluggable backends
    details: An in-memory reference backend (three-valued null logic, the semantics oracle) and a JPA backend that translates the OCL IR into Jakarta Criteria queries — including $apply as a single grouped query.
    link: /guides/02-server
    linkText: Server manual
  - icon: 🔌
    title: Schema-aware client
    details: A fluent EMF client for any conformant OData v4 service — reads $metadata into Ecore, decodes through the same codec profile, writes (incl. @odata.bind), $batch, CSRF and SSRF hardening, and a decoupled schema registry.
    link: /guides/03-client
    linkText: Client manual
  - icon: ✅
    title: Conformance-tracked
    details: "4.0 and 4.01 Minimal and Intermediate are met (incl. the Updatable Service), verified clause-by-clause against the OASIS spec and thousands of ABNF acceptance cases."
    link: /guides/05-conformance
    linkText: Conformance levels
  - icon: 📐
    title: Spec feature matrix
    details: What the spec requires, what we implement, and the exact spec reference for each item — the map of our OData surface against OData v4.01 Part 1 (Protocol) and Part 2 (URL Conventions).
    link: /guides/06-feature-matrix
    linkText: Feature matrix
---

## About Fennec OData

Fennec OData (`org.eclipse.fennec.odata`) is an **OData v4.01 server *and* client**
built on the [Eclipse Modeling Framework](https://eclipse.dev/modeling/emf/) in the
[Eclipse Fennec](https://github.com/eclipse-fennec) ecosystem. Models are plain Ecore
`EPackage`s; the server exposes them as an OData service and the client consumes any
conformant OData v4 service back into Ecore.

Key design decisions (see the [architecture guide](/guides/01-architecture)):

- **No Olingo at runtime** — the URI parser, expression parser and batch splitter are
  own ANTLR4 grammars; Olingo source is a study reference only.
- **No Jakarta REST** — transport is a catch-all servlet on the OSGi HTTP Whiteboard on
  the server, and `java.net.http` on the client.
- **OCL as the query IR** — `$filter`/`$orderby` become a typed OCL predicate tree that
  every backend consumes; there is no string-concatenated query path.
- **CSDL without an intermediate EDM object model** — a direct Ecore↔EDM converter.

The documentation here is the user-facing manual and the OData specification feature
matrix. Internal development notes (the architecture change-log, backlogs, ADRs and the
detailed conformance analysis) live in the
[`docs/` folder on GitHub](https://github.com/eclipse-fennec/emf.odata/tree/main/docs).
