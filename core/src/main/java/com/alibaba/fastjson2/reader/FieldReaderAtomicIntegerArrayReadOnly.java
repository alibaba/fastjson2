package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

final class FieldReaderAtomicIntegerArrayReadOnly<T>
        extends FieldReader<T> {
    FieldReaderAtomicIntegerArrayReadOnly(String fieldName, Class fieldType, int ordinal, JSONSchema jsonSchema, Method method) {
        super(fieldName, fieldType, fieldType, ordinal, 0, null, null, null, jsonSchema, method, null);
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
            AtomicIntegerArray atomic = (AtomicIntegerArray) method.invoke(object);
            if (value instanceof AtomicIntegerArray) {
                AtomicIntegerArray array = (AtomicIntegerArray) value;
                for (int i = 0; i < array.length(); i++) {
                    atomic.set(i, array.get(i));
                }
            } else {
                List values = (List) value;
                for (int i = 0; i < values.size(); i++) {
                    int itemValue = TypeUtils.toIntValue(values.get(i));
                    atomic.set(i, itemValue);
                }
            }
        } catch (Exception e) {
            throw new JSONException("set " + fieldName + " error", e);
        }
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
            throw new JSONException(jsonReader.info("set " + fieldName + " error"), e);
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
    public Object readFieldValue(JSONReader jsonReader) {
        if (jsonReader.nextIfNull()) {
            return null;
        }
        return jsonReader.readArray(Integer.class);
    }
}
