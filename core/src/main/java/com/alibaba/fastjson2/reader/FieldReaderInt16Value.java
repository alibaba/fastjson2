package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.function.ObjShortConsumer;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;

final class FieldReaderInt16Value<T>
        extends FieldReaderObject<T> {
    public FieldReaderInt16Value(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Short defaultValue,
            JSONSchema schema,
            Method method,
            Field field,
            ObjShortConsumer<T> function,
            String paramName,
            Parameter parameter
    ) {
        super(fieldName, short.class, short.class, ordinal, features, format, locale, defaultValue, schema, method, field,
                function, paramName, parameter, null);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return (short) jsonReader.readInt32Value();
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        short fieldValue = (short) jsonReader.readInt32Value();
        propertyAccessor.setShortValue(object, fieldValue);
    }

    @Override
    public void accept(T object, Object value) {
        short shortValue = TypeUtils.toShortValue(value);
        propertyAccessor.setShortValue(object, shortValue);
    }
}
