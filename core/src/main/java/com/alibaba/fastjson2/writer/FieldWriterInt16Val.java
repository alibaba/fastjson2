package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.function.ToShortFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class FieldWriterInt16Val
        extends FieldWriterInt16 {
    FieldWriterInt16Val(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method,
            ToShortFunction function
    ) {
        super(name, ordinal, features, format, label, short.class, field, method, function);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, Object object) {
        short value;
        try {
            value = propertyAccessor.getShortValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == 0 && jsonWriter.isEnabled(JSONWriter.Feature.NotWriteDefaultValue) && defaultValue == null) {
            return false;
        }

        writeInt16(jsonWriter, value);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, Object object) {
        jsonWriter.writeInt32(
                propertyAccessor.getShortValue(object)
        );
    }
}
