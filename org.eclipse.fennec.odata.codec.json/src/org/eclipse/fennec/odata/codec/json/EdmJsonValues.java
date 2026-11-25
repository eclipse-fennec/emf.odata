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
import java.math.BigDecimal;
import java.math.BigInteger;
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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.codec.value.CodecReaderContext;
import org.eclipse.fennec.codec.value.CodecValueReader;
import org.eclipse.fennec.codec.value.CodecValueWriter;
import org.eclipse.fennec.codec.value.CodecWriterContext;

/**
 * OData-JSON value formats for {@code Edm.*} primitive types that JSON does not carry natively
 * (req §3.7): {@code Edm.Date} (ISO date without time), {@code Edm.TimeOfDay}, {@code Edm.DateTimeOffset}
 * (ISO 8601 with offset), {@code Edm.Binary} (base64url). Writers accept the attribute's Java value
 * ({@code java.util.Date} from {@code EDate}, {@code XMLGregorianCalendar} from the XSD datatypes of
 * {@code XMLTypePackage}, or {@code String} when the Edm type was forced onto a string attribute via
 * {@code @OData.Type}) and always emit the spec string form; readers convert back to the attribute's
 * instance class.
 *
 * <p>{@code Edm.Guid} and {@code Edm.Duration} need no custom codec: their Java values (a string,
 * or a {@code javax.xml.datatype.Duration} from {@code xs:duration}) already convert to the spec
 * form through the datatype itself. {@code Edm.Decimal} gets an explicit NUMBER writer — the base
 * codec would render {@code BigDecimal} as a string, but [OData-JSON] 7.1 wants numbers by
 * default. The exception is {@code IEEE754Compatible=true} ([OData-JSON] 8.1), which
 * serializes {@code Edm.Int64} and {@code Edm.Decimal} as STRINGS so JavaScript clients do
 * not lose precision to IEEE 754 doubles.
 */
final class EdmJsonValues {

	/** {@code uuuu-MM-ddTHH:mm:ss±HH:MM} ("Z" at zero offset) — valid Edm.DateTimeOffset AND xs:dateTime. */
	private static final DateTimeFormatter EDM_DATE_TIME = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssXXX");

	/** {@code IEEE754Compatible=true}: Int64/Decimal in their exact lexical string form. */
	private static final CodecValueWriter<Object, EAttribute> IEEE754_WRITER =
			writer("odataIeee754", value -> value instanceof BigDecimal decimal
					? decimal.toPlainString() : String.valueOf(value));

	private static final CodecValueReader<Object, EAttribute> IEEE754_READER =
			reader("odataIeee754", EdmJsonValues::readIeee754);

	private static final Map<String, CodecValueWriter<Object, EAttribute>> WRITERS = Map.of(
			"Edm.Date", writer("odataDate", EdmJsonValues::writeDate),
			"Edm.TimeOfDay", writer("odataTimeOfDay", EdmJsonValues::writeTimeOfDay),
			"Edm.DateTimeOffset", writer("odataDateTimeOffset", EdmJsonValues::writeDateTimeOffset),
			"Edm.Binary", writer("odataBinary", EdmJsonValues::writeBinary),
			"Edm.Decimal", decimalNumberWriter());

	private static final Map<String, CodecValueReader<Object, EAttribute>> READERS = Map.of(
			"Edm.Date", reader("odataDate", EdmJsonValues::readDate),
			"Edm.TimeOfDay", reader("odataTimeOfDay", EdmJsonValues::readTimeOfDay),
			"Edm.DateTimeOffset", reader("odataDateTimeOffset", EdmJsonValues::readDateTimeOffset),
			"Edm.Binary", reader("odataBinary", EdmJsonValues::readBinary));

	/** Writer for the (collection-unwrapped) Edm type name, or {@code null} if the default suffices. */
	static CodecValueWriter<Object, EAttribute> writer(String edmType, boolean ieee754Compatible) {
		if (ieee754Compatible && isIeee754Affected(edmType)) {
			return IEEE754_WRITER;
		}
		return WRITERS.get(edmType);
	}

	/** Reader for the (collection-unwrapped) Edm type name, or {@code null} if the default suffices. */
	static CodecValueReader<Object, EAttribute> reader(String edmType, boolean ieee754Compatible) {
		if (ieee754Compatible && isIeee754Affected(edmType)) {
			return IEEE754_READER;
		}
		return READERS.get(edmType);
	}

	private static boolean isIeee754Affected(String edmType) {
		return "Edm.Int64".equals(edmType) || "Edm.Decimal".equals(edmType);
	}

	private static Object readIeee754(String raw, EAttribute attribute) {
		Class<?> target = attribute.getEAttributeType().getInstanceClass();
		try {
			if (target == Long.class || target == long.class) {
				return Long.parseLong(raw);
			}
			if (target != null && BigInteger.class.isAssignableFrom(target)) {
				return new BigInteger(raw);
			}
			if (target != null && BigDecimal.class.isAssignableFrom(target)) {
				return new BigDecimal(raw);
			}
			return raw; // @OData.Type forced onto a string attribute — keep the lexical form
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("invalid IEEE754Compatible number value", e);
		}
	}

	// --- write: Java value → OData JSON string form ---

	private static String writeDate(Object value) {
		if (value instanceof Date date) {
			return LocalDate.ofInstant(date.toInstant(), ZoneOffset.UTC).toString();
		}
		if (value instanceof XMLGregorianCalendar calendar) {
			// an xs:date MAY carry a timezone, an Edm.Date never does — take the date fields
			return hasDate(calendar) ? localDate(calendar).toString() : calendar.toXMLFormat();
		}
		return String.valueOf(value); // @OData.Type on a string attribute → already spec form
	}

