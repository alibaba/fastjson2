package com.alibaba.fastjson3.reader;

import com.alibaba.fastjson3.JSON;
import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONObject;
import com.alibaba.fastjson3.schema.JSONSchema;
import com.alibaba.fastjson3.util.JDKUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a single field to read during JSON deserialization.
 * Holds metadata (name, type, ordinal) and a reference to the underlying
 * {@link Field} or setter {@link Method} used to inject the value.
 */
public final class FieldReader implements Comparable<FieldReader> {
    public final String fieldName;
    public final String[] alternateNames;
    public final Type fieldType;
    public final Class<?> fieldClass;
    public final int ordinal;
    public final String defaultValue;
    public final boolean required;

    // Element class for List<X> fields
    public final Class<?> elementClass;

    // Exactly one of these is non-null
    private final Field field;
    private final Method setter;

    // Unsafe field offset for direct field injection (-1 if unavailable)
    public final long fieldOffset;

    // Pre-encoded '"fieldName":' as bytes for fast ordered matching
    public final byte[] fieldNameHeader;

    // Pre-computed long words for fast header comparison (avoids byte-by-byte loop)
    public final long hdrWord0;     // first 8 bytes as long (masked for short headers)
    public final long hdrMask0;     // mask for first word comparison
    public final long hdrWord1;     // bytes 8-15 as long (for headers > 8 bytes)
    public final long hdrMask1;     // mask for second word comparison

    // Optional: date/time format pattern + cached formatter
    public final String format;
    public final java.time.format.DateTimeFormatter formatter;

    // Optional: custom ObjectReader class (from @JSONField(deserializeUsing=))
    public final Class<?> deserializeUsingClass;

    // Optional: JSON Schema for validation during deserialization (from @JSONField(schema=))
    public final JSONSchema jsonSchema;

    // Index in the fieldReaders array (set after construction)
    public int index = -1;

    // Type tag for fast dispatch
    public static final int TAG_STRING = 1;
    public static final int TAG_INT = 2;
    public static final int TAG_LONG = 3;
    public static final int TAG_DOUBLE = 4;
    public static final int TAG_BOOLEAN = 5;
    public static final int TAG_FLOAT = 6;
    public static final int TAG_INT_OBJ = 7;
    public static final int TAG_LONG_OBJ = 8;
    public static final int TAG_DOUBLE_OBJ = 9;
    public static final int TAG_BOOLEAN_OBJ = 10;
    public static final int TAG_LIST = 11;
    public static final int TAG_POJO = 12;
    public static final int TAG_STRING_ARRAY = 13;
    public static final int TAG_LONG_ARRAY = 14;
    public static final int TAG_GENERIC = 0;

    public int typeTag;

    public FieldReader(
            String fieldName,
            String[] alternateNames,
            Type fieldType,
            Class<?> fieldClass,
            int ordinal,
            String defaultValue,
            boolean required,
            Field field,
            Method setter
    ) {
        this(fieldName, alternateNames, fieldType, fieldClass, ordinal, defaultValue,
                required, field, setter, null, null, null);
    }

    public FieldReader(
            String fieldName,
            String[] alternateNames,
            Type fieldType,
            Class<?> fieldClass,
            int ordinal,
            String defaultValue,
            boolean required,
            Field field,
            Method setter,
            String format,
            Class<?> deserializeUsingClass
    ) {
        this(fieldName, alternateNames, fieldType, fieldClass, ordinal, defaultValue,
                required, field, setter, format, deserializeUsingClass, null);
    }

