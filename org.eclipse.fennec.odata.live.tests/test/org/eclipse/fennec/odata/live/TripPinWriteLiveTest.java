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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.client.ODataClient;
import org.eclipse.fennec.odata.client.ODataClientException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The client's WRITE path against a foreign server — TripPin RW isolates every connection in
 * its own session (the 302 → session-URL pattern), so creating/patching/deleting here touches
 * nobody. This is the only place the write client is proven against a server we did not write.
 */
@Tag("live")
@DisplayName("Live writes: create → read → patch → delete against a TripPin session")
class TripPinWriteLiveTest {

	@Test
	@DisplayName("full write cycle with ETag handling on a foreign 4.0 server")
	void writeCycle() {
		LiveServices.assumeReachable(LiveServices.TRIPPIN);
		try (ODataClient client = ODataClient.connect(LiveServices.TRIPPIN)) {
			EClass person = client.entityType("People");
			String userName = "fenneclivesuite"; // the session is fresh — no collision possible

			// CREATE (POST People) — required properties only
			EObject fresh = person.getEPackage().getEFactoryInstance().create(person);
			fresh.eSet(person.getEStructuralFeature("UserName"), userName);
			fresh.eSet(person.getEStructuralFeature("FirstName"), "Fennec");
			fresh.eSet(person.getEStructuralFeature("LastName"), "LiveSuite");
			EObject created = client.entitySet("People").create(fresh);
			assertNotNull(created, "TripPin answers the created entity");
			assertEquals(userName, created.eGet(person.getEStructuralFeature("UserName")));

			// READ BACK
			EObject read = client.entitySet("People").get("'" + userName + "'");
			assertEquals("Fennec", read.eGet(person.getEStructuralFeature("FirstName")));

			// PATCH (merge, If-Match: * — TripPin People carry ETags)
			EObject patch = person.getEPackage().getEFactoryInstance().create(person);
			patch.eSet(person.getEStructuralFeature("FirstName"), "Renamed");
			client.entitySet("People").update("'" + userName + "'", patch, "*");
			EObject renamed = client.entitySet("People").get("'" + userName + "'");
			assertEquals("Renamed", renamed.eGet(person.getEStructuralFeature("FirstName")));

			// DELETE, then the entity is gone (404)
			assertTrue(client.entitySet("People").delete("'" + userName + "'", "*"));
			ODataClientException gone = assertThrows(ODataClientException.class,
					() -> client.entitySet("People").get("'" + userName + "'"));
			// spec-conformant servers answer 404; TripPin (WCF) answers 204/no-content for a
			// missing entity — the client rejects the empty body either way
			assertTrue(gone.status() == 404 || gone.status() == 0,
					"the deleted person is gone (status " + gone.status() + ")");
		}
	}
}
