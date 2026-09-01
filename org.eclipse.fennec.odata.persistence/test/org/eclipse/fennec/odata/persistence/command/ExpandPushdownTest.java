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
import java.util.EnumSet;
import java.util.Set;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.fennec.model.query.Expand;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.persistence.capabilities.QueryCapabilities;
import org.eclipse.fennec.persistence.capabilities.QueryCapabilitiesBuilder;
import org.eclipse.fennec.persistence.capabilities.QueryFeature;
import org.eclipse.fennec.persistence.query.QueryConstants;
import org.eclipse.fennec.persistence.query.memory.MemoryQueryProcessor;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.ExpandPushdown;
import org.eclipse.fennec.odata.persistence.api.ExpandSpec;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.query.ODataQueryParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What we ASK an expand-capable backend for, and what we REPORT back to the caller (ADR-0008).
 *
 * <p>Deliberately not "what the backend then does": honouring the ask is upstream's contract and
 * its TCK's business. What is ours — and what silently goes wrong if it is not pinned — is which
 * options we hand down, which combinations we refuse to hand down, and whether the report matches
 * what we actually asked. A report that claims more than was pushed makes the caller skip an
 * in-memory pass that was still needed; one that claims less makes it run twice.
 */
@DisplayName("$expand options: the ask and the report")
class ExpandPushdownTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/expandshop";

	private EPackage pkg;
	private EClass customerClass;
	private EClass orderClass;
	private EAttribute customerId;
	private EAttribute orderId;
	private EAttribute orderAmount;
	private EReference customerOrders;

	private final ODataQueryParser parser = new ODataQueryParser();
	private FakeCommandBackend backend;
	private CommandPersistenceService service;

	/**
	 * The capability gate reads the registered {@code QueryProcessor} for the store's scheme,
	 * not the resource — so declaring EXPAND_FILTER/EXPAND_PAGE means registering a processor
	 * that declares them. The memory engine deliberately declares neither (it is handed
	 * resolved objects and has nothing to select), which is exactly the "no capability" case.
	 */
	private static final class FakeProcessor extends MemoryQueryProcessor {

		private final QueryCapabilities capabilities;

		FakeProcessor(QueryFeature... extra) {
			Set<QueryFeature> features = EnumSet.copyOf(super.capabilities().supported());
			features.addAll(List.of(extra));
			capabilities = QueryCapabilitiesBuilder.create()
					.support(features.toArray(QueryFeature[]::new))
					.maxFeaturePathDepth(-1).build();
		}

		@Override
		public String backend() {
			return "fake";
		}

		@Override
		public QueryCapabilities capabilities() {
			return capabilities;
		}

		/**
		 * The engine validates against its own hard-coded set, so the declared extras have to
		 * be forgiven here too. Everything else it complains about still stands — this is a
		 * test double for the CAPABILITY, not a licence to push anything.
		 */
		@Override
		public Diagnostic validate(Query query, EClass rootEClass) {
			Diagnostic diagnostic = super.validate(query, rootEClass);
			List<Diagnostic> kept = diagnostic.getChildren().stream()
					.filter(child -> !child.getMessage().contains("EXPAND_FILTER")
							&& !child.getMessage().contains("EXPAND_PAGE"))
					.toList();
			if (kept.size() == diagnostic.getChildren().size()) {
				return diagnostic;
			}
			BasicDiagnostic reduced = new BasicDiagnostic(diagnostic.getSource(),
					diagnostic.getCode(), diagnostic.getMessage(), null);
			kept.forEach(reduced::add);
			return reduced;
		}
	}

	private void register(QueryFeature... extra) {
		service.addQueryProcessor(new FakeProcessor(extra),
				Map.of(QueryConstants.BACKEND_PROPERTY, "fake"));
	}

	@BeforeEach
	void setUp() {
		buildModel();
		backend = new FakeCommandBackend();
		EObject customer = pkg.getEFactoryInstance().create(customerClass);
		customer.eSet(customerId, 1);
		backend.storeFor("Customer").put(1, customer);

		service = new CommandPersistenceService();
		service.setResourceSetFactory(() -> {
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("fake", backend);
			return resourceSet;
		});
		service.activate(Map.of(CommandPersistenceService.URI_PROPERTY, "fake://store",
				CommandPersistenceService.PACKAGES_PROPERTY, NS_URI));
	}

	private QueryResult run(ExpandSpec spec) {
		return service.execute(new EntityQuery(customerClass, null, null, List.of(), 0, -1, false,
				List.of(spec)));
	}

	private ExpandSpec spec(String filter, int skip, int top) {
		return new ExpandSpec("orders",
				filter == null ? null : parser.parseFilter(filter, orderClass), List.of(), skip,
				top);
	}

	private Expand pushedExpand() {
		assertThat(backend.lastQuery().getExpand()).as("exactly one expansion").hasSize(1);
		return backend.lastQuery().getExpand().get(0);
	}

	@Test
	@DisplayName("a backend that declares neither capability is asked for the plain path only")
	void withoutCapabilitiesNothingIsPushed() {
		register(QueryFeature.EXPAND); // plain expansion only, like the memory engine

		QueryResult result = run(spec("amount gt 100", 0, 5));

		assertThat(result.pushedFor("orders")).isEqualTo(ExpandPushdown.NONE);
		assertThat(pushedExpand().getFilter()).as("the ask degrades to the plain fetch hint")
				.isNull();
		assertThat(pushedExpand().getTop()).isZero();
	}

	@Test
	@DisplayName("a declaring backend gets the filter, and the report says so")
	void filterIsPushedWhenDeclared() {
		register(QueryFeature.EXPAND, QueryFeature.EXPAND_FILTER, QueryFeature.EXPAND_PAGE);

		QueryResult result = run(spec("amount gt 100", 0, -1));

		assertThat(result.pushedFor("orders").filter()).isTrue();
		assertThat(result.pushedFor("orders").paging()).isFalse();
		assertThat(pushedExpand().getFilter()).isNotNull();
	}

	@Test
	@DisplayName("paging rides along with the filter, never ahead of it")
	void pagingIsPushedWithTheFilter() {
		register(QueryFeature.EXPAND, QueryFeature.EXPAND_FILTER, QueryFeature.EXPAND_PAGE);

		QueryResult result = run(spec("amount gt 100", 2, 5));

		assertThat(result.pushedFor("orders")).isEqualTo(new ExpandPushdown(true, true));
		assertThat(pushedExpand().getSkip()).isEqualTo(2);
		assertThat(pushedExpand().getTop()).isEqualTo(5);
	}

	@Test
	@DisplayName("paging alone is pushed when there is no filter to lose")
	void pagingWithoutAFilter() {
		register(QueryFeature.EXPAND, QueryFeature.EXPAND_FILTER, QueryFeature.EXPAND_PAGE);

		QueryResult result = run(spec(null, 0, 3));

		assertThat(result.pushedFor("orders")).isEqualTo(new ExpandPushdown(false, true));
		assertThat(pushedExpand().getTop()).isEqualTo(3);
	}

	@Test
	@DisplayName("$top=0 is never pushed: Expand.top spells unlimited as 0")
	void topZeroStaysInMemory() {
		register(QueryFeature.EXPAND, QueryFeature.EXPAND_FILTER, QueryFeature.EXPAND_PAGE);

		QueryResult result = run(spec(null, 0, 0));

		assertThat(result.pushedFor("orders")).isEqualTo(ExpandPushdown.NONE);
		assertThat(pushedExpand().getTop()).isZero();
	}

	@Test
	@DisplayName("a plain expansion asks for nothing and reports nothing, capability or not")
	void plainExpansionStaysPlain() {
		register(QueryFeature.EXPAND, QueryFeature.EXPAND_FILTER, QueryFeature.EXPAND_PAGE);

		QueryResult result = run(ExpandSpec.of("orders"));

		assertThat(result.pushedFor("orders")).isEqualTo(ExpandPushdown.NONE);
		assertThat(pushedExpand().getFilter()).isNull();
	}

	@Test
	@DisplayName("an unexpanded path reports nothing rather than null")
	void unaskedPathsReportNothing() {
		register(QueryFeature.EXPAND, QueryFeature.EXPAND_FILTER, QueryFeature.EXPAND_PAGE);

		QueryResult result = run(spec("amount gt 100", 0, -1));

		assertThat(result.pushedFor("nothing-of-the-sort")).isEqualTo(ExpandPushdown.NONE);
	}

	private void buildModel() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		pkg = ecore.createEPackage();
		pkg.setName("expandshop");
		pkg.setNsPrefix("exp");
		pkg.setNsURI(NS_URI);

		orderClass = ecore.createEClass();
		orderClass.setName("Order");
		orderId = ecore.createEAttribute();
		orderId.setName("id");
		orderId.setEType(EcorePackage.Literals.EINT);
		orderId.setID(true);
		orderAmount = ecore.createEAttribute();
		orderAmount.setName("amount");
		orderAmount.setEType(EcorePackage.Literals.EINT);
		orderClass.getEStructuralFeatures().addAll(List.of(orderId, orderAmount));

		customerClass = ecore.createEClass();
		customerClass.setName("Customer");
		customerId = ecore.createEAttribute();
		customerId.setName("id");
		customerId.setEType(EcorePackage.Literals.EINT);
		customerId.setID(true);
		customerOrders = ecore.createEReference();
		customerOrders.setName("orders");
		customerOrders.setEType(orderClass);
		customerOrders.setUpperBound(-1);
		customerClass.getEStructuralFeatures().addAll(List.of(customerId, customerOrders));

		pkg.getEClassifiers().addAll(List.of(customerClass, orderClass));
	}
}
