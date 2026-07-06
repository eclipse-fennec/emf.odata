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

import java.util.List;

/**
 * A parsed OData resource path (ADR-0005, [OData-URL]/[OData-ABNF] {@code resourcePath}
 * subset): the addressed entity set, an optional key predicate (raw literal text — literal
 * typing happens at the protocol layer) and the navigation/property segments. Purely
 * syntactic — resolution against the model is the caller's job.
 *
 * @param entitySet the first path segment (entity-set name)
 * @param key       raw key literal (still quoted for strings), or null
 * @param segments  the remaining segments in order (may be empty)
 */
public record ResourcePath(String entitySet, String key, List<Segment> segments) {

	public ResourcePath {
		segments = segments == null ? List.of() : List.copyOf(segments);
	}

	/** One path segment after the entity set. */
	public sealed interface Segment {
	}

	/** A property or navigation segment, optionally keyed ({@code /reviews(5)}). */
	public record PropertySegment(String name, String key) implements Segment {
	}

	/** Terminal {@code /$count} on a collection. */
	public record CountSegment() implements Segment {
	}

	/** Terminal {@code /$value} on a primitive property (raw value). */
	public record ValueSegment() implements Segment {
	}

	/** Terminal {@code /$ref} (entity reference) — recognized, execution optional. */
	public record RefSegment() implements Segment {
	}
}
