package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Field;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

class FieldReaderInt32ValueField<T>
        extends FieldReaderObjectField<T> {
    final long fieldOffset;
    FieldReaderInt32ValueField(String fieldName, Class fieldType, int ordinal, String format, Integer defaultValue, Field field) {
        super(fieldName, fieldType, fieldType, ordinal, 0, format, defaultValue, field);
        fieldOffset = JDKUtils.UNSAFE.objectFieldOffset(field);
    }

    @Override
    public void readFieldValue(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();
        UNSAFE.putInt(object, fieldOffset, fieldInt);
    }

    @Override
    public void readFieldValueJSONB(JSONReader jsonReader, T object) {
        int fieldInt = jsonReader.readInt32Value();
        accept(object, fieldInt);
    }

    @Override
    public void accept(T object, float value) {
        accept(object, Integer.valueOf((int) value));
    }

    @Override
    public void accept(T object, double value) {
        accept(object, Integer.valueOf((int) value));
    }

    @Override
    public void accept(T object, Object value) {
        int intValue = TypeUtils.toIntValue(value);
        UNSAFE.putInt(object, fieldOffset, intValue);
    }

    @Override
    public void accept(T object, long value) {
        int intValue = (int) value;
        UNSAFE.putInt(object, fieldOffset, intValue);
    }

    @Override
    public Object readFieldValue(JSONReader jsonReader) {
        return jsonReader.readInt32Value();
    }
}
