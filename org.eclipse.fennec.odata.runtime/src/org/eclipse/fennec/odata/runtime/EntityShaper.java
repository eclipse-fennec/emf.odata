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
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

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

	/**
	 * @param select        validated {@code $select} tree, or null when absent
	 * @param expand        validated {@code $expand} navigation names (may be empty)
	 * @param expandedRoots when non-null, receives the copies of expanded targets — callers
	 *                      serializing to a single self-contained document (XMI) add them as
	 *                      extra roots
	 */
	public EObject shape(EObject entity, EClass entityType, SelectTree select, Set<String> expand,
			List<EObject> expandedRoots) {
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
			prune(copy, entityType, select, expand);
		}
		if (expandedRoots != null) {
			collectExpandedTargets(copy, entityType, expand, expandedRoots);
		}
		return copy;
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
			roots.add(shape(entity, entityType, select, expand, expandedRoots));
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
