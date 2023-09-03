package com.alibaba.fastjson2.reader;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.function.BiFunction;
import com.alibaba.fastjson2.function.Function;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.Fnv;
import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
final class ConstructorFunction<T>
        implements Function<Map<Long, Object>, T> {
    final Constructor constructor;
    final int parameterCount;
    final Function function;
    final BiFunction biFunction;

    final Class[] parameterTypes;
    final boolean kotlinMaker;
    final long[] hashCodes;
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
        this.parameterCount = this.constructor.getParameterTypes().length;
        this.parameterTypes = constructor.getParameterTypes();
        this.hashCodes = new long[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            String name = null;
            if (i < paramNames.length) {
                name = paramNames[i];
            }

            if (name == null) {
                name = "arg" + i;
            }
            hashCodes[i] = Fnv.hashCode64(name);
        }

        if (alternateConstructors != null) {
            alternateConstructorMap = new HashMap<>(alternateConstructors.size());
            alternateConstructorNames = new HashMap<>(alternateConstructors.size());
            alternateConstructorArgTypes = new HashMap<>(alternateConstructors.size());
            alternateConstructorNameHashCodes = new HashMap<>(alternateConstructors.size());
            for (int i = 0; i < alternateConstructors.size(); i++) {
                Constructor alternateConstructor = alternateConstructors.get(i);
                alternateConstructor.setAccessible(true);

                String[] parameterNames = BeanUtils.lookupParameterNames(alternateConstructor);

                Class[] parameters = alternateConstructor.getParameterTypes();
                FieldInfo fieldInfo = new FieldInfo();
                ObjectReaderProvider provider = JSONFactory.defaultObjectReaderProvider;
                Annotation[][] parameterAnnotations = alternateConstructor.getParameterAnnotations();
                for (int j = 0; i < parameters.length && j < parameterNames.length; j++) {
                    fieldInfo.init();

                    provider.getFieldInfo(fieldInfo, alternateConstructor.getDeclaringClass(), alternateConstructor, j, parameterAnnotations);
                    if (fieldInfo.fieldName != null) {
                        parameterNames[j] = fieldInfo.fieldName;
                    }
                }

                long[] parameterNameHashCodes = new long[parameterNames.length];
                Type[] parameterTypes = alternateConstructor.getGenericParameterTypes();
                Set<Long> paramHashCodes = new HashSet<>(parameterNames.length);
                for (int j = 0; j < parameterNames.length; j++) {
                    String paramName = parameterNames[j];
                    long hashCode64 = paramName == null ? 0 : Fnv.hashCode64(paramName);
                    parameterNameHashCodes[j] = hashCode64;
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
        for (int i = 0; i < hashCodes.length; i++) {
            if (!values.containsKey(hashCodes[i])) {
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

        if (function != null && parameterTypes.length == 1) {
            Object arg = values.get(hashCodes[0]);
            Class<?> paramType = (Class<?>) parameterTypes[0];
            if (arg == null) {
                arg = TypeUtils.getDefaultValue(paramType);
            } else {
                if (!paramType.isInstance(arg)) {
                    arg = TypeUtils.cast(arg, paramType, JSONFactory.defaultObjectReaderProvider);
                }
            }
            return (T) function.apply(arg);
        }

        if (biFunction != null && parameterTypes.length == 2) {
            Object arg0 = values.get(hashCodes[0]);
            Class<?> param0Type = parameterTypes[0];
            if (arg0 == null) {
                arg0 = TypeUtils.getDefaultValue(param0Type);
            } else {
                if (!param0Type.isInstance(arg0)) {
                    arg0 = TypeUtils.cast(arg0, param0Type, JSONFactory.defaultObjectReaderProvider);
                }
            }

            Object arg1 = values.get(hashCodes[1]);
            Class<?> param1Type = parameterTypes[1];
            if (arg1 == null) {
                arg1 = TypeUtils.getDefaultValue(param1Type);
            } else {
                if (!param1Type.isInstance(arg1)) {
                    arg1 = TypeUtils.cast(arg1, param1Type, JSONFactory.defaultObjectReaderProvider);
                }
            }

            return (T) biFunction.apply(arg0, arg1);
        }

        final int size = parameterTypes.length;
        Object[] args = new Object[parameterCount];

        if (kotlinMaker) {
            int i = 0, flag = 0;
            for (int n; i < size; i = n) {
                Object arg = values.get(hashCodes[i]);
                if (arg != null) {
                    args[i] = arg;
                } else {
                    flag |= (1 << i);
                    Class<?> paramType = parameterTypes[i];
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
                Class<?> paramClass = parameterTypes[i];
                Object arg = values.get(hashCodes[i]);
                if (arg == null) {
                    arg = TypeUtils.getDefaultValue(paramClass);
                } else {
                    if (!paramClass.isInstance(arg)) {
                        arg = TypeUtils.cast(arg, paramClass, JSONFactory.defaultObjectReaderProvider);
                    } else if (Collection.class.isAssignableFrom(paramClass) || Map.class.isAssignableFrom(paramClass)) {
                        Type[] genericParameterTypes = constructor.getGenericParameterTypes();
                        if (genericParameterTypes.length == parameterTypes.length) {
                            Type paramType = genericParameterTypes[i];
                            arg = TypeUtils.cast(arg, paramType);
                        }
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
