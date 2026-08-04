package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.util.BeanUtils;
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
        this.paramType = BeanUtils.getEnclosingInstanceParamType(constructor);
    }

    @Override
    public Object get() {
        try {
            if (useClassNewInstance) {
                return objectClass.newInstance();
            }

            if (paramType != null) {
                Object dummy;
                try {
                    dummy = JDKUtils.UNSAFE.allocateInstance(paramType);
                } catch (InstantiationException ignored) {
                    // the enclosing class cannot be allocated (for example an abstract class),
                    // pass null as before JDK 25
                    dummy = null;
                }
                return constructor.newInstance(dummy);
            }

            if (constructor.getParameterCount() == 1) {
                return constructor.newInstance(new Object[1]);
            }
            return constructor.newInstance();
        } catch (Throwable e) {
            throw new JSONException("create instance error", e);
        }
    }
}
