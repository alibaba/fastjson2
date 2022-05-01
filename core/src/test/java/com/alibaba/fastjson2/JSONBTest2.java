package com.alibaba.fastjson2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JSONBTest2 {
    @Test
    public void test_0() throws Exception {
        byte[] bytes = JSONB.toBytes(101);
        String str = JSONB.toJSONString(bytes);
        assertEquals("101", str);
    }
}
