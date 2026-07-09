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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.client.ODataClient;
import org.eclipse.fennec.odata.client.ODataPage;
import org.eclipse.fennec.odata.runtime.MirrorServerHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The MAXIMAL round trip: the schema of a PUBLIC service — read live through our client — is
 * mirrored onto OUR server (in-memory backend, synthetic demo data), and the SAME request specs
 * run against both. The behaviour must match exactly (success, decodability, response shape);
 * only the data differs. This exercises, in one chain: foreign-$metadata → Ecore fidelity, our
 * server under a foreign model it has never seen, and client↔server parity.
 */
@Tag("live")
@DisplayName("Mirror round trip: same requests against the open service and OUR mirror of it")
class MirrorRoundTripTest {

	/** A request spec: runs against ONE client and answers a data-independent shape signature. */
	private record Probe(String name, Function<ODataClient, String> call) {
	}

	@Test
	@DisplayName("TripPin schema mirrored onto our server answers the same request specs")
	void tripPinMirror() throws Exception {
		mirror(LiveServices.TRIPPIN, "People", "UserName", "FirstName");
	}

	@Test
	@DisplayName("OData demo schema mirrored onto our server answers the same request specs")
	void odataDemoMirror() throws Exception {
		mirror(LiveServices.ODATA_DEMO, "Products", "Name", "Description");
	}

	@Test
	@DisplayName("Northwind schema mirrored onto our server answers the same request specs")
	void northwindMirror() throws Exception {
		mirror(LiveServices.NORTHWIND, "Customers", "CompanyName", "City");
	}

	private void mirror(String liveRoot, String setName, String stringProp, String selectProp)
			throws Exception {
		LiveServices.assumeReachable(liveRoot);
		try (ODataClient live = ODataClient.connect(liveRoot)) {
			// the mirrored schema IS the live service's schema, read through our client
			try (MirrorServerHarness harness = MirrorServerHarness.start(live.metadata())) {
				int seeded = DemoData.fill(harness.repository(), live.metadata());
				assertTrue(seeded > 0, "the mirror carries demo data");

				// the mirror client discovers OUR $metadata — the schema round-trips a second time
				try (ODataClient mirror = ODataClient.connect(harness.serviceRoot())) {
					for (Probe probe : probes(setName, stringProp, selectProp)) {
						String liveShape = probe.call().apply(live);
						String mirrorShape = probe.call().apply(mirror);
						assertEquals(liveShape, mirrorShape,
								"probe '" + probe.name() + "' must behave identically");
					}
				}
			}
		}
	}

	/** Data-independent request specs — each answers a shape signature, never data. */
	private List<Probe> probes(String setName, String stringProp, String selectProp) {
		return List.of(
				new Probe("discovery", client ->
						"type:" + client.entityType(setName).getName()),
				new Probe("list top 2", client ->
						pageShape(client, client.entitySet(setName).top(2).list())),
				new Probe("orderBy " + stringProp, client ->
						pageShape(client, client.entitySet(setName).orderBy(stringProp).top(2).list())),
				new Probe("filter " + stringProp + " ne null", client -> pageShape(client,
						client.entitySet(setName).filter(stringProp + " ne null").top(2).list())),
				new Probe("select " + selectProp, client -> pageShape(client,
						client.entitySet(setName).select(selectProp).top(1).list())),
				new Probe("inline count", client -> {
					ODataPage page = client.entitySet(setName).top(1).count().list();
					return "count:" + (page.totalCount() > 0);
				}),
				new Probe("totalCount", client ->
						"total:" + (client.entitySet(setName).totalCount() > 0)),
				new Probe("get first by key", client -> {
					EClass type = client.entityType(setName);
					ODataPage page = client.entitySet(setName).top(1).list();
					EObject first = page.entities().get(0);
					EObject fetched = client.entitySet(setName).get(keyLiteral(first, type));
					return "entity:" + fetched.eClass().getName();
				}));
	}

	/** Shape signature of a page: decoded type + non-emptiness — deliberately no data. */
	private static String pageShape(ODataClient client, ODataPage page) {
		String type = page.entities().isEmpty() ? "-" : page.entities().get(0).eClass().getName();
		return "page:" + type + ":" + !page.entities().isEmpty();
	}

	/** The entity's key as an OData key literal (string keys quoted). */
	private static String keyLiteral(EObject entity, EClass type) {
		EAttribute key = type.getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElseThrow();
		Object value = entity.eGet(key);
		return key.getEAttributeType().getInstanceClass() == String.class
				? "'" + String.valueOf(value).replace("'", "''") + "'"
				: String.valueOf(value);
	}
}
