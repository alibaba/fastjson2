package com.alibaba.fastjson3.writer;

import com.alibaba.fastjson3.JSONException;
import com.alibaba.fastjson3.JSONGenerator;
import com.alibaba.fastjson3.ObjectMapper;
import com.alibaba.fastjson3.ObjectWriter;
import com.alibaba.fastjson3.WriteFeature;
import com.alibaba.fastjson3.filter.NameFilter;
import com.alibaba.fastjson3.filter.PropertyFilter;
import com.alibaba.fastjson3.filter.ValueFilter;
import com.alibaba.fastjson3.util.JDKUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Represents a single field to be written during JSON serialization.
 * Pre-encodes the field name token ({@code "fieldName":}) as both char[] and byte[]
 * for direct bulk copy during writing.
 *
 * <p>Uses a type tag + switch dispatch instead of polymorphic subclasses to keep
 * the call site monomorphic (avoids megamorphic vtable dispatch in the hot loop).</p>
 */
public final class FieldWriter implements Comparable<FieldWriter> {
    // Type tags
    static final int TYPE_GENERIC = 0;
    static final int TYPE_STRING = 1;
    static final int TYPE_INT = 2;
    static final int TYPE_LONG = 3;
    static final int TYPE_DOUBLE = 4;
    static final int TYPE_FLOAT = 5;
    static final int TYPE_BOOL = 6;
    static final int TYPE_OBJECT = 7;
    static final int TYPE_LIST_STRING = 8;
    static final int TYPE_LIST_OBJECT = 9;

    final String fieldName;
    final int ordinal;
    final Type fieldType;
    final Class<?> fieldClass;
    final Method getter;
    final Field field;
    final long fieldOffset; // Unsafe field offset, -1 if unavailable
    final int typeTag;

    // Pre-encoded field name token: "fieldName":
    final char[] nameChars;
    final byte[] nameBytes;
    // Pre-encoded as long[] for bulk Unsafe.putLong writes (8 bytes at a time)
    final long[] nameByteLongs;
    final int nameBytesLen; // actual byte count (not padded)

    // For TYPE_OBJECT / TYPE_LIST_OBJECT: cached ObjectWriter + class guard
    private volatile ObjectWriter<Object> cachedWriter;
    private volatile Class<?> cachedWriterClass;

    // For TYPE_LIST_STRING / TYPE_LIST_OBJECT: element class
    final Class<?> elementClass;

    // Private constructor — use factory methods
    private FieldWriter(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass,
            Method getter, Field field, int typeTag, Class<?> elementClass
    ) {
        this.fieldName = fieldName;
        this.ordinal = ordinal;
        this.fieldType = fieldType;
        this.fieldClass = fieldClass;
        this.getter = getter;
        this.field = field;
        this.fieldOffset = (field != null && JDKUtils.UNSAFE_AVAILABLE)
                ? JDKUtils.objectFieldOffset(field) : -1;
        this.typeTag = typeTag;
        this.elementClass = elementClass;
        this.nameChars = encodeNameChars(fieldName);
        this.nameBytes = encodeNameBytes(fieldName);
        this.nameBytesLen = nameBytes.length;
        this.nameByteLongs = encodeByteLongs(nameBytes);
    }

    // ==================== Factory methods ====================

    public static FieldWriter ofGetter(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass, Method getter
    ) {
        return new FieldWriter(fieldName, ordinal, fieldType, fieldClass, getter, null,
                typeTagFor(fieldClass), null);
    }

    public static FieldWriter ofField(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass, Field field
    ) {
        return new FieldWriter(fieldName, ordinal, fieldType, fieldClass, null, field,
                typeTagFor(fieldClass), null);
    }

    public static FieldWriter ofList(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass,
            Class<?> elementClass, Method getter
    ) {
        int tag = elementClass == String.class ? TYPE_LIST_STRING : TYPE_LIST_OBJECT;
        return new FieldWriter(fieldName, ordinal, fieldType, fieldClass, getter, null,
                tag, elementClass);
    }

