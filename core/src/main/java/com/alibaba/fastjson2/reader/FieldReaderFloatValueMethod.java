package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Locale;

final class FieldReaderFloatValueMethod<T>
        extends FieldReaderObject<T> {
    FieldReaderFloatValueMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Locale locale, Float defaultValue, JSONSchema schema, Method setter) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, locale, defaultValue, schema, setter, null, null);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        float fieldValue = jsonReader.readFloatValue();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setFloat(object, fieldValue);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        float fieldValue = jsonReader.readFloatValue();

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        propertyAccessor.setFloat(object, fieldValue);
    }

    @Override
    public void accept(T object, Object value) {
        float floatValue = TypeUtils.toFloatValue(value);

        if (schema != null) {
            schema.assertValidate(floatValue);
        }

        propertyAccessor.setFloat(object, floatValue);
    }

    @Override
    public void accept(T object, int value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        propertyAccessor.setFloat(object, value);
    }
}
