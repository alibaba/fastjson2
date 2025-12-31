package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

final class FieldReaderDoubleValueMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderDoubleValueMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Locale locale, Double defaultValue, JSONSchema schema, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        double fieldValue = jsonReader.readDoubleValue();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setDouble(object, fieldValue);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        double fieldValue = jsonReader.readDoubleValue();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setDouble(object, fieldValue);
    }

    @Override
    public void accept(T object, Object value) {
        double doubleValue = TypeUtils.toDoubleValue(value);

        if (schema != null) {
            schema.assertValidate(doubleValue);
        }

        propertyAccessor.setDouble(object, doubleValue);
    }

    @Override
    public void accept(T object, int value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setDouble(object, value);
    }
}
