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
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.fennec.odata.persistence.api.ExpandPushdown;
import org.eclipse.fennec.odata.ocl.evaluator.OclEvaluator;
import org.eclipse.fennec.odata.query.OrderBySegment;

/**
 * Applies {@code $select}/{@code $expand} to serialization copies of entities. Copying is
 * mandatory twice over: serialization must not rip entities out of their containing resource,
 * and the payload must never point at server-side objects or resource URIs.
 *
 * <p>The copy is a TREE that mirrors the request, not a copy of the object graph: every
 * position of the {@code $expand} tree gets copies of its own, linked to their parent in one
 * direction only. Two positions may show the same backend object with different shapes
 * ({@code Datastreams?$expand=Thing($expand=Datastreams)} shows the root datastream again,
 * without its {@code Thing}), and a bidirectional navigation must not drag its opposite end
 * along — a shared, two-way linked copy would hand the serializer a cycle (#112).
 * Non-expanded non-containment references are absent (the OData default); {@code $select}
 * keeps the chosen features, the expanded navigations and always the key — nested
 * {@code $select} trees ([OData-URL] 5.1.3, 4.01) prune the selected structured values
 * recursively.
 */
public class EntityShaper {

	/**
	 * What one entity position keeps: the {@code $select} tree (null = every property) and the
	 * expanded navigations by name.
	 */
	record Shape(SelectTree select, Map<String, Expansion> expand) {

		static final Shape ALL = new Shape(null, Map.of());

		Shape {
			expand = expand == null ? Map.of() : expand;
		}

		/** Every navigation name anywhere in the tree — what the serializer writes inline. */
		Set<String> navigationNames() {
			Set<String> names = new LinkedHashSet<>();
			collectNames(names);
			return names;
		}

		private void collectNames(Set<String> names) {
			expand.forEach((name, expansion) -> {
				names.add(name);
				expansion.shape().collectNames(names);
			});
		}

		/** The slash paths of the tree ({@code nav}, {@code nav/next}, …), parents first. */
		Set<String> paths() {
			Set<String> paths = new LinkedHashSet<>();
			collectPaths("", paths);
			return paths;
		}

		private void collectPaths(String prefix, Set<String> paths) {
			expand.forEach((name, expansion) -> {
				String path = prefix + name;
				paths.add(path);
				expansion.shape().collectPaths(path + "/", paths);
			});
		}

		/** The plain tree of a set of slash paths (no options, no select). */
		static Shape ofPaths(SelectTree select, Set<String> paths) {
			Map<String, Expansion> expand = new LinkedHashMap<>();
			for (String path : paths) {
				Map<String, Expansion> level = expand;
				for (String segment : path.split("/")) {
					Expansion expansion = level.computeIfAbsent(segment,
							s -> new Expansion(new Shape(null, new LinkedHashMap<>()),
									CollectionOptions.NONE, null));
					level = expansion.shape().expand();
				}
			}
			return new Shape(select, expand);
		}
	}

	/**
	 * One expanded navigation: the shape of its targets, the collection options still to be
	 * applied here ({@code $filter}/{@code $orderby}/{@code $top}/…, Advanced 9.2/9.4–9.7) and
	 * the derived-type cast ([OData-URL] 5.1.3.2), null without one.
	 */
	record Expansion(Shape shape, CollectionOptions options, EClass cast) {

		Expansion {
			shape = shape == null ? Shape.ALL : shape;
			options = options == null ? CollectionOptions.NONE : options;
		}
	}

	/** Evaluates nested collection options on shaped copies (never on backend objects). */
	private final OclEvaluator evaluator = new OclEvaluator();

	/**
	 * Resolves the key attributes of a type — the resolved OData profile, not an {@code isID} scan,
	 * so a composite identity declared on the type survives the pruning (emf.odata#35).
	 */
	private final Function<EClass, List<EAttribute>> keyAttributes;

	EntityShaper(Function<EClass, List<EAttribute>> keyAttributes) {
		this.keyAttributes = keyAttributes;
	}

