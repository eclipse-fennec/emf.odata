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
package org.eclipse.fennec.odata.codec.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.WeakHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.codec.config.ConfigurationResolver;
import org.eclipse.fennec.codec.constants.CodecOptions;
import org.eclipse.fennec.codec.resource.CodecResource;
import org.eclipse.fennec.model.metadata.PackageProfile;
import org.eclipse.fennec.model.metadata.TypeStrategy;
import org.eclipse.fennec.model.metadata.api.MetadataService;
import org.eclipse.fennec.odata.csdl.OdataResolver;
import org.eclipse.fennec.odata.csdl.profile.ODataClassProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile;

/**
 * EMF Resource for the OData-JSON data format (req §3.7) — the instance-side counterpart of the
 * CSDL codec: while {@code org.eclipse.fennec.odata.csdl} transports schemas, this resource
 * transports {@link EObject}s in the OData JSON dialect. Configured as a Fennec codec profile
 * (analogous to the GeoJSON profile):
 * <ul>
 *   <li>{@code @odata.type} type key, schema-qualified ({@link TypeStrategy#SCHEMA_AND_TYPE})</li>
 *   <li>{@code @odata.id} id key, {@code COMBINED} strategy with key mode {@code BOTH}
 *       (composite keys stay as properties AND form the aggregate id)</li>
 *   <li>{@code Edm.*} value formats via {@link EdmJsonValues}, wired per feature from the
 *       resolved OData profile (E1): {@code Edm.Date}, {@code Edm.TimeOfDay},
 *       {@code Edm.DateTimeOffset}, {@code Edm.Binary}</li>
 * </ul>
 *
 * <p>The profile is taken from the {@link MetadataService} when the {@code ODataAspectProvider}
 * is installed (O(1), precomputed) and falls back to a locally cached {@link OdataResolver} run
 * otherwise, so the resource also works in plain-Java setups without the E1 whiteboard.
 *
 * <p>Deliberate v1 gaps (follow-ups): {@code @odata.context} emission and the
 * {@code metadata=minimal/full/none} profile variants belong to the server runtime (E6);
 * entity-URL-shaped {@code @odata.id} values need the service root, which only the runtime
 * knows. The {@code "#Namespace.Type"} WRITE form for derived instances is available via
 * {@link #typeDiscriminator(EObject)} (the runtime decides against the declared type);
 * READING {@code #Ns.Type}-discriminated payloads is an E8 client work package.
 */
public class ODataJsonResourceImpl extends CodecResource {

	/**
	 * Aspect type id of the OData provider. Mirrors {@code ODataAspectProvider.ASPECT_TYPE_ID}
	 * ("odata"), which lives in the metadata bundle's INTERNAL (non-exported) provider package —
	 * duplicated here as a literal rather than exposing that internal API as export.
	 */
	private static final String ODATA_ASPECT_TYPE_ID = "odata";
	/** CSDL collection-type prefix; mirrors {@code EdmTypes.COLLECTION_OPEN} (package-private in csdl). */
	private static final String COLLECTION_OPEN = "Collection(";

	private final MetadataService odataMetadataService;
	/** {@code IEEE754Compatible=true} exchanges: Edm.Int64/Edm.Decimal travel as strings. */
	private boolean ieee754Compatible;
	/**
	 * Fallback profiles for packages the MetadataService has no OData profile for. Weakly keyed
	 * so unregistered/discarded EPackages can be collected — safe because the csdl profile holds
	 * only strings, never references back to the EPackage.
	 */
	private static final Map<EPackage, ODataPackageProfile> RESOLVED =
			Collections.synchronizedMap(new WeakHashMap<>());

	public ODataJsonResourceImpl(URI uri, MetadataService metadataService) {
		this(uri, metadataService, Set.of());
	}

	/**
	 * @param expandedReferences names of navigation references to serialize INLINE
	 *                           ({@code $expand}); all other non-containment references are
	 *                           omitted from the payload by the caller's shaping
	 */
	public ODataJsonResourceImpl(URI uri, MetadataService metadataService, Set<String> expandedReferences) {
		this(uri, metadataService, expandedReferences, false);
	}

	private ODataJsonResourceImpl(URI uri, MetadataService metadataService, Set<String> expandedReferences,
			boolean minimalMetadata) {
		super(uri, metadataService, createODataResolver(expandedReferences, minimalMetadata), null);
		this.odataMetadataService = metadataService;
	}

	/**
	 * Resource for {@code odata.metadata=minimal} server responses ([OData-JSON] 4.5.8): control
	 * information that the client can compute from the context URL — {@code @odata.type} of
	 * non-derived types and {@code @odata.id} of entities with key properties in the payload —
	 * is OMITTED. Key properties themselves always stay in the payload.
	 */
	public static ODataJsonResourceImpl minimalMetadata(URI uri, MetadataService metadataService,
			Set<String> expandedReferences) {
		return new ODataJsonResourceImpl(uri, metadataService, expandedReferences, true);
	}

	/**
	 * Switches the resource to {@code IEEE754Compatible=true} ([OData-JSON] 8.1): {@code Edm.Int64}
	 * and {@code Edm.Decimal} values are written as (and read from) STRINGS, preserving their exact
	 * value for clients whose numbers are IEEE 754 doubles.
	 */
	public ODataJsonResourceImpl ieee754Compatible(boolean on) {
		this.ieee754Compatible = on;
		return this;
	}

