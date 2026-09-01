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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OclFactory;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.odata.query.ODataQueryParseException;
import org.eclipse.fennec.odata.query.OrderBySegment;

/**
 * The nested options a {@code $expand} or {@code $select} item may apply to its collection
 * ([OData-URL] 5.1.2/5.1.3; Advanced §13.1.3/9.2+9.4–9.7 and §13.2.3/5.1–5.4): a filter over
 * the items (free-text {@code $search} folds into it), ordering, paging and an inline count.
 * Evaluation happens on SHAPED copies ({@link EntityShaper}), never on backend objects.
 *
 * @param filter  item predicate ({@code $filter}, AND-folded with {@code $search}), or null
 * @param orderBy sort segments, never null (may be empty)
 * @param skip    items to skip ({@code $skip}, 0 = none)
 * @param top     maximum items to keep ({@code $top}, -1 = unlimited)
 * @param count   whether the (filtered, pre-paging) item count is requested ({@code $count})
 */
public record CollectionOptions(OclExpression filter, List<OrderBySegment> orderBy,
		int skip, int top, boolean count) {

	public static final CollectionOptions NONE = new CollectionOptions(null, List.of(), 0, -1, false);

	public CollectionOptions {
		orderBy = orderBy == null ? List.of() : List.copyOf(orderBy);
	}

	/** Only a filter (the pre-existing nested-{@code $filter} shape). */
	public static CollectionOptions filterOnly(OclExpression filter) {
		return new CollectionOptions(filter, List.of(), 0, -1, false);
	}

	/** The same options minus the item predicate — the backend already applied it (ADR-0008). */
	public CollectionOptions withoutFilter() {
		return filter == null ? this : new CollectionOptions(null, orderBy, skip, top, count);
	}

	/** The same options minus paging — the backend already applied it (ADR-0008). */
	public CollectionOptions withoutPaging() {
		return skip == 0 && top < 0 ? this
				: new CollectionOptions(filter, orderBy, 0, -1, count);
	}

	public boolean isNone() {
		return filter == null && orderBy.isEmpty() && skip == 0 && top < 0 && !count;
	}

	/**
	 * Accumulates the {@code ;}-separated nested options of one {@code $expand}/{@code $select}
	 * item. {@link #accept} consumes the options this record models and leaves everything else
	 * to the caller ({@code $select} recursion, honest 501s). {@code $search} folds into the
	 * filter as an AND — the same semantics as the top-level option pair.
	 */
	static final class Accumulator {

		private static final Pattern FILTER = Pattern.compile("(?i)^\\$?filter=");
		private static final Pattern ORDERBY = Pattern.compile("(?i)^\\$?orderby=");
		private static final Pattern TOP = Pattern.compile("(?i)^\\$?top=");
		private static final Pattern SKIP = Pattern.compile("(?i)^\\$?skip=");
		private static final Pattern COUNT = Pattern.compile("(?i)^\\$?count=");
		private static final Pattern SEARCH = Pattern.compile("(?i)^\\$?search=");

		private OclExpression filter;
		private List<OrderBySegment> orderBy = List.of();
		private int skip;
		private int top = -1;
		private boolean count;

		/** Consumes one nested option; false when it is none of this record's options. */
		boolean accept(String option, EStructuralFeature feature, EClass context,
				NestedOptionParser parser) {
			Matcher matcher = FILTER.matcher(option);
			if (matcher.find()) {
				requireCollection(feature, "$filter");
				fold(parser.filter(option.substring(matcher.end()).trim(), context));
				return true;
			}
			matcher = SEARCH.matcher(option);
			if (matcher.find()) {
				requireCollection(feature, "$search");
				fold(parser.search(option.substring(matcher.end()).trim(), context));
				return true;
			}
			matcher = ORDERBY.matcher(option);
			if (matcher.find()) {
				requireCollection(feature, "$orderby");
				orderBy = parser.orderBy(option.substring(matcher.end()).trim(), context);
				return true;
			}
			matcher = TOP.matcher(option);
			if (matcher.find()) {
				requireCollection(feature, "$top");
				top = positiveInt(option.substring(matcher.end()).trim(), "$top");
				return true;
			}
			matcher = SKIP.matcher(option);
			if (matcher.find()) {
				requireCollection(feature, "$skip");
				skip = positiveInt(option.substring(matcher.end()).trim(), "$skip");
				return true;
			}
			matcher = COUNT.matcher(option);
			if (matcher.find()) {
				requireCollection(feature, "$count");
				count = Boolean.parseBoolean(option.substring(matcher.end()).trim());
				return true;
			}
			return false;
		}

		private void fold(OclExpression predicate) {
			if (filter == null) {
				filter = predicate;
			} else { // $filter and $search combine as AND
				OperationCallExp and = OclFactory.eINSTANCE.createOperationCallExp();
				and.setName("and");
				and.setOwnedSource(filter);
				and.getOwnedArguments().add(predicate);
				filter = and;
			}
		}

		private static void requireCollection(EStructuralFeature feature, String option) {
			if (!feature.isMany()) {
				throw new ODataQueryParseException(
						option + " inside $expand/$select applies to collection-valued properties");
			}
		}

		private static int positiveInt(String value, String option) {
			try {
				int parsed = Integer.parseInt(value);
				if (parsed < 0) {
					throw new NumberFormatException();
				}
				return parsed;
			} catch (NumberFormatException e) {
				throw new ODataQueryParseException("invalid nested " + option + " value");
			}
		}

		CollectionOptions build() {
			return new CollectionOptions(filter, orderBy, skip, top, count);
		}
	}
}
