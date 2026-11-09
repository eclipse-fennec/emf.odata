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
package org.eclipse.fennec.odata.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.odata.codec.json.ODataJsonResourceImpl;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.odata.ocl.evaluator.OclEvaluationException;
import org.eclipse.fennec.odata.query.ODataQueryNotImplementedException;
import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.ResourcePath;
import org.eclipse.fennec.odata.query.WireView;
import org.eclipse.fennec.odata.wire.api.ControlNames;
import org.eclipse.fennec.odata.wire.api.WireJson;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The write arm of {@link ODataServlet} (OASIS "Updatable Service": POST to the set,
 * PATCH/PUT/DELETE of an entity, writes below the entity level, delta collection updates,
 * payload decoding incl. {@code @odata.bind} extraction and the created/updated response
 * shapes). Extracted dispatcher — routing, ETag preconditions, media and action dispatch
 * stay on the servlet and are reached through package-private collaborators.
 */
final class WriteDispatcher {

	private static final System.Logger LOGGER = System.getLogger(WriteDispatcher.class.getName());

	private final ODataServlet servlet;

	/**
	 * Set while a {@code $batch} atomicity group holds a transaction on this thread: a deep insert
	 * joins it instead of opening its own.
	 */
	static final ThreadLocal<Boolean> GROUP_TRANSACTION = new ThreadLocal<>();

	WriteDispatcher(ODataServlet servlet) {
		this.servlet = servlet;
	}


void execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
	response.setHeader("OData-Version", ODataServlet.negotiateVersion(request));
	try {
		dispatchWrite(request, response);
	} catch (WriteConflictException e) {
		servlet.error(response, HttpServletResponse.SC_CONFLICT, e.getMessage());
	} catch (OclEvaluationException | IllegalArgumentException e) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
	} catch (ODataQueryNotImplementedException e) {
		servlet.error(response, 501, e.getMessage());
	} catch (UnsupportedOperationException e) {
		servlet.error(response, 501, "the backend does not support this request");
	} catch (Exception e) {
		// no exception details leave the server (no class names, no stack traces) — but the
		// server MUST record what it hid, so an operator can tell a bug from an attack
		LOGGER.log(System.Logger.Level.ERROR, () -> "unhandled failure serving "
				+ request.getMethod() + " " + request.getRequestURI(), e);
		servlet.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error");
	}
}

private void dispatchWrite(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	String rawPath = request.getPathInfo() == null ? "/" : request.getPathInfo();
	if ("DELETE".equals(request.getMethod()) && rawPath.startsWith("/$async/")) {
		// cancelling the monitor aborts a still-running execution and discards its result
		// (11.6: DELETE the monitor) — cancel(true) interrupts the worker, best effort
		if (servlet.asyncDispatcher.cancel(rawPath.substring("/$async/".length()))) {
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		} else {
			servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "unknown status monitor");
		}
		return;
	}
	if ("/".equals(rawPath) || rawPath.startsWith("/$")) {
		servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"this resource is not writable");
		return;
	}
	if ("POST".equals(request.getMethod()) && servlet.operations.isActionImport(rawPath.substring(1))) {
		servlet.operations.actionImport(rawPath.substring(1), request, response); // POST ActionName, params in the body
		return;
	}
	ResourcePath path;
	try {
		path = servlet.resourceParser.parse(rawPath.substring(1));
	} catch (ODataQueryParseException e) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "resource not found");
		return;
	}
	path = servlet.resolveKeyAliases(path, request, response);
	if (path == null) {
		return; // 400 already written
	}
	path = servlet.keyAsSegment(path);
	EClass entityType = servlet.resolveEntityType(path.entitySet());
	if (entityType == null) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND,
				"unknown entity set '" + ODataJson.sanitize(path.entitySet()) + "'");
		return;
	}
	// POST Set(key)/Ns.Action — a bound action (parameters in the body). The qualified action
	// name parses as a single cast-shaped segment; it is dispatched through the operation SPI,
	// not the write backend, so it is intercepted before the WriteService is resolved.
	if ("POST".equals(request.getMethod()) && path.key() != null && path.segments().size() == 1
			&& path.segments().get(0) instanceof ResourcePath.TypeCastSegment action) {
		servlet.operations.boundAction(path, action.qualifiedName(), request, response);
		return;
	}
	// 4.01 13.2.1/9.5: the same action invoked UNQUALIFIED (default namespace) parses as a
	// property segment — dispatched as an action when the name is an operation, not a feature
	if ("POST".equals(request.getMethod()) && path.key() != null && path.segments().size() == 1
			&& path.segments().get(0) instanceof ResourcePath.PropertySegment property
			&& property.key() == null
			&& entityType.getEStructuralFeature(property.name()) == null
			&& OperationDispatcher.hasBoundOperation(entityType, property.name())) {
		servlet.operations.boundAction(path, property.name(), request, response);
		return;
	}
	// entity-level compound-key writes go through the named-key SPI overloads; below the
	// entity ($ref/nav/media) the SPI is single-raw-key — refused honestly
	if (!path.namedKeys().isEmpty() && !path.segments().isEmpty()) {
		servlet.error(response, 501, "writes below a composite-key entity are not supported");
		return;
	}
	// PUT Set(key)/$value on a media entity replaces the binary stream — routed to the
	// MediaService SPI before the WriteService (and its JSON-only content-type guard).
	if ("PUT".equals(request.getMethod()) && path.key() != null && path.segments().size() == 1
			&& path.segments().get(0) instanceof ResourcePath.ValueSegment
			&& ODataServlet.hasStream(entityType)) {
		servlet.mediaWrite(entityType, path, request, response);
		return;
	}
	WriteService writeService = servlet.writeServices.stream()
			.filter(s -> s.supports(entityType)).findFirst().orElse(null);
	if (writeService == null) {
		servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"no writable backend for '" + ODataJson.sanitize(path.entitySet()) + "'");
		return;
	}
	if (!path.segments().isEmpty()) {
		writeBelowEntity(path, entityType, writeService, request, response);
		return;
	}

	switch (request.getMethod()) {
		case "POST" -> {
			if (path.key() != null) {
				servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
						"POST addresses the entity set, not an entity");
				return;
			}
			WritePayload payload = readPayload(request, response, entityType);
			if (payload == null) {
				return; // error already written
			}
			EObject created = deepCreate(entityType, writeService, payload.entity());
			if (!payload.bindings().isEmpty()) {
				applyBindings(writeService, entityType,
						rawKeyOf(created, entityType), payload.bindings());
			}
			respondCreated(path.entitySet(), created, entityType, request, response);
		}
		case "PATCH", "PUT" -> {
			if (path.key() == null) {
				if ("PATCH".equals(request.getMethod()) && path.segments().isEmpty()) {
					// [OData-JSON] "Update a Collection of Entities": a delta payload
					collectionUpdate(entityType, writeService, request, response);
					return;
				}
				servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
						request.getMethod() + " addresses one entity by key");
				return;
			}
			if (!servlet.preconditionHolds(entityType, path, request, response)) {
				return; // 428/412 already written
			}
			WritePayload payload = readPayload(request, response, entityType);
			if (payload == null) {
				return; // error already written
			}
			boolean replace = "PUT".equals(request.getMethod());
			EObject entity = payload.entity();
			if (!replace && payload.clearsParts()) {
				entity = path.namedKeys().isEmpty() ? withClearedParts(entityType, path.key(), payload) : null;
				if (entity == null) {
					servlet.error(response, 501, "clearing a wire property needs a read backend for the current state");
					return;
				}
				replace = true;
			}
			WriteService.WriteResult result = path.namedKeys().isEmpty()
					? writeService.update(entityType, path.key(), entity, replace)
					: writeService.update(entityType, path.namedKeys(), entity, replace);
			if (!payload.bindings().isEmpty()) {
				applyBindings(writeService, entityType, path.key(), payload.bindings());
			}
			if (result.created()) { // OData upsert (13.1.1/29)
				respondCreated(path.entitySet(), result.entity(), entityType, request, response);
			} else {
				respondUpdated(path.entitySet(), result.entity(), entityType, request, response);
			}
		}
		case "DELETE" -> {
			if (path.key() == null) {
				servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
						"DELETE addresses one entity by key");
				return;
			}
			if (!servlet.preconditionHolds(entityType, path, request, response)) {
				return; // 428/412 already written
			}
			if (path.namedKeys().isEmpty() ? writeService.delete(entityType, path.key())
					: writeService.delete(entityType, path.namedKeys())) {
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			} else {
				servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
			}
		}
		default -> servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"method not supported");
	}
}

