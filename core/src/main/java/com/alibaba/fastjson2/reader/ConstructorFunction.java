package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.internal.asm.ASMUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ConstructorFunction<T>
        implements Function<Map<Long, Object>, T> {
    final Constructor constructor;
    final Function function;
    final BiFunction biFunction;

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
            Function function,
            BiFunction biFunction,
            Constructor markerConstructor,
            String... paramNames
    ) {
        this.kotlinMaker = markerConstructor != null;
        this.function = function;
        this.biFunction = biFunction;
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
            if (name == null) {
                name = "arg" + i;
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

                Parameter[] parameters = alternateConstructor.getParameters();
                FieldInfo fieldInfo = new FieldInfo();
                ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
                for (int i = 0; i < parameters.length && i < parameterNames.length; i++) {
                    fieldInfo.init();

                    Parameter parameter = parameters[i];
                    provider.getFieldInfo(fieldInfo, alternateConstructor.getDeclaringClass(), alternateConstructor, i, parameter);
                    if (fieldInfo.fieldName != null) {
                        parameterNames[i] = fieldInfo.fieldName;
                    }
                }

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
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                         InvocationTargetException e) {
                    throw new JSONException("invoke constructor error, " + constructor, e);
                }
            }
        }

        if (function != null && parameters.length == 1) {
            Parameter param = parameters[0];
            Object arg = values.get(hashCodes[0]);
            Class<?> paramType = param.getType();
            if (arg == null) {
                arg = TypeUtils.getDefaultValue(paramType);
            } else {
                if (!paramType.isInstance(arg)) {
                    arg = TypeUtils.cast(arg, paramType);
                }
            }
            return (T) function.apply(arg);
        }

        if (biFunction != null && parameters.length == 2) {
            Object arg0 = values.get(hashCodes[0]);
            Parameter param0 = parameters[0];
            Class<?> param0Type = param0.getType();
            if (arg0 == null) {
                arg0 = TypeUtils.getDefaultValue(param0Type);
            } else {
                if (!param0Type.isInstance(arg0)) {
                    arg0 = TypeUtils.cast(arg0, param0Type);
                }
            }

            Object arg1 = values.get(hashCodes[1]);
            Parameter param1 = parameters[1];
            Class<?> param1Type = param1.getType();
            if (arg1 == null) {
                arg1 = TypeUtils.getDefaultValue(param1Type);
            } else {
                if (!param1Type.isInstance(arg1)) {
                    arg1 = TypeUtils.cast(arg1, param1Type);
                }
            }

            return (T) biFunction.apply(arg0, arg1);
        }

        final int size = parameters.length;
        Object[] args = new Object[constructor.getParameterCount()];

        if (kotlinMaker) {
            int i = 0, flag = 0;
            for (int n; i < size; i = n) {
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
                n = i + 1;
                if (n % 32 == 0 || n == size) {
                    args[size + i / 32] = flag;
                    flag = 0;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                Class<?> paramType = parameters[i].getType();
                Object arg = values.get(hashCodes[i]);
                if (arg == null) {
                    arg = TypeUtils.getDefaultValue(paramType);
                } else {
                    if (!paramType.isInstance(arg)) {
                        arg = TypeUtils.cast(arg, paramType);
                    }
                }
                args[i] = arg;
            }
        }

        try {
            return (T) constructor.newInstance(args);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            throw new JSONException("invoke constructor error, " + constructor, e);
        }
    }
}
