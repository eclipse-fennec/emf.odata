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
package org.eclipse.fennec.odata.wire.api;

/**
 * The member names of the control information a profile writes ([OData-JSON] 4.5 for the OData
 * originals). {@code null} means the member is not written. The names apply to reading as well:
 * a profile's {@link #keyValue()} is what a request body carries as the key (ADR-0010, stage 2).
 *
 * @param context        context URL ({@code @odata.context})
 * @param type           type discriminator of a derived instance ({@code @odata.type})
 * @param keyValue       the key VALUE as a control member (SensorThings {@code @iot.id}); OData
 *                       has none — its {@code @odata.id} is a URL, not the key
 * @param selfLink       absolute URL of the entity (SensorThings {@code @iot.selfLink})
 * @param navigationLink SUFFIX of the per-navigation link member,
 *                       {@code <Nav>@iot.navigationLink}
 * @param count          count of a collection, also as the suffix of an inline count
 *                       ({@code <Nav>@iot.count})
 * @param nextLink       link to the next page of a collection
 */
public record ControlNames(String context, String type, String keyValue, String selfLink,
		String navigationLink, String count, String nextLink) {

	/** What a root without a profile writes: plain OData, minimal metadata. */
	public static final ControlNames ODATA = new ControlNames("@odata.context", "@odata.type", null,
			null, null, "@odata.count", "@odata.nextLink");
}