	private static ConfigurationResolver createODataResolver(Set<String> expandedReferences,
			boolean minimalMetadata) {
		ConfigurationResolver.Builder builder = ConfigurationResolver.builder()
				.typeKey("@odata.type")
				.typeStrategy(TypeStrategy.SCHEMA_AND_TYPE)
				.typeInclude(!minimalMetadata);
		if (minimalMetadata) {
			// key PROPERTIES stay — only the redundant control info goes. useId(false) alone
			// switches the strategy, but the serializer gates on the KEY MODE: without
			// FEATURE_ONLY a codec-internal "_id" field leaks into the OData payload
			builder.useId(false).idKeyMode("FEATURE_ONLY");
		} else {
			builder.useId(true)
					.idKey("@odata.id")
					.idStrategy("COMBINED")
					.idKeyMode("BOTH")
					.idOnTop(true);
		}
		if (!expandedReferences.isEmpty()) {
			builder.expand(expandedReferences);
		}
		return builder.build();
	}

	/** Serializes with the profile-driven {@code Edm.*} value writers merged into the options. */
	@Override
	protected void doSave(OutputStream outputStream, Map<?, ?> options) throws IOException {
		Map<Object, Object> effective = new HashMap<>();
		if (options != null) {
			options.forEach(effective::put);
		}
		mergeValueCodecs(effective, CodecOptions.CODEC_FEATURE_VALUE_WRITER_INSTANCES, contentPackages(), true);
		super.doSave(outputStream, effective);
	}

	/** Loads with the profile-driven {@code Edm.*} value readers for the root type's package. */
	@Override
	protected void doLoad(InputStream inputStream, Map<?, ?> options) throws IOException {
		Map<Object, Object> effective = new HashMap<>();
		if (options != null) {
			options.forEach(effective::put);
		}
		if (effective.get(CODEC_ROOT_TYPE) instanceof EClass rootType) {
			mergeValueCodecs(effective, CodecOptions.CODEC_FEATURE_VALUE_READER_INSTANCES,
					Set.of(rootType.getEPackage()), false);
		}
		super.doLoad(inputStream, effective);
	}

	private Set<EPackage> contentPackages() {
		Set<EPackage> packages = new LinkedHashSet<>();
		for (EObject root : getContents()) {
			packages.add(root.eClass().getEPackage());
			root.eAllContents().forEachRemaining(o -> packages.add(o.eClass().getEPackage()));
		}
		return packages;
	}

	/**
	 * Computes feature→writer/reader instances for every {@code Edm.*}-typed attribute of the
	 * given packages and merges them under the codec option {@code key}, keeping entries the
	 * caller already provided.
	 */
	private void mergeValueCodecs(Map<Object, Object> options, String key, Set<EPackage> packages, boolean write) {
		Map<EStructuralFeature, Object> instances = new HashMap<>();
		if (options.get(key) instanceof Map<?, ?> provided) {
			provided.forEach((f, v) -> instances.put((EStructuralFeature) f, v));
		}
		for (EPackage pkg : packages) {
			ODataPackageProfile profile = odataProfile(pkg);
			if (profile == null) {
				continue;
			}
			for (ODataClassProfile classProfile : profile.getClasses()) {
				if (!(pkg.getEClassifier(classProfile.getName()) instanceof EClass eClass)) {
					continue;
				}
				for (ODataPropertyProfile property : classProfile.getProperties()) {
					if (!(eClass.getEStructuralFeature(property.getName()) instanceof EAttribute attribute)) {
						continue;
					}
					Object codec = write
							? EdmJsonValues.writer(unwrapCollection(property.getTypeName()), ieee754Compatible)
							: EdmJsonValues.reader(unwrapCollection(property.getTypeName()), ieee754Compatible);
					if (codec != null) {
						instances.putIfAbsent(attribute, codec);
					}
				}
			}
		}
		if (!instances.isEmpty()) {
			options.put(key, instances);
		}
	}

	/**
	 * The single-field {@code "#Namespace.Type"} discriminator ([OData-JSON] 4.5.8): required
	 * in minimal-metadata payloads when the instance type DERIVES from the type the context
	 * URL declares — then the type is not computable and must travel with the entity. The
	 * namespace is the schema namespace of the instance's package profile (same source as
	 * {@code $metadata}).
	 */
	public String typeDiscriminator(EObject object) {
		ODataPackageProfile profile = odataProfile(object.eClass().getEPackage());
		return "#" + profile.getNamespace() + "." + object.eClass().getName();
	}

	/** E1 profile via MetadataService when present, else a locally cached resolver run. */
	private ODataPackageProfile odataProfile(EPackage pkg) {
		PackageProfile profile = odataMetadataService == null ? null
				: odataMetadataService.getPackageProfile(pkg, ODATA_ASPECT_TYPE_ID);
		if (profile instanceof org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile composed
				&& composed.getOdataProfile() != null) {
			return composed.getOdataProfile();
		}
		return RESOLVED.computeIfAbsent(pkg, p -> new OdataResolver().resolve(p));
	}

	private static String unwrapCollection(String typeName) {
		if (typeName != null && typeName.startsWith(COLLECTION_OPEN)) {
			return typeName.substring(COLLECTION_OPEN.length(), typeName.length() - 1);
		}
		return typeName;
	}
}
