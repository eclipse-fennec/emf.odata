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

import java.util.function.Consumer;
import java.math.BigInteger;

import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TAction;
import org.open.oasis.docs.odata.ns.edm.TActionFunctionParameter;
import org.open.oasis.docs.odata.ns.edm.TActionFunctionReturnType;
import org.open.oasis.docs.odata.ns.edm.TActionImport;
import org.open.oasis.docs.odata.ns.edm.TComplexType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TEntityKeyElement;
import org.open.oasis.docs.odata.ns.edm.TEntitySet;
import org.open.oasis.docs.odata.ns.edm.TEntityType;
import org.open.oasis.docs.odata.ns.edm.TEnumType;
import org.open.oasis.docs.odata.ns.edm.TEnumTypeMember;
import org.open.oasis.docs.odata.ns.edm.TFunction;
import org.open.oasis.docs.odata.ns.edm.TFunctionImport;
import org.open.oasis.docs.odata.ns.edm.TNavigationProperty;
import org.open.oasis.docs.odata.ns.edm.TNavigationPropertyBinding;
import org.open.oasis.docs.odata.ns.edm.TProperty;
import org.open.oasis.docs.odata.ns.edm.TPropertyRef;
import org.open.oasis.docs.odata.ns.edm.TReferentialConstraint;
import org.open.oasis.docs.odata.ns.edm.TSingleton;
import org.open.oasis.docs.odata.ns.edmx.EdmxFactory;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;
import org.open.oasis.docs.odata.ns.edmx.TInclude;
import org.open.oasis.docs.odata.ns.edmx.TReference;
import org.open.oasis.docs.odata.ns.edmx.TVersion;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Parses a CSDL <b>JSON</b> document ([OData-CSDL-JSON]) into the OASIS EDM/EDMX model — the
 * mirror of {@link CsdlJsonWriter} and the JSON sibling of the XML load path
 * ({@code CsdlXmlLoad}): the produced {@link EdmxRoot} feeds the same {@link EdmToEcoreConverter}.
 * JSON-specific semantics are translated back to the XML model's conventions: {@code $Collection}
 * becomes the {@code Collection(...)} type wrapper, {@code $Nullable} (JSON default false) is set
 * explicitly, and an omitted {@code $Type} means {@code Edm.String}. Vocabulary {@code $Kind: Term}
 * members are skipped (vocabularies are consumed from their XML form).
 */
public final class CsdlJsonReader {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final EdmFactory edm = EdmFactory.eINSTANCE;
	private final EdmxFactory edmx = EdmxFactory.eINSTANCE;

	/** Parses a CSDL JSON document. Malformed input raises {@link IllegalArgumentException}. */
	public EdmxRoot read(String json) {
		JsonNode document;
		try {
			document = MAPPER.readTree(json);
		} catch (RuntimeException e) {
			throw new IllegalArgumentException("not a parseable CSDL JSON document", e);
		}
		if (!(document instanceof ObjectNode root)) {
			throw new IllegalArgumentException("a CSDL JSON document is a JSON object");
		}
		EdmxRoot result = edmx.createEdmxRoot();
		TEdmx edmxElement = edmx.createTEdmx();
		TVersion version = TVersion.get(root.path("$Version").asString("4.0"));
		edmxElement.setVersion(version != null ? version : TVersion._40);
		if (root.get("$Reference") instanceof ObjectNode references) {
			references.propertyStream().forEach(entry -> {
				TReference reference = edmx.createTReference();
				reference.setUri(entry.getKey());
				if (entry.getValue().get("$Include") instanceof ArrayNode includes) {
					for (JsonNode inc : includes) {
						TInclude include = edmx.createTInclude();
						include.setNamespace(inc.path("$Namespace").asString(null));
						if (inc.hasNonNull("$Alias")) {
							include.setAlias(inc.path("$Alias").asString());
						}
						reference.getInclude().add(include);
					}
				}
				edmxElement.getReference().add(reference);
			});
		}
		TDataServices dataServices = edmx.createTDataServices();
		root.propertyStream()
				.filter(e -> !e.getKey().startsWith("$") && !e.getKey().startsWith("@")
						&& e.getValue() instanceof ObjectNode)
				.forEach(e -> dataServices.getSchema()
						.add(schema(e.getKey(), (ObjectNode) e.getValue())));
		edmxElement.setDataServices(dataServices);
		result.setEdmx(edmxElement);
		return result;
	}

