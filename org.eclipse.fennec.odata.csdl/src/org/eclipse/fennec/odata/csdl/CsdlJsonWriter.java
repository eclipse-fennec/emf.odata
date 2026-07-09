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
import java.util.List;

import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TAction;
import org.open.oasis.docs.odata.ns.edm.TActionFunctionParameter;
import org.open.oasis.docs.odata.ns.edm.TActionFunctionReturnType;
import org.open.oasis.docs.odata.ns.edm.TActionImport;
import org.open.oasis.docs.odata.ns.edm.TComplexType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
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
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;
import org.open.oasis.docs.odata.ns.edmx.TInclude;
import org.open.oasis.docs.odata.ns.edmx.TReference;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Serializes the OASIS EDM/EDMX model to the CSDL <b>JSON</b> representation ([OData-CSDL-JSON]).
 * The counterpart of the EMF/XMI serialization of the same {@link EdmxRoot} tree — one converter
 * output (ADR-0002), two wire forms. Coverage matches what {@link EcoreToEdmConverter} emits:
 * entity/complex types (keys, inheritance, facets), navigation (partner, containment, referential
 * constraints), enums, functions/actions + imports, entity sets/singletons with bindings,
 * references, and constant-expression annotations. JSON semantics differ from XML in two places
 * this writer normalises: {@code $Nullable} defaults to <i>false</i> (XML: true) and collections
 * are {@code $Collection: true} instead of the {@code Collection(...)} type wrapper.
 */
public final class CsdlJsonWriter {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private CsdlJsonWriter() {
	}

	/** The CSDL JSON document for the EDMX tree, as a JSON string. */
	public static String write(EdmxRoot root) {
		TEdmx edmx = root.getEdmx();
		ObjectNode document = MAPPER.createObjectNode();
		document.put("$Version", edmx.getVersion() != null ? edmx.getVersion().getLiteral() : "4.0");
		if (!edmx.getReference().isEmpty()) {
			ObjectNode references = document.putObject("$Reference");
			for (TReference reference : edmx.getReference()) {
				ObjectNode ref = references.putObject(reference.getUri());
				ArrayNode includes = ref.putArray("$Include");
				for (TInclude include : reference.getInclude()) {
					ObjectNode inc = includes.addObject();
					inc.put("$Namespace", include.getNamespace());
					if (include.getAlias() != null) {
						inc.put("$Alias", include.getAlias());
					}
				}
			}
		}
		String entityContainer = null;
		for (SchemaType schema : edmx.getDataServices().getSchema()) {
			document.set(schema.getNamespace(), schemaNode(schema));
			for (TEntityContainer container : schema.getEntityContainer()) {
				entityContainer = schema.getNamespace() + "." + container.getName();
			}
		}
		if (entityContainer != null) {
			document.put("$EntityContainer", entityContainer);
		}
		return document.toPrettyString();
	}

	private static ObjectNode schemaNode(SchemaType schema) {
		ObjectNode node = MAPPER.createObjectNode();
		if (schema.getAlias() != null && !schema.getAlias().isBlank()) {
			node.put("$Alias", schema.getAlias());
		}
		annotations(schema.getAnnotation(), node);
		for (TEnumType en : schema.getEnumType()) {
			ObjectNode enumNode = node.putObject(en.getName());
			enumNode.put("$Kind", "EnumType");
			for (TEnumTypeMember member : en.getMember()) {
				enumNode.put(member.getName(), member.getValue());
			}
		}
		for (TEntityType t : schema.getEntityType()) {
			node.set(t.getName(), entityTypeNode(t));
		}
		for (TComplexType t : schema.getComplexType()) {
			node.set(t.getName(), complexTypeNode(t));
		}
		for (TFunction f : schema.getFunction()) { // overloads share one array member
			overloads(node, f.getName()).add(functionNode(f));
		}
		for (TAction a : schema.getAction()) {
			overloads(node, a.getName()).add(actionNode(a));
		}
		for (TEntityContainer container : schema.getEntityContainer()) {
			node.set(container.getName(), containerNode(container));
		}
		return node;
	}

	private static ArrayNode overloads(ObjectNode schema, String name) {
		return schema.get(name) instanceof ArrayNode existing ? existing : schema.putArray(name);
	}

