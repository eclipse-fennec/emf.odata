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
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CommandReadServiceTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/readshop";

	private EPackage shopPackage;
	private EClass personClass;
	private EAttribute personId;
	private EAttribute personName;
	private EAttribute personAge;
	private EReference personFriend;

	private final ODataQueryParser parser = new ODataQueryParser();
	private FakeCommandBackend backend;
	private CommandPersistenceService service;

	@BeforeEach
	void setUp() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		shopPackage = ecore.createEPackage();
		shopPackage.setName("readshop");
		shopPackage.setNsPrefix("read");
		shopPackage.setNsURI(NS_URI);
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
		EAttribute personBorn = ecore.createEAttribute();
		personBorn.setName("born");
		personBorn.setEType(EcorePackage.Literals.EDATE);
		personClass.getEStructuralFeatures().add(personBorn);
		personFriend = ecore.createEReference();
		personFriend.setName("friend");
		personFriend.setEType(personClass);
		personClass.getEStructuralFeatures().addAll(List.of(personId, personName, personAge,
				personFriend));
		shopPackage.getEClassifiers().add(personClass);

		backend = new FakeCommandBackend();
		for (int i = 1; i <= 5; i++) {
			EObject person = shopPackage.getEFactoryInstance().create(personClass);
			person.eSet(personId, i);
			person.eSet(personName, "P" + i);
			person.eSet(personAge, 20 + i);
			backend.storeFor("Person").put(i, person);
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

	private List<OrderBySegment> byId() {
		return parser.parseOrderBy("id asc", personClass);
	}

	@Test
	void pagesWithSkipTopAndTotalBeforePaging() {
		QueryResult result = service.execute(new EntityQuery(personClass, null,
				parser.parseFilter("age gt 21", personClass), byId(), 1, 2, true));
		assertThat(result.entities()).extracting(entity -> entity.eGet(personId))
				.containsExactly(3, 4);
		assertThat(result.totalCount()).isEqualTo(4);
	}

	@Test
	void topZeroServesTheCountPath() {
		// GET Set/$count sends top == 0 and reads only the total
		QueryResult result = service.execute(new EntityQuery(personClass, null, null,
				List.of(), 0, 0, true));
		assertThat(result.entities()).isEmpty();
		assertThat(result.totalCount()).isEqualTo(5);
	}

	@Test
	void unboundedReadsAreCappedByTheConfiguredPageSize() {
		CommandPersistenceService capped = new CommandPersistenceService();
		capped.setResourceSetFactory(() -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("fake", backend);
			return resourceSet;
		});
		capped.activate(Map.of(CommandPersistenceService.URI_PROPERTY, "fake://store",
				CommandPersistenceService.PAGE_SIZE_PROPERTY, "2"));
		QueryResult result = capped.execute(new EntityQuery(personClass, null, null, byId(), 0, -1, false));
		assertThat(result.entities()).hasSize(2);
	}

	@Test
	void unsupportedFilterConstructsAreRefusedAs501() {
		// date() is deliberately outside the bridge's blessed subset (evaluator-only)
		assertThatThrownBy(() -> service.execute(new EntityQuery(personClass, null,
				parser.parseFilter("date(born) eq 1990-01-01", personClass), List.of(), 0, -1,
				false)))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void expandResolvesProxiesThroughTheResourceContract() {
		// friend of P1 is a proxy carrying only the target key — like a real backend
		EObject proxy = shopPackage.getEFactoryInstance().create(personClass);
		((InternalEObject) proxy).eSetProxyURI(URI.createURI("fake://store/Person#2"));
		backend.storeFor("Person").get(1).eSet(personFriend, proxy);

		QueryResult result = service.execute(new EntityQuery(personClass, null,
				parser.parseFilter("id eq 1", personClass), List.of(), 0, -1, false,
				Set.of("friend")));
		EObject person = result.entities().get(0);
		EObject friend = (EObject) person.eGet(personFriend);
		assertThat(friend.eIsProxy()).as("the expanded navigation must be materialized").isFalse();
		assertThat(friend.eGet(personName)).isEqualTo("P2");
	}
}
