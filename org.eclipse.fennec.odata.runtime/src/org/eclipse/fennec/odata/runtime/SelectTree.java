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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.odata.query.ODataQueryParseException;

/**
 * Parsed and model-validated {@code $select} tree ([OData-URL] 5.1.3): plain properties are
 * leaves, structured properties (complex or navigation) may carry a nested
 * {@code ($select=…)} option list — the 4.01 Intermediate MUST. Other nested options
 * ({@code $filter}, {@code $top}, …) are recognized and rejected as unimplemented.
 *
 * <p>Validation happens during parsing against the context {@link EClass}, recursively
 * against the target type of each structured property — unknown names fail the parse
 * (400 at the protocol layer).
 */
public final class SelectTree {

	private static final SelectTree LEAF = new SelectTree(Map.of());
	/** Nested option name incl. 4.01 variants: {@code $select=} / {@code select=} / case-insensitive. */
	private static final Pattern NESTED_SELECT = Pattern.compile("(?i)^\\$?select=");
	private static final Pattern NESTED_KNOWN = Pattern.compile(
			"(?i)^\\$?(filter|orderby|top|skip|count|expand|search|compute|levels)=");

	private final Map<String, SelectTree> children;

	private SelectTree(Map<String, SelectTree> children) {
		this.children = children;
	}

	/** Parses and validates a {@code $select} value against the entity type. */
	public static SelectTree parse(String select, EClass entityType) {
		return new SelectTree(items(select, entityType));
	}

	/** The sub-tree selected under {@code name}, or null when the property is not selected. */
	public SelectTree child(String name) {
		return children.get(name);
	}

	/** A leaf selection takes the property value as a whole. */
	public boolean isLeaf() {
		return children.isEmpty();
	}

	/** The selected property names on this level. */
	public Set<String> names() {
		return children.keySet();
	}

	private static Map<String, SelectTree> items(String value, EClass type) {
		Map<String, SelectTree> items = new LinkedHashMap<>();
		for (String item : splitTopLevel(value, ',')) {
			String trimmed = item.trim();
			if (trimmed.isEmpty()) {
				throw new ODataQueryParseException("empty $select item");
			}
			int paren = trimmed.indexOf('(');
			if (paren < 0) {
				feature(type, trimmed);
				items.put(trimmed, LEAF);
				continue;
			}
			if (!trimmed.endsWith(")")) {
				throw new ODataQueryParseException("malformed nested $select option: " + trimmed);
			}
			String name = trimmed.substring(0, paren).trim();
			EStructuralFeature feature = feature(type, name);
			if (!(feature.getEType() instanceof EClass target)) {
				throw new ODataQueryParseException(
						"nested $select requires a structured property, '" + name + "' is primitive");
			}
			items.put(name, new SelectTree(
					nestedItems(trimmed.substring(paren + 1, trimmed.length() - 1), target)));
		}
		return items;
	}

	/** The parenthesized option list: {@code ;}-separated, v1 implements only {@code $select}. */
	private static Map<String, SelectTree> nestedItems(String optionList, EClass target) {
		Map<String, SelectTree> nested = null;
		for (String option : splitTopLevel(optionList, ';')) {
			String trimmed = option.trim();
			var select = NESTED_SELECT.matcher(trimmed);
			if (select.find()) {
				if (nested != null) {
					throw new ODataQueryParseException("duplicate nested $select");
				}
				nested = items(trimmed.substring(select.end()), target);
				continue;
			}
			throw new ODataQueryParseException(NESTED_KNOWN.matcher(trimmed).find()
					? "nested $select supports only $select sub-options (v1)"
					: "unknown nested $select option: " + trimmed);
		}
		if (nested == null || nested.isEmpty()) {
			throw new ODataQueryParseException("empty nested $select option list");
		}
		return nested;
	}

	private static EStructuralFeature feature(EClass type, String name) {
		EStructuralFeature feature = type.getEStructuralFeature(name);
		if (feature == null) {
			throw new ODataQueryParseException(
					"unknown $select property '" + name + "' on " + type.getName());
		}
		return feature;
	}

	/** Splits on {@code separator} outside parentheses (nested option lists stay intact). */
	private static List<String> splitTopLevel(String value, char separator) {
		List<String> parts = new ArrayList<>();
		int depth = 0;
		int start = 0;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '(') {
				depth++;
			} else if (c == ')') {
				if (--depth < 0) {
					throw new ODataQueryParseException("unbalanced parentheses in $select");
				}
			} else if (c == separator && depth == 0) {
				parts.add(value.substring(start, i));
				start = i + 1;
			}
		}
		if (depth != 0) {
			throw new ODataQueryParseException("unbalanced parentheses in $select");
		}
		parts.add(value.substring(start));
		return parts;
	}
}
