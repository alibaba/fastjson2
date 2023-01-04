package com.alibaba.fastjson2.jsonb;

import com.alibaba.fastjson2.JSONB;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoubleTest {
    @Test
    public void testInt() {
        Random r = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            long intValue = r.nextLong();
            double doubleValue = (double) intValue;
            byte[] jsonbBytes = JSONB.toBytes(doubleValue);
            Double parsed = (Double) JSONB.parse(jsonbBytes);
            assertEquals(parsed.doubleValue(), doubleValue);
        }
    }

    @Test
    public void testFloat() {
        Random r = new Random();
        for (int i = 0; i < 1_000_000; i++) {
            double doubleValue = r.nextDouble();
            byte[] jsonbBytes = JSONB.toBytes(doubleValue);
            Double parsed = (Double) JSONB.parse(jsonbBytes);
            assertEquals(parsed.doubleValue(), doubleValue);
        }
    }
}
