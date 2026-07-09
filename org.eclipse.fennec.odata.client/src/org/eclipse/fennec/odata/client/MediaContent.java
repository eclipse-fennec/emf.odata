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

/**
 * A media entity's binary stream ({@code GET Set(key)/$value} on a HasStream type): the raw
 * content plus its media type.
 */
public record MediaContent(byte[] content, String contentType) {

	public MediaContent {
		content = content == null ? new byte[0] : content.clone();
	}

	@Override
	public byte[] content() {
		return content.clone(); // keep the record's state immutable
	}
}
