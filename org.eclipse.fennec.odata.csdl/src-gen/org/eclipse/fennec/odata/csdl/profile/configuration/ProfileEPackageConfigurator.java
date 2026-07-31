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
package org.eclipse.fennec.odata.csdl.profile.configuration;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;

import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;

import org.eclipse.fennec.odata.csdl.profile.ProfilePackage;

/**
 * <!-- begin-user-doc -->
 * The <b>EPackageConfiguration</b> and <b>ResourceFactoryConfigurator</b> for the model.
 * The package will be registered into a OSGi base model registry.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Standalone, fully-resolved OData profile model owned by the CSDL converter (org.eclipse.fennec.odata.csdl). It is the precomputed artifact produced by resolving an EPackage (+ @OData.* / external config): the OdataResolver fills it, the EDM builder serializes it 1:1 to CSDL. Has NO dependency on the Model Metadata Service — the metadata layer (emf.odata.metadata) composes this model by containment (ADR-0003).
 * <!-- end-model-doc -->
 * @see EPackageConfigurator
 * @generated
 */
public class ProfileEPackageConfigurator implements EPackageConfigurator {
	
	/**
	 * The fingerprint of this model version, computed from the <code>.ecore</code> at build
	 * time. It identifies the model content, not the artifact - see the <code>emf.fingerprint</code>
	 * service property.
	 * @generated
	 */
	public static final String FINGERPRINT = "fp1:dbe35177d9defb8b8d5276082f71c382e705dced4c558eebd6bd9bf46106d3b7";

	private ProfilePackage ePackage;

	protected ProfileEPackageConfigurator(ProfilePackage ePackage){
		this.ePackage = ePackage;
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.emf.osgi.EPackageRegistryConfigurator#configureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
	 * @generated
	 */
	@Override
	public void configureEPackage(org.eclipse.emf.ecore.EPackage.Registry registry) {
		registry.put(ProfilePackage.eNS_URI, ePackage);
	}
	
	/**
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.emf.osgi.EPackageRegistryConfigurator#unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry)
	 * @generated
	 */
	@Override
	public void unconfigureEPackage(org.eclipse.emf.ecore.EPackage.Registry registry) {
		registry.remove(ProfilePackage.eNS_URI);
	}
	
	/**
	 * A method providing the Properties the services around this Model should be registered with.
	 * @generated
	 */
	public Map<String, Object> getServiceProperties() {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(EMFNamespaces.EMF_NAME, ProfilePackage.eNAME);
		properties.put(EMFNamespaces.EMF_MODEL_NSURI, ProfilePackage.eNS_URI);
		properties.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_PROVIDED);
		properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, "profile");
		properties.put(EMFNamespaces.EMF_MODEL_VERSION, "1.0");
		properties.put(EMFNamespaces.EMF_MODEL_FINGERPRINT, FINGERPRINT);
		return properties;
	}
}