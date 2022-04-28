package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicReference;

final class FieldReaderAtomicReferenceMethodReadOnly<T>
        extends FieldReaderAtomicReference<T> implements FieldReaderReadOnly<T> {

    final Method method;

    FieldReaderAtomicReferenceMethodReadOnly(String fieldName, Type fieldType, Class fieldClass, int ordinal, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, 0, null);
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
            AtomicReference atomic = (AtomicReference) method.invoke(object);
            atomic.set(value);
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
    }
}
