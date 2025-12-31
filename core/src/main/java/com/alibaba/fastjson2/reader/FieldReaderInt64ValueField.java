package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

class FieldReaderInt64ValueField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt64ValueField(String fieldName, Class fieldType, int ordinal, long features, String format, Long defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, null, defaultValue, schema, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        long fieldLong = jsonReader.readInt64Value();

        if (schema != null) {
            schema.assertValidate(fieldLong);
        }

        propertyAccessor.setLong(object, fieldLong);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        readFieldValue(jsonReader, object);
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Long.valueOf((long) value));
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Long.valueOf((long) value));
    }

    @Override
    public void accept(T object, Object value) {
        long longValue = TypeUtils.toLongValue(value);

        if (schema != null) {
            schema.assertValidate(longValue);
        }

        propertyAccessor.setLong(object, longValue);
    }
}
