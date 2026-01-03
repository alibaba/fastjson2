package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Function;

final class FieldWriterBigDecimal<T>
        extends FieldWriter<T> {
    FieldWriterBigDecimal(
            String fieldName,
            int ordinal,
            long features,
            String format,
            Locale locale,
            String label,
            Field field,
            Method method,
            Function function
    ) {
        super(fieldName, ordinal, features, format, locale, label, BigDecimal.class, BigDecimal.class, field, method, function);
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
            value = propertyAccessor.getBigDecimal(object);
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
