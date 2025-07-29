package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

abstract class FieldWriterInt8<T>
        extends FieldWriter<T> {
    final boolean writeNonStringValueAsString;
    FieldWriterInt8(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Class fieldClass,
            Field field,
            Method method
    ) {
        super(name, ordinal, features, format, label, fieldClass, fieldClass, field, method);
        writeNonStringValueAsString = (features & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
    }

    protected final void writeInt8(JSONWriter jsonWriter, byte value) {
        writeFieldName(jsonWriter);
        if (writeNonStringValueAsString) {
            jsonWriter.writeString(value);
        } else {
            jsonWriter.writeInt8(value);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        Byte value;
        try {
            value = (Byte) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & (JSONWriter.Feature.WriteNulls.mask | JSONWriter.Feature.NullAsDefaultValue.mask)) == 0) {
                return false;
            }
            writeFieldName(jsonWriter);
            jsonWriter.writeNumberNull();
            return true;
        }

        writeInt8(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        Byte value = (Byte) getFieldValue(object);
        if (value == null) {
            jsonWriter.writeNumberNull();
            return;
        }
        jsonWriter.writeInt32(value.byteValue());
    }
}
