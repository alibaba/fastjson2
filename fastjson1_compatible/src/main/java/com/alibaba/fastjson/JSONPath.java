package com.alibaba.fastjson;

import com.alibaba.fastjson2.JSONReader;

import java.util.Map;

public class JSONPath {
    public static Object eval(Object rootObject, String path) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        return jsonPath.eval(rootObject);
    }

    public boolean set(Object rootObject, Object value) {
        return set(rootObject, value, true);
    }

    public boolean set(Object rootObject, Object value, boolean p) {
        throw new JSONException("TODO"); // TODO : JSONPath.set
    }

    public static boolean set(Object rootObject, String path, Object value) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        jsonPath.set(rootObject, value);
        return true;
    }

    public static Map<String, Object> paths(Object javaObject) {
        return com.alibaba.fastjson2.JSONPath.paths(javaObject);
    }
//
//    public boolean contains(Object rootObject) {
//        throw new JSONException("TODO");
//    }

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
}
