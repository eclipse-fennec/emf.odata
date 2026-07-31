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
package org.eclipse.fennec.odata.metadata.provider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.metadata.AttributeMetadata;
import org.eclipse.fennec.model.metadata.ClassAspect;
import org.eclipse.fennec.model.metadata.ClassMetadata;
import org.eclipse.fennec.model.metadata.FeatureAspect;
import org.eclipse.fennec.model.metadata.FeatureMetadata;
import org.eclipse.fennec.model.metadata.OperationAspect;
import org.eclipse.fennec.model.metadata.OperationMetadata;
import org.eclipse.fennec.model.metadata.PackageAspect;
import org.eclipse.fennec.model.metadata.PackageMetadata;
import org.eclipse.fennec.model.metadata.PackageProfile;
import org.eclipse.fennec.model.metadata.ReferenceMetadata;
import org.eclipse.fennec.model.metadata.api.AspectProvider;
import org.eclipse.fennec.odata.csdl.OdataResolver;
import org.eclipse.fennec.odata.metadata.odata.ODataClassProfile;
import org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile;
import org.eclipse.fennec.odata.metadata.odata.OdataFactory;

/**
 * OData {@link AspectProvider} — E1 stub.
 * <p>
 * Translates OData metadata (from {@code odata.ecore}, once defined) into the Model
 * Metadata Service's aspect/profile structures, analogous to {@code CodecAspectProvider}.
 * Built per registered {@code EPackage} by the {@code MetadataService} whiteboard.
 * <p>
 * <b>Stub state:</b> the concrete set of OData aspects is not yet decided, so every
 * {@code build*} method returns {@code null} (= "this provider contributes nothing")
 * until the aspect model exists. The TODOs sketch the intended responsibilities.
 */
public class ODataAspectProvider implements AspectProvider {

	/** Aspect type id used to filter OData aspects/profiles within the metadata. */
	public static final String ASPECT_TYPE_ID = "odata";

	@Override
	public String getAspectTypeId() {
		return ASPECT_TYPE_ID;
	}

	@Override
	public PackageAspect buildPackageAspect(PackageMetadata packageMetadata) {
		// TODO E1: Schema / EntityContainer-level aspect from packageMetadata.getEPackage().
		return null;
	}

	@Override
	public ClassAspect buildClassAspect(ClassMetadata classMetadata) {
		// TODO E1: EntityType vs ComplexType + key resolution from classMetadata.getEClass().
		return null;
	}

	@Override
	public FeatureAspect buildFeatureAspect(FeatureMetadata featureMetadata) {
		// TODO E1: generic feature fallback (the MetadataService dispatches attributes and
		// references to the methods below first, falling back here).
		return null;
	}

	@Override
	public FeatureAspect buildAttributeAspect(AttributeMetadata attributeMetadata) {
		// TODO E1: primitive property facets (nullable, key part, computed, ...) from
		// attributeMetadata.getEAttribute().
		return null;
	}

	@Override
	public FeatureAspect buildReferenceAspect(ReferenceMetadata referenceMetadata) {
		// TODO E1: navigation property (target, containment, partner) from
		// referenceMetadata.getEReference().
		return null;
	}

	@Override
	public OperationAspect buildOperationAspect(OperationMetadata operationMetadata) {
		// OData functions/actions are resolved via the CSDL profile path in buildProfiles(),
		// not as per-element aspects — so, like the other build*Aspect hooks, this contributes
		// nothing.
		return null;
	}

	/**
	 * ADR-0003 Phase 2: this provider is a thin adapter. The OData resolution + conversion logic
	 * lives entirely in the standalone, service-independent CSDL converter
	 * ({@code org.eclipse.fennec.odata.csdl.OdataResolver}). Here we just invoke it and
	 * <em>compose</em> the resulting {@code csdl ODataPackageProfile} into a metadata
	 * {@link PackageProfile} (containment), with a per-EClass cross-reference for O(1) lookup
	 * by service consumers (query / OCL type-resolution). The per-element {@code build*Aspect}
	 * hooks therefore contribute nothing.
	 */
	@Override
	public PackageProfile buildProfiles(PackageMetadata filteredMetadataCopy) {
		EPackage ePackage = filteredMetadataCopy.getEPackage();
		if (ePackage == null) {
			return null;
		}
		org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile csdlProfile =
				new OdataResolver().resolve(ePackage);

		ODataPackageProfile profile = OdataFactory.eINSTANCE.createODataPackageProfile();
		profile.setTypeId(ASPECT_TYPE_ID);
		profile.setNamespace(csdlProfile.getNamespace());
		profile.setOdataProfile(csdlProfile); // composition: contain the whole resolved profile

		Map<String, org.eclipse.fennec.odata.csdl.profile.ODataClassProfile> byName = new HashMap<>();
		csdlProfile.getClasses().forEach(c -> byName.put(c.getName(), c));

		for (ClassMetadata classMetadata : filteredMetadataCopy.getClasses()) {
			EClass eClass = classMetadata.getEClass();
			if (eClass == null) {
				continue;
			}
			org.eclipse.fennec.odata.csdl.profile.ODataClassProfile csdlClass = byName.get(eClass.getName());
			if (csdlClass == null) {
				continue; // e.g. enums carry no class profile
			}
			ODataClassProfile classProfile = OdataFactory.eINSTANCE.createODataClassProfile();
			classProfile.setEClass(eClass);
			classProfile.setOdataProfile(csdlClass); // cross-ref into the contained tree
			profile.getClassProfiles().add(classProfile);
		}
		return profile;
	}
}
