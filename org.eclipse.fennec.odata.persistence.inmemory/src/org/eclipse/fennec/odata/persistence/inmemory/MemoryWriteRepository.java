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
import org.eclipse.fennec.odata.persistence.api.EntityKeys;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.EntityRepository;
import org.eclipse.fennec.odata.persistence.api.MediaService;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.odata.ocl.evaluator.OclEvaluator;
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
	 * Per-thread transaction scope for {@code $batch} atomicity: on the FIRST mutation of an
	 * entity/media value within the transaction its prior state is captured (a deep copy, or the
	 * absent marker), and {@link #rollback()} restores ONLY the captured — i.e. TOUCHED — keys.
	 * This gives change sets all-or-nothing atomicity WITHOUT (a) a costly whole-store copy at
	 * {@link #begin()} and WITHOUT (b) clobbering entities a CONCURRENT writer committed in the
	 * meantime (the previous whole-store restore destroyed foreign commits). It is atomicity, not
	 * isolation: two batches writing the SAME entity still race — a real transactional backend
	 * (JPA) provides isolation.
	 */
	private final ThreadLocal<TxScope> tx = new ThreadLocal<>();

	/** Identity of a stored value across the entity and media maps. */
	private record StoreKey(EClass type, String key) {
	}

	/**
	 * Captured prior state of the keys a transaction touched. A key present in the map with a
	 * {@code null} value was ABSENT before the transaction (→ remove on rollback); a non-null value
	 * is the prior copy (→ restore on rollback). {@code containsKey} distinguishes captured from
	 * not-yet-touched (first-touch-only capture).
	 */
	private static final class TxScope {
		private final Map<StoreKey, EObject> priorEntities = new LinkedHashMap<>();
		private final Map<StoreKey, MediaStream> priorMedia = new LinkedHashMap<>();
	}

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
		// polymorphic like the JPA backend and FileEntityRepository: a base-set query MUST see
		// derived instances too ([OData-URL] 4.11) — the store keys entities by their EXACT
		// eClass (write-side identity), so a base read unions every subtype's class store
		// defensive deep copy: the query pipeline iterates these OUTSIDE any store lock, so a
		// concurrent writer structurally mutating a multi-valued feature would throw a
		// ConcurrentModificationException — at the reader AND during the copy itself. Every write
		// to an entity happens under its class-store lock, so the copy of each class runs under the
		// SAME lock. Cross-class references resolve to the live originals (benign scalar race).
		// (Reference-backend cost: O(n) per query.)
		List<EObject> copies = new ArrayList<>();
		store.forEach((type, entities) -> {
			if (entityType.isSuperTypeOf(type)) {
				synchronized (entities) {
					EcoreUtil.Copier copier = new EcoreUtil.Copier();
					copier.copyAll(entities.values());
					copier.copyReferences();
					entities.values().forEach(entity -> copies.add((EObject) copier.get(entity)));
				}
			}
		});
		return copies;
	}

	/** Deep-copies each live entity under its class-store lock (CME-safe); cross-refs stay live. */
	private List<EObject> copyUnderLock(List<EObject> live) {
		List<EObject> copies = new ArrayList<>(live.size());
		for (EObject entity : live) {
			Map<String, EObject> classStore = store.get(entity.eClass());
			synchronized (classStore != null ? classStore : entity) {
				copies.add(EcoreUtil.copy(entity));
			}
		}
		return copies;
	}

	// --- transactions (thread-bound; atomic change sets for $batch) ---

	@Override
	public boolean transactional() {
		return true;
	}

	@Override
	public void begin() {
		// no whole-store copy: prior state is captured lazily on first touch (see TxScope)
		tx.set(new TxScope());
		journal.begin();
	}

	@Override
	public void commit() {
		journal.commit();
		tx.remove();
	}

	@Override
	public void rollback() {
		journal.rollback(); // a rolled-back transaction never happened, journal-wise
		TxScope scope = tx.get();
		if (scope == null) {
			return;
		}
		// restore ONLY the keys this transaction touched — entries a concurrent writer committed
		// meanwhile are left intact
		scope.priorEntities.forEach((sk, prior) -> {
			Map<String, EObject> entities = classStore(sk.type());
			synchronized (entities) {
				if (prior == null) {
					entities.remove(sk.key()); // was absent → undo the create
				} else {
					entities.put(sk.key(), prior); // restore the prior state
				}
			}
		});
		scope.priorMedia.forEach((sk, prior) -> {
			// media is guarded by the entity class-store lock (see writeMedia)
			synchronized (classStore(sk.type())) {
				Map<String, MediaStream> streams = media.get(sk.type());
				if (prior == null) {
					if (streams != null) {
						streams.remove(sk.key());
					}
				} else {
					media.computeIfAbsent(sk.type(), type -> new ConcurrentHashMap<>())
							.put(sk.key(), prior);
				}
			}
		});
		tx.remove();
	}

	/**
	 * Captures the prior state of an entity key on FIRST touch within a transaction (a deep copy,
	 * or the absent marker) so {@link #rollback()} can restore exactly it. No-op outside a
	 * transaction. MUST be called under the class-store lock, before the mutation.
	 */
	private void captureEntity(EClass type, String key) {
		TxScope scope = tx.get();
		if (scope == null) {
			return;
		}
		StoreKey sk = new StoreKey(type, key);
		if (scope.priorEntities.containsKey(sk)) {
			return; // first-touch only
		}
		Map<String, EObject> entities = store.get(type);
		EObject current = entities == null ? null : entities.get(key);
		scope.priorEntities.put(sk, current == null ? null : EcoreUtil.copy(current));
	}

	/** Media counterpart of {@link #captureEntity}; called under the class-store lock. */
	private void captureMedia(EClass type, String key) {
		TxScope scope = tx.get();
		if (scope == null) {
			return;
		}
		StoreKey sk = new StoreKey(type, key);
		if (scope.priorMedia.containsKey(sk)) {
			return;
		}
		Map<String, MediaStream> streams = media.get(type);
		scope.priorMedia.put(sk, streams == null ? null : streams.get(key));
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
		resolveReferences(entityType, entity); // takes other class-store locks — BEFORE ours
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			if (entities.containsKey(key)) {
				throw new WriteConflictException(
						"an entity with this key already exists in " + entityType.getName());
			}
			captureEntity(entityType, key); // prior = absent → rollback removes it
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
		resolveReferences(entityType, payload); // takes other class-store locks — BEFORE ours
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			captureEntity(entityType, key); // capture prior state (absent → upsert, else the pre-PATCH state)
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
			captureEntity(entityType, key); // prior = the entity → rollback re-adds it
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
		} else {
			resolveReferences(child.eClass(), child); // the child's own bindings (own locks)
		}
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			EObject owner = requiredEntity(entities, rawKey);
			captureEntity(entityType, unquote(rawKey)); // owner is mutated by attach
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
			captureEntity(entityType, unquote(rawKey)); // owner is mutated by attach
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
			captureEntity(entityType, unquote(rawKey)); // owner is mutated below
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

	/**
	 * PATCH: only the payload's set features; PUT additionally resets missing STRUCTURAL
	 * features. An omitted non-containment navigation keeps its binding under both verbs —
	 * PUT replace semantics cover structural properties only ([OData-Protocol] 11.4.3).
	 * Reference members were already resolved to store instances ({@link #resolveReferences}).
	 */
	private static void apply(EClass entityType, EObject payload, EObject target, boolean replace,
			EAttribute id) {
		for (EStructuralFeature feature : entityType.getEAllStructuralFeatures()) {
			if (feature == id) {
				continue; // the key is immutable
			}
			boolean navigation = feature instanceof EReference reference && !reference.isContainment();
			if (payload.eIsSet(feature)) {
				target.eSet(feature, payload.eGet(feature));
			} else if (replace && target.eIsSet(feature) && !navigation) {
				target.eUnset(feature);
			}
		}
	}

	/**
	 * Resolves every non-containment payload member to its STORE instance (by key) and rejects
	 * unknown targets — a payload member is a reference to an EXISTING entity, never a silent
	 * deep insert; storing the detached payload stub would corrupt the store with partial rows.
	 * Containment children ride along as payload objects, but their own bindings resolve too
	 * (recursive). Looks up OTHER class stores (own locks) — call BEFORE taking a class lock.
	 */
	private void resolveReferences(EClass entityType, EObject entity) {
		for (EReference reference : entityType.getEAllReferences()) {
			if (!entity.eIsSet(reference)) {
				continue;
			}
			if (reference.isContainment()) {
				if (reference.isMany()) {
					for (Object member : (List<?>) entity.eGet(reference)) {
						if (member instanceof EObject child) {
							resolveReferences(child.eClass(), child);
						}
					}
				} else if (entity.eGet(reference) instanceof EObject child) {
					resolveReferences(child.eClass(), child);
				}
				continue;
			}
			if (reference.isMany()) {
				@SuppressWarnings("unchecked")
				List<EObject> members = (List<EObject>) entity.eGet(reference);
				members.replaceAll(member -> requiredTarget(reference, member));
			} else if (entity.eGet(reference) instanceof EObject member) {
				entity.eSet(reference, requiredTarget(reference, member));
			}
		}
	}

	/** The STORE entity a payload reference member points at, resolved by its key. */
	private EObject requiredTarget(EReference reference, EObject member) {
		EAttribute id = keyAttribute(member.eClass());
		Object key = id == null ? null : member.eGet(id);
		if (key == null) {
			throw new IllegalArgumentException("the payload value of '" + reference.getName()
					+ "' must carry the key of an existing " + member.eClass().getName());
		}
		EObject target = findByKey(reference.getEReferenceType(), String.valueOf(key));
		if (target == null) {
			throw new IllegalArgumentException("the reference target does not exist");
		}
		return target;
	}

	@Override
	public WriteResult update(EClass entityType, Map<String, String> namedKeys,
			EObject payload, boolean replace) {
		String key = keyOf(entityType, namedKeys);
		EAttribute id = requiredKeyAttribute(entityType);
		resolveReferences(entityType, payload); // takes other class-store locks — BEFORE ours
		Map<String, EObject> entities = classStore(entityType);
		synchronized (entities) {
			captureEntity(entityType, key); // prior state for rollback (absent → upsert, else pre-PATCH)
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
			captureEntity(entityType, key); // prior = the entity → rollback re-adds it
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
		return changesSince(query, token, Long.MAX_VALUE);
	}

	@Override
	public DeltaResult changesSince(EntityQuery query, String token, long maxSpan) {
		ChangeJournal.Window window = journal.since(token, query.entityType(), maxSpan);
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
			// The SAME span keeps the journal-wide window aligned with the typed one.
			ChangeJournal.Window all = journal.since(token, null, maxSpan);
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
		// defensive deep copy of the upserts (same reason as entities()): the delta payload is
		// serialized OUTSIDE any store lock. (Residual: expanded members reachable via a copied
		// owner are not themselves copied — a narrower race than the hot read path.)
		return new DeltaResult(copyUnderLock(changed), removals, window.nextToken(), window.more());
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
			captureMedia(entityType, key); // prior stream (or absent) for rollback
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
		for (EAttribute id : EntityKeys.of(entityType)) {
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
		for (EAttribute id : EntityKeys.of(entityType)) {
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
		return EntityKeys.first(entityType);
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
