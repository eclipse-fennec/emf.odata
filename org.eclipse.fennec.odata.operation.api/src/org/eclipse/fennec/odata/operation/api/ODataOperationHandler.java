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
package org.eclipse.fennec.odata.operation.api;

import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Provides the behaviour of an OData function or action. A dynamic EMF model only DECLARES operation
 * signatures ({@link EOperation}); this SPI supplies what they DO. The runtime resolves the addressed
 * operation from the metadata, picks the first handler that {@link #handles(String) handles} it,
 * invokes it and serialises the result. With no matching handler the request is answered {@code 501}.
 *
 * <p>Registered as OSGi services; {@link org.osgi.framework.Constants#SERVICE_RANKING} breaks ties.
 */
@ConsumerType
public interface ODataOperationHandler {

	/**
	 * Whether this handler implements the operation with the given namespace-qualified name
	 * (e.g. {@code My.Shop.discount}).
	 */
	boolean handles(String qualifiedOperationName);

	/**
	 * Invokes the operation.
	 *
	 * @param operation     the resolved operation signature (parameters, return type)
	 * @param boundInstance the entity the operation is bound to, or {@code null} for an unbound
	 *                      function/action import
	 * @param parameters    the supplied parameters, keyed by name, already coerced to the parameter
	 *                      types where possible
	 * @return the result: a primitive value, an {@link EObject}, a collection thereof, or
	 *         {@code null} for a void action
	 */
	Object invoke(EOperation operation, EObject boundInstance, Map<String, Object> parameters);
}