/**
 * Writes below the entity level ([OData-Protocol] 11.4): {@code POST Set(key)/nav}
 * creates a related entity (20), {@code PUT/POST/DELETE …/nav/$ref} manage references
 * (21/22/25), {@code PATCH/PUT/DELETE Set(key)/prop} write one primitive property (30/31).
 */
private void writeBelowEntity(ResourcePath path, EClass entityType, WriteService writeService,
		HttpServletRequest request, HttpServletResponse response) throws IOException {
	if (path.key() == null || path.segments().size() > 2
			|| !(path.segments().get(0) instanceof ResourcePath.PropertySegment property)) {
		servlet.error(response, 501, "this write target is not implemented");
		return;
	}
	EStructuralFeature feature = entityType.getEStructuralFeature(property.name());
	WireView view = Wire.current().view();
	if (feature != null && view.partOf(entityType, feature) != null) {
		feature = null; // a part of a wire property is not addressable (#119)
	}
	WireView.Binding wire = feature == null && path.segments().size() == 1 && property.key() == null
			? view.property(entityType, property.name()) : null;
	if (wire != null) {
		wirePropertyWrite(path, entityType, wire, writeService, request, response);
		return;
	}
	if (feature == null) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND,
				"unknown property '" + ODataJson.sanitize(property.name()) + "'");
		return;
	}
	QueryService reader = servlet.queryServices.stream()
			.filter(s -> s.supports(entityType)).findFirst().orElse(null);
	if (reader != null && servlet.currentEntity(entityType, path.key()) == null) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "entity not found");
		return;
	}
	if (path.segments().size() == 2) { // …/nav/$ref  or  …/nav(targetKey)/$ref
		if (!(path.segments().get(1) instanceof ResourcePath.RefSegment)
				|| !(feature instanceof EReference reference)) {
			servlet.error(response, 501, "this write target is not implemented");
			return;
		}
		if (property.key() != null) { // 4.01 (13.2.1/19): remove a collection member by key
			if (!"DELETE".equals(request.getMethod()) || !reference.isMany()) {
				servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
						"keyed $ref segments only support DELETE on collections");
				return;
			}
			if (writeService.unlink(entityType, path.key(), reference.getName(), property.key())) {
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			} else {
				servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "reference not found");
			}
			return;
		}
		referenceWrite(path, entityType, reference, writeService, request, response);
		return;
	}
	if (property.key() != null) {
		servlet.error(response, 501, "this write target is not implemented");
		return;
	}
	if (feature instanceof EReference reference) { // POST Set(key)/nav → create related
		if (!"POST".equals(request.getMethod())) {
			servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"only POST creates related entities");
			return;
		}
		WritePayload child = readPayload(request, response, reference.getEReferenceType());
		if (child == null) {
			return; // error already written
		}
		if (!child.bindings().isEmpty()) {
			servlet.error(response, 501, "@odata.bind is not supported below the entity level");
			return;
		}
		EObject created = writeService.createRelated(entityType, path.key(),
				reference.getName(), child.entity());
		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setHeader("Location", request.getRequestURI());
		String json = servlet.formats.entityJson(created, created.eClass(), null, Set.of());
		response.setContentType(ODataServlet.contentTypeJson());
		response.getWriter().write(json);
		return;
	}
	propertyWrite(path, entityType, feature, writeService, request, response);
}