	private SchemaType schema(String namespace, ObjectNode node) {
		SchemaType schema = edm.createSchemaType();
		schema.setNamespace(namespace);
		if (node.hasNonNull("$Alias")) {
			schema.setAlias(node.path("$Alias").asString());
		}
		annotations(node, schema.getAnnotation()::add);
		node.propertyStream().forEach(entry -> {
			String name = entry.getKey();
			JsonNode value = entry.getValue();
			if (name.startsWith("$") || name.startsWith("@")) {
				return;
			}
			if (value instanceof ArrayNode array) { // function/action overloads
				for (JsonNode overload : array) {
					operation(name, overload, schema);
				}
				return;
			}
			if (!(value instanceof ObjectNode member)) {
				return;
			}
			switch (member.path("$Kind").asString("")) {
				case "EntityType" -> schema.getEntityType().add(entityType(name, member));
				case "ComplexType" -> schema.getComplexType().add(complexType(name, member));
				case "EnumType" -> schema.getEnumType().add(enumType(name, member));
				case "EntityContainer" -> schema.getEntityContainer().add(container(name, member));
				default -> { /* Term / TypeDefinition / unknown — out of the structural scope */ }
			}
		});
		return schema;
	}

	private TEntityType entityType(String name, ObjectNode node) {
		TEntityType t = edm.createTEntityType();
		t.setName(name);
		if (node.path("$Abstract").asBoolean(false)) {
			t.setAbstract(true);
		}
		if (node.path("$OpenType").asBoolean(false)) {
			t.setOpenType(true);
		}
		if (node.path("$HasStream").asBoolean(false)) {
			t.setHasStream(true);
		}
		if (node.hasNonNull("$BaseType")) {
			t.setBaseType(node.path("$BaseType").asString());
		}
		if (node.get("$Key") instanceof ArrayNode key && !key.isEmpty()) {
			TEntityKeyElement keyElement = edm.createTEntityKeyElement();
			for (JsonNode ref : key) {
				TPropertyRef propertyRef = edm.createTPropertyRef();
				propertyRef.setName(ref.asString());
				keyElement.getPropertyRef().add(propertyRef);
			}
			t.getKey().add(keyElement);
		}
		annotations(node, t.getAnnotation()::add);
		structural(node, t.getProperty()::add, t.getNavigationProperty()::add);
		return t;
	}

	private TComplexType complexType(String name, ObjectNode node) {
		TComplexType t = edm.createTComplexType();
		t.setName(name);
		if (node.path("$Abstract").asBoolean(false)) {
			t.setAbstract(true);
		}
		if (node.path("$OpenType").asBoolean(false)) {
			t.setOpenType(true);
		}
		if (node.hasNonNull("$BaseType")) {
			t.setBaseType(node.path("$BaseType").asString());
		}
		annotations(node, t.getAnnotation()::add);
		structural(node, t.getProperty()::add, t.getNavigationProperty()::add);
		return t;
	}

