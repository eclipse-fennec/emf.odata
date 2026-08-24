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

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService.WriteResult;
import org.eclipse.fennec.persistence.helper.CompositeIds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandPersistenceServiceTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/commandshop";

	private EPackage shopPackage;
	private EClass personClass;
	private EAttribute personId;
	private EAttribute personName;
	private EAttribute personAge;
	private EAttribute personTags;
	private EClass addressClass;
	private EAttribute addressStreet;
	private EReference personAddress;
	private EReference personFriend;
	private EReference personColleagues;
	private EClass slotClass;
	private EAttribute slotDay;
	private EAttribute slotRoom;
	private EAttribute slotCapacity;

	private FakeCommandBackend backend;
	private CommandPersistenceService service;

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

	private void buildModel() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		shopPackage = ecore.createEPackage();
		shopPackage.setName("commandshop");
		shopPackage.setNsPrefix("cshop");
		shopPackage.setNsURI(NS_URI);

		addressClass = ecore.createEClass();
		addressClass.setName("Address");
		addressStreet = attribute(ecore, "street", false);
		addressClass.getEStructuralFeatures().add(addressStreet);

		personClass = ecore.createEClass();
		personClass.setName("Person");
		personId = ecore.createEAttribute();
		personId.setName("id");
		personId.setEType(EcorePackage.Literals.EINT);
		personId.setID(true);
		personName = attribute(ecore, "name", false);
		// unsettable, so an explicit JSON null is distinguishable from an omitted member
		personName.setUnsettable(true);
		personAge = ecore.createEAttribute();
		personAge.setName("age");
		personAge.setEType(EcorePackage.Literals.EINT);
		personTags = attribute(ecore, "tags", true);
		personAddress = ecore.createEReference();
		personAddress.setName("address");
		personAddress.setEType(addressClass);
		personAddress.setContainment(true);
		personFriend = ecore.createEReference();
		personFriend.setName("friend");
		personFriend.setEType(personClass);
		personColleagues = ecore.createEReference();
		personColleagues.setName("colleagues");
		personColleagues.setEType(personClass);
		personColleagues.setUpperBound(-1);
		personClass.getEStructuralFeatures().addAll(List.of(personId, personName, personAge,
				personTags, personAddress, personFriend, personColleagues));

		slotClass = ecore.createEClass();
		slotClass.setName("Slot");
		// the canonical composite declaration (persistence-jpa#115): explicit idFeatures,
		// at most one isID — valid Ecore (validateEClass_AtMostOneID)
		EAnnotation identity = ecore.createEAnnotation();
		identity.setSource(CompositeIds.ANNOTATION_SOURCE);
		identity.getDetails().put(CompositeIds.ID_FEATURES, "day,room");
		slotClass.getEAnnotations().add(identity);
		slotDay = attribute(ecore, "day", false);
		slotDay.setID(true);
		slotRoom = attribute(ecore, "room", false);
		slotCapacity = ecore.createEAttribute();
		slotCapacity.setName("capacity");
		slotCapacity.setEType(EcorePackage.Literals.EINT);
		slotClass.getEStructuralFeatures().addAll(List.of(slotDay, slotRoom, slotCapacity));

		shopPackage.getEClassifiers().addAll(List.of(personClass, addressClass, slotClass));
	}

	private static EAttribute attribute(EcoreFactory ecore, String name, boolean many) {
		EAttribute attribute = ecore.createEAttribute();
		attribute.setName(name);
		attribute.setEType(EcorePackage.Literals.ESTRING);
		if (many) {
			attribute.setUpperBound(-1);
		}
		return attribute;
	}

	private EObject person(int id, String name, int age, String... tags) {
		EObject person = shopPackage.getEFactoryInstance().create(personClass);
		person.eSet(personId, id);
		if (name != null) {
			person.eSet(personName, name);
		}
		if (age >= 0) {
			person.eSet(personAge, age);
		}
		if (tags.length > 0) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) person.eGet(personTags);
			list.addAll(List.of(tags));
		}
		return person;
	}

	private EObject stored(int id) {
		return backend.storeFor("Person").get(id);
	}

	@Test
	void supportsHonorsPackageFilterAndKeys() {
		assertThat(service.supports(personClass)).isTrue();
		// Address has no id attribute
		assertThat(service.supports(addressClass)).isFalse();
		// foreign package
		assertThat(service.supports(EcorePackage.Literals.EANNOTATION)).isFalse();
	}

	@Test
	void createStoresThePayloadAndReturnsTheStoredState() {
		EObject created = service.create(personClass, person(1, "Ada", 36));
		assertThat(created.eGet(personName)).isEqualTo("Ada");
		assertThat(stored(1)).isNotNull();
		assertThat(stored(1).eGet(personAge)).isEqualTo(36);
	}

	@Test
	void createWithExistingKeyIsRefusedWith409() {
		service.create(personClass, person(1, "Ada", 36));
		assertThatThrownBy(() -> service.create(personClass, person(1, "Impostor", 99)))
				.isInstanceOf(WriteConflictException.class);
	}

	@Test
	void createRequiresTheKeyInThePayload() {
		EObject keyless = shopPackage.getEFactoryInstance().create(personClass);
		keyless.eSet(personName, "Nobody");
		assertThatThrownBy(() -> service.create(personClass, keyless))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("key");
	}

	@Test
	void createAllowsContainmentChildren() {
		EObject payload = person(1, "Ada", 36);
		EObject address = shopPackage.getEFactoryInstance().create(addressClass);
		address.eSet(addressStreet, "Lovelace Lane 1");
		payload.eSet(personAddress, address);
		service.create(personClass, payload);
		EObject storedAddress = (EObject) stored(1).eGet(personAddress);
		assertThat(storedAddress.eGet(addressStreet)).isEqualTo("Lovelace Lane 1");
	}

	@Test
	void createBindsNonContainmentMembersByKey() {
		service.create(personClass, person(1, "Ada", 36));
		EObject payload = person(2, "Grace", 40);
		payload.eSet(personFriend, friendStub(1)); // id-stub → bound to the EXISTING Ada
		service.create(personClass, payload);
		EObject stored = stored(2);
		assertThat(((EObject) stored.eGet(personFriend)).eGet(personName)).isEqualTo("Ada");

		// a dangling target is a client error, not a silent insert
		EObject dangling = person(3, "Ghost", 1);
		dangling.eSet(personFriend, friendStub(99));
		assertThatThrownBy(() -> service.create(personClass, dangling))
				.isInstanceOf(IllegalArgumentException.class);
	}

	private EObject friendStub(int id) {
		EObject stub = shopPackage.getEFactoryInstance().create(personClass);
		stub.eSet(personId, id);
		return stub;
	}

	@Test
	void deleteRemovesByKeyAndReportsMisses() {
		service.create(personClass, person(1, "Ada", 36));
		assertThat(service.delete(personClass, "1")).isTrue();
		assertThat(stored(1)).isNull();
		assertThat(service.delete(personClass, "1")).isFalse();
	}

	/**
	 * A refusal a client can act on must not arrive as a 500 (#43). The backend refuses with
	 * {@code CODE_REFERENTIAL_INTEGRITY} on the diagnostic (persistence-jpa#229) — the code is
	 * what this routes on, because the one contract has three wordings upstream — and the
	 * service turns it into the conflict the servlet answers with 409.
	 */
	@Test
	void deletingAStillReferencedEntityConflictsInsteadOfFailing() {
		service.create(personClass, person(1, "Ada", 36));
		EObject grace = person(2, "Grace", 45);
		grace.eSet(personFriend, friendStub(1));
		service.create(personClass, grace);

		assertThatThrownBy(() -> service.delete(personClass, "1"))
				.isInstanceOf(WriteConflictException.class)
				.hasMessageContaining("Person")
				.hasMessageContaining("references")
				// the backend's own wording (and any JPQL in it) stays out of the answer
				.hasMessageNotContaining("Cannot delete");
		assertThat(stored(1)).isNotNull();

		// the referrer goes first, then the target — the ordinary way out for the client
		assertThat(service.delete(personClass, "2")).isTrue();
		assertThat(service.delete(personClass, "1")).isTrue();
	}

	/**
	 * Upstream documents this at its own guard: a referrer among the matched objects counts,
	 * so an entity pointing at itself cannot be deleted. Pinned so the answer is a documented
	 * 409 rather than a surprise.
	 */
	@Test
	void deletingASelfReferencingEntityConflicts() {
		// the link comes after the insert: an insert cannot bind a target that does not
		// exist yet, not even itself (upstream refuses the dangling target)
		service.create(personClass, person(1, "Narcissus", 30));
		service.link(personClass, "1", "friend", "1");

		assertThatThrownBy(() -> service.delete(personClass, "1"))
				.isInstanceOf(WriteConflictException.class);
	}

	@Test
	void patchChangesOnlyTransmittedAttributes() {
		service.create(personClass, person(1, "Ada", 36, "math"));
		EObject patch = shopPackage.getEFactoryInstance().create(personClass);
		patch.eSet(personName, "Ada Lovelace");
		WriteResult result = service.update(personClass, "1", patch, false);
		assertThat(result.created()).isFalse();
		assertThat(stored(1).eGet(personName)).isEqualTo("Ada Lovelace");
		assertThat(stored(1).eGet(personAge)).isEqualTo(36);
		assertThat(stored(1).eGet(personTags)).isEqualTo(List.of("math"));
	}

	@Test
	void patchWithExplicitNullClearsTheValue() {
		service.create(personClass, person(1, "Ada", 36));
		EObject patch = shopPackage.getEFactoryInstance().create(personClass);
		patch.eSet(personName, null);
		service.update(personClass, "1", patch, false);
		assertThat(stored(1).eGet(personName)).isNull();
	}

	@Test
	void putUnsetsOmittedAttributes() {
		service.create(personClass, person(1, "Ada", 36, "math", "pioneer"));
		EObject replacement = shopPackage.getEFactoryInstance().create(personClass);
		replacement.eSet(personName, "Ada");
		WriteResult result = service.update(personClass, "1", replacement, true);
		assertThat(result.created()).isFalse();
		assertThat(stored(1).eGet(personName)).isEqualTo("Ada");
		assertThat(stored(1).eIsSet(personAge)).isFalse();
		assertThat(stored(1).eGet(personTags)).isEqualTo(List.of());
	}

	@Test
	void manyValuedAttributesAreRewrittenDeterministically() {
		service.create(personClass, person(1, "Ada", 36, "math", "pioneer"));
		EObject patch = shopPackage.getEFactoryInstance().create(personClass);
		@SuppressWarnings("unchecked")
		List<String> tags = (List<String>) patch.eGet(personTags);
		tags.addAll(List.of("visionary", "math"));
		service.update(personClass, "1", patch, false);
		assertThat(stored(1).eGet(personTags)).isEqualTo(List.of("visionary", "math"));
	}

	@Test
	void updateOfUnknownKeyUpsertsWithTheUrlKey() {
		EObject payload = shopPackage.getEFactoryInstance().create(personClass);
		payload.eSet(personId, 99); // must lose against the URL key
		payload.eSet(personName, "Grace");
		WriteResult result = service.update(personClass, "7", payload, false);
		assertThat(result.created()).isTrue();
		assertThat(stored(7)).isNotNull();
		assertThat(stored(99)).isNull();
		assertThat(result.entity().eGet(personName)).isEqualTo("Grace");
	}

	@Test
	void updatePatchesReferenceMembersByKey() {
		service.create(personClass, person(1, "Ada", 36));
		service.create(personClass, person(2, "Grace", 40));
		EObject patch = shopPackage.getEFactoryInstance().create(personClass);
		patch.eSet(personFriend, friendStub(2));
		service.update(personClass, "1", patch, false);
		assertThat(((EObject) stored(1).eGet(personFriend)).eGet(personName)).isEqualTo("Grace");

		// explicit null clears; a dangling target refuses as a client error
		EObject clear = shopPackage.getEFactoryInstance().create(personClass);
		clear.eSet(personFriend, friendStub(99));
		assertThatThrownBy(() -> service.update(personClass, "1", clear, false))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void updateWithoutChangesIsANoOp() {
		service.create(personClass, person(1, "Ada", 36));
		EObject empty = shopPackage.getEFactoryInstance().create(personClass);
		WriteResult result = service.update(personClass, "1", empty, false);
		assertThat(result.created()).isFalse();
		assertThat(result.entity().eGet(personName)).isEqualTo("Ada");
	}

	@Test
	void stringKeysAreUnquoted() {
		EPackage stringPackage = EcoreFactory.eINSTANCE.createEPackage();
		stringPackage.setName("strings");
		stringPackage.setNsPrefix("str");
		stringPackage.setNsURI(NS_URI + "/strings");
		EClass tagClass = EcoreFactory.eINSTANCE.createEClass();
		tagClass.setName("Tag");
		EAttribute tagId = attribute(EcoreFactory.eINSTANCE, "id", false);
		tagId.setID(true);
		tagClass.getEStructuralFeatures().add(tagId);
		stringPackage.getEClassifiers().add(tagClass);
		CommandPersistenceService stringService = new CommandPersistenceService();
		stringService.setResourceSetFactory(() -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("fake", backend);
			return resourceSet;
		});
		stringService.activate(Map.of(CommandPersistenceService.URI_PROPERTY, "fake://store"));

		EObject tag = stringPackage.getEFactoryInstance().create(tagClass);
		tag.eSet(tagId, "it's");
		stringService.create(tagClass, tag);
		assertThat(backend.storeFor("Tag").get("it's")).isNotNull();
		assertThat(stringService.delete(tagClass, "'it''s'")).isTrue();
		assertThat(backend.storeFor("Tag")).isEmpty();
	}

	@Test
	void linkUnlinkAndCreateRelated() {
		service.create(personClass, person(1, "Ada", 36));
		service.create(personClass, person(2, "Grace", 40));

		service.link(personClass, "1", "friend", "2"); // single-valued → SET
		assertThat(((EObject) stored(1).eGet(personFriend)).eGet(personName)).isEqualTo("Grace");
		assertThat(service.unlink(personClass, "1", "friend", null)).isTrue();
		assertThat(stored(1).eGet(personFriend)).isNull();

		service.link(personClass, "1", "colleagues", "2"); // many-valued → ADD/REMOVE by id
		assertThat((List<?>) stored(1).eGet(personColleagues)).hasSize(1);
		assertThat(service.unlink(personClass, "1", "colleagues", "2")).isTrue();
		assertThat(service.unlink(personClass, "1", "colleagues", "2"))
				.as("the second unlink finds no member").isFalse();

		EObject related = service.createRelated(personClass, "1", "friend", person(5, "New", 20));
		assertThat(related.eGet(personId)).isEqualTo(5);
		assertThat(((EObject) stored(1).eGet(personFriend)).eGet(personId)).isEqualTo(5);

		assertThatThrownBy(() -> service.link(personClass, "1", "address", "2"))
				.as("containments are no navigations for $ref")
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void compositeKeysRideTheFragmentContract() {
		assertThat(service.supports(slotClass)).isTrue();

		EObject slot = shopPackage.getEFactoryInstance().create(slotClass);
		slot.eSet(slotDay, "mo");
		slot.eSet(slotRoom, "r1");
		slot.eSet(slotCapacity, 5);
		service.create(slotClass, slot);
		assertThat(backend.storeFor("Slot")).containsKey("day=mo,room=r1");

		EObject patch = shopPackage.getEFactoryInstance().create(slotClass);
		patch.eSet(slotCapacity, 9);
		WriteResult patched = service.update(slotClass,
				Map.of("day", "'mo'", "room", "'r1'"), patch, false);
		assertThat(patched.created()).isFalse();
		assertThat(patched.entity().eGet(slotCapacity)).isEqualTo(9);

		// a single raw literal cannot address a composite key — client error, not guesswork
		assertThatThrownBy(() -> service.delete(slotClass, "'mo'"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("composite");
		assertThat(service.delete(slotClass, Map.of("day", "'mo'", "room", "'r1'"))).isTrue();
		assertThat(backend.storeFor("Slot")).isEmpty();
	}

	@Test
	void batchBracketsAreAtomic() {
		assertThat(service.transactional()).as("the backend supports command brackets").isTrue();

		service.begin();
		service.create(personClass, person(1, "Rolled back", 1));
		service.rollback();
		assertThat(stored(1)).as("a rolled-back bracket never happened").isNull();

		service.begin();
		service.create(personClass, person(2, "Committed", 2));
		service.update(personClass, "2", named("Committed v2"), false);
		service.commit();
		assertThat(stored(2).eGet(personName))
				.as("the update saw the bracket's own uncommitted insert")
				.isEqualTo("Committed v2");
	}

	private EObject named(String name) {
		EObject payload = shopPackage.getEFactoryInstance().create(personClass);
		payload.eSet(personName, name);
		return payload;
	}

	@Test
	void activationRequiresTheBackendUri() {
		CommandPersistenceService unconfigured = new CommandPersistenceService();
		assertThatThrownBy(() -> unconfigured.activate(Map.of()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(CommandPersistenceService.URI_PROPERTY);
	}
}