/** {@code PUT} sets a single-valued, {@code POST} adds to a collection-valued reference. */
private void referenceWrite(ResourcePath path, EClass entityType, EReference reference,
		WriteService writeService, HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	switch (request.getMethod()) {
		case "PUT", "POST" -> {
			if ("PUT".equals(request.getMethod()) == reference.isMany()) {
				servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
						"PUT sets single-valued, POST adds to collection-valued references");
				return;
			}
			String targetKey = refTargetKey(referenceUrlFromBody(request, response),
					reference.getEReferenceType(), response);
			if (targetKey == null) {
				return; // error already written
			}
			writeService.link(entityType, path.key(), reference.getName(), targetKey);
			response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		}
		case "DELETE" -> {
			String targetKey = null;
			if (reference.isMany()) { // 4.0: DELETE …/$ref?$id=<target url>
				String id = ODataServlet.option(request, "$id");
				if (id == null) {
					servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
							"removing a collection reference requires $id");
					return;
				}
				targetKey = refTargetKey(id, reference.getEReferenceType(), response);
				if (targetKey == null) {
					return; // error already written
				}
			}
			if (writeService.unlink(entityType, path.key(), reference.getName(), targetKey)) {
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);
			} else {
				servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "reference not found");
			}
		}
		default -> servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"unsupported $ref method");
	}
}

/**
 * Single primitive property: {@code PATCH/PUT} with a value document, {@code DELETE} →
 * null (11.4.9.2). Expressed as a REPLACE of the current state with the one property
 * changed — a merge payload cannot say "set to null/default" in EMF terms ({@code eIsSet}
 * would read as absent and the backend would skip it).
 */
private void propertyWrite(ResourcePath path, EClass entityType, EStructuralFeature feature,
		WriteService writeService, HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	if (feature.isMany() || !(feature instanceof EAttribute attribute)
			|| servlet.keyAttributes(entityType).contains(attribute)) {
		servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
				"only single-valued non-key properties are writable");
		return;
	}
	if (!servlet.preconditionHolds(entityType, path.key(), request, response)) {
		return; // 428/412 already written
	}
	EObject current = servlet.currentEntity(entityType, path.key());
	if (current == null) {
		servlet.error(response, 501, "property writes need a read backend for the current state");
		return;
	}
	EObject payload = EcoreUtil.copy(current);
	switch (request.getMethod()) {
		case "PATCH", "PUT" -> {
			JsonNode document = readValueDocument(request, response);
			if (document == null) {
				return; // error already written
			}
			JsonNode value = document.get("value");
			if (value == null) {
				servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
						"property updates carry a {\"value\": …} document");
				return;
			}
			if (value.isNull()) {
				payload.eUnset(attribute); // the replace resets it to the default (null)
			} else {
				payload.eSet(attribute, EcoreUtil.createFromString(
						attribute.getEAttributeType(), value.asString()));
			}
		}
		case "DELETE" -> payload.eUnset(attribute); // the replace resets it to the default
		default -> {
			servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"unsupported property method");
			return;
		}
	}
	writeService.update(entityType, path.key(), payload, true);
	response.setStatus(HttpServletResponse.SC_NO_CONTENT);
}

/**
 * {@code PATCH/PUT/DELETE Set(key)/wireProperty} (#119): the value document carries the value
 * under {@code value} or the profile's member; DELETE writes null. Like a property write, a
 * replace of the current state with the parts changed.
 */
private void wirePropertyWrite(ResourcePath path, EClass entityType, WireView.Binding wire,
		WriteService writeService, HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	if (!servlet.preconditionHolds(entityType, path.key(), request, response)) {
		return; // 428/412 already written
	}
	EObject current = servlet.currentEntity(entityType, path.key());
	if (current == null) {
		servlet.error(response, 501, "property writes need a read backend for the current state");
		return;
	}
	JsonNode value;
	switch (request.getMethod()) {
		case "PATCH", "PUT" -> {
			JsonNode document = readValueDocument(request, response);
			if (document == null) {
				return; // error already written
			}
			String member = Wire.current().profile().propertyMember(ODataServlet.wireFeature(wire));
			value = member != null && document.has(member) ? document.get(member) : document.get("value");
			if (value == null) {
				servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
						"property updates carry a {\"value\": …} document");
				return;
			}
		}
		case "DELETE" -> value = null;
		default -> {
			servlet.error(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"unsupported property method");
			return;
		}
	}
	EObject payload = EcoreUtil.copy(current);
	readWire(wire, value).forEach((part, partValue) -> {
		if (partValue == null) {
			payload.eUnset(part);
		} else {
			payload.eSet(part, partValue);
		}
	});
	writeService.update(entityType, path.key(), payload, true);
	response.setStatus(HttpServletResponse.SC_NO_CONTENT);
}

/** The {@code @odata.id} of a {@code $ref} body ({@code {"@odata.id": "…"}}). */
private String referenceUrlFromBody(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	JsonNode document = readValueDocument(request, response);
	if (document == null) {
		return null;
	}
	JsonNode id = document.get("@odata.id");
	if (id == null || !id.isString()) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
				"$ref bodies carry {\"@odata.id\": \"…\"}");
		return null;
	}
	return id.asString();
}

