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
package org.eclipse.fennec.odata.wire.mapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** #119: a profile hands over its wire properties as a model that points into the domain model. */
@DisplayName("WireMapping")
class WireMappingTest {

	private static final String NS = "http://example.org/obs";

	private final EPackage domain = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass observation = EcoreFactory.eINSTANCE.createEClass();

	WireMappingTest() {
		domain.setName("obs");
		domain.setNsURI(NS);
		domain.setNsPrefix("obs");
		observation.setName("Observation");
		domain.getEClassifiers().add(observation);
		for (String name : List.of("phenomenonTimeStart", "phenomenonTimeEnd")) {
			attribute(name, EcorePackage.Literals.EDATE);
		}
		attribute("resultNumber", EcorePackage.Literals.EDOUBLE_OBJECT);
		attribute("resultBoolean", EcorePackage.Literals.EBOOLEAN_OBJECT);
		attribute("resultText", EcorePackage.Literals.ESTRING);
	}

	private void attribute(String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		observation.getEStructuralFeatures().add(attribute);
	}

	private EAttribute attribute(String name) {
		return (EAttribute) observation.getEStructuralFeature(name);
	}

	private WireProperty property(String name, String... parts) {
		WireProperty property = WireMappingFactory.eINSTANCE.createWireProperty();
		property.setName(name);
		for (String part : parts) {
			property.getParts().add(attribute(part));
		}
		return property;
	}

	private WireMapping sta() {
		ClassMapping mapping = WireMappingFactory.eINSTANCE.createClassMapping();
		mapping.setEClass(observation);
		mapping.getProperties().add(property("phenomenonTime", "phenomenonTimeStart", "phenomenonTimeEnd"));
		mapping.getProperties().add(property("result", "resultNumber", "resultBoolean", "resultText"));
		WireMapping wire = WireMappingFactory.eINSTANCE.createWireMapping();
		wire.getClasses().add(mapping);
		return wire;
	}

	@Test
	@DisplayName("survives XMI with its references into the domain model")
	void xmiRoundTrip() throws IOException {
		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		set.getPackageRegistry().put(WireMappingPackage.eNS_URI, WireMappingPackage.eINSTANCE);
		set.getPackageRegistry().put(NS, domain);
		Resource domainResource = set.createResource(URI.createURI(NS));
		domainResource.getContents().add(domain);

		Resource out = set.createResource(URI.createURI("mem:/sta.wiremapping"));
		out.getContents().add(sta());
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		out.save(bytes, Map.of());

		Resource in = set.createResource(URI.createURI("mem:/read.wiremapping"));
		in.load(new ByteArrayInputStream(bytes.toByteArray()), Map.of());
		ClassMapping read = ((WireMapping) in.getContents().get(0)).getClasses().get(0);
		assertSame(observation, read.getEClass());
		WireProperty time = read.getProperties().get(0);
		assertEquals("phenomenonTime", time.getName());
		assertEquals(List.of(attribute("phenomenonTimeStart"), attribute("phenomenonTimeEnd")), time.getParts());
		assertEquals(List.of(attribute("resultNumber"), attribute("resultBoolean"), attribute("resultText")),
				read.getProperties().get(1).getParts(), "the parts keep their order");
	}
}
