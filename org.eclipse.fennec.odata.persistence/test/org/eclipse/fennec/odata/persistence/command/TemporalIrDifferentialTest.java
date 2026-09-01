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
package org.eclipse.fennec.odata.persistence.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.ocl.evaluator.OclEvaluator;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * The temporal half of the {@link ExpressionIrDifferentialTest} corpus, which is
 * string/number/enum-centric and carried no date case at all — which is why nobody
 * noticed that EVERY comparison against a temporal literal answered nothing (#62).
 *
 * <p>Cause of that: OCL has no temporal literal, so {@code ODataToOclBuilder} carries
 * {@code Edm.Date}/{@code Edm.DateTimeOffset}/{@code Edm.TimeOfDay} as a string
 * pre-typed with the Edm name, and the bridge used to drop the type — the engines then
 * compared a {@code String} against a {@code Date}. Never equal, never an error
 * (persistence-jpa#263, fixed in the 2026-09-01 snapshot: {@code OclToExpr} reads the
 * type name and emits a {@code TemporalLiteral}).
 *
 * <p>Every case therefore asserts TWICE: against the expected rows, and against the
 * {@link OclEvaluator} reference. Parity alone would be satisfied by two engines that
 * are wrong in the same way — which is exactly the state this test was written for.
 */
public class TemporalIrDifferentialTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/timeshop";

	private EPackage shopPackage;
	private EClass personClass;
	private EAttribute personId;
	private EAttribute personName;
	private EAttribute personBorn;

	private final ODataQueryParser parser = new ODataQueryParser();
	private final OclEvaluator evaluator = new OclEvaluator();
	private FakeCommandBackend backend;
	private CommandPersistenceService service;
	private List<EObject> seed;

	@BeforeEach
	void setUp() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		shopPackage = ecore.createEPackage();
		shopPackage.setName("timeshop");
		shopPackage.setNsPrefix("time");
		shopPackage.setNsURI(NS_URI);
		personClass = ecore.createEClass();
		personClass.setName("Person");
		personId = attribute(ecore, "id", EcorePackage.Literals.EINT);
		personId.setID(true);
		personName = attribute(ecore, "name", EcorePackage.Literals.ESTRING);
		personBorn = attribute(ecore, "born", EcorePackage.Literals.EDATE);
		personClass.getEStructuralFeatures().addAll(List.of(personId, personName, personBorn));
		shopPackage.getEClassifiers().add(personClass);

		seed = new ArrayList<>();
		seed.add(person(1, "Alice", LocalDate.of(1990, 1, 1), 10, 30, 45));
		seed.add(person(2, "Bob", LocalDate.of(1990, 1, 1), 23, 59, 0));
		seed.add(person(3, "Carol", LocalDate.of(1986, 7, 1), 0, 5, 0));
		seed.add(person(4, "Dave", null, 0, 0, 0));

		backend = new FakeCommandBackend();
		for (EObject person : seed) {
			backend.storeFor("Person").put(person.eGet(personId), person);
		}
		service = new CommandPersistenceService();
		service.setResourceSetFactory(() -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("fake", backend);
			return resourceSet;
		});
		service.activate(Map.of(CommandPersistenceService.URI_PROPERTY, "fake://store",
				CommandPersistenceService.PACKAGES_PROPERTY, NS_URI));
	}

	private static EAttribute attribute(EcoreFactory ecore, String name,
			org.eclipse.emf.ecore.EClassifier type) {
		EAttribute attribute = ecore.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		return attribute;
	}

	/** {@code born} is an {@code EDate}, i.e. a {@code java.util.Date} — a UTC instant. */
	private EObject person(int id, String name, LocalDate day, int hour, int minute, int second) {
		EObject person = shopPackage.getEFactoryInstance().create(personClass);
		person.eSet(personId, id);
		person.eSet(personName, name);
		if (day != null) {
			person.eSet(personBorn,
					Date.from(day.atTime(hour, minute, second).toInstant(ZoneOffset.UTC)));
		}
		return person;
	}

	private record Case(String label, String filter, String orderBy, boolean ordered,
			List<String> expected) {
		Case(String label, String filter, List<String> expected) {
			this(label, filter, null, false, expected);
		}
	}

	@TestFactory
	List<DynamicTest> temporalFilterParity() {
		List<Case> corpus = List.of(
				// the plain attribute against an Edm.DateTimeOffset literal — no function
				// involved, and broken in exactly the same way until #263
				new Case("instant eq", "born eq 1990-01-01T10:30:45Z", List.of("Alice")),
				new Case("instant ge", "born ge 1990-01-01T00:00:00Z", List.of("Alice", "Bob")),
				new Case("instant lt", "born lt 1990-01-01T23:00:00Z", List.of("Alice", "Carol")),
				// date(): the whole day as ONE predicate, which is the point of having it
				new Case("date eq", "date(born) eq 1990-01-01", List.of("Alice", "Bob")),
				new Case("date gt", "date(born) gt 1986-07-01", List.of("Alice", "Bob")),
				new Case("date le", "date(born) le 1986-07-01", List.of("Carol")),
				// Dave has no birthday: 3VL makes the comparison UNKNOWN, not true
				new Case("date ne", "date(born) ne 1990-01-01", List.of("Carol")),
				new Case("time eq", "time(born) eq 23:59:00", List.of("Bob")),
				new Case("time lt", "time(born) lt 10:00:00", List.of("Carol")),
				// the component functions were never broken (their peer is an integer) —
				// they stay in the corpus as the regression guard for the fix
				new Case("year", "year(born) eq 1990", List.of("Alice", "Bob")),
				new Case("hour", "hour(born) eq 23", List.of("Bob")),
				new Case("null probe", "born eq null", List.of("Dave")),
				new Case("ordered by date", "born ne null", "date(born) desc,id asc", true,
						List.of("Alice", "Bob", "Carol")));
		return corpus.stream().map(testCase -> DynamicTest.dynamicTest(testCase.label(),
				() -> assertParity(testCase))).toList();
	}

	private void assertParity(Case testCase) {
		OclExpression filter = testCase.filter() == null ? null
				: parser.parseFilter(testCase.filter(), personClass);
		List<OrderBySegment> orderBy = testCase.orderBy() == null ? List.of()
				: parser.parseOrderBy(testCase.orderBy(), personClass);

		List<String> viaIr = service
				.execute(new EntityQuery(personClass, null, filter, orderBy, 0, -1, false))
				.entities().stream().map(entity -> (String) entity.eGet(personName)).toList();
		List<String> reference = seed.stream()
				.filter(person -> filter == null || evaluator.matchesNullSafe(filter, person))
				.map(person -> (String) person.eGet(personName)).toList();

		if (testCase.ordered()) {
			assertThat(viaIr).as("pushed down").isEqualTo(testCase.expected());
		} else {
			assertThat(viaIr).as("pushed down")
					.containsExactlyInAnyOrderElementsOf(testCase.expected());
		}
		assertThat(reference).as("the OclEvaluator reference must select the same rows")
				.containsExactlyInAnyOrderElementsOf(testCase.expected());
	}
}