    public FieldReader(
            String fieldName,
            String[] alternateNames,
            Type fieldType,
            Class<?> fieldClass,
            int ordinal,
            String defaultValue,
            boolean required,
            Field field,
            Method setter,
            String format,
            Class<?> deserializeUsingClass,
            String schema
    ) {
        this.fieldName = fieldName;
        this.alternateNames = alternateNames != null ? alternateNames : new String[0];
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.ordinal = ordinal;
        this.defaultValue = defaultValue;
        this.required = required;
        this.field = field;
        this.setter = setter;
        this.format = format;
        this.formatter = (format != null && !format.isEmpty())
                ? java.time.format.DateTimeFormatter.ofPattern(format) : null;
        this.deserializeUsingClass = deserializeUsingClass;

        // Parse JSON Schema for validation
        if (schema != null && !schema.isEmpty()) {
            JSONObject schemaObj = JSON.parseObject(schema);
            this.jsonSchema = (schemaObj != null && !schemaObj.isEmpty())
                    ? JSONSchema.of(schemaObj, fieldClass) : null;
        } else {
            this.jsonSchema = null;
        }

        if (field != null) {
            field.setAccessible(true);
        }
        if (setter != null) {
            setter.setAccessible(true);
        }

        // Extract element class for List<X>
        this.elementClass = resolveElementClass(fieldType, fieldClass);

        // Resolve Unsafe field offset (look up corresponding field if we only have a setter)
        this.fieldOffset = resolveFieldOffset(field, setter, fieldName, fieldClass);

        // Pre-compute type tag
        this.typeTag = resolveTypeTag(fieldClass);

        // Pre-encode '"fieldName":' as UTF-8 bytes for fast ordered matching
        {
            byte[] nameBytes = fieldName.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            byte[] h = new byte[nameBytes.length + 3];
            h[0] = '"';
            System.arraycopy(nameBytes, 0, h, 1, nameBytes.length);
            h[nameBytes.length + 1] = '"';
            h[nameBytes.length + 2] = ':';
            this.fieldNameHeader = h;
        }
        byte[] hdr = this.fieldNameHeader;

        // Pre-compute long words for fast header comparison
        int hdrLen = hdr.length;
        byte[] padded = new byte[16];
        System.arraycopy(hdr, 0, padded, 0, Math.min(hdrLen, 16));
        this.hdrWord0 = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(padded, 0);
        this.hdrMask0 = (hdrLen >= 8) ? -1L : (1L << (hdrLen * 8)) - 1;
        if (hdrLen > 8) {
            this.hdrWord1 = com.alibaba.fastjson3.util.JDKUtils.getLongDirect(padded, 8);
            this.hdrMask1 = (hdrLen - 8 >= 8) ? -1L : (1L << ((hdrLen - 8) * 8)) - 1;
        } else {
            this.hdrWord1 = 0;
            this.hdrMask1 = 0;
        }
    }

    private static int resolveTypeTag(Class<?> fc) {
        if (fc == String.class) {
            return TAG_STRING;
        } else if (fc == int.class) {
            return TAG_INT;
        } else if (fc == long.class) {
            return TAG_LONG;
        } else if (fc == double.class) {
            return TAG_DOUBLE;
        } else if (fc == boolean.class) {
            return TAG_BOOLEAN;
        } else if (fc == float.class) {
            return TAG_FLOAT;
        } else if (fc == Integer.class) {
            return TAG_INT_OBJ;
        } else if (fc == Long.class) {
            return TAG_LONG_OBJ;
        } else if (fc == Double.class) {
            return TAG_DOUBLE_OBJ;
        } else if (fc == Boolean.class) {
            return TAG_BOOLEAN_OBJ;
        } else if (List.class.isAssignableFrom(fc) && fc.isAssignableFrom(java.util.ArrayList.class)) {
            return TAG_LIST;
        } else if (fc == String[].class) {
            return TAG_STRING_ARRAY;
        } else if (fc == long[].class) {
            return TAG_LONG_ARRAY;
        }
        return TAG_GENERIC;
    }

