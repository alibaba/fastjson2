package com.alibaba.fastjson2.issues_7000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue7670 {
    @Test
    public void test() {
        String json = "{\"classes\":[{\"className\":\"yuwen\"},{\"className\":\"shuxue\"}]}";
        Object test = JSON.parse(json);
        assertTrue(JSONPath.contains(test, "$.classes[?(@.className == 'yuwen')]"));
        assertTrue(JSONPath.contains(test, "$.classes[?(@.className == 'shuxue')]"));
        assertFalse(JSONPath.contains(test, "$.classes[?(@.className == 'yingyu')]"));
    }
}
