package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

abstract class FieldWriterInt16<T>
        extends FieldWriter<T> {
    FieldWriterInt16(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Class fieldClass,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, null, label, fieldClass, fieldClass, field, method);
    }

    protected final void writeInt16(JSONWriter jsonWriter, short value) {
        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue) && defaultValue == null) {
            return;
        }
        boolean writeNonStringValueAsString = (jsonWriter.getFeatures(features) & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
        if (writeNonStringValueAsString) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(Short.toString(value));
            return;
        }
        writeFieldName(jsonWriter);
        jsonWriter.writeInt16(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Short value;
        try {
            value = (Short) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeIntNull(jsonWriter);
        }

        writeInt16(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Short value = (Short) getFieldValue(object);
        if (value == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt32(value);
    }

    @Override
    public ObjectWriter getObjectWriter(JSONWriter jsonWriter, Class valueClass) {
        if (valueClass == fieldClass) {
            return ObjectWriterImplInt16.INSTANCE;
        }
        return jsonWriter.getObjectWriter(valueClass);
    }
}