/** Reads a small JSON document (value/$ref bodies) under the same guards as payloads. */
private JsonNode readValueDocument(HttpServletRequest request,
		HttpServletResponse response) throws IOException {
	String contentType = request.getContentType();
	if (contentType == null
			|| !contentType.toLowerCase(Locale.ROOT).startsWith("application/json")) {
		servlet.error(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
				"write payloads must be application/json");
		return null;
	}
	byte[] body = request.getInputStream().readNBytes(servlet.limits.maxBodyBytes() + 1);
	if (body.length > servlet.limits.maxBodyBytes()) {
		servlet.error(response, 413, "payload exceeds the maximum size of "
				+ servlet.limits.maxBodyBytes() + " bytes");
		return null;
	}
	try {
		return ODataServlet.JSON.readTree(body);
	} catch (Exception e) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return null;
	}
}

/**
 * Extracts the raw key of a reference-target URL ({@code @odata.id} / {@code $id}) and
 * validates that the addressed set matches the navigation's target type. Writes the error
 * response and returns null when the URL does not identify a matching entity.
 */
private String refTargetKey(String url, EClass targetType, HttpServletResponse response)
		throws IOException {
	if (url == null) {
		return null; // error already written
	}
	String tail = url.substring(url.lastIndexOf('/') + 1);
	ResourcePath ref;
	try {
		ref = servlet.resourceParser.parse(tail);
	} catch (ODataQueryParseException e) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "invalid reference target URL");
		return null;
	}
	EClass set = servlet.resolveEntityType(ref.entitySet());
	if (ref.key() == null || !ref.segments().isEmpty() || set == null
			|| !(targetType.isSuperTypeOf(set) || set.isSuperTypeOf(targetType))) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
				"the reference target does not address an entity of the navigation's type");
		return null;
	}
	return ref.key();
}

/** A decoded write payload: the entity plus {@code @odata.bind} targets per navigation. */
/**
 * @param wireParts the parts the payload's wire properties stand for (#119); a null value
 *                  clears the part, which a merge cannot express ({@link #withClearedParts})
 */
private record WritePayload(EObject entity, Map<EReference, List<String>> bindings,
		Map<EAttribute, Object> wireParts) {

	boolean clearsParts() {
		return wireParts.containsValue(null);
	}
}

private static final byte[] ODATA_BIND_MARKER = "@odata.bind".getBytes(StandardCharsets.US_ASCII);

/**
 * Whether {@code haystack} contains the ASCII {@code needle} — a byte scan that avoids
 * allocating a full {@code String} copy of the (up to {@code maxBodyBytes}) payload just to
 * probe for the rare {@code @odata.bind} marker.
 */
private static boolean containsAscii(byte[] haystack, byte[] needle) {
	if (needle.length == 0 || haystack.length < needle.length) {
		return needle.length == 0;
	}
	int last = haystack.length - needle.length;
	outer:
	for (int i = 0; i <= last; i++) {
		for (int j = 0; j < needle.length; j++) {
			if (haystack[i + j] != needle[j]) {
				continue outer;
			}
		}
		return true;
	}
	return false;
}

/**
 * Reads and decodes the JSON payload into an EObject of the addressed type — the codec
 * leaves exactly the transmitted features set ({@code eIsSet} = "was in the payload").
 * {@code "nav@odata.bind"} members ([OData-JSON] 8.5 / [OData-Protocol] 11.4.2.1) are
 * extracted BEFORE decoding and returned as raw target keys per navigation.
 * Writes the error response and returns null for media-type, size and syntax violations.
 */
/**
 * {@code PATCH Set} with a delta payload ([OData-JSON] "Update a Collection of Entities"):
 * the body carries {@code "@context":"#$delta"} and a {@code value} array of added/changed
 * entities (applied as PATCH upserts) and {@code @removed} deleted-entity objects (applied
 * as deletes). Runs inside a backend transaction when available — without
 * {@code continue-on-error} support, the request is all-or-nothing. Not implemented (501):
 * 4.0 flattened link objects, nested {@code nav@delta} representations, {@code @odata.bind}.
 */
private void collectionUpdate(EClass entityType, WriteService writeService,
		HttpServletRequest request, HttpServletResponse response) throws IOException {
	String contentType = request.getContentType();
	if (contentType == null
			|| !contentType.toLowerCase(Locale.ROOT).startsWith("application/json")) {
		servlet.error(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
				"write payloads must be application/json");
		return;
	}
	byte[] body = request.getInputStream().readNBytes(servlet.limits.maxBodyBytes() + 1);
	if (body.length > servlet.limits.maxBodyBytes()) {
		servlet.error(response, 413, "payload exceeds the maximum size of "
				+ servlet.limits.maxBodyBytes() + " bytes");
		return;
	}
	JsonNode document;
	try {
		document = body.length == 0 ? null : ODataServlet.JSON.readTree(body);
	} catch (Exception e) {
		document = null;
	}
	if (!(document instanceof ObjectNode envelope)) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return;
	}
	JsonNode context = envelope.has("@context") ? envelope.get("@context")
			: envelope.get("@odata.context");
	if (context == null || !context.asString().endsWith("#$delta")) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
				"collection updates carry \"@context\":\"#$delta\"");
		return;
	}
	if (!(envelope.get("value") instanceof ArrayNode entries)) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
				"the delta payload carries no 'value' array");
		return;
	}
	boolean transactional = writeService.transactional();
	if (transactional) {
		writeService.begin();
	}
	try {
		for (JsonNode element : entries) {
			applyDeltaEntry(element, entityType, writeService);
		}
		if (transactional) {
			writeService.commit();
		}
	} catch (RuntimeException e) {
		if (transactional) {
			writeService.rollback(); // all-or-nothing: no continue-on-error support
		}
		throw e; // the write() catches map parse/backend failures to 400/501/409
	}
	response.setStatus(HttpServletResponse.SC_NO_CONTENT);
}

