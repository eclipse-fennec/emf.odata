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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Builder for an OData v4.01 JSON {@code $batch} request. Sub-requests are accumulated and sent as a
 * single {@code POST <root>/$batch} with a {@code {"requests":[…]}} body; {@link #execute()} returns
 * one {@link Result} per sub-request, in the order the server answered them.
 *
 * <p>Convenience methods ({@link #read}, {@link #create}, {@link #update}, {@link #delete}) auto-assign
 * sequential ids; use {@link #add} to supply an explicit id and {@code dependsOn} references (the id of
 * a request that must succeed first — the server short-circuits dependents of a failed request to 424).
 */
public final class ODataBatch {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final ODataClient client;
	private final ArrayNode requests = MAPPER.createArrayNode();
	private int sequence = 0;

	ODataBatch(ODataClient client) {
		this.client = client;
	}

	/** GET a resource relative to the service root (e.g. {@code "Product"}, {@code "Product('p1')"}). */
	public ODataBatch read(String relativeUrl) {
		return add(nextId(), "GET", relativeUrl, null, List.of());
	}

	/** POST a new entity to an entity set; the entity is encoded with the same codec as a direct create. */
	public ODataBatch create(String setName, EObject entity) {
		return add(nextId(), "POST", setName, encode(entity), List.of());
	}

	/** PATCH an existing entity addressed by its relative URL (e.g. {@code "Product('p1')"}). */
	public ODataBatch update(String entityUrl, EObject entity) {
		return add(nextId(), "PATCH", entityUrl, encode(entity), List.of());
	}

	/** DELETE an entity addressed by its relative URL. */
	public ODataBatch delete(String entityUrl) {
		return add(nextId(), "DELETE", entityUrl, null, List.of());
	}

	/**
	 * Adds a sub-request with an explicit id, verb, relative URL, optional JSON body and
	 * {@code dependsOn} references. Returns {@code this} for chaining.
	 */
	public ODataBatch add(String id, String method, String url, JsonNode body, List<String> dependsOn) {
		ObjectNode request = MAPPER.createObjectNode();
		request.put("id", id);
		request.put("method", method);
		request.put("url", url);
		if (body != null && !body.isNull()) {
			request.set("body", body);
			ObjectNode headers = MAPPER.createObjectNode();
			headers.put("content-type", "application/json");
			request.set("headers", headers);
		}
		if (dependsOn != null && !dependsOn.isEmpty()) {
			ArrayNode deps = MAPPER.createArrayNode();
			dependsOn.forEach(deps::add);
			request.set("dependsOn", deps);
		}
		requests.add(request);
		return this;
	}

	/** The id the NEXT convenience call ({@link #read}/{@link #create}/…) will use — handy for {@code dependsOn}. */
	public String nextId() {
		return String.valueOf(sequence++);
	}

	/** Sends the accumulated sub-requests and decodes the {@code responses} envelope. */
	public List<Result> execute() {
		ObjectNode envelope = MAPPER.createObjectNode();
		envelope.set("requests", requests);
		String body = MAPPER.writeValueAsString(envelope);

		ODataClient.Response response =
				client.exchange("POST", "$batch", "application/json", body, "application/json", Map.of());
		if (response.status() / 100 != 2) {
			throw new ODataClientException("$batch answered " + response.status(),
					response.status(), response.body());
		}

		JsonNode responses = MAPPER.readTree(response.body()).get("responses");
		if (responses == null || !responses.isArray()) {
			throw new ODataClientException("$batch response carries no 'responses' array");
		}
		List<Result> results = new ArrayList<>();
		for (JsonNode node : responses) {
			String id = node.path("id").asString(null);
			int status = node.path("status").asInt(0);
			JsonNode bodyNode = node.get("body");
			String text = bodyNode == null || bodyNode.isNull() ? null
					: bodyNode.isObject() || bodyNode.isArray() ? bodyNode.toString() : bodyNode.asString("");
			results.add(new Result(id, status, text, client));
		}
		return results;
	}

	private JsonNode encode(EObject entity) {
		String json = ODataJsonEncoder.encode(entity, entity.eClass(), client.metadataService());
		return MAPPER.readTree(json);
	}

	/** One sub-response of a {@code $batch}: its correlation id, HTTP status and raw JSON body. */
	public record Result(String id, int status, String body, ODataClient client) {

		/** {@code true} for a 2xx status. */
		public boolean isSuccess() {
			return status / 100 == 2;
		}

		/** Decodes the body as a single entity of the given type (for GET-one / create responses). */
		public EObject asEntity(EClass entityType) {
			return ODataJsonDecoder.entity(body, entityType, client.metadataService());
		}

		/** Decodes the body as a collection page (for GET-set responses). */
		public ODataPage asPage(EClass entityType) {
			return ODataJsonDecoder.page(body, entityType, client.metadataService());
		}
	}
}
