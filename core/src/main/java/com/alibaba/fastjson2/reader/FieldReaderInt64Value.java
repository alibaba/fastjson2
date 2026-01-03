package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Locale;
import java.util.function.ObjLongConsumer;

final class FieldReaderInt64Value<T>
        extends FieldReaderObject<T> {
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
        super(fieldName, long.class, long.class, ordinal, features, format, locale, defaultValue, schema, method, field,
                function, paramName, parameter, null);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt64Value();
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        long fieldValue = jsonReader.readInt64Value();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setLongValue(object, fieldValue);
    }

    @Override
    public void accept(T object, Object value) {
        long longValue = TypeUtils.toLongValue(value);

        if (schema != null) {
            schema.assertValidate(longValue);
        }

        propertyAccessor.setLongValue(object, longValue);
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setLongValue(object, value);
    }
}
