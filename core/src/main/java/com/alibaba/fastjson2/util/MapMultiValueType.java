package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.0.34
 */
public class MapMultiValueType<T extends Map>
        implements Type {
    private final Class<T> mapType;
    private final Map<String, Type> valueTypes = new HashMap<>();

    MapMultiValueType(Class<T> mapType, String name, Type type) {
        this.mapType = mapType;
        this.valueTypes.put(name, type);
    }

    MapMultiValueType(Class<T> mapType, Map<String, Type> types) {
        this.mapType = mapType;
        this.valueTypes.putAll(types);
    }

    public Class<T> getMapType() {
        return mapType;
    }

    public Type getType(String name) {
        return this.valueTypes.get(name);
    }

    public static MapMultiValueType<JSONObject> of(String name, Type type) {
        return new MapMultiValueType<>(JSONObject.class, name, type);
    }

    public static MapMultiValueType<JSONObject> of(Map<String, Type> types) {
        return new MapMultiValueType<>(JSONObject.class, types);
    }

    public static <T extends Map> MapMultiValueType<T> of(
            Class<T> mapType,
            String name,
            Type type
    ) {
        return new MapMultiValueType<T>(mapType, name, type);
    }

    public static <T extends Map> MapMultiValueType<T> of(
            Class<T> mapType,
            Map<String, Type> types
    ) {
        return new MapMultiValueType<T>(mapType, types);
    }
}
