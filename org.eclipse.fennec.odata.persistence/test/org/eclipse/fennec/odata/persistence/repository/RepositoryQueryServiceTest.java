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
package org.eclipse.fennec.odata.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.common.util.URI;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.eclipse.fennec.persistence.capabilities.CommandCapabilitiesBuilder;
import org.eclipse.fennec.persistence.capabilities.PersistenceCapabilities;
import org.eclipse.fennec.persistence.capabilities.QueryCapabilities;
import org.eclipse.fennec.persistence.capabilities.QueryCapabilitiesBuilder;
import org.eclipse.fennec.persistence.capabilities.QueryFeature;
import org.eclipse.fennec.persistence.capabilities.StoreCapabilitiesBuilder;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.memory.MemoryQueries;
import org.eclipse.fennec.persistence.query.memory.MemoryQueryProcessor;
import org.eclipse.fennec.persistence.repository.api.ReadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.service.component.ComponentServiceObjects;

/**
 * The repository backend against a {@link ReadRepository} double whose {@code find}/{@code count}
 * run the IR through the reference memory engine: the OData read arrives as ONE pushed-down
 * query, capabilities gate what is asked, every request takes and returns its own
 * prototype-scoped instance, and the objects survive the instance.
 */
class RepositoryQueryServiceTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/reposhop";

	private EPackage shopPackage;
	private EClass personClass;
	private EAttribute personId;
	private EAttribute personName;
	private EAttribute personAge;
	private EReference personFriend;

	private final ODataQueryParser parser = new ODataQueryParser();
	private final List<EObject> persons = new ArrayList<>();
	private final List<Query> seenQueries = new ArrayList<>();
	private ResourceSet resourceSet;
	private Resource storeResource;
	private ReadRepository repository;
	private ComponentServiceObjects<ReadRepository> repositories;
	private RepositoryQueryService service;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() throws IOException {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		shopPackage = ecore.createEPackage();
		shopPackage.setName("reposhop");
		shopPackage.setNsPrefix("repo");
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
		personFriend = ecore.createEReference();
		personFriend.setName("friend");
		personFriend.setEType(personClass);
		personClass.getEStructuralFeatures().addAll(List.of(personId, personName, personAge, personFriend));
		shopPackage.getEClassifiers().add(personClass);

		// the facade attaches what it reads: the double keeps the store in a resource of the
		// instance's resource set, exactly what detaching must leave intact
		resourceSet = new ResourceSetImpl();
		storeResource = new ResourceImpl(URI.createURI("repo://store/Person"));
		resourceSet.getResources().add(storeResource);
		for (int i = 1; i <= 5; i++) {
			EObject person = shopPackage.getEFactoryInstance().create(personClass);
			person.eSet(personId, i);
			person.eSet(personName, "P" + i);
			person.eSet(personAge, 20 + i);
			persons.add(person);
		}
		persons.get(0).eSet(personFriend, persons.get(1));
		storeResource.getContents().addAll(persons);

		repository = mock(ReadRepository.class);
		when(repository.capabilities()).thenReturn(capabilities(new MemoryQueryProcessor().capabilities()));
		when(repository.getResourceSet()).thenReturn(resourceSet);
		when(repository.isDisposed()).thenReturn(false);
		when(repository.find(any(Query.class))).thenAnswer(invocation -> {
			Query query = invocation.getArgument(0);
			seenQueries.add(query);
			return MemoryQueries.execute(query, persons, Map.of());
		});
		when(repository.count(any(Query.class))).thenAnswer(invocation -> {
			Query query = invocation.getArgument(0);
			seenQueries.add(query);
			try (var result = MemoryQueries.execute(query, persons, Map.of())) {
				return result.count();
			}
		});
		repositories = mock(ComponentServiceObjects.class);
		when(repositories.getService()).thenReturn(repository);

		service = new RepositoryQueryService();
		service.setRepository(repositories);
		service.activate(Map.of(RepositoryQueryService.PACKAGES_PROPERTY, NS_URI));
	}

	private static PersistenceCapabilities capabilities(QueryCapabilities query) {
		return PersistenceCapabilities.of(query, CommandCapabilitiesBuilder.create().build(),
				StoreCapabilitiesBuilder.create().build());
	}

	private EntityQuery query(String filter, String orderBy, int skip, int top, boolean count) {
		return new EntityQuery(personClass, null,
				filter == null ? null : parser.parseFilter(filter, personClass),
				orderBy == null ? List.of() : parser.parseOrderBy(orderBy, personClass),
				skip, top, count);
	}

	@Test
	@DisplayName("filter, order, skip/top and $count arrive as pushed-down queries at the facade")
	void pagesThroughTheFacade() {
		QueryResult result = service.execute(query("age gt 21", "id asc", 1, 2, true));

		assertThat(result.entities()).extracting(entity -> entity.eGet(personId)).containsExactly(3, 4);
		assertThat(result.totalCount()).isEqualTo(4);
		assertThat(seenQueries).hasSize(2);
		Query count = seenQueries.get(0);
		assertThat(count.isCountOnly()).as("$count is a countOnly query").isTrue();
		assertThat(count.getPredicate()).as("the predicate travels with the count").isNotNull();
		Query page = seenQueries.get(1);
		assertThat(page.getPredicate()).isNotNull();
		assertThat(page.getOrderBy()).hasSize(1);
		assertThat(page.getSkip()).isEqualTo(1);
		assertThat(page.getTop()).isEqualTo(2);
	}

	@Test
	@DisplayName("every request takes its own prototype instance and gives it back, objects detached")
	void oneInstancePerRequest() {
		QueryResult result = service.execute(query(null, "id asc", 0, -1, true));

		verify(repositories, times(2)).getService();
		verify(repositories, times(2)).ungetService(repository);
		assertThat(storeResource.getContents()).as("what the instance attached is detached before it goes").isEmpty();
		assertThat(result.entities()).hasSize(5);
		EObject first = result.entities().get(0);
		assertThat(first.eResource()).isNull();
		assertThat(first.eGet(personName)).as("detached objects keep their state").isEqualTo("P1");
		assertThat(((EObject) first.eGet(personFriend)).eGet(personName))
				.as("and their references").isEqualTo("P2");
	}

	@Test
	@DisplayName("an unbounded page is capped by the configured page size")
	void unboundedPageIsCapped() {
		service.activate(Map.of(RepositoryQueryService.PACKAGES_PROPERTY, NS_URI,
				RepositoryQueryService.PAGE_SIZE_PROPERTY, "2"));

		QueryResult result = service.execute(query(null, "id asc", 0, -1, false));

		assertThat(result.entities()).hasSize(2);
		assertThat(seenQueries.get(0).getTop()).isEqualTo(2);
	}

	@Test
	@DisplayName("a feature the repository does not declare is refused before it runs — 501, not 500")
	void undeclaredFeatureIsUnsupported() {
		when(repository.capabilities()).thenReturn(capabilities(QueryCapabilitiesBuilder.create()
				.support(QueryFeature.WHERE_EQ, QueryFeature.SORT, QueryFeature.LIMIT, QueryFeature.SKIP)
				.build()));

		assertThatThrownBy(() -> service.execute(query("age gt 21", "id asc", 0, 10, false)))
				.isInstanceOf(UnsupportedOperationException.class);
		assertThat(seenQueries).as("nothing reached the facade").isEmpty();
		verify(repositories).ungetService(repository);
	}

	@Test
	@DisplayName("a refusal the facade raises at execution time is unsupported, not an internal error")
	void facadeRefusalIsUnsupported() throws IOException {
		when(repository.find(any(Query.class))).thenThrow(new IOException(
				"Query rejected: GeoDistance supports range comparisons only", new QueryException("range only")));

		assertThatThrownBy(() -> service.execute(query(null, "id asc", 0, 10, false)))
				.isInstanceOf(UnsupportedOperationException.class)
				.hasMessageContaining("range comparisons only");
		verify(repositories).ungetService(repository);
	}

	@Test
	@DisplayName("any other facade failure stays an internal fault")
	void facadeFailureIsInternal() throws IOException {
		when(repository.find(any(Query.class))).thenThrow(new IOException("connection reset"));

		assertThatThrownBy(() -> service.execute(query(null, "id asc", 0, 10, false)))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	@DisplayName("supports: keyed, concrete classes of the configured packages only")
	void supportsConfiguredPackages() {
		assertThat(service.supports(personClass)).isTrue();

		EClass foreign = EcoreFactory.eINSTANCE.createEClass();
		foreign.setName("Foreign");
		EAttribute foreignId = EcoreFactory.eINSTANCE.createEAttribute();
		foreignId.setName("id");
		foreignId.setEType(EcorePackage.Literals.ESTRING);
		foreignId.setID(true);
		foreign.getEStructuralFeatures().add(foreignId);
		EPackage other = EcoreFactory.eINSTANCE.createEPackage();
		other.setNsURI("http://fennec.eclipse.org/test/other");
		other.getEClassifiers().add(foreign);
		assertThat(service.supports(foreign)).as("another package").isFalse();

		service.activate(Map.of());
		assertThat(service.supports(foreign)).as("unrestricted: every keyed class").isTrue();
		EClass keyless = EcoreFactory.eINSTANCE.createEClass();
		keyless.setName("Keyless");
		other.getEClassifiers().add(keyless);
		assertThat(service.supports(keyless)).isFalse();
	}

	@Test
	@DisplayName("$apply runs as a pipeline query on the facade")
	void applyThroughTheFacade() {
		ApplyResult result = service.executeApply(new ApplyQuery(personClass,
				parser.parseApply("aggregate(age with sum as total,$count as n)", personClass), null,
				List.of(), 0, -1, false));

		assertThat(result.rows()).hasSize(1);
		assertThat(((Number) result.rows().get(0).get("total")).longValue()).isEqualTo(115L);
		assertThat(((Number) result.rows().get(0).get("n")).longValue()).isEqualTo(5L);
		verify(repositories).ungetService(repository);
	}
}
