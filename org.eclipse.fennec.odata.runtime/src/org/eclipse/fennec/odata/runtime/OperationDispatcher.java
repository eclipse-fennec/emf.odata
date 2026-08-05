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
package org.eclipse.fennec.odata.runtime;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.eclipse.fennec.m2x.model.ocl.IntegerLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OclFactory;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.RealLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.Variable;
import org.eclipse.fennec.m2x.model.ocl.VariableExp;
import org.eclipse.fennec.emf.osgi.metadata.MetadataService;
import org.eclipse.fennec.odata.codec.json.ODataJsonResourceImpl;
import org.eclipse.fennec.odata.csdl.CsdlJsonWriter;
import org.eclipse.fennec.odata.csdl.EcoreToEdmConverter;
import org.eclipse.fennec.odata.csdl.ODataAnnotationConstants;
import org.eclipse.fennec.odata.csdl.OdataResolver;
import org.eclipse.fennec.odata.csdl.profile.ODataPackageProfile;
import org.eclipse.fennec.odata.operation.api.ODataOperationHandler;
import org.eclipse.fennec.odata.persistence.api.ApplyQuery;
import org.eclipse.fennec.odata.persistence.api.ApplyResult;
import org.eclipse.fennec.odata.persistence.api.DeltaGoneException;
import org.eclipse.fennec.odata.persistence.api.DeltaService;
import org.eclipse.fennec.odata.persistence.api.EntityQuery;
import org.eclipse.fennec.odata.persistence.api.MediaService;
import org.eclipse.fennec.odata.persistence.api.QueryResult;
import org.eclipse.fennec.odata.persistence.api.QueryService;
import org.eclipse.fennec.odata.persistence.api.WriteService;
import org.eclipse.fennec.odata.query.CachingODataQueryParser;
import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.ODataResourceParser;
import org.eclipse.fennec.odata.ocl.evaluator.OclEvaluator;
import org.eclipse.fennec.odata.query.OrderBySegment;
import org.eclipse.fennec.odata.query.ResourcePath;
import org.eclipse.fennec.odata.query.apply.ApplyPipeline;
import org.eclipse.fennec.odata.query.apply.ComputeExpression;
import org.eclipse.fennec.odata.query.apply.ComputeTransformation;
import org.open.oasis.docs.odata.ns.edm.AnnotationType;
import org.open.oasis.docs.odata.ns.edm.EdmFactory;
import org.open.oasis.docs.odata.ns.edm.EdmPackage;
import org.open.oasis.docs.odata.ns.edm.SchemaType;
import org.open.oasis.docs.odata.ns.edm.TEntityContainer;
import org.open.oasis.docs.odata.ns.edm.TPropertyValue;
import org.open.oasis.docs.odata.ns.edm.TRecordExpression;
import org.open.oasis.docs.odata.ns.edmx.EdmxFactory;
import org.open.oasis.docs.odata.ns.edmx.EdmxPackage;
import org.open.oasis.docs.odata.ns.edmx.EdmxRoot;
import org.open.oasis.docs.odata.ns.edmx.TDataServices;
import org.open.oasis.docs.odata.ns.edmx.TEdmx;
import org.open.oasis.docs.odata.ns.edmx.TInclude;
import org.open.oasis.docs.odata.ns.edmx.TReference;
import org.open.oasis.docs.odata.ns.edmx.TVersion;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletName;
import org.osgi.service.servlet.whiteboard.propertytypes.HttpWhiteboardServletPattern;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The function/action arm of {@link ODataServlet} ([OData-Protocol] 11.5): unbound
 * function/action imports, bound functions ({@code Set(key)/Ns.Fn(...)}) and bound actions
 * ({@code POST Set(key)/Ns.Action}), parameter parsing/coercion and the operation-result
 * response shape. Invocation goes through the registered {@code ODataOperationHandler}
 * services; entity fetch and serialization stay on the servlet.
 */
final class OperationDispatcher {

	private final ODataServlet servlet;

