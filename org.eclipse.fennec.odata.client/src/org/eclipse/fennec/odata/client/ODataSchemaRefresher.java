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

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.fennec.odata.client.ODataSchemaManager.RefreshResult;
import org.eclipse.fennec.odata.schema.api.ODataSchemaResolver;
import org.eclipse.fennec.odata.schema.api.SchemaScope;

/**
 * The re-check trigger (ADR-0007): periodically (or on demand) re-reads every registered endpoint's
 * {@code $metadata} through the {@link ODataSchemaManager} — a conditional GET, so an unchanged
 * document is not re-downloaded and only a genuine change re-registers. Scheduling policy is left
 * to the deployment: use {@link #refreshAll()} from any scheduler, or {@link #start(Duration)} for
 * a built-in daemon timer.
 */
public final class ODataSchemaRefresher implements AutoCloseable {

	private static final System.Logger LOGGER = System.getLogger(ODataSchemaRefresher.class.getName());

	private final ODataSchemaManager manager;
	private final ODataSchemaResolver resolver;
	private ScheduledExecutorService scheduler;

	public ODataSchemaRefresher(ODataSchemaManager manager, ODataSchemaResolver resolver) {
		this.manager = manager;
		this.resolver = resolver;
	}

	/** Re-checks every registered endpoint once and returns the per-scope outcome. */
	public Map<SchemaScope, RefreshResult> refreshAll() {
		Map<SchemaScope, RefreshResult> results = new LinkedHashMap<>();
		for (SchemaScope scope : resolver.scopes()) {
			results.put(scope, manager.refresh(scope));
		}
		return results;
	}

	/** Starts periodic {@link #refreshAll()} on a daemon thread; {@link #close()} stops it. */
	public synchronized void start(Duration interval) {
		if (scheduler != null) {
			throw new IllegalStateException("the refresher is already running");
		}
		scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
			Thread thread = new Thread(runnable, "odata-schema-refresh");
			thread.setDaemon(true);
			return thread;
		});
		long millis = interval.toMillis();
		scheduler.scheduleWithFixedDelay(this::refreshQuietly, millis, millis, TimeUnit.MILLISECONDS);
	}

	private void refreshQuietly() {
		try {
			refreshAll();
		} catch (RuntimeException e) {
			// a transient endpoint failure must not kill the timer — log and try again next tick
			LOGGER.log(System.Logger.Level.WARNING, "scheduled schema refresh failed", e);
		}
	}

	@Override
	public synchronized void close() {
		if (scheduler != null) {
			scheduler.shutdownNow();
			scheduler = null;
		}
	}
}