	/**
	 * @param select        validated {@code $select} tree, or null when absent
	 * @param expand        validated {@code $expand} navigation names or slash paths (may be empty)
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
		return shape(entity, entityType, select, expand, expandedRoots, selectCounts, Map.of());
	}

	/**
	 * {@link #shape} knowing which expanded navigations the BACKEND already narrowed
	 * (ADR-0008). For those the copy takes exactly the resolved entries: the options selected
	 * which proxies got resolved, so {@code eIsProxy()} is the selection and the untouched
	 * remainder must stay untouched — reading it would resolve it, which is both the N+1 we
	 * pushed down to avoid and the loss of the very information we are reading.
	 */
	public EObject shape(EObject entity, EClass entityType, SelectTree select, Set<String> expand,
			List<EObject> expandedRoots, Map<String, Long> selectCounts,
			Map<String, ExpandPushdown> narrowed) {
		Map<EObject, Map<String, Long>> counts = new IdentityHashMap<>();
		EObject copy = shape(entity, Shape.ofPaths(select, expand), narrowed, counts, expandedRoots);
		if (selectCounts != null && counts.containsKey(copy)) {
			selectCounts.putAll(counts.get(copy));
		}
		return copy;
	}

	/**
	 * Shapes one entity along a request tree: copies, expands, applies the collection options of
	 * every expanded navigation (minus what the backend {@code narrowed} at the first level,
	 * ADR-0008), then the {@code $select} projections. Inline counts land in {@code counts},
	 * keyed by the copy that carries them (navigation or selected collection name → filtered,
	 * pre-paging count).
	 */
	EObject shape(EObject entity, Shape shape, Map<String, ExpandPushdown> narrowed,
			Map<EObject, Map<String, Long>> counts, List<EObject> expandedRoots) {
		EObject copy = build(entity, shape, narrowed, expandedRoots);
		finish(copy, shape, narrowed, counts);
		return copy;
	}

	/**
	 * Copies the entity with its containment tree — and nothing else: no non-containment
	 * reference is copied, so nothing outside the request tree is read (no lazy loads, no proxy
	 * resolution) — then attaches the expanded targets as copies of their own.
	 */
	private EObject build(EObject source, Shape shape, Map<String, ExpandPushdown> narrowed,
			List<EObject> expandedRoots) {
		EcoreUtil.Copier copier = new EcoreUtil.Copier(true, false);
		EObject copy = copier.copy(source);
		Map<EObject, EObject> sources = new IdentityHashMap<>();
		copier.forEach((original, copied) -> sources.put(copied, original));
		attach(copy, source, shape, narrowed, sources, expandedRoots);
		return copy;
	}

	private void attach(EObject copy, EObject source, Shape shape, Map<String, ExpandPushdown> narrowed,
			Map<EObject, EObject> sources, List<EObject> expandedRoots) {
		for (Map.Entry<String, Expansion> entry : shape.expand().entrySet()) {
			if (!(copy.eClass().getEStructuralFeature(entry.getKey()) instanceof EReference reference)) {
				continue;
			}
			Shape childShape = entry.getValue().shape();
			if (reference.isContainment()) {
				// contained targets are already in the copy — their own navigations still need
				// the originals they were copied from
				for (EObject child : values(copy.eGet(reference))) {
					EObject original = sources.get(child);
					if (original != null) {
						attach(child, original, childShape, Map.of(), sources, expandedRoots);
					}
				}
				continue;
			}
			boolean selective = !narrowed.getOrDefault(entry.getKey(), ExpandPushdown.NONE).isNone();
			for (EObject target : targets(source, reference, selective)) {
				EObject child = build(target, childShape, Map.of(), expandedRoots);
				link(copy, reference, child);
				if (expandedRoots != null) {
					expandedRoots.add(child);
				}
			}
		}
	}

	/**
	 * The targets of a navigation. A {@code selective} read takes exactly the resolved entries
	 * WITHOUT resolving anything (ADR-0008); the ordinary read resolves, and whatever stays a
	 * proxy after that cannot be shown.
	 */
	private static List<EObject> targets(EObject source, EReference reference, boolean selective) {
		Object value = source.eGet(reference, !selective);
		List<EObject> targets = new ArrayList<>();
		if (value instanceof List<?> list) {
			Iterator<?> items = selective && list instanceof InternalEList<?> internal
					? internal.basicIterator() : list.iterator();
			while (items.hasNext()) {
				if (items.next() instanceof EObject target && !target.eIsProxy()) {
					targets.add(target);
				}
			}
		} else if (value instanceof EObject target && !target.eIsProxy()) {
			targets.add(target);
		}
		return targets;
	}

