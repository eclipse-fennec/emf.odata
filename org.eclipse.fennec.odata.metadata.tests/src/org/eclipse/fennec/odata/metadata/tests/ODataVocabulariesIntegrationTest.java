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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * OSGi wiring test for the E1 vocabulary supply (closes the "no OSGi test for vocabularies"
 * backlog item): activating the vocabularies bundle must register the OASIS vocabulary
 * {@link EPackage}s as whiteboard services with their {@code emf.*} properties, ready for
 * the Model Metadata Service and every other EPackage consumer.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@DisplayName("OASIS vocabulary EPackage services")
public class ODataVocabulariesIntegrationTest {

	@InjectService(filter = "(emf.nsURI=http://eclipse.org/fennec/odata/Org.OData.Core.V1)")
	EPackage core;

	@InjectService(filter = "(emf.nsURI=http://eclipse.org/fennec/odata/Org.OData.Capabilities.V1)")
	EPackage capabilities;

	@Test
	@DisplayName("Core and Capabilities surface as EPackage services with usable content")
	public void vocabulariesAreRegistered() {
		assertEquals("Core", core.getName(), "schema alias becomes the package name");
		EEnum permission = (EEnum) core.getEClassifier("Permission");
		assertNotNull(permission.getEEnumLiteral("Read"), "Core.Permission enum readable");

		assertEquals("Capabilities", capabilities.getName());
		assertNotNull(capabilities.getEClassifier("FilterRestrictionsType"),
				"complex vocabulary types become EClasses");
	}
}
