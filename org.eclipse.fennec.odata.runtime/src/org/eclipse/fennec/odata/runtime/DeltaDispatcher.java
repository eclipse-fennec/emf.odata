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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.odata.persistence.api.DeltaGoneException;
import org.eclipse.fennec.odata.persistence.api.DeltaService;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The change-tracking arm of {@link ODataServlet} ([OData-Protocol] 11.3, [OData-JSON] delta
 * payloads): {@code Prefer: odata.track-changes} detection, delta-link minting around the
 * defining query, the delta response shape (changed entities, removals in both wire forms)
 * and {@code $count} on a delta link. Backends provide the changes via the DeltaService SPI.
 */
final class DeltaDispatcher {

	private final ODataServlet servlet;

	DeltaDispatcher(ODataServlet servlet) {
		this.servlet = servlet;
	}


/** Whether the client sent {@code Prefer: odata.track-changes} (prefix optional, 4.01). */
static boolean trackChangesRequested(HttpServletRequest request) {
	String prefer = request.getHeader("Prefer");
	if (prefer == null) {
		return false;
	}
	for (String preference : prefer.split(",")) {
		String name = preference.trim().split("=", 2)[0].trim().toLowerCase(Locale.ROOT);
		if ("odata.track-changes".equals(name) || "track-changes".equals(name)) {
			return true;
		}
	}
	return false;
}

/**
 * The delta backend for the type — preferring the one that IS the serving query backend
 * (the JPA service implements both), so tokens and data always come from the same journal.
 */
DeltaService deltaService(EClass entityType, QueryService queryService) {
	return servlet.deltaServices.stream()
			.filter(s -> s == queryService).filter(s -> s.supports(entityType)).findFirst()
			.orElseGet(() -> servlet.deltaServices.stream()
					.filter(s -> s.supports(entityType)).findFirst().orElse(null));
}

/**
 * The delta link: the defining query's {@linkplain #ODataServlet.DELTA_LINK_OPTIONS carry-over options}
 * (plus its {@code @}-parameter aliases) re-encoded around the fresh {@code $deltatoken} —
 * self-describing, so the server stays stateless per client.
 */
String deltaLink(HttpServletRequest request, String token) {
	return definingUrl(request) + (definingUrl(request).indexOf('?') < 0 ? '?' : '&')
			+ "$deltatoken=" + java.net.URLEncoder.encode(token, StandardCharsets.UTF_8);
}

/** The defining query's URL without the token — the refetch target for {@code 410 Gone}. */
private String definingUrl(HttpServletRequest request) {
	StringBuilder link = new StringBuilder(request.getRequestURI());
	char separator = '?';
	for (String option : ODataServlet.DELTA_LINK_OPTIONS) {
		String value = ODataServlet.option(request, option);
		if (value != null) {
			link.append(separator).append(option).append('=')
					.append(java.net.URLEncoder.encode(value, StandardCharsets.UTF_8));
			separator = '&';
		}
	}
	for (Map.Entry<String, String> alias : servlet.parameterAliases(request).entrySet()) {
		link.append(separator)
				.append(java.net.URLEncoder.encode(alias.getKey(), StandardCharsets.UTF_8))
				.append('=').append(java.net.URLEncoder.encode(alias.getValue(), StandardCharsets.UTF_8));
		separator = '&';
	}
	return link.toString();
}

/**
 * Answers a delta link ({@code GET Set?$deltatoken=…}): everything that changed since the
 * token, as a delta payload — upserts with their current state, removals as deleted-entity
 * objects in the negotiated version's form, and a fresh delta link for the next round.
 * An aged-out token answers {@code 410 Gone} with the refetch URL in {@code Location}.
 */
void deltaResponse(String setName, String castName, EClass castType, ODataServlet.Target target,
		HttpServletRequest request, HttpServletResponse response) throws IOException {
	for (String name : request.getParameterMap().keySet()) {
		String normalized = ODataServlet.normalizeOption(name);
		if (normalized.startsWith("$") && !"$deltatoken".equals(normalized)
				&& !ODataServlet.DELTA_LINK_OPTIONS.contains(normalized)) {
			servlet.error(response, HttpServletResponse.SC_BAD_REQUEST,
					"query options must not be appended to a delta link");
			return;
		}
	}
	DeltaService deltaService = deltaService(target.entityType(), target.queryService());
	if (deltaService == null) {
		servlet.error(response, 501, "change tracking is not supported for this entity set");
		return;
	}
	EClass context = castType != null ? castType : target.entityType();
	Map<String, ResponseFormatter.ExpandItem> expand = servlet.formats.expandOption(request, context);
	if (!expand.isEmpty()) {
		if ("4.0".equals(ODataServlet.negotiateVersion(request))) {
			// 4.0 deltas MUST flatten expanded changes into link objects ([OData-JSON]) —
			// we emit the 4.01 form (full expanded representations) only
			servlet.error(response, 501, "4.0 flattened delta payloads are not implemented");
			return;
		}
		if (!deltaService.supportsExpandTracking()) {
			servlet.error(response, 501, "expanded change tracking is not supported by this backend");
			return;
		}
	}
	Map<String, String> aliases = servlet.parameterAliases(request);
	SelectTree select = servlet.formats.selectOption(request, context);
	EntityQuery definingQuery = new EntityQuery(target.entityType(), castType,
			servlet.parseChecked(servlet.filterWithSearch(request, context),
					filter -> aliases.isEmpty() ? servlet.parser.parseFilter(filter, context)
							: servlet.parser.parseFilter(filter, context, aliases)),
			List.of(), 0, -1, false, expand.keySet());

	DeltaService.DeltaResult delta;
	try {
		// Prefer: maxpagesize pages the delta response server-driven (11.3.2): a truncated
		// window's follow-up link is a NEXT link; the final page carries the delta link
		int span = ODataServlet.maxPageSizePreference(request);
		delta = span > 0
				? deltaService.changesSince(definingQuery, ODataServlet.option(request, "$deltatoken"), span)
				: deltaService.changesSince(definingQuery, ODataServlet.option(request, "$deltatoken"));
	} catch (DeltaGoneException e) {
		// the client refetches the full set: the defining query without the token (11.3.2)
		response.setHeader("Location", definingUrl(request));
		servlet.error(response, 410, "the delta token is no longer valid");
		return;
	}

	StringBuilder json = ODataServlet.envelopeHead(ODataServlet.contextRoot(request) + "/$metadata#" + setName
			+ (castName != null ? "/" + castName : "") + "/$delta");
	ODataServlet.envelopeProperty(json).append("\"value\":[");
	boolean first = true;
	for (EObject entity : delta.changed()) {
		if (!first) {
			json.append(',');
		}
		first = false;
		// upserts ride the regular expand pipeline: an expanding defining query serializes
		// the FULL current representation of the expanded navigations ([OData-JSON] — the
		// spec-legal alternative to nested delta representations)
		json.append(servlet.formats.entityJson(entity, context, select, expand));
	}
	boolean v40 = "4.0".equals(ODataServlet.negotiateVersion(request));
	for (DeltaService.Removal removal : delta.removals()) {
		if (!first) {
			json.append(',');
		}
		first = false;
		String id = setName + "(" + ODataServlet.keyLiteral(removal.keyValues()) + ")";
		if (v40) { // 4.0 deleted-entity object: context fragment + plain id property
			json.append("{\"@odata.context\":\"#").append(setName).append("/$deletedEntity\",")
					.append("\"reason\":\"").append(removal.reason()).append("\",")
					.append("\"id\":\"").append(ODataJson.sanitize(id)).append("\"}");
		} else { // 4.01 form: @removed control information
			json.append("{\"@removed\":{\"reason\":\"").append(removal.reason()).append("\"},")
					.append("\"@id\":\"").append(ODataJson.sanitize(id)).append("\"}");
		}
	}
	json.append(']');
	json.append(delta.truncated() ? ",\"@odata.nextLink\":\"" : ",\"@odata.deltaLink\":\"")
			.append(ODataJson.sanitize(deltaLink(request, delta.nextToken()))).append('"');
	json.append('}');
	response.setContentType(ODataServlet.contentTypeJson());
	response.getWriter().write(json.toString());
}

/**
 * {@code GET Set/$count?$deltatoken=…} ([OData-Protocol] 11.3.2): the number of changes the
 * delta link would return — added, changed and deleted entities.
 */
void deltaCount(ODataServlet.Target target, EClass castType, HttpServletRequest request,
		HttpServletResponse response) throws IOException {
	DeltaService deltaService = deltaService(target.entityType(), target.queryService());
	if (deltaService == null) {
		servlet.error(response, 501, "change tracking is not supported for this entity set");
		return;
	}
	EClass context = castType != null ? castType : target.entityType();
	Map<String, String> aliases = servlet.parameterAliases(request);
	EntityQuery definingQuery = new EntityQuery(target.entityType(), castType,
			servlet.parseChecked(servlet.filterWithSearch(request, context),
					filter -> aliases.isEmpty() ? servlet.parser.parseFilter(filter, context)
							: servlet.parser.parseFilter(filter, context, aliases)),
			List.of(), 0, -1, false);
	DeltaService.DeltaResult delta;
	try {
		delta = deltaService.changesSince(definingQuery, ODataServlet.option(request, "$deltatoken"));
	} catch (DeltaGoneException e) {
		response.setHeader("Location", definingUrl(request));
		servlet.error(response, 410, "the delta token is no longer valid");
		return;
	}
	response.setContentType("text/plain;charset=UTF-8");
	response.getWriter().write(String.valueOf(delta.changed().size() + delta.removals().size()));
}
}
