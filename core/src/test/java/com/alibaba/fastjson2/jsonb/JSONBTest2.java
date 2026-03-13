package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("jsonb")
public class JSONBTest2 {
    @Test
    public void test_0() throws Exception {
        byte[] bytes = JSONB.toBytes(101);
        String str = JSONB.toJSONString(bytes);
        assertEquals("101", str);
    }
}
