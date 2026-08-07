package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue530 {
    @Test
    public void test() {
        String str = "{\"@type\":\"java.net.Inet4Address\",\"val\":\"dnslog\"}";
        assertEquals(JSONObject.class, JSON.parse(str).getClass());
    }
}
