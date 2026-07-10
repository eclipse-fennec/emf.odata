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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.eclipse.fennec.odata.csdl.profile.ODataAnnotation;
import org.eclipse.fennec.odata.csdl.profile.ODataClassProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataNavigationProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataOperationKind;
import org.eclipse.fennec.odata.csdl.profile.ODataOperationProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataPropertyProfile;
import org.eclipse.fennec.odata.csdl.profile.ODataTypeKind;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TActionImport;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TEntitySet;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TFunction;
import org.open.oasis.docs.odata.ns.edm.TFunctionImport;
import org.open.oasis.docs.odata.ns.edm.TNavigationProperty;
import org.open.oasis.docs.odata.ns.edm.TNavigationPropertyBinding;
import org.open.oasis.docs.odata.ns.edm.TProperty;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;

/**
 * E2 annotation-layer test (ADR-0003 Phase 1b): verifies that {@link OdataResolver} applies the
 * {@code @OData.*} {@link org.eclipse.emf.ecore.EAnnotation} overrides ({@link ODataAnnotationConstants})
 * on top of the plain-Ecore defaults — Namespace/Alias, OpenType/HasStream, an explicit Key on a
 * non-{@code iD} attribute, a Type override, MaxLength, Computed, a forced ContainsTarget, an
 * operation-kind override, and unbound operations that surface as container imports. The resulting
 * CSDL is also asserted XSD-valid.
 */
class OdataAnnotationResolutionTest {

	private EcoreHelper ecoreHelper;
	private EPackage model;

	@BeforeEach
	void loadModel() throws Exception {
		Path ecore = findResource("testdata/catalog.ecore",
				"org.eclipse.fennec.odata.csdl/testdata/catalog.ecore");
		ecoreHelper = new EcoreHelper();
		model = ecoreHelper.loadEcore(ecore);
		assertTrue(model.eResource().getErrors().isEmpty(), "catalog.ecore must load cleanly");
	}

	@AfterEach
	void cleanup() {
		if (ecoreHelper != null) {
			ecoreHelper.releaseAll();
		}
	}

	@Test
	void resolvesAnnotationOverridesIntoProfile() {
		ODataPackageProfile profile = new OdataResolver().resolve(model);

		assertEquals("My.Catalog", profile.getNamespace(), "@OData.Namespace override");
		assertEquals("Cat", profile.getAlias(), "@OData.Alias");

		ODataClassProfile doc = byName(profile.getClasses(), ODataClassProfile::getName, "Document");
		assertEquals(ODataTypeKind.ENTITY, doc.getKind(), "entity via @OData.Key, no ecore iD");
		assertTrue(doc.isOpenType());
		assertTrue(doc.isHasStream());
		assertEquals(List.of("docId"), doc.getKeyPropertyNames());

		assertEquals("Edm.Guid", byName(doc.getProperties(), ODataPropertyProfile::getName, "guid").getTypeName(),
				"@OData.Type override");
		assertEquals(200, byName(doc.getProperties(), ODataPropertyProfile::getName, "title").getMaxLength());
		assertTrue(byName(doc.getProperties(), ODataPropertyProfile::getName, "createdAt").isComputed());

		ODataNavigationProfile account = byName(doc.getNavigationProperties(), ODataNavigationProfile::getName, "account");
		assertTrue(account.isContainsTarget(), "ContainsTarget forced on by annotation");

		ODataOperationProfile archive = byName(doc.getOperations(), ODataOperationProfile::getName, "archive");
		assertEquals(ODataOperationKind.ACTION, archive.getKind(), "@OData.OperationKind override (non-void→Action)");
		assertTrue(archive.isBound());

		ODataClassProfile account2 = byName(profile.getClasses(), ODataClassProfile::getName, "Account");
		ODataOperationProfile reset = byName(account2.getOperations(), ODataOperationProfile::getName, "resetAll");
		assertFalse(reset.isBound(), "@OData.Bound=false → unbound");
		assertEquals(ODataOperationKind.ACTION, reset.getKind());
		ODataOperationProfile search = byName(account2.getOperations(), ODataOperationProfile::getName, "searchDocuments");
		assertFalse(search.isBound());
		assertEquals(ODataOperationKind.FUNCTION, search.getKind());
		assertTrue(search.isComposable());
		assertTrue(search.getReturnTypeName().contains("Collection(") && search.getReturnTypeName().contains(".Document"));
	}

