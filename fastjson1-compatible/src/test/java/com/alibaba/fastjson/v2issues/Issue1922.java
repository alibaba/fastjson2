package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Issue1922 {
    @Test
    public void test() {
        JSONObject jsonObject = new JSONObject();
        JSONPath.set(
                jsonObject, "$a.d",
                new JSONObject().fluentPut("cc", 1));
        assertEquals(
                JSONObject.class,
                jsonObject.get("a").getClass());
    }
}
