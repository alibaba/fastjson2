package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONB.Constants.BC_FALSE;
import static com.alibaba.fastjson2.JSONB.Constants.BC_TRUE;
import static org.junit.jupiter.api.Assertions.*;

public class BooleanTest {
    @Test
    public void testTrue() {
        byte[] bytes = JSONB.toBytes(true);
        assertEquals(1, bytes.length);
        assertEquals(BC_TRUE, bytes[0]);

        Boolean parsed = (Boolean) JSONB.parse(bytes);
        assertTrue(parsed.booleanValue());
    }

    @Test
    public void testFalse() {
        byte[] bytes = JSONB.toBytes(false);
        assertEquals(1, bytes.length);
        assertEquals(BC_FALSE, bytes[0]);

        Boolean parsed = (Boolean) JSONB.parse(bytes);
        assertFalse(parsed.booleanValue());
    }
}
