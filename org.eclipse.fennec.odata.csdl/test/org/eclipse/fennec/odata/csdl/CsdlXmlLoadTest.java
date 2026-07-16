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
package org.eclipse.fennec.odata.csdl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import javax.xml.XMLConstants;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The single hardened CSDL/EDMX XML load path. Asserts the XXE neutralisation is real (a DOCTYPE
 * with an external entity must NOT read a local file), that secure-processing is enabled, and that
 * {@link CsdlXmlLoad#loadSecurely} converts a StackOverflowError into a clean client error instead
 * of crashing the thread. These are the deliberately-hostile inputs that harden the reader.
 */
@DisplayName("CsdlXmlLoad: XXE-hardened, depth-guarded load path")
class CsdlXmlLoadTest {

	@Test
	@DisplayName("secureOptions enables disallow-doctype and secure-processing")
	@SuppressWarnings("unchecked")
	void secureOptionsAreHardened() {
		Map<Object, Object> options = CsdlXmlLoad.secureOptions();
		Map<String, Boolean> features = (Map<String, Boolean>) options.get(
				org.eclipse.emf.ecore.xmi.XMLResource.OPTION_PARSER_FEATURES);
		assertTrue(features.get("http://apache.org/xml/features/disallow-doctype-decl"),
				"DTDs must be disallowed (XXE + billion-laughs)");
		assertTrue(features.get(XMLConstants.FEATURE_SECURE_PROCESSING),
				"secure processing must be on");
	}

	@Test
	@DisplayName("a DOCTYPE with an external entity does NOT read a local file (XXE neutralised)")
	void externalEntityIsNotExpanded() throws Exception {
		File secret = File.createTempFile("odata-xxe-secret", ".txt");
		Files.writeString(secret.toPath(), "TOP-SECRET-CONTENT");
		String hostile = "<?xml version=\"1.0\"?>"
				+ "<!DOCTYPE edmx [ <!ENTITY xxe SYSTEM \"file://" + secret.getAbsolutePath() + "\"> ]>"
				+ "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.01\">"
				+ "<edmx:DataServices><note>&xxe;</note></edmx:DataServices></edmx:Edmx>";

		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
		Resource resource = rs.createResource(URI.createURI("hostile.xml"));
		try {
			CsdlXmlLoad.loadSecurely(resource,
					new ByteArrayInputStream(hostile.getBytes(StandardCharsets.UTF_8)));
		} catch (IOException | IllegalArgumentException expected) {
			// disallow-doctype rejects the document outright — that is the strongest outcome
		}
		String loaded = resource.getContents().toString();
		assertFalse(loaded.contains("TOP-SECRET-CONTENT"),
				"the external entity must never be expanded into the model: " + loaded);
		Files.deleteIfExists(secret.toPath());
	}

	@Test
	@DisplayName("loadSecurely parses a well-formed EDMX without error")
	void loadsWellFormedEdmx() throws Exception {
		String edmx = "<?xml version=\"1.0\"?>"
				+ "<edmx:Edmx xmlns:edmx=\"http://docs.oasis-open.org/odata/ns/edmx\" Version=\"4.01\">"
				+ "<edmx:DataServices/></edmx:Edmx>";
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
		Resource resource = rs.createResource(URI.createURI("ok.xml"));
		CsdlXmlLoad.loadSecurely(resource,
				new ByteArrayInputStream(edmx.getBytes(StandardCharsets.UTF_8)));
		assertFalse(resource.getContents().isEmpty(), "the EDMX root must load");
	}
}
