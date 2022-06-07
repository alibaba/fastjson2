package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue424 {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject();
        JSONPath.set(jsonObject, "a[0].b", 1);
        assertEquals("{\"a\":[{\"b\":1}]}", jsonObject.toString());
    }
}
