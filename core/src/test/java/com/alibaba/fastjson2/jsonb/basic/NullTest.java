package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NullTest {
    @Test
    public void testNull() {
        byte[] bytes = JSONB.toBytes(null);
        assertEquals(1, bytes.length);
        assertEquals(BC_NULL, bytes[0]);
    }
}
