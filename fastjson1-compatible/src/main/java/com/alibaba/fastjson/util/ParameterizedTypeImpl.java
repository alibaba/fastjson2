package com.alibaba.fastjson.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ParameterizedTypeImpl
        extends com.alibaba.fastjson2.util.ParameterizedTypeImpl {
    public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
        super(actualTypeArguments, ownerType, rawType);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }
}
