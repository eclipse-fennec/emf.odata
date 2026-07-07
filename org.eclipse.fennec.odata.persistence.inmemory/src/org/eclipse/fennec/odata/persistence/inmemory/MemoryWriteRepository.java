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
package org.eclipse.fennec.odata.persistence.inmemory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.odata.persistence.api.EntityRepository;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Writable in-memory store (the reference backend for the write path): implements BOTH sides
 * of the persistence SPI — {@link EntityRepository} feeds the read pipeline through the
 * in-memory {@code QueryService}, {@link WriteService} mutates the same store. Entities live
 * in insertion-ordered per-EClass maps keyed by their EMF id attribute.
 *
 * <p>PATCH applies exactly the payload's set features; PUT resets all other features to
 * their defaults (key excluded); updates with unknown keys create (OData upsert). Everything
 * synchronizes on the per-class map — reference semantics, not a transactional store.
 */
@Component(service = { EntityRepository.class, WriteService.class })
public class MemoryWriteRepository implements EntityRepository, WriteService {

	private final List<EPackage> packages = new CopyOnWriteArrayList<>();
	private final Map<EClass, Map<String, EObject>> store = new ConcurrentHashMap<>();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addEPackage(EPackage ePackage) {
		packages.add(ePackage);
	}

	void removeEPackage(EPackage ePackage) {
		packages.remove(ePackage);
	}

	// --- read side ---

	@Override
	public boolean supplies(EClass entityType) {
		return supports(entityType);
	}

	@Override
	public List<EObject> entities(EClass entityType) {
		Map<String, EObject> entities = store.get(entityType);
		if (entities == null) {
			return List.of();
		}
		synchronized (entities) {
			return new ArrayList<>(entities.values());
		}
	}

	// --- write side ---

	@Override
	public boolean supports(EClass entityType) {
		return entityType != null && !entityType.isAbstract()
				&& packages.contains(entityType.getEPackage()) && keyAttribute(entityType) != null;
	}

	@Override
	public EObject create(EClass entityType, EObject entity) {
		String key = keyOf(entityType, entity);
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			if (entities.containsKey(key)) {
				throw new WriteConflictException(
						"an entity with this key already exists in " + entityType.getName());
			}
			entities.put(key, entity);
		}
		return entity;
	}

	@Override
	public WriteResult update(EClass entityType, String rawKey, EObject payload, boolean replace) {
		String key = unquote(rawKey);
		EAttribute id = keyAttribute(entityType);
		payload.eSet(id, EcoreUtil.createFromString(id.getEAttributeType(), key)); // URL key wins
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			EObject existing = entities.get(key);
			if (existing == null) { // OData upsert (13.1.1/29)
				entities.put(key, payload);
				return new WriteResult(payload, true);
			}
			apply(entityType, payload, existing, replace, id);
			return new WriteResult(existing, false);
		}
	}

	@Override
	public boolean delete(EClass entityType, String rawKey) {
		Map<String, EObject> entities = store.get(entityType);
		if (entities == null) {
			return false;
		}
		synchronized (entities) {
			return entities.remove(unquote(rawKey)) != null;
		}
	}

	/** PATCH: only the payload's set features; PUT additionally resets everything else. */
	private static void apply(EClass entityType, EObject payload, EObject target, boolean replace,
			EAttribute id) {
		for (EStructuralFeature feature : entityType.getEAllStructuralFeatures()) {
			if (feature == id) {
				continue; // the key is immutable
			}
			if (payload.eIsSet(feature)) {
				target.eSet(feature, payload.eGet(feature));
			} else if (replace && target.eIsSet(feature)) {
				target.eUnset(feature);
			}
		}
	}

	private Map<String, EObject> classStore(EClass entityType) {
		return store.computeIfAbsent(entityType, type -> new LinkedHashMap<>());
	}

	private String keyOf(EClass entityType, EObject entity) {
		EAttribute id = keyAttribute(entityType);
		Object value = entity.eGet(id);
		if (value == null || !entity.eIsSet(id)) {
			throw new IllegalArgumentException(
					"the key property '" + id.getName() + "' is required");
		}
		return String.valueOf(value);
	}

	private static EAttribute keyAttribute(EClass entityType) {
		return entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
	}

	private static String unquote(String rawKey) {
		if (rawKey != null && rawKey.length() >= 2 && rawKey.startsWith("'") && rawKey.endsWith("'")) {
			return rawKey.substring(1, rawKey.length() - 1).replace("''", "'");
		}
		return rawKey;
	}
}
