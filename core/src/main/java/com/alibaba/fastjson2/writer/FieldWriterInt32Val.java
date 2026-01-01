package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.ToIntFunction;

class FieldWriterInt32Val
        extends FieldWriterInt32 {
    FieldWriterInt32Val(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            ToIntFunction function
    ) {
        super(name, ordinal, features, format, label, int.class, int.class, field, method, function);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        int value;
        try {
            value = propertyAccessor.getInt(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue) && defaultValue == null) {
            return false;
        }

        writeInt32(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        jsonWriter.writeInt32(
                propertyAccessor.getInt(object)
        );
    }
}
