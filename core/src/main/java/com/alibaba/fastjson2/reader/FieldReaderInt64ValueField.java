package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

class FieldReaderInt64ValueField<T>
        extends FieldReaderObjectField<T> {
    final long fieldOffset;
    FieldReaderInt64ValueField(String fieldName, Class fieldType, int ordinal, long features, String format, Long defaultValue, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, features, format, defaultValue, field);
        fieldOffset = JDKUtils.UNSAFE.objectFieldOffset(field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        long fieldLong = jsonReader.readInt64Value();
        UNSAFE.putLong(object, fieldOffset, fieldLong);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        readFieldValue(jsonReader, object);
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Long.valueOf((long) value));
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Long.valueOf((long) value));
    }

    @Override
    public void accept(T object, Object value) {
        long longValue = TypeUtils.toLongValue(value);
        UNSAFE.putLong(object, fieldOffset, longValue);
    }
}