    private static Class<?> resolveElementClass(Type fieldType, Class<?> fieldClass) {
        if (List.class.isAssignableFrom(fieldClass) && fieldType instanceof ParameterizedType pt) {
            Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?> c) {
                return c;
            }
        }
        return null;
    }

    private static long resolveFieldOffset(Field field, Method setter, String fieldName, Class<?> declaringClassUnused) {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return -1;
        }
        if (field != null) {
            return JDKUtils.objectFieldOffset(field);
        }
        // For setter-based fields, find the corresponding instance field
        if (setter != null) {
            Class<?> declaringClass = setter.getDeclaringClass();
            Class<?> current = declaringClass;
            while (current != null && current != Object.class) {
                try {
                    Field f = current.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    return JDKUtils.objectFieldOffset(f);
                } catch (NoSuchFieldException ignored) {
                }
                current = current.getSuperclass();
            }
        }
        return -1;
    }

    /**
     * Set the deserialized value onto the target bean instance.
     */
    public void setFieldValue(Object bean, Object value) {
        if (jsonSchema != null && value != null) {
            jsonSchema.assertValidate(value);
        }
        if (fieldOffset >= 0) {
            if (fieldClass == int.class) {
                JDKUtils.putInt(bean, fieldOffset, ((Number) value).intValue());
                return;
            } else if (fieldClass == long.class) {
                JDKUtils.putLongField(bean, fieldOffset, ((Number) value).longValue());
                return;
            } else if (fieldClass == boolean.class) {
                JDKUtils.putBoolean(bean, fieldOffset, (Boolean) value);
                return;
            } else if (fieldClass == double.class) {
                JDKUtils.putDouble(bean, fieldOffset, ((Number) value).doubleValue());
                return;
            } else if (fieldClass == float.class) {
                JDKUtils.putFloat(bean, fieldOffset, ((Number) value).floatValue());
                return;
            } else if (!fieldClass.isPrimitive()) {
                JDKUtils.putObject(bean, fieldOffset, value);
                return;
            }
            // short, byte primitives: fall through to reflection
        }
        try {
            if (setter != null) {
                setter.invoke(bean, value);
            } else {
                field.set(bean, value);
            }
        } catch (Exception e) {
            throw new JSONException("error setting field '" + fieldName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Set a reference-type value directly via Unsafe, skipping primitive type checks.
     */
    public void setObjectValue(Object bean, Object value) {
        if (jsonSchema != null && value != null) {
            jsonSchema.assertValidate(value);
        }
        if (fieldOffset >= 0) {
            JDKUtils.putObject(bean, fieldOffset, value);
            return;
        }
        setFieldValue(bean, value);
    }

    public void setIntValue(Object bean, int value) {
        if (jsonSchema != null) {
            jsonSchema.assertValidate((long) value);
        }
        if (fieldOffset >= 0) {
            JDKUtils.putInt(bean, fieldOffset, value);
            return;
        }
        setFieldValue(bean, value);
    }

    public void setLongValue(Object bean, long value) {
        if (jsonSchema != null) {
            jsonSchema.assertValidate(value);
        }
        if (fieldOffset >= 0) {
            JDKUtils.putLongField(bean, fieldOffset, value);
            return;
        }
        setFieldValue(bean, value);
    }

    public void setDoubleValue(Object bean, double value) {
        if (jsonSchema != null) {
            jsonSchema.assertValidate(value);
        }
        if (fieldOffset >= 0) {
            JDKUtils.putDouble(bean, fieldOffset, value);
            return;
        }
        setFieldValue(bean, value);
    }

    public void setBooleanValue(Object bean, boolean value) {
        if (jsonSchema != null) {
            jsonSchema.assertValidate((Object) value);
        }
        if (fieldOffset >= 0) {
            JDKUtils.putBoolean(bean, fieldOffset, value);
            return;
        }
        setFieldValue(bean, value);
    }

    /**
     * Convert a raw JSON-parsed value to the type expected by this field.
     */
    public Object convertValue(Object value) {
        if (value == null) {
            return null;
        }

        // Fast path: already the right type
        if (fieldClass.isInstance(value)) {
            return value;
        }

        // Numeric narrowing / widening / temporal conversion
        if (value instanceof Number number) {
            if (fieldClass == int.class || fieldClass == Integer.class) {
                return number.intValue();
            }
            if (fieldClass == long.class || fieldClass == Long.class) {
                return number.longValue();
            }
            if (fieldClass == double.class || fieldClass == Double.class) {
                return number.doubleValue();
            }
            if (fieldClass == float.class || fieldClass == Float.class) {
                return number.floatValue();
            }
            if (fieldClass == short.class || fieldClass == Short.class) {
                return number.shortValue();
            }
            if (fieldClass == byte.class || fieldClass == Byte.class) {
                return number.byteValue();
            }
            if (fieldClass == AtomicInteger.class) {
                return new AtomicInteger(number.intValue());
            }
            if (fieldClass == AtomicLong.class) {
                return new AtomicLong(number.longValue());
            }
            // Number → temporal type conversion (millis timestamp)
            long millis = number.longValue();
            if (fieldClass == Date.class) {
                return new Date(millis);
            }
            if (fieldClass == Instant.class) {
                return Instant.ofEpochMilli(millis);
            }
            if (fieldClass == LocalDateTime.class) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID);
            }
            if (fieldClass == LocalDate.class) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID).toLocalDate();
            }
            if (fieldClass == ZonedDateTime.class) {
                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID);
            }
            if (fieldClass == OffsetDateTime.class) {
                return OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID);
            }
        }

        // Boolean → AtomicBoolean conversion
        if (value instanceof Boolean b && fieldClass == AtomicBoolean.class) {
            return new AtomicBoolean(b);
        }

        // Any value → AtomicReference conversion
        if (fieldClass == AtomicReference.class) {
            return new AtomicReference<>(value);
        }

        // String → temporal type conversion
        // If format is specified, use DateTimeFormatter; otherwise use high-performance manual parsing
        if (value instanceof String str && !str.isEmpty()) {
            if (formatter != null) {
                return parseWithFormatter(str);
            }
            if (fieldClass == LocalDateTime.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseLocalDateTime(str);
            }
            if (fieldClass == LocalDate.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseLocalDate(str);
            }
            if (fieldClass == LocalTime.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseLocalTime(str);
            }
            if (fieldClass == Instant.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseInstant(str);
            }
            if (fieldClass == ZonedDateTime.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseZonedDateTime(str);
            }
            if (fieldClass == OffsetDateTime.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseOffsetDateTime(str);
            }
            if (fieldClass == Date.class) {
                return com.alibaba.fastjson3.util.DateUtils.parseDate(str);
            }
            // String → JDK types (builtin codecs)
            if (fieldClass == UUID.class) {
                return UUID.fromString(str);
            }
            if (fieldClass == Duration.class) {
                return Duration.parse(str);
            }
            if (fieldClass == Period.class) {
                return Period.parse(str);
            }
            if (fieldClass == YearMonth.class) {
                return YearMonth.parse(str);
            }
            if (fieldClass == MonthDay.class) {
                return MonthDay.parse(str);
            }
            if (fieldClass == URI.class) {
                return URI.create(str);
            }
            if (Path.class.isAssignableFrom(fieldClass)) {
                return Path.of(str);
            }
        }

        // Number → Year conversion
        if (value instanceof Number number && fieldClass == Year.class) {
            return Year.of(number.intValue());
        }

        // Wrap into Optional types
        if (fieldClass == Optional.class) {
            return Optional.ofNullable(value);
        }
        if (fieldClass == OptionalInt.class && value instanceof Number n) {
            return OptionalInt.of(n.intValue());
        }
        if (fieldClass == OptionalLong.class && value instanceof Number n) {
            return OptionalLong.of(n.longValue());
        }
        if (fieldClass == OptionalDouble.class && value instanceof Number n) {
            return OptionalDouble.of(n.doubleValue());
        }

        // Map → POJO/Record conversion (for nested objects parsed via readAny())
        if (value instanceof java.util.Map<?, ?> map && !fieldClass.isInterface()
                && !java.util.Map.class.isAssignableFrom(fieldClass)) {
            String json = com.alibaba.fastjson3.JSON.toJSONString(value);
            return com.alibaba.fastjson3.JSON.parseObject(json, fieldClass);
        }

        // No conversion found; return as-is
        return value;
    }

    private Object parseWithFormatter(String str) {
        if (fieldClass == LocalDateTime.class) {
            return LocalDateTime.parse(str, formatter);
        }
        if (fieldClass == LocalDate.class) {
            return LocalDate.parse(str, formatter);
        }
        if (fieldClass == LocalTime.class) {
            return LocalTime.parse(str, formatter);
        }
        if (fieldClass == ZonedDateTime.class) {
            return ZonedDateTime.parse(str, formatter);
        }
        if (fieldClass == OffsetDateTime.class) {
            return OffsetDateTime.parse(str, formatter);
        }
        if (fieldClass == Date.class) {
            var zdt = ZonedDateTime.parse(str, formatter.withZone(
                    com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID));
            return Date.from(zdt.toInstant());
        }
        if (fieldClass == Instant.class) {
            return Instant.from(formatter.withZone(
                    com.alibaba.fastjson3.util.DateUtils.DEFAULT_ZONE_ID).parse(str));
        }
        // Unsupported type with format — fall through to default parsing
        return str;
    }

    public int compareTo(FieldReader other) {
        int cmp = Integer.compare(this.ordinal, other.ordinal);
        if (cmp != 0) {
            return cmp;
        }
        return this.fieldName.compareTo(other.fieldName);
    }

    @Override
    public String toString() {
        return "FieldReader{" + fieldName + ", type=" + fieldClass.getSimpleName() + "}";
    }
}
