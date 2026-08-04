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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandWriteServiceTest {

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

	private FakeCommandBackend backend;
	private CommandWriteService service;

	@BeforeEach
	void setUp() {
		buildModel();
		backend = new FakeCommandBackend();
		ResourceSetFactory factory = () -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("fake", backend);
			return resourceSet;
		};
		service = new CommandWriteService();
		service.setResourceSetFactory(factory);
		service.activate(Map.of(CommandWriteService.URI_PROPERTY, "fake://store",
				CommandWriteService.PACKAGES_PROPERTY, NS_URI));
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
		personClass.getEStructuralFeatures().addAll(List.of(personId, personName, personAge,
				personTags, personAddress, personFriend));

		shopPackage.getEClassifiers().addAll(List.of(personClass, addressClass));
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
	void createRefusesNonContainmentMembers() {
		EObject payload = person(2, "Grace", 40);
		payload.eSet(personFriend, person(1, "Ada", 36));
		assertThatThrownBy(() -> service.create(personClass, payload))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("friend");
	}

	@Test
	void deleteRemovesByKeyAndReportsMisses() {
		service.create(personClass, person(1, "Ada", 36));
		assertThat(service.delete(personClass, "1")).isTrue();
		assertThat(stored(1)).isNull();
		assertThat(service.delete(personClass, "1")).isFalse();
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
	void updateRefusesReferenceMembers() {
		service.create(personClass, person(1, "Ada", 36));
		EObject patch = shopPackage.getEFactoryInstance().create(personClass);
		patch.eSet(personFriend, person(2, "Grace", 40));
		assertThatThrownBy(() -> service.update(personClass, "1", patch, false))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("reference");
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
		CommandWriteService stringService = new CommandWriteService();
		stringService.setResourceSetFactory(() -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("fake", backend);
			return resourceSet;
		});
		stringService.activate(Map.of(CommandWriteService.URI_PROPERTY, "fake://store"));

		EObject tag = stringPackage.getEFactoryInstance().create(tagClass);
		tag.eSet(tagId, "it's");
		stringService.create(tagClass, tag);
		assertThat(backend.storeFor("Tag").get("it's")).isNotNull();
		assertThat(stringService.delete(tagClass, "'it''s'")).isTrue();
		assertThat(backend.storeFor("Tag")).isEmpty();
	}

	@Test
	void reportsNoBatchTransactionSupport() {
		assertThat(service.transactional()).isFalse();
	}

	@Test
	void activationRequiresTheBackendUri() {
		CommandWriteService unconfigured = new CommandWriteService();
		assertThatThrownBy(() -> unconfigured.activate(Map.of()))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining(CommandWriteService.URI_PROPERTY);
	}
}
