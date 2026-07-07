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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.query.OrderBySegment;
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
@Component(service = QueryService.class)
public class JpaQueryService implements QueryService {

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
			// single-valued $expand targets ride along in the SAME statement (paging-safe)
			for (EReference reference : expandReferences(query)) {
				if (!reference.isMany()) {
					root.fetch(reference.getName(), JoinType.LEFT);
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

	/** The non-containment navigations of {@code $expand} (containments load with the owner). */
	private List<EReference> expandReferences(EntityQuery query) {
		EClass context = query.castType() != null ? query.castType() : query.entityType();
		List<EReference> references = new ArrayList<>();
		for (String name : query.expand()) {
			if (context.getEStructuralFeature(name) instanceof EReference reference
					&& !reference.isContainment()) {
				references.add(reference);
			}
		}
		return references;
	}

	/**
	 * Touches the expanded navigations while the EntityManager is still open, so their values
	 * are REAL materialized objects afterwards — the SPI contract promises the caller plain
	 * readable results, never unresolved proxies or post-close lazy loads.
	 */
	private void materializeExpanded(List<EObject> entities, EntityQuery query) {
		List<EReference> references = expandReferences(query);
		if (references.isEmpty()) {
			return;
		}
		for (EObject entity : entities) {
			for (EReference reference : references) {
				Object value = entity.eGet(reference);
				if (value instanceof List<?> members) {
					members.size(); // triggers the batched load of the whole collection
				}
			}
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
