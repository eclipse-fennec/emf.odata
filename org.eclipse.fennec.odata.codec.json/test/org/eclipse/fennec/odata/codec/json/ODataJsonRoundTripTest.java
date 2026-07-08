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
package org.eclipse.fennec.odata.codec.json;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.codec.util.MetadataServiceFactory;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.model.metadata.api.MetadataWhiteboard;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * E3 round-trip test for the OData-JSON codec profile: OData keys ({@code @odata.type},
 * {@code @odata.id}) and the profile-driven {@code Edm.*} value formats ({@link EdmJsonValues}).
 * Plain-Java wiring (no OSGi): {@link MetadataServiceFactory} + the resource's local
 * {@code OdataResolver} fallback; the test model is a {@code .ecore} file, following the
 * codec examples' conventions.
 */
@DisplayName("OData-JSON codec profile round-trip")
class ODataJsonRoundTripTest {

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private MetadataWhiteboard metadataService;

	private EClass productClass;
	private EAttribute idAttr;
	private EAttribute nameAttr;
	private EAttribute priceAttr;
	private EAttribute releaseDateAttr;
	private EAttribute modifiedAttr;
	private EAttribute photoAttr;
	private EAttribute guidAttr;

	@BeforeEach
	void setUp() throws IOException {
		Path ecore = findResource("testdata/shop.ecore",
				"org.eclipse.fennec.odata.codec.json/testdata/shop.ecore");
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(ecore);
		EPackage.Registry.INSTANCE.put(pkg.getNsURI(), pkg);

		metadataService = MetadataServiceFactory.create();
		metadataService.registerPackage(pkg);

		productClass = EcoreHelper.getEClass(pkg, "Product");
		idAttr = (EAttribute) EcoreHelper.getFeature(productClass, "id");
		nameAttr = (EAttribute) EcoreHelper.getFeature(productClass, "name");
		priceAttr = (EAttribute) EcoreHelper.getFeature(productClass, "price");
		releaseDateAttr = (EAttribute) EcoreHelper.getFeature(productClass, "releaseDate");
		modifiedAttr = (EAttribute) EcoreHelper.getFeature(productClass, "modified");
		photoAttr = (EAttribute) EcoreHelper.getFeature(productClass, "photo");
		guidAttr = (EAttribute) EcoreHelper.getFeature(productClass, "guid");
	}

	@AfterEach
	void tearDown() {
		EPackage.Registry.INSTANCE.remove(pkg.getNsURI());
		ecoreHelper.releaseAll();
	}

	@Test
	@DisplayName("serializes OData keys and Edm.* value formats")
	void writesODataJson() throws IOException {
		String json = serialize(sampleProduct());

		assertTrue(json.contains("\"@odata.id\""), "OData id key: " + json);
		assertTrue(json.contains("\"@odata.type\""), "OData type key: " + json);
		assertTrue(json.contains("\"Product\""), "schema-qualified type info: " + json);

		assertTrue(json.contains("\"releaseDate\":\"2024-05-03\""), "Edm.Date = ISO date without time: " + json);
		assertTrue(json.contains("\"modified\":\"2024-05-03T10:15:30Z\""), "Edm.DateTimeOffset = ISO instant: " + json);
		assertTrue(json.contains("\"photo\":\"-_A\""), "Edm.Binary = base64url without padding: " + json);
		assertTrue(json.contains("19.99"), "Edm.Decimal stays a JSON number: " + json);
		assertTrue(json.contains("\"guid\":\"f89dee73-af9f-4cd4-b330-db93c25ff3c7\""), "Edm.Guid passthrough: " + json);
	}

	@Test
	@DisplayName("minimal metadata: no codec control fields, key property stays")
	void minimalMetadataHasNoControlFields() throws IOException {
		ODataJsonResourceImpl resource = ODataJsonResourceImpl.minimalMetadata(
				URI.createURI("test://product-minimal.odatajson"), metadataService, Set.of());
		resource.getContents().add(sampleProduct());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, null);
		String json = out.toString(StandardCharsets.UTF_8);

