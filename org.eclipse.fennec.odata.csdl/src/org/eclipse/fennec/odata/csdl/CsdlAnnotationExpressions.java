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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMap;
import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.TBoolConstantExpression;
import org.open.oasis.docs.odata.ns.edm.TCollectionExpression;
import org.open.oasis.docs.odata.ns.edm.TDecimalConstantExpression;
import org.open.oasis.docs.odata.ns.edm.TFloatConstantExpression;
import org.open.oasis.docs.odata.ns.edm.TIntConstantExpression;
import org.open.oasis.docs.odata.ns.edm.TPropertyValue;
import org.open.oasis.docs.odata.ns.edm.TRecordExpression;
import org.open.oasis.docs.odata.ns.edm.TStringConstantExpression;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Rich vocabulary-annotation expressions (E2 AP-5 rest): maps CSDL {@code <Record>}/
 * {@code <Collection>}/path-flavoured/{@code <EnumMember>} annotation values to a compact
 * JSON text form and back. The text form IS the [OData-CSDL-JSON] value encoding — records
 * are objects (record type under {@code "@type"}), collections are arrays, paths are
 * {@code {"$Path": "…"}}-style objects, enum members {@code {"$EnumMember": "Ns.E/A Ns.E/B"}}
 * — so the same nodes feed the CSDL JSON writer/reader directly and the string form is what
 * the {@code …/odata/annotations} EAnnotation detail carries on the Ecore side.
 *
 * <p>Constants inside rich values keep their JSON types (bool/number/string). Expression
 * kinds outside this subset (Apply/If/casts/labeled elements/UrlRef) stay unmapped: reading
 * skips them, they never round-trip silently wrong.
 *
 * <p>{@link AnnotationType} and {@link TPropertyValue} share the same expression-choice
 * features without a common interface — the carrier plumbing is reflective over the shared
 * feature names.
 */
final class CsdlAnnotationExpressions {

	private static final ObjectMapper MAPPER = new ObjectMapper();
	private static final EdmFactory EDM = EdmFactory.eINSTANCE;

	/** JSON marker key → the EDM ATTRIBUTE-notation feature of the path-flavoured forms. */
	private static final Map<String, String> PATH_ATTRIBUTES = Map.of(
			"$Path", "path1",
			"$PropertyPath", "propertyPath1",
			"$NavigationPropertyPath", "navigationPropertyPath1",
			"$AnnotationPath", "annotationPath1");

	/** JSON marker key → the EDM ELEMENT-notation feature (collection members). */
	private static final Map<String, String> PATH_ELEMENTS = Map.of(
			"$Path", "path",
			"$PropertyPath", "propertyPath",
			"$NavigationPropertyPath", "navigationPropertyPath",
			"$AnnotationPath", "annotationPath");

	private CsdlAnnotationExpressions() {
	}

	// ---------- write: JSON text → EDM expression tree ----------

	/** Whether a detail value is the rich-expression JSON encoding (vs a plain constant). */
	static boolean isRich(String value) {
		return value != null && !value.isEmpty()
				&& (value.charAt(0) == '{' || value.charAt(0) == '[');
	}

	/**
	 * Applies a rich-expression JSON text onto the annotation; {@code false} when the text is
	 * not parseable JSON — the caller then falls back to the constant forms.
	 */
	static boolean apply(String json, AnnotationType target) {
		JsonNode value;
		try {
			value = MAPPER.readTree(json);
		} catch (RuntimeException e) {
			return false;
		}
		if (!value.isObject() && !value.isArray()) {
			return false;
		}
		applyValue(value, target);
		return true;
	}

	/** A JSON value → expression members on an {@link AnnotationType}/{@link TPropertyValue}. */
	static void applyValue(JsonNode value, EObject carrier) {
		if (value instanceof ObjectNode object) {
			String pathForm = pathFormOf(object);
			if (pathForm != null) {
				set(carrier, PATH_ATTRIBUTES.get(pathForm), object.get(pathForm).asString());
			} else if (object.hasNonNull("$EnumMember")) {
				set(carrier, "enumMember1", members(object.get("$EnumMember").asString()));
			} else {
				addTo(carrier, "record", record(object));
			}
			return;
		}
		if (value instanceof ArrayNode array) {
			addTo(carrier, "collection", collection(array));
			return;
		}
		if (value.isBoolean()) {
			set(carrier, "bool1", value.asBoolean());
		} else if (value.isIntegralNumber()) {
			set(carrier, "int1", value.bigIntegerValue());
		} else if (value.isNumber()) {
			set(carrier, "decimal1", value.decimalValue().toPlainString());
		} else if (!value.isNull()) { // a null member is simply not emitted
			set(carrier, "string1", value.asString());
		}
	}