	OperationDispatcher(ODataServlet servlet) {
		this.servlet = servlet;
	}


/** A single-segment path {@code Name()} / {@code Name(p=…)} — a function/action import call. */
static boolean isFunctionCall(String rawPath) {
	if (rawPath.indexOf('/') >= 0 || !rawPath.endsWith(")")) {
		return false;
	}
	int paren = rawPath.indexOf('(');
	if (paren <= 0) {
		return false;
	}
	return isFunctionArgs(rawPath.substring(paren + 1, rawPath.length() - 1));
}

/** A multi-segment path whose LAST segment is a function call — a bound function invocation. */
static boolean isBoundFunctionCall(String rawPath) {
	int lastSlash = rawPath.lastIndexOf('/');
	if (lastSlash < 0 || !rawPath.endsWith(")")) {
		return false;
	}
	String segment = rawPath.substring(lastSlash + 1);
	int paren = segment.indexOf('(');
	return paren > 0 && isFunctionArgs(segment.substring(paren + 1, segment.length() - 1));
}

/** Function arguments distinguish a call from an entity key: named params (or none). */
private static boolean isFunctionArgs(String inside) {
	return inside.isBlank() || inside.matches("\\s*[A-Za-z_]\\w*\\s*=.*");
}

/** Invokes a bound function {@code Set(key)/Ns.Func(p=…)} on the addressed entity. */
void boundFunction(String rawPath, HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	int lastSlash = rawPath.lastIndexOf('/');
	String prefix = rawPath.substring(0, lastSlash);
	String segment = rawPath.substring(lastSlash + 1);
	int paren = segment.indexOf('(');
	String qualified = segment.substring(0, paren);
	String localName = qualified.contains(".")
			? qualified.substring(qualified.lastIndexOf('.') + 1) : qualified;
	String parameterList = segment.substring(paren + 1, segment.length() - 1);

	ResourcePath path;
	try {
		path = servlet.resourceParser.parse(prefix);
	} catch (ODataQueryParseException e) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "resource not found");
		return;
	}
	path = servlet.keyAsSegment(path);
	if (path.key() == null || !path.segments().isEmpty()) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND,
				"a bound function is invoked on a keyed entity");
		return;
	}
	ODataServlet.Target target = servlet.resolveTarget(path.entitySet(), response);
	if (target == null) {
		return;
	}
	EObject entity = servlet.fetchByKey(target, path.key(), path.namedKeys(), Set.of(), response);
	if (entity == null) {
		return; // error already written
	}
	invokeBoundFunction(entity, target.entityType(), localName, parameterList, request, response);
}

/** Whether the type carries a BOUND operation with the given (local) name. */
static boolean hasBoundOperation(EClass entityType, String localName) {
	return entityType.getEAllOperations().stream()
			.anyMatch(op -> op.getName().equals(localName) && !isUnbound(op));
}

/** Resolves and dispatches a bound function on an already-loaded entity (shared tail). */
void invokeBoundFunction(EObject entity, EClass declaredType, String localName,
		String parameterList, HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	EOperation operation = entity.eClass().getEAllOperations().stream()
			.filter(op -> op.getName().equals(localName) && !isUnbound(op)).findFirst().orElse(null);
	if (operation == null) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "no bound function '" + localName + "'");
		return;
	}
	String qualifiedName = operationNamespace(declaredType) + "." + localName;
	ODataOperationHandler handler = servlet.operationHandlers.stream()
			.filter(h -> h.handles(qualifiedName)).findFirst().orElse(null);
	if (handler == null) {
		servlet.error(response, 501, "no handler for the operation");
		return;
	}
	Object result = handler.invoke(operation, entity, functionParameters(parameterList, operation));
	writeFunctionResult(result, request, response);
}

/**
 * Invokes a bound action {@code POST Set(key)/Ns.Action} on the addressed entity, with the
 * parameters in the JSON body ([OData-Protocol] 11.5.4.2). Mirrors {@link #boundFunction} but for
 * the POST/body shape; the result is serialised like any operation result (void → 204).
 */
