package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

final class FieldWriterObjectFieldUF
        extends FieldWriterObjectField {
    final long fieldOffset;

    protected FieldWriterObjectFieldUF(
            String name,
            int ordinal,
            long features,
            String format,
            String label,
            Type fieldType,
            Class fieldClass,
            Field field
    ) {
        super(name, ordinal, features, format, label, fieldType, fieldClass, field);

        fieldOffset = UnsafeUtils.objectFieldOffset(field);
    }

    @Override
    public Object getFieldValue(Object object) {
        return UNSAFE.getObject(object, fieldOffset);
    }
}
