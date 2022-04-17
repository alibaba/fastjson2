package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

final class FieldReaderAtomicBooleanMethodReadOnly<T>
        extends FieldReaderImpl<T> implements FieldReaderReadOnly<T> {

    final Method method;

    FieldReaderAtomicBooleanMethodReadOnly(String fieldName, Class fieldClass, int ordinal, Method method) {
        super(fieldName, fieldClass, fieldClass, ordinal, 0, null);
        this.method = method;
    }

    public boolean isReadOnly() {
        return true;
    }

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

    public void readFieldValue(JSONReader jsonReader, T object) {
        Boolean value = jsonReader.readBool();
        accept(object, value);
    }

    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readBool();
    }

    public String toString() {
        return method.getName();
    }
}
