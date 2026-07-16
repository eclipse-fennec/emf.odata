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
package org.eclipse.fennec.odata.vocabularies;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.fennec.odata.csdl.CsdlXmlLoad;
import org.eclipse.fennec.odata.csdl.EdmToEcoreConverter;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;

/**
 * The OASIS standard vocabularies as {@link EPackage}s (E1, req §3.4/Q5): the vendored CSDL
 * definitions (bundled next to this class) are loaded into the EDM model and bootstrapped
 * through the CSDL read path ({@link EdmToEcoreConverter}). Structured vocabulary types
 * (ComplexType/EnumType/TypeDefinition) become EClasses/EEnums/EDataTypes; the {@code <Term>}
 * declarations become one {@code EAnnotation} per term on the package
 * ({@code ODataAnnotationConstants.TERM_SOURCE_PREFIX + termName}).
 *
 * <p>Packages are resolved lazily and cached; {@link ODataVocabulariesComponent} registers them
 * as OSGi services so the Model Metadata Service whiteboard picks them up.
 */
public final class ODataVocabularies {

	public static final String CORE = "Org.OData.Core.V1";
	public static final String CAPABILITIES = "Org.OData.Capabilities.V1";
	public static final String VALIDATION = "Org.OData.Validation.V1";
	public static final String MEASURES = "Org.OData.Measures.V1";

	private static final Map<String, EPackage> CACHE = new ConcurrentHashMap<>();

	/** All vendored OASIS vocabulary namespaces. */
	public static List<String> all() {
		return List.of(CORE, CAPABILITIES, VALIDATION, MEASURES);
	}

	/** The vocabulary EPackage for one of the {@link #all()} namespaces. */
	public static EPackage getEPackage(String namespace) {
		EPackage cached = CACHE.get(namespace);
		if (cached != null) {
			return cached;
		}
		loadAll();
		EPackage pkg = CACHE.get(namespace);
		if (pkg == null) {
			throw new IllegalArgumentException("no vendored vocabulary for namespace " + namespace);
		}
		return pkg;
	}

	/**
	 * Bootstraps ALL vocabularies in dependency order (Core first — the other vocabularies
	 * type their terms and properties with Core types) and resolves each package's pending
	 * cross-vocabulary references against the ones loaded before it. Whatever cannot be
	 * resolved is removed by the converter — a feature without a type is invalid Ecore and
	 * breaks the metadata whiteboard registration.
	 */
	private static synchronized void loadAll() {
		if (CACHE.size() == all().size()) {
			return;
		}
		EdmToEcoreConverter converter = new EdmToEcoreConverter();
		Map<String, EPackage> known = new HashMap<>();
		for (String namespace : List.of(CORE, MEASURES, VALIDATION, CAPABILITIES)) {
			EPackage pkg = CACHE.get(namespace);
			if (pkg == null) {
				pkg = load(namespace);
				converter.resolveReferences(pkg, known);
				CACHE.put(namespace, pkg);
			}
			known.put(namespace, pkg);
			known.put(pkg.getName(), pkg); // vocabulary refs use the ALIAS form (Core.Tag)
		}
	}

	private static EPackage load(String namespace) {
		try (InputStream in = ODataVocabularies.class.getResourceAsStream(namespace + ".xml")) {
			if (in == null) {
				throw new IllegalArgumentException("no vendored vocabulary for namespace " + namespace);
			}
			ResourceSet rs = new ResourceSetImpl();
			rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
			rs.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
			rs.getPackageRegistry().put(EdmxPackage.eNS_URI, EdmxPackage.eINSTANCE);

			Resource res = rs.createResource(URI.createURI(namespace + ".xml"));
			CsdlXmlLoad.loadSecurely(res, in); // XXE-hardened + depth-guarded, shared load path
			if (!res.getErrors().isEmpty()) {
				throw new IllegalStateException("vocabulary " + namespace + " did not parse cleanly: "
						+ res.getErrors().get(0).getMessage());
			}

			EObject root = res.getContents().get(0);
			TEdmx edmx = (root instanceof EdmxRoot er) ? er.getEdmx() : (TEdmx) root;
			SchemaType schema = edmx.getDataServices().getSchema().get(0);
			return new EdmToEcoreConverter().toEPackage(schema);
		} catch (IOException e) {
			throw new UncheckedIOException("failed to load vocabulary " + namespace, e);
		}
	}

	private ODataVocabularies() {
	}
}
