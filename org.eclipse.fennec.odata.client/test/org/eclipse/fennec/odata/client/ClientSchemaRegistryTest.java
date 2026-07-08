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
package org.eclipse.fennec.odata.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.odata.schema.api.ODataSchema;
import org.eclipse.fennec.odata.schema.api.ODataSchemaReader;
import org.eclipse.fennec.odata.schema.api.SchemaScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The decoupled schema registry/manager (ADR-0007): registration, change-detection and the
 * data-path lookup are exercised WITHOUT the network — a fake reader stands in for the HTTP
 * $metadata fetch, so only the wiring/logic is under test.
 */
@DisplayName("Client schema registry: register / refresh / data-path lookup")
class ClientSchemaRegistryTest {

	private static final SchemaScope SCOPE = SchemaScope.of("http://host/odata");

	private EPackage webshop;
	private final HttpClient http = HttpClient.newHttpClient();

	@BeforeEach
	void loadModel() {
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource resource = rs.createResource(URI.createFileURI(findResource(
				"testdata/webshop.ecore",
				"org.eclipse.fennec.odata.client/testdata/webshop.ecore").toString()));
		try {
			resource.load(null);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		webshop = (EPackage) resource.getContents().get(0);
	}

	private ODataSchema schema(String hash) {
		return new ODataSchema(SCOPE, List.of(webshop), hash, Optional.empty(), Optional.empty());
	}

	@Test
	@DisplayName("register / lookup / version / remove round-trip in the default registry")
	void registryRoundTrip() {
		EPackageSchemaRegistry registry = new EPackageSchemaRegistry();
		assertTrue(registry.lookup(SCOPE).isEmpty(), "nothing registered yet");

		registry.register(schema("h1"));
		assertTrue(registry.lookup(SCOPE).isPresent());
		assertEquals("h1", registry.version(SCOPE).orElseThrow().contentHash());

		registry.remove(SCOPE);
		assertTrue(registry.lookup(SCOPE).isEmpty(), "removed again");
	}

	@Test
	@DisplayName("manager: onRegister persists; refresh re-registers only on a changed hash")
	void managerLifecycle() {
		FakeReader reader = new FakeReader();
		EPackageSchemaRegistry registry = new EPackageSchemaRegistry();
		ODataSchemaManager manager = new ODataSchemaManager(reader, registry, registry);

		reader.next = schema("h1");
		manager.onRegister(SCOPE);
		assertEquals("h1", registry.version(SCOPE).orElseThrow().contentHash());

		// same content → no re-registration
		assertEquals(ODataSchemaManager.RefreshResult.UNCHANGED, manager.refresh(SCOPE));
		// server reports unchanged (304)
		reader.notModified = true;
		assertEquals(ODataSchemaManager.RefreshResult.UNCHANGED, manager.refresh(SCOPE));
		// genuinely changed content → UPDATED
		reader.notModified = false;
		reader.next = schema("h2");
		assertEquals(ODataSchemaManager.RefreshResult.UPDATED, manager.refresh(SCOPE));
		assertEquals("h2", registry.version(SCOPE).orElseThrow().contentHash());
		// unknown scope
		assertEquals(ODataSchemaManager.RefreshResult.NOT_FOUND,
				manager.refresh(SchemaScope.of("http://host/other")));
	}

	@Test
	@DisplayName("data path: fail-fast when the endpoint was never registered")
	void forEndpointFailsFast() {
		EPackageSchemaRegistry empty = new EPackageSchemaRegistry();
		ODataClientException error = assertThrows(ODataClientException.class,
				() -> ODataClient.forEndpoint(SCOPE, empty, http));
		assertTrue(error.getMessage().contains("not registered"), error.getMessage());
	}

	@Test
	@DisplayName("data path: a registered schema resolves to a usable data client (no fetch)")
	void forEndpointResolvesFromRegistry() {
		EPackageSchemaRegistry registry = new EPackageSchemaRegistry();
		registry.register(schema("h1"));
		ODataClient client = ODataClient.forEndpoint(SCOPE, registry, http);
		assertNotNull(client.entityType("Product"), "the set→EClass mapping comes from the registry");
	}

	@Test
	@DisplayName("data path: lazy policy registers on first use via the manager")
	void forEndpointLazyRegisters() {
		FakeReader reader = new FakeReader();
		reader.next = schema("h1");
		EPackageSchemaRegistry registry = new EPackageSchemaRegistry();
		ODataSchemaManager manager = new ODataSchemaManager(reader, registry, registry);

		ODataClient client = ODataClient.forEndpoint(SCOPE, manager, http);
		assertNotNull(client.entityType("Product"));
		assertEquals(1, reader.reads, "the schema was read exactly once and then registered");
		assertFalse(registry.lookup(SCOPE).isEmpty(), "and left registered for the next call");
	}

	@Test
	@DisplayName("client factory builds a data client for a registered endpoint via the resolver")
	void clientFactoryBuildsFromResolver() {
		EPackageSchemaRegistry registry = new EPackageSchemaRegistry();
		registry.register(schema("h1"));
		try (DefaultODataClientFactory factory = new DefaultODataClientFactory(registry)) {
			ODataClient client = factory.forEndpoint(SCOPE);
			assertNotNull(client.entityType("Product"), "the factory resolves the schema and wires a client");
		}
	}

	@Test
	@DisplayName("refresher re-checks every registered endpoint and re-registers changed schemas")
	void refresherReChecksAll() {
		FakeReader reader = new FakeReader();
		EPackageSchemaRegistry registry = new EPackageSchemaRegistry();
		ODataSchemaManager manager = new ODataSchemaManager(reader, registry, registry);
		reader.next = schema("h1");
		manager.onRegister(SCOPE);

		reader.next = schema("h2"); // the endpoint changed since registration
		try (ODataSchemaRefresher refresher = new ODataSchemaRefresher(manager, registry)) {
			Map<SchemaScope, ODataSchemaManager.RefreshResult> results = refresher.refreshAll();
			assertEquals(ODataSchemaManager.RefreshResult.UPDATED, results.get(SCOPE));
		}
		assertEquals("h2", registry.version(SCOPE).orElseThrow().contentHash(),
				"the changed schema was re-registered");
	}

	/** Stands in for the HTTP $metadata reader — returns a preset schema, no network. */
	private static final class FakeReader implements ODataSchemaReader {
		private ODataSchema next;
		private boolean notModified;
		private int reads;

		@Override
		public Optional<ODataSchema> read(SchemaScope scope, Conditional conditional) {
			reads++;
			return notModified ? Optional.empty() : Optional.of(next);
		}

		@Override
		public ODataSchema read(SchemaScope scope, String csdl) {
			return next;
		}
	}

	private static Path findResource(String... candidatesRelative) {
		Path start = Paths.get("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidatesRelative) {
				Path candidate = dir.resolve(rel);
				if (Files.exists(candidate)) {
					return candidate;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
