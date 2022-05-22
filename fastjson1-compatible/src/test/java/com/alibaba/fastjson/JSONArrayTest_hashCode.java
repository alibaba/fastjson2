package com.alibaba.fastjson;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONArrayTest_hashCode {
    @Test
    public void test_hashCode() throws Exception {
        assertEquals(new JSONArray().hashCode(), new JSONArray().hashCode());
    }

    @Test
    public void test_hashCode_1() throws Exception {
        assertEquals(JSON.parseArray("[]"), JSON.parseArray("[]"));
    }
}
