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

import static org.eclipse.fennec.odata.wire.api.WireCondition.and;
import static org.eclipse.fennec.odata.wire.api.WireCondition.compare;
import static org.eclipse.fennec.odata.wire.api.WireCondition.constant;
import static org.eclipse.fennec.odata.wire.api.WireCondition.isNull;
import static org.eclipse.fennec.odata.wire.api.WireCondition.not;
import static org.eclipse.fennec.odata.wire.api.WireCondition.or;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.m2x.model.ocl.BooleanLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.IntegerLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.NullLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.RealLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.odata.wire.api.ControlNames;
import org.eclipse.fennec.odata.wire.api.OperandKind;
import org.eclipse.fennec.odata.wire.api.WireCondition;
import org.eclipse.fennec.odata.wire.api.WireOperator;
import org.eclipse.fennec.odata.wire.api.WireProfile;
import org.eclipse.fennec.odata.wire.api.WirePropertyHandler;
import org.eclipse.fennec.odata.wire.mapping.ClassMapping;
import org.eclipse.fennec.odata.wire.mapping.WireMapping;
import org.eclipse.fennec.odata.wire.mapping.WireMappingFactory;
import org.eclipse.fennec.odata.wire.mapping.WireProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** #119: wire properties in $filter/$orderby become conditions over their parts. */
@DisplayName("Wire properties in queries")
class WireQueryTest {

	private final EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
	private final EClass datastream = type("Datastream");
	private final EClass observation = type("Observation");
	private final EAttribute start = attribute(observation, "phenomenonTimeStart", EcorePackage.Literals.EDATE);
	private final EAttribute end = attribute(observation, "phenomenonTimeEnd", EcorePackage.Literals.EDATE);
	private final EAttribute number = attribute(observation, "resultNumber", EcorePackage.Literals.EDOUBLE_OBJECT);
	private final EAttribute bool = attribute(observation, "resultBoolean", EcorePackage.Literals.EBOOLEAN_OBJECT);
	private final EAttribute text = attribute(observation, "resultText", EcorePackage.Literals.ESTRING);
	private final EAttribute id = attribute(observation, "id", EcorePackage.Literals.ELONG_OBJECT);
	private final EAttribute dsId = attribute(datastream, "id", EcorePackage.Literals.ELONG_OBJECT);
	private final EAttribute dsStart = attribute(datastream, "phenomenonTimeStart", EcorePackage.Literals.EDATE);
	private final EAttribute dsEnd = attribute(datastream, "phenomenonTimeEnd", EcorePackage.Literals.EDATE);
	private final EReference link;

	private final ODataQueryParser parser = new ODataQueryParser();

	WireQueryTest() {
		pkg.setName("sta");
		pkg.setNsURI("http://example.org/sta");
		link = EcoreFactory.eINSTANCE.createEReference();
		link.setName("Datastream");
		link.setEType(datastream);
		observation.getEStructuralFeatures().add(link);
		parser.wireView(WireView.of(new Profile(), type -> List.of(type == observation ? id : dsId)));
	}

	private EClass type(String name) {
		EClass type = EcoreFactory.eINSTANCE.createEClass();
		type.setName(name);
		pkg.getEClassifiers().add(type);
		return type;
	}

