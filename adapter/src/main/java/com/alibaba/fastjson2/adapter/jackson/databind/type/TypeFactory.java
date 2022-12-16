package com.alibaba.fastjson2.adapter.jackson.databind.type;

import com.alibaba.fastjson2.adapter.jackson.core.type.TypeReference;
import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

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

    public TypeFactory withClassLoader(ClassLoader classLoader) {
        return this;
    }

    public CollectionType constructCollectionType(
            Class<? extends Collection> collectionClass,
            Class<?> elementClass
    ) {
        return new CollectionType(collectionClass, elementClass);
    }

    public MapType constructMapType(Class<? extends Map> mapClass,
                                    Class<?> keyClass, Class<?> valueClass) {
        return new MapType(mapClass, keyClass, valueClass);
    }
}
