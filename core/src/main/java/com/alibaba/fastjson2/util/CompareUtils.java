package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.*;

/**
 * 两个json比较
 * <p>
 * 实现思路：
 * <p>
 * 1，把json按照json path平铺展开
 * <p>
 * 2，对平铺展开的json进行对比
 * <p>
 * 3，返回对比接口
 * <p>
 * 两个主要接口:
 * <p>
 * compare: 返回所有结果
 * <p>
 * diff: 返回差异结果
 *
 * @author yanchangyou
 */
public class CompareUtils {
    public static final String DIFF_TYPE_OF_MODIFY = "MODIFY";
    public static final String DIFF_TYPE_OF_ADD = "ADD";
    public static final String DIFF_TYPE_OF_REMOVE = "REMOVE";

    public static final String FIELD_NAME_OF_VALUE_EQUAL = "valueEqual";
    public static final String FIELD_NAME_OF_TYPE_EQUAL = "typeEqual";
    public static final String FIELD_NAME_OF_DIFF_TYPE = "diffType";

    /**
     * 总结比较结果
     *
     * @param list compare result list
     * @return summary
     */
    public static JSONObject sum(JSONArray list) {
        JSONObject result = new JSONObject();
        boolean equal = true;
        int modifyCount = 0;
        int addCount = 0;
        int removeCount = 0;
        int valueEqualCount = 0;
        int typeEqualCount = 0;

        for (Object itemObject : list) {
            JSONObject item = (JSONObject) itemObject;
            if (Boolean.FALSE.equals(item.get(FIELD_NAME_OF_VALUE_EQUAL))) {
                equal = false;
            } else {
                valueEqualCount++;
            }
            if (Boolean.TRUE.equals(item.get(FIELD_NAME_OF_TYPE_EQUAL))) {
                typeEqualCount++;
            }
            String diffType = item.getString(FIELD_NAME_OF_DIFF_TYPE);
            if (diffType != null) {
                switch (diffType) {
                    case DIFF_TYPE_OF_MODIFY:
                        modifyCount++;
                        break;
                    case DIFF_TYPE_OF_ADD:
                        addCount++;
                        break;
                    case DIFF_TYPE_OF_REMOVE:
                        removeCount++;
                }
            }
        }
        result.put("equal", equal);
        result.put("total", addCount + removeCount + modifyCount + valueEqualCount);
        result.put("valueEqualCount", valueEqualCount);
        result.put("typeEqualCount", typeEqualCount);
        result.put("diffCount", addCount + removeCount + modifyCount);
        result.put("addCount", addCount);
        result.put("removeCount", removeCount);
        result.put("modifyCount", modifyCount);

        return result;
    }

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

    public static JSONArray diffToArray(JSONObject json1, JSONObject json2) {
        JSONObject diffJson = diff(json1, json2);
        JSONArray array = new JSONArray(diffJson.size());
        array.addAll(diffJson.values());
        return array;
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

    public static JSONArray compareToArray(JSONObject json1, JSONObject json2) {
        JSONObject diffJson = compare(json1, json2);
        JSONArray array = new JSONArray(diffJson.size());
        array.addAll(diffJson.values());
        return array;
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

        Set<String> jsonPathSet = new HashSet<>(jsonPath1.keySet());
        jsonPathSet.addAll(jsonPath2.keySet());

        JSONObject result = new JSONObject(jsonPathSet.size());

        Set<String> ignorePathPrefixList = new HashSet<>();
        outer:
        for (String path : jsonPathSet) {
            boolean json1Contain = jsonPath1.containsKey(path);
            boolean json2Contain = jsonPath2.containsKey(path);

            JSONObject pathResult = new JSONObject(8);
            pathResult.put("path", path);

            for (String ignorePathPrefix : ignorePathPrefixList) {
                if (path.startsWith(ignorePathPrefix)) {
                    continue outer;
                }
            }
            if (json1Contain && !json2Contain) {
                pathResult.put(FIELD_NAME_OF_VALUE_EQUAL, false);
                pathResult.put(FIELD_NAME_OF_DIFF_TYPE, DIFF_TYPE_OF_REMOVE);
                Object value1 = json1.getByPath(path);
                if (value1 instanceof JSONObject || value1 instanceof JSONArray) {
                    ignorePathPrefixList.add(path);
                }
                pathResult.put("value1", value1);
            } else if (!json1Contain && json2Contain) {
                pathResult.put(FIELD_NAME_OF_VALUE_EQUAL, false);
                pathResult.put(FIELD_NAME_OF_DIFF_TYPE, DIFF_TYPE_OF_ADD);
                Object value2 = json2.getByPath(path);
                pathResult.put("value2", value2);
                if (value2 instanceof JSONObject || value2 instanceof JSONArray) {
                    ignorePathPrefixList.add(path);
                }
            } else if (json1Contain) {
                Object value1 = json1.getByPath(path);
                Object value2 = json2.getByPath(path);

                if ((value1 instanceof JSONObject && value2 instanceof JSONObject) || (value2 instanceof JSONArray && value1 instanceof JSONArray)) {
                    continue;
                }
                if ((value1 instanceof JSONObject && value2 instanceof JSONArray) || (value2 instanceof JSONObject && value1 instanceof JSONArray)) {
                    pathResult.put(FIELD_NAME_OF_VALUE_EQUAL, false);
                    pathResult.put(FIELD_NAME_OF_TYPE_EQUAL, false);
                    pathResult.put(FIELD_NAME_OF_DIFF_TYPE, DIFF_TYPE_OF_MODIFY);
                    pathResult.put("value1", value1);
                    pathResult.put("value2", value2);
                    ignorePathPrefixList.add(path);
                } else {
                    pathResult.putAll(compareValue(value1, value2));
                    if (Boolean.FALSE.equals(pathResult.get(FIELD_NAME_OF_VALUE_EQUAL))) {
                        pathResult.put("value1", value1);
                        pathResult.put("value2", value2);
                    }
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
    public static JSONObject convertWithJsonPath(JSONObject json) {
        if (json == null) {
            return new JSONObject(0);
        }
        JSONObject newMap = new JSONObject(json.size() + 4);
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
                newMap.put((fieldName), value);

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
                newMap.put(fieldName, value);
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
        JSONObject result = new JSONObject(4);

        boolean equal = Objects.equals(value1, value2);
        result.put(FIELD_NAME_OF_VALUE_EQUAL, equal);

        if (!equal) {
            if (value1 != null && value2 != null) {
                boolean typeEqual = (value1.getClass().equals(value2.getClass()));
                result.put(FIELD_NAME_OF_TYPE_EQUAL, typeEqual);
            } else {
                result.put(FIELD_NAME_OF_TYPE_EQUAL, false);
            }
            result.put(FIELD_NAME_OF_DIFF_TYPE, DIFF_TYPE_OF_MODIFY);
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
            if (Boolean.TRUE.equals(((JSONObject) entry.getValue()).get(FIELD_NAME_OF_VALUE_EQUAL))) {
                equalPathList.add(entry.getKey());
            }
        }
        return equalPathList;
    }

    /**
     * 处理特殊key，key中有点号，然后冲突
     *
     * @param key field key
     * @return norm key
     */
    static String buildJsonPathKey(String key) {
        if (key.contains("[")) {
            key = key.replace("[", "").replace("]", "");
        }
        if (key.contains(".") || key.contains("-")) {
            return "['".concat(key).concat("']");
        }
        return key;
    }
}