	private static EAttribute attribute(EClass owner, String name, EDataType type) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(type);
		owner.getEStructuralFeatures().add(attribute);
		return attribute;
	}

	/** E3 of #119 as a test handler: an interval over two parts. */
	private record Interval(EAttribute s, EAttribute e) implements WirePropertyHandler {

		@Override
		public Object write(EObject entity) {
			return null;
		}

		@Override
		public Map<EAttribute, Object> read(Object value) {
			return Map.of();
		}

		@Override
		public WireCondition compare(WireOperator op, OperandKind operand) {
			if (operand == OperandKind.NULL) {
				return op == WireOperator.EQ ? isNull(s) : not(isNull(s));
			}
			if (operand != OperandKind.DATE_TIME) {
				throw new IllegalArgumentException("an interval compares with a date");
			}
			WireCondition endOk = or(isNull(e), WireCondition.compare(e, WireOperator.LE));
			return switch (op) {
			case GT, GE -> WireCondition.compare(s, op);
			case LT -> and(WireCondition.compare(s, WireOperator.LT), endOk);
			case LE -> and(WireCondition.compare(s, WireOperator.LE), endOk);
			case EQ -> and(WireCondition.compare(s, WireOperator.EQ), or(isNull(e), WireCondition.compare(e, WireOperator.EQ)));
			case NE -> not(compare(WireOperator.EQ, operand));
			};
		}

		@Override
		public EAttribute operandOf(String function) {
			return "year".equals(function) ? s : null;
		}

		@Override
		public List<EAttribute> orderBy() {
			return List.of(s, e);
		}
	}

	/** E5 of #119 as a test handler: alternatives picked by the operand. */
	private final class Result implements WirePropertyHandler {

		@Override
		public Object write(EObject entity) {
			return null;
		}

		@Override
		public Map<EAttribute, Object> read(Object value) {
			return Map.of();
		}

		@Override
		public WireCondition compare(WireOperator op, OperandKind operand) {
			EAttribute part = switch (operand) {
			case NUMBER -> number;
			case STRING -> text;
			case BOOLEAN -> bool;
			case NULL -> null;
			case UNKNOWN -> throw new IllegalArgumentException("ambiguous");
			default -> throw new UnsupportedOperationException("no part for " + operand);
			};
			if (part == null) {
				return op == WireOperator.EQ ? and(isNull(number), isNull(bool), isNull(text))
						: not(and(isNull(number), isNull(bool), isNull(text)));
			}
			return WireCondition.compare(part, op);
		}

		@Override
		public EAttribute operandOf(String function) {
			return switch (function) {
			case "length", "contains" -> text;
			case "add", "round" -> number;
			default -> null;
			};
		}

		@Override
		public List<EAttribute> orderBy() {
			return List.of(text, bool, number);
		}
	}

	private class Profile implements WireProfile {
		private final WireProperty time = property("phenomenonTime", start, end);
		private final WireProperty result = property("result", number, bool, text);
		private final WireProperty dsTime = property("phenomenonTime", dsStart, dsEnd);

		@Override
		public ControlNames controls() {
			return new ControlNames(null, null, "@iot.id", "@iot.selfLink", "@iot.navigationLink",
					"@iot.count", "@iot.nextLink");
		}

		@Override
		public Optional<WireMapping> mapping() {
			WireMapping mapping = WireMappingFactory.eINSTANCE.createWireMapping();
			mapping.getClasses().add(classMapping(observation, time, result));
			mapping.getClasses().add(classMapping(datastream, dsTime));
			return Optional.of(mapping);
		}

		@Override
		public Optional<WirePropertyHandler> handler(WireProperty property) {
			if (property == result) {
				return Optional.of(new Result());
			}
			return Optional.of(new Interval(property.getParts().get(0), property.getParts().get(1)));
		}
	}

	private static WireProperty property(String name, EAttribute... parts) {
		WireProperty property = WireMappingFactory.eINSTANCE.createWireProperty();
		property.setName(name);
		property.getParts().addAll(List.of(parts));
		return property;
	}

	private static ClassMapping classMapping(EClass type, WireProperty... properties) {
		ClassMapping mapping = WireMappingFactory.eINSTANCE.createClassMapping();
		mapping.setEClass(type);
		mapping.getProperties().addAll(List.of(properties));
		return mapping;
	}

	/** A compact rendering of the IR: {@code (a < X)}, {@code not (…)}, {@code Datastream/a}. */
	private static String render(OclExpression exp) {
		return switch (exp) {
		case PropertyCallExp p -> (p.getOwnedSource() == null ? "" : render(p.getOwnedSource()) + "/")
				+ p.getReferredProperty().getName();
		case OperationCallExp op when "not".equals(op.getName()) -> "not " + render(op.getOwnedSource());
		case OperationCallExp op when op.getOwnedArguments().size() == 1 && !Character.isLetter(op.getName().charAt(0))
				|| "and".equals(op.getName()) || "or".equals(op.getName()) ->
			"(" + render(op.getOwnedSource()) + " " + op.getName() + " " + render(op.getOwnedArguments().get(0)) + ")";
		case OperationCallExp op -> op.getName() + "(" + render(op.getOwnedSource())
				+ op.getOwnedArguments().stream().map(a -> ", " + render(a)).collect(Collectors.joining()) + ")";
		case NullLiteralExp n -> "null";
		case BooleanLiteralExp b -> String.valueOf(b.isBooleanSymbol());
		case IntegerLiteralExp i -> String.valueOf(i.getIntegerSymbol());
		case RealLiteralExp r -> String.valueOf(r.getRealSymbol());
		case StringLiteralExp s -> "'" + s.getStringSymbol() + "'";
		default -> exp.getClass().getSimpleName();
		};
	}

	private String filter(String filter) {
		return render(parser.parseFilter(filter, observation));
	}

	private static final String X = "'2020-01-01T00:00:00Z'";

	@Test
	@DisplayName("an interval comparison becomes the handler's condition over the parts")
	void interval() {
		assertEquals("(phenomenonTimeStart > " + X + ")", filter("phenomenonTime gt 2020-01-01T00:00:00Z"));
		assertEquals("((phenomenonTimeStart < " + X + ") and ((phenomenonTimeEnd = null) or (phenomenonTimeEnd <= " + X + ")))",
				filter("phenomenonTime lt 2020-01-01T00:00:00Z"));
		assertEquals("not ((phenomenonTimeStart = " + X + ") and ((phenomenonTimeEnd = null) or (phenomenonTimeEnd = " + X + ")))",
				filter("phenomenonTime ne 2020-01-01T00:00:00Z"));
		assertEquals("(phenomenonTimeStart = null)", filter("phenomenonTime eq null"));
	}

	@Test
	@DisplayName("the wire property on the right: the operator is turned around")
	void converse() {
		assertEquals("(phenomenonTimeStart >= " + X + ")", filter("2020-01-01T00:00:00Z le phenomenonTime"));
	}

	@Test
	@DisplayName("the operand's type picks the alternative")
	void alternatives() {
		assertEquals("(resultNumber > 4)", filter("result gt 4"));
		assertEquals("(resultText = '5')", filter("result eq '5'"));
		assertEquals("(resultBoolean = true)", filter("result eq true"));
		assertEquals("(resultNumber > (resultNumber + 1))", filter("result gt result add 1"),
				"arithmetic on the property uses its number part");
		assertEquals("(((resultNumber = null) and (resultBoolean = null)) and (resultText = null))", filter("result eq null"));
	}

	@Test
	@DisplayName("functions take the part the handler names")
	void functions() {
		assertEquals("(year(phenomenonTimeStart) = 2019)", filter("year(phenomenonTime) eq 2019"));
		assertEquals("(size(resultText) > 3)", filter("length(result) gt 3"));
		assertTrue(assertThrows(ODataQueryParseException.class, () -> filter("month(phenomenonTime) eq 7"))
				.getMessage().contains("no operand of 'month'"));
	}

	@Test
	@DisplayName("in expands to eq comparisons")
	void in() {
		assertEquals("((resultNumber = 1) or (resultNumber = 2))", filter("result in (1, 2)"));
	}

	@Test
	@DisplayName("a navigation path keeps its source on every part")
	void navigation() {
		assertEquals("(Datastream/phenomenonTimeStart > " + X + ")", filter("Datastream/phenomenonTime gt 2020-01-01T00:00:00Z"));
	}

	@Test
	@DisplayName("handler errors: IllegalArgument is 400, Unsupported 501")
	void handlerErrors() {
		assertThrows(ODataQueryParseException.class, () -> filter("phenomenonTime gt 'x'"));
		assertThrows(ODataQueryNotImplementedException.class, () -> filter("result eq 2020-01-01T00:00:00Z"));
		assertThrows(ODataQueryNotImplementedException.class, () -> filter("phenomenonTime eq Datastream/phenomenonTime"));
		assertThrows(ODataQueryNotImplementedException.class, () -> filter("result eq result"), "two wire properties: #129");
		assertThrows(ODataQueryParseException.class, () -> filter("result eq Datastream"), "an operand of unknown kind");
	}

	@Test
	@DisplayName("the parts are not addressable, a bare wire property is no filter")
	void notAddressable() {
		assertThrows(ODataQueryParseException.class, () -> filter("phenomenonTimeStart gt 2020-01-01T00:00:00Z"));
		assertThrows(ODataQueryParseException.class, () -> filter("resultText eq 'x'"));
		assertThrows(ODataQueryParseException.class, () -> filter("result"));
		assertThrows(ODataQueryParseException.class, () -> filter("phenomenonTime/x eq 1"));
	}

	@Test
	@DisplayName("$orderby sorts by the handler's keys")
	void orderBy() {
		List<OrderBySegment> segments = parser.parseOrderBy("result desc,phenomenonTime", observation);
		assertEquals(List.of("resultText", "resultBoolean", "resultNumber", "phenomenonTimeStart", "phenomenonTimeEnd"),
				segments.stream().map(s -> render(s.expression())).toList());
		assertEquals(List.of(false, false, false, true, true), segments.stream().map(OrderBySegment::ascending).toList());
	}

	@Test
	@DisplayName("the profile's key value name is an alias of a single key (E9)")
	void keyAlias() {
		assertEquals("(id = 42)", filter("@iot.id eq 42"));
		assertEquals("(Datastream/id = 1)", filter("Datastream/@iot.id eq 1"));
		assertEquals(List.of("id"), parser.parseOrderBy("@iot.id desc", observation).stream()
				.map(s -> render(s.expression())).toList());
	}

	@Test
	@DisplayName("plain OData: no wire properties, @-names stay what they were")
	void plain() {
		ODataQueryParser plain = new ODataQueryParser();
		assertEquals("(phenomenonTimeStart > " + X + ")",
				render(plain.parseFilter("phenomenonTimeStart gt 2020-01-01T00:00:00Z", observation)));
		assertThrows(ODataQueryParseException.class, () -> plain.parseFilter("phenomenonTime gt 2020-01-01T00:00:00Z", observation));
		assertThrows(UnsupportedOperationException.class, () -> plain.parseFilter("@iot.id eq 1", observation));
	}

	@Test
	@DisplayName("a broken mapping fails the view")
	void brokenMapping() {
		WireProfile noHandler = new Profile() {
			@Override
			public Optional<WirePropertyHandler> handler(WireProperty property) {
				return Optional.empty();
			}
		};
		assertThrows(IllegalArgumentException.class, () -> WireView.of(noHandler, type -> List.of()));
		WireProfile foreignPart = new Profile() {
			@Override
			public Optional<WireMapping> mapping() {
				WireMapping mapping = WireMappingFactory.eINSTANCE.createWireMapping();
				mapping.getClasses().add(classMapping(datastream, property("phenomenonTime", start, end)));
				return Optional.of(mapping);
			}
		};
		assertTrue(assertThrows(IllegalArgumentException.class, () -> WireView.of(foreignPart, type -> List.of()))
				.getMessage().contains("no attribute of Datastream"));
	}
}
