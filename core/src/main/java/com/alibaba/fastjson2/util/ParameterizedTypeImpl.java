package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

@JSONType(deserializeFeatures = JSONReader.Feature.SupportAutoType, typeName = "java.lang.reflect.ParameterizedType")
public class ParameterizedTypeImpl
        implements ParameterizedType {
    private final Type[] actualTypeArguments;
    private final Type ownerType;
    private final Type rawType;

    @JSONCreator
    public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = ownerType;
        this.rawType = rawType;
    }

    public ParameterizedTypeImpl(Type rawType, Type... actualTypeArguments) {
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
        this.ownerType = null;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ParameterizedType)) {
            return false;
        }

        ParameterizedType that = (ParameterizedType) o;

        if (!Arrays.equals(actualTypeArguments, that.getActualTypeArguments())) {
            return false;
        }
        if (!Objects.equals(ownerType, that.getOwnerType())) {
            return false;
        }
        return Objects.equals(rawType, that.getRawType());
    }

    @Override
    public int hashCode() {
        // Match the reflection implementation so equal types share cache entries.
        // <details><summary>中文</summary>与反射实现保持一致，使相等类型共享缓存项。</details>
        return Arrays.hashCode(actualTypeArguments)
                ^ Objects.hashCode(ownerType)
                ^ Objects.hashCode(rawType);
    }
}
