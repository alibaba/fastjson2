package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.support.LambdaMiscCodec;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class GuavaSupport {
    static Class CLASS_IMMUTABLE_MAP;
    static Class CLASS_IMMUTABLE_LIST;
    static Class CLASS_IMMUTABLE_SET;
    static Class CLASS_ARRAYLIST_MULTI_MAP;

    static Supplier FUNC_IMMUTABLE_MAP_OF_0;
    static BiFunction FUNC_IMMUTABLE_MAP_OF_1;
    static Function FUNC_IMMUTABLE_MAP_COPY_OF;

    static Supplier FUNC_IMMUTABLE_LIST_OF_0;
    static Function FUNC_IMMUTABLE_LIST_OF_1;
    static Function FUNC_IMMUTABLE_LIST_COPY_OF;

    static Supplier FUNC_IMMUTABLE_SET_OF_0;
    static Function FUNC_IMMUTABLE_SET_OF_1;
    static Function FUNC_IMMUTABLE_SET_COPY_OF;

    static Supplier FUNC_ARRAYLIST_MULTI_MAP_CREATE;
    static Method METHOD_ARRAYLIST_MULTI_MAP_PUT_ALL;
    static volatile boolean METHOD_ARRAYLIST_MULTI_MAP_ERROR;

    static BiFunction FUNC_SINGLETON_IMMUTABLE_BIMAP;

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
            if (CLASS_IMMUTABLE_SET == null) {
                CLASS_IMMUTABLE_SET = TypeUtils.loadClass("com.google.common.collect.ImmutableSet");
            }

            if (CLASS_IMMUTABLE_SET == null) {
                throw new JSONException("class not found : com.google.common.collect.ImmutableSet");
            }

            List list = (List) object;
            if (list.isEmpty()) {
                if (FUNC_IMMUTABLE_SET_OF_0 == null) {
                    try {
                        Method method = CLASS_IMMUTABLE_SET.getMethod("of");
                        FUNC_IMMUTABLE_SET_OF_0 = LambdaMiscCodec.createSupplier(method);
                    } catch (NoSuchMethodException e) {
                        throw new JSONException("method not found : com.google.common.collect.ImmutableSet.of", e);
                    }
                }

                return FUNC_IMMUTABLE_SET_OF_0.get();
            }

            if (list.size() == 1) {
                if (FUNC_IMMUTABLE_SET_OF_1 == null) {
                    try {
                        Method method = CLASS_IMMUTABLE_SET.getMethod("of", Object.class);
                        FUNC_IMMUTABLE_SET_OF_1 = LambdaMiscCodec.createFunction(method);
                    } catch (NoSuchMethodException e) {
                        throw new JSONException("method not found : com.google.common.collect.ImmutableSet.of", e);
                    }
                }

                Object first = list.get(0);
                return FUNC_IMMUTABLE_SET_OF_1.apply(first);
            }

            if (FUNC_IMMUTABLE_SET_COPY_OF == null) {
                try {
                    Method method = CLASS_IMMUTABLE_SET.getMethod("copyOf", Collection.class);
                    FUNC_IMMUTABLE_SET_COPY_OF = LambdaMiscCodec.createFunction(method);
                } catch (NoSuchMethodException e) {
                    throw new JSONException("method not found : com.google.common.collect.ImmutableSet.copyOf", e);
                }
            }

            return FUNC_IMMUTABLE_SET_COPY_OF.apply(list);
        }
    }

    static class ImmutableListConvertFunction
            implements Function {
        @Override
        public Object apply(Object object) {
            if (CLASS_IMMUTABLE_LIST == null) {
                CLASS_IMMUTABLE_LIST = TypeUtils.loadClass("com.google.common.collect.ImmutableList");
            }

            if (CLASS_IMMUTABLE_LIST == null) {
                throw new JSONException("class not found : com.google.common.collect.ImmutableList");
            }

            List list = (List) object;
            if (list.isEmpty()) {
                if (FUNC_IMMUTABLE_LIST_OF_0 == null) {
                    try {
                        Method method = CLASS_IMMUTABLE_LIST.getMethod("of");
                        FUNC_IMMUTABLE_LIST_OF_0 = LambdaMiscCodec.createSupplier(method);
                    } catch (NoSuchMethodException e) {
                        throw new JSONException("method not found : com.google.common.collect.ImmutableList.of", e);
                    }
                }

                return FUNC_IMMUTABLE_LIST_OF_0.get();
            }

            if (list.size() == 1) {
                if (FUNC_IMMUTABLE_LIST_OF_1 == null) {
                    try {
                        Method method = CLASS_IMMUTABLE_LIST.getMethod("of", Object.class);
                        FUNC_IMMUTABLE_LIST_OF_1 = LambdaMiscCodec.createFunction(method);
                    } catch (NoSuchMethodException e) {
                        throw new JSONException("method not found : com.google.common.collect.ImmutableList.of", e);
                    }
                }

                Object first = list.get(0);
                return FUNC_IMMUTABLE_LIST_OF_1.apply(first);
            }

            if (FUNC_IMMUTABLE_LIST_COPY_OF == null) {
                try {
                    Method method = CLASS_IMMUTABLE_LIST.getMethod("copyOf", Collection.class);
                    FUNC_IMMUTABLE_LIST_COPY_OF = LambdaMiscCodec.createFunction(method);
                } catch (NoSuchMethodException e) {
                    throw new JSONException("method not found : com.google.common.collect.ImmutableList.copyOf", e);
                }
            }

            return FUNC_IMMUTABLE_LIST_COPY_OF.apply(list);
        }
    }

    public static ObjectWriter createAsMapWriter(Class objectClass) {
        return new AsMapWriter(objectClass);
    }

    static class AsMapWriter
            implements ObjectWriter {
        final Class objectClass;
        final String typeName;
        final long typeNameHash;
        final Function asMap;

        protected byte[] typeNameJSONB;

        public AsMapWriter(Class objectClass) {
            this.objectClass = objectClass;
            this.typeName = TypeUtils.getTypeName(objectClass);
            this.typeNameHash = Fnv.hashCode64(typeName);
            try {
                Method method = objectClass.getMethod("asMap");
                asMap = LambdaMiscCodec.createFunction(method);
            } catch (NoSuchMethodException e) {
                throw new JSONException("create Guava AsMapWriter error", e);
            }
        }

        @Override
        public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            Map map = (Map) asMap.apply(object);
            jsonWriter.write(map);
        }

        @Override
        public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
            if (jsonWriter.isWriteTypeInfo(object, fieldType, features)) {
                if (typeNameJSONB == null) {
                    typeNameJSONB = JSONB.toBytes(typeName);
                }
                jsonWriter.writeTypeName(typeNameJSONB, typeNameHash);
            }

            Map map = (Map) asMap.apply(object);
            jsonWriter.write(map);
        }
    }

    public static Function createConvertFunction(Class objectClass) {
        String instanceTypeName = objectClass.getName();
        if ("com.google.common.collect.ArrayListMultimap".equals(instanceTypeName)) {
            if (CLASS_ARRAYLIST_MULTI_MAP == null) {
                CLASS_ARRAYLIST_MULTI_MAP = objectClass;
            }

            if (!METHOD_ARRAYLIST_MULTI_MAP_ERROR && FUNC_ARRAYLIST_MULTI_MAP_CREATE == null) {
                try {
                    Method method = CLASS_ARRAYLIST_MULTI_MAP.getMethod("create");
                    FUNC_ARRAYLIST_MULTI_MAP_CREATE = LambdaMiscCodec.createSupplier(method);
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

            if (FUNC_ARRAYLIST_MULTI_MAP_CREATE != null && METHOD_ARRAYLIST_MULTI_MAP_PUT_ALL != null) {
                return new ArrayListMultimapConvertFunction(FUNC_ARRAYLIST_MULTI_MAP_CREATE, METHOD_ARRAYLIST_MULTI_MAP_PUT_ALL);
            }
        }

        throw new JSONException("create map error : " + objectClass);
    }

    static class ArrayListMultimapConvertFunction
            implements Function {
        final Supplier method;
        final Method putAllMethod;

        public ArrayListMultimapConvertFunction(Supplier method, Method putAllMethod) {
            this.method = method;
            this.putAllMethod = putAllMethod;
        }

        @Override
        public Object apply(Object o) {
            Map map = (Map) o;

            Object multiMap = method.get();

            for (Map.Entry entry : (Iterable<Map.Entry>) map.entrySet()) {
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
            if (FUNC_SINGLETON_IMMUTABLE_BIMAP == null) {
                try {
                    Constructor constructor = TypeUtils.loadClass("com.google.common.collect.SingletonImmutableBiMap")
                            .getDeclaredConstructor(Object.class, Object.class);
                    FUNC_SINGLETON_IMMUTABLE_BIMAP = LambdaMiscCodec.createBiFunction(constructor);
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("method not found : com.google.common.collect.SingletonImmutableBiMap(Object, Object)", e);
                }
            }

            Map map = (Map) object;
            Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();

            return FUNC_SINGLETON_IMMUTABLE_BIMAP.apply(entry.getKey(), entry.getValue());
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
                if (FUNC_IMMUTABLE_MAP_OF_0 == null) {
                    try {
                        Method method = CLASS_IMMUTABLE_MAP.getMethod("of");
                        FUNC_IMMUTABLE_MAP_OF_0 = LambdaMiscCodec.createSupplier(method);
                    } catch (NoSuchMethodException e) {
                        throw new JSONException("method not found : com.google.common.collect.ImmutableMap.of", e);
                    }
                }

                return FUNC_IMMUTABLE_MAP_OF_0.get();
            }

            if (map.size() == 1) {
                if (FUNC_IMMUTABLE_MAP_OF_1 == null) {
                    try {
                        Method method = CLASS_IMMUTABLE_MAP.getMethod("of", Object.class, Object.class);
                        method.setAccessible(true);
                        FUNC_IMMUTABLE_MAP_OF_1 = LambdaMiscCodec.createBiFunction(method);
                    } catch (NoSuchMethodException e) {
                        throw new JSONException("method not found : com.google.common.collect.ImmutableBiMap.of", e);
                    }
                }

                Map.Entry entry = (Map.Entry) map.entrySet().iterator().next();
                return FUNC_IMMUTABLE_MAP_OF_1.apply(entry.getKey(), entry.getValue());
            }

            if (FUNC_IMMUTABLE_MAP_COPY_OF == null) {
                try {
                    Method method = CLASS_IMMUTABLE_MAP.getMethod("copyOf", Map.class);
                    FUNC_IMMUTABLE_MAP_COPY_OF = LambdaMiscCodec.createFunction(method);
                } catch (NoSuchMethodException e) {
                    throw new JSONException("method not found : com.google.common.collect.ImmutableBiMap.copyOf", e);
                }
            }

            return FUNC_IMMUTABLE_MAP_COPY_OF.apply(map);
        }
    }
}
