# ADR 0006 – JPA-Backend als Criteria-Eigenbau im OData-Repo (Q21 entschieden)

| Feld     | Wert                                                                     |
|----------|--------------------------------------------------------------------------|
| Status   | Akzeptiert                                                               |
| Datum    | 2026-07-06                                                               |
| Betrifft | req-doc §7 Q21 (`OrmAspectProvider`), §9 E5 (Persistence Adapter JPA)    |

## Kontext

Q21 fragte, ob `emf.persistence-jpa` einen `OrmAspectProvider` hat oder bekommt — davon hing
ab, wie schmal E5 wird. Die Kartierung des Repos (2026-07-06) ergab:

- Es gibt **keinen** `OrmAspectProvider` und **keinerlei Query-/Filter-Abstraktion**
  (`REVIEW.md` F4: nur Full-Load, Count und Offset-Paging in `JPAResourceImpl`;
  „abstraktes EMF-Query-Modell → JPA" ist explizit ein offenes Arbeitspaket).
- Der nutzbare Vertrag ist die **`EntityManagerFactory` als OSGi-Service** (eine pro
  Persistence-Unit, Property `osgi.unit.name`), mit **dynamischen EMF-Entities**
  (EclipseLink Dynamic Entities, die `EObject` implementieren): Entity-Name =
  `EClass.getName()`, Attribut-Namen = EMF-Feature-Namen.

## Entscheidung

E5 wird als **leichtgewichtiger Criteria-Eigenbau im OData-Repo** umgesetzt
(Bundle `org.eclipse.fennec.odata.persistence.jpa`), NICHT als Vorarbeit in
`emf.persistence-jpa`:

1. `JpaQueryService implements QueryService` konsumiert `EntityManagerFactory`-Services
   und übersetzt das OCL-Prädikat-IR per **Jakarta Criteria API** — voller Pushdown
   (Filter/Order/Paging/Count/`TYPE()`-Casts via `treat()`), kein In-Memory-Filtering.
2. Konstrukte ohne Übersetzung werfen `UnsupportedOperationException` → Servlet antwortet
   **501** (nie stillschweigend falsche Ergebnisse); der In-Memory-Backend bleibt
   Referenz-Orakel (Differenzialtests gegen H2).
3. `$apply`-Pushdown (Criteria `groupBy`) ist ein Folge-Arbeitspaket; bis dahin 501.

Ein `OrmAspectProvider` upstream bleibt möglich (Cache/Lifecycle-Adapter, analog VA1/ADR-0004),
ist aber KEINE Voraussetzung mehr.

## Konsequenzen

- Zwei in `emf.persistence-jpa` gefundene Bugs wurden dort (Feature-Branch
  `fix/metamodel-refresh-dynamic-types`, je Fix ein Test) behoben, da ohne sie keine
  Criteria-Query gegen dynamische Typen möglich ist:
  1. dynamisch registrierte Deskriptoren waren im gecachten JPA-Metamodel unsichtbar
     (`EDynamicHelper.addETypes` erneuert es jetzt; Fennec-Accessoren erweitern
     `ValuesAccessor` als Dynamic-Marker für die Metamodel-Initialisierung);
  2. `EBigDecimal`-Spalten verloren Nachkommastellen (NUMERIC-Scale-0-Default → jetzt
     NUMERIC(38,19)-Default, explizite EORM-Facetten gewinnen).
- Bis die Fixes als Snapshot publiziert sind, läuft der OData-Workspace lokal gegen die
  getauschten Jars in `~/.m2` (Original im Session-Scratchpad gesichert).
