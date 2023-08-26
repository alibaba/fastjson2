package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Locale;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

final class FieldReaderListField<T>
        extends FieldReaderList<T, Object> {
    FieldReaderListField(
            String fieldName,
            Type fieldType,
            Class fieldClass,
            Type itemType,
            Class itemClass,
            int ordinal,
            long features,
            String format,
            Locale locale,
            Collection defaultValue,
            JSONSchema schema,
            Field field) {
        super(
                fieldName,
                fieldType,
                fieldClass,
                itemType,
                itemClass,
                ordinal,
                features,
                format,
                locale,
                defaultValue,
                schema,
                null,
                field,
                null
        );
    }

    @Override
    public void accept(Object object, Object value) {
        if (schema != null) {
            schema.assertValidate(value);
        }

        UNSAFE.putObject(object, fieldOffset, value);
    }
}
