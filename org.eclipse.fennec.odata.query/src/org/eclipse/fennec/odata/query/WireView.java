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
package org.eclipse.fennec.odata.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.odata.wire.api.ControlNames;
import org.eclipse.fennec.odata.wire.api.WireProfile;
import org.eclipse.fennec.odata.wire.api.WirePropertyHandler;
import org.eclipse.fennec.odata.wire.mapping.ClassMapping;
import org.eclipse.fennec.odata.wire.mapping.WireMapping;
import org.eclipse.fennec.odata.wire.mapping.WireProperty;

/**
 * A root's view of its wire profile (ADR-0010, #119): which wire properties a type has, which
 * attributes they hide, and the alias of the key in queries. Built and checked once when the
 * profile binds; a mapping the runtime cannot serve fails the root.
 */
public final class WireView {

	/** A wire property with the handler that gives it meaning. */
	public record Binding(WireProperty property, WirePropertyHandler handler) {

		public String name() {
			return property.getName();
		}

		public List<EAttribute> parts() {
			return property.getParts();
		}
	}

	/** Plain OData: no wire properties, no key alias. */
	public static final WireView NONE = new WireView(List.of(), null, type -> List.of());

	private final List<ClassBindings> mapped;
	private final String keyAlias;
	private final Function<EClass, List<EAttribute>> keys;
	private final Map<EClass, Map<String, Binding>> byType = new ConcurrentHashMap<>();

	private record ClassBindings(EClass eClass, List<Binding> bindings) {
	}

	private WireView(List<ClassBindings> mapped, String keyAlias, Function<EClass, List<EAttribute>> keys) {
		this.mapped = mapped;
		this.keyAlias = keyAlias;
		this.keys = keys;
	}

	/**
	 * The view of a profile.
	 *
	 * @param keys the key attributes of an entity type (empty for a keyless type)
	 * @throws IllegalArgumentException when the mapping is broken: a property without name, parts
	 *         or handler, a part that is no attribute of the type, a name used twice or taken by
	 *         another feature of the type
	 */
	public static WireView of(WireProfile profile, Function<EClass, List<EAttribute>> keys) {
		ControlNames names = profile.controls();
		String keyAlias = ControlNames.ODATA.equals(names) ? null : names.keyValue();
		List<ClassBindings> mapped = new ArrayList<>();
		WireMapping mapping = profile.mapping().orElse(null);
		if (mapping != null) {
			for (ClassMapping classMapping : mapping.getClasses()) {
				EClass type = classMapping.getEClass();
				if (type == null) {
					throw new IllegalArgumentException("wire mapping: a class mapping without eClass");
				}
				List<Binding> bindings = new ArrayList<>();
				Set<String> seen = new HashSet<>();
				for (WireProperty property : classMapping.getProperties()) {
					String name = property.getName();
					String where = type.getName() + "." + name;
					if (name == null || name.isBlank()) {
						throw new IllegalArgumentException("wire mapping: a property of " + type.getName() + " without name");
					}
					if (!seen.add(name)) {
						throw new IllegalArgumentException("wire mapping: " + where + " is mapped twice");
					}
					if (property.getParts().isEmpty()) {
						throw new IllegalArgumentException("wire mapping: " + where + " has no parts");
					}
					for (EAttribute part : property.getParts()) {
						if (!type.getEAllAttributes().contains(part)) {
							throw new IllegalArgumentException("wire mapping: part " + part.getName() + " of " + where
									+ " is no attribute of " + type.getName());
						}
					}
					EStructuralFeature clash = type.getEStructuralFeature(name);
					if (clash != null && !property.getParts().contains(clash)) {
						throw new IllegalArgumentException("wire mapping: " + where + " hides the feature " + name);
					}
					WirePropertyHandler handler = profile.handler(property).orElseThrow(
							() -> new IllegalArgumentException("wire mapping: no handler for " + where));
					bindings.add(new Binding(property, handler));
				}
				mapped.add(new ClassBindings(type, List.copyOf(bindings)));
			}
		}
		return mapped.isEmpty() && keyAlias == null ? NONE : new WireView(List.copyOf(mapped), keyAlias, keys);
	}

	/** Whether the view changes nothing (plain OData). */
	public boolean isEmpty() {
		return mapped.isEmpty() && keyAlias == null;
	}

	/** The wire properties of a type, including those mapped on its supertypes, by name. */
	public Map<String, Binding> properties(EClass type) {
		if (mapped.isEmpty() || type == null) {
			return Map.of();
		}
		return byType.computeIfAbsent(type, t -> {
			Map<String, Binding> properties = new HashMap<>();
			for (ClassBindings classBindings : mapped) {
				if (classBindings.eClass() == t || classBindings.eClass().isSuperTypeOf(t)) {
					classBindings.bindings().forEach(binding -> properties.putIfAbsent(binding.name(), binding));
				}
			}
			return Map.copyOf(properties);
		});
	}

	/** The wire property of a type by its wire name, or null. */
	public Binding property(EClass type, String name) {
		return properties(type).get(name);
	}

	/** The wire property a feature is a part of, or null when the feature is addressable. */
	public Binding partOf(EClass type, EStructuralFeature feature) {
		for (Binding binding : properties(type).values()) {
			if (binding.parts().contains(feature)) {
				return binding;
			}
		}
		return null;
	}

	/**
	 * The key attribute a name stands for when it is the profile's key value name
	 * ({@code @iot.id}) and the type has a single key; null otherwise.
	 */
	public EAttribute keyAlias(EClass type, String name) {
		if (keyAlias == null || type == null || !keyAlias.equals(name)) {
			return null;
		}
		List<EAttribute> key = keys.apply(type);
		return key.size() == 1 ? key.get(0) : null;
	}

	/** The profile's key value name, or null. */
	public String keyAliasName() {
		return keyAlias;
	}
}
