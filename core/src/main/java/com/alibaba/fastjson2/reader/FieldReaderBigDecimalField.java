package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;

final class FieldReaderBigDecimalField<T>
        extends FieldReaderObjectField<T> {
    FieldReaderBigDecimalField(
            String fieldName,
            Class fieldType,
            int ordinal,
            long features,
            String format,
            BigDecimal defaultValue,
            Field field
    ) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        BigDecimal fieldValue = jsonReader.readBigDecimal();

        try {
            field.set(object, fieldValue);
        } catch (Exception e) {
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
        }
    }

    @Override
    public void accept(T object, int value) {
        try {
            field.set(object, BigDecimal.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, long value) {
        try {
            field.set(object, BigDecimal.valueOf(value));
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void accept(T object, Object value) {
        BigDecimal decimalValue = TypeUtils.toBigDecimal(value);

        try {
            field.set(object, decimalValue);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
