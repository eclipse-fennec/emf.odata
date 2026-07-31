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
package org.eclipse.fennec.odata.codec.json;

import static java.util.Objects.requireNonNull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Resource factory for {@link ODataJsonResourceImpl} — registers the OData-JSON codec profile
 * with the EMF-OSGi resource whiteboard (configurator name {@code odatajson}), analogous to the
 * GeoJSON factory. Consumers inject a {@code ResourceSet} filtered on
 * {@code (emf.configuratorName=odatajson)} or use the {@code odatajson} file extension.
 */
@Component(service = Resource.Factory.class,
	property = {
		EMFNamespaces.EMF_CONFIGURATOR_NAME + "=odatajson",
		EMFNamespaces.EMF_MODEL_FILE_EXT + "=odatajson",
		EMFNamespaces.EMF_MODEL_VERSION + "=1.0"
	}
)
public class ODataJsonResourceFactoryImpl extends ResourceFactoryImpl {

	private final MetadataService metadataService;

	@Activate
	public ODataJsonResourceFactoryImpl(@Reference MetadataService metadataService) {
		this.metadataService = requireNonNull(metadataService, "metadataService must not be null");
	}

	@Override
	public Resource createResource(URI uri) {
		return new ODataJsonResourceImpl(uri, metadataService);
	}
}