	/**
	 * Links a target copy in ONE direction: the inverse end of a bidirectional reference is
	 * left alone, so the copy tree never points back at its parent.
	 */
	@SuppressWarnings("unchecked")
	private static void link(EObject copy, EReference reference, EObject child) {
		if (reference.isMany()) {
			((InternalEList<EObject>) copy.eGet(reference)).basicAdd(child, null);
		} else if (reference.getEOpposite() != null) {
			((InternalEObject) copy).eInverseAdd((InternalEObject) child,
					copy.eClass().getFeatureID(reference), null, null);
		} else {
			copy.eSet(reference, child);
		}
	}

	/** Removes one link the way {@link #link} made it — the target keeps its own state. */
	private static void unlink(EObject copy, EReference reference, EObject child) {
		if (!reference.isContainment() && reference.getEOpposite() != null) {
			((InternalEObject) copy).eInverseRemove((InternalEObject) child,
					copy.eClass().getFeatureID(reference), null, null);
		} else {
			copy.eUnset(reference);
		}
	}

	private static List<EObject> values(Object value) {
		if (value instanceof List<?> list) {
			List<EObject> objects = new ArrayList<>();
			for (Object item : list) {
				if (item instanceof EObject object) {
					objects.add(object);
				}
			}
			return objects;
		}
		return value instanceof EObject object ? List.of(object) : List.of();
	}

	/**
	 * Applies, depth first, the cast and collection options of every expansion and then the
	 * {@code $select} of this position.
	 */
	private void finish(EObject copy, Shape shape, Map<String, ExpandPushdown> narrowed,
			Map<EObject, Map<String, Long>> counts) {
		EClass type = copy.eClass();
		for (Map.Entry<String, Expansion> entry : shape.expand().entrySet()) {
			if (!(type.getEStructuralFeature(entry.getKey()) instanceof EReference reference)) {
				continue;
			}
			Expansion expansion = entry.getValue();
			ExpandPushdown pushed = narrowed.getOrDefault(entry.getKey(), ExpandPushdown.NONE);
			CollectionOptions remaining = expansion.options();
			if (pushed.filter()) {
				remaining = remaining.withoutFilter();
			}
			if (pushed.paging()) {
				remaining = remaining.withoutPaging();
			}
			Object value = copy.eGet(reference);
			if (value instanceof List<?> children) {
				if (expansion.cast() != null) { // cast-in-expand: only derived instances stay (5.1.3.2)
					List<Object> kept = new ArrayList<>(children);
					kept.removeIf(child -> !expansion.cast().isInstance(child));
					replaceAll(children, kept);
				}
				if (!remaining.isNone()) {
					long total = applyOptions(children, remaining);
					if (remaining.count()) {
						counts.computeIfAbsent(copy, c -> new LinkedHashMap<>()).put(entry.getKey(), total);
					}
				}
				for (EObject child : values(children)) {
					finish(child, expansion.shape(), Map.of(), counts);
				}
			} else if (value instanceof EObject child) {
				if (expansion.cast() != null && !expansion.cast().isInstance(child)) {
					unlink(copy, reference, child); // single-valued: a non-matching instance is not expanded
				} else {
					finish(child, expansion.shape(), Map.of(), counts);
				}
			}
		}
		if (shape.select() != null) {
			applySelectOptions(copy, type, shape.select(), counts);
			prune(copy, type, shape.select(), shape.expand().keySet());
		}
	}