	@Test
	void buildsValidCsdlReflectingOverrides() throws Exception {
		SchemaType schema = new EcoreToEdmConverter().toSchema(model);
		assertEquals("My.Catalog", schema.getNamespace());

		TEntityType doc = byName(schema.getEntityType(), TEntityType::getName, "Document");
		assertTrue(doc.isOpenType());
		assertTrue(doc.isHasStream());
		assertEquals("docId", doc.getKey().get(0).getPropertyRef().get(0).getName());
		assertEquals("Edm.Guid", String.valueOf(byName(doc.getProperty(), TProperty::getName, "guid").getType()));
		assertEquals("200", String.valueOf(byName(doc.getProperty(), TProperty::getName, "title").getMaxLength()));
		assertTrue(byName(doc.getNavigationProperty(), p -> p.getName(), "account").isContainsTarget());

		// unbound operations → container imports
		TEntityContainer container = schema.getEntityContainer().get(0);
		assertEquals("resetAll", byName(container.getActionImport(), TActionImport::getName, "resetAll").getName());
		TFunctionImport fi = byName(container.getFunctionImport(), TFunctionImport::getName, "searchDocuments");
		assertTrue(fi.getFunction().endsWith(".searchDocuments"));
		assertTrue(byName(schema.getFunction(), TFunction::getName, "searchDocuments").isIsComposable());

		// AP-5: computed property surfaces as a Core-vocabulary annotation …
		TProperty createdAt = byName(doc.getProperty(), TProperty::getName, "createdAt");
		assertEquals(1, createdAt.getAnnotation().size());
		assertEquals("Org.OData.Core.V1.Computed", createdAt.getAnnotation().get(0).getTerm());
		assertTrue(createdAt.getAnnotation().get(0).isBool1());

		// … and the used vocabulary is declared as an edmx:Reference/Include
		EdmxRoot edmx = new EcoreToEdmConverter().toEdmx(model);
		assertEquals(1, edmx.getEdmx().getReference().size());
		assertEquals("Org.OData.Core.V1", edmx.getEdmx().getReference().get(0).getInclude().get(0).getNamespace());

		assertValidCsdl(serialize(edmx));
	}

	@Test
	void referentialConstraintsAndNavigationBindings() throws Exception {
		// profile: @OData.NavigationProperty.ReferentialConstraint parsed into constraint pairs
		ODataPackageProfile profile = new OdataResolver().resolve(model);
		ODataClassProfile doc = byName(profile.getClasses(), ODataClassProfile::getName, "Document");
		ODataNavigationProfile owner = byName(doc.getNavigationProperties(), ODataNavigationProfile::getName, "owner");
		assertEquals(1, owner.getReferentialConstraints().size());
		assertEquals("ownerId", owner.getReferentialConstraints().get(0).getProperty());
		assertEquals("id", owner.getReferentialConstraints().get(0).getReferencedProperty());

		// EDM write path: ReferentialConstraint element + container-level NavigationPropertyBinding
		SchemaType schema = new EcoreToEdmConverter().toSchema(model);
		TEntityType docType = byName(schema.getEntityType(), TEntityType::getName, "Document");
		TNavigationProperty ownerNav = byName(docType.getNavigationProperty(), TNavigationProperty::getName, "owner");
		assertEquals("ownerId", ownerNav.getReferentialConstraint().get(0).getProperty());
		assertEquals("id", ownerNav.getReferentialConstraint().get(0).getReferencedProperty());

		TEntityContainer container = schema.getEntityContainer().get(0);
		TEntitySet docSet = byName(container.getEntitySet(), TEntitySet::getName, "Document");
		assertEquals(List.of("owner"),
				docSet.getNavigationPropertyBinding().stream().map(TNavigationPropertyBinding::getPath).toList(),
				"owner is bound; the containment nav 'account' must NOT get a binding");
		assertEquals("Account", docSet.getNavigationPropertyBinding().get(0).getTarget());

		assertValidCsdl(serialize(new EcoreToEdmConverter().toEdmx(model)));

		// read path: constraints come back as the @OData annotation (round-trip fidelity)
		EPackage read = new EdmToEcoreConverter().toEPackage(schema);
		EReference readOwner = (EReference) ((EClass) read.getEClassifier("Document")).getEStructuralFeature("owner");
		EAnnotation ann = readOwner.getEAnnotation(ODataAnnotationConstants.SOURCE);
		assertEquals("ownerId=id", ann.getDetails().get(ODataAnnotationConstants.REFERENTIAL_CONSTRAINT));
	}

