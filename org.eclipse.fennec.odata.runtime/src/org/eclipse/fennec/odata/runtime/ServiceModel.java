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
package org.eclipse.fennec.odata.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.odata.csdl.ODataAnnotationConstants;

/**
 * The entity data model ONE service root publishes ([OData-Protocol] §3, [OData-CSDL] Part 3):
 * the schemas, the container's entity sets (name → type) and its singletons. Computed from the
 * {@code EPackage}s bound to a servlet instance and that instance's configuration, and recomputed
 * whenever either changes — so every consumer ({@code $metadata}, the service document, entity-set
 * routing, cast and operation resolution) sees the same selection.
 *
 * <p>Configuration (both keys optional; empty/absent = "everything bound", the pre-allowlist
 * behaviour):
 * <ul>
 *   <li>{@code odata.model.packages} — nsURIs of the packages to publish. A bound package outside
 *       the list contributes NO schema, entity set, singleton, cast type or operation.</li>
 *   <li>{@code odata.model.entitysets} — entries {@code [SetName=]nsURI#EClass} or
 *       {@code [SetName=]EClass}: exactly these entity sets are published (in the container and
 *       for routing), under the configured name where one is given. Without the list every
 *       concrete class of every published package is a set, named by the package's
 *       {@link ODataAnnotationConstants#ENTITY_SETS_SOURCE} rename annotation or its type name.</li>
 * </ul>
 * Both keys accept a {@code String[]}, a {@link Collection} or a single comma/whitespace separated
 * string. An entity-set entry that resolves to no (published) class is skipped with a warning:
 * the packages arrive dynamically, so the entry resolves once its package is bound.
 */
final class ServiceModel {

	static final String PACKAGES_KEY = "odata.model.packages";
	static final String ENTITY_SETS_KEY = "odata.model.entitysets";

	private static final System.Logger LOGGER = System.getLogger(ServiceModel.class.getName());

	/** The parsed allowlist configuration; {@link #ALL} publishes everything bound. */
	record Selection(Set<String> nsUris, List<String> entitySets) {

		static final Selection ALL = new Selection(Set.of(), List.of());

		static Selection fromConfiguration(Map<String, Object> configuration) {
			if (configuration == null) {
				return ALL;
			}
			return new Selection(new LinkedHashSet<>(values(configuration.get(PACKAGES_KEY))),
					values(configuration.get(ENTITY_SETS_KEY)));
		}

		/** Values of a multi-valued property: array, collection or one separated string. */
		static List<String> values(Object value) {
			List<String> values = new ArrayList<>();
			if (value == null) {
				return values;
			}
			Collection<?> raw = value instanceof Collection<?> c ? c
					: value instanceof Object[] a ? List.of(a)
					: List.of(value);
			for (Object item : raw) {
				if (item == null) {
					continue;
				}
				for (String token : String.valueOf(item).split("[,\\s]+")) {
					if (!token.isBlank()) {
						values.add(token.trim());
					}
				}
			}
			return values;
		}

		boolean restrictsPackages() {
			return !nsUris.isEmpty();
		}

		boolean restrictsEntitySets() {
			return !entitySets.isEmpty();
		}
	}

	private final List<EPackage> packages;
	/** set name → type, in set-name order (the service document lists them sorted). */
	private final Map<String, EClass> entitySets;
	private final Map<EClass, String> setNames;
	private final Map<String, EClass> singletons;

	private ServiceModel(List<EPackage> packages, Map<String, EClass> entitySets,
			Map<EClass, String> setNames, Map<String, EClass> singletons) {
		this.packages = List.copyOf(packages);
		this.entitySets = Collections.unmodifiableMap(entitySets);
		this.setNames = Collections.unmodifiableMap(setNames);
		this.singletons = Collections.unmodifiableMap(singletons);
	}

	/** The model over the bound packages with nothing filtered — what an unconfigured root serves. */
	static ServiceModel of(List<EPackage> bound) {
		return of(bound, Selection.ALL);
	}

