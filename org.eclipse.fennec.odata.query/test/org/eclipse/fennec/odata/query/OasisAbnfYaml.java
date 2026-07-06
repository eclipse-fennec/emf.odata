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
package org.eclipse.fennec.odata.query;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tolerant line-based reader for the flat OASIS ABNF test-case YAML files
 * (oasis-tcs/odata-abnf: Core, Aggregation, Temporal — all share the
 * {@code - Name:}/{@code Rule:}/{@code Input:}/{@code FailAt:} shape). Deliberately not a YAML
 * dependency: the files are stereotyped enough for a line reader, incl. wrapped continuation
 * lines and YAML single-/double-quoted scalars.
 */
final class OasisAbnfYaml {

	/** One OASIS test case; {@code failAt} is the 0-based failure position, -1 = positive case. */
	record Case(String name, String rule, String input, int failAt) {

		boolean negative() {
			return failAt >= 0;
		}
	}

	private OasisAbnfYaml() {
	}

	static List<Case> load(Path yaml) throws IOException {
		List<Case> cases = new ArrayList<>();
		String name = null;
		String rule = null;
		StringBuilder input = null;
		int failAt = -1;
		List<String> lines = new ArrayList<>(Files.readAllLines(yaml));
		lines.add("  - Name: <eof>"); // flush trailing entry
		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.startsWith("- Name:")) {
				if (name != null && rule != null && input != null) {
					cases.add(new Case(name, rule, unquote(input.toString().trim()), failAt));
				}
				name = trimmed.substring("- Name:".length()).trim();
				rule = null;
				input = null;
				failAt = -1;
			} else if (trimmed.startsWith("Rule:")) {
				rule = stripComment(trimmed.substring("Rule:".length()));
			} else if (trimmed.startsWith("FailAt:")) {
				failAt = Integer.parseInt(stripComment(trimmed.substring("FailAt:".length())));
			} else if (trimmed.startsWith("Input:")) {
				input = new StringBuilder(trimmed.substring("Input:".length()).trim());
			} else if (input != null && !trimmed.isEmpty() && !trimmed.startsWith("#")
					&& !trimmed.startsWith("- ") && !trimmed.matches("[A-Za-z][A-Za-z0-9]*:.*")) {
				input.append(' ').append(trimmed); // wrapped continuation line
			}
		}
		return cases;
	}

	/** Inline YAML comment after a scalar field value (Input keeps its raw text). */
	private static String stripComment(String value) {
		int hash = value.indexOf(" #");
		return (hash < 0 ? value : value.substring(0, hash)).trim();
	}

	/** YAML scalar quoting: double-quoted with backslash escapes, single-quoted with {@code ''}. */
	private static String unquote(String raw) {
		if (raw.length() >= 2 && raw.startsWith("\"") && raw.endsWith("\"")) {
			return raw.substring(1, raw.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
		}
		if (raw.length() >= 2 && raw.startsWith("'") && raw.endsWith("'")) {
			return raw.substring(1, raw.length() - 1).replace("''", "'");
		}
		return raw;
	}

	/** Walks up from the working directory so the tests run from bundle AND workspace root. */
	static Path findResource(String... candidatesRelative) {
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
