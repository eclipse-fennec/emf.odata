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

import java.util.Optional;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.metadata.MetadataHandler;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.emf.osgi.model.metadata.AspectEntry;
import org.eclipse.fennec.emf.osgi.model.metadata.MetadataFactory;
import org.eclipse.fennec.emf.osgi.model.metadata.PackageMetadata;
import org.eclipse.fennec.odata.csdl.OdataResolver;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;

/**
 * OData {@link MetadataHandler} — the E1 seam between the Model Metadata Service and the CSDL
 * layer.
 * <p>
 * ADR-0003: this handler is a thin adapter. The whole OData resolution logic lives in the
 * standalone, service-independent converter ({@link OdataResolver}); here it is merely invoked
 * once per registered {@code EPackage} and its result — a fully resolved
 * {@link ODataPackageProfile} — is attached to the package's metadata as the {@code "odata"}
 * {@link AspectEntry}. Consumers (CSDL, the OData-JSON codec, query type resolution) read it
 * back through {@link MetadataService#getPackageAspect(EPackage, String)} instead of
 * re-resolving the Ecore themselves.
 * <p>
 * There are deliberately no per-EClass or per-feature entries: {@code AspectEntry.content} is a
 * containment, so a class-level entry could not hold the {@link ODataPackageProfile}'s own class
 * profiles, and the profile model already indexes them by name.
 */
public class ODataMetadataHandler implements MetadataHandler {

	/** Aspect type id under which the resolved OData profile is attached. */
	public static final String ASPECT_TYPE_ID = "odata";

	@Override
	public void onPackageRegistered(PackageMetadata packageMetadata) {
		EPackage ePackage = packageMetadata.getEPackage();
		if (ePackage == null) {
			return;
		}
		AspectEntry entry = MetadataFactory.eINSTANCE.createAspectEntry();
		entry.setTypeId(ASPECT_TYPE_ID);
		entry.setContent(new OdataResolver().resolve(ePackage));
		packageMetadata.getAspects().add(entry);
	}

	/**
	 * Reads the OData profile out of a metadata element's aspect entries.
	 * <p>
	 * {@link AspectEntry#getContent()} is a bare {@code EObject} — every consumer would otherwise
	 * repeat the same type id filter plus cast.
	 *
	 * @param aspects the aspect entries of a package
	 * @return the resolved profile, or empty if no OData entry is attached
	 */
	public static Optional<ODataPackageProfile> odataProfile(EList<AspectEntry> aspects) {
		return aspects.stream()
				.filter(entry -> ASPECT_TYPE_ID.equals(entry.getTypeId()))
				.map(AspectEntry::getContent)
				.filter(ODataPackageProfile.class::isInstance)
				.map(ODataPackageProfile.class::cast)
				.findFirst();
	}
}
