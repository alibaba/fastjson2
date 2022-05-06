package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class FieldReaderListMethod<T> extends FieldReaderObjectMethod<T>
        implements FieldReaderList<T, Object> {
    final Type itemType;
    final Class itemClass;

    FieldReaderListMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, method);
        if (fieldType instanceof ParameterizedType) {
            itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
        } else {
            itemType = Object.class;
        }
        this.itemClass = TypeUtils.getClass(itemType);
    }

    FieldReaderListMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, Type itemType, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, null, method);
        this.itemType = itemType;
        this.itemClass = TypeUtils.getClass(itemType);
    }

    @Override
    public Type getItemType() {
        return itemType;
    }
}
