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
package org.eclipse.fennec.odata.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.fennec.odata.ocl.evaluator.OclEvaluationException;
import org.eclipse.fennec.odata.ocl.evaluator.OclEvaluator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@code $filter} and {@code $orderby} over the XSD datatypes of {@link XMLTypePackage} (#144).
 * An Ecore generated from an XSD types its features with these, and since {@code EdmTypes}
 * publishes them as the {@code Edm.*} primitive they are modelled on, the evaluator has to be
 * able to evaluate the order {@code $metadata} promises: the value classes are
 * {@code XMLGregorianCalendar} and {@code javax.xml.datatype.Duration}, neither of which is
 * {@link Comparable}, so every comparison used to fail with "cannot compare XMLCalendar with
 * String" — a 400 for a legal query.
 *
 * <p>The invariant behind the assertions: comparing is by VALUE, never by representation — the
 * same instant in two offsets is equal, a duration compares by its length — and {@code $orderby}
 * derives its key from the very same semantics, so sorting and filtering cannot disagree.
 */
@DisplayName("$filter/$orderby over XSD temporal datatypes (XMLTypePackage)")
class XsdTemporalFilterTest {

	private static final XMLTypePackage XSD = XMLTypePackage.eINSTANCE;

	private final EPackage model = model();
	private final EClass dataset = (EClass) model.getEClassifier("Dataset");

	@Test
	@DisplayName("the XSD temporal types compare against their OData literal")
	void comparesAgainstTheMatchingLiteral() {
		EObject row = row("2024-05-03", "2024-05-03T10:15:30Z", "08:30:00", "P1DT2H", "2024");

		assertTrue(matches("issued gt 2024-01-01", row));
		assertTrue(matches("issued eq 2024-05-03", row));
		assertFalse(matches("issued lt 2024-05-03", row));

		assertTrue(matches("modified gt 2024-01-01T00:00:00Z", row));
		assertTrue(matches("modified eq 2024-05-03T10:15:30Z", row));
		assertFalse(matches("modified gt 2024-05-03T10:15:31Z", row));

		assertTrue(matches("opens gt 08:00:00", row));
		assertTrue(matches("opens eq 08:30:00", row));
		assertFalse(matches("opens gt 09:00:00", row));
	}

	@Test
	@DisplayName("a duration compares by its length, not by its lexical form")
	void durationComparesByLength() {
		EObject row = row("2024-05-03", "2024-05-03T10:15:30Z", "08:30:00", "PT10H", "2024");

		// the regression this guards: lexically "PT10H" < "PT9H", by length it is longer
		assertTrue(matches("resolution gt duration'PT9H'", row));
		assertFalse(matches("resolution lt duration'PT9H'", row));
		assertTrue(matches("resolution eq duration'PT600M'", row), "the same length, written differently");
		assertTrue(matches("resolution eq duration'PT10H'", row));
	}

	@Test
	@DisplayName("the same instant in two offsets is one value")
	void equalInstantsInDifferentOffsetsAreEqual() {
		EObject row = row("2024-05-03", "2024-05-03T12:15:30+02:00", "08:30:00", "P1DT2H", "2024");

		// ZonedDateTime.compareTo falls back to the local time and the zone id, which would rank
		// 10:15Z below 12:15+02:00 although they are the same moment — comparison is by instant
		assertTrue(matches("modified eq 2024-05-03T10:15:30Z", row));
		assertFalse(matches("modified gt 2024-05-03T10:15:30Z", row));
		assertFalse(matches("modified lt 2024-05-03T10:15:30Z", row));
	}

	@Test
	@DisplayName("a date-only value ignores a timezone it carries, like the payload does")
	void dateOnlyValueIgnoresItsTimezone() {
		// xs:date MAY carry a timezone, Edm.Date never does, and the JSON codec writes the date
		// fields alone — so the filter must read the same value the payload shows
		EObject row = row("2024-05-03+02:00", "2024-05-03T10:15:30Z", "08:30:00", "P1DT2H", "2024");

		assertTrue(matches("issued eq 2024-05-03", row));
		assertFalse(matches("issued lt 2024-05-03", row));
	}

	@Test
	@DisplayName("the date/time component functions read an XSD calendar")
	void componentFunctionsReadACalendar() {
		EObject row = row("2024-05-03", "2024-05-03T12:15:30+02:00", "08:30:45", "P1DT2H", "2024");

		assertTrue(matches("year(issued) eq 2024", row));
		assertTrue(matches("month(issued) eq 5", row));
		assertTrue(matches("day(issued) eq 3", row));
		// a component is read in the value's OWN offset (12:15+02:00), the comparison by instant
		assertTrue(matches("hour(modified) eq 12", row));
		assertTrue(matches("minute(modified) eq 15", row));
		assertTrue(matches("second(opens) eq 45", row));
		assertTrue(matches("date(modified) eq '2024-05-03'", row));
	}

	@Test
	@DisplayName("the partial calendars (gYear, gMonth, …) compare as the Edm.String they are")
	void partialCalendarsCompareLexically() {
		EObject row = row("2024-05-03", "2024-05-03T10:15:30Z", "08:30:00", "P1DT2H", "2024");

		// xs:gYear denotes no instant and EdmTypes publishes it as Edm.String; its XSD lexical
		// form is zero-padded, so it still orders like the value
		assertTrue(matches("vintage eq '2024'", row));
		assertTrue(matches("vintage gt '2023'", row));
		assertFalse(matches("vintage gt '2025'", row));
	}

	@Test
	@DisplayName("a duration in years or months is refused, never ordered on a guess")
	void yearMonthDurationIsRefused() {
		EObject row = row("2024-05-03", "2024-05-03T10:15:30Z", "08:30:00", "P1M", "2024");

		// P1M is neither longer nor shorter than P30D — answering either way would be the
		// "successful and wrong" failure mode the architecture rules out
		OclEvaluationException refused = assertThrows(OclEvaluationException.class,
				() -> matches("resolution gt duration'P30D'", row));
		assertTrue(refused.getMessage().contains("no fixed length"), refused.getMessage());

		// equality, however, IS decidable: eq/ne must not refuse what only lt/gt has to
		assertTrue(matches("resolution eq duration'P1M'", row));
		assertFalse(matches("resolution eq duration'P30D'", row), "merely not-equal, not an error");
		assertTrue(matches("resolution ne duration'P30D'", row));

		assertThrows(IllegalArgumentException.class,
				() -> OclEvaluator.orderingKey(xsd(XSD.getDuration(), "P1Y")),
				"$orderby must refuse it too, not sort on an invented month length");
	}

	@Test
	@DisplayName("$orderby keys agree with the relational operators")
	void orderingKeysAgreeWithComparisons() {
		assertEquals(Instant.parse("2024-05-03T00:00:00Z"),
				OclEvaluator.orderingKey(xsd(XSD.getDate(), "2024-05-03")));
		assertEquals(Instant.parse("2024-05-03T10:15:30Z"),
				OclEvaluator.orderingKey(xsd(XSD.getDateTime(), "2024-05-03T12:15:30+02:00")),
				"ordered by instant, so two offsets of one moment sort as one value");
		assertEquals(new BigDecimal("93600"), OclEvaluator.orderingKey(xsd(XSD.getDuration(), "P1DT2H")));
		assertEquals(new BigDecimal("1000"), OclEvaluator.orderingKey(new BigInteger("1000")));
		assertEquals("2024", OclEvaluator.orderingKey(xsd(XSD.getGYear(), "2024")));

		// the actual promise: sorting by the key puts the rows in the order `lt` reports
		List<String> durations = new ArrayList<>(List.of("PT9H", "P1DT2H", "PT30M", "PT10H"));
		durations.sort(Comparator.comparing(d -> OclEvaluator.orderingKey(xsd(XSD.getDuration(), d))));
		assertEquals(List.of("PT30M", "PT9H", "PT10H", "P1DT2H"), durations);

		EObject row = row("2024-05-03", "2024-05-03T10:15:30Z", "08:30:00", "PT9H", "2024");
		assertTrue(matches("resolution lt duration'PT10H'", row), "and the operator says the same");
	}

	@Test
	@DisplayName("an XSD integer still compares numerically, not lexicographically")
	void xsdIntegersCompareNumerically() {
		EObject row = row("2024-05-03", "2024-05-03T10:15:30Z", "08:30:00", "P1DT2H", "2024");
		row.eSet(dataset.getEStructuralFeature("byteSize"), new BigInteger("1000"));

		// the measurement in #144: as Edm.String, '999' ranked above '1000'
		assertTrue(matches("byteSize gt 999", row));
		assertFalse(matches("byteSize gt 1001", row));
		assertTrue(matches("byteSize eq 1000", row));
	}

	// === helpers =============================================================================

	private boolean matches(String filter, EObject row) {
		return new OclEvaluator().matches(new ODataQueryParser().parseFilter(filter, dataset), row);
	}

	private EObject row(String issued, String modified, String opens, String resolution, String vintage) {
		EObject row = model.getEFactoryInstance().create(dataset);
		row.eSet(dataset.getEStructuralFeature("id"), "d1");
		row.eSet(dataset.getEStructuralFeature("issued"), xsd(XSD.getDate(), issued));
		row.eSet(dataset.getEStructuralFeature("modified"), xsd(XSD.getDateTime(), modified));
		row.eSet(dataset.getEStructuralFeature("opens"), xsd(XSD.getTime(), opens));
		row.eSet(dataset.getEStructuralFeature("resolution"), xsd(XSD.getDuration(), resolution));
		row.eSet(dataset.getEStructuralFeature("vintage"), xsd(XSD.getGYear(), vintage));
		return row;
	}

	/** A value of an XSD datatype, built by the datatype's own factory (as EMF itself does). */
	private static Object xsd(EDataType type, String literal) {
		return EcoreUtil.createFromString(type, literal);
	}

	/** A dynamic model shaped like an Ecore generated from an XSD. */
	private static EPackage model() {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("dcat");
		pkg.setNsPrefix("dcat");
		pkg.setNsURI("http://example.org/dcat-ap-temporal");

		EClass dataset = EcoreFactory.eINSTANCE.createEClass();
		dataset.setName("Dataset");
		pkg.getEClassifiers().add(dataset);

		dataset.getEStructuralFeatures().add(attribute("id", EcorePackage.eINSTANCE.getEString(), true));
		dataset.getEStructuralFeatures().add(attribute("byteSize", XSD.getNonNegativeInteger(), false));
		dataset.getEStructuralFeatures().add(attribute("issued", XSD.getDate(), false));
		dataset.getEStructuralFeatures().add(attribute("modified", XSD.getDateTime(), false));
		dataset.getEStructuralFeatures().add(attribute("opens", XSD.getTime(), false));
		dataset.getEStructuralFeatures().add(attribute("resolution", XSD.getDuration(), false));
		dataset.getEStructuralFeatures().add(attribute("vintage", XSD.getGYear(), false));
		return pkg;
	}

	private static EAttribute attribute(String name, EDataType type, boolean key) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		attribute.setID(key);
		return attribute;
	}
}
