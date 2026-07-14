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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.odata.persistence.api.ChangeJournal;
import org.eclipse.fennec.odata.persistence.api.DeltaService;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.EntityRepository;
import org.eclipse.fennec.odata.persistence.api.MediaService;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.odata.query.OclEvaluator;
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
@Component(service = { EntityRepository.class, WriteService.class, MediaService.class,
		DeltaService.class })
public class MemoryWriteRepository
		implements EntityRepository, WriteService, MediaService, DeltaService {

	private final List<EPackage> packages = new CopyOnWriteArrayList<>();
	private final Map<EClass, Map<String, EObject>> store = new ConcurrentHashMap<>();
	/** Media streams of HasStream entities, keyed like the entity store (reference semantics). */
	private final Map<EClass, Map<String, MediaStream>> media = new ConcurrentHashMap<>();
	/**
	 * Per-thread snapshot of the whole store, taken at {@link #begin()} and restored on
	 * {@link #rollback()}. The reference backend gives $batch change sets ATOMICITY (all-or-nothing)
	 * this way, not full isolation from concurrent writers — a real transactional backend (JPA) would.
	 */
	private final ThreadLocal<Map<EClass, Map<String, EObject>>> snapshot = new ThreadLocal<>();

	// --- change journal (DeltaService, [OData-Protocol] 11.3) ---

	/** Retention window: tokens older than this many changes answer 410 Gone. */
	private static final int MAX_JOURNAL_ENTRIES = 10_000;

	private final ChangeJournal journal = new ChangeJournal(MAX_JOURNAL_ENTRIES);

	private final OclEvaluator evaluator = new OclEvaluator();

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

	// --- transactions (thread-bound; atomic change sets for $batch) ---

	@Override
	public boolean transactional() {
		return true;
	}

	@Override
	public void begin() {
		EcoreUtil.Copier copier = new EcoreUtil.Copier();
		List<EObject> all = new ArrayList<>();
		store.forEach((type, entities) -> {
			synchronized (entities) {
				all.addAll(entities.values());
			}
		});
		copier.copyAll(all); // one copier over the whole store keeps cross-entity references consistent
		copier.copyReferences();
		Map<EClass, Map<String, EObject>> snap = new LinkedHashMap<>();
		store.forEach((type, entities) -> {
			synchronized (entities) {
				Map<String, EObject> copy = new LinkedHashMap<>();
				entities.forEach((key, value) -> copy.put(key, copier.get(value)));
				snap.put(type, copy);
			}
		});
		snapshot.set(snap);
		journal.begin();
	}

	@Override
	public void commit() {
		journal.commit();
		snapshot.remove();
	}

	@Override
	public void rollback() {
		journal.rollback(); // a rolled-back transaction never happened, journal-wise
		Map<EClass, Map<String, EObject>> snap = snapshot.get();
		if (snap == null) {
			return;
		}
		store.forEach((type, entities) -> { // classes that gained a store during the tx are emptied
			if (!snap.containsKey(type)) {
				synchronized (entities) {
					entities.clear();
				}
			}
		});
		snap.forEach((type, copy) -> {
			Map<String, EObject> entities = classStore(type);
			synchronized (entities) {
				entities.clear();
				entities.putAll(copy);
			}
		});
		snapshot.remove();
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
			journal(entity, entityType, key, false);
		}
		return entity;
	}

	@Override
	public WriteResult update(EClass entityType, String rawKey, EObject payload, boolean replace) {
		String key = unquote(rawKey);
		EAttribute id = requiredKeyAttribute(entityType);
		payload.eSet(id, EcoreUtil.createFromString(id.getEAttributeType(), key)); // URL key wins
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			EObject existing = entities.get(key);
			if (existing == null) { // OData upsert (13.1.1/29)
				entities.put(key, payload);
				journal(payload, entityType, key, false);
				return new WriteResult(payload, true);
			}
			apply(entityType, payload, existing, replace, id);
			journal(existing, entityType, key, false);
			return new WriteResult(existing, false);
		}
	}

	@Override
	public boolean delete(EClass entityType, String rawKey) {
		Map<String, EObject> entities = store.get(entityType);
		if (entities == null) {
			return false;
		}
		String key = unquote(rawKey);
		synchronized (entities) {
			EObject removed = entities.remove(key);
			if (removed != null) {
				journal(removed, entityType, key, true);
			}
			return removed != null;
		}
	}

	// Relationship mutations lock the OWNER's per-class map — the same discipline as
	// create/update/delete — so the owner lookup and the shared EMF EList mutation are atomic
	// against concurrent writers/readers. Any lookup in ANOTHER class store (the child in
	// createRelated, the target in link) is done FIRST, under that store's own lock, and released
	// before the owner lock is taken: no method ever holds two class-store locks at once, so the
	// per-class locks cannot deadlock.

	@Override
	public EObject createRelated(EClass entityType, String rawKey, String navigation, EObject child) {
		EReference reference = requiredReference(entityType, navigation);
		if (!reference.isContainment()) {
			create(child.eClass(), child); // related entities live in their own set too (own lock)
		}
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			EObject owner = requiredEntity(entities, rawKey);
			attach(owner, reference, child);
			journal(owner, entityType, unquote(rawKey), false); // expand membership changed
		}
		return child;
	}

	@Override
	public void link(EClass entityType, String rawKey, String navigation, String targetRawKey) {
		EReference reference = requiredReference(entityType, navigation);
		EObject target = findByKey(reference.getEReferenceType(), unquote(targetRawKey)); // own lock
		if (target == null) {
			throw new IllegalArgumentException("the reference target does not exist");
		}
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			EObject owner = requiredEntity(entities, rawKey);
			attach(owner, reference, target);
			journal(owner, entityType, unquote(rawKey), false); // expand membership changed
		}
	}

	@Override
	public boolean unlink(EClass entityType, String rawKey, String navigation, String targetRawKey) {
		EReference reference = requiredReference(entityType, navigation);
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			EObject owner = requiredEntity(entities, rawKey);
			boolean removed;
			if (reference.isMany()) {
				String key = unquote(targetRawKey);
				@SuppressWarnings("unchecked")
				List<EObject> members = (List<EObject>) owner.eGet(reference);
				removed = members.removeIf(member -> key != null && key.equals(keyString(member)));
			} else if (owner.eGet(reference) == null) {
				removed = false;
			} else {
				owner.eSet(reference, null);
				removed = true;
			}
			if (removed) {
				journal(owner, entityType, unquote(rawKey), false); // expand membership changed
			}
			return removed;
		}
	}

	/** Attaches under the owner's lock (held by the caller). */
	@SuppressWarnings("unchecked")
	private static void attach(EObject owner, EReference reference, EObject target) {
		if (reference.isMany()) {
			((List<EObject>) owner.eGet(reference)).add(target); // unique refs dedupe in EMF
		} else {
			owner.eSet(reference, target);
		}
	}

	/** Owner lookup; the caller must hold the lock on {@code entities}. */
	private static EObject requiredEntity(Map<String, EObject> entities, String rawKey) {
		EObject entity = entities.get(unquote(rawKey));
		if (entity == null) {
			throw new IllegalArgumentException("entity not found");
		}
		return entity;
	}

	private static EReference requiredReference(EClass entityType, String navigation) {
		if (!(entityType.getEStructuralFeature(navigation) instanceof EReference reference)) {
			throw new IllegalArgumentException(
					"'" + navigation + "' is not a navigation of " + entityType.getName());
		}
		return reference;
	}

	/** Target lookup honoring inheritance: derived instances live under their own class. */
	private EObject findByKey(EClass targetType, String key) {
		for (Map.Entry<EClass, Map<String, EObject>> entry : store.entrySet()) {
			if (targetType.isSuperTypeOf(entry.getKey()) || targetType == entry.getKey()) {
				synchronized (entry.getValue()) {
					EObject found = entry.getValue().get(key);
					if (found != null) {
						return found;
					}
				}
			}
		}
		return null;
	}

	private static String keyString(EObject entity) {
		EAttribute id = keyAttribute(entity.eClass());
		return id == null ? null : String.valueOf(entity.eGet(id));
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

	@Override
	public WriteResult update(EClass entityType, Map<String, String> namedKeys,
			EObject payload, boolean replace) {
		String key = keyOf(entityType, namedKeys);
		EAttribute id = requiredKeyAttribute(entityType);
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			EObject existing = entities.get(key);
			if (existing == null) { // upsert: URL key components win over payload values
				namedKeys.forEach((name, raw) -> {
					if (entityType.getEStructuralFeature(name) instanceof EAttribute attribute) {
						payload.eSet(attribute, EcoreUtil.createFromString(
								attribute.getEAttributeType(), unquote(raw)));
					}
				});
				entities.put(key, payload);
				journal(payload, entityType, key, false);
				return new WriteResult(payload, true);
			}
			apply(entityType, payload, existing, replace, id);
			journal(existing, entityType, key, false);
			return new WriteResult(existing, false);
		}
	}

	@Override
	public boolean delete(EClass entityType, Map<String, String> namedKeys) {
		String key = keyOf(entityType, namedKeys);
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			EObject removed = entities.remove(key);
			if (removed != null) {
				journal(removed, entityType, key, true);
			}
			return removed != null;
		}
	}

	// --- delta side (change tracking, [OData-Protocol] 11.3) ---

	@Override
	public String trackingToken(EClass entityType) {
		return journal.token();
	}

	@Override
	public boolean supportsExpandTracking() {
		return true;
	}

	@Override
	public DeltaResult changesSince(EntityQuery query, String token) {
		ChangeJournal.Window window = journal.since(token, query.entityType());
		List<EObject> changed = new ArrayList<>();
		List<Removal> removals = new ArrayList<>();
		Set<EObject> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		for (ChangeJournal.Change change : window.changes()) {
			EObject current = null;
			Map<String, EObject> entities = store.get(change.type());
			if (entities != null) {
				synchronized (entities) {
					current = entities.get(change.storeKey());
				}
			}
			if (current == null) {
				removals.add(new Removal(change.keyValues(), REASON_DELETED));
			} else if (!seen.add(current)) {
				continue; // already reported (e.g. as an expand owner)
			} else if (query.castType() != null && !query.castType().isInstance(current)) {
				removals.add(new Removal(change.keyValues(), REASON_CHANGED));
			} else if (query.filter() == null || evaluator.matchesNullSafe(query.filter(), current)) {
				changed.add(current);
			} else { // still exists, but left the tracked membership ([OData-Protocol] 11.3.1)
				removals.add(new Removal(change.keyValues(), REASON_CHANGED));
			}
		}
		if (!query.expand().isEmpty()) {
			// expanded tracking (11.3.1): a change to a MEMBER of an expanded navigation reports
			// the owner — the protocol layer serializes it with the full current representation.
			// The window is journal-wide here: member changes live under the MEMBER's type.
			ChangeJournal.Window all = journal.since(token, null); // member changes live under the MEMBER's type
			for (EObject owner : ownersOfChangedMembers(query, all.changes())) {
				if (!seen.add(owner)) {
					continue;
				}
				if ((query.castType() == null || query.castType().isInstance(owner))
						&& (query.filter() == null || evaluator.matchesNullSafe(query.filter(), owner))) {
					changed.add(owner);
				}
			}
		}
		return new DeltaResult(changed, removals, window.nextToken());
	}

	/** Tracked-set owners whose EXPANDED navigation contains one of the changed entities. */
	private List<EObject> ownersOfChangedMembers(EntityQuery query,
			List<ChangeJournal.Change> changes) {
		List<EObject> owners = new ArrayList<>();
		for (Map.Entry<EClass, Map<String, EObject>> entry : store.entrySet()) {
			if (!query.entityType().isSuperTypeOf(entry.getKey())) {
				continue;
			}
			List<EObject> candidates;
			synchronized (entry.getValue()) {
				candidates = new ArrayList<>(entry.getValue().values());
			}
			for (EObject owner : candidates) {
				if (expandedMemberChanged(owner, query.expand(), changes)) {
					owners.add(owner);
				}
			}
		}
		return owners;
	}

	/** Whether any (first-segment) expanded navigation of the owner holds a changed entity. */
	private boolean expandedMemberChanged(EObject owner, Set<String> expand,
			List<ChangeJournal.Change> changes) {
		for (String path : expand) {
			int slash = path.indexOf('/');
			String navigation = slash < 0 ? path : path.substring(0, slash);
			if (!(owner.eClass().getEStructuralFeature(navigation) instanceof EReference reference)
					|| reference.isContainment()) {
				continue; // containment children have no set-level journal entries
			}
			Object value = owner.eGet(reference);
			List<?> members = value instanceof List<?> list ? list
					: value instanceof EObject single ? List.of(single) : List.of();
			for (Object member : members) {
				if (!(member instanceof EObject target)) {
					continue;
				}
				String targetKey = keyString(target);
				for (ChangeJournal.Change change : changes) {
					if (change.type() == target.eClass() && change.storeKey().equals(targetKey)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Records one mutation; called under the owner's class-store lock. */
	private void journal(EObject entity, EClass storeType, String storeKey, boolean deleted) {
		journal.record(storeType, storeKey, ChangeJournal.keyValuesOf(entity), deleted);
	}

	// --- media side (HasStream entities, [OData-Protocol] 11.2.4/11.4.7) ---

	@Override
	public Optional<MediaStream> readMedia(EClass entityType, String rawKey) {
		String key = unquote(rawKey);
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			if (!entities.containsKey(key)) {
				return Optional.empty(); // no entity → no stream (404)
			}
			return Optional.ofNullable(
					media.getOrDefault(entityType, Map.of()).get(key));
		}
	}

	@Override
	public boolean writeMedia(EClass entityType, String rawKey, MediaStream stream) {
		String key = unquote(rawKey);
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			if (!entities.containsKey(key)) {
				return false; // the media value belongs to an EXISTING entity (404)
			}
			media.computeIfAbsent(entityType, type -> new ConcurrentHashMap<>()).put(key, stream);
			return true;
		}
	}

	private Map<String, EObject> classStore(EClass entityType) {
		return store.computeIfAbsent(entityType, type -> new LinkedHashMap<>());
	}

	private String keyOf(EClass entityType, EObject entity) {
		requiredKeyAttribute(entityType); // clear client error for keyless types
		StringBuilder joined = new StringBuilder();
		for (EAttribute id : entityType.getEAllAttributes()) {
			if (!id.isID()) {
				continue;
			}
			Object value = entity.eGet(id);
			if (value == null || !entity.eIsSet(id)) {
				throw new IllegalArgumentException(
						"the key property '" + id.getName() + "' is required");
			}
			if (joined.length() > 0) {
				joined.append('\u0000'); // composite store key: all id values, attribute order
			}
			joined.append(value);
		}
		return joined.toString();
	}

	/** The store key for a compound key predicate — id-attribute order, values unquoted. */
	private String keyOf(EClass entityType, Map<String, String> namedKeys) {
		StringBuilder joined = new StringBuilder();
		for (EAttribute id : entityType.getEAllAttributes()) {
			if (!id.isID()) {
				continue;
			}
			String raw = namedKeys.get(id.getName());
			if (raw == null) {
				throw new IllegalArgumentException(
						"the key predicate must name '" + id.getName() + "'");
			}
			if (joined.length() > 0) {
				joined.append('\u0000');
			}
			joined.append(unquote(raw));
		}
		return joined.toString();
	}

	private static EAttribute keyAttribute(EClass entityType) {
		return entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
	}

	/**
	 * Like {@link #keyAttribute} but fails with a clear client error instead of returning null
	 * (and, downstream, NPE-ing on a keyless type) — mirrors the JPA sibling. {@link #supports}
	 * still uses the nullable form to advertise the capability.
	 */
	private static EAttribute requiredKeyAttribute(EClass entityType) {
		EAttribute id = keyAttribute(entityType);
		if (id == null) {
			throw new IllegalArgumentException(
					"entity type '" + entityType.getName() + "' has no key attribute");
		}
		return id;
	}

	private static String unquote(String rawKey) {
		if (rawKey != null && rawKey.length() >= 2 && rawKey.startsWith("'") && rawKey.endsWith("'")) {
			return rawKey.substring(1, rawKey.length() - 1).replace("''", "'");
		}
		return rawKey;
	}
}
