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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.codec.util.MetadataServiceFactory;
import org.eclipse.fennec.model.metadata.api.MetadataWhiteboard;
import org.eclipse.fennec.odata.persistence.inmemory.InMemoryBackends;
import org.eclipse.fennec.odata.persistence.inmemory.MemoryWriteRepository;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * The MIRROR server for the live round-trip suite: OUR {@link ODataServlet} wired plain-Java
 * (no OSGi) over the in-memory backend, serving REAL HTTP through a thin
 * {@code com.sun.net.httpserver} → servlet bridge. Lives in the runtime package because the
 * servlet's wiring methods are the package-private DS bind points.
 */
public final class MirrorServerHarness implements AutoCloseable {

	private final HttpServer server;
	private final MemoryWriteRepository repository;
	private final String serviceRoot;

	private MirrorServerHarness(HttpServer server, MemoryWriteRepository repository, String root) {
		this.server = server;
		this.repository = repository;
		this.serviceRoot = root;
	}

	/** Wires the servlet for the given packages and serves it on an ephemeral port. */
	public static MirrorServerHarness start(List<EPackage> packages) throws IOException {
		InMemoryBackends.Wiring backend = InMemoryBackends.wire(packages);
		MetadataWhiteboard metadataService = MetadataServiceFactory.create();
		packages.forEach(metadataService::registerPackage);

		ODataServlet servlet = new ODataServlet();
		servlet.activate(Map.of());
		packages.forEach(servlet::addEPackage);
		servlet.setMetadataService(metadataService);
		servlet.addQueryService(backend.queryService());
		servlet.addWriteService(backend.repository());
		servlet.addMediaService(backend.repository());

		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/odata/", exchange -> bridge(servlet, exchange));
		server.start();
		String root = "http://127.0.0.1:" + server.getAddress().getPort() + "/odata/";
		return new MirrorServerHarness(server, backend.repository(), root);
	}

	public String serviceRoot() {
		return serviceRoot;
	}

	public MemoryWriteRepository repository() {
		return repository;
	}

	@Override
	public void close() {
		server.stop(0);
	}

	/** One HTTP exchange through the servlet: Mockito-backed request/response adapters. */
	private static void bridge(ODataServlet servlet, HttpExchange exchange) throws IOException {
		byte[] requestBody = exchange.getRequestBody().readAllBytes();
		String fullPath = exchange.getRequestURI().getPath();
		String pathInfo = fullPath.substring("/odata".length());
		Map<String, String[]> parameters = parseQuery(exchange.getRequestURI().getRawQuery());

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getMethod()).thenReturn(exchange.getRequestMethod());
		when(request.getPathInfo()).thenReturn(pathInfo.isEmpty() ? "/" : pathInfo);
		when(request.getRequestURI()).thenReturn(fullPath);
		when(request.getContentType())
				.thenReturn(exchange.getRequestHeaders().getFirst("Content-Type"));
		when(request.getHeader(anyString())).thenAnswer(invocation ->
				exchange.getRequestHeaders().getFirst(invocation.getArgument(0)));
		when(request.getInputStream()).thenReturn(servletInput(requestBody));
		when(request.getParameterMap()).thenReturn(parameters);
		when(request.getParameterNames())
				.thenAnswer(invocation -> Collections.enumeration(parameters.keySet()));
		when(request.getParameter(anyString())).thenAnswer(invocation -> {
			String[] values = parameters.get((String) invocation.getArgument(0));
			return values == null || values.length == 0 ? null : values[0];
		});

		ByteArrayOutputStream body = new ByteArrayOutputStream();
		PrintWriter writer = new PrintWriter(
				new java.io.OutputStreamWriter(body, StandardCharsets.UTF_8), true);
		AtomicInteger status = new AtomicInteger(200);
		Map<String, String> headers = new LinkedHashMap<>();
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getWriter()).thenReturn(writer);
		when(response.getOutputStream()).thenReturn(servletOutput(body));
		doAnswer(invocation -> {
			status.set(invocation.getArgument(0));
			return null;
		}).when(response).setStatus(anyInt());
		doAnswer(invocation -> {
			headers.put(invocation.getArgument(0), invocation.getArgument(1));
			return null;
		}).when(response).setHeader(anyString(), anyString());
		doAnswer(invocation -> {
			headers.put("Content-Type", invocation.getArgument(0));
			return null;
		}).when(response).setContentType(anyString());

		try {
			servlet.service(request, response);
		} catch (Exception e) {
			status.set(500);
			body.reset();
			body.write("{\"error\":{\"code\":\"500\",\"message\":\"bridge failure\"}}"
					.getBytes(StandardCharsets.UTF_8));
			headers.put("Content-Type", "application/json");
		}
		writer.flush();

		headers.forEach((name, value) -> exchange.getResponseHeaders().set(name, value));
		byte[] bytes = body.toByteArray();
		exchange.sendResponseHeaders(status.get(), bytes.length == 0 ? -1 : bytes.length);
		if (bytes.length > 0) {
			exchange.getResponseBody().write(bytes);
		}
		exchange.close();
	}

	private static Map<String, String[]> parseQuery(String rawQuery) {
		Map<String, String[]> parameters = new HashMap<>();
		if (rawQuery == null || rawQuery.isEmpty()) {
			return parameters;
		}
		for (String pair : rawQuery.split("&")) {
			int equals = pair.indexOf('=');
			String name = URLDecoder.decode(equals < 0 ? pair : pair.substring(0, equals),
					StandardCharsets.UTF_8);
			String value = equals < 0 ? ""
					: URLDecoder.decode(pair.substring(equals + 1), StandardCharsets.UTF_8);
			parameters.merge(name, new String[] { value }, (a, b) -> {
				String[] merged = new String[a.length + 1];
				System.arraycopy(a, 0, merged, 0, a.length);
				merged[a.length] = b[0];
				return merged;
			});
		}
		return parameters;
	}

	private static ServletInputStream servletInput(byte[] data) {
		ByteArrayInputStream bytes = new ByteArrayInputStream(data);
		return new ServletInputStream() {
			@Override
			public int read() {
				return bytes.read();
			}

			@Override
			public boolean isFinished() {
				return bytes.available() == 0;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(jakarta.servlet.ReadListener listener) {
			}
		};
	}

	private static ServletOutputStream servletOutput(ByteArrayOutputStream target) {
		return new ServletOutputStream() {
			@Override
			public void write(int b) {
				target.write(b);
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setWriteListener(WriteListener listener) {
			}
		};
	}
}
