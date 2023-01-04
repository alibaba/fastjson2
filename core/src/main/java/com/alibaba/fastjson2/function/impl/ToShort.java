package com.alibaba.fastjson2.function.impl;

import com.alibaba.fastjson2.JSONException;

import java.util.function.Function;

public class ToShort
        implements Function {
    final Short defaultValue;

    public ToShort(Short defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(Object o) {
        if (o == null) {
            return defaultValue;
        }

        if (o instanceof Boolean) {
            return ((Boolean) o).booleanValue() ? (short) 1 : (short) 0;
        }

        if (o instanceof Number) {
            return ((Number) o).shortValue();
        }

        throw new JSONException("can not cast to Short " + o.getClass());
    }
}
