package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicIntegerArray;

final class FieldReaderAtomicIntegerArrayReadOnly<T> extends FieldReaderImpl<T> {
    final Method method;

    FieldReaderAtomicIntegerArrayReadOnly(String fieldName, Class fieldType, int ordinal, Method method) {
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
    public void readFieldValue(JSONReader jsonReader, T object) {
        if (jsonReader.readIfNull()) {
            return;
        }

        AtomicIntegerArray atomic;
        try {
            atomic = (AtomicIntegerArray) method.invoke(object);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }

        if (jsonReader.nextIfMatch('[')) {
            for (int i = 0; ; ++i) {
                if (jsonReader.nextIfMatch(']')) {
                    break;
                }

                int value = jsonReader.readInt32Value();
                if (atomic != null && i < atomic.length()) {
                    atomic.set(i, value);
                }
            }
        }
    }

    @Override
    public String toString() {
        return method.getName();
    }
}
