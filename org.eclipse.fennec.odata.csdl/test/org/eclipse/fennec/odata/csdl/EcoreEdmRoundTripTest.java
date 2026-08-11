/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package org.eclipse.fennec.odata.csdl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TAction;
import org.open.oasis.docs.odata.ns.edm.TComplexType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TEntitySet;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TEnumType;
import org.open.oasis.docs.odata.ns.edm.TFunction;
import org.open.oasis.docs.odata.ns.edm.TNavigationProperty;
import org.open.oasis.docs.odata.ns.edm.TProperty;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;

/**
 * E2 converter test — drives the bidirectional Ecore↔EDM converter
 * ({@link EcoreToEdmConverter} → {@link EdmToEcoreConverter}) with a deliberately non-trivial
 * model loaded from {@code testdata/company.ecore} via the Fennec {@link EcoreHelper} (instead
 * of a hand-built EPackage). The model exercises: a 3-level entity inheritance chain with an
 * abstract root, a composite key, complex types (including nesting and a collection-valued
 * complex member), enum-typed attributes, and three kinds of bidirectional reference
 * (self-referential 1:N, N:M, and a containment 1:N) plus a single navigation without partner.
 */
class EcoreEdmRoundTripTest {

	private EcoreHelper ecoreHelper;
	private EPackage model;

	@BeforeEach
	void loadModel() throws Exception {
		Path ecore = findResource(
				"testdata/company.ecore",
				"org.eclipse.fennec.odata.csdl/testdata/company.ecore");
		ecoreHelper = new EcoreHelper();
		model = ecoreHelper.loadEcore(ecore);
		assertTrue(model.eResource().getErrors().isEmpty(), "company.ecore must load cleanly");
	}

	@AfterEach
	void cleanup() {
		if (ecoreHelper != null) {
			ecoreHelper.releaseAll();
		}
	}

	// ============================================================ forward (Ecore -> EDM)

