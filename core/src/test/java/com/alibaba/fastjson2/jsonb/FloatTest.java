package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FloatTest {
    @Test
    public void testInt() {
        Random r = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            int intValue = r.nextInt();
            float floatValue = (float) intValue;
            byte[] jsonbBytes = JSONB.toBytes(floatValue);
            Float parsed = (Float) JSONB.parse(jsonbBytes);
            assertEquals(parsed.floatValue(), floatValue);
        }
    }

    @Test
    public void testFloat() {
        Random r = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            float floatValue = r.nextFloat();
            byte[] jsonbBytes = JSONB.toBytes(floatValue);
            Float parsed = (Float) JSONB.parse(jsonbBytes);
            assertEquals(parsed.floatValue(), floatValue);
        }
    }
}
