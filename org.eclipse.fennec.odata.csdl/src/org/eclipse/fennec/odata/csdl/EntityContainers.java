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
package org.eclipse.fennec.odata.csdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TEntitySet;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TNavigationProperty;
import org.open.oasis.docs.odata.ns.edm.TNavigationPropertyBinding;

/**
 * Composes the per-package schemas of ONE service into a metadata document with exactly one
 * entity container ([OData-CSDL-XML] §13 / [OData-CSDL-JSON] §13: "each metadata document used to
 * describe an OData service MUST define exactly one entity container"). {@link EcoreToEdmConverter}
 * builds a default container per package; a service publishing several packages hands all their
 * schemas to {@link #merge(List)}, which keeps the types where they are and moves every container
 * member into the container of the FIRST schema.
 */
public final class EntityContainers {

	private static final System.Logger LOGGER = System.getLogger(EntityContainers.class.getName());

	private EntityContainers() {
	}

	/**
	 * Moves the entity sets, singletons, function and action imports of every schema's container
	 * into one container hosted by the first schema (named like the first container found), drops
	 * the emptied containers and adds the navigation bindings that cross schema boundaries.
	 * A member whose name the host container already declares is dropped with a warning (names are
	 * unique within a container, §13.1). Returns the single container, or {@code null} when no
	 * schema declares one.
	 */
	public static TEntityContainer merge(List<SchemaType> schemas) {
		TEntityContainer host = null;
		for (SchemaType schema : schemas) {
			if (!schema.getEntityContainer().isEmpty()) {
				host = schema.getEntityContainer().get(0);
				break;
			}
		}
		if (host == null) {
			return null;
		}
		SchemaType first = schemas.get(0);
		if (host.eContainer() != first) { // the first schema hosts it — stable across rebinds
			first.getEntityContainer().add(0, host);
		}
		Set<String> names = new HashSet<>();
		host.getEntitySet().forEach(m -> names.add(m.getName()));
		host.getSingleton().forEach(m -> names.add(m.getName()));
		host.getFunctionImport().forEach(m -> names.add(m.getName()));
		host.getActionImport().forEach(m -> names.add(m.getName()));
		for (SchemaType schema : schemas) {
			for (TEntityContainer container : new ArrayList<>(schema.getEntityContainer())) {
				if (container == host) {
					continue;
				}
				moveMembers(container.getEntitySet(), host.getEntitySet(), TEntitySet::getName, names);
				moveMembers(container.getSingleton(), host.getSingleton(), m -> m.getName(), names);
				moveMembers(container.getFunctionImport(), host.getFunctionImport(),
						m -> m.getName(), names);
				moveMembers(container.getActionImport(), host.getActionImport(),
						m -> m.getName(), names);
				schema.getEntityContainer().remove(container);
			}
		}
		bindNavigations(schemas, host);
		return host;
	}

	private static <T> void moveMembers(List<T> from, List<T> to, Function<T, String> name,
			Set<String> names) {
		for (T member : new ArrayList<>(from)) {
			if (names.add(name.apply(member))) {
				to.add(member); // moves it: the containment list re-parents the object
			} else {
				LOGGER.log(System.Logger.Level.WARNING, () -> "entity container member '"
						+ name.apply(member) + "' is declared by more than one schema — kept once");
			}
		}
	}

	/**
	 * Adds the {@code NavigationPropertyBinding}s the per-schema containers could not declare:
	 * each entity set binds every (declared or inherited) non-containment navigation whose target
	 * type is served by a set of the container — now including types and base types of OTHER
	 * schemas. Bindings already present (same path) are kept untouched.
	 */
	static void bindNavigations(List<SchemaType> schemas, TEntityContainer container) {
		Map<String, TEntityType> typesByQualifiedName = new HashMap<>();
		for (SchemaType schema : schemas) {
			for (TEntityType type : schema.getEntityType()) {
				typesByQualifiedName.put(schema.getNamespace() + "." + type.getName(), type);
			}
		}
		Map<String, TEntitySet> setByEntityType = new HashMap<>();
		container.getEntitySet()
				.forEach(s -> setByEntityType.putIfAbsent(String.valueOf(s.getEntityType()), s));

		for (TEntitySet set : container.getEntitySet()) {
			Set<String> bound = new HashSet<>();
			set.getNavigationPropertyBinding().forEach(b -> bound.add(String.valueOf(b.getPath())));
			Set<String> visited = new HashSet<>();
			String qualifiedName = String.valueOf(set.getEntityType());
			while (qualifiedName != null && visited.add(qualifiedName)) {
				TEntityType type = typesByQualifiedName.get(qualifiedName);
				if (type == null) {
					break;
				}
				qualifiedName = type.getBaseType() == null ? null : String.valueOf(type.getBaseType());
				for (TNavigationProperty navigation : type.getNavigationProperty()) {
					if (navigation.isContainsTarget() || bound.contains(navigation.getName())) {
						continue;
					}
					TEntitySet target = setByEntityType.get(unwrapCollection(
							String.valueOf(navigation.getType())));
					if (target == null) {
						continue;
					}
					TNavigationPropertyBinding binding = EdmFactory.eINSTANCE
							.createTNavigationPropertyBinding();
					binding.setPath(navigation.getName());
					binding.setTarget(target.getName());
					set.getNavigationPropertyBinding().add(binding);
					bound.add(navigation.getName());
				}
			}
		}
	}

	private static String unwrapCollection(String typeName) {
		return typeName.startsWith(EdmTypes.COLLECTION_OPEN) && typeName.endsWith(")")
				? typeName.substring(EdmTypes.COLLECTION_OPEN.length(), typeName.length() - 1)
				: typeName;
	}
}