	private static ObjectNode entityTypeNode(TEntityType t) {
		ObjectNode node = MAPPER.createObjectNode();
		node.put("$Kind", "EntityType");
		if (t.isAbstract()) {
			node.put("$Abstract", true);
		}
		if (t.isOpenType()) {
			node.put("$OpenType", true);
		}
		if (t.isHasStream()) {
			node.put("$HasStream", true);
		}
		if (t.getBaseType() != null) {
			node.put("$BaseType", String.valueOf(t.getBaseType()));
		}
		if (!t.getKey().isEmpty()) {
			ArrayNode key = node.putArray("$Key");
			for (TPropertyRef ref : t.getKey().get(0).getPropertyRef()) {
				key.add(ref.getName());
			}
		}
		annotations(t.getAnnotation(), node);
		structural(t.getProperty(), t.getNavigationProperty(), node);
		return node;
	}

	private static ObjectNode complexTypeNode(TComplexType t) {
		ObjectNode node = MAPPER.createObjectNode();
		node.put("$Kind", "ComplexType");
		if (t.isAbstract()) {
			node.put("$Abstract", true);
		}
		if (t.isOpenType()) {
			node.put("$OpenType", true);
		}
		if (t.getBaseType() != null) {
			node.put("$BaseType", String.valueOf(t.getBaseType()));
		}
		annotations(t.getAnnotation(), node);
		structural(t.getProperty(), t.getNavigationProperty(), node);
		return node;
	}

	private static void structural(List<TProperty> properties,
			List<TNavigationProperty> navigations, ObjectNode node) {
		for (TProperty p : properties) {
			ObjectNode prop = node.putObject(p.getName());
			type(String.valueOf(p.getType()), prop);
			if (p.isNullable()) { // JSON default is false — only nullable is written explicitly
				prop.put("$Nullable", true);
			}
			if (p.getMaxLength() != null) {
				prop.put("$MaxLength", Integer.parseInt(String.valueOf(p.getMaxLength())));
			}
			if (p.getPrecision() != null) {
				prop.put("$Precision", p.getPrecision().intValue());
			}
			if (p.getScale() != null) {
				prop.put("$Scale", Integer.parseInt(String.valueOf(p.getScale())));
			}
			if (p.getDefaultValue() != null) {
				prop.put("$DefaultValue", p.getDefaultValue());
			}
			annotations(p.getAnnotation(), prop);
		}
		for (TNavigationProperty n : navigations) {
			ObjectNode nav = node.putObject(n.getName());
			nav.put("$Kind", "NavigationProperty");
			type(String.valueOf(n.getType()), nav);
			if (!isCollection(String.valueOf(n.getType())) && n.isNullable()) {
				nav.put("$Nullable", true);
			}
			if (n.getPartner() != null) {
				nav.put("$Partner", n.getPartner());
			}
			if (n.isContainsTarget()) {
				nav.put("$ContainsTarget", true);
			}
			if (!n.getReferentialConstraint().isEmpty()) {
				ObjectNode constraints = nav.putObject("$ReferentialConstraint");
				for (TReferentialConstraint rc : n.getReferentialConstraint()) {
					constraints.put(String.valueOf(rc.getProperty()),
							String.valueOf(rc.getReferencedProperty()));
				}
			}
			annotations(n.getAnnotation(), nav);
		}
	}

	private static ObjectNode functionNode(TFunction f) {
		ObjectNode node = MAPPER.createObjectNode();
		node.put("$Kind", "Function");
		if (f.isIsBound()) {
			node.put("$IsBound", true);
		}
		if (f.isIsComposable()) {
			node.put("$IsComposable", true);
		}
		if (f.getEntitySetPath() != null) {
			node.put("$EntitySetPath", f.getEntitySetPath());
		}
		parameters(f.getParameter(), node);
		if (f.getReturnType() != null) {
			node.set("$ReturnType", returnTypeNode(f.getReturnType()));
		}
		return node;
	}

	private static ObjectNode actionNode(TAction a) {
		ObjectNode node = MAPPER.createObjectNode();
		node.put("$Kind", "Action");
		if (a.isIsBound()) {
			node.put("$IsBound", true);
		}
		if (a.getEntitySetPath() != null) {
			node.put("$EntitySetPath", a.getEntitySetPath());
		}
		parameters(a.getParameter(), node);
		if (!a.getReturnType().isEmpty()) {
			node.set("$ReturnType", returnTypeNode(a.getReturnType().get(0)));
		}
		return node;
	}

