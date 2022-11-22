package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue947 {
    @Test
    public void test() {
        String str = "{\"@type\":\"java.util.HashMap\",\"key1\":\"value1\",\"key2\":2,\"key3\":{\"@type\":\"java.util.HashMap\",\"k1\":123,\"k2\":12B}}";
        HashMap map = (HashMap) JSON.parseObject(str, Object.class, JSONReader.Feature.SupportAutoType);
        assertEquals(3, map.size());
        HashMap map3 = (HashMap) map.get("key3");
        assertEquals(2, map3.size());
    }
}
