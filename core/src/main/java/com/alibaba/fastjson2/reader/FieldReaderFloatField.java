package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderFloatField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderFloatField(String fieldName, Class fieldType, int ordinal, long features, String format, Float defaultValue, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Float fieldValue = jsonReader.readFloat();
        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readFloat();
    }

    @Override
    public void accept(T object, Object value) {
        Float floatValue = TypeUtils.toFloat(value);
        try {
            field.set(object, floatValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
