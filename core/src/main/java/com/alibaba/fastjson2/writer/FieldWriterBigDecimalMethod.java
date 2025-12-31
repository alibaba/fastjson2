package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;

final class FieldWriterBigDecimalMethod<T>
        extends FieldWriter<T> {
    FieldWriterBigDecimalMethod(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Field field,
            Method method
    ) {
        super(fieldName, ordinal, features, format, null, label, BigDecimal.class, BigDecimal.class, field, method);
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        BigDecimal value = (BigDecimal) getFieldValue(object);
        jsonWriter.writeDecimal(value, features, decimalFormat);
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        BigDecimal value;
        try {
            value = (BigDecimal) getFieldValue(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeFloatNull(jsonWriter);
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeDecimal(value, features, decimalFormat);
        return true;
    }
}
