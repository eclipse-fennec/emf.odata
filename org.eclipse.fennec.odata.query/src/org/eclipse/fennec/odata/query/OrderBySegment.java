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

import org.eclipse.fennec.m2x.model.ocl.OclExpression;

/**
 * One {@code $orderby} item: the sort expression (OCL IR, req §3.5 Stufe 2) and its direction
 * (OData default is ascending).
 */
public record OrderBySegment(OclExpression expression, boolean ascending) {
}
