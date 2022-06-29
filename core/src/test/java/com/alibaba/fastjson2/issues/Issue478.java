package com.alibaba.fastjson2.issues;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue478 {
    @Test
    public void test() {
        JSONObject object = JSON.parseObject("{a: \"test\"}", JSONReader.Feature.AllowUnQuotedFieldNames);
        assertEquals("test", object.get("a"));
    }
}
