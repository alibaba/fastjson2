package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue956 {
    @Test
    public void test() {
        Map data = new LinkedHashMap<>();
        JSONPath.set(data, "$.obj['5']", "设置成功");
        assertEquals("{\"obj\":{\"5\":\"设置成功\"}}", JSONObject.toJSONString(data));
    }

    @Test
    public void test1() {
        Map data = new LinkedHashMap<>();
        JSONPath.set(data, "$.obj.5", "设置成功");
        assertEquals("{\"obj\":{\"5\":\"设置成功\"}}", JSONObject.toJSONString(data));
    }
}
