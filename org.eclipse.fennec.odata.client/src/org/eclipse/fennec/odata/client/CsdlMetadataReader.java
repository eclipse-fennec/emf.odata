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
package org.eclipse.fennec.odata.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.fennec.odata.csdl.EdmToEcoreConverter;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;

/**
 * {@code $metadata} → Ecore for the client: parses the CSDL XML into the OASIS EDM/EDMX
 * model (the same XSD-generated model the server serializes from) and converts every schema
 * through the E2 read path ({@link EdmToEcoreConverter}) — one {@link EPackage} per schema.
 */
final class CsdlMetadataReader {

	private CsdlMetadataReader() {
	}

	static List<EPackage> read(String csdlXml) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*",
				new XMLResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
		resourceSet.getPackageRegistry().put(EdmxPackage.eNS_URI, EdmxPackage.eINSTANCE);
		Resource resource = resourceSet.createResource(URI.createURI("metadata.xml"));
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
		try {
			resource.load(new ByteArrayInputStream(csdlXml.getBytes(StandardCharsets.UTF_8)),
					options);
		} catch (IOException e) {
			throw new ODataClientException("the service's $metadata is not parseable CSDL XML", e);
		}
		if (resource.getContents().isEmpty()) {
			throw new ODataClientException("the service's $metadata document is empty");
		}
		EObject root = resource.getContents().get(0);
		TEdmx edmx = root instanceof EdmxRoot edmxRoot ? edmxRoot.getEdmx() : (TEdmx) root;
		if (edmx == null || edmx.getDataServices() == null) {
			throw new ODataClientException("the service's $metadata carries no DataServices");
		}
		// multi-schema conversion: cross-schema navigation targets resolve in the final pass
		return new EdmToEcoreConverter().toEPackages(edmx);
	}
}
