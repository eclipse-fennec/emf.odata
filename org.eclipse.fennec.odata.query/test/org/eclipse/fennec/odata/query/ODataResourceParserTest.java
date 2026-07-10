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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.fennec.odata.query.ResourcePath.CountSegment;
import org.eclipse.fennec.odata.query.ResourcePath.PropertySegment;
import org.eclipse.fennec.odata.query.ResourcePath.ValueSegment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The own resource-path parser (ADR-0005): shapes, key literals, terminal segments and the
 * destructive contract (malformed paths, segment bombs, traversal shapes → parse exception).
 */
@DisplayName("Resource-path parser (own URI parsing)")
class ODataResourceParserTest {

	private final ODataResourceParser parser = new ODataResourceParser();

	@Test
	@DisplayName("shapes: set, keyed entity, navigation, property, $value/$count")
	void shapes() {
		ResourcePath set = parser.parse("Product");
		assertEquals("Product", set.entitySet());
		assertNull(set.key());
		assertTrue(set.segments().isEmpty());

		ResourcePath keyed = parser.parse("Product('p1')");
		assertEquals("'p1'", keyed.key(), "key stays raw — typing happens at the protocol layer");

		ResourcePath navigation = parser.parse("Product('p''1')/category/name/$value");
		assertEquals("'p''1'", navigation.key(), "escaped quotes preserved verbatim");
		assertEquals("category", ((PropertySegment) navigation.segments().get(0)).name());
		assertEquals("name", ((PropertySegment) navigation.segments().get(1)).name());
		assertInstanceOf(ValueSegment.class, navigation.segments().get(2));

		ResourcePath count = parser.parse("Product/$count");
		assertInstanceOf(CountSegment.class, count.segments().get(0));

		ResourcePath keyedNav = parser.parse("Product('p1')/reviews(5)");
		PropertySegment reviews = (PropertySegment) keyedNav.segments().get(0);
		assertEquals("reviews", reviews.name());
		assertEquals("5", reviews.key());
	}

	@Test
	@DisplayName("derived-type cast segments: set cast, keyed cast, cast in navigation")
	void typeCasts() {
		ResourcePath setCast = parser.parse("Product/webshop.DiscountedProduct");
		ResourcePath.TypeCastSegment cast =
				(ResourcePath.TypeCastSegment) setCast.segments().get(0);
		assertEquals("webshop.DiscountedProduct", cast.qualifiedName());
		assertNull(cast.key());

		ResourcePath keyedCast = parser.parse("Product/webshop.DiscountedProduct('d1')");
		assertEquals("'d1'",
				((ResourcePath.TypeCastSegment) keyedCast.segments().get(0)).key());

		ResourcePath navCast = parser.parse("Category('c1')/products/shop.Special/$count");
		assertEquals("shop.Special",
				((ResourcePath.TypeCastSegment) navCast.segments().get(1)).qualifiedName());
	}

	@Test
	@DisplayName("key-as-segment: bare literal segments parse as KeySegment ([OData-URL] 4.3.3)")
	void keyAsSegment() {
		ResourcePath numeric = parser.parse("Product/5");
		assertEquals("5", ((ResourcePath.KeySegment) numeric.segments().get(0)).value());

		ResourcePath quoted = parser.parse("Product/'p1'/category");
		assertEquals("'p1'", ((ResourcePath.KeySegment) quoted.segments().get(0)).value());
		assertEquals("category", ((PropertySegment) quoted.segments().get(1)).name());

		ResourcePath nested = parser.parse("Category('c1')/products/5/$count");
		assertEquals("5", ((ResourcePath.KeySegment) nested.segments().get(1)).value());
		assertInstanceOf(CountSegment.class, nested.segments().get(2));

		// a bare IDENT stays a PropertySegment — the protocol layer disambiguates vs the model
		ResourcePath ident = parser.parse("Product/p1");
		assertEquals("p1", ((PropertySegment) ident.segments().get(0)).name());
	}

	@Test
	@DisplayName("destructive: malformed paths, segment bombs, traversal shapes")
	void destructive() {
		for (String bad : new String[] {
				"", "Product(", "Product('p1'", "Product()", "Product('p1'))",
				"Product/../secret", "Product('p1')//name", "Pro duct", "Product('a'' or 1)(",
				"Product?",
				// $count/$value/$ref are terminal (OASIS ABNF; TC cases 4.4/4.7/4.8)
				"Product/$count/name", "Product('p1')/name/$value/foo",
				"Product('p1')/reviews/$ref/$count",
				// at most ONE cast per navigation step (OASIS ABNF; TC case 4.11)
				"Product(1)/a.B/a.B", "Product/ns.T1/ns.T2" }) {
			assertThrows(ODataQueryParseException.class, () -> parser.parse(bad), bad);
		}
		String bomb = "Product" + "/a".repeat(ODataResourceParser.MAX_SEGMENTS + 1);
		assertThrows(ODataQueryParseException.class, () -> parser.parse(bomb), "segment cap");
	}
}
