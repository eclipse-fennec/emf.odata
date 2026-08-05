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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.odata.persistence.api.DeltaGoneException;
import org.eclipse.fennec.odata.persistence.api.DeltaService;
import org.eclipse.fennec.odata.persistence.api.DeltaService.DeltaResult;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Change tracking through the command backend ([OData-Protocol] 11.3): the service-layer
 * journal records every write that succeeded, {@code changesSince} re-queries the touched
 * keys through the read path, membership follows the defining query's filter.
 */
@DisplayName("CommandPersistenceService: DeltaService over the service-layer journal")
class CommandDeltaServiceTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/commanddelta";

	private EPackage pkg;
	private EClass personClass;
	private EAttribute personId;
	private EAttribute personName;
	private EAttribute personAge;
	private EReference personFriend;
	private EReference personColleagues;

	private FakeCommandBackend backend;
	private CommandPersistenceService service;
	private final ODataQueryParser parser = new ODataQueryParser();

	@BeforeEach
	void setUp() {
		buildModel();
		backend = new FakeCommandBackend();
		ResourceSetFactory factory = () -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("fake", backend);
			return resourceSet;
		};
		service = new CommandPersistenceService();
		service.setResourceSetFactory(factory);
		service.activate(Map.of(CommandPersistenceService.URI_PROPERTY, "fake://store",
				CommandPersistenceService.PACKAGES_PROPERTY, NS_URI));
	}

	@Test
	@DisplayName("create/update/delete since the token → upsert + removal, follow-up token is quiet")
	void changesRoundTrip() {
		service.create(personClass, person(1, "Water", 10));
		service.create(personClass, person(2, "Juice", 20));

		String token = service.trackingToken(personClass);
		DeltaResult quiet = service.changesSince(EntityQuery.all(personClass), token);
		assertThat(quiet.changed()).as("nothing changed yet").isEmpty();
		assertThat(quiet.removals()).isEmpty();

		service.update(personClass, "1", named("Sparkling Water"), false);
		service.delete(personClass, "2");
		service.create(personClass, person(3, "Tea", 30));

		DeltaResult delta = service.changesSince(EntityQuery.all(personClass), token);
		assertThat(delta.changed()).extracting(e -> e.eGet(personName))
				.as("updated and created entities appear with their current state")
				.containsExactly("Sparkling Water", "Tea");
		assertThat(delta.removals()).hasSize(1);
		assertThat(delta.removals().get(0).reason()).isEqualTo(DeltaService.REASON_DELETED);
		assertThat(delta.removals().get(0).keyValues()).containsEntry("id", 2);

		DeltaResult next = service.changesSince(EntityQuery.all(personClass), delta.nextToken());
		assertThat(next.changed()).as("the follow-up token is quiet").isEmpty();
		assertThat(next.removals()).isEmpty();
	}

	@Test
	@DisplayName("membership follows the defining filter: leaving → removal(changed), entering → upsert")
	void filterMembership() {
		service.create(personClass, person(1, "Insider", 30));
		service.create(personClass, person(2, "Outsider", 10));
		String token = service.trackingToken(personClass);

		service.update(personClass, "1", aged(10), false); // leaves the tracked result
		service.update(personClass, "2", aged(30), false); // enters it

		EntityQuery tracked = new EntityQuery(personClass,
				parser.parseFilter("age gt 21", personClass), List.of(), 0, -1, false);
		DeltaResult delta = service.changesSince(tracked, token);
		assertThat(delta.changed()).extracting(e -> e.eGet(personId)).containsExactly(2);
		assertThat(delta.removals()).hasSize(1);
		assertThat(delta.removals().get(0).reason()).isEqualTo(DeltaService.REASON_CHANGED);
		assertThat(delta.removals().get(0).keyValues()).containsEntry("id", 1);
	}

	@Test
	@DisplayName("multiple changes to one entity collapse into the latest outcome")
	void changesCollapse() {
		String token = service.trackingToken(personClass);
		service.create(personClass, person(1, "V1", 10));
		service.update(personClass, "1", named("V2"), false);
		service.update(personClass, "1", named("V3"), false);
		service.create(personClass, person(9, "Ghost", 10));
		service.delete(personClass, "9");

		DeltaResult delta = service.changesSince(EntityQuery.all(personClass), token);
		assertThat(delta.changed()).as("three writes, one upsert").hasSize(1);
		assertThat(delta.changed().get(0).eGet(personName)).isEqualTo("V3");
		// created-and-deleted inside the window: a removal the client can apply as a no-op
		assertThat(delta.removals()).hasSize(1);
		assertThat(delta.removals().get(0).reason()).isEqualTo(DeltaService.REASON_DELETED);
	}

	@Test
	@DisplayName("refused or empty writes never reach the journal")
	void noopsStayInvisible() {
		service.create(personClass, person(1, "Stable", 10));
		String token = service.trackingToken(personClass);

		service.update(personClass, "1", empty(), false); // empty PATCH → no command
		service.delete(personClass, "404"); // miss → nothing deleted

		DeltaResult delta = service.changesSince(EntityQuery.all(personClass), token);
		assertThat(delta.changed()).isEmpty();
		assertThat(delta.removals()).isEmpty();
	}

	@Test
	@DisplayName("expanded tracking, single-valued navigation: a member content change reports the owner")
	void expandTrackingSingleValued() {
		EObject member = person(2, "Cable", 10);
		EObject owner = person(1, "Milk", 10);
		owner.eSet(personFriend, member);
		seed(owner, member);
		String token = service.trackingToken(personClass);

		service.update(personClass, "2", named("Golden Cable"), false);

		EntityQuery expanded = new EntityQuery(personClass, null,
				parser.parseFilter("id eq 1", personClass), List.of(), 0, -1, false,
				Set.of("friend"));
		DeltaResult delta = service.changesSince(expanded, token);
		assertThat(delta.changed()).extracting(e -> e.eGet(personId))
				.as("a change INSIDE the expanded navigation reports the owner").contains(1);

		// without $expand the same window reports only the member, which fails the filter
		DeltaResult plain = service.changesSince(new EntityQuery(personClass,
				parser.parseFilter("id eq 1", personClass), List.of(), 0, -1, false), token);
		assertThat(plain.changed()).isEmpty();
	}

	@Test
	@DisplayName("expanded tracking, many-valued navigation: EXISTS over the changed member keys")
	void expandTrackingManyValued() {
		EObject member = person(2, "Cable", 10);
		EObject owner = person(3, "Crate", 10);
		@SuppressWarnings("unchecked")
		List<EObject> colleagues = (List<EObject>) owner.eGet(personColleagues);
		colleagues.add(member);
		seed(owner, member);
		String token = service.trackingToken(personClass);

		service.update(personClass, "2", named("Golden Cable"), false);

		EntityQuery expanded = new EntityQuery(personClass, null,
				parser.parseFilter("id eq 3", personClass), List.of(), 0, -1, false,
				Set.of("colleagues"));
		DeltaResult delta = service.changesSince(expanded, token);
		assertThat(delta.changed()).extracting(e -> e.eGet(personId)).contains(3);
	}

	@Test
	@DisplayName("expanded tracking is claimed without a bound processor (capability unknown)")
	void expandTrackingClaim() {
		assertThat(service.supportsExpandTracking()).isTrue();
	}

	@Test
	@DisplayName("bounded windows page a delta: truncated pages chain to the full result")
	void boundedWindows() {
		String token = service.trackingToken(personClass);
		for (int i = 1; i <= 5; i++) {
			service.create(personClass, person(i, "N" + i, 10));
		}
		List<Object> collected = new ArrayList<>();
		String cursor = token;
		int pages = 0;
		DeltaResult page;
		do {
			page = service.changesSince(EntityQuery.all(personClass), cursor, 2);
			page.changed().forEach(e -> collected.add(e.eGet(personName)));
			cursor = page.nextToken();
			pages++;
		} while (page.truncated());
		assertThat(collected).as("chained pages cover every change exactly once, in order")
				.containsExactly("N1", "N2", "N3", "N4", "N5");
		assertThat(pages).as("5 changes with span 2 → 2+2+1").isEqualTo(3);
	}

	@Test
	@DisplayName("invalid or aged-out tokens raise DeltaGoneException (→ 410)")
	void goneTokens() {
		EntityQuery all = EntityQuery.all(personClass);
		assertThatThrownBy(() -> service.changesSince(all, "not-a-token"))
				.isInstanceOf(DeltaGoneException.class);
		assertThatThrownBy(() -> service.changesSince(all, "-1"))
				.isInstanceOf(DeltaGoneException.class);
		assertThatThrownBy(() -> service.changesSince(all, "999999"))
				.as("a token from the future is not ours").isInstanceOf(DeltaGoneException.class);

		// age a token out of the retention window: > journal capacity subsequent changes
		String token = service.trackingToken(personClass);
		service.create(personClass, person(1, "Churn", 10));
		for (int i = 0; i < 10_001; i++) {
			service.update(personClass, "1", named("Churn " + i), false);
		}
		assertThatThrownBy(() -> service.changesSince(all, token))
				.as("changes were evicted — the client must refetch")
				.isInstanceOf(DeltaGoneException.class);
		assertThat(service.changesSince(all, service.trackingToken(personClass)).changed())
				.as("a fresh token still works").isEmpty();
	}

	private void buildModel() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		pkg = ecore.createEPackage();
		pkg.setName("commanddelta");
		pkg.setNsPrefix("cdelta");
		pkg.setNsURI(NS_URI);

		personClass = ecore.createEClass();
		personClass.setName("Person");
		personId = ecore.createEAttribute();
		personId.setName("id");
		personId.setEType(EcorePackage.Literals.EINT);
		personId.setID(true);
		personName = ecore.createEAttribute();
		personName.setName("name");
		personName.setEType(EcorePackage.Literals.ESTRING);
		personAge = ecore.createEAttribute();
		personAge.setName("age");
		personAge.setEType(EcorePackage.Literals.EINT);
		personFriend = ecore.createEReference();
		personFriend.setName("friend");
		personFriend.setEType(personClass);
		personColleagues = ecore.createEReference();
		personColleagues.setName("colleagues");
		personColleagues.setEType(personClass);
		personColleagues.setUpperBound(-1);
		personClass.getEStructuralFeatures().addAll(List.of(personId, personName, personAge,
				personFriend, personColleagues));

		pkg.getEClassifiers().add(personClass);
	}

	private EObject person(int id, String name, int age) {
		EObject person = empty();
		person.eSet(personId, id);
		person.eSet(personName, name);
		person.eSet(personAge, age);
		return person;
	}

	private EObject named(String name) {
		EObject payload = empty();
		payload.eSet(personName, name);
		return payload;
	}

	private EObject aged(int age) {
		EObject payload = empty();
		payload.eSet(personAge, age);
		return payload;
	}

	private EObject empty() {
		return pkg.getEFactoryInstance().create(personClass);
	}

	/** Seeds linked persons directly into the store — the write path refuses references. */
	private void seed(EObject... persons) {
		Map<Object, EObject> store = backend.storeFor("Person");
		for (EObject person : persons) {
			store.put(person.eGet(personId), person);
		}
	}
}
