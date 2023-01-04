package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderInt16Field<T>
        extends FieldReaderObjectField<T> {
    FieldReaderInt16Field(String fieldName, Class fieldType, int ordinal, long features, String format, Short defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, schema, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        int intValue = jsonReader.readInt32Value();
        Short fieldValue;
        if (jsonReader.wasNull()) {
            fieldValue = null;
        } else {
            fieldValue = (short) intValue;
        }

        if (schema != null) {
            schema.assertValidate(fieldValue);
        }

        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Short.valueOf((short) value));
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Short.valueOf((short) value));
    }

    @Override
    public void accept(T object, int value) {
        accept(object, Short.valueOf((short) value));
    }

    @Override
    public void accept(T object, long value) {
        accept(object, Short.valueOf((short) value));
    }

    @Override
    public void accept(T object, Object value) {
        Short shortValue = TypeUtils.toShort(value);

        if (schema != null) {
            schema.assertValidate(shortValue);
        }

        try {
            field.set(object, shortValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return (short) jsonReader.readInt32Value();
    }
}
