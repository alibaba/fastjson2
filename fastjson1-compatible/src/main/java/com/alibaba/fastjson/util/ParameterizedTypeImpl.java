package com.alibaba.fastjson.util;

import java.lang.reflect.Type;

public class ParameterizedTypeImpl
        extends com.alibaba.fastjson2.util.ParameterizedTypeImpl {
    public ParameterizedTypeImpl(Type[] actualTypeArguments, Type ownerType, Type rawType) {
        super(actualTypeArguments, ownerType, rawType);
    }
}
