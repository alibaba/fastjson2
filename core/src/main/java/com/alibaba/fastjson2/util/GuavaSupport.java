package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GuavaSupport {
    static Class CLASS_IMMUTABLE_MAP = null;
    static Method METHOD_IMMUTABLE_MAP_OF_0 = null;
    static Method METHOD_IMMUTABLE_MAP_OF_1 = null;
    static Method METHOD_IMMUTABLE_MAP_COPY_OF = null;

    static Constructor CONSTRUCTOR_SINGLETON_IMMUTABLE_BIMAP = null;

    public static Function immutableListConverter() {
        return new ImmutableListConvertFunction();
    }

    public static Function immutableSetConverter() {
        return new ImmutableSetConvertFunction();
    }

    public static Function immutableMapConverter() {
        return new ImmutableSingletonMapConvertFunction();
    }

    public static Function singletonBiMapConverter() {
        return new SingletonImmutableBiMapConvertFunction();
    }

    static class ImmutableSetConvertFunction implements Function {
        @Override
        public Object apply(Object object) {
            List list = (List) object;
            if (list.isEmpty()) {
                return ImmutableSet.of();
            }

            if (list.size() == 1) {
                return ImmutableSet.of(list.get(0));
            }

            return ImmutableSet.copyOf(list);
        }
    }


    static class ImmutableListConvertFunction implements Function {
        @Override
        public Object apply(Object object) {
            List list = (List) object;
            if (list.isEmpty()) {
                return ImmutableList.of();
            }

            if (list.size() == 1) {
                return ImmutableList.of(list.get(0));
            }

            return ImmutableList.copyOf(list);
        }
    }

    static class SingletonImmutableBiMapConvertFunction implements Function {
        @Override
        public Object apply(Object object) {

            if (CONSTRUCTOR_SINGLETON_IMMUTABLE_BIMAP == null) {
                try {
                    Constructor constructor = TypeUtils.loadClass("com.google.common.collect.SingletonImmutableBiMap")
                            .getDeclaredConstructor(Object.class, Object.class);
                    constructor.setAccessible(true);
                    CONSTRUCTOR_SINGLETON_IMMUTABLE_BIMAP = constructor;
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("method not found : com.google.common.collect.SingletonImmutableBiMap(Object, Object)", e);
                }
            }

            Map map = (Map) object;
            Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();

            try {
                return CONSTRUCTOR_SINGLETON_IMMUTABLE_BIMAP.newInstance(entry.getKey(), entry.getValue());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new JSONException("create map error", e);
            }
        }
    }

    static class ImmutableSingletonMapConvertFunction implements Function {
        @Override
        public Object apply(Object object) {
            if (CLASS_IMMUTABLE_MAP == null) {
                CLASS_IMMUTABLE_MAP = TypeUtils.loadClass("com.google.common.collect.ImmutableMap");
            }

            if (CLASS_IMMUTABLE_MAP == null) {
                throw new JSONException("class not found : com.google.common.collect.ImmutableMap");
            }

            Map map = (Map) object;

            if (map.size() == 0) {
                if (METHOD_IMMUTABLE_MAP_OF_0 == null) {
                    try {
                        Method method = CLASS_IMMUTABLE_MAP.getMethod("of");
                        method.setAccessible(true);
                        METHOD_IMMUTABLE_MAP_OF_0 = method;
                    } catch (NoSuchMethodException e) {
                        throw new JSONException("method not found : com.google.common.collect.ImmutableMap.of", e);
                    }
                }

                try {
                    return METHOD_IMMUTABLE_MAP_OF_0.invoke(null);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new JSONException("create map error", e);
                }
            }

            if (map.size() == 1) {
                if (METHOD_IMMUTABLE_MAP_OF_1 == null) {
                    try {
                        Method method = CLASS_IMMUTABLE_MAP.getMethod("of", Object.class, Object.class);
                        method.setAccessible(true);
                        METHOD_IMMUTABLE_MAP_OF_1 = method;
                    } catch (NoSuchMethodException e) {
                        throw new JSONException("method not found : com.google.common.collect.ImmutableBiMap.of", e);
                    }
                }

                Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();

                try {
                    return METHOD_IMMUTABLE_MAP_OF_1.invoke(null, entry.getKey(), entry.getValue());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new JSONException("create map error", e);
                }
            }

            if (METHOD_IMMUTABLE_MAP_COPY_OF == null) {
                try {
                    Method method = CLASS_IMMUTABLE_MAP.getMethod("copyOf", Map.class);
                    method.setAccessible(true);
                    METHOD_IMMUTABLE_MAP_COPY_OF = method;
                } catch (NoSuchMethodException e) {
                    throw new JSONException("method not found : com.google.common.collect.ImmutableBiMap.copyOf", e);
                }
            }

            try {
                return METHOD_IMMUTABLE_MAP_COPY_OF.invoke(null, map);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("create map error", e);
            }
        }
    }
}
