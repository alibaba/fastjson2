package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue643 {
    @Test
    public void test() {
        String json = "{\"a1\": null}";
        JSONObject jsonObject = JSON.parseObject(json);

        assertTrue(
                JSONPath.contains(jsonObject, "$.a1")
        );
        assertFalse(
                JSONPath.contains(jsonObject, "$.a2")
        );
    }

    @Test
    public void test1() {
        String json = "{\"a1\":{\"a2\":null}}";
        JSONObject jsonObject = JSON.parseObject(json);

        assertTrue(
                JSONPath.contains(jsonObject, "$.a1")
        );
        assertFalse(
                JSONPath.contains(jsonObject, "$.a2")
        );
        assertTrue(
                JSONPath.contains(jsonObject, "$.a1.a2")
        );
    }

    @Test
    public void test2() {
        String json = "{\"a1\":{\"a2\":{\"a3\":null}}}";
        JSONObject jsonObject = JSON.parseObject(json);

        assertTrue(
                JSONPath.contains(jsonObject, "$.a1")
        );
        assertTrue(
                JSONPath.contains(jsonObject, "$.a1.a2")
        );
        assertTrue(
                JSONPath.contains(jsonObject, "$.a1.a2.a3")
        );
    }
}