	static ServiceModel of(List<EPackage> bound, Selection selection) {
		List<EPackage> published = new ArrayList<>();
		for (EPackage pkg : bound) {
			if (!selection.restrictsPackages() || selection.nsUris().contains(pkg.getNsURI())) {
				published.add(pkg);
			}
		}
		// binding order is arbitrary (dynamic references) — the first package hosts the service's
		// one entity container, so the order must survive a restart: configured order, else nsURI
		List<String> configured = List.copyOf(selection.nsUris());
		published.sort(selection.restrictsPackages()
				? Comparator.comparingInt(pkg -> configured.indexOf(pkg.getNsURI()))
				: Comparator.comparing(EPackage::getNsURI, Comparator.nullsLast(String::compareTo)));
		// the package rename annotations first (set name → type); set renames are declared per
		// package but the container may live in another schema than its types (Northwind), so the
		// renames of all published packages apply everywhere
		Map<String, EClass> sets = new TreeMap<>();
		if (selection.restrictsEntitySets()) {
			for (String entry : selection.entitySets()) {
				int equals = entry.indexOf('=');
				String configuredName = equals > 0 ? entry.substring(0, equals).trim() : null;
				String reference = equals > 0 ? entry.substring(equals + 1).trim() : entry;
				EClass type = resolve(reference, published);
				if (type == null) {
					LOGGER.log(System.Logger.Level.WARNING,
							() -> ENTITY_SETS_KEY + " entry '" + entry
									+ "' names no concrete entity type of a published package — skipped"
									+ " (it resolves once its package is bound)");
					continue;
				}
				String name = configuredName != null ? configuredName : annotatedSetName(type, published);
				putSet(sets, name, type);
			}
		} else {
			for (EPackage pkg : published) {
				for (var classifier : pkg.getEClassifiers()) {
					if (classifier instanceof EClass type && !type.isAbstract()) {
						putSet(sets, annotatedSetName(type, published), type);
					}
				}
			}
		}
		Map<EClass, String> names = new LinkedHashMap<>();
		sets.forEach((name, type) -> names.putIfAbsent(type, name));

		Map<String, EClass> singletons = new LinkedHashMap<>();
		for (EPackage pkg : published) {
			EAnnotation annotation = pkg.getEAnnotation(ODataAnnotationConstants.SINGLETONS_SOURCE);
			if (annotation == null) {
				continue;
			}
			annotation.getDetails().forEach(detail -> {
				if (pkg.getEClassifier(detail.getValue()) instanceof EClass type
						&& (!selection.restrictsEntitySets() || names.containsKey(type))) {
					singletons.put(detail.getKey(), type);
				}
			});
		}
		return new ServiceModel(published, sets, names, singletons);
	}

	/** Registers a set; a name already serving ANOTHER type keeps its first type, with a warning. */
	private static void putSet(Map<String, EClass> sets, String name, EClass type) {
		EClass existing = sets.putIfAbsent(name, type);
		if (existing != null && existing != type) {
			LOGGER.log(System.Logger.Level.WARNING, () -> "entity set '" + name + "' would serve both "
					+ existing.getEPackage().getNsURI() + "#" + existing.getName() + " and "
					+ type.getEPackage().getNsURI() + "#" + type.getName()
					+ " — the second is not published; rename it in " + ENTITY_SETS_KEY);
		}
	}

	/** {@code nsURI#Name} or a bare {@code Name} → the concrete class among the published packages. */
	private static EClass resolve(String reference, List<EPackage> published) {
		int hash = reference.indexOf('#');
		String nsUri = hash >= 0 ? reference.substring(0, hash) : null;
		String localName = hash >= 0 ? reference.substring(hash + 1) : reference;
		for (EPackage pkg : published) {
			if (nsUri != null && !nsUri.equals(pkg.getNsURI())) {
				continue;
			}
			if (pkg.getEClassifier(localName) instanceof EClass type && !type.isAbstract()) {
				return type;
			}
		}
		return null;
	}

	/** The set name the packages' rename annotations declare for the type, or its type name. */
	private static String annotatedSetName(EClass type, List<EPackage> published) {
		for (EPackage pkg : published) {
			EAnnotation sets = pkg.getEAnnotation(ODataAnnotationConstants.ENTITY_SETS_SOURCE);
			if (sets == null) {
				continue;
			}
			for (Map.Entry<String, String> entry : sets.getDetails()) {
				if (entry.getValue().equals(type.getName())) {
					return entry.getKey();
				}
			}
		}
		return type.getName();
	}

	/** The published packages — one Schema each — in configured order, else by nsURI. */
	List<EPackage> packages() {
		return packages;
	}

	/** The container's entity-set names, sorted. */
	List<String> entitySetNames() {
		return List.copyOf(entitySets.keySet());
	}

	/** The entity type behind a published set name, or null (→ 404, whether unknown or unpublished). */
	EClass entityType(String setName) {
		return entitySets.get(setName);
	}

	/** True when the type is served as an entity set of this root. */
	boolean publishes(EClass type) {
		return setNames.containsKey(type);
	}

	/**
	 * The set name serving the type — honours configured and annotated renames. A type without a
	 * set (reached through navigation only) answers with its type name, as a plain OData service
	 * would for an unbound navigation target.
	 */
	String setNameOf(EClass type) {
		return setNames.getOrDefault(type, type.getName());
	}

	/** {@code type name → set name} for every published set — the $metadata rename map. */
	Map<String, String> typeToSetNames() {
		Map<String, String> renames = new LinkedHashMap<>();
		setNames.forEach((type, name) -> renames.put(type.getName(), name));
		return renames;
	}

	/** The container singletons of this root ([OData-CSDL] 13.5), name → type. */
	Map<String, EClass> singletons() {
		return singletons;
	}
}