	@Test
	void genericAnnotationsRoundTrip() throws Exception {
		// resolver: @…/annotations details → profile annotations (package + class level)
		ODataPackageProfile profile = new OdataResolver().resolve(model);
		assertEquals("Product catalog schema",
				byName(profile.getAnnotations(), ODataAnnotation::getTerm, "Org.OData.Core.V1.Description").getValue());
		ODataClassProfile doc = byName(profile.getClasses(), ODataClassProfile::getName, "Document");
		assertEquals("42", byName(doc.getAnnotations(), ODataAnnotation::getTerm, "My.Custom.Rank").getValue());

		// write: constant kind derived lexically — string stays String, "42" becomes Int
		SchemaType schema = new EcoreToEdmConverter().toSchema(model);
		assertEquals("Product catalog schema",
				byName(schema.getAnnotation(), AnnotationType::getTerm, "Org.OData.Core.V1.Description").getString1());
		TEntityType docType = byName(schema.getEntityType(), TEntityType::getName, "Document");
		assertEquals(BigInteger.valueOf(42),
				byName(docType.getAnnotation(), AnnotationType::getTerm, "My.Custom.Rank").getInt1());
		assertEquals("A stored document",
				byName(docType.getAnnotation(), AnnotationType::getTerm, "Org.OData.Core.V1.Description").getString1());
		assertValidCsdl(serialize(new EcoreToEdmConverter().toEdmx(model)));

		// read: generic <Annotation> elements come back as the @…/annotations EAnnotation
		EPackage read = new EdmToEcoreConverter().toEPackage(schema);
		assertEquals("Product catalog schema",
				read.getEAnnotation(ODataAnnotationConstants.ANNOTATIONS_SOURCE)
						.getDetails().get("Org.OData.Core.V1.Description"));
		EAnnotation readDoc = ((EClass) read.getEClassifier("Document"))
				.getEAnnotation(ODataAnnotationConstants.ANNOTATIONS_SOURCE);
		assertEquals("A stored document", readDoc.getDetails().get("Org.OData.Core.V1.Description"));
		assertEquals("42", readDoc.getDetails().get("My.Custom.Rank"));
	}

