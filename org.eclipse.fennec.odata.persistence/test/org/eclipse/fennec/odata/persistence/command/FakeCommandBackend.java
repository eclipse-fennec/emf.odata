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

	private final Map<String, Map<Object, EObject>> stores = new LinkedHashMap<>();

	@Override
	public Resource createResource(URI uri) {
		return new FakeResource(uri, this);
	}

	Map<Object, EObject> storeFor(String entityName) {
		return stores.computeIfAbsent(entityName, name -> new LinkedHashMap<>());
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
			try {
				return MemoryQueries.execute(query,
						List.copyOf(backend.storeFor(query.getFrom().getName()).values()), null);
			} catch (QueryException e) {
				throw new IOException("Query rejected: " + e.getMessage(), e);
			}
		}

		@Override
		public QueryResult query(Query query, Map<String, Object> parameters, Map<?, ?> options)
				throws IOException {
			return query(query);
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
			if (command instanceof InsertCommand insert) {
				return executeInsert(insert);
			}
			if (command instanceof DeleteCommand delete) {
				return executeDelete(delete);
			}
			if (command instanceof UpdateCommand update) {
				return executeUpdate(update);
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

		private long executeDelete(DeleteCommand delete) throws IOException {
			guardPlainSelector(delete.getSelector(), "Delete");
			List<EObject> matches = matches(delete.getSelector(), "Delete");
			Map<Object, EObject> store = backend.storeFor(delete.getSelector().getFrom().getName());
			matches.forEach(match -> store.remove(keyOf(match)));
			return matches.size();
		}

		private long executeUpdate(UpdateCommand update) throws IOException {
			guardPlainSelector(update.getSelector(), "Update");
			try {
				ChangeTemplates.validate(update.getTemplate(), update.getSelector().getFrom());
				List<EObject> matches = matches(update.getSelector(), "Update");
				for (EObject match : matches) {
					ChangeTemplates.apply(update.getTemplate(), match, backend::resolveById);
				}
				return matches.size();
			} catch (QueryException e) {
				throw new IOException("Update rejected: " + e.getMessage(), e);
			}
		}

		private List<EObject> matches(Query selector, String operation) throws IOException {
			try (QueryResult result = MemoryQueries.execute(selector,
					List.copyOf(backend.storeFor(selector.getFrom().getName()).values()), null);
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
