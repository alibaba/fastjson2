package com.alibaba.fastjson;

import com.alibaba.fastjson.parser.Feature;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LiuchanTest {
    @Test
    public void test4() {
        Object a = JSON.parseObject("{\"@type\":\"com.alibaba.fastjson.JSONObject\",\"success\":true}",
                new TypeReference<Map<String, Object>>(){}//, Feature.DisableSpecialKeyDetect
        );
        assertEquals(com.alibaba.fastjson.JSONObject.class, a.getClass());
    }

    @Test
    public void test5() {
        Map<String, Object> a = JSON.parseObject("{\"list\":[\"foo\",\"bar\"],\"success\":true}",
                new TypeReference<Map<String, Object>>(){},
                Feature.UseNativeJavaObject
        );
        assertEquals(java.util.ArrayList.class, a.get("list").getClass());
    }

    @Test
    public void test6() {
        Map<String, Object> a = JSON.parseObject("{\"map\":{\"a\":\"b\"},\"success\":true}",
                new TypeReference<Map<String, Object>>() {
                },
                Feature.UseNativeJavaObject
        );
        assertEquals(java.util.HashMap.class, a.get("map").getClass());
    }
}
