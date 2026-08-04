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
package org.eclipse.fennec.odata.persistence.command;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test gating in the spirit of the upstream persistence TCK: an explicit
 * {@code -Dmongo.uri}/{@code MONGO_URI} wins; otherwise a throwaway container is
 * started via docker/podman ({@code -Dmongo.test.image}, default {@code mongo:7})
 * and torn down by a shutdown hook. {@code null} means: no MongoDB available —
 * callers skip via JUnit assumption.
 */
final class MongoSupport {

	private static final Logger LOG = Logger.getLogger(MongoSupport.class.getName());
	private static final String IMAGE = System.getProperty("mongo.test.image", "docker.io/library/mongo:7");

	private static volatile String uri;
	private static volatile String containerId;
	private static volatile String cli;
	private static volatile boolean initialized;

	private MongoSupport() {
	}

	static synchronized String connectionString() {
		if (initialized) {
			return uri;
		}
		initialized = true;
		String external = System.getProperty("mongo.uri", System.getenv("MONGO_URI"));
		if (external != null && !external.isBlank()) {
			uri = external.trim();
			return uri;
		}
		try {
			// stderr is merged into the output (podman's docker emulation logs a banner
			// there) — the container id is the LAST non-blank line, the mapping the last
			// line carrying a colon
			String id = exec(180, resolveCli(), "run", "-d", "--rm", "-p", "127.0.0.1::27017", IMAGE);
			containerId = lastLine(id, line -> !line.isBlank());
			// register the hook BEFORE anything else can fail — otherwise the container leaks
			Runtime.getRuntime().addShutdownHook(new Thread(MongoSupport::shutdown));
			String mapping = exec(20, resolveCli(), "port", containerId, "27017/tcp");
			String port = lastLine(mapping, line -> line.contains(":"));
			port = port.substring(port.lastIndexOf(':') + 1);
			uri = "mongodb://127.0.0.1:" + port;
		} catch (Exception e) {
			LOG.log(Level.INFO, "No MongoDB available for the command backend tests: " + e.getMessage());
			uri = null;
		}
		return uri;
	}

	private static String resolveCli() {
		if (cli != null) {
			return cli;
		}
		for (String candidate : new String[] { "docker", "podman" }) {
			try {
				exec(15, candidate, "version");
				cli = candidate;
				return cli;
			} catch (Exception e) {
				LOG.log(Level.FINE, () -> "Container CLI '" + candidate + "' not usable: " + e.getMessage());
			}
		}
		throw new IllegalStateException("no container CLI (docker/podman) available");
	}

	private static void shutdown() {
		if (containerId != null) {
			try {
				exec(30, resolveCli(), "stop", containerId);
			} catch (Exception e) {
				LOG.log(Level.FINE, () -> "Stopping the MongoDB container failed: " + e.getMessage());
			}
		}
	}

	private static String lastLine(String output, Predicate<String> filter) {
		return output.lines().map(String::trim).filter(filter)
				.reduce((first, second) -> second)
				.orElseThrow(() -> new IllegalStateException("unexpected output: " + output.trim()));
	}

	private static String exec(int timeoutSeconds, String... command) throws Exception {
		Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes());
		if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
			process.destroyForcibly();
			throw new IOException("timeout: " + String.join(" ", command));
		}
		if (process.exitValue() != 0) {
			throw new IOException(String.join(" ", command) + " failed: " + output.trim());
		}
		return output;
	}
}
