package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.adapter.jackson.core.JsonGenerator;

import java.io.IOException;

public abstract class SerializerProvider {
    public final void defaultSerializeField(String fieldName, Object value, JsonGenerator gen)
            throws IOException {
        // TODO defaultSerializeField
        throw new JSONException("TODO");
    }
}
