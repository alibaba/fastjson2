package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.Supplier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

final class ConstructorSupplier
        implements Supplier {
    final Constructor constructor;
    final Class objectClass;
    final boolean useClassNewInstance;
    final int parameterCount;

    public ConstructorSupplier(Constructor constructor) {
        constructor.setAccessible(true);
        this.constructor = constructor;
        this.objectClass = this.constructor.getDeclaringClass();
        this.parameterCount = constructor.getParameterTypes().length;
        this.useClassNewInstance = parameterCount == 0
                && Modifier.isPublic(constructor.getModifiers())
                && Modifier.isPublic(objectClass.getModifiers());
    }

    @Override
    public Object get() {
        try {
            if (useClassNewInstance) {
                return objectClass.newInstance();
            } else {
                if (parameterCount == 1) {
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