/** One delta-payload entry: an {@code @removed} object → delete, anything else → upsert. */
private void applyDeltaEntry(JsonNode element, EClass entityType, WriteService writeService)
		throws IOException {
	if (!(element instanceof ObjectNode entry)) {
		throw new IllegalArgumentException("delta entries must be JSON objects");
	}
	String entryContext = entry.has("@odata.context") ? entry.get("@odata.context").asString()
			: entry.has("@context") ? entry.get("@context").asString() : "";
	if (entryContext.contains("/$link") || entryContext.contains("/$deletedLink")) {
		throw new UnsupportedOperationException(
				"4.0 flattened link objects are not implemented");
	}
	List<String> members = entry.propertyStream().map(Map.Entry::getKey).toList();
	for (String member : members) {
		if (member.endsWith("@delta")) {
			throw new UnsupportedOperationException(
					"nested delta representations are not implemented");
		}
		if (member.endsWith("@odata.bind") || member.endsWith("@bind")) {
			throw new UnsupportedOperationException(
					"@odata.bind inside collection updates is not implemented");
		}
	}
	boolean removed = entry.has("@removed") || entry.has("@odata.removed")
			|| entryContext.contains("$deletedEntity");
	if (removed) {
		if (!writeService.delete(entityType, deltaEntryKey(entry, entityType))) {
			throw new IllegalArgumentException("the delta removes an entity that does not exist");
		}
		return;
	}
	ObjectNode plain = entry.deepCopy(); // control information is not entity content
	members.stream().filter(member -> member.contains("@")).forEach(plain::remove);
	EObject payload = decodeEntity(ODataServlet.JSON.writeValueAsBytes(plain), entityType);
	writeService.update(entityType, deltaEntryKey(entry, entityType), payload, false);
}

/**
 * The addressed key of a delta entry: the {@code @id} control information
 * ({@code Set(key)} → the key literal) or the entry's key property. Compound key
 * predicates are not supported here (501 — the write SPI is single-raw-key).
 */
private String deltaEntryKey(ObjectNode entry, EClass entityType) {
	JsonNode id = entry.has("@id") ? entry.get("@id") : entry.get("@odata.id");
	if (id != null) {
		String url = id.asString();
		int open = url.lastIndexOf('(');
		if (open < 0 || !url.endsWith(")")) {
			throw new IllegalArgumentException("the entry's @id is not an entity id");
		}
		String literal = url.substring(open + 1, url.length() - 1);
		if (literal.contains("=")) {
			throw new UnsupportedOperationException(
					"compound keys in collection updates are not implemented");
		}
		return literal;
	}
	EAttribute key = servlet.keyAttribute(entityType);
	if (key == null || !entry.hasNonNull(key.getName())) {
		throw new IllegalArgumentException(
				"delta entries carry the @id control information or the key property");
	}
	return entry.get(key.getName()).asString();
}

/** Decodes one entity payload through the codec ({@code eIsSet} = "was in the payload"). */
private EObject decodeEntity(byte[] body, EClass entityType) throws IOException {
	ODataJsonResourceImpl resource = new ODataJsonResourceImpl(
			URI.createURI("request.odatajson"), servlet.metadataService);
	Map<Object, Object> options = new HashMap<>();
	options.put(CodecResource.CODEC_ROOT_TYPE, entityType);
	try {
		resource.load(new ByteArrayInputStream(body), options);
	} catch (Exception e) {
		throw new IllegalArgumentException("malformed payload");
	}
	if (resource.getContents().isEmpty()
			|| !(resource.getContents().get(0) instanceof EObject entity)) {
		throw new IllegalArgumentException("malformed payload");
	}
	return entity;
}

private WritePayload readPayload(HttpServletRequest request, HttpServletResponse response,
		EClass entityType) throws IOException {
	String contentType = request.getContentType();
	if (contentType == null
			|| !contentType.toLowerCase(Locale.ROOT).startsWith("application/json")) {
		servlet.error(response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
				"write payloads must be application/json");
		return null;
	}
	byte[] body = request.getInputStream().readNBytes(servlet.limits.maxBodyBytes() + 1);
	if (body.length > servlet.limits.maxBodyBytes()) {
		servlet.error(response, 413, "payload exceeds the maximum size of "
				+ servlet.limits.maxBodyBytes() + " bytes");
		return null;
	}
	if (body.length == 0) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "empty payload");
		return null;
	}
	ControlNames names = Wire.current().names();
	if (!Wire.current().odataControls() && names.keyValue() != null
			&& containsAscii(body, names.keyValue().getBytes(StandardCharsets.UTF_8))) {
		body = keyValuesToKeys(body, entityType, names.keyValue(), response);
		if (body == null) {
			return null; // error already written
		}
	}
	Map<EAttribute, Object> wireParts = new LinkedHashMap<>();
	if (!Wire.current().view().properties(entityType).isEmpty()) {
		body = extractWireProperties(body, entityType, wireParts, response);
		if (body == null) {
			return null; // error already written
		}
	}
	Map<EReference, List<String>> bindings = new LinkedHashMap<>();
	if (containsAscii(body, ODATA_BIND_MARKER)) {
		body = extractBindings(body, entityType, bindings, response);
		if (body == null) {
			return null; // error already written
		}
	}
	ODataJsonResourceImpl resource = new ODataJsonResourceImpl(
			URI.createURI("request.odatajson"), servlet.metadataService);
	String payloadContentType = request.getContentType();
	if (payloadContentType != null) { // IEEE754Compatible=true payloads carry Int64/Decimal as strings
		Matcher matcher = ODataServlet.IEEE754_PARAM.matcher(payloadContentType);
		resource.ieee754Compatible(matcher.find() && "true".equalsIgnoreCase(matcher.group(1)));
	}
	Map<Object, Object> options = new HashMap<>();
	options.put(CodecResource.CODEC_ROOT_TYPE, entityType);
	try {
		resource.load(new ByteArrayInputStream(body), options);
	} catch (Exception e) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return null;
	}
	if (resource.getContents().isEmpty()
			|| !(resource.getContents().get(0) instanceof EObject entity)) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return null;
	}
	wireParts.forEach((part, value) -> {
		if (value != null) {
			entity.eSet(part, value);
		}
	});
	return new WritePayload(entity, bindings, wireParts);
}

