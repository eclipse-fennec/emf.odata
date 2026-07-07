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
package org.eclipse.fennec.odata.persistence.jpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.persistence.jpa.JpaHelper;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.config.QueryHints;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.FetchParent;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;

/**
 * JPA {@link QueryService} (E5, req §3.5): full pushdown of the OCL predicate IR into
 * Jakarta Persistence Criteria queries against the {@link EntityManagerFactory} services
 * that Eclipse Fennec Persistence JPA registers (one per persistence unit). Entities are
 * dynamic EMF entities — every result row IS an {@link EObject}; the entity name equals
 * {@link EClass#getName()} and attribute names equal the EMF feature names, so the model
 * addressed by OData and the model persisted by JPA are the same.
 *
 * <p>Filter, order, paging ({@code setFirstResult}/{@code setMaxResults}), {@code $count}
 * (separate COUNT query with the same predicate) and derived-type casts
 * ({@code TYPE(e) IN (subtypes)}) all run in the database; {@code $apply} pipelines become
 * grouped criteria queries ({@link JpaApplyExecutor}: WHERE/GROUP BY/HAVING). Constructs
 * without a pushdown raise {@link UnsupportedOperationException} — never a silently wrong
 * result (the servlet answers 501).
 */
@Component(service = { QueryService.class, WriteService.class })
public class JpaQueryService implements QueryService, WriteService {

	private final List<EntityManagerFactory> factories = new CopyOnWriteArrayList<>();
	private final OclToCriteriaTranslator translator = new OclToCriteriaTranslator();
	private final JpaApplyExecutor applyExecutor = new JpaApplyExecutor();

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addEntityManagerFactory(EntityManagerFactory factory) {
		factories.add(factory);
	}

	void removeEntityManagerFactory(EntityManagerFactory factory) {
		factories.remove(factory);
	}

	@Override
	public boolean supports(EClass entityType) {
		return factoryFor(entityType) != null;
	}

	@Override
	public QueryResult execute(EntityQuery query) {
		EntityManagerFactory factory = factoryFor(query.entityType());
		if (factory == null) {
			throw new IllegalStateException(
					"no persistence unit for entity type " + query.entityType().getName());
		}
		try (EntityManager em = factory.createEntityManager()) {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			EntityType<?> entity = entityType(factory, query.entityType());

			CriteriaQuery<Object> cq = cb.createQuery(Object.class);
			Root<?> root = cq.from(entity);
			cq.select(root);
			// the derived type is the context after a cast — treat() makes its attributes
			// addressable in criteria paths ([OData-URL] 4.11)
			From<?, ?> context = treated(query.castType(), cb, root, factory);
			// single-valued $expand/walk chains ride along in the SAME statement (paging-safe
			// nested LEFT fetch joins, deduplicated by prefix); chains hitting a to-many
			// segment fall back to batch hints below
			List<String> batchChains = new ArrayList<>();
			Map<String, FetchParent<?, ?>> fetched = new HashMap<>();
			for (List<EReference> chain : expandPaths(query)) {
				FetchParent<?, ?> fetch = root;
				StringBuilder prefix = new StringBuilder();
				for (EReference reference : chain) {
					prefix.append(prefix.isEmpty() ? "" : ".").append(reference.getName());
					if (reference.isMany()) {
						batchChains.add(prefix.toString());
						break;
					}
					FetchParent<?, ?> parent = fetch;
					fetch = fetched.computeIfAbsent(prefix.toString(),
							key -> parent.fetch(reference.getName(), JoinType.LEFT));
				}
			}
			Predicate where = wherePredicate(query, cb, cq, root, context, factory);
			if (where != null) {
				cq.where(where);
			}
			if (!query.orderBy().isEmpty()) {
				cq.orderBy(orders(query.orderBy(), cb, cq, context));
			}

			TypedQuery<Object> typedQuery = em.createQuery(cq);
			// EVERY to-many feature (containments, element collections, expanded to-many
			// navigations) loads per row by default — N+1 on each cache miss. Batching turns
			// that into ONE IN-query per feature, and stays correct under setMaxResults
			// (a collection fetch join would break the row counting).
			root.alias("e");
			EClass shape = query.castType() != null ? query.castType() : query.entityType();
			for (EStructuralFeature feature : shape.getEAllStructuralFeatures()) {
				if (feature.isMany()) {
					typedQuery.setHint(QueryHints.BATCH_TYPE, BatchFetchType.IN);
					typedQuery.setHint(QueryHints.BATCH, "e." + feature.getName());
				}
			}
			for (String batchChain : batchChains) {
				typedQuery.setHint(QueryHints.BATCH_TYPE, BatchFetchType.IN);
				typedQuery.setHint(QueryHints.BATCH, "e." + batchChain);
			}
			if (query.skip() > 0) {
				typedQuery.setFirstResult(query.skip());
			}
			if (query.top() >= 0) {
				typedQuery.setMaxResults(query.top());
			}
			List<EObject> entities = new ArrayList<>();
			for (Object row : typedQuery.getResultList()) {
				entities.add((EObject) row);
			}
			materializeExpanded(entities, query);

			long total = query.count() ? count(query, em, cb, factory, entity) : -1;
			return new QueryResult(entities, total);
		}
	}

