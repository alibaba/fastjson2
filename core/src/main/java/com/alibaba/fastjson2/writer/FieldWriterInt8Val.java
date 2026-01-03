package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToByteFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class FieldWriterInt8Val
        extends FieldWriterInt8 {
    FieldWriterInt8Val(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            ToByteFunction function
    ) {
        super(name, ordinal, features, format, label, byte.class, field, method, function);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        byte value;
        try {
            value = propertyAccessor.getByteValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue) && defaultValue == null) {
            return false;
        }

        writeInt8(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        byte value = propertyAccessor.getByteValue(object);
        jsonWriter.writeInt32(value);
    }
}
