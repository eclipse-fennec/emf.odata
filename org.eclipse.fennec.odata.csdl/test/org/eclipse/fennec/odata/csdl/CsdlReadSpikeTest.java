package org.eclipse.fennec.odata.csdl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.junit.jupiter.api.Test;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TAction;
import org.open.oasis.docs.odata.ns.edm.TComplexType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TEntitySet;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TEnumType;
import org.open.oasis.docs.odata.ns.edm.TEnumTypeMember;
import org.open.oasis.docs.odata.ns.edm.TFunction;
import org.open.oasis.docs.odata.ns.edm.TFunctionImport;
import org.open.oasis.docs.odata.ns.edm.TNavigationProperty;
import org.open.oasis.docs.odata.ns.edm.TProperty;
import org.open.oasis.docs.odata.ns.edm.TSingleton;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;

/**
 * E2 read-path spike — deep validation that a real-world {@code $metadata} document (OData
 * TripPin reference service, vendored under {@code testdata/} so CI runs it) parses into the
 * OASIS EDM/EDMX model via plain EMF, faithfully across the whole graph: enum members,
 * complex-type inheritance, entity properties / collections / navigation, entity-type
 * inheritance chains, functions/actions with parameters, the entity container (sets,
 * singleton, imports), and annotations.
 */
class CsdlReadSpikeTest {

	private static String typeOf(Object o) {
		return String.valueOf(o);
	}

	private static <T> Set<String> names(List<T> list, java.util.function.Function<T, String> nameFn) {
		return list.stream().map(nameFn).collect(Collectors.toSet());
	}

	@Test
	void parsesRealTripPinMetadataIntoEdmModel() throws Exception {
		Path metadata = findResource(
				"testdata/trippin-v4-metadata.xml",
				"org.eclipse.fennec.odata.csdl/testdata/trippin-v4-metadata.xml");

		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMLResourceFactoryImpl());
		rs.getPackageRegistry().put(EdmPackage.eNS_URI, EdmPackage.eINSTANCE);
		rs.getPackageRegistry().put(EdmxPackage.eNS_URI, EdmxPackage.eINSTANCE);

		Map<Object, Object> options = new HashMap<>();
		options.put(XMLResource.OPTION_EXTENDED_META_DATA, Boolean.TRUE);

		Resource res = rs.createResource(URI.createFileURI(metadata.toString()));
		res.load(options);

		res.getErrors().forEach(d -> System.out.println("LOAD ERROR: " + d.getMessage()));
		assertTrue(res.getErrors().isEmpty(), "metadata must parse without errors");
		assertFalse(res.getContents().isEmpty(), "resource must have a root");

		EObject rootObj = res.getContents().get(0);
		TEdmx edmx = (rootObj instanceof EdmxRoot er) ? er.getEdmx() : (TEdmx) rootObj;
		assertNotNull(edmx, "edmx element");

		TDataServices dataServices = edmx.getDataServices();
		assertNotNull(dataServices, "DataServices");
		assertEquals(1, dataServices.getSchema().size(), "one schema");
		SchemaType schema = dataServices.getSchema().get(0);
		assertEquals("Microsoft.OData.SampleService.Models.TripPin", schema.getNamespace());

		// --- EnumType: PersonGender { Male=0, Female=1, Unknown=2 } ---
		assertEquals(1, schema.getEnumType().size());
		TEnumType gender = schema.getEnumType().get(0);
		assertEquals("PersonGender", gender.getName());
		Map<String, Long> members = gender.getMember().stream()
				.collect(Collectors.toMap(TEnumTypeMember::getName, TEnumTypeMember::getValue));
		assertEquals(Map.of("Male", 0L, "Female", 1L, "Unknown", 2L), members, "enum members + values");

		// --- ComplexTypes + inheritance ---
		assertEquals(Set.of("City", "Location", "EventLocation", "AirportLocation"),
				names(schema.getComplexType(), TComplexType::getName));
		TComplexType eventLocation = byName(schema.getComplexType(), TComplexType::getName, "EventLocation");
		assertTrue(eventLocation.getBaseType().endsWith(".Location"), "EventLocation extends Location");
		assertTrue(eventLocation.isOpenType(), "EventLocation is open");

		// --- EntityTypes: exact set of 9 ---
		assertEquals(
				Set.of("Photo", "Person", "Airline", "Airport", "PlanItem",
						"PublicTransportation", "Flight", "Event", "Trip"),
				names(schema.getEntityType(), TEntityType::getName));

		// Photo has a media stream
		assertTrue(byName(schema.getEntityType(), TEntityType::getName, "Photo").isHasStream());

		// inheritance chain Flight -> PublicTransportation -> PlanItem
		assertTrue(byName(schema.getEntityType(), TEntityType::getName, "Flight")
				.getBaseType().endsWith(".PublicTransportation"));
		assertTrue(byName(schema.getEntityType(), TEntityType::getName, "PublicTransportation")
				.getBaseType().endsWith(".PlanItem"));

