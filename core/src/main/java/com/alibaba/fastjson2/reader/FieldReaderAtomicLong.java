package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

final class FieldReaderAtomicLong<T>
        extends FieldReader<T> {
    FieldReaderAtomicLong(
            String fieldName,
            Class fieldType,
            int ordinal,
            JSONSchema schema,
            Method method,
            Field field
    ) {
        super(fieldName, fieldType, fieldType, ordinal, 0, null, null, null, schema, method, field);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void accept(T object, Object value) {
        if (readOnly) {
            if (value != null) {
                AtomicLong atomic = (AtomicLong) propertyAccessor.getObject(object);
                long longValue = ((Number) value).longValue();
                atomic.set(longValue);
            }
        } else {
            if ((!(value instanceof AtomicLong)) && value instanceof Number) {
                value = new AtomicLong(((Number) value).longValue());
            }
            propertyAccessor.setObject(object, value);
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
