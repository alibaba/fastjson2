package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.math.BigDecimal;

final class FieldWriterBigDecimalField<T>
        extends FieldWriter<T> {
    protected FieldWriterBigDecimalField(String name, int ordinal, long features, String format, String label, Field field) {
        super(name, ordinal, features, format, label, BigDecimal.class, BigDecimal.class, field, null);
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return field.get(object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new JSONException("field.get error, " + fieldName, e);
        }
    }

    @Override
    public boolean write(JSONWriter jsonWriter, T object) {
        BigDecimal value = (BigDecimal) getFieldValue(object);
        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) == 0) {
                return false;
            }
        }

        writeFieldName(jsonWriter);
        if (features != 0) {
            jsonWriter.writeDecimal(value, features);
        } else {
            jsonWriter.writeDecimal(value);
        }

        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        BigDecimal value = (BigDecimal) getFieldValue(object);
        jsonWriter.writeDecimal(value);
    }
}