		// --- Person: key, properties, collections, enum-typed prop, navigation ---
		TEntityType person = byName(schema.getEntityType(), TEntityType::getName, "Person");
		assertTrue(person.isOpenType(), "Person is open");
		assertEquals("UserName", person.getKey().get(0).getPropertyRef().get(0).getName());

		Map<String, TProperty> personProps = person.getProperty().stream()
				.collect(Collectors.toMap(TProperty::getName, p -> p));
		assertTrue(personProps.keySet().containsAll(
				Set.of("UserName", "FirstName", "LastName", "Emails", "AddressInfo", "Gender", "Concurrency")),
				"Person properties");
		assertTrue(typeOf(personProps.get("Emails").getType()).contains("Collection"), "Emails is a collection");
		assertTrue(typeOf(personProps.get("AddressInfo").getType()).contains("Collection")
				&& typeOf(personProps.get("AddressInfo").getType()).contains("Location"), "AddressInfo: Collection of Location");
		assertTrue(typeOf(personProps.get("Gender").getType()).contains("PersonGender"), "Gender typed by enum");

		Map<String, TNavigationProperty> personNav = person.getNavigationProperty().stream()
				.collect(Collectors.toMap(TNavigationProperty::getName, n -> n));
		assertTrue(personNav.keySet().containsAll(Set.of("Friends", "Trips", "Photo")), "Person navigation");
		assertTrue(typeOf(personNav.get("Friends").getType()).contains("Collection"), "Friends is a collection");
		assertTrue(personNav.get("Trips").isContainsTarget(), "Trips is a containment navigation");

		// annotations parsed onto a property (Trip.Budget carries ISOCurrency + Scale)
		TEntityType trip = byName(schema.getEntityType(), TEntityType::getName, "Trip");
		TProperty budget = byName(trip.getProperty(), TProperty::getName, "Budget");
		assertFalse(budget.getAnnotation().isEmpty(), "Trip.Budget annotations parsed");
		assertFalse(schema.getAnnotations().isEmpty(), "schema-level <Annotations> block parsed");

		// --- Functions (4) + Actions (2) with parameters ---
		assertEquals(4, schema.getFunction().size());
		TFunction nearest = byName(schema.getFunction(), TFunction::getName, "GetNearestAirport");
		assertFalse(nearest.isIsBound(), "GetNearestAirport is unbound");
		assertEquals(2, nearest.getParameter().size(), "lat + lon");
		assertNotNull(nearest.getReturnType());
		assertTrue(typeOf(nearest.getReturnType().getType()).contains("Airport"));
		assertTrue(byName(schema.getFunction(), TFunction::getName, "GetFavoriteAirline").isIsBound(),
				"GetFavoriteAirline is bound");

		assertEquals(2, schema.getAction().size());
		assertEquals(Set.of("ResetDataSource", "ShareTrip"), names(schema.getAction(), TAction::getName));
		TAction shareTrip = byName(schema.getAction(), TAction::getName, "ShareTrip");
		assertTrue(shareTrip.isIsBound());
		assertEquals(3, shareTrip.getParameter().size(), "person + userName + tripId");
		assertEquals(0, byName(schema.getAction(), TAction::getName, "ResetDataSource").getParameter().size());

		// --- EntityContainer ---
		assertEquals(1, schema.getEntityContainer().size());
		TEntityContainer container = schema.getEntityContainer().get(0);
		assertEquals("DefaultContainer", container.getName());
		assertEquals(Set.of("Photos", "People", "Airlines", "Airports"),
				names(container.getEntitySet(), TEntitySet::getName));

		TEntitySet people = byName(container.getEntitySet(), TEntitySet::getName, "People");
		assertTrue(people.getEntityType().endsWith(".Person"), "People bound to Person");
		assertTrue(people.getNavigationPropertyBinding().size() >= 5, "People navigation bindings");

		TSingleton me = container.getSingleton().get(0);
		assertEquals("Me", me.getName());
		assertTrue(me.getType().endsWith(".Person"));

		TFunctionImport fi = container.getFunctionImport().get(0);
		assertEquals("GetNearestAirport", fi.getName());
		assertTrue(fi.getFunction().endsWith(".GetNearestAirport"));
		assertEquals("Airports", fi.getEntitySet());
		assertTrue(fi.isIncludeInServiceDocument());

		assertEquals("ResetDataSource", container.getActionImport().get(0).getName());

		System.out.println("=== read-path spike OK: full TripPin $metadata graph validated against the EDM model ===");
	}

	private static <T> T byName(List<T> list, java.util.function.Function<T, String> nameFn, String name) {
		return list.stream().filter(t -> name.equals(nameFn.apply(t))).findFirst()
				.orElseThrow(() -> new AssertionError("not found: " + name));
	}

	/** Walk up from the working dir trying each candidate relative path (cwd may be project or workspace root). */
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
