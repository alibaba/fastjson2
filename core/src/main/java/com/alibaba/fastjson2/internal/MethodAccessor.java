package com.alibaba.fastjson2.internal;

import java.lang.reflect.Method;

public abstract class MethodAccessor implements PropertyAccessor {
    protected final String name;
    protected final Method method;

    protected MethodAccessor(String name, Method method) {
        this.name = name;
        this.method = method;
    }

    @Override
    public final String name() {
        return name;
    }
}