void boundAction(ResourcePath path, String qualified, HttpServletRequest request,
		HttpServletResponse response) throws IOException {
	String localName = qualified.contains(".")
			? qualified.substring(qualified.lastIndexOf('.') + 1) : qualified;
	ODataServlet.Target target = servlet.resolveTarget(path.entitySet(), response);
	if (target == null) {
		return;
	}
	EObject entity = servlet.fetchByKey(target, path.key(), path.namedKeys(), Set.of(), response);
	if (entity == null) {
		return; // error already written
	}
	EOperation operation = entity.eClass().getEAllOperations().stream()
			.filter(op -> op.getName().equals(localName) && !isUnbound(op)).findFirst().orElse(null);
	if (operation == null) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "no bound action '" + localName + "'");
		return;
	}
	Map<String, Object> parameters = readActionParameters(request, operation, response);
	if (parameters == null) {
		return; // error already written
	}
	String qualifiedName = operationNamespace(target.entityType()) + "." + localName;
	ODataOperationHandler handler = servlet.operationHandlers.stream()
			.filter(h -> h.handles(qualifiedName)).findFirst().orElse(null);
	if (handler == null) {
		servlet.error(response, 501, "no handler for the operation");
		return;
	}
	writeFunctionResult(handler.invoke(operation, entity, parameters), request, response);
}

private String operationNamespace(EClass entityType) {
	return servlet.profiles.computeIfAbsent(entityType.getEPackage(), p -> new OdataResolver().resolve(p))
			.getNamespace();
}

/** A bare name (no key, no nav) that is an unbound operation rather than an entity set. */
boolean isActionImport(String segment) {
	return segment.indexOf('/') < 0 && segment.indexOf('(') < 0
			&& servlet.resolveEntityType(segment) == null && resolveUnboundFunction(segment) != null;
}

/** Invokes an unbound action import: {@code POST ActionName} with the parameters in the body. */
void actionImport(String name, HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	UnboundOperation resolved = resolveUnboundFunction(name);
	Map<String, Object> parameters = readActionParameters(request, resolved.operation(), response);
	if (parameters == null) {
		return; // error already written
	}
	ODataOperationHandler handler = servlet.operationHandlers.stream()
			.filter(h -> h.handles(resolved.qualifiedName())).findFirst().orElse(null);
	if (handler == null) {
		servlet.error(response, 501, "no handler for the operation");
		return;
	}
	writeFunctionResult(handler.invoke(resolved.operation(), null, parameters), request, response);
}

/** Reads action parameters from the JSON request body, coerced to the operation's parameter types. */
private Map<String, Object> readActionParameters(HttpServletRequest request, EOperation operation,
		HttpServletResponse response) throws IOException {
	byte[] body = request.getInputStream().readNBytes(servlet.limits.maxBodyBytes() + 1);
	if (body.length > servlet.limits.maxBodyBytes()) {
		servlet.error(response, 413, "payload exceeds the maximum size of " + servlet.limits.maxBodyBytes() + " bytes");
		return null;
	}
	Map<String, Object> parameters = new LinkedHashMap<>();
	if (body.length == 0) {
		return parameters; // a parameterless action
	}
	JsonNode node;
	try {
		node = ODataServlet.JSON.readTree(body);
	} catch (Exception e) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "malformed payload");
		return null;
	}
	if (!(node instanceof ObjectNode object)) {
		servlet.error(response, HttpServletResponse.SC_BAD_REQUEST, "action parameters must be a JSON object");
		return null;
	}
	for (EParameter parameter : operation.getEParameters()) {
		JsonNode value = object.get(parameter.getName());
		if (value != null && !value.isNull()) {
			parameters.put(parameter.getName(), parameter.getEType() instanceof EDataType dataType
					? EcoreUtil.createFromString(dataType, value.asString())
					: value.asString());
		}
	}
	return parameters;
}

