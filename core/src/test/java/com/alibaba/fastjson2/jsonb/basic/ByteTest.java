package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.*;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONB.Constants.BC_INT8;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ByteTest {
    @Test
    public void testByte() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            Byte value = (byte) i;
            byte[] bytes = JSONB.toBytes(value);
            assertEquals(2, bytes.length);
            assertEquals(BC_INT8, bytes[0]);
            assertEquals(value.byteValue(), bytes[1]);
        }
    }

    @Test
    public void testByteValue() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; ++i) {
            byte value = (byte) i;
            byte[] bytes = JSONB.toBytes(value);
            assertEquals(2, bytes.length);
            assertEquals(BC_INT8, bytes[0]);
            assertEquals(value, bytes[1]);
        }
    }
}
