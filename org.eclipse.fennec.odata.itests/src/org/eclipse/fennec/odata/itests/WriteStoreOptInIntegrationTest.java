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
package org.eclipse.fennec.odata.itests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.eclipse.fennec.odata.persistence.api.DeltaService;
import org.eclipse.fennec.odata.persistence.api.EntityRepository;
import org.eclipse.fennec.odata.persistence.api.MediaService;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The in-memory write store is OPT-IN (#149). Installing its bundle is not enough: without a
 * configuration for {@value #PID} the component must publish nothing at all.
 *
 * <p>Both halves of the original bug are asserted, because they are separate failures with one
 * cause. The component registers four services, and an ungated activation made a deliberately
 * read-only deployment
 * <ul>
 * <li><strong>writable</strong> — a {@code WriteService} appeared, so writes answered 201 where
 *     the deployment intended 405, bypassing its own write boundary entirely;</li>
 * <li><strong>and its reads polluted</strong> — the same component is an
 *     {@code EntityRepository}, which the in-memory QueryService aggregates, so whatever was
 *     POSTed came back inside the entity sets as if the real backend had supplied it.</li>
 * </ul>
 * The services are matched on {@code component.name}: other backends in this runtime (command,
 * repository) legitimately publish a {@code WriteService}, so "none at all" would be the wrong
 * assertion — the question is whether THIS component is bound.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@DisplayName("the in-memory write store is off until it is configured (#149)")
public class WriteStoreOptInIntegrationTest {

	static final String PID = "org.eclipse.fennec.odata.repository.memory";
	private static final String COMPONENT =
			"org.eclipse.fennec.odata.persistence.inmemory.write.MemoryWriteRepository";
	private static final String FILTER = "(component.name=" + COMPONENT + ")";

	private static final long TIMEOUT_MS = TimeUnit.SECONDS.toMillis(10);

	@InjectBundleContext
	BundleContext context;

	private Configuration configuration;

	@AfterEach
	void dropConfiguration() throws Exception {
		if (configuration != null) {
			configuration.delete();
			configuration = null;
			awaitBound(false);
		}
	}

	@Test
	@DisplayName("unconfigured: the store publishes no service — not even the EntityRepository")
	void unconfiguredPublishesNothing(@InjectService ConfigurationAdmin ignored) throws Exception {
		awaitBound(false);

		assertEquals(0, references(WriteService.class).size(), "a read-only deployment stays 405");
		assertEquals(0, references(MediaService.class).size());
		assertEquals(0, references(DeltaService.class).size());
		assertEquals(0, references(EntityRepository.class).size(),
				"and nothing of its contents can reach an entity set");
	}

	@Test
	@DisplayName("configured: all four services appear, and go away again when it is withdrawn")
	void configuredPublishesTheStore(@InjectService ConfigurationAdmin configurationAdmin)
			throws Exception {
		configuration = configurationAdmin.getConfiguration(PID, "?");
		configuration.update(new Hashtable<>()); // no property is read; presence is the switch
		awaitBound(true);

		assertEquals(1, references(WriteService.class).size());
		assertEquals(1, references(MediaService.class).size());
		assertEquals(1, references(DeltaService.class).size());
		assertEquals(1, references(EntityRepository.class).size());

		configuration.delete();
		configuration = null;
		awaitBound(false);
		assertEquals(0, references(WriteService.class).size(),
				"withdrawing the configuration takes the write boundary back down");
	}

	private Collection<ServiceReference<WriteService>> references() throws Exception {
		return context.getServiceReferences(WriteService.class, FILTER);
	}

	private <T> Collection<ServiceReference<T>> references(Class<T> type) throws Exception {
		return context.getServiceReferences(type, FILTER);
	}

	/** DS reacts to a configuration change asynchronously — poll rather than assume. */
	private void awaitBound(boolean expected) throws Exception {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			if (!references().isEmpty() == expected) {
				return;
			}
			Thread.sleep(50);
		}
		assertTrue(!references().isEmpty() == expected,
				"the write store should " + (expected ? "" : "not ") + "be bound within "
						+ TIMEOUT_MS + "ms");
	}
}