	private void structural(ObjectNode node, Consumer<TProperty> properties,
			Consumer<TNavigationProperty> navigations) {
		node.propertyStream().forEach(entry -> {
			String name = entry.getKey();
			if (name.startsWith("$") || name.startsWith("@")
					|| !(entry.getValue() instanceof ObjectNode member)) {
				return;
			}
			if ("NavigationProperty".equals(member.path("$Kind").asString(""))) {
				TNavigationProperty n = edm.createTNavigationProperty();
				n.setName(name);
				n.setType(typeOf(member));
				// the XML model's Nullable default is true; JSON's is false — set it explicitly
				n.setNullable(member.path("$Nullable").asBoolean(false));
				if (member.hasNonNull("$Partner")) {
					n.setPartner(member.path("$Partner").asString());
				}
				if (member.path("$ContainsTarget").asBoolean(false)) {
					n.setContainsTarget(true);
				}
				if (member.get("$ReferentialConstraint") instanceof ObjectNode constraints) {
					constraints.propertyStream().forEach(c -> {
						TReferentialConstraint rc = edm.createTReferentialConstraint();
						rc.setProperty(c.getKey());
						rc.setReferencedProperty(c.getValue().asString());
						n.getReferentialConstraint().add(rc);
					});
				}
				annotations(member, n.getAnnotation()::add);
				navigations.accept(n);
			} else if (member.path("$Kind").asString("").isEmpty()) { // a structural property
				TProperty p = edm.createTProperty();
				p.setName(name);
				p.setType(typeOf(member));
				p.setNullable(member.path("$Nullable").asBoolean(false));
				if (member.hasNonNull("$MaxLength")) {
					p.setMaxLength(BigInteger.valueOf(member.path("$MaxLength").asLong()));
				}
				if (member.hasNonNull("$Precision")) {
					p.setPrecision(BigInteger.valueOf(member.path("$Precision").asLong()));
				}
				if (member.hasNonNull("$Scale") && member.path("$Scale").isNumber()) {
					p.setScale(BigInteger.valueOf(member.path("$Scale").asLong()));
				}
				if (member.hasNonNull("$DefaultValue")) {
					p.setDefaultValue(member.path("$DefaultValue").asString());
				}
				annotations(member, p.getAnnotation()::add);
				properties.accept(p);
			}
		});
	}

	private TEnumType enumType(String name, ObjectNode node) {
		TEnumType t = edm.createTEnumType();
		t.setName(name);
		node.propertyStream().forEach(entry -> {
			if (!entry.getKey().startsWith("$") && !entry.getKey().startsWith("@")
					&& entry.getValue().isNumber()) {
				TEnumTypeMember member = edm.createTEnumTypeMember();
				member.setName(entry.getKey());
				member.setValue(entry.getValue().asLong());
				t.getMember().add(member);
			}
		});
		return t;
	}

	private void operation(String name, JsonNode overload, SchemaType schema) {
		boolean isAction = "Action".equals(overload.path("$Kind").asString(""));
		if (isAction) {
			TAction a = edm.createTAction();
			a.setName(name);
			a.setIsBound(overload.path("$IsBound").asBoolean(false));
			if (overload.hasNonNull("$EntitySetPath")) {
				a.setEntitySetPath(overload.path("$EntitySetPath").asString());
			}
			parameters(overload, a.getParameter()::add);
			if (overload.get("$ReturnType") instanceof ObjectNode rt) {
				a.getReturnType().add(returnType(rt));
			}
			schema.getAction().add(a);
		} else {
			TFunction f = edm.createTFunction();
			f.setName(name);
			f.setIsBound(overload.path("$IsBound").asBoolean(false));
			if (overload.path("$IsComposable").asBoolean(false)) {
				f.setIsComposable(true);
			}
			if (overload.hasNonNull("$EntitySetPath")) {
				f.setEntitySetPath(overload.path("$EntitySetPath").asString());
			}
			parameters(overload, f.getParameter()::add);
			if (overload.get("$ReturnType") instanceof ObjectNode rt) {
				f.setReturnType(returnType(rt));
			}
			schema.getFunction().add(f);
		}
	}

	private void parameters(JsonNode overload,
			Consumer<TActionFunctionParameter> out) {
		if (!(overload.get("$Parameter") instanceof ArrayNode parameters)) {
			return;
		}
		for (JsonNode node : parameters) {
			TActionFunctionParameter p = edm.createTActionFunctionParameter();
			p.setName(node.path("$Name").asString(null));
			p.setType(typeOf(node));
			p.setNullable(node.path("$Nullable").asBoolean(false));
			out.accept(p);
		}
	}

