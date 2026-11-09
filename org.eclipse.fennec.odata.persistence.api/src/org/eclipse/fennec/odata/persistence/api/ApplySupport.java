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
package org.eclipse.fennec.odata.persistence.api;

import java.util.List;

/**
 * What a backend can execute of {@code $apply} ([OData-Data-Agg] 3), as the protocol layer
 * announces it with {@code Aggregation.ApplySupported} in {@code $metadata} (#103).
 *
 * <p>The announcement is coarse by design: a backend lists a transformation it executes in
 * general, even when it still refuses a particular combination with {@code 501} (e.g. a
 * transformation only at a certain position in the pipeline).
 *
 * @param transformations the transformation names of {@code Aggregation.Transformation}
 *            ({@code aggregate}, {@code groupby}, {@code filter}, …) the backend executes; empty
 *            means no {@code $apply} support at all
 * @param rollup how many {@code rollup} hierarchies a {@code groupby} may contain
 */
public record ApplySupport(List<String> transformations, Rollup rollup) {

	/** No {@code $apply} support — the default of {@link QueryService#applySupport()}. */
	public static final ApplySupport NONE = new ApplySupport(List.of(), Rollup.NONE);

	/** {@code Aggregation.RollupType}. */
	public enum Rollup {
		/** No {@code rollup} in {@code groupby}. */
		NONE("None"),
		/** At most one {@code rollup} per {@code groupby}. */
		SINGLE_HIERARCHY("SingleHierarchy"),
		/** Any number of {@code rollup}s per {@code groupby}. */
		MULTIPLE_HIERARCHIES("MultipleHierarchies");

		private final String member;

		Rollup(String member) {
			this.member = member;
		}

		/** The member name of {@code Aggregation.RollupType}. */
		public String member() {
			return member;
		}
	}

	public ApplySupport {
		transformations = List.copyOf(transformations);
		if (rollup == null) {
			throw new IllegalArgumentException("rollup must not be null, use Rollup.NONE");
		}
	}

	/** Whether the backend executes {@code $apply} at all. */
	public boolean supported() {
		return !transformations.isEmpty();
	}
}
