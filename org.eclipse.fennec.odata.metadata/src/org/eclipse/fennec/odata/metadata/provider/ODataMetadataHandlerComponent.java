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

import org.eclipse.fennec.emf.osgi.metadata.MetadataHandler;
import org.osgi.service.component.annotations.Component;

/**
 * OSGi Declarative Services component that publishes {@link ODataMetadataHandler} as a
 * {@link MetadataHandler} service. The {@code MetadataService} whiteboard discovers it and
 * applies the OData aspect to every registered {@code EPackage} — no manual registration
 * (analogous to {@code CodecAspectProviderComponent}).
 */
@Component(name = "ODataMetadataHandlerComponent", service = MetadataHandler.class)
public class ODataMetadataHandlerComponent extends ODataMetadataHandler {
	// Logic inherited from ODataMetadataHandler; this class only exposes it as a DS service.
}
