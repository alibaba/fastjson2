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
        JSONReader.Context context = JSON.createReadContext(JSON.DEFAULT_PARSER_FEATURE);
        JSONReader jsonReader = JSONReader.of(json, context);
        Object r = jsonPath.extract(jsonReader);
        return TypeUtils.cast(r, clazz, parserConfig);
    }

    public static <T> T read(String json, String path, Type clazz) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        Object r = jsonPath.extract(JSONReader.of(json));
        return TypeUtils.cast(r, clazz, ParserConfig.global);
    }

    public static Object eval(String rootObject, String path) {
        Object result = com.alibaba.fastjson2.JSONPath.eval(rootObject, path);
        return adaptResult(result);
    }

    public static Object eval(Object rootObject, String path) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        Object result = jsonPath.eval(rootObject);
        return adaptResult(result);
    }

    private static Object adaptResult(Object result) {
        if (result instanceof com.alibaba.fastjson2.JSONArray) {
            result = new JSONArray((com.alibaba.fastjson2.JSONArray) result);
        } else if (result instanceof com.alibaba.fastjson2.JSONObject) {
            result = new JSONObject((com.alibaba.fastjson2.JSONObject) result);
        }
        return result;
    }

    public static boolean set(Object rootObject, String path, Object value) {
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        jsonPath.setReaderContext(JSON.createReadContext(JSON.DEFAULT_PARSER_FEATURE));
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
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        JSONReader.Context context = JSON.createReadContext(JSON.DEFAULT_PARSER_FEATURE);
        JSONReader jsonReader = JSONReader.of(json, context);
        Object result = jsonPath.extract(jsonReader);
        return adaptResult(result);
    }

    public static boolean remove(Object root, String path) {
        return com.alibaba.fastjson2.JSONPath
                .of(path)
                .remove(root);
    }

    public static boolean contains(Object rootObject, String path) {
        if (rootObject == null) {
            return false;
        }
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        return jsonPath.contains(rootObject);
    }

    public static Object read(String json, String path) {
        JSONReader.Context context = JSON.createReadContext(JSON.DEFAULT_PARSER_FEATURE);
        JSONReader jsonReader = JSONReader.of(json, context);
        com.alibaba.fastjson2.JSONPath jsonPath = com.alibaba.fastjson2.JSONPath.of(path);
        return jsonPath.extract(jsonReader);
    }
}
