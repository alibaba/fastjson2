package com.alibaba.fastjson2.issues_2500;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2584 {
    @Test
    public void testMutant1() {
        JSONObject obj = JSONObject.of("data", JSONArray.of(1));
        String str = JSON.toJSONString(obj);
        assertEquals(JSONPath.eval(str, "$.data[0][0]"), JSONPath.eval(obj, "$.data[0][0]"));
    }

    @Test
    public void testMutant2() {
        JSONObject obj = JSONObject.of("data", JSONArray.of(1, 2));
        String str = JSON.toJSONString(obj);
        assertEquals(JSONPath.eval(str, "$.data[0][0]"), JSONPath.eval(obj, "$.data[0][0]"));
    }

    @Test
    public void testMutant3() {
        JSONObject obj = JSONObject.of("data", JSONArray.of(JSONObject.of("id", "1")));
        String str = JSON.toJSONString(obj);
        assertEquals(JSONPath.eval(str, "$.data[0][0]"), JSONPath.eval(obj, "$.data[0][0]"));
    }
}
