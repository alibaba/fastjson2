package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.ObjLongConsumer;

final class FieldReaderInt64Value<T>
        extends FieldReader<T> {
    public FieldReaderInt64Value(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Long defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjLongConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, long.class, long.class, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);
    }

    @Override
    public void accept(T object, long value) {
        propertyAccessor.setLongValue(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        propertyAccessor.setObject(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        propertyAccessor.setLongValue(object,
                jsonReader.readInt64Value());
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64Value();
    }
}
