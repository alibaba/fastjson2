package com.alibaba.fastjson2.reader;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

class FieldReaderListStrField<T>
        extends FieldReaderObjectField<T>
        implements FieldReaderList<T, Object> {

    FieldReaderListStrField(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Field field) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, field);
    }

    @Override
    public Type getItemType() {
        return String.class;
    }
}