	private static void parameters(List<TActionFunctionParameter> parameters, ObjectNode node) {
		if (parameters.isEmpty()) {
			return;
		}
		ArrayNode array = node.putArray("$Parameter");
		for (TActionFunctionParameter p : parameters) {
			ObjectNode param = array.addObject();
			param.put("$Name", p.getName());
			type(String.valueOf(p.getType()), param);
			if (!isCollection(String.valueOf(p.getType())) && p.isNullable()) {
				param.put("$Nullable", true);
			}
		}
	}

	private static ObjectNode returnTypeNode(TActionFunctionReturnType rt) {
		ObjectNode node = MAPPER.createObjectNode();
		type(String.valueOf(rt.getType()), node);
		if (!isCollection(String.valueOf(rt.getType())) && rt.isNullable()) {
			node.put("$Nullable", true);
		}
		return node;
	}

	private static ObjectNode containerNode(TEntityContainer container) {
		ObjectNode node = MAPPER.createObjectNode();
		node.put("$Kind", "EntityContainer");
		annotations(container.getAnnotation(), node);
		for (TEntitySet set : container.getEntitySet()) {
			ObjectNode setNode = node.putObject(set.getName());
			setNode.put("$Collection", true);
			setNode.put("$Type", String.valueOf(set.getEntityType()));
			bindings(set.getNavigationPropertyBinding(), setNode);
		}
		for (TSingleton singleton : container.getSingleton()) {
			ObjectNode singleNode = node.putObject(singleton.getName());
			singleNode.put("$Type", String.valueOf(singleton.getType()));
			bindings(singleton.getNavigationPropertyBinding(), singleNode);
		}
		for (TFunctionImport imp : container.getFunctionImport()) {
			ObjectNode impNode = node.putObject(imp.getName());
			impNode.put("$Function", String.valueOf(imp.getFunction()));
			if (imp.getEntitySet() != null) {
				impNode.put("$EntitySet", String.valueOf(imp.getEntitySet()));
			}
		}
		for (TActionImport imp : container.getActionImport()) {
			ObjectNode impNode = node.putObject(imp.getName());
			impNode.put("$Action", String.valueOf(imp.getAction()));
			if (imp.getEntitySet() != null) {
				impNode.put("$EntitySet", String.valueOf(imp.getEntitySet()));
			}
		}
		return node;
	}

	private static void bindings(List<TNavigationPropertyBinding> bindings, ObjectNode node) {
		if (bindings.isEmpty()) {
			return;
		}
		ObjectNode bindingNode = node.putObject("$NavigationPropertyBinding");
		for (TNavigationPropertyBinding binding : bindings) {
			bindingNode.put(String.valueOf(binding.getPath()), String.valueOf(binding.getTarget()));
		}
	}

	/**
	 * Constant-expression annotations as {@code "@Term": value} members. The value keeps its JSON
	 * type (bool/number/string); enum members serialise as their qualified-name string.
	 */
	private static void annotations(List<AnnotationType> annotations, ObjectNode node) {
		for (AnnotationType a : annotations) {
			if (a.getTerm() == null) {
				continue;
			}
			String member = "@" + a.getTerm();
			if (a.isSetBool1()) {
				node.put(member, a.isBool1());
			} else if (a.getInt1() != null) {
				node.put(member, a.getInt1());
			} else if (a.getDecimal1() != null) {
				node.put(member, new BigDecimal(a.getDecimal1()));
			} else if (a.getString1() != null) {
				node.put(member, a.getString1());
			} else if (a.getEnumMember1() != null && !a.getEnumMember1().isEmpty()) {
				node.put(member, String.join(" ", a.getEnumMember1()));
			}
		}
	}

	/** {@code Collection(X)} (the XML type form) → {@code $Collection: true} + unwrapped {@code $Type}. */
	private static void type(String typeName, ObjectNode node) {
		if (isCollection(typeName)) {
			node.put("$Collection", true);
			node.put("$Type", typeName.substring(EdmTypes.COLLECTION_OPEN.length(), typeName.length() - 1));
		} else {
			node.put("$Type", typeName);
		}
	}

	private static boolean isCollection(String type) {
		return type.startsWith(EdmTypes.COLLECTION_OPEN);
	}
}
