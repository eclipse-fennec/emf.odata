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

import java.util.Map;
import java.util.Set;

import java.math.BigInteger;
import java.math.BigDecimal;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.ocl.BooleanLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.CollectionItem;
import org.eclipse.fennec.m2x.model.ocl.CollectionKind;
import org.eclipse.fennec.m2x.model.ocl.CollectionLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.CollectionLiteralPart;
import org.eclipse.fennec.m2x.model.ocl.CollectionType;
import org.eclipse.fennec.m2x.model.ocl.IntegerLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.IteratorExp;
import org.eclipse.fennec.m2x.model.ocl.NullLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OclFactory;
import org.eclipse.fennec.m2x.model.ocl.OclType;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.RealLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.TypeExp;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;

/**
 * Standalone bottom-up type resolution for the OCL predicate IR built by
 * {@link ODataToOclBuilder} (ADR-0004): {@code PropertyCallExp.type} comes from the eagerly
 * resolved {@code referredProperty}, named-operation result types from a dispatch table
 * (the same names the backend translators dispatch on), literal types directly.
 *
 * <p>Deliberately NO dependency on a metadata service or whiteboard — this is the ADR-0003
 * pattern applied to §3.5 type resolution. The {@code OclAspectProvider} (VA1) is only the
 * later cache/lifecycle adapter around this core. Unknown operation names resolve to no type
 * ({@code null}) rather than failing — typing is best-effort enrichment, parsing already
 * validated the expression.
 */
public class OclTypeResolver {

	private static final OclFactory FACTORY = OclFactory.eINSTANCE;

	private static final Set<String> BOOLEAN_OPS = Set.of(
			"=", "<>", "<", "<=", ">", ">=", "and", "or", "not", "includes",
			"contains", "startsWith", "endsWith", "has", "notEmpty");
	private static final Set<String> INTEGER_OPS = Set.of(
			"size", "indexOf", "year", "month", "day", "hour", "minute", "second",
			"round", "floor", "ceiling");
	private static final Set<String> STRING_OPS = Set.of(
			"toLower", "toUpper", "trim", "substring", "concat", "date", "time");
	/** Numeric ops whose result widens to Real iff an operand is Real; {@code /} is always Real. */
	private static final Set<String> NUMERIC_OPS = Set.of("+", "-", "*", "mod");

	private static final Map<Class<?>, String> PRIMITIVE_BY_INSTANCE_CLASS = Map.ofEntries(
			Map.entry(String.class, "String"),
			Map.entry(Boolean.class, "Boolean"), Map.entry(boolean.class, "Boolean"),
			Map.entry(Integer.class, "Integer"), Map.entry(int.class, "Integer"),
			Map.entry(Long.class, "Integer"), Map.entry(long.class, "Integer"),
			Map.entry(Short.class, "Integer"), Map.entry(short.class, "Integer"),
			Map.entry(Byte.class, "Integer"), Map.entry(byte.class, "Integer"),
			Map.entry(BigInteger.class, "Integer"),
			Map.entry(Double.class, "Real"), Map.entry(double.class, "Real"),
			Map.entry(Float.class, "Real"), Map.entry(float.class, "Real"),
			Map.entry(BigDecimal.class, "Real"));

	/** Types the expression tree in place and returns it (bottom-up, idempotent). */
	public <T extends OclExpression> T resolve(T expression) {
		type(expression);
		return expression;
	}

	private OclType type(OclExpression exp) {
		if (exp == null) {
			return null;
		}
		if (exp.getType() != null) {
			return exp.getType();
		}
		OclType resolved = switch (exp) {
			case OperationCallExp op -> operationType(op);
			case PropertyCallExp property -> propertyType(property);
			case IteratorExp iterator -> iteratorType(iterator);
			case VariableExp variable -> variable.getReferredVariable() == null ? null
					: copy(variable.getReferredVariable().getType());
			case CollectionLiteralExp collection -> collectionType(collection);
			case StringLiteralExp s -> primitive("String");
			case BooleanLiteralExp b -> primitive("Boolean");
			case IntegerLiteralExp i -> primitive("Integer");
			case RealLiteralExp r -> primitive("Real");
			case NullLiteralExp n -> primitive("OclVoid");
			default -> null;
		};
		exp.setType(resolved);
		return resolved;
	}

