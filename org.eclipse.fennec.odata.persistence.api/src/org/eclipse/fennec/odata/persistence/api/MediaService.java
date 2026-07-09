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
package org.eclipse.fennec.odata.persistence.api;

import java.util.Optional;

import org.eclipse.emf.ecore.EClass;

/**
 * Backend SPI for <b>media entities</b> ([OData-Protocol] 11.2.4 / 11.4.7): an entity type marked
 * {@code HasStream} carries a binary media stream alongside its structural properties, addressed
 * as {@code GET/PUT Set(key)/$value}. The protocol layer routes those requests here; a type
 * without a registered MediaService answers {@code 501}.
 */
public interface MediaService {

	/** A media value: the raw content plus its media type (e.g. {@code image/png}). */
	record MediaStream(byte[] content, String contentType) {

		public MediaStream {
			content = content == null ? new byte[0] : content.clone();
			contentType = contentType == null || contentType.isBlank()
					? "application/octet-stream" : contentType;
		}

		@Override
		public byte[] content() {
			return content.clone(); // records expose their state — keep the stored bytes immutable
		}
	}

	/** Whether this backend serves media streams for the given entity type. */
	boolean supports(EClass entityType);

	/**
	 * The media stream of the entity with the given key, or {@link Optional#empty()} when the
	 * entity does not exist or carries no content yet ({@code 404}).
	 */
	Optional<MediaStream> readMedia(EClass entityType, String rawKey);

	/**
	 * Replaces the media stream of the entity with the given key. Returns {@code false} when the
	 * entity does not exist ({@code 404}).
	 */
	boolean writeMedia(EClass entityType, String rawKey, MediaStream stream);
}