    public static FieldWriter ofList(
            String fieldName, int ordinal, Type fieldType, Class<?> fieldClass,
            Class<?> elementClass, Field field
    ) {
        int tag = elementClass == String.class ? TYPE_LIST_STRING : TYPE_LIST_OBJECT;
        return new FieldWriter(fieldName, ordinal, fieldType, fieldClass, null, field,
                tag, elementClass);
    }

    private static int typeTagFor(Class<?> fieldClass) {
        if (fieldClass == String.class) {
            return TYPE_STRING;
        }
        if (fieldClass == int.class || fieldClass == Integer.class) {
            return TYPE_INT;
        }
        if (fieldClass == long.class || fieldClass == Long.class) {
            return TYPE_LONG;
        }
        if (fieldClass == double.class || fieldClass == Double.class) {
            return TYPE_DOUBLE;
        }
        if (fieldClass == float.class || fieldClass == Float.class) {
            return TYPE_FLOAT;
        }
        if (fieldClass == boolean.class || fieldClass == Boolean.class) {
            return TYPE_BOOL;
        }
        if (!fieldClass.isPrimitive() && !fieldClass.isArray()
                && fieldClass != Object.class
                && !java.util.Collection.class.isAssignableFrom(fieldClass)
                && !java.util.Map.class.isAssignableFrom(fieldClass)) {
            return TYPE_OBJECT;
        }
        return TYPE_GENERIC;
    }

    // ==================== Name encoding ====================

    static char[] encodeNameChars(String name) {
        int len = name.length();
        char[] chars = new char[len + 3];
        chars[0] = '"';
        name.getChars(0, len, chars, 1);
        chars[len + 1] = '"';
        chars[len + 2] = ':';
        return chars;
    }

    static byte[] encodeNameBytes(String name) {
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[nameBytes.length + 3];
        result[0] = '"';
        System.arraycopy(nameBytes, 0, result, 1, nameBytes.length);
        result[nameBytes.length + 1] = '"';
        result[nameBytes.length + 2] = ':';
        return result;
    }

    /**
     * Encode byte[] as long[] for bulk Unsafe.putLong writes.
     * Pads to 8-byte boundary so the last long can safely overwrite trailing bytes.
     */
    public static long[] encodeByteLongs(byte[] bytes) {
        if (!JDKUtils.UNSAFE_AVAILABLE) {
            return null;
        }
        int len = bytes.length;
        int longCount = (len + 7) >>> 3; // ceil(len / 8)
        byte[] padded = new byte[longCount << 3]; // pad to 8-byte boundary
        System.arraycopy(bytes, 0, padded, 0, len);
        long[] result = new long[longCount];
        for (int i = 0; i < longCount; i++) {
            result[i] = JDKUtils.getLongDirect(padded, i << 3);
        }
        return result;
    }

    // ==================== Accessors ====================

