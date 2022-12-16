package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.JSONException;

public class JsonFactoryBuilder {
    public JsonFactoryBuilder disable(JsonFactory.Feature f) {
        return this;
    }

    public JsonFactory build() {
        // TODO build
        throw new JSONException("TODO");
    }
}
