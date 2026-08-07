package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSON;

import java.lang.reflect.Type;

public class MultiType
        implements Type {
    private final Type[] types;

    public MultiType(Type... types) {
        this.types = types;
    }

    public int size() {
        return types.length;
    }

    public Type getType(int index) {
        return types[index];
    }

    @Override
    public String toString() {
        return JSON.toJSONString(types);
    }
}
