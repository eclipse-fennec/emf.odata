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

import org.eclipse.fennec.odata.wire.api.WireProfile;
import org.osgi.service.component.annotations.Component;

/**
 * Plain OData as a wire profile (ADR-0010): the default every root binds without a configured
 * {@code wireProfile.target}. All of its behaviour is the defaults of {@link WireProfile}.
 */
@Component(service = WireProfile.class, property = WireProfile.PROFILE_PROPERTY + "=" + WireProfile.ODATA)
public class ODataWireProfile implements WireProfile {

	/** The instance of a root outside a framework (unit tests, before binding). */
	static final WireProfile INSTANCE = new ODataWireProfile();
}
