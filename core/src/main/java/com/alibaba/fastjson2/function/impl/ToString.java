package com.alibaba.fastjson2.function.impl;

import com.alibaba.fastjson2.function.Function;

public class ToString
        implements Function {
    @Override
    public Object apply(Object o) {
        if (o == null) {
            return null;
        }
        return o.toString();
    }
}
