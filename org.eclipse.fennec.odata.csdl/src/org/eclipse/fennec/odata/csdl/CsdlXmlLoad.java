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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;

/**
 * XXE-hardened load options for CSDL/EDMX XML, shared by EVERY place that parses OData XML — the
 * vendored OASIS vocabularies and, above all, a client reading a remote service's {@code $metadata}
 * (attacker-controlled input). Disabling the DTD outright neutralises XML External Entity attacks
 * (local-file read, SSRF via {@code SYSTEM}/{@code PUBLIC} ids) AND entity-expansion / billion-laughs
 * denial of service in one move, since no DTD means no entity declarations at all.
 */
public final class CsdlXmlLoad {

	private static final String DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
	private static final String EXTERNAL_GENERAL = "http://xml.org/sax/features/external-general-entities";
	private static final String EXTERNAL_PARAMETER = "http://xml.org/sax/features/external-parameter-entities";
	private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

	private CsdlXmlLoad() {
	}

	/**
	 * A fresh, mutable options map for {@link org.eclipse.emf.ecore.resource.Resource#load}, with
	 * OData's extended-metadata mapping enabled and the parser hardened against XXE. Callers may
	 * add further options to the returned map.
	 */
	public static Map<Object, Object> secureOptions() {
		Map<String, Boolean> features = new HashMap<>();
		features.put(DISALLOW_DOCTYPE, Boolean.TRUE);
		features.put(EXTERNAL_GENERAL, Boolean.FALSE);
		features.put(EXTERNAL_PARAMETER, Boolean.FALSE);
		features.put(LOAD_EXTERNAL_DTD, Boolean.FALSE);
		// defence in depth beyond DTD-off: activates the JDK's built-in secure-processing limits
		// (entity-expansion count, attribute count, name limits) for the parser
		features.put(XMLConstants.FEATURE_SECURE_PROCESSING, Boolean.TRUE);

		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
		options.put(XMLResource.OPTION_PARSER_FEATURES, features);
		return options;
	}

	/**
	 * Loads XML into {@code resource} with {@link #secureOptions()}, converting a
	 * {@link StackOverflowError} from a pathologically deep (yet DTD-free and size-bounded)
	 * document into a clean {@link IllegalArgumentException} so it never crashes the calling
	 * thread. Callers that read attacker-controlled {@code $metadata} should use this rather than
	 * {@code resource.load(in, secureOptions())} directly. (SAX parsing is event-based, so depth
	 * rarely reaches the stack limit — this is cheap insurance; the primary large-input DoS is
	 * bounded by the caller's size cap.)
	 */
	public static void loadSecurely(Resource resource, InputStream in) throws IOException {
		try {
			resource.load(in, secureOptions());
		} catch (StackOverflowError deep) {
			throw new IllegalArgumentException("the XML document is too deeply nested", deep);
		}
	}
}
