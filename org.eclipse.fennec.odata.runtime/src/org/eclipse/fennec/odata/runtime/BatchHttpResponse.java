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

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

/** Synthetic response that captures status, headers and body of one batch sub-request. */
final class BatchHttpResponse extends HttpServletResponseWrapper {
	private int status = HttpServletResponse.SC_OK;
	private final Map<String, String> headers = new LinkedHashMap<>(); // keys lower-cased
	private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	private PrintWriter writer;

	BatchHttpResponse(HttpServletResponse outer) {
		super(outer);
	}

	int status() {
		return status;
	}

	Map<String, String> headers() {
		return headers;
	}

	byte[] body() {
		flushBufferQuietly();
		return buffer.toByteArray();
	}

	void flushBufferQuietly() {
		if (writer != null) {
			writer.flush();
		}
	}

	@Override
	public void setStatus(int sc) {
		this.status = sc;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public void setContentType(String type) {
		headers.put("content-type", type);
	}

	@Override
	public String getContentType() {
		return headers.get("content-type");
	}

	@Override
	public void setHeader(String name, String value) {
		headers.put(name.toLowerCase(Locale.ROOT), value);
	}

	@Override
	public void addHeader(String name, String value) {
		headers.put(name.toLowerCase(Locale.ROOT), value);
	}

	@Override
	public boolean containsHeader(String name) {
		return headers.containsKey(name.toLowerCase(Locale.ROOT));
	}

	@Override
	public String getHeader(String name) {
		return headers.get(name.toLowerCase(Locale.ROOT));
	}

	@Override
	public void setCharacterEncoding(String charset) {
		// captured buffer is always UTF-8
	}

	@Override
	public void setContentLength(int len) {
		// captured, never delegated — async workers must not touch the recycled response
		headers.put("content-length", String.valueOf(len));
	}

	@Override
	public void setContentLengthLong(long len) {
		headers.put("content-length", String.valueOf(len));
	}

	@Override
	public PrintWriter getWriter() {
		if (writer == null) {
			writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
		}
		return writer;
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return new ServletOutputStream() {
			@Override
			public void write(int b) {
				buffer.write(b);
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setWriteListener(WriteListener writeListener) {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public void reset() {
		buffer.reset();
		headers.clear();
		status = HttpServletResponse.SC_OK;
		writer = null;
	}
}
