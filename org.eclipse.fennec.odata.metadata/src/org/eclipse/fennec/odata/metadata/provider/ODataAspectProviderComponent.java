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
package org.eclipse.fennec.odata.metadata.provider;

import org.eclipse.fennec.model.metadata.api.AspectProvider;
import org.osgi.service.component.annotations.Component;

/**
 * OSGi Declarative Services component that publishes {@link ODataAspectProvider} as an
 * {@link AspectProvider} service. The {@code MetadataService} whiteboard discovers it and
 * applies OData aspects to every registered {@code EPackage} — no manual registration
 * (analogous to {@code CodecAspectProviderComponent}).
 */
@Component(name = "ODataAspectProviderComponent", service = AspectProvider.class)
public class ODataAspectProviderComponent extends ODataAspectProvider {
	// Logic inherited from ODataAspectProvider; this class only exposes it as a DS service.
}