	/**
	 * The {@code $expand}/walk prefetch requests as reference chains, resolved against the
	 * (cast-aware) context type. Entries may be plain navigation names or slash-separated
	 * navigation paths (the servlet sends the walked prefix of a resource path); segments
	 * that are no navigations end the chain — there is nothing to prefetch beyond them.
	 */
	private List<List<EReference>> expandPaths(EntityQuery query) {
		EClass context = query.castType() != null ? query.castType() : query.entityType();
		List<List<EReference>> paths = new ArrayList<>();
		for (String expand : query.expand()) {
			List<EReference> chain = new ArrayList<>();
			EClass current = context;
			for (String segment : expand.split("/")) {
				if (current == null
						|| !(current.getEStructuralFeature(segment) instanceof EReference reference)) {
					break;
				}
				chain.add(reference);
				current = reference.getEReferenceType();
			}
			if (!chain.isEmpty()) {
				paths.add(chain);
			}
		}
		return paths;
	}

	/**
	 * Touches the expanded navigation chains while the EntityManager is still open, so their
	 * values are REAL materialized objects afterwards — the SPI contract promises the caller
	 * plain readable results, never unresolved proxies or post-close lazy loads.
	 */
	private void materializeExpanded(List<EObject> entities, EntityQuery query) {
		List<List<EReference>> paths = expandPaths(query);
		if (paths.isEmpty()) {
			return;
		}
		for (EObject entity : entities) {
			for (List<EReference> chain : paths) {
				descend(entity, chain, 0);
			}
		}
	}

	private void descend(EObject object, List<EReference> chain, int index) {
		if (index >= chain.size()) {
			return;
		}
		Object value = object.eGet(chain.get(index));
		if (value instanceof List<?> members) {
			for (Object member : members) { // touching loads the batched collection
				if (member instanceof EObject child) {
					descend(child, chain, index + 1);
				}
			}
		} else if (value instanceof EObject child) {
			descend(child, chain, index + 1);
		}
	}

	/** The total BEFORE paging: a separate COUNT query sharing the same predicate. */
	private long count(EntityQuery query, EntityManager em, CriteriaBuilder cb,
			EntityManagerFactory factory, EntityType<?> entity) {
		CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
		Root<?> countRoot = countQuery.from(entity);
		countQuery.select(cb.count(countRoot));
		From<?, ?> context = treated(query.castType(), cb, countRoot, factory);
		Predicate where = wherePredicate(query, cb, countQuery, countRoot, context, factory);
		if (where != null) {
			countQuery.where(where);
		}
		return em.createQuery(countQuery).getSingleResult();
	}

	/** The cast type's treated root (or the plain root without a cast). */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private From<?, ?> treated(EClass castType, CriteriaBuilder cb, Root<?> root,
			EntityManagerFactory factory) {
		if (castType == null) {
			return root;
		}
		return cb.treat((Root) root, (Class) entityType(factory, castType).getJavaType());
	}

	/** Filter + derived-type cast, translated against the given query's context root. */
	private Predicate wherePredicate(EntityQuery query, CriteriaBuilder cb,
			CriteriaQuery<?> cq, Root<?> root, From<?, ?> context, EntityManagerFactory factory) {
		List<Predicate> predicates = new ArrayList<>();
		if (query.castType() != null) {
			predicates.add(castRestriction(query.castType(), cb, root, factory));
		}
		if (query.filter() != null) {
			predicates.add(translator.predicate(query.filter(), cb, cq, context));
		}
		if (predicates.isEmpty()) {
			return null;
		}
		return predicates.size() == 1 ? predicates.get(0)
				: cb.and(predicates.toArray(Predicate[]::new));
	}

