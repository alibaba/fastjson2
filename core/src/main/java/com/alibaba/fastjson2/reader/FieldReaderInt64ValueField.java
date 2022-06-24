package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderInt64ValueField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt64ValueField(String fieldName, Class fieldType, int ordinal, long features, String format, Long defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        long fieldLong = jsonReader.readInt64Value();

        if (schema != null) {
            schema.assertValidate(fieldLong);
        }

        try {
            field.setLong(object, fieldLong);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
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
        long intValue = TypeUtils.toLongValue(value);

        if (schema != null) {
            schema.assertValidate(intValue);
        }

        try {
            field.setLong(object, intValue);
        } catch (Exception e) {
            throw new JSONException("set " + getFieldName() + " error", e);
        }
    }
}
