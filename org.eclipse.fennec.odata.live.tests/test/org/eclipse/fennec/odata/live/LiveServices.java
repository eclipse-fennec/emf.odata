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

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * The public OData v4 reference services the live suite runs against. External systems are not
 * CI material: {@link #assumeReachable} SKIPS a test (JUnit assumption) instead of failing it
 * when the service is down or the build machine is offline.
 */
final class LiveServices {

	/** OASIS/Microsoft reference service, read-write via per-session roots (302 → session URL). */
	static final String TRIPPIN = "https://services.odata.org/V4/TripPinServiceRW/";
	/** The compact OData demo service (read-only). */
	static final String ODATA_DEMO = "https://services.odata.org/V4/OData/OData.svc/";
	/** The classic Northwind model (read-only, server-driven paging via $skiptoken). */
	static final String NORTHWIND = "https://services.odata.org/V4/Northwind/Northwind.svc/";

	private LiveServices() {
	}

	/** Skips the calling test when the service root does not answer within a short timeout. */
	static void assumeReachable(String serviceRoot) {
		boolean reachable;
		try (HttpClient http = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5)).build()) {
			HttpResponse<Void> response = http.send(
					HttpRequest.newBuilder(URI.create(serviceRoot))
							.timeout(Duration.ofSeconds(10)).GET().build(),
					HttpResponse.BodyHandlers.discarding());
			reachable = response.statusCode() < 500;
		} catch (Exception e) {
			reachable = false;
		}
		assumeTrue(reachable, "live service unreachable, skipping: " + serviceRoot);
	}
}
