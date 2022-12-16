package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

final class ConstructorFunction<T>
        implements Function<Map<Long, Object>, T> {
    final Constructor constructor;
    final Parameter[] parameters;
    final String[] paramNames;
    final boolean kotlinMaker;
    final long[] hashCodes;
    final List<Constructor> alternateConstructors;
    Map<Set<Long>, Constructor> alternateConstructorMap;
    Map<Set<Long>, String[]> alternateConstructorNames;
    Map<Set<Long>, long[]> alternateConstructorNameHashCodes;
    Map<Set<Long>, Type[]> alternateConstructorArgTypes;

    ConstructorFunction(
            List<Constructor> alternateConstructors,
            Constructor constructor,
            Constructor markerConstructor,
            String... paramNames
    ) {
        this.kotlinMaker = markerConstructor != null;
        this.constructor = kotlinMaker ? markerConstructor : constructor;
        this.parameters = constructor.getParameters();
        this.paramNames = paramNames;
        this.hashCodes = new long[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            String name;
            if (i < paramNames.length) {
                name = paramNames[i];
            } else {
                name = parameters[i].getName();
            }
            hashCodes[i] = Fnv.hashCode64(name);
        }

        this.alternateConstructors = alternateConstructors;
        if (alternateConstructors != null) {
            alternateConstructorMap = new HashMap<>(alternateConstructors.size());
            alternateConstructorNames = new HashMap<>(alternateConstructors.size());
            alternateConstructorArgTypes = new HashMap<>(alternateConstructors.size());
            alternateConstructorNameHashCodes = new HashMap<>(alternateConstructors.size());
            for (Constructor alternateConstructor : alternateConstructors) {
                alternateConstructor.setAccessible(true);

                String[] parameterNames = ASMUtils.lookupParameterNames(alternateConstructor);
                long[] parameterNameHashCodes = new long[parameterNames.length];
                Type[] parameterTypes = alternateConstructor.getGenericParameterTypes();
                Set<Long> paramHashCodes = new HashSet<>(parameterNames.length);
                for (int i = 0; i < parameterNames.length; i++) {
                    long hashCode64 = Fnv.hashCode64(parameterNames[i]);
                    parameterNameHashCodes[i] = hashCode64;
                    paramHashCodes.add(hashCode64);
                }
                alternateConstructorMap.put(paramHashCodes, alternateConstructor);
                alternateConstructorNames.put(paramHashCodes, parameterNames);
                alternateConstructorNameHashCodes.put(paramHashCodes, parameterNameHashCodes);
                alternateConstructorArgTypes.put(paramHashCodes, parameterTypes);
            }
        }
    }

    @Override
    public T apply(Map<Long, Object> values) {
        boolean containsAll = true;
        for (long hashCode : hashCodes) {
            if (!values.containsKey(hashCode)) {
                containsAll = false;
                break;
            }
        }

        if (!containsAll && alternateConstructorMap != null) {
            Set<Long> key = values.keySet();
            Constructor constructor = alternateConstructorMap.get(key);
            if (constructor != null) {
                long[] hashCodes = alternateConstructorNameHashCodes.get(key);
                Type[] paramTypes = alternateConstructorArgTypes.get(key);
                Object[] args = new Object[hashCodes.length];
                for (int i = 0; i < hashCodes.length; i++) {
                    Object arg = values.get(hashCodes[i]);
                    Type paramType = paramTypes[i];
                    if (arg == null) {
                        arg = TypeUtils.getDefaultValue(paramType);
                    }
                    args[i] = arg;
                }

                try {
                    return (T) constructor.newInstance(args);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new JSONException("invoke constructor error, " + constructor, e);
                }
            }
        }

        Object[] args;
        if (kotlinMaker) {
            args = new Object[parameters.length + 2];
            int i = 0, flag = 0;
            for (; i < parameters.length; i++) {
                Object arg = values.get(hashCodes[i]);
                if (arg != null) {
                    args[i] = arg;
                } else {
                    flag |= (1 << i);
                    Class<?> paramType = parameters[i].getType();
                    if (paramType.isPrimitive()) {
                        args[i] = TypeUtils.getDefaultValue(paramType);
                    }
                }
            }
            args[i] = flag;
        } else {
            args = new Object[parameters.length];
            for (int i = 0; i < args.length; i++) {
                Class<?> paramType = parameters[i].getType();
                Object arg = values.get(hashCodes[i]);
                if (arg == null) {
                    arg = TypeUtils.getDefaultValue(paramType);
                }
                args[i] = arg;
            }
        }

        try {
            return (T) constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new JSONException("invoke constructor error, " + constructor, e);
        }
    }
}
