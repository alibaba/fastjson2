package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 两个json比较<br/>
 * 实现思路：<br/>
 * 1，把json按照json path平铺展开<br/>
 * 2，对平铺展开的json进行对比<br/>
 * 3，返回对比接口<br/>
 * <p>
 * compare: 返回所有结果<br/>
 * diff: 返回差异结果<br/>
 * <p>
 * * @author yanchangyou
 */
public class CompareUtils {
    public static final String DIFF_TYPE_OF_MODIFY = "MODIFY";
    public static final String DIFF_TYPE_OF_ADD = "ADD";
    public static final String DIFF_TYPE_OF_REMOVE = "REMOVE";

    /**
     * 比较json是否相同
     *
     * @param json1 json1
     * @param json2 json2
     * @return result
     */
    public static boolean equals(JSONObject json1, JSONObject json2) {
        JSONObject result = diff(json1, json2);
        return result.isEmpty();
    }

    /**
     * 比较json，只保留差异部分
     *
     * @param json1 json1
     * @param json2 json2
     * @return result
     */
    public static JSONObject diff(JSONObject json1, JSONObject json2) {
        JSONObject result = compare(json1, json2);
        List<String> equalPathList = getEqualsJSONPathList(result);
        for (String path : equalPathList) {
            result.remove(path);
        }
        return result;
    }

    /**
     * 比较json
     *
     * @param json1 json1
     * @param json2 json2
     * @return result
     */
    public static JSONObject compare(JSONObject json1, JSONObject json2) {
        JSONObject jsonPath1 = convertWithJsonPath(json1);
        JSONObject jsonPath2 = convertWithJsonPath(json2);

        JSONObject result = new JSONObject();
        List<String> jsonPathList = new ArrayList<>();

        jsonPathList.addAll(jsonPath1.keySet());
        for (String path : jsonPath2.keySet()) {
            if (!jsonPathList.contains(path)) {
                jsonPathList.add(path);
            }
        }

        for (String path : jsonPathList) {
            boolean json1Contain = jsonPath1.containsKey(path);
            boolean json2Contain = jsonPath2.containsKey(path);

            JSONObject pathResult = new JSONObject();
            if (json1Contain && !json2Contain) {
                pathResult.put("equal", false);
                pathResult.put("type", DIFF_TYPE_OF_REMOVE);
            } else if (!json1Contain && json2Contain) {
                pathResult.put("equal", false);
                pathResult.put("type", DIFF_TYPE_OF_ADD);
            } else if (json1Contain && json2Contain) {
                Object value1 = json1.getByPath(path);
                Object value2 = json2.getByPath(path);
                pathResult = compareValue(value1, value2);
                if (Boolean.FALSE.equals(pathResult.get("equal"))) {
                    pathResult.put("type", DIFF_TYPE_OF_MODIFY);
                }
            }

            result.put(path, pathResult);
        }

        return result;
    }

    /**
     * 按照json path平铺展开
     *
     * @param json 入参json
     * @return 返回 json object
     */
    private static JSONObject convertWithJsonPath(JSONObject json) {
        JSONObject newMap = new JSONObject();
        if (json == null) {
            return newMap;
        }
        for (Map.Entry<String, Object> field : json.entrySet()) {
            Object value = field.getValue();
            String fieldName = buildJsonPathKey((field.getKey()));
            if (value instanceof JSONObject || value instanceof JSONArray) {
                JSONArray array;
                boolean isArray = true;
                if (value instanceof JSONObject) {
                    array = new JSONArray(1);
                    array.add(value);
                    isArray = false;
                } else {
                    array = (JSONArray) value;
                }
                int index = 0;
                for (Object item : array) {
                    String arrayFieldName = fieldName.concat("[".concat(index + "").concat("]"));
                    if (item instanceof JSONObject) {
                        JSONObject subMap = convertWithJsonPath((JSONObject) item);
                        for (Map.Entry<String, Object> entry : subMap.entrySet()) {
                            String newKey = entry.getKey();
                            if (!newKey.startsWith("[")) {
                                newKey = ".".concat(newKey);
                            }
                            String newFieldName = isArray ? arrayFieldName.concat(newKey) : fieldName.concat(newKey);
                            newMap.put((newFieldName), entry.getValue());
                        }
                    } else {
                        newMap.put((arrayFieldName), item);
                    }
                    index++;
                }
            } else {
                newMap.put((fieldName), value);
            }
        }
        return newMap;
    }

    /**
     * 比较值
     *
     * @param value1 value1
     * @param value2 value2
     * @return result
     */
    private static JSONObject compareValue(Object value1, Object value2) {
        JSONObject result = new JSONObject();

        boolean equal = Objects.equals(value1, value2);
        result.put("equal", equal);

        if (!equal) {
            if (value1 != null && value2 != null) {
                boolean typeEqual = (value1.getClass().equals(value2.getClass()));
                result.put("typeEqual", typeEqual);
            } else {
                result.put("typeEqual", false);
            }
        }

        return result;
    }

    /**
     * 获取差异的json path列表
     *
     * @param jsonCompareResult json比较结果
     * @return json path列表
     */
    private static List<String> getEqualsJSONPathList(JSONObject jsonCompareResult) {
        List<String> equalPathList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : jsonCompareResult.entrySet()) {
            if (Boolean.TRUE.equals(((JSONObject) entry.getValue()).get("equal"))) {
                equalPathList.add(entry.getKey());
            }
        }
        return equalPathList;
    }

    /**
     * 处理特殊key，key中有点号，然后冲突
     *
     * @param key
     * @return
     */
    static String buildJsonPathKey(String key) {
        //TODO 避免json path解析报错，key包含特殊字符
        if (key.contains("[")) {
            key = key.replace("[", "").replace("]", "");
        }
        if (key.contains(".") || key.contains("-")) {
            return "['".concat(key).concat("']");
        }
        return key;
    }
}
