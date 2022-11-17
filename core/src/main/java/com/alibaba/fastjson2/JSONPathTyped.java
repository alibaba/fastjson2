package com.alibaba.fastjson2;

import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.function.BiFunction;

class JSONPathTyped
        extends JSONPath {
    final JSONPath jsonPath;
    final Type type;
    protected JSONPathTyped(JSONPath jsonPath, Type type) {
        super(jsonPath.path, jsonPath.features);
        this.type = type;
        this.jsonPath = jsonPath;
    }
    @Override
    public boolean isRef() {
        return jsonPath.isRef();
    }

    @Override
    public boolean contains(Object object) {
        return jsonPath.contains(object);
    }

    @Override
    public Object eval(Object object) {
        Object result = jsonPath.eval(object);
        return TypeUtils.cast(result, type);
    }

    @Override
    public Object extract(JSONReader jsonReader) {
        Object result = jsonPath.extract(jsonReader);
        return TypeUtils.cast(result, type);
    }

    @Override
    public String extractScalar(JSONReader jsonReader) {
        return jsonPath.extractScalar(jsonReader);
    }

    @Override
    public void set(Object object, Object value) {
        jsonPath.set(object, value);
    }

    @Override
    public void set(Object object, Object value, JSONReader.Feature... readerFeatures) {
        jsonPath.set(object, value, readerFeatures);
    }

    @Override
    public void setCallback(Object object, BiFunction callback) {
        jsonPath.setCallback(object, callback);
    }

    @Override
    public void setInt(Object object, int value) {
        jsonPath.setInt(object, value);
    }

    @Override
    public void setLong(Object object, long value) {
        jsonPath.setLong(object, value);
    }

    @Override
    public boolean remove(Object object) {
        return jsonPath.remove(object);
    }

    public Type getType() {
        return type;
    }

    public static JSONPath of(JSONPath jsonPath, Type type) {
        if (type == null || type == Object.class) {
            return jsonPath;
        }

        if (jsonPath instanceof JSONPathTyped) {
            JSONPathTyped jsonPathTyped = (JSONPathTyped) jsonPath;
            if (jsonPathTyped.type.equals(type)) {
                return jsonPath;
            }
        }

        if (jsonPath instanceof JSONPathSingleName) {
            if (type == Integer.class) {
                return new JSONPathSingleNameInteger((JSONPathSingleName) jsonPath);
            }

            if (type == Long.class) {
                return new JSONPathSingleNameLong((JSONPathSingleName) jsonPath);
            }

            if (type == String.class) {
                return new JSONPathSingleNameString((JSONPathSingleName) jsonPath);
            }

            if (type == BigDecimal.class) {
                return new JSONPathSingleNameDecimal((JSONPathSingleName) jsonPath);
            }
        }

        return new JSONPathTyped(jsonPath, type);
    }
}
