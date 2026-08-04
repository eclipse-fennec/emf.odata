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
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.emf.osgi.metadata.MetadataServices;
import org.eclipse.fennec.emf.osgi.metadata.MetadataWhiteboard;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService.WriteResult;
import org.eclipse.fennec.persistence.mongo.MongoResourceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * The SAME {@link CommandPersistenceService} against a real MongoDB: proof that the
 * command write path is backend-neutral. Wiring follows the upstream TCK — the
 * {@code MongoResourceFactory} (API since eclipse-fennec/emf.persistence-jpa#90)
 * is registered for the {@code mongodb} protocol on a plain ResourceSet, the
 * model is served by a {@code MetadataWhiteboard}. Skipped when no MongoDB is
 * available (see {@link MongoSupport}).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Command write path against MongoDB")
public class MongoCommandPersistenceServiceTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/mongocommandshop";

	private MongoClient client;
	private MongoDatabase database;
	private EPackage shopPackage;
	private EClass personClass;
	private EAttribute personId;
	private EAttribute personName;
	private EAttribute personAge;
	private EReference personFriend;
	private CommandPersistenceService service;

	@BeforeAll
	void setUp() {
		String connectionString = MongoSupport.connectionString();
		assumeTrue(connectionString != null,
				"No MongoDB available (set -Dmongo.uri or provide docker/podman)");
		client = MongoClients.create(connectionString);
		database = client.getDatabase("odata_cmd_" + UUID.randomUUID().toString().replace("-", ""));
		buildModel();
		MetadataWhiteboard metadata = MetadataServices.createWhiteboard();
		metadata.registerPackage(shopPackage);
		ResourceSetFactory factory = () -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getPackageRegistry().put(NS_URI, shopPackage);
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap()
					.put("mongodb", new MongoResourceFactory(database, metadata, null));
			return resourceSet;
		};
		service = new CommandPersistenceService();
		service.setResourceSetFactory(factory);
		service.activate(Map.of(CommandPersistenceService.URI_PROPERTY, "mongodb://" + database.getName(),
				CommandPersistenceService.PACKAGES_PROPERTY, NS_URI));
	}

	@AfterAll
	void tearDown() {
		if (database != null) {
			database.drop();
		}
		if (client != null) {
			client.close();
		}
	}

	private void buildModel() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		shopPackage = ecore.createEPackage();
		shopPackage.setName("mongocommandshop");
		shopPackage.setNsPrefix("mcshop");
		shopPackage.setNsURI(NS_URI);

		personClass = ecore.createEClass();
		personClass.setName("Person");
		personId = ecore.createEAttribute();
		personId.setName("id");
		personId.setEType(EcorePackage.Literals.ESTRING);
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
		personClass.getEStructuralFeatures().addAll(List.of(personId, personName, personAge,
				personFriend));
		shopPackage.getEClassifiers().add(personClass);
	}

	private EObject person(String id, String name, int age) {
		EObject person = shopPackage.getEFactoryInstance().create(personClass);
		person.eSet(personId, id);
		if (name != null) {
			person.eSet(personName, name);
		}
		person.eSet(personAge, age);
		return person;
	}

	@Test
	@DisplayName("CRUD round trip through Insert/Update/DeleteCommand on MongoDB")
	void crudRoundTripOnMongo() {
		// POST → InsertCommand (existence pre-check → conflict on the second attempt)
		EObject created = service.create(personClass, person("m1", "Mongo", 30));
		assertThat(created.eGet(personName)).isEqualTo("Mongo");
		assertThatThrownBy(() -> service.create(personClass, person("m1", "Impostor", 99)))
				.isInstanceOf(WriteConflictException.class);

		// PATCH → SET template; untouched attributes survive
		EObject patch = shopPackage.getEFactoryInstance().create(personClass);
		patch.eSet(personName, "Mongo v2");
		WriteResult patched = service.update(personClass, "'m1'", patch, false);
		assertThat(patched.created()).isFalse();
		assertThat(patched.entity().eGet(personName)).isEqualTo("Mongo v2");
		assertThat(patched.entity().eGet(personAge)).isEqualTo(30);

		// PUT → UNSET for omitted attributes
		EObject replacement = shopPackage.getEFactoryInstance().create(personClass);
		replacement.eSet(personName, "Mongo v3");
		WriteResult replaced = service.update(personClass, "'m1'", replacement, true);
		assertThat(replaced.entity().eGet(personName)).isEqualTo("Mongo v3");
		assertThat(replaced.entity().eIsSet(personAge)).isFalse();

		// upsert: unknown key inserts with the URL key
		EObject upsert = shopPackage.getEFactoryInstance().create(personClass);
		upsert.eSet(personName, "Upserted");
		WriteResult upserted = service.update(personClass, "'m2'", upsert, false);
		assertThat(upserted.created()).isTrue();
		assertThat(upserted.entity().eGet(personId)).isEqualTo("m2");

		// reference members stay honest 501s
		EObject withReference = shopPackage.getEFactoryInstance().create(personClass);
		withReference.eSet(personFriend, person("m9", "Ghost", 1));
		assertThatThrownBy(() -> service.update(personClass, "'m1'", withReference, false))
				.isInstanceOf(UnsupportedOperationException.class);

		// DELETE → DeleteCommand; the second attempt reports the miss
		assertThat(service.delete(personClass, "'m1'")).isTrue();
		assertThat(service.delete(personClass, "'m2'")).isTrue();
		assertThat(service.delete(personClass, "'m1'")).isFalse();
	}
}