    public String getFieldName() {
        return fieldName;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public Type getFieldType() {
        return fieldType;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }

    // ==================== Field value reading ====================

    private Object getFieldValue(Object bean) {
        try {
            if (getter != null) {
                return getter.invoke(bean);
            } else {
                return field.get(bean);
            }
        } catch (InvocationTargetException e) {
            throw new JSONException("Error reading field '" + fieldName + "'", e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new JSONException("Error reading field '" + fieldName + "'", e);
        }
    }

    private Object getObjectValue(Object bean) {
        if (fieldOffset >= 0) {
            if (fieldClass == int.class) {
                return JDKUtils.getInt(bean, fieldOffset);
            } else if (fieldClass == long.class) {
                return JDKUtils.getLongField(bean, fieldOffset);
            } else if (fieldClass == boolean.class) {
                return JDKUtils.getBoolean(bean, fieldOffset);
            } else if (fieldClass == double.class) {
                return JDKUtils.getDouble(bean, fieldOffset);
            } else if (fieldClass == float.class) {
                return JDKUtils.getFloat(bean, fieldOffset);
            } else {
                return JDKUtils.getObject(bean, fieldOffset);
            }
        }
        return getFieldValue(bean);
    }

    private void writeNull(JSONGenerator generator, long features) {
        if ((features & WriteFeature.WriteNulls.mask) != 0) {
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
            generator.writeNull();
        }
    }

    // ==================== Main dispatch ====================

    /**
     * Write this field (name + value) to the generator.
     * Single monomorphic method with switch dispatch on type tag.
     */
    public void writeField(JSONGenerator generator, Object bean, long features) {
        switch (typeTag) {
            case TYPE_STRING -> writeString(generator, bean, features);
            case TYPE_INT -> writeInt(generator, bean, features);
            case TYPE_LONG -> writeLong(generator, bean, features);
            case TYPE_DOUBLE -> writeDouble(generator, bean, features);
            case TYPE_FLOAT -> writeFloat(generator, bean, features);
            case TYPE_BOOL -> writeBool(generator, bean, features);
            case TYPE_OBJECT -> writeObject(generator, bean, features);
            case TYPE_LIST_STRING -> writeListString(generator, bean, features);
            case TYPE_LIST_OBJECT -> writeListObject(generator, bean, features);
            default -> writeGeneric(generator, bean, features);
        }
    }

    // ==================== Type-specific write methods ====================

    private void writeString(JSONGenerator generator, Object bean, long features) {
        String value = (String) getObjectValue(bean);
        if (value == null) {
            writeNull(generator, features);
            return;
        }
        generator.writeNameString(nameByteLongs, nameBytesLen, nameBytes, nameChars, value);
    }

    private void writeInt(JSONGenerator generator, Object bean, long features) {
        if (fieldOffset >= 0 && fieldClass == int.class) {
            generator.writeNameInt32(nameByteLongs, nameBytesLen, nameBytes, nameChars,
                    JDKUtils.getInt(bean, fieldOffset));
        } else {
            Object value = getObjectValue(bean);
            if (value == null) {
                writeNull(generator, features);
                return;
            }
            generator.writeNameInt32(nameByteLongs, nameBytesLen, nameBytes, nameChars, (Integer) value);
        }
    }

    private void writeLong(JSONGenerator generator, Object bean, long features) {
        if (fieldOffset >= 0 && fieldClass == long.class) {
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
            long v = JDKUtils.getLongField(bean, fieldOffset);
            generator.writeInt64(v);
        } else {
            Object value = getObjectValue(bean);
            if (value == null) {
                writeNull(generator, features);
                return;
            }
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
            generator.writeInt64((Long) value);
        }
    }

    private void writeDouble(JSONGenerator generator, Object bean, long features) {
        if (fieldOffset >= 0 && fieldClass == double.class) {
            generator.writeNameDouble(nameByteLongs, nameBytesLen, nameBytes, nameChars,
                    JDKUtils.getDouble(bean, fieldOffset));
        } else {
            Object value = getObjectValue(bean);
            if (value == null) {
                writeNull(generator, features);
                return;
            }
            generator.writeNameDouble(nameByteLongs, nameBytesLen, nameBytes, nameChars, (Double) value);
        }
    }

    private void writeFloat(JSONGenerator generator, Object bean, long features) {
        if (fieldOffset >= 0 && fieldClass == float.class) {
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
            generator.writeFloat(JDKUtils.getFloat(bean, fieldOffset));
        } else {
            Object value = getObjectValue(bean);
            if (value == null) {
                writeNull(generator, features);
                return;
            }
            generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
            generator.writeFloat((Float) value);
        }
    }

    private void writeBool(JSONGenerator generator, Object bean, long features) {
        if (fieldOffset >= 0 && fieldClass == boolean.class) {
            generator.writeNameBool(nameByteLongs, nameBytesLen, nameBytes, nameChars,
                    JDKUtils.getBoolean(bean, fieldOffset));
        } else {
            Object value = getObjectValue(bean);
            if (value == null) {
                writeNull(generator, features);
                return;
            }
            generator.writeNameBool(nameByteLongs, nameBytesLen, nameBytes, nameChars, (Boolean) value);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeObject(JSONGenerator generator, Object bean, long features) {
        Object value = getObjectValue(bean);
        if (value == null) {
            writeNull(generator, features);
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);

        Class<?> valueClass = value.getClass();
        ObjectWriter<Object> writer = cachedWriter;
        if (writer == null || cachedWriterClass != valueClass) {
            writer = (ObjectWriter<Object>) ObjectMapper.shared().getObjectWriter(valueClass);
            if (writer != null) {
                cachedWriter = writer;
                cachedWriterClass = valueClass;
            }
        }
        if (writer != null) {
            writer.write(generator, value, fieldName, fieldType, features);
        } else {
            generator.writeAny(value);
        }
    }

    @SuppressWarnings("unchecked")
    private void writeListString(JSONGenerator generator, Object bean, long features) {
        java.util.List<?> list = (java.util.List<?>) getObjectValue(bean);
        if (list == null) {
            writeNull(generator, features);
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        generator.startArray();
        for (int i = 0, size = list.size(); i < size; i++) {
            String s = (String) list.get(i);
            if (s == null) {
                generator.writeNull();
            } else {
                generator.writeString(s);
            }
        }
        generator.endArray();
    }

    @SuppressWarnings("unchecked")
    private void writeListObject(JSONGenerator generator, Object bean, long features) {
        java.util.List<?> list = (java.util.List<?>) getObjectValue(bean);
        if (list == null) {
            writeNull(generator, features);
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        generator.startArray();

        ObjectWriter<Object> elemWriter = cachedWriter;
        if (elemWriter == null && elementClass != null) {
            elemWriter = (ObjectWriter<Object>) ObjectMapper.shared().getObjectWriter(elementClass);
            if (elemWriter != null) {
                cachedWriter = elemWriter;
            }
        }
        for (int i = 0, size = list.size(); i < size; i++) {
            Object item = list.get(i);
            if (item == null) {
                generator.writeNull();
            } else if (elemWriter != null) {
                elemWriter.write(generator, item, null, null, features);
            } else {
                generator.writeAny(item);
            }
        }
        generator.endArray();
    }

    private void writeGeneric(JSONGenerator generator, Object bean, long features) {
        Object value = getObjectValue(bean);
        if (value == null) {
            writeNull(generator, features);
            return;
        }
        generator.writePreEncodedNameLongs(nameByteLongs, nameBytesLen, nameChars, nameBytes);
        if (value instanceof String s) {
            generator.writeString(s);
        } else if (value instanceof Integer i) {
            generator.writeInt32(i);
        } else if (value instanceof Long l) {
            generator.writeInt64(l);
        } else if (value instanceof Boolean b) {
            generator.writeBool(b);
        } else if (value instanceof Double d) {
            generator.writeDouble(d);
        } else if (value instanceof Float f) {
            generator.writeFloat(f);
        } else if (value instanceof BigDecimal bd) {
            generator.writeDecimal(bd);
        } else {
            generator.writeAny(value);
        }
    }

    // ==================== Filter-aware write ====================

    /**
     * Write this field with filter support. Only called when filters are configured.
     * Zero overhead when not used — callers check filter array length before calling.
     */
    public void writeFieldFiltered(JSONGenerator generator, Object bean, long features,
                                   PropertyFilter[] propertyFilters,
                                   ValueFilter[] valueFilters,
                                   NameFilter[] nameFilters) {
        Object value = getObjectValue(bean);

        // PropertyFilter: check if field should be included
        for (PropertyFilter pf : propertyFilters) {
            if (!pf.apply(bean, fieldName, value)) {
                return;
            }
        }

        // ValueFilter: transform value
        for (ValueFilter vf : valueFilters) {
            value = vf.apply(bean, fieldName, value);
        }

        // NameFilter: transform name
        String name = fieldName;
        for (NameFilter nf : nameFilters) {
            name = nf.apply(bean, name, value);
        }

        // Write name + value (use generic path since name/value may be transformed)
        if (value == null) {
            if ((features & WriteFeature.WriteNulls.mask) != 0) {
                generator.writeName(name);
                generator.writeNull();
            }
            return;
        }
        generator.writeName(name);
        generator.writeAny(value);
    }

    // ==================== Comparable ====================

    @Override
    public int compareTo(FieldWriter other) {
        int cmp = Integer.compare(this.ordinal, other.ordinal);
        if (cmp != 0) {
            return cmp;
        }
        return this.fieldName.compareTo(other.fieldName);
    }

    @Override
    public String toString() {
        return "FieldWriter{name='" + fieldName + "', ordinal=" + ordinal
                + ", type=" + fieldClass.getSimpleName() + "}";
    }
}
