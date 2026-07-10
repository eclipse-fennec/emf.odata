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
package org.eclipse.fennec.odata.metadata.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * OSGi wiring test for the E3 OData-JSON codec (closes the "no OSGi test for codec.json"
 * backlog item): the {@code odatajson} DS factory must surface as a whiteboard
 * {@link Resource.Factory} — with the {@code MetadataService} reference satisfied — and
 * produce resources that serialize the OData JSON dialect.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@DisplayName("odatajson codec whiteboard integration")
public class ODataJsonCodecIntegrationTest {

	private static final String ECORE = "/org/eclipse/fennec/odata/metadata/tests/catalog.ecore";

	@InjectService(filter = "(emf.configuratorName=odatajson)")
	Resource.Factory factory;

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private ServiceRegistration<EPackage> pkgReg;

	@BeforeEach
	public void setUp(@InjectBundleContext BundleContext ctx) throws IOException {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(ECORE, ODataJsonCodecIntegrationTest.class);
		EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);
		pkgReg = ctx.registerService(EPackage.class, pkg, null);
	}

	@AfterEach
	public void tearDown() {
		if (pkgReg != null) {
			pkgReg.unregister();
		}
		EPackage.Registry.INSTANCE.remove(pkg.getNsURI());
		ecoreHelper.releaseAll();
	}

	@Test
	@DisplayName("the DS factory creates resources that write @odata.* control information")
	public void factoryWritesODataJson() throws IOException {
		EClass documentClass = EcoreHelper.getEClass(pkg, "Document");
		EObject document = pkg.getEFactoryInstance().create(documentClass);
		document.eSet(documentClass.getEStructuralFeature("docId"), "d1");
		document.eSet(documentClass.getEStructuralFeature("title"), "Wired");

		Resource resource = factory.createResource(URI.createURI("wired.odatajson"));
		resource.getContents().add(document);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, null);
		String json = out.toString(StandardCharsets.UTF_8);

		assertTrue(json.contains("\"@odata.type\""), "OData type key: " + json);
		assertTrue(json.contains("\"docId\":\"d1\""), json);
		assertTrue(json.contains("\"title\":\"Wired\""), json);
	}
}
