package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class FieldReaderInt32ValueMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderInt32ValueMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Integer defaultValue, JSONSchema schema, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, defaultValue, schema, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();

        if (schema != null) {
            schema.assertValidate(fieldInt);
        }

        propertyAccessor.setInt(object, fieldInt);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();

        if (schema != null) {
            schema.assertValidate(fieldInt);
        }

        propertyAccessor.setInt(object, fieldInt);
    }

    @Override
    public void accept(T object, Object value) {
        int intValue = TypeUtils.toIntValue(value);

        if (schema != null) {
            schema.assertValidate(intValue);
        }

        propertyAccessor.setInt(object, intValue);
    }

    @Override
    public void accept(T object, long value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setInt(object, (int) value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32Value();
    }
}
