package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue476 {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("c", "c");
        JSONPath.set(jsonObject, "$.a.b", "123");
        assertEquals("{\"c\":\"c\",\"a\":{\"b\":\"123\"}}", jsonObject.toJSONString());
    }
}
