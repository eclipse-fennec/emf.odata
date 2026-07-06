package org.eclipse.fennec.odata.csdl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.helper.EcoreHelper;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TEntitySet;
import org.open.oasis.docs.odata.ns.edm.TEntityKeyElement;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TProperty;
import org.open.oasis.docs.odata.ns.edm.TPropertyRef;
import org.open.oasis.docs.odata.ns.edmx.EdmxFactory;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;
import org.open.oasis.docs.odata.ns.edmx.TVersion;

/**
 * E2 write-path test — proves the architectural bet: we BUILD CSDL ({@code $metadata}) by
 * populating an in-memory EDM/EDMX model instance and letting plain EMF serialize it (XML
 * mapping comes from the OASIS-XSD-derived ExtendedMetaData), instead of hand-writing a CSDL
 * writer. Both cases assert the output is <strong>schema-valid</strong> against the vendored
 * OASIS CSDL XSDs ({@code testdata/schemas/edmx.xsd} + {@code edm.xsd}) — not just substring
 * checks — so a structurally wrong document fails the test.
 */
class CsdlSerializationSpikeTest {

	@Test
	void buildsValidCsdlXmlFromHandBuiltEdmModel() throws Exception {
		EdmFactory edm = EdmFactory.eINSTANCE;
		EdmxFactory edmx = EdmxFactory.eINSTANCE;

		TProperty id = edm.createTProperty();
		id.setName("Id");
		id.setType("Edm.String");
		id.setNullable(false);

		TProperty name = edm.createTProperty();
		name.setName("Name");
		name.setType("Edm.String");

		TPropertyRef idRef = edm.createTPropertyRef();
		idRef.setName("Id");
		TEntityKeyElement key = edm.createTEntityKeyElement();
		key.getPropertyRef().add(idRef);

		TEntityType person = edm.createTEntityType();
		person.setName("Person");
		person.getKey().add(key);
		person.getProperty().add(id);
		person.getProperty().add(name);

		TEntityContainer container = edm.createTEntityContainer();
		container.setName("Container");
		// the CSDL XSD requires a container to expose at least one element
		TEntitySet people = edm.createTEntitySet();
		people.setName("People");
		people.setEntityType("Demo.Person");
		container.getEntitySet().add(people);

		SchemaType schema = edm.createSchemaType();
		schema.setNamespace("Demo");
		schema.getEntityType().add(person);
		schema.getEntityContainer().add(container);

		TDataServices ds = edmx.createTDataServices();
		ds.getSchema().add(schema);
		TEdmx edmxElement = edmx.createTEdmx();
		edmxElement.setVersion(TVersion._40);
		edmxElement.setDataServices(ds);
		EdmxRoot root = edmx.createEdmxRoot();
		root.setEdmx(edmxElement);

		String xml = serialize(root);
		System.out.println("=== CSDL spike output (hand-built) ===");
		System.out.println(xml);

		assertTrue(xml.contains("http://docs.oasis-open.org/odata/ns/edm"), "edm namespace");
		assertValidCsdl(xml);
	}

	/**
	 * Stronger end-to-end assertion: run the real {@link EcoreToEdmConverter} over the rich
	 * {@code company.ecore} model and verify the serialized {@code $metadata} is XSD-valid —
	 * i.e. the converter emits spec-compliant CSDL for inheritance, complex types, enums,
	 * navigation with partners, composite keys and containment.
	 */
	@Test
	void convertsCompanyModelToValidCsdl() throws Exception {
		EcoreHelper helper = new EcoreHelper();
		try {
			Path ecore = findResource("testdata/company.ecore",
					"org.eclipse.fennec.odata.csdl/testdata/company.ecore");
			EdmxRoot root = new EcoreToEdmConverter().toEdmx(helper.loadEcore(ecore));

			String xml = serialize(root);
			System.out.println("=== CSDL from EcoreToEdmConverter(company.ecore) ===");
			System.out.println(xml);

			assertValidCsdl(xml);
		} finally {
			helper.releaseAll();
		}
	}

	// === helpers ===

	/** Serialize an EDMX document via plain EMF + ExtendedMetaData (the production write path). */
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

	/** Validate a CSDL document against the vendored OASIS XSDs (edmx.xsd imports edm.xsd). */
	private static void assertValidCsdl(String xml) throws Exception {
		File edmxXsd = findResource("testdata/schemas/edmx.xsd",
				"org.eclipse.fennec.odata.csdl/testdata/schemas/edmx.xsd").toFile();
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(edmxXsd); // relative import of edm.xsd resolves next to edmx.xsd
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(new StringReader(xml))); // throws SAXException on invalid CSDL
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
