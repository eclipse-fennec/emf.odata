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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.emf.osgi.metadata.MetadataHandler;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.emf.osgi.model.metadata.AspectEntry;
import org.eclipse.fennec.emf.osgi.model.metadata.PackageMetadata;
import org.eclipse.fennec.odata.csdl.profile.ODataClassProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataTypeKind;
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
 * {@code MetadataServiceComponent} whiteboard to the {@code ODataMetadataHandlerComponent},
 * which attaches the standalone CSDL profile (built by {@code OdataResolver}) as the
 * {@code "odata"} {@link AspectEntry} of the package's metadata.
 * <p>
 * Every assertion goes through {@link MetadataService#getPackageMetadata(String)} — the
 * registry-only lookup. The {@code EPackage} overload resolves-or-builds on demand and would
 * therefore succeed even without the whiteboard, which is exactly what these tests must not
 * assume.
 * <p>
 * Mirrors the setup of {@code org.eclipse.fennec.codec.osgi.tests.BasicRoundTripExample}:
 * the test model is a {@code .ecore} file (annotation-rich {@code catalog.ecore}, same
 * fixture as the csdl bundle's {@code OdataAnnotationResolutionTest}) loaded via
 * {@link EcoreHelper} — no programmatic EPackage construction.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@DisplayName("ODataMetadataHandler whiteboard integration")
public class ODataMetadataHandlerIntegrationTest {

	private static final String ECORE = "/org/eclipse/fennec/odata/metadata/tests/catalog.ecore";
	/** Aspect type id of the OData handler (ODataMetadataHandler.ASPECT_TYPE_ID, package-private bundle-side). */
	private static final String ODATA = "odata";
	/** DS component name of the handler under test — its identity without exporting the class. */
	private static final String COMPONENT_NAME = "ODataMetadataHandlerComponent";

	@InjectService
	MetadataService metadataService;

	private EcoreHelper ecoreHelper;
	private EPackage pkg;
	private ServiceRegistration<EPackage> pkgReg;

	@BeforeEach
	public void setUp(@InjectBundleContext BundleContext ctx) throws IOException {
		ecoreHelper = new EcoreHelper();
		pkg = ecoreHelper.loadEcore(ECORE, ODataMetadataHandlerIntegrationTest.class);
		// Registering as OSGi service triggers MetadataServiceComponent.addEPackage()
		// which applies ODataMetadataHandlerComponent automatically.
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
	@DisplayName("ODataMetadataHandlerComponent is registered as MetadataHandler service")
	public void odataHandlerIsRegistered(@InjectService ServiceAware<MetadataHandler> handlers) {
		// other whiteboard handlers (codec, ...) register too — the odata one must be among them
		long deadline = System.currentTimeMillis() + 5_000;
		while (System.currentTimeMillis() < deadline) {
			if (handlers.getServiceReferences().stream()
					.anyMatch(ref -> COMPONENT_NAME.equals(ref.getProperty("component.name")))) {
				return;
			}
			sleepOrFail("interrupted while waiting for the odata MetadataHandler");
		}
		fail("no MetadataHandler named '" + COMPONENT_NAME + "' registered within 5s: "
				+ handlers.getServiceReferences().stream().map(ref -> ref.getProperty("component.name")).toList());
	}

	@Test
	@DisplayName("EPackage registration attaches the resolved OData profile as the 'odata' aspect")
	public void packageAspectIsAttached() {
		ODataPackageProfile profile = awaitOdataProfile();

		assertEquals("My.Catalog", profile.getNamespace(), "@OData.Namespace override from catalog.ecore");
		assertEquals("Cat", profile.getAlias(), "@OData.Alias");
	}

	@Test
	@DisplayName("The attached profile carries the fully resolved class profiles")
	public void attachedProfileCarriesClassProfiles() {
		ODataPackageProfile profile = awaitOdataProfile();

		ODataClassProfile document = byName(profile.getClasses(), ODataClassProfile::getName, "Document");
		assertEquals(ODataTypeKind.ENTITY, document.getKind(), "entity via @OData.Key, no ecore iD");
		assertEquals(List.of("docId"), document.getKeyPropertyNames());
		assertTrue(document.isOpenType(), "@OData.OpenType");
		assertTrue(document.isHasStream(), "@OData.HasStream");
		assertTrue(byName(document.getNavigationProperties(), ODataNavigationProfile::getName, "account")
				.isContainsTarget(), "@OData.NavigationProperty.ContainsTarget");

		// the class profiles live INSIDE the aspect's containment tree
		assertSame(profile, document.eContainer());
	}

	@Test
	@DisplayName("Unregistering the EPackage removes the metadata again")
	public void metadataRemovedOnPackageUnregistration() {
		awaitOdataProfile();

		pkgReg.unregister();
		pkgReg = null;

		assertTrue(metadataService.getPackageMetadata(pkg.getNsURI()).isEmpty(),
				"package metadata gone after unregistration");
	}

	/**
	 * The EPackage/MetadataHandler whiteboard binds dynamically, so the metadata may appear
	 * shortly after {@code registerService} returns — poll briefly instead of asserting
	 * immediately.
	 */
	private ODataPackageProfile awaitOdataProfile() {
		long deadline = System.currentTimeMillis() + 5_000;
		while (System.currentTimeMillis() < deadline) {
			Optional<AspectEntry> entry = metadataService.getPackageMetadata(pkg.getNsURI())
					.flatMap(ODataMetadataHandlerIntegrationTest::odataAspect);
			if (entry.isPresent()) {
				return assertInstanceOf(ODataPackageProfile.class, entry.get().getContent(),
						"the odata aspect content is the resolved CSDL profile");
			}
			sleepOrFail("interrupted while waiting for the OData package aspect");
		}
		return fail("no OData aspect built within 5s for " + pkg.getNsURI());
	}

	private static Optional<AspectEntry> odataAspect(PackageMetadata metadata) {
		return metadata.getAspects().stream().filter(entry -> ODATA.equals(entry.getTypeId())).findFirst();
	}

	private static void sleepOrFail(String message) {
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			fail(message);
		}
	}

	private static <T> T byName(List<T> list, Function<T, String> nameGetter, String name) {
		return list.stream().filter(e -> name.equals(nameGetter.apply(e))).findFirst()
				.orElseGet(() -> fail("no element named '" + name + "'"));
	}
}
