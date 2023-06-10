package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.BiFunction;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

final class FactoryFunction<T>
        implements Function<Map<Long, Object>, T> {
    final Method factoryMethod;
    final Function function;
    final BiFunction biFunction;
    final String[] paramNames;
    final long[] hashCodes;

    FactoryFunction(Method factoryMethod, String... paramNames) {
        this.factoryMethod = factoryMethod;
        Class[] parameters = factoryMethod.getParameterTypes();
        this.paramNames = new String[parameters.length];
        this.hashCodes = new long[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            String name = null;
            if (i < paramNames.length) {
                name = paramNames[i];
            }
            if (name == null) {
                name = "arg" + i;
            }
            paramNames[i] = name;
            hashCodes[i] = Fnv.hashCode64(name);
        }

        this.function = null;
        this.biFunction = null;
    }

    @Override
    public T apply(Map<Long, Object> values) {
        if (function != null) {
            Object arg = values.get(hashCodes[0]);
            return (T) function.apply(arg);
        }

        if (biFunction != null) {
            Object arg0 = values.get(hashCodes[0]);
            Object arg1 = values.get(hashCodes[1]);
            return (T) biFunction.apply(arg0, arg1);
        }

        Object[] args = new Object[hashCodes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = values.get(hashCodes[i]);
        }
        try {
            return (T) factoryMethod.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            throw new JSONException("invoke factoryMethod error", e);
        }
    }
}
