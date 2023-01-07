package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.alibaba.fastjson2.JSONB.Constants.BC_BINARY;
import static com.alibaba.fastjson2.JSONB.Constants.BC_INT32_NUM_0;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryTest {
    @Test
    public void test() {
        byte[] bytes = JSONB.toBytes(new byte[0]);
        assertEquals(2, bytes.length);
        assertEquals(BC_BINARY, bytes[0]);
        assertEquals(BC_INT32_NUM_0, bytes[1]);

        byte[] parsed = (byte[]) JSONB.parse(bytes);
        assertEquals(0, parsed.length);
    }

    @Test
    public void testN() {
        for (int i = 0; i <= 47; i++) {
            byte[] value = new byte[i];
            Arrays.fill(value, (byte) 1);

            byte[] bytes = JSONB.toBytes(value);
            assertEquals(2 + i, bytes.length);
            assertEquals(BC_BINARY, bytes[0]);
            assertEquals(i, bytes[1]); // length

            byte[] parsed = (byte[]) JSONB.parse(bytes);
            assertArrayEquals(value, parsed);
        }
    }
}
