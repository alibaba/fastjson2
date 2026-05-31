package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.ObjIntConsumer;

final class FieldReaderInt32Value<T>
        extends FieldReader<T> {
    public FieldReaderInt32Value(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Integer defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjIntConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, int.class, int.class, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);
    }

    @Override
    public void accept(T object, int value) {
        propertyAccessor.setIntValue(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        propertyAccessor.setObject(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        propertyAccessor.setIntValue(object,
                jsonReader.readInt32Value());
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32Value();
    }
}
