package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

class FieldReaderAtomicInteger<T>
        extends FieldReader<T> {
    FieldReaderAtomicInteger(
            String fieldName,
            Class fieldType,
            int ordinal,
            JSONSchema jsonSchema,
            Method method,
            Field field
    ) {
        super(fieldName, fieldType, fieldType, ordinal, 0, null, null, null, jsonSchema, method, field);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public void accept(T object, Object value) {
        if (readOnly) {
            if (value != null) {
                AtomicInteger atomic = (AtomicInteger) propertyAccessor.getObject(object);
                int intValue = TypeUtils.toIntValue(value);
                atomic.set(intValue);
            }
        } else {
            if ((!(value instanceof AtomicInteger)) && value instanceof Number) {
                value = new AtomicInteger(((Number) value).intValue());
            }
            propertyAccessor.setObject(object, value);
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
