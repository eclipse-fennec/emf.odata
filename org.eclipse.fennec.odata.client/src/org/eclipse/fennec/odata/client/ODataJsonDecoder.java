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
		// 4.01 servers may omit the "odata." prefix on control information ([OData-JSON] 4.01
		// §4.1) — RESTier does when the client sends OData-MaxVersion: 4.01
		JsonNode count = root.has("@odata.count") ? root.get("@odata.count") : root.get("@count");
		JsonNode next = root.has("@odata.nextLink") ? root.get("@odata.nextLink") : root.get("@nextLink");
		long totalCount = count == null ? -1 : count.asLong(-1);
		String nextLink = next == null ? null : next.asString();
		return new ODataPage(entities, totalCount, nextLink, link(root, "deltaLink"));
	}

	/**
	 * Decodes a {@code $ref} read ([OData-Protocol] 11.2.8): a single entity-reference object
	 * or a {@code value} collection of them → the entity ids (e.g. {@code Products('p1')}).
	 */
	static List<String> referenceIds(String json) {
		JsonNode root = parse(json);
		JsonNode value = root.get("value");
		List<String> ids = new ArrayList<>();
		if (value != null && value.isArray()) {
			for (JsonNode element : value) {
				String id = referenceId(element);
				if (id != null) {
					ids.add(id);
				}
			}
			return ids;
		}
		String id = referenceId(root);
		if (id != null) {
			ids.add(id);
		}
		return ids;
	}

	private static String referenceId(JsonNode node) {
		JsonNode id = node.has("@odata.id") ? node.get("@odata.id") : node.get("@id");
		return id == null ? null : id.asString();
	}

	/** An {@code @odata.}-prefixed or 4.01 prefix-free control link of the envelope, or null. */
	private static String link(JsonNode root, String name) {
		JsonNode value = root.has("@odata." + name) ? root.get("@odata." + name)
				: root.get("@" + name);
		return value == null ? null : value.asString();
	}

	/**
	 * Decodes a delta response ([OData-JSON] delta payloads): entries carrying {@code @removed}
	 * (4.01) or a {@code …/$deletedEntity} context fragment (4.0) become {@link ODataDelta.Removal}s,
	 * everything else decodes as an added/changed entity with its current state.
	 */
	static ODataDelta delta(String json, EClass entityType, MetadataService metadataService) {
		JsonNode root = parse(json);
		JsonNode value = root.get("value");
		if (value == null || !value.isArray()) {
			throw new ODataClientException("the delta response carries no 'value' array");
		}
		List<EObject> changed = new ArrayList<>();
		List<ODataDelta.Removal> removals = new ArrayList<>();
		for (JsonNode element : value) {
			if (!(element instanceof ObjectNode object)) {
				throw new ODataClientException("expected a JSON delta entry object");
			}
			JsonNode removed = object.has("@removed") ? object.get("@removed")
					: object.get("@odata.removed");
			JsonNode context = object.has("@odata.context") ? object.get("@odata.context")
					: object.get("@context");
			boolean deleted40 = context != null && context.asString().contains("/$deletedEntity");
			if (removed == null && !deleted40) {
				changed.add(decode(element, entityType, metadataService));
				continue;
			}
			JsonNode id = object.has("@id") ? object.get("@id")
					: object.has("@odata.id") ? object.get("@odata.id") : object.get("id");
			String reason = null;
			if (removed instanceof ObjectNode removedObject && removedObject.hasNonNull("reason")) {
				reason = removedObject.get("reason").asString();
			} else if (object.hasNonNull("reason")) { // the 4.0 deleted-entity form
				reason = object.get("reason").asString();
			}
			removals.add(new ODataDelta.Removal(id == null ? null : id.asString(), reason));
		}
		return new ODataDelta(changed, removals, link(root, "deltaLink"), link(root, "nextLink"));
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

	/**
	 * Decodes a {@code $compute} read into typed rows: each row's members are split into the entity's
	 * own model properties (decoded into a typed {@link EObject}) and the dynamic computed members
	 * (any member the entity type has no structural feature for), which stay as coercible raw values.
	 */
	static List<ComputedRow> computedRows(String json, EClass entityType,
			MetadataService metadataService) {
		JsonNode root = parse(json);
		JsonNode value = root.get("value");
		if (value == null || !value.isArray()) {
			throw new ODataClientException("the collection response carries no 'value' array");
		}
		List<ComputedRow> rows = new ArrayList<>();
		for (JsonNode element : value) {
			if (!(element instanceof ObjectNode object)) {
				throw new ODataClientException("expected a JSON entity object");
			}
			stripControlInformation(object);
			ObjectNode entityNode = MAPPER.createObjectNode();
			Map<String, Object> computed = new java.util.LinkedHashMap<>();
			object.propertyStream().forEach(member -> {
				if (entityType.getEStructuralFeature(member.getKey()) != null) {
					entityNode.set(member.getKey(), member.getValue());
				} else {
					computed.put(member.getKey(), MAPPER.convertValue(member.getValue(), Object.class));
				}
			});
			rows.add(new ComputedRow(decode(entityNode, entityType, metadataService), computed));
		}
		return rows;
	}

	private static boolean isControlInformation(String memberName) {
		int at = memberName.indexOf('@');
		if (at < 0) {
			return false;
		}
		String suffix = memberName.substring(at + 1);
		return suffix.startsWith("odata.") || !suffix.contains(".");
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

	/** Serializes a value (e.g. an action parameter map) to a JSON string. */
	static String toJson(Object value) {
		try {
			return MAPPER.writeValueAsString(value);
		} catch (RuntimeException e) {
			throw new ODataClientException("could not encode the request body", e);
		}
	}

	/** Extracts the {@code value} member of a function/primitive response as a plain Java value. */
	static Object value(String json) {
		JsonNode value = parse(json).get("value");
		if (value == null) {
			throw new ODataClientException("the response carries no 'value' member");
		}
		return MAPPER.convertValue(value, Object.class);
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

	/**
	 * Removes control information (also property-scoped) recursively: {@code @odata.*} members and
	 * the 4.01 prefix-free form ({@code @count}, {@code name@type}, …) — a suffix WITHOUT a dot is
	 * control information, {@code @Ns.Term} instance annotations (always qualified) survive.
	 */
	private static void stripControlInformation(JsonNode node) {
		if (node instanceof ObjectNode object) {
			List<String> control = new ArrayList<>();
			object.propertyStream().map(Map.Entry::getKey)
					.filter(ODataJsonDecoder::isControlInformation).forEach(control::add);
			control.forEach(object::remove);
			object.propertyStream().forEach(entry -> stripControlInformation(entry.getValue()));
		} else if (node != null && node.isArray()) {
			node.forEach(ODataJsonDecoder::stripControlInformation);
		}
	}
}
