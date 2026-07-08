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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.model.metadata.api.MetadataService;
import org.eclipse.fennec.odata.codec.json.ODataJsonResourceImpl;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * OData-JSON responses → EObjects: the envelope ({@code value}, {@code @odata.count},
 * {@code @odata.nextLink}) is read with Jackson, every entity object is decoded through the
 * SAME codec profile the server serializes with (E3, {@link ODataJsonResourceImpl}) —
 * {@code @odata.*} control members are stripped first, they are control information, not data.
 */
final class ODataJsonDecoder {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private ODataJsonDecoder() {
	}

	static ODataPage page(String json, EClass entityType, MetadataService metadataService) {
		JsonNode root = parse(json);
		JsonNode value = root.get("value");
		if (value == null || !value.isArray()) {
			throw new ODataClientException("the collection response carries no 'value' array");
		}
		List<EObject> entities = new ArrayList<>();
		for (JsonNode element : value) {
			entities.add(decode(element, entityType, metadataService));
		}
		long count = root.has("@odata.count") ? root.get("@odata.count").asLong() : -1;
		String nextLink = root.has("@odata.nextLink") ? root.get("@odata.nextLink").asString() : null;
		return new ODataPage(entities, count, nextLink);
	}

	static EObject entity(String json, EClass entityType, MetadataService metadataService) {
		return decode(parse(json), entityType, metadataService);
	}

	/**
	 * Decodes a {@code $apply} response into generic rows: aggregation results are grouping keys +
	 * aggregate aliases (possibly nested), NOT entities, so they surface as plain maps.
	 */
	static List<Map<String, Object>> rows(String json) {
		JsonNode root = parse(json);
		JsonNode value = root.get("value");
		if (value == null || !value.isArray()) {
			throw new ODataClientException("the $apply response carries no 'value' array");
		}
		List<Map<String, Object>> rows = new ArrayList<>();
		for (JsonNode element : value) {
			@SuppressWarnings("unchecked")
			Map<String, Object> row = MAPPER.convertValue(element, Map.class);
			rows.add(row);
		}
		return rows;
	}

	private static JsonNode parse(String json) {
		try {
			return MAPPER.readTree(json);
		} catch (RuntimeException e) {
			// Jackson 3 signals parse failure with an (unchecked) JacksonException; wrap it as the
			// client's exception type. Errors (OOM/StackOverflow) are NOT swallowed — they propagate.
			throw new ODataClientException("the service answer is not parseable JSON", e);
		}
	}

	private static EObject decode(JsonNode node, EClass entityType,
			MetadataService metadataService) {
		if (!(node instanceof ObjectNode object)) {
			throw new ODataClientException("expected a JSON entity object");
		}
		stripControlInformation(object);
		ODataJsonResourceImpl resource = new ODataJsonResourceImpl(
				URI.createURI("client-response.odatajson"), metadataService);
		Map<Object, Object> options = new HashMap<>();
		options.put(CodecResource.CODEC_ROOT_TYPE, entityType);
		try {
			resource.load(new ByteArrayInputStream(
					MAPPER.writeValueAsBytes(object)), options);
		} catch (IOException | RuntimeException e) {
			// codec/IO failures on an untrusted server payload become the client's exception type;
			// Errors propagate rather than being masked as an "undecodable payload"
			throw new ODataClientException("undecodable entity payload for type '"
					+ entityType.getName() + "'", e);
		}
		if (resource.getContents().isEmpty()
				|| !(resource.getContents().get(0) instanceof EObject entity)) {
			throw new ODataClientException("undecodable entity payload for type '"
					+ entityType.getName() + "'");
		}
		return entity;
	}

	/** Removes {@code @odata.*} members (also property-scoped ones) recursively. */
	private static void stripControlInformation(JsonNode node) {
		if (node instanceof ObjectNode object) {
			List<String> control = new ArrayList<>();
			object.propertyStream().map(Map.Entry::getKey)
					.filter(name -> name.contains("@odata.")).forEach(control::add);
			control.forEach(object::remove);
			object.propertyStream().forEach(entry -> stripControlInformation(entry.getValue()));
		} else if (node != null && node.isArray()) {
			node.forEach(ODataJsonDecoder::stripControlInformation);
		}
	}
}
