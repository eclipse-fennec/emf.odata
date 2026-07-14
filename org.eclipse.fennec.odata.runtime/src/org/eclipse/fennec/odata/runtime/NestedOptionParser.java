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

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.odata.query.OrderBySegment;

/**
 * Parses the expression-valued nested options of {@code $expand}/{@code $select} items against
 * an item context type. The protocol layer supplies the implementation (the guarded
 * {@code ODataQueryParser} plus its request limits) so {@link SelectTree} stays a pure
 * structure.
 */
public interface NestedOptionParser {

	/** A nested {@code $filter} expression → predicate IR over the item context. */
	OclExpression filter(String expression, EClass context);

	/** A nested {@code $orderby} value → sort segments over the item context. */
	List<OrderBySegment> orderBy(String expression, EClass context);

	/**
	 * A nested {@code $search} value → predicate IR: the free-text term matched against the
	 * item context's string properties (the same mapping as the top-level {@code $search}).
	 */
	OclExpression search(String term, EClass context);
}
