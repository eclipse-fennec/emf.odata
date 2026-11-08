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
/**
 * The wire profile of an OData service root (ADR-0010): how an OData dialect such as OGC
 * SensorThings writes control information, keys, links and its root document. A root selects
 * at most one {@link org.eclipse.fennec.odata.wire.api.WireProfile} by configuration; without
 * one it speaks plain OData.
 */
@org.osgi.annotation.bundle.Export
@org.osgi.annotation.versioning.Version("1.0.0")
package org.eclipse.fennec.odata.wire.api;
