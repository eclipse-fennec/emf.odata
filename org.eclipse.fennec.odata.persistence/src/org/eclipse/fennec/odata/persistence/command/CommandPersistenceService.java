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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteConflictException;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.persistence.query.QueryConstants;
import org.eclipse.fennec.persistence.query.api.CommandResource;
import org.eclipse.fennec.persistence.query.api.QueryFeature;
import org.eclipse.fennec.persistence.query.api.QueryProcessor;
import org.eclipse.fennec.persistence.query.api.QueryableResource;
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
 * many-valued ones). The upstream patch-apply engine cannot express reference
 * changes — payloads carrying navigation members are refused with
 * {@link UnsupportedOperationException}, which the servlet maps to an honest 501.
 *
 * <p>Each command executes in its own backend transaction; there is no
 * cross-command transaction to join, so this backend does not offer
 * {@code $batch} atomicity ({@link #transactional()} stays {@code false}).
 *
 * <p>Configuration (factory configurations supported): {@value #URI_PROPERTY} is the
 * backend base URI (required); {@value #PACKAGES_PROPERTY} optionally restricts the
 * served EPackages by nsURI — without it every single-key EClass is claimed, which is
 * almost never what a runtime with more than one backend wants.
 */
@Component(configurationPid = CommandPersistenceService.PID, configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = "fennec.odata.backend=command")
public class CommandPersistenceService implements QueryService, WriteService {

	public static final String PID = "org.eclipse.fennec.odata.persistence.command";
	public static final String URI_PROPERTY = "backend.uri";
	public static final String PACKAGES_PROPERTY = "emf.nsURIs";
	public static final String PAGE_SIZE_PROPERTY = "max.page.size";

	private static final int DEFAULT_MAX_PAGE_SIZE = 1000;

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
		return singleKeyAttribute(entityType) != null;
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
		QueryBuilder builder = QueryBuilder.from(entityType);
		Expression predicate = ReadQueries.predicate(query.filter(), entityType, query.castType());
		if (predicate != null) {
			builder.where(predicate);
		}
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
				// the IR fetch hint carries single-segment paths; deeper levels are
				// materialized through the proxy contract below
				builder.expand(chain.get(0));
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
		EAttribute id = requiredKeyAttribute(entityType);
		if (!entity.eIsSet(id)) {
			throw new IllegalArgumentException(
					"the payload must carry the key of the new " + entityType.getName());
		}
		Object key = entity.eGet(id);
		refuseNonContainmentReferences(entity);
		if (fetchOne(entityType, id, key) != null) {
			throw new WriteConflictException(
					"a " + entityType.getName() + " with this key already exists");
		}
		insert(entityType, entity);
		EObject stored = fetchOne(entityType, id, key);
		return stored != null ? stored : entity;
	}

	@Override
	public WriteResult update(EClass entityType, String rawKey, EObject payload, boolean replace) {
		EAttribute id = requiredKeyAttribute(entityType);
		Object key = decodeKey(id, rawKey);
		EObject current = fetchOne(entityType, id, key);
		if (current == null) {
			// upsert: the URL key wins over anything in the payload
			payload.eSet(id, key);
			refuseNonContainmentReferences(payload);
			insert(entityType, payload);
			EObject stored = fetchOne(entityType, id, key);
			return new WriteResult(stored != null ? stored : payload, true);
		}
		ChangeSet template = template(entityType, id, payload, current, replace);
		if (!template.getEntries().isEmpty()) {
			UpdateCommand command = CommandFactory.eINSTANCE.createUpdateCommand();
			command.setSelector(keySelector(entityType, id, key));
			command.setTemplate(template);
			execute(entityType, command);
		}
		EObject stored = fetchOne(entityType, id, key);
		return new WriteResult(stored != null ? stored : current, false);
	}

	@Override
	public boolean delete(EClass entityType, String rawKey) {
		EAttribute id = requiredKeyAttribute(entityType);
		Object key = decodeKey(id, rawKey);
		DeleteCommand command = CommandFactory.eINSTANCE.createDeleteCommand();
		command.setSelector(keySelector(entityType, id, key));
		return execute(entityType, command) > 0;
	}

	private void insert(EClass entityType, EObject entity) {
		InsertCommand command = CommandFactory.eINSTANCE.createInsertCommand();
		command.getObjects().add(entity);
		execute(entityType, command);
	}

	/**
	 * Builds the patch template: SET for every transmitted single-valued attribute
	 * (a transmitted {@code null} sets null), a full REMOVE-descending/ADD-append
	 * rewrite for transmitted many-valued attributes, and — only under PUT — UNSET
	 * respectively REMOVE-all for attributes the payload omitted. The key attribute
	 * is never touched; transmitted references are refused because the upstream
	 * engine cannot patch them.
	 */
	private ChangeSet template(EClass entityType, EAttribute id, EObject payload, EObject current,
			boolean replace) {
		for (EReference reference : entityType.getEAllReferences()) {
			if (payload.eIsSet(reference)) {
				throw new UnsupportedOperationException("the command backend cannot change the reference '"
						+ reference.getName() + "' — reference patching is not supported yet");
			}
		}
		ChangeSet template = StreamFactory.eINSTANCE.createChangeSet();
		List<ChangeEntry> entries = template.getEntries();
		for (EAttribute attribute : entityType.getEAllAttributes()) {
			if (attribute == id || !attribute.isChangeable() || attribute.isDerived()
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
		return template;
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

	private static ChangeEntry entry(DeltaKind kind, EClass entityType, EAttribute attribute,
			String valueNew) {
		ChangeEntry entry = StreamFactory.eINSTANCE.createChangeEntry();
		entry.setKind(kind);
		entry.setFeatureId(entityType.getFeatureID(attribute));
		entry.setValueNew(valueNew);
		return entry;
	}

	private static String literal(EAttribute attribute, Object value) {
		return value == null ? null : EcoreUtil.convertToString(attribute.getEAttributeType(), value);
	}

	private static void refuseNonContainmentReferences(EObject root) {
		checkNonContainment(root);
		root.eAllContents().forEachRemaining(CommandPersistenceService::checkNonContainment);
	}

	private static void checkNonContainment(Object candidate) {
		EObject object = (EObject) candidate;
		for (EReference reference : object.eClass().getEAllReferences()) {
			if (!reference.isContainment() && !reference.isContainer() && object.eIsSet(reference)) {
				throw new UnsupportedOperationException("the command backend cannot bind the reference '"
						+ reference.getName() + "' — non-containment members are not supported yet");
			}
		}
	}

	private static Query keySelector(EClass entityType, EAttribute id, Object key) {
		return QueryBuilder.from(entityType).where(Expressions.path(id).eq(key)).build();
	}

	/**
	 * Keyed lookup via the EMF fragment contract: both backends resolve a plain-id
	 * fragment to a primary-key find ({@code em.find} respectively {@code _id} query).
	 * Deliberately not {@code QueryableResource.query} — EclipseLink turns an
	 * ID-equality JPQL into a {@code ReadObjectQuery}, on which the read path's
	 * scrollable-cursor hint is invalid (upstream issue).
	 */
	private EObject fetchOne(EClass entityType, EAttribute id, Object key) {
		Resource resource = resource(entityType);
		return resource.getEObject(EcoreUtil.convertToString(id.getEAttributeType(), key));
	}

	private long execute(EClass entityType, Command command) {
		Resource resource = resource(entityType);
		try {
			return commands(resource).execute(command);
		} catch (IOException e) {
			throw refused(entityType, e);
		}
	}

	private Resource resource(EClass entityType) {
		return resource(resourceSetFactory.createResourceSet(), entityType);
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

	private static IllegalStateException refused(EClass entityType, IOException cause) {
		return new IllegalStateException(
				"the persistence backend refused the " + entityType.getName() + " command", cause);
	}

	private static EAttribute requiredKeyAttribute(EClass entityType) {
		EAttribute id = singleKeyAttribute(entityType);
		if (id == null) {
			throw new IllegalArgumentException(
					entityType.getName() + " has no single key attribute");
		}
		return id;
	}

	private static EAttribute singleKeyAttribute(EClass entityType) {
		EAttribute key = null;
		for (EAttribute attribute : entityType.getEAllAttributes()) {
			if (attribute.isID()) {
				if (key != null) {
					return null; // composite keys are not supported yet
				}
				key = attribute;
			}
		}
		return key;
	}

	private static Object decodeKey(EAttribute id, String rawKey) {
		Object key = EcoreUtil.createFromString(id.getEAttributeType(), unquote(rawKey));
		return Objects.requireNonNull(key, "the key literal could not be decoded");
	}

	private static String unquote(String raw) {
		if (raw.length() >= 2 && raw.startsWith("'") && raw.endsWith("'")) {
			return raw.substring(1, raw.length() - 1).replace("''", "'");
		}
		return raw;
	}
}
