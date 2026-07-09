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
package org.eclipse.fennec.odata.live;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.client.MediaContent;
import org.eclipse.fennec.odata.client.ODataClient;
import org.eclipse.fennec.odata.client.ODataPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The client against PUBLIC OData v4 reference services — real-world interop, live over the
 * internet ({@code ./gradlew liveTest}; unreachable services skip). This is where foreign-server
 * behaviours surface that no stub reproduces: session-root redirects, plain-http control links
 * behind an https root, set names that differ from type names, {@code $skiptoken} paging.
 */
@Tag("live")
@DisplayName("Live interop: client against TripPin / OData demo / Northwind")
class PublicServicesLiveTest {

	@Test
	@DisplayName("TripPin: discovery, queries, paging, singleton Me, functions, media")
	void tripPin() {
		LiveServices.assumeReachable(LiveServices.TRIPPIN);
		try (ODataClient client = ODataClient.connect(LiveServices.TRIPPIN)) {
			// discovery: set names differ from type names (People -> Person)
			EClass person = client.entityType("People");
			assertEquals("Person", person.getName());
			assertNotNull(person.getEStructuralFeature("UserName"));

			// queries: filter/orderby/select/top and a keyed read. TripPin RW is a 4.0 service:
			// enum literals must be namespace-qualified (the prefix-free form is 4.01-only)
			ODataPage page = client.entitySet("People")
					.filter("Gender eq Microsoft.OData.SampleService.Models.TripPin.PersonGender'Female'")
					.orderBy("UserName").select("UserName", "FirstName")
					.top(2).count().list();
			assertFalse(page.entities().isEmpty(), "TripPin has female persons");
			assertTrue(page.totalCount() > 0, "$count=true fills the envelope total");
			Object userName = page.entities().get(0)
					.eGet(person.getEStructuralFeature("UserName"));
			EObject one = client.entitySet("People").get("'" + userName + "'");
			assertEquals(userName, one.eGet(person.getEStructuralFeature("UserName")));

			// paging: TripPin serves plain-http @odata.nextLink behind the https root — the
			// scheme upgrade must carry nextPage() through
			ODataPage first = client.entitySet("People").top(2).list();
			if (first.hasMore()) {
				ODataPage second = client.entitySet("People").nextPage(first);
				assertFalse(second.entities().isEmpty(), "the next page decodes");
			}

			// $expand: single-valued navigation decodes inline
			EObject withTrips = client.entitySet("People").expand("Trips").get("'" + userName + "'");
			assertNotNull(withTrips, "expanded entity decodes");

			// singleton Me ([OData-CSDL] 13.5)
			EObject me = client.singleton("Me");
			assertNotNull(me.eGet(me.eClass().getEStructuralFeature("UserName")),
					"the Me singleton decodes into Person");

			// unbound function import with parameters, entity-typed result
			EClass airport = client.entityType("Airports");
			EObject nearest = client.functionAsEntity("GetNearestAirport",
					Map.of("lat", 33.0, "lon", -118.0), airport);
			assertNotNull(nearest.eGet(airport.getEStructuralFeature("IcaoCode")),
					"GetNearestAirport answers an Airport");

			// media entity (Photo HasStream): read the binary stream when one exists
			ODataPage photos = client.entitySet("Photos").top(1).list();
			if (!photos.entities().isEmpty()) {
				EClass photo = client.entityType("Photos");
				Object id = photos.entities().get(0).eGet(photo.getEStructuralFeature("Id"));
				MediaContent media = client.entitySet("Photos").mediaRead(String.valueOf(id));
				assertTrue(media.content().length > 0, "the photo stream has bytes");
			}
		}
	}

	@Test
	@DisplayName("OData demo: discovery, queries, $expand across renamed sets")
	void odataDemo() {
		LiveServices.assumeReachable(LiveServices.ODATA_DEMO);
		try (ODataClient client = ODataClient.connect(LiveServices.ODATA_DEMO)) {
			EClass product = client.entityType("Products");
			assertEquals("Product", product.getName());

			ODataPage page = client.entitySet("Products")
					.filter("Rating gt 2").orderBy("Name").top(3).count().list();
			assertFalse(page.entities().isEmpty());
			assertTrue(page.totalCount() > 0);

			Object id = page.entities().get(0).eGet(product.getEStructuralFeature("ID"));
			EObject one = client.entitySet("Products").expand("Categories").get(String.valueOf(id));
			assertEquals(id, one.eGet(product.getEStructuralFeature("ID")));

			long total = client.entitySet("Products").totalCount();
			assertTrue(total > 0, "Products/$count answers");
		}
	}

	@Test
	@DisplayName("Northwind: discovery, string keys, $expand, $skiptoken paging")
	void northwind() {
		LiveServices.assumeReachable(LiveServices.NORTHWIND);
		try (ODataClient client = ODataClient.connect(LiveServices.NORTHWIND)) {
			EClass customer = client.entityType("Customers");
			assertEquals("Customer", customer.getName());

			ODataPage page = client.entitySet("Customers")
					.filter("startswith(CompanyName,'A')").orderBy("CompanyName")
					.select("CustomerID", "CompanyName").top(3).list();
			assertFalse(page.entities().isEmpty());

			EObject alfki = client.entitySet("Customers").expand("Orders").get("'ALFKI'");
			assertEquals("ALFKI", alfki.eGet(customer.getEStructuralFeature("CustomerID")));

			// Northwind pages Orders server-driven ($skiptoken nextLink) — follow one hop
			ODataPage orders = client.entitySet("Orders").list();
			if (orders.hasMore()) {
				ODataPage next = client.entitySet("Orders").nextPage(orders);
				assertFalse(next.entities().isEmpty(), "the $skiptoken page decodes");
			}
		}
	}
}
