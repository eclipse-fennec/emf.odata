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
package org.eclipse.fennec.odata.persistence.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.read.ApplyQueries;
import org.eclipse.fennec.odata.persistence.read.ReadPlans;
import org.eclipse.fennec.odata.persistence.read.ReadQueries;
import org.eclipse.fennec.persistence.capabilities.PersistenceCapabilities;
import org.eclipse.fennec.persistence.capabilities.QueryFeature;
import org.eclipse.fennec.persistence.helper.CompositeIds;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.QueryResultRow;
import org.eclipse.fennec.persistence.query.support.QueryValidator;
import org.eclipse.fennec.persistence.repository.api.ReadRepository;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * {@link QueryService} over the emf.persistence-jpa repository facade: every OData read
 * becomes one Fennec {@code Query} executed through {@link ReadRepository#find(Query)} /
 * {@link ReadRepository#count(Query)}, so whatever the repository layers over its backend
 * — bridged inputs, transformations, query-defined data sets — stays in the loop, and the
 * pushdown the facade offers is used (#79). The command backend, by contrast, addresses a
 * persistence unit directly.
 *
 * <p>Capabilities are gated exactly as the command backend gates them: the plan is validated
 * against the repository's declared {@code QueryCapabilities} before it runs, unsupported
 * features answer an honest 501, structural violations a 400, and a refusal the backend
 * raises at execution time is classified the same way. {@code $expand} options are pushed
 * down only where the repository declares {@code EXPAND}/{@code EXPAND_FILTER}/
 * {@code EXPAND_PAGE} and resolved in memory otherwise (ADR-0008).
 *
 * <p>A repository instance owns a {@code ResourceSet} that is not thread-safe and attaches
 * every object it reads; the facade therefore registers with prototype scope. This service
 * takes one instance per request from its {@link ComponentServiceObjects}, detaches the
 * result graph from that instance and gives it back — the entities stay plain readable
 * objects, the instance's resources are released, nothing accumulates across requests.
 *
 * <p>Configuration (factory configurations, one per repository): the repository is chosen
 * with the standard DS target {@code repository.target=(persistence.repository.id=<id>)};
 * {@value #PACKAGES_PROPERTY} optionally restricts the served EPackages by nsURI (without
 * it every keyed EClass is claimed — rarely what a runtime with several backends wants);
 * {@value #PAGE_SIZE_PROPERTY} caps an unbounded page.
 */
@Component(configurationPid = RepositoryQueryService.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = "fennec.odata.backend=repository")
public class RepositoryQueryService implements QueryService {

	public static final String PID = "org.eclipse.fennec.odata.persistence.repository";
	public static final String PACKAGES_PROPERTY = "emf.nsURIs";
	public static final String PAGE_SIZE_PROPERTY = "max.page.size";

	private static final int DEFAULT_MAX_PAGE_SIZE = 1000;

	private ComponentServiceObjects<ReadRepository> repositories;
	private volatile PersistenceCapabilities capabilities;
	private Set<String> nsUris = Set.of();
	private int maxPageSize = DEFAULT_MAX_PAGE_SIZE;

	@Reference(scope = ReferenceScope.PROTOTYPE_REQUIRED)
	void setRepository(ComponentServiceObjects<ReadRepository> repositories) {
		this.repositories = repositories;
	}

	@Activate
	void activate(Map<String, Object> properties) {
		nsUris = stringSet(properties.get(PACKAGES_PROPERTY));
		Object pageSize = properties.get(PAGE_SIZE_PROPERTY);
		if (pageSize != null) {
			maxPageSize = Integer.parseInt(String.valueOf(pageSize).trim());
		}
	}

	private static Set<String> stringSet(Object value) {
		Set<String> result = new LinkedHashSet<>();
		if (value instanceof String single) {
			if (!single.isBlank()) {
				result.add(single.trim());
			}
		} else if (value instanceof String[] array) {
			for (String entry : array) {
				result.addAll(stringSet(entry));
			}
		} else if (value instanceof Collection<?> collection) {
			for (Object entry : collection) {
				result.addAll(stringSet(String.valueOf(entry)));
			}
		}
		return Set.copyOf(result);
	}

	@Override
	public boolean supports(EClass entityType) {
		if (entityType.isAbstract() || entityType.isInterface()) {
			return false;
		}
		if (!nsUris.isEmpty() && (entityType.getEPackage() == null
				|| !nsUris.contains(entityType.getEPackage().getNsURI()))) {
			return false;
		}
		return !CompositeIds.idAttributes(entityType).isEmpty();
	}

	@Override
	public QueryResult execute(EntityQuery query) {
		EClass entityType = query.entityType();
		long total = query.count() ? count(query, entityType) : -1;
		if (query.top() == 0) {
			return new QueryResult(List.of(), total);
		}
		Expression predicate = ReadQueries.predicate(query.filter(), entityType, query.castType());
		return withRepository(entityType, repository -> {
			ReadPlans.Page plan = ReadPlans.validated(
					ReadPlans.page(query, predicate, true, maxPageSize, this::supportsFeature),
					irQuery -> validate(irQuery, entityType, repository));
			List<EObject> entities;
			try (var result = repository.find(plan.query());
					Stream<EObject> objects = result.objects()) {
				entities = new ArrayList<>(objects.toList());
			}
			ReadPlans.materialize(entities, plan.chains(), repository.getResourceSet());
			return new QueryResult(entities, total, plan.pushedExpands());
		});
	}

	/** {@code $count} is total-before-paging: the facade's own count over the predicate. */
	private long count(EntityQuery query, EClass entityType) {
		Query irQuery = ReadPlans.count(query);
		return withRepository(entityType, repository -> {
			ReadPlans.raise(validate(irQuery, entityType, repository));
			return repository.count(irQuery);
		});
	}

	/**
	 * {@code $apply} on the pipeline stages of the query envelope: leading filters fold into
	 * WHERE, groupby/aggregate/compute become stages, the post-pipeline options ride the
	 * envelope. Row shape follows the reference backend. {@code $count} is a second, unpaged
	 * run whose rows are counted while streaming — the engines expose no countOnly over
	 * pipelines.
	 */
	@Override
	public ApplyResult executeApply(ApplyQuery query) {
		EClass entityType = query.entityType();
		long total = -1;
		if (query.count()) {
			ApplyQueries.Plan unpaged = ApplyQueries.plan(new ApplyQuery(entityType,
					query.pipeline(), query.rowFilter(), List.of(), 0, -1, false), 0);
			total = executePlan(unpaged, entityType, rows -> rows.count());
		}
		ApplyQueries.Plan plan = ApplyQueries.plan(query, maxPageSize);
		List<Map<String, Object>> rows = executePlan(plan, entityType, Stream::toList);
		return new ApplyResult(rows, total);
	}

	private <T> T executePlan(ApplyQueries.Plan plan, EClass entityType,
			Function<Stream<Map<String, Object>>, T> terminal) {
		return withRepository(entityType, repository -> {
			ReadPlans.raise(validate(plan.query(), entityType, repository));
			try (var result = repository.find(plan.query())) {
				if (plan.columns().isEmpty()) {
					try (Stream<EObject> objects = result.objects()) {
						return terminal.apply(objects.map(RepositoryQueryService::attributeRow));
					}
				}
				try (Stream<QueryResultRow> resultRows = result.rows()) {
					return terminal.apply(resultRows.map(row -> ApplyQueries.row(row, plan.columns())));
				}
			}
		});
	}

	private static Map<String, Object> attributeRow(EObject entity) {
		Map<String, Object> row = new LinkedHashMap<>();
		for (EAttribute attribute : entity.eClass().getEAllAttributes()) {
			row.put(attribute.getName(), entity.eGet(attribute));
		}
		return row;
	}

	/** One unit of work against a fresh repository instance. */
	private interface Read<T> {
		T apply(ReadRepository repository) throws IOException;
	}

	/**
	 * Runs the read on its own repository instance: taken from the prototype-scoped service
	 * for this request, released afterwards. Whatever the read attached to the instance is
	 * detached first, so the returned objects outlive the instance as plain EObjects
	 * (unresolved navigations stay proxies — the SPI's "not selected" shape). Backend
	 * refusals keep their honesty classes: "not supported" and translation refusals
	 * ({@link QueryException}) → 501, anything else an internal fault.
	 */
	private <T> T withRepository(EClass entityType, Read<T> read) {
		ReadRepository repository = repositories.getService();
		try {
			return read.apply(repository);
		} catch (IOException e) {
			throw refused(entityType, e);
		} finally {
			try {
				detachAll(repository);
			} finally {
				repositories.ungetService(repository); // disposes the instance: empty resources unload
			}
		}
	}

	/** Detaches everything the read attached: the objects keep their state, the instance can go. */
	private static void detachAll(ReadRepository repository) {
		if (repository.isDisposed()) {
			return;
		}
		ResourceSet resourceSet = repository.getResourceSet();
		for (Resource resource : new ArrayList<>(resourceSet.getResources())) {
			resource.getContents().clear();
		}
	}

	/** The repository's validation of a plan, or null when it declares no capabilities. */
	private Diagnostic validate(Query irQuery, EClass entityType, ReadRepository repository) {
		PersistenceCapabilities declared = capabilities(repository);
		return declared == null ? null
				: QueryValidator.validate(irQuery, entityType, declared.query());
	}

	private boolean supportsFeature(QueryFeature feature) {
		PersistenceCapabilities declared = capabilities;
		return declared != null && declared.query().supports(feature);
	}

	/** Capabilities are a property of the backend, not of the instance — probed once. */
	private PersistenceCapabilities capabilities(ReadRepository repository) {
		PersistenceCapabilities declared = capabilities;
		if (declared == null) {
			try {
				declared = repository.capabilities();
			} catch (RuntimeException probeFailed) {
				return null; // a backend that answers no capabilities is not validated up front
			}
			capabilities = declared;
		}
		return declared;
	}

	private static RuntimeException refused(EClass entityType, IOException cause) {
		String message = String.valueOf(cause.getMessage());
		if (message.contains("is not supported") || cause.getCause() instanceof QueryException
				|| message.contains("rejected")) {
			return new UnsupportedOperationException(message, cause);
		}
		return new IllegalStateException("the repository failed the " + entityType.getName()
				+ " query", cause);
	}
}
