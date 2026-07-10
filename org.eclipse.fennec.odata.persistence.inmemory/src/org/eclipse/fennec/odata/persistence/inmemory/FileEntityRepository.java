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
package org.eclipse.fennec.odata.persistence.inmemory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

import org.eclipse.fennec.odata.persistence.api.EntityRepository;

/**
 * File-backed {@link EntityRepository}: loads all {@code *.xmi}/{@code *.xml} resources from
 * ONE configured directory at activation and serves their contents grouped by EClass. This is
 * the "data from files" face of the persistence abstraction — the query layer never knows.
 *
 * <p>Security: the directory comes exclusively from the component configuration
 * ({@code directory} property, factory PID {@value #PID}) — request input NEVER influences
 * file-system access; entity-set names only select EClasses of already-loaded packages.
 * The directory is read once at activation (no reload on request paths).
 */
@Component(service = EntityRepository.class, configurationPid = FileEntityRepository.PID,
		configurationPolicy = ConfigurationPolicy.REQUIRE)
public class FileEntityRepository implements EntityRepository {

	public static final String PID = "org.eclipse.fennec.odata.repository.file";

	private final Map<EClass, List<EObject>> byType = new HashMap<>();
	private final ResourceSet resourceSet; // keeps the loaded resources (and their objects) alive

	/** DS constructor: configuration must provide {@code directory}. */
	@Activate
	public FileEntityRepository(Map<String, Object> configuration) {
		this(Path.of(String.valueOf(configuration.get("directory"))), null);
	}

	/**
	 * Plain-Java/test constructor.
	 *
	 * @param directory the data directory to scan (non-recursive)
	 * @param packages  packages to register locally for parsing, or null when the global/OSGi
	 *                  registry already knows them
	 */
	public FileEntityRepository(Path directory, List<EPackage> packages) {
		if (!Files.isDirectory(directory)) {
			throw new IllegalArgumentException("not a directory: " + directory);
		}
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		if (packages != null) {
			packages.forEach(p -> resourceSet.getPackageRegistry().put(p.getNsURI(), p));
		}
		try (Stream<Path> files = Files.list(directory)) {
			files.filter(f -> {
				String name = f.getFileName().toString();
				return name.endsWith(".xmi") || name.endsWith(".xml");
			}).sorted().forEach(this::load);
		} catch (IOException e) {
			throw new UncheckedIOException("cannot read data directory " + directory, e);
		}
	}

	private void load(Path file) {
		try {
			Resource resource = resourceSet.createResource(URI.createFileURI(file.toString()));
			resource.load(null);
			if (!resource.getErrors().isEmpty()) {
				throw new IllegalArgumentException("data file " + file.getFileName() + " did not load cleanly: "
						+ resource.getErrors().get(0).getMessage());
			}
			for (TreeIterator<EObject> it = resource.getAllContents(); it.hasNext();) {
				EObject entity = it.next();
				// polymorphic like the JPA backend: a derived instance is part of every
				// base type's set (queries on the base set see DiscountedProducts too)
				byType.computeIfAbsent(entity.eClass(), c -> new ArrayList<>()).add(entity);
				for (EClass superType : entity.eClass().getEAllSuperTypes()) {
					byType.computeIfAbsent(superType, c -> new ArrayList<>()).add(entity);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("cannot load data file " + file, e);
		}
	}

	@Override
	public boolean supplies(EClass entityType) {
		return byType.containsKey(entityType);
	}

	@Override
	public List<EObject> entities(EClass entityType) {
		return List.copyOf(byType.getOrDefault(entityType, List.of()));
	}
}
