package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

/**
 * FieldWriterInt32 is an abstract base class for writing integer (int32) fields to JSON format.
 * It handles serialization of both primitive int and boxed Integer field types.
 *
 * <p>This class provides support for:
 * <ul>
 *   <li>Integer field serialization as JSON numbers</li>
 *   <li>Integer to string conversion when WriteNonStringValueAsString is enabled</li>
 *   <li>Custom format support for integer fields</li>
 *   <li>Null handling for boxed Integer fields</li>
 *   <li>Error handling for field access failures</li>
 * </ul>
 *
 * <p>Concrete implementations of this class handle different access methods:
 * <ul>
 *   <li>Direct field access via reflection</li>
 *   <li>Method-based access (getters)</li>
 *   <li>Function-based access</li>
 * </ul>
 *
 * @param <T> the type of the object containing the integer field
 * @since 2.0.0
 */
abstract class FieldWriterInt32<T>
        extends FieldWriter<T> {
    final boolean toString;

    protected FieldWriterInt32(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, null, label, fieldType, fieldClass, field, method);
        toString = (features & WriteNonStringValueAsString.mask) != 0
                || "string".equals(format);
    }

    @Override
    public final void writeInt32(JSONWriter jsonWriter, int value) {
        if (toString) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(Integer.toString(value));
            return;
        }
        writeFieldName(jsonWriter);
        if (format != null) {
            jsonWriter.writeInt32(value, format);
        } else {
            jsonWriter.writeInt32(value);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Integer value;
        try {
            value = (Integer) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeIntNull(jsonWriter);
        }

        writeInt32(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Integer value = (Integer) getFieldValue(object);

        if (value == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        jsonWriter.writeInt32(value);
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (valueClass == this.fieldClass) {
            return ObjectWriterImplInt32.INSTANCE;
        }

        return jsonWriter.getObjectWriter(valueClass);
    }
}