/**
 * The profile's key value member ({@code @iot.id}) as the key property, in the entity and in
 * every nested navigation value (#117): {@code {"Datastream":{"@iot.id":5}}} becomes
 * {@code {"Datastream":{"id":5}}} — a nested value with its key references the existing entity,
 * one without is created (deep insert). Only for a single key; null after a written error.
 */
private byte[] keyValuesToKeys(byte[] body, EClass entityType, String keyValue,
		HttpServletResponse response) throws IOException {
	JsonNode document;
	try {
		document = ODataServlet.JSON.readTree(body);
	} catch (Exception e) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return null;
	}
	if (!(document instanceof ObjectNode object)) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return null;
	}
	String problem = keyValuesToKeys(object, entityType, keyValue, 0);
	if (problem != null) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, problem);
		return null;
	}
	return ODataServlet.JSON.writeValueAsBytes(object);
}

private String keyValuesToKeys(ObjectNode object, EClass type, String keyValue, int depth) {
	if (depth > servlet.limits.maxNestingDepth()) {
		return "the payload nests too deep";
	}
	if (object.has(keyValue)) {
		List<EAttribute> keys = servlet.keyAttributes(type);
		if (keys.size() != 1) {
			return "'" + ODataJson.sanitize(keyValue) + "' needs a single key on " + type.getName();
		}
		String key = keys.get(0).getName();
		JsonNode value = object.remove(keyValue);
		if (object.has(key) && !object.get(key).equals(value)) {
			return "'" + ODataJson.sanitize(keyValue) + "' and '" + key + "' disagree";
		}
		object.set(key, value);
	}
	for (EReference reference : type.getEAllReferences()) {
		JsonNode member = object.get(reference.getName());
		List<JsonNode> values = member instanceof ArrayNode array ? array.valueStream().toList()
				: member == null ? List.of() : List.of(member);
		for (JsonNode value : values) {
			if (value instanceof ObjectNode nested) {
				String problem = keyValuesToKeys(nested, reference.getEReferenceType(), keyValue, depth + 1);
				if (problem != null) {
					return problem;
				}
			}
		}
	}
	return null;
}

/**
 * Creates an entity with its deep insert ([OData-Protocol] 11.4.2.2, #117): a nested value of a
 * navigation that carries no key is a new entity. Single-valued ones are created first and
 * referenced by key, collection members after the entity, inside its navigation — or with
 * their back reference set, when the navigation has a single-valued opposite. Atomic when the
 * backend is transactional; inside a {@code $batch} group it joins that transaction.
 */
EObject deepCreate(EClass entityType, WriteService writeService, EObject entity) {
	if (!hasNewNested(entity, 0)) {
		return writeService.create(entityType, entity);
	}
	boolean own = writeService.transactional() && GROUP_TRANSACTION.get() == null;
	if (own) {
		writeService.begin();
	}
	try {
		EObject created = create(entityType, writeService, entity, 0);
		if (own) {
			writeService.commit();
		}
		return created;
	} catch (RuntimeException e) {
		if (own) {
			writeService.rollback();
		}
		throw e;
	}
}

private record Pending(EReference reference, EObject child) {
}

private EObject create(EClass type, WriteService writeService, EObject entity, int depth) {
	List<Pending> later = prepare(entity, depth);
	EObject created = writeService.create(type, entity);
	createInside(type, writeService, created, later, depth);
	return created;
}

/**
 * Creates the new single-valued targets of an entity and references them by key; takes the
 * new collection members out, to be created after the entity.
 */
@SuppressWarnings("unchecked")
private List<Pending> prepare(EObject entity, int depth) {
	if (depth > servlet.limits.maxNestingDepth()) {
		throw new IllegalArgumentException("the deep insert nests too deep");
	}
	List<Pending> later = new ArrayList<>();
	for (EReference reference : entity.eClass().getEAllReferences()) {
		if (reference.isContainment() || reference.isContainer() || !entity.eIsSet(reference)) {
			continue;
		}
		if (reference.isMany()) {
			List<EObject> members = (List<EObject>) entity.eGet(reference);
			for (EObject member : List.copyOf(members)) {
				if (isNew(member)) {
					members.remove(member);
					later.add(new Pending(reference, member));
				}
			}
		} else if (entity.eGet(reference) instanceof EObject target && isNew(target)) {
			EObject created = create(target.eClass(), writeServiceFor(target.eClass()), target, depth + 1);
			entity.eSet(reference, keyStub(created));
		}
	}
	return later;
}

private void createInside(EClass ownerType, WriteService ownerService, EObject owner,
		List<Pending> pending, int depth) {
	for (Pending item : pending) {
		EObject child = item.child();
		EReference opposite = item.reference().getEOpposite();
		if (opposite != null && !opposite.isMany() && !opposite.isContainment()) {
			child.eSet(opposite, keyStub(owner)); // created with its back reference
			create(child.eClass(), writeServiceFor(child.eClass()), child, depth + 1);
			continue;
		}
		List<Pending> later = prepare(child, depth + 1);
		EObject created = ownerService.createRelated(ownerType, rawKeyOf(owner, ownerType),
				item.reference().getName(), child);
		createInside(child.eClass(), writeServiceFor(child.eClass()), created, later, depth + 1);
	}
}

/** Whether a payload holds a new entity in a navigation, at any depth. */
private boolean hasNewNested(EObject entity, int depth) {
	if (depth > servlet.limits.maxNestingDepth()) {
		return true; // prepare() refuses it
	}
	for (EReference reference : entity.eClass().getEAllReferences()) {
		if (reference.isContainment() || reference.isContainer() || !entity.eIsSet(reference)) {
			continue;
		}
		for (Object value : reference.isMany() ? (List<?>) entity.eGet(reference) : List.of(entity.eGet(reference))) {
			if (value instanceof EObject target && (isNew(target) || hasNewNested(target, depth + 1))) {
				return true;
			}
		}
	}
	return false;
}

