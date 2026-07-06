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
package org.eclipse.fennec.odata.example;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.odata.persistence.api.EntityRepository;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * The whole example in one component: loads the dummy {@code shop.ecore} (dynamic EMF — no
 * code generation required), builds a handful of demo products/categories/reviews, and
 * registers BOTH whiteboard contributions the OData stack consumes:
 * <ul>
 *   <li>the {@link EPackage} — picked up by the metadata service and the ODataServlet
 *       (entity sets, $metadata)</li>
 *   <li>an {@link EntityRepository} with the demo instances — served by the in-memory
 *       {@code QueryService}</li>
 * </ul>
 * Swap the repository for the file-backed one (factory PID
 * {@code org.eclipse.fennec.odata.repository.file}, property {@code directory}) to serve
 * XMI files from disk instead — see README.md.
 */
@Component(immediate = true)
public class ShopExampleComponent {

	private final EcoreHelper ecoreHelper = new EcoreHelper();
	private final List<ServiceRegistration<?>> registrations = new ArrayList<>();

	@Activate
	void activate(BundleContext context) throws IOException {
		EPackage shop = ecoreHelper.loadEcore("shop.ecore", ShopExampleComponent.class);

		EClass categoryClass = EcoreHelper.getEClass(shop, "Category");
		EClass productClass = EcoreHelper.getEClass(shop, "Product");
		EClass reviewClass = EcoreHelper.getEClass(shop, "Review");

		EObject dairy = create(shop, categoryClass, Map.of("id", "c1", "name", "Dairy"));
		EObject bakery = create(shop, categoryClass, Map.of("id", "c2", "name", "Bakery"));
		EObject milk = create(shop, productClass, Map.of("id", "p1", "name", "Milk",
				"price", new BigDecimal("1.20"), "rating", 3, "active", true, "category", dairy));
		EObject cheese = create(shop, productClass, Map.of("id", "p2", "name", "Cheese",
				"price", new BigDecimal("4.50"), "rating", 5, "active", true, "category", dairy));
		cheese.eSet(productClass.getEStructuralFeature("reviews"), List.of(
				create(shop, reviewClass, Map.of("stars", 5, "comment", "great with wine")),
				create(shop, reviewClass, Map.of("stars", 4, "comment", "a bit pricey"))));
		EObject bread = create(shop, productClass, Map.of("id", "p3", "name", "Bread",
				"price", new BigDecimal("2.80"), "rating", 4, "active", false, "category", bakery));

		List<EObject> entities = List.of(dairy, bakery, milk, cheese, bread);
		EntityRepository repository = new EntityRepository() {
			@Override
			public boolean supplies(EClass entityType) {
				return entityType.getEPackage() == shop;
			}

			@Override
			public List<EObject> entities(EClass entityType) {
				return entities.stream().filter(e -> e.eClass() == entityType).toList();
			}
		};

		registrations.add(context.registerService(EPackage.class, shop, null));
		registrations.add(context.registerService(EntityRepository.class, repository, null));
	}

	@Deactivate
	void deactivate() {
		registrations.forEach(ServiceRegistration::unregister);
		registrations.clear();
		ecoreHelper.releaseAll();
	}

	private static EObject create(EPackage pkg, EClass type, Map<String, Object> values) {
		EObject object = pkg.getEFactoryInstance().create(type);
		values.forEach((feature, value) -> object.eSet(type.getEStructuralFeature(feature), value));
		return object;
	}
}
