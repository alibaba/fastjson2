package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Field;
import java.math.BigInteger;

final class FieldWriterBigIntField<T>
        extends FieldWriter<T> {
    protected FieldWriterBigIntField(String name, int ordinal, long features, String format, String label, Field field) {
        super(name, ordinal, features, format, label, BigInteger.class, BigInteger.class, field, null);
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
        BigInteger value = (BigInteger) getFieldValue(object);
        if (value == null) {
            long features = this.features | jsonWriter.getFeatures();
            if ((features & JSONWriter.Feature.WriteNulls.mask) == 0) {
                return false;
            }
        }

        writeFieldName(jsonWriter);
        jsonWriter.writeBigInt(value, features);
        return true;
    }

    @Override
    public void writeValue(JSONWriter jsonWriter, T object) {
        BigInteger value = (BigInteger) getFieldValue(object);
        jsonWriter.writeBigInt(value, features);
    }
}
