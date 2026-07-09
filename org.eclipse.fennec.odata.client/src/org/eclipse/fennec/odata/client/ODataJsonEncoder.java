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
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.model.metadata.api.MetadataService;
import org.eclipse.fennec.odata.codec.json.ODataJsonResourceImpl;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * EObject → OData-JSON for write request bodies: the counterpart of {@link ODataJsonDecoder}, using
 * the SAME codec profile (E3, {@link ODataJsonResourceImpl}) the server decodes with. Only the
 * features the caller actually set are emitted (EMF {@code eIsSet}), so a PATCH sends exactly the
 * changed properties. Containment children ride along, giving deep-insert bodies for free; links to
 * ALREADY-existing related entities are expressed as {@code "nav@odata.bind"} members
 * ([OData-JSON] 8.5 / [OData-Protocol] 11.4.2.1).
 */
final class ODataJsonEncoder {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ODataJsonEncoder() {
	}

	static String encode(EObject entity, EClass entityType, MetadataService metadataService) {
		return encode(entity, entityType, metadataService, Map.of());
	}

	/**
	 * As {@link #encode(EObject, EClass, MetadataService)}, but additionally emits
	 * {@code "nav@odata.bind"} members for links to existing entities. Each binding maps a
	 * navigation property name to its target(s): a single edit URL {@code String} for a
	 * single-valued navigation, an {@code Iterable<String>} (or {@code String[]}) of edit URLs for a
	 * collection-valued one. The navigation must be a non-containment reference of {@code entityType}
	 * — containment children are written by deep insert, not bound.
	 */
	static String encode(EObject entity, EClass entityType, MetadataService metadataService,
			Map<String, ?> bindings) {
		String json = codecEncode(entity, entityType, metadataService);
		if (bindings == null || bindings.isEmpty()) {
			return json;
		}
		ObjectNode object;
		try {
			object = (ObjectNode) MAPPER.readTree(json);
		} catch (RuntimeException e) {
			throw new ODataClientException("could not encode a " + entityType.getName() + " payload", e);
		}
		bindings.forEach((navigation, target) -> applyBind(object, entityType, navigation, target));
		return object.toString();
	}

	private static String codecEncode(EObject entity, EClass entityType,
			MetadataService metadataService) {
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

	/** Sets the {@code nav@odata.bind} member on the payload, validating navigation + cardinality. */
	private static void applyBind(ObjectNode object, EClass entityType, String navigation,
			Object target) {
		String member = navigation + "@odata.bind";
		if (!(entityType.getEStructuralFeature(navigation) instanceof EReference reference)) {
			throw new ODataClientException(
					"'" + navigation + "' is not a navigation property of " + entityType.getName());
		}
		if (reference.isContainment()) {
			throw new ODataClientException("'" + navigation + "' is a containment navigation — write its"
					+ " children by deep insert, do not @odata.bind them");
		}
		if (reference.isMany()) {
			Iterable<?> urls = target instanceof Object[] array ? java.util.Arrays.asList(array)
					: target instanceof Iterable<?> iterable ? iterable
					: null;
			if (urls == null) {
				throw new ODataClientException("@odata.bind on the collection-valued '" + navigation
						+ "' needs an Iterable or array of entity URLs");
			}
			ArrayNode array = MAPPER.createArrayNode();
			urls.forEach(url -> array.add(requireUrl(navigation, url)));
			object.set(member, array);
			return;
		}
		if (target instanceof Iterable || target instanceof Object[]) {
			throw new ODataClientException("@odata.bind on the single-valued '" + navigation
					+ "' takes exactly one entity URL");
		}
		object.put(member, requireUrl(navigation, target));
	}

	private static String requireUrl(String navigation, Object url) {
		if (url instanceof CharSequence text) {
			return text.toString();
		}
		throw new ODataClientException(
				"@odata.bind target for '" + navigation + "' must be an entity URL string");
	}
}
