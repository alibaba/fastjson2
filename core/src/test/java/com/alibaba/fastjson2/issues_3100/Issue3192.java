package com.alibaba.fastjson2.issues_3100;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3192 {
    @Test
    public void test_jsonpath_array() {
        String json = "{ \"array\": null }";
        assertEquals("[]", JSON.toJSONString(JSONPath.extract(json, "$.array[0].key", JSONPath.Feature.KeepNullValue, JSONPath.Feature.AlwaysReturnList)));
        assertEquals("[]", JSON.toJSONString(JSONPath.extract(json, "$.array[*].key", JSONPath.Feature.KeepNullValue, JSONPath.Feature.AlwaysReturnList)));

        json = "{ \"array\": [{\"key\":1}, {\"key\":null}, {\"key\":3}] }";
        assertEquals("[1]", JSON.toJSONString(JSONPath.extract(json, "$.array[0].key", JSONPath.Feature.KeepNullValue, JSONPath.Feature.AlwaysReturnList)));
        assertEquals("[1,null,3]", JSON.toJSONString(JSONPath.extract(json, "$.array[*].key", JSONPath.Feature.KeepNullValue, JSONPath.Feature.AlwaysReturnList)));
    }
}
