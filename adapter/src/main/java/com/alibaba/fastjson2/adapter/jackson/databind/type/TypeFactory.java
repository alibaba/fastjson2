package com.alibaba.fastjson2.adapter.jackson.databind.type;

import com.alibaba.fastjson2.adapter.jackson.core.type.TypeReference;
import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;

import java.lang.reflect.Type;

public class TypeFactory
        implements java.io.Serializable {
    protected static final TypeFactory instance = new TypeFactory();

    public static TypeFactory defaultInstance() {
        return instance;
    }

    public JavaType constructType(Type type) {
        return new JavaType(type);
    }

    public JavaType constructType(TypeReference<?> typeRef) {
        return constructType(typeRef.getType());
    }

    public void clearCache() {
    }

    public JavaType constructParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
        ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(parametrized, parameterClasses);
        return new JavaType(parameterizedType);
    }
}