/** A navigation value without its key is a new entity; with it, a reference. */
private boolean isNew(EObject value) {
	List<EAttribute> keys = servlet.keyAttributes(value.eClass());
	return !keys.isEmpty() && keys.stream().noneMatch(value::eIsSet);
}

/** The entity as a reference: its type with nothing but its key. */
private EObject keyStub(EObject entity) {
	EObject stub = EcoreUtil.create(entity.eClass());
	for (EAttribute key : servlet.keyAttributes(entity.eClass())) {
		stub.eSet(key, entity.eGet(key));
	}
	return stub;
}

private WriteService writeServiceFor(EClass type) {
	return servlet.writeServices.stream().filter(s -> s.supports(type)).findFirst()
			.orElseThrow(() -> new IllegalArgumentException("no backend writes " + type.getName()));
}

/**
 * Takes the wire properties out of a payload (#119): each value goes through its handler, the
 * parts it yields land in {@code parts}; the body comes back without them, so the codec sees
 * model features only. A part named in the payload is an unknown property, as on the wire.
 * Null after a written error response.
 */
private byte[] extractWireProperties(byte[] body, EClass entityType, Map<EAttribute, Object> parts,
		HttpServletResponse response) throws IOException {
	JsonNode document;
	try {
		document = ODataServlet.JSON.readTree(body);
	} catch (Exception e) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return null;
	}
	if (!(document instanceof ObjectNode object)) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return null;
	}
	WireView view = Wire.current().view();
	for (String member : object.propertyStream().map(Map.Entry::getKey).toList()) {
		EStructuralFeature feature = entityType.getEStructuralFeature(member);
		WireView.Binding wire = view.property(entityType, member);
		if (wire == null) {
			if (feature != null && view.partOf(entityType, feature) != null) {
				servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
						"unknown property '" + ODataJson.sanitize(member) + "'");
				return null;
			}
			continue;
		}
		parts.putAll(readWire(wire, object.get(member)));
		object.remove(member);
	}
	return ODataServlet.JSON.writeValueAsBytes(object);
}

/** The parts of one wire value; a JSON object or array reaches the handler as its text. */
static Map<EAttribute, Object> readWire(WireView.Binding wire, JsonNode value) {
	Object scalar;
	if (value == null || value.isNull()) {
		scalar = null;
	} else if (value.isString()) {
		scalar = value.asString();
	} else if (value.isBoolean()) {
		scalar = value.booleanValue();
	} else if (value.isNumber()) {
		scalar = value.isIntegralNumber() && value.canConvertToLong() ? (Object) value.longValue()
				: (Object) value.decimalValue();
	} else {
		scalar = new WireJson(value.toString()); // a JSON object or array (#118)
	}
	Map<EAttribute, Object> parts = wire.handler().read(scalar); // IllegalArgument → 400
	if (parts == null) {
		return Map.of();
	}
	for (EAttribute part : parts.keySet()) {
		if (!wire.parts().contains(part)) {
			throw new IllegalStateException("the handler of '" + wire.name()
					+ "' read an attribute that is not one of its parts");
		}
	}
	return parts;
}

/**
 * A merge that clears parts of a wire property as a replace of the current state (#119): a
 * merge payload cannot say "set to null" in EMF terms ({@code eIsSet} reads as absent). Null
 * when there is no read backend for the current state.
 */
private EObject withClearedParts(EClass entityType, String key, WritePayload payload) {
	EObject current = servlet.currentEntity(entityType, key);
	if (current == null) {
		return null;
	}
	EObject merged = EcoreUtil.copy(current);
	for (EAttribute attribute : entityType.getEAllAttributes()) {
		if (payload.entity().eIsSet(attribute)) {
			merged.eSet(attribute, payload.entity().eGet(attribute));
		}
	}
	payload.wireParts().forEach((part, value) -> {
		if (value == null) {
			merged.eUnset(part);
		} else {
			merged.eSet(part, value);
		}
	});
	return merged;
}

/**
 * Pulls {@code "nav@odata.bind"} members out of the payload: validates the navigation and
 * the target URLs, fills {@code bindings} and returns the body WITHOUT the bind members
 * (the codec only sees plain features). Null after a written error response.
 */
private byte[] extractBindings(byte[] body, EClass entityType,
		Map<EReference, List<String>> bindings, HttpServletResponse response)
		throws IOException {
	JsonNode document;
	try {
		document = ODataServlet.JSON.readTree(body);
	} catch (Exception e) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return null;
	}
	if (!(document instanceof ObjectNode object)) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return null;
	}
	List<String> bindMembers = new ArrayList<>();
	object.propertyStream().map(Map.Entry::getKey)
			.filter(name -> name.endsWith("@odata.bind")).forEach(bindMembers::add);
	for (String member : bindMembers) {
		String navigationName = member.substring(0, member.length() - "@odata.bind".length());
		if (!(entityType.getEStructuralFeature(navigationName) instanceof EReference reference)) {
			servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "'"
					+ ODataJson.sanitize(navigationName) + "' is not a navigation property");
			return null;
		}
		JsonNode value = object.get(member);
		List<String> targets = new ArrayList<>();
		if (value.isArray() && reference.isMany()) {
			for (JsonNode element : value) {
				if (!element.isString()) {
					servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
							"@odata.bind targets must be entity URLs");
					return null;
				}
				String key = refTargetKey(element.asString(),
						reference.getEReferenceType(), response);
				if (key == null) {
					return null; // error already written
				}
				targets.add(key);
			}
		} else if (value.isString() && !reference.isMany()) {
			String key = refTargetKey(value.asString(), reference.getEReferenceType(), response);
			if (key == null) {
				return null; // error already written
			}
			targets.add(key);
		} else {
			servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
					"@odata.bind takes a single entity URL for single-valued and"
							+ " an array of entity URLs for collection-valued navigations");
			return null;
		}
		bindings.put(reference, targets);
		object.remove(member);
	}
	return ODataServlet.JSON.writeValueAsBytes(object);
}

