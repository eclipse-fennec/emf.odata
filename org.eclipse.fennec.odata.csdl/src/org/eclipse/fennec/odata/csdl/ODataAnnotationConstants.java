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
package org.eclipse.fennec.odata.csdl;

/**
 * Keys for the OData EMF {@link org.eclipse.emf.ecore.EAnnotation} layer that the
 * {@link OdataResolver} reads to override the plain-Ecore defaults. All keys live under a single
 * annotation {@link #SOURCE} on the annotated model element; values are strings (booleans parsed
 * via {@link Boolean#parseBoolean}, ints via {@link Integer#parseInt}).
 *
 * <p>Placement:
 * <ul>
 *   <li>{@link #NAMESPACE}, {@link #ALIAS} — on the {@code EPackage}.</li>
 *   <li>{@link #OPEN_TYPE}, {@link #HAS_STREAM} — on an {@code EClass}.</li>
 *   <li>{@link #KEY}, {@link #COMPUTED}, {@link #IMMUTABLE}, {@link #TYPE}, {@link #MAX_LENGTH},
 *       {@link #PRECISION}, {@link #SCALE}, {@link #DEFAULT_VALUE} — on an {@code EAttribute}.</li>
 *   <li>{@link #CONTAINS_TARGET}, {@link #ON_DELETE} — on an {@code EReference}.</li>
 *   <li>{@link #OPERATION_KIND}, {@link #BOUND}, {@link #COMPOSABLE}, {@link #ENTITY_SET_PATH}
 *       — on an {@code EOperation}.</li>
 * </ul>
 */
public final class ODataAnnotationConstants {

	/** Single EAnnotation source URI carrying all OData detail keys. */
	public static final String SOURCE = "https://eclipse.org/fennec/odata";

	/**
	 * Source-URI prefix for vocabulary terms mapped onto an {@code EPackage} by the CSDL read
	 * path: one EAnnotation per term, source {@code TERM_SOURCE_PREFIX + termName}, details
	 * {@code type}, {@code appliesTo}, {@code defaultValue}, {@code baseTerm}.
	 */
	public static final String TERM_SOURCE_PREFIX = "https://eclipse.org/fennec/odata/term/";

	/**
	 * EAnnotation source for generic vocabulary-term annotations (AP-5, constant-expression
	 * subset): each detail entry {@code qualifiedTermName -> constantValue} maps to a CSDL
	 * {@code <Annotation Term="..." String|Bool|Int|Decimal="..."/>} on the corresponding
	 * schema element, and back. Rich expressions (Record/Collection/Path) are out of scope.
	 */
	public static final String ANNOTATIONS_SOURCE = "https://eclipse.org/fennec/odata/annotations";

	/**
	 * EAnnotation source for container-level singletons ([OData-CSDL] 13.5), placed on the
	 * {@code EPackage}: each detail entry {@code singletonName -> EClassName} becomes a
	 * {@code <Singleton Name="singletonName" Type="Namespace.EClassName"/>} in the entity
	 * container, and back. The value is the simple {@code EClass} name; the converter qualifies it
	 * with the schema namespace.
	 */
	public static final String SINGLETONS_SOURCE = "https://eclipse.org/fennec/odata/singletons";

	/**
	 * EAnnotation source for container entity-set names, placed on the {@code EPackage}: each
	 * detail entry {@code setName -> EClassName} names an entity set whose name DIFFERS from its
	 * entity type's (e.g. TripPin's {@code People -> Person}). The CSDL read path captures the
	 * container's sets here; the write path and the runtime honour it — without the annotation the
	 * convention "set name = type name" applies.
	 */
	public static final String ENTITY_SETS_SOURCE = "https://eclipse.org/fennec/odata/entitysets";

	// package
	public static final String NAMESPACE = "OData.Namespace";
	public static final String ALIAS = "OData.Alias";

	// class
	public static final String OPEN_TYPE = "OData.OpenType";
	public static final String HAS_STREAM = "OData.HasStream";

	// attribute → property
	public static final String KEY = "OData.Key";
	public static final String COMPUTED = "OData.Property.Computed";
	public static final String IMMUTABLE = "OData.Property.Immutable";
	public static final String TYPE = "OData.Type";
	public static final String MAX_LENGTH = "OData.MaxLength";
	public static final String PRECISION = "OData.Precision";
	public static final String SCALE = "OData.Scale";
	public static final String DEFAULT_VALUE = "OData.DefaultValue";
	/** Spatial reference system id facet — numeric or the symbolic {@code Variable}. */
	public static final String SRID = "OData.SRID";
	/** Unicode facet ([OData-CSDL] 7.2.5): {@code false} = ASCII-only string property. */
	public static final String UNICODE = "OData.Unicode";

	// reference → navigation
	public static final String CONTAINS_TARGET = "OData.NavigationProperty.ContainsTarget";
	public static final String ON_DELETE = "OData.NavigationProperty.OnDelete";
	/**
	 * Referential constraint(s) of the navigation: {@code "property=referencedProperty"} pairs,
	 * comma-separated — dependent (foreign-key) property on the declaring type = principal key
	 * property on the navigation target (e.g. {@code "ownerId=id"}).
	 */
	public static final String REFERENTIAL_CONSTRAINT = "OData.NavigationProperty.ReferentialConstraint";

	// operation → function/action
	/** {@code "Function"} or {@code "Action"} (overrides the void-return→Action default). */
	public static final String OPERATION_KIND = "OData.OperationKind";
	/** {@code "false"} makes the operation unbound (no binding parameter; surfaces as an import). */
	public static final String BOUND = "OData.Bound";
	public static final String COMPOSABLE = "OData.Composable";
	public static final String ENTITY_SET_PATH = "OData.EntitySetPath";

	private ODataAnnotationConstants() {
	}
}
