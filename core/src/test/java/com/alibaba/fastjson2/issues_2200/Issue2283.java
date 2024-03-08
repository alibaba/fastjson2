package com.alibaba.fastjson2.issues_2200;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue2283 {
    @Test
    public void test() {
        BigDecimal num = new BigDecimal("0.123456789012345678");
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("num", num);
        String expected = "{\"num\":\"0.123456789012345678\"}";
        assertEquals(expected, jsonObj.toString(JSONWriter.Feature.BrowserCompatible));
        assertEquals(expected, new String(JSON.toJSONBytes(jsonObj, JSONWriter.Feature.BrowserCompatible)));
    }
}
