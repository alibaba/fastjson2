package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.alibaba.fastjson2.JSONReader;

import java.lang.reflect.Type;
import java.util.Map;

public class JSONPath {
    private final com.alibaba.fastjson2.JSONPath path;

    private JSONPath(com.alibaba.fastjson2.JSONPath path) {
        this.path = path;
    }

    public static JSONPath compile(String path) {
        if (path == null) {
            throw new JSONException("jsonpath can not be null");
        }

        return new JSONPath(com.alibaba.fastjson2.JSONPath.of(path));
    }

    public Object eval(Object object) {
        return path.eval(object);
    }

    public boolean set(Object object, Object value) {
        path.set(object, value);
        return true;
    }

    public String getPath() {
        return path.toString();
    }

    public static <T> T read(String json, String path, Type clazz, ParserConfig parserConfig) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        Object r = jsonPath.extract(JSONReader.of(json));
        return TypeUtils.cast(r, clazz, parserConfig);
    }

    public static <T> T read(String json, String path, Type clazz) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        Object r = jsonPath.extract(JSONReader.of(json));
        return TypeUtils.cast(r, clazz, ParserConfig.global);
    }

    public static Object eval(Object rootObject, String path) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        return jsonPath.eval(rootObject);
    }

    public static boolean set(Object rootObject, String path, Object value) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        jsonPath.set(rootObject, value);
        return true;
    }

    public static Map<String, Object> paths(Object javaObject) {
        return com.alibaba.fastjson2.JSONPath.paths(javaObject);
    }

    public static void arrayAdd(Object rootObject, String path, Object... values) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        jsonPath.arrayAdd(rootObject, values);
    }

    public static Object extract(String json, String path) {
        return com.alibaba.fastjson2.JSONPath
                .of(path)
                .extract(
                        JSONReader.of(json));
    }

    public static boolean remove(Object root, String path) {
        return com.alibaba.fastjson2.JSONPath
                .of(path)
                .remove(root);
    }

    public static boolean contains(Object rootObject, String path) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        return jsonPath.contains(rootObject);
    }

    public static Object read(String json, String path) {
        return com.alibaba.fastjson2.JSONPath.extract(json, path);
    }
}
