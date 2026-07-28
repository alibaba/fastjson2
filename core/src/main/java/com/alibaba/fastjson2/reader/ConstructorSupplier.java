package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.function.Supplier;

final class ConstructorSupplier
        implements Supplier {
    final Constructor constructor;
    final Class objectClass;
    final boolean useClassNewInstance;
    final Class paramType;

    public ConstructorSupplier(Constructor constructor) {
        constructor.setAccessible(true);
        this.constructor = constructor;
        this.objectClass = this.constructor.getDeclaringClass();
        this.useClassNewInstance = constructor.getParameterCount() == 0
                && Modifier.isPublic(constructor.getModifiers())
                && Modifier.isPublic(objectClass.getModifiers());
        this.paramType = constructor.getParameterCount() == 1
                ? constructor.getParameterTypes()[0]
                : null;
    }

    @Override
    public Object get() {
        try {
            if (useClassNewInstance) {
                return objectClass.newInstance();
            } else {
                if (paramType != null) {
                    Object dummy = JDKUtils.UNSAFE.allocateInstance(paramType);
                    return constructor.newInstance(dummy);
                } else {
                    return constructor.newInstance();
                }
            }
        } catch (Throwable e) {
            throw new JSONException("create instance error", e);
        }
    }
}
