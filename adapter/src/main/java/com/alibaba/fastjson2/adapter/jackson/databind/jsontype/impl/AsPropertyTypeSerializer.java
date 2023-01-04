package com.alibaba.fastjson2.adapter.jackson.databind.jsontype.impl;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.adapter.jackson.databind.BeanProperty;
import com.alibaba.fastjson2.adapter.jackson.databind.jsontype.TypeIdResolver;

public class AsPropertyTypeSerializer
        extends AsArrayTypeSerializer {
    public AsPropertyTypeSerializer(TypeIdResolver idRes, BeanProperty property, String propName) {
        throw new JSONException("TODO");
    }
}
