package com.alibaba.fastjson2.issues_2000;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class Issue2040 {
    @Test
    public void test() {
        assertNull(JSONObject.parseObject("null"));
        assertNull(JSON.parseArray("null"));
    }
}