	@Test
	void richExpressionAnnotationsRoundTrip() throws Exception {
		// rich values in their CSDL-JSON encoding as annotation details (AP-5 rest)
		String budget = "{\"@type\":\"My.Custom.BudgetType\",\"Amount\":3000,"
				+ "\"Currency\":\"USD\",\"Approved\":true}";
		String tags = "[\"a\",\"b\",2.5]";
		String permissions = "{\"$EnumMember\":\"Org.OData.Core.V1.Permission/Read\"}";
		String titlePath = "{\"$Path\":\"title\"}";
		String paths = "[{\"$PropertyPath\":\"title\"},{\"$PropertyPath\":\"createdAt\"}]";
		EClass documentClass = (EClass) model.getEClassifier("Document");
		EAnnotation holder = documentClass.getEAnnotation(ODataAnnotationConstants.ANNOTATIONS_SOURCE);
		holder.getDetails().put("My.Custom.Budget", budget);
		holder.getDetails().put("My.Custom.Tags", tags);
		holder.getDetails().put("Org.OData.Core.V1.Permissions", permissions);
		holder.getDetails().put("My.Custom.TitlePath", titlePath);
		holder.getDetails().put("My.Custom.Paths", paths);

		// write: the JSON encoding becomes structural EDM expressions
		SchemaType schema = new EcoreToEdmConverter().toSchema(model);
		TEntityType docType = byName(schema.getEntityType(), TEntityType::getName, "Document");
		AnnotationType record = byName(docType.getAnnotation(), AnnotationType::getTerm, "My.Custom.Budget");
		assertEquals("My.Custom.BudgetType", record.getRecord().get(0).getType());
		assertEquals(3, record.getRecord().get(0).getPropertyValue().size());
		AnnotationType collection = byName(docType.getAnnotation(), AnnotationType::getTerm, "My.Custom.Tags");
		assertEquals(2, collection.getCollection().get(0).getString().size());
		assertEquals(1, collection.getCollection().get(0).getDecimal().size());
		assertEquals(List.of("Org.OData.Core.V1.Permission/Read"),
				byName(docType.getAnnotation(), AnnotationType::getTerm, "Org.OData.Core.V1.Permissions")
						.getEnumMember1());
		assertEquals("title",
				byName(docType.getAnnotation(), AnnotationType::getTerm, "My.Custom.TitlePath").getPath1());
		assertValidCsdl(serialize(new EcoreToEdmConverter().toEdmx(model)));

		// read: back to the SAME canonical JSON detail strings (round-trip fidelity)
		EPackage read = new EdmToEcoreConverter().toEPackage(schema);
		EAnnotation readDoc = ((EClass) read.getEClassifier("Document"))
				.getEAnnotation(ODataAnnotationConstants.ANNOTATIONS_SOURCE);
		assertEquals(budget, readDoc.getDetails().get("My.Custom.Budget"));
		assertEquals(tags, readDoc.getDetails().get("My.Custom.Tags"));
		assertEquals(permissions, readDoc.getDetails().get("Org.OData.Core.V1.Permissions"));
		assertEquals(titlePath, readDoc.getDetails().get("My.Custom.TitlePath"));
		assertEquals(paths, readDoc.getDetails().get("My.Custom.Paths"));

		// the CSDL JSON wire form carries the same value nodes (writer ∘ reader fixpoint)
		String json = CsdlJsonWriter.write(new EcoreToEdmConverter().toEdmx(model));
		assertTrue(json.replaceAll("\\s", "").contains("\"@My.Custom.Budget\":"
				+ budget.replaceAll("\\s", "")), json);
		EPackage viaJson = new EdmToEcoreConverter().toEPackages(
				new CsdlJsonReader().read(json).getEdmx()).get(0);
		assertEquals(budget, ((EClass) viaJson.getEClassifier("Document"))
				.getEAnnotation(ODataAnnotationConstants.ANNOTATIONS_SOURCE)
				.getDetails().get("My.Custom.Budget"));
	}

	@Test
	void sridAndUnicodeFacetsRoundTrip() throws Exception {
		EClass documentClass = (EClass) model.getEClassifier("Document");
		EAttribute title = (EAttribute) documentClass.getEStructuralFeature("title");
		EAnnotation odata = title.getEAnnotation(ODataAnnotationConstants.SOURCE);
		odata.getDetails().put(ODataAnnotationConstants.SRID, "variable");
		odata.getDetails().put(ODataAnnotationConstants.UNICODE, "false");

		ODataPackageProfile profile = new OdataResolver().resolve(model);
		ODataPropertyProfile titleProfile = byName(
				byName(profile.getClasses(), ODataClassProfile::getName, "Document").getProperties(),
				ODataPropertyProfile::getName, "title");
		assertEquals("variable", titleProfile.getSrid(), "symbolic SRID kept verbatim");
		assertEquals(Boolean.FALSE, titleProfile.getUnicode());

		SchemaType schema = new EcoreToEdmConverter().toSchema(model);
		TProperty titleProperty = byName(
				byName(schema.getEntityType(), TEntityType::getName, "Document").getProperty(),
				TProperty::getName, "title");
		assertEquals("variable", String.valueOf(titleProperty.getSRID()));
		assertTrue(titleProperty.isSetUnicode());
		assertFalse(titleProperty.isUnicode());
		assertValidCsdl(serialize(new EcoreToEdmConverter().toEdmx(model)));

		// read path: ALL facets come back as @OData annotation details (AP-4 closure)
		EPackage read = new EdmToEcoreConverter().toEPackage(schema);
		EAnnotation readAnnotation = ((EClass) read.getEClassifier("Document"))
				.getEStructuralFeature("title").getEAnnotation(ODataAnnotationConstants.SOURCE);
		assertEquals("variable", readAnnotation.getDetails().get(ODataAnnotationConstants.SRID));
		assertEquals("false", readAnnotation.getDetails().get(ODataAnnotationConstants.UNICODE));
		assertEquals("200", readAnnotation.getDetails().get(ODataAnnotationConstants.MAX_LENGTH),
				"MaxLength survives the read now too");
	}

