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
package org.eclipse.fennec.odata.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.odata.persistence.api.ExpandPushdown;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The mechanism ADR-0008 rests on: where the backend narrowed an expansion, the payload is the
 * RESOLVED entries and the untouched remainder must stay untouched.
 *
 * <p>"Stay untouched" is the load-bearing half and the easy one to lose: reading a proxy resolves
 * it, so a shaper that inspects the collection the ordinary way both reintroduces the N+1 the
 * pushdown removed and destroys the very information it is inspecting. The counter here fails the
 * test if anything walks into the resource to resolve.
 */
@DisplayName("EntityShaper: eIsProxy() as the selection, without resolving it")
class EntityShaperProxyTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/shapeproxy";

	private EPackage pkg;
	private EClass customerClass;
	private EClass orderClass;
	private EAttribute customerId;
	private EAttribute orderId;
	private EReference customerOrders;

	private final EntityShaper shaper = new EntityShaper(
			type -> type.getEAllAttributes().stream().filter(EAttribute::isID).toList());
	private final AtomicInteger resolutions = new AtomicInteger();
	private ResourceSetImpl resourceSet;
	private EObject customer;

	@BeforeEach
	void setUp() {
		buildModel();
		// a resource set that COUNTS resolutions instead of performing them
		resourceSet = new ResourceSetImpl() {
			@Override
			public EObject getEObject(URI uri, boolean loadOnDemand) {
				resolutions.incrementAndGet();
				return null;
			}
		};
		Resource resource = new ResourceImpl(URI.createURI("fake://store/Customer"));
		resourceSet.getResources().add(resource);

		customer = pkg.getEFactoryInstance().create(customerClass);
		customer.eSet(customerId, 1);
		resource.getContents().add(customer);

		@SuppressWarnings("unchecked")
		List<EObject> orders = (List<EObject>) customer.eGet(customerOrders);
		orders.add(order(10, false)); // resolved: the backend selected it
		orders.add(order(11, true)); // proxy: not selected
		orders.add(order(12, false)); // resolved: selected
		// EcoreEList#add probes contains(), which resolves what is already in the list —
		// seeding is not the measurement
		resolutions.set(0);
	}

	private EObject order(int id, boolean proxy) {
		EObject order = pkg.getEFactoryInstance().create(orderClass);
		order.eSet(orderId, id);
		if (proxy) {
			((InternalEObject) order).eSetProxyURI(URI.createURI("fake://store/Order#" + id));
		} else {
			// resolved members live in the same resource, like a batched expansion leaves them
			resourceSet.getResources().get(0).getContents().add(order);
		}
		return order;
	}

	@Test
	@DisplayName("a narrowed expansion delivers the resolved entries and resolves nothing")
	void narrowedTakesResolvedOnly() {
		EObject copy = shaper.shape(customer, customerClass, (SelectTree) null, Set.of("orders"),
				new ArrayList<>(), null, Map.of("orders", new ExpandPushdown(true, false)));

		assertThat(idsOf(copy)).containsExactly(10, 12);
		assertThat(resolutions).hasValue(0);
	}

	@Test
	@DisplayName("without a report the collection is read the ordinary way — proxies and all")
	void plainExpansionKeepsTodaysBehaviour() {
		EObject copy = shaper.shape(customer, customerClass, (SelectTree) null, Set.of("orders"),
				new ArrayList<>(), null, Map.of());

		// the unresolvable proxy drops out of the copy, but only after it was READ: what this
		// pins is that the plain path did not silently inherit the selective one
		assertThat(idsOf(copy)).contains(10, 12);
		assertThat(resolutions).as("the ordinary read walks into the resource set")
				.hasValueGreaterThan(0);
	}

	@SuppressWarnings("unchecked")
	private List<Integer> idsOf(EObject copy) {
		return ((List<EObject>) copy.eGet(customerOrders)).stream()
				.map(order -> (Integer) order.eGet(orderId)).toList();
	}

	private void buildModel() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		pkg = ecore.createEPackage();
		pkg.setName("shapeproxy");
		pkg.setNsPrefix("sp");
		pkg.setNsURI(NS_URI);

		orderClass = ecore.createEClass();
		orderClass.setName("Order");
		orderId = ecore.createEAttribute();
		orderId.setName("id");
		orderId.setEType(EcorePackage.Literals.EINT);
		orderId.setID(true);
		orderClass.getEStructuralFeatures().add(orderId);

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
		EPackage.Registry.INSTANCE.put(NS_URI, pkg);
	}
}
