package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;

final class FieldWriterBigDecimalMethod<T>
        extends FieldWriter<T> {
    protected FieldWriterBigDecimalMethod(
            String fieldName,
            int ordinal,
            long features,
            String format,
            String label,
            Method method
    ) {
        super(fieldName, ordinal, features, format, label, BigDecimal.class, BigDecimal.class, null, method);
    }

    @Override
    public Object getFieldValue(T object) {
        try {
            return method.invoke(object);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new JSONException("invoke getter method error, " + fieldName, e);
        }
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
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) == 0) {
                return false;
            }
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeDecimal(value, features, decimalFormat);
        return true;
    }
}
