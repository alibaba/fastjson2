package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.schema.JSONSchema;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

class FieldReaderListStrField<T>
        extends FieldReaderObjectField<T>
        implements FieldReaderList<T, Object> {
    FieldReaderListStrField(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, JSONSchema schema, Field field) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, schema, field);
    }

    @Override
    public Type getItemType() {
        return String.class;
    }
}
