package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

final class FieldReaderBoolValueField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderBoolValueField(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Boolean defaultValue,
            Field field
    ) {
        super(fieldName, boolean.class, boolean.class, ordinal, features, format, defaultValue, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        boolean fieldValue = jsonReader.readBoolValue();
        try {
            field.setBoolean(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, int value) {
        accept(object, TypeUtils.toBooleanValue(value));
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            if ((features & JSONReader.Feature.IgnoreSetNullValue.mask) != 0) {
                return;
            }

            accept(object, false);
            return;
        }

        if (value instanceof Boolean) {
            accept(object, ((Boolean) value).booleanValue());
            return;
        }

        throw new JSONException("set " + fieldName + " error, type not support " + value.getClass());
    }

    @Override
    public void accept(T object, boolean value) {
        if (fieldOffset != -1) {
            JDKUtils.UNSAFE.putBoolean(object, fieldOffset, value);
            return;
        }

        try {
            field.setBoolean(object, value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }
}
