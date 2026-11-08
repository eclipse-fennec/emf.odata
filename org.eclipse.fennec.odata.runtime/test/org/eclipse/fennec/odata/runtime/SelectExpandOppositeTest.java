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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.codec.util.MetadataServiceFactory;
import org.eclipse.fennec.emf.osgi.metadata.MetadataWhiteboard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * #112: navigations with an {@code eOpposite} (the SensorThings model is made of them) are
 * expanded inline, and expanding back over the opposite end yields a tree.
 */
@DisplayName("$select + $expand over bidirectional navigations")
class SelectExpandOppositeTest {

	private EPackage pkg;
	private EClass datastreamClass;
	private EClass thingClass;
	private EReference datastreamThing;
	private ODataServlet servlet;

	@BeforeEach
	void setUp() {
		EcoreFactory f = EcoreFactory.eINSTANCE;
		pkg = f.createEPackage();
		pkg.setName("sta");
		pkg.setNsPrefix("sta");
		pkg.setNsURI("https://example.org/test/sta");
		thingClass = eClass("Thing");
		datastreamClass = eClass("Datastream");
		datastreamThing = f.createEReference();
		datastreamThing.setName("Thing");
		datastreamThing.setEType(thingClass);
		datastreamThing.setLowerBound(1);
		datastreamClass.getEStructuralFeatures().add(datastreamThing);
		EReference thingDatastreams = f.createEReference();
		thingDatastreams.setName("Datastreams");
		thingDatastreams.setEType(datastreamClass);
		thingDatastreams.setUpperBound(-1);
		thingClass.getEStructuralFeatures().add(thingDatastreams);
		datastreamThing.setEOpposite(thingDatastreams);
		thingDatastreams.setEOpposite(datastreamThing);

		servlet = new ODataServlet();
		servlet.activate(Map.of());
		servlet.addEPackage(pkg);
		MetadataWhiteboard metadataService = MetadataServiceFactory.create();
		metadataService.registerPackage(pkg);
		servlet.setMetadataService(metadataService);
	}

	private EClass eClass(String name) {
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(name);
		EAttribute id = EcoreFactory.eINSTANCE.createEAttribute();
		id.setName("id");
		id.setEType(EcorePackage.Literals.ESTRING);
		id.setID(true);
		eClass.getEStructuralFeatures().add(id);
		EAttribute label = EcoreFactory.eINSTANCE.createEAttribute();
		label.setName("name");
		label.setEType(EcorePackage.Literals.ESTRING);
		eClass.getEStructuralFeatures().add(label);
		pkg.getEClassifiers().add(eClass);
		return eClass;
	}

	private EObject create(EClass type, String id, String name) {
		EObject object = pkg.getEFactoryInstance().create(type);
		object.eSet(type.getEStructuralFeature("id"), id);
		object.eSet(type.getEStructuralFeature("name"), name);
		return object;
	}

	private static ResponseFormatter.ExpandItem item(Map<String, ResponseFormatter.ExpandItem> nested) {
		return new ResponseFormatter.ExpandItem(CollectionOptions.NONE, false, null, 1, null, nested);
	}

	private String json(EObject datastream, String select, Map<String, ResponseFormatter.ExpandItem> expand)
			throws Exception {
		return servlet.formats.entityJson(datastream, datastreamClass,
				select == null ? null : SelectTree.parse(select, datastreamClass), expand, Map.of());
	}

	@Test
	@DisplayName("an expanded bidirectional navigation is written in full, with and without $select")
	void bidirectionalNavigationIsInlined() throws Exception {
		EObject thing = create(thingClass, "t1", "Pool");
		EObject datastream = create(datastreamClass, "d1", "temp");
		datastream.eSet(datastreamThing, thing);
		Map<String, ResponseFormatter.ExpandItem> expand = Map.of("Thing", item(Map.of()));

		assertThat(json(datastream, null, expand))
				.contains("\"Thing\":{\"id\":\"t1\",\"name\":\"Pool\"}").doesNotContain("_ref");
		assertThat(json(datastream, "name,Thing", expand))
				.contains("\"Thing\":{\"id\":\"t1\",\"name\":\"Pool\"}").doesNotContain("_ref");
	}

	@Test
	@DisplayName("expanding back over the opposite end is a tree, not a cycle")
	void backAndForthIsATree() throws Exception {
		ResourceSetImpl resourceSet = new ResourceSetImpl();
		Resource things = new ResourceImpl(URI.createURI("jpa://streams/Thing"));
		resourceSet.getResources().add(things);
		EObject thing = create(thingClass, "t1", "Pool");
		things.getContents().add(thing);
		EObject datastream = create(datastreamClass, "d1", "temp");
		EObject other = create(datastreamClass, "d2", "ph");
		datastream.eSet(datastreamThing, thing);
		other.eSet(datastreamThing, thing);

		String json = json(datastream, null,
				Map.of("Thing", item(Map.of("Datastreams", item(Map.of())))));

		assertThat(json).contains("\"Datastreams\":[{\"id\":\"d1\",\"name\":\"temp\"},"
				+ "{\"id\":\"d2\",\"name\":\"ph\"}]");
		// the backend objects are untouched: the copies are linked one way only
		assertThat(datastream.eGet(datastreamThing)).isSameAs(thing);
		assertThat(thing.eResource()).isSameAs(things);
	}
}
