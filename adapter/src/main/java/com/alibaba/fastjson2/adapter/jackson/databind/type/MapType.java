package com.alibaba.fastjson2.adapter.jackson.databind.type;

import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;

import java.lang.reflect.Type;

public class MapType
        extends JavaType {
    public MapType(Type rawType, Type keyType, Type valueType) {
        super(new ParameterizedTypeImpl(rawType, keyType, valueType));
    }
}
