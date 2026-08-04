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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.command.Command;
import org.eclipse.fennec.model.command.DeleteCommand;
import org.eclipse.fennec.model.command.InsertCommand;
import org.eclipse.fennec.model.command.UpdateCommand;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.persistence.query.QueryException;
import org.eclipse.fennec.persistence.query.api.CommandResource;
import org.eclipse.fennec.persistence.query.api.QueryResult;
import org.eclipse.fennec.persistence.query.api.QueryableResource;
import org.eclipse.fennec.persistence.query.memory.MemoryQueries;
import org.eclipse.fennec.persistence.query.support.ChangeTemplates;

/**
 * In-memory stand-in for a persistence backend with the REAL upstream semantics:
 * selectors are evaluated by {@code MemoryQueries} and update templates applied by
 * {@code ChangeTemplates}, so refusals (reference patching, non-plain selectors)
 * surface exactly like the JPA and Mongo resources — as {@link IOException}s with
 * the upstream message prefixes.
 */
final class FakeCommandBackend implements Resource.Factory {

	private final Map<String, Map<Object, EObject>> stores = new LinkedHashMap<>();

	@Override
	public Resource createResource(URI uri) {
		return new FakeResource(uri, storeFor(uri.lastSegment()));
	}

	Map<Object, EObject> storeFor(String entityName) {
		return stores.computeIfAbsent(entityName, name -> new LinkedHashMap<>());
	}

	private static Object keyOf(EObject object) {
		for (EAttribute attribute : object.eClass().getEAllAttributes()) {
			if (attribute.isID()) {
				return object.eGet(attribute);
			}
		}
		throw new IllegalStateException(object.eClass().getName() + " has no id attribute");
	}

	private static final class FakeResource extends ResourceImpl
			implements QueryableResource, CommandResource {

		private final Map<Object, EObject> store;

		private FakeResource(URI uri, Map<Object, EObject> store) {
			super(uri);
			this.store = store;
		}

		@Override
		public QueryResult query(Query query) throws IOException {
			try {
				return MemoryQueries.execute(query, List.copyOf(store.values()), null);
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

		private long executeInsert(InsertCommand insert) {
			long count = 0;
			for (EObject object : insert.getObjects()) {
				EObject copy = EcoreUtil.copy(object);
				store.put(keyOf(copy), copy);
				count++;
			}
			return count;
		}

		private long executeDelete(DeleteCommand delete) throws IOException {
			guardPlainSelector(delete.getSelector(), "Delete");
			List<EObject> matches = matches(delete.getSelector(), "Delete");
			matches.forEach(match -> store.remove(keyOf(match)));
			return matches.size();
		}

		private long executeUpdate(UpdateCommand update) throws IOException {
			guardPlainSelector(update.getSelector(), "Update");
			try {
				ChangeTemplates.validate(update.getTemplate(), update.getSelector().getFrom());
				List<EObject> matches = matches(update.getSelector(), "Update");
				for (EObject match : matches) {
					ChangeTemplates.apply(update.getTemplate(), match);
				}
				return matches.size();
			} catch (QueryException e) {
				throw new IOException("Update rejected: " + e.getMessage(), e);
			}
		}

		private List<EObject> matches(Query selector, String operation) throws IOException {
			try (QueryResult result = MemoryQueries.execute(selector, List.copyOf(store.values()), null);
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