	private static String writeTimeOfDay(Object value) {
		if (value instanceof Date date) {
			return LocalTime.ofInstant(date.toInstant(), ZoneOffset.UTC).toString();
		}
		if (value instanceof XMLGregorianCalendar calendar) {
			return hasTime(calendar) ? localTime(calendar).toString() : calendar.toXMLFormat();
		}
		return String.valueOf(value);
	}

	private static String writeDateTimeOffset(Object value) {
		if (value instanceof Date date) {
			return DateTimeFormatter.ISO_INSTANT.format(date.toInstant());
		}
		if (value instanceof XMLGregorianCalendar calendar) {
			return hasDate(calendar) && hasTime(calendar)
					? edmDateTime(OffsetDateTime.of(localDate(calendar), localTime(calendar),
							offset(calendar)))
					: calendar.toXMLFormat();
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
		if (targetsCalendar(attribute)) {
			try {
				return calendar(attribute, LocalDate.parse(raw).toString());
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
		if (targetsCalendar(attribute)) {
			try {
				return calendar(attribute, xsdTime(LocalTime.parse(raw)));
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
		if (targetsCalendar(attribute)) {
			try {
				return calendar(attribute, edmDateTime(OffsetDateTime.parse(raw)));
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

	// --- XSD calendar values (XMLTypePackage: date, dateTime, time) ---

	/** An attribute typed by an XSD date/time datatype — its value is an XMLGregorianCalendar. */
	private static boolean targetsCalendar(EAttribute attribute) {
		Class<?> instanceClass = attribute.getEAttributeType().getInstanceClass();
		return instanceClass != null && XMLGregorianCalendar.class.isAssignableFrom(instanceClass);
	}

	/**
	 * The XSD lexical form parsed by the DATATYPE'S OWN factory, so the value is the
	 * XMLGregorianCalendar implementation EMF itself creates (XMI serialization and
	 * {@code convertToString} expect it, a foreign implementation can break them).
	 */
	private static Object calendar(EAttribute attribute, String lexical) {
		return EcoreUtil.createFromString(attribute.getEAttributeType(), lexical);
	}

	private static boolean hasDate(XMLGregorianCalendar calendar) {
		return calendar.getYear() != DatatypeConstants.FIELD_UNDEFINED
				&& calendar.getMonth() != DatatypeConstants.FIELD_UNDEFINED
				&& calendar.getDay() != DatatypeConstants.FIELD_UNDEFINED;
	}

	private static boolean hasTime(XMLGregorianCalendar calendar) {
		return calendar.getHour() != DatatypeConstants.FIELD_UNDEFINED
				&& calendar.getMinute() != DatatypeConstants.FIELD_UNDEFINED;
	}

	private static LocalDate localDate(XMLGregorianCalendar calendar) {
		return LocalDate.of(calendar.getYear(), calendar.getMonth(), calendar.getDay());
	}

	private static LocalTime localTime(XMLGregorianCalendar calendar) {
		int second = calendar.getSecond() == DatatypeConstants.FIELD_UNDEFINED ? 0 : calendar.getSecond();
		BigDecimal fraction = calendar.getFractionalSecond();
		int nanos = fraction == null ? 0 : fraction.movePointRight(9).intValue();
		return LocalTime.of(calendar.getHour(), calendar.getMinute(), second, nanos);
	}

	/** A timezone-less xs:dateTime is read as UTC — Edm.DateTimeOffset has no "local" form. */
	private static ZoneOffset offset(XMLGregorianCalendar calendar) {
		return calendar.getTimezone() == DatatypeConstants.FIELD_UNDEFINED
				? ZoneOffset.UTC
				: ZoneOffset.ofTotalSeconds(calendar.getTimezone() * 60);
	}

	/**
	 * Edm.DateTimeOffset with the seconds field always present: OData allows omitting it, XSD
	 * does not, and this form is both — so it round-trips through the XSD datatype unchanged.
	 */
	private static String edmDateTime(OffsetDateTime value) {
		return value.getNano() == 0 ? value.format(EDM_DATE_TIME) : value.toString();
	}

	/** Edm.TimeOfDay in the xs:time lexical form (seconds are mandatory there). */
	private static String xsdTime(LocalTime value) {
		return value.getNano() == 0
				? String.format("%02d:%02d:%02d", value.getHour(), value.getMinute(), value.getSecond())
				: value.toString(); // a fractional part already forces the seconds field
	}

	// --- adapter plumbing ---

	private interface StringForm {
		String apply(Object value);
	}

	private interface ValueParse {
		Object apply(String raw, EAttribute attribute);
	}

	/** {@code Edm.Decimal} as a JSON NUMBER ([OData-JSON] 7.1) — exact, no double detour. */
	private static CodecValueWriter<Object, EAttribute> decimalNumberWriter() {
		return new CodecValueWriter<>() {
			@Override
			public String getName() {
				return "odataDecimal";
			}

			@Override
			public void write(Object value, EAttribute feature, CodecWriterContext ctx) throws IOException {
				switch (value) {
					case null -> ctx.getGenerator().writeNull();
					case BigDecimal decimal -> ctx.getGenerator().writeNumber(decimal);
					case BigInteger integer -> ctx.getGenerator().writeNumber(integer);
					default -> {
						try { // @OData.Type on a string attribute — spec form is still a number
							ctx.getGenerator().writeNumber(new BigDecimal(String.valueOf(value)));
						} catch (NumberFormatException e) {
							ctx.getGenerator().writeString(String.valueOf(value));
						}
					}
				}
			}
		};
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
