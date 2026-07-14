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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.odata.query.OclEvaluator;
import org.eclipse.fennec.odata.query.OrderBySegment;

/**
 * Applies {@code $select}/{@code $expand} to serialization copies of entities. Copying is
 * mandatory twice over: serialization must not rip entities out of their containing resource,
 * and expanded targets are co-copied with the SAME copier so every reference in the payload is
 * internal — no server-side resource URIs can leak into any output format. Non-expanded
 * non-containment references are omitted (the OData default); {@code $select} keeps the chosen
 * features, the expanded navigations and always the key — nested {@code $select} trees
 * ([OData-URL] 5.1.3, 4.01) prune the selected structured values recursively.
 */
public class EntityShaper {

	/** Evaluates nested collection options on shaped copies (never on backend objects). */
	private final OclEvaluator evaluator = new OclEvaluator();

	/**
	 * @param select        validated {@code $select} tree, or null when absent
	 * @param expand        validated {@code $expand} navigation names (may be empty)
	 * @param expandedRoots when non-null, receives the copies of expanded targets — callers
	 *                      serializing to a single self-contained document (XMI) add them as
	 *                      extra roots
	 */
	public EObject shape(EObject entity, EClass entityType, SelectTree select, Set<String> expand,
			List<EObject> expandedRoots) {
		return shape(entity, entityType, select, expand, expandedRoots, null);
	}

	/**
	 * {@link #shape} additionally evaluating the nested {@code $select} collection options
	 * ([OData-URL] 5.1.3, 4.01 Advanced §13.2.3/5.1–5.4) — filter/search, ordering and paging
	 * run BEFORE pruning, so their expressions may reference properties the projection drops.
	 * {@code selectCounts}, when non-null, receives the requested inline counts of TOP-LEVEL
	 * selected collections (property name → filtered, pre-paging count).
	 */
	public EObject shape(EObject entity, EClass entityType, SelectTree select, Set<String> expand,
			List<EObject> expandedRoots, Map<String, Long> selectCounts) {
		EcoreUtil.Copier copier = new EcoreUtil.Copier();
		EObject copy = copier.copy(entity);
		for (String name : expand) {
			if (entityType.getEStructuralFeature(name) instanceof EReference reference
					&& !reference.isContainment()) {
				Object value = entity.eGet(reference);
				if (value instanceof List<?> targets) {
					targets.forEach(target -> copier.copy((EObject) target));
				} else if (value instanceof EObject target) {
					copier.copy(target);
				}
			}
		}
		copier.copyReferences();

		for (EReference reference : entityType.getEAllReferences()) {
			if (!reference.isContainment() && !expand.contains(reference.getName())) {
				copy.eUnset(reference);
			}
		}
		if (select != null) {
			applySelectOptions(copy, entityType, select, selectCounts);
			prune(copy, entityType, select, expand);
		}
		if (expandedRoots != null) {
			collectExpandedTargets(copy, entityType, expand, expandedRoots);
		}
		return copy;
	}

	/**
	 * Nested {@code $select} collection options, recursively along the selection tree:
	 * filter/search prune items (navigation targets match against their type, primitive items
	 * as {@code $it}), then ordering and paging apply; requested counts of top-level
	 * collections land in {@code counts}.
	 */
	private void applySelectOptions(EObject copy, EClass type, SelectTree select,
			Map<String, Long> counts) {
		for (String name : select.names()) {
			SelectTree child = select.child(name);
			EStructuralFeature feature = type.getEStructuralFeature(name);
			if (feature == null) {
				continue;
			}
			Object value = copy.eGet(feature);
			if (value instanceof List<?> items && !child.options().isNone()) {
				long total = applyOptions(items, child.options());
				if (child.options().count() && counts != null) {
					counts.put(name, total);
				}
			}
			if (child.isLeaf() || !(feature.getEType() instanceof EClass childType)) {
				continue;
			}
			if (value instanceof List<?> items) {
				for (Object item : items) {
					if (item instanceof EObject nested) {
						applySelectOptions(nested, childType, child, null);
					}
				}
			} else if (value instanceof EObject nested) {
				applySelectOptions(nested, childType, child, null);
			}
		}
	}

