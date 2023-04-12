package com.alibaba.fastjson2.issues_1000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1350 {
    @Test
    public void test() {
        Map map = new HashMap<>();
        map.put(Long.MIN_VALUE, Long.MIN_VALUE);
        String str = JSON.toJSONString(map, JSONWriter.Feature.BrowserCompatible);
        assertEquals("{\"-9223372036854775808\":\"-9223372036854775808\"}", str);
    }

    @Test
    public void test1() {
        Map map = new HashMap<>();
        map.put(Long.MAX_VALUE, Long.MAX_VALUE);
        String str = JSON.toJSONString(map, JSONWriter.Feature.BrowserCompatible);
        assertEquals("{\"9223372036854775807\":\"9223372036854775807\"}", str);
    }
}
