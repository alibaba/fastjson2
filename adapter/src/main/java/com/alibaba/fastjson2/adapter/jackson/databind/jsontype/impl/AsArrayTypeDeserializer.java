package com.alibaba.fastjson2.adapter.jackson.databind.jsontype.impl;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import com.alibaba.fastjson2.adapter.jackson.databind.DeserializationContext;
import com.alibaba.fastjson2.adapter.jackson.databind.jsontype.TypeDeserializer;

import java.io.IOException;

public class AsArrayTypeDeserializer
        extends TypeDeserializer {
    @Override
    public Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException {
        throw new JSONException("TODO");
    }
}
