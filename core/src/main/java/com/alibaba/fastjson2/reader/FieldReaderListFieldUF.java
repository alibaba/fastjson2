package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

final class FieldReaderListFieldUF<T> extends FieldReaderListField<T> {
    final long fieldOffset;

    FieldReaderListFieldUF(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            Type itemType,
            int ordinal,
            long features,
            String format,
            Field field) {
        super(fieldName, fieldType, fieldClass, itemType, ordinal, features, format, field);
        fieldOffset = UnsafeUtils.objectFieldOffset(field);
    }

    @Override
    public void accept(Object object, Object value) {
        UNSAFE.putObject(object, fieldOffset, value);
    }
}