/** Invokes an unbound function import: resolve the operation, coerce params, dispatch, serialize. */
void functionImport(String rawPath, HttpServletRequest request, HttpServletResponse response)
		throws IOException {
	int paren = rawPath.indexOf('(');
	String name = rawPath.substring(0, paren);
	String parameterList = rawPath.substring(paren + 1, rawPath.length() - 1);
	UnboundOperation resolved = resolveUnboundFunction(name);
	if (resolved == null) {
		servlet.error(response, HttpServletResponse.SC_NOT_FOUND, "no function import '" + name + "'");
		return;
	}
	Map<String, Object> parameters = functionParameters(parameterList, resolved.operation());
	ODataOperationHandler handler = servlet.operationHandlers.stream()
			.filter(h -> h.handles(resolved.qualifiedName())).findFirst().orElse(null);
	if (handler == null) {
		servlet.error(response, 501, "no handler for the operation");
		return;
	}
	Object result = handler.invoke(resolved.operation(), null, parameters);
	writeFunctionResult(result, request, response);
}

/** An unbound operation plus its namespace-qualified name (the handler dispatch key). */
private record UnboundOperation(EOperation operation, String qualifiedName) {
}

/** Finds an unbound ({@code @OData.Bound=false}) operation with the given name across the models. */
UnboundOperation resolveUnboundFunction(String name) {
	for (EPackage pkg : servlet.packages) {
		ODataPackageProfile profile = servlet.profiles.computeIfAbsent(pkg,
				p -> new OdataResolver().resolve(p));
		for (EClassifier classifier : pkg.getEClassifiers()) {
			if (classifier instanceof EClass eClass) {
				for (EOperation operation : eClass.getEAllOperations()) {
					if (operation.getName().equals(name) && isUnbound(operation)) {
						return new UnboundOperation(operation, profile.getNamespace() + "." + name);
					}
				}
			}
		}
	}
	return null;
}

private static boolean isUnbound(EOperation operation) {
	EAnnotation annotation = operation.getEAnnotation(ODataAnnotationConstants.SOURCE);
	return annotation != null
			&& "false".equals(annotation.getDetails().get(ODataAnnotationConstants.BOUND));
}

private static Map<String, Object> functionParameters(String parameterList, EOperation operation) {
	Map<String, Object> parameters = new LinkedHashMap<>();
	if (parameterList.isBlank()) {
		return parameters;
	}
	for (String part : parameterList.split(",")) {
		int equals = part.indexOf('=');
		if (equals < 0) {
			throw new ODataQueryParseException("function parameter must be name=value: " + part);
		}
		String parameterName = part.substring(0, equals).trim();
		String raw = part.substring(equals + 1).trim();
		EParameter parameter = operation.getEParameters().stream()
				.filter(p -> p.getName().equals(parameterName)).findFirst()
				.orElseThrow(() -> new ODataQueryParseException(
						"unknown parameter '" + parameterName + "'"));
		parameters.put(parameterName, coerceParameter(raw, parameter));
	}
	return parameters;
}

private static Object coerceParameter(String raw, EParameter parameter) {
	String literal = raw.length() >= 2 && raw.startsWith("'") && raw.endsWith("'")
			? raw.substring(1, raw.length() - 1).replace("''", "'")
			: raw;
	if (parameter.getEType() instanceof EDataType dataType) {
		return EcoreUtil.createFromString(dataType, literal);
	}
	return literal;
}

/** Serializes a function/action result: void (204), a single entity, a collection, or a value. */
private void writeFunctionResult(Object result, HttpServletRequest request,
		HttpServletResponse response) throws IOException {
	if (result == null) {
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		return;
	}
	response.setContentType(ODataServlet.contentTypeJson());
	if (result instanceof EObject entity) {
		String json = servlet.formats.entityJson(entity, entity.eClass(), null, Set.of());
		response.getWriter().write(ODataServlet.withContext(ODataServlet.contextRoot(request) + "/$metadata#"
				+ entity.eClass().getName() + "/$entity", json));
		return;
	}
	if (result instanceof Collection<?> collection) {
		StringBuilder body = new StringBuilder("{\"value\":[");
		boolean first = true;
		for (Object element : collection) {
			if (!(element instanceof EObject entity)) {
				throw new ODataQueryParseException("a collection function result must hold entities");
			}
			body.append(first ? "" : ",").append(servlet.formats.entityJson(entity, entity.eClass(), null, Set.of()));
			first = false;
		}
		response.getWriter().write(body.append("]}").toString());
		return;
	}
	response.getWriter().write("{\"value\":" + ODataServlet.JSON.writeValueAsString(result) + "}");
}
}
