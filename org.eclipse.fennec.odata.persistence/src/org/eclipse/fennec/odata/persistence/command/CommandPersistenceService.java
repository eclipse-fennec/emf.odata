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
package org.eclipse.fennec.odata.persistence.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.model.command.Command;
import org.eclipse.fennec.model.command.CommandFactory;
import org.eclipse.fennec.model.command.DeleteCommand;
import org.eclipse.fennec.model.command.InsertCommand;
import org.eclipse.fennec.model.command.UpdateCommand;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.builder.Expressions;
import org.eclipse.fennec.model.query.builder.QueryBuilder;
import org.eclipse.fennec.model.stream.ChangeEntry;
import org.eclipse.fennec.model.stream.ChangeSet;
import org.eclipse.fennec.model.stream.DeltaKind;
import org.eclipse.fennec.model.stream.StreamFactory;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.ChangeJournal;
import org.eclipse.fennec.odata.persistence.api.DeltaService;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.persistence.helper.CompositeIds;
import org.eclipse.fennec.persistence.query.QueryConstants;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.CommandFeature;
import org.eclipse.fennec.persistence.query.api.CommandResource;
import org.eclipse.fennec.persistence.query.api.QueryFeature;
import org.eclipse.fennec.persistence.query.api.QueryProcessor;
import org.eclipse.fennec.persistence.query.api.QueryResultRow;
import org.eclipse.fennec.persistence.query.api.QueryableResource;
import org.eclipse.fennec.persistence.query.support.CommandTransaction;
import org.eclipse.fennec.persistence.query.support.QueryValidator;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Backend-neutral {@link WriteService} over the Fennec Persistence command SPI.
 *
 * <p>Every operation creates one resource from the configured backend URI
 * ({@code jpa://<unit>} or {@code mongodb://<db>}) plus the entity name and executes
 * one command against it: POST becomes an {@code InsertCommand}, DELETE a
 * {@code DeleteCommand} whose selector is the key predicate as Expression IR, and
 * PATCH/PUT an {@code UpdateCommand} carrying a {@code ChangeSet} template
 * (SET/UNSET for single-valued attributes, a deterministic REMOVE/ADD sequence for
 * many-valued ones; non-containment references as id-valued entries, persistence-jpa#107).
 * Relationship operations map onto reference-entry {@code UpdateCommand}s, composite
 * keys ride the {@code CompositeIds} fragment contract (#109), and {@code $batch}
 * atomicity groups run in the backend's cross-command transaction bracket where the
 * deployment supports one (#108, probed once).
 *
 * <p>Change tracking ({@link DeltaService}, [OData-Protocol] 11.3) rides a service-layer
 * {@link ChangeJournal}: every write that went through THIS service is journaled once its
 * command succeeded, and {@code changesSince} re-queries the touched keys through the read
 * path ({@code key IN (touched)} folded into the defining predicate — membership stays
 * pushed down). Writes applied behind the service's back are invisible to the journal.
 *
 * <p>Configuration (factory configurations supported): {@value #URI_PROPERTY} is the
 * backend base URI (required); {@value #PACKAGES_PROPERTY} optionally restricts the
 * served EPackages by nsURI — without it every keyed EClass is claimed, which is
 * almost never what a runtime with more than one backend wants.
 */
@Component(configurationPid = CommandPersistenceService.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = "fennec.odata.backend=command")
public class CommandPersistenceService implements QueryService, WriteService, DeltaService {

	public static final String PID = "org.eclipse.fennec.odata.persistence.command";
	public static final String URI_PROPERTY = "backend.uri";
	public static final String PACKAGES_PROPERTY = "emf.nsURIs";
	public static final String PAGE_SIZE_PROPERTY = "max.page.size";

	private static final int DEFAULT_MAX_PAGE_SIZE = 1000;

	/**
	 * Change journal for the {@link DeltaService}: one entry per succeeded command. Entries
	 * publish immediately outside a batch bracket; inside one they buffer and publish on
	 * commit (the journal's transaction hooks mirror the backend bracket, #108).
	 */
	private final ChangeJournal journal = new ChangeJournal(10_000);
	private final Map<String, QueryProcessor> queryProcessors = new ConcurrentHashMap<>();
	private ResourceSetFactory resourceSetFactory;
	private URI baseUri;
	private Set<String> nsUris = Set.of();
	private int maxPageSize = DEFAULT_MAX_PAGE_SIZE;

	@Reference
	void setResourceSetFactory(ResourceSetFactory resourceSetFactory) {
		this.resourceSetFactory = resourceSetFactory;
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
	void addQueryProcessor(QueryProcessor processor, Map<String, Object> properties) {
		Object backend = properties.get(QueryConstants.BACKEND_PROPERTY);
		if (backend != null) {
			queryProcessors.put(String.valueOf(backend), processor);
		}
	}

	void removeQueryProcessor(QueryProcessor processor, Map<String, Object> properties) {
		Object backend = properties.get(QueryConstants.BACKEND_PROPERTY);
		if (backend != null) {
			queryProcessors.remove(String.valueOf(backend), processor);
		}
	}

	@Activate
	void activate(Map<String, Object> properties) {
		Object uri = properties.get(URI_PROPERTY);
		if (uri == null || String.valueOf(uri).isBlank()) {
			throw new IllegalArgumentException("the configuration must set '" + URI_PROPERTY + "'");
		}
		baseUri = URI.createURI(String.valueOf(uri));
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
		long total = query.count() ? executeCount(query, entityType) : -1;
		List<EObject> entities = query.top() == 0 ? List.of() : executePage(query, entityType);
		return new QueryResult(entities, total);
	}

	/** {@code $count} is total-before-paging: a separate countOnly query, no order, no page. */
	private long executeCount(EntityQuery query, EClass entityType) {
		QueryBuilder builder = QueryBuilder.from(entityType).countOnly();
		Expression predicate = ReadQueries.predicate(query.filter(), entityType, query.castType());
		if (predicate != null) {
			builder.where(predicate);
		}
		Query irQuery = builder.build();
		validate(irQuery, entityType);
		Resource resource = resource(resourceSetFactory.createResourceSet(), entityType);
		try (var result = queryable(resource).query(irQuery)) {
			return result.count();
		} catch (IOException e) {
			throw readRefused(entityType, e);
		}
	}

	private List<EObject> executePage(EntityQuery query, EClass entityType) {
		Expression predicate = ReadQueries.predicate(query.filter(), entityType, query.castType());
		return fetchMatching(query, entityType, predicate, true);
	}

	/**
	 * Runs one backend query for the given predicate; {@code paged} applies the query's
	 * ordering and paging (a delta re-query must stay complete — its bound is the journal
	 * window, not a page cap).
	 */
	private List<EObject> fetchMatching(EntityQuery query, EClass entityType, Expression predicate,
			boolean paged) {
		QueryBuilder builder = QueryBuilder.from(entityType);
		if (predicate != null) {
			builder.where(predicate);
		}
		if (paged) {
			ReadQueries.applyOrderBy(builder, query.orderBy(), entityType, query.castType());
			if (query.skip() > 0) {
				builder.skip(query.skip());
			}
			if (query.top() > 0) {
				builder.top(query.top());
			} else if (maxPageSize > 0) {
				// server-driven paging safety net for unbounded reads (top == -1)
				builder.top(maxPageSize);
			}
		}
		EClass context = query.castType() != null ? query.castType() : entityType;
		List<List<EReference>> chains = new ArrayList<>();
		boolean pushExpand = supportsFeature(QueryFeature.EXPAND);
		for (String path : query.expand()) {
			List<EReference> chain = ReadQueries.referenceChain(context, path);
			if (chain.isEmpty()) {
				continue;
			}
			chains.add(chain);
			if (pushExpand) {
				// full multi-segment fetch hint (persistence-jpa#95: nested JOIN FETCH /
				// batch hints); the proxy walk below stays as the backend-neutral safety net
				builder.expand(chain.toArray(EReference[]::new));
			}
		}
		Query irQuery = builder.build();
		validate(irQuery, entityType);
		ResourceSet resourceSet = resourceSetFactory.createResourceSet();
		Resource resource = resource(resourceSet, entityType);
		try (var result = queryable(resource).query(irQuery)) {
			List<EObject> entities;
			try (Stream<EObject> objects = result.objects()) {
				entities = new ArrayList<>(objects.toList());
			}
			// resolve inside the try: the backend session dies with close()
			materialize(entities, chains, resourceSet);
			return entities;
		} catch (IOException e) {
			throw readRefused(entityType, e);
		}
	}

	/**
	 * {@code $apply} on the pipeline stages of the query envelope (#12): leading filters
	 * fold into WHERE, groupby/aggregate/compute become stages, the post-pipeline options
	 * (row filter with alias references, row sort, paging) ride the envelope. Row shape
	 * follows the reference backend: grouping paths nest, aliases stay flat. {@code $count}
	 * is a second, unpaged run whose rows are counted while streaming — the engines expose
	 * no countOnly over pipelines.
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
		validate(plan.query(), entityType);
		Resource resource = resource(resourceSetFactory.createResourceSet(), entityType);
		try (var result = queryable(resource).query(plan.query())) {
			if (plan.columns().isEmpty()) {
				// the pipeline never left the entity shape — flatten attributes (v1 row
				// contract of the reference backend: attribute values, no references)
				try (Stream<EObject> objects = result.objects()) {
					return terminal.apply(objects.map(CommandPersistenceService::attributeRow));
				}
			}
			try (Stream<QueryResultRow> resultRows = result.rows()) {
				return terminal.apply(resultRows.map(row -> ApplyQueries.row(row, plan.columns())));
			}
		} catch (IOException e) {
			throw readRefused(entityType, e);
		}
	}

	private static Map<String, Object> attributeRow(EObject entity) {
		Map<String, Object> row = new LinkedHashMap<>();
		for (EAttribute attribute : entity.eClass().getEAllAttributes()) {
			row.put(attribute.getName(), entity.eGet(attribute));
		}
		return row;
	}

	/**
	 * The SPI promises plain readable results — walk every {@code $expand} chain and
	 * swap proxies for their resolved targets (keyed find through the resource
	 * factory's {@code getEObject} contract, deduplicated per proxy URI).
	 */
	private void materialize(List<EObject> entities, List<List<EReference>> chains,
			ResourceSet resourceSet) {
		if (chains.isEmpty() || entities.isEmpty()) {
			return;
		}
		Map<String, EObject> resolved = new HashMap<>();
		for (List<EReference> chain : chains) {
			for (EObject entity : entities) {
				descend(entity, chain, 0, resourceSet, resolved);
			}
		}
	}

	private void descend(EObject object, List<EReference> chain, int index, ResourceSet resourceSet,
			Map<String, EObject> resolved) {
		if (object == null || index >= chain.size()) {
			return;
		}
		EReference reference = chain.get(index);
		if (!reference.getEContainingClass().isInstance(object)) {
			return; // polymorphic page: this row does not carry the navigation
		}
		if (reference.isMany()) {
			@SuppressWarnings("unchecked")
			List<EObject> members = (List<EObject>) object.eGet(reference);
			for (ListIterator<EObject> iterator = members.listIterator(); iterator.hasNext();) {
				EObject member = iterator.next();
				EObject target = resolve(member, resourceSet, resolved);
				if (target != member) {
					iterator.set(target);
				}
				descend(target, chain, index + 1, resourceSet, resolved);
			}
		} else if (object.eGet(reference, false) instanceof EObject member) {
			EObject target = resolve(member, resourceSet, resolved);
			if (target != member) {
				object.eSet(reference, target);
			}
			descend(target, chain, index + 1, resourceSet, resolved);
		}
	}

	private EObject resolve(EObject candidate, ResourceSet resourceSet, Map<String, EObject> resolved) {
		if (!candidate.eIsProxy()) {
			return candidate;
		}
		String key = ((InternalEObject) candidate).eProxyURI().toString();
		EObject target = resolved.computeIfAbsent(key,
				proxyUri -> EcoreUtil.resolve(candidate, resourceSet));
		if (target.eIsProxy()) {
			throw new IllegalStateException("the backend returned an unresolvable reference");
		}
		return target;
	}

	/**
	 * Pre-validation against the backend's declared capabilities turns refusals into
	 * structured errors: unsupported features → 501, structural violations → 400.
	 * Without a bound {@link QueryProcessor} the resource-level IOException fallback
	 * in {@link #readRefused} applies.
	 */
	private void validate(Query irQuery, EClass entityType) {
		QueryProcessor processor = processor();
		if (processor == null) {
			return;
		}
		Diagnostic diagnostic = processor.validate(irQuery, entityType);
		if (diagnostic.getSeverity() < Diagnostic.ERROR) {
			return;
		}
		List<String> unsupported = new ArrayList<>();
		List<String> invalid = new ArrayList<>();
		for (Diagnostic child : diagnostic.getChildren()) {
			if (child.getSeverity() < Diagnostic.ERROR) {
				continue;
			}
			if (child.getCode() == QueryValidator.CODE_UNSUPPORTED_FEATURE) {
				unsupported.add(child.getMessage());
			} else {
				invalid.add(child.getMessage());
			}
		}
		if (!unsupported.isEmpty()) {
			throw new UnsupportedOperationException(String.join("; ", unsupported));
		}
		throw new IllegalArgumentException(String.join("; ", invalid));
	}

	private boolean supportsFeature(QueryFeature feature) {
		QueryProcessor processor = processor();
		return processor != null && processor.capabilities().supports(feature);
	}

	private QueryProcessor processor() {
		String scheme = baseUri.scheme();
		String backend = "mongodb".equals(scheme) ? "mongo" : scheme;
		return queryProcessors.get(backend);
	}

	private static QueryableResource queryable(Resource resource) {
		if (resource instanceof QueryableResource queryableResource) {
			return queryableResource;
		}
		throw new IllegalStateException("the backend resource does not support queries");
	}

	private static RuntimeException readRefused(EClass entityType, IOException cause) {
		String message = String.valueOf(cause.getMessage());
		if (message.contains("is not supported by this backend")) {
			return new UnsupportedOperationException(message, cause);
		}
		return new IllegalStateException(
				"the persistence backend failed the " + entityType.getName() + " query", cause);
	}

	@Override
	public EObject create(EClass entityType, EObject entity) {
		String fragment = CompositeIds.fragment(entity);
		if (fragment == null) {
			throw new IllegalArgumentException(
					"the payload must carry the key of the new " + entityType.getName());
		}
		if (fetchOne(entityType, fragment) != null) {
			throw new WriteConflictException(
					"a " + entityType.getName() + " with this key already exists");
		}
		insert(entityType, entity);
		EObject stored = fetchOne(entityType, fragment);
		EObject created = stored != null ? stored : entity;
		journal.record(entityType, fragment, ChangeJournal.keyValuesOf(created), false);
		return created;
	}

	@Override
	public WriteResult update(EClass entityType, String rawKey, EObject payload, boolean replace) {
		return updateByFragment(entityType, fragmentFromRaw(entityType, rawKey), payload, replace);
	}

	@Override
	public WriteResult update(EClass entityType, Map<String, String> namedKeys, EObject payload,
			boolean replace) {
		return updateByFragment(entityType, fragmentFromNamed(entityType, namedKeys), payload, replace);
	}

	private WriteResult updateByFragment(EClass entityType, String fragment, EObject payload,
			boolean replace) {
		EObject current = fetchOne(entityType, fragment);
		if (current == null) {
			// upsert: the URL key wins over anything in the payload
			CompositeIds.setId(payload, fragment);
			insert(entityType, payload);
			EObject stored = fetchOne(entityType, fragment);
			EObject upserted = stored != null ? stored : payload;
			journal.record(entityType, fragment, ChangeJournal.keyValuesOf(upserted), false);
			return new WriteResult(upserted, true);
		}
		ChangeSet template = template(entityType, payload, current, replace);
		if (!template.getEntries().isEmpty()) {
			UpdateCommand command = CommandFactory.eINSTANCE.createUpdateCommand();
			command.setSelector(keySelector(entityType, fragment));
			command.setTemplate(template);
			if (execute(entityType, command) > 0) {
				journal.record(entityType, fragment, ChangeJournal.keyValuesOf(current), false);
			}
		}
		EObject stored = fetchOne(entityType, fragment);
		return new WriteResult(stored != null ? stored : current, false);
	}

	@Override
	public boolean delete(EClass entityType, String rawKey) {
		return deleteByFragment(entityType, fragmentFromRaw(entityType, rawKey));
	}

	@Override
	public boolean delete(EClass entityType, Map<String, String> namedKeys) {
		return deleteByFragment(entityType, fragmentFromNamed(entityType, namedKeys));
	}

	private boolean deleteByFragment(EClass entityType, String fragment) {
		EObject current = fetchOne(entityType, fragment);
		DeleteCommand command = CommandFactory.eINSTANCE.createDeleteCommand();
		command.setSelector(keySelector(entityType, fragment));
		boolean deleted = execute(entityType, command) > 0;
		if (deleted) {
			Map<String, Object> keyValues = current != null ? ChangeJournal.keyValuesOf(current)
					: Map.of();
			journal.record(entityType, fragment, keyValues, true);
		}
		return deleted;
	}

	private void insert(EClass entityType, EObject entity) {
		InsertCommand command = CommandFactory.eINSTANCE.createInsertCommand();
		command.getObjects().add(entity);
		execute(entityType, command);
	}

	// --- delta side (change tracking, [OData-Protocol] 11.3) ---

	@Override
	public String trackingToken(EClass entityType) {
		return journal.token();
	}

	/**
	 * Expanded tracking needs the owner lookup pushed down as {@code IN}/{@code EXISTS}
	 * ({@link #ownersOfChangedMembers}). Without a bound {@link QueryProcessor} the
	 * capability is unknown and the query itself refuses at runtime — same leniency as
	 * {@link #validate}.
	 */
	@Override
	public boolean supportsExpandTracking() {
		QueryProcessor processor = processor();
		return processor == null || (processor.capabilities().supports(QueryFeature.IN)
				&& processor.capabilities().supports(QueryFeature.EXISTS));
	}

	@Override
	public DeltaResult changesSince(EntityQuery query, String token) {
		return changesSince(query, token, Long.MAX_VALUE);
	}

	/**
	 * Changes since the token, out of the service-layer journal. Membership stays PUSHED
	 * DOWN: the defining predicate is combined with a {@code key IN (touched keys)}
	 * restriction and runs as ONE backend query — touched keys the query does not return
	 * were deleted or left the membership.
	 */
	@Override
	public DeltaResult changesSince(EntityQuery query, String token, long maxSpan) {
		EClass entityType = query.entityType();
		ChangeJournal.Window window = journal.since(token, entityType, maxSpan);
		List<Removal> removals = new ArrayList<>();
		Map<String, ChangeJournal.Change> touched = new LinkedHashMap<>();
		for (ChangeJournal.Change change : window.changes()) {
			if (change.deleted()) {
				removals.add(new Removal(change.keyValues(), REASON_DELETED));
			} else {
				touched.put(change.storeKey(), change);
			}
		}
		if (!query.expand().isEmpty()) {
			// expanded tracking (11.3.1): owners whose expanded navigation holds a changed
			// member report too; membership changes through link/unlink/createRelated and
			// reference patches journal the owner directly (updateReference).
			for (ChangeJournal.Change owner : ownersOfChangedMembers(query, token, maxSpan)) {
				touched.putIfAbsent(owner.storeKey(), owner);
			}
		}
		List<EObject> changed = List.of();
		if (!touched.isEmpty()) {
			Expression defining = ReadQueries.predicate(query.filter(), entityType, query.castType());
			Expression restricted = keysPredicate(entityType, touched.keySet());
			changed = fetchMatching(query, entityType,
					defining == null ? restricted : Expressions.and(defining, restricted), false);
			Set<String> matchedKeys = new HashSet<>();
			for (EObject entity : changed) {
				matchedKeys.add(CompositeIds.fragment(entity));
			}
			touched.forEach((storeKey, change) -> {
				if (!matchedKeys.contains(storeKey)) { // left the tracked membership (11.3.1)
					removals.add(new Removal(change.keyValues(), REASON_CHANGED));
				}
			});
		}
		return new DeltaResult(changed, removals, window.nextToken(), window.more());
	}

	/**
	 * {@code key IN (touched)} for single-id types, an OR of AND-of-id-equalities for
	 * composite ones — the store keys are the keyed-access fragments.
	 */
	private static Expression keysPredicate(EClass entityType, Collection<String> fragments) {
		List<EAttribute> ids = CompositeIds.idAttributes(entityType);
		if (ids.size() == 1) {
			EAttribute id = ids.get(0);
			Object[] keys = fragments.stream()
					.map(fragment -> decodeComponent(id, fragment)).toArray();
			return Expressions.path(id).in(keys);
		}
		Expression[] selectors = fragments.stream()
				.map(fragment -> keyPredicate(entityType, fragment)).toArray(Expression[]::new);
		return selectors.length == 1 ? selectors[0] : Expressions.or(selectors);
	}

	/**
	 * Owners whose EXPANDED navigation contains an entity changed inside the token window —
	 * one pushed-down query per (first-segment) navigation: {@code EXISTS(nav, key IN
	 * (changed member keys))}, respectively a plain multi-segment {@code nav.key IN (...)}
	 * for single-valued navigations.
	 */
	private List<ChangeJournal.Change> ownersOfChangedMembers(EntityQuery query, String token,
			long maxSpan) {
		List<ChangeJournal.Change> owners = new ArrayList<>();
		EClass entityType = query.entityType();
		for (String path : query.expand()) {
			int slash = path.indexOf('/');
			String navigation = slash < 0 ? path : path.substring(0, slash);
			if (!(entityType.getEStructuralFeature(navigation) instanceof EReference reference)
					|| reference.isContainment()) {
				continue; // containment children have no set-level journal entries
			}
			EClass targetType = reference.getEReferenceType();
			List<EAttribute> targetIds = CompositeIds.idAttributes(targetType);
			if (targetIds.isEmpty()) {
				continue; // members without keys are never journaled
			}
			List<String> memberFragments = journal.since(token, targetType, maxSpan).changes()
					.stream().filter(change -> !change.deleted())
					.map(ChangeJournal.Change::storeKey).toList();
			if (memberFragments.isEmpty()) {
				continue;
			}
			Expression membersChanged = reference.isMany()
					? Expressions.any(Expressions.propertyPath(reference),
							it -> memberPredicate(it, targetType, targetIds, memberFragments))
					: navigationKeysPredicate(reference, targetType, targetIds, memberFragments);
			Query irQuery = QueryBuilder.from(entityType).where(membersChanged).build();
			validate(irQuery, entityType);
			Resource resource = resource(resourceSetFactory.createResourceSet(), entityType);
			try (var result = queryable(resource).query(irQuery);
					Stream<EObject> objects = result.objects()) {
				for (EObject owner : objects.toList()) {
					owners.add(new ChangeJournal.Change(0, owner.eClass(),
							CompositeIds.fragment(owner),
							ChangeJournal.keyValuesOf(owner), false));
				}
			} catch (IOException e) {
				throw readRefused(entityType, e);
			}
		}
		return owners;
	}

	/** Member keys inside an EXISTS body: {@code it.key IN} single-id, OR-of-ANDs composite. */
	private static Expression memberPredicate(Expressions.It it, EClass targetType,
			List<EAttribute> targetIds, List<String> fragments) {
		if (targetIds.size() == 1) {
			EAttribute id = targetIds.get(0);
			return it.path(id).in(fragments.stream()
					.map(fragment -> decodeComponent(id, fragment)).toArray());
		}
		Expression[] selectors = fragments.stream().map(fragment -> {
			List<String> components = CompositeIds.parse(targetType, fragment);
			Expression[] equalities = new Expression[targetIds.size()];
			for (int i = 0; i < targetIds.size(); i++) {
				equalities[i] = it.path(targetIds.get(i))
						.eq(decodeComponent(targetIds.get(i), components.get(i)));
			}
			return Expressions.and(equalities);
		}).toArray(Expression[]::new);
		return selectors.length == 1 ? selectors[0] : Expressions.or(selectors);
	}

	/** Member keys behind a single-valued navigation: multi-segment paths instead of EXISTS. */
	private static Expression navigationKeysPredicate(EReference reference, EClass targetType,
			List<EAttribute> targetIds, List<String> fragments) {
		if (targetIds.size() == 1) {
			EAttribute id = targetIds.get(0);
			return Expressions.path(reference, id).in(fragments.stream()
					.map(fragment -> decodeComponent(id, fragment)).toArray());
		}
		Expression[] selectors = fragments.stream().map(fragment -> {
			List<String> components = CompositeIds.parse(targetType, fragment);
			Expression[] equalities = new Expression[targetIds.size()];
			for (int i = 0; i < targetIds.size(); i++) {
				equalities[i] = Expressions.path(reference, targetIds.get(i))
						.eq(decodeComponent(targetIds.get(i), components.get(i)));
			}
			return Expressions.and(equalities);
		}).toArray(Expression[]::new);
		return selectors.length == 1 ? selectors[0] : Expressions.or(selectors);
	}

	// --- relationship operations ([OData-Protocol] 13.1.1, persistence-jpa#107) ---

	@Override
	public void link(EClass entityType, String rawKey, String navigation, String targetRawKey) {
		EReference reference = navigationReference(entityType, navigation);
		String targetFragment = fragmentFromRaw(reference.getEReferenceType(), targetRawKey);
		ChangeSet template = StreamFactory.eINSTANCE.createChangeSet();
		if (reference.isMany()) {
			ChangeEntry add = entry(DeltaKind.ADD, entityType, reference, targetFragment);
			add.setIndex(-1);
			template.getEntries().add(add);
		} else {
			template.getEntries().add(entry(DeltaKind.SET, entityType, reference, targetFragment));
		}
		updateReference(entityType, fragmentFromRaw(entityType, rawKey), template, true);
	}

	@Override
	public boolean unlink(EClass entityType, String rawKey, String navigation, String targetRawKey) {
		EReference reference = navigationReference(entityType, navigation);
		ChangeSet template = StreamFactory.eINSTANCE.createChangeSet();
		if (reference.isMany()) {
			ChangeEntry remove = entry(DeltaKind.REMOVE, entityType, reference, null);
			remove.setValueOld(fragmentFromRaw(reference.getEReferenceType(), targetRawKey));
			template.getEntries().add(remove);
		} else {
			template.getEntries().add(entry(DeltaKind.SET, entityType, reference, null));
		}
		return updateReference(entityType, fragmentFromRaw(entityType, rawKey), template, false);
	}

	@Override
	public EObject createRelated(EClass entityType, String rawKey, String navigation, EObject child) {
		EReference reference = navigationReference(entityType, navigation);
		EClass childType = child.eClass();
		if (reference.isContainment()) {
			throw new UnsupportedOperationException(
					"containment children are created through their owner's write");
		}
		EObject created = create(childType, child);
		try {
			link(entityType, rawKey, navigation, "'"
					+ Objects.requireNonNull(CompositeIds.fragment(created)).replace("'", "''") + "'");
		} catch (RuntimeException e) {
			delete(childType, "'" + CompositeIds.fragment(created).replace("'", "''") + "'");
			throw e;
		}
		return created;
	}

	/** Executes a reference-entry template against the keyed owner; journals the owner. */
	private boolean updateReference(EClass entityType, String fragment, ChangeSet template,
			boolean failOnMiss) {
		UpdateCommand command = CommandFactory.eINSTANCE.createUpdateCommand();
		command.setSelector(keySelector(entityType, fragment));
		command.setTemplate(template);
		long updated;
		try {
			updated = execute(entityType, command);
		} catch (IllegalArgumentException e) {
			if (!failOnMiss && String.valueOf(e.getMessage()).contains("no member with id")) {
				return false; // unlink of a member that was not linked
			}
			throw e;
		}
		if (updated > 0) {
			EObject owner = fetchOne(entityType, fragment);
			journal.record(entityType, fragment,
					owner != null ? ChangeJournal.keyValuesOf(owner) : Map.of(), false);
		} else if (failOnMiss) {
			throw new IllegalArgumentException("no " + entityType.getName() + " with this key");
		}
		return updated > 0;
	}

	private static EReference navigationReference(EClass entityType, String navigation) {
		if (entityType.getEStructuralFeature(navigation) instanceof EReference reference
				&& !reference.isContainment() && !reference.isContainer()) {
			return reference;
		}
		throw new IllegalArgumentException(
				"'" + navigation + "' is no non-containment navigation of " + entityType.getName());
	}

	// --- $batch atomicity ([OData-Protocol] 11.7.4, persistence-jpa#108) ---

	/** Thread-bound transaction bracket: created lazily on the first command inside it. */
	private static final class Bracket {
		private Resource resource;
		private CommandTransaction transaction;
	}

	private final ThreadLocal<Bracket> bracket = new ThreadLocal<>();
	private volatile Boolean transactionsSupported;

	/** Read once from the declared command capabilities (persistence-jpa#114). */
	@Override
	public boolean transactional() {
		Boolean supported = transactionsSupported;
		if (supported == null) {
			try {
				Resource probe = resourceSetFactory.createResourceSet()
						.createResource(baseUri.appendSegment("tx-probe"));
				supported = probe instanceof CommandResource commandResource
						&& commandResource.capabilities()
								.supports(CommandFeature.TRANSACTION_BRACKET);
			} catch (RuntimeException e) {
				supported = false;
			}
			transactionsSupported = supported;
		}
		return supported;
	}

	@Override
	public void begin() {
		if (bracket.get() != null) {
			throw new IllegalStateException("a batch bracket is already open on this thread");
		}
		bracket.set(new Bracket());
		journal.begin(); // delta entries publish only when the backend commit held
	}

	@Override
	public void commit() {
		Bracket open = bracket.get();
		bracket.remove();
		if (open == null) {
			return;
		}
		try {
			if (open.transaction != null) {
				open.transaction.commit();
			}
			journal.commit();
		} catch (IOException e) {
			journal.rollback();
			throw new IllegalStateException("the backend could not commit the batch", e);
		}
	}

	@Override
	public void rollback() {
		Bracket open = bracket.get();
		bracket.remove();
		journal.rollback();
		if (open != null && open.transaction != null) {
			open.transaction.rollback();
		}
	}

	// --- templates, keys, plumbing ---

	/**
	 * Builds the patch template: SET for every transmitted single-valued attribute
	 * (a transmitted {@code null} sets null), a full REMOVE-descending/ADD-append
	 * rewrite for transmitted many-valued attributes, and — only under PUT — UNSET
	 * respectively REMOVE-all for attributes the payload omitted. Transmitted
	 * non-containment references become id-valued entries (persistence-jpa#107:
	 * SET/UNSET single-valued, REMOVE-by-id/ADD many-valued); omitted references stay
	 * untouched also under PUT (established backend semantics). Key attributes are
	 * never touched; transmitted containment references stay refused (object
	 * lifecycle, not patching).
	 */
	private ChangeSet template(EClass entityType, EObject payload, EObject current,
			boolean replace) {
		ChangeSet template = StreamFactory.eINSTANCE.createChangeSet();
		List<ChangeEntry> entries = template.getEntries();
		for (EAttribute attribute : entityType.getEAllAttributes()) {
			if (attribute.isID() || !attribute.isChangeable() || attribute.isDerived()
					|| attribute.isTransient()) {
				continue;
			}
			if (payload.eIsSet(attribute)) {
				if (attribute.isMany()) {
					rewriteList(entries, entityType, attribute, current, (List<?>) payload.eGet(attribute));
				} else {
					entries.add(entry(DeltaKind.SET, entityType, attribute,
							literal(attribute, payload.eGet(attribute))));
				}
			} else if (replace && current.eIsSet(attribute)) {
				if (attribute.isMany()) {
					rewriteList(entries, entityType, attribute, current, List.of());
				} else {
					entries.add(entry(DeltaKind.UNSET, entityType, attribute, null));
				}
			}
		}
		for (EReference reference : entityType.getEAllReferences()) {
			if (!payload.eIsSet(reference) || !reference.isChangeable() || reference.isDerived()) {
				continue;
			}
			if (reference.isContainment() || reference.isContainer()) {
				throw new UnsupportedOperationException("the containment '" + reference.getName()
						+ "' is object lifecycle — replace it through its own resource path");
			}
			if (reference.isMany()) {
				@SuppressWarnings("unchecked")
				List<EObject> members = (List<EObject>) payload.eGet(reference);
				@SuppressWarnings("unchecked")
				List<EObject> currentMembers = (List<EObject>) current.eGet(reference);
				for (EObject member : currentMembers) {
					ChangeEntry remove = entry(DeltaKind.REMOVE, entityType, reference, null);
					remove.setValueOld(requiredFragment(reference, member));
					entries.add(remove);
				}
				for (EObject member : members) {
					ChangeEntry add = entry(DeltaKind.ADD, entityType, reference,
							requiredFragment(reference, member));
					add.setIndex(-1);
					entries.add(add);
				}
			} else {
				EObject target = (EObject) payload.eGet(reference);
				entries.add(entry(DeltaKind.SET, entityType, reference,
						target == null ? null : requiredFragment(reference, target)));
			}
		}
		return template;
	}

	private static String requiredFragment(EReference reference, EObject target) {
		String fragment = CompositeIds.fragment(target);
		if (fragment == null) {
			throw new IllegalArgumentException("the member of '" + reference.getName()
					+ "' must carry its key for binding");
		}
		return fragment;
	}

	private void rewriteList(List<ChangeEntry> entries, EClass entityType, EAttribute attribute,
			EObject current, List<?> values) {
		int size = ((List<?>) current.eGet(attribute)).size();
		for (int index = size - 1; index >= 0; index--) {
			ChangeEntry remove = entry(DeltaKind.REMOVE, entityType, attribute, null);
			remove.setIndex(index);
			entries.add(remove);
		}
		for (Object value : values) {
			entries.add(entry(DeltaKind.ADD, entityType, attribute, literal(attribute, value)));
		}
	}

	private static ChangeEntry entry(DeltaKind kind, EClass entityType, EStructuralFeature feature,
			String valueNew) {
		ChangeEntry entry = StreamFactory.eINSTANCE.createChangeEntry();
		entry.setKind(kind);
		entry.setFeatureId(entityType.getFeatureID(feature));
		entry.setValueNew(valueNew);
		return entry;
	}

	private static String literal(EAttribute attribute, Object value) {
		return value == null ? null : EcoreUtil.convertToString(attribute.getEAttributeType(), value);
	}

	/** The keyed-access fragment as AND-of-id-equalities Expression IR selector. */
	private static Query keySelector(EClass entityType, String fragment) {
		return QueryBuilder.from(entityType).where(keyPredicate(entityType, fragment)).build();
	}

	private static Expression keyPredicate(EClass entityType, String fragment) {
		List<EAttribute> ids = CompositeIds.idAttributes(entityType);
		if (ids.size() == 1) {
			EAttribute id = ids.get(0);
			return Expressions.path(id).eq(decodeComponent(id, fragment));
		}
		List<String> components = CompositeIds.parse(entityType, fragment);
		Expression[] equalities = new Expression[ids.size()];
		for (int i = 0; i < ids.size(); i++) {
			equalities[i] = Expressions.path(ids.get(i)).eq(decodeComponent(ids.get(i),
					components.get(i)));
		}
		return equalities.length == 1 ? equalities[0] : Expressions.and(equalities);
	}

	private static Object decodeComponent(EAttribute id, String value) {
		Object decoded = EcoreUtil.createFromString(id.getEAttributeType(), value);
		return Objects.requireNonNull(decoded, "the key literal could not be decoded");
	}

	/** URL key literal(s) → the CompositeIds fragment contract (persistence-jpa#109). */
	private static String fragmentFromRaw(EClass entityType, String rawKey) {
		List<EAttribute> ids = CompositeIds.idAttributes(entityType);
		if (ids.isEmpty()) {
			throw new IllegalArgumentException(entityType.getName() + " has no key attribute");
		}
		if (ids.size() == 1) {
			String value = unquote(rawKey);
			decodeComponent(ids.get(0), value); // validate early → 400, not backend noise
			return value;
		}
		// composite single-literal keys are ambiguous — the servlet passes named keys
		throw new IllegalArgumentException(entityType.getName()
				+ " has a composite key — address it as (k1=v1,k2=v2)");
	}

	private static String fragmentFromNamed(EClass entityType, Map<String, String> namedKeys) {
		List<EAttribute> ids = CompositeIds.idAttributes(entityType);
		if (ids.size() == 1 && namedKeys.size() == 1) {
			return unquote(namedKeys.values().iterator().next());
		}
		EObject probe = entityType.getEPackage().getEFactoryInstance().create(entityType);
		for (EAttribute id : ids) {
			String raw = namedKeys.get(id.getName());
			if (raw == null) {
				throw new IllegalArgumentException("the key of " + entityType.getName()
						+ " misses the component '" + id.getName() + "'");
			}
			probe.eSet(id, decodeComponent(id, unquote(raw)));
		}
		return Objects.requireNonNull(CompositeIds.fragment(probe),
				"the key literals could not be decoded");
	}

	/**
	 * Keyed lookup via the fragment contract (persistence-jpa#109): both backends
	 * resolve it to a primary-key find. Deliberately not {@code QueryableResource.query}
	 * — EclipseLink turns an ID-equality JPQL into a {@code ReadObjectQuery}, on which
	 * the read path's scrollable-cursor hint is invalid. Inside a bracket the lookup
	 * runs on the bracket's resource so it sees uncommitted batch writes.
	 */
	private EObject fetchOne(EClass entityType, String fragment) {
		return resourceFor(entityType).getEObject(fragment);
	}

	private long execute(EClass entityType, Command command) {
		try {
			return commands(resourceFor(entityType)).execute(command);
		} catch (IOException e) {
			throw refused(entityType, e);
		}
	}

	/** Inside an open bracket every command and keyed read runs on the bracket's resource. */
	private Resource resourceFor(EClass entityType) {
		Bracket open = bracket.get();
		if (open == null) {
			return resource(resourceSetFactory.createResourceSet(), entityType);
		}
		if (open.resource == null) {
			open.resource = resource(resourceSetFactory.createResourceSet(), entityType);
			try {
				open.transaction = commands(open.resource).begin();
			} catch (IOException e) {
				throw new UnsupportedOperationException(
						"the backend does not support atomic batches", e);
			}
		}
		return open.resource;
	}

	private Resource resource(ResourceSet resourceSet, EClass entityType) {
		Resource resource = resourceSet.createResource(baseUri.appendSegment(entityType.getName()));
		if (resource == null) {
			throw new IllegalStateException("no resource factory serves " + baseUri);
		}
		return resource;
	}

	private static CommandResource commands(Resource resource) {
		if (resource instanceof CommandResource commands) {
			return commands;
		}
		throw new IllegalStateException("the backend resource does not support commands");
	}

	/**
	 * Backend refusals keep their honesty classes: "rejected" = the request is invalid
	 * for the data (dangling reference target, malformed template) → 400; "not
	 * supported" → 501; everything else stays an internal fault.
	 */
	private static RuntimeException refused(EClass entityType, IOException cause) {
		String message = String.valueOf(cause.getMessage());
		if (message.contains("is not supported")) {
			return new UnsupportedOperationException(message, cause);
		}
		// a QueryException cause is the upstream client-error shape (invalid template,
		// dangling reference target) — "rejected" at validation, "failed" at apply time
		if (cause.getCause() instanceof QueryException || message.contains("rejected")) {
			return new IllegalArgumentException(message, cause);
		}
		return new IllegalStateException(
				"the persistence backend refused the " + entityType.getName() + " command", cause);
	}

	private static String unquote(String raw) {
		if (raw.length() >= 2 && raw.startsWith("'") && raw.endsWith("'")) {
			return raw.substring(1, raw.length() - 1).replace("''", "'");
		}
		return raw;
	}
}