	@Test
	void ecoreToEdmProducesExpectedShape() {
		SchemaType schema = new EcoreToEdmConverter().toSchema(model);

		assertEquals("company", schema.getNamespace());

		// --- enums ---
		TEnumType priority = byName(schema.getEnumType(), TEnumType::getName, "Priority");
		assertEquals(4, priority.getMember().size());
		assertEquals(3L, byName(priority.getMember(),
				org.open.oasis.docs.odata.ns.edm.TEnumTypeMember::getName, "CRITICAL").getValue());
		assertTrue(names(schema.getEnumType(), TEnumType::getName).containsAll(Set.of("Priority", "Department")));

		// --- complex types: keyless EClasses, never entities ---
		assertEquals(Set.of("Address", "GeoLocation", "ContactInfo"),
				names(schema.getComplexType(), TComplexType::getName));

		// --- entity types: all 6 (incl. the abstract root) ---
		assertEquals(Set.of("AbstractEntity", "Person", "Employee", "Project", "Milestone", "RegionCode"),
				names(schema.getEntityType(), TEntityType::getName));

		// abstract root is flagged Abstract and gets NO entity set
		TEntityType abstractEntity = byName(schema.getEntityType(), TEntityType::getName, "AbstractEntity");
		assertTrue(abstractEntity.isAbstract(), "AbstractEntity is Abstract");
		assertEquals("id", abstractEntity.getKey().get(0).getPropertyRef().get(0).getName(), "key on the root type");

		TEntityContainer container = schema.getEntityContainer().get(0);
		assertEquals(Set.of("Person", "Employee", "Project", "Milestone", "RegionCode"),
				names(container.getEntitySet(), TEntitySet::getName), "no set for the abstract type");

		// --- composite key ---
		TEntityType region = byName(schema.getEntityType(), TEntityType::getName, "RegionCode");
		assertEquals(List.of("countryCode", "regionCode"),
				region.getKey().get(0).getPropertyRef().stream().map(r -> r.getName()).toList());

		// --- inheritance: a subtype carries only its own key segment (here: none) ---
		TEntityType person = byName(schema.getEntityType(), TEntityType::getName, "Person");
		assertTrue(person.getKey().isEmpty(), "Person inherits the key, declares none");
		assertTrue(person.getBaseType().endsWith(".AbstractEntity"));
		assertTrue(byName(schema.getEntityType(), TEntityType::getName, "Employee").getBaseType().endsWith(".Person"));

		// --- complex-typed members become Properties, NOT navigation ---
		assertTrue(names(person.getProperty(), TProperty::getName).containsAll(Set.of("contact", "previousAddresses")),
				"complex members are structural Properties");
		assertTrue(names(person.getNavigationProperty(), TNavigationProperty::getName).isEmpty(),
				"Person has no entity navigation");
		assertTrue(typeOf(byName(person.getProperty(), TProperty::getName, "contact")).endsWith(".ContactInfo"));
		assertTrue(typeOf(byName(person.getProperty(), TProperty::getName, "previousAddresses")).contains("Collection("),
				"previousAddresses is a collection of a complex type");
		TComplexType contactInfo = byName(schema.getComplexType(), TComplexType::getName, "ContactInfo");
		assertTrue(contactInfo.getNavigationProperty().isEmpty(), "complex types contain no navigation");
		assertTrue(names(contactInfo.getProperty(), TProperty::getName).containsAll(Set.of("address", "location", "phones")));

		// --- enum-typed attributes ---
		assertTrue(typeOf(byName(byName(schema.getEntityType(), TEntityType::getName, "Employee").getProperty(),
				TProperty::getName, "department")).endsWith(".Department"));
		assertTrue(typeOf(byName(byName(schema.getEntityType(), TEntityType::getName, "Project").getProperty(),
				TProperty::getName, "priority")).endsWith(".Priority"));

		// --- navigation: self 1:N, N:M, containment, and partnerless single ---
		TEntityType employee = byName(schema.getEntityType(), TEntityType::getName, "Employee");
		TNavigationProperty manager = byName(employee.getNavigationProperty(), TNavigationProperty::getName, "manager");
		assertFalse(typeOf(manager).contains("Collection("), "manager is single");
		assertTrue(manager.isNullable());
		assertEquals("reports", manager.getPartner());
		TNavigationProperty reports = byName(employee.getNavigationProperty(), TNavigationProperty::getName, "reports");
		assertTrue(typeOf(reports).contains("Collection("));
		assertEquals("manager", reports.getPartner());
		assertEquals("team", byName(employee.getNavigationProperty(), TNavigationProperty::getName, "projects").getPartner());

		TEntityType project = byName(schema.getEntityType(), TEntityType::getName, "Project");
		assertEquals("projects", byName(project.getNavigationProperty(), TNavigationProperty::getName, "team").getPartner());
		assertNull(byName(project.getNavigationProperty(), TNavigationProperty::getName, "lead").getPartner(),
				"lead has no opposite");
		TNavigationProperty milestones = byName(project.getNavigationProperty(), TNavigationProperty::getName, "milestones");
		assertTrue(milestones.isContainsTarget(), "milestones is a containment navigation");
		assertEquals("project", milestones.getPartner());

		TEntityType milestone = byName(schema.getEntityType(), TEntityType::getName, "Milestone");
		TNavigationProperty backToProject = byName(milestone.getNavigationProperty(), TNavigationProperty::getName, "project");
		assertFalse(backToProject.isContainsTarget(), "the container side is not contains-target");
		assertFalse(backToProject.isNullable(), "Milestone.project is mandatory (lowerBound 1)");
		assertEquals("milestones", backToProject.getPartner());

		// --- bound operations from EOperations (binding parameter synthesized first) ---
		TAction raise = byName(schema.getAction(), TAction::getName, "raiseSalary");
		assertTrue(raise.isIsBound(), "raiseSalary is a bound Action (void return)");
		assertEquals("bindingParameter", raise.getParameter().get(0).getName(), "binding parameter is first");
		assertTrue(String.valueOf(raise.getParameter().get(0).getType()).endsWith(".Employee"), "bound to Employee");
		assertEquals(2, raise.getParameter().size(), "binding + amount");
		assertTrue(raise.getReturnType().isEmpty(), "void Action has no return type");

		TFunction current = byName(schema.getFunction(), TFunction::getName, "currentProjects");
		assertTrue(current.isIsBound(), "currentProjects is a bound Function");
		assertNotNull(current.getReturnType(), "a Function must declare a return type");
		assertTrue(String.valueOf(current.getReturnType().getType()).contains("Collection("), "returns a collection");
		assertTrue(String.valueOf(current.getReturnType().getType()).contains(".Project"), "of Project");
		assertEquals(1, current.getParameter().size(), "only the synthesized binding parameter");
	}

	// ============================================================ round trip (Ecore -> EDM -> Ecore)