	/**
	 * Applies {@link CollectionOptions} to a shaped, MUTABLE item list in the OData option
	 * order — filter, count, order, skip/top — and returns the filtered, pre-paging total.
	 */
	long applyOptions(List<?> items, CollectionOptions options) {
		@SuppressWarnings("unchecked")
		List<Object> mutable = (List<Object>) items;
		if (options.filter() != null) {
			mutable.removeIf(item -> !evaluator.matchesNullSafe(options.filter(), item));
		}
		long total = mutable.size();
		if (!options.orderBy().isEmpty()) {
			mutable.sort(comparator(options.orderBy()));
		}
		if (options.skip() > 0 || options.top() >= 0) {
			int from = Math.min(options.skip(), mutable.size());
			int to = options.top() < 0 ? mutable.size()
					: Math.min(from + options.top(), mutable.size());
			List<Object> page = new ArrayList<>(mutable.subList(from, to));
			mutable.clear();
			mutable.addAll(page);
		}
		return total;
	}

	/** Multi-key comparator over the evaluated order-by expressions (null-safe, like SQL). */
	private Comparator<Object> comparator(List<OrderBySegment> segments) {
		Comparator<Object> comparator = null;
		for (OrderBySegment segment : segments) {
			Comparator<Object> byKey = (a, b) -> compareValues(
					evaluator.evaluate(segment.expression(), a),
					evaluator.evaluate(segment.expression(), b));
			if (!segment.ascending()) {
				byKey = byKey.reversed();
			}
			comparator = comparator == null ? byKey : comparator.thenComparing(byKey);
		}
		return comparator;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static int compareValues(Object left, Object right) {
		if (left == null || right == null) {
			return left == right ? 0 : left == null ? -1 : 1; // nulls first, both directions
		}
		if (left instanceof Comparable comparable && right.getClass() == left.getClass()) {
			return comparable.compareTo(right);
		}
		return String.valueOf(left).compareTo(String.valueOf(right));
	}

	/** Keeps selected/expanded/key features; nested trees prune the structured values. */
	private void prune(EObject copy, EClass type, SelectTree select, Set<String> expand) {
		for (EStructuralFeature feature : type.getEAllStructuralFeatures()) {
			SelectTree child = select.child(feature.getName());
			boolean keep = child != null || expand.contains(feature.getName())
					|| feature instanceof EAttribute attribute && attribute.isID();
			if (!keep) {
				if (copy.eIsSet(feature)) {
					copy.eUnset(feature);
				}
				continue;
			}
			if (child == null || child.isLeaf() || !(feature.getEType() instanceof EClass childType)) {
				continue; // whole value selected (or kept as expand/key)
			}
			Object value = copy.eGet(feature);
			if (value instanceof List<?> members) {
				members.forEach(member -> {
					if (member instanceof EObject object) {
						prune(object, childType, child, Set.of());
					}
				});
			} else if (value instanceof EObject object) {
				prune(object, childType, child, Set.of());
			}
		}
	}

	/** All shaped copies plus the expanded targets as extra roots (self-contained document). */
	public List<EObject> shapeAll(List<EObject> entities, EClass entityType, SelectTree select,
			Set<String> expand) {
		List<EObject> roots = new ArrayList<>();
		List<EObject> expandedRoots = new ArrayList<>();
		for (EObject entity : entities) {
			roots.add(shape(entity, entityType, select, expand, expandedRoots, null));
		}
		roots.addAll(expandedRoots);
		return roots;
	}

	private void collectExpandedTargets(EObject copy, EClass entityType, Set<String> expand,
			List<EObject> expandedRoots) {
		for (String name : expand) {
			if (entityType.getEStructuralFeature(name) instanceof EReference reference
					&& !reference.isContainment()) {
				Object value = copy.eGet(reference);
				if (value instanceof List<?> targets) {
					targets.forEach(target -> expandedRoots.add((EObject) target));
				} else if (value instanceof EObject target) {
					expandedRoots.add(target);
				}
			}
		}
	}
}
