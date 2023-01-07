package com.alibaba.fastjson2.jsonb.basic;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import static com.alibaba.fastjson2.JSONB.Constants.BC_INT16;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShortTest {
    @Test
    public void test() {
        short[] shorts = new short[]{
                0, 1, -1, 10, -10, 100, -100, 200, -200, 500, -500, 1000, -1000,
                Short.MAX_VALUE,
                Short.MAX_VALUE
        };

        for (short shortValue : shorts) {
            Short value = shortValue;
            byte[] bytes = JSONB.toBytes(value);
            assertEquals(3, bytes.length);
            assertEquals(BC_INT16, bytes[0]);

            byte b1 = bytes[1];
            byte b2 = bytes[2];
            int int16Value = ((b2 & 0xFF) +
                    (b1 << 8));
            assertEquals(value.shortValue(), int16Value);

            Short parsed = (Short) JSONB.parse(bytes);
            assertEquals(value, parsed);
        }
    }

    @Test
    public void testValue() {
        short[] shorts = new short[]{
                0, 1, -1, 10, -10, 100, -100, 200, -200, 500, -500, 1000, -1000,
                Short.MAX_VALUE,
                Short.MAX_VALUE
        };

        for (short value : shorts) {
            byte[] bytes = JSONB.toBytes(value);
            assertEquals(3, bytes.length);
            assertEquals(BC_INT16, bytes[0]);

            byte b1 = bytes[1];
            byte b2 = bytes[2];
            int int16Value = ((b2 & 0xFF) +
                    (b1 << 8));
            assertEquals(value, int16Value);

            Short parsed = (Short) JSONB.parse(bytes);
            assertEquals(value, parsed);
        }
    }
}
