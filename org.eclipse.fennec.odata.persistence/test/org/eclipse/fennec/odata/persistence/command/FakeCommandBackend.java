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
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.command.Command;
import org.eclipse.fennec.model.command.DeleteCommand;
import org.eclipse.fennec.model.command.InsertCommand;
import org.eclipse.fennec.model.command.UpdateCommand;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.persistence.capabilities.CommandCapabilitiesBuilder;
import org.eclipse.fennec.persistence.capabilities.CommandFeature;
import org.eclipse.fennec.persistence.capabilities.PersistenceCapabilities;
import org.eclipse.fennec.persistence.capabilities.StoreCapabilitiesBuilder;
import org.eclipse.fennec.persistence.capabilities.StoreFeature;
import org.eclipse.fennec.persistence.diagnostic.PersistenceDiagnostic;
import org.eclipse.fennec.persistence.helper.CompositeIds;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.CommandResource;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.persistence.query.api.QueryableResource;
import org.eclipse.fennec.persistence.query.memory.MemoryQueries;
import org.eclipse.fennec.persistence.query.memory.MemoryQueryProcessor;
import org.eclipse.fennec.persistence.query.support.ChangeTemplates;
import org.eclipse.fennec.persistence.query.support.CommandTransaction;
import org.eclipse.fennec.persistence.query.support.ReferenceResolver;
import org.eclipse.fennec.persistence.resource.PersistenceResource;

/**
 * In-memory stand-in for a persistence backend with the REAL upstream semantics:
 * selectors are evaluated by {@code MemoryQueries}, update templates applied by
 * {@code ChangeTemplates} (incl. reference patching via a {@link ReferenceResolver},
 * persistence-jpa#107), inserts bind non-containment targets by id, keyed access
 * follows the {@link CompositeIds} fragment contract (#109) and {@code begin()} opens
 * a store-snapshot transaction bracket (#108) — so refusals and effects surface
 * exactly like the JPA and Mongo resources.
 */
final class FakeCommandBackend implements Resource.Factory {

	private static final String DIAGNOSTIC_SOURCE = FakeCommandBackend.class.getName();

	private final Map<String, Map<Object, EObject>> stores = new LinkedHashMap<>();

	/** What the next query throws instead of running, if anything — see the two setters. */
	private IOException queryFailure;

	/** The query this backend last received — the pushed-down shape, for assertions. */
	private Query lastQuery;

	/** The last translated query, for asserting what was ASKED rather than what came back. */
	Query lastQuery() {
		return lastQuery;
	}
	/**
	 * Makes the next query refuse in the shape both real backends use: an
	 * {@code IOException} whose message says "rejected" and whose cause is a
	 * {@code QueryException}, raised at translation time rather than reported by
	 * {@code validate()} (persistence-jpa#237 is one real instance).
	 */
	void refuseNextQuery(String reason) {
		this.queryFailure = new IOException("Query rejected: " + reason, new QueryException(reason));
	}

	/** Makes the next query fail as a plain fault — no refusal shape, no cause to read. */
	void failNextQueryWith(IOException failure) {
		this.queryFailure = failure;
	}

	@Override
	public Resource createResource(URI uri) {
		return new FakeResource(uri, this);
	}

	Map<Object, EObject> storeFor(String entityName) {
		return stores.computeIfAbsent(entityName, name -> new LinkedHashMap<>());
	}

	/**
	 * The §4c contract both database backends hold since persistence-jpa#195/#219: an object
	 * something still points at is not deleted. A referrer that is itself among the deleted
	 * objects counts, exactly as upstream documents at its own guard.
	 *
	 * @return {@code <Type>.<reference>} of the first inbound reference found, or {@code null}
	 */
	String referrerOf(EObject target) {
		String fragment = CompositeIds.fragment(target);
		for (Map<Object, EObject> store : stores.values()) {
			for (EObject candidate : store.values()) {
				for (EReference reference : candidate.eClass().getEAllReferences()) {
					if (reference.isContainment() || reference.isDerived()
							|| !reference.getEReferenceType().isInstance(target)) {
						continue;
					}
					if (pointsAt(candidate.eGet(reference), target, fragment)) {
						return candidate.eClass().getName() + "." + reference.getName();
					}
				}
			}
		}
		return null;
	}

	private static boolean pointsAt(Object value, EObject target, String fragment) {
		if (value instanceof List<?> many) {
			return many.stream().anyMatch(held -> pointsAt(held, target, fragment));
		}
		return value == target || (value instanceof EObject held && fragment != null
				&& fragment.equals(CompositeIds.fragment(held)));
	}

	private EObject resolveById(EReference reference, String id) {
		EClass targetType = reference.getEReferenceType();
		return stores.values().stream().flatMap(store -> store.values().stream())
				.filter(targetType::isInstance)
				.filter(candidate -> id.equals(CompositeIds.fragment(candidate)))
				.findFirst().orElse(null);
	}

