package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

class FieldReaderAtomicIntegerMethodReadOnly<T>
        extends FieldReader<T> {
    FieldReaderAtomicIntegerMethodReadOnly(
            String fieldName,
            Class fieldType,
            int ordinal,
            Method method
    ) {
        super(fieldName, fieldType, fieldType, ordinal, 0, null, null, null, method, null);
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
            int intValue = ((Number) value).intValue();
            atomic.set(intValue);
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
    public Object readFieldValue(JSONReader jsonReader) {
        int intValue = jsonReader.readInt32Value();
        if (jsonReader.wasNull()) {
            return null;
        }

        return new AtomicInteger(intValue);
    }
}
