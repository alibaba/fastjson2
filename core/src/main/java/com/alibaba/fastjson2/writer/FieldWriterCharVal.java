package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToCharFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class FieldWriterCharVal
        extends FieldWriterChar {
    FieldWriterCharVal(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            ToCharFunction function
    ) {
        super(name, ordinal, features, format, label, char.class, char.class, field, method, function);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        char value;
        try {
            value = propertyAccessor.getChar(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        long features = jsonWriter.getFeatures(this.features);
        if (value == '\0' && (features & JSONWriter.Feature.NotWriteDefaultValue.mask) != 0 && defaultValue == null) {
            return false;
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeChar(value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        char value = propertyAccessor.getChar(object);
        jsonWriter.writeChar(value);
    }
}
