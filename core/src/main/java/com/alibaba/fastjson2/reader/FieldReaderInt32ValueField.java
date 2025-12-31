package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

class FieldReaderInt32ValueField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt32ValueField(String fieldName, Class fieldType, int ordinal, String format, Integer defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, 0, format, null, defaultValue, schema, field);
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
        accept(object, fieldInt);
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Integer.valueOf((int) value));
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Integer.valueOf((int) value));
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

        propertyAccessor.setLong(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32Value();
    }
}