	private OclType operationType(OperationCallExp op) {
		OclType sourceType = type(op.getOwnedSource());
		op.getOwnedArguments().forEach(this::type);

		String name = op.getName();
		if (name == null) {
			return null;
		}
		if (BOOLEAN_OPS.contains(name) || "oclIsKindOf".equals(name)) {
			return primitive("Boolean");
		}
		if ("oclAsType".equals(name)) { // cast result = the referred type
			return op.getOwnedArguments().stream()
					.filter(TypeExp.class::isInstance).map(TypeExp.class::cast)
					.findFirst().map(t -> copy(t.getReferredType())).orElse(null);
		}
		if (INTEGER_OPS.contains(name)) {
			return primitive("Integer");
		}
		if (STRING_OPS.contains(name)) {
			return primitive("String");
		}
		if ("/".equals(name)) {
			return primitive("Real");
		}
		if (NUMERIC_OPS.contains(name)) {
			return isReal(sourceType) || op.getOwnedArguments().stream()
					.anyMatch(a -> isReal(a.getType())) ? primitive("Real") : primitive("Integer");
		}
		return null; // unknown op: leave untyped, best-effort
	}

	private OclType iteratorType(IteratorExp iterator) {
		type(iterator.getOwnedSource());
		type(iterator.getOwnedBody());
		String name = iterator.getName();
		if ("exists".equals(name) || "forAll".equals(name)) {
			return primitive("Boolean");
		}
		return null;
	}

	private OclType propertyType(PropertyCallExp property) {
		type(property.getOwnedSource());
		EStructuralFeature feature = property.getReferredProperty();
		if (feature == null) {
			return null;
		}
		if (feature.isMany()) {
			return collectionTypeFor(feature);
		}
		return classifierOrPrimitive(feature.getEType());
	}

	private OclType collectionType(CollectionLiteralExp collection) {
		OclType elementType = null;
		for (CollectionLiteralPart part : collection.getOwnedParts()) {
			if (part instanceof CollectionItem item) {
				OclType itemType = type(item.getOwnedItem());
				if (elementType == null) {
					elementType = itemType;
				}
			}
		}
		CollectionType type = FACTORY.createCollectionType();
		type.setKind(collection.getKind());
		type.setElementType(copy(elementType));
		return type;
	}

	// --- type construction (fresh instances per node — type is a plain reference) ---

	/** EMF list semantics → OCL collection kind, mirroring the m2x parser's feature mapping. */
	private OclType collectionTypeFor(EStructuralFeature feature) {
		CollectionType type = FACTORY.createCollectionType();
		if (feature instanceof EReference reference) {
			type.setKind(reference.isOrdered()
					? (reference.isUnique() ? CollectionKind.ORDERED_SET : CollectionKind.SEQUENCE)
					: (reference.isUnique() ? CollectionKind.SET : CollectionKind.BAG));
		} else {
			type.setKind(CollectionKind.SEQUENCE);
		}
		type.setElementType(classifierOrPrimitive(feature.getEType()));
		return type;
	}

	/** EDataTypes with a primitive-ish instance class → OCL primitive, everything else → classifier. */
	private OclType classifierOrPrimitive(EClassifier classifier) {
		if (classifier == null) {
			return null;
		}
		if (classifier instanceof EDataType dataType && !(classifier instanceof EEnum)) {
			String primitive = PRIMITIVE_BY_INSTANCE_CLASS.get(dataType.getInstanceClass());
			if (primitive != null) {
				return primitive(primitive);
			}
		}
		var type = FACTORY.createClassifierType();
		type.setReferredClassifier(classifier);
		type.setName(classifier.getName());
		return type;
	}

	private OclType primitive(String name) {
		var type = FACTORY.createPrimitiveType();
		type.setName(name);
		return type;
	}

	private boolean isReal(OclType type) {
		return type != null && "Real".equals(type.getName());
	}

	private OclType copy(OclType type) {
		if (type == null) {
			return null;
		}
		return EcoreUtil.copy(type);
	}
}
