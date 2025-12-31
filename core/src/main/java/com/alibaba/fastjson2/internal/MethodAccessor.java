package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public abstract class MethodAccessor implements PropertyAccessor {
    protected final String name;
    protected final Class<?> propertyClass;
    protected final Method getter;
    protected final Method setter;

    public MethodAccessor(String name, Class<?> propertyClass, Method getter, Method setter) {
        this.name = name;
        this.propertyClass = propertyClass;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public final String name() {
        return name;
    }

    public final boolean supportGet() {
        return getter != null;
    }

    public final boolean supportSet() {
        return setter != null;
    }

    public final Class<?> propertyClass() {
        return propertyClass;
    }

    public final Type propertyType() {
        return propertyClass;
    }

    final JSONException errorForGet(Exception e) {
        return new JSONException(getter.toString() + " get error", e);
    }

    final JSONException errorForSet(Exception e) {
        return new JSONException(setter.toString() + " set error", e);
    }
}
