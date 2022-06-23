package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.schema.JSONSchema;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

final class FieldReaderListMethod<T>
        extends FieldReaderObjectMethod<T>
        implements FieldReaderList<T, Object> {
    final Type itemType;
    final Class itemClass;
    final long itemClassHash;

    FieldReaderListMethod(String fieldName, Type fieldType, Class fieldClass, int ordinal, long features, String format, JSONSchema schema, Type itemType, Method method) {
        super(fieldName, fieldType, fieldClass, ordinal, features, format, null, null, schema, method);
        if (itemType == null) {
            if (fieldType instanceof ParameterizedType) {
                itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
            } else {
                itemType = Object.class;
            }
        }
        this.itemType = itemType;
        this.itemClass = TypeUtils.getClass(itemType);
        this.itemClassHash = this.itemClass == null ? 0 : Fnv.hashCode64(itemClass.getName());
    }

    @Override
    public Type getItemType() {
        return itemType;
    }

    @Override
    public Class getItemClass() {
        return itemClass;
    }

    @Override
    public long getItemClassHash() {
        return itemClassHash;
    }
}
