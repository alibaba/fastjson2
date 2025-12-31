package com.alibaba.fastjson2.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class FieldAccessor
        implements PropertyAccessor {
    protected final Field field;
    protected final String fieldName;
    protected final Class<?> fieldClass;
    protected final boolean supportSet;

    protected FieldAccessor(Field field) {
        this.field = field;
        this.fieldClass = field.getType();
        this.fieldName = field.getName();
        supportSet = !Modifier.isFinal(field.getModifiers());
    }

    @Override
    public final Field field() {
        return field;
    }

    @Override
    public final String name() {
        return fieldName;
    }

    @Override
    public final boolean supportGet() {
        return true;
    }

    @Override
    public final boolean supportSet() {
        return supportSet;
    }
}
