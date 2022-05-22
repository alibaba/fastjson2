package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GuavaSupport {
    static Class CLASS_IMMUTABLE_MAP = null;
    static Class CLASS_ARRAYLIST_MULTI_MAP = null;

    static Method METHOD_IMMUTABLE_MAP_OF_0 = null;
    static Method METHOD_IMMUTABLE_MAP_OF_1 = null;
    static Method METHOD_IMMUTABLE_MAP_COPY_OF = null;

    static Method METHOD_ARRAYLIST_MULTI_MAP_CREATE = null;
    static Method METHOD_ARRAYLIST_MULTI_MAP_PUT_ALL = null;
    static volatile  boolean METHOD_ARRAYLIST_MULTI_MAP_ERROR = false;

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

    static class ImmutableSetConvertFunction
            implements Function {
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

    static class ImmutableListConvertFunction
            implements Function {
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

    public static ObjectWriter createAsMapWriter(Class objectClass) {
        return new AsMapWriter(objectClass);
    }

    static class AsMapWriter
            implements ObjectWriter {
        private Method method;

        public AsMapWriter(Class objectClass) {
            try {
                method = objectClass.getMethod("asMap");
                method.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new JSONException("create Guava AsMapWriter error", e);
            }
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            try {
                Map map = (Map) method.invoke(object);
                jsonWriter.write(map);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new JSONException("create Guava AsMapWriter error", e);
            }
        }

        @Override
        public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            try {
                Map map = (Map) method.invoke(object);
                jsonWriter.write(map);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new JSONException("create Guava AsMapWriter error", e);
            }
        }
    }

    public static Function createConvertFunction(Class objectClass) {
        String instanceTypeName = objectClass.getName();
        switch (instanceTypeName) {
            case "com.google.common.collect.ArrayListMultimap":
                if (CLASS_ARRAYLIST_MULTI_MAP == null) {
                    CLASS_ARRAYLIST_MULTI_MAP = objectClass;
                }

                if (!METHOD_ARRAYLIST_MULTI_MAP_ERROR && METHOD_ARRAYLIST_MULTI_MAP_CREATE == null) {
                    try {
                        METHOD_ARRAYLIST_MULTI_MAP_CREATE = CLASS_ARRAYLIST_MULTI_MAP.getMethod("create");
                    } catch (Throwable ignored) {
                        METHOD_ARRAYLIST_MULTI_MAP_ERROR = true;
                    }
                }

                if (!METHOD_ARRAYLIST_MULTI_MAP_ERROR && METHOD_ARRAYLIST_MULTI_MAP_PUT_ALL == null) {
                    try {
                        METHOD_ARRAYLIST_MULTI_MAP_PUT_ALL = CLASS_ARRAYLIST_MULTI_MAP.getMethod("putAll", Object.class, Iterable.class);
                    } catch (Throwable ignored) {
                        METHOD_ARRAYLIST_MULTI_MAP_ERROR = true;
                    }
                }

                if (METHOD_ARRAYLIST_MULTI_MAP_CREATE != null && METHOD_ARRAYLIST_MULTI_MAP_PUT_ALL != null) {
                    return new ArrayListMultimapConvertFunction(METHOD_ARRAYLIST_MULTI_MAP_CREATE, METHOD_ARRAYLIST_MULTI_MAP_PUT_ALL);
                }
        }

        throw new JSONException("create map error : " + objectClass);
    }

    static class ArrayListMultimapConvertFunction
            implements Function {
        final Method method;
        final Method putAllMethod;

        public ArrayListMultimapConvertFunction(Method method, Method putAllMethod) {
            this.method = method;
            this.putAllMethod = putAllMethod;
        }

        @Override
        public Object apply(Object o) {
            Map map = (Map) o;

            Object multiMap;
            try {
                multiMap = method.invoke(null);
            } catch (Throwable e) {
                throw new JSONException("create ArrayListMultimap error", e);
            }

            for (Iterator<Map.Entry> it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = it.next();
                Object key = entry.getKey();
                Iterable item = (Iterable) entry.getValue();

                try {
                    putAllMethod.invoke(multiMap, key, item);
                } catch (Throwable e) {
                    throw new JSONException("putAll ArrayListMultimap error", e);
                }
            }

            return multiMap;
        }
    }

    static class SingletonImmutableBiMapConvertFunction
            implements Function {
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

    static class ImmutableSingletonMapConvertFunction
            implements Function {
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
