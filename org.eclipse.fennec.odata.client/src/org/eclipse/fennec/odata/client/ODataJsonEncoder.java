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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.model.metadata.api.MetadataService;
import org.eclipse.fennec.odata.codec.json.ODataJsonResourceImpl;

/**
 * EObject → OData-JSON for write request bodies: the counterpart of {@link ODataJsonDecoder}, using
 * the SAME codec profile (E3, {@link ODataJsonResourceImpl}) the server decodes with. Only the
 * features the caller actually set are emitted (EMF {@code eIsSet}), so a PATCH sends exactly the
 * changed properties. Containment children ride along, giving deep-insert bodies for free.
 */
final class ODataJsonEncoder {

	private ODataJsonEncoder() {
	}

	static String encode(EObject entity, EClass entityType, MetadataService metadataService) {
		ODataJsonResourceImpl resource = new ODataJsonResourceImpl(
				URI.createURI("client-write.odatajson"), metadataService);
		// copy so the caller's object is not attached to (moved into) this throwaway resource;
		// EcoreUtil.copy preserves the set/unset state, i.e. the minimal-payload semantics
		resource.getContents().add(EcoreUtil.copy(entity));
		Map<Object, Object> options = new HashMap<>();
		options.put(CodecResource.CODEC_ROOT_TYPE, entityType);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resource.save(out, options);
		} catch (IOException e) {
			throw new ODataClientException("could not encode a " + entityType.getName() + " payload", e);
		}
		return out.toString(StandardCharsets.UTF_8);
	}
}