	/**
	 * Nested {@code $select} collection options, recursively along the selection tree:
	 * filter/search prune items (navigation targets match against their type, primitive items
	 * as {@code $it}), then ordering and paging apply; requested counts land in {@code counts}
	 * under the copy that owns the collection.
	 */
	private void applySelectOptions(EObject copy, EClass type, SelectTree select,
			Map<EObject, Map<String, Long>> counts) {
		for (String name : select.names()) {
			SelectTree child = select.child(name);
			EStructuralFeature feature = type.getEStructuralFeature(name);
			if (feature == null) {
				continue;
			}
			Object value = copy.eGet(feature);
			if (value instanceof List<?> items && !child.options().isNone()) {
				long total = applyOptions(items, child.options());
				if (child.options().count()) {
					counts.computeIfAbsent(copy, c -> new LinkedHashMap<>()).put(name, total);
				}
			}
			if (child.isLeaf() || !(feature.getEType() instanceof EClass childType)) {
				continue;
			}
			for (EObject nested : values(value)) {
				applySelectOptions(nested, childType, child, counts);
			}
		}
	}

	/**
	 * Applies {@link CollectionOptions} to a shaped, MUTABLE item list in the OData option
	 * order — filter, count, order, skip/top — and returns the filtered, pre-paging total.
	 */
	long applyOptions(List<?> items, CollectionOptions options) {
		List<Object> work = new ArrayList<>(items);
		if (options.filter() != null) {
			work.removeIf(item -> !evaluator.matchesNullSafe(options.filter(), item));
		}
		long total = work.size();
		if (!options.orderBy().isEmpty()) {
			work.sort(comparator(options.orderBy()));
		}
		if (options.skip() > 0 || options.top() >= 0) {
			int from = Math.min(options.skip(), work.size());
			int to = options.top() < 0 ? work.size()
					: Math.min(from + options.top(), work.size());
			work = new ArrayList<>(work.subList(from, to));
		}
		replaceAll(items, work);
		return total;
	}

	/**
	 * Replaces a list's content. A navigation list is rewritten with the basic operations: the
	 * ordinary ones would update the inverse ends and link the targets back to their parent.
	 */
	@SuppressWarnings("unchecked")
	private static void replaceAll(List<?> items, List<Object> kept) {
		if (items instanceof EStructuralFeature.Setting setting
				&& setting.getEStructuralFeature() instanceof EReference reference
				&& !reference.isContainment() && items instanceof InternalEList<?> internal) {
			InternalEList<Object> list = (InternalEList<Object>) internal;
			for (Object item : new ArrayList<>(list)) {
				list.basicRemove(item, null);
			}
			for (Object item : kept) {
				list.basicAdd(item, null);
			}
			return;
		}
		List<Object> list = (List<Object>) items;
		list.clear();
		list.addAll(kept);
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

	@SuppressWarnings("unchecked")
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
					|| keyAttributes.apply(type).contains(feature);
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
		return shapeAll(entities, entityType, select, expand, Map.of());
	}

	/** {@link #shapeAll} carrying the backend's expand report (ADR-0008). */
	public List<EObject> shapeAll(List<EObject> entities, EClass entityType, SelectTree select,
			Set<String> expand, Map<String, ExpandPushdown> narrowed) {
		return shapeAll(entities, Shape.ofPaths(select, expand), narrowed);
	}

	/** {@link #shapeAll} along a request tree. */
	List<EObject> shapeAll(List<EObject> entities, Shape shape, Map<String, ExpandPushdown> narrowed) {
		List<EObject> roots = new ArrayList<>();
		List<EObject> expandedRoots = new ArrayList<>();
		for (EObject entity : entities) {
			roots.add(shape(entity, shape, narrowed, new IdentityHashMap<>(), expandedRoots));
		}
		// what the options dropped is no longer part of the tree
		Set<EObject> reachable = Collections.newSetFromMap(new IdentityHashMap<>());
		roots.forEach(root -> collectReachable(root, shape, reachable));
		expandedRoots.removeIf(target -> !reachable.contains(target));
		roots.addAll(expandedRoots);
		return roots;
	}

	private static void collectReachable(EObject copy, Shape shape, Set<EObject> reachable) {
		for (Map.Entry<String, Expansion> entry : shape.expand().entrySet()) {
			if (copy.eClass().getEStructuralFeature(entry.getKey()) instanceof EReference reference) {
				for (EObject child : values(copy.eGet(reference))) {
					reachable.add(child);
					collectReachable(child, entry.getValue().shape(), reachable);
				}
			}
		}
	}
}
