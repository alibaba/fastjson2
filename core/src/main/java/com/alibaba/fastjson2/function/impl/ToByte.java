package com.alibaba.fastjson2.function.impl;

import com.alibaba.fastjson2.JSONException;

import java.util.function.Function;

public class ToByte
        implements Function {
    final Byte defaultValue;

    public ToByte(Byte defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(Object o) {
        if (o == null) {
            return defaultValue;
        }

        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue() ? (byte) 1 : (byte) 0;
        }

        if (o instanceof Number) {
            return ((Number) o).byteValue();
        }

        throw new JSONException("can not cast to Byte " + o.getClass());
    }
}