	/**
	 * URL type cast ([OData-URL] 4.11) → {@code TYPE(e) IN (cast + its subtypes)}. Dynamic
	 * entity classes mirror the EClass hierarchy, so assignability identifies the subtypes.
	 */
	private Predicate castRestriction(EClass castType, CriteriaBuilder cb, Root<?> root,
			EntityManagerFactory factory) {
		EntityType<?> cast = entityType(factory, castType);
		List<Class<?>> matching = factory.getMetamodel().getEntities().stream()
				.map(EntityType::getJavaType)
				.filter(type -> cast.getJavaType().isAssignableFrom(type))
				.<Class<?>>map(type -> type)
				.toList();
		return root.type().in(matching);
	}

	private List<Order> orders(List<OrderBySegment> orderBy, CriteriaBuilder cb,
			CriteriaQuery<?> cq, From<?, ?> root) {
		List<Order> orders = new ArrayList<>();
		for (OrderBySegment segment : orderBy) {
			var key = translator.expression(segment.expression(), cb, cq, root);
			orders.add(segment.ascending() ? cb.asc(key) : cb.desc(key));
		}
		return orders;
	}

	@Override
	public ApplyResult executeApply(ApplyQuery query) {
		EntityManagerFactory factory = factoryFor(query.entityType());
		if (factory == null) {
			throw new IllegalStateException(
					"no persistence unit for entity type " + query.entityType().getName());
		}
		try (EntityManager em = factory.createEntityManager()) {
			return applyExecutor.execute(query, em, entityType(factory, query.entityType()));
		}
	}

	// --- write side (OASIS "Updatable Service" v1: attributes + containment children) ---

	@Override
	public EObject create(EClass entityType, EObject payload) {
		EntityManagerFactory factory = requireFactory(entityType);
		Object key = typedKey(entityType, payload);
		try (EntityManager em = factory.createEntityManager()) {
			Class<?> javaType = entityType(factory, entityType).getJavaType();
			em.getTransaction().begin();
			if (em.find(javaType, key) != null) {
				em.getTransaction().rollback();
				throw new WriteConflictException(
						"an entity with this key already exists in " + entityType.getName());
			}
			EObject entity = newInstance(factory, entityType);
			copyFeatures(entityType, payload, entity, factory, true);
			entity.eSet(keyAttribute(entityType), key); // copyFeatures leaves the key alone
			em.persist(entity);
			em.getTransaction().commit();
			return entity;
		}
	}

	@Override
	public WriteResult update(EClass entityType, String rawKey, EObject payload, boolean replace) {
		EntityManagerFactory factory = requireFactory(entityType);
		EAttribute id = keyAttribute(entityType);
		Object key = EcoreUtil.createFromString(id.getEAttributeType(), unquote(rawKey));
		try (EntityManager em = factory.createEntityManager()) {
			Class<?> javaType = entityType(factory, entityType).getJavaType();
			em.getTransaction().begin();
			EObject existing = (EObject) em.find(javaType, key);
			if (existing == null) { // OData upsert (13.1.1/29) — the URL key wins
				EObject entity = newInstance(factory, entityType);
				copyFeatures(entityType, payload, entity, factory, true);
				entity.eSet(id, key);
				em.persist(entity);
				em.getTransaction().commit();
				return new WriteResult(entity, true);
			}
			copyFeatures(entityType, payload, existing, factory, replace);
			existing.eSet(id, key); // the key is immutable
			em.getTransaction().commit();
			return new WriteResult(existing, false);
		}
	}

	@Override
	public boolean delete(EClass entityType, String rawKey) {
		EntityManagerFactory factory = requireFactory(entityType);
		EAttribute id = keyAttribute(entityType);
		Object key = EcoreUtil.createFromString(id.getEAttributeType(), unquote(rawKey));
		try (EntityManager em = factory.createEntityManager()) {
			Class<?> javaType = entityType(factory, entityType).getJavaType();
			em.getTransaction().begin();
			Object entity = em.find(javaType, key);
			if (entity == null) {
				em.getTransaction().rollback();
				return false;
			}
			em.remove(entity);
			em.getTransaction().commit();
			return true;
		}
	}

