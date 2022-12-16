package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.databind.type.TypeFactory;

import java.lang.reflect.Type;

public abstract class DatabindContext {
    public abstract Object getAttribute(Object key);

    public abstract TypeFactory getTypeFactory();

    public abstract JavaType constructSpecializedType(JavaType baseType, Class<?> subclass);

    public JavaType constructType(Type type) {
        if (type == null) {
            return null;
        }
        return getTypeFactory().constructType(type);
    }
}
