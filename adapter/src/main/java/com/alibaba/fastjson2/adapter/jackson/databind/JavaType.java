package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.adapter.jackson.core.type.ResolvedType;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class JavaType
        extends ResolvedType
        implements java.io.Serializable, // 2.1
        java.lang.reflect.Type // 2.2
{
    protected final Type type;
    protected final Class clazz;

    public JavaType(Type type) {
        this.type = type;
        this.clazz = TypeUtils.getClass(type);
    }

    @Override
    public final Class<?> getRawClass() {
        return clazz;
    }

    @Override
    public boolean isContainerType() {
        return Map.class.isAssignableFrom(clazz)
                || Collection.class.isAssignableFrom(clazz);
    }

    public Type getType() {
        return type;
    }
}
