package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

abstract class FieldWriterInt8<T>
        extends FieldWriter<T> {
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
    }

    protected final void writeInt8(JSONWriter jsonWriter, byte value) {
        boolean writeNonStringValueAsString = (jsonWriter.getFeatures() & JSONWriter.Feature.WriteNonStringValueAsString.mask) != 0;
        if (writeNonStringValueAsString) {
            writeFieldName(jsonWriter);
            jsonWriter.writeString(Byte.toString(value));
            return;
        }
        writeFieldName(jsonWriter);
        jsonWriter.writeInt8(value);
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
            if ((features & JSONWriter.Feature.WriteNulls.mask) == 0) {
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
        jsonWriter.writeInt32(value);
    }
}
