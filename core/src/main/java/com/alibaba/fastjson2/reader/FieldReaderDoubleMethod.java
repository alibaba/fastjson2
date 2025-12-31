package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;

final class FieldReaderDoubleMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderDoubleMethod(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Double defaultValue,
            JSONSchema schema,
            Method setter
    ) {
        super(fieldName, Double.class, Double.class, ordinal, features, format, null, defaultValue, schema, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Double fieldValue = jsonReader.readDouble();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        if (fieldValue == null && defaultValue != null) {
            return;
        }

        propertyAccessor.setObject(object, fieldValue);
    }

    @Override
    public void accept(T object, Object value) {
        Double doubleValue = TypeUtils.toDouble(value);

        if (schema != null) {
            schema.assertValidate(doubleValue);
        }

        propertyAccessor.setObject(object, doubleValue);
    }
}
