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

import java.util.HashMap;
import java.util.Map;

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

		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
		options.put(XMLResource.OPTION_PARSER_FEATURES, features);
		return options;
	}
}
