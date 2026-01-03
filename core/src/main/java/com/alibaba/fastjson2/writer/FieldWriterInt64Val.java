package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.ToLongFunction;

final class FieldWriterInt64Val
        extends FieldWriterInt64 {
    FieldWriterInt64Val(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            ToLongFunction function
    ) {
        super(name, ordinal, features, format, label, long.class, field, method, function);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        long value;
        try {
            value = propertyAccessor.getLongValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue) && defaultValue == null) {
            return false;
        }

        writeInt64(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        jsonWriter.writeInt64(
                propertyAccessor.getLongValue(object)
        );
    }
}
