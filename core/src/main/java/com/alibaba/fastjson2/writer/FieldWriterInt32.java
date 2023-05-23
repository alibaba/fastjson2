package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

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
        super(name, ordinal, features, format, label, fieldType, fieldClass, field, method);
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
        jsonWriter.writeInt32(value);
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
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask | JSONWriter.Feature.WriteNullNumberAsZero.mask)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            jsonWriter.writeNumberNull();
            return true;
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
