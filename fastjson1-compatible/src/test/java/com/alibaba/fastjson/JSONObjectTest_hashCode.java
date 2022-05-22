package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONObjectTest_hashCode {
    @Test
    public void test_hashCode() throws Exception {
        assertEquals(new JSONObject().hashCode(), new JSONObject().hashCode());
    }

    @Test
    public void test_hashCode_1() throws Exception {
        assertEquals(JSON.parseObject("{a:1}"), JSON.parseObject("{'a':1}"));
    }
}
