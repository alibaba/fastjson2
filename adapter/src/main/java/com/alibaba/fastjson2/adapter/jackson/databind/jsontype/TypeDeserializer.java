package com.alibaba.fastjson2.adapter.jackson.databind.jsontype;

import com.alibaba.fastjson2.adapter.jackson.core.JsonParser;
import com.alibaba.fastjson2.adapter.jackson.databind.DeserializationContext;

import java.io.IOException;

public abstract class TypeDeserializer {
    public abstract Object deserializeTypedFromAny(JsonParser p, DeserializationContext ctxt) throws IOException;
}
