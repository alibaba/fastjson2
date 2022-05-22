package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

final class FieldReaderObjectFieldUF
        extends FieldReaderObjectField {
    final long fieldOffset;

    FieldReaderObjectFieldUF(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Object defaultValue, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, defaultValue, schema, field);

        fieldOffset = UnsafeUtils.objectFieldOffset(field);
    }

    @Override
    public void accept(Object object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        UNSAFE.putObject(object, fieldOffset, value);
    }
}
