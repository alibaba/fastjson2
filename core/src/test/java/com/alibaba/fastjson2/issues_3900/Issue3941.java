package com.alibaba.fastjson2.issues_3900;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue3941 {
    @Test
    public void test() {
        String string = "{\"test\": 897623178028414900000}";
        Map<String, Double> top = JSON.parseObject(string, new TypeReference<Map<String, Double>>() {});
        assertEquals(8.976231780284149E20, top.get("test"));
    }
}
