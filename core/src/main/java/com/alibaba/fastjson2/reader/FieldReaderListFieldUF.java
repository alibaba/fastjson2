package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.UnsafeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Locale;

import static com.alibaba.fastjson2.util.UnsafeUtils.UNSAFE;

final class FieldReaderListFieldUF<T>
        extends FieldReaderListField<T> {
    final long fieldOffset;

    FieldReaderListFieldUF(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            Type itemType,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Collection defaultValue,
            JSONSchema schema,
            Field field) {
        super(fieldName, fieldType, fieldClass, itemType, ordinal, features, format, locale, defaultValue, schema, field);
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