/** Applies {@code @odata.bind} targets as reference operations after the entity write. */
private void applyBindings(WriteService writeService, EClass entityType, String rawKey,
		Map<EReference, List<String>> bindings) {
	for (Map.Entry<EReference, List<String>> binding : bindings.entrySet()) {
		for (String targetKey : binding.getValue()) {
			writeService.link(entityType, rawKey, binding.getKey().getName(), targetKey);
		}
	}
}

/**
 * The entity's raw key literal (as it would appear in its edit URL), or null — the named-pair form
 * {@code k1=v1,k2=v2} for a composite key, which used to be truncated to its first component.
 */
private String rawKeyOf(EObject entity, EClass entityType) {
	List<EAttribute> ids = servlet.keyAttributes(entityType);
	if (ids.isEmpty()) {
		return null;
	}
	if (ids.size() == 1) {
		return urlKeyLiteral(ids.get(0), entity.eGet(ids.get(0)));
	}
	return ids.stream().map(id -> id.getName() + "=" + urlKeyLiteral(id, entity.eGet(id)))
			.collect(Collectors.joining(","));
}

/**
 * 201 with Location/OData-EntityId and the created entity body, unless the client asked for
 * {@code Prefer: return=minimal} — then 204 with just the headers ([OData-Protocol] 8.2.8.7).
 * A honoured preference is echoed via {@code Preference-Applied}.
 */
private void respondCreated(String setName, EObject entity, EClass entityType,
		HttpServletRequest request, HttpServletResponse response) throws IOException {
	String rawKey = rawKeyOf(entity, entityType);
	String editUrl = ODataServlet.contextRoot(request) + "/" + setName
			+ (rawKey == null ? "" : "(" + rawKey + ")");
	response.setHeader("Location", editUrl);
	response.setHeader("OData-EntityId", editUrl);
	if ("minimal".equals(returnPreference(request))) {
		response.setHeader("Preference-Applied", "return=minimal");
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		return;
	}
	if ("representation".equals(returnPreference(request))) {
		response.setHeader("Preference-Applied", "return=representation");
	}
	response.setStatus(HttpServletResponse.SC_CREATED);
	String json = servlet.formats.entityJson(entity, entityType, null, Set.of());
	response.setContentType(ODataServlet.contentTypeJson());
	response.getWriter().write(ODataServlet.withContext(
			ODataServlet.contextRoot(request) + "/$metadata#" + setName + "/$entity", json));
}

/**
 * 204 for a successful update, unless the client asked for {@code Prefer: return=representation}
 * — then 200 with the updated entity ([OData-Protocol] 8.2.8.7). A honoured preference is echoed
 * via {@code Preference-Applied}.
 */
private void respondUpdated(String setName, EObject entity, EClass entityType,
		HttpServletRequest request, HttpServletResponse response) throws IOException {
	if ("representation".equals(returnPreference(request)) && entity != null) {
		response.setHeader("Preference-Applied", "return=representation");
		response.setStatus(HttpServletResponse.SC_OK);
		String json = servlet.formats.entityJson(entity, entityType, null, Set.of());
		response.setContentType(ODataServlet.contentTypeJson());
		response.getWriter().write(ODataServlet.withContext(
				ODataServlet.contextRoot(request) + "/$metadata#" + setName + "/$entity", json));
		return;
	}
	if ("minimal".equals(returnPreference(request))) {
		response.setHeader("Preference-Applied", "return=minimal");
	}
	response.setStatus(HttpServletResponse.SC_NO_CONTENT);
}

/** The {@code return=} value of the {@code Prefer} header ("minimal"/"representation"), or null. */
private static String returnPreference(HttpServletRequest request) {
	String prefer = request.getHeader("Prefer");
	if (prefer == null) {
		return null;
	}
	for (String token : prefer.split(",")) {
		String t = token.trim();
		if (t.regionMatches(true, 0, "return=", 0, 7)) {
			String value = t.substring(7).trim();
			if (value.equalsIgnoreCase("minimal")) {
				return "minimal";
			}
			if (value.equalsIgnoreCase("representation")) {
				return "representation";
			}
		}
	}
	return null;
}

/** URL form of a key value: quoted (with {@code ''} escape) for strings, raw otherwise. */
private static String urlKeyLiteral(EAttribute id, Object value) {
	String text = String.valueOf(value);
	if (id.getEAttributeType() != null
			&& String.class.equals(id.getEAttributeType().getInstanceClass())) {
		return "'" + encodeControlChars(text.replace("'", "''")) + "'";
	}
	return encodeControlChars(text);
}

/**
 * Percent-encodes ISO control characters (incl. CR/LF) so a persisted key value cannot inject
 * line breaks into the {@code Location}/{@code OData-EntityId} response headers (HTTP response
 * splitting). Printable key values pass through unchanged.
 */
private static String encodeControlChars(String text) {
	StringBuilder encoded = null;
	for (int i = 0; i < text.length(); i++) {
		char c = text.charAt(i);
		if (c < 0x20 || c == 0x7F) {
			if (encoded == null) {
				encoded = new StringBuilder(text.length() + 8).append(text, 0, i);
			}
			encoded.append('%').append(HexFormat.of().withUpperCase().toHexDigits((byte) c));
		} else if (encoded != null) {
			encoded.append(c);
		}
	}
	return encoded == null ? text : encoded.toString();
}
}
