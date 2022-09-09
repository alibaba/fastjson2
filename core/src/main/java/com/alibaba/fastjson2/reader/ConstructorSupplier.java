package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

final class ConstructorSupplier
        implements Supplier {
    final Constructor constructor;
    final Class objectClass;
    final boolean useClassNewIntance;

    public ConstructorSupplier(Constructor constructor) {
        this.constructor = constructor;
        this.objectClass = this.constructor.getDeclaringClass();
        this.useClassNewIntance = constructor.getParameterCount() == 0
                && Modifier.isPublic(constructor.getModifiers())
                && Modifier.isPublic(objectClass.getModifiers());
    }

    @Override
    public Object get() {
        try {
            if (useClassNewIntance) {
                return objectClass.newInstance();
            } else {
                return constructor.newInstance();
            }
        } catch (Throwable e) {
            throw new JSONException("create instance error", e);
        }
    }
}
