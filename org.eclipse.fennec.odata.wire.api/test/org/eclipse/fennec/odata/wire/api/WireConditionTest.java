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
package org.eclipse.fennec.odata.wire.api;

import static org.eclipse.fennec.odata.wire.api.WireCondition.and;
import static org.eclipse.fennec.odata.wire.api.WireCondition.compare;
import static org.eclipse.fennec.odata.wire.api.WireCondition.isNull;
import static org.eclipse.fennec.odata.wire.api.WireCondition.or;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** #119: the condition algebra is enough to state a dialect's semantics without the query IR. */
@DisplayName("WireCondition")
class WireConditionTest {

	@Test
	@DisplayName("the converse puts the wire property on the left")
	void converse() {
		assertEquals(WireOperator.GT, WireOperator.LT.converse());
		assertEquals(WireOperator.LE, WireOperator.GE.converse());
		assertEquals(WireOperator.EQ, WireOperator.EQ.converse());
		assertEquals(WireOperator.NE, WireOperator.NE.converse());
	}

	@Test
	@DisplayName("an interval's lt over its parts: s < X and (e null or e <= X)")
	void interval() {
		EAttribute start = EcoreFactory.eINSTANCE.createEAttribute();
		EAttribute end = EcoreFactory.eINSTANCE.createEAttribute();
		WireCondition lt = and(compare(start, WireOperator.LT), or(isNull(end), compare(end, WireOperator.LE)));
		assertEquals(List.of(start, end), parts(lt).stream().distinct().toList(), "only the parts appear");
	}

	@Test
	@DisplayName("composites are immutable")
	void immutable() {
		List<WireCondition> conditions = new ArrayList<>(List.of(WireCondition.constant(true)));
		WireCondition.And and = new WireCondition.And(conditions);
		conditions.clear();
		assertEquals(1, and.conditions().size());
		assertThrows(UnsupportedOperationException.class, () -> and.conditions().add(WireCondition.constant(false)));
	}

	private static List<EAttribute> parts(WireCondition condition) {
		return switch (condition) {
		case WireCondition.Compare c -> List.of(c.part());
		case WireCondition.CompareBound b -> List.of(b.part());
		case WireCondition.CompareParts p -> List.of(p.part(), p.otherPart());
		case WireCondition.IsNull n -> List.of(n.part());
		case WireCondition.Not n -> parts(n.condition());
		case WireCondition.And a -> a.conditions().stream().flatMap(c -> parts(c).stream()).toList();
		case WireCondition.Or o -> o.conditions().stream().flatMap(c -> parts(c).stream()).toList();
		case WireCondition.Constant c -> List.of();
		};
	}
}