		assertFalse(json.contains("\"_id\""), "codec-internal id field must not leak: " + json);
		assertFalse(json.contains("\"@odata.id\""), "computable control info is omitted: " + json);
		assertTrue(json.contains("\"id\":\"p1\""), "the key PROPERTY stays: " + json);
	}

	@Test
	@DisplayName("round-trips Edm.* values back to the attribute types")
	void roundTripsValues() throws IOException {
		String json = serialize(sampleProduct());

		ODataJsonResourceImpl loadResource = new ODataJsonResourceImpl(
				URI.createURI("test://product-load.odatajson"), metadataService);
		Map<Object, Object> options = new HashMap<>();
		options.put(CodecResource.CODEC_ROOT_TYPE, productClass);
		loadResource.load(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), options);

		EObject loaded = loadResource.getContents().get(0);
		assertNotNull(loaded);
		assertEquals("p1", loaded.eGet(idAttr));
		assertEquals("Widget", loaded.eGet(nameAttr));
		assertEquals(0, new BigDecimal("19.99").compareTo((BigDecimal) loaded.eGet(priceAttr)));
		// Edm.Date deliberately drops the time part -> start of day UTC
		assertEquals(Date.from(Instant.parse("2024-05-03T00:00:00Z")), loaded.eGet(releaseDateAttr));
		assertEquals(Date.from(Instant.parse("2024-05-03T10:15:30Z")), loaded.eGet(modifiedAttr));
		assertArrayEquals(new byte[] { (byte) 0xFB, (byte) 0xF0 }, (byte[]) loaded.eGet(photoAttr));
		assertEquals("f89dee73-af9f-4cd4-b330-db93c25ff3c7", loaded.eGet(guidAttr));
	}

	@Test
	@DisplayName("a malformed Edm.Date value fails with a typed error, not a raw crash")
	void malformedDateRejected() {
		String json = "{\"@odata.type\":\"#" + productClass.getName()
				+ "\",\"id\":\"p1\",\"name\":\"Widget\",\"releaseDate\":\"2024-13-99\"}";
		ODataJsonResourceImpl resource = new ODataJsonResourceImpl(
				URI.createURI("test://bad-date.odatajson"), metadataService);
		Map<Object, Object> options = new HashMap<>();
		options.put(CodecResource.CODEC_ROOT_TYPE, productClass);
		assertThrows(IllegalArgumentException.class, () -> resource.load(
				new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), options));
	}

	@Test
	@DisplayName("Int64 / TimeOfDay / Duration / enum / collection value formats round-trip")
	@SuppressWarnings("unchecked")
	void extendedValueFormats() throws IOException {
		EObject product = pkg.getEFactoryInstance().create(productClass);
		product.eSet(idAttr, "p9");
		long bigSerial = 9007199254740993L; // 2^53 + 1 — NOT representable as an IEEE754 double
		product.eSet(feature("serial"), bigSerial);
		product.eSet(feature("openingTime"), Date.from(Instant.parse("1970-01-01T08:30:00Z")));
		product.eSet(feature("warranty"), "P1Y6M");
		org.eclipse.emf.ecore.EEnum colorEnum = (org.eclipse.emf.ecore.EEnum) pkg.getEClassifier("Color");
		product.eSet(feature("color"), colorEnum.getEEnumLiteral("Green").getInstance());
		((java.util.List<Object>) product.eGet(feature("tags"))).addAll(java.util.List.of("a", "b"));

		String json = serialize(product);
		assertTrue(json.contains("\"warranty\":\"P1Y6M\""), "Edm.Duration passes through: " + json);
		assertTrue(json.contains("\"openingTime\":\"08:30\""), "Edm.TimeOfDay (seconds omitted): " + json);
		assertTrue(json.contains("\"tags\":[\"a\",\"b\"]"), "collection of primitives: " + json);
		assertTrue(json.contains("\"color\":\"Green\""), "enum by member NAME, not ordinal: " + json);
		// Int64: currently a bare JSON number. Exact for a Java/long client; a JavaScript/double
		// client loses precision above 2^53 — the IEEE754Compatible string form is a known gap.
		assertTrue(json.contains("\"serial\":9007199254740993"), "Int64 wire form: " + json);

		EObject loaded = load(json);
		assertEquals(bigSerial, ((Number) loaded.eGet(feature("serial"))).longValue(),
				"Int64 survives the round-trip exactly at the Java level");
		assertEquals(Date.from(Instant.parse("1970-01-01T08:30:00Z")), loaded.eGet(feature("openingTime")));
		assertEquals("P1Y6M", loaded.eGet(feature("warranty")));
		assertEquals(java.util.List.of("a", "b"), loaded.eGet(feature("tags")));
		assertEquals("Green", String.valueOf(loaded.eGet(feature("color"))));
	}

	@Test
	@DisplayName("absent optional values are omitted from the payload and stay unset on read")
	void nullValuesOmitted() throws IOException {
		EObject product = pkg.getEFactoryInstance().create(productClass);
		product.eSet(idAttr, "p10");
		product.eSet(nameAttr, "Bare");
		// price / releaseDate / photo / serial left unset
		String json = serialize(product);
		assertFalse(json.contains("\"price\""), "unset value is omitted, not null: " + json);
		assertFalse(json.contains("\"releaseDate\""), json);

		EObject loaded = load(json);
		assertFalse(loaded.eIsSet(priceAttr), "an omitted value stays unset after decode");
		assertEquals("Bare", loaded.eGet(nameAttr));
	}

	// === helpers ===

	private EAttribute feature(String name) {
		return (EAttribute) EcoreHelper.getFeature(productClass, name);
	}

	private EObject load(String json) throws IOException {
		ODataJsonResourceImpl resource = new ODataJsonResourceImpl(
				URI.createURI("test://load.odatajson"), metadataService);
		Map<Object, Object> options = new HashMap<>();
		options.put(CodecResource.CODEC_ROOT_TYPE, productClass);
		resource.load(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)), options);
		return resource.getContents().get(0);
	}

	private EObject sampleProduct() {
		EObject product = pkg.getEFactoryInstance().create(productClass);
		product.eSet(idAttr, "p1");
		product.eSet(nameAttr, "Widget");
		product.eSet(priceAttr, new BigDecimal("19.99"));
		product.eSet(releaseDateAttr, Date.from(Instant.parse("2024-05-03T10:15:30Z")));
		product.eSet(modifiedAttr, Date.from(Instant.parse("2024-05-03T10:15:30Z")));
		product.eSet(photoAttr, new byte[] { (byte) 0xFB, (byte) 0xF0 });
		product.eSet(guidAttr, "f89dee73-af9f-4cd4-b330-db93c25ff3c7");
		return product;
	}

	private String serialize(EObject object) throws IOException {
		ODataJsonResourceImpl resource = new ODataJsonResourceImpl(
				URI.createURI("test://product.odatajson"), metadataService);
		resource.getContents().add(object);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, null);
		return out.toString(StandardCharsets.UTF_8);
	}

	private static Path findResource(String... candidatesRelative) {
		Path start = Paths.get("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidatesRelative) {
				Path p = dir.resolve(rel);
				if (Files.exists(p)) {
					return p;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
