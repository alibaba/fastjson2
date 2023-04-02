package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.support.LambdaMiscCodec;
import com.alibaba.fastjson2.util.Fnv;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

final class FactoryFunction<T>
        implements Function<Map<Long, Object>, T> {
    final Method factoryMethod;
    final Function function;
    final BiFunction biFunction;
    final String[] paramNames;
    final long[] hashCodes;

    FactoryFunction(Method factoryMethod, String... paramNames) {
        this.factoryMethod = factoryMethod;
        Parameter[] parameters = factoryMethod.getParameters();
        this.paramNames = new String[parameters.length];
        this.hashCodes = new long[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            String name;
            if (i < paramNames.length) {
                name = paramNames[i];
            } else {
                name = parameters[i].getName();
            }
            paramNames[i] = name;
            hashCodes[i] = Fnv.hashCode64(name);
        }

        Function function = null;
        BiFunction biFunction = null;
        if (ObjectReaderCreator.JIT) {
            int parameterCount = factoryMethod.getParameterCount();
            if (parameterCount == 1) {
                function = LambdaMiscCodec.createFunction(factoryMethod);
            } else if (parameterCount == 2) {
                biFunction = LambdaMiscCodec.createBiFunction(factoryMethod);
            }
        }
        this.function = function;
        this.biFunction = biFunction;
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
