package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue431 {
    @Test
    public void test() {
        String jsonStr = "{\"aaa\":\"a111\",\"bbb\":\"b11111\"}";
        JSONObject jsonObject = JSON.parseObject(jsonStr);

        String valueStr = "{\"ccc\":\"c111\",\"ddd\":\"d11111\"}";
        JSONObject value = JSON.parseObject(valueStr);

        JSONPath.set(jsonObject, "$.aaa", value);
        assertEquals("{\"aaa\":{\"ccc\":\"c111\",\"ddd\":\"d11111\"},\"bbb\":\"b11111\"}", jsonObject.toString());
    }

    @Test
    public void test1() {
        JSONObject object = JSONObject.of("id", 123);
        JSONPath path = JSONPath.of("$.id");

        path.set(object, 101);
        assertEquals(101, object.get("id"));

        path.set(object, 102);
        assertEquals(102, object.get("id"));

        path.set(object, 103, JSONReader.Feature.DuplicateKeyValueAsArray);
        assertEquals("[102,103]", object.get("id").toString());
    }
}