	private TActionFunctionReturnType returnType(ObjectNode node) {
		TActionFunctionReturnType rt = edm.createTActionFunctionReturnType();
		rt.setType(typeOf(node));
		rt.setNullable(node.path("$Nullable").asBoolean(false));
		return rt;
	}

	private TEntityContainer container(String name, ObjectNode node) {
		TEntityContainer container = edm.createTEntityContainer();
		container.setName(name);
		annotations(node, container.getAnnotation()::add);
		node.propertyStream().forEach(entry -> {
			if (entry.getKey().startsWith("$") || entry.getKey().startsWith("@")
					|| !(entry.getValue() instanceof ObjectNode member)) {
				return;
			}
			if (member.hasNonNull("$Function")) {
				TFunctionImport imp = edm.createTFunctionImport();
				imp.setName(entry.getKey());
				imp.setFunction(member.path("$Function").asString());
				if (member.hasNonNull("$EntitySet")) {
					imp.setEntitySet(member.path("$EntitySet").asString());
				}
				container.getFunctionImport().add(imp);
			} else if (member.hasNonNull("$Action")) {
				TActionImport imp = edm.createTActionImport();
				imp.setName(entry.getKey());
				imp.setAction(member.path("$Action").asString());
				if (member.hasNonNull("$EntitySet")) {
					imp.setEntitySet(member.path("$EntitySet").asString());
				}
				container.getActionImport().add(imp);
			} else if (member.path("$Collection").asBoolean(false)) {
				TEntitySet set = edm.createTEntitySet();
				set.setName(entry.getKey());
				set.setEntityType(member.path("$Type").asString(null));
				bindings(member, set.getNavigationPropertyBinding()::add);
				container.getEntitySet().add(set);
			} else if (member.hasNonNull("$Type")) { // no $Collection → a singleton
				TSingleton singleton = edm.createTSingleton();
				singleton.setName(entry.getKey());
				singleton.setType(member.path("$Type").asString());
				bindings(member, singleton.getNavigationPropertyBinding()::add);
				container.getSingleton().add(singleton);
			}
		});
		return container;
	}

	private void bindings(ObjectNode member,
			Consumer<TNavigationPropertyBinding> out) {
		if (member.get("$NavigationPropertyBinding") instanceof ObjectNode bindings) {
			bindings.propertyStream().forEach(entry -> {
				TNavigationPropertyBinding binding = edm.createTNavigationPropertyBinding();
				binding.setPath(entry.getKey());
				binding.setTarget(entry.getValue().asString());
				out.accept(binding);
			});
		}
	}

	/** {@code "@Term": value} members → {@link AnnotationType}s (constants and rich expressions). */
	private void annotations(ObjectNode node, Consumer<AnnotationType> out) {
		node.propertyStream().forEach(entry -> {
			if (!entry.getKey().startsWith("@")) {
				return;
			}
			JsonNode value = entry.getValue();
			AnnotationType a = edm.createAnnotationType();
			a.setTerm(entry.getKey().substring(1));
			if (value.isBoolean()) {
				a.setBool1(value.asBoolean());
			} else if (value.isIntegralNumber()) {
				a.setInt1(value.bigIntegerValue());
			} else if (value.isNumber()) {
				a.setDecimal1(value.decimalValue().toPlainString());
			} else if (value.isString()) {
				a.setString1(value.asString());
			} else if (value.isObject() || value.isArray()) {
				CsdlAnnotationExpressions.applyValue(value, a); // Record/Collection/path forms
			} else {
				return;
			}
			out.accept(a);
		});
	}

	/** {@code $Collection}/{@code $Type} → the XML model's type form; omitted type = Edm.String. */
	private static String typeOf(JsonNode member) {
		String type = member.path("$Type").asString("Edm.String");
		return member.path("$Collection").asBoolean(false)
				? EdmTypes.COLLECTION_OPEN + type + ")"
				: type;
	}
}
