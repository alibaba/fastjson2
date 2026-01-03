package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.ToLongFunction;

final class FieldWriterMillis<T>
        extends FieldWriterDate<T> {
    FieldWriterMillis(String fieldName,
            int ordinal,
            long features,
            String dateTimeFormat,
            String label,
            Field field,
            Method method,
            ToLongFunction function
    ) {
        super(fieldName, ordinal, features, dateTimeFormat, label, long.class, long.class, field, method, function);
    }

    @Override
    public Object getFieldValue(T object) {
        return propertyAccessor.getLongValue(object);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        long millis = propertyAccessor.getLongValue(object);
        if (millis == 0 && !"iso8601".equals(this.format)) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) != 0) {
                writeFieldName(jsonWriter);
                jsonWriter.writeNull();
                return true;
            } else {
                return false;
            }
        }

        writeDate(jsonWriter, millis);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        writeDate(jsonWriter, false,
                propertyAccessor.getLongValue(object));
    }
}
