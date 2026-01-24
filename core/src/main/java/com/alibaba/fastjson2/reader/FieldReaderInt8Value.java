package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjByteConsumer;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderInt8Value<T>
        extends FieldReader<T> {
    public FieldReaderInt8Value(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Byte defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjByteConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, byte.class, byte.class, ordinal, features, format, locale, defaultValue, schema, method, field, function, paramName, parameter);
    }

    @Override
    public void accept(T object, byte value) {
        propertyAccessor.setByteValue(object, value);
    }

    @Override
    public void accept(T object, Object value) {
        propertyAccessor.setObject(object, value);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        propertyAccessor.setByteValue(object,
                (byte) jsonReader.readInt32Value());
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return (byte) jsonReader.readInt32Value();
    }
}
