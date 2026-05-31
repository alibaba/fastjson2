package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjCharConsumer;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderCharValue<T>
        extends FieldReader<T> {
    FieldReaderCharValue(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Character defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjCharConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, char.class, char.class, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);
    }

    @Override
    public void accept(T object, char value) {
        propertyAccessor.setCharValue(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        propertyAccessor.setObject(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        char ch = jsonReader.readCharValue();
        if (ch == '\0' && jsonReader.wasNull()) {
            return;
        }
        propertyAccessor.setCharValue(object, ch);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        String str = jsonReader.readString();
        return str == null || str.isEmpty() ? '\0' : str.charAt(0);
    }
}