	/** Store keys: the raw id value for single-id types (test seeding convention), the fragment else. */
	private static Object keyOf(EObject object) {
		List<EAttribute> ids = CompositeIds.idAttributes(object.eClass());
		if (ids.size() == 1) {
			Object value = object.eGet(ids.get(0));
			if (value == null) {
				throw new IllegalStateException(object.eClass().getName() + " has no id value");
			}
			return value;
		}
		String fragment = CompositeIds.fragment(object);
		if (fragment == null) {
			throw new IllegalStateException(object.eClass().getName() + " has no id value");
		}
		return fragment;
	}

	private static final class FakeResource extends ResourceImpl
			implements PersistenceResource, QueryableResource, CommandResource {

		private final FakeCommandBackend backend;
		private Map<String, Map<Object, EObject>> snapshot;

		private FakeResource(URI uri, FakeCommandBackend backend) {
			super(uri);
			this.backend = backend;
		}

		private Map<Object, EObject> store() {
			return backend.storeFor(getURI().lastSegment());
		}

		@Override
		public EObject getEObject(String uriFragment) {
			// keyed lookup via the CompositeIds fragment contract, like the real resources
			return store().values().stream()
					.filter(object -> uriFragment.equals(CompositeIds.fragment(object)))
					.findFirst().orElse(null);
		}

		@Override
		public QueryResult query(Query query) throws IOException {
			return query(query, null, null);
		}

		@Override
		public QueryResult query(Query query, Map<String, Object> parameters, Map<?, ?> options)
				throws IOException {
			if (backend.queryFailure != null) {
				IOException failure = backend.queryFailure;
				backend.queryFailure = null;
				throw failure;
			}
			// the ask is what a consumer test asserts on; the reference engine deliberately
			// serves no expand options (it has no proxies to select), so the copy it runs is
			// stripped of them — this double stands in for a store that hands out proxies
			backend.lastQuery = EcoreUtil.copy(query);
			query.getExpand().forEach(expand -> {
				expand.setFilter(null);
				expand.getOrderBy().clear();
				expand.setTop(0);
				expand.setSkip(0);
			});
			try {
				return MemoryQueries.execute(query,
						List.copyOf(backend.storeFor(query.getFrom().getName()).values()),
						parameters);
			} catch (QueryException e) {
				throw new IOException("Query rejected: " + e.getMessage(), e);
			}
		}

		@Override
		public QueryResult query(String name, Map<String, Object> parameters, Map<?, ?> options)
				throws IOException {
			throw new IOException("named queries are not supported");
		}

		/**
		 * Mirror the real backends (persistence-jpa#114, #134): the full command vocabulary,
		 * the store bracket this fake really opens in {@link #begin()}, and — since selectors
		 * are evaluated by {@code MemoryQueries} — that engine's own query declaration.
		 */
		@Override
		public PersistenceCapabilities capabilities() {
			return PersistenceCapabilities.of(new MemoryQueryProcessor().capabilities(),
					CommandCapabilitiesBuilder.create()
							.support(CommandFeature.INSERT, CommandFeature.DELETE_BY_SELECTOR,
									CommandFeature.UPDATE_BY_SELECTOR)
							.build(),
					StoreCapabilitiesBuilder.create().support(StoreFeature.TRANSACTION_BRACKET)
							.build());
		}

		// --- PersistenceResource beyond the capability statement: the store is the map ---

		@Override
		public void updateDefaultOptions(Map<Object, Object> options, ActionType... types) {
			// the fake reads no options
		}

		@Override
		public long count() throws IOException {
			return store().size();
		}

		@Override
		public long count(Map<?, ?> options) throws IOException {
			return count();
		}

		@Override
		public boolean exist() throws IOException {
			return !store().isEmpty();
		}

		@Override
		public boolean exist(Map<?, ?> options) throws IOException {
			return exist();
		}

		@Override
		public void close() {
			unload();
		}

		@Override
		public CommandTransaction begin() throws IOException {
			if (snapshot != null) {
				throw new IOException("a transaction bracket is already open on this resource");
			}
			// deep store snapshot: updates mutate stored objects in place
			Map<String, Map<Object, EObject>> copy = new LinkedHashMap<>();
			backend.stores.forEach((name, store) -> {
				Map<Object, EObject> storeCopy = new LinkedHashMap<>();
				store.forEach((key, object) -> storeCopy.put(key, EcoreUtil.copy(object)));
				copy.put(name, storeCopy);
			});
			snapshot = copy;
			return new CommandTransaction() {

				@Override
				public void commit() {
					snapshot = null;
				}

				@Override
				public void rollback() {
					if (snapshot != null) {
						backend.stores.clear();
						backend.stores.putAll(snapshot);
						snapshot = null;
					}
				}

				@Override
				public void close() {
					rollback();
				}
			};
		}

		@Override
		public long execute(Command command) throws IOException {
			return execute(command, null, null);
		}

		/**
		 * The bound form (persistence-jpa#202): a selector may carry {@code ParameterRef}
		 * nodes, and the values reach the selector evaluation the same way they reach the
		 * real backends. Options are read by neither.
		 */
		@Override
		public long execute(Command command, Map<String, Object> parameters, Map<?, ?> options)
				throws IOException {
			if (command instanceof InsertCommand insert) {
				return executeInsert(insert);
			}
			if (command instanceof DeleteCommand delete) {
				return executeDelete(delete, parameters);
			}
			if (command instanceof UpdateCommand update) {
				return executeUpdate(update, parameters);
			}
			throw new IOException("Unsupported command " + command.eClass().getName());
		}

		private long executeInsert(InsertCommand insert) throws IOException {
			try {
				Map<EObject, EObject> copies = new IdentityHashMap<>();
				for (EObject object : insert.getObjects()) {
					copies.put(object, EcoreUtil.copy(object));
				}
				// non-containment targets bind by id, like the real backends (#107)
				ChangeTemplates.bindInsertReferences(copies, backend::resolveById);
				for (EObject copy : copies.values()) {
					backend.storeFor(copy.eClass().getName()).put(keyOf(copy), copy);
				}
				return copies.size();
			} catch (QueryException e) {
				throw new IOException("Insert rejected: " + e.getMessage(), e);
			}
		}

		private long executeDelete(DeleteCommand delete, Map<String, Object> parameters)
				throws IOException {
			guardPlainSelector(delete.getSelector(), "Delete");
			List<EObject> matches = matches(delete.getSelector(), parameters, "Delete");
			refuseWhenStillReferenced(matches);
			Map<Object, EObject> store = backend.storeFor(delete.getSelector().getFrom().getName());
			matches.forEach(match -> store.remove(keyOf(match)));
			return matches.size();
		}

		private long executeUpdate(UpdateCommand update, Map<String, Object> parameters)
				throws IOException {
			guardPlainSelector(update.getSelector(), "Update");
			try {
				ChangeTemplates.validate(update.getTemplate(), update.getSelector().getFrom());
				List<EObject> matches = matches(update.getSelector(), parameters, "Update");
				for (EObject match : matches) {
					ChangeTemplates.apply(update.getTemplate(), match, backend::resolveById);
				}
				return matches.size();
			} catch (QueryException e) {
				throw new IOException("Update rejected: " + e.getMessage(), e);
			}
		}

		/**
		 * Refuses like both real backends do, and — the part that matters for the consumer —
		 * with {@code CODE_REFERENTIAL_INTEGRITY} on the diagnostic. Since
		 * persistence-jpa#229 that code, not the wording, is what a caller routes on: the
		 * three paths upstream phrase this refusal three different ways.
		 */
		private void refuseWhenStillReferenced(List<EObject> matches) throws IOException {
			for (EObject match : matches) {
				String referrer = backend.referrerOf(match);
				if (referrer != null) {
					String message = "Cannot delete " + match.eClass().getName() + " '"
							+ CompositeIds.fragment(match) + "': " + referrer
							+ " still references it";
					getErrors().add(PersistenceDiagnostic.error(
							PersistenceDiagnostic.CODE_REFERENTIAL_INTEGRITY, DIAGNOSTIC_SOURCE,
							message, getURI(), null));
					throw new IOException(message);
				}
			}
		}

		private List<EObject> matches(Query selector, Map<String, Object> parameters,
				String operation) throws IOException {
			try (QueryResult result = MemoryQueries.execute(selector,
					List.copyOf(backend.storeFor(selector.getFrom().getName()).values()),
					parameters);
					Stream<EObject> objects = result.objects()) {
				return objects.toList();
			} catch (QueryException e) {
				throw new IOException(operation + " selector rejected: " + e.getMessage(), e);
			} catch (Exception e) {
				throw new IOException(operation + " failed: " + e.getMessage(), e);
			}
		}

		private static void guardPlainSelector(Query selector, String operation) throws IOException {
			boolean plain = selector.getSelect().isEmpty() && selector.getApply() == null
					&& selector.getOrderBy().isEmpty() && selector.getExpand().isEmpty()
					&& selector.getTop() <= 0 && selector.getSkip() <= 0 && !selector.isDistinct()
					&& !selector.isCountOnly();
			if (!plain) {
				throw new IOException(operation + " selector rejected: Command selectors must be plain"
						+ " filters — projection/aggregation/ordering/paging are not allowed");
			}
		}
	}
}