	@Test
	void singletonsRoundTrip() {
		EAnnotation ann = EcoreFactory.eINSTANCE.createEAnnotation();
		ann.setSource(ODataAnnotationConstants.SINGLETONS_SOURCE);
		ann.getDetails().put("Me", "Person");
		model.getEAnnotations().add(ann);

		TEntityContainer container = new EcoreToEdmConverter().toSchema(model)
				.getEntityContainer().get(0);
		assertEquals(1, container.getSingleton().size(), "one <Singleton> emitted");
		assertEquals("Me", container.getSingleton().get(0).getName());
		assertEquals("company.Person", container.getSingleton().get(0).getType());

		EPackage rt = new EdmToEcoreConverter().toEPackage(new EcoreToEdmConverter().toEdmx(model));
		EAnnotation roundTripped = rt.getEAnnotation(ODataAnnotationConstants.SINGLETONS_SOURCE);
		assertNotNull(roundTripped, "singleton annotation survives the round trip");
		assertEquals("Person", roundTripped.getDetails().get("Me"));
	}

	@Test
	void entitySetNamesRoundTrip() {
		// [OData-CSDL] 13.2: the entity set's Name is independent of its type's name
		EAnnotation ann = EcoreFactory.eINSTANCE.createEAnnotation();
		ann.setSource(ODataAnnotationConstants.ENTITY_SETS_SOURCE);
		ann.getDetails().put("Staff", "Person");
		model.getEAnnotations().add(ann);

		TEntityContainer container = new EcoreToEdmConverter().toSchema(model)
				.getEntityContainer().get(0);
		assertTrue(container.getEntitySet().stream().anyMatch(s -> "Staff".equals(s.getName())),
				"the set is emitted under its declared name");
		assertTrue(container.getEntitySet().stream().noneMatch(s -> "Person".equals(s.getName())),
				"the type-named default disappears for the renamed set");
		assertTrue(container.getEntitySet().stream()
				.flatMap(s -> s.getNavigationPropertyBinding().stream())
				.noneMatch(b -> "Person".equals(String.valueOf(b.getTarget()))),
				"binding targets follow the rename");

		EPackage rt = new EdmToEcoreConverter().toEPackage(new EcoreToEdmConverter().toEdmx(model));
		EAnnotation roundTripped = rt.getEAnnotation(ODataAnnotationConstants.ENTITY_SETS_SOURCE);
		assertNotNull(roundTripped, "the set mapping survives the round trip");
		assertEquals("Person", roundTripped.getDetails().get("Staff"));
	}

