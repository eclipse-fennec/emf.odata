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
package org.eclipse.fennec.odata.metadata.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.model.metadata.ClassProfile;
import org.eclipse.fennec.model.metadata.PackageProfile;
import org.eclipse.fennec.model.metadata.api.AspectProvider;
import org.eclipse.fennec.model.metadata.api.MetadataService;
import org.eclipse.fennec.odata.metadata.odata.ODataClassProfile;
import org.eclipse.fennec.odata.metadata.odata.ODataPackageProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * OSGi integration tests for the E1 whiteboard wiring (ADR-0003 Phase 2):
 * registering an {@link EPackage} as an OSGi service must flow through the
 * {@code MetadataServiceComponent} whiteboard to the {@code ODataAspectProviderComponent},
 * which composes the standalone CSDL profile (built by {@code OdataResolver}) into the
 * metadata-side {@link ODataPackageProfile} / {@link ODataClassProfile}.
 * <p>
 * Mirrors the setup of {@code org.eclipse.fennec.codec.osgi.tests.BasicRoundTripExample}:
 * the test model is a {@code .ecore} file (annotation-rich {@code catalog.ecore}, same
 * fixture as the csdl bundle's {@code OdataAnnotationResolutionTest}) loaded via
 * {@link EcoreHelper} — no programmatic EPackage construction.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@DisplayName("ODataAspectProvider whiteboard integration")
public class ODataAspectProviderIntegrationTest {

	private static final String ECORE = "/org/eclipse/fennec/odata/metadata/tests/catalog.ecore";
	/** Aspect type id of the OData provider (ODataAspectProvider.ASPECT_TYPE_ID, package-private bundle-side). */
	private static final String ODATA = "odata";

	@InjectService
	MetadataService metadataService;

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private ServiceRegistration<EPackage> pkgReg;

	@BeforeEach
	public void setUp(@InjectBundleContext BundleContext ctx) throws IOException {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(ECORE, ODataAspectProviderIntegrationTest.class);
		// Registering as OSGi service triggers MetadataServiceComponent.addEPackage()
		// which applies ODataAspectProviderComponent automatically.
		pkgReg = ctx.registerService(EPackage.class, pkg, null);
	}

	@AfterEach
	public void tearDown() {
		if (pkgReg != null) {
			pkgReg.unregister();
			pkgReg = null;
		}
		ecoreHelper.releaseAll();
	}

	@Test
	@DisplayName("ODataAspectProviderComponent is registered as AspectProvider service")
	public void odataAspectProviderIsRegistered(
			@InjectService ServiceAware<AspectProvider> providers) {
		// other whiteboard providers (codec, ...) register too — the odata one must be among them
		long deadline = System.currentTimeMillis() + 5_000;
		while (System.currentTimeMillis() < deadline) {
			if (providers.getServices().stream()
					.anyMatch(provider -> ODATA.equals(provider.getAspectTypeId()))) {
				return;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("interrupted while waiting for the odata AspectProvider");
			}
		}
		fail("no AspectProvider with type id '" + ODATA + "' registered within 5s: "
				+ providers.getServices().stream().map(AspectProvider::getAspectTypeId).toList());
	}

	@Test
	@DisplayName("EPackage registration builds the composed OData package profile")
	public void packageProfileIsComposed() {
		PackageProfile profile = awaitPackageProfile();

		ODataPackageProfile odataProfile = assertInstanceOf(ODataPackageProfile.class, profile);
		assertEquals(ODATA, odataProfile.getTypeId());
		assertEquals("My.Catalog", odataProfile.getNamespace(), "@OData.Namespace override from catalog.ecore");

		org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile csdlProfile = odataProfile.getOdataProfile();
		assertNotNull(csdlProfile, "resolved CSDL profile must be composed by containment");
		assertSame(odataProfile, csdlProfile.eContainer(), "composition = containment, not a cross-ref");
		assertEquals("My.Catalog", csdlProfile.getNamespace());
		assertEquals("Cat", csdlProfile.getAlias(), "@OData.Alias");
	}

	@Test
	@DisplayName("Class profiles cross-reference into the contained CSDL profile tree")
	public void classProfileCrossReferencesCsdlProfile() {
		awaitPackageProfile();

		EClass documentClass = EcoreHelper.getEClass(pkg, "Document");
		ClassProfile classProfile = metadataService.getClassProfile(documentClass, ODATA);

		ODataClassProfile odataClassProfile = assertInstanceOf(ODataClassProfile.class, classProfile);
		assertSame(documentClass, odataClassProfile.getEClass());

		org.eclipse.fennec.odata.csdl.profile.ODataClassProfile csdlClass = odataClassProfile.getOdataProfile();
		assertNotNull(csdlClass, "cross-ref into the contained CSDL profile tree");
		assertEquals("Document", csdlClass.getName());
		assertEquals(org.eclipse.fennec.odata.csdl.profile.ODataTypeKind.ENTITY, csdlClass.getKind(),
				"entity via @OData.Key, no ecore iD");
		assertEquals(List.of("docId"), csdlClass.getKeyPropertyNames());
		assertTrue(csdlClass.isOpenType(), "@OData.OpenType");
		assertTrue(csdlClass.isHasStream(), "@OData.HasStream");
		assertTrue(byName(csdlClass.getNavigationProperties(),
				org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile::getName, "account").isContainsTarget(),
				"@OData.NavigationProperty.ContainsTarget");

		// the cross-referenced class profile lives INSIDE the package profile's containment tree
		ODataPackageProfile packageProfile = (ODataPackageProfile) metadataService.getPackageProfile(pkg, ODATA);
		assertSame(packageProfile.getOdataProfile(), csdlClass.eContainer());
	}

	@Test
	@DisplayName("Unregistering the EPackage removes the OData profile again")
	public void profileRemovedOnPackageUnregistration() {
		awaitPackageProfile();

		pkgReg.unregister();
		pkgReg = null;

		assertNull(metadataService.getPackageMetadata(pkg.getNsURI()), "package metadata gone after unregistration");
	}

	/**
	 * The EPackage/AspectProvider whiteboard binds dynamically, so the profile may appear
	 * shortly after {@code registerService} returns — poll briefly instead of asserting
	 * immediately.
	 */
	private PackageProfile awaitPackageProfile() {
		long deadline = System.currentTimeMillis() + 5_000;
		while (System.currentTimeMillis() < deadline) {
			PackageProfile profile = metadataService.getPackageProfile(pkg, ODATA);
			if (profile != null) {
				return profile;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("interrupted while waiting for the OData package profile");
			}
		}
		return fail("no OData package profile built within 5s for " + pkg.getNsURI());
	}

	private static <T> T byName(List<T> list, Function<T, String> nameGetter, String name) {
		return list.stream().filter(e -> name.equals(nameGetter.apply(e))).findFirst()
				.orElseGet(() -> fail("no element named '" + name + "'"));
	}
}
