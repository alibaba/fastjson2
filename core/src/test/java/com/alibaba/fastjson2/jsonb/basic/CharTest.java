package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONB.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CharTest {
    @Test
    public void testCharInt32Num() {
        for (int i = 0; i <= 47; ++i) {
            Character ch = (char) i;
            byte[] bytes = JSONB.toBytes(ch);
            assertEquals(2, bytes.length);
            assertEquals(BC_CHAR, bytes[0]);
            assertEquals(ch.charValue(), bytes[1]);

            Character parsed = (Character) JSONB.parse(bytes);
            assertEquals(ch, parsed);
        }
    }

    @Test
    public void testCharInt32Byte() {
        for (int i = 48; i <= INT64_BYTE_MAX; ++i) {
            Character ch = (char) i;
            byte[] bytes = JSONB.toBytes(ch);
            assertEquals(3, bytes.length);
            assertEquals(BC_CHAR, bytes[0]);

            byte b1 = bytes[1];
            byte b2 = bytes[2];

            int value = ((b1 - BC_INT32_BYTE_ZERO) << 8) + (b2 & 0xFF);
            assertEquals(ch.charValue(), value);

            Character parsed = (Character) JSONB.parse(bytes);
            assertEquals(ch, parsed);
        }
    }
}