	/**
	 * Applies the payload onto the target entity: attributes directly, containment children
	 * as freshly built store instances (recursive); non-containment navigations are IGNORED
	 * in v1 ({@code @odata.bind} is a follow-up). PATCH copies only set features, PUT
	 * ({@code replace}) additionally resets everything missing to the defaults.
	 */
	private void copyFeatures(EClass entityType, EObject payload, EObject target,
			EntityManagerFactory factory, boolean replace) {
		EAttribute id = keyAttribute(entityType);
		for (EStructuralFeature feature : entityType.getEAllStructuralFeatures()) {
			if (feature == id) {
				continue;
			}
			if (feature instanceof EReference reference && !reference.isContainment()) {
				continue; // non-containment bindings are a follow-up
			}
			if (!payload.eIsSet(feature)) {
				if (replace && target.eIsSet(feature)) {
					target.eUnset(feature);
				}
				continue;
			}
			if (feature instanceof EReference reference) { // containment children
				Object value = payload.eGet(reference);
				if (reference.isMany()) {
					List<EObject> children = new ArrayList<>();
					for (Object member : (List<?>) value) {
						children.add(rebuild((EObject) member, factory));
					}
					target.eSet(reference, children);
				} else {
					target.eSet(reference, value == null ? null : rebuild((EObject) value, factory));
				}
			} else {
				target.eSet(feature, payload.eGet(feature));
			}
		}
	}

	/** A payload child as a store instance of ITS OWN class (recursive, incl. derived types). */
	private EObject rebuild(EObject payload, EntityManagerFactory factory) {
		EObject instance = newInstance(factory, payload.eClass());
		copyFeatures(payload.eClass(), payload, instance, factory, true);
		EAttribute id = payload.eClass().getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst().orElse(null);
		if (id != null && payload.eIsSet(id)) {
			instance.eSet(id, payload.eGet(id));
		}
		return instance;
	}

	/** A fresh dynamic store instance — payload EObjects are plain EMF, not entity classes. */
	private EObject newInstance(EntityManagerFactory factory, EClass entityType) {
		return (EObject) JpaHelper.getServerSession(factory)
				.getDescriptorForAlias(entityType.getName())
				.getInstantiationPolicy().buildNewInstance();
	}

	private Object typedKey(EClass entityType, EObject payload) {
		EAttribute id = keyAttribute(entityType);
		if (!payload.eIsSet(id) || payload.eGet(id) == null) {
			throw new IllegalArgumentException(
					"the key property '" + id.getName() + "' is required");
		}
		return payload.eGet(id);
	}

	private static EAttribute keyAttribute(EClass entityType) {
		return entityType.getEAllAttributes().stream()
				.filter(EAttribute::isID).findFirst()
				.orElseThrow(() -> new IllegalArgumentException(
						"entity type " + entityType.getName() + " has no key"));
	}

	private static String unquote(String rawKey) {
		if (rawKey != null && rawKey.length() >= 2 && rawKey.startsWith("'") && rawKey.endsWith("'")) {
			return rawKey.substring(1, rawKey.length() - 1).replace("''", "'");
		}
		return rawKey;
	}

	private EntityManagerFactory requireFactory(EClass entityType) {
		EntityManagerFactory factory = factoryFor(entityType);
		if (factory == null) {
			throw new IllegalStateException(
					"no persistence unit for entity type " + entityType.getName());
		}
		return factory;
	}

	// --- entity resolution: EClass name = entity name (Fennec Persistence JPA contract) ---

	private EntityManagerFactory factoryFor(EClass entityType) {
		for (EntityManagerFactory factory : factories) {
			if (findEntityType(factory, entityType) != null) {
				return factory;
			}
		}
		return null;
	}

	private static EntityType<?> entityType(EntityManagerFactory factory, EClass eClass) {
		EntityType<?> entity = findEntityType(factory, eClass);
		if (entity == null) {
			throw new IllegalStateException("no JPA entity for EClass " + eClass.getName());
		}
		return entity;
	}

	private static EntityType<?> findEntityType(EntityManagerFactory factory, EClass eClass) {
		try {
			for (EntityType<?> entity : factory.getMetamodel().getEntities()) {
				if (eClass.getName().equals(entity.getName())) {
					return entity;
				}
			}
		} catch (IllegalStateException e) {
			return null; // factory not (yet) initialized — not a match
		}
		return null;
	}
}
