package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

final class FieldReaderAtomicLongReadOnly<T>
        extends FieldReader<T> {
    FieldReaderAtomicLongReadOnly(String fieldName, Class fieldType, int ordinal, JSONSchema schema, Method method) {
        super(fieldName, fieldType, fieldType, ordinal, 0, null, null, null, schema, method, null);
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
            long longValue = ((Number) value).longValue();
            atomic.set(longValue);
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
}
