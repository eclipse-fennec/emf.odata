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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The respond-async arm of {@link ODataServlet} ([OData-Protocol] 11.6, {@code Prefer:
 * respond-async}): accepts a GET onto a virtual-thread worker with an immediate 202, parks the
 * finished result behind a one-shot {@code /$async/<id>} status monitor (bounded LRU), and
 * enforces the configured in-flight cap. Extracted dispatcher — the worker re-enters the
 * servlet through {@link ODataServlet#dispatchDirectly}, bypassing the async trigger.
 */
final class AsyncDispatcher {

	private static final System.Logger LOGGER = System.getLogger(AsyncDispatcher.class.getName());

	private final ODataServlet servlet;

	AsyncDispatcher(ODataServlet servlet) {
		this.servlet = servlet;
	}


/** A completed response parked behind its status monitor ({@code /$async/<id>}). */
private record AsyncResult(int status, Map<String, String> headers, byte[] body) {
}

/**
 * One virtual thread per async request (Java 21): the worker mostly waits on the backend, so
 * threads are cheap and no pool sizing is needed. Daemon by nature — a forgotten shutdown
 * never blocks the framework.
 */
private final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();

/** Running and parked async executions, bounded LRU — unclaimed monitors age out (cancelled). */
private final Map<String, Future<AsyncResult>> asyncResults =
		Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Future<AsyncResult>> eldest) {
				// servlet.maxAsyncMonitors <= 0 means unbounded parking (never evict) — a foot-gun
				if (servlet.maxAsyncMonitors > 0 && size() > servlet.maxAsyncMonitors) {
					eldest.getValue().cancel(true); // releases the in-flight permit if still running
					return true;
				}
				return false;
			}
		});

/** Whether the client sent {@code Prefer: respond-async} ([OData-Protocol] 8.2.8.8). */
static boolean requested(HttpServletRequest request) {
	String prefer = request.getHeader("Prefer");
	if (prefer == null) {
		return false;
	}
	for (String preference : prefer.split(",")) {
		if ("respond-async".equals(preference.trim().toLowerCase(Locale.ROOT))) {
			return true;
		}
	}
	return false;
}

/**
 * Hands the GET to a virtual-thread worker and answers {@code 202 Accepted} immediately —
 * the status monitor reports 202 while the execution runs and delivers the result once done
 * ([OData-Protocol] 11.6). The worker gets a request SNAPSHOT: the container recycles the
 * original request/response objects when this method returns, so the background thread must
 * never touch them.
 */
void accept(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	// DoS guard: bound concurrently EXECUTING async requests so respond-async cannot open an
	// unbounded number of backend sessions/threads. At the limit, refuse with 503 + Retry-After
	// rather than accept work we cannot run ([OData-Protocol] 11.6 does not mandate acceptance).
	Semaphore permits = servlet.asyncInflight;
	boolean bounded = permits != null;
	if (bounded && !permits.tryAcquire()) {
		response.setHeader("Retry-After", "1");
		servlet.error(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
				"the server is at its concurrent asynchronous-request limit");
		return;
	}
	BatchHttpRequest snapshot = BatchHttpRequest.asyncSnapshot(request);
	BatchHttpResponse captured = new BatchHttpResponse(response);
	String metadataLevel = ODataServlet.METADATA_LEVEL.get();
	Boolean ieee754 = ODataServlet.IEEE754.get();
	String id = UUID.randomUUID().toString();
	asyncResults.put(id, asyncExecutor.submit(() -> {
		try {
			return executeAsync(snapshot, captured, metadataLevel, ieee754);
		} finally {
			if (bounded) {
				permits.release(); // in-flight = executing; the permit frees when the run ends
			}
		}
	}));
	response.setStatus(HttpServletResponse.SC_ACCEPTED);
	response.setHeader("Location", ODataServlet.contextRoot(request) + "/$async/" + id);
	response.setHeader("Preference-Applied", "respond-async");
}