	private static TRecordExpression record(ObjectNode object) {
		TRecordExpression record = EDM.createTRecordExpression();
		object.properties().forEach(entry -> {
			if ("@type".equals(entry.getKey())) {
				record.setType(entry.getValue().asString());
				return;
			}
			if (entry.getValue().isNull()) {
				return;
			}
			TPropertyValue member = EDM.createTPropertyValue();
			member.setProperty(entry.getKey());
			applyValue(entry.getValue(), member);
			record.getPropertyValue().add(member);
		});
		return record;
	}

	private static TCollectionExpression collection(ArrayNode array) {
		TCollectionExpression collection = EDM.createTCollectionExpression();
		for (JsonNode item : array) {
			if (item instanceof ObjectNode object) {
				String pathForm = pathFormOf(object);
				if (pathForm != null) { // collection members use the ELEMENT notation
					addTo(collection, PATH_ELEMENTS.get(pathForm), object.get(pathForm).asString());
				} else if (object.hasNonNull("$EnumMember")) {
					addTo(collection, "enumMember", members(object.get("$EnumMember").asString()));
				} else {
					collection.getRecord().add(record(object));
				}
			} else if (item instanceof ArrayNode nested) {
				collection.getCollection().add(collection(nested));
			} else if (item.isBoolean()) {
				TBoolConstantExpression constant = EDM.createTBoolConstantExpression();
				constant.setValue(item.asBoolean());
				collection.getBool().add(constant);
			} else if (item.isIntegralNumber()) {
				TIntConstantExpression constant = EDM.createTIntConstantExpression();
				constant.setValue(item.bigIntegerValue());
				collection.getInt().add(constant);
			} else if (item.isNumber()) {
				TDecimalConstantExpression constant = EDM.createTDecimalConstantExpression();
				constant.setValue(item.decimalValue().toPlainString());
				collection.getDecimal().add(constant);
			} else if (!item.isNull()) {
				TStringConstantExpression constant = EDM.createTStringConstantExpression();
				constant.setValue(item.asString());
				collection.getString().add(constant);
			}
		}
		return collection;
	}

	private static String pathFormOf(ObjectNode object) {
		return object.size() == 1
				? PATH_ATTRIBUTES.keySet().stream().filter(object::hasNonNull).findFirst().orElse(null)
				: null;
	}

	private static List<String> members(String spaceSeparated) {
		return Arrays.asList(spaceSeparated.trim().split("\\s+"));
	}

	// ---------- read: EDM expression tree → JSON ----------

	/**
	 * The rich value of an annotation/property value as its JSON text, or {@code null} when
	 * the carrier holds a plain constant (the caller's constant mapping wins) or only
	 * unmapped expression kinds.
	 */
	static String richText(EObject carrier) {
		JsonNode node = richNode(carrier);
		return node == null ? null : node.toString();
	}

	/** {@link #richText} as the tree node — reused by the CSDL JSON writer. */
	static JsonNode richNode(EObject carrier) {
		List<?> records = list(carrier, "record");
		if (!records.isEmpty()) {
			return recordNode((TRecordExpression) records.get(0));
		}
		List<?> collections = list(carrier, "collection");
		if (!collections.isEmpty()) {
			return collectionNode((TCollectionExpression) collections.get(0));
		}
		for (Map.Entry<String, String> form : PATH_ATTRIBUTES.entrySet()) {
			Object path = get(carrier, form.getValue());
			if (path instanceof String text) {
				return MAPPER.createObjectNode().put(form.getKey(), text);
			}
		}
		Object attributeMembers = get(carrier, "enumMember1");
		if (attributeMembers instanceof List<?> list && !list.isEmpty()) {
			return enumMemberNode(list);
		}
		List<?> elementMembers = list(carrier, "enumMember");
		if (!elementMembers.isEmpty() && elementMembers.get(0) instanceof List<?> list
				&& !list.isEmpty()) {
			return enumMemberNode(list);
		}
		return null;
	}

	private static ObjectNode enumMemberNode(List<?> members) {
		String joined = members.stream().map(String::valueOf)
				.reduce((a, b) -> a + " " + b).orElse("");
		return MAPPER.createObjectNode().put("$EnumMember", joined);
	}

