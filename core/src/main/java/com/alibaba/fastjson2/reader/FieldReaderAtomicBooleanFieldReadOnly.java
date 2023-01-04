package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

final class FieldReaderAtomicBooleanFieldReadOnly<T>
        extends FieldReader<T> {
    FieldReaderAtomicBooleanFieldReadOnly(String fieldName, Class fieldClass, int ordinal, String format, AtomicBoolean defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldClass, fieldClass, ordinal, 0, format, null, defaultValue, schema, null, field);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            return;
        }

        try {
            AtomicBoolean atomic = (AtomicBoolean) field.get(object);
            if (value instanceof AtomicBoolean) {
                value = ((AtomicBoolean) value).get();
            }
            atomic.set((Boolean) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Boolean value = jsonReader.readBool();
        accept(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }
}