/**
 * Runs one async GET on the worker thread. The request-scoped ThreadLocals travel from the
 * accepting container thread explicitly; failures are captured as a sanitized 500 result so
 * the monitor always has something to deliver.
 */
private AsyncResult executeAsync(BatchHttpRequest request, BatchHttpResponse captured,
		String metadataLevel, Boolean ieee754) {
	ODataServlet.METADATA_LEVEL.set(metadataLevel);
	ODataServlet.IEEE754.set(ieee754);
	try {
		servlet.dispatchDirectly(request, captured); // HttpServlet dispatch → doGet, bypasses respond-async
		captured.flushBufferQuietly();
		return new AsyncResult(captured.status(),
				new LinkedHashMap<>(captured.headers()), captured.body());
	} catch (Exception e) {
		LOGGER.log(System.Logger.Level.ERROR,
				() -> "unhandled failure serving async GET " + request.getRequestURI(), e);
		return new AsyncResult(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				Map.of("content-type", "application/json;charset=UTF-8"),
				ODataJson.error(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"internal server error").getBytes(StandardCharsets.UTF_8));
	} finally {
		ODataServlet.METADATA_LEVEL.remove();
		ODataServlet.IEEE754.remove();
	}
}

/**
 * {@code GET /$async/<id>}: 202 + Location while the execution still runs; once done, the
 * result as an {@code application/http} message ([OData-Protocol] 11.6.1) — ONE-shot, the
 * monitor is gone after delivery. Unknown/expired/cancelled monitors answer 404.
 */
void monitor(String id, HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	Future<AsyncResult> execution = asyncResults.get(id);
	if (execution == null) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "unknown status monitor");
		return;
	}
	if (!execution.isDone()) {
		response.setStatus(HttpServletResponse.SC_ACCEPTED);
		response.setHeader("Location", ODataServlet.contextRoot(request) + "/$async/" + id);
		return;
	}
	if (asyncResults.remove(id) == null) {
		// a concurrent poll claimed the finished result first — one-shot stays one-shot
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "unknown status monitor");
		return;
	}
	AsyncResult result;
	try {
		result = execution.get();
	} catch (CancellationException e) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "unknown status monitor");
		return;
	} catch (InterruptedException e) {
		Thread.currentThread().interrupt();
		servlet.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error");
		return;
	} catch (ExecutionException e) {
		// executeAsync never throws — belt and braces for the unforeseen
		LOGGER.log(System.Logger.Level.ERROR,
				() -> "async execution failed for monitor " + id, e.getCause());
		servlet.error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal server error");
		return;
	}
	response.setStatus(HttpServletResponse.SC_OK);
	response.setContentType("application/http");
	response.setHeader("AsyncResult", String.valueOf(result.status()));
	StringBuilder message = new StringBuilder("HTTP/1.1 ").append(result.status())
			.append(' ').append(reasonPhrase(result.status())).append("\r\n");
	result.headers().forEach((name, value) ->
			message.append(name).append(": ").append(value).append("\r\n"));
	message.append("\r\n");
	response.getOutputStream().write(message.toString().getBytes(StandardCharsets.UTF_8));
	response.getOutputStream().write(result.body());
}

private static String reasonPhrase(int status) {
	return switch (status) {
		case 200 -> "OK";
		case 204 -> "No Content";
		case 400 -> "Bad Request";
		case 404 -> "Not Found";
		case 410 -> "Gone";
		case 501 -> "Not Implemented";
		default -> "";
	};
}

	/** DELETE of a status monitor (11.6): aborts a still-running execution, discards the result. */
	boolean cancel(String id) {
		Future<AsyncResult> cancelled = asyncResults.remove(id);
		if (cancelled != null) {
			cancelled.cancel(true); // interrupts the worker, best effort
			return true;
		}
		return false;
	}

	/** Deactivation: stop the workers, cancel and drop every parked execution. */
	void shutdown() {
		asyncExecutor.shutdownNow();
		synchronized (asyncResults) {
			asyncResults.values().forEach(execution -> execution.cancel(true));
			asyncResults.clear();
		}
	}
}