	private static ObjectNode recordNode(TRecordExpression record) {
		ObjectNode node = MAPPER.createObjectNode();
		if (record.getType() != null) {
			node.put("@type", record.getType());
		}
		for (TPropertyValue member : record.getPropertyValue()) {
			JsonNode value = memberNode(member);
			if (member.getProperty() != null && value != null) {
				node.set(member.getProperty(), value);
			}
		}
		return node;
	}

	/** A record member's value: rich forms first, then the constant attribute/element forms. */
	private static JsonNode memberNode(TPropertyValue member) {
		JsonNode rich = richNode(member);
		if (rich != null) {
			return rich;
		}
		if (get(member, "string1") instanceof String text) {
			return MAPPER.createObjectNode().stringNode(text);
		}
		if (member.eIsSet(feature(member, "bool1"))) {
			return MAPPER.createObjectNode().booleanNode((Boolean) get(member, "bool1"));
		}
		if (get(member, "int1") != null) {
			return MAPPER.createObjectNode().numberNode(new BigDecimal(get(member, "int1").toString()));
		}
		if (get(member, "decimal1") instanceof String decimal) {
			return MAPPER.createObjectNode().numberNode(new BigDecimal(decimal));
		}
		if (member.eIsSet(feature(member, "float1"))) {
			return MAPPER.createObjectNode().numberNode((Double) get(member, "float1"));
		}
		// element-notation constants (<String>…</String> etc.)
		for (Object wrapped : list(member, "string")) {
			return MAPPER.createObjectNode().stringNode(((TStringConstantExpression) wrapped).getValue());
		}
		for (Object wrapped : list(member, "bool")) {
			return MAPPER.createObjectNode().booleanNode(((TBoolConstantExpression) wrapped).isValue());
		}
		for (Object wrapped : list(member, "int")) {
			return MAPPER.createObjectNode().numberNode(
					new BigDecimal(((TIntConstantExpression) wrapped).getValue()));
		}
		for (Object wrapped : list(member, "decimal")) {
			return MAPPER.createObjectNode().numberNode(
					new BigDecimal(((TDecimalConstantExpression) wrapped).getValue()));
		}
		return null; // unmapped expression kind — the member is skipped, never guessed
	}

	private static ArrayNode collectionNode(TCollectionExpression collection) {
		ArrayNode array = MAPPER.createArrayNode();
		for (FeatureMap.Entry entry : collection.getGExpression()) {
			String featureName = entry.getEStructuralFeature().getName();
			switch (entry.getValue()) {
				case TStringConstantExpression constant -> array.add(constant.getValue());
				case TBoolConstantExpression constant -> array.add(constant.isValue());
				case TIntConstantExpression constant -> array.add(new BigDecimal(constant.getValue()));
				case TDecimalConstantExpression constant ->
					array.add(new BigDecimal(constant.getValue()));
				case TFloatConstantExpression constant -> array.add(constant.getValue());
				case TRecordExpression record -> array.add(recordNode(record));
				case TCollectionExpression nested -> array.add(collectionNode(nested));
				case String path -> { // element-notation path forms carry raw strings
					PATH_ELEMENTS.entrySet().stream()
							.filter(form -> form.getValue().equals(featureName)).findFirst()
							.ifPresent(form -> array.add(
									MAPPER.createObjectNode().put(form.getKey(), path)));
				}
				case List<?> members when "enumMember".equals(featureName) ->
					array.add(enumMemberNode(members));
				default -> { // unmapped expression kinds are skipped
				}
			}
		}
		return array;
	}

	// ---------- reflective carrier plumbing (AnnotationType/TPropertyValue) ----------

	private static EStructuralFeature feature(EObject carrier, String name) {
		return carrier.eClass().getEStructuralFeature(name);
	}

	private static Object get(EObject carrier, String featureName) {
		EStructuralFeature feature = feature(carrier, featureName);
		return feature == null ? null : carrier.eGet(feature);
	}

	private static List<?> list(EObject carrier, String featureName) {
		return get(carrier, featureName) instanceof List<?> values ? values : List.of();
	}

	private static void set(EObject carrier, String featureName, Object value) {
		carrier.eSet(feature(carrier, featureName), value);
	}

	@SuppressWarnings("unchecked")
	private static void addTo(EObject carrier, String featureName, Object value) {
		((List<Object>) carrier.eGet(feature(carrier, featureName))).add(value);
	}
}
