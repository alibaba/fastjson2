package com.alibaba.fastjson.v2issues;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue2040 {
    @Test
    public void test() {
        assertNull(JSONObject.parseObject("null"));
        assertNull(JSON.parseArray("null"));
    }
}
