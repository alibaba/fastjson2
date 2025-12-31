package com.alibaba.fastjson2.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public abstract class FieldAccessor
        implements PropertyAccessor {
    protected final Field field;
    protected final String fieldName;
    protected final Class<?> propertyClass;
    protected final boolean supportSet;

    protected FieldAccessor(Field field) {
        this.field = field;
        this.propertyClass = field.getType();
        this.fieldName = field.getName();
        supportSet = (field.getModifiers() & Modifier.FINAL) == 0;
    }

    @Override
    public final Field field() {
        return field;
    }

    @Override
    public final Class<?> propertyClass() {
        return propertyClass;
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
