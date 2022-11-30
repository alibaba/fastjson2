package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue842 {
    @Test
    public void test() {
        String str = "{\"items\": [{\"data\":{\"0\":\"abc\"}}]}";
        Object result = JSONPath.eval(str, "$.items[0].data.0");
        assertEquals("abc", result);
    }

    @Test
    public void test1() {
        String jsonPath = "$.items[0].data.0";

        Map data = new LinkedHashMap<>();
        JSONPath.set(data, jsonPath, "设置成功");
        assertEquals("{\"items\":[{\"data\":{\"0\":\"设置成功\"}}]}", JSON.toJSONString(data));
    }

    @Test
    public void test1a() {
        String jsonPath = "$.items[0].data[0]";

        Map data = new LinkedHashMap<>();
        JSONPath.set(data, jsonPath, "设置成功");
        assertEquals("{\"items\":[{\"data\":[\"设置成功\"]}]}", JSON.toJSONString(data));
    }

    @Test
    public void test2() {
        Map data = new LinkedHashMap<>();
        JSONPath.set(data, "$.obj.5", "设置成功");
        assertEquals("{\"obj\":{\"5\":\"设置成功\"}}", JSONObject.toJSONString(data));
    }

    @Test
    public void test2a() {
        Map data = new LinkedHashMap<>();
        JSONPath.set(data, "$.obj[5]", "设置成功");
        assertEquals("{\"obj\":[null,null,null,null,null,\"设置成功\"]}", JSONObject.toJSONString(data));
    }

    @Test
    public void test3() {
        Map data = new LinkedHashMap<>();
        JSONPath.set(data, "$.obj.2.5", "设置成功");
        assertEquals("{\"obj\":{\"2\":{\"5\":\"设置成功\"}}}", JSONObject.toJSONString(data));
    }

    @Test
    public void test3a() {
        Map data = new LinkedHashMap<>();
        JSONPath.set(data, "$.obj[2][5]", "设置成功");
        assertEquals("{\"obj\":[null,null,[null,null,null,null,null,\"设置成功\"]]}", JSONObject.toJSONString(data));
    }
}
