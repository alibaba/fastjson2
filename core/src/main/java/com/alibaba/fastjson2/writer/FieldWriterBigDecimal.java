package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.JSONWriterJSONB;
import com.alibaba.fastjson2.JSONWriterUTF16;
import com.alibaba.fastjson2.JSONWriterUTF8;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.function.Function;

import static com.alibaba.fastjson2.JSONWriter.MASK_IGNORE_ERROR_GETTER;

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
    public boolean writeJSONB(JSONWriterJSONB jsonWriter, T object) {
        BigDecimal value;
        try {
            value = (BigDecimal) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeFloatNull(jsonWriter);
        }

        writeFieldNameJSONB(jsonWriter);
        jsonWriter.writeDecimal(value, features, decimalFormat);
        return true;
    }

    @Override
    public boolean writeUTF8(JSONWriterUTF8 jsonWriter, T object) {
        long features = jsonWriter.getFeatures(this.features);
        BigDecimal value;
        try {
            value = (BigDecimal) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if ((features & MASK_IGNORE_ERROR_GETTER) != 0) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeFloatNull(jsonWriter);
        }

        jsonWriter.writeNameRaw(fieldNameUTF8(features));
        jsonWriter.writeDecimal(value, features, decimalFormat);
        return true;
    }

    @Override
    public boolean writeUTF16(JSONWriterUTF16 jsonWriter, T object) {
        BigDecimal value;
        try {
            value = (BigDecimal) propertyAccessor.getObject(object);
        } catch (RuntimeException error) {
            if (jsonWriter.isIgnoreErrorGetter()) {
                return false;
            }
            throw error;
        }

        if (value == null) {
            return writeFloatNull(jsonWriter);
        }

        writeFieldNameUTF16(jsonWriter);
        jsonWriter.writeDecimal(value, features, decimalFormat);
        return true;
    }
}
