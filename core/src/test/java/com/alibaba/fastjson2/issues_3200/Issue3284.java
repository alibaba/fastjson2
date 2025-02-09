package com.alibaba.fastjson2.issues_3200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class Issue3284 {
    @Test
    public void test() {
        String str = "{\"endDataTime\":\"2024\",\"@type\":\"java.util.HashMap\",\"startDataTime\":\"2023\"}";
        HashMap hashMap = JSON.parseObject(str, HashMap.class, JSONReader.Feature.SupportAutoType);
        assertFalse(hashMap.containsKey("@type"));
    }
}
