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

/**
 * Whether Jackson (the bundle's OPTIONAL import) is present. The XML CSDL paths must work
 * without Jackson — only the JSON wire form and the rich-annotation-expression encoding
 * need it, so their call sites gate on {@link #PRESENT} before touching any class that
 * links against {@code tools.jackson}.
 */
final class JacksonSupport {

	static final boolean PRESENT = detect();

	private static boolean detect() {
		try {
			Class.forName("tools.jackson.databind.ObjectMapper", false,
					JacksonSupport.class.getClassLoader());
			return true;
		} catch (ClassNotFoundException | LinkageError e) {
			return false;
		}
	}

	private JacksonSupport() {
	}
}
