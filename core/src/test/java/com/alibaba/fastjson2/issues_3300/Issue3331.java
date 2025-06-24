package com.alibaba.fastjson2.issues_3300;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3331 {
    @Test
    public void test() {
        String json = "[{\"priority_flag\":9,\"service_type\":\"bbb\",\"destination_addr\":\"bbb\"}]";
        Object object1 = com.alibaba.fastjson.JSON.parse(json);
        Object object2 = com.alibaba.fastjson2.JSON.parse(json);
        String path = "$.service_type";
        assertEquals(com.alibaba.fastjson.JSONPath.contains(object1, path), com.alibaba.fastjson2.JSONPath.contains(object2, path));
        path = "$[0].service_type";
        assertEquals(com.alibaba.fastjson.JSONPath.contains(object1, path), com.alibaba.fastjson2.JSONPath.contains(object2, path));
    }
}
