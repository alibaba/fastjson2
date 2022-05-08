package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

final class FieldReaderAtomicBooleanFieldReadOnly<T>
        extends FieldReaderImpl<T> implements FieldReaderReadOnly<T> {

    final Field field;

    FieldReaderAtomicBooleanFieldReadOnly(String fieldName, Class fieldClass, int ordinal, String format, AtomicBoolean defaultValue, Field field) {
        super(fieldName, fieldClass, fieldClass, ordinal, 0, format, defaultValue);
        this.field = field;
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public void accept(T object, Object value) {
        if (value == null) {
            return;
        }

        try {
            AtomicBoolean atomic = (AtomicBoolean) field.get(object);
            atomic.set((Boolean) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void readFieldValue(JSONReader reader, T object) {
        Boolean value = reader.readBool();
        accept(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader reader) {
        return reader.readBool();
    }

    @Override
    public String toString() {
        return field.getName();
    }
}
