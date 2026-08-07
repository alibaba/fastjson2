package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

final class ConstructorSupplier
        implements Supplier {
    final Constructor constructor;
    final Class objectClass;
    final boolean useClassNewInstance;

    public ConstructorSupplier(Constructor constructor) {
        constructor.setAccessible(true);
        this.constructor = constructor;
        this.objectClass = this.constructor.getDeclaringClass();
        this.useClassNewInstance = constructor.getParameterCount() == 0
                && Modifier.isPublic(constructor.getModifiers())
                && Modifier.isPublic(objectClass.getModifiers());
    }

    @Override
    public Object get() {
        try {
            if (useClassNewInstance) {
                return objectClass.newInstance();
            } else {
                if (constructor.getParameterCount() == 1) {
                    return constructor.newInstance(new Object[1]);
                } else {
                    return constructor.newInstance();
                }
            }
        } catch (Throwable e) {
            throw new JSONException("create instance error", e);
        }
    }
}
