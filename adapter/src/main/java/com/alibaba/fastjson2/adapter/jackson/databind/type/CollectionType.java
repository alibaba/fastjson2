package com.alibaba.fastjson2.adapter.jackson.databind.type;

import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import com.alibaba.fastjson2.util.ParameterizedTypeImpl;

import java.lang.reflect.Type;

public class CollectionType
        extends JavaType {
    public CollectionType(Class rawType, Type itemType) {
        super(new ParameterizedTypeImpl(rawType, itemType));
    }
}
