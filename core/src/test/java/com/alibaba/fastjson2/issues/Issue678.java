package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue678 {
    @Test
    public void test() {
        String STR = "中国";
        String JSON_STR = "\"\\u4e2d\\u56fd\"";
        String json = JSON.toJSONString(STR, JSONWriter.Feature.EscapeNoneAscii);
        assertEquals(JSON_STR, json);
        assertEquals(STR, JSON.parse(json));
    }
}
