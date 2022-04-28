package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

final class FieldReaderAtomicBooleanMethodReadOnly<T>
        extends FieldReaderImpl<T> implements FieldReaderReadOnly<T> {

    final Method method;

    FieldReaderAtomicBooleanMethodReadOnly(String fieldName, Class fieldClass, int ordinal, Method method) {
        super(fieldName, fieldClass, fieldClass, ordinal, 0, null);
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
            AtomicBoolean atomic = (AtomicBoolean) method.invoke(object);
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

    @Override
    public String toString() {
        return method.getName();
    }
}
