package com.alibaba.fastjson2.adapter.jackson.databind.jsontype.impl;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.adapter.jackson.annotation.JsonTypeInfo;
import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import com.alibaba.fastjson2.adapter.jackson.databind.jsontype.TypeIdResolver;

public class AsPropertyTypeDeserializer
        extends AsArrayTypeDeserializer {
    public AsPropertyTypeDeserializer(
            JavaType bt,
            TypeIdResolver idRes,
            String typePropertyName,
            boolean typeIdVisible,
            JavaType defaultImpl
    ) {
        this(bt, idRes, typePropertyName, typeIdVisible, defaultImpl, JsonTypeInfo.As.PROPERTY);
    }

    public AsPropertyTypeDeserializer(
            JavaType bt,
            TypeIdResolver idRes,
            String typePropertyName,
            boolean typeIdVisible,
            JavaType defaultImpl,
            JsonTypeInfo.As inclusion
    ) {
        throw new JSONException("TODO");
    }
}
