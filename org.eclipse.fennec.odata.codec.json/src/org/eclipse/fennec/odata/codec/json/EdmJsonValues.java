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
package org.eclipse.fennec.odata.codec.json;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.fennec.codec.value.CodecReaderContext;
import org.eclipse.fennec.codec.value.CodecValueReader;
import org.eclipse.fennec.codec.value.CodecValueWriter;
import org.eclipse.fennec.codec.value.CodecWriterContext;

/**
 * OData-JSON value formats for {@code Edm.*} primitive types that JSON does not carry natively
 * (req §3.7): {@code Edm.Date} (ISO date without time), {@code Edm.TimeOfDay}, {@code Edm.DateTimeOffset}
 * (ISO 8601 with offset), {@code Edm.Binary} (base64url). Writers accept the attribute's Java value
 * ({@code java.util.Date} from {@code EDate}, or {@code String} when the Edm type was forced onto a
 * string attribute via {@code @OData.Type}) and always emit the spec string form; readers convert
 * back to the attribute's instance class.
 *
 * <p>{@code Edm.Guid}, {@code Edm.Duration} and {@code Edm.Decimal} need no custom codec: guid and
 * duration live on string attributes (pass through), decimals serialize as JSON numbers.
 */
final class EdmJsonValues {

	private static final Map<String, CodecValueWriter<Object, EAttribute>> WRITERS = Map.of(
			"Edm.Date", writer("odataDate", EdmJsonValues::writeDate),
			"Edm.TimeOfDay", writer("odataTimeOfDay", EdmJsonValues::writeTimeOfDay),
			"Edm.DateTimeOffset", writer("odataDateTimeOffset", EdmJsonValues::writeDateTimeOffset),
			"Edm.Binary", writer("odataBinary", EdmJsonValues::writeBinary));

	private static final Map<String, CodecValueReader<Object, EAttribute>> READERS = Map.of(
			"Edm.Date", reader("odataDate", EdmJsonValues::readDate),
			"Edm.TimeOfDay", reader("odataTimeOfDay", EdmJsonValues::readTimeOfDay),
			"Edm.DateTimeOffset", reader("odataDateTimeOffset", EdmJsonValues::readDateTimeOffset),
			"Edm.Binary", reader("odataBinary", EdmJsonValues::readBinary));

	/** Writer for the (collection-unwrapped) Edm type name, or {@code null} if the default suffices. */
	static CodecValueWriter<Object, EAttribute> writer(String edmType) {
		return WRITERS.get(edmType);
	}

	/** Reader for the (collection-unwrapped) Edm type name, or {@code null} if the default suffices. */
	static CodecValueReader<Object, EAttribute> reader(String edmType) {
		return READERS.get(edmType);
	}

	// --- write: Java value → OData JSON string form ---

	private static String writeDate(Object value) {
		if (value instanceof Date date) {
			return LocalDate.ofInstant(date.toInstant(), ZoneOffset.UTC).toString();
		}
		return String.valueOf(value); // @OData.Type on a string attribute → already spec form
	}

	private static String writeTimeOfDay(Object value) {
		if (value instanceof Date date) {
			return LocalTime.ofInstant(date.toInstant(), ZoneOffset.UTC).toString();
		}
		return String.valueOf(value);
	}

	private static String writeDateTimeOffset(Object value) {
		if (value instanceof Date date) {
			return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
		}
		return String.valueOf(value);
	}

	private static String writeBinary(Object value) {
		if (value instanceof byte[] bytes) {
			return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
		}
		return String.valueOf(value);
	}

	// --- read: OData JSON string form → the attribute's instance class ---

	private static Object readDate(String raw, EAttribute attribute) {
		if (targetsDate(attribute)) {
			try {
				return Date.from(LocalDate.parse(raw).atStartOfDay(ZoneOffset.UTC).toInstant());
			} catch (DateTimeException e) {
				throw new IllegalArgumentException("invalid Edm.Date value", e);
			}
		}
		return raw;
	}

	private static Object readTimeOfDay(String raw, EAttribute attribute) {
		if (targetsDate(attribute)) {
			try {
				return Date.from(LocalTime.parse(raw).atDate(LocalDate.EPOCH).toInstant(ZoneOffset.UTC));
			} catch (DateTimeException e) {
				throw new IllegalArgumentException("invalid Edm.TimeOfDay value", e);
			}
		}
		return raw;
	}

	private static Object readDateTimeOffset(String raw, EAttribute attribute) {
		if (targetsDate(attribute)) {
			try {
				return Date.from(OffsetDateTime.parse(raw).toInstant());
			} catch (DateTimeException e) {
				throw new IllegalArgumentException("invalid Edm.DateTimeOffset value", e);
			}
		}
		return raw;
	}

	private static Object readBinary(String raw, EAttribute attribute) {
		try {
			return Base64.getUrlDecoder().decode(raw);
		} catch (IllegalArgumentException urlDecodeFailed) {
			try {
				return Base64.getDecoder().decode(raw); // tolerate plain base64 on input
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("invalid Edm.Binary (base64) value", e);
			}
		}
	}

	private static boolean targetsDate(EAttribute attribute) {
		Class<?> instanceClass = attribute.getEAttributeType().getInstanceClass();
		return instanceClass != null && (Date.class.isAssignableFrom(instanceClass)
				|| Instant.class.isAssignableFrom(instanceClass));
	}

	// --- adapter plumbing ---

	private interface StringForm {
		String apply(Object value);
	}

	private interface ValueParse {
		Object apply(String raw, EAttribute attribute);
	}

	private static CodecValueWriter<Object, EAttribute> writer(String name, StringForm form) {
		return new CodecValueWriter<>() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public void write(Object value, EAttribute feature, CodecWriterContext ctx) throws IOException {
				if (value == null) {
					ctx.getGenerator().writeNull();
				} else {
					ctx.getGenerator().writeString(form.apply(value));
				}
			}
		};
	}

	private static CodecValueReader<Object, EAttribute> reader(String name, ValueParse parse) {
		return new CodecValueReader<>() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public Object read(CodecReaderContext ctx, EAttribute feature) throws IOException {
				String raw = ctx.getParser().getString();
				return raw == null ? null : parse.apply(raw, feature);
			}
		};
	}

	private EdmJsonValues() {
	}
}
