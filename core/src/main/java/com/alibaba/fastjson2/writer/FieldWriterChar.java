package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

class FieldWriterChar
        extends FieldWriter {
    FieldWriterChar(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Object function
    ) {
        super(name, ordinal, features, format, null, label, fieldType, fieldClass, field, method, function);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        Character value = (Character) getFieldValue(object);
        if (value == null) {
            jsonWriter.writeNull();
            return;
        }
        jsonWriter.writeChar(value);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        Character value;
        try {
            value = (Character) getFieldValue(object);
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
            jsonWriter.writeNull();
            return true;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeChar(value);
        return true;
    }
}