	@Test
	void crossPackageTypeReferences() {
		EcoreFactory ecore = EcoreFactory.eINSTANCE;
		EPackage basePkg = ecore.createEPackage();
		basePkg.setName("base");
		basePkg.setNsPrefix("base");
		basePkg.setNsURI("http://example.org/base");
		EClass root = ecore.createEClass();
		root.setName("Root");
		EAttribute rootId = ecore.createEAttribute();
		rootId.setName("id");
		rootId.setID(true);
		rootId.setLowerBound(1);
		rootId.setEType(EcorePackage.Literals.ESTRING);
		root.getEStructuralFeatures().add(rootId);
		basePkg.getEClassifiers().add(root);

		EPackage extPkg = ecore.createEPackage();
		extPkg.setName("ext");
		extPkg.setNsPrefix("ext");
		extPkg.setNsURI("http://example.org/ext");
		EClass sub = ecore.createEClass();
		sub.setName("Sub");
		sub.getESuperTypes().add(root);
		extPkg.getEClassifiers().add(sub);
		EClass holder = ecore.createEClass();
		holder.setName("Holder");
		EAttribute holderId = ecore.createEAttribute();
		holderId.setName("id");
		holderId.setID(true);
		holderId.setLowerBound(1);
		holderId.setEType(EcorePackage.Literals.ESTRING);
		holder.getEStructuralFeatures().add(holderId);
		EReference navigation = ecore.createEReference();
		navigation.setName("root");
		navigation.setEType(root);
		holder.getEStructuralFeatures().add(navigation);
		extPkg.getEClassifiers().add(holder);

		// AP-6 write path: types from ANOTHER package qualify with THAT package's namespace
		ODataPackageProfile profile = new OdataResolver().resolve(extPkg);
		assertEquals("base.Root",
				byName(profile.getClasses(), ODataClassProfile::getName, "Sub")
						.getBaseTypeQualifiedName());
		assertEquals("base.Root",
				byName(byName(profile.getClasses(), ODataClassProfile::getName, "Holder")
						.getNavigationProperties(), ODataNavigationProfile::getName, "root")
						.getTypeName());
	}

	// === helpers ===

	private static <T> T byName(List<T> list, Function<T, String> nameFn, String name) {
		return list.stream().filter(t -> name.equals(nameFn.apply(t))).findFirst()
				.orElseThrow(() -> new AssertionError("not found: " + name));
	}

	private static String serialize(EdmxRoot root) throws Exception {
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
		rs.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
		rs.getPackageRegistry().put(EdmxPackage.eNS_URI, EdmxPackage.eINSTANCE);
		Resource res = rs.createResource(URI.createURI("metadata.xml"));
		res.getContents().add(root);
		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);
		options.put(XMLResource.OPTION_ENCODING, "UTF-8");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		res.save(out, options);
		return out.toString(StandardCharsets.UTF_8);
	}

	private static void assertValidCsdl(String xml) throws Exception {
		File edmxXsd = findResource("testdata/schemas/edmx.xsd",
				"org.eclipse.fennec.odata.csdl/testdata/schemas/edmx.xsd").toFile();
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Validator validator = sf.newSchema(edmxXsd).newValidator();
		validator.validate(new StreamSource(new StringReader(xml)));
	}

	private static Path findResource(String... candidatesRelative) {
		Path start = Paths.get("").toAbsolutePath();
		for (Path dir = start; dir != null; dir = dir.getParent()) {
			for (String rel : candidatesRelative) {
				Path p = dir.resolve(rel);
				if (Files.exists(p)) {
					return p;
				}
			}
		}
		throw new IllegalStateException("test resource not found from " + start);
	}
}
