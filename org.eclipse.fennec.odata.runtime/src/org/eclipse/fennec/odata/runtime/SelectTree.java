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
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.query.ODataQueryParseException;

/**
 * Parsed and model-validated {@code $select} tree ([OData-URL] 5.1.3): plain properties are
 * leaves, structured properties (complex or navigation) may carry a nested
 * {@code ($select=…)} option list — the 4.01 Intermediate MUST — and collection-valued
 * properties the nested collection options {@code $filter} (the 4.01 Advanced MUST
 * §13.2.3/5.1), {@code $orderby}, {@code $top}, {@code $skip}, {@code $count} and
 * {@code $search} (the §13.2.3/5.2–5.4 SHOULDs). A navigation-collection expression parses
 * against the target type; a primitive-collection expression addresses the item as {@code $it}
 * (e.g. {@code tags($filter=$it eq 'sale')}). Unrecognized nested options are rejected.
 *
 * <p>Validation happens during parsing against the context {@link EClass}, recursively
 * against the target type of each structured property — unknown names fail the parse
 * (400 at the protocol layer).
 */
public final class SelectTree {

	private static final SelectTree LEAF = new SelectTree(Map.of(), CollectionOptions.NONE);
	/** Nested option name incl. 4.01 variants: {@code $select=} / {@code select=} / case-insensitive. */
	private static final Pattern NESTED_SELECT = Pattern.compile("(?i)^\\$?select=");
	private static final Pattern NESTED_KNOWN = Pattern.compile(
			"(?i)^\\$?(filter|orderby|top|skip|count|expand|search|compute|levels)=");

	private final Map<String, SelectTree> children;
	/** Nested collection options over this selected collection's items. */
	private final CollectionOptions options;

	private SelectTree(Map<String, SelectTree> children, CollectionOptions options) {
		this.children = children;
		this.options = options;
	}

	/** Parses and validates a {@code $select} value; expression-valued nested options → 501. */
	public static SelectTree parse(String select, EClass entityType) {
		return parse(select, entityType, null);
	}

	/**
	 * Parses and validates a {@code $select} value. {@code optionParser} turns nested
	 * {@code $filter}/{@code $orderby}/{@code $search} expressions into IR for the item context
	 * (the target type of a navigation collection, the OWNING type for a primitive collection
	 * whose items are addressed as {@code $it}); {@code null} rejects them as unimplemented.
	 */
	public static SelectTree parse(String select, EClass entityType, NestedOptionParser optionParser) {
		return new SelectTree(items(select, entityType, optionParser, 0), CollectionOptions.NONE);
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

	/** The nested collection options of this selected collection ({@code NONE} without any). */
	public CollectionOptions options() {
		return options;
	}

	private static Map<String, SelectTree> items(String value, EClass type,
			NestedOptionParser optionParser, int depth) {
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
			items.put(name, nested(trimmed.substring(paren + 1, trimmed.length() - 1),
					feature, optionParser, depth));
		}
		return items;
	}

	/**
	 * The parenthesized option list ({@code ;}-separated): {@code $select} on structured
	 * properties, the collection options on collection-valued ones.
	 */
	private static SelectTree nested(String optionList, EStructuralFeature feature,
			NestedOptionParser optionParser, int depth) {
		Map<String, SelectTree> nested = null;
		CollectionOptions.Accumulator options = new CollectionOptions.Accumulator();
		boolean any = false;
		// navigation collections evaluate against the target type; primitive collections have
		// no type of their own — their items are addressed as $it
		EClass context = feature.getEType() instanceof EClass target ? target
				: feature.getEContainingClass();
		for (String option : splitTopLevel(optionList, ';')) {
			String trimmed = option.trim();
			var select = NESTED_SELECT.matcher(trimmed);
			if (select.find()) {
				if (!(feature.getEType() instanceof EClass target)) {
					throw new ODataQueryParseException("nested $select requires a structured property, '"
							+ feature.getName() + "' is primitive");
				}
				if (nested != null) {
					throw new ODataQueryParseException("duplicate nested $select");
				}
				nested = items(trimmed.substring(select.end()), target, optionParser, depth + 1);
				any = true;
				continue;
			}
			if (!(feature.getEType() instanceof EClass)
					&& trimmed.matches("(?i)^\\$?search=.*")) {
				// $search matches string PROPERTIES of the item type — primitive items have none
				throw new ODataQueryParseException(
						"$search inside $select applies to entity collections");
			}
			if (optionParser != null && options.accept(trimmed, feature, context, optionParser)) {
				any = true;
				continue;
			}
			throw NESTED_KNOWN.matcher(trimmed).find()
					? new UnsupportedOperationException(
							"this nested $select option is not implemented")
					: new ODataQueryParseException("unknown nested $select option: " + trimmed);
		}
		if (!any) {
			throw new ODataQueryParseException("empty nested $select option list");
		}
		CollectionOptions built = options.build();
		if (built.count() && depth > 0) {
			// the inline count is spliced next to the property in the response envelope —
			// only expressible for top-level selections (honest 501 below)
			throw new UnsupportedOperationException(
					"$count on nested selections below the top level is not implemented");
		}
		return new SelectTree(nested == null ? Map.of() : nested, built);
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
	static List<String> splitTopLevel(String value, char separator) {
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