	@Test
	void roundTripsBackToEcore() {
		EdmxRoot edmx = new EcoreToEdmConverter().toEdmx(model);
		EPackage rt = new EdmToEcoreConverter().toEPackage(edmx);

		assertEquals("company", rt.getName());

		// --- enums ---
		EEnum priority = (EEnum) classifier(rt, "Priority");
		assertEquals(4, priority.getELiterals().size());
		assertEquals(3, priority.getEEnumLiteral("CRITICAL").getValue());

		// --- abstract + inheritance chain ---
		EClass abstractEntity = (EClass) classifier(rt, "AbstractEntity");
		assertTrue(abstractEntity.isAbstract(), "abstract flag round-trips");
		EClass person = (EClass) classifier(rt, "Person");
		EClass employee = (EClass) classifier(rt, "Employee");
		assertFalse(person.isAbstract(), "Person is concrete");
		assertTrue(person.getESuperTypes().contains(abstractEntity), "Person -> AbstractEntity");
		assertTrue(employee.getESuperTypes().contains(person), "Employee -> Person");
		assertTrue(((EClass) classifier(rt, "Project")).getESuperTypes().contains(abstractEntity));

		// --- complex vs entity: asserted on a second forward pass, since the resolved profile (not
		// an isID scan) is what classifies a type ---
		SchemaType again = new EcoreToEdmConverter().toSchema(rt);
		assertTrue(names(again.getComplexType(), TComplexType::getName).contains("Address"),
				"Address stays complex");
		assertTrue(names(again.getEntityType(), TEntityType::getName).contains("Person"),
				"Person stays an entity");

		// --- key: inherited single + composite ---
		EAttribute id = (EAttribute) abstractEntity.getEStructuralFeature("id");
		assertTrue(id.isID(), "id is the key");
		assertTrue(((EAttribute) person.getEStructuralFeature("id")).isID(), "Person inherits the key attribute");
		// a multi-part key comes back as the one identity declaration, in canonical key order —
		// never as several isID attributes, which Ecore does not allow (persistence-jpa#115)
		EClass region = (EClass) classifier(rt, "RegionCode");
		EAnnotation identity = region.getEAnnotation(ODataAnnotationConstants.IDENTITY_SOURCE);
		assertNotNull(identity, "the composite identity survives the round trip");
		assertEquals("countryCode,regionCode",
				identity.getDetails().get(ODataAnnotationConstants.ID_FEATURES));
		assertFalse(((EAttribute) region.getEStructuralFeature("countryCode")).isID());
		assertFalse(((EAttribute) region.getEStructuralFeature("regionCode")).isID());

		// --- complex-typed members come back as containment references ---
		EReference contact = (EReference) person.getEStructuralFeature("contact");
		assertTrue(contact.isContainment());
		assertFalse(contact.isMany());
		assertEquals(classifier(rt, "ContactInfo"), contact.getEType());
		EReference prevAddresses = (EReference) person.getEStructuralFeature("previousAddresses");
		assertTrue(prevAddresses.isMany() && prevAddresses.isContainment());
		assertEquals(classifier(rt, "Address"), prevAddresses.getEType());

		// nested complex + primitive collection
		EClass contactInfo = (EClass) classifier(rt, "ContactInfo");
		assertEquals(classifier(rt, "Address"), ((EReference) contactInfo.getEStructuralFeature("address")).getEType());
		assertTrue(((EReference) contactInfo.getEStructuralFeature("location")).isContainment());
		assertTrue(((EAttribute) contactInfo.getEStructuralFeature("phones")).isMany(), "phones stays a collection");

		// --- enum-typed attributes re-resolve to the EEnum ---
		assertSame(priority, ((EAttribute) ((EClass) classifier(rt, "Project"))
				.getEStructuralFeature("priority")).getEType());
		assertSame(classifier(rt, "Department"), ((EAttribute) employee.getEStructuralFeature("department")).getEType());

		// --- THE bidirectional references: eOpposite must survive the trip ---
		EReference manager = (EReference) employee.getEStructuralFeature("manager");
		EReference reports = (EReference) employee.getEStructuralFeature("reports");
		assertSame(reports, manager.getEOpposite(), "manager <-> reports (self 1:N)");
		assertSame(manager, reports.getEOpposite());

		EReference projects = (EReference) employee.getEStructuralFeature("projects");
		EClass project = (EClass) classifier(rt, "Project");
		EReference team = (EReference) project.getEStructuralFeature("team");
		assertSame(team, projects.getEOpposite(), "projects <-> team (N:M)");
		assertSame(projects, team.getEOpposite());

		EReference milestones = (EReference) project.getEStructuralFeature("milestones");
		EReference backToProject = (EReference) ((EClass) classifier(rt, "Milestone")).getEStructuralFeature("project");
		assertTrue(milestones.isContainment(), "milestones stays containment");
		assertSame(backToProject, milestones.getEOpposite(), "milestones <-> project (containment 1:N)");
		assertSame(milestones, backToProject.getEOpposite());

		assertNull(((EReference) project.getEStructuralFeature("lead")).getEOpposite(), "lead has no opposite");

		// --- multiplicity / nullability ---
		assertFalse(manager.isMany());
		assertTrue(reports.isMany());
		assertEquals(1, backToProject.getLowerBound(), "Milestone.project is mandatory");
		assertEquals(0, abstractEntity.getEStructuralFeature("createdAt").getLowerBound(), "createdAt stays optional");
		assertEquals(EcorePackage.eINSTANCE.getEString(), id.getEType());
	}

	// ============================================================ helpers

	private static String typeOf(Object navOrProp) {
		if (navOrProp instanceof TNavigationProperty n) {
			return String.valueOf(n.getType());
		}
		if (navOrProp instanceof TProperty p) {
			return String.valueOf(p.getType());
		}
		return String.valueOf(navOrProp);
	}

	private static <T> Set<String> names(List<T> list, java.util.function.Function<T, String> nameFn) {
		return list.stream().map(nameFn).collect(Collectors.toSet());
	}

	private static <T> T byName(List<T> list, java.util.function.Function<T, String> nameFn, String name) {
		return list.stream().filter(t -> name.equals(nameFn.apply(t))).findFirst()
				.orElseThrow(() -> new AssertionError("not found: " + name));
	}

	private static EClassifier classifier(EPackage pkg, String name) {
		EClassifier c = pkg.getEClassifier(name);
		assertNotNull(c, name);
		return c;
	}

	/** Walk up from the working dir trying each candidate relative path (cwd may be project or workspace root). */
	private static Path findResource(String... candidatesRelative) {
		Path start = Paths.get("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidatesRelative) {
				Path p = dir.resolve(rel);
				if (Files.exists(p)) {
					return p;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
