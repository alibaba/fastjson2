package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;

final class FieldReaderListFunc<T, V>
        extends FieldReaderImpl<T>
        implements FieldReaderList<T, Object> {

    final Method method;
    final BiConsumer<T, V> function;
    final Type itemType;
    private ObjectReader itemObjectReader;

    FieldReaderListFunc(String fieldName, Type fieldType, Class<V> fieldClass, int ordinal, Method method, BiConsumer<T, V> function) {
        super(fieldName, fieldType, fieldClass, ordinal, 0, null);
        this.method = method;
        this.function = function;
        if (fieldType instanceof ParameterizedType) {
            itemType = ((ParameterizedType) fieldType).getActualTypeArguments()[0];
        } else {
            itemType = null;
        }
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public ObjectReader getItemObjectReader(JSONReader.Context ctx) {
        if (itemObjectReader != null) {
            return itemObjectReader;
        }
        return itemObjectReader = ctx.getObjectReader(itemType);
    }

    @Override
    public void accept(T object, Object value) {
        function.accept(object, (V) value);
    }

    @Override
    public Type getItemType() {
        return itemType;
    }
}
