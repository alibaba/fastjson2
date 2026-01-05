package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.function.Function;

final class FieldWriterUUID<T>
        extends FieldWriterObjectFinal<T> {
    FieldWriterUUID(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field,
            Method method,
            Function function
    ) {
        super(name, ordinal, features, format, null, label, fieldType, fieldClass, field, method, function);
    }

    @Override
    public Object getFieldValue(Object object) {
        return propertyAccessor.getObject(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        UUID uuid = (UUID) propertyAccessor.getObject(object);
        if (uuid == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }
        writeFieldName(jsonWriter);
        jsonWriter.writeUUID(uuid);
        return true;
    }

    @Override
    public Function getFunction() {
        return function;
    }
}
