package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue518 {
    @Test
    public void test() {
        JSONObject object = JSON.parseObject("{1:101}");
        assertTrue(object.containsKey(1));
        assertEquals(101, object.get(1));
    }
}
