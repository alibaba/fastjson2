package com.alibaba.fastjson2.adapter.jackson.core;

import com.alibaba.fastjson2.JSONPath;

public class JsonPointer {
    final JSONPath path;

    JsonPointer(JSONPath path) {
        this.path = path;
    }

    public static JsonPointer compile(String expr) throws IllegalArgumentException {
        JSONPath jsonPath = JSONPath.of(expr);
        return new JsonPointer(jsonPath);
    }
}
