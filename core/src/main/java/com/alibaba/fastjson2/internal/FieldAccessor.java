package com.alibaba.fastjson2.internal;

import java.lang.reflect.Field;

public abstract class FieldAccessor
        implements PropertyAccessor {
    protected final Field field;

    protected FieldAccessor(Field field) {
        this.field = field;
    }

    public final Field field() {
        return field;
    }

    public final String name() {
        return field.getName();
    }
}
