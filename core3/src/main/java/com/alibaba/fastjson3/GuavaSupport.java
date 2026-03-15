package com.alibaba.fastjson3;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Optional Guava collection support via pure reflection.
 * Zero compile-time dependency on Guava — types are detected by class name
 * and factory methods are invoked reflectively.
 *
 * <p>Supports ImmutableList, ImmutableSet, ImmutableMap deserialization.
 * Serialization requires no special handling (Guava collections implement standard interfaces).</p>
 */
final class GuavaSupport {
    private static final boolean AVAILABLE;
    private static final Class<?> IMMUTABLE_LIST_CLASS;
    private static final Class<?> IMMUTABLE_SET_CLASS;
    private static final Class<?> IMMUTABLE_MAP_CLASS;
    private static final Method LIST_COPY_OF;
    private static final Method SET_COPY_OF;
    private static final Method MAP_COPY_OF;

    static {
        Class<?> listClass = null;
        Class<?> setClass = null;
        Class<?> mapClass = null;
        Method listCopyOf = null;
        Method setCopyOf = null;
        Method mapCopyOf = null;
        boolean available = false;
        try {
            listClass = Class.forName("com.google.common.collect.ImmutableList");
            setClass = Class.forName("com.google.common.collect.ImmutableSet");
            mapClass = Class.forName("com.google.common.collect.ImmutableMap");
            listCopyOf = listClass.getMethod("copyOf", java.util.Collection.class);
            setCopyOf = setClass.getMethod("copyOf", java.util.Collection.class);
            mapCopyOf = mapClass.getMethod("copyOf", Map.class);
            available = true;
        } catch (Exception ignored) {
        }
        AVAILABLE = available;
        IMMUTABLE_LIST_CLASS = listClass;
        IMMUTABLE_SET_CLASS = setClass;
        IMMUTABLE_MAP_CLASS = mapClass;
        LIST_COPY_OF = listCopyOf;
        SET_COPY_OF = setCopyOf;
        MAP_COPY_OF = mapCopyOf;
    }

    private GuavaSupport() {
    }

    static boolean isAvailable() {
        return AVAILABLE;
    }

    static boolean isImmutableList(Class<?> type) {
        return AVAILABLE && IMMUTABLE_LIST_CLASS.isAssignableFrom(type);
    }

    static boolean isImmutableSet(Class<?> type) {
        return AVAILABLE && IMMUTABLE_SET_CLASS.isAssignableFrom(type);
    }

    static boolean isImmutableMap(Class<?> type) {
        return AVAILABLE && IMMUTABLE_MAP_CLASS.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    static <T> T toImmutableList(List<?> list) {
        try {
            return (T) LIST_COPY_OF.invoke(null, list);
        } catch (Exception e) {
            throw new JSONException("failed to create ImmutableList", e);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T toImmutableSet(java.util.Collection<?> collection) {
        try {
            return (T) SET_COPY_OF.invoke(null, collection);
        } catch (Exception e) {
            throw new JSONException("failed to create ImmutableSet", e);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> T toImmutableMap(Map<?, ?> map) {
        try {
            return (T) MAP_COPY_OF.invoke(null, map);
        } catch (Exception e) {
            throw new JSONException("failed to create ImmutableMap", e);
        }
    }

    // ==================== ObjectReader factories ====================

    static ObjectReader<?> getReader(Class<?> type) {
        if (!AVAILABLE) {
            return null;
        }
        if (isImmutableList(type)) {
            return IMMUTABLE_LIST_READER;
        }
        if (isImmutableSet(type)) {
            return IMMUTABLE_SET_READER;
        }
        if (isImmutableMap(type)) {
            return IMMUTABLE_MAP_READER;
        }
        return null;
    }

    private static Class<?> extractElementType(java.lang.reflect.Type fieldType) {
        if (fieldType instanceof java.lang.reflect.ParameterizedType pt) {
            java.lang.reflect.Type[] args = pt.getActualTypeArguments();
            if (args.length >= 1 && args[0] instanceof Class<?> c) {
                return c;
            }
        }
        return null;
    }

    private static List<?> convertElements(List<?> raw, java.lang.reflect.Type fieldType) {
        Class<?> elemClass = extractElementType(fieldType);
        if (elemClass == null || elemClass == Object.class || elemClass == String.class) {
            return raw;
        }

        // Check if any element needs conversion (e.g., JSONObject → POJO)
        boolean needsConversion = false;
        for (Object item : raw) {
            if (item != null && !elemClass.isInstance(item)) {
                needsConversion = true;
                break;
            }
        }
        if (!needsConversion) {
            return raw;
        }

        ObjectMapper mapper = ObjectMapper.shared();
        List<Object> converted = new ArrayList<>(raw.size());
        for (Object item : raw) {
            if (item == null || elemClass.isInstance(item)) {
                converted.add(item);
            } else {
                converted.add(mapper.convertValue(item, elemClass));
            }
        }
        return converted;
    }

    private static final ObjectReader<Object> IMMUTABLE_LIST_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                JSONArray arr = parser.readArray();
                if (arr == null) {
                    return null;
                }
                return toImmutableList(convertElements(arr, fieldType));
            };

    private static final ObjectReader<Object> IMMUTABLE_SET_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                JSONArray arr = parser.readArray();
                if (arr == null) {
                    return null;
                }
                return toImmutableSet(convertElements(arr, fieldType));
            };

    private static final ObjectReader<Object> IMMUTABLE_MAP_READER =
            (parser, fieldType, fieldName, features) -> {
                if (parser.readNull()) {
                    return null;
                }
                JSONObject obj = parser.readObject();
                if (obj == null) {
                    return null;
                }
                return toImmutableMap(obj);
            };
}
