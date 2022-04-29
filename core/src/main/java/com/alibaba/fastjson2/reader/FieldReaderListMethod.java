package com.alibaba.fastjson2.reader;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class FieldReaderListMethod<T> extends FieldReaderObjectMethod<T>
        implements FieldReaderList<T, Object> {
    final Type itemType;

    FieldReaderListMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        if (fieldType instanceof ParameterizedType) {
            itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
        } else {
            itemType = Object.class;
        }
    }

    FieldReaderListMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, Type itemType, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, null, method);
        this.itemType = itemType;
    }

    @Override
    public Type getItemType() {
        return itemType;
    }
}
