package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

class FieldReaderAtomicIntegerMethodReadOnly<T> extends FieldReaderImpl<T> implements FieldReaderReadOnly<T> {
    final Method method;

    FieldReaderAtomicIntegerMethodReadOnly(String fieldName, Class fieldType, int ordinal, Method method) {
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
            AtomicInteger atomic = (AtomicInteger) method.invoke(object);
            atomic.set((Integer) value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        Integer value = jsonReader.readInt32();
        accept(object, value);
    }

    @Override
    public String toString() {
        return method.getName();
    }
}
