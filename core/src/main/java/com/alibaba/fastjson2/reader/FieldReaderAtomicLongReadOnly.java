package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

final class FieldReaderAtomicLongReadOnly<T> extends FieldReaderImpl<T> implements FieldReaderReadOnly<T> {
    protected final Method method;

    FieldReaderAtomicLongReadOnly(String fieldName, Class fieldType, int ordinal, Method method) {
        super(fieldName, fieldType, fieldType, ordinal, 0, null);
        this.method = method;
    }

    @Override
    public Method getMethod() {
        return method;
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
            AtomicLong atomic = (AtomicLong) method.invoke(object);
            atomic.set((Long) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Long value = jsonReader.readInt64();
        accept(object, value);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        long longValue = jsonReader.readInt64Value();
        if (jsonReader.wasNull()) {
            return null;
        }

        return new AtomicLong(longValue);
    }

    @Override
    public String toString() {
        return method.getName();
    }
}
